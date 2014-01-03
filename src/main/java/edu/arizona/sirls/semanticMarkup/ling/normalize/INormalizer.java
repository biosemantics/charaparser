package edu.arizona.sirls.semanticMarkup.ling.normalize;

/**
 * An INormalizer normalizes a string input
 * @author rodenhausen
 */
public interface INormalizer {

	public String normalize(String str, String tag, String modifier, String source);
	
}
