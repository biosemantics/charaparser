package semanticMarkup.core.transformation.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkerChain;
import semanticMarkup.ling.extract.IDescriptionExtractor;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.parse.IParser;
import semanticMarkup.ling.pos.IPOSTagger;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.log.LogLevel;

public class DescriptionExtractorRun implements Runnable {

	private Treatment treatment;
	private Map<Thread, SentenceChunkerRun> sentenceChunkerRuns = new LinkedHashMap<Thread, SentenceChunkerRun>();
	private List<ChunkCollector> treatmentChunkCollectors = new ArrayList<ChunkCollector>();
	private ITerminologyLearner terminologyLearner;
	private INormalizer normalizer;
	private ITokenizer wordTokenizer;
	private IPOSTagger posTagger;
	private IParser parser;
	private ChunkerChain chunkerChain;
	private IDescriptionExtractor descriptionExtractor;
	private Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker;
	private boolean parallelProcessing;
	private int sentenceChunkerRunMaximum;

	public DescriptionExtractorRun(Treatment treatment, ITerminologyLearner terminologyLearner, 
			INormalizer normalizer, ITokenizer wordTokenizer, IPOSTagger posTagger, IParser parser, ChunkerChain chunkerChain, 
			IDescriptionExtractor descriptionExtractor, Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker, boolean parallelProcessing,
			int sentenceChunkerRunMaximum) {
		this.treatment = treatment;
		this.terminologyLearner = terminologyLearner;
		this.normalizer = normalizer;
		this.wordTokenizer = wordTokenizer;
		this.posTagger = posTagger;
		this.parser = parser;
		this.chunkerChain = chunkerChain;
		this.descriptionExtractor = descriptionExtractor;
		this.sentencesForOrganStateMarker = sentencesForOrganStateMarker;
		this.parallelProcessing = parallelProcessing;
		this.sentenceChunkerRunMaximum = sentenceChunkerRunMaximum;
	}
	
	@Override
	public void run() {
		log(LogLevel.DEBUG, "Create description for treatment: " + treatment.getName());
		if(sentencesForOrganStateMarker.containsKey(treatment))
			createNewDescription(treatment, sentencesForOrganStateMarker.get(treatment));
	}
	
	private void createNewDescription(Treatment treatment, //List<Token> sentences, 
			LinkedHashMap<String, String> sentences) {
		treatmentChunkCollectors.clear();
		sentenceChunkerRuns.clear();
		int threadId = 0;
		for(Entry<String, String> sentenceEntry : sentences.entrySet()) {
			if(threadId % this.sentenceChunkerRunMaximum == 0)
				waitForThreadsToFinish();
			threadId++;
			String sentenceString = sentenceEntry.getValue();
			String source = sentenceEntry.getKey();
			SentenceChunkerRun sentenceChunker = new SentenceChunkerRun(source, sentenceString, treatment, terminologyLearner, normalizer, wordTokenizer, 
					posTagger, parser, chunkerChain);
			Thread thread = new Thread(sentenceChunker);
			sentenceChunkerRuns.put(thread, sentenceChunker);
			if(parallelProcessing)
				thread.start();
			else
				thread.run();
		}
		waitForThreadsToFinish();
		
		log(LogLevel.DEBUG, "Extract new description using " + descriptionExtractor.getDescription() + "...");
		TreatmentElement newDescriptionElement = descriptionExtractor.extract(treatmentChunkCollectors);

		List<ValueTreatmentElement> descriptions = treatment.getValueTreatmentElements("description");
		for(ValueTreatmentElement description : descriptions) { 
			treatment.addTreatmentElement(newDescriptionElement);
			treatment.removeTreatmentElement(description);
			break;
		}
		log(LogLevel.DEBUG, " -> JAXB: ");
		log(LogLevel.DEBUG, treatment.toString());
	}

	private void waitForThreadsToFinish() {
		for(Thread thread : sentenceChunkerRuns.keySet()) {
			SentenceChunkerRun sentenceChunker = sentenceChunkerRuns.get(thread);
			try {
				thread.join();
				treatmentChunkCollectors.add(sentenceChunker.getResult());
			} catch (InterruptedException e) {
				log(LogLevel.ERROR, e);
			}
		}
		sentenceChunkerRuns.clear();
	}
}
