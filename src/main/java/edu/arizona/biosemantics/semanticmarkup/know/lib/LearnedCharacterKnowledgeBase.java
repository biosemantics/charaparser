package edu.arizona.biosemantics.semanticmarkup.know.lib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;



import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * LearnedCharacterKnowledgeBase poses an ICharacterKnowledgeBase by making use of learned terminology by an ITerminologyLearner
 * and an IGlossary
 * @author rodenhausen
 */
public class LearnedCharacterKnowledgeBase implements ICharacterKnowledgeBase {
	
	private IGlossary glossary;
	private String or = "_or_";
	private String c = "_c_";
	
	private String negWords; 
	//private ITerminologyLearner terminologyLearner;
	//private ConcurrentHashMap<String, String> addedCharacters = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, Match> addedCharacters = new ConcurrentHashMap<String, Match>();
	private ConcurrentHashMap<String, Match> characterCache = new ConcurrentHashMap<String, Match> ();

	/**
	 * @param terminologyLearner
	 * @param glossary
	 */
	@Inject
	public LearnedCharacterKnowledgeBase(/*ITerminologyLearner terminologyLearner,*/ IGlossary glossary, 
			@Named("NegationWords")String negWords) {
		//this.terminologyLearner = terminologyLearner;
		this.glossary = glossary;
		this.negWords = negWords;
	}
	
	@Override
	public boolean isOrgan(String word){
		String cats = this.getCharacterName(word).getCategories();
		return cats !=null && cats.matches(".*?(^|_)structure(_|$).*");
	}
	
	@Override
	public boolean isState(String word){
		String cats = this.getCharacterName(word).getCategories();
		return cats!=null && !cats.matches(".*?(^|_)structure(_|$).*");
	}

	@Override
	public Match getCharacterName(String word) {//hyphened or underscored word? 
		String wo = word;

		if(word.matches("("+negWords+")")) return new Match(null);
		
		if (word.trim().length() == 0 || word.matches("\\W+"))
			return new Match(null);
		if(this.addedCharacters.containsKey(word)) //"brownish red" from "brownish_c_red", terms created by the program
			return addedCharacters.get(word);
		
		if (word.indexOf(" ") > 0)
			word = word.substring(word.lastIndexOf(" ") + 1).trim();
		word = word.replaceAll("\\d+[–-]", "_") //TODO check: use _ vs -
				.replaceAll("–", "-")
				./* replaceAll(" ", ""). */replaceAll("_+", "_"); //\\–\\- -
		// "(3-)5-merous"/ =>_merous
		word = word.replaceFirst(".*?_(?=[a-z]+$)", ""); 
		// _or_ribbed
		String wc = word;
	
		//TODO characterhash
		if(word.matches("at[-_]least")) return new Match(null);
		if (word.endsWith("shaped")) {
			//return "shape";
			word = word.replaceFirst("shaped", "-shaped");
		}
		
		String[] ws = word.split("-or-"); //palm_or_fern_like
		word = ws[ws.length-1];

		if(word.indexOf('-')>0 && !word.endsWith("like")){
		    ws = word.split("-+");
			word = ws[ws.length-1];
		}

		Match ch = new Match(lookup(word));
		if(ch == null && word.endsWith("like")){//cup_like
			HashSet<Term> result = new HashSet<Term> ();
			result.add(new Term(wo, "shape"));
			ch = new Match(result);
		}
		if (ch == null && wc.indexOf('-') > 0) {// pani_culiform
			ch = new Match(lookup(wc.replaceAll("-", "")));
		}
		if(ch == null && wc.indexOf('-') > 0) {
			ch = new Match(lookup(ws[0]));
		}
		return ch;
	}
	
	/*
	 @Override
	public String getCharacterName(String word) {//hyphened or underscored word? 
		if (word.trim().length() == 0 || word.matches("\\W+"))
			return null;
		if(this.addedCharacters.containsKey(word)) //TODO
			return addedCharacters.get(word);
		
		if (word.indexOf(" ") > 0)
			word = word.substring(word.lastIndexOf(" ") + 1).trim();
		word = word.replaceAll("\\d+[–-]", "_") //TODO check: use _ vs -
				.replaceAll("–", "-")
				.replaceAll("_+", "_"); //\\–\\- -
		// "(3-)5-merous"/ =>_merous
		word = word.replaceFirst(".*?_(?=[a-z]+$)", ""); 
		// _or_ribbed
		String wc = word;
	
		//TODO characterhash
		String ch = "";
		if(word.matches("at[-_]least")) return null;
		if (word.endsWith("shaped")) {
			//return "shape";
			word = word.replaceFirst("shaped", "-shaped");
		}
		
		String[] ws = word.split("-or-"); //palm_or_fern_like
		word = ws[ws.length-1];

		if(word.indexOf('-')>0 && !word.endsWith("like")){
		    ws = word.split("-+");
			word = ws[ws.length-1];
		}

		ch = lookup(word);
		if(ch == null && word.endsWith("like")){//cup_like
			ch = new String[]{"shape", ""};
		}
		if (ch == null && wc.indexOf('-') > 0) {// pani_culiform
			ch = lookup(wc.replaceAll("-", ""));
		}
		if(ch == null && wc.indexOf('-') > 0) {
			ch = lookup(ws[0]);
		}
		return ch;
	}
	 */
	
	//private String lookup(String word) {
	private Set<Term> lookup(String word) {
		StringBuilder categoriesStringBuilder = new StringBuilder();
		
		// check glossarytable
		if(this.glossary.contains(word)) {
			
			Set<Term> categories = this.glossary.getInfo(word);
			categories.addAll(this.glossary.getInfo("_" + word));
			
			/*Set<Term> glossaryCategories = this.glossary.getCategories(word);
			glossaryCategories.addAll(this.glossary.getCategories("_" + word));
			
			// check _term_category table
			Set<Term> categories = new HashSet<Term>();

			/*Map<String, Set<String>> termCategories = terminologyLearner.getTermCategories(); //Question TODO checking terminologyLearner is redundant?
			//log(LogLevel.DEBUG, "termcategories " + termCategories);
			if(termCategories.containsKey(word))
				categories.addAll(termCategories.get(word));
			//Set<String> categories = new HashSet<String>(termCategories.get(w));
			categories.remove("structure");
			categories.addAll(glossaryCategories);
			*/
			
			/*String[] categoriesArray = categories.toArray(new String[] {});
			Arrays.sort(categoriesArray);
	
			for (String category : categoriesArray) {
				categoriesStringBuilder.append(category.replaceAll("\\s+", "_") + this.or);
			}
			String categoriesString = categoriesStringBuilder.toString();
			if (categoriesString.length() > 0) {
				categoriesString = categoriesString.replaceFirst(this.or + "$", "");
				return categoriesString;
			}*/
			return categories;
		}

		return null;
	}
	
	/*private String lookup(String word) {
		StringBuilder categoriesStringBuilder = new StringBuilder();

		// check glossarytable
		if(this.glossary.contains(word)) {
			Set<String> glossaryCategories = this.glossary.getCategories(word);
			glossaryCategories.addAll(this.glossary.getCategories("_" + word));
			
			// check _term_category table
			Set<String> categories = new HashSet<String>();

			Map<String, Set<String>> termCategories = terminologyLearner.getTermCategories(); //Question TODO checking terminologyLearner is redundant?
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
				return categoriesString;
			}
		}

		return null;
	}*/

	@Override
	public boolean containsCharacterState(String word) {
		return this.getCharacterName(word)!=null;
	}

	/*@Override
	public void addCharacterStateToName(String word, String character) {
		this.addedCharacters.put(word, character);
	}*/
	
	@Override
	public void addCharacterStateToName(String word, Match match) {
		word = word.replaceAll(this.c, " ");
		this.addedCharacters.put(word, match);
	}

	@Override
	public boolean containsCharacterName(String characterName) {
		Set<String> glossaryCategories = glossary.getWords("character");
		//Set<String> learnedCategories = terminologyLearner.getCategoryTerms().get("character");
		//if(learnedCategories == null)
			//learnedCategories = new HashSet<String>();
		return glossaryCategories.contains(characterName); //|| learnedCategories.contains(characterName);
	}

}
