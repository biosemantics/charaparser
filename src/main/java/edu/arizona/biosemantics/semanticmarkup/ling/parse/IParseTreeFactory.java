package edu.arizona.biosemantics.semanticmarkup.ling.parse;

/**
 * A ParseTreeFactory creates AbstractParseTrees
 * @author rodenhausen
 */
public interface IParseTreeFactory {

	/**
	 * @return a created parseTree
	 */
	public AbstractParseTree create();
	
}
