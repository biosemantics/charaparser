package semanticMarkup.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElementType;
import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * CommaChunkProcessor processes chunks of ChunkType.COMMA
 * @author rodenhausen
 */
public class CommaChunkProcessor extends AbstractChunkProcessor {

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
	 * @param attachToLast
	 * @param times
	 */
	@Inject
	public CommaChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("AttachToLast")boolean attachToLast, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, attachToLast, times);
	}

	@Override
	protected List<DescriptionTreatmentElement> processChunk(Chunk chunk,
			ProcessingContext processingContext) {
		List<DescriptionTreatmentElement> result = new ArrayList<DescriptionTreatmentElement>();
		
		processingContext.getCurrentState().clearUnassignedModifiers();
		
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		processingContextState.setCommaAndOrEosEolAfterLastElements(true);
		if(!processingContextState.getLastElements().isEmpty()) {
			DescriptionTreatmentElement lastElement = processingContextState.getLastElements().get(0);
			if(lastElement.isOfDescriptionType(DescriptionTreatmentElementType.CHARACTER)) {
				DescriptionTreatmentElement parent = processingContext.getParent(lastElement);
				if(parent!=null) {
					LinkedList<DescriptionTreatmentElement> newLastElements = new LinkedList<DescriptionTreatmentElement>();
					newLastElements.add(parent);
					processingContextState.setLastElements(newLastElements);
				}
			}
		}
		
		List<DescriptionTreatmentElement> unassignedCharacters = processingContextState.getUnassignedCharacters();
		if(!unassignedCharacters.isEmpty() && nextChunkIsOrgan(processingContext)) {
			DescriptionTreatmentElement structureElement = new DescriptionTreatmentElement(DescriptionTreatmentElementType.STRUCTURE);
			int structureIdString = processingContextState.fetchAndIncrementStructureId(structureElement);
			structureElement.setAttribute("id", "o" + String.valueOf(structureIdString));	
			structureElement.setAttribute("name", "whole_organism"); 
			LinkedList<DescriptionTreatmentElement> structureElements = new LinkedList<DescriptionTreatmentElement>();
			structureElements.add(structureElement);
			result.addAll(establishSubject(structureElements, processingContextState));
			
			for(DescriptionTreatmentElement character : unassignedCharacters) {
				for(DescriptionTreatmentElement parent : structureElements) {
					parent.addTreatmentElement(character);
				}
			}
			unassignedCharacters.clear(); 
		}
		
		return result;
	}
	
	private boolean nextChunkIsOrgan(ProcessingContext processingContext) {
		ListIterator<Chunk> iterator = processingContext.getChunkListIterator();
		Chunk nextChunk = iterator.next();
		int i=1;
		while(nextChunk.isOfChunkType(ChunkType.UNASSIGNED)) {
			if(iterator.hasNext()) {
				nextChunk = iterator.next();
				i++;
			} else
				return false;
		}
		
		for(int j=0; j<i; j++) 
			iterator.previous();
		
		return processingContext.getChunkCollector().isPartOfChunkType(nextChunk.getTerminals().get(0), ChunkType.ORGAN) || 
				processingContext.getChunkCollector().isPartOfChunkType(nextChunk.getTerminals().get(0), ChunkType.CONSTRAINT);
	}
}
