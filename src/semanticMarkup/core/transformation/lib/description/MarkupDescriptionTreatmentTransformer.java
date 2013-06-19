package semanticMarkup.core.transformation.lib.description;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

import oto.beans.TermCategory;
import oto.beans.TermSynonym;
import oto.beans.WordRole;
import oto.full.IOTOClient;
import oto.full.beans.GlossaryDownload;
import oto.lite.IOTOLiteClient;
import oto.lite.beans.Decision;
import oto.lite.beans.Download;
import oto.lite.beans.Synonym;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.know.IGlossary;
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
 * This can be used for the second and hence the 'markup' application for the iPlant integration
 * @author rodenhausen
 */
public class MarkupDescriptionTreatmentTransformer extends DescriptionTreatmentTransformer {

	protected IParser parser;
	protected IPOSTagger posTagger;
	protected IDescriptionExtractor descriptionExtractor;
	protected INormalizer normalizer;
	protected ITerminologyLearner terminologyLearner;
	protected ITokenizer wordTokenizer;
	protected ChunkerChain chunkerChain;
	protected int descriptionExtractorRunMaximum;
	protected int sentenceChunkerRunMaximum;
	protected Map<Treatment, Future<TreatmentElement>> futureNewDescriptions = new HashMap<Treatment, Future<TreatmentElement>>();
	protected IOTOClient otoClient;
	protected String databasePrefix;
	protected IGlossary glossary;
	protected Connection connection;
	protected String glossaryType;
	protected IOTOLiteClient otoLiteClient;
	protected String otoLiteTermReviewURL;
	protected Set<String> selectedSources;
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
			IOTOClient otoClient, 
			IOTOLiteClient otoLiteClient, 
			@Named("otoLiteTermReviewURL") String otoLiteTermReviewURL,
			@Named("databaseHost") String databaseHost,
			@Named("databasePort") String databasePort,
			@Named("databaseName")String databaseName,
			@Named("databaseUser")String databaseUser,
			@Named("databasePassword")String databasePassword,
			@Named("databasePrefix")String databasePrefix, 
			@Named("glossaryType")String glossaryType,
			IGlossary glossary, 
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
		this.otoClient = otoClient;
		this.otoLiteClient = otoLiteClient;
		this.otoLiteTermReviewURL = otoLiteTermReviewURL;
		this.databasePrefix = databasePrefix;
		this.glossary = glossary;
		this.glossaryType = glossaryType;
		this.selectedSources = selectedSources;
		this.glossaryTable = glossaryTable;
		
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://" + databaseHost + ":" + databasePort +"/" + databaseName + "?connectTimeout=0&socketTimeout=0&autoReconnect=true",
				databaseUser, databasePassword);
	}

	@Override
	public List<Treatment> transform(List<Treatment> treatments) {
		
		
		//download gloss again from real OTO because the last download is no longer in memory
		//it is possible for gloss o change from last run, make sure to grab the correct version.
		//when remove MYSQL, take care of this issue
		
		//TODO: String version = otoClient.getLatestVersion();
		//String tablePrefix = glossaryType + "_" + glossaryDownload.getVersion();
		//String glossaryTable = tablePrefix + "_glossary";
		//if(!glossaryExistsLocally(tablePrefix)) {
		
		String glossaryVersion = getGlossaryVersionOfLearn();
		if(glossaryVersion == null)
			glossaryVersion = "latest";
		
		GlossaryDownload glossaryDownload = otoClient.download(glossaryType, glossaryVersion); 
		glossaryVersion = glossaryDownload.getVersion();
		for(Treatment treatment : treatments) {
			if(!treatment.containsContainerTreatmentElement("meta"))
				treatment.addTreatmentElement(new ContainerTreatmentElement("meta"));
			ContainerTreatmentElement metaElement = treatment.getContainerTreatmentElement("meta");
			metaElement.addTreatmentElement(new ValueTreatmentElement("charaparser_version", version));
			metaElement.addTreatmentElement(new ValueTreatmentElement("glossary_name", glossaryType));
			metaElement.addTreatmentElement(new ValueTreatmentElement("glossary_version", glossaryVersion));
		}
		
		int uploadId;
		Download download;
		try {
			uploadId = readUploadId();		
			download = otoLiteClient.download(uploadId);
		} catch (SQLException e) {
			this.log(LogLevel.ERROR, "Problem reading uploadId", e);
			download = new Download();
		}
		
		log(LogLevel.DEBUG, "Size of permanent glossary downloaded:\n" +
				"Number of term categoy relations " + glossaryDownload.getTermCategories().size() + "\n" +
				"Number of term synonym relations " + glossaryDownload.getTermSynonyms().size());
		log(LogLevel.DEBUG, "Size of temporary glossary downloaded:\n" +
				"Number of term categoy relations " + download.getDecisions().size() + "\n" +
				"Number of term synonym relations " + download.getSynonyms().size());
		storeInLocalDB(glossaryDownload, download, this.databasePrefix);
		initGlossary(glossaryDownload, download);
		
		//this is needed to initialize terminologylearner (DatabaseInputNoLearner / fileTreatments)
		//even though no actual learning is taking place
		terminologyLearner.learn(treatments, glossaryTable);
		terminologyLearner.readResults(treatments);
		Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker = terminologyLearner.getSentencesForOrganStateMarker();
		// do the actual markup
		markupDescriptions(treatments, sentencesForOrganStateMarker);		
		return treatments;
	}

	private String getGlossaryVersionOfLearn() {
		String glossaryVersion = null;
		try {
			String sql = "SELECT glossary_version FROM datasetprefixes WHERE prefix = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, databasePrefix);
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();
			
			while(resultSet.next()) {
				glossaryVersion = resultSet.getString("glossary_version");
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "Could not read glossary version used for learning", e);
		}
		return glossaryVersion;
	}

	private int readUploadId() throws SQLException {
		int uploadId = -1;
		String sql = "SELECT oto_uploadid FROM datasetprefixes WHERE prefix = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setString(1, databasePrefix);
		preparedStatement.execute();
		ResultSet resultSet = preparedStatement.getResultSet();
		while(resultSet.next()) {
			uploadId = resultSet.getInt("oto_uploadid");
		}
		return uploadId;
	}

	/**
	 * TODO: OTO Webservice should probably only return one term category list.
	 * No need to return an extra term synonym list just because it might make sense to have them seperate in a relational database schema
	 * @param otoGlossary
	 */
	protected void initGlossary(GlossaryDownload glossaryDownload, Download download) {
		for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
			glossary.addEntry(termCategory.getTerm(), termCategory.getCategory());
		}	
		for(Decision decision : download.getDecisions()) {
			glossary.addEntry(decision.getTerm(), decision.getCategory());
		}
	}
	
	private void storeInLocalDB(GlossaryDownload glossaryDownload, Download download, 
			String tablePrefix) {
		List<TermCategory> termCategories = new ArrayList<TermCategory>();
		List<WordRole> wordRoles = new ArrayList<WordRole>();
		List<TermSynonym> termSynonyms = new ArrayList<TermSynonym>();
		
		termCategories.addAll(glossaryDownload.getTermCategories());
		termSynonyms.addAll(glossaryDownload.getTermSynonyms());
		
		List<Decision> decisions = download.getDecisions();
		List<Synonym> synonyms = download.getSynonyms();

		HashSet<String> hasSynSet = new HashSet<String>();
		for(Synonym synonym : synonyms) {
			TermSynonym termSynonym = new TermSynonym();
			termSynonym.setTerm(synonym.getTerm());
			termSynonym.setSynonym(synonym.getSynonym());
			termSynonyms.add(termSynonym);
			hasSynSet.add(synonym.getTerm());
		}
		
		for(Decision decision : decisions) {
			TermCategory termCategory = new TermCategory();
			termCategory.setTerm(decision.getTerm());
			termCategory.setCategory(decision.getCategory());
			termCategory.setHasSyn(hasSynSet.contains(decision.getTerm()));
			termCategories.add(termCategory);
		}
		
		/** generate the wordroles from termcategories **/
		//remove duplicates, term is primary key in the table
		HashSet<String> wordSet = new HashSet<String>();
		for(TermCategory termCategory : termCategories) {
			if(wordSet.contains(termCategory.getTerm()))
				continue;
			wordSet.add(termCategory.getTerm());
			WordRole wordRole = new WordRole();
			wordRole.setWord(termCategory.getTerm());
			String semanticRole = "c";
			if(termCategory.getCategory().equalsIgnoreCase("structure")) {
				semanticRole = "op";
			}
			wordRole.setSemanticRole(semanticRole);
			wordRole.setSavedid(""); //not really needed, is a left over from earlier charaparser times
			wordRoles.add(wordRole);
		}
		
		try {
			Statement stmt = connection.createStatement();
	        String cleanupQuery = "DROP TABLE IF EXISTS " + 
									tablePrefix + "_term_category, " + 
									tablePrefix + "_syns, " +
									tablePrefix + "_wordroles;";
	        stmt.execute(cleanupQuery);
	        stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_syns (`term` varchar(200) DEFAULT NULL, `synonym` varchar(200) DEFAULT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_term_category (`term` varchar(100) DEFAULT NULL, `category` varchar(200) " +
					"DEFAULT NULL, `hasSyn` tinyint(1) DEFAULT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_wordroles (`word` varchar(50) NOT NULL DEFAULT '', `semanticrole` varchar(2) " +
					"NOT NULL DEFAULT '', `savedid` varchar(40) DEFAULT NULL, PRIMARY KEY (`word`,`semanticrole`));");
			
			for(TermCategory termCategory : termCategories) {
				PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tablePrefix + "_term_category (`term`, `category`, `hasSyn`) VALUES (?, ?, ?)");
				preparedStatement.setString(1, termCategory.getTerm());
				preparedStatement.setString(2, termCategory.getCategory());
				preparedStatement.setBoolean(3, termCategory.isHasSyn());
				preparedStatement.executeUpdate();
			}
			for(TermSynonym termSynonym : termSynonyms) {
				PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tablePrefix + "_syns (`term`, `synonym`) VALUES (?, ?)");
				preparedStatement.setString(1, termSynonym.getTerm());
				preparedStatement.setString(2, termSynonym.getSynonym());
				preparedStatement.executeUpdate();
			}
			for(WordRole wordRole : wordRoles) {
				PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tablePrefix + "_wordroles" + " VALUES (?, ?, ?)");
				preparedStatement.setString(1, wordRole.getWord());
				preparedStatement.setString(2, wordRole.getSemanticRole());
				preparedStatement.setString(3, wordRole.getSavedid());
				preparedStatement.executeUpdate();
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, "Problem storing glossary in local DB", e);
		}
	}

	/**
	 * @param treatments
	 * @param sentencesForOrganStateMarker
	 */
	protected void markupDescriptions(List<Treatment> treatments, Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker) {
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
			if(description!=null) 
				treatment.removeTreatmentElement(description);
			try {
				treatment.addTreatmentElement(futureNewDescription.get());
			} catch (Exception e) {
				log(LogLevel.ERROR, "Problem getting Future from new description", e);
			}
			log(LogLevel.DEBUG, " -> JAXB: ");
			log(LogLevel.DEBUG, treatment.toString());
		}
	}
}
