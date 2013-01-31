package semanticMarkup.ling.extract.lib;

import java.util.ArrayList;
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

public class MyCharacterStateChunkProcessor extends AbstractChunkProcessor {

	private boolean attachToLast;

	@Inject
	public MyCharacterStateChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
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
		LinkedList<DescriptionTreatmentElement> parents = lastStructures(processingContext, processingContextState);
		LinkedList<DescriptionTreatmentElement> characters = processCharacterState(chunk, parents, 
				processingContextState, processingContext);//apices of basal leaves spread 
		//processingContextState.setLastElements(characters);
		return characters;
	}
	
	/**
	 * @param content: m[usually] coloration[dark brown]: there is only one character states and several modifiers
	 * @param parents: of the character states
	 */
	protected LinkedList<DescriptionTreatmentElement> processCharacterState(Chunk content,
			LinkedList<DescriptionTreatmentElement> parents, ProcessingContextState processingContextState, 
			ProcessingContext processingContext) {
		LinkedList<DescriptionTreatmentElement> results = new LinkedList<DescriptionTreatmentElement>();
		List<Chunk> modifiers = new LinkedList<Chunk>();
		List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
		modifiers.addAll(unassignedModifiers);
		modifiers.addAll(content.getChunks(ChunkType.MODIFIER));	
		
		String character = content.getPropertyBFS("characterName");
		Chunk characterState = content.getChunkDFS(ChunkType.STATE);
		String characterStateString = characterState.getTerminalsText();
		
		List<Chunk> characters = new ArrayList<Chunk>();
		characters.add(content);
		if(character != null) {
			//LinkedList<DescriptionTreatmentElement> characterElement = this.processCharacterText(characters, parents, character, processingContextState);
			if(characterStateString.contains(" to "))
				createRangeCharacterElement(parents, results, modifiers, characterStateString, character, processingContextState);
			else {
				DescriptionTreatmentElement characterElement = createCharacterElement(parents, modifiers, characterStateString, character, "", processingContextState);
				if(characterElement!=null)
					results.add(characterElement);
			}
			/*for(AbstractParseTree state : characterState.getTerminals()) {
				String stateText = state.getTerminalsText().trim();
				if(!stateText.equals(",") && !stateText.equals("and") && !stateText.equals("or")) {
					DescriptionTreatmentElement characterElement = createCharacterElement(parents, modifiers, state.getTerminalsText(), character, "", processingContextState);
					if(characterElement!=null)
						results.add(characterElement);
				}
			}*/
		} else {
			if(characterState.size() > 1) {
				LinkedList<DescriptionTreatmentElement> result = this.processCharacterText(characters, parents, character, processingContextState, 
						processingContext);
				//results = this.processCharacterList(content, parents, processingContextState);
				return results;
			}
			
			if(processingContextState.getUnassignedCharacter()!=null) {
				character = processingContextState.getUnassignedCharacter();
				processingContextState.setUnassignedCharacter(null);
			}
			
			String newState = equalCharacters.get(characterState);
			if(newState != null){
				characterStateString = newState;
				if(characterKnowledgeBase.contains(characterStateString))
					character = characterKnowledgeBase.getCharacter(characterStateString);
			}
			if(character.equals("character") && modifiers.size() == 0) {
				//high relief: character=relief, reset the character of "high" to "relief"
				DescriptionTreatmentElement lastElement = processingContextState.getLastElements().getLast();
				if(lastElement.isOfDescriptionType(DescriptionType.CHARACTER)) 
					for(DescriptionTreatmentElement element : processingContextState.getLastElements()) 
						element.setProperty("name", characterStateString);
				else if(lastElement.isOfDescriptionType(DescriptionType.STRUCTURE))
					processingContextState.setUnassignedCharacter(characterStateString);
				results.addAll(processingContextState.getLastElements());
			} else if(characterStateString.length() > 0) {
				DescriptionTreatmentElement characterElement = createCharacterElement(parents, modifiers, characterStateString, character, "", processingContextState);
				if(characterElement!=null)
					results.add(characterElement);
			}
		}
		unassignedModifiers.clear();
		
		return results;
	}
}
