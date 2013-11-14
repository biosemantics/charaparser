package semanticMarkup.eval.matcher;

import semanticMarkup.model.Element;

public interface IMatcher<T extends Element> {

	public boolean isMatch(T elementA, T elementB);
	
}
