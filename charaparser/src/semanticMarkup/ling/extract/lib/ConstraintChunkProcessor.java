package semanticMarkup.ling.extract.lib;

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

public class ConstraintChunkProcessor extends AbstractChunkProcessor {

	@Inject
	public ConstraintChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
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
		LinkedList<DescriptionTreatmentElement> result = new LinkedList<DescriptionTreatmentElement>();
		ProcessingContextState contextState = processingContext.getCurrentState();
		List<DescriptionTreatmentElement> lastElements = contextState.getLastElements();
		
		//for(DescriptionTreatmentElement lastElement : lastElements) {
		if(!lastElements.isEmpty()) {
			DescriptionTreatmentElement lastElement = lastElements.get(0);
			DescriptionTreatmentElement structure = null;
			if(lastElement.isOfDescriptionType(DescriptionType.CHARACTER)) {
				structure = processingContext.getParent(lastElements.get(0));
			} else if(lastElement.isOfDescriptionType(DescriptionType.STRUCTURE)) {
				structure = lastElement;
			}
			if(structure!=null) {
				DescriptionTreatmentElement constraintStructure = 
						new DescriptionTreatmentElement(DescriptionType.STRUCTURE);
				int structureId = contextState.fetchAndIncrementStructureId(constraintStructure);
				constraintStructure.setProperty("id", "o" + structureId);
				constraintStructure.setProperty("name", structure.getProperty("name"));
				constraintStructure.setProperty("constraint", chunk.getTerminalsText());
				result.add(constraintStructure);
				contextState.setLastElements(result);
			}
		}
		contextState.setCommaEosEolAfterLastElements(false);
		return result;
	}
}
