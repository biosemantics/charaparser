package semanticMarkup.ling.learn.lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.beans.Term;
import edu.arizona.sirls.beans.TermCategory;
import edu.arizona.sirls.beans.TermSynonym;
import edu.arizona.sirls.beans.WordRole;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.know.net.IOTOClient;
import semanticMarkup.know.net.LocalGlossary;
import semanticMarkup.know.net.OTOGlossary;
import semanticMarkup.ling.learn.ILearner;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.log.LogLevel;

/**
 * OTOLearner learns by reading from an IVolumeReader and learning using an ITerminologyLearner
 * For learning, additional input is read from an IOTOClient
 * Moreover, learned results are handed to an IOTOClient
 * This can be used for the first and hence the 'learn' application for the iPlant integration
 * @author rodenhausen
 */
public class OTOLearner implements ILearner {

	private IVolumeReader volumeReader;
	private ITerminologyLearner terminologyLearner;
	private IOTOClient otoClient;
	private String databasePrefix;
	private IGlossary glossary;
	private Connection connection;
	private String glossaryTable;
	private IPOSKnowledgeBase posKnowledgeBase;
	private String permanentGlossaryPrefix;
	
	/**
	 * @param volumeReader
	 * @param terminologyLearner
	 * @param otoClient
	 * @param databaseName
	 * @param databaseUser
	 * @param databasePassword
	 * @param databasePrefix
	 * @param glossary
	 * @throws Exception
	 */
	@Inject
	public OTOLearner(@Named("Learner_VolumeReader")IVolumeReader volumeReader, 
			ITerminologyLearner terminologyLearner, 
			IOTOClient otoClient, 
			@Named("databaseName")String databaseName,
			@Named("databaseUser")String databaseUser,
			@Named("databasePassword")String databasePassword,
			@Named("databasePrefix")String databasePrefix, 
			@Named("permanentGlossaryPrefix")String permanentGlossaryPrefix,
			IGlossary glossary, 
			@Named("GlossaryTable")String glossaryTable, 
			IPOSKnowledgeBase posKnowledgeBase) throws Exception {	
		this.volumeReader = volumeReader;
		this.terminologyLearner = terminologyLearner;
		this.otoClient = otoClient;
		this.permanentGlossaryPrefix = permanentGlossaryPrefix;
		this.databasePrefix = databasePrefix;
		this.glossary = glossary;
		this.glossaryTable = glossaryTable;
		this.posKnowledgeBase = posKnowledgeBase;
		
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + databaseName, databaseUser, databasePassword);
	}
	
	@Override
	public void learn() throws Exception {
		List<Treatment> treatments = volumeReader.read();
		
		//no prefix, simply use the glossary 
		OTOGlossary otoGlossary = otoClient.read(permanentGlossaryPrefix);
		storeInLocalDB(otoGlossary);

		//not really needed for learning part the in-memory glossary, not before markup step
		//initGlossary(otoGlossary);
		
		terminologyLearner.learn(treatments);
		otoClient.put(readLocalGlossary(), databasePrefix);		
	}
	
	/**
	 * Initialize the glossary passed to the learner, so that classes who share the glossary have access to an up-to-date version
	 * TODO: OTO Webservice should probably only return one term category list.
	 * No need to return an extra term synonym list just because it might make sense to have them seperate in a relational database schema
	 * @param otoGlossary
	 */
/*	private void initGlossary(OTOGlossary otoGlossary) {
		for(TermCategory termCategory : otoGlossary.getTermCategories()) {
			this.glossary.addEntry(termCategory.getTerm(), termCategory.getCategory());
		}
	}
*/
	
	private void storeInLocalDB(OTOGlossary otoGlossary) {
		try {
			Statement stmt = connection.createStatement();
	        String cleanupQuery = "DROP TABLE IF EXISTS " + 
									this.databasePrefix + "_term_category, " + 
									this.databasePrefix + "_syns, " +
									this.databasePrefix + "_wordroles, " +
									this.glossaryTable + ";";
	        stmt.execute(cleanupQuery);
	        stmt.execute("CREATE TABLE IF NOT EXISTS " + this.databasePrefix + "_syns (`term` varchar(200) DEFAULT NULL, `synonym` varchar(200) DEFAULT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS " + this.databasePrefix + "_term_category (`term` varchar(100) DEFAULT NULL, `category` varchar(200) " +
					"DEFAULT NULL, `hasSyn` tinyint(1) DEFAULT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS " + this.databasePrefix + "_wordroles (`word` varchar(50) NOT NULL DEFAULT '', `semanticrole` varchar(2) " +
					"NOT NULL DEFAULT '', `savedid` varchar(40) DEFAULT NULL, PRIMARY KEY (`word`,`semanticrole`));");
			stmt.execute("CREATE TABLE IF NOT EXISTS " + this.glossaryTable + " (`term` varchar(100) DEFAULT NULL, `category` varchar(200) " +
					"DEFAULT NULL, `hasSyn` tinyint(1) DEFAULT NULL)");
			
			for(TermCategory termCategory : otoGlossary.getTermCategories()) {
				 stmt.execute("INSERT INTO " + this.databasePrefix + "_term_category (`term`, `category`, `hasSyn`) VALUES " +
				 		"('" + termCategory.getTerm() +"', '" + termCategory.getCategory() + "', '" + termCategory.getHasSyn() +"');");
			}
			for(TermCategory termCategory : otoGlossary.getTermCategories()) {
				 stmt.execute("INSERT INTO " + this.glossaryTable + " (`term`, `category`, `hasSyn`) VALUES " +
				 		"('" + termCategory.getTerm() +"', '" + termCategory.getCategory() + "', '" + termCategory.getHasSyn() +"');");		
			}
			for(TermSynonym termSynonym : otoGlossary.getTermSynonyms()) {
				 stmt.execute("INSERT INTO " + this.databasePrefix + "_syns (`term`, `synonym`) VALUES " +
					 		"('" + termSynonym.getTerm() +"', '" + termSynonym.getSynonym() + "');");
			}
			for(WordRole wordRole : otoGlossary.getWordRoles()) {
				stmt.execute("INSERT INTO " + this.databasePrefix + "_wordroles" + " VALUES ('" +
						wordRole.getWord() + "','" +
						wordRole.getSemanticRole() + "','" +
						wordRole.getSavedid() + "')");
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, e);
		}
	}

	private LocalGlossary readLocalGlossary() {
		//List<TermCategory> termCategories = new ArrayList<TermCategory>();
		//List<Sentence> sentences = new ArrayList<Sentence>();

		/*
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select * from " + this.databasePrefix + "_sentence order by sentid");
			while(resultSet.next()) {
				sentences.add(new Sentence(resultSet.getInt("sentid"), resultSet.getString("source"), resultSet.getString("sentence"),
						resultSet.getString("originalsent"), resultSet.getString("lead"), resultSet.getString("status"), resultSet.getString("tag"),
						resultSet.getString("modifier"), resultSet.getString("charsegment")));
			}
			
			ResultSet resultSet = statement.executeQuery("select * from " + this.databasePrefix + "_term_category");
			while(resultSet.next()) {
				termCategories.add(new TermCategory(resultSet.getString("term"), resultSet.getString("category"), resultSet.getString("hasSyn")));
			}
		
		} catch(Exception e) {
			log(LogLevel.ERROR, e);
		}*/
		
		LocalGlossary localGlossary = new LocalGlossary(getStructures(), getCharacters(), getOtherTerms());
		return localGlossary;
	}

	private List<Term> getOtherTerms() {
		List<Term> result = new ArrayList<Term>();
		try {
			List<String> otherTerms = this.fetchContentTerms();
			for(String otherTerm : otherTerms) 
				result.add(new Term(otherTerm));
		} catch (Exception e) {
			log(LogLevel.ERROR, e);
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
			log(LogLevel.ERROR, e);
		}
		return result;
	}

	private List<Term> getStructures() {
		List<Term> result = new ArrayList<Term>();
		try {
			List<String> structureTerms = this.fetchStructureTerms();
			for(String structureTerm : structureTerms) 
				result.add(new Term(structureTerm));
		} catch (Exception e) {
			log(LogLevel.ERROR, e);
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
			if(posKnowledgeBase.isVerb(word) || posKnowledgeBase.isAdverb(word) /*|| Utilities.partOfPrepPhrase(word, this.conn, prefix)*/){
				//if(Utilities.mustBeAdv(word) /*|| Utilities.partOfPrepPhrase(word, this.conn, prefix)*/){
					noneqwords.add(word);
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
    	
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
 		String filter1 = "";
 		String filter2 = "";
 		String filter3 = "";
		Statement stmt1 = connection.createStatement();
		ResultSet rs1 = stmt1.executeQuery("show tables");
		while(rs1.next()){
			if(rs1.getString(1).equals("noneqterms")) {
				filter1 = " and tag not in (select word from noneqterms) "; 
				filter2 = " and plural not in (select word from noneqterms) " ;
				filter3 = " and word not in (select word from noneqterms) ";
			}
		}
 		rs1.close();
 		stmt1.close();

		String sql = "select distinct tag as structure from "+this.databasePrefix+"_sentence where tag != 'unknown' and tag is not null and tag not like '% %' " +
		filter1 +
		"union select distinct plural as structure from "+this.databasePrefix+"_singularplural"+","+ this.databasePrefix+"_sentence where singular=tag "+
		filter2 +
		"order by structure"; 		
		stmt = connection.prepareStatement(sql);
		rs = stmt.executeQuery();
		while (rs.next()) {
			String tag = rs.getString("structure");
			populateCurationList(tagList, tag); //select tags for curation, filter against the glossary
		}
		sql = "select distinct word from "+this.databasePrefix+"_wordpos where pos in ('p', 's', 'n') and saved_flag !='red' "+
		filter3+" order by word";
		stmt = connection.prepareStatement(sql);
		rs = stmt.executeQuery();
		while (rs.next()) {
			String tag = rs.getString("word");
			PreparedStatement stmtSentence = connection.prepareStatement("select * from " + this.databasePrefix + "_sentence where sentence like '% " + tag + "%'");
			ResultSet rs2 = stmtSentence.executeQuery();
			if (rs2.next()) {
				populateCurationList(tagList, tag); //select tags for curation, filter against the glossary
			}
		}
		return deduplicateSort(tagList);
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
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("select category from "+this.glossaryTable+" where term ='"+word+"'");
		if(rs.next()){
			String cat = rs.getString("category");
			if(cat.matches("(STRUCTURE|FEATURE|SUBSTANCE|PLANT|nominative|structure|life_style)")){
				add2WordRolesTable(word, "op");
			}else{
				add2WordRolesTable(word, "c");
			}
		}else{
			curationList.add(word);
		}
	}
	
	/**
	 * 
	 * @param w
	 * @param role
	 */
	private void add2WordRolesTable(String w, String role) throws Exception {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("select * from "+this.databasePrefix + "_wordroles where word='"+w+"' and semanticrole='"+role+"'");
		if(!rs.next()){
			stmt.execute("insert into "+this.databasePrefix+"_wordroles (word, semanticrole) values ('"+w+"','"+role+"')");
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
			if(this.posKnowledgeBase.isVerb(word) || posKnowledgeBase.isAdverb(word) /*|| Utilities.partOfPrepPhrase(word, this.conn, prefix)*/){
				noneqwords.add(word);
				continue;
			}
			filteredwords.add(word);
		}
		//mainDb.recordNonEQTerms(noneqwords, null, null);

		return filteredwords;	
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
			if(posKnowledgeBase.isVerb(word) || posKnowledgeBase.isAdverb(word) /*|| Utilities.partOfPrepPhrase(word, this.conn, prefix)*/){
				noneqwords.add(word);
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
		Statement stmt1 = connection.createStatement();
		ResultSet rs1 = stmt1.executeQuery("show tables");
		while(rs1.next()){
			if(rs1.getString(1).equals("noneqterms")){
				filter = " and dhword not in (select word from noneqterms)";
			}
		}
 		rs1.close();
 		stmt1.close();
 		
 		String sql = "select dhword from "+this.databasePrefix+"_allwords" +
		//" where count>=3 and inbrackets=0 and dhword not like '%\\_%' and " +
		" where dhword not like '%\\_%' and " +		
		" dhword not in (select word from "+ this.databasePrefix+"_wordpos where saved_flag='red')"+
		filter +
		" and dhword not in (select word from "+ this.databasePrefix+"_wordroles) order by dhword";
		PreparedStatement stmt = connection.prepareStatement(sql);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String word = rs.getString("dhword");
			if(!structures.contains(word) && !characters.contains(word)){
				populateCurationList(curationList, word);
			}
		}
		return this.deduplicateSort(curationList);
    }
	
	/**
	 * load descriptor subtab in step 4 (perl markup)
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<String> descriptorTerms4Curation() throws Exception {
		ArrayList<String> words = new ArrayList<String>();
		
		PreparedStatement stmt = null;
		ResultSet rset = null;
 		String filter = "";
		Statement stmt1 = connection.createStatement();
		ResultSet rs1 = stmt1.executeQuery("show tables");
		while(rs1.next()){
			if(rs1.getString(1).equals("noneqterms")) {
				filter = " and word not in (select word from noneqterms) ";
			}
		}
 		rs1.close();
 		stmt1.close();
 		
 		String sql = "select word from " + this.databasePrefix + "_wordpos where pos=? and saved_flag !='red' "+
 				filter+"order by word";
		//stmt = conn.prepareStatement("select word from "+this.tablePrefix+"_"+ApplicationUtilities.getProperty("POSTABLE")+" where pos=? and word not in (select distinct term from "+this.glossarytable+")");
		stmt = connection.prepareStatement(sql);
		stmt.setString(1, "b");
		rset = stmt.executeQuery();
		if (rset != null) {
			while(rset.next()){
				String word = rset.getString("word");
				PreparedStatement stmtSentence = connection.prepareStatement("select * from " + this.databasePrefix + "_sentence where sentence like '% " + word + "%'");
				ResultSet rs2 = stmtSentence.executeQuery();
				if (rs2.next()) {
					populateDescriptorList(words, word);
				}
			}	
		}
		words = deduplicateSort(words);
		return words;
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

			Statement stmt = connection.createStatement();
			ResultSet rset = stmt.executeQuery("select category from " + this.glossaryTable + " where term ='"+w+"'");					 
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
