package edu.arizona.biosemantics.semanticmarkup.ling.transform.lib;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;

/**
 * SomeInflector uses word endings and IPOSKnowledgeBase to determine inflections
 * @author rodenhausen
 */
public class SomeInflector implements IInflector {

	private HashMap<String, String> singulars = new HashMap<String, String>();
	private HashMap<String, String> plurals = new HashMap<String, String>();
	private Pattern lyAdverbPattern = Pattern.compile("[a-z]{3,}ly");
	private Pattern p1 = Pattern.compile("(.*?[^aeiou])ies$");
	private Pattern p2 = Pattern.compile("(.*?)i$");
	private Pattern p3 = Pattern.compile("(.*?)ia$");
	private Pattern p4 = Pattern.compile("(.*?(x|ch|sh|ss))es$");
	private Pattern p5 = Pattern.compile("(.*?)ves$");
	private Pattern p6 = Pattern.compile("(.*?)ices$");
	private Pattern p7 = Pattern.compile("(.*?a)e$");
	private Pattern p75 = Pattern.compile("(.*?)us$");
	private Pattern p8 = Pattern.compile("(.*?)s$");
	private Pattern p9 = Pattern.compile("(.*?)a$");
	private Pattern p10 = Pattern.compile("(.*?ma)ta$"); //stigmata => stigma (20 cases)
	private Pattern p11 = Pattern.compile("(.*?)des$"); //crepides => crepis (4 cases)
	private Pattern p12 = Pattern.compile("(.*?)es$"); // (14 cases)
	private IPOSKnowledgeBase posKnowledgeBase;

	/**
	 * @param posKnowledgeBase
	 */
	@Inject
	public SomeInflector(IPOSKnowledgeBase posKnowledgeBase, @Named("Singulars") HashMap<String, String> singulars, @Named("Plurals") HashMap<String, String> plurals) { //Hong TODO load in singular-plural mapping from those learned from perl step
		this.posKnowledgeBase = posKnowledgeBase;
		this.singulars = singulars;
		this.plurals = plurals;
	}
	
	@Override
	public String getSingular(String word) {
		if(!word.matches(".*?[a-zA-Z].*")) //not a word, e.g 3-5
			return word;
		
		String s = "";
		//word = word.toLowerCase().replaceAll("\\W", "").trim();
		word = word.toLowerCase();
		String originalWord = word;
		String[] parts = word.split("-"); //may be a good or bad hyphen. find singular for the last part if it is a word
		String prefix = "";
		/*if(parts[parts.length-1] not in WordNet, glossary, or ontology){ //Hong TODO
			word = originalWord.replace("-", "");
			prefix = "";
		}else{*/
		   word = parts[parts.length-1];
		   prefix = originalWord.replaceFirst(word+"$", "");
        //}
        

		// check cache
		s = singulars.get(word);
		if (s != null)
			return prefix+s;

		if(word.matches("\\w+_[ivx-]+")){
			singulars.put(word, word);
			plurals.put(word, word);
			return prefix+word;
		}

		if(word.matches("[ivx-]+")){
			singulars.put(word, word);
			plurals.put(word, word);
			return prefix+word;
		}
		
		// adverbs
		Matcher matcher = lyAdverbPattern.matcher(word);
		if (matcher.matches()) {
			singulars.put(word, word);
			plurals.put(word, word);
			return prefix+word;
		}

		String wordcopy = new String(word);
		wordcopy = checkWN4Singular(wordcopy);
		if (wordcopy != null && wordcopy.length() == 0) {
			return prefix+word;
		} else if (wordcopy != null) {
			singulars.put(word, wordcopy);
			if (!wordcopy.equals(word))
				plurals.put(wordcopy, word);
			return prefix+wordcopy;
		} else {// word not in wn
			Matcher m1 = p1.matcher(word);
			Matcher m2 = p2.matcher(word);
			Matcher m3 = p3.matcher(word);
			Matcher m4 = p4.matcher(word);
			Matcher m5 = p5.matcher(word);
			Matcher m6 = p6.matcher(word);
			Matcher m7 = p7.matcher(word);
			Matcher m75 = p75.matcher(word);
			Matcher m8 = p8.matcher(word);
			Matcher m9 = p9.matcher(word);
			Matcher m10 = p10.matcher(word);
			Matcher m11 = p11.matcher(word);
			Matcher m12 = p12.matcher(word);

			if (m1.matches()) {
				s = m1.group(1) + "y";
			} else if (m2.matches()) {
				s = m2.group(1) + "us";
			} else if (m3.matches()) {
				s = m3.group(1) + "ium";
			} else if (m4.matches()) {
				s = m4.group(1);
			} else if (m5.matches()) {
				s = m5.group(1) + "f";
			} else if (m6.matches()) {
				s = m6.group(1) + "ex";
				//if(!inGlossaryOntology(s)) s = m6.group(1)+"ix"; //Hong TODO use glossary and ontology for getSingular 
				//if(!inGlossaryOntology(s, conn)) s = null;
			} else if (m7.matches()) {
				s = m7.group(1);
			} else if (m75.matches()) {
				s = word;
			} else if (m8.matches()) {
				s = m8.group(1);
			}else if(m9.matches()){
				s = m9.group(1)+"um";
				//if(!inGlossaryOntology(s, conn)) s = m9.group(1)+"on";
				//if(!inGlossaryOntology(s, conn)) s = null;
			}else if(m10.matches()){
				s = m10.group(1);
			}else if(m11.matches()){
				s = m11.group(1)+"s";
				//if(!inGlossaryOntology(s, conn)) s = null;
			}
			
			if(s==null & m12.matches()){
				s = m12.group(1)+"is";
				//if(!inGlossaryOntology(s, conn)) s = null;
			}

			if (s != null) {
				singulars.put(word, s);
				if (!s.equals(word))
					plurals.put(s, word);
				return prefix+s;
			}
		}
		return prefix+word;
	}
	
	public String checkWN4Singular(String word) {
		word = word.trim().toLowerCase();
		
		List<String> singulars = posKnowledgeBase.getSingulars(word);
		for(String singular : singulars)
			return singular;
		return null;
	}

	@Override
	public String getPlural(String word) {
		word = word.trim().toLowerCase();

		if(plurals.containsKey(word))
			return plurals.get(word);
		if(word.endsWith("s"))
			return word + "es";
		else
			return word + "s";
	}

	@Override
	public boolean isPlural(String word) {
		word = word.trim().toLowerCase();
		word = word.replaceAll("\\W", "");
		if(word.matches("series|species|fruit")){
			return true;
		}
		if(!word.equals(getSingular(word))) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isSingular(String word) {
		return false;
	}

}
