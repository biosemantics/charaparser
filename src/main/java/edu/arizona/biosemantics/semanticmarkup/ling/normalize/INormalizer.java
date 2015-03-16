package edu.arizona.biosemantics.semanticmarkup.ling.normalize;

import java.util.Hashtable;

/**
 * An INormalizer normalizes a string input
 * @author rodenhausen
 */
public interface INormalizer {

	public String normalize(String str, String tag, String modifier, String source, Hashtable<String, String> prevMissingOrgan);

	public void init();
	
}
