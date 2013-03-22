package semanticMarkup.markup;

import java.util.List;

import semanticMarkup.core.Treatment;

/**
 * Annotator annotates a volume based on a given configuration
 * @author thomas rodenhausen
 */
public interface IMarkupCreator {

	public void create();
	
	public List<Treatment> getResult();
	
	public String getDescription();
	
}
