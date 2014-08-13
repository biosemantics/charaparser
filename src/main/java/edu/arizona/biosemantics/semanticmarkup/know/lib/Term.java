/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.know.lib;

import java.util.Comparator;

import edu.arizona.biosemantics.semanticmarkup.know.ITerm;

/**
 * @author Hong Cui
 * one term  = a string + a category
 *
 */
public class Term implements ITerm, Comparator<ITerm>{
	private String label;
	private String category;

	public Term(String str, String category){
		this.label = str;
		this.category = category;
		
	}
	@Override
	public void setLabel(String label) {
		this.label = label;
		
	}

	@Override
	public void setCategory(String category) {
		this.category = category;
		
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public int compare(ITerm t1, ITerm t2) {
		if(t1.getLabel().compareTo(t2.getLabel())==0 &&
				t1.getCategory().compareTo(t2.getCategory())==0)
		return 0;
		
		return t1.getLabel().compareTo(t2.getLabel());
	}

	@Override
	public boolean equals(Object o) {
		return this.getLabel().compareTo(((Term)o).getLabel())==0 &&
				this.getCategory().compareTo(((Term)o).getCategory())==0;
	}
	
	@Override
	public int hashCode(){
		return (label+" "+category).hashCode();
	}
}
