package semanticMarkup.know.lib;

import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

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
		if(!learned)
			addLearnedOrgansAndStates();
		
		return super.isOrgan(word);
	}

	@Override
	public boolean isState(String word) {
		word = word.toLowerCase();
		
		if(!learned)
			addLearnedOrgansAndStates();
		
		boolean result = states.contains(word);
		if(!result && word.contains("-")) {
			String[] parts = word.split("-");
			if(parts.length == 2 && parts[0].matches("\\d+"))
				result = states.contains(parts[1]);
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
		this.organs.add("arrays");
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
