package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;







import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * MyCharacterStateChunkProcessor processes chunks of ChunkType.CHARACTER_STATE
 * @author rodenhausen
 */
public class MyCharacterStateChunkProcessor extends AbstractChunkProcessor {

	private Pattern hyphenedCharacterPattern = Pattern.compile("\\w+-(\\w+)");
	private boolean eqcharaExempt = false;

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
	public MyCharacterStateChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		ArrayList<String> alternativeIds = new ArrayList<String>();
		List<BiologicalEntity> parents = parentStructures(processingContext, processingContextState, alternativeIds);
		List<Element> elements = processCharacterState(chunk, parents, 
				processingContextState, processingContext);//apices of basal leaves spread 
		addAlternativeIds(elements, alternativeIds); 
		if(parents.isEmpty()) {
			for(Element element : elements)
				if(element.isCharacter())
					processingContextState.getUnassignedCharacters().add((Character)element);
		}
		processingContextState.setLastElements(elements);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return elements;
	}
	
	/**
	 * @param content: m[usually] coloration[dark brown]: there is only one character states and several modifiers
	 * @param parents: of the character states
	 */
	protected List<Element> processCharacterState(Chunk content,
			List<BiologicalEntity> parents, ProcessingContextState processingContextState, 
			ProcessingContext processingContext) {
		
		List<Element> results = new LinkedList<Element>();
		List<Chunk> modifiers = new LinkedList<Chunk>();
		List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
		modifiers.addAll(unassignedModifiers);
		modifiers.addAll(content.getChunks(ChunkType.MODIFIER));	
		
		String character = content.getPropertyBFS("characterName");
		if(processingContextState.getUnassignedCharacter()!=null) { //override character with unassigned character
			character = processingContextState.getUnassignedCharacter();
			processingContextState.setUnassignedCharacter(null);
		}
		Chunk characterState = content.getChunkDFS(ChunkType.STATE);
		String characterStateString = characterState.getTerminalsText();
		
		if(characterState.size() > 1) {
			List<Chunk> characters = new ArrayList<Chunk>();
			characters.add(content);
			List<Element> result = this.processCharacterText(characters, parents, character, processingContextState, 
					processingContext, false);
			//results = this.processCharacterList(content, parents, processingContextState);
			//return results;
			return result;
		}
	
		boolean characterrole = false;  

		Element lastElement = null;
		if(processingContextState.getLastElements().size() > 0){
			lastElement = processingContextState.getLastElements().getLast();
		}
		if(lastElement!=null && lastElement.isCharacter() && processingContext.getLastChunkYieldElement()){ //last element is a character, it is possible for the
				characterrole = true;      //state plays the character role. if the last element is a structure, then we need a state here.
		}
		
		if(characterrole && !this.eqcharaExempt){
			String newState = equalCharacters.get(characterStateString);
			if(newState != null){
				characterStateString = newState;
				if(characterKnowledgeBase.containsCharacterState(characterStateString)){
					character = characterKnowledgeBase.getCharacterName(characterStateString).getCategories();
					if(character==null){
						character = "character";//characterStateString; //characterStateString not in glossary, should be added
						log(LogLevel.INFO, characterStateString +" should be added to the glossary as a 'character' ");
					}
				}
			}
		}
		if(character.equals("character") && modifiers.size() == 0 &&!this.eqcharaExempt) {
			//high relief: character=relief, reset the character of "high" to "relief"
			boolean dealt = false;
			//if(processingContextState.getLastElements().size() > 0){
			//	Element lastElement = processingContextState.getLastElements().getLast();
				if(lastElement!=null && lastElement.isCharacter()){ 
					for(Element element : processingContextState.getLastElements()) 
						if(element.isCharacter()){
							((Character)element).setName(characterStateString);
							dealt = true;
						}
				}
			//}
			if(!dealt)
				processingContextState.setUnassignedCharacter(characterStateString);
			results.addAll(processingContextState.getLastElements());
		} else if(characterStateString.length() > 0) {
			//Character characterElement = createCharacterElement(parents, modifiers, characterStateString, character, "", processingContextState);
			//if(characterElement!=null)
			//	results.add(characterElement);
			String characterStateText = characterState.getTerminalsText();
			Matcher matcher = hyphenedCharacterPattern.matcher(characterStateText); //TODO Hong not sure what this does
			if(matcher.matches()) {
				ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
				int backupNextIndex = chunkListIterator.nextIndex();
				String suffix = matcher.group(1);
				results.addAll(findPreviousCharacterList(processingContext, character, suffix));
				processingContextState.getCarryOverDataFrom(processingContext.getCurrentState());
				processingContext.setCurrentState(processingContextState);
				log(LogLevel.DEBUG, "restored current state after findPreviousCharacterList is run.");
				while(chunkListIterator.nextIndex() < backupNextIndex)
					chunkListIterator.next();
			}	
			if(characterStateString.matches(".*?\\bto\\b.*"))
				results.addAll(createRangeCharacterElement(parents, modifiers, characterStateString.replaceAll("-to-", " to "), character, processingContextState));
			else {
				Character characterElement = createCharacterElement(parents, modifiers, characterStateString, character, "", processingContextState, false);
				if(characterElement!=null)
					results.add(characterElement);
			}
		}
		unassignedModifiers.clear();
		
		return results;
	}
	
	public void setEqcharaExempt(){
		this.eqcharaExempt = true;
	}
	
	public void resetEqcharaExempt(){
		this.eqcharaExempt = false;
	}
	/*Thomas'.
	 protected List<Element> processCharacterState(Chunk content,
			List<Structure> parents, ProcessingContextState processingContextState, 
			ProcessingContext processingContext) {
		List<Element> results = new LinkedList<Element>();
		List<Chunk> modifiers = new LinkedList<Chunk>();
		List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
		modifiers.addAll(unassignedModifiers);
		modifiers.addAll(content.getChunks(ChunkType.MODIFIER));	
		
		String character = content.getPropertyBFS("characterName");
		Chunk characterState = content.getChunkDFS(ChunkType.STATE);
		String characterStateString = characterState.getTerminalsText();
		
		String characterStateText = characterState.getTerminalsText();
		Matcher matcher = hyphenedCharacterPattern.matcher(characterStateText);
		if(matcher.matches()) {
			ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
			int backupNextIndex = chunkListIterator.nextIndex();
			String suffix = matcher.group(1);
			results.addAll(findPreviousCharacterList(processingContext, character, suffix));
			while(chunkListIterator.nextIndex() < backupNextIndex)
				chunkListIterator.next();
		}
		
		List<Chunk> characters = new ArrayList<Chunk>();
		characters.add(content);
		if(character != null) {
			//LinkedList<DescriptionTreatmentElement> characterElement = this.processCharacterText(characters, parents, character, processingContextState);
			if(processingContextState.getUnassignedCharacter()!=null) {
				character = processingContextState.getUnassignedCharacter();
				processingContextState.setUnassignedCharacter(null);
			}
			
			if(characterStateString.contains(" to ")) //Hong: will this 'to' block ever be reached? 
				results.addAll(createRangeCharacterElement(parents, modifiers, characterStateString, character, processingContextState));
			else {
				Character characterElement = createCharacterElement(parents, modifiers, characterStateString, character, "", processingContextState);
				if(characterElement!=null)
					results.add(characterElement);
			}
		} else {
			if(characterState.size() > 1) {
				List<Element> result = this.processCharacterText(characters, parents, character, processingContextState, 
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
				if(characterKnowledgeBase.containsCharacterState(characterStateString))
					character = characterKnowledgeBase.getCharacterName(characterStateString);
			}
			if(character.equals("character") && modifiers.size() == 0) {
				//high relief: character=relief, reset the character of "high" to "relief"
				Element lastElement = processingContextState.getLastElements().getLast();
				if(lastElement.isCharacter()) 
					for(Element element : processingContextState.getLastElements()) 
						if(element.isCharacter())
							((Character)element).setName(characterStateString);
				else if(lastElement.isStructure())
					processingContextState.setUnassignedCharacter(characterStateString);
				results.addAll(processingContextState.getLastElements());
			} else if(characterStateString.length() > 0) {
				Character characterElement = createCharacterElement(parents, modifiers, characterStateString, character, "", processingContextState);
				if(characterElement!=null)
					results.add(characterElement);
			}
		}
		unassignedModifiers.clear();
		
		return results;
	}
	
	 */

	private List<Element> findPreviousCharacterList(ProcessingContext processingContext, String characterName, String suffix) {
		List<Element> result = new LinkedList<Element>();
		ListIterator<Chunk> listIterator = processingContext.getChunkListIterator();
		listIterator.previous();
		
		while(true) {
			if(listIterator.hasPrevious()) {
				Chunk previousChunk = listIterator.previous();

				if(previousChunk.isOfChunkType(ChunkType.OR) || previousChunk.isOfChunkType(ChunkType.AND) || 
					previousChunk.isOfChunkType(ChunkType.COMMA)) {
					continue;
				} else if((previousChunk instanceof AbstractParseTree) && previousChunk.getTerminalsText().endsWith("-") && previousChunk.getTerminalsText().length() > 1) {
					AbstractParseTree previousTerminal = (AbstractParseTree)previousChunk;
					previousTerminal.setTerminalsText(previousTerminal.getTerminalsText() + suffix);
					Chunk stateChunk = new Chunk(ChunkType.STATE, previousChunk);
					Chunk characterStateChunk = new Chunk(ChunkType.CHARACTER_STATE, stateChunk);
					characterStateChunk.setProperty("characterName", characterName);
					result.addAll(processingContext.getChunkProcessor(ChunkType.CHARACTER_STATE).process(characterStateChunk, processingContext));
				}
			}
			break;
		}
		
		
		//String previousElement = ""; //modifier, character, connector, hyphen
		
		
		
/*		while(true) {
			if(listIterator.hasPrevious()) {
				Chunk previousChunk = listIterator.previous();

				if(previousChunk.isOfChunkType(ChunkType.OR) || previousChunk.isOfChunkType(ChunkType.AND) || 
					previousChunk.isOfChunkType(ChunkType.COMMA)) {
					previousElement = "connector";
				} else if(previousChunk.getTerminalsText().equals("-")) {
					previousElement = "hyphen";
				} else if(previousChunk.getTerminalsText().endsWith("-")) {
					Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, previousChunk);
					characterChunk.setProperty("characterName", characterName);
					result.addAll(processingContext.getChunkProcessor(ChunkType.CHARACTER_STATE).process(characterChunk, processingContext));
				} else if(previousChunk.isOfChunkType(ChunkType.UNASSIGNED) && previousElement.equals("hyphen")) {
					
					
				} else {
					break;
				}
			} else {
				break;
			}
		}*/
		return result;
	}


}
