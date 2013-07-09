package semanticMarkup.markupElement.habitat.markup;

import semanticMarkup.markup.IMarkupCreator;

public interface IHabitatMarkupCreator extends IMarkupCreator {

	/**
	 * Create the markup of treatments
	 */
	public HabitatMarkupResult create();

}
