package semanticMarkup.ling.extract.lib;

import java.util.ArrayList;
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
import semanticMarkup.ling.extract.ILastChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * EosEolChunkProcessor processes chunks of ChunkType.END_OF_LINE or ChunkType.END_OF_SUBCLAUSE
 * @author rodenhausen
 */
public class EosEolChunkProcessor extends AbstractChunkProcessor implements ILastChunkProcessor {

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
	public EosEolChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("AttachToLast")boolean attachToLast, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, attachToLast, times);
	}

	@Override
	protected List<DescriptionTreatmentElement> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<DescriptionTreatmentElement> result = new ArrayList<DescriptionTreatmentElement>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		processingContextState.setCommaAndOrEosEolAfterLastElements(true);
		String modifierString = "";
		List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
		for(Chunk modifier : unassignedModifiers)
			modifierString += modifier.getTerminalsText() + " ";
		
		if(!unassignedModifiers.isEmpty()) {
			LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
			if(!lastElements.isEmpty() && lastElements.getLast().isOfDescriptionType(DescriptionTreatmentElementType.STRUCTURE)) {
				for(DescriptionTreatmentElement element : lastElements) {
					int structureId = Integer.valueOf(element.getAttribute("id").substring(1));
					
					Set<DescriptionTreatmentElement> relations = processingContextState.getRelationsTo(structureId);
					int greatestId = 0;
					DescriptionTreatmentElement latestRelation = null;
					for(DescriptionTreatmentElement relation : relations) {
						int id = Integer.valueOf(relation.getAttribute("id").substring(1));
						if(id > greatestId) {
							greatestId = id;
							latestRelation = relation;
						}
					}
					
					if(latestRelation != null) {
						latestRelation.appendAttribute("modifier", modifierString);
						result.add(latestRelation);
					}
					//TODO: otherwise, categorize modifier and create a character for the structure e.g.{thin} {dorsal} {median} <septum> {centrally} only ;
				}
				
			} else if(!lastElements.isEmpty() && lastElements.getLast().isOfDescriptionType(DescriptionTreatmentElementType.CHARACTER)) {
				for(DescriptionTreatmentElement element : lastElements) {
					element.appendAttribute("modifier", modifierString);
					result.add(element);
				}
			}
		}
		
		List<DescriptionTreatmentElement> unassignedCharacters = processingContextState.getUnassignedCharacters();
		if(!unassignedCharacters.isEmpty()) {
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
		}
		unassignedCharacters.clear();
		
		processingContextState.clearUnassignedModifiers();
		
		return result;
	}

	@Override
	public List<DescriptionTreatmentElement> process(ProcessingContext processingContext) {
		return this.process(null, processingContext);
	}
}
