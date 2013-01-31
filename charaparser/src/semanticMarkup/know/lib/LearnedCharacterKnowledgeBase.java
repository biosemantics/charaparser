package semanticMarkup.know.lib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.learn.ITerminologyLearner;

import com.google.inject.Inject;

public class LearnedCharacterKnowledgeBase implements ICharacterKnowledgeBase {
	
	private IGlossary glossary;
	private String or = "_or_";
	private ITerminologyLearner terminologyLearner;
	private HashMap<String, String> addedCharacters = new HashMap<String, String>();

	@Inject
	public LearnedCharacterKnowledgeBase(ITerminologyLearner terminologyLearner, IGlossary glossary) {
		this.terminologyLearner = terminologyLearner;
		this.glossary = glossary;
	}
	
	@Override
	public String getCharacter(String word) {
		if (word.trim().length() == 0)
			return null;
		if(this.addedCharacters.containsKey(word))
			return addedCharacters.get(word);
		if (word.indexOf(" ") > 0)
			word = word.substring(word.lastIndexOf(" ") + 1).trim();
		word = word.replaceAll("[{}<>()]", "").replaceAll("\\d+[–-]", "_")
				.replaceAll("–", "-")
				./* replaceAll(" ", ""). */replaceAll("_+", "_");
		// "(3-)5-merous"/ =>_merous
		word = word.replaceFirst(".*?_(?=[a-z]+$)", ""); 
		// _or_ribbed
		String wc = word;
	
		String ch = "";
		if (word.endsWith("shaped")) {
			return "shape";
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
	
	
	public String lookup(String word) {
		StringBuilder categoriesStringBuilder = new StringBuilder();

		// check glossarytable
		if(this.glossary.contains(word)) {
			Set<String> glossaryCategories = this.glossary.getCategories(word);
			glossaryCategories.addAll(this.glossary.getCategories("_" + word));
		
	
			// check _term_category table
			Set<String> categories = new HashSet<String>();

			Map<String, Set<String>> termCategories = terminologyLearner.getTermCategories();
			//System.out.println("termcategories " + termCategories);
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
					System.out.println("categoriesString " + categoriesString);
				return categoriesString;
			}
		}

		return null;
	}

	@Override
	public boolean contains(String word) {
		return this.getCharacter(word)!=null;
	}

	@Override
	public void addCharacter(String word, String character) {
		this.addedCharacters.put(word, character);
	}

}
