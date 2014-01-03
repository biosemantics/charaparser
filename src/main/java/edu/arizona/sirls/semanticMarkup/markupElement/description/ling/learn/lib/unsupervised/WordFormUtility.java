package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.arizona.sirls.semanticMarkup.know.lib.WordNetPOSKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.ling.transform.lib.PorterStemmer;


public class WordFormUtility {
	private WordNetPOSKnowledgeBase myWN;
	private Map<String, String> numberRecords = new HashMap<String, String>(); // word->(p|s)
	private Map<String, String> singularRecords = new HashMap<String, String>();// word->singular
	private Map<String, String> POSRecords = new HashMap<String, String>(); // word->POSs
	
	private Map<String, Integer> WORDS = new HashMap<String, Integer>();	
	private Hashtable<String, String> PLURALS = new Hashtable<String, String>();
	
	// Porter Stemmer
	private PorterStemmer myStemmer;

	public WordFormUtility(WordNetPOSKnowledgeBase wn) {
		this.myWN = wn;
		this.myStemmer = new PorterStemmer();
	}
	
	/**
	 * 1) Check wordnet to gether information about a word 
	 * 2) Save checked words in three hash tables, singularRecords (singular), numberRecords (number), and POSRecords (pos), respectively
	 * 
	 * @param word
	 *            The word to check
	 * @param mode
	 *            The mode can be "signular", "number", "pos"
	 * @return 1) mode "singular": if a plural noun, return its singular form, otherwise, return itself. 
	 *         2) mode "number": if a noun, return "p" [plural] or "s"[singular], else if not in WN "", otherwise "x".
	 *         3) mode "pos": return n [p,s], v, a, r, "" (not in WN).
	 */
	public String checkWN(String word, String mode) {		
		/**
		 * 0.0 If the word contains nothing but non-word characters, such as <>, return empty
		 * 0.1 Check singularRecordsprevious records
		 * 0.2 Special cases
		 * 1   Word not in WordNet
		 * 	1.1
		 * 	1.2
		 * 	1.3
		 * 2.  Word in WordNet
		 * 	2.1 mode is singular or number
		 * 		2.1.1
		 * 		2.1.2
		 *  2.2 mode is pos
		 */
		
		//this.myWN.getMostLikleyPOS(word);		
		// If the word contains nothing but non-word characters, such as <>, return empty
		word = word.replaceAll("\\W", "");
		if (word.equals("")) {
			return "";
		}
		
		// Check previous records
		// singular case
		String singular = null;
		if (StringUtils.equals(mode, "singular")) {
			singular = this.singularRecords.get(word);
		}
		if (singular != null) {
			if (singular.matches("^.*\\w.*$")) {
				return singular;
			}
		}

		// number case
		String number = null;
		if (StringUtils.equals(mode, "number")) {
			number = this.numberRecords.get(word);
		}
		if (number != null) {
			if (number.matches("^.*\\w.*$")) {
				return number;
			}
		}

		// pos case
		String pos = null;
		if (StringUtils.equals(mode, "pos")) {
			pos = this.POSRecords.get(word);
		}
		if (pos != null) {
			if (pos.matches("^.*\\w.*$")) {
				return pos;
			}
		}
	
		// Case 0: special cases
		if (word.equals("teeth")) {
			this.numberRecords.put("teeth", "p");
			this.singularRecords.put("teeth", "tooth");
			return mode.equals("singular")?"tooth":"p";
		}
		
		if (word.equals("tooth")) {
			this.numberRecords.put("tooth", "s");
			this.singularRecords.put("tooth", "tooth");
			return mode.equals("singular")?"tooth":"s";
		}

		if (word.equals("NUM")) {
			return mode.equals("singular")?"NUM":"s";
		}

		if (word.equals("or")) {
			return mode.equals("singular")?"or":"";
		}

		if (word.equals("and")) {
			return mode.equals("singular")?"and":"";
		}

		// concentrically
		if (word.matches("^.*[a-z]{3,}ly$")) {
			if (mode.equals("singular")) {
				return word;	
			}
			if (mode.equals("number")) {
				return "";	
			}
			if (mode.equals("pos")) {
				return "r";	
			}
		}
		
		// otherwise, call WordNet
		// Case 1
		if (!this.myWN.contains(word)) {// word not in WN
			boolean f = this.myWN.contains(word);
			this.POSRecords.put(word, "");
			String wordCopy = word;
			word = word.replaceAll("ed$", "");
			// Case 1.1
			if (!word.equals(wordCopy)) {
				if (this.myWN.contains(word)) {
					// Case 1.1.1-1.1.3
					if (mode.equals("singular")) {
						return word;
					}
					if (mode.equals("number")) {
						return "";
					}
					if (mode.equals("pos")) {
						return "a";
					}
				}
			}
			
			word = wordCopy;
			word = word.replace("^(" + Constant.PREFIX + ")+", "");
			// Case 1.2
			if (word.equals(wordCopy)) {
				return mode.equals("singular") ? word : "";
			} 
			// Case 1.3
			else {
				if (!this.myWN.contains(word)) {
					return mode.equals("singular") ? wordCopy : "";
				}
			}
		}
		else { if (mode.equals("singular") || mode.equals("number")) {
			// Case 2.1.1: not a noun
			if (!this.myWN.isNoun(word)) {
				return mode.equals("singular") ? word : "x";
			}
			List<String> stemList = myWN.getSingulars(word);
			int maxLength = 100;
			String sWord = "";
			for (int i=0;i<stemList.size();i++) {
				String stem = stemList.get(i);
				if (stem.length()<maxLength) {
					maxLength = stem.length();
					sWord = stem;
				}
			}
			this.singularRecords.put(word, sWord);
			// Case 2.1.2: singular
			if (sWord.equals(word)) {
				this.numberRecords.put(word, "s");
				return mode.equals("singular")?sWord:"s";
			}
			// Case 2.1.3: plural
			else {
				this.numberRecords.put(word, "p");
				return mode.equals("singular")?sWord:"p";
			}		
		}
		// Case 2.2
		else if (mode.equals("pos")) {
			pos = "";
			if (this.myWN.isNoun(word)){
				pos = pos+"n";
			}
			if (this.myWN.isVerb(word)){
				pos = pos+"v";
			}
			if (this.myWN.isAdjective(word)){
				pos = pos+"a";
			}
			if (this.myWN.isAdverb(word)){
				pos = pos+"r";
			}
			this.POSRecords.put(word, pos);
			if ((this.myWN.isNoun(word)) 
					&& (this.myWN.isVerb(word))
					&& (word.matches("^.*(ed|ing)$"))) {
				pos.replaceAll("n", "");
			}
			this.POSRecords.put(word, pos);
			return pos;
			}
		}
		return "";
	}

	/**
	 * Helper of method updateTable: Given a word, return [p] if it is a plural, [s] if it is singular
	 * @param w
	 * @return
	 */
	public String getNumber(String word) {
		//remove non-word characters, such as <>
		Matcher m = StringUtility.createMatcher("\\W", word);
		word = m.replaceAll("");
		
		String number = checkWN(word, "number");
		String rt = "";

		// Case 1
		rt = getNumberHelper1(number);
		if (rt != null) {
			return rt;
		}

		// Case 2
		rt = getNumberHelper2(word);
		if (rt != null) {
			return rt;
		} 
		// Case 3: return "s"
		else {
			return "s";
		}
	}

	/**
	 * First Helper of method getNumber
	 * 
	 * @param number
	 * @return if match return the right number, otherwise return null means not
	 *         match
	 */
	public String getNumberHelper1(String number) {
		if (number.matches("^.*[sp].*$")) {
			return number;
		}
		// return "?" for "x"
		else if (number.matches("^.*x.*$")) {
			return "?";
		}
		else {
			return null;
		}
	}

	/**
	 * Second Helper of method getNumber
	 * 
	 * @param word
	 * @return if match return the right number, otherwise return null means not
	 *         match
	 */
	public String getNumberHelper2(String word) {
		// Calyculi => 1. Calyculus, pappi => pappus
		if (word.matches("^.*i$")) {
			return "p";
		}
		else if (word.matches("^.*ss$")) {
			return "s";
		}
		else if (word.matches("^.*ia$")) {
			return "p";
		}
		else if (word.matches("^.*[it]um$")) {
			return "s";
		}
		else if (word.matches("^.*ae$")) {
			return "p";
		}
		// non-noun cases
		else if (word.matches("^.*ous$")) {
			return "?"; 
		}
		// non-noun cases
		else if (word.matches("^[aiu]s$")) {
			return "?";
		}
		// this case only handle three words: as, is, us
		else if (word.matches("^.*us$")) {
			return "s";
		}
		else if (word.matches("^.*es$") || word.matches("^.*s$")) {
			return "p";
		}
		// non-noun cases
		else if (word.matches("^.*ate$")) {
			return "?";
		}
		else {
			return null;
		}
		
	}	

	/**
	 * Get the singular form of a word
	 * 
	 * @param word
	 *            the word in query
	 * @return the singular form of the input word. If it the method fails to
	 *         find any singular form of the word, return an empty string
	 */
	public String getSingular(String word) {
		if (!word.matches("^.*\\w.*$")) {
			return "";
		}

		if (word.equals("valves")) {
			return "valve";
		} else if (word.equals("media")) {
			return "media";
		} else if (word.equals("species")) {
			return "species";
		} else if (word.equals("axes")) {
			return "axis";
		} else if (word.equals("calyces")) {
			return "calyx";
		} else if (word.equals("frons")) {
			return "frons";
		} else if (word.equals("grooves")) {
			return "groove";
		} else if (word.equals("nerves")) {
			return "nerve";
		}

		String singular = "";
		if (getNumber(word).equals("p")) {
			// Case 1
			Pattern p = Pattern.compile("(^.*?[^aeiou])ies$");
			Matcher m = p.matcher(word);
			if (m.lookingAt()) {
				singular = m.group(1) + "y";
			} 
			else {
			// Case 2
			p = Pattern.compile("(^.*?)i$");
			m = p.matcher(word);
			if (m.lookingAt()) {
				singular = m.group(1) + "us";
			} 
			else {	
			// Case 3				
			p = Pattern.compile("(^.*?)ia$");
			m = p.matcher(word);
			if (m.lookingAt()) {
				singular = m.group(1) + "ium";
			} 
			else {
			// Case 4	
			p = Pattern.compile("(^.*?(x|ch|sh|ss))es$");
			m = p.matcher(word);
			if (m.lookingAt()) {
				singular = m.group(1);
			} 
			else {
			// Case 5	
			p = Pattern.compile("(^.*?)ves$");
			m = p.matcher(word);
			if (m.lookingAt()) {
				singular = m.group(1) + "f";
				} 
			else {
			// Case 6	
			p = Pattern.compile("(^.*?)ices");
			m = p.matcher(word);
			if (m.lookingAt()) {
				singular = m.group(1) + "ex";
			} 
			else {
			// Case 7.1
			// pinnae ->pinna
			p = Pattern.compile("(^.*?a)e$");
			m = p.matcher(word);
			if (m.lookingAt()) {
				singular = m.group(1);
			} 
			else {
			// Case 7.2
			// fruits->fruit
			p = Pattern.compile("(^.*?)s$");
			m = p.matcher(word);
			if (m.lookingAt()) {
				singular = m.group(1);
			}
			}
			}
			}
			}
			}
			}
			}
		}

		if (singular.matches("^.*\\w.*$")) {
			return singular;
		}

		singular = checkWN(word, "singular");
		if (singular.matches("^.*\\w.*$")) {
			return singular;
		}

		return "";
	}	

	/**
	 * Get all the plural forms of a word
	 * 
	 * @param word
	 *            the word in query
	 * @return a list of the plural forms of the word. If the method fails to
	 *         find any plural forms of the method, return an empty list
	 */
	public List<String> getPlural(String word) {
		
		if (word.matches("^(n|2n|x)$")) {
			return new ArrayList<String>();
		}
		
		String plural = "";
		if (this.PLURALS.containsKey(word)) {
			plural = this.PLURALS.get(word);

			if (plural.matches("^.*\\w+.*$")) {
				String[] pArray = plural.split(" ");
				List<String> pList = new ArrayList<String>();
				Collections.addAll(pList, pArray);
				return pList;
			}
		}
		
		plural = getPluralSpecialCaseHelper(word);
		if (!plural.equals("")) {
			;
		}
		else {
			plural = getPluralRuleHelper(word);
			plural = plural+" "+word+"s";
		}

		plural=plural.replaceAll("^\\s+", "");
		plural=plural.replaceAll("\\s+$", "");
		String[] pls = plural.split(" ");
		String plStr = "";
		for (int i = 0; i < pls.length; i++) {
			if (this.getWORDS().containsKey(pls[i])) {
				if (this.getWORDS().get(pls[i]) >= 1) {
					plStr = plStr + pls[i] + " ";
				}
			}
		}
		plStr = plStr.replaceAll("\\s+$", "");
		this.PLURALS.put(word, plStr);
		
		List<String> pList = new ArrayList<String>();
		
		if (!plStr.equals("")) {
			String[] pArray = plStr.split(" ");
			Collections.addAll(pList, pArray);
		}
		
		return pList;
	}

	/**
	 * A helper method used by method getPlural. Help to apply a number of rules
	 * 
	 * @param word
	 * @return if the word has plural form(s), return it(them); otherwise return ""
	 */
	public String getPluralRuleHelper(String word) {
		String plural;
		Pattern p;
		Matcher m;

		// Case 1
		p = Pattern.compile("(^.*?)(ex|ix)$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "ices";
			plural = plural + " " + m.group(1) + m.group(2) + "es";
			return plural;
		}

		// Case 2
		p = Pattern.compile("^.*(x|ch|ss|sh)$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = word + "es";
			return plural;
		}

		// Case 3
		p = Pattern.compile("(^.*?)([^aeiouy])y$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + m.group(2) + "ies";
			return plural;
		}

		// Case 4
		p = Pattern.compile("(^.*?)(?:([^f])fe|([oaelr])f)$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			String s1 = m.group(1);
			String s2 = m.group(2);
			String s3 = m.group(3);
			if (s2 != null) {
				plural = s1 + s2 + "ves";
			} else {
				plural = s1 + s3 + "ves";
			}
			return plural;
		}

		// Case 5
		p = Pattern.compile("(^.*?)(x|s)is$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + m.group(2) + "es";
			return plural;
		}

		// Case 6
		p = Pattern.compile("(^.*?)([tidlv])um$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + m.group(2) + "a";
			return plural;
		}

		// Case 7
		p = Pattern.compile("(^.*?)(ex|ix)$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "ices";
			return plural;
		}

		// Case 8
		p = Pattern.compile("(^.*?[^t][^i])on$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "a";
			return plural;
		}

		// Case 9
		p = Pattern.compile("(^.*?)a$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "ae";
			return plural;
		}

		// Case 10
		p = Pattern.compile("(^.*?)man$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "men";
			return plural;
		}

		// Case 11
		p = Pattern.compile("(^.*?)child$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "children";
			return plural;
		}

		// Case 12
		p = Pattern.compile("(^.*)status$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "statuses";
			return plural;
		}

		// Case 13
		p = Pattern.compile("(^.+?)us$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "i";
			plural = plural + " " + m.group(1) + "uses";
			return plural;
		}

		// Case 14
		p = Pattern.compile("^.*s$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = word + "es";
			return plural;
		}

		return "";
	}

	/**
	 * A helper method used by method getPlural. Help to handle special cases.
	 * 
	 * @param word
	 * @return if the word match any special case, return its plural form, return it; otherwise return ""
	 */
	public String getPluralSpecialCaseHelper(String word) {
		String plural;
		Pattern p;
		Matcher m;

		// Case 1
		p = Pattern.compile("^.*series$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = word;
			return plural;
		}

		// Case 2		
		p = Pattern.compile("(^.*?)foot$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "feet";
			return plural;
		}
		
		// Case 3
		p = Pattern.compile("(^.*?)tooth$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "teeth";
			return plural;
		}

		// Case 4		
		p = Pattern.compile("(^.*?)alga$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "algae";
			return plural;
		}

		// Case 5		
		p = Pattern.compile("(^.*?)genus$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "genera";
			return plural;
		}

		// Case 6		
		p = Pattern.compile("(^.*?)corpus$");
		m = p.matcher(word);
		if (m.lookingAt()) {
			plural = m.group(1) + "corpora";
			return plural;
		}

		return "";
	}
	

	List<String> getSingularPluralPair(String word1, String word2) {
		List<String> pair = new ArrayList<String>();
		String singular = "";
		String plural = "";
		
		// put the shorter word at first
		int len1 = word1.length();
		int len2 = word2.length();
		if (len1 > len2) {
			String temp_word = word1;
			word1 = word2;
			word2 = temp_word;
			int temp_len = len1;
			len1 = len2;
			len2 = temp_len;
		}
		
		if ((word1.matches("^.*" + Constant.SENDINGS))
				&& (word2.matches("^.*" + Constant.PENDINGS))) {		
			// case 1.1.1
			if (word2.matches("^.*" + "es$")
					&& word1.matches("^.*" + "is$")
					&& Math.abs(len1 - len2) == 0) {
				singular = word1;
				plural = word2;
			} 
			// case 1.5
			else if (word1.matches("^.*" + "us$")
					&& word2.matches("^.*" + "a$")
					&& Math.abs(len1 - len2) < 2) {
				singular = word1;
				plural = word2;
			}

		}
		else if ((word1.matches("^.*" + Constant.PENDINGS))
				&& (word2.matches("^.*" + Constant.SENDINGS))) {
			// case 1.1.2
			if (word1.matches("^.*" + "es$") 
					&& word2.matches("^.*" + "is$")
					&& Math.abs(len1 - len2) == 0) {
				singular = word2;
				plural = word1;
				
			}
			// case 1.2
			else if (word1.matches("^.*" + "a$")
					&& word2.matches("^.*" + "on$")
					&& Math.abs(len1 - len2) < 2) {
				singular = word2;
				plural = word1;
			}
			// case 1.3
			else if (word1.matches("^.*" + "a$")
					&& word2.matches("^.*" + "um$")
					&& Math.abs(len1 - len2) < 2) {
				singular = word2;
				plural = word1;
			}
			// case 1.4
			else if (word1.matches("^.*" + "i$")
					&& word2.matches("^.*" + "us$")
					&& Math.abs(len1 - len2) < 2) {
				singular = word2;
				plural = word1;
			} 
		} else {
			// thicker, thickness; species, specimens; tomentulose, tomentulous;
			// later laterals
			if (word2.matches("^.*s$")) {
				if (getSingularPluralPairHelper(word1, word2)) {
					singular = word1;
					plural = word2;
				}
			}
		}

		if ((!singular.equals("")) && (!plural.equals(""))) {
			pair.add(singular);
			pair.add(plural);
		}

		return pair;
	}

	// word2 has no other letters except those appearing in word1 or ies, and
	// vice versa.
	public boolean getSingularPluralPairHelper(String word1, String word2) {
		int len1 = word1.length();
		int len2 = word2.length();

		if ((!word2.matches("^\\[^" + word1 + "yies" + "\\]*&"))
				&& (!word1.matches("^\\[^" + word2 + "yies" + "\\]*&"))
				&& (Math.abs(len1 - len2) > 0) && (Math.abs(len1 - len2) < 3)) {
			return true;
		} else {
			return false;
		}
	}

	public String getRoot(String word) {
		String root;
		this.myStemmer.add(word.toCharArray(), word.length());
		this.myStemmer.stem();
		root = this.myStemmer.toString();
		return root;
	}
	

	public Map<String, Integer> getWORDS() {
		return WORDS;
	}

	public void setWORDS(Map<String, Integer> w) {
		WORDS = w;
	}	
	
	
}
