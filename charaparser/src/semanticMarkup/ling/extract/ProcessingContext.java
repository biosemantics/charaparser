package semanticMarkup.ling.extract;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionType;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;

public class ProcessingContext {

	private IChunkProcessorProvider chunkProcessorProvider;
	private List<DescriptionTreatmentElement> result;
	private ListIterator<Chunk> chunkListIterator;
	private ChunkCollector chunkCollector;
	private ProcessingContextState currentState = new ProcessingContextState();
	
	public ProcessingContextState getCurrentState() {
		return currentState;
	}
	
	public void setCurrentState(Chunk previousChunk) {
		this.currentState = states.get(previousChunk);
	}

	public void setCurrentState(ProcessingContextState currentState) {
		this.currentState = currentState;
	}

	private HashMap<Chunk, ProcessingContextState> states = new HashMap<Chunk, ProcessingContextState>();

	public ProcessingContextState getState(Chunk chunk) {
		return states.get(chunk);
	}

	public boolean containsState(Chunk chunk) {
		return states.containsKey(chunk);
	}

	public ProcessingContextState addState(Chunk chunk, ProcessingContextState processingContextState) {
		return states.put(chunk, processingContextState);
	}
	
	public ChunkCollector getChunkCollector() {
		return chunkCollector;
	}

	public ListIterator<Chunk> getChunkListIterator() {
		return chunkListIterator;
	}

	public void setChunkCollector(ChunkCollector chunkCollector) {
		this.chunkCollector = chunkCollector;
	}

	public void setChunkListIterator(ListIterator<Chunk> chunkListIterator) {
		this.chunkListIterator = chunkListIterator;
	}
	
	public IChunkProcessor getChunkProcessor(ChunkType chunkType) {
		return this.chunkProcessorProvider.getChunkProcessor(chunkType);
	}
	
	public void setChunkProcessorsProvider(IChunkProcessorProvider chunkProcessorProvider) {
		this.chunkProcessorProvider = chunkProcessorProvider;
	}

	public void setResult(List<DescriptionTreatmentElement> result) {
		this.result = result;
	}
	
	public List<DescriptionTreatmentElement> getResult() {
		return this.result;
	}

	public DescriptionTreatmentElement getLastResult() {
		return result.get(result.size()-1);
	}

	public DescriptionTreatmentElement getLastResult(DescriptionType descriptionType) {
		DescriptionTreatmentElement result = null;
		for(int i=this.result.size()-1; i>=0; i--) {
			DescriptionTreatmentElement element = this.result.get(i);
			if(element.isOfDescriptionType(descriptionType)) {
				result = element;
				break;
			}
		}
		return result;
	}
	
	public DescriptionTreatmentElement getParent(DescriptionTreatmentElement descriptionTreatmentElement) {
		for(DescriptionTreatmentElement resultElement : result) {
			TreatmentElement parent = resultElement.getParent(descriptionTreatmentElement);
			if(parent!=null && parent instanceof DescriptionTreatmentElement)
				return (DescriptionTreatmentElement)parent;
		}
		return null;
	}
	
	
	public boolean isNewStatement() {
		chunkListIterator.previous();
		Chunk previousChunk = chunkListIterator.previous();
		chunkListIterator.next();
		chunkListIterator.next();
		return previousChunk.getTerminalsText().matches("(:|;|\\.)");
	}
	
	public boolean isNewSegment() {
		chunkListIterator.previous();
		if(!chunkListIterator.hasPrevious()) {
			chunkListIterator.next();
			return true;
		}
		Chunk previousChunk = chunkListIterator.previous();
		chunkListIterator.next();
		chunkListIterator.next();
		return previousChunk.getTerminalsText().matches(",");
	}

	public void reset() {
		 currentState = new ProcessingContextState();
	}
}
