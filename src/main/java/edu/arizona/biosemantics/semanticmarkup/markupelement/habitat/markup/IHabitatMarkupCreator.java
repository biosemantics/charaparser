package edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.markup;

import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupCreator;

public interface IHabitatMarkupCreator extends IMarkupCreator {

	/**
	 * Create the markup of treatments
	 */
	public HabitatMarkupResult create();

}
