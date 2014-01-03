package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class StringUtility {

	public StringUtility() {
		// TODO Auto-generated constructor stub
	}

	public static String strip(String text) {				
		text=text.replaceAll("<(([^ >]|\n)*)>", " ");
		text=text.replaceAll("<\\?[^>]*\\?>", " "); //<? ... ?>
		text=text.replaceAll("&[^ ]{2,5};", " "); //remove &nbsp;
		text=text.replaceAll("\\s+", " ");
		
		return text;
	}
	
	/**
	 * 
	 * @param text
	 *            : string in which all punctuations to remove
	 * @param c
	 *            : a punctuatin to keep
	 * @return: string after puctuations are removed except the one in c
	 */

	public static String removePunctuation(String text, String c) {
		//System.out.println("Old: " + text);
		if (c == null) {
			text = text.replaceAll("[\\p{Punct}]", "");
		} else {
			text = text.replaceAll(c, "aaa");
			text = text.replaceAll("[\\p{Punct}]", "");
			text = text.replaceAll("aaa", c);
		}
		//System.out.println("New: " + text);

		return text;
	}
	
	public static String trimString (String text){
		String myText = text;
		myText = myText.replaceAll("^\\s+|\\s+$", "");
		return myText;
	}
	
	/**
	 * Helper of method updateTable: process word
	 * 
	 * @param w
	 * @return
	 */

	public static String processWord(String word) {
		//$word =~ s#<\S+?>##g; #remove tag from the word
		//$word =~ s#\s+$##;
		//$word =~ s#^\s*##;
		
		word = word.replaceAll("<\\S+?>", "");
		word = word.replaceAll("\\s+$", "");
		word = word.replaceAll("^\\s*", "");
		
		return word;
	}
	
	public static String removeAll(String word, String regex) {
		String newWord = word.replaceAll(regex, ""); 
		return newWord;
	}
	
	public static String removeAllRecursive(String text, String regex) {
		String newText = text.replaceAll(regex, "");
				
		while (!newText.equals(text)) {
			text = newText;
			newText = text.replaceAll(regex, "");
		}
		
		return newText;
	}
	
	// if($t !~ /\b(?:$STOP)\b/ && $t =~/\w/ && $t !~ /\d/ && length $t > 1){
	public static boolean isWord(String token) {
		String regex = "\\b(" + Constant.STOP + ")\\b";
		if (token.matches(regex)) {
			return false;
		}

		if (!token.matches("\\w+")) {
			return false;
		}

		if (token.length() <= 1) {
			return false;
		}

		return true;
	}
	
	/**
	 * in perl, it escape [] {} and () for mysql regexp, not perl regrexp. May
	 * not be necessary in Java
	 * 
	 * @param singularPluralVariations
	 * @return
	 */
	public static String escape(String singularPluralVariations) {
		// TODO Auto-generated method stub
		return singularPluralVariations;
	}

	/**
	 * check if a word is a word in the wordList
	 * 
	 * @param word
	 *            the word to check
	 * 
	 * @param wordList
	 *            the words to match to
	 * @return a boolean variable. true mean word is a word in the list. false
	 *         means it is not
	 */
	public static boolean isMatchedWords(String word, String wordList){
		return word.matches("^.*\\b(?:"+wordList+")\\b.*$");
	}

	/**
	 * Given a list of words in one string in the form of
	 * "(word1|word2|...|wordn)", remove the word from the list if it is in the
	 * list.
	 * 
	 * @param word
	 *            the word to remove
	 * @param wordList
	 *            the list to remove the word from
	 * @return the list after remove the word
	 */
	public static String removeFromWordList(String word, String wordList) {
		String newWordList = wordList;
		
		newWordList = newWordList.replaceAll("\\b" + word + "\\b", "");
		newWordList = newWordList.replaceAll("^\\|", "");
		newWordList = newWordList.replaceAll("\\|\\|", "|");
		newWordList = newWordList.replaceAll("\\|$", "");
		
		return newWordList;
	}
	
//	public static boolean equalsWithNull(String s1, String s2) {
////		boolean flag = false;
////		flag = (s1==null)? (s2==null) : s1.equals(s2);
////		
////		return flag;
//		return StringUtils.equals(s1, s2);
//	}
	
	/**
	 * Convert a string array of to a string of words separated by space
	 * 
	 * @param words
	 * @return the string
	 */
	public static String stringArray2String(String [] words) {
		String wordsString = "";
		
		for (int i=0;i<words.length;i++) {
			wordsString = wordsString + words[i] + " ";
		}
		
		wordsString = wordsString.substring(0, wordsString.length()-1); 
		
		return wordsString;
	}
	
//	public static List<String>
	
	//Arrays.asList
	
	/**
	 * Get a splice of the string list between the index of the start
	 * (inclusive) and the end (exclusive)from the string list
	 * 
	 * @param words
	 *            the string list
	 * @param start
	 *            the start index of the section
	 * @param end
	 *            the end index of the section
	 * @return the splice
	 */
	public static List<String> stringArraySplice(List<String> words, int start, int end) {
		List<String> splicedWords = new ArrayList<String>();
		splicedWords.addAll(words.subList(start, end));
		
		return splicedWords;
	}
	
	/**
	 * Join a list of string together
	 * 
	 * @param separater
	 * @param list
	 * @return the string
	 */
	public static String joinList(String separater, List<String> list){
		String result = "";
		
		for (int i=0;i<list.size();i++) {
			result = result + list.get(i)+separater;
		}
		
		if (!result.equals("")) {
			result = result.substring(0, result.length()-separater.length());
		}
		
		return result;
	}
	
	/**
	 * Given a regex and an input, returns a matcher to match the regex to the
	 * input
	 * 
	 * @param regex
	 *            the regular expression
	 * @param input
	 *            the input char sequence
	 * @return the matcher
	 */
	public static Matcher createMatcher(String regex, CharSequence input) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		return m;
	}

	/**
	 * Null-safe method to match a entire text against the pattern
	 * 
	 * @param pattern
	 *            the pattern to match against
	 * @param text
	 *            the text to match
	 * @return true if matches, false otherwise
	 */
	public static boolean isEntireMatched(String pattern, String text) {
		if (pattern == null || text == null) {
			return false;
		}
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		if (m.matches()) {
			return true;
		}
		else {
			return false;
		}

	}
	
}
