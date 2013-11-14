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
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.markupElement.description.ling.extract.AbstractChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContextState;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.model.Character;
import semanticMarkup.markupElement.description.model.Structure;
import semanticMarkup.model.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

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
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk,
			ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		
		processingContext.getCurrentState().clearUnassignedModifiers();
		
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		processingContextState.setCommaAndOrEosEolAfterLastElements(true);
		if(!processingContextState.getLastElements().isEmpty()) {
			Element lastElement = processingContextState.getLastElements().get(0);
			if(lastElement.isCharacter()) {
				Structure parent = processingContext.getParentStructure((Character)lastElement);
				if(parent!=null) {
					List<Element> newLastElements = new LinkedList<Element>();
					newLastElements.add(parent);
					processingContextState.setLastElements(newLastElements);
				}
			}
		}
		
		List<Character> unassignedCharacters = processingContextState.getUnassignedCharacters();
		if(!unassignedCharacters.isEmpty() && nextChunkIsOrgan(processingContext)) {
			Structure structureElement = new Structure();
			int structureIdString = processingContext.fetchAndIncrementStructureId(structureElement);
			structureElement.setId("o" + String.valueOf(structureIdString));	
			structureElement.setName("whole_organism"); 
			List<Structure> structureElements = new LinkedList<Structure>();
			structureElements.add(structureElement);
			result.addAll(establishSubject(structureElements, processingContextState));
			
			for(Character character : unassignedCharacters) {
				for(Structure parent : structureElements) {
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
