package semanticMarkup.core.transformation.lib.description;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkerChain;
import semanticMarkup.ling.extract.IDescriptionExtractor;
import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.parse.IParser;
import semanticMarkup.ling.pos.IPOSTagger;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.log.LogLevel;

/**
 * A DescriptionExtractorRun creates a new description for a given treatment by semantically marking up the old description
 * @author rodenhausen
 */
public class DescriptionExtractorRun implements Callable<TreatmentElement> {

	private Treatment treatment;
	private List<Future<ChunkCollector>> futureChunkCollectors = new ArrayList<Future<ChunkCollector>>();
	private INormalizer normalizer;
	private ITokenizer wordTokenizer;
	private IPOSTagger posTagger;
	private IParser parser;
	private ChunkerChain chunkerChain;
	private IDescriptionExtractor descriptionExtractor;
	private Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker;
	private boolean parallelProcessing;
	private int sentenceChunkerRunMaximum;
	private CountDownLatch descriptionExtractorsLatch;

	/**
	 * @param treatment
	 * @param normalizer
	 * @param wordTokenizer
	 * @param posTagger
	 * @param parser
	 * @param chunkerChain
	 * @param descriptionExtractor
	 * @param sentencesForOrganStateMarker
	 * @param parallelProcessing
	 * @param sentenceChunkerRunMaximum
	 * @param latch 
	 */
	public DescriptionExtractorRun(Treatment treatment, 
			INormalizer normalizer, ITokenizer wordTokenizer, IPOSTagger posTagger, IParser parser, ChunkerChain chunkerChain, 
			IDescriptionExtractor descriptionExtractor, Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker, boolean parallelProcessing,
			int sentenceChunkerRunMaximum, CountDownLatch descriptionExtractorsLatch) {
		this.treatment = treatment;
		this.normalizer = normalizer;
		this.wordTokenizer = wordTokenizer;
		this.posTagger = posTagger;
		this.parser = parser;
		this.chunkerChain = chunkerChain;
		this.descriptionExtractor = descriptionExtractor;
		this.sentencesForOrganStateMarker = sentencesForOrganStateMarker;
		this.parallelProcessing = parallelProcessing;
		this.sentenceChunkerRunMaximum = sentenceChunkerRunMaximum;
		this.descriptionExtractorsLatch = descriptionExtractorsLatch;
	}

	@Override
	public TreatmentElement call() throws Exception {
		log(LogLevel.DEBUG, "Create description for treatment: " + treatment.getName());
		Map<String, String> sentences = sentencesForOrganStateMarker.get(treatment);
	
		//configure exectuorService to only allow a number of threads to run at a time
		ExecutorService executorService = null;
		if(!this.parallelProcessing)
			executorService = Executors.newSingleThreadExecutor();
		if(this.parallelProcessing && this.sentenceChunkerRunMaximum < Integer.MAX_VALUE)
			executorService = Executors.newFixedThreadPool(sentenceChunkerRunMaximum);
		if(this.parallelProcessing && this.sentenceChunkerRunMaximum == Integer.MAX_VALUE)
			executorService = Executors.newCachedThreadPool();
		
		CountDownLatch sentencesLatch = new CountDownLatch(sentences.size());
		
		// process each sentence separately
		for(Entry<String, String> sentenceEntry : sentences.entrySet()) {
			String sentenceString = sentenceEntry.getValue();
			String source = sentenceEntry.getKey();
			
			// start a SentenceChunkerRun for the treatment to process as a separate thread
			SentenceChunkerRun sentenceChunker = new SentenceChunkerRun(source, sentenceString, treatment, normalizer, wordTokenizer, 
					posTagger, parser, chunkerChain, sentencesLatch);
			Future<ChunkCollector> futureResult = executorService.submit(sentenceChunker);
			futureChunkCollectors.add(futureResult);
		}
		
		// only continue when all threads are done
		try {
			sentencesLatch.await();
			executorService.shutdown();
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, "Problem with sentencesLatch or executorService", e);
		}
		
		log(LogLevel.DEBUG, "Extract new description using " + descriptionExtractor.getDescription() + "...");
		
		// extract the new description from the result of all chunked sentences of the treatment
		List<ChunkCollector> treatmentChunkCollectors = new ArrayList<ChunkCollector>();
		for(Future<ChunkCollector> futureChunkCollector : this.futureChunkCollectors) {
			try {
				treatmentChunkCollectors.add(futureChunkCollector.get());
			} catch (Exception e) {
				log(LogLevel.ERROR, "Problem getting Future from chunkCollector", e);
			}
		}
		
		log(LogLevel.DEBUG, "extract for treatment " + treatment.getName());
		TreatmentElement newDescriptionElement = descriptionExtractor.extract(treatmentChunkCollectors);
		
		descriptionExtractorsLatch.countDown();
		return newDescriptionElement;
	}
}
