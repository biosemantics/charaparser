/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.enhance.know.partof;

/**
 * @author updates
 *
 */
public interface IOntology {
	
	boolean isPart(String part, String parent);
	boolean objectCreated();
	

}
