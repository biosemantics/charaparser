package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.know.ICharacterKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.know.IGlossary;
import edu.arizona.sirls.semanticMarkup.know.IPOSKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.ling.chunk.Chunk;
import edu.arizona.sirls.semanticMarkup.ling.transform.IInflector;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.ProcessingContextState;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Character;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Structure;

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
