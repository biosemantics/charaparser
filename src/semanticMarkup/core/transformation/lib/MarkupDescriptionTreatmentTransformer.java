package semanticMarkup.core.transformation.lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.gui.MainForm;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.net.IOTOClient;
import semanticMarkup.know.net.OTOGlossary;
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

import edu.arizona.sirls.beans.TermCategory;
import edu.arizona.sirls.beans.TermSynonym;

/**
 * Transforms the treatments by semantically marking up the description treatment element of a treatment
 * This can be used for the second and hence the 'markup' application for the iPlant integration
 * @author rodenhausen
 */
public class MarkupDescriptionTreatmentTransformer extends DescriptionTreatmentTransformer {

	private IParser parser;
	private IPOSTagger posTagger;
	private IDescriptionExtractor descriptionExtractor;
	private INormalizer normalizer;
	private ITerminologyLearner terminologyLearner;
	private ITokenizer wordTokenizer;
	private ChunkerChain chunkerChain;
	private int descriptionExtractorRunMaximum;
	private int sentenceChunkerRunMaximum;
	private Map<Treatment, Future<TreatmentElement>> futureNewDescriptions = new HashMap<Treatment, Future<TreatmentElement>>();
	private IOTOClient otoClient;
	private String databasePrefix;
	private IGlossary glossary;
	private Connection connection;
	
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
	 * @param otoClient
	 * @param databaseName
	 * @param databaseUser
	 * @param databasePassword
	 * @param databasePrefix
	 * @param glossary
	 * @throws Exception
	 */
	@Inject
	public MarkupDescriptionTreatmentTransformer(
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
			IOTOClient otoClient, 
			@Named("databaseName")String databaseName,
			@Named("databaseUser")String databaseUser,
			@Named("databasePassword")String databasePassword,
			@Named("databasePrefix")String databasePrefix, 
			
			IGlossary glossary) throws Exception {
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
		this.otoClient = otoClient;
		this.databasePrefix = databasePrefix;
		this.glossary = glossary;
		
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + databaseName, databaseUser, databasePassword);
	}

	@Override
	public List<Treatment> transform(List<Treatment> treatments) {
		OTOGlossary otoGlossary = otoClient.read(databasePrefix);
		storeInLocalDB(otoGlossary);
		initGlossary(otoGlossary);
		
		Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker = terminologyLearner.getSentencesForOrganStateMarker();
		// do the actual markup
		markupDescriptions(treatments, sentencesForOrganStateMarker);		
		return treatments;
	}

	/**
	 * TODO: OTO Webservice should probably only return one term category list.
	 * No need to return an extra term synonym list just because it might make sense to have them seperate in a relational database schema
	 * @param otoGlossary
	 */
	private void initGlossary(OTOGlossary otoGlossary) {
		for(TermCategory termCategory : otoGlossary.getTermCategories()) {
			glossary.addEntry(termCategory.getTerm(), termCategory.getCategory());
		}
	}

	
	private void storeInLocalDB(OTOGlossary otoGlossary) {
		try {
			Statement stmt = connection.createStatement();
	        String cleanupQuery = "DROP TABLE IF EXISTS " + 
									this.databasePrefix + "_term_category, " + 
									this.databasePrefix + "_syns;";
	        stmt.execute(cleanupQuery);
	        stmt.execute("CREATE TABLE IF NOT EXISTS " + this.databasePrefix + "_syns (`term` varchar(200) DEFAULT NULL, `synonym` varchar(200) DEFAULT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS " + this.databasePrefix + "_term_category (`term` varchar(100) DEFAULT NULL, `category` varchar(200) " +
					"DEFAULT NULL, `hasSyn` tinyint(1) DEFAULT NULL)");
			
			for(TermCategory termCategory : otoGlossary.getTermCategories()) {
				 stmt.execute("INSERT INTO " + this.databasePrefix + "_term_category (`term`, `category`, `hasSyn`) VALUES " +
				 		"('" + termCategory.getTerm() +"', '" + termCategory.getCategory() + "', '" + termCategory.getHasSyn() +"');");
					
			}
			for(TermSynonym termSynonym : otoGlossary.getTermSynonyms()) {
				 stmt.execute("INSERT INTO " + this.databasePrefix + "_syns (`term`, `synonym`) VALUES " +
					 		"('" + termSynonym.getTerm() +"', '" + termSynonym.getSynonym() + "');");
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, e);
		}
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
					descriptionExtractorsLatch);
			Future<TreatmentElement> futureNewDescription = executorService.submit(descriptionExtractorRun);
			this.futureNewDescriptions.put(treatment, futureNewDescription);
		}
		
		//only continue when all threads are done
		try {
			descriptionExtractorsLatch.await();
			executorService.shutdown();
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, e);
		}
		
		for(Treatment treatment : treatments) {
			Future<TreatmentElement> futureNewDescription = futureNewDescriptions.get(treatment);
			ValueTreatmentElement description = treatment.getValueTreatmentElement("description");
			treatment.removeTreatmentElement(description);
			try {
				treatment.addTreatmentElement(futureNewDescription.get());
			} catch (Exception e) {
				log(LogLevel.DEBUG, e);
			}
			log(LogLevel.DEBUG, " -> JAXB: ");
			log(LogLevel.DEBUG, treatment.toString());
		}
	}
}
