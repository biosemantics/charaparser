package semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
 * CharacterStateChunkProcessor processes chunks of ChunkType.CHARACTER_STATE
 * @author rodenhausen
 */
public class CharacterStateChunkProcessor extends AbstractChunkProcessor {

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
	public CharacterStateChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		List<Structure> parents = lastStructures(processingContext, processingContextState);
		List<Element> characters = processCharacterState(chunk, parents, 
				processingContextState);//apices of basal leaves spread 
		
		processingContextState.setLastElements(characters);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return characters;
	}
	
	/**
	 * @param content: m[usually] coloration[dark brown]: there is only one character states and several modifiers
	 * @param parents: of the character states
	 */
	protected List<Element> processCharacterState(Chunk content,
			List<Structure> parents, ProcessingContextState processingContextState) {
		List<Element> results = new LinkedList<Element>();

		List<Chunk> modifiers = content.getChunks(ChunkType.MODIFIER);
		Chunk characterChunk = content.getChunkDFS(ChunkType.CHARACTER_STATE);
		String character = characterChunk.getProperty("characterName");
		if(processingContextState.getUnassignedCharacter()!=null) {
			character = processingContextState.getUnassignedCharacter();
			processingContextState.setUnassignedCharacter(null);
		}
		String state = characterChunk.getTerminalsText();
		
		String newState = equalCharacters.get(state);
		if(newState != null){
			state = newState;
			String newCharacter = characterKnowledgeBase.getCharacterName(state);
			if(newCharacter != null) {
				character = newCharacter;
			}
		}
		if(character.equals("character") && modifiers.size() == 0) {
			//high relief: character=relief, reset the character of "high" to "relief"
			Element lastElement = processingContextState.getLastElements().getLast();
			if(lastElement.isCharacter()) 
				for(Element element : processingContextState.getLastElements()) 
					if(element.isCharacter())
						((Character)element).setName(state);
			else if(lastElement.isStructure())
				processingContextState.setUnassignedCharacter(state);
			results.addAll(processingContextState.getLastElements());
		}else if(state.length()>0) {
			Character characterElement = this.createCharacterElement(parents, modifiers, state, character, "", processingContextState);
			if(characterElement!=null)
				results.add(characterElement);
		}
		
		return results;
	}
}