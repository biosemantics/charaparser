package semanticMarkup.core.transformation.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	private Map<Thread, DescriptionExtractorRun> descriptionExtractorRuns = new HashMap<Thread, DescriptionExtractorRun>();
	
	@Inject
	public OldPerlTreatmentTransformer(
			@Named("WordTokenizer")ITokenizer wordTokenizer, 
			IParser parser,
			@Named("ChunkerChain")ChunkerChain chunkerChain,
			IPOSTagger posTagger, 
			IDescriptionExtractor descriptionExtractor, 
			INormalizer normalizer,
			ITerminologyLearner terminologyLearner,
			@Named("MarkupDescriptionTreatmentTransformer_parallelProcessing")boolean parallelProcessing) throws Exception {
		super(parallelProcessing);
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
			DescriptionExtractorRun descriptionExtractorRun = new DescriptionExtractorRun(treatment, terminologyLearner, normalizer, wordTokenizer, 
					posTagger, parser, chunkerChain, descriptionExtractor, sentencesForOrganStateMarker, parallelProcessing);
			Thread thread = new Thread(descriptionExtractorRun);
			descriptionExtractorRuns.put(thread, descriptionExtractorRun);
			if(this.parallelProcessing)
				thread.start();
			else 
				thread.run();
		}
		for(Thread thread : descriptionExtractorRuns.keySet()) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
