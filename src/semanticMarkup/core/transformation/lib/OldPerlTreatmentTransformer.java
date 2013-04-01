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
import semanticMarkup.gui.MainForm;
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
public class OldPerlTreatmentTransformer extends MarkupDescriptionTreatmentTransformer {

	private IParser parser;
	private IPOSTagger posTagger;
	private IDescriptionExtractor descriptionExtractor;
	private INormalizer normalizer;
	private ITerminologyLearner terminologyLearner;
	private ITokenizer wordTokenizer;
	private ChunkerChain chunkerChain;
	private Map<Thread, DescriptionExtractorRun> descriptionExtractorRuns = new LinkedHashMap<Thread, DescriptionExtractorRun>();
	private int descriptionExtractorRunMaximum;
	private int sentenceChunkerRunMaximum;
	private MainForm mainForm;
	
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
	 */
	@Inject
	public OldPerlTreatmentTransformer(
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
			MainForm mainForm) {
		super(parallelProcessing);
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
	}

	@Override
	public List<Treatment> transform(List<Treatment> treatments) {
		// prepare main form (e.g. needs to create certain database tables) 
		mainForm.startMarkup(treatments);
		
		// learn terminology
		terminologyLearner.learn(treatments);
		
		// show main form to correct/extend learned terminology
		try {
			mainForm.open();
		} catch (Exception e) {
			e.printStackTrace();
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
		int threadId = 0;
		
		// process each treatment separately
		for(Treatment treatment : treatments) {
			
			//only maximally start a descriptionExtractorRunMaximum number of threads to process treatments, hence wait if already processing this many
			if(threadId % descriptionExtractorRunMaximum == 0)
				waitForThreadsToFinish();
			
			threadId++;
			
			// start a DescriptionExtractorRun for the treatment to process as a separate thread
			DescriptionExtractorRun descriptionExtractorRun = new DescriptionExtractorRun(treatment, terminologyLearner, normalizer, wordTokenizer, 
					posTagger, parser, chunkerChain, descriptionExtractor, sentencesForOrganStateMarker, parallelProcessing, sentenceChunkerRunMaximum);
			Thread thread = new Thread(descriptionExtractorRun);
			descriptionExtractorRuns.put(thread, descriptionExtractorRun);
			//if parallel processing is to be used fork here
			if(this.parallelProcessing)
				thread.start();
			else 
				thread.run();
		}
		//only continue when all threads are done
		waitForThreadsToFinish();
	}

	/**
	 * holds this executing thread until all the descriptionExtractorRun threads are done processing
	 */
	private void waitForThreadsToFinish() {
		for(Thread thread : descriptionExtractorRuns.keySet()) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				log(LogLevel.ERROR, e);
			}
		}
		descriptionExtractorRuns.clear();
	}
}
