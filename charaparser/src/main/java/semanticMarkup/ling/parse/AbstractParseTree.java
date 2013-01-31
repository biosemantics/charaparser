package semanticMarkup.ling.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.pos.POS;

public abstract class AbstractParseTree extends Chunk implements IParseTree {

	public AbstractParseTree() {
		super(ChunkType.UNASSIGNED);
	}
	
	@Override
	public List<AbstractParseTree> getDescendants(POS posA, Set<POS> posBs) {
		List<AbstractParseTree> result = new ArrayList<AbstractParseTree>();
		
		for(IParseTree subTree : this.getChildren()) {
			
			if(!subTree.isTerminal() && subTree.getPOS().equals(posA)) {
				for(AbstractParseTree subsubTree : subTree.getChildren()) {
					if(!subsubTree.isTerminal() && posBs.contains(subsubTree.getPOS())) {
						result.add(subsubTree);
					}
				}
			}
			result.addAll(subTree.getDescendants(posA, posBs));
		}
		return result;
	}

	@Override
	public List<AbstractParseTree> getDescendants(POS posA, POS posB) {
		List<AbstractParseTree> result = new ArrayList<AbstractParseTree>();
		
		for(IParseTree subTree : this.getChildren()) {
			
			if(!subTree.isTerminal() && subTree.getPOS().equals(posA)) {
				for(AbstractParseTree subsubTree : subTree.getChildren()) {
					if(!subsubTree.isTerminal() && subsubTree.getPOS().equals(posB)) {
						result.add(subsubTree);
					}
				}
			}
			result.addAll(subTree.getDescendants(posA, posB));
		}
		return result;
	}

	@Override
	public List<IParseTree> getDescendants(POS pos) {
		List<IParseTree> result = new ArrayList<IParseTree>();
		
		for(IParseTree subTree : this.getChildren()) {
			if(!subTree.isTerminal() && subTree.getPOS().equals(pos)) {
				result.add(subTree);
			}
			result.addAll(subTree.getDescendants(pos));
		}
		return result;
	}
	
	@Override
	public List<IParseTree> getDescendants(POS posA, POS posB, String terminalText) {
		List<IParseTree> result = new ArrayList<IParseTree>();
		
		for(IParseTree subTree : this.getChildren()) {
			if(!subTree.isTerminal() && subTree.getPOS().equals(posA)) {
				for(IParseTree subsubTree : subTree.getChildren()) {
					if(!subsubTree.isTerminal() && subsubTree.getPOS().equals(posB)) {
						for(IParseTree subsubsubTree : subsubTree.getChildren()) {
							if(subsubsubTree.getTerminalsText().equalsIgnoreCase(terminalText)) 
								result.add(subsubsubTree);
						}
					}
				}
			}
			result.addAll(subTree.getDescendants(posA, posB, terminalText));
		}
		return result;
	}
	
	@Override
	public List<IParseTree> getTerminalsOfText(String text) {
		List<IParseTree> result = new ArrayList<IParseTree>();
		List<AbstractParseTree> terminals = this.getTerminals();
		for(IParseTree terminal : terminals) {
			if(terminal.getTerminalsText().equals(text)) 
				result.add(terminal);
		}
		return result;
	}
	
	@Override
	public List<IParseTree> getTerminalsThatContain(String text) {
		List<IParseTree> result = new ArrayList<IParseTree>();
		List<AbstractParseTree> terminals = this.getTerminals();
		for(IParseTree terminal : terminals) {
			if(terminal.getTerminalsText().contains(text)) 
				result.add(terminal);
		}
		return result;
	}
}
