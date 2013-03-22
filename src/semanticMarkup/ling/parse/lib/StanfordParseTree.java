package semanticMarkup.ling.parse.lib;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTree;
import semanticMarkup.ling.pos.POS;
import semanticMarkup.ling.pos.POSedToken;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.trees.Tree;

public class StanfordParseTree extends AbstractParseTree {
	
	private Tree stanfordParseTree;

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
			this.setText(pos.toString());
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
	 * Implements a hashCode for StanfordParseTree's. Two trees should have the same hashcode
	 * if they are equal, so we hash on the label value and the parent tree
	 * If a new wrapper is created for a Tree the StanfordParseTree's that wrap this Tree are to produce the same hash code
	 * If two Tree's have the same label they however should not be given the same hash code. 
	 * Same word in the same sentence is to be given different hashCodes, to be able to assign different chunks to it in ChunkCollector.
	 * @return The hash code
	 */
	@Override
	public int hashCode() {
		// dont have parent information
		return this.stanfordParseTree.hashCode();
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof StanfordParseTree) {
			StanfordParseTree objectTree = (StanfordParseTree)object;
			return this.stanfordParseTree==objectTree.stanfordParseTree;
		}
		return false;
	}
	
	/*@Override
	//Tree's equals() had to be overwritten to be useful
	public boolean equals(Object object) {
		if(object instanceof StanfordParseTree) {
			StanfordParseTree otherTree = (StanfordParseTree)object;
			return this.stanfordParseTree.equals(otherTree.stanfordParseTree);
		}
		return false;
	}*/

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
