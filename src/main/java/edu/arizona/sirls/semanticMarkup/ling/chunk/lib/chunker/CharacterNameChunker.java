package edu.arizona.sirls.semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.List;
import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.know.ICharacterKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.know.IGlossary;
import edu.arizona.sirls.semanticMarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.ling.chunk.AbstractChunker;
import edu.arizona.sirls.semanticMarkup.ling.chunk.Chunk;
import edu.arizona.sirls.semanticMarkup.ling.chunk.ChunkCollector;
import edu.arizona.sirls.semanticMarkup.ling.chunk.ChunkType;
import edu.arizona.sirls.semanticMarkup.ling.parse.AbstractParseTree;
import edu.arizona.sirls.semanticMarkup.ling.parse.IParseTreeFactory;
import edu.arizona.sirls.semanticMarkup.ling.transform.IInflector;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;

/**
 * AndChunker chunks by handling character name describing terminals
 * @author rodenhausen
 */
public class CharacterNameChunker extends AbstractChunker {

	private ICharacterKnowledgeBase characterKnowledgeBase;

	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 * @param characterKnowledgeBase
	 */
	@Inject
	public CharacterNameChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			ICharacterKnowledgeBase characterKnowledgeBase, 
			IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, 
				terminologyLearner, inflector, organStateKnowledgeBase);
		this.characterKnowledgeBase = characterKnowledgeBase;
	}

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		for(AbstractParseTree terminal : terminals) {
			if(!chunkCollector.isPartOfANonTerminalChunk(terminal)) {
				if(characterKnowledgeBase.containsCharacterName(terminal.getTerminalsText())) {
					chunkCollector.addChunk(new Chunk(ChunkType.CHARACTER_NAME, terminal));
				}
			}
		}
	}
}
