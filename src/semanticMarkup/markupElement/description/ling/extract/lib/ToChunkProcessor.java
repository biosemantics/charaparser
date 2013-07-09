package semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.IChunkProcessor;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.markupElement.description.ling.extract.AbstractChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContextState;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.model.Structure;
import semanticMarkup.model.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * ToChunkProcessor processes chunks of ChunkType.TO
 * @author rodenhausen
 */
public class ToChunkProcessor extends AbstractChunkProcessor {

	/**
	 * @param inflector
	 * @param glossary
	 * @param terminologyLearner
	 * @param characterKnowledgeBase
	 * @param posKnowledgeBase
	 * @param baseCountWords
	 * @param locationPrepositions
	 * @param clusters
	 * @param units
	 * @param equalCharacters
	 * @param numberPattern
	 * @param times
	 */
	@Inject
	public ToChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase,
				posKnowledgeBase, baseCountWords, locationPrepositions, clusters,
				units, equalCharacters, numberPattern, times);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk,
			ProcessingContext processingContext) {
		List<Element> results = new LinkedList<Element>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		List<Structure> parents = lastStructures(processingContext, processingContextState);
		List<Chunk> modifiers = new LinkedList<Chunk>();
		List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
		modifiers.addAll(unassignedModifiers);
		
		String characterName = null;
		if(processingContextState.getUnassignedCharacter()!=null) {
			characterName = processingContextState.getUnassignedCharacter();
		}
				
		//process characters recursively with the appropriate processor
		List<Chunk> characterStateChunks = chunk.getChunks(ChunkType.CHARACTER_STATE);
		if(!characterStateChunks.isEmpty()) {
			for(Chunk characterStateChunk : characterStateChunks) {
				processingContextState.setUnassignedCharacter(characterName);
				processingContextState.setLastElements(parents);
				IChunkProcessor processor = processingContext.getChunkProcessor(ChunkType.CHARACTER_STATE);
				results.addAll(processor.process(characterStateChunk, processingContext));
				processingContextState = processingContext.getCurrentState();
			}
			
			//and create a range charater too
			if(characterName == null)
				characterName = characterStateChunks.get(0).getProperty("characterName");
			
			String character = chunk.getTerminalsText();
			results.addAll(createRangeCharacterElement(parents, modifiers, character, characterName, processingContextState));
			
		}
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		processingContextState.setUnassignedCharacter(null);
		processingContextState.setLastElements(results);
		
		return new LinkedList<Element>();
	}
}
