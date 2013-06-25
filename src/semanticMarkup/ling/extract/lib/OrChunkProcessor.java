package semanticMarkup.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElementType;
import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.IChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * OrChunkProcessor processes chunks of ChunkType.OR
 * @author rodenhausen
 */
public class OrChunkProcessor extends AbstractChunkProcessor {

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
	 * @param attachToLast
	 * @param times
	 */
	@Inject
	public OrChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("AttachToLast")boolean attachToLast, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, attachToLast, times);
	}

	@Override
	protected List<DescriptionTreatmentElement> processChunk(Chunk chunk, ProcessingContext processingContext) {
		LinkedList<DescriptionTreatmentElement> result = new LinkedList<DescriptionTreatmentElement>();
		
		ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
		
		if(chunkListIterator.hasNext()) {
			chunkListIterator.previous();
			Chunk  previousChunk = chunkListIterator.previous();
			chunkListIterator.next();
			chunkListIterator.next();
			Chunk nextChunk = chunkListIterator.next();
			
			if(nextChunk.isOfChunkType(ChunkType.END_OF_SUBCLAUSE)) 
				return result;
			if(!lastElements.isEmpty() && lastElements.getLast().isOfDescriptionType(DescriptionTreatmentElementType.CHARACTER)) {
				
				String characterName = lastElements.getLast().getAttribute("name");
				if(!nextChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && 
						!nextChunk.isOfChunkType(ChunkType.COUNT) && 
						!nextChunk.isOfChunkType(ChunkType.NUMERICALS) && 
						!nextChunk.isOfChunkType(ChunkType.ORGAN) && 
						!nextChunk.isOfChunkType(ChunkType.NON_SUBJECT_ORGAN) &&
						!nextChunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN) && 
						!nextChunk.isOfChunkType(ChunkType.THAN_PHRASE) && 
						!nextChunk.isOfChunkType(ChunkType.PP)) {
					IChunkProcessor previousChunkProcessor = processingContext.getChunkProcessor(previousChunk.getChunkType());
					ProcessingContextState currentState = processingContext.getCurrentState();
					processingContext.setCurrentState(previousChunk);
					List<DescriptionTreatmentElement> previousResult = previousChunkProcessor.process(previousChunk, processingContext);
					
					//TODO need a command construct here that allows the following
					// chunkprocessor return the command to make changes to the result
					// command can be fired or not, depending on whether one goes back in time to find out what was created or it is regular processing
					// changes of a chunkrprocessor can be: new structure,relation,character,change of property field...
					//alternatively take result into contextstate and clone it to process on somehow

					processingContext.setCurrentState(currentState);
					
					boolean processedNextChunk = false;
					if(!previousResult.isEmpty()) {
						DescriptionTreatmentElement structure = processingContext.getParent(previousResult.get(0));				
						if(structure != null) {
							DescriptionTreatmentElement newElement = new DescriptionTreatmentElement(DescriptionTreatmentElementType.CHARACTER);
							structure.addTreatmentElement(newElement);
							newElement.setAttribute("name", characterName);
							String chunkText = nextChunk.getTerminalsText();
							if(chunkText.contains("~list~")) {
								chunkText = chunkText.replaceFirst("\\w{2,}.*?~list~","").replaceAll("punct", ",").replaceAll("~", " ");
							}
							newElement.setAttribute("value", chunkText);
							addClauseModifierConstraint(newElement, processingContextState); 
							result.add(newElement);
							
							//TODO this is just the quick and dirty fix of the above issue
							removeResults(previousResult, processingContext.getResult());
							processedNextChunk = true;
						}
					} 
					if(!processedNextChunk)
						chunkListIterator.previous();
					//if(!result.isEmpty())
					//	processingContextState.setLastElements(result);
					
					if(!result.isEmpty()) {
						DescriptionTreatmentElement parent = processingContext.getParent(result.get(0));
						LinkedList<DescriptionTreatmentElement> newLastElements = new LinkedList<DescriptionTreatmentElement>();
						newLastElements.add(parent);
						processingContextState.setLastElements(newLastElements);
					}
					
					return result;
				} else if(nextChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && previousChunk.containsChunkType(ChunkType.MODIFIER)) {
					//List<Chunk> modifierChunks = previousChunk.getChunks(ChunkType.MODIFIER);
					//processingContextState.getUnassignedModifiers().addAll(modifierChunks);
				}
			} else if(previousChunk.isOfChunkType(ChunkType.PP) && nextChunk.isOfChunkType(ChunkType.PP)) {
				IChunkProcessor ppChunkProcessor = processingContext.getChunkProcessor(ChunkType.PP);
				//save actual current state to reset to correct current state after replay of chunk processing of previous chunk
				ProcessingContextState currentState = processingContext.getCurrentState();
				processingContext.setCurrentState(previousChunk);
				List<DescriptionTreatmentElement> ppResult = ppChunkProcessor.process(previousChunk, processingContext);
				this.removeResults(ppResult, processingContext.getResult());
				DescriptionTreatmentElement previousRelation = this.getFirstDescriptionElement(ppResult, DescriptionTreatmentElementType.RELATION);

				processingContext.setCurrentState(currentState);
				List<DescriptionTreatmentElement> nextResult = ppChunkProcessor.process(nextChunk, processingContext);
				DescriptionTreatmentElement newRelation = this.getFirstDescriptionElement(nextResult, DescriptionTreatmentElementType.RELATION);
				if(previousRelation !=null && newRelation!=null) {
					newRelation.setAttribute("from", previousRelation.getAttribute("from"));
				}
				result.addAll(nextResult);
				if(!result.isEmpty())
					processingContextState.setLastElements(result);
				return result;
			} else if(previousChunk.isOfChunkType(ChunkType.PP)) {
				LinkedList<DescriptionTreatmentElement> subjects = processingContextState.getSubjects();
				if(!subjects.isEmpty()) {
					lastElements.clear();
					lastElements.add(subjects.getLast());
				}
			}
			chunkListIterator.previous();
		}
		
		if(!result.isEmpty())
			processingContextState.setLastElements(result);
		processingContextState.setCommaAndOrEosEolAfterLastElements(true);
		return result;
	}

	private void removeResults(List<DescriptionTreatmentElement> previousResult, List<DescriptionTreatmentElement> result) {
		for(DescriptionTreatmentElement element : result) {
			for(DescriptionTreatmentElement toRemove : previousResult) {
				element.removeTreatmentElementRecursively(toRemove);
			}
		}
	}
}
