package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.oto.client.WordRole;
import edu.arizona.biosemantics.oto.client.lite.OTOLiteClient;
import edu.arizona.biosemantics.oto.client.oto.OTOClient;
import edu.arizona.biosemantics.oto.model.GlossaryDownload;
import edu.arizona.biosemantics.oto.model.TermCategory;
import edu.arizona.biosemantics.oto.model.TermSynonym;
import edu.arizona.biosemantics.oto.model.lite.Decision;
import edu.arizona.biosemantics.oto.model.lite.Download;
import edu.arizona.biosemantics.oto.model.lite.Sentence;
import edu.arizona.biosemantics.oto.model.lite.Synonym;
import edu.arizona.biosemantics.oto.model.lite.Term;
import edu.arizona.biosemantics.oto.model.lite.Upload;
import edu.arizona.biosemantics.oto.model.lite.UploadResult;
import edu.arizona.biosemantics.semanticmarkup.config.Configuration;
import edu.arizona.biosemantics.semanticmarkup.db.ConnectionPool;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.lib.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ILearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFileList;

/**
 * OTOLearner learns by reading from an IVolumeReader and learning using an ITerminologyLearner
 * For learning, additional input is read from an IOTOClient
 * Moreover, learned results are handed to an IOTOLiteClient
 * This can be used for the first and hence the 'learn' application for the iPlant integration
 * @author rodenhausen
 */
public class OTOLearner implements ILearner {

	private ITerminologyLearner terminologyLearner;
	private OTOClient otoClient;
	private String databasePrefix;
	private String glossaryTable;
	private IPOSKnowledgeBase posKnowledgeBase;
	private TaxonGroup taxonGroup;
	private IGlossary glossary;
	private OTOLiteClient otoLiteClient;
	private String otoLiteTermReviewURL;
	private String otoLiteReviewFile;
	private String runRootDirectory;
	private IDescriptionReader descriptionReader;
	private String inputDirectory;
	private String bioportalAPIKey;
	private String bioportalUserId;
	private String etcUser;
	private String sourceOfDescriptions;
	private String units;
	private boolean useOtoCommuntiyDownload;
	private boolean useEmptyGlossary;
	private ConnectionPool connectionPool;
	private IInflector inflector;
	
	/**
	 * @param volumeReader
	 * @param terminologyLearner
	 * @param otoClient
	 * @param databasePrefix
	 * @param glossary
	 * @throws Exception
	 */
	@Inject
	public OTOLearner(@Named("DescriptionMarkupCreator_DescriptionReader")IDescriptionReader descriptionReader, 
			@Named("DescriptionReader_InputDirectory")String inputDirectory,
			ITerminologyLearner terminologyLearner, 
			OTOClient otoClient, 
			OTOLiteClient otoLiteClient,
			@Named("OTOLiteTermReviewURL") String otoLiteTermReviewURL,
			@Named("OTOLiteReviewFile") String otoLiteReviewFile,
			@Named("DatabasePrefix")String databasePrefix, 
			@Named("TaxonGroup")TaxonGroup taxonGroup,
			IGlossary glossary, 
			@Named("GlossaryTable")String glossaryTable, 
			@Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase, 
			@Named("Run_RootDirectory")String runRootDirectory,
			@Named("BioportalAPIKey")String bioportalAPIKey, 
			@Named("BioportalUserId")String bioportalUserId, 
			@Named("EtcUser")String etcUser, 
			@Named("SourceOfDescriptions")String sourceOfDescriptions,
			@Named("UseOtoCommunityDownload")boolean useOtoCommuntiyDownload,
			@Named("Units")String units,
			@Named("UseEmptyGlossary")boolean useEmptyGlossary, 
			IInflector inflector, 
			ConnectionPool connectionPool) throws Exception {  
		this.inputDirectory = inputDirectory;
		this.descriptionReader = descriptionReader;
		this.terminologyLearner = terminologyLearner;
		this.otoClient = otoClient;
		this.otoLiteClient = otoLiteClient;
		this.otoLiteTermReviewURL = otoLiteTermReviewURL;
		this.otoLiteReviewFile = otoLiteReviewFile;
		this.taxonGroup = taxonGroup;
		this.glossary = glossary;
		this.databasePrefix = databasePrefix;
		this.glossaryTable = glossaryTable;
		this.posKnowledgeBase = posKnowledgeBase;
		this.runRootDirectory = runRootDirectory;
		this.bioportalAPIKey = bioportalAPIKey;
		this.bioportalUserId = bioportalUserId;
		this.etcUser = etcUser;
		this.sourceOfDescriptions = sourceOfDescriptions;
		this.useOtoCommuntiyDownload = useOtoCommuntiyDownload;
		this.units = units;
		this.useEmptyGlossary = useEmptyGlossary;
		this.connectionPool = connectionPool;
		this.inflector = inflector;
	}
	
	@Override
	public void learn() throws Throwable {
		DescriptionsFileList descriptionsFileList = descriptionReader.read(inputDirectory);
		
		GlossaryDownload glossaryDownload = new GlossaryDownload();
		glossaryDownload.setVersion("N/A");
		if(!useEmptyGlossary) {
			glossaryDownload = getGlossaryDownload();
			log(LogLevel.INFO, "Loaded oto glossary with term-categories: " + glossaryDownload.getTermCategories().size() + " and "
					+ "synonyms: " + glossaryDownload.getTermSynonyms().size());
			
			if(useOtoCommuntiyDownload) {
				Download communityDownload = getCommunityDownload(glossaryDownload);
				if(communityDownload != null) {
					log(LogLevel.INFO, "Downloaded oto community decisions with categorizatino decisions: "
							+ "" + communityDownload.getDecisions().size() + " and "
							+ "synonyms: " + communityDownload.getSynonyms().size());
					
					for(Decision decision : communityDownload.getDecisions()) {
						glossaryDownload.getTermCategories().add(new TermCategory(decision.getTerm(), decision.getCategory(), 
								decision.isHasSynonym(), decision.getSourceDataset(), decision.getId()));
					}
					for(Synonym synonym : communityDownload.getSynonyms()) {
						glossaryDownload.getTermSynonyms().add(new TermSynonym(
								synonym.getTerm(), synonym.getCategory(), synonym.getSynonym(), synonym.getId()));
					}
				}
			}
		}
		
		storeInLocalDB(glossaryDownload, this.databasePrefix);
		//glossary is needed to prematch phrases
		initGlossary(glossaryDownload);
		terminologyLearner.learn(descriptionsFileList.getDescriptionsFiles(), glossaryTable);
		UploadResult uploadResult = sendLearnedResultToOtoLite();
		storeUploadToDB(uploadResult, glossaryDownload);
		storeReviewToFile(uploadResult);
	}


	private void storeReviewToFile(UploadResult uploadResult) throws IOException {
		//store URL that uses upload id in a local file so that user can look it up
		/*FileWriter fw = new FileWriter(runRootDirectory + File.separator + otoLiteReviewFile);  
		fw.write("Please visit the link [1] below to categorize a selection of terms that appeared in the descriptions you provided as input. ");
		fw.write("Categorizing these terms will ensure you obtain the best results possible from CharaParser Markup.\n\n");
		fw.write("The categorization can be done by selecting terms on the left and dragging and dropping the arrow into the corresponding category on the right-hand side.\n\n");
		fw.write("[1]: " + this.otoLiteTermReviewURL + "?uploadID=" + uploadId);  
		fw.close();*/
		
		FileWriter fw = new FileWriter(runRootDirectory + File.separator + otoLiteReviewFile);  
		fw.write("<!DOCTYPE html>");
		fw.write("<html>");
		fw.write("<head>");
		String link = this.otoLiteTermReviewURL + "?id=" + uploadResult.getUploadId() + "&secret=" +
				uploadResult.getSecret() + "&origin=iplant";
		fw.write("<meta http-equiv=\"refresh\" content=\"0; url=" + link + "\" charset=\"UTF-8\">");
		fw.write("<title>Term categorization</title>");
		fw.write("</head>");
		fw.write("<body>");
		fw.write("If you are not redirected automatically, please click the link <a href=\"" + link + "\">" 
				+ link + "</a> " +
				"to categorize a selection of terms that appeared in the descriptions you provided as input. Categorizing these terms will ensure you obtain the best " +
				"results possible from CharaParser Markup.");
		//fw.write("<br /><br />");
		//fw.write("The categorization can be done by selecting terms on the left and dragging and dropping the arrow into the corresponding category on the right-hand side.");
		fw.write("</body>");
		fw.write("</html>");
		fw.close();
	}

	private void storeUploadToDB(UploadResult uploadResult, GlossaryDownload glossaryDownload) throws SQLException {
		//store uploadid for the prefix so it is available for the markup part (filesystem cannot be used as a tool's directory is cleanedup after each run in the
		//iplant environment, hence the dependency on mysql can for iplant not completely be removed
		try(Connection connection = connectionPool.getConnection()) {
			String sql = "UPDATE datasetprefixes SET oto_uploadid = ?, oto_secret = ?, glossary_version = ? WHERE prefix = ?";
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				preparedStatement.setInt(1, uploadResult.getUploadId());
				preparedStatement.setString(2, uploadResult.getSecret());
				preparedStatement.setString(3, glossaryDownload.getVersion());
				preparedStatement.setString(4, databasePrefix);
				preparedStatement.execute();
			}
		}
	}

	private UploadResult sendLearnedResultToOtoLite() throws InterruptedException, ExecutionException {
		otoLiteClient.open();
		Future<UploadResult> futureUploadResult = otoLiteClient.putUpload(readUpload());
		UploadResult uploadResult = futureUploadResult.get();
		otoLiteClient.close();
		return uploadResult;
	}

	private Download getCommunityDownload(GlossaryDownload glossaryDownload) {
		log(LogLevel.INFO, "Will download oto community decisions to add additionally to glossary used");
		otoLiteClient.open();
		Future<Download> futureCommunityDownload = otoLiteClient.getCommunityDownload(taxonGroup.getDisplayName());
		
		boolean downloadSuccessful = false;
		Download communityDownload = null;
		try {
			communityDownload = futureCommunityDownload.get();
			downloadSuccessful = communityDownload != null;
		} catch(Throwable t) {
			log(LogLevel.ERROR, "Couldn't download glossary will fallback to locally stored glossary", t);
		}

		otoLiteClient.close();
		if(downloadSuccessful) 
			storeToLocalCommunityDownload(communityDownload, taxonGroup);
		else
			try {
				communityDownload = getLocalCommunityDownload(taxonGroup);
			} catch (ClassNotFoundException | IOException e) {
				log(LogLevel.ERROR, "Couldn't get local community download for taxon group " + taxonGroup, e);
				return null;
			}
		return communityDownload;
	}

	private void storeToLocalCommunityDownload(Download communityDownload, TaxonGroup taxonGroup) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Configuration.glossariesDownloadDirectory + File.separator + 
					"CommunityDownload." + taxonGroup.getDisplayName() + ".ser"));
			out.writeObject(communityDownload);
		    out.close();
		} catch(Exception e) {
			log(LogLevel.ERROR, "Couldn't store glossaryDownload locally", e);
		}
	}

	private Download getLocalCommunityDownload(TaxonGroup taxonGroup) throws ClassNotFoundException, IOException {
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(Configuration.glossariesDownloadDirectory + File.separator + 
				"CommunityDownload." + taxonGroup.getDisplayName() + ".ser"));
		Download communityDownload = (Download) objectIn.readObject();
		objectIn.close();
		return communityDownload;
	}

	private GlossaryDownload getGlossaryDownload() throws ClassNotFoundException, IOException {
		otoClient.open();
		Future<GlossaryDownload> futureGlossaryDownload = otoClient.getGlossaryDownload(taxonGroup.getDisplayName());
		
		boolean downloadSuccessful = false;
		GlossaryDownload glossaryDownload = null;
		try {
			glossaryDownload = futureGlossaryDownload.get();
			downloadSuccessful = glossaryDownload != null;
		} catch(Throwable t) {
			log(LogLevel.ERROR, "Couldn't download glossary will fallback to locally stored glossary", t);
		}
		
		otoClient.close();
		if(downloadSuccessful) 
			storeToLocalGlossaryDownload(glossaryDownload, taxonGroup);
		else 
			glossaryDownload = getLocalGlossaryDownload(taxonGroup);
		return glossaryDownload;
	}

	private void storeToLocalGlossaryDownload(GlossaryDownload glossaryDownload, TaxonGroup taxonGroup) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Configuration.glossariesDownloadDirectory + File.separator + 
					"GlossaryDownload." + taxonGroup.getDisplayName() + ".ser"));
			out.writeObject(glossaryDownload);
		    out.close();
		} catch(Exception e) {
			log(LogLevel.ERROR, "Couldn't store glossaryDownload locally", e);
		}
	}

	private GlossaryDownload getLocalGlossaryDownload(TaxonGroup taxonGroup) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(Configuration.glossariesDownloadDirectory + File.separator + 
				"GlossaryDownload." + taxonGroup.getDisplayName() + ".ser"));
		GlossaryDownload glossaryDownload = (GlossaryDownload) objectIn.readObject();
		objectIn.close();
		return glossaryDownload;
	}

	/**
	 * Initialize the glossary passed to the learner, so that classes who share the glossary have access to an up-to-date version
	 * @param GlossaryDownload
	 */
	private void initGlossary(GlossaryDownload glossaryDownload) {
		//add the syn set of the glossary
		HashSet<edu.arizona.biosemantics.semanticmarkup.know.lib.Term> gsyns = new HashSet<edu.arizona.biosemantics.semanticmarkup.know.lib.Term>();
		for(TermSynonym termSyn: glossaryDownload.getTermSynonyms()){
			termSyn.setTerm(termSyn.getTerm().replaceAll("_+", "_")); //multiple _ cause inflector to throw exception.
			termSyn.setSynonym(termSyn.getSynonym().replaceAll("_+", "_"));
			//do not replaceAll("_", "-")
			//if(termSyn.getCategory().compareTo("structure")==0){
			if(termSyn.getCategory().matches(ElementRelationGroup.entityElements)){
				//take care of singular and plural forms
				String syns = ""; 
				String synp = "";
				String terms = "";
				String termp = "";
				if(inflector.isPlural(termSyn.getSynonym())){
					synp = termSyn.getSynonym();
					syns = inflector.getSingular(synp);					
				}else{
					syns = termSyn.getSynonym();
					synp = inflector.getPlural(syns);
				}
				if(inflector.isPlural(termSyn.getTerm())){
					termp = termSyn.getTerm();
					terms = inflector.getSingular(termp);					
				}else{
					terms = termSyn.getTerm();
					termp = inflector.getPlural(terms);
				}
				glossary.addSynonym(syns, termSyn.getCategory(), terms);
				glossary.addSynonym(synp, termSyn.getCategory(), termp);
				gsyns.add(new edu.arizona.biosemantics.semanticmarkup.know.lib.Term(syns, termSyn.getCategory()));
				gsyns.add(new edu.arizona.biosemantics.semanticmarkup.know.lib.Term(synp, termSyn.getCategory()));
			}else{
				glossary.addSynonym(termSyn.getSynonym(), termSyn.getCategory(), termSyn.getTerm());
				gsyns.add(new edu.arizona.biosemantics.semanticmarkup.know.lib.Term(termSyn.getSynonym(), termSyn.getCategory()));
			}
		}

		//the glossary, excluding gsyns
		for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
			if(!gsyns.contains(new edu.arizona.biosemantics.semanticmarkup.know.lib.Term(termCategory.getTerm(), termCategory.getCategory())))
				glossary.addEntry(termCategory.getTerm(), termCategory.getCategory()); 
		}
		
		//previous code, without loading synonyms
		//for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
		//	this.glossary.addEntry(termCategory.getTerm(), termCategory.getCategory());
		//}
	}	
	
	private void storeInLocalDB(GlossaryDownload glossaryDownload, String tablePrefix) {
		List<WordRole> wordRoles = new LinkedList<WordRole>();
		HashSet<String> wordSet = new HashSet<String>();
		for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
			//remove duplicates, term is primary key in the table
			if(wordSet.contains(termCategory.getTerm()))
				continue;
			wordSet.add(termCategory.getTerm());
			
			WordRole wordRole = new WordRole();
			wordRole.setWord(termCategory.getTerm());
			String semanticRole = "c";
			//if(termCategory.getCategory().equalsIgnoreCase("structure")) {
			if(termCategory.getCategory().toLowerCase().matches(ElementRelationGroup.entityElements)){
				semanticRole = "op";
			}
			wordRole.setSemanticRole(semanticRole);
			wordRole.setSavedid(""); //not really needed, is a left over from earlier charaparser times
			wordRoles.add(wordRole);
		}
		

		try(Connection connection = connectionPool.getConnection()) {
			try(Statement stmt = connection.createStatement()) {
		        String cleanupQuery = "DROP TABLE IF EXISTS " + 
										tablePrefix + "_term_category, " + 
										tablePrefix + "_syns, " +
										tablePrefix + "_wordroles, " +
										this.glossaryTable + ";";
		        stmt.execute(cleanupQuery);
		        stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_syns (`term` varchar(200) DEFAULT NULL, `synonym` varchar(200) DEFAULT NULL) "
		        		+ "CHARACTER SET utf8 engine=innodb");
				stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_term_category (`term` varchar(100) DEFAULT NULL, `category` varchar(200) " +
						"DEFAULT NULL, `hasSyn` tinyint(1) DEFAULT NULL) CHARACTER SET utf8 engine=innodb");
				stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_wordroles (`word` varchar(200) NOT NULL DEFAULT '', `semanticrole` varchar(2) " +
						"NOT NULL DEFAULT '', `savedid` varchar(40) DEFAULT NULL, PRIMARY KEY (`word`,`semanticrole`)) CHARACTER SET utf8 engine=innodb;");
				stmt.execute("CREATE TABLE IF NOT EXISTS " + glossaryTable + " (`term` varchar(100) DEFAULT NULL, `category` varchar(200) " +
						"DEFAULT NULL, `hasSyn` tinyint(1) DEFAULT NULL) CHARACTER SET utf8 engine=innodb");
				
				for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
					try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tablePrefix + "_term_category (`term`, `category`, `hasSyn`) VALUES (?, ?, ?)")) {
						preparedStatement.setString(1, termCategory.getTerm());
						preparedStatement.setString(2, termCategory.getCategory());
						preparedStatement.setBoolean(3, termCategory.isHasSyn());
						preparedStatement.executeUpdate();
					}
				}
				for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
					try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + glossaryTable + " (`term`, `category`, `hasSyn`) VALUES (?, ?, ?)")) {
						preparedStatement.setString(1, termCategory.getTerm());
						preparedStatement.setString(2, termCategory.getCategory());
						preparedStatement.setBoolean(3, termCategory.isHasSyn());
						preparedStatement.executeUpdate();
					}
				}
				for(TermSynonym termSynonym : glossaryDownload.getTermSynonyms()) {
					try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tablePrefix + "_syns (`term`, `synonym`) VALUES (?, ?)")) {
						preparedStatement.setString(1, termSynonym.getTerm());
						preparedStatement.setString(2, termSynonym.getSynonym());
						preparedStatement.executeUpdate();
					}
				}
				for(WordRole wordRole : wordRoles) {
					try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tablePrefix + "_wordroles" + " VALUES (?, ?, ?)")) {
						preparedStatement.setString(1, wordRole.getWord());
						preparedStatement.setString(2, wordRole.getSemanticRole());
						preparedStatement.setString(3, wordRole.getSavedid());
						preparedStatement.executeUpdate();
					}
				}
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, "Problem storing glossary in local DB", e);
		}
	}

	private boolean glossaryExistsLocally(String tablePrefix) throws SQLException {
		try(Connection connection = connectionPool.getConnection()) {
			DatabaseMetaData metadata = connection.getMetaData();
			boolean result = true;
			try(ResultSet resultSet = metadata.getTables(null, null, tablePrefix + "_term_category", null)) {
				result &= resultSet.next();
			}
			try(ResultSet resultSet = metadata.getTables(null, null, tablePrefix + "_syns", null)) {
				result &= resultSet.next();	
			}
			try(ResultSet resultSet = metadata.getTables(null, null, tablePrefix + "_wordroles", null)) {
				result &= resultSet.next();	
			}
			return result;
		}
	}

	private Upload readUpload() {
		Upload upload = new Upload();
		upload.setBioportalAPIKey(bioportalAPIKey);
		upload.setBioportalUserId(bioportalUserId);
		upload.setUser(etcUser);
		upload.setSource(sourceOfDescriptions);
		
		List<String> taxonNames = getTaxonNames();
		
		List<Term> taxonNameTerms = new ArrayList<Term>();
		for(String name: taxonNames){
			taxonNameTerms.add(new Term(name));
		}
		upload.setPossibleTaxonNames(taxonNameTerms);

		List<Term> terms = getStructures(taxonNames);
		//for(Term t: terms){
		//	System.out.println(t.getTerm());
		//}
		upload.setPossibleStructures(terms);
		
		
		
		terms = getCharacters();
		//for(Term t: terms){
		//	System.out.println(t.getTerm());
		//}
		upload.setPossibleCharacters(terms);
		
		terms = getOtherTerms();
		//for(Term t: terms){
		//	System.out.println(t.getTerm());
		//}
		upload.setPossibleOtherTerms(terms);
		upload.setSentences(getSentences());
		upload.setGlossaryType(taxonGroup.getDisplayName());
		
		return upload;
	}

	private List<Sentence> getSentences() {
		List<Sentence> sentences = new LinkedList<Sentence>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select * from " + this.databasePrefix + "_sentence order by sentid")) {
					while(resultSet.next()) {
						sentences.add(new Sentence(resultSet.getInt("sentid"), resultSet.getString("source"), resultSet.getString("sentence"),
								resultSet.getString("originalsent")));
					}	
				}
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, "Problem accessing sentence table", e);
		}
		return sentences;
	}

	private List<Term> getOtherTerms() {
		List<Term> result = new ArrayList<Term>();
		try {
			List<String> otherTerms = this.fetchContentTerms();
			for(String otherTerm : otherTerms) 
				result.add(new Term(otherTerm));
		} catch (Exception e) {
			log(LogLevel.ERROR, "Problem fetching content terms", e);
		}
		return result;
	}

	private List<Term> getCharacters() {
		List<Term> result = new ArrayList<Term>();
		try {
			List<String> characterTerms = this.fetchCharacterTerms();
			for(String characterTerm : characterTerms) 
				result.add(new Term(characterTerm));
		} catch (Exception e) {
			log(LogLevel.ERROR, "Problem fetching character terms", e);
		}
		return result;
	}

	private List<String> getTaxonNames() {
		List<String> result = new ArrayList<String>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement stmt = connection.createStatement()) {
				try(ResultSet rs = stmt.executeQuery("select name from "+this.databasePrefix+"_taxonnames")) {
					while(rs.next())
						result.add(rs.getString("name"));	
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log(LogLevel.ERROR, "Problem fetching taxon names", e);
		}
		return result;
	}
	
	
	private List<Term> getStructures(List<String> remove) {
		List<Term> result = new ArrayList<Term>();
		try {
			List<String> structureTerms = this.fetchStructureTerms();
			for(String structureTerm : structureTerms) {
				if(!remove.contains(structureTerm)){
					Term t = new Term(structureTerm);
					result.add(t);
				}
			}
		} catch (Exception e) {
			log(LogLevel.ERROR, "Problem fetching structure terms", e);
		}
		return result;
	}

	@Override
	public String getDescription() {
		return this.getClass().toString();
	}
	
	
	
	/**
	 * this procedure seems to be slow and only a handful of terms are filtered.
	 * 1. search db for candidate structure terms
	 * 2. apply heuristic rules to filter the terms
	 * 		2.1 pos = v|adv
	 * 		2.2 does not ...
	 * 		2.3 by [means] of
	 * 3. filtered terms are not displayed and they will not be saved to wordroles table as "os" or "op".
	 * 4. terms filtered by 2.1.adv or 2.3 will be saved in NONEQTERMSTABLE
	 * 5. cache results to reduce cost
	 * 
	 * @return filtered candidate structure words
	 * @throws Exception 
	 */
	private ArrayList<String> fetchStructureTerms() throws Exception{
		ArrayList <String> words = new ArrayList<String>();
		ArrayList <String> filteredwords = new ArrayList<String>();
		ArrayList <String> noneqwords = new ArrayList<String>();
	
		words = this.structureTags4Curation(words);
		
		for(String word: words){
			if(word.compareToIgnoreCase("ditto")==0) continue;
			if(word.compareToIgnoreCase("general")==0) continue;
			if(word.length()==0) continue;
			if(word.startsWith("[") && word.endsWith("]")) continue;
			//before structure terms are set, partOfPrepPhrases can not be reliability determined
			//getMostLikelyPOS does stemming, while isVerb does not.
			if(isNoise(word)) continue;
			if(!word.endsWith("ed") && (posKnowledgeBase.getMostLikleyPOS(word) == edu.arizona.biosemantics.semanticmarkup.ling.pos.POS.VB 
				|| posKnowledgeBase.isAdverb(word) /*|| Utilities.partOfPrepPhrase(word, this.conn, prefix)*/)){
				//if(Utilities.mustBeAdv(word) /*|| Utilities.partOfPrepPhrase(word, this.conn, prefix)*/){
					noneqwords.add(word);
					log(LogLevel.DEBUG, word+" [non-ed-ending verb/adv] is considered an non-eq term and removed");
				//}					
				continue;
			}
			filteredwords.add(word);
		}
		//mainDb.recordNonEQTerms(noneqwords, null, null);
		words = null;

		return filteredwords;
	}
	
	
	  /**
     * display learned new structures in structures subtab in step 4 (perl markup) for curation.
     * 
     * @param tagList
     * @throws ParsingException
     * @throws SQLException
     */
    public ArrayList<String> structureTags4Curation(List <String> tagList) throws Exception {
    	String filter1 = "";
 		String filter2 = "";
 		String filter3 = "";
 		try(Connection connection = connectionPool.getConnection()) {
			try(Statement stmt1 = connection.createStatement()) {
				try(ResultSet rs1 = stmt1.executeQuery("show tables")) {
					while(rs1.next()){
						if(rs1.getString(1).equals("noneqterms")) {
							filter1 = " and tag not in (select word from noneqterms) "; 
							filter2 = " and plural not in (select word from noneqterms) " ;
							filter3 = " and word not in (select word from noneqterms) ";
						}
					}
				}
			}
 		}

		String sql = "select distinct tag as structure from "+this.databasePrefix+"_sentence where tag != 'unknown' and tag is not null and tag not like '% %' " +
		filter1 +
		"union select distinct plural as structure from "+this.databasePrefix+"_singularplural"+","+ this.databasePrefix+"_sentence where singular=tag "+
		filter2 +
		"order by structure"; 
		try(Connection connection = connectionPool.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement(sql)) {
				try(ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						String tag = rs.getString("structure");
						populateCurationList(tagList, tag); //select tags for curation, filter against the glossary
					}
					sql = "select distinct word from "+this.databasePrefix+"_wordpos where pos in ('p', 's', 'n') and saved_flag !='red' "+
					filter3+" order by word";
					try(PreparedStatement s = connection.prepareStatement(sql)) {
						try(ResultSet r = s.executeQuery()) {
							while (r.next()) {
								String tag = r.getString("word");
								try(PreparedStatement stmtSentence = connection.prepareStatement("select * from " + this.databasePrefix + "_sentence where sentence like '% " + tag + "%'")) {
									try(ResultSet rs2 = stmtSentence.executeQuery()) {
										if (rs2.next()) {
											populateCurationList(tagList, tag); //select tags for curation, filter against the glossary
										}
									}
								}	
							}
							return deduplicateSort(tagList);
						}
					}
				}
			}
		}
    }
    
	private ArrayList<String> deduplicateSort(List<String> tagList) {
		HashSet<String> set = new HashSet<String>(tagList);
		String[] sorted = set.toArray(new String[]{}); 
		Arrays.sort(sorted);
		ArrayList<String> results = new ArrayList<String>();
		for(int i=0; i<sorted.length; i++){
			results.add(sorted[i]);
		}
		return results;
	}
    
    /**
     * if word in glossary, add it to wordroles
     * if not in glossary, add to curationList
     * @param curationList
     * @param word
     * @throws Exception 
     */
	private void populateCurationList(List<String> curationList, String word) throws Exception {
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement stmt = connection.createStatement()) {
				try(ResultSet rs = stmt.executeQuery("select category from "+this.glossaryTable+" where term ='"+word+"'")) {
					if(rs.next()){
						String cat = rs.getString("category");
						if(cat.matches("(substance|nominative|structure|life_style|growth_form|structure_subtype|structure_in_adjective_form|taxon_name)")){
							add2WordRolesTable(word, "op");
						} else {
							add2WordRolesTable(word, "c");
						}
					} else {
						curationList.add(word);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param w
	 * @param role
	 */
	private void add2WordRolesTable(String w, String role) throws Exception {
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement stmt = connection.createStatement()) {
				try(ResultSet rs = stmt.executeQuery("select * from "+this.databasePrefix + "_wordroles where word='"+w+"' and semanticrole='"+role+"'")) {
					if(!rs.next()){
						stmt.execute("insert into "+this.databasePrefix+"_wordroles (word, semanticrole) values ('"+w+"','"+role+"')");
					}
				}
			}
		}
	}

	/**
	 * 1. search db for candidate character terms
	 * 2. apply heuristic rules to filter the terms
	 * 		2.1 pos = adv
	 * 		2.2 by [means] of
	 * 3. filtered terms are not displayed and they will not be saved to wordroles table as "os" or "op".
	 * 4. terms filtered by 2.1.adv or 2.2 will be saved in NONEQTERMSTABLE
	 * 5. cache results to reduce cost
	 * @return filtered candidate character words
	 * @throws Exception 
	 */
	private ArrayList<String> fetchCharacterTerms() throws Exception{
		ArrayList <String> words = new ArrayList<String>();;
		ArrayList <String> filteredwords = new ArrayList<String>();
		ArrayList <String> noneqwords = new ArrayList<String>();

		words = descriptorTerms4Curation();
		for(String word: words){
			if(isNoise(word)) continue;
			if(!word.endsWith("ed") && (posKnowledgeBase.getMostLikleyPOS(word) == edu.arizona.biosemantics.semanticmarkup.ling.pos.POS.VB 
					|| posKnowledgeBase.isAdverb(word) /*|| Utilities.partOfPrepPhrase(word, this.conn, prefix)*/)){
				noneqwords.add(word);
				log(LogLevel.DEBUG, word+"[non-ed-ending verb/adv] is considered an non-eq term and removed");
				continue;
			}
			filteredwords.add(word);
		}
		//mainDb.recordNonEQTerms(noneqwords, null, null);

		return filteredwords;	
	}
	
	private boolean isNoise(String word){
		//TODO: move to configuration
		if(word.matches("times|time|and or|i e|e g|they|their|it|its|others|\\w+selves|\\w+self") || word.matches(units)|| word.matches(".*\\b(and|or)\\b.*")){
			log(LogLevel.DEBUG, word+" is considered a noise and removed");		
			return true;
		}
		
		if(word.length()==1){
			if(this.glossary.hasIndexedStructure() && word.matches("[ivx\\d]")){
				return false;
			}else{
				log(LogLevel.DEBUG, word+" is considered a noise and removed");		
				return true;
			}
		}
		return false;
	}
	private ArrayList<String> fetchContentTerms() throws Exception {
		ArrayList<String> words = new ArrayList<String>();
		ArrayList <String> filteredwords = new ArrayList<String>();
		ArrayList <String> noneqwords = new ArrayList<String>();

		ArrayList<String> inistructureterms = new ArrayList<String>();
		ArrayList<String> inicharacterterms = new ArrayList<String>();
		
		if(inistructureterms==null || inistructureterms.size()==0){
			inistructureterms = structureTags4Curation(new ArrayList<String>());
		}
		if(inicharacterterms==null || inicharacterterms.size()==0){
			inicharacterterms = descriptorTerms4Curation();
		}
		words = contentTerms4Curation(words, inistructureterms, inicharacterterms);
		
		for(String word: words){
			if(isNoise(word)) continue;
			if(!word.endsWith("ed") && (posKnowledgeBase.getMostLikleyPOS(word) == edu.arizona.biosemantics.semanticmarkup.ling.pos.POS.VB 
					|| posKnowledgeBase.isAdverb(word) /*|| Utilities.partOfPrepPhrase(word, this.conn, prefix)*/)){
				noneqwords.add(word);
				log(LogLevel.DEBUG, word+"[non-ed-ending verb/adv] is considered an non-eq term and removed");
				continue;
			}
			filteredwords.add(word);
		}
		//mainDb.recordNonEQTerms(noneqwords, null, null);
		return filteredwords;
	}
	
	   /**
     * display unknown terms in morestructure/moredescriptor subtabs 
     * in step 4 (perl markup) for curation.
     * @param curationList
     * @throws ParsingException
     * @throws SQLException
     */
    public ArrayList<String> contentTerms4Curation(List <String> curationList, ArrayList<String> structures, ArrayList<String> characters) throws Exception {
 		String filter = "";
 		try(Connection connection = connectionPool.getConnection()) {
	 		try(Statement stmt1 = connection.createStatement()) {
	 			try(ResultSet rs1 = stmt1.executeQuery("show tables")) {
	 				while(rs1.next()){
						if(rs1.getString(1).equals("noneqterms")){
							filter = " and dhword not in (select word from noneqterms)";
						}
					}
	 			}
	 		}
	 		String sql = "select dhword from "+this.databasePrefix+"_allwords" +
			//" where count>=3 and inbrackets=0 and dhword not like '%\\_%' and " +
			" where dhword not like '%\\_%' and " +		
			" dhword not in (select word from "+ this.databasePrefix+"_wordpos where saved_flag='red')"+
			filter +
			" and dhword not in (select word from "+ this.databasePrefix+"_wordroles) order by dhword";
			try(PreparedStatement stmt = connection.prepareStatement(sql)) {
				try(ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						String word = rs.getString("dhword").trim();
						if(!structures.contains(word) && !characters.contains(word)){
							populateCurationList(curationList, word);
						}
					}
					return this.deduplicateSort(curationList);
				}
			}
 		}
    }
	
	/**
	 * load descriptor subtab in step 4 (perl markup)
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<String> descriptorTerms4Curation() throws Exception {
		ArrayList<String> words = new ArrayList<String>();
		
		String filter = "";
 		try(Connection connection = connectionPool.getConnection()) {
	 		try(Statement stmt1 = connection.createStatement()) {
	 			try(ResultSet rs1 = stmt1.executeQuery("show tables")) {
					while(rs1.next()){
						if(rs1.getString(1).equals("noneqterms")) {
							filter = " and word not in (select word from noneqterms) ";
						}
					}
	 			}
	 		}
	 		
			String sql = "select word from " + this.databasePrefix + "_wordpos where pos=? and saved_flag !='red' "+
	 				filter+"order by word";
			//stmt = conn.prepareStatement("select word from "+this.tablePrefix+"_"+ApplicationUtilities.getProperty("POSTABLE")+" where pos=? and word not in (select distinct term from "+this.glossarytable+")");
			try(PreparedStatement stmt2 = connection.prepareStatement(sql)) {
				stmt2.setString(1, "b");
				try(ResultSet rset2 = stmt2.executeQuery()) {
					if (rset2 != null) {
						while(rset2.next()){
							String word = rset2.getString("word");
							try(PreparedStatement stmtSentence = connection.prepareStatement("select * from " + this.databasePrefix + "_sentence where sentence like '% " + word + "%'")) {
								try(ResultSet rs3 = stmtSentence.executeQuery()) {
									if (rs3.next()) {
										populateDescriptorList(words, word);
									}
								}
							}
						}	
					}
					words = deduplicateSort(words);
					return words;
 				}
 			}	
 		}
	}
	
	/**
	 * w is put into words only if 
	 * it is a word
	 * it is not a pronoun, a stopword, a preposition, an adv (-ly), or -shaped
	 * it is not in the glossary
	 * 
	 * if it is in the glossary, get its role from glossary and save it in wordroles table.
	 * @param words
	 * @param w
	 */
	private void populateDescriptorList(ArrayList<String> words, String w) throws Exception {
		if(w.matches(".*?\\w.*")){
			String wc = w;
			if(w.indexOf("-")>=0 || w.indexOf("_")>=0){
				String[] ws = w.split("[_-]");
				w = ws[ws.length-1];
			}

			try(Connection connection = connectionPool.getConnection()) {
				try(Statement stmt = connection.createStatement()) {
					try(ResultSet rset = stmt.executeQuery("select category from " + this.glossaryTable + " where term ='"+w+"'")) { 
						if(rset.next()){//in glossary
							String cat = rset.getString(1);
							if(cat.matches("\\b(STRUCTURE|FEATURE|SUBSTANCE|PLANT|nominative|structure)\\b")){
								add2WordRolesTable(wc, "os");
							}else{
								add2WordRolesTable(wc, "c");
							}
							
						}else{ //not in glossary
							words.add(wc);
						}
					}
				}
			}
		}
	}
}
