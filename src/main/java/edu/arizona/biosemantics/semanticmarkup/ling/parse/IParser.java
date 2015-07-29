package edu.arizona.biosemantics.semanticmarkup.ling.parse;

import java.util.List;

import edu.arizona.biosemantics.common.ling.Token;

/**
 * An IParser parses a list of tokens (e.g. a sentence)
 * @author rodenhausen
 */
public interface IParser {

	/**
	 * @param sentence
	 * @return the resulting parse tree
	 */
	public AbstractParseTree parse(List<? extends Token> sentence);
	
}
