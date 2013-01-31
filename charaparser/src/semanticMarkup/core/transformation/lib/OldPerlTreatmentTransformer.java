package semanticMarkup.core.transformation.lib;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.ling.Token;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkerChain;
import semanticMarkup.ling.extract.IDescriptionExtractor;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParser;
import semanticMarkup.ling.pos.IPOSTagger;
import semanticMarkup.ling.transform.ITokenizer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class OldPerlTreatmentTransformer extends MarkupDescriptionTreatmentTransformer {

	private IParser parser;
	private IPOSTagger posTagger;
	private IDescriptionExtractor descriptionExtractor;
	private INormalizer normalizer;
	private ITerminologyLearner terminologyLearner;
	private ITokenizer wordTokenizer;
	private ChunkerChain chunkerChain;
	
	@Inject
	public OldPerlTreatmentTransformer(
			@Named("WordTokenizer")ITokenizer wordTokenizer, 
			IParser parser,
			@Named("ChunkerChain")ChunkerChain chunkerChain,
			IPOSTagger posTagger, 
			IDescriptionExtractor descriptionExtractor, 
			INormalizer normalizer,
			ITerminologyLearner terminologyLearner
			) throws Exception {
		super();
		this.parser = parser;
		this.posTagger = posTagger;
		this.chunkerChain = chunkerChain;
		this.descriptionExtractor = descriptionExtractor;
		this.normalizer = normalizer;
		this.terminologyLearner = terminologyLearner;
		this.wordTokenizer = wordTokenizer;
	}

	public List<Treatment> transform(List<Treatment> treatments) {
		terminologyLearner.learn(treatments);
		Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker = terminologyLearner.getSentencesForOrganStateMarker();
		markupDescriptions(treatments, sentencesForOrganStateMarker);		
		return treatments;
	}


	private void markupDescriptions(List<Treatment> treatments, Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker) {
		for(Treatment treatment : treatments) {
			System.out.println("Create description for treatment: " + treatment.getName());
			createNewDescription(treatment, sentencesForOrganStateMarker.get(treatment));
		}
	}

	private void createNewDescription(Treatment treatment, //List<Token> sentences, 
			LinkedHashMap<String, String> sentences) {
		List<ChunkCollector> treatmentChunkCollectors = new ArrayList<ChunkCollector>();
		for(Entry<String, String> sentenceEntry : sentences.entrySet()) {
			String sentenceString = sentenceEntry.getValue();
			String source = sentenceEntry.getKey();
			
			System.out.println("Process sentence: " + sentenceString);
			
			String[] sentenceArray = sentenceString.split("##");
			sentenceString = sentenceArray[2];
			String subjectTag = sentenceArray[1];
			String modifier = sentenceArray[0];
			modifier = modifier.replaceAll("\\[|\\]|>|<|(|)", "");
			subjectTag = subjectTag.replaceAll("\\[|\\]|>|<|(|)", "");
			
			String normalizedSentence = normalizer.normalize(sentenceString, subjectTag, modifier, source);
			System.out.println("Normalized sentence: " + normalizedSentence);
			List<Token> sentence = wordTokenizer.tokenize(normalizedSentence);
			
			List<Token> posedSentence = posTagger.tag(sentence);
			System.out.println("POSed sentence " + posedSentence);
			
			AbstractParseTree parseTree = parser.parse(posedSentence);
			System.out.println("Parse tree: ");
			parseTree.prettyPrint();
			
			ChunkCollector chunkCollector = chunkerChain.chunk(parseTree, subjectTag, treatment, source, sentenceString);
			treatmentChunkCollectors.add(chunkCollector);
			System.out.println("Sentence processing finished.\n");
		}
		
		System.out.println("Extract new description using " + descriptionExtractor.getDescription() + "...");
		TreatmentElement newDescriptionElement = descriptionExtractor.extract(treatmentChunkCollectors);

		List<ValueTreatmentElement> descriptions = treatment.getValueTreatmentElements("description");
		for(ValueTreatmentElement description : descriptions) { 
			treatment.addTreatmentElement(newDescriptionElement);
			treatment.removeTreatmentElement(description);
			break;
		}
		System.out.println(" -> JAXB: ");
		System.out.println(treatment);
	}
}
