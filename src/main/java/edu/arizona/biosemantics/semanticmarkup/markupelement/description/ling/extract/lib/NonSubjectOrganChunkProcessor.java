package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
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
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * NonSubjectOrganChunkProcessor processes chunks of ChunkType.NON_SUBJECT_ORGAN
 * @author rodenhausen
 */
public class NonSubjectOrganChunkProcessor extends AbstractChunkProcessor {

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
	public NonSubjectOrganChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<BiologicalEntity> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<BiologicalEntity> result = new ArrayList<BiologicalEntity>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		
		ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
		Chunk nextChunk = null;
		if(chunkListIterator.hasNext()){
			nextChunk = chunkListIterator.next();
			chunkListIterator.previous();
		}
		
		boolean isConstraintOrgan = false;
		Chunk previousChunk1 = null; //:
		Chunk previousChunk2 = null; //PP: [PREPOSITION: [CHARACTER_STATE: characterName->character; [STATE: [length]], of], OBJECT: [ORGAN: [tibia/metatarsus]]]
		if(chunkListIterator.hasPrevious()){
			chunkListIterator.previous();
			if(chunkListIterator.hasPrevious()){
				previousChunk1 = chunkListIterator.previous();
				if(chunkListIterator.hasPrevious()){
					previousChunk2 = chunkListIterator.previous();
					chunkListIterator.next();
				}
				chunkListIterator.next();
			}
			chunkListIterator.next();
		}
		
		Chunk character = null;
		if(previousChunk2 !=null && previousChunk2.getChunkType().equals(ChunkType.PP)){
			character = previousChunk2.getChildChunk(ChunkType.PREPOSITION).getChildChunk(ChunkType.CHARACTER_STATE);
		}else if(previousChunk2 !=null && previousChunk2.getChunkType().equals(ChunkType.CHARACTER_STATE)){
			character = previousChunk2;
		}
		
		if(previousChunk1 !=null &&  character!=null &&  (previousChunk1.getTerminalsText().equals(":") || previousChunk1.getChunkType().equals(ChunkType.COMMA) && 
				character.getProperty("characterName").compareTo("character")==0 )){
			isConstraintOrgan = true;
		}
		
		boolean commaAndOrEosEolAfterLastElements = nextChunk==null || nextChunk.isOfChunkType(ChunkType.COMMA) || nextChunk.isOfChunkType(ChunkType.END_OF_LINE)||nextChunk.isOfChunkType(ChunkType.END_OF_SUBCLAUSE);
		
		if(isConstraintOrgan && processingContextState.getUnassignedCharacter()!=null && processingContextState.getLastElements().getLast().isStructure() && ! processingContext.getLastChunkYieldElement()){ //length of tibia: leg I, 7.0 mm, current chunk = leg
			//add the organ of this chunk as constraint to the last elements
			List<Element> elements = processingContextState.getLastElements();
			for(Element element: elements){
				((BiologicalEntity) element).appendConstraint(chunk.getTerminalsText());
			}
		}else if(commaAndOrEosEolAfterLastElements){
			//LinkedList<Element> lastElements = processingContextState.getLastElements();
			ArrayList<Chunk> chunks = new ArrayList<Chunk>();
			chunks.add(chunk);
			List<BiologicalEntity> structures = this.createStructureElements(chunks, processingContext, processingContextState);
			result.addAll(structures);
			//Element lastElement = lastElements.isEmpty() ? null : lastElements.getLast();
			//if(lastElement != null && lastElement.isOfDescriptionType(DescriptionTreatmentElementType.STRUCTURE))
			//annotateType(chunk, lastElement);
			//else 
			processingContextState.setLastElements(structures);
		}else{
			result = this.establishSubject(chunk, processingContext, processingContextState);
		}
		processingContextState.setCommaAndOrEosEolAfterLastElements(commaAndOrEosEolAfterLastElements);
		return result;
	}

}
