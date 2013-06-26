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
 * ConstraintChunkProcessor processes chunks of ChunkType.CONSTRAINT
 * @author rodenhausen
 */
public class ConstraintChunkProcessor extends AbstractChunkProcessor {

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
	public ConstraintChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
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
			if(lastElement.isOfDescriptionType(DescriptionTreatmentElementType.CHARACTER)) {
				structure = processingContext.getParent(lastElements.get(0));
			} else if(lastElement.isOfDescriptionType(DescriptionTreatmentElementType.STRUCTURE)) {
				structure = lastElement;
			}
			if(structure!=null) {
				DescriptionTreatmentElement constraintStructure = 
						new DescriptionTreatmentElement(DescriptionTreatmentElementType.STRUCTURE);
				int structureId = contextState.fetchAndIncrementStructureId(constraintStructure);
				constraintStructure.setAttribute("id", "o" + structureId);
				constraintStructure.setAttribute("name", structure.getAttribute("name"));
				constraintStructure.setAttribute("constraint", chunk.getTerminalsText());
				result.add(constraintStructure);
				contextState.setLastElements(result);
			}
		}
		contextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}
}
