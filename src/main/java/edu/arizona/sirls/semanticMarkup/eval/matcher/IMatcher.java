package edu.arizona.sirls.semanticMarkup.eval.matcher;

import edu.arizona.sirls.semanticMarkup.model.Element;

public interface IMatcher<T extends Element> {

	public boolean isMatch(T elementA, T elementB);
	
}
