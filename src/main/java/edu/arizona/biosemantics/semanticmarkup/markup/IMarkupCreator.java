package edu.arizona.biosemantics.semanticmarkup.markup;

public interface IMarkupCreator {
	
	/**
	 * @return a descriptive String of the IMarkupCreator
	 */
	public String getDescription();
	
	public IMarkupResult create();
}
