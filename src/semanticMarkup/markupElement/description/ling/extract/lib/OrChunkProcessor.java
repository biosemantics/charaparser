package semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.IChunkProcessor;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.markupElement.description.ling.extract.AbstractChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContextState;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.model.Character;
import semanticMarkup.markupElement.description.model.Relation;
import semanticMarkup.markupElement.description.model.Structure;
import semanticMarkup.model.Element;

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
	 * @param times
	 */
	@Inject
	public OrChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		
		ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		
		if(chunkListIterator.hasNext()) {
			chunkListIterator.previous();
			Chunk  previousChunk = chunkListIterator.previous();
			chunkListIterator.next();
			chunkListIterator.next();
			Chunk nextChunk = chunkListIterator.next();
			
			if(nextChunk.isOfChunkType(ChunkType.END_OF_SUBCLAUSE)) 
				return result;
			
			if(!lastElements.isEmpty() && lastElements.getLast().isCharacter()) {
				
				Character character = (Character)lastElements.getLast();
				String characterName = character.getName();
				if(nextChunk.isOfChunkType(ChunkType.PP)){
					Structure parent = processingContext.getParentStructure(character);
					List<Structure> parents = new LinkedList<Structure>();
					parents.add(parent);
					this.createCharacterElement(parents, new LinkedList<Chunk>(), nextChunk.getTerminalsText(), 
							characterName, "", processingContextState);
				}
				
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
					List<Element> previousResult = new LinkedList<Element>(
							previousChunkProcessor.process(previousChunk, processingContext));
					
					//TODO need a command construct here that allows the following
					// chunkprocessor return the command to make changes to the result
					// command can be fired or not, depending on whether one goes back in time to find out what was created or it is regular processing
					// changes of a chunkrprocessor can be: new structure,relation,character,change of property field...
					//alternatively take result into contextstate and clone it to process on somehow

					processingContext.setCurrentState(currentState);
					
					boolean processedNextChunk = false;
					if(!previousResult.isEmpty()) {
						if(previousResult.get(0).isCharacter()) {
							Character previousCharacter = (Character)previousResult.get(0);
							Structure structure = processingContext.getParentStructure(previousCharacter);				
							if(structure != null) {
								Character newElement = new Character();
								structure.addCharacter(newElement);
								newElement.setName(characterName);
								String chunkText = nextChunk.getTerminalsText();
								if(chunkText.contains("~list~")) {
									chunkText = chunkText.replaceFirst("\\w{2,}.*?~list~","").replaceAll("punct", ",").replaceAll("~", " ");
								}
								newElement.setValue(chunkText);
								addClauseModifierConstraint(newElement, processingContextState); 
								result.add(newElement);
								
								//TODO this is just the quick and dirty fix of the above issue
								removeResults(previousResult, processingContext.getResult());
								processedNextChunk = true;
							}
						}
					} 
					if(!processedNextChunk)
						chunkListIterator.previous();
					//if(!result.isEmpty())
					//	processingContextState.setLastElements(result);
					
					if(!result.isEmpty() && result.get(0).isCharacter()) {
						Structure parent = processingContext.getParentStructure((Character)result.get(0));
						List<Element> newLastElements = new LinkedList<Element>();
						newLastElements.add(parent);
						processingContextState.setLastElements(newLastElements);
					}
					
					return result;
				} else if(nextChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && previousChunk.containsChunkType(ChunkType.MODIFIER)) {
					List<Chunk> modifiers = new LinkedList<Chunk>();
					modifiers.addAll(processingContext.getState(previousChunk).getUnassignedModifiers());
					modifiers.addAll(nextChunk.getChunks(ChunkType.MODIFIER));
					
					processingContext.getCurrentState().getUnassignedModifiers().clear();
					processingContext.getCurrentState().getUnassignedModifiers().addAll(modifiers);
				}
			} else if(previousChunk.isOfChunkType(ChunkType.PP) && nextChunk.isOfChunkType(ChunkType.PP)) {
				IChunkProcessor ppChunkProcessor = processingContext.getChunkProcessor(ChunkType.PP);
				//save actual current state to reset to correct current state after replay of chunk processing of previous chunk
				ProcessingContextState currentState = processingContext.getCurrentState();
				processingContext.setCurrentState(previousChunk);
				List<Element> ppResult = new LinkedList<Element>(
						ppChunkProcessor.process(previousChunk, processingContext));
				this.removeResults(ppResult, processingContext.getResult());
				Relation previousRelation = (Relation)this.getFirstDescriptionElement(ppResult, Relation.class);

				processingContext.setCurrentState(currentState);
				List<Element> nextResult = new LinkedList<Element>(
						ppChunkProcessor.process(nextChunk, processingContext));
				Relation newRelation = (Relation)this.getFirstDescriptionElement(nextResult, Relation.class);
				if(previousRelation !=null && newRelation!=null) {
					newRelation.setFrom(previousRelation.getFrom());
				}
				result.addAll(nextResult);
				if(!result.isEmpty())
					processingContextState.setLastElements(result);
				return result;
			} else if(previousChunk.isOfChunkType(ChunkType.PP)) {
				LinkedList<Structure> subjects = processingContextState.getSubjects();
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

	private void removeResults(List<Element> previousResult, List<Element> result) {
		for(Element element : result) {
			for(Element toRemove : previousResult) {
				element.removeElementRecursively(toRemove);
			}
		}
	}
}
