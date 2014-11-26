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
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * CountChunkProcessor processes chunks of ChunkType.COUNT
 * @author rodenhausen
 */
public class CountChunkProcessor extends AbstractChunkProcessor {

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
	public CountChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Character> processChunk(Chunk chunk, ProcessingContext processingContext) {
		LinkedList<Character> result = new LinkedList<Character>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		List<Chunk> modifiers = processingContextState.getUnassignedModifiers();
		
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		LinkedList<BiologicalEntity> subjects = processingContextState.getSubjects();
		LinkedList<BiologicalEntity> parents = new LinkedList<BiologicalEntity>();
		if(lastElements.isEmpty() || !lastElements.getLast().isStructure()) {
			if(!subjects.isEmpty())
				parents.add(subjects.getLast());
		} else {
			parents.add((BiologicalEntity)lastElements.getLast()); 
		}
		
		if(!parents.isEmpty() && parents.getLast().isStructure()) {
			List<Character> characterElement = 
					this.annotateNumericals(chunk.getTerminalsText(), "count", modifiers, parents, false, processingContextState);
			//DescriptionTreatmentElement characterElement = createCharacterElement(parents, modifiers, chunk.getTerminalsText(), 
			//		"count", "", processingContextState);
			if(characterElement!=null)
				result.addAll(characterElement);
			processingContextState.clearUnassignedModifiers();
		} else {
			List<Character> characterElement = 
					this.annotateNumericals(chunk.getTerminalsText(), "count", modifiers, parents, false, processingContextState);
			//DescriptionTreatmentElement characterElement = createCharacterElement(parents, modifiers, chunk.getTerminalsText(), 
			//		"count", "", processingContextState);
			processingContextState.getUnassignedCharacters().addAll(characterElement);
		}
		
		processingContextState.setLastElements(result);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}

}
