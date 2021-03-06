package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.arizona.biosemantics.common.ling.pos.IPOSTagger;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.INormalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParser;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.IDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;


/**
 * A DescriptionExtractorRun creates a new description for a given treatment by semantically marking up the old description
 * @author rodenhausen
 */
public class DescriptionExtractorRun implements Callable<Description> {

	private AbstractDescriptionsFile descriptionsFile;
	private Description description;
	private List<Future<ChunkCollector>> futureChunkCollectors = new ArrayList<Future<ChunkCollector>>();
	private INormalizer normalizer;
	private ITokenizer wordTokenizer;
	private IPOSTagger posTagger;
	private IParser parser;
	private ChunkerChain chunkerChain;
	private IDescriptionExtractor descriptionExtractor;
	private Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker;
	private boolean parallelProcessing;
	private int sentenceChunkerRunMaximum;
	private CountDownLatch descriptionExtractorsLatch;
	private Set<String> selectedSources;
	private int descriptionNumber;

	/**
	 *
	 * @param descriptionsFile
	 * @param description
	 * @param descriptionNumber
	 * @param normalizer
	 * @param wordTokenizer
	 * @param posTagger
	 * @param parser
	 * @param chunkerChain
	 * @param descriptionExtractor
	 * @param sentencesForOrganStateMarker
	 * @param parallelProcessing
	 * @param sentenceChunkerRunMaximum
	 * @param descriptionExtractorsLatch
	 * @param selectedSources
	 */
	public DescriptionExtractorRun(AbstractDescriptionsFile descriptionsFile, Description description, int descriptionNumber,
			INormalizer normalizer, ITokenizer wordTokenizer, IPOSTagger posTagger, IParser parser, ChunkerChain chunkerChain,
			IDescriptionExtractor descriptionExtractor, Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker, boolean parallelProcessing,
			int sentenceChunkerRunMaximum, CountDownLatch descriptionExtractorsLatch, Set<String> selectedSources) {
		this.descriptionsFile = descriptionsFile;
		this.description = description;
		this.descriptionNumber = descriptionNumber;
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
		this.selectedSources = selectedSources;
	}

	@Override
	public Description call() throws Exception {
		try {
			log(LogLevel.DEBUG, "Create description for : " + descriptionsFile.getName());
			Map<String, String> sentences = sentencesForOrganStateMarker.get(description);
			
			//configure exectuorService to only allow a number of threads to run at a time
			ExecutorService executorService = null;
			if(!this.parallelProcessing)
				executorService = Executors.newSingleThreadExecutor();
			if(this.parallelProcessing && this.sentenceChunkerRunMaximum < Integer.MAX_VALUE)
				executorService = Executors.newFixedThreadPool(sentenceChunkerRunMaximum);
			if(this.parallelProcessing && this.sentenceChunkerRunMaximum == Integer.MAX_VALUE)
				executorService = Executors.newCachedThreadPool();

			List<Entry<String, String>> selectedSentences = new LinkedList<Entry<String, String>>();
			if(sentences != null) {
				for(Entry<String, String> sentenceEntry : sentences.entrySet()) {
					String source = sentenceEntry.getKey();
					if(selectedSources.isEmpty() || selectedSources.contains(source)) {
						selectedSentences.add(sentenceEntry);
					}
				}
			}
			CountDownLatch sentencesLatch = new CountDownLatch(selectedSentences.size());
			//check this for multi-threaded run
			Hashtable<String, String> prevMissingOrgan = new Hashtable<String, String>(); //hold two entries only -- the missing organ found for the last immediate sentence. It's scope is the sentences from one description.
			prevMissingOrgan.put("source", "");
			prevMissingOrgan.put("missing", "");
			// process each sentence separately
			int i = 0;
			for(Entry<String, String> sentenceEntry : selectedSentences) {
				String sentenceString = sentenceEntry.getValue();
				String source = sentenceEntry.getKey();
				
				//Hong 11/18/2018: write out <text> here to avoid missing <text> due to potential exceptions down the road
				Statement statement = new Statement();
				String[] sentenceArray = sentenceString.split("##");
				statement.setText(sentenceArray[3]);
				String statementId = "d" + descriptionNumber + "_s" + i;
				i++;
				statement.setId(statementId);
				description.addStatement(statement);

				// start a SentenceChunkerRun for the treatment to process as a separate thread
				SentenceChunkerRun sentenceChunker = new SentenceChunkerRun(source, sentenceString,
						description, descriptionsFile, normalizer, wordTokenizer,
						posTagger, parser, chunkerChain, prevMissingOrgan, sentencesLatch);
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
					ChunkCollector chunkCollector = futureChunkCollector.get();
					if(chunkCollector != null)
						treatmentChunkCollectors.add(chunkCollector);
				} catch (Exception e) {
					log(LogLevel.ERROR, "Problem getting Future from chunkCollector", e);
				}
			}

			log(LogLevel.DEBUG, "extract for treatment " + descriptionsFile.getName());
			descriptionExtractor.extract(description, descriptionNumber, treatmentChunkCollectors); 
			} 
		    catch (Throwable t) {
			log(LogLevel.ERROR, "Problem extracting from description \"" + description + "\" in file " +
					descriptionsFile.getFile().getAbsolutePath(), t);
		}
		descriptionExtractorsLatch.countDown();
		return description;
	}
}
