package semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class DataHolder {
	// all unique words in the input treatments
	public Map<String, Integer> allWords;
	
	// Data holders
	// Table heuristicnoun
	private Map<String, String> heuristicNounTable;
	public static final byte HEURISTICNOUN = 1;

	// Table discounted
	private Map<DiscountedKey, String> discountedTable;
	public static final byte DISCOUNTED = 2;
	
	// Table isATable
	private Map<Integer, IsAValue> isATable;
	public static final byte ISA = 3;
	
	// Table modifier
	private Map<String, ModifierTableValue> modifierTable;
	public static final byte MODIFIER = 4;
	
	// Table sentence
	private List<SentenceStructure> sentenceTable = new LinkedList<SentenceStructure>();
	private int sentenceCount;
	//private Map<Integer, Sentence> sentenceCollection;
	public static final byte SENTENCE = 5;

	// Table singularPlural
	private Set<SingularPluralPair> singularPluralTable;
	public static final byte SINGULAR_PLURAL = 6;

	// Table termCategory
	private Set<StringPair> termCategoryTable;
	public static final byte TERM_CATEGORY = 10;
	
	// Table unknownword
	private Map<String, String> unknownWordTable;
	public static final byte UNKNOWNWORD = 7;

	// Table wordpos
	private Map<WordPOSKey, WordPOSValue> wordPOSTable;
	public static final byte WORDPOS = 8;
	
	// Table wordrole
	private Map<StringPair, String> wordRoleTable;
	public static final byte WORDROLE = 9;

	private Configuration myConfiguratio;
	private Utility myUtility;
	
	public DataHolder(Configuration myConfiguration, Utility myUtility) {
		this.myConfiguratio = myConfiguration;
		this.myUtility = myUtility;
		this.allWords = new HashMap<String, Integer>();
		
		this.discountedTable = new HashMap<DiscountedKey, String>();
		this.heuristicNounTable = new HashMap<String, String>();
		this.isATable = new HashMap<Integer, IsAValue>();
		this.modifierTable = new HashMap<String, ModifierTableValue>();
		
		this.sentenceTable = new LinkedList<SentenceStructure>();
		this.sentenceCount = 0;
		
		this.singularPluralTable = new HashSet<SingularPluralPair>();
		this.termCategoryTable = new HashSet<StringPair>();
		this.unknownWordTable = new HashMap<String, String>();
		this.wordPOSTable = new HashMap<WordPOSKey, WordPOSValue>();
		this.wordRoleTable = new HashMap<StringPair, String>();
		
	}
	
//	/**
//	 * This method updates a new word in the unknownWord table
//	 * 
//	 * @param newWord
//	 * @param sourceWord
//	 * @return if any updates occurred, returns true; otherwise, returns false
//	 */
//	public boolean updateUnknownWord(String newWord, String flag) {
//		boolean result = false;
//		Iterator<Map.Entry<String, String>> iter = this.unknownWordTable
//				.entrySet().iterator();
//
//		while (iter.hasNext()) {
//			Map.Entry<String, String> unknownWord = iter.next();
//			if (unknownWord.getKey().equals(newWord)) {
//				unknownWord.setValue(flag);
//				result = true;
//			}
//		}
//
//		return result;
//	}
	
	
	/** Operations**/
	
	/**
	 * 
	 * @param word
	 * @param flag
	 */
	public void updateUnknownWord(String word, String flag){
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.updateUnkownWord");	
		this.unknownWordTable.put(word, flag);
		myLogger.trace(String.format("Added (%s, %s) into UnknownWord holder", word, flag));
	}
	
	public void addSentence(String source, String sentence,
			String originalSentence, String lead, String status, String tag,
			String modifier, String type) {
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.addSentence");	
		
		SentenceStructure newSent = new SentenceStructure(this.sentenceCount, source, sentence, originalSentence, lead,
				status, tag, modifier, type);
		this.sentenceCount++;
		this.sentenceTable.add(newSent);
		myLogger.trace("Added Sentence: ");
		myLogger.trace("\tSource: " + source);
		myLogger.trace("\tSentence: " + sentence);
		myLogger.trace("\tOriginal Sentence: " + originalSentence);
		myLogger.trace("\tLead: " + lead);
		myLogger.trace("\tStatus: " + status);
		myLogger.trace("\tTag: " + tag);
		myLogger.trace("\tModifier: " + modifier);
		myLogger.trace("\tType: " + type);
		
		myLogger.trace("Quite\n");
	}
	
	public SentenceStructure getSentence(int ID) {
		Iterator<SentenceStructure> iter = this.sentenceTable.iterator();
		
		while(iter.hasNext()) {
			SentenceStructure sentence = iter.next();
			if (sentence.getID()==ID) {
				return sentence;
			}
		}
		
		return null;
	}
    
    public List<Entry<WordPOSKey,WordPOSValue>> getWordPOSEntries(String word) {
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.getWordPOSEntries");
        
		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.getWordPOSHolder()
				.entrySet().iterator();
		List<Entry<WordPOSKey, WordPOSValue>> result = new ArrayList<Entry<WordPOSKey, WordPOSValue>>();
		while (iter.hasNext()) {
			Map.Entry<WordPOSKey, WordPOSValue> wordPOSEntry = iter.next();
			if (StringUtils.equals(wordPOSEntry.getKey().getWord(), word)) {
				result.add(wordPOSEntry);
			}
		}
		
		myLogger.trace("Get WordPOS Entries of word: " + word);
		myLogger.trace(StringUtils.join(result, ",\n"));		
		
		return result;
    }
    
    public void updateWordPOS(String word, String POS, String role, int certaintyU, int certaintyL, String savedFlag, String savedID) {
    	PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.addWordPOS");
        
        WordPOSKey key = new WordPOSKey(word, POS);
		WordPOSValue value = new WordPOSValue(role, certaintyU, certaintyL, savedFlag, savedID);
		this.getWordPOSHolder().put(key, value);
        
		myLogger.trace(String.format(
				"Added [Key: %s = Value: %s] into WordPOS holder",
				key.toString(), value.toString()));
    }    

    @Override
	public boolean equals(Object obj) {
		if (obj==this){
			return true;
		}
		
		if (obj==null||obj.getClass()!=this.getClass()){
			return false;
		}
		
		DataHolder myDataHolder = (DataHolder) obj;
		
		return ((this.discountedTable.equals(myDataHolder.discountedTable))
				&& (this.heuristicNounTable.equals(myDataHolder.heuristicNounTable))
				&& (this.modifierTable.equals(myDataHolder.modifierTable))
				&& (this.sentenceTable.equals(myDataHolder.sentenceTable))
				&& (this.singularPluralTable.equals(myDataHolder.singularPluralTable))
				&& (this.unknownWordTable.equals(myDataHolder.unknownWordTable))
				&& (this.wordPOSTable.equals(myDataHolder.wordPOSTable))
				&& (this.allWords.equals(myDataHolder.allWords))
				);
	}	
	
	/** Sentence Table Utility***************************************/

	public Map<DiscountedKey, String> getDiscountedHolder(){
		return this.discountedTable;
	}
	
	public Map<String, ModifierTableValue> getModifierHolder(){
		return this.modifierTable;
	}
	
	public Map<String, String> getHeuristicNounHolder(){
		return this.heuristicNounTable;
	}

	public List<SentenceStructure> getSentenceHolder(){
		return this.sentenceTable;
	}
	
	public Set<SingularPluralPair> getSingularPluralHolder(){
		return this.singularPluralTable;
	}
	
	public Set<StringPair> getTermCategoryHolder() {
		return this.termCategoryTable;
	}

	public Map<String, String> getUnknownWordHolder(){
		return this.unknownWordTable;
	}

	public Map<WordPOSKey, WordPOSValue> getWordPOSHolder(){
		return this.wordPOSTable;
	}
	
	public Map<StringPair, String> getWordRoleHolder(){
		return this.wordRoleTable;
	}
	
	
	/** Heuristic Noun Table Utility*********************************/
	public Map<String, String> getHeuristicNounTable(){
		return this.heuristicNounTable;
	}
	
	
	/** Singular Plural Table Utility********************************/
	
	/**
	 * check if the word is in the singularPluralTable.
	 * 
	 * @param word
	 *            the word to check
	 * @return true if the word is in the SingularPluralTable; false otherwise.
	 */
	public boolean isInSingularPluralPair(String word) {
		Iterator<SingularPluralPair> iter = this.singularPluralTable.iterator();

		while (iter.hasNext()) {
			SingularPluralPair spp = iter.next();
			if ((spp.getSingular().equals(word))
					|| (spp.getPlural().equals(word))) {
				return true;
			}
		}
		return false;
	}
	
	public List<String> getWordByPOS(String POSs) {
		List<String> words = new ArrayList<String>();
		int index = POSs.length();
		for (int i = 0;i<POSs.length();i++) {
			String POS = POSs.substring(i,i+1);
			Iterator<Entry<WordPOSKey, WordPOSValue>> iterator = this.wordPOSTable.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<WordPOSKey, WordPOSValue> entry = iterator.next();
				WordPOSKey key = entry.getKey();
				if (StringUtils.equals(key.getPOS(),POS)) {
					words.add(key.getWord());
				}
				
			}
		}
		
		return words;
	}
	
	/**
	 * add the singular form and the plural form of a word into the
	 * singularPluarlTable
	 * 
	 * @param sgl
	 *            singular form
	 * @param pl
	 *            plural form
	 * @return if add a pair, return true; otherwise return false
	 */
	public boolean addSingularPluralPair(String sgl, String pl) {
		
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.addsingularpluralpair");				
		
		SingularPluralPair pair = new SingularPluralPair(sgl, pl);
		boolean result = this.singularPluralTable.add(pair);
		
		myLogger.debug(String.format("Added singular-plural pair (%s, %s)", sgl, pl));
		return result;
	}
	
	
	/** Unknown Word Table Utility***********************************/
	
	/**
	 * 
	 * @param word
	 * @param tag
	 */
	public void addUnknown(String word, String tag) {
		this.unknownWordTable.put(word, tag);
	}
	
	
	/** Modifier Table Utility***************************************/
	
	/**
	 * Take a new word, insert it into the modifier holder, or update its count in
	 * modifier holder if it already exists
	 * 
	 * @param newWord
	 * @param increment
	 * @return if anything changed in modifier holder, return true; otherwise
	 *         return false
	 */
	public int addModifier(String newWord, int increment) {
		int isUpdate = 0;

		if ((newWord.matches("(" + Constant.STOP + "|^.*\\w+ly$)"))
				|| (!(newWord.matches("^.*\\w.*$")))) {
			return isUpdate;
		}

		if (this.modifierTable.containsKey(newWord)) {
			int count = this.modifierTable.get(newWord).getCount();
			count = count + increment;
			this.modifierTable.get(newWord).setCount(count);
			// isUpdate = 1;
		} else {
			this.modifierTable.put(newWord, new ModifierTableValue(1, false));
			isUpdate = 1;
		}

		return isUpdate;
	}

	/**
	 * Pick one from bPOS and otherPOS and return it
	 * 
	 * @param newWord
	 * @param bPOS
	 * @param otherPOS
	 * @return if the newWord appears after a plural noun in any untagged
	 *         sentence, return the bPOS; otherwise, return the otherPOS
	 */
	public String resolveConflict(String newWord, String bPOS, String otherPOS) {
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.resolveConflict");
		
		myLogger.trace("Enter resolveConflict");

		int count = 0;
		List<SentenceStructure> mySentenceHolder = this.getSentenceHolder();
		for (int i = 0; i < mySentenceHolder.size(); i++) {
			SentenceStructure sentence = mySentenceHolder.get(i);
			boolean flag = false;
			flag = sentence.getTag() == null ? 
					true : (!sentence.getTag().equals("ignore"));
			if (flag) {
				String regex = "^.*?([a-z]+(" + Constant.PLENDINGS + ")) ("
						+ newWord + ").*$";
				Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				String originalSentence = sentence.getOriginalSentence();
				Matcher m = p.matcher(originalSentence);
				if (m.find()) {
					String plural = m.group(1).toLowerCase();
					if (this.myUtility.getWordFormUtility().getNumber(plural)
							.equals("p")) {
						count++;
					}
					if (count >= 1) {
						myLogger.trace("Quite resolveConflict, return: " + bPOS);
						return bPOS;
					}
				}
			}
		}
		
		myLogger.trace("Quite resolveConflict, return: "+otherPOS);
		return otherPOS;
	}
	
	/**
	 * Discount existing pos, but do not establish suggested pos
	 * 
	 * @param newWord
	 * @param oldPOS
	 * @param newPOS
	 * @param mode
	 *            "byone" - reduce certainty 1 by 1. "all" - remove this POS
	 */
	public void discountPOS(String newWord, String oldPOS, String newPOS,
			String mode) {
		/**
		 * 1. Find the flag of newWord in unknownWords table
		 * 1. Select all words from unknownWords table who has the same flag (including newWord)
		 * 1. From wordPOS table, select certaintyU of the (word, oldPOS) where word is in the words list
		 *     For each of them
		 *     1.1 Case 1: certaintyu less than 1, AND mode is "all"
		 *         1.1.1 Delete the entry from wordpos table
		 *         1.1.1 Update unknownwords
		 *             1.1.1.1 Case 1: the pos is "s" or "p"
		 *                 Delete all entries contains word from singularplural table as well
		 *         1.1.1 Insert (word, oldpos, newpos) into discounted table
		 */
		
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.discountPOS");
		
		myLogger.trace("Enter discountPOS");
		
		// get the flag of the newWord
		String flag = this.unknownWordTable.get(newWord);

		// get the word list
		List<String> wordList = new ArrayList<String>();
		Iterator<Map.Entry<String, String>> unknownWordIter = this.unknownWordTable.entrySet().iterator();
		while (unknownWordIter.hasNext()) {
			Map.Entry<String, String> e = unknownWordIter.next();
			if (e.getValue().equals(flag)) {
				wordList.add(e.getKey());
			}
		}
		myLogger.debug(wordList.toString());
		
		//wordList.add(newWord);
		
		for (int i=0;i<wordList.size();i++) {
			String word = wordList.get(i);
			WordPOSKey key = new WordPOSKey(word, oldPOS);
			if (this.wordPOSTable.containsKey(key)) {
				WordPOSValue value = this.wordPOSTable.get(key);
				int cU = value.getCertaintyU();
				if (cU <= 1 && mode.equals("all")) {
					this.wordPOSTable.remove(key);
					this.updateUnknownWord(word, "unknown");
					// delete from SingularPluralHolder
					if (oldPOS.matches("^.*[sp].*$")) {
						// list of entries to be deleted
						ArrayList<SingularPluralPair> delList = new ArrayList<SingularPluralPair>();

						// find entries to be deleted, put them into delList
						Iterator<SingularPluralPair> iterSPTable = this.singularPluralTable.iterator();
						while (iterSPTable.hasNext()) {
							SingularPluralPair spp = iterSPTable.next();
							if (spp.getSingular().equals(word)
									|| spp.getPlural().equals(word)) {
								delList.add(spp);
							}
						}

						// delete all entries in delList from singularPluralTable
						Iterator<SingularPluralPair> delListIter = delList.iterator();
						while (delListIter.hasNext()) {
							SingularPluralPair del = delListIter.next();
							this.singularPluralTable.remove(del);
						}
					}
					
					DiscountedKey dKey = new DiscountedKey(word, oldPOS);
					this.discountedTable.put(dKey, newPOS);
				}
				else {
					WordPOSValue temp = this.wordPOSTable.get(key);
					int certaintyU = temp.getCertaintyU();
					temp.setCertiantyU(certaintyU-1);
					this.wordPOSTable.put(key, temp);
				}
			}
		}
		
		myLogger.trace("Quite discountPOS");
	}
	
	
	/**
	 * Given a new role, and the old role, of a word, decide the right role to
	 * return
	 * 
	 * @param oldRole
	 * @param newRole
	 * @return oldRole or newRole, whichever wins
	 */
	public String mergeRole(String oldRole, String newRole) {

		// if old role is "*", return the new role
		if (oldRole.equals("*")) {
			return newRole;
		}
		// if the new role is "*", return the old rule
		else if (newRole.equals("*")) {
			return oldRole;
		}

		// if the old role is empty, return the new role
		if (oldRole.equals("")) {
			return newRole;
		}
		// if the new role is empty, return the old role
		else if (newRole.equals("")) {
			return oldRole;
		}
		// if the old role is not same as the new role, return "+"
		else if (!oldRole.equals(newRole)) {
			return "+";
		}
		// if none of above apply, return the old role by default
		else {
			return oldRole;
		}
	}
	
	/**
	 * Find the tag of the sentence of which this sentid (clause) is a part of
	 * 
	 * @param sentID
	 * @return a tag
	 */
	public String getParentSentenceTag(int sentID) {
		/**
		 * 1. Get the originalsent of sentence with sentID 
		 * 1. Case 1: the originalsent of $sentence sentID starts with a [a-z\d] 
		 * 1.1 select modifier and tag from Sentence where tag is not "ignore" OR tag is null 
		 *      AND originalsent COLLATE utf8_bin regexp '^[A-Z].*' OR originalsent rlike ': *\$' AND id < sentID 
		 * 1.1 take the tag of the first sentence (with smallest id), get its modifier and tag 
		 * 1.1 if modifier matches \w, tag = modifier + space + tag 
		 * 1.1 remove [ and ] from tag 
		 * 1. if tag matches \w return [+tag+], else return [parenttag]
		 */

		String originalSentence = this.sentenceTable.get(sentID)
				.getOriginalSentence();
		String tag = "";
		String oSentence = "";
		if (originalSentence.matches("^\\s*[^A-Z].*$")) {
		//if (originalSent.matches("^\\s*([a-z]|\\d).*$")) {
			for (int i = 0; i < sentID; i++) {
				SentenceStructure sentence = this.sentenceTable.get(i);
				tag = sentence.getTag();
				oSentence = sentence.getOriginalSentence();
				boolean flag = (tag == null)? true : (!tag.matches("ignore"));

				if (flag && ((oSentence.matches("^[A-Z].*$")) || (oSentence
								.matches("^.*:\\s*$")))) {
					String modifier = sentence.getModifier();
					if (modifier.matches("^.*\\w.*$")) {
						if (tag == null) {
							tag = "";
						}
						tag = modifier + " " + tag;
						tag = tag.replaceAll("[\\[\\]]", "");
					}
					break;
				}
			}
		}

		return tag.matches("^.*\\w.*$") ? "[" + tag + "]" : "[parenttag]" ;
	}
	
	/**
	 * 
	 * @param tag
	 * @return
	 */
	public List<String> getMTFromParentTag(String tag) {
		String modifier = "";
		String newTag = "";

		Pattern p = Pattern.compile("^\\[(\\w+)\\s+(\\w+)\\]$");
		Matcher m = p.matcher(tag);
		if (m.lookingAt()) {
			modifier = m.group(1);
			newTag = m.group(2);
		} else {
			p = Pattern.compile("^(\\w+)\\s+(\\w+)$");
			m = p.matcher(tag);
			if (m.lookingAt()) {
				modifier = m.group(1);
				newTag = m.group(2);
			}

		}
		List<String> pair = new ArrayList<String>();
		pair.add(modifier);
		pair.add(newTag);

		return pair;
	}
	
	/**
	 * Remove ly ending word which is a "b" in the WordPOS, from the modifier
	 * 
	 * @param modifier
	 * @return the new modifer
	 */
	public String tagSentWithMTRemoveLyEndingBoundary(String modifier) {
		
		Pattern p = Pattern.compile("^(\\w+ly)\\s*(.*)$");
		Matcher m = p.matcher(modifier);
		while (m.lookingAt()) {
			String wordly = m.group(1);
			String rest = m.group(2);
			WordPOSKey wp = new WordPOSKey(wordly, "b");
			if (this.wordPOSTable.containsKey(wp)) {
				modifier = rest;
				m = p.matcher(modifier);
			} else {
				break;
			}
		}
		
		return modifier;
	}
		
	/**
	 * 
	 * @param word
	 * @param pos
	 * @param role
	 * @param table
	 * @param increment
	 * @return
	 */
	public int updateDataHolder(String word, String pos, String role, String table,
			int increment) {
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder");
		myLogger.trace(String.format("Enter (%s, %s, %s, %s, %d)", word, pos, role, table, increment));
		
		int result = 0;

		word = StringUtility.processWord(word);
		// empty word
		if (word.length() < 1) {
			return 0;
		}

		// forbidden word
		if (word.matches("\\b(?:" + Constant.FORBIDDEN + ")\\b")) {
			return 0;
		}

		// if it is a n word, check if it is singular or plural, and update the
		// pos
		if (pos.equals("n")) {
			pos = this.myUtility.getWordFormUtility().getNumber(word);
		}

		result = result + markKnown(word, pos, role, table, increment);
		myLogger.trace("result1: " + result);
		// 1) if the word is a singular form n word, find its plural form, then add
		// the plural form, and add the singular - pluarl pair into
		// singularPluarlTable;
		// 2) if the word is a plural form n word, find its singular form, then add
		// the singular form, and add the singular - pluarl pair into
		// singularPluarlTable;
		if (!this.isInSingularPluralPair(word)) {
			myLogger.trace("Search for singular-plural pair of word: " + word);
			
			if (pos.equals("p")) {
				myLogger.trace("Case 1");
				String pl = word;
				word = this.myUtility.getWordFormUtility().getSingular(word);
				myLogger.trace(String.format("Get singular form of %s: %s", pl,
						word));

				// add "*" and 0: pos for those words are inferred based on
				// other clues, not seen directly from the text
				result = result + this.markKnown(word, "s", "*", table, 0);
				myLogger.trace("result2: " + result);
				this.addSingularPluralPair(word, pl);
				myLogger.trace(String.format("Added (%s, %s)", word, pl));

			}
			else if (pos.equals("s")) {
				myLogger.trace("Case 2");
				List<String> words = this.myUtility.getWordFormUtility().getPlural(word);
				String sg = word;
//				if (sg.equals("centrum")) {
//					System.out.println("Return Size: "+words.size());
//				}
				for (int i = 0; i < words.size(); i++) {
					if (words.get(i).matches("^.*\\w.*$")) {
						result = result
								+ this.markKnown(words.get(i), "p", "*", table,
										0);
						myLogger.trace("result3: " + result);
					}
					this.addSingularPluralPair(sg, words.get(i));
					myLogger.trace(String.format("Added (%s, %s)", sg, words.get(i)));
				}
			}
			else {
				myLogger.trace("Nothing added");
			}
		}

		myLogger.trace("Return: "+result+"\n");
		return result;
	}

	/**
	 * mark a word with its pos and role in wordpos holder, or ???
	 * 
	 * @param word
	 *            the word to mark
	 * @param pos
	 *            the pos of the word
	 * @param role
	 *            the role of the word
	 * @param table
	 *            which table to mark
	 * @param increment
	 * @return
	 */
	public int markKnown(String word, String pos, String role, String table,
			int increment) {
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.markKnown");		
		myLogger.trace("Enter markKnown");
		
		String pattern = "";
		int sign = 0;
		String otherPrefix = "";
		String spWords = "";

		// forbidden word
		if (word.matches("\\b(?:" + Constant.FORBIDDEN + ")\\b")) {
			return 0;
		}

		// stop words
		if (word.matches("^(" + Constant.STOP + ")$")) {
			sign = sign
					+ processNewWord(word, pos, role, table, word, increment);
			return sign;
		}

		// process this new word
		sign = sign + processNewWord(word, pos, role, table, word, increment);
		
		// Case 1: we try to learn those new words based on this one
		Pattern p = Pattern.compile("^(" + Constant.PREFIX + ")(\\S+).*$");
		Matcher m = p.matcher(word);
		if (m.lookingAt()) {
			myLogger.trace("Case 1");
			String g1 = m.group(1); // the prefix
			String g2 = m.group(2); // the remaining

			otherPrefix = StringUtility.removeFromWordList(g1, Constant.PREFIX);

			spWords = "("
					+ StringUtility.escape(this.singularPluralVariations(g2,
							this.getSingularPluralHolder())) + ")";
			pattern = "^(" + otherPrefix + ")?" + spWords + "$";

			Iterator<Map.Entry<String, String>> iter1 = this.getUnknownWordHolder()
					.entrySet().iterator();

			
			while (iter1.hasNext()) {
				Map.Entry<String, String> entry = iter1.next();
				String newWord = entry.getKey();
				String flag = entry.getValue();

				if ((newWord.matches(pattern)) && (flag.equals("unknown"))) {
					sign = sign
							+ processNewWord(newWord, pos, "*", table, word, 0);
					
					myLogger.trace("Case 1.1");
					myLogger.trace("by removing prefix of " + word + ", know "
							+ newWord + " is a [" + pos + "]");
				}
			}
		}

		// Case 2: word starts with a lower case letter
		if (word.matches("^[a-z].*$")) {
			myLogger.trace("Case 2");
			spWords = "("
					+ StringUtility.escape(this.singularPluralVariations(word,
							this.getSingularPluralHolder())) + ")";
			// word=shrubs, pattern = (pre|sub)shrubs
			pattern = "^(" + Constant.PREFIX + ")" + spWords + "$";

			Iterator<Map.Entry<String, String>> iter2 = this.getUnknownWordHolder()
					.entrySet().iterator();

			while (iter2.hasNext()) {
				Map.Entry<String, String> entry = iter2.next();
				String newWord = entry.getKey();

				String flag = entry.getValue();
				// case 2.1
				if ((newWord.matches(pattern)) && (flag.equals("unknown"))) {
					sign = sign
							+ processNewWord(newWord, pos, "*", table, word, 0);
					
					myLogger.debug("Case 2.1");
					myLogger.debug("by adding a prefix to " + word
							+ ", know " + newWord + " is a [" + pos + "]");
				
				}
			}
			
			spWords = "("
					+ StringUtility.escape(this.singularPluralVariations(word,
							this.getSingularPluralHolder())) + ")";
			pattern = "^.*_" + spWords + "$";
			Iterator<Map.Entry<String, String>> iter3 = this.getUnknownWordHolder()
					.entrySet().iterator();
			while (iter3.hasNext()) {
				Map.Entry<String, String> entry = iter3.next();
				String newWord = entry.getKey();
				String flag = entry.getValue();
				// case 2.2: word_$spwords
				if ((newWord.matches(pattern)) && (flag.equals("unknown"))) {
					sign = sign
							+ processNewWord(newWord, pos, "*", table, word, 0);
					
					myLogger.debug("Case 2.2");
					myLogger.debug("by adding a prefix to " + word
								+ ", know " + newWord + " is a [" + pos + "]");
				}
			}
		}

		return sign;
	}
	
	/**
	 * This method handles a new word when the updateDataHolder method is called
	 * 
	 * @param newWord
	 * @param pos
	 * @param role
	 * @param table which table to update. "wordpos" or "modifiers"
	 * @param flag
	 * @param increment
	 * @return if a new word was added, returns 1; otherwise returns 0
	 */
	public int processNewWord(String newWord, String pos, String role,
			String table, String flag, int increment) {
				
		int sign = 0;
		// remove the new word from unknownword holder
		this.updateUnknownWord(newWord, flag);
		
		// insert the new word to the specified data holder
		if (table.equals("wordpos")) {
			sign = sign + updatePOS(newWord, pos, role, increment);
		} else if (table.equals("modifiers")) {
			sign = sign + this.addModifier(newWord, increment);
		}

		return sign;
	}
	
	/**
	 * update the pos of a word
	 * 
	 * @param newWord
	 * @param newPOS
	 * @param newRole
	 * @param increment
	 * @return
	 */
	public int updatePOS(String newWord, String newPOS, String newRole, int increment) {		
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.updatePOS");
		
		myLogger.trace("Enter updatePOS");
		myLogger.trace("Word: "+newWord+", POS: "+newPOS);
		
		
		int n = 0;
				
		String regex = "^.*(\\b|_)(NUM|" + Constant.NUMBER + "|"
				+ Constant.CLUSTERSTRING + "|" + Constant.CHARACTER + ")\\b.*$";
		//regex = "(NUM|" + "rows" + ")";
		boolean case1 = newWord.matches(regex);
		boolean case2 = newPOS.matches("[nsp]"); 
		if (case1 && case2) {
			myLogger.trace("Case 0");
			myLogger.trace("Quite updatePOS");
			return 0;
		}

//		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.getWordPOSHolder()
//				.entrySet().iterator();
//		// boolean isExist = false;
//		Map.Entry<WordPOSKey, WordPOSValue> targetWordPOS = null;
//		while (iter.hasNext()) {
//			Map.Entry<WordPOSKey, WordPOSValue> wordPOS = iter.next();
//			if (wordPOS.getKey().getWord().equals(newWord)) {
//				targetWordPOS = wordPOS;
//				break;
//			}
//		}
        
        List<Entry<WordPOSKey, WordPOSValue>> entryList = getWordPOSEntries(newWord);
        int certaintyU = 0;
		// case 1: the word does not exist, add it
        if (entryList.size()==0) {
		// if (targetWordPOS == null) {
			myLogger.trace("Case 1");
			certaintyU += increment;
			this.getWordPOSHolder().put(new WordPOSKey(newWord, newPOS),
					new WordPOSValue(newRole, certaintyU, 0, null, null));
			n = 1;
			myLogger.trace(String.format("\t: new [%s] pos=%s, role =%s, certaintyU=%d", newWord, newPOS, newRole, certaintyU));
		// case 2: the word already exists, update it
		} else {
			myLogger.trace("Case 2");
			Entry<WordPOSKey, WordPOSValue> targetWordPOS = entryList.get(0);
			String oldPOS = targetWordPOS.getKey().getPOS();
			String oldRole = targetWordPOS.getValue().getRole();
			certaintyU = targetWordPOS.getValue().getCertaintyU();
			// case 2.1 
			// 		the old POS is NOT same as the new POS, 
			// 	AND	the old POS is b or the new POS is b
			if ((!oldPOS.equals(newPOS))
					&& ((oldPOS.equals("b")) || (newPOS.equals("b")))) {
				myLogger.trace("Case 2.1");
				String otherPOS = newPOS.equals("b") ? oldPOS : newPOS;
				newPOS = this.resolveConflict(newWord, "b", otherPOS);

				boolean flag = false;
				if (newPOS != null) {
					if (!newPOS.equals(oldPOS)) {
						flag = true;
					}
				}

				// new pos win
				if (flag) { 
					newRole = newRole.equals("*") ? "" : newRole;
					n = n + changePOS(newWord, oldPOS, newPOS, newRole, increment);
				// old pos win
				} else { 
					newRole = oldRole.equals("*") ? newRole : oldRole;
					certaintyU = certaintyU + increment;
//					WordPOSKey key = new WordPOSKey("newWord", "pos");
//					WordPOSValue value = new WordPOSValue(newRole, certaintyU, 0,
//							null, null);
//					this.getWordPOSHolder().put(key, value);
					
					this.updateWordPOS(newWord, newPOS, newRole, certaintyU, 0, null, null);
					
					myLogger.debug(String.format("\t: update [%s (%s):a] role: %s=>%s, certaintyU=%d\n",
									newWord, newPOS, oldRole, newRole, certaintyU));
				}
				
			// case 2.2: the old POS and the new POS are all [n],  update role and certaintyU
			} else {
				myLogger.trace("Case 2.2");
				newRole = this.mergeRole(oldRole, newRole);
				certaintyU += increment;
//				WordPOSKey key = new WordPOSKey(newWord, newPOS);
//				WordPOSValue value = new WordPOSValue(newRole, certaintyU, 0,
//						null, null);
//				this.getWordPOSHolder().put(key, value);
				
				this.updateWordPOS(newWord, newPOS, newRole, certaintyU, 0, null, null);
				
				myLogger.debug(String.format("\t: update [%s (%s):b] role: %s => %s, certaintyU=%d\n",
								newWord, newPOS, oldRole, newRole, certaintyU));
			}
		}

		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter2 = this.getWordPOSHolder()
				.entrySet().iterator();
		int certaintyL = 0;
		while (iter2.hasNext()) {
			Map.Entry<WordPOSKey, WordPOSValue> e = iter2.next();
			if (e.getKey().getWord().equals(newWord)) {
				certaintyL += e.getValue().getCertaintyU();
			}
		}
		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter3 = this.getWordPOSHolder()
				.entrySet().iterator();
		while (iter3.hasNext()) {
			Map.Entry<WordPOSKey, WordPOSValue> e = iter3.next();
			if (e.getKey().getWord().equals(newWord)) {
				e.getValue().setCertiantyU(certaintyL);
			}
		}

		myLogger.debug(String.format("\t: total occurance of [%s] = %d\n", newWord, certaintyL));
		myLogger.trace("Return: " + n);
		return n;
	}
	
	/**
	 * This method corrects the pos of the word from N to M (establish newPOS)
	 * 
	 * @param newWord
	 * @param oldPOS
	 * @param newPOS
	 * @param newRole
	 * @param increment
	 * @return
	 */
	public int changePOS(String newWord, String oldPOS, String newPOS,
			String newRole, int increment) {		
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.changePOS");		
		myLogger.trace("Enter changePOS");
		myLogger.trace("newWord: "+newWord);
		myLogger.trace("oldPOS: "+oldPOS);
		myLogger.trace("newPOS: "+newPOS);
		myLogger.trace("newRole: "+newRole);
		
		oldPOS = oldPOS.toLowerCase();
		newPOS = newPOS.toLowerCase();

		String modifier = "";
		String tag = "";
		String sentence = null;
		int sign = 0;

		// case 1: oldPOS is "s" AND newPOS is "m"
		//if (oldPOS.matches("^.*s.*$") && newPOS.matches("^.*m.*$")) {
		if (oldPOS.equals("s") && newPOS.equals("m")) {
			myLogger.trace("Case 1");
			this.discountPOS(newWord, oldPOS, newPOS, "all");
			sign += markKnown(newWord, "m", "", "modifiers", increment);
			
			// For all the sentences tagged with $word (m), re tag by finding their parent tag.
			for (int i = 0; i < this.getSentenceHolder().size(); i++) {
				SentenceStructure sent = this.getSentenceHolder().get(i);
				if (sent.getTag().equals(newWord)) {
					int sentID = i;
					modifier = sent.getModifier();
					tag = sent.getTag();
					sentence = sent.getSentence();
					
					tag = this.getParentSentenceTag(sentID);
					modifier = modifier + " " + newWord;
					modifier.replaceAll("^\\s*", "");
					List<String> pair = this.getMTFromParentTag(tag);
					String m = pair.get(1);
					tag = pair.get(2);
					if (m.matches("^.*\\w.*$")) {
						modifier = modifier + " " + m;
					}
					this.tagSentenceWithMT(sentID, sentence, modifier, tag, "changePOS[n->m:parenttag]");
				}
			}
			
		} 
		// case 2: oldPOS is "s" AND newPOS is "b"
		else if ((oldPOS.matches("s")) && (newPOS.matches("b"))) {
			myLogger.trace("Case 2");
			int certaintyU = 0;

			// find (newWord, oldPOS)
			WordPOSKey newOldKey = new WordPOSKey(newWord, oldPOS);
			if (this.getWordPOSHolder().containsKey(newOldKey)) {
				WordPOSValue v = this.getWordPOSHolder().get(newOldKey);
				certaintyU = v.getCertaintyU();
				certaintyU += increment;
				this.discountPOS(newWord, oldPOS, newPOS, "all");
			}

			// find (newWord, newPOS)
			WordPOSKey newNewKey = new WordPOSKey(newWord, newPOS);
			if (!this.getWordPOSHolder().containsKey(newNewKey)) {
//				this.getWordPOSHolder().put(newNewKey, new WordPOSValue(newRole,
//						certaintyU, 0, "", ""));
				this.add2Holder(DataHolder.WORDPOS, 
						Arrays.asList(new String [] {newWord, newPOS, newRole, Integer.toString(certaintyU), "0", "", ""}));
			}
			
			myLogger.debug("\t: change ["+newWord+"("+oldPOS+" => "+newPOS+")] role=>"+newRole+"\n");
			sign++;

			// for all sentences tagged with (newWord, "b"), re tag them
			for (int i = 0; i < this.getSentenceHolder().size(); i++) {
				String thisTag = this.getSentenceHolder().get(i).getTag();
				int thisSentID = i;
				String thisSent = this.getSentenceHolder().get(i).getSentence();
				if (StringUtils.equals(thisTag, newWord)) {						
					this.tagSentenceWithMT(thisSentID, thisSent, "", "NULL", "changePOS[s->b: reset to NULL]");
				}
			}
		}
		// case 3: oldPOS is "b" AND newPOS is "s"
		else if (oldPOS.matches("b") && newPOS.matches("s")) {
			myLogger.trace("Case 3");
			int certaintyU = 0;

			// find (newWord, oldPOS)
			WordPOSKey newOldKey = new WordPOSKey(newWord, oldPOS);
			if (this.getWordPOSHolder().containsKey(newOldKey)) {
				WordPOSValue v = this.getWordPOSHolder().get(newOldKey);
				certaintyU = v.getCertaintyU();
				certaintyU += increment;
				this.discountPOS(newWord, oldPOS, newPOS, "all");
			}

			// find (newWord, newPOS)
			WordPOSKey newNewKey = new WordPOSKey(newWord, newPOS);
			if (!this.getWordPOSHolder().containsKey(newOldKey)) {
				this.getWordPOSHolder().put(newNewKey, new WordPOSValue(newRole,
						certaintyU, 0, "", ""));
			}
			
			myLogger.debug("\t: change ["+newWord+"("+oldPOS+" => "+newPOS+")] role=>"+newRole+"\n");
			sign++;
		}
		
		int sum_certaintyU = this.getSumCertaintyU(newWord);
		
		if (sum_certaintyU > 0) {
			Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter2 = this.getWordPOSHolder()
					.entrySet().iterator();
			while (iter2.hasNext()) {
				Map.Entry<WordPOSKey, WordPOSValue> e = iter2.next();
				if (e.getKey().getWord().equals(newWord)) {
					e.getValue().setCertiantyL(sum_certaintyU);
				}
			}
		}

		myLogger.trace("Return: "+sign);
		myLogger.trace("Quite changePOS\n");
		return sign;
	}
	
	/**
	 * 
	 * @param sentID
	 * @param sentence
	 * @param modifier
	 * @param tag tag could be "null"
	 * @param label
	 */
	public void tagSentenceWithMT(int sentID, String sentence, String modifier,
			String tag, String label) {
		/**
		 * 1. Do some preprocessing of modifier and tag 
		 *     1. Remove -ly words 
		 *     1. Update modifier and tag of sentence sentID in Sentence
		 */
		
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.tagSentenceWithWT");
		
		myLogger.trace("Enter tagSentenceWithMT");
		
		//modifier preprocessing
		modifier = this.tagSentWithMTPreProcessing(modifier);
		tag = this.tagSentWithMTPreProcessing(tag);
		
		//Remove any -ly ending word which is a "b" in the WordPOS, from the modifier
		modifier = this.tagSentWithMTRemoveLyEndingBoundary(modifier);

		modifier = StringUtility.removeAll(modifier, "(^\\s*|\\s*$)");
		tag = StringUtility.removeAll(tag, "(^\\s*|\\s*$)");

		if (tag == null) {
			this.getSentenceHolder().get(sentID).setTag(null);
			this.getSentenceHolder().get(sentID).setModifier(modifier);			
		}
		else {
			if (tag.length() > this.myConfiguratio.getMaxTagLength()) {
				tag = tag.substring(0, this.myConfiguratio.getMaxTagLength());
			}
			this.sentenceTable.get(sentID).setTag(tag);
			this.sentenceTable.get(sentID).setModifier(modifier);	
		}

		for (int i = 0; i < this.sentenceTable.size(); i++) {
			this.sentenceTable.get(sentID).setTag(tag);
			this.sentenceTable.get(sentID).setModifier(modifier);
		}

		myLogger.trace(label);
		myLogger.trace("Quite tagSentenceWithMT");
	}
	
	public String tagSentWithMTPreProcessing(String text) {		
		text = text.replaceAll("<\\S+?>", "");

		text = StringUtility.removeAllRecursive(text, "^(" + Constant.STOP
				+ "|" + Constant.FORBIDDEN+")\\b\\s*");

		// remove stop and forbidden words from ending
		text = StringUtility.removeAllRecursive(text, "\\s*\\b(" + Constant.STOP
				+ "|" + Constant.FORBIDDEN + "|\\w+ly)$");

		// remove all pronoun words
		text = StringUtility.removeAllRecursive(text, "\\b(" + Constant.PRONOUN
				+ ")\\b");
		
		return text;
	}
	
	public int getSumCertaintyU(String word) {
		int sumCertaintyU = 0;
		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.wordPOSTable.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<WordPOSKey, WordPOSValue> e = iter.next();
			if (e.getKey().getWord().equals(word)) {
				sumCertaintyU += e.getValue().getCertaintyU();
			}
		}
		
		return sumCertaintyU;
	}

	/**
	 * return singular and plural variations of the word
	 * 
	 * @param word
	 * @return all variations of the word
	 */
	public String singularPluralVariations(String word, Set<SingularPluralPair> singularPluralHolder) {
		String variations = word + "|";
		Iterator<SingularPluralPair> iter = singularPluralHolder.iterator();
		while (iter.hasNext()) {
			SingularPluralPair pair = iter.next();
			String sg = pair.getSingular();
			String pl = pair.getPlural();
			if (sg.equals(word) && (!pl.equals(""))) {
				variations = variations + pl + "|";
			}
			if (pl.equals(word) && (!sg.equals(""))) {
				variations = variations + sg + "|";
			}
		}

		variations = StringUtility.removeAll(variations, "\\|+$");

		return variations;
	}
	
	/**
	 * mark the words between the start index and the end index as modifiers if
	 * they are valid words.
	 * 
	 * @param start
	 *            the start index
	 * @param end
	 *            the end index
	 * @param words
	 *            a list of words
	 * @return number of updates made
	 */
	public int updateDataHolderNN(int start, int end, List<String> words) {
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.updateDataHolderNN");
		myLogger.trace(String.format("Enter (%d, %d, %s)", start, end,
				words.toString()));
				
		int update=0;
		List<String> splicedWords = StringUtility.stringArraySplice(words, start, end); 
		
		for (int i=0;i<splicedWords.size()-1;i++) {
			String word = splicedWords.get(i);

			myLogger.trace("Check N: " + word);

			if (this.updateDataHolderNNConditionHelper(word)) {
				myLogger.trace("Update N: " + word);
				int temp = this.updateDataHolder(word, "m", "", "modifiers", 1);
				update = update + temp;
				myLogger.trace("Return: " + temp);
			}
		}
		
		myLogger.trace("Return: " + update + "\n");
		return update;
	}
	
	/**
	 * A helper of method updateDataHolderNN. Check if the condition is meet.
	 * 
	 * @param word
	 *            the word to check
	 * @return a boolean variable
	 */
	public boolean updateDataHolderNNConditionHelper(String word) {
		boolean flag = false;
		
		flag = (   (!word.matches("^.*\\b("+Constant.STOP+")\\b.*$"))
				&& (!word.matches("^.*ly\\s*$"))
				&& (!word.matches("^.*\\b("+Constant.FORBIDDEN+")\\b.*$"))
				);
		
		return flag;
	}
	
	/**
	 * Return (POS, role, certaintyU, certaintyL) of a word
	 * 
	 * @param word
	 *            the word to check
	 * @return entries of (POS, role, certaintyU, certaintyL) of the word in a
	 *         list
	 */
	public List<POSInfo> checkPOSInfo(String word) {
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.checkPOSInfo");
		myLogger.trace("Enter ("+word+")");
		List<POSInfo> POSInfoList = new ArrayList<POSInfo>();

		word = StringUtility.removeAll(word, "^\\s*");
		word = StringUtility.removeAll(word, "\\s+$");

		if (word.matches("^\\d+.*$")) {
			POSInfo p = new POSInfo(word, "b", "", 1, 1);
			POSInfoList.add(p);
			myLogger.trace("Reture: "+POSInfoList);
			return POSInfoList;
		}

		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.wordPOSTable.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<WordPOSKey, WordPOSValue> e = iter.next();
			String w = e.getKey().getWord();
			if (w.equals(word)) {
				String POS = e.getKey().getPOS();
				String role = e.getValue().getRole();
				int certaintyU = e.getValue().getCertaintyU();
				int certaintyL = e.getValue().getCertaintyL();
				POSInfo p = new POSInfo(word, POS, role, certaintyU, certaintyL);
				POSInfoList.add(p);
			}
		}

		// nothing found
		if (POSInfoList.size() != 0) {
			// sort the list in ascending order of certaintyU/certaintyL
			Collections.sort(POSInfoList);
			// reverse it into descending order
			Collections.reverse(POSInfoList);
		}

		myLogger.trace("Reture: "+POSInfoList);
		return POSInfoList;

	}
	
	/******** Utilities *************/
	
	public void add2Holder(byte holderID, List<String> args){

		if (holderID == DataHolder.DISCOUNTED) {
			this.discountedTable = this.add2DiscountedHolder(this.discountedTable, args);
		}
		
		if (holderID == DataHolder.ISA) {
			this.isATable = this.add2IsAHolder(this.isATable, args);
		}
		
		if (holderID == DataHolder.MODIFIER) {
			this.modifierTable = this.add2ModifierHolder(this.modifierTable, args);
		}
		
		if (holderID == DataHolder.SENTENCE) {
			this.sentenceTable = this.add2SentenceHolder(this.sentenceTable,args);
		}
		
		if (holderID == DataHolder.SINGULAR_PLURAL) {
			this.singularPluralTable = this.add2SingularPluralHolder(this.singularPluralTable, args);
		}
		
		if (holderID == DataHolder.UNKNOWNWORD) {
			this.unknownWordTable = this.add2UnknowWordHolder(this.unknownWordTable, args);
		}
		
		if (holderID == DataHolder.WORDPOS) {
			this.wordPOSTable = this.add2WordPOSHolder(this.wordPOSTable, args);
		}
		
	}

	public Map<Integer, IsAValue> add2IsAHolder (Map<Integer, IsAValue> isAHolder, List<String> args) {
		int index = 0;
		
		String instance = args.get(index++);
		String cls = args.get(index++);
		
		isAHolder.put(isAHolder.size()+1, new IsAValue(instance, cls));
		
		return isAHolder;
	}
	
	public Map<String, ModifierTableValue> add2ModifierHolder(Map<String, ModifierTableValue> modifierTable, List<String> args) {
		int index = 0;
		
		String word = args.get(index++);
		int count  = new Integer(args.get(index++));
		boolean isTypeModifier = false;
		String isTypeModifierString = args.get(index++);
		if (StringUtils.equals(isTypeModifierString, "true")) {
			isTypeModifier = true;
		}

		
		modifierTable.put(word, new ModifierTableValue(count, isTypeModifier));
		
		return modifierTable;
	}

	public Map<String, String> add2UnknowWordHolder(Map<String, String> unknownWordHolder, List<String> args){
		int index = 0;
		
		String word = args.get(index++);
		String flag = args.get(index++);
		unknownWordHolder.put(word, flag);
		
		return unknownWordHolder;
	}
	
	public Map<WordPOSKey, WordPOSValue> add2WordPOSHolder(Map<WordPOSKey, WordPOSValue> wordPOSHolder, List<String> args){
		int index = 0;
		
		String word = args.get(index++);
		String POS = args.get(index++);
		String role = args.get(index++);
		int certaintyU = new Integer(args.get(index++));
		int certaintyL = new Integer(args.get(index++));
		String savedFlag = args.get(index++);
		String savedID = args.get(index++);
		wordPOSHolder.put(
				new WordPOSKey(word, POS), 
				new WordPOSValue(role, certaintyU, certaintyL, savedFlag, savedID));
		
		return wordPOSHolder; 
	}
	
	public Set<SingularPluralPair> add2SingularPluralHolder(Set<SingularPluralPair> singularPluralHolder, List<String> args){
		int index = 0;
		
		String singular = args.get(index++);
		String plural = args.get(index++);
		singularPluralHolder.add(new SingularPluralPair(singular, plural));
		
		return singularPluralHolder; 
	}
	
	public Map<DiscountedKey, String> add2DiscountedHolder(Map<DiscountedKey, String> discountedHolder, List<String> args){
		int index = 0;
		
		String word = args.get(index++);
		String POS = args.get(index++);
		String newPOS = args.get(index++);
		discountedHolder.put(new DiscountedKey(word, POS), newPOS);
		
		return discountedHolder; 
	}

	public List<SentenceStructure> add2SentenceHolder(List<SentenceStructure> sentenceTable,
			List<String> args) {
		int index = 0;
		
		String source=args.get(index++);
		String sentence=args.get(index++);
		String originalSentence=args.get(index++);
		String lead=args.get(index++);
		String status=args.get(index++);
		String tag=args.get(index++);
		String modifier=args.get(index++);
		String type=args.get(index++);
		
		this.addSentence(source, sentence, originalSentence, lead, status, tag, modifier, type);
		//sentenceTable.add(new Sentence(source, sentence, originalSentence, lead, status, tag, modifier, type));
		return sentenceTable;

	}
	
	// Holder Output
	public void printHolder(byte holderID) {
		if (holderID == DataHolder.SENTENCE) {
			printHolder(holderID, 0, this.sentenceTable.size()-1);
		}
		
		if (holderID == DataHolder.SINGULAR_PLURAL) {
			printHolder(holderID, 0, this.singularPluralTable.size()-1);
		}
		
		if (holderID == DataHolder.UNKNOWNWORD) {
			printHolder(holderID, 0, this.unknownWordTable.size()-1);
		}
		
		if (holderID == DataHolder.WORDPOS) {
			printHolder(holderID, 0, this.wordPOSTable.size()-1);
		}
		
	}
	
	public void printHolder(byte holderID, int startIndex, int endIndex){
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("dataholder.printHolder");
		
		if (holderID == DataHolder.SENTENCE) {
			for (int i = startIndex; i<=endIndex; i++) {
				SentenceStructure sentence = this.sentenceTable.get(i);
				myLogger.info("Index: "+i);
				myLogger.info(sentence.toString());
//				myLogger.info("Sentence ID: "+sentence.getID());
//				myLogger.info("Source: "+sentence.getSource());
//				myLogger.info("Sentence: "+sentence.getSentence());
//				myLogger.info("Original Sentence: "+sentence.getSentence());
//				myLogger.info("Lead: "+sentence.getLead());
//				myLogger.info("Status: "+sentence.getStatus());
//				myLogger.info("Tag: "+sentence.getTag());
//				myLogger.info("Modifier: "+sentence.getModifier());
//				myLogger.info("Type: "+sentence.getType());
//				myLogger.info("\n");
			}
		}
		
		if (holderID == DataHolder.SINGULAR_PLURAL) {
			myLogger.info("==SingularPlural Table==");
			
			Iterator<SingularPluralPair> iter = this.singularPluralTable.iterator();
			
			List<SingularPluralPair> singularPluralPairList = new LinkedList<SingularPluralPair>();
			singularPluralPairList.addAll(singularPluralTable);
			Collections.sort(singularPluralPairList);
			
			for (int i = 0; i<singularPluralPairList.size();i++) {
				if ((i >= startIndex) && (i <=endIndex)) {
					SingularPluralPair entry = singularPluralPairList.get(i);
					
					myLogger.info("Index: " + i);
					myLogger.info("Singular: " + entry.getSingular());
					myLogger.info("Plural: " + entry.getPlural());
					myLogger.info("\n");
				}
			}
			
//			int index = 0;
//			while (iter.hasNext()) {
//				if ((index >= startIndex) && (index <=endIndex)) {
//					SingularPluralPair entry = iter.next();
//					
//					myLogger.info("Index: " + index);
//					myLogger.info("Singular: " + entry.getSingular());
//					myLogger.info("Plural: " + entry.getPlural());
//					myLogger.info("\n");
//				}
//				index++;
//			}
		}
		
		if (holderID == DataHolder.UNKNOWNWORD) {
			int index = 0;
			Iterator<Entry<String, String>> iter = this.unknownWordTable.entrySet().iterator();
			
			while (iter.hasNext()) {
				if ((index >= startIndex) && (index <= endIndex)) {
					Entry<String, String> entry = iter.next();
					
					myLogger.info("Index: " + index);
					myLogger.info("Key: " + entry.getKey());
					myLogger.info("Value: " + entry.getValue());
					myLogger.info("\n");
				}
				index++;
			}
		}
		
		if (holderID == DataHolder.WORDPOS) {
			int index = 0;
			Iterator<Entry<WordPOSKey, WordPOSValue>> iter = this.wordPOSTable.entrySet().iterator();			
			while (iter.hasNext()) {				
				if ((index >= startIndex) && (index <= endIndex)) {
					Entry<WordPOSKey, WordPOSValue> entry = iter.next();
					
					myLogger.info(entry.toString());
					myLogger.info("\n");

				}
				index++;
			}
			
		}
		
		myLogger.info("Total: "+(endIndex-startIndex+1)+"\n");
	}

	/**
	 * Get a list of all tags which is not "ignore".
	 * 
	 * @return a set of tags
	 */
	public Set<String> getCurrentTags() {
		Set<String> tags = new HashSet<String>();
		
		for (int i=0;i<this.sentenceTable.size();i++) {
			SentenceStructure sentence = this.sentenceTable.get(i);
			String tag = sentence.getTag();
			if ((!StringUtils.equals(tag, "ignore"))){
				tags.add(tag);
			}
		}
		
		return tags;
	}


}
