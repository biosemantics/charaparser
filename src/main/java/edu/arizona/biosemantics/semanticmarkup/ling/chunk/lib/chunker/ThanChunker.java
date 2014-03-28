package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;



import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

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
			 ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, 
				terminologyLearner, inflector,  learnedCharacterKnowledgeBase);
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
						(terminal.getTerminalsText().matches("\\w+er|more|less") && !chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN)//)) {
						|| (i<terminals.size()-1 && terminals.get(i+1).getTerminalsText().equals("than")))) {
					comparisonTerminal = terminal;
					if(terminal.getTerminalsText().equals("more") || terminal.getTerminalsText().equals("less"))
						more = "more";
					else if(terminal.getTerminalsText().matches("\\w+er"))
						more = "er";
				
				} else if(more.equals("er") && !terminal.getTerminalsText().matches("\\w+er|more|less|and|or|than")){
					more = "";
					comparisonTerminal = null;//this.chunkedtokens.size();; (index out of bounds?)
				} else if(chunkCollector.isOfChunkType(terminal, ChunkType.COMMA) || 
							chunkCollector.isOfChunkType(terminal, ChunkType.END_OF_LINE) || 
							chunkCollector.isOfChunkType(terminal, ChunkType.END_OF_SUBCLAUSE)) {
					more = "";
					comparisonTerminal = null;
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
					
					//could already contain object from PP Chunk
					if(!than.containsChunkType(ChunkType.OBJECT)) {
						//do the collapsing			
						//scan for the object of "than"
						Chunk objectChunk = new Chunk(ChunkType.OBJECT);
						for(i=i+1; i<terminals.size(); i++) {
							AbstractParseTree possibleObjectTerminal = terminals.get(i);
							if(possibleObjectTerminal.getTerminalsText().matches(prepositionWords + "|and|that|which|but") ||
									possibleObjectTerminal.getTerminalsText().matches(".*?\\p{Punct}.*")) {
								//should allow �, n[{shorter} than] � {campanulate} <throats>
								
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
								than.getChunks().add(objectChunk);
								Chunk thanChunk = new Chunk(ChunkType.THAN_PHRASE, collectedTerminals);
								chunkCollector.addChunk(thanChunk);
								break;
							} else {
								Chunk possibleObjectChunk = chunkCollector.getChunk(possibleObjectTerminal);
								objectChunk.getChunks().add(possibleObjectChunk);
							}
						}
					} else {
						Chunk thanChunk = new Chunk(ChunkType.THAN_PHRASE, collectedTerminals);
						chunkCollector.addChunk(thanChunk);
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
