package edu.arizona.sirls.semanticMarkup.know.lib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import com.google.inject.Inject;

import edu.arizona.sirls.semanticMarkup.know.ICharacterKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.know.IGlossary;
import edu.arizona.sirls.semanticMarkup.log.LogLevel;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;

/**
 * LearnedCharacterKnowledgeBase poses an ICharacterKnowledgeBase by making use of learned terminology by an ITerminologyLearner
 * and an IGlossary
 * @author rodenhausen
 */
public class LearnedCharacterKnowledgeBase implements ICharacterKnowledgeBase {
	
	private IGlossary glossary;
	private String or = "_or_";
	private ITerminologyLearner terminologyLearner;
	private ConcurrentHashMap<String, String> addedCharacters = new ConcurrentHashMap<String, String>();

	/**
	 * @param terminologyLearner
	 * @param glossary
	 */
	@Inject
	public LearnedCharacterKnowledgeBase(ITerminologyLearner terminologyLearner, IGlossary glossary) {
		this.terminologyLearner = terminologyLearner;
		this.glossary = glossary;
	}
	
	@Override
	public String getCharacterName(String word) {
		if (word.trim().length() == 0)
			return null;
		if(this.addedCharacters.containsKey(word))
			return addedCharacters.get(word);
		if (word.indexOf(" ") > 0)
			word = word.substring(word.lastIndexOf(" ") + 1).trim();
		word = word.replaceAll("[{}<>()]", "").replaceAll("\\d+[�-]", "_")
				.replaceAll("�", "-")
				./* replaceAll(" ", ""). */replaceAll("_+", "_");
		// "(3-)5-merous"/ =>_merous
		word = word.replaceFirst(".*?_(?=[a-z]+$)", ""); 
		// _or_ribbed
		String wc = word;
	
		String ch = "";
		if (word.endsWith("shaped")) {
			//return "shape";
			word = word.replaceFirst("shaped", "-shaped");
		}
		String[] ws = { word };
		if (word.indexOf('-') > 0) {
			ws = word.split("-+");
			word = ws[ws.length - 1];
		}
		ch = lookup(word);
		if (ch == null && wc.indexOf('-') > 0) {// pani_culiform
			ch = lookup(wc.replaceAll("-", ""));
		}
		if(ch == null && wc.indexOf('-') > 0) {
			ch = lookup(ws[0]);
		}
		return ch;
	}
	
	
	private String lookup(String word) {
		StringBuilder categoriesStringBuilder = new StringBuilder();

		// check glossarytable
		if(this.glossary.contains(word)) {
			Set<String> glossaryCategories = this.glossary.getCategories(word);
			glossaryCategories.addAll(this.glossary.getCategories("_" + word));
			
			// check _term_category table
			Set<String> categories = new HashSet<String>();

			Map<String, Set<String>> termCategories = terminologyLearner.getTermCategories();
			//log(LogLevel.DEBUG, "termcategories " + termCategories);
			if(termCategories.containsKey(word))
				categories.addAll(termCategories.get(word));
			//Set<String> categories = new HashSet<String>(termCategories.get(w));
			categories.remove("structure");
			
			categories.addAll(glossaryCategories);
			
			String[] categoriesArray = categories.toArray(new String[] {});
			Arrays.sort(categoriesArray);
	
			for (String category : categoriesArray) {
				categoriesStringBuilder.append(category.replaceAll("\\s+", "_") + this.or);
			}
			String categoriesString = categoriesStringBuilder.toString();
			if (categoriesString.length() > 0) {
				categoriesString = categoriesString.replaceFirst(this.or + "$", "");
				if(word.equals("pale"))
					log(LogLevel.DEBUG, "categoriesString " + categoriesString);
				return categoriesString;
			}
		}

		return null;
	}

	@Override
	public boolean containsCharacterState(String word) {
		return this.getCharacterName(word)!=null;
	}

	@Override
	public void addCharacterStateToName(String word, String character) {
		this.addedCharacters.put(word, character);
	}

	@Override
	public boolean containsCharacterName(String characterName) {
		Set<String> glossaryCategories = glossary.getWords("character");
		Set<String> learnedCategories = terminologyLearner.getCategoryTerms().get("character");
		if(learnedCategories == null)
			learnedCategories = new HashSet<String>();
		return glossaryCategories.contains(characterName) || learnedCategories.contains(characterName);
	}

}
