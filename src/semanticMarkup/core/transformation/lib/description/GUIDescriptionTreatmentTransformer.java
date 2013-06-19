package semanticMarkup.core.transformation.lib.description;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.gui.MainForm;
import semanticMarkup.ling.chunk.ChunkerChain;
import semanticMarkup.ling.extract.IDescriptionExtractor;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.parse.IParser;
import semanticMarkup.ling.pos.IPOSTagger;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Transforms the treatments by semantically marking up the description treatment element of a treatment
 * Uses MainForm from previous Charaparser (all classes in semanticMarkup.gui are of the previous version and 
 * coupled with MainForm but nothing outside of the package) and hence requires e.g. certain database tables 
 * to process the results of the perl learning part.
 * @author rodenhausen
 */
public class GUIDescriptionTreatmentTransformer extends DescriptionTreatmentTransformer {

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
	private Map<Treatment, Future<TreatmentElement>> futureNewDescriptions = new HashMap<Treatment, Future<TreatmentElement>>();
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
	public GUIDescriptionTreatmentTransformer(
			@Named("Version") String version,
			@Named("WordTokenizer")ITokenizer wordTokenizer, 
			IParser parser,
			@Named("ChunkerChain")ChunkerChain chunkerChain,
			IPOSTagger posTagger, 
			IDescriptionExtractor descriptionExtractor, 
			INormalizer normalizer,
			ITerminologyLearner terminologyLearner,
			@Named("MarkupDescriptionTreatmentTransformer_parallelProcessing")boolean parallelProcessing, 
			@Named("MarkupDescriptionTreatmentTransformer_descriptionExtractorRunMaximum")int descriptionExtractorRunMaximum, 
			@Named("MarkupDescriptionTreatmentTransformer_sentenceChunkerRunMaximum")int sentenceChunkerRunMaximum, 
			MainForm mainForm, 
			@Named("selectedSources")Set<String> selectedSources, 
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
	public List<Treatment> transform(List<Treatment> treatments) {
		// prepare main form (e.g. needs to create certain database tables) 
		mainForm.startMarkup(treatments);
		
		// learn terminology
		terminologyLearner.learn(treatments, glossaryTable);
		
		// show main form to correct/extend learned terminology
		try {
			mainForm.open();
		} catch (Exception e) {
			log(LogLevel.ERROR, "Problem opening MainForm", e);
		}
		
		// MainForm expects datatable structure of perl learning part and hence if PerlTerminologyLearner is
		// used terminology has to be re-read from database 
		terminologyLearner.readResults(treatments);
		
		Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker = terminologyLearner.getSentencesForOrganStateMarker();
		// do the actual markup
		markupDescriptions(treatments, sentencesForOrganStateMarker);		
		return treatments;
	}

	/**
	 * @param treatments
	 * @param sentencesForOrganStateMarker
	 */
	private void markupDescriptions(List<Treatment> treatments, Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker) {
		//configure exectuorService to only allow a number of threads to run at a time
		ExecutorService executorService = null;
		if(!this.parallelProcessing)
			executorService = Executors.newSingleThreadExecutor();
		if(this.parallelProcessing && this.descriptionExtractorRunMaximum < Integer.MAX_VALUE)
			executorService = Executors.newFixedThreadPool(descriptionExtractorRunMaximum);
		if(this.parallelProcessing && this.descriptionExtractorRunMaximum == Integer.MAX_VALUE)
			executorService = Executors.newCachedThreadPool();
		
		CountDownLatch descriptionExtractorsLatch = new CountDownLatch(treatments.size());
		
		// process each treatment separately
		for(Treatment treatment : treatments) {
			// start a DescriptionExtractorRun for the treatment to process as a separate thread
			DescriptionExtractorRun descriptionExtractorRun = new DescriptionExtractorRun(treatment, normalizer, wordTokenizer, 
					posTagger, parser, chunkerChain, descriptionExtractor, sentencesForOrganStateMarker, parallelProcessing, sentenceChunkerRunMaximum, 
					descriptionExtractorsLatch, selectedSources);
			Future<TreatmentElement> futureNewDescription = executorService.submit(descriptionExtractorRun);
			this.futureNewDescriptions.put(treatment, futureNewDescription);
		}
		
		//only continue when all threads are done
		try {
			descriptionExtractorsLatch.await();
			executorService.shutdown();
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, "Problem with descriptionExtractorsLatch or executorService", e);
		}
		
		for(Treatment treatment : treatments) {
			Future<TreatmentElement> futureNewDescription = futureNewDescriptions.get(treatment);
			ValueTreatmentElement description = treatment.getValueTreatmentElement("description");
			treatment.removeTreatmentElement(description);
			try {
				treatment.addTreatmentElement(futureNewDescription.get());
			} catch (Exception e) {
				log(LogLevel.DEBUG, "Problem getting Future from new description", e);
			}
			log(LogLevel.DEBUG, " -> JAXB: ");
			log(LogLevel.DEBUG, treatment.toString());
		}
	}
}
