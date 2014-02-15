package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.markup;

import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupCreator;

/**
 * IMarkupCreator creates a markup for treatments and can return the marked up result
 * @author thomas rodenhausen
 */
public interface IHabitatMarkupCreator extends IMarkupCreator {

	/**
	 * Create the markup of treatments
	 */
	public HabitatMarkupResult create();
	
}
