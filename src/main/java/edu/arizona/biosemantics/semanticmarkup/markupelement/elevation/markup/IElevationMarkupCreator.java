package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.markup;

import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupCreator;

/**
 * IMarkupCreator creates a markup for treatments and can return the marked up result
 * @author thomas rodenhausen
 */
public interface IElevationMarkupCreator extends IMarkupCreator {

	/**
	 * Create the markup of treatments
	 */
	public ElevationMarkupResult create();
	
}
