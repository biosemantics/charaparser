package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;





import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * PPChunkProcessor processes chunks of ChunkType.PP
 * @author rodenhausen
 */
public class PPChunkProcessor extends AbstractChunkProcessor {

	private Pattern hyphenedCharacterPattern = Pattern.compile("\\w+-(\\w+)");

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
	public PPChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();

		
		ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
		Chunk nextChunk = null;
		if(chunkListIterator.hasNext()){
			nextChunk = chunkListIterator.next();
			chunkListIterator.previous();
		}

		if(!chunk.containsChunkType(ChunkType.ORGAN) && (nextChunk==null || nextChunk.isOfChunkType(ChunkType.CHARACTER_STATE))) { //at flowering?
			processingContextState.setClauseModifierContraint(chunk.getTerminalsText());
			return result;
		}
		
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
		
		
		//r[{} {} p[of] o[.....]]
		List<Chunk> modifier = new ArrayList<Chunk>();// chunk.getChunks(ChunkType.MODIFIER);
		Chunk preposition = chunk.getChunkDFS(ChunkType.PREPOSITION);
		LinkedHashSet<Chunk> prepositionChunks = preposition.getChunks();
		Chunk object = chunk.getChunkDFS(ChunkType.OBJECT); //PP: [PREPOSITION: [PP_LIST: [at, OR: [or], near]], ORGAN: [bases]]
		
		if(object ==null) return result;
		
		if(characterPrep(chunk, processingContextState))
			return result;	
		
		boolean lastIsStructure = false;
		boolean lastIsCharacter = false;
		//boolean lastIsComma = false;
		
		//length of leaves
		if(preposition.getChildChunk(ChunkType.CHARACTER_STATE)!=null && 
				preposition.getChildChunk(ChunkType.CHARACTER_STATE).getProperty("characterName").compareTo("character")==0 //&&
				//object.containsChunkType(ChunkType.ORGAN)
				){//PREPOSITION: [CHARACTER_STATE: characterName->character; [STATE: [length]], of]
				//save character
				processingContextState.setUnassignedCharacter(preposition.getChildChunk(ChunkType.CHARACTER_STATE).getTerminalsText());
				//create biological entity elements for the organs
				ArrayList<Chunk> organs = new ArrayList<Chunk>();
				organs.addAll(splitObject(object));
				List<BiologicalEntity> elements = this.createStructureElements(organs, processingContext, processingContextState);
				processingContext.getCurrentState().setLastElements(elements);
				result.addAll(elements);
				processingContextState.setCommaAndOrEosEolAfterLastElements(false);
				return result;
		}
		
		
		if (lastElements.isEmpty()) {
			unassignedModifiers.add(chunk);
			return result;
		}
		
		List<BiologicalEntity> lastElementStructures = new LinkedList<BiologicalEntity>();
		for(Element element : lastElements) {
			if(element.isStructure())
				lastElementStructures.add((BiologicalEntity)element);
		}
		
		Element lastElement = lastElements.getLast();
		if(lastElement != null) {
			lastIsStructure = lastElement.isStructure();
			lastIsCharacter = lastElement.isCharacter();
			
			
			if(lastIsStructure && isNumerical(object)) {
				List<Chunk> modifiers = new ArrayList<Chunk>();
				result.addAll(annotateNumericals(object.getTerminalsText(), "count", modifiers, lastElementStructures, false, processingContextState));
				return result;
			}
			
			Set<Chunk> objects = new LinkedHashSet<Chunk>();
			if(object.containsChildOfChunkType(ChunkType.OR) || object.containsChildOfChunkType(ChunkType.AND) || object.containsChildOfChunkType(ChunkType.NP_LIST)) {
				objects = splitObject(object);
			} else {
				objects.add(object);
			}
			
			List<BiologicalEntity> subjectStructures = lastElementStructures;
			if(!lastIsStructure || processingContextState.isCommaAndOrEosEolAfterLastElements()) {
				subjectStructures = processingContextState.getSubjects();
			}
			
			String usedRelation = null;
			for(Chunk aObject : objects) {
				boolean lastChunkIsOrgan = true;
				boolean foundOrgan = false;
				LinkedHashSet<Chunk> beforeOrganChunks = new LinkedHashSet<Chunk>(); 
				LinkedHashSet<Chunk> organChunks = new LinkedHashSet<Chunk>();
				LinkedHashSet<Chunk> afterOrganChunks = new LinkedHashSet<Chunk>(); 
				
				this.getOrganChunks(aObject, beforeOrganChunks, organChunks, afterOrganChunks);
				foundOrgan = !organChunks.isEmpty();
				lastChunkIsOrgan = afterOrganChunks.isEmpty() && foundOrgan;
				
				/*for(Chunk objectChunk : object.getChunks()) {
				if(objectChunk.isOfChunkType(ChunkType.ORGAN)) {
					lastChunkIsOrgan = true;
					foundOrgan = true;
					
					organChunks.add(objectChunk);
				} else {
					lastChunkIsOrgan = false;
					
					if(foundOrgan)
						afterOrganChunks.add(objectChunk);
					else
						beforeOrganChunks.add(objectChunk);
				}
			}*/
				
				if(preposition.getTerminalsText().equals("to") && !foundOrgan && containsCharacter(beforeOrganChunks)) {
					result.addAll(connectCharacters(subjectStructures, unassignedModifiers, preposition, beforeOrganChunks, processingContext));
				} else if(lastChunkIsOrgan) {
					List<Element> linkedResult = linkObjects(subjectStructures, modifier, preposition, aObject, lastIsStructure, lastIsCharacter, processingContext, processingContextState, usedRelation, lastElement);
					usedRelation = getRelation(linkedResult);
					result.addAll(linkedResult);
				} else if(foundOrgan) {
					LinkedHashSet<Chunk> objectChunks = new LinkedHashSet<Chunk>();
					objectChunks.addAll(beforeOrganChunks);
					objectChunks.addAll(organChunks);
					//obj = beforeOrganChunks + organChunks;
					//modi = afterOrganChunks;
					
					Chunk objectChunk = new Chunk(ChunkType.UNASSIGNED, objectChunks);
					List<Element> linkedResult = linkObjects(subjectStructures, modifier, preposition, objectChunk, lastIsStructure, lastIsCharacter, processingContext, processingContextState, usedRelation, lastElement);
					usedRelation = getRelation(linkedResult);
					result.addAll(linkedResult); 
					//result.addAll(structures);
				} else {
					if(lastIsStructure){
						//context: on cutting first white
						ArrayList<Chunk> mods = new ArrayList<Chunk> ();
						mods.add(chunk);
						processingContextState.setUnassignedModifiers(mods);
						//((Structure)lastElement).appendConstraint(chunk.getTerminalsText()); //should not be a constraint on the structure, should be a modifier for a character
					}
					else if(lastIsCharacter) {
						List<BiologicalEntity> objectStructures = 
								this.extractStructuresFromObject(object, processingContext, processingContextState); 
						((Character)lastElement).appendConstraint(chunk.getTerminalsText());
						if(!objectStructures.isEmpty()) {
							result.addAll(objectStructures);
							((Character)lastElement).setConstraintId(listStructureIds(objectStructures));
						}
					}
				}
				//not sure the afterorgan logic is useful. not used for 'and-ed' chunks.
				LinkedList<Element> lastElementsBackup =(LinkedList<Element>) processingContext.getCurrentState().getLastElements().clone();
				LinkedList<Element> newLastElements = new LinkedList<Element>();
				for(Element resultElement : result) {
					if(resultElement.isStructure())
						newLastElements.add(resultElement);
				}
				processingContext.getCurrentState().setLastElements(newLastElements);
				for(Chunk afterOrganChunk : afterOrganChunks) {
					IChunkProcessor chunkProcessor = processingContext.getChunkProcessor(afterOrganChunk.getChunkType());
					if(chunkProcessor != null){ 
						result.addAll(chunkProcessor.process(afterOrganChunk, processingContext));
						processingContextState.getCarryOverDataFrom(processingContext.getCurrentState());
						processingContext.setCurrentState(processingContextState);
						log(LogLevel.DEBUG, "restored current state after "+chunkProcessor.getClass()+" is run.");
					}
				}
				processingContext.getCurrentState().setLastElements(lastElementsBackup);
			}
		}
		
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}

	private String getRelation(List<Element> elements) {
		for(Element element : elements) {
			if(element.isRelation())
				return ((Relation)element).getName();
		}
		return null;
	}

	private List<Character> connectCharacters(
			List<BiologicalEntity> subjectStructures, List<Chunk> modifiers, 
			Chunk preposition, LinkedHashSet<Chunk> beforeOrganChunks,
			ProcessingContext processingContext) {
		List<Character> result = new LinkedList<Character>();
		ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
		chunkListIterator.previous();
		Chunk beforePPChunk = chunkListIterator.previous();
	
		Chunk characterStateChunk = null;
		for(Chunk chunk : beforeOrganChunks) {
			if(chunk.containsChunkType(ChunkType.CHARACTER_STATE)) {
				characterStateChunk = chunk.getChunkDFS(ChunkType.CHARACTER_STATE);
				break;
			}
		}
		if(characterStateChunk != null) {
			Matcher matcher = hyphenedCharacterPattern.matcher(characterStateChunk.getTerminalsText());
			String characterSuffix = "";
			if(matcher.matches()) {
				characterSuffix = matcher.group(1);
			}
			
			String beforePPChunkText = beforePPChunk.getTerminalsText();
			matcher = hyphenedCharacterPattern.matcher(beforePPChunkText);
			if(!matcher.matches()) {
				if(beforePPChunkText.endsWith("-")) {
					beforePPChunkText = beforePPChunkText + characterSuffix;
				} else {
					beforePPChunkText = beforePPChunkText + "-" + characterSuffix;
				}
			}
			
			String character = beforePPChunkText + " " + preposition.getTerminalsText() + " " + characterStateChunk.getTerminalsText();
			String characterName = characterStateChunk.getProperty("characterName");
			result.addAll(createRangeCharacterElement(subjectStructures, modifiers, character, characterName, processingContext.getCurrentState()));
		}
		chunkListIterator.next();
		chunkListIterator.next();
		return result;
	}

	private boolean containsCharacter(LinkedHashSet<Chunk> beforeOrganChunks) {
		for(Chunk chunk : beforeOrganChunks) {
			if(chunk.containsChunkType(ChunkType.CHARACTER_STATE))
				return true;
		}
		return false;
	}

	private Set<Chunk> splitObject(Chunk object) {
		Set<Chunk> objectChunks = new LinkedHashSet<Chunk>();
		LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
		boolean collectedOrgan = false;
		
		if(object.containsChildOfChunkType(ChunkType.NP_LIST))
			object = object.getChildChunk(ChunkType.NP_LIST);
		
		for(Chunk chunk : object.getChunks()) {
			if((chunk.isOfChunkType(ChunkType.OR) || chunk.isOfChunkType(ChunkType.AND) || chunk.getTerminalsText().equals("and") || chunk.getTerminalsText().equals("or")) && collectedOrgan) {
				Chunk objectChunk = new Chunk(ChunkType.OBJECT, childChunks);
				objectChunks.add(objectChunk);				
				childChunks.clear();
			} else {
				for(AbstractParseTree terminal : chunk.getTerminals()) {
					if(chunk.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
						collectedOrgan = true;
						childChunks.add(chunk);
					} else {
						childChunks.add(chunk);
					}
				}
			}
		}
		Chunk objectChunk = new Chunk(ChunkType.OBJECT, childChunks);
		objectChunks.add(objectChunk);
		return objectChunks;
	}

	private void getOrganChunks(Chunk organParentChunk,
			LinkedHashSet<Chunk> beforeOrganChunks,
			LinkedHashSet<Chunk> organChunks,
			LinkedHashSet<Chunk> afterOrganChunks) {
		if(organParentChunk.isOfChunkType(ChunkType.ORGAN)) {
			organChunks.add(organParentChunk);
		} else if(!organParentChunk.containsChunkType(ChunkType.ORGAN) ||
				!afterOrganChunks.isEmpty()) {
			if(organChunks.isEmpty()) {
				beforeOrganChunks.add(organParentChunk);
			} 
			if(!organChunks.isEmpty()) {
				afterOrganChunks.add(organParentChunk);
			}
		} else if(organParentChunk.containsChunkType(ChunkType.ORGAN)) {
			LinkedHashSet<Chunk> chunks = organParentChunk.getChunks();
			for(Chunk chunk : chunks) {
				getOrganChunks(chunk, beforeOrganChunks, organChunks, afterOrganChunks);
			}
		}
	}
}
