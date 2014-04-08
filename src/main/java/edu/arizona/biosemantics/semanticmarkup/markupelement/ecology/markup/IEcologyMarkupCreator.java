/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.markup;

import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.markup.EcologyMarkupResult;

/**
 * @author Hong Cui
 *
 */
public interface IEcologyMarkupCreator extends IMarkupCreator {
	
	/**
	 * Create the markup of treatments
	 */
	public EcologyMarkupResult create();

}
