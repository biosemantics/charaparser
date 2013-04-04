package semanticMarkup.ling.parse.lib;

import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTreeFactory;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;

/**
 * A StanfordParseTreeFactory creates StanfordParseTrees
 * @author rodenhausen
 */
public class StanfordParseTreeFactory implements IParseTreeFactory {

	@Override
	public AbstractParseTree create() {
		Tree tree = new LabeledScoredTreeNode();
		return new StanfordParseTree(tree);
	}

}
