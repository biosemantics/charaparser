package semanticMarkup.ling.parse.lib;

import java.util.Iterator;

import semanticMarkup.ling.parse.IParseTree;
import edu.stanford.nlp.trees.Tree;

public class StanfordParseTreeIterator implements Iterator<IParseTree> {

	private Iterator<Tree> stanfordParseTreeIterator;
	
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
