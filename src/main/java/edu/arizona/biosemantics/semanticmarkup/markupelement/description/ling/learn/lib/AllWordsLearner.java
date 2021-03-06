package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.db.ConnectionPool;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;

public class AllWordsLearner {

	private String hyphen = "-";
	private HashMap<String, Integer> wordCounts = new HashMap<String, Integer>();
	private HashMap<String, Integer> wordInBracketsCounts = new HashMap<String, Integer>(); 
	private HashMap<String, String> deHyphenizedWords = new HashMap<String, String>();
	private ITokenizer tokenizer;
	private IGlossary glossary;
	private String tablename;
	private ConnectionPool connectionPool;
	private HashMap<String, List<Token>> fileContent = new HashMap<String, List<Token>>();
	
	@Inject
	public AllWordsLearner(@Named("WordTokenizer")ITokenizer tokenizer,
			IGlossary glossary,
			ConnectionPool connectionPool,
			@Named("DatabasePrefix") String databasePrefix) throws SQLException, ClassNotFoundException
			 {
		this.tokenizer = tokenizer;
		this.glossary = glossary;
		this.tablename = databasePrefix + "_allwords";
		this.connectionPool = connectionPool;

		// TODO removable once OldPerlTreatmentTransformer is no longer used.
	}
	
	public void learn(List<AbstractDescriptionsFile> descriptionsFiles) throws SQLException {
		createAllWordsTable();
		countWords(descriptionsFiles);
		
		for(String word : wordCounts.keySet()) {
			if(word.contains("-")) {
				String deHyphenizedWord = normalFormat(word).replaceAll("-", "_");
				deHyphenizedWords.put(word, deHyphenizedWord);
			} 
		}
		
		try {
			fillInWords();
		} catch (Exception e) {
			log(LogLevel.ERROR, "Problem to feed dehyphenized words to DB", e);
		}
	}
	
	/* update description text for perl
	 * replace some hyphen-ed words with dehyphened words: cau-lin => caulin
	 * while keep others intact, e.g.,  leaf-blade
	 */
	public HashMap<String, String> getDehypenHash(){
		return deHyphenizedWords;
	}

		
		
	private void createAllWordsTable() throws SQLException {
		try(Connection connection = connectionPool.getConnection()) {
			try(Statement statement = connection.createStatement()) {
		        statement.execute("drop table if exists " + tablename);
		        String query = "create table if not exists " + tablename + " (word varchar(150) unique not null primary key, count int, dhword varchar(150), inbrackets int default 0)"
		        		            + " CHARACTER SET utf8 engine=innodb";
		        statement.execute(query);        
			}
		}
    }
	
	/**
     * check for unmatched brackets too.
     * Records the words contained in a volume in a database table including how often they appear in general and how often they
     *  appear within brackets
	 * @throws Exception 
     */
    private void fillInWords() throws Exception{
    	try(Connection connection = connectionPool.getConnection()) {
	    	try(Statement statement = connection.createStatement()) {
		        for(String word : wordCounts.keySet()) {
		        	int count = this.wordCounts.get(word);
		        	int inBracketCount = this.wordInBracketsCounts.get(word);
		        	String deHyphenizedWord = word;
		        	if(this.deHyphenizedWords.containsKey(word)) 
		        		deHyphenizedWord = this.deHyphenizedWords.get(word);
		        	statement.execute("insert into "+tablename+" (word, count, dhword, inbrackets) values('" + word + "', " + count + ",'" + deHyphenizedWord + "'," + inBracketCount + ")");
		        	
		        }
	    	}
    	}
	}
	
	
    /**
     * check for unmatched brackets too.
     * Records the words contained in a volume including how often they appear in general and how often they
     * appear within brackets
     */
	private void countWords(List<AbstractDescriptionsFile> descriptionsFiles) {
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			//String descriptionText = "";
			List<Token> tokensInFile = new ArrayList<Token>();
			int order = 0;
			for(Description description : descriptionsFile.getDescriptions()) {
				//descriptionText += description.getText() + " ";
				List<Token> tokensInDscrpt = tokenizer.tokenize(description.getText());
				//collect tokens
				tokensInFile.addAll(tokensInDscrpt);
				//collect file content as tokens -- to be updated with dehyphened words
				fileContent.put(descriptionsFile.getName()+"_"+(order++), tokensInDscrpt);
			}
							
			int lround = 0;
            int lsquare = 0;
            int lcurly = 0;
            int inbracket = 0;
            for(Token token : tokensInFile){
            	String wordcp = token.getContent().trim().toLowerCase();
                if(wordcp.equals("(")) lround++;
                else if(wordcp.equals(")")) lround--;
                else if(wordcp.equals("[")) lsquare++;
                else if(wordcp.equals("]")) lsquare--;
                else if(wordcp.equals("{")) lcurly++;
                else if(wordcp.equals("}")) lcurly--;
                else{
                	wordcp = wordcp.replaceAll("[^-a-z_]", " ").trim();
                	String[] ws = wordcp.split("\\s+");
                	for(String word: ws){
                		if(word.matches(".*?\\w.*")) {
                			if(lround+lsquare+lcurly > 0)
                				inbracket = 1;
                			else 
                				inbracket = 0;

                			int count = 1;
                			if(wordCounts.containsKey(word))
                				count += wordCounts.get(word);
                			wordCounts.put(word, count);

                			if(wordInBracketsCounts.containsKey(word)) 
                				inbracket *= wordInBracketsCounts.get(word);
                			wordInBracketsCounts.put(word, inbracket);
                		}
                	}
                }
            }
		}
	}
    
	public String normalFormat(String hyphened){
		/*if(hyphened.startsWith("NUM-")){
			return hyphened;
		}*/
		hyphened = hyphened.replaceFirst("^_", "");
		String[] segments = hyphened.split(this.hyphen);
		String[] terms = new String[segments.length];
		int[][] matrix = new int[segments.length][segments.length];
		//fill matrix
		fillMatrix(segments, matrix);
		collectTerms(segments, terms, matrix);
			
		//out put term
		String term = "";
		for(int i =0; i<terms.length; i++){
			if(terms[i] != null){
				term += terms[i]+"-";
			}
		}
		return term.replaceFirst("-$", "");
	}
	
	protected void collectTerms(String[] segments, String[] terms, int[][] matrix) {
		//rank rows by the distance between a 1 in upper matrix to the diagonal line
		int max = 0;
		HashMap<String, String> rank = new HashMap<String, String>();
		for(int i = 0; i < segments.length; i++){
			int distance = getDistance(matrix[i], i);
			if(distance > max){
				max = distance;
			}
			String list = (String)rank.get(distance+"");
			if(list == null){
				rank.put(distance+"", i+"");
			}else{
				rank.put(distance+"", list+" "+i+"");
			}
		}
		//collect terms
		String checked="-";
		for(int i = max; i >= 0; i--){
			String rows = (String)rank.get(i+"");
			if(rows!= null && i == 0){//term not see in learned or glossary, and not connectable to other terms
				String[] rowss = rows.split(" ");
				for(int j = 0; j < rowss.length; j++){
					int arow = Integer.parseInt(rowss[j]);
					if(checked.indexOf("-"+arow+"-")<0){
						terms[arow] = segments[arow];
					}
				}
			}else if(rows!=null){
				String[] rowss = rows.split(" ");
				for(int j = 0; j < rowss.length; j++){
					int arow = Integer.parseInt(rowss[j]);
					if(checked.indexOf("-"+arow+"-")<0){
						terms[arow] = formTerm(segments, arow, arow+i);
						checked += formString(arow, arow+i, "-");
					}
				}
			}
		}
	}

	/**
	 * @param arow
	 * @param rownumber
	 * @return the distance between a 1 in upper matrix to the diagonal line
	 */
	private int getDistance(int[] arow, int rownumber) {
		for(int i = arow.length-1; i>=0; i--){
			if(arow[i] == 1){
				return i - rownumber; 
			}
		}
		return 0;
	}

	private void fillMatrix(String[] segments, int[][] matrix) {
		for(int i = 0; i < segments.length; i++){
			for(int j = i; j<segments.length; j++){
				if(isTerm(segments, i, j)) 
					matrix[i][j] = 1;
				else 
					matrix[i][j] = 0;
				matrix[j][i] = matrix[i][j];
			}
		}
	}
	protected String formString(int start, int end, String connector){
		String result = "";
		for(int i = start; i<=end; i++){
			result += i + connector;
		}
		return result;
	}
	
	protected String formTerm(String[] segments, int start, int end){
		String str="";
		for(int i = start; i<=end; i++){
			str +=segments[i];
		}
		return str;
	}
	
	private boolean isTerm(String[] segs, int start, int end){
		String word = formTerm(segs, start, end);
		if(glossary.contains(word)) 
			return true;
		if(wordCounts.containsKey(word))
			return true;
		return false;
	}
}
