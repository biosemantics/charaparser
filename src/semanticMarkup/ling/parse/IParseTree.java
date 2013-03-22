package semanticMarkup.ling.parse;

import java.util.List;
import java.util.Set;

import semanticMarkup.ling.pos.POS;
import semanticMarkup.ling.pos.POSedToken;

public interface IParseTree extends Iterable<IParseTree> {
	
	public List<AbstractParseTree> getChildren();
	
	public List<AbstractParseTree> getTerminals();
	
	public List<IParseTree> getTerminalsOfText(String text);
	
	public IParseTree getAncestor(int height, IParseTree root);
	
	public AbstractParseTree getParent(IParseTree root);
	
	public POS getPOS();
	
	public String getTerminalsText();
	
	public List<POSedToken> getSentence();

	public List<IParseTree> getChildrenOfPOS(POS pos);
	
	public boolean isTerminal();

	public int getTerminalID(IParseTree terminal);

	public void removeChild(IParseTree subtree);

	public void removeAllChildren();

	public void setPOS(POS pos);
	
	public void setTerminalsText(String text);

	public void addChild(IParseTree child);
	
	public int indexOf(IParseTree child);
	
	public void addChild(int index, IParseTree child);
	
	/**
	 * Implementation has to ensure that the order of subtrees is according to the sentence word order 
	 * @param firstText
	 * @param secondText
	 * @return list of subtrees that match the first and second text in consecutive order
	 */
	public List<AbstractParseTree> getDescendants(POS posA, POS posB);
	
	public List<AbstractParseTree> getDescendants(POS posA, Set<POS> posBs);

	public List<IParseTree> getDescendants(POS pos);

	public List<IParseTree> getDescendants(POS posA, POS posB, String terminalText);
	
	public String prettyPrint();
	
	public boolean isRoot();
	
	public boolean is(POS pos);

	public void addChildren(List<AbstractParseTree> children);

	public void removeDescendant(IParseTree descendant);

	public List<IParseTree> getTerminalsThatContain(String text);
	
	public boolean hasChildren();

	public void addChildren(int beforePreviousParentIndex, List<AbstractParseTree> children);
	
	public int getDepth(IParseTree parseTree);
}
