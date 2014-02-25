package edu.arizona.biosemantics.semanticmarkup.ling.parse.lib;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.POS;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.POSedToken;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.trees.Tree;

/**
 * A StanfordParseTree poses an IParseTree and is the result of StanfordParserWrapper
 * @author rodenhausen
 */
public class StanfordParseTree extends AbstractParseTree {
	
	private Tree stanfordParseTree;
	
	/**
	 * @param stanfordParseTree
	 */
	public StanfordParseTree(Tree stanfordParseTree) {
		super();
		this.stanfordParseTree = stanfordParseTree;
	}

	@Override
	public List<AbstractParseTree> getChildren() {
		List<AbstractParseTree> children = new ArrayList<AbstractParseTree>();
		for(Tree stanfordTree : stanfordParseTree.getChildrenAsList()) {
			children.add(new StanfordParseTree(stanfordTree));
		}
		return children;
	}

	@Override
	public POS getPOS() {
		//POS is any non-trminal labels, such as NNS, NP, S, Frag, etc..
		if(this.isTerminal()) 
			return null;
		POS result;
		try {
			result = POS.valueOf(stanfordParseTree.value());
		} catch (Exception e) {
			result = POS.NONE;
		}
		return result;
	}
	
	@Override
	public String getTerminalsText() {
		StringBuilder stringBuilder = new StringBuilder();
		ArrayList<HasWord> hasWords = stanfordParseTree.yieldHasWord();
		for(HasWord hasWord : hasWords) {
			stringBuilder.append(hasWord.word() + " ");
		}
		return stringBuilder.toString().trim();
	}
	
	@Override
	public String toString() {
		return stanfordParseTree.toString();
	}

	@Override
	public List<AbstractParseTree> getTerminals() {
		List<Tree> leaves = this.stanfordParseTree.getLeaves();
		List<AbstractParseTree> result = new ArrayList<AbstractParseTree>();
		for(Tree leaf : leaves) {
			result.add(new StanfordParseTree(leaf));
		}
		return result;
	}

	@Override
	public AbstractParseTree getAncestor(int height, IParseTree root) {
		if(root instanceof StanfordParseTree) {
			StanfordParseTree rootTree = (StanfordParseTree) root;
			Tree ancestor = this.stanfordParseTree.ancestor(height, rootTree.stanfordParseTree);
			if(ancestor != null) 
				return new StanfordParseTree(ancestor);
		}
		return null;
	}

	@Override
	public List<IParseTree> getChildrenOfPOS(POS pos) {
		List<IParseTree> childrenOfPOS = new ArrayList<IParseTree>();
		for(IParseTree child : this.getChildren()) {
			if(!child.isTerminal() && child.getPOS().equals(pos)) {
				childrenOfPOS.add(child);
			}
		}
		return childrenOfPOS;
	}

	@Override
	public boolean isTerminal() {
		return stanfordParseTree.isLeaf();
	}

	@Override
	public AbstractParseTree getParent(IParseTree root) {
		return this.getAncestor(1, root);
	}

	@Override
	public Iterator<IParseTree> iterator() {
		return new StanfordParseTreeIterator(stanfordParseTree.iterator());
	}

	@Override
	public int getTerminalID(IParseTree terminal) {
		List<AbstractParseTree> terminals = this.getTerminals();
		for(int i=0; i<terminals.size(); i++) {
			if(terminals.get(i).equals(terminal)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void removeChild(IParseTree subtree) {
		if(subtree instanceof StanfordParseTree) {
			StanfordParseTree stanfordParseSubTree = (StanfordParseTree)subtree;
			int childId = stanfordParseTree.indexOf(stanfordParseSubTree.stanfordParseTree);
			stanfordParseTree.removeChild(childId);
		}
	}

	@Override
	public void removeAllChildren() {
		for(Tree tree : stanfordParseTree.children()) {
			int childId = stanfordParseTree.indexOf(tree);
			stanfordParseTree.removeChild(childId);
		}
	}


	@Override
	public void addChild(IParseTree child) {
		if(child instanceof StanfordParseTree) {
			StanfordParseTree stanfordParseSubTree = (StanfordParseTree)child;
			stanfordParseTree.addChild(stanfordParseSubTree.stanfordParseTree);
		}
	}
	
	@Override
	public void addChildren(List<AbstractParseTree> children) {
		for(IParseTree child : children) 
			addChild(child);
	}

	@Override
	public String prettyPrint() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		this.stanfordParseTree.indentedXMLPrint(printWriter, true);
		printWriter.flush();
		printWriter.close();
		return stringWriter.toString();
		
	}

	@Override
	public boolean isRoot() {
		return stanfordParseTree.value().equals("ROOT");
	}

	@Override
	public boolean is(POS pos) {
		return stanfordParseTree.value().equals(pos.toString());
	}

	@Override
	public void setPOS(POS pos) {
		//a node may just have been created and hence be neither terminal nor non-terminal as not yet inserted in final position in another tree
		// one may still already want to set its text. therefore dont restrict here
		//if(!this.isTerminal()) 
			this.setText(pos.toString());//this.setPOS(pos);
	}

	@Override
	public void setTerminalsText(String text) {
		//a node may just have been created and hence be neither terminal nor non-terminal as not yet inserted in final position in another tree
		// one may still already want to set its text. therefore dont restrict here
		//if(this.isTerminal())
			this.setText(text);
	}

	private void setText(String text) {
		CoreLabel coreLabel = new CoreLabel();
		coreLabel.setValue(text);
		stanfordParseTree.setLabel(coreLabel);
	}

	@Override
	public List<POSedToken> getSentence() {
		List<POSedToken> sentence = new ArrayList<POSedToken>();
		List<AbstractParseTree> terminals = this.getTerminals();
		for(IParseTree terminal : terminals) {
			IParseTree parent = terminal.getParent(this);
			String content = terminal.getTerminalsText();
			POSedToken token = new POSedToken(content, parent.getPOS());
			sentence.add(token);
		}
		return sentence;
	}


	/**
	 * Two StanfordParseTrees that wrap the same Tree should return the same hashCode. 
	 * This becomes necessary, as e.g. getTerminals() creates a new Tree-wrapping-StanfordParseTree object upon every call.
	 * ChunkCollector requires them to be hashed equally in order to store the terminal -> chunk mapping.
	 * 
	 * Tree hashes in its implementation not on System.identityHashCode() (the default hash for any object), but rather based on content.
	 * Since Tree's hashcode is only based on label value and children's label valuues, the hash becomes equal for leaves with equal text.
	 * This is not sufficient for our needs, since a sentence can easily contain the same word twice
	 */ 
	@Override
	public int hashCode() {
		return System.identityHashCode(this.stanfordParseTree);
	}
	
	/**
	 * Two StanfordParseTrees are to be considered equal when their wrapped Tree is equal according to the hash function (i.e. exactly the same object)
	 */
	@Override 
	public boolean equals(Object object) {
		if(object == null)
			return false;
		return object.hashCode()==this.hashCode();
	}
	
	@Override
	public void removeDescendant(IParseTree descendant) {
		if(descendant instanceof StanfordParseTree) {
			StanfordParseTree toRemove = (StanfordParseTree)descendant;
			
			for(IParseTree child : this.getChildren()) {
				if(child.equals(toRemove)) {
					this.removeChild(child);
					break;
				}
				child.removeDescendant(descendant);
			}
			
		}
	}

	@Override
	public int indexOf(IParseTree child) {
		if(child instanceof StanfordParseTree) {
			StanfordParseTree stanfordParseSubTree = (StanfordParseTree)child;
			return stanfordParseTree.indexOf(stanfordParseSubTree.stanfordParseTree);
		}
		return -1;
	}

	@Override
	public void addChild(int index, IParseTree child) {
		if(child instanceof StanfordParseTree) {
			StanfordParseTree stanfordParseSubTree = (StanfordParseTree)child;
			stanfordParseTree.addChild(index, stanfordParseSubTree.stanfordParseTree);
		}
	}
	
	@Override
	public void addChildren(int index, List<AbstractParseTree> children) {
		for(int i=children.size()-1; i>=0; i--) {
			addChild(index, children.get(i));
		}
	}

	@Override
	public boolean hasChildren() {
		return stanfordParseTree.numChildren()>0;
	}

	@Override
	public int getDepth(IParseTree parseTree) {
		if(parseTree instanceof StanfordParseTree) {
			StanfordParseTree descendant = (StanfordParseTree)parseTree;
			return stanfordParseTree.depth(descendant.stanfordParseTree);
		}
		return -1;
	}

}
