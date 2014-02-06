package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;



import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

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
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk,
			ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		ProcessingContextState contextState = processingContext.getCurrentState();
		List<Element> lastElements = contextState.getLastElements();
		
		//for(DescriptionTreatmentElement lastElement : lastElements) {
		if(!lastElements.isEmpty()) {
			Element lastElement = lastElements.get(0);
			Structure structure = null;
			if(lastElement.isCharacter()) {
				structure = processingContext.getParentStructure((Character)lastElements.get(0));
			} else if(lastElement.isStructure()) {
				structure = (Structure)lastElement;
			}
			if(structure!=null) {
				Structure constraintStructure = new Structure();
				int structureId = processingContext.fetchAndIncrementStructureId(constraintStructure);
				constraintStructure.setId("o" + structureId);
				constraintStructure.setName(structure.getName());
				constraintStructure.setConstraint(chunk.getTerminalsText());
				result.add(constraintStructure);
				contextState.setLastElements(result);
			}
		}
		contextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}
}
