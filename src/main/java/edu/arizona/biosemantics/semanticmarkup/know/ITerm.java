/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.know;

import java.util.Comparator;

/**
 * @author Hong Cui
 *
 */
public interface ITerm {
	void setLabel(String label);
	void setCategory(String category);
	String getLabel();
	String getCategory();
		
}
