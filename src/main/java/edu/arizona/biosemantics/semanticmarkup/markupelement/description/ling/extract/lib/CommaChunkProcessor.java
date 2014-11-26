package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;




import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

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
	 * @param times
	 */
	@Inject
	public CommaChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk,
			ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		
		//processingContext.getCurrentState().clearUnassignedModifiers(); //hong 5/12/2014 "When young, leaflets purple."
		
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		processingContextState.setCommaAndOrEosEolAfterLastElements(true);
		if(!processingContextState.getLastElements().isEmpty()) {
			Element lastElement = processingContextState.getLastElements().get(0);
			if(lastElement.isCharacter()) {
				BiologicalEntity parent = processingContext.getParentStructure((Character)lastElement);
				if(parent!=null) {
					List<Element> newLastElements = new LinkedList<Element>();
					newLastElements.add(parent);
					processingContextState.setLastElements(newLastElements);
				}
			}
		}
		
		List<Character> unassignedCharacters = processingContextState.getUnassignedCharacters();
		if(!unassignedCharacters.isEmpty() && nextChunkIsOrgan(processingContext)) {
			BiologicalEntity structureElement = new BiologicalEntity();
			int structureIdString = processingContext.fetchAndIncrementStructureId(structureElement);
			structureElement.setId("o" + String.valueOf(structureIdString));	
			structureElement.setName("whole_organism"); 
			structureElement.setNameOriginal("");
			structureElement.setType("structure");
			List<BiologicalEntity> structureElements = new LinkedList<BiologicalEntity>();
			structureElements.add(structureElement);
			result.addAll(establishSubject(structureElements, processingContextState));
			
			for(Character character : unassignedCharacters) {
				for(BiologicalEntity parent : structureElements) {
					parent.addCharacter(character);
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
