package edu.arizona.sirls.semanticMarkup.markupElement.habitat.markup;

import edu.arizona.sirls.semanticMarkup.markup.IMarkupCreator;

public interface IHabitatMarkupCreator extends IMarkupCreator {

	/**
	 * Create the markup of treatments
	 */
	public HabitatMarkupResult create();

}
