package semanticMarkup.know.lib;

import java.util.HashSet;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.ling.transform.IInflector;

/**
 * GlossaryOrganStateKnowledgeBase creates an IOrganStateKnowledgeBase using an IGlossary
 * @author rodenhausen
 */
public class GlossaryOrganStateKnowledgeBase implements IOrganStateKnowledgeBase {

	protected Set<String> organs = new HashSet<String>();
	protected Set<String> states = new HashSet<String>();
	private IGlossary glossary;
	protected IInflector inflector;
	
	/**
	 * @param glossary
	 * @param inflector
	 */
	public GlossaryOrganStateKnowledgeBase(IGlossary glossary, IInflector inflector) {
		this.glossary = glossary;
		this.inflector = inflector;
		getOrgans();
		getStates();
	}
	

	@Override
	public boolean isOrgan(String word) {
		word = word.toLowerCase();
		word = inflector.getSingular(word);
		return organs.contains(word);
	}

	@Override
	public boolean isState(String word) {
		word = word.toLowerCase();
		return states.contains(word);
	}

	private void getOrgans() {
		String[] categories = { "STRUCTURE", "FEATURE", "SUBSTANCE", "PLANT",
				"nominative" };
		for (String category : categories) {
			Set<String> words = this.glossary.getWords(category);
			if(words != null) {
				for(String word : words) {
					this.organs.add(word);
				}
			}
		}
	}
	
	private void getStates() {
		Set<String> notCategories = new HashSet<String>();
		notCategories.add("STRUCTURE");
		notCategories.add("FEATURE");
		notCategories.add("SUBSTANCE");
		notCategories.add("PLANT");
		notCategories.add("nominative");
		notCategories.add("life_style");
		Set<String> terms = this.glossary.getWordsNotInCategories(notCategories);
		for (String term : terms) {
			term = term.indexOf(" ") > 0 ? term.substring(term.lastIndexOf(' ') + 1) : term;
			states.add(term);
		}
	}


	@Override
	public void addState(String word) {
		this.states.add(word);
	}


	@Override
	public void addOrgan(String word) {
		this.organs.add(word);
	}


	@Override
	public boolean contains(String word) {
		return this.isOrgan(word) || this.isState(word);
	}
}
