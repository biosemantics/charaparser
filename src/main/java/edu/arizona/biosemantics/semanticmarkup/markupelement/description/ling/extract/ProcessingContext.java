package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessorProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.model.Element;


/**
 * ProcessingContext provides contextual information e.g. chunkListIterator, chunkCollector, ... 
 * and stores ProcessingContextStates at each processing step of a chunk
 * @author rodenhausen
 */
public class ProcessingContext {

	private IChunkProcessorProvider chunkProcessorProvider;
	private List<Element> result;
	private ListIterator<Chunk> chunkListIterator;
	private ChunkCollector chunkCollector;
	private ProcessingContextState currentState = new ProcessingContextState();
	
	/** these can't be reset for each new statement, Ids have to be unique over the whole xml schema. Also references by relations can be accross statements **/
	private int structureId;
	private HashMap<Integer, Structure> structures = new HashMap<Integer, Structure>();
	private int relationId;
	private HashMap<Integer, Relation> relations = new HashMap<Integer, Relation>();
	private HashMap<Integer, Set<Relation>> relationsFromStructure = new HashMap<Integer, Set<Relation>>();
	private HashMap<Integer, Set<Relation>> relationsToStructure = new HashMap<Integer, Set<Relation>>();
	
	public ProcessingContext(int structureId, int relationId) {
		this.structureId = structureId;
		this.relationId = relationId;
	}
	
	/**
	 * @return the current processingContextState
	 */
	public ProcessingContextState getCurrentState() {
		return currentState;
	}
	
	/**
	 * set the current ProcessingContextState to the state of a previously seen chunk
	 * @param previousChunk
	 */
	public void setCurrentState(Chunk previousChunk) {
		this.currentState = states.get(previousChunk);
	}

	/**
	 * set the current ProcessingContextState
	 * @param currentState
	 */
	public void setCurrentState(ProcessingContextState currentState) {
		this.currentState = currentState;
	}

	private HashMap<Chunk, ProcessingContextState> states = new HashMap<Chunk, ProcessingContextState>();

	/**
	 * @param chunk
	 * @return the ProcessingContextState of a previously seen chunk
	 */
	public ProcessingContextState getState(Chunk chunk) {
		return states.get(chunk);
	}

	/**
	 * @param chunk
	 * @return if a ProcessingContextState of a chunk is available
	 */
	public boolean containsState(Chunk chunk) {
		return states.containsKey(chunk);
	}

	/**
	 * @param chunk
	 * @param processingContextState to add for the chunk
	 */
	public void addState(Chunk chunk, ProcessingContextState processingContextState) {
		states.put(chunk, processingContextState);
	}
	
	/**
	 * @return the chunkCollector
	 */
	public ChunkCollector getChunkCollector() {
		return chunkCollector;
	}

	/**
	 * @return the chunkListIterator
	 */
	public ListIterator<Chunk> getChunkListIterator() {
		return chunkListIterator;
	}

	/**
	 * @param chunkCollector to set
	 */
	public void setChunkCollector(ChunkCollector chunkCollector) {
		this.chunkCollector = chunkCollector;
	}

	/**
	 * @param chunkListIterator to set
	 */
	public void setChunkListIterator(ListIterator<Chunk> chunkListIterator) {
		this.chunkListIterator = chunkListIterator;
	}
	
	/**
	 * @param chunkType
	 * @return the IChunkProcessor for the chunkType
	 */
	public IChunkProcessor getChunkProcessor(ChunkType chunkType) {
		return this.chunkProcessorProvider.getChunkProcessor(chunkType);
	}
	
	/**
	 * @param chunkProcessorProvider to set
	 */
	public void setChunkProcessorsProvider(IChunkProcessorProvider chunkProcessorProvider) {
		this.chunkProcessorProvider = chunkProcessorProvider;
	}

	/**
	 * @param result to set
	 */
	public void setResult(List<Element> result) {
		this.result = result;
	}
	
	/**
	 * @return result
	 */
	public List<Element> getResult() {
		return this.result;
	}

	/**
	 * @return the last DescriptionTreatmentElement of the result
	 */
	public Element getLastResult() {
		return result.get(result.size()-1);
	}

	/**
	 * @param descriptionTreatmentElementType
	 * @return the last DescriptionTreatmentElement of descriptionTreatmentElementType of the result
	 */
	public Element getLastResult(Class<? extends Element> elementType) {
		Element result = null;
		for(int i=this.result.size()-1; i>=0; i--) {
			Element element = this.result.get(i);
			if(element.isOfType(elementType)) {
				result = element;
				break;
			}
		}
		return result;
	}
	
	/**
	 * @param descriptionTreatmentElement
	 * @return the parent DescriptionTreatmentElement of the descriptionTreatmentElement given within the result 
	 * or null if none exists
	 */
	///at creation time it should be possible to assign them their parent?
	public Structure getParentStructure(Character character) {
		for(Element element : result) {
			if(element.isStructure()) {
				Structure structure = (Structure)element;
				if(structure.getCharacters().contains(character)) {
					return structure;
				}
			}
		}
		return null;
	}

	/**
	 * Reset the current ProcessingContextState
	 */
	public void reset() {
		 currentState = new ProcessingContextState();
	}
	
	/**
	 * @param structureId
	 * @return the structure with the structureId
	 */
	public Structure getStructure(int structureId) {
		return structures.get(structureId);
	}
	
	/**
	 * @param relationId
	 * @return the relation with the relationId
	 */
	public Relation getRelation(int relationId) {
		return relations.get(relationId);
	}
	
	/**
	 * @param toStructureId
	 * @return the set of relations that use toStructureId has target
	 */
	public Set<Relation> getRelationsTo(int toStructureId) {
		Set<Relation> result = new HashSet<Relation>();
		if(relationsToStructure.containsKey(toStructureId))
			return relationsToStructure.get(toStructureId);
		return result;
	}
	
	/**
	 * @param fromStructureId
	 * @return the set of relations that use fromStructureId as source
	 */
	public Set<Relation> getRelationsFrom(int fromStructureId) {
		Set<Relation> result = new HashSet<Relation>();
		if(relationsFromStructure.containsKey(fromStructureId))
			return relationsFromStructure.get(fromStructureId);
		return result;
	}
	
	/**
	 * @return the current relationId
	 */
	public int getRelationId() {
		return relationId;
	}

	/**
	 * @param relation
	 * @return returns and increases the current relationId
	 */
	public int fetchAndIncrementRelationId(Relation relation) {
		relations.put(relationId, relation);
		int fromId = Integer.parseInt(relation.getFrom().substring(1));
		int toId = Integer.parseInt(relation.getTo().substring(1));
		
		if(!relationsFromStructure.containsKey(fromId))
			relationsFromStructure.put(fromId, new HashSet<Relation>());
		if(!relationsToStructure.containsKey(toId))
			relationsToStructure.put(toId, new HashSet<Relation>());
		relationsFromStructure.get(fromId).add(relation);
		relationsToStructure.get(toId).add(relation);
		
		return relationId++;
	}

	/**
	 * @param relationId to set
	 */
	public void setRelationId(int relationId) {
		this.relationId = relationId;
	}
	
	/**
	 * @return the current structure id
	 */
	public int getStructureId() {
		return structureId;
	}
	
	/**
	 * @param structureId to set
	 */
	public void setStructureId(int structureId) {
		this.structureId = structureId;
	}
	
	/**
	 * @param structure
	 * @return and increase the current structure id
	 */
	public int fetchAndIncrementStructureId(Structure structure) {
		structures.put(structureId, structure);
		return structureId++;
	}
}
