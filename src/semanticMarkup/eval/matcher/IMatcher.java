package semanticMarkup.eval.matcher;

import semanticMarkup.eval.model.Element;

public interface IMatcher<T extends Element> {

	public boolean isMatch(T elementA, T elementB);
	
}
