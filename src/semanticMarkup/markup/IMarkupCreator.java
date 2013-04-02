package semanticMarkup.markup;

import java.util.List;

import semanticMarkup.core.Treatment;

/**
 * IMarkupCreator creates a markup for treatments and can return the marked up result
 * @author thomas rodenhausen
 */
public interface IMarkupCreator {

	/**
	 * Create the markup of treatments
	 */
	public void create();
	
	/**
	 * @return the resulting treatments
	 */
	public List<Treatment> getResult();
	
	/**
	 * @return a descriptive String of the IMarkupCreator
	 */
	public String getDescription();
	
}
