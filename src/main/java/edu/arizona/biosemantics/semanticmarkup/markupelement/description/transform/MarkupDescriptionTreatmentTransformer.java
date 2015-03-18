package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.oto.client.WordRole;
import edu.arizona.biosemantics.oto.client.lite.OTOLiteClient;
import edu.arizona.biosemantics.oto.client.oto.OTOClient;
import edu.arizona.biosemantics.oto.common.model.GlossaryDownload;
import edu.arizona.biosemantics.oto.common.model.TermCategory;
import edu.arizona.biosemantics.oto.common.model.TermSynonym;
import edu.arizona.biosemantics.oto.common.model.lite.Decision;
import edu.arizona.biosemantics.oto.common.model.lite.Download;
import edu.arizona.biosemantics.oto.common.model.lite.Synonym;
import edu.arizona.biosemantics.oto.common.model.lite.UploadResult;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.ITerm;
import edu.arizona.biosemantics.semanticmarkup.know.lib.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.know.lib.Term;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.INormalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParser;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.IPOSTagger;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.ITokenizer;
import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.IDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.LearnException;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Processor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Resource;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Software;


/**
 * Transforms the treatments by semantically marking up the description treatment element of a treatment
 * This can be used for the second and hence the 'markup' application for the iPlant integration
 * @author rodenhausen
 */
public class MarkupDescriptionTreatmentTransformer extends AbstractDescriptionTransformer {

	protected IParser parser;
	protected IPOSTagger posTagger;
	protected IDescriptionExtractor descriptionExtractor;
	protected INormalizer normalizer;
	protected ITerminologyLearner terminologyLearner;
	protected ITokenizer wordTokenizer;
	protected ChunkerChain chunkerChain;
	protected int descriptionExtractorRunMaximum;
	protected int sentenceChunkerRunMaximum;
	protected Map<Description, Future<Description>> futureNewDescriptions = 
			new HashMap<Description, Future<Description>>();
	protected OTOClient otoClient;
	protected String databasePrefix;
	protected IGlossary glossary;
	protected Connection connection;
	protected TaxonGroup taxonGroup;
	protected OTOLiteClient otoLiteClient;
	protected String otoLiteTermReviewURL;
	protected Set<String> selectedSources;
	private String glossaryTable;
	private boolean termCategorizationRequired;
	private IInflector inflector;
	private String etcUser;
	private boolean useEmptyGlossary;
	
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
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
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
			@Named("MarkupDescriptionTreatmentTransformer_ParallelProcessing")boolean parallelProcessing, 
			@Named("MarkupDescriptionTreatmentTransformer_DescriptionExtractorRunMaximum")int descriptionExtractorRunMaximum, 
			@Named("MarkupDescriptionTreatmentTransformer_SentenceChunkerRunMaximum")int sentenceChunkerRunMaximum,  
			OTOClient otoClient, 
			OTOLiteClient otoLiteClient, 
			@Named("OTOLiteTermReviewURL") String otoLiteTermReviewURL,
			@Named("DatabaseHost") String databaseHost,
			@Named("DatabasePort") String databasePort,
			@Named("DatabaseName")String databaseName,
			@Named("DatabaseUser")String databaseUser,
			@Named("DatabasePassword")String databasePassword,
			@Named("DatabasePrefix")String databasePrefix, 
			@Named("TaxonGroup")TaxonGroup taxonGroup,
			IGlossary glossary, 
			@Named("SelectedSources")Set<String> selectedSources, 
			@Named("GlossaryTable")String glossaryTable,
			@Named("termCategorizationRequired")boolean termCategorizationRequired,
			IInflector inflector, 
			@Named("EtcUser")String etcUser, 
			@Named("UseEmptyGlossary") boolean useEmptyGlossary) throws ClassNotFoundException, SQLException {
		super(version, parallelProcessing);
		this.inflector = inflector;
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
		this.taxonGroup = taxonGroup;
		this.selectedSources = selectedSources;
		this.glossaryTable = glossaryTable;
		this.termCategorizationRequired = termCategorizationRequired;
		this.etcUser = etcUser;
		this.useEmptyGlossary = useEmptyGlossary;
		
		//normalizer.init(); //moved after learn is complete so normalizer can read the results from learn
		
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://" + databaseHost + ":" + databasePort +"/" + databaseName + "?connectTimeout=0&socketTimeout=0&autoReconnect=true",
				databaseUser, databasePassword);
	}

	@Override
	public Processor transform(List<AbstractDescriptionsFile> descriptionsFiles) throws TransformationException, LearnException {
		//download gloss again from real OTO because the last download is no longer in memory
		//it is possible for gloss o change from last run, make sure to grab the correct version.
		//when remove MYSQL, take care of this issue
		
		//TODO: String version = otoClient.getLatestVersion();
		//String tablePrefix = glossaryType + "_" + glossaryDownload.getVersion();
		//String glossaryTable = tablePrefix + "_glossary";
		//if(!glossaryExistsLocally(tablePrefix)) {
		
		GlossaryDownload glossaryDownload = new GlossaryDownload();
		String glossaryVersion = "N/A";
		if(!useEmptyGlossary) {
			glossaryVersion = getGlossaryVersionOfLearn();
			if(glossaryVersion == null)
				glossaryVersion = "latest";
			
			otoClient.open();
			Future<GlossaryDownload> futureGlossaryDownload = otoClient.getGlossaryDownload(taxonGroup.getDisplayName(), glossaryVersion);
			
			try {
				glossaryDownload = futureGlossaryDownload.get();
			} catch (Exception e) {
				otoClient.close();
				log(LogLevel.ERROR, "Couldn't download glossary " + taxonGroup.getDisplayName() + " version: " + glossaryVersion, e);
				throw new TransformationException();
			}
			otoClient.close();
			
			glossaryVersion = glossaryDownload.getVersion();
		}
				
        UploadResult uploadResult = null;
        Download download = null;
		try {
			uploadResult = readUploadResult();
		} catch (SQLException e) {
			this.log(LogLevel.ERROR, "Problem reading upload result", e);
			throw new TransformationException();
		}
		
		 
		if(uploadResult != null) {
			try {
				otoLiteClient.open();
				Future<Download> futureDownload = otoLiteClient.getDownload(uploadResult);
				download = futureDownload.get();
				otoLiteClient.close();
			} catch(InterruptedException | ExecutionException e) {
				otoLiteClient.close();
				this.log(LogLevel.ERROR, "Problem downloading oto lite categorizations for upload " + uploadResult.getUploadId(), e);
				throw new TransformationException();
			}
		}

		
		if(download == null)
			throw new TransformationException();
        if(!download.isFinalized() && termCategorizationRequired) {
        	log(LogLevel.ERROR, "The term categorization has to be finalized to run markup. Please return to categorizing terms and finalize first.");
        	throw new TransformationException();
        }
		
		log(LogLevel.DEBUG, "Size of permanent glossary downloaded:\n" +
				"Number of term categoy relations " + glossaryDownload.getTermCategories().size() + "\n" +
				"Number of term synonym relations " + glossaryDownload.getTermSynonyms().size());
		if(download != null) 
			log(LogLevel.DEBUG, "Size of temporary glossary downloaded:\n" +
					"Number of term categoy relations " + download.getDecisions().size() + "\n" +
					"Number of term synonym relations " + download.getSynonyms().size());
		//storeInLocalDB(glossaryDownload, download, this.databasePrefix);
		 
		initGlossary(glossaryDownload, download); //turn "_" in glossary terms to "-"
		
		//this is needed to initialize terminologylearner (DatabaseInputNoLearner / fileTreatments)
		//even though no actual learning is taking place
		//Question TODO  all term category info are in the glossary, then why do we need terminologyLearner?
		terminologyLearner.learn(descriptionsFiles, glossaryTable);
		terminologyLearner.readResults(descriptionsFiles);
		
		normalizer.init();
		

		Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker = 
				terminologyLearner.getSentencesForOrganStateMarker(); //sentence level markup: modifier##tag##sentence text
		// do the actual markup
		markupDescriptions(descriptionsFiles, sentencesForOrganStateMarker);		

		Processor processor = new Processor();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		processor.setDate(dateFormat.format(new Date()));
		processor.setOperator(etcUser);
		Resource resource = new Resource();
		resource.setName(taxonGroup.getDisplayName());
		resource.setType("OTO Glossary");
		resource.setVersion(glossaryVersion);
		Software software = new Software();
		software.setName("CharaParser");
		software.setType("Semantic Markup");
		software.setVersion(version);
		processor.setSoftware(software);
		if(!useEmptyGlossary)
			processor.setResource(resource);
		
		return processor;
	}
	
    private UploadResult readUploadResult() throws SQLException {
        int uploadId = -1;
        String secret = "";
        String sql = "SELECT oto_uploadid, oto_secret FROM datasetprefixes WHERE prefix = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, databasePrefix);
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getResultSet();
        while(resultSet.next()) {
                uploadId = resultSet.getInt("oto_uploadid");
                secret = resultSet.getString("oto_secret");
        }
        return new UploadResult(uploadId, secret);
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
	 * notes: OTO Webservice should probably only return one term category list.
	 * No need to return an extra term synonym list just because it might make sense to have them separate in a relational database schema
	 * 
	 * notes: returning a term synonym list makes sense, but here we need to add syn info to the glossary.
	 * 
	 * Merge glossaryDownload and download to one glossary which holds both terms and synonyms
	 * For structure terms, both singular and plural forms are included in the synonyms
	 * @param otoGlossary
	 */
	protected void initGlossary(GlossaryDownload glossaryDownload, Download download) {

		//add the syn set of the glossary
		HashSet<Term> gsyns = new HashSet<Term>();
		for(TermSynonym termSyn: glossaryDownload.getTermSynonyms()){

			//if(termSyn.getCategory().compareTo("structure")==0){
			if(termSyn.getCategory().matches(ElementRelationGroup.entityElements)){
				//take care of singular and plural forms
				String syns = ""; 
				String synp = "";
				String terms = "";
				String termp = "";
				if(inflector.isPlural(termSyn.getSynonym().replaceAll("_",  "-"))){
					synp = termSyn.getSynonym().replaceAll("_",  "-");
					syns = inflector.getSingular(synp);					
				}else{
					syns = termSyn.getSynonym().replaceAll("_",  "-");
					synp = inflector.getPlural(syns);
				}

				if(inflector.isPlural(termSyn.getTerm().replaceAll("_",  "-"))){
					termp = termSyn.getTerm().replaceAll("_",  "-");
					terms = inflector.getSingular(termp);					
				}else{
					terms = termSyn.getTerm().replaceAll("_",  "-");
					termp = inflector.getPlural(terms);
				}
				glossary.addSynonym(syns, termSyn.getCategory(), terms);
				glossary.addSynonym(synp, termSyn.getCategory(), termp);
				gsyns.add(new Term(syns, termSyn.getCategory()));
				gsyns.add(new Term(synp, termSyn.getCategory()));
			}else{
				//glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), "arrangement", termSyn.getTerm());
				glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory(), termSyn.getTerm());
				gsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory()));
				//gsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), "arrangement"));
			}
		}

		//the glossary, excluding gsyns
		for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
			if(!gsyns.contains(new Term(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory())))
				glossary.addEntry(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory()); //primocane_foliage =>primocane-foliage Hong 3/2014
		}

		//add syn set of term_category
		HashSet<Term> dsyns = new HashSet<Term>();
		if(download != null) {
			for(Synonym termSyn: download.getSynonyms()){
				//Hong TODO need to add category info to synonym entry in OTOLite
				//if(termSyn.getCategory().compareTo("structure")==0){
				if(termSyn.getCategory().matches(ElementRelationGroup.entityElements)){
					//take care of singular and plural forms
					String syns = ""; 
					String synp = "";
					String terms = "";
					String termp = "";
					if(inflector.isPlural(termSyn.getSynonym().replaceAll("_",  "-"))){
						synp = termSyn.getSynonym().replaceAll("_",  "-");
						syns = inflector.getSingular(synp);					
					}else{
						syns = termSyn.getSynonym().replaceAll("_",  "-");
						synp = inflector.getPlural(syns);
					}

					if(inflector.isPlural(termSyn.getTerm().replaceAll("_",  "-"))){
						termp = termSyn.getTerm().replaceAll("_",  "-");
						terms = inflector.getSingular(termp);					
					}else{
						terms = termSyn.getTerm().replaceAll("_",  "-");
						termp = inflector.getPlural(terms);
					}
					//glossary.addSynonym(syns, termSyn.getCategory(), terms);
					//glossary.addSynonym(synp, termSyn.getCategory(), termp);
					//dsyns.add(new Term(syns, termSyn.getCategory());
					//dsyns.add(new Term(synp, termSyn.getCategory());
					glossary.addSynonym(syns, termSyn.getCategory(), terms);
					glossary.addSynonym(synp,termSyn.getCategory(), termp);
					dsyns.add(new Term(syns, termSyn.getCategory()));
					dsyns.add(new Term(synp, termSyn.getCategory()));
				}else{//forking_1 and forking are syns 5/5/14 hong test, shouldn't _1 have already been removed?
					glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory(), termSyn.getTerm());
					dsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory()));
				}					
			}

			//term_category from OTO, excluding dsyns
			for(Decision decision : download.getDecisions()) {
				if(!dsyns.contains(new Term(decision.getTerm().replaceAll("_",  "-"), decision.getCategory())))//calyx_tube => calyx-tube
					glossary.addEntry(decision.getTerm().replaceAll("_",  "-"), decision.getCategory());  
			}
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
			//if(termCategory.getCategory().equalsIgnoreCase("structure")) {
			if(termCategory.getCategory().toLowerCase().matches(ElementRelationGroup.entityElements)) {
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
	        stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_syns (`term` varchar(200) DEFAULT NULL, `synonym` varchar(200) DEFAULT NULL)  CHARACTER SET utf8 engine=innodb");
			stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_term_category (`term` varchar(100) DEFAULT NULL, `category` varchar(200) " +
					"DEFAULT NULL, `hasSyn` tinyint(1) DEFAULT NULL)  CHARACTER SET utf8 engine=innodb");
			stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_wordroles (`word` varchar(50) NOT NULL DEFAULT '', `semanticrole` varchar(2) " +
					"NOT NULL DEFAULT '', `savedid` varchar(40) DEFAULT NULL, PRIMARY KEY (`word`,`semanticrole`))  CHARACTER SET utf8 engine=innodb");
			
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
	protected void markupDescriptions(List<AbstractDescriptionsFile> descriptionsFiles, Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker) {
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
			for(int i=0; i<descriptionsFile.getDescriptions().size(); i++) {
				Description description = descriptionsFile.getDescriptions().get(i);
				//start a DescriptionExtractorRun for the treatment to process as a separate thread
				DescriptionExtractorRun descriptionExtractorRun = new DescriptionExtractorRun(
						descriptionsFile, description, i, normalizer, wordTokenizer, 
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
				description.setText("");
				/*try {
					//description.setText(futureNewDescription.get().getText());
				} catch (Exception e) {
					log(LogLevel.DEBUG, "Problem getting Future from new description", e);
				}*/
				log(LogLevel.DEBUG, " -> JAXB: ");
				log(LogLevel.DEBUG, description.toString());
			}
		}
	}

}
