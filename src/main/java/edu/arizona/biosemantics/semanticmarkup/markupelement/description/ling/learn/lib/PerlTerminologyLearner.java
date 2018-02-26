package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.db.ConnectionPool;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib.PhraseMarker;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.ParentTagProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.AdjectiveReplacementForNoun;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.LearnException;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.TaxonIdentification;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.TaxonName;

/**
 * PerlTerminologyLearner learns using the previous charaparser perl part
 * The Markup run reads the results from the database tables populated by perl to continue the character level markup
 * The description text that perl processes (from the temp folder) is what need to be updated if any normalization step is needed for various types of descriptions.
 * @author rodenhausen
 */
public class PerlTerminologyLearner implements ITerminologyLearner {

	protected Set<String> taxonNames;
	protected Set<String> sentences;
	//
	protected Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker;
	protected List<String> adjnouns;
	protected Map<String, String> adjnounsent;
	//protected Map<Description, LinkedHashMap<String, String>> sentenceTags;
	protected Set<String> bracketTags;
	protected Set<String> wordRoleTags;
	//protected Map<String, Set<String>> wordSources;
	protected Map<String, Map<String, Set<Integer>>> wordSources; //word => (source => position+)+
	protected Map<String, Set<String>> roleToWords;
	protected Map<String, Set<String>> wordsToRoles;
	protected Map<String, String> heuristicNouns;
	protected Map<String, Set<String>> termCategories;
	protected Set<String> tags;
	protected Set<String> modifiers;
	protected Map<String, Set<String>> categoryTerms;

	private String temporaryPath;
	private String databasePrefix;
	private String markupMode;
	private String databaseName;
	private String databasePassword;
	private String databaseUser;
	private IGlossary glossary;
	private PhraseMarker pm;
	private IInflector inflector;
	private ITokenizer tokenizer;
	private Set<String> stopWords;
	private Set<String> selectedSources;
	protected Map<String, Description> fileTreatments = new HashMap<String, Description>();
	private ParentTagProvider parentTagProvider;
	private String databaseHost;
	private String databasePort;
	private Map<String, AdjectiveReplacementForNoun> adjectiveReplacementsForNouns;
	private String perlDirectory;
	private ConnectionPool connectionPool;
	private AllWordsLearner allWordsLearner;

	/**
	 * @param temporaryPath
	 * @param markupMode
	 * @param databaseName
	 * @param glossaryTable
	 * @param databasePrefix
	 * @param databaseUser
	 * @param databasePassword
	 * @param stopWords
	 * @param selectedSources
	 * @param glossary
	 * @param tokenizer
	 * @throws Exception
	 */
	@Inject
	public PerlTerminologyLearner(@Named("Run_TemporaryDirectory") String temporaryPath,
			@Named("MarkupMode") String markupMode,
			@Named("DatabaseHost") String databaseHost,
			@Named("DatabasePort") String databasePort,
			@Named("DatabaseName") String databaseName,
			@Named("DatabasePrefix") String databasePrefix, 
			@Named("DatabaseUser") String databaseUser, 
			@Named("DatabasePassword") String databasePassword, 
			@Named("StopWords") Set<String> stopWords,
			@Named("SelectedSources") Set<String> selectedSources,
			IGlossary glossary, 
			@Named("WordTokenizer") ITokenizer tokenizer,
			@Named("ParentTagProvider") ParentTagProvider parentTagProvider,
			@Named("PerlDirectory") String perlDirectory,
			IInflector inflector, 
			ConnectionPool connectionPool) throws Exception {
		this.temporaryPath = temporaryPath;
		this.markupMode = markupMode;
		this.databasePrefix = databasePrefix;
		this.glossary = glossary;
		this.inflector = inflector;
		this.tokenizer = tokenizer;
		this.stopWords = stopWords;
		this.selectedSources = selectedSources;
		this.parentTagProvider = parentTagProvider;
		this.databaseHost = databaseHost;
		this.databasePort = databasePort;
		this.databaseName = databaseName;
		this.databaseUser = databaseUser;
		this.databasePassword = databasePassword;
		this.perlDirectory = perlDirectory;
		this.connectionPool = connectionPool;
	}
	
	@Override
	public void learn(List<AbstractDescriptionsFile> descriptionsFiles, String glossaryTable) throws LearnException {
		File directory = new File(temporaryPath);

		try {
			File inDirectory = new File(directory.getAbsolutePath() + File.separator + "perlInput");
			inDirectory.mkdirs();

			//create the files
			this.pm = new PhraseMarker(glossary, inflector);
			
			//create tables
			createTablesNeededForPerl(descriptionsFiles);
			//dehyphen and update descriptionFiles
			allWordsLearner = new AllWordsLearner(this.tokenizer, this.glossary, connectionPool, databasePrefix);
			allWordsLearner.learn(descriptionsFiles);
			
			writeTreatmentsToFiles(descriptionsFiles, inDirectory);

			//run the perl script	
			runPerl(inDirectory, descriptionsFiles, glossaryTable);

			this.readResults(descriptionsFiles);

		}catch(Exception e) {
			log(LogLevel.ERROR, "Problem with output/input or calling of perl", e);
			throw new LearnException();
		}
	}

	/**
	 * save all taxon names to the database and the taxonNames field 
	 * @param descriptionsFiles
	 */
	private void collectTaxonIdentifications(
			List<AbstractDescriptionsFile> descriptionsFiles) {
		this.taxonNames = new HashSet<String>();
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			List<TaxonIdentification> tis = descriptionsFile.getTaxonIdentifications();
			for(TaxonIdentification ti: tis){
			    if(ti.getTaxonNames().size()>=1){
				    for(TaxonName name: ti.getTaxonNames()){
					    String[] nameparts = name.getText().split("\\s+"); //name should be one word long, but just in case 
					    for(String namepart: nameparts){
						    taxonNames.add(namepart.toLowerCase());
					    }
				    }
			    }
				if(ti.hasStrainNumber()) {
					if(ti.getStrainNumber().getAccessionNumber16sRrna()!=null)
						taxonNames.add(ti.getStrainNumber().getAccessionNumber16sRrna());
					if(ti.getStrainNumber().getAccessionNumberForGenomeSequence()!=null)
						taxonNames.add(ti.getStrainNumber().getAccessionNumberForGenomeSequence());
					taxonNames.add(ti.getStrainNumber().getStrainNumber());
				}
			}
		}

		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				for(String taxonName: taxonNames){
					statement.execute("insert into "+this.databasePrefix+"_taxonnames (name) value ('"+taxonName+"')");
				}
			}
		} catch(SQLException e) {
			log(LogLevel.ERROR, "problem inserting taxon name to the taxonnames table", e);
		}
	}

	/*public Hashtable<String, String> selectMatchingSentences(String criterion) {
		Hashtable<String, String> matches = new Hashtable<String, String>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select source, sentence from " + this.databasePrefix + "_sentence where sentence rlike '"+criterion+"'");
			while(resultSet.next()) {
				matches.put(getSource(resultSet.getString("source")), resultSet.getString("sentence"));
			}
		} catch(SQLException e) {
			log(LogLevel.ERROR, "problem selecting sentences from sentence table", e);
		}
		return matches;
	}*/

	/**
	 * count the number of sentences containing the phrase consists of the phraseParts in the order as they presented in the arraylist
	 */
	public int countMatchingSentences(String phrase) {
		String[] phraseParts = phrase.trim().split("\\s+");
		int count =0;
		Map<String, Set<Integer>> sourcePositions = this.wordSources.get(phraseParts[0]); //word => sources => positions
		
		if(sourcePositions==null) return count;
		
		Set<String> srcs = sourcePositions.keySet();

		for(String src : srcs){
			boolean commonSource = true; //all parts appear in commonSource
			for(int i = 1; i < phraseParts.length; i++){
				if(this.wordSources.get(phraseParts[i])==null || !this.wordSources.get(phraseParts[i]).containsKey(src)){
					commonSource = false;
					break;
				}
			}

			if(commonSource){
				ArrayList<Set<Integer>> positions = new ArrayList<Set<Integer>> ();
				//collect position info for all parts
				for(String part: phraseParts){
					positions.add(this.wordSources.get(part).get(src));
				}

				//if consecutive positions indicate a phrase
				if(!positions.isEmpty()){
					for(Integer pos: positions.get(0)){
						boolean isPhrase = true;
						for(int i = 1; i<positions.size(); i++){
							if(!positions.get(i).contains(pos+i)){
								isPhrase = false;
								break;
							}
						}
						if(isPhrase) count++;
					}
				}
			}
		}
		return count;
	}

	protected Set<String> readModifiers() {
		Set<String> modifiers = new HashSet<String>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select modifier from " + this.databasePrefix + "_sentence")) {
					while(resultSet.next()) {
						String modifier = resultSet.getString("modifier");
						modifiers.add(modifier);
					}
				}
			}
		} catch(SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		return modifiers;
	}


	protected Set<String> readTags() {
		Set<String> tags = new HashSet<String>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select tag from " + this.databasePrefix + "_sentence")) {
					while(resultSet.next()) {
						String tag = resultSet.getString("tag");
						tags.add(tag);
					}
				}
			}
		} catch(SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		return tags;
	}


	protected Map<String, Set<String>> readTermCategories() {
		Map<String, Set<String>> termCategories = new HashMap<String, Set<String>>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select term, category from " + this.databasePrefix + "_term_category")) {
					while(resultSet.next()) {
						String term = resultSet.getString("term");
						String category = resultSet.getString("category");
						if(!termCategories.containsKey(term))
							termCategories.put(term, new HashSet<String>());
						termCategories.get(term).add(category);
					}
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing term_category table", e);
		}
		return termCategories;
	}

	protected Map<String, Set<String>> readCategoryTerms() {
		Map<String, Set<String>> categoryNames = new HashMap<String, Set<String>>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select term, category from " + this.databasePrefix + "_term_category")) {
					while(resultSet.next()) {
						String term = resultSet.getString("term");
						String category = resultSet.getString("category");
						if(!categoryNames.containsKey(category))
							categoryNames.put(category, new HashSet<String>());
						categoryNames.get(category).add(term);
					}
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing term_category table", e);
		}
		return categoryNames;
	}

	protected Map<String, Set<String>> readWordsToRoles() {
		Map<String, Set<String>> wordsToRoles = new HashMap<String, Set<String>>();
		//TODO wordroles table is populated by the GUI User interaction see MainForm.java. Therefore simply left as empty for now
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select word, semanticrole from " + this.databasePrefix + "_wordroles")) {
					while(resultSet.next()) {
						String word = resultSet.getString("word");
						//perl treated hyphens as underscores
						word = word.replaceAll("_", "-");
						String semanticRole = resultSet.getString("semanticrole");
						if(!wordsToRoles.containsKey(word))
							wordsToRoles.put(word, new HashSet<String>());
						wordsToRoles.get(word).add(semanticRole);
					}
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing wordroles table", e);
		}
		return wordsToRoles;
	}

	protected Map<String, String> readHeuristicNouns() {
		Map<String, String> heuristicNouns = new HashMap<String, String>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select word, type from " + this.databasePrefix + "_heuristicnouns")) {
					while(resultSet.next()) {
						heuristicNouns.put(resultSet.getString("word"), resultSet.getString("type"));
					}
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing heuristicnouns table", e);
		}
		return heuristicNouns;
	}

	protected Map<String, Set<String>> readRoleToWords() {
		Map<String, Set<String>> roleToWords = new HashMap<String, Set<String>>();
		//TODO wordroles table is populated by the GUI User interaction see MainForm.java. Therefore simply left as empty for now
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select word, semanticrole from " + this.databasePrefix + "_wordroles")) {
					while(resultSet.next()) {
						String word = resultSet.getString("word");
						//perl treated hyphens as underscores
						word = word.replaceAll("_", "-");
						String semanticRole = resultSet.getString("semanticrole");
						if(!roleToWords.containsKey(semanticRole))
							roleToWords.put(semanticRole, new HashSet<String>());
						roleToWords.get(semanticRole).add(word);
					}
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing wordroles table", e);
		}
		return roleToWords;
	}

	/*protected Map<String, Set<String>> readWordToSourcesMap() {
		Map<String, Set<String>> wordToSources = new HashMap<String, Set<String>>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select source, sentence from " + this.databasePrefix + "_sentence");
			while(resultSet.next()) {
				String source = getSource(resultSet.getString("source"));
				String sentence = resultSet.getString("sentence");
				List<Token> tokens = tokenizer.tokenize(sentence);
				for(Token token : tokens) {
					String word = token.getContent();
					if(!wordToSources.containsKey(word))
						wordToSources.put(word, new HashSet<String>());
					wordToSources.get(word).add(source);
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		return wordToSources;
	}*/

	/**
	 * record positions of a word (not a number) in different sentences
	 * @return
	 */
	protected Map<String, Map<String, Set<Integer>>> readWordToSourcesMap() {
		Map<String, Map<String, Set<Integer>>> wordToSources = new HashMap<String, Map<String, Set<Integer>>>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select source, sentence from " + this.databasePrefix + "_sentence")) {
					while(resultSet.next()) {
						String source = getSource(resultSet.getString("source"));
						String sentence = resultSet.getString("sentence");
						List<Token> tokens = tokenizer.tokenize(sentence);
						int p = 0;
						for(Token token : tokens) {
							String word = token.getContent();
							if(word.matches(".*?[a-z].*")){
								if(!wordToSources.containsKey(word))
									wordToSources.put(word, new HashMap<String, Set<Integer>>());
								Map<String, Set<Integer>> positions = wordToSources.get(word);
								if(!positions.containsKey(source))
									positions.put(source, new HashSet<Integer>());
								positions.get(source).add(p);
							}
							p++;
						}
					}
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		return wordToSources;
	}

	protected Set<String> readWordRoleTags()  {
		Set<String> tags = new HashSet<String>();
		//TODO wordroles table is populated by the GUI User interaction see MainForm.java. Therefore simply left as empty for now
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				String wordroletable = this.databasePrefix + "_wordroles";
				try(ResultSet resultSet = statement.executeQuery("select word from "+wordroletable+" where semanticrole in ('op', 'os')")) {
					while(resultSet.next()) {
						//perl treated hyphens as underscores
						String tag = resultSet.getString("word").replaceAll("_", "-").trim();
						if(!tag.isEmpty())
							tags.add(tag);
					}
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing wordrole table", e);
		}
		return tags;
	}

	protected Set<String> readBracketTags(List<AbstractDescriptionsFile> descriptionsFiles) {
		Set<String> tags = new HashSet<String>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select modifier, tag from " + this.databasePrefix + "_sentence where tag  like '[%]'")) { //inner [tepal]
					while(resultSet.next()){
						String modifier = resultSet.getString("modifier");
						modifier = modifier.replaceAll("\\[^\\[*\\]", ""); 
						if(!modifier.equals("")){
							String tag;
							if(modifier.lastIndexOf(" ")<0) {
								tag = modifier;
							} else {
								tag = modifier.substring(modifier.lastIndexOf(" ")+1); //last word from modifier
							}
							if(tag.indexOf("[")>=0 || stopWords.contains(tag) || tag.matches(".*?(\\d).*")) 
								continue;
							if(!tag.trim().isEmpty())
								tags.add(tag);
						}
					}
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		return tags;
	}

	/*protected Map<Description, LinkedHashMap<String, String>> readSentenceTags(List<AbstractDescriptionsFile> descriptionsFiles) {
		Map<Description, LinkedHashMap<String, String>> tags = new HashMap<Description, LinkedHashMap<String, String>>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select source, tag from " + this.databasePrefix + "_sentence order by sentid");
			String previousTag = null;
			//int listId = -1;
			String previousTreatmentId = "-1";
			while(resultSet.next()) {
				String source = getSource(resultSet.getString("source"));
				String treatmentId = getTreatmentId(resultSet.getString("source"));
				if(selectedSources.isEmpty() || selectedSources.contains(source)) {

					if(!treatmentId.equals(previousTreatmentId)) {
						previousTreatmentId = treatmentId;
						//listId++;
					}

					String tag = resultSet.getString("tag");
					if(tag == null)
						tag = "";
					tag = tag.replaceAll("\\W", "");

					Description description = fileTreatments.get(treatmentId);
					if(!tags.containsKey(description)) 
						tags.put(description, new LinkedHashMap<String, String>());
					if(!tag.equals("ditto")) {
						tags.get(description).put(source, tag);
						previousTag = tag;
					} else {
						tags.get(description).put(source, previousTag);
					}			
				}
			}
			resultSet.close();
			statement.close();
		} catch(Exception e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		return tags;
	}*/

	protected HashMap<Description,  LinkedHashMap<String, String>> readSentencesForOrganStateMarker(List<AbstractDescriptionsFile> descriptionsFiles) {
		HashMap<Description, LinkedHashMap<String, String>> sentences = new  HashMap<Description, LinkedHashMap<String, String>>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet rs = statement.executeQuery("select source, modifier, tag, sentence, originalsent from " + this.databasePrefix + "_sentence")) {
					// order by sentid desc");

					//int listId = -1;
					String previousTreatmentId = "-1";
				
		
					//leave ditto as it is
					while(rs.next()){ //read sent in in reversed order
						String tag = rs.getString("tag");
						String sent = rs.getString("sentence").trim();
		
						if(sent.length()!=0){
							String treatmentId = getTreatmentId(rs.getString("source"));
							String source = getSource(rs.getString("source"));
		
							if(selectedSources.isEmpty() || selectedSources.contains(source)) {
								if(!treatmentId.equals(previousTreatmentId)) {
									previousTreatmentId = treatmentId;
									//listId++; // in the db 1 is followed by 10 by 11 and not 2
								}
		
								String osent = rs.getString("originalsent");
								String text = sent;
								text = text.replaceAll("[ _-]+\\s*shaped", "-shaped").replaceAll("(?<=\\s)[μµ]\\s+m\\b", "um");
								text = text.replaceAll("&#176;", "°");
								text = text.replaceAll("\\bca\\s*\\.", "ca");
								text = rs.getString("modifier")+"##"+tag+"##"+text+"##"+osent;
		
								Description description = fileTreatments.get(treatmentId);
								if(!sentences.containsKey(description))
									sentences.put(description, new LinkedHashMap<String, String>());
								sentences.get(description).put(source, text);
							}
						}
					}
				}
			}
		} catch(SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}

		return sentences;
	}

	protected String getTreatmentId(String sourceString) {
		String[] sourceParts = sourceString.split("\\.");
		return sourceParts[0];
	}


	protected String getSource(String sourceString) {
		String[] sourceParts = sourceString.split("\\.");
		return sourceParts[0] + "." + sourceParts[2];
	}


	protected Map<String, String> readAdjNounSent() {
		Map<String, String> result = new HashMap<String, String>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("SELECT source, tag, modifier FROM " + this.databasePrefix + "_sentence s where modifier != \"\" and tag like \"[%\"")) {
					while(resultSet.next()){
						String modifier = resultSet.getString(3);
						String tag = resultSet.getString(2);
		
						modifier = modifier.replaceAll("\\[|\\]|>|<|(|)", "");
						tag = tag.replaceAll("\\[|\\]|>|<|(|)", "");
		
						result.put(tag, modifier);
					}
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		return result;
	}

	protected List<String> readAdjNouns() {
		List<String> result = new ArrayList<String>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("SELECT distinct modifier FROM " + this.databasePrefix + "_sentence s where modifier != \"\" and tag like \"[%\"")) {
					while(resultSet.next()) {
						String modifier = resultSet.getString(1).replaceAll("\\[.*?\\]", "").trim();
						result.add(modifier);
					}
				}
			}
		} catch(SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		return result;
	}

	protected Set<String> readSentences() {
		Set<String> result = new HashSet<String>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("SELECT sentence FROM " + this.databasePrefix + "_sentence order by sentid")) {
					while(resultSet.next()) {
						String sentence = resultSet.getString(1);
						result.add(sentence);
					}
				}
			}
		} catch(SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		return result;
	}



	@Override
	public List<String> getAdjNouns() {
		return this.adjnouns;
	}

	@Override
	public Map<String, String> getAdjNounSent() {
		return this.adjnounsent;
	}

	@Override
	public Set<String> getBracketTags() {
		return this.bracketTags;
	}

	@Override
	public Set<String> getWordRoleTags() {
		return this.wordRoleTags;
	}


	@Override
	public Map<String, Set<String>> getRoleToWords() {
		return this.roleToWords;
	}

	@Override
	public Map<String, Set<String>> getWordsToRoles() {
		return this.wordsToRoles;
	}


	@Override
	public Map<String, Map<String, Set<Integer>>> getWordToSources() {
		return this.wordSources;
	}
	/*public Map<String, Set<String>> getWordToSources() {
		return this.wordSources;
	}*/



	@Override
	public Map<String, String> getHeuristicNouns() {
		return this.heuristicNouns;
	}

	private void runPerl(File inDirectory, List<AbstractDescriptionsFile> descriptionsFiles, String glossaryTable) throws IOException, LearnException {

		String inDirectoryPath = inDirectory.getAbsolutePath() + "/"; //yes "/", not File.seperator; perl wants "/"

		//assumption: windows paths can contain spaces and are therefore put into double quotes; for unix that is not required, and actually would lead to missbehavior of perl script
		if(SystemUtils.IS_OS_WINDOWS)
			inDirectoryPath = "\"" + inDirectoryPath + "\"";
		//else if(SystemUtils.IS_OS_UNIX)

		String command = "perl -I " + perlDirectory + " " + perlDirectory + File.separator + "unsupervisedClauseMarkupBenchmarked.pl " 
				+ inDirectoryPath +  
				// there is a strange requirement for a slash here to make perl parse the arguments correctly
				" " + this.markupMode + " " + this.databaseHost + " " + this.databasePort + " " + this.databaseName + " " 
				+ this.databaseUser + " " + this.databasePassword + " " + this.databasePrefix + " " + glossaryTable;
		log(LogLevel.DEBUG, command);
		
		collectTaxonIdentifications(descriptionsFiles);
		runCommand(command);
	}

	private void createTablesNeededForPerl(List<AbstractDescriptionsFile> descriptionsFiles) throws LearnException {
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement stmt = connection.createStatement()) {
				String cleanupQuery = "DROP TABLE IF EXISTS " + 
						//this.databasePrefix + "_allwords, " +  //allwords table is created outside of perl by dehypenizer
						this.databasePrefix + "_discounted, " + 
						this.databasePrefix + "_heuristicnouns, " + 
						this.databasePrefix + "_isa, " + 
						this.databasePrefix + "_modifiers, " + 
						this.databasePrefix + "_sentence, " + 
						this.databasePrefix + "_sentinfile, " + 
						this.databasePrefix + "_singularplural, " + 
						this.databasePrefix + "_substructure, " + 
						this.databasePrefix + "_unknownwords, " + 
						this.databasePrefix + "_wordpos, " + 
						this.databasePrefix + "_taxonnames, " + 
						this.databasePrefix + "_wordroles;";
				stmt.execute(cleanupQuery);
				stmt.execute("create table if not exists " + this.databasePrefix + "_allwords (word varchar(150) unique not null primary key, count int, "
						+ "dhword varchar(150), inbrackets int default 0) CHARACTER SET utf8 engine=innodb");
							
				stmt.execute("create table if not exists " + this.databasePrefix + "_wordroles (word varchar(200), semanticrole varchar(2), savedid varchar(40), "
						+ "primary key(word, semanticrole)) CHARACTER SET utf8 engine=innodb");  
				stmt.execute("create table if not exists " + this.databasePrefix + "_taxonnames (name varchar(100), primary key(name)) CHARACTER SET utf8 engine=innodb");
			}
		} catch(SQLException e) {
			log(LogLevel.ERROR, "problem initalizing tables", e);
			throw new LearnException();
		}
	}
	
	private class TerminatePerlHook extends Thread {
		final Process process;
		public TerminatePerlHook(Process process) {
			this.process = process;
		}
		@Override
		public void run() {
			try {
				process.getInputStream().close();
				process.getOutputStream().close();
				process.getErrorStream().close();

				//if (process instanceof UNIXProcess) {
			    Field field = process.getClass().getDeclaredField("pid");
			    field.setAccessible(true);
			    int pid =field.getInt(process);
				Runtime.getRuntime().exec("kill -9 " + pid);
				//}
				
			} catch(Throwable t) {
				log(LogLevel.ERROR, "Could not kill perl process. Running on non-Unix OS?", t);
			}
			process.destroy();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				log(LogLevel.ERROR, "Interrupted", e);
			}
		}
	}

	private void runCommand(String command) throws IOException {
		long time = System.currentTimeMillis();

		Process process = Runtime.getRuntime().exec(command);
		
		// add shutdown hook to clean up in case of failure
		TerminatePerlHook terminatePerlHook = new TerminatePerlHook(process);
		Runtime.getRuntime().addShutdownHook(terminatePerlHook);
		
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process
				.getInputStream()));

		BufferedReader errInput = new BufferedReader(new InputStreamReader(process
				.getErrorStream()));

		// read the output from the command
		String s = "";
		int i = 0;
		while ((s = stdInput.readLine()) != null) {
			log(LogLevel.DEBUG, s + " at " + (System.currentTimeMillis() - time)
					/ 1000 + " seconds");
		}

		// read the errors from the command
		String e = "";
		while ((e = errInput.readLine()) != null) {
			log(LogLevel.DEBUG, e + " at " + (System.currentTimeMillis() - time)
					/ 1000 + " seconds");
		}
		
		// remove shutdown hook
		Runtime.getRuntime().removeShutdownHook(terminatePerlHook);
	}

	protected String intToString(int num, int digits) {
		// create variable length array of zeros
		char[] zeros = new char[digits];
		Arrays.fill(zeros, '0');
		// format number as String
		DecimalFormat df = new DecimalFormat(String.valueOf(zeros));

		return df.format(num);
	}

	/**
	 * Renames files other than the original input files to get a order for perl as input that is independent on the OS 
	 * that CharaParser is run on. E.g. when iterating files in unix and windows it can be different 
	 * Also, if a input descriptionsFile contains multiple descriptions we want to feed perl one description per file.
	 * @param descriptionsFiles
	 * @param directory
	 * @throws IOException
	 */
	private void writeTreatmentsToFiles(List<AbstractDescriptionsFile> descriptionsFiles, File directory) throws IOException {
		int i = 0;
		int descriptionCount = 0;
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			descriptionCount += descriptionsFile.getDescriptions().size();
		}
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			for(Description description : descriptionsFile.getDescriptions()) {
				String prefix = intToString(i++, Math.max(String.valueOf(descriptionCount).length(), 3)); //prefix = a description in a file represented as a [number of same length]. one file may have multiple description.		
				//System.out.println(prefix);
				File treatmentFile = File.createTempFile(prefix  + ".", ".txt", directory); //create temp file by Java: a long number generated and used by Java and attached after the prefix. 
				treatmentFile.deleteOnExit();
				//File treatmentFile = new File(file.getAbsolutePath() + File.separator + i++ + ".txt");
				log(LogLevel.DEBUG, treatmentFile.getAbsolutePath());
				BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(treatmentFile), "UTF-8"));
				
				//deal with hypens
				String text = updateHyphenedWords(description.getText().replaceAll("[\\n\\r]", " ").replaceAll("\\s+", " ").trim(), this.allWordsLearner.getDehypenHash());
				
				//mark phrases
				text = pm.markPhrases(text);
				
				description.setText(text);

				fileWriter.write(description.getText() + "\n");
				fileWriter.close();

				fileTreatments.put(prefix, description);
			}
		}
	}

	private String updateHyphenedWords (String text, HashMap<String, String> dephyened){
		String[] words = text.split("\\s+");
		StringBuffer updated = new StringBuffer();
		
		for(String word: words){
			String key = word.replaceAll("[\\p{Punct}&&[^_-]]", "");
			if(word.indexOf(key)>=0){
				String beforeKey = word.substring(0, word.indexOf(key));
				String afterKey = word.substring(word.indexOf(key)+key.length());
				if(dephyened.containsKey(key.toLowerCase())){
					String dehyphened = dephyened.get(key.toLowerCase());
					if(Character.isUpperCase(key.charAt(0))){
						dehyphened = WordUtils.capitalize(dehyphened);
					}
					word = beforeKey+dehyphened+afterKey;
				}
			}
			updated.append(word);
			updated.append(" ");
			
		}
		return updated.toString().trim();
		
	}

	@Override
	public Set<String> getSentences() {
		return this.sentences;
	}


	@Override
	public Map<Description, LinkedHashMap<String, String>> getSentencesForOrganStateMarker() {
		return this.sentencesForOrganStateMarker;
	}


	/*@Override
	public Map<Description, LinkedHashMap<String, String>> getSentenceTags() {
		return this.sentenceTags;
	}*/


	@Override
	public Map<String, Set<String>> getTermCategories() {
		return this.termCategories;
	}


	@Override
	public Set<String> getTags() {
		return this.tags;
	}


	@Override
	public Set<String> getModifiers() {
		return this.modifiers;
	}


	@Override
	public Map<String, Set<String>> getCategoryTerms() {
		return this.categoryTerms;
	}


	@Override
	public void readResults(List<AbstractDescriptionsFile> descriptionsFiles) {
		this.sentences = readSentences();
		this.sentencesForOrganStateMarker = readSentencesForOrganStateMarker(descriptionsFiles);
		this.adjnouns = readAdjNouns();
		this.adjnounsent = readAdjNounSent();
		//this.sentenceTags = readSentenceTags(descriptionsFiles);
		this.bracketTags = readBracketTags(descriptionsFiles);
		this.wordRoleTags = readWordRoleTags(); 
		this.wordSources = readWordToSourcesMap();
		this.roleToWords = readRoleToWords();
		this.wordsToRoles = readWordsToRoles();
		this.heuristicNouns = readHeuristicNouns();
		this.termCategories = readTermCategories();
		this.categoryTerms = readCategoryTerms();
		this.tags = readTags();
		this.modifiers = readModifiers();
		this.adjectiveReplacementsForNouns = readAdjectiveReplacementsForNouns();
		initParentTagProvider(parentTagProvider);
	}


	private Map<String, AdjectiveReplacementForNoun> readAdjectiveReplacementsForNouns() {
		Map<String, AdjectiveReplacementForNoun> result = new HashMap<String, AdjectiveReplacementForNoun>();
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("SELECT source, tag, modifier FROM " + this.databasePrefix + "_sentence s where modifier != \"\" and tag like \"[%\"")) {
					while(resultSet.next()){
						String source = resultSet.getString(1);
						String modifier = resultSet.getString(3);
						String tag = resultSet.getString(2);
		
						modifier = modifier.replaceAll("\\[|\\]|>|<|(|)", "");
						tag = tag.replaceAll("\\[|\\]|>|<|(|)", "");
		
						result.put(source, new AdjectiveReplacementForNoun(modifier, tag, source));
					}
					resultSet.close();
				}
				statement.close();
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		return result;
	}


	private void initParentTagProvider(ParentTagProvider parentTagProvider2) {
		HashMap<String, String> parentTags = new HashMap<String, String>();
		HashMap<String, String> grandParentTags = new HashMap<String, String>();

		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				try(ResultSet resultSet = statement.executeQuery("select source, tag from " + this.databasePrefix + "_sentence order by sentid")) {
					String parentTag = "";
					String grandParentTag = "";
					String prevSource = "";
					while(resultSet.next()) {
						String source = getSource(resultSet.getString("source"));
						String tag = resultSet.getString("tag");
		
						if(source.replaceFirst("-.*", "").compareTo(prevSource.replaceFirst("-.*", ""))!=0){ //a new description starts
							parentTag = "";
							grandParentTag = "";
						}
		
						parentTags.put(source, parentTag);
						grandParentTags.put(source, grandParentTag);
		
						grandParentTag = parentTag;
						if(tag != null && !tag.equals("ditto"))
							parentTag = tag;
						else if(tag == null)
							parentTag = "";
		
						prevSource = source;
					}
				}
			}
		} catch(SQLException e) {
			log(LogLevel.ERROR, "problem accessing sentence table", e);
		}
		this.parentTagProvider.init(parentTags, grandParentTags);
	}


	@Override
	public Map<String, AdjectiveReplacementForNoun> getAdjectiveReplacementsForNouns() {
		return this.adjectiveReplacementsForNouns;
	}
	
}
