package semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
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
import semanticMarkup.model.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * ValuePercentageOrDegreeChunkProcessor processes chunks of ChunkType.VALUE_PERCENTAGE or ChunkType.VALUE_DEGREE
 * @author rodenhausen
 */
public class ValuePercentageOrDegreeChunkProcessor extends AbstractChunkProcessor {

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
	public ValuePercentageOrDegreeChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		String content = chunk.getTerminalsText();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		if(!lastElements.isEmpty() && lastElements.getLast().isCharacter()) {
			Character lastElement = (Character)lastElements.getLast();
			lastElement.setModifier(content);
			result.add(lastElement);
		} else {
			processingContextState.getUnassignedModifiers().add(chunk);
		}	
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}

}
