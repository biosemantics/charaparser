package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTree;
import semanticMarkup.ling.parse.IParseTreeFactory;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * ThanChunker chunks comparison phrases connected by "than", "more", etc.
 * @author rodenhausen
 */
public class ThanChunker extends AbstractChunker {

	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 */
	@Inject
	public ThanChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, 
				terminologyLearner, inflector, organStateKnowledgeBase);
	}

	/**
	 * shorter and wider than ...
	 * more/less smooth than ...
	 * pretty good now
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		String prepositionWords = this.prepositionWords.replaceFirst("\\bthan\\|", "").replaceFirst("\\bto\\|", "");
		IParseTree parseTree = chunkCollector.getParseTree();
		List<IParseTree> thanSubTrees = parseTree.getTerminalsThatContain("than");
		String more = "";
		if(thanSubTrees.size() > 0) {
			IParseTree comparisonTerminal = null;
			AbstractParseTree thanTerminal = null;
			LinkedHashSet<Chunk> collectedTerminals = new LinkedHashSet<Chunk>();
			List<AbstractParseTree> terminals = chunkCollector.getTerminals();
			//List<POSedToken> sentence = parseTree.getSentence();
			for(int i=0; i<terminals.size(); i++) {
				AbstractParseTree terminal = terminals.get(i);
				if(more.isEmpty() && 
						(terminal.getTerminalsText().matches("\\w+er|more|less") && !chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN) 
						|| (i<terminals.size()-1 && terminals.get(i+1).getTerminalsText().equals("than")))) {
					comparisonTerminal = terminal;
					if(terminal.getTerminalsText().equals("more"))
						more = "more";
					else if(terminal.getTerminalsText().matches("\\w+er"))
						more = "er";
				
				} else if(more.equals("er") && !terminal.getTerminalsText().matches("\\w+er|more|less|and|or|than")){
					more = "";
					comparisonTerminal = null;//this.chunkedtokens.size();; (index out of bounds?)
				}
				
				if(terminal.getTerminalsText().matches("than")) {
					thanTerminal = terminal;
				
					int comparisonTerminalIndex = terminals.indexOf(comparisonTerminal);
					int thanTerminalIndex = terminals.indexOf(thanTerminal);
					if(comparisonTerminal!=null && comparisonTerminalIndex < thanTerminalIndex) {
						for(int j=comparisonTerminalIndex; j<thanTerminalIndex; j++) {
							AbstractParseTree possibleObjectTerminal = terminals.get(j);
							collectedTerminals.add(chunkCollector.getChunk(possibleObjectTerminal));
						}
					}
					
					Chunk than = new Chunk(ChunkType.THAN, chunkCollector.getChunk(thanTerminal));
					chunkCollector.addChunk(than);
					collectedTerminals.add(than);
					
					//do the collapsing			
					//scan for the object of "than"
					for(i=i+1; i<terminals.size(); i++) {
						AbstractParseTree possibleObjectTerminal = terminals.get(i);
						if(possibleObjectTerminal.getTerminalsText().matches(prepositionWords + "|and|that|which|but") ||
								possibleObjectTerminal.getTerminalsText().matches(".*?\\p{Punct}.*")) {
							//should allow ±, n[{shorter} than] ± {campanulate} <throats>
							
							//create the collapse chunk
							/*StringBuilder collapsedTextBuilder = new StringBuilder();
							for(IParseTree collectedTerminal : collectedTerminals) 
								collapsedTextBuilder.append(collectedTerminal.getTerminalsText()).append(" ");
							IParseTree collapseRoot = comparisonTerminal.getAncestor(2, parseTree);
							IParseTree posTree = parseTreeFactory.create();
							posTree.setPOS(POS.COLLAPSED_THAN);
							IParseTree terminalTree = parseTreeFactory.create();
							terminalTree.setTerminalsText(collapsedTextBuilder.toString().trim());
							posTree.addChild(terminalTree);
							collapseRoot.addChild(posTree);*/
							Chunk thanChunk = new Chunk(ChunkType.THAN_PHRASE, collectedTerminals);
							chunkCollector.addChunk(thanChunk);
							break;
						} else {
							Chunk chunkToAdd = chunkCollector.getChunk(possibleObjectTerminal);
							collectedTerminals.add(chunkToAdd);
						}
					}
					
					//reset
					collectedTerminals.clear();
					comparisonTerminal = null;
					thanTerminal = null;
				}
			}	
		}
	}
}
