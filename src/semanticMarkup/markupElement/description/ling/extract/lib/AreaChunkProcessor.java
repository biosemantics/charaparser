package semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.markupElement.description.ling.extract.AbstractChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContextState;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.model.Character;
import semanticMarkup.markupElement.description.model.Structure;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * AreaChunkProcessor processes chunks of ChunkType.AREA
 * @author rodenhausen
 */
public class AreaChunkProcessor extends AbstractChunkProcessor {
	
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
	public AreaChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Character> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		List<Structure> parents = lastStructures(processingContext, processingContextState);
		List<Chunk> modifiers = new ArrayList<Chunk>();
		List<Character> characters = annotateNumericals(chunk.getTerminalsText(), "area", modifiers, 
				parents, false, processingContextState);
		processingContextState.getLastElements().addAll(characters);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return characters;
	}


}
