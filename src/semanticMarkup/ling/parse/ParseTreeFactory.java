package semanticMarkup.ling.parse;

/**
 * A ParseTreeFactory creates AbstractParseTrees
 * @author rodenhausen
 */
public interface ParseTreeFactory {

	/**
	 * @return a created parseTree
	 */
	public AbstractParseTree create();
	
}
