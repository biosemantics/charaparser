package edu.arizona.biosemantics.semanticmarkup.ling.know.lib;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.pos.POS;

/**
 * LearnedPOSKnowledgeBase poses an IPOSKnowledgeBase by relying on learned part of speech and using a fall back knowledgebase
 * such as WordNetPOSKnowledgeBase
 * @author rodenhausen
 */
public class LearnedPOSKnowledgeBase implements IPOSKnowledgeBase {

	private IGlossary glossary;
	private IPOSKnowledgeBase fallbackKnowledgeBase;
	private Set<String> notNouns = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private Set<String> nouns = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private Set<String> verbs = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private Set<String> notVerbs = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private Set<String> adverbs = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private Set<String> notAdverbs = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private Set<String> adjectives = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private Set<String> notAdjectives = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	
	private Set<String> stopWords;

	/**
	 * @param glossary
	 * @param fallbackKnowledgeBase
	 * @param stopWords
	 */
	@Inject
	public LearnedPOSKnowledgeBase(IGlossary glossary, IPOSKnowledgeBase fallbackKnowledgeBase,
			@Named("StopWords") Set<String> stopWords) {
		this.glossary = glossary;
		this.fallbackKnowledgeBase = fallbackKnowledgeBase;
		this.stopWords = stopWords;
	}
	
	@Override
	public boolean isNoun(String word) {
		word = word.trim();
		if(word.indexOf(' ') > 0)
			return false;
		word = word.replaceAll("[<>{}\\]\\[]", "");

		if(nouns.contains(word)){
			return true;
		}
		if(notNouns.contains(word)){
			return false;
		}
		if(!word.matches(".*?[a-z]+.*")){
			notNouns.add(word);
			return false;
		}
		if(stopWords.contains(word)){
			notNouns.add(word);
			return false;
		}

		POS pos = fallbackKnowledgeBase.getMostLikleyPOS(word);
		if(pos != null)
			if(pos.equals(POS.NN)) {
				nouns.add(word);
				return true;
			}
		notNouns.add(word);
		return false;
	}

	@Override
	public boolean isAdjective(String word) {
		return fallbackKnowledgeBase.isAdjective(word);
	}

	@Override
	public boolean isAdverb(String word) {
		word = word.replaceAll("[<>{}\\]\\[()\\d+-]", "").trim();
		
		if(adverbs.contains(word)){
			return true;
		}
		if(notAdverbs.contains(word)){
			return false;
		}
		
		if(word.matches("(not|at-?least|throughout|much)")){
			return true;
		}
		if(word.matches("in.*?(profile|view)")){//covers in-dorsal-view, in-profile
			return true;
		}
		/*mohan code to make as-long-as an adverb*/
		/*if(word.matches("aslongas")){//covers as-long-as
			return true;
		}*/
		/*End mohan code*/
		if(word.compareTo("moreorless")==0){
			return true;
		}
		if(word.compareTo("becoming")==0){
			return true;
		}
		if(word.compareTo("±")==0){
			return true;
		}
		if(!word.matches(".*?[a-z]+.*")){
			notAdverbs.add(word);
			return false;
		}
		if(stopWords.contains(word)){
			notAdverbs.add(word);
			return false;
		}
		
		POS pos = fallbackKnowledgeBase.getMostLikleyPOS(word);
		if(pos != null){
			if(pos.equals(POS.RB)){
				adverbs.add(word);
				return true;
			}
		}else{
			if(word.endsWith("ly")){
				adverbs.add(word);
				return true;
			}
		}
		notAdverbs.add(word);
		return false;
	}

	@Override
	public boolean isVerb(String word) {
		word = word.replaceAll("[<>{}\\]\\[]", "").trim();
		
		if(verbs.contains(word)){
			return true;
		}
		if(notVerbs.contains(word)){
			return false;
		}
		
		if(!word.matches(".*?[a-z]+.*")){
			notVerbs.add(word);
			return false;
		}
		if(stopWords.contains(word)){
			notVerbs.add(word);
			return false;
		}

		POS pos = fallbackKnowledgeBase.getMostLikleyPOS(word);
		if(pos != null) {
			if(pos.equals(POS.VB)) {
				verbs.add(word);
				return true;
			} else {
				if(fallbackKnowledgeBase.isVerb(word) && word.endsWith("ed")){
					verbs.add(word);
					return true;
				}
			}
		}
		notVerbs.add(word);
		return false;
	}

	@Override
	public POS getMostLikleyPOS(String word) {
		return fallbackKnowledgeBase.getMostLikleyPOS(word);
	}

	@Override
	public boolean contains(String word) {
		return fallbackKnowledgeBase.contains(word);
	}

	@Override
	public List<String> getSingulars(String word) {
		return fallbackKnowledgeBase.getSingulars(word);
	}

	@Override
	public void addVerb(String word) {
		this.verbs.add(word);
	}

	@Override
	public void addNoun(String word) {
		this.nouns.add(word);
	}

	@Override
	public void addAdjective(String word) {
		this.adjectives.add(word);
		
	}

	@Override
	public void addAdverb(String word) {
		this.adverbs.add(word);
		
	}

}
