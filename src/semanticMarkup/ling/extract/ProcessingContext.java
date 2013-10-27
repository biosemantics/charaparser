package semanticMarkup.ling.extract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElementType;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;

/**
 * ProcessingContext provides contextual information e.g. chunkListIterator, chunkCollector, ... 
 * and stores ProcessingContextStates at each processing step of a chunk
 * @author rodenhausen
 */
public class ProcessingContext {

	private IChunkProcessorProvider chunkProcessorProvider;
	private List<DescriptionTreatmentElement> result;
	private ListIterator<Chunk> chunkListIterator;
	private ChunkCollector chunkCollector;
	private ProcessingContextState currentState = new ProcessingContextState();
	
	/** these can't be reset for each new statement, Ids have to be unique over the whole xml schema. Also references by relations can be accross statements **/
	private int structureId;
	private HashMap<Integer, DescriptionTreatmentElement> structures = new HashMap<Integer, DescriptionTreatmentElement>();
	private int relationId;
	private HashMap<Integer, DescriptionTreatmentElement> relations = new HashMap<Integer, DescriptionTreatmentElement>();
	private HashMap<Integer, Set<DescriptionTreatmentElement>> relationsFromStructure = new HashMap<Integer, Set<DescriptionTreatmentElement>>();
	private HashMap<Integer, Set<DescriptionTreatmentElement>> relationsToStructure = new HashMap<Integer, Set<DescriptionTreatmentElement>>();
	
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
	public void setResult(List<DescriptionTreatmentElement> result) {
		this.result = result;
	}
	
	/**
	 * @return result
	 */
	public List<DescriptionTreatmentElement> getResult() {
		return this.result;
	}

	/**
	 * @return the last DescriptionTreatmentElement of the result
	 */
	public DescriptionTreatmentElement getLastResult() {
		return result.get(result.size()-1);
	}

	/**
	 * @param descriptionTreatmentElementType
	 * @return the last DescriptionTreatmentElement of descriptionTreatmentElementType of the result
	 */
	public DescriptionTreatmentElement getLastResult(DescriptionTreatmentElementType descriptionTreatmentElementType) {
		DescriptionTreatmentElement result = null;
		for(int i=this.result.size()-1; i>=0; i--) {
			DescriptionTreatmentElement element = this.result.get(i);
			if(element.isOfDescriptionType(descriptionTreatmentElementType)) {
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
	public DescriptionTreatmentElement getParent(DescriptionTreatmentElement descriptionTreatmentElement) {
		for(DescriptionTreatmentElement resultElement : result) {
			TreatmentElement parent = resultElement.getParent(descriptionTreatmentElement);
			if(parent!=null && parent instanceof DescriptionTreatmentElement)
				return (DescriptionTreatmentElement)parent;
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
	public DescriptionTreatmentElement getStructure(int structureId) {
		return structures.get(structureId);
	}
	
	/**
	 * @param relationId
	 * @return the relation with the relationId
	 */
	public DescriptionTreatmentElement getRelation(int relationId) {
		return relations.get(relationId);
	}
	
	/**
	 * @param toStructureId
	 * @return the set of relations that use toStructureId has target
	 */
	public Set<DescriptionTreatmentElement> getRelationsTo(int toStructureId) {
		Set<DescriptionTreatmentElement> result = new HashSet<DescriptionTreatmentElement>();
		if(relationsToStructure.containsKey(toStructureId))
			return relationsToStructure.get(toStructureId);
		return result;
	}
	
	/**
	 * @param fromStructureId
	 * @return the set of relations that use fromStructureId as source
	 */
	public Set<DescriptionTreatmentElement> getRelationsFrom(int fromStructureId) {
		Set<DescriptionTreatmentElement> result = new HashSet<DescriptionTreatmentElement>();
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
	public int fetchAndIncrementRelationId(DescriptionTreatmentElement relation) {
		relations.put(relationId, relation);
		int fromId = Integer.parseInt(relation.getAttribute("from").substring(1));
		int toId = Integer.parseInt(relation.getAttribute("to").substring(1));
		
		if(!relationsFromStructure.containsKey(fromId))
			relationsFromStructure.put(fromId, new HashSet<DescriptionTreatmentElement>());
		if(!relationsToStructure.containsKey(toId))
			relationsToStructure.put(toId, new HashSet<DescriptionTreatmentElement>());
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
	public int fetchAndIncrementStructureId(DescriptionTreatmentElement structure) {
		structures.put(structureId, structure);
		return structureId++;
	}
}
