package semanticMarkup.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElementType;
import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

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
	protected List<DescriptionTreatmentElement> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<DescriptionTreatmentElement> result = new LinkedList<DescriptionTreatmentElement>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		String content = chunk.getTerminalsText();
		LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
		if(!lastElements.isEmpty() && lastElements.getLast().isOfDescriptionType(DescriptionTreatmentElementType.CHARACTER)) {
			DescriptionTreatmentElement lastElement = lastElements.getLast();
			lastElement.setAttribute("modifier", content);
			result.add(lastElement);
		} else {
			processingContextState.getUnassignedModifiers().add(chunk);
		}	
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}

}
