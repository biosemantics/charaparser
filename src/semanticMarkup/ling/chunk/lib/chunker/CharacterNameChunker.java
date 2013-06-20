package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTreeFactory;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.log.LogLevel;

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
