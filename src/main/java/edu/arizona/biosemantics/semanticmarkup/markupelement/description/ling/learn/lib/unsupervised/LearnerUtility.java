package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.unsupervised;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.arizona.biosemantics.semanticmarkup.ling.transform.ITokenizer;


public class LearnerUtility {

	private Configuration myConfiguration;
	private ITokenizer mytokenizer;
	
	public LearnerUtility(Configuration configuration, ITokenizer tokenizer) {
		this.myConfiguration = configuration;
		this.mytokenizer = tokenizer;
	}
	
	public ITokenizer getTokenizer(){
		return this.mytokenizer;
	}
	
	public List<String> tokenizeText(String sentence, String mode) {
		if (StringUtils.equals(mode, "firstseg")) {
			sentence = getSentenceHead(sentence);
		}
		else {
			;
		}
		
		String[] tempWords = sentence.split("\\s+");
		List<String> words = new ArrayList<String>();
		words.addAll(Arrays.asList(tempWords));
		
		return words;
	}
	
	/**
	 * Get the portion in the input sentence before any of ,:;.[(, or any
	 * preposition word, if any
	 * 
	 * @param sentence
	 *            the input sentence
	 * @return the portion in the head
	 */
	public String getSentenceHead(String sentence) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger
				.getLogger("learn.populateSentence.getFirstNWords.getHead");

		if (sentence == null) {
			return sentence;
		}
		else if (sentence.equals("")) {
			return sentence;
		} 
		else {
			String head = "";
			int end = sentence.length();

			String pattern1 = " [,:;.\\[(]";
			String pattern2 = "\\b" + "(" + Constant.PREPOSITION + ")" + "\\s";

			myLogger.trace("Pattern1: " + pattern1);
			myLogger.trace("Pattern2: " + pattern2);

			Pattern p1 = Pattern.compile(pattern1);
			Pattern p2 = Pattern.compile(pattern2);

			Matcher m1 = p1.matcher(sentence);
			Matcher m2 = p2.matcher(sentence);

			boolean case1 = m1.find();
			boolean case2 = m2.find();
			
			if (case1 || case2) {
				// case 1
				if (case1) {
					int temp1 = m1.end();
					end = temp1 < end ? temp1 : end;
					end = end - 1;
				}
				// case 2
				else {
					int temp2 = m2.end();
					end = temp2 < end ? temp2 : end;
				}

				head = sentence.substring(0, end - 1);
			}
			else {
				head = sentence;
			}

			myLogger.trace("Return: " + head);
			return head;
		}
	}


}
