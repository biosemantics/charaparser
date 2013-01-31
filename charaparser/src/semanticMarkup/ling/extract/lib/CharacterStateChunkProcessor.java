package semanticMarkup.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionType;
import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CharacterStateChunkProcessor extends AbstractChunkProcessor {

	private boolean attachToLast;

	@Inject
	public CharacterStateChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("AttachToLast")boolean attachToLast, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, attachToLast, times);
	}

	@Override
	protected List<DescriptionTreatmentElement> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<DescriptionTreatmentElement> parents = this.attachToLast? lastStructures(processingContext, processingContextState) 
				: processingContextState.getSubjects();
		LinkedList<DescriptionTreatmentElement> characters = processCharacterState(chunk, parents, 
				processingContextState);//apices of basal leaves spread 
		
		processingContextState.setLastElements(characters);
		return characters;
	}
	
	/**
	 * @param content: m[usually] coloration[dark brown]: there is only one character states and several modifiers
	 * @param parents: of the character states
	 */
	protected LinkedList<DescriptionTreatmentElement> processCharacterState(Chunk content,
			LinkedList<DescriptionTreatmentElement> parents, ProcessingContextState processingContextState) {
		LinkedList<DescriptionTreatmentElement> results = new LinkedList<DescriptionTreatmentElement>();

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
			String newCharacter = characterKnowledgeBase.getCharacter(state);
			if(newCharacter != null) {
				character = newCharacter;
			}
		}
		if(character.equals("character") && modifiers.size() == 0) {
			//high relief: character=relief, reset the character of "high" to "relief"
			DescriptionTreatmentElement lastElement = processingContextState.getLastElements().getLast();
			if(lastElement.isOfDescriptionType(DescriptionType.CHARACTER)) 
				for(DescriptionTreatmentElement element : processingContextState.getLastElements()) 
					element.setProperty("name", state);
			else if(lastElement.isOfDescriptionType(DescriptionType.STRUCTURE))
				processingContextState.setUnassignedCharacter(state);
			results.addAll(processingContextState.getLastElements());
		}else if(state.length()>0) {
			DescriptionTreatmentElement characterElement = this.createCharacterElement(parents, modifiers, state, character, "", processingContextState);
			if(characterElement!=null)
				results.add(characterElement);
		}
		
		return results;
	}
}
