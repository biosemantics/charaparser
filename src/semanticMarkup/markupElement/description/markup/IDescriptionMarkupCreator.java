package semanticMarkup.markupElement.description.markup;

import semanticMarkup.markup.IMarkupCreator;

/**
 * IMarkupCreator creates a markup for treatments and can return the marked up result
 * @author thomas rodenhausen
 */
public interface IDescriptionMarkupCreator extends IMarkupCreator {

	/**
	 * Create the markup of treatments
	 */
	public DescriptionMarkupResult create();
	
}
