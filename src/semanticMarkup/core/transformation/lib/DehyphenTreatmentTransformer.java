package semanticMarkup.core.transformation.lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.core.transformation.ITreatmentTransformer;
import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.Token;
import semanticMarkup.ling.transform.ITokenCombiner;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DehyphenTreatmentTransformer implements ITreatmentTransformer {

	private String hyphen = "-";
	private HashMap<String, Integer> wordCounts = new HashMap<String, Integer>();
	private HashMap<String, Integer> wordInBracketsCounts = new HashMap<String, Integer>(); 
	private HashMap<String, String> deHyphenizedWords = new HashMap<String, String>();
	private ITokenizer tokenizer;
	private ITokenCombiner tokenCombiner;
	private IGlossary glossary;
	private String tablename;
	private Connection connection;
	
	@Inject
	public DehyphenTreatmentTransformer(@Named("WordTokenizer")ITokenizer tokenizer,
			@Named("WordCombiner")ITokenCombiner tokenCombiner, IGlossary glossary,
			@Named("databaseHost") String databaseHost,
			@Named("databasePort") String databasePort,
			@Named("databaseName") String databaseName,
			@Named("databasePrefix") String databasePrefix,
			@Named("databaseUser") String databaseUser,
			@Named("databasePassword") String databasePassword)
			throws Exception {
		this.tokenizer = tokenizer;
		this.tokenCombiner = tokenCombiner;
		this.glossary = glossary;
		this.tablename = databasePrefix + "_allWords";

		// TODO removable once OldPerlTreatmentTransformer is no longer used.
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://" + databaseHost + ":" + databasePort +"/" + databaseName + "?connectTimeout=0&socketTimeout=0&autoReconnect=true", 
				databaseUser, databasePassword);
		createAllWordsTable();
	}

	public List<Treatment> transform(List<Treatment> treatments) {
		if(!checkTreatments(treatments))
			return treatments;
		//TODO is this actually needed? -> Perl at least uses this table
		countWords(treatments);
				
		for(String word : wordCounts.keySet()) {
			if(word.contains("-")) {
				 //so dhwords in _allwords table are comparable to words in _wordpos and other tables.
				String deHyphenizedWord = normalFormat(word).replaceAll("-", "_");
				deHyphenizedWords.put(word, deHyphenizedWord);
			} else {
				deHyphenizedWords.put(word,  word);
			}
		}
		
		//TODO removable once OldPerlTreatmentTransformer is no longer used.
		try {
			fillInWords();
		} catch (Exception e) {
			log(LogLevel.ERROR, "Problem to fill dehyphenized words to DB", e);
		}
			
		normalizeTreatments(treatments);
		return treatments;
	}
	
	private void createAllWordsTable() throws Exception {
        Statement statement = connection.createStatement();
        statement.execute("drop table if exists " + tablename);
        String query = "create table if not exists " + tablename + " (word varchar(150) unique not null primary key, count int, dhword varchar(150), inbrackets int default 0) "
        		+ "CHARACTER SET utf8 engine=innodb";
        statement.execute(query);        
    }
	
	/**
     * check for unmatched brackets too.
     * Records the words contained in a volume in a database table including how often they appear in general and how often they
     *  appear within brackets
	 * @throws Exception 
     */
    private void fillInWords() throws Exception{
        Statement statement = connection.createStatement();
        for(String word : wordCounts.keySet()) {
        	int count = this.wordCounts.get(word);
        	int inBracketCount = this.wordInBracketsCounts.get(word);
        	String deHyphenizedWord = word;
        	if(this.deHyphenizedWords.containsKey(word)) 
        		deHyphenizedWord = this.deHyphenizedWords.get(word);
        	statement.execute("insert into "+tablename+" (word, count, dhword, inbrackets) values('" + word + "', " + count + "," + deHyphenizedWord + "," + inBracketCount + ")");
        }
        statement.close();
    }
	
	
    /**
     * check for unmatched brackets too.
     * Records the words contained in a volume including how often they appear in general and how often they
     * appear within brackets
     */
	private void countWords(List<Treatment> treatments) {
		for(Treatment treatment : treatments) {
			List<ValueTreatmentElement> elements = treatment.getValueTreatmentElementsRecursively();
			for(ValueTreatmentElement element : elements) {
				String text = element.getValue();
				List<Token> tokens = tokenizer.tokenize(text);
								
				int lround = 0;
                int lsquare = 0;
                int lcurly = 0;
                int inbracket = 0;
                for(Token token : tokens){
                	String word = token.getContent().trim().toLowerCase();
                    if(word.equals("(")) lround++;
                    else if(word.equals(")")) lround--;
                    else if(word.equals("[")) lsquare++;
                    else if(word.equals("]")) lsquare--;
                    else if(word.equals("{")) lcurly++;
                    else if(word.equals("}")) lcurly--;
                    else{
                    	word = word.replaceAll("[^-a-z]", " ").trim();
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

	private boolean checkTreatments(List<Treatment> treatments) {
		boolean result = true;
		for(Treatment treatment : treatments) {
			List<ValueTreatmentElement> valueTreatmentElements = treatment.getValueTreatmentElementsRecursively();
			for(ValueTreatmentElement valueTreatmentElement : valueTreatmentElements) {
				String text = valueTreatmentElement.getValue();
				if(hasTextProblems(text))
					return false;
			}
		}
		return result;
	}

	private boolean hasTextProblems(String text) {
		 if(hasUnmatchedBrackets(text)){
         	return true;
         }
         //check for missing spaces between text and numbers: 
         if(text.matches(".*[a-zA-Z]\\d.*") || text.matches(".*\\d[a-zA-Z].*")){
         	//has =true; //ant descriptions contain "Mf4"
          	//vd.showPerlMessage((++problemcount)+": "+flist[i].getAbsolutePath()+" misses a space between a word and a number in \""+text+"\"\n");      	       
         }
         //check for (?)
         if(text.matches(".*?\\(\\s*\\?\\s*\\).*")){
        	return true;
         	//vd.showPerlMessage((++problemcount)+": "+flist[i].getAbsolutePath()+" contains expression (?) in \""+text+"\"\n");  
          	//vd.showPerlMessage("Change (?) to an text expression such as (not certain)");
         }
		return false;
	}

	
    //TODO
    /* this method can be cheated by havnig closing brakets always preceed opening brackets */
    /* also the nesting of brakets is not necessarily correct as they may overlap */
    private boolean hasUnmatchedBrackets(String text) {
    	String[] lbrackets = new String[]{"\\[", "(", "{"};
    	String[] rbrackets = new String[]{"\\]", ")", "}"};
    	for(int i = 0; i<lbrackets.length; i++){
    		int left1 = text.replaceAll("[^"+lbrackets[i]+"]", "").length();
    		int right1 = text.replaceAll("[^"+rbrackets[i]+"]", "").length();
    		if(left1!=right1) return true;
    	}
		return false;
	}
    
    
    private void normalizeTreatments(List<Treatment> treatments){
    	for(Treatment treatment : treatments) {
			List<ValueTreatmentElement> elements = treatment.getValueTreatmentElementsRecursively();
			for(ValueTreatmentElement element : elements) {
				String text = element.getValue();
                text = replaceByDeHyphenizedWords(text);
               
                //turn "." that are in brackets as [.DOT.] for unsupervised learning pl.
                int lround = 0;
                int lsquare = 0;
                int lcurly = 0;
                StringBuffer stringBuffer = new StringBuffer();
                                
                List<Token> tokens = tokenizer.tokenize(text);
                for(Token token : tokens) {
                    String w = token.getContent();
                	if(w.equals("(")) {
                    	lround++;
                    	stringBuffer.append(w);
                    } else if(w.equals(")")) {
                    	lround--;
                    	stringBuffer.append(w);
                    } else if(w.equals("[")) {
                    	lsquare++;
                    	stringBuffer.append(w);
                    } else if(w.equals("]")) {
                    	lsquare--;
                    	stringBuffer.append(w);
                    } else if(w.equals("{")) {
                    	lcurly++;
                    	stringBuffer.append(w);
                    } else if(w.equals("}")) {
                    	lcurly--;
                    	stringBuffer.append(w);
                    } else {
                    	if(w.matches(".*?[.?;:!].*?") && (lround+lsquare+lcurly)>0) {
                    		w = w.replaceAll("\\.", "[DOT]");
                    		w = w.replaceAll("\\?", "[QST]");
                    		w = w.replaceAll(";", "[SQL]");
                    		w = w.replaceAll(":", "[QLN]");
                    		w = w.replaceAll("!", "[EXM]");
                    	}
                    	stringBuffer.append(w + " ");                        	
                    }                        
                }
                text = stringBuffer.toString().replaceAll("\\s*\\(\\s*", "(").replaceAll("\\s*\\)\\s*", ")")
                .replaceAll("(?<=[^0-9+�-])\\(", " (").replaceAll("\\)(?=[a-z])", ") ").trim();
                element.setValue(text);
            }
    	}
    }
    
    private String replaceByDeHyphenizedWords(String original){
        List<Token> tokens = tokenizer.tokenize(original);
    	for(Token token : tokens) {
    		String word = token.getContent();
    		String lowercaseWord = word.toLowerCase();
    		if(!lowercaseWord.startsWith("-") && !lowercaseWord.endsWith("-") && !deHyphenizedWords.get(lowercaseWord).equals(lowercaseWord)) {
    			if(Character.isUpperCase(word.charAt(0))) {
    				String replacementLowerCase = deHyphenizedWords.get(lowercaseWord).replaceAll("-", " ");
    				String replacement = replacementLowerCase.substring(0, 1).toUpperCase() + replacementLowerCase.substring(1);
    				word = replacement;
    			} else
    				word = deHyphenizedWords.get(lowercaseWord).replaceAll("-", " ");	
    			token.setContent(word);
    		}
    	}
    	return tokenCombiner.combine(tokens);
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
