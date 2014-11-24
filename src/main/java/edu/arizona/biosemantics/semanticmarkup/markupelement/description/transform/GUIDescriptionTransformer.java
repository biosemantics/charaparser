package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.gui.MainForm;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.INormalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParser;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.IPOSTagger;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.ITokenizer;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.IDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Processor;

/**
 * Transforms the treatments by semantically marking up the description treatment element of a treatment
 * Uses MainForm from previous Charaparser (all classes in semanticmarkup.gui are of the previous version and 
 * coupled with MainForm but nothing outside of the package) and hence requires e.g. certain database tables 
 * to process the results of the perl learning part.
 * @author rodenhausen
 */
public class GUIDescriptionTransformer extends AbstractDescriptionTransformer {

	private IParser parser;
	private IPOSTagger posTagger;
	private IDescriptionExtractor descriptionExtractor;
	private INormalizer normalizer;
	private ITerminologyLearner terminologyLearner;
	private ITokenizer wordTokenizer;
	private ChunkerChain chunkerChain;
	private int descriptionExtractorRunMaximum;
	private int sentenceChunkerRunMaximum;
	private MainForm mainForm;
	private Map<Description, Future<Description>> futureNewDescriptions = 
			new HashMap<Description, Future<Description>>();
	private Set<String> selectedSources;
	private String glossaryTable;

	/**
	 * @param wordTokenizer
	 * @param parser
	 * @param chunkerChain
	 * @param posTagger
	 * @param descriptionExtractor
	 * @param normalizer
	 * @param terminologyLearner
	 * @param parallelProcessing
	 * @param descriptionExtractorRunMaximum
	 * @param sentenceChunkerRunMaximum
	 * @param mainForm
	 * @throws Exception
	 */
	@Inject
	public GUIDescriptionTransformer(
			@Named("Version") String version,
			@Named("WordTokenizer")ITokenizer wordTokenizer, 
			IParser parser,
			@Named("ChunkerChain")ChunkerChain chunkerChain,
			IPOSTagger posTagger, 
			IDescriptionExtractor descriptionExtractor, 
			INormalizer normalizer,
			ITerminologyLearner terminologyLearner,
			@Named("MarkupDescriptionTreatmentTransformer_ParallelProcessing")boolean parallelProcessing, 
			@Named("MarkupDescriptionTreatmentTransformer_DescriptionExtractorRunMaximum")int descriptionExtractorRunMaximum, 
			@Named("MarkupDescriptionTreatmentTransformer_SentenceChunkerRunMaximum")int sentenceChunkerRunMaximum, 
			MainForm mainForm, 
			@Named("SelectedSources")Set<String> selectedSources, 
			@Named("GlossaryTable")String glossaryTable) throws Exception {
		super(version, parallelProcessing);
		this.parser = parser;
		this.posTagger = posTagger;
		this.chunkerChain = chunkerChain;
		this.descriptionExtractor = descriptionExtractor;
		this.normalizer = normalizer;
		this.terminologyLearner = terminologyLearner;
		this.wordTokenizer = wordTokenizer;
		this.descriptionExtractorRunMaximum = descriptionExtractorRunMaximum;
		this.sentenceChunkerRunMaximum = sentenceChunkerRunMaximum;
		this.mainForm = mainForm;
		this.selectedSources = selectedSources;
		this.glossaryTable = glossaryTable;
	}

	@Override
	public Processor transform(List<AbstractDescriptionsFile> descriptionsFiles) {
		// prepare main form (e.g. needs to create certain database tables) 
		mainForm.startMarkup(descriptionsFiles);
		
		// learn terminology
		terminologyLearner.learn(descriptionsFiles, glossaryTable);
		
		// show main form to correct/extend learned terminology
		try {
			mainForm.open();
		} catch (Exception e) {
			log(LogLevel.ERROR, "Problem opening MainForm", e);
		}
		
		// MainForm expects datatable structure of perl learning part and hence if PerlTerminologyLearner is
		// used terminology has to be re-read from database 
		terminologyLearner.readResults(descriptionsFiles);
		
		Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker = 
				terminologyLearner.getSentencesForOrganStateMarker();
		// do the actual markup
		markupDescriptions(descriptionsFiles, sentencesForOrganStateMarker);	
		return new TransformationReport(version, "none yet", "none yet");
	}

	/**
	 * @param treatments
	 * @param sentencesForOrganStateMarker
	 */
	private void markupDescriptions(List<AbstractDescriptionsFile> descriptionsFiles, 
			Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker) {
		//configure exectuorService to only allow a number of threads to run at a time
		ExecutorService executorService = null;
		if(!this.parallelProcessing)
			executorService = Executors.newSingleThreadExecutor();
		if(this.parallelProcessing && this.descriptionExtractorRunMaximum < Integer.MAX_VALUE)
			executorService = Executors.newFixedThreadPool(descriptionExtractorRunMaximum);
		if(this.parallelProcessing && this.descriptionExtractorRunMaximum == Integer.MAX_VALUE)
			executorService = Executors.newCachedThreadPool();
		
		int descriptionCount = 0;
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			descriptionCount += descriptionsFile.getDescriptions().size();
		}
		CountDownLatch descriptionExtractorsLatch = new CountDownLatch(descriptionCount);
		
		// process each treatment separately
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			for(Description description : descriptionsFile.getDescriptions()) {
				// start a DescriptionExtractorRun for the treatment to process as a separate thread
				DescriptionExtractorRun descriptionExtractorRun = new DescriptionExtractorRun(descriptionsFile, description, normalizer, wordTokenizer, 
						posTagger, parser, chunkerChain, descriptionExtractor, sentencesForOrganStateMarker, parallelProcessing, sentenceChunkerRunMaximum, 
						descriptionExtractorsLatch, selectedSources);
				Future<Description> futureNewDescription = executorService.submit(descriptionExtractorRun);
				this.futureNewDescriptions.put(description, futureNewDescription);
			}
		}
		
		//only continue when all threads are done
		try {
			descriptionExtractorsLatch.await();
			executorService.shutdown();
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, "Problem with descriptionExtractorsLatch or executorService", e);
		}
		
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			for(Description description : descriptionsFile.getDescriptions()) {
				Future<Description> futureNewDescription = futureNewDescriptions.get(description);
				try {
					description.setText(futureNewDescription.get().getText());
				} catch (Exception e) {
					log(LogLevel.DEBUG, "Problem getting Future from new description", e);
				}
				log(LogLevel.DEBUG, " -> JAXB: ");
				log(LogLevel.DEBUG, description.toString());
			}
		}
	}
}
