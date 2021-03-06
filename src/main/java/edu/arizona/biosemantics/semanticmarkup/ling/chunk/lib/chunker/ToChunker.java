package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.pos.POS;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * ToChunker chunks by handling "to" terminals
 * @author rodenhausen
 */
public class ToChunker extends AbstractChunker {

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
	public ToChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters,
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector,
			ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary,
				terminologyLearner, inflector,  learnedCharacterKnowledgeBase);
	}


	/**
	 * expanded to &lt;throats&gt;
	 * to 6 m.
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		boolean startNoun = false;
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		LinkedHashSet<Chunk> collectedTerminals = new LinkedHashSet<Chunk>();
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			if(terminal.getTerminalsText().equals("to") || terminal.getTerminalsText().matches(".*?\\bto]+$")) {
				collectedTerminals.add(chunkCollector.getChunk(terminal));

				//scan for the next organ
				for(int j=i+1; j<terminals.size(); j++) {

					AbstractParseTree lookAheadTerminal = terminals.get(j);
					String lookAheadText = lookAheadTerminal.getTerminalsText();
					if(j==i+1 && lookAheadText.matches("\\d[^a-z]*")) {
						formRange(chunkCollector, i);
						break;
					}

					if(startNoun && chunkCollector.isPartOfChunkType(lookAheadTerminal, ChunkType.ORGAN)) {
						break;
					}

					boolean isPartOfVPOrPPChunk = isPartOfVPOrPPChunk(lookAheadTerminal, chunkCollector);
					if(lookAheadText.matches("[,:;\\d]") ||
							isPartOfVPOrPPChunk	||
							lookAheadText.matches(this.prepositionWords + "|and|or|that|which|but")){
						break;
					}

					collectedTerminals.add(chunkCollector.getChunk(lookAheadTerminal));

					if(chunkCollector.isPartOfChunkType(lookAheadTerminal, ChunkType.ORGAN)) {
						startNoun = true;
					}
				}

				if(startNoun) {
					//scan forward for the start of the chunk
					//boolean startChunk = false; //find the start of the chunk
					collectedTerminals.add(chunkCollector.getChunk(terminal));
					for(int j = i-1; j>=0; j--){
						AbstractParseTree lookBehindTerminal = terminals.get(j);
						if(lookBehindTerminal.getTerminalsText().matches(prepositionWords+"|and|or|that|which|but") ||
								lookBehindTerminal.getTerminalsText().matches("[>;,:]") ||
								(chunkCollector.isPartOfANonTerminalChunk(lookBehindTerminal) && j != i-1) ) {
							//the last condition is to avoid nested chunks. cannot immediately before w[].
							//e.g: b[v[{placed}] o[{close}]] w[to {posterior} (shell) (margin)] ;

							Chunk toChunk = new Chunk(ChunkType.TO, collectedTerminals);
							chunkCollector.addChunk(toChunk);

							startNoun = false;
							//startChunk = true;
							break;
						} else {
							List<Chunk> collectedTerminalsList = new ArrayList<Chunk>(collectedTerminals);
							collectedTerminals.clear();
							collectedTerminals.add(chunkCollector.getChunk(lookBehindTerminal));
							collectedTerminals.addAll(collectedTerminalsList);
						}

					}
				}
			}
		}
	}

	private boolean isPartOfVPOrPPChunk(AbstractParseTree lookAheadTerminal,
			ChunkCollector chunkCollector) {
		return chunkCollector.isPartOfChunkType(lookAheadTerminal, ChunkType.PP)  ||
				chunkCollector.isPartOfChunkType(lookAheadTerminal, ChunkType.VP);
	}

	/**
	 * form a chunk if a pattern "to # unit" is found starting from i
	 * @param i: index of "to", which is followed by a number
	 */
	private void formRange(ChunkCollector chunkCollector, int i) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		String collapsedText = "to " + terminals.get(i+1).getTerminalsText();
		if(terminals.size() > i+2) {
			IParseTree possibleUnit = terminals.get(i+2);
			String possibleUnitText = possibleUnit.getTerminalsText().replaceAll("\\W", " ").trim();
			if(possibleUnitText.matches(this.units)) {
				collapsedText += " " + possibleUnitText;
			}
		}

		IParseTree collapseRoot = terminals.get(i).getAncestor(2, chunkCollector.getParseTree());
		IParseTree posTree = parseTreeFactory.create();
		collapseRoot.setPOS(POS.COLLAPSED_TO);
		collapseRoot.addChild(posTree);
		IParseTree terminalTree = parseTreeFactory.create();
		terminalTree.setTerminalsText(collapsedText);
		posTree.addChild(terminalTree);
	}
}
