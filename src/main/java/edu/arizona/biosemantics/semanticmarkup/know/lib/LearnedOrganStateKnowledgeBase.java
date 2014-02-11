package edu.arizona.biosemantics.semanticmarkup.know.lib;

import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * LearnedOrganStateKnowledgeBase creates an IOrganStateKnowledgeBase relying on learned terminology
 * @author rodenhausen
 */
public class LearnedOrganStateKnowledgeBase extends GlossaryOrganStateKnowledgeBase implements IOrganStateKnowledgeBase {

	private ITerminologyLearner terminologyLearner;
	private Set<String> stopWords;
	private Set<String> prepositionWords;
	private boolean learned = false;

	/**
	 * @param glossary
	 * @param inflector
	 * @param terminologyLearner
	 * @param stopWords
	 * @param prepositionWords
	 */
	@Inject
	public LearnedOrganStateKnowledgeBase(IGlossary glossary, IInflector inflector, ITerminologyLearner terminologyLearner, 
			@Named("StopWords") Set<String> stopWords, @Named("PrepositionWordsSet") Set<String> prepositionWords) {
		super(glossary, inflector);
		this.terminologyLearner = terminologyLearner;
		this.stopWords = stopWords;
		this.prepositionWords = prepositionWords;
	}
	
	@Override
	public boolean isOrgan(String word) {	
		if(word.matches("\\W+")) return false;
		if(!learned)
			addLearnedOrgansAndStates();
		
		return super.isOrgan(word);
	}

	@Override
	public boolean isState(String word) {
		if(word.matches("\\W+")) return false;
		word = word.toLowerCase();
		
		if(!learned)
			addLearnedOrgansAndStates();
		boolean result = super.isState(word); //populated states from perm. glossary
		//boolean result = states.contains(word);
		if(!result && word.contains("-")) {
			String[] parts = word.split("-");
			result = states.contains(parts[parts.length-1]);
			//if(parts.length == 2 && parts[0].matches("\\d+"))
			//	result = states.contains(parts[1]);
			
			if(!result)
				for(String state : states) 
					if(word.endsWith(state)) {
						result = true;
						break;
					}
		}
		return result;
	}
	
	private void addLearnedOrgansAndStates() {
		addOrgans();
		addStates();
		learned = true;
	}
	
	private void addOrgans() {
		this.organs.addAll(terminologyLearner.getBracketTags());
		this.organs.addAll(terminologyLearner.getWordRoleTags());
		this.organs.add("array");
		this.organs.add("arrays");//TODO Hong 
	}
	
	private void addStates() {
		if(terminologyLearner.getRoleToWords().containsKey("c")) {
			Set<String> words = terminologyLearner.getRoleToWords().get("c");
			for (String word : words) {
				if (!word.matches("\\W+")
						&& !stopWords.contains(word)
						&& !prepositionWords.contains(word)) {
					this.states.add(word);
				}
			}
		}
	}
}
