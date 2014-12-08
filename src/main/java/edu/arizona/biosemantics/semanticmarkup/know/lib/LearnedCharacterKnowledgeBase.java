package edu.arizona.biosemantics.semanticmarkup.know.lib;

import java.util.ArrayList;
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
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
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
	private String advModifiers;
	//private ITerminologyLearner terminologyLearner;
	//private ConcurrentHashMap<String, String> addedCharacters = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, Match> addedCharacters = new ConcurrentHashMap<String, Match>();
	private ConcurrentHashMap<String, Match> characterCache = new ConcurrentHashMap<String, Match> ();
	private ConcurrentHashMap<String, Boolean> isEntityCache = new ConcurrentHashMap<String, Boolean> ();
	private ConcurrentHashMap<String, Boolean> isStateCache = new ConcurrentHashMap<String, Boolean> ();
	private ConcurrentHashMap<String, ArrayList<String>> entityTypeCache = new ConcurrentHashMap<String, ArrayList<String>> (); //term => entity type (structure, taxon_name, etc.)
	private IInflector inflector;
	/**
	 * @param glossary
	 * @param negWords
	 */
	@Inject
	public LearnedCharacterKnowledgeBase(/*ITerminologyLearner terminologyLearner,*/ IGlossary glossary, 
			@Named("NegationWords")String negWords, @Named("AdvModifiers") String advModifiers, IInflector inflector) {
		//this.terminologyLearner = terminologyLearner;
		this.glossary = glossary;
		this.negWords = negWords;
		this.advModifiers = advModifiers+"|"+advModifiers.replaceAll(" ", "[_-]"); //at least|at[_-]least
		this.inflector = inflector;
	}
	
	@Override
	public boolean isEntity(String word){
		if(isEntityCache.get(word)!=null) return isEntityCache.get(word);
				
		String cats = this.getCharacterName(word).getCategories();
		//boolean isEntity = cats !=null && cats.matches(".*?(^|_)structure(_|$).*");
		boolean isEntity = cats !=null && cats.matches(".*?(^|_)("+ElementRelationGroup.entityElements+")(_|$).*");
		isEntityCache.put(word, isEntity);
		if(isEntity){
			String[] catArray = cats.split(or);
			for(String cat: catArray){
				ArrayList<String> types = entityTypeCache.get(cat);
				if(types==null)	types = new ArrayList<String>();
				types.add(cat);
				entityTypeCache.put(word, types);
			}
		}
		return isEntity;
	}
		
	@Override
	public String getEntityType(String singular, String original){
		ArrayList<String> types = new ArrayList<String>();
		if(entityTypeCache.get(original)!=null) types = entityTypeCache.get(original); 
		else if(entityTypeCache.get(singular)!=null) types = entityTypeCache.get(singular);
		String typeString = "";
		for(String type: types){
			typeString += type+this.or;
		}
		return typeString.replaceFirst(or+"$", "");
	}
	
	@Override
	public boolean isState(String word){
		if(isStateCache.get(word)!=null) return isStateCache.get(word);
		
		String cats = this.getCharacterName(word).getCategories();
		//boolean isstate = cats!=null && !cats.matches(".*?(^|_)structure(_|$).*");
		boolean isstate = cats!=null && !cats.matches(".*?(^|_)("+ElementRelationGroup.entityElements+")(_|$).*");
		isStateCache.put(word, isstate);
		return isstate;
	}

	@Override
	public Match getCharacterName(String word) {//hyphened words (standardized "_" to "-").
		String wo = word;

		//rejected searches
		if(word.matches("("+negWords+")")) return new Match(null);
		if (word.trim().length() == 0 || word.matches("\\W+")) return new Match(null);
		//if(word.matches("at[-_]least")) return new Match(null);//TODO: synchronize with Normalizer.modifierPhrases. move to configuration.
		if(word.matches("(?:"+this.advModifiers+")")) return new Match(null);
		
		//check caches
		if(characterCache.get(wo)!=null) return characterCache.get(wo);
		if(this.addedCharacters.containsKey(word)) //"brownish red" from "brownish_c_red", terms created by the program
			return addedCharacters.get(word);
		
        //normalize search term
		String[] ws = word.split("-or-"); //palm_or_fern_like
		word = ws[ws.length-1];
		ws = word.split("\\s+"); //brownish red
		word = ws[ws.length-1];
		if (word.endsWith("shaped")) word = word.replaceFirst("shaped", "-shaped");
		
		
		//standarize "_" to "-"
		word = word.replaceAll("[– ]+", "-").replaceAll("[_ ]+", "-");
		// "(3-)5-merous"/ =>merous
		word = word.replaceFirst(".*?_(?=[a-z]+$)", ""); 
		String wc = word;
	
		//search
		Set<Term> ch = lookup(word);
		Match m = new Match(ch);
		if(ch == null && word.endsWith("like")){//cup_like, cuplike
			HashSet<Term> result = new HashSet<Term> ();
			result.add(new Term(wo, "shape"));
			m = new Match(result);
		}else if (ch == null && wc.indexOf('-') > 0) {// pani_culiform, primocane_foliage
			ws = word.split("-+");
			word = word.replaceFirst(ws[ws.length-1]+"$", inflector.getSingular(ws[ws.length-1])); //pl -> singular, to get a match for the added pl form of phrases in PhraseMarker
			ch = lookup(word);
			if(ch==null){
				word = ws[ws.length-1];//search last word in the phrase
				ch = lookup(word);
				if(ch == null){
					ch = lookup(wc.replaceAll("-", ""));
					if(ch == null){
						//ch = lookup(ws[0]); //searching the first word in a "-"-connected phrase doesn't make sense hong 5/5/14
						m = new Match(ch);
					}else{
						m = new Match(ch);
					}
				}else{
					for(Term t: ch){
						t.setLabel(wc);
					}
					m = new Match(ch);
				}
			}else{
				m = new Match(ch);
			}
		}
		
		characterCache.put(wo, m);
		return m;
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
