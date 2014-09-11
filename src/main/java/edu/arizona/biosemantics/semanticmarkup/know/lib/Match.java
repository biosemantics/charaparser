/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.know.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.arizona.biosemantics.semanticmarkup.know.IMatch;

/**
 * @author Hong Cui
 * holding the match from a character look up in LearnedCharacterKnowledgeBase
 *
 */
public class Match implements IMatch {
	private Set<Term> content = new HashSet<Term>();
	private String categories = null; //or-contactnated categories
	private String preferedTerm = null; //not null when all Terms has the same preferred term/label
	private String or = "_or_";
	
	public Match(Set<Term> content){
		this.content = content;
		if(content!=null && content.size()>0) populateMatch();
	}
	
	private void populateMatch(){
		Iterator<Term> terms = content.iterator();
		HashSet<String> labels = new HashSet<String>();
		//collect all categories 
		ArrayList<String> cats = new ArrayList<String>();
		while(terms.hasNext()){
			Term t = terms.next();
			cats.add(t.getCategory());
			labels.add(t.getLabel());
		}
		
		String[] categoriesArray = cats.toArray(new String[] {});
		Arrays.sort(categoriesArray);
		
		StringBuilder categoriesStringBuilder = new StringBuilder();
		for (String category : categoriesArray) {
			categoriesStringBuilder.append(category.replaceAll("\\s+", "_") + this.or);
		}
		String categoriesString = categoriesStringBuilder.toString();
		if (categoriesString.length() > 0) {
			categoriesString = categoriesString.replaceFirst(this.or + "$", "");
			this.categories = categoriesString;
		}
		
		//find the preferedTerm
		if(labels.size()==1){
			Iterator<String> it = labels.iterator();
			this.preferedTerm = it.next();
		}
	}

	public String getLabel(String category){
		if(content!=null){
			Iterator<Term> terms = content.iterator();
			while(terms.hasNext()){
				Term t = terms.next();
				if(category.compareTo(t.getCategory())==0) return t.getLabel();
			}
		}
		return null;
	}
	
	public String getLabel(){
		return this.preferedTerm;
	}
	
	public String getCategories(){
		return this.categories;
	}
}
