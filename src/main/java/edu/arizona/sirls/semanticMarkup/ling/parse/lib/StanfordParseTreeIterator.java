package edu.arizona.sirls.semanticMarkup.ling.parse.lib;

import java.util.Iterator;

import edu.arizona.sirls.semanticMarkup.ling.parse.IParseTree;
import edu.stanford.nlp.trees.Tree;

/**
 * StanfordParseTreeIterator allows to iterate over all the nodes in a StanfordParseTree in preorder
 * @author rodenhausen
 */
public class StanfordParseTreeIterator implements Iterator<IParseTree> {

	private Iterator<Tree> stanfordParseTreeIterator;
	
	/**
	 * @param stanfordParseTreeIterator
	 */
	public StanfordParseTreeIterator(Iterator<Tree> stanfordParseTreeIterator) {
		this.stanfordParseTreeIterator = stanfordParseTreeIterator;
	}
	
	@Override
	public boolean hasNext() {
		return stanfordParseTreeIterator.hasNext();
	}

	@Override
	public StanfordParseTree next() {
		return new StanfordParseTree(stanfordParseTreeIterator.next());
	}

	@Override
	public void remove() {
		stanfordParseTreeIterator.remove();		
	}

}
