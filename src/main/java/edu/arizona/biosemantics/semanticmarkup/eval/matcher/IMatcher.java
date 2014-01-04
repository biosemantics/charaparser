package edu.arizona.biosemantics.semanticmarkup.eval.matcher;

import edu.arizona.biosemantics.semanticmarkup.model.Element;

public interface IMatcher<T extends Element> {

	public boolean isMatch(T elementA, T elementB);
	
}
