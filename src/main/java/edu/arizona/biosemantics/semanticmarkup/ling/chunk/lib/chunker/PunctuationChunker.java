package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * PunctuationChunker chunks by handling punctuation terminals
 * @author rodenhausen
 */
public class PunctuationChunker extends AbstractChunker {

	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 * @param organStateKnowledgeBase
	 */
	@Inject
	public PunctuationChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			 ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, terminologyLearner,
				inflector,  learnedCharacterKnowledgeBase);
	}
	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		boolean lastWasMainSubjectOrgan = false;
		for(int i=terminals.size()-1; i>=0; i--) {
			AbstractParseTree terminal = terminals.get(i);
			if(chunkCollector.isOfChunkType(terminal, ChunkType.MAIN_SUBJECT_ORGAN)) {
				lastWasMainSubjectOrgan = true;
				continue;
			}
			if(!chunkCollector.isPartOfANonTerminalChunk(terminal)) {
				//if(terminal.getTerminalsText().matches("\\.")) {
				//	chunkCollector.addChunk(new Chunk(ChunkType.END_OF_SUBCLAUSE, terminal));
				//}
				if(terminal.getTerminalsText().equals(";") || terminal.getTerminalsText().equals(".") || terminal.getTerminalsText().equals(",")) {
					if(i==terminals.size()-1) 
						chunkCollector.addChunk(new Chunk(ChunkType.END_OF_LINE, terminal));
					else if(lastWasMainSubjectOrgan && (terminal.getTerminalsText().equals(",")||terminal.getTerminalsText().equals(";"))) //'.' in the middle of a sentence is not a period.
						chunkCollector.addChunk(new Chunk(ChunkType.END_OF_SUBCLAUSE, terminal));
					else if(terminal.getTerminalsText().equals(","))
						chunkCollector.addChunk(new Chunk(ChunkType.COMMA, terminal));
				}
			}

			lastWasMainSubjectOrgan = false;
		}
	}
}
