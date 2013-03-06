package semanticMarkup.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionType;
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

public class CommaChunkProcessor extends AbstractChunkProcessor {

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
			if(lastElement.isOfDescriptionType(DescriptionType.CHARACTER)) {
				DescriptionTreatmentElement parent = processingContext.getParent(lastElement);
				if(parent!=null) {
					LinkedList<DescriptionTreatmentElement> newLastElements = new LinkedList<DescriptionTreatmentElement>();
					newLastElements.add(parent);
					processingContextState.setLastElements(newLastElements);
				}
			}
		}
		
		List<DescriptionTreatmentElement> unassignedCharacters = processingContextState.getUnassignedCharacters();
		if(!unassignedCharacters.isEmpty()) {
			DescriptionTreatmentElement structureElement = new DescriptionTreatmentElement(DescriptionType.STRUCTURE);
			int structureIdString = processingContextState.fetchAndIncrementStructureId(structureElement);
			structureElement.setProperty("id", "o" + String.valueOf(structureIdString));	
			structureElement.setProperty("name", "whole_organism"); 
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
		
		return result;
	}
	
	@Override
	public String getDescription() {
		return "comma chunk processor";
	}

}
