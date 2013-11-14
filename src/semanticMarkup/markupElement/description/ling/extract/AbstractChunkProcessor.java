package semanticMarkup.markupElement.description.ling.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.IChunkProcessor;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.log.LogLevel;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.model.Character;
import semanticMarkup.markupElement.description.model.Relation;
import semanticMarkup.markupElement.description.model.Structure;
import semanticMarkup.model.Element;
import semanticMarkup.model.NamedElement;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * AbstractChunkProcessor implements common functionality of an IChunkProcessor shared among concret IChunkProcessor implementations
 * @author rodenhausen
 */
public abstract class AbstractChunkProcessor implements IChunkProcessor {

	protected IInflector inflector;
	protected IGlossary glossary;
	protected ITerminologyLearner terminologyLearner;
	protected Set<String> baseCountWords;
	protected Set<String> locationPrepositions;
	protected Set<String> clusters;
	protected ICharacterKnowledgeBase characterKnowledgeBase;
	protected IPOSKnowledgeBase posKnowledgeBase;
	protected String units;
	protected HashMap<String, String> equalCharacters;
	protected String numberPattern;
	protected String times;
	
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
	public AbstractChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositions")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("Times")String times) {
		this.inflector = inflector;
		this.glossary = glossary;
		this.characterKnowledgeBase = characterKnowledgeBase;
		this.posKnowledgeBase = posKnowledgeBase;
		this.terminologyLearner = terminologyLearner;
		this.baseCountWords = baseCountWords;
		this.locationPrepositions = locationPrepositions;
		this.clusters = clusters;
		this.units = units;
		this.equalCharacters = equalCharacters;
		this.numberPattern = numberPattern;
		this.times = times;
	}
	
	/**
	 * The current processingContextState of the given processingContext will be cloned and preserved for restore
	 * @param chunk
	 * @param processingContext
	 * @return list of DescriptionTreatmentElements resulting from the processing of chunk in processingContext
	 * TODO: it shouldnt be the chunk processors responsibility and freedom to or not to preserve the processingContextState
	 * This should be taken care of elsewhere
	 */
	public List<? extends Element> process(Chunk chunk, ProcessingContext processingContext) {
		log(LogLevel.DEBUG, "process chunk " + chunk);
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		processingContext.addState(chunk, processingContextState);
		ProcessingContextState newState = (ProcessingContextState)processingContextState.clone();
		processingContext.setCurrentState(newState);
		return processChunk(chunk, processingContext);
	}
	
	/**
	 * @param chunk
	 * @param processingContext
	 * @return list of DescriptionTreatmentElements resulting from the processing of chunk in processingContext
	 */
	protected abstract List<? extends Element> processChunk(Chunk chunk, ProcessingContext processingContext);
		
	protected List<Structure> establishSubject(List<Structure> subjectStructures, 
			ProcessingContextState processingContextState) {
		List<Structure> result = new LinkedList<Structure>();
		result.addAll(subjectStructures);
		
		LinkedList<Structure> subjects = processingContextState.getSubjects();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		subjects.clear();
		lastElements.clear();
		
		for(Structure structure : subjectStructures) {
			subjects.add(structure);
			lastElements.add(structure);
		}
		return result;
	}
	
	protected List<Structure> establishSubject(
			Chunk subjectChunk, ProcessingContext processingContext, ProcessingContextState processingContextState) {
		log(LogLevel.DEBUG, "establish subject from " + subjectChunk);
		List<Structure> result = new LinkedList<Structure>();
		
		List<Chunk> subjectChunks = new LinkedList<Chunk>();
		subjectChunks.addAll(processingContextState.getUnassignedConstraints());
		subjectChunks.add(subjectChunk);
		processingContextState.clearUnassignedConstraints();
		List<Structure> subjectStructures = createStructureElements(subjectChunks, processingContext, processingContextState);
		return this.establishSubject(subjectStructures, processingContextState);
	}

	
	protected List<Structure> reestablishSubject(ProcessingContextState processingContextState) {
		log(LogLevel.DEBUG, "reestablish subject");
		List<Structure> result = new LinkedList<Structure>();
		
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		LinkedList<Structure> subjects = processingContextState.getSubjects();
		lastElements.clear();
		for(Structure structure : subjects) {
			lastElements.add(structure);
			//element.detach();
			//result.remove(element);
			result.add(structure);
		}
		return result;
	}

	protected List<Structure> createStructureElements(List<Chunk> subjectChunks, ProcessingContext processingContext, ProcessingContextState processingContextState) {
		LinkedList<Structure> results = new LinkedList<Structure>();	
		Chunk subjectChunk = new Chunk(ChunkType.UNASSIGNED, subjectChunks);
		log(LogLevel.DEBUG, "create structure element from subjectChunks:\n" + subjectChunks);
		List<Chunk> organChunks = subjectChunk.getChunks(ChunkType.ORGAN);
		if(!organChunks.isEmpty()) {
			for(Chunk organChunk : organChunks) {
				Structure structure = new Structure();
				int structureIdString = processingContext.fetchAndIncrementStructureId(structure);
				structure.setId("o" + String.valueOf(structureIdString));
			
				Chunk constraintChunk = getConstraintOf(organChunk, subjectChunk);
				
				if(constraintChunk != null) {
					if(!constraintChunk.getTerminalsText().isEmpty()) 
						structure.setConstraint(constraintChunk.getTerminalsText());
					
					String organName = organChunk.getTerminalsText();
					structure.setName(inflector.getSingular(organName));
					
					List<Structure> parents = new LinkedList<Structure>();
					parents.add(structure);
					
					List<AbstractParseTree> terminals = subjectChunk.getTerminals();
					for(int i=0; i<terminals.size(); i++) {
						if(organChunk.containsOrEquals(terminals.get(i))) {
							if(i-1>=0 && (terminals.get(i-1).getTerminalsText().equals("a") || terminals.get(i-1).getTerminalsText().equals("an"))) {
								this.createCharacterElement(parents, null, "1", "count", "", processingContextState);
							}
							break;
						}
					}
					
					LinkedHashSet<Chunk> characterStateChunks = getCharacterStatesOf(organChunk, subjectChunk);
					for(Chunk characterStateChunk : characterStateChunks) {
						String character = characterStateChunk.getProperty("characterName");
						
						Chunk state = characterStateChunk.getChunkDFS(ChunkType.STATE);
						LinkedHashSet<Chunk> modifierChunks = getModifiersOf(characterStateChunk, subjectChunk);
						List<Chunk> modifierChunkList = new LinkedList<Chunk>(modifierChunks);
						//List<Chunk> modifierChunks = characterStateChunk.getChunks(ChunkType.MODIFIER);
						//modifierChunks.addAll(subjectChunk.getChunks(ChunkType.MODIFIER))
						
						this.createCharacterElement(parents, modifierChunkList, state.getTerminalsText(), character, "", processingContextState);
						
						//Chunk modifierChunk = new Chunk(ChunkType.UNASSIGNED, modifierChunks);
						//DescriptionTreatmentElement characterElement = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
						//characterElement.setProperty(, value)
					}
					
					List<Character> unassignedCharacters = processingContextState.getUnassignedCharacters();
					for(Character unassignedCharacter : unassignedCharacters) {
						structure.addCharacter(unassignedCharacter);
					}
					unassignedCharacters.clear();
				
					results.add(structure);
				}
			}
		}
		return results;
	}
	
	private LinkedHashSet<Chunk> getModifiersOf(Chunk characterStateChunk, Chunk subjectChunk) {
		LinkedHashSet<Chunk> modifiers = new LinkedHashSet<Chunk>();
		
		for(AbstractParseTree terminal : subjectChunk.getTerminals()) {
			if(subjectChunk.isPartOfChunkType(terminal, ChunkType.MODIFIER)) {
				Chunk modifier = subjectChunk.getChunkOfTypeAndTerminal(ChunkType.MODIFIER, terminal);
				if(modifier!=null)
					modifiers.add(modifier);
			} else if(characterStateChunk.containsOrEquals(terminal)) {
				modifiers.addAll(characterStateChunk.getChunks(ChunkType.MODIFIER));
				return modifiers;
			} else {
				modifiers.clear();
			}
		}
		
		return modifiers;
	}


	private LinkedHashSet<Chunk> getCharacterStatesOf(Chunk organChunk,
			Chunk subjectChunk) {
		LinkedHashSet<Chunk> characterStates = new LinkedHashSet<Chunk>();
		
		boolean isLastOrgan = false;
		for(AbstractParseTree terminal : subjectChunk.getTerminals()) {
			if(subjectChunk.isPartOfChunkType(terminal, ChunkType.ORGAN) && subjectChunk.getChunkOfTypeAndTerminal(ChunkType.ORGAN, terminal).equals(organChunk)) {
				isLastOrgan = true;
			}
			if(subjectChunk.isPartOfChunkType(terminal, ChunkType.ORGAN) && !subjectChunk.getChunkOfTypeAndTerminal(ChunkType.ORGAN, terminal).equals(organChunk)) {
				isLastOrgan = false;
			}
		}
		
		for(AbstractParseTree terminal : subjectChunk.getTerminals()) {
			if(subjectChunk.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE)) {
				Chunk characterState = subjectChunk.getChunkOfTypeAndTerminal(ChunkType.CHARACTER_STATE, terminal);
				if(characterState!=null)
					characterStates.add(characterState);
			}
			if(organChunk.containsOrEquals(terminal) && !isLastOrgan) {
				return characterStates;
			} 
		}
		return characterStates;
	}


	private Chunk getConstraintOf(Chunk organChunk, Chunk subjectChunk) {
		boolean organChunkIsLast = false;
		for(AbstractParseTree terminal : subjectChunk.getTerminals()) {
			if(subjectChunk.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
				Chunk organChunkInSubject = subjectChunk.getChunkOfTypeAndTerminal(ChunkType.ORGAN, terminal);
				if(organChunkInSubject.getTerminalsText().equals(organChunk.getTerminalsText())) {
					organChunkIsLast = false;
				}
				if(organChunkInSubject.equals(organChunk))
					organChunkIsLast = true;
					
			}
		}
		
		LinkedHashSet<Chunk> constraints = new LinkedHashSet<Chunk>();
		if(organChunkIsLast) {
			for(AbstractParseTree terminal : subjectChunk.getTerminals()) {
				if(subjectChunk.isPartOfChunkType(terminal, ChunkType.CONSTRAINT)) {
					Chunk constraintChunk = subjectChunk.getChunkOfTypeAndTerminal(ChunkType.CONSTRAINT, terminal);
					if(constraintChunk!=null)
						constraints.addAll(constraintChunk.getTerminals());
				} else if(terminal.getTerminalsText().equals("and") || terminal.getTerminalsText().equals("or"))
					constraints.add(terminal);
				if(organChunk.containsOrEquals(terminal)) {
					Chunk returnChunk = new Chunk(ChunkType.CONSTRAINT, constraints);
					return returnChunk;
				}
			}
		} else {
			return null;
		}
		return new Chunk(ChunkType.CONSTRAINT);
	}


	protected void addClauseModifierConstraint(Element element, ProcessingContextState processingContextState) {
		String clauseModifierConstraint = processingContextState.getClauseModifierContraint();
		String clauseModifierConstraintId = processingContextState.getClauseModifierContraintId();
		if (clauseModifierConstraint != null)
			if(element.isCharacter())
				((Character)element).setConstraint(clauseModifierConstraint);
			if(element.isStructure())
				((Structure)element).setConstraint(clauseModifierConstraint);
		if (clauseModifierConstraintId != null)
			if(element.isCharacter())
				((Character)element).setConstraintId(clauseModifierConstraintId);
			if(element.isStructure())
				((Structure)element).setConstraintId(clauseModifierConstraintId);
		processingContextState.setClauseModifierContraint(null);
		processingContextState.setClauseModifierContraintId(null);
	}
	
	
	
	protected List<Structure> lastStructures(ProcessingContext processingContext, 
			ProcessingContextState processingContextState) {
		LinkedList<Structure> parents = new LinkedList<Structure>();
		
		boolean newSegment = processingContext.getCurrentState().isCommaAndOrEosEolAfterLastElements();
		if(!newSegment && (processingContextState.getLastElements().size()> 0 && 
				processingContextState.getLastElements().getLast().isStructure())) {
			for(Element lastElement : processingContextState.getLastElements())
				if(lastElement.isStructure())
					parents.add((Structure)lastElement);
		}else{
			parents.addAll(processingContextState.getSubjects());
		}
		return parents;
	}
	
	protected Chunk getLastOrgan(List<Chunk> chunks) {
		for(int i=chunks.size()-1; i>=0; i--) {
			Chunk chunk = chunks.get(i);
			if(chunk.isOfChunkType(ChunkType.ORGAN)) 
				return chunk;
		}
		return null;
	}

	protected LinkedHashSet<Chunk> plusFollowsOrgan(List<Chunk> chunks, ChunkCollector chunkCollector) {
		LinkedHashSet<Chunk> beforePlus = null;
		
		List<AbstractParseTree> terminals = new LinkedList<AbstractParseTree>();
		for(Chunk chunk : chunks) {
			terminals.addAll(chunk.getTerminals());
		}
		
		for(int i=0; i < terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			if(terminal.getTerminalsText().equals("plus") && 
					chunkCollector.isPartOfChunkType(terminals.get(i-1), ChunkType.ORGAN)) {
				return beforePlus;
			}
			
			Chunk chunk = chunkCollector.getChunk(terminal);
			if(beforePlus == null)
				beforePlus = new LinkedHashSet<Chunk>();
			beforePlus.add(chunk);
		}
		
		return null;
	}

	//separate o[......... {m} {m} (o1) and {m} (o2)] to two parts: the last part include all organ names
	//e.g., o[(cypselae) -LSB-/-LSB- {minute} (crowns) -RSB-/-RSB-]
	protected List<LinkedList<Chunk>> separate(Chunk object) {
		ArrayList<LinkedList<Chunk>> twoParts  = new ArrayList<LinkedList<Chunk>>();
		LinkedList<Chunk> nonOrgan = new LinkedList<Chunk>();
		LinkedList<Chunk> organ = new LinkedList<Chunk>();
		
		boolean foundOrgan = false;
		
		List<Chunk> constraintCandidates = new LinkedList<Chunk>();
		
		List<Chunk> chunks = new LinkedList<Chunk>(object.getChunks());
		for(int i=0; i<chunks.size(); i++) {
			Chunk chunk = chunks.get(i);
			Chunk nextChunk = null;
			if(i+1<chunks.size())
				 nextChunk = chunks.get(i+1);
			Chunk previousChunk = null;
			if(i-1>=0)
				previousChunk = chunks.get(i-1);
			
			if(chunk.isOfChunkType(ChunkType.CONSTRAINT) || 
					chunk.getTerminalsText().equals("and") || chunk.getTerminalsText().equals("or") 
					|| chunk.isOfChunkType(ChunkType.COMMA)) {
				constraintCandidates.add(chunk);
			}
			else if(chunk.containsChunkType(ChunkType.ORGAN) || foundOrgan) {
				organ.addAll(constraintCandidates);
				organ.add(chunk);
				foundOrgan = true;
			}
			else {
				nonOrgan.add(chunk);
				constraintCandidates.clear();
			}
		}
		
		if(!nonOrgan.isEmpty()) {
			Chunk lastNonOrgan = nonOrgan.get(nonOrgan.size()-1);
			if(lastNonOrgan.getTerminalsText().equals("a") ||
					lastNonOrgan.getTerminalsText().equals("an")) {
				organ.add(0, lastNonOrgan);
				nonOrgan.remove(lastNonOrgan);
			}
		}
		twoParts.add(nonOrgan);
		twoParts.add(organ);
		return twoParts;
	}
	
	/**
	 * o[.........{m} {m} (o1) and {m} (o2)]
	 * o[each {bisexual} , architecture[{architecture-list-functionally-staminate-punct-or-pistillate}] (floret)]] ; 
	 * @param object
	 * @return the extracted structures
	 */
	protected List<Structure> extractStructuresFromObject(Chunk object, ProcessingContext processingContext, 
			ProcessingContextState processingContextState) {
		ChunkCollector chunkCollector = processingContext.getChunkCollector();
		
		List<Structure> structures;		
		List<LinkedList<Chunk>> twoParts = separate(object);  
		//find the organs in object o[.........{m} {m} (o1) and {m} (o2)]
		
		//log(LogLevel.DEBUG, "twoParts " + twoParts);
		structures = createStructureElements(twoParts.get(1), processingContext, processingContextState);
		// 7-12-02 add cs//to be added structures found in 2nd part, not rewrite this.latestelements yet
		if(!twoParts.get(0).isEmpty()) {
			LinkedList<Structure> structuresCopy = new LinkedList<Structure>(structures);
			
			LinkedHashSet<Chunk> beforePlus = plusFollowsOrgan(twoParts.get(1), chunkCollector);
			if(beforePlus != null) {
				//(teeth) plus 1-2 (bristles), the structure comes after "plus" should be excluded
				LinkedHashSet<Chunk> firstOrgans = beforePlus;
				Chunk lastOrgan = getLastOrgan(twoParts.get(1));
				for(int i = structures.size()-1; i>=0;  i--){
					//log(LogLevel.DEBUG, structures.get(i));
					//log(LogLevel.DEBUG, lastOrgan);
					if(!structures.get(i).getName().equals(inflector.getSingular(lastOrgan.getTerminalsText()))){
						structures.remove(i);
					}
				}
			}
			//processingContext.getCurrentState().setCommaAndOrEosEolAfterLastElements(false);
			processCharacterText(twoParts.get(0), structures, null, processingContextState, processingContext);
			// 7-12-02 add cs //process part 1, which applies to all lateststructures, invisible
			structures = structuresCopy;
		}
		return structures;
	}

	/**
	 * bases and tips mostly rounded 
	 * @param tokens
	 * @param parents
	 */
	protected List<Element> processCharacterText(List<Chunk> tokens, List<Structure> parents, 
			String character, ProcessingContextState processingContextState, ProcessingContext processingContext) {
		LinkedList<Element> results = new LinkedList<Element>();
		//determine characters and modifiers
		List<Chunk> modifiers = new LinkedList<Chunk>();
		
		for(Chunk token : tokens) {
			processingContextState = processingContext.getCurrentState();
			if(token.isOfChunkType(ChunkType.TO_PHRASE)) {
				processingContextState.setLastElements(new LinkedList<Element>(parents));
				processingContextState.setCommaAndOrEosEolAfterLastElements(false);
				IChunkProcessor processor = processingContext.getChunkProcessor(ChunkType.TO_PHRASE);
				List<? extends Element> result = processor.process(token, processingContext);
				results.addAll(result);
				//results = this.processCharacterList(token, parents, processingContextState, processingContext);
			} else {
				List<Chunk> chunkModifiers = token.getChunks(ChunkType.MODIFIER);
				modifiers.addAll(chunkModifiers);
				
				String w = token.getTerminalsText();
				if(token.containsChunkType(ChunkType.STATE))
					w = token.getChunkBFS(ChunkType.STATE).getTerminalsText();
				String tokensCharacter = null;
				if(token.isOfChunkType(ChunkType.CHARACTER_STATE)) {
					tokensCharacter = token.getProperty("characterName");
				} else {
					tokensCharacter = characterKnowledgeBase.getCharacterName(w);
				}
				if(tokensCharacter==null && w.matches("no")){
					tokensCharacter = "presence";
				}
				if(tokensCharacter==null && posKnowledgeBase.isAdverb(w) && !modifiers.contains(token)) {
					//TODO: can be made more efficient, since sometimes character is already given
					modifiers.add(token);
				}else if(w.matches(".*?\\d.*") && !w.matches(".*?[a-z].*")){//TODO: 2 times =>2-times?
					results.addAll(this.annotateNumericals(w, "count", modifiers, parents, false, processingContextState));
					//annotateCount(parents, w, modifiers);
					modifiers.clear();
				}else{
					//String chara = MyPOSTagger.characterhash.get(w);
					if(tokensCharacter != null){
						if(character!=null){
							tokensCharacter = character;
						}
						if(tokensCharacter.equals("characterName") && modifiers.size() ==0) {
							//high relief: character=relief, reset the character of "high" to "relief"
							Element lastelement = null;
							if(results.size() >= 1){
								lastelement = results.getLast();
							}else if(processingContextState.getLastElements().size() >= 1){
								lastelement = processingContextState.getLastElements().getLast();
							}
							if(lastelement != null && lastelement.isCharacter()){
								((Character)lastelement).setName(w);
							}
						}else{
							String[] characterValues = w.split("\\bor\\b|\\band\\b");
							for(String characterValue : characterValues) 
								results.add(createCharacterElement(parents, modifiers, characterValue.trim(), tokensCharacter, "", processingContextState)); 
							//default type "" = individual vaues
							modifiers.clear();
						}
					} else {
						processingContextState.setLastElements(new LinkedList<Element>(parents));
						processingContextState.setCommaAndOrEosEolAfterLastElements(false);
						IChunkProcessor processor = processingContext.getChunkProcessor(token.getChunkType());
						
						if(processor != null) {
							List<? extends Element> result = processor.process(token, processingContext);
							results.addAll(result);
						}
					}
				}
			}
		}
		return results;
	}
	
	/**
	 * crowded to open
	 * for categorical range-value
	 * @param parents
	 * @param results
	 * @param modifier
	 * @param cvalue
	 * @param cname
	 */
	protected List<Character> createRangeCharacterElement(List<Structure> parents,
			List<Chunk> modifiers, String characterValue, String characterName, ProcessingContextState processingContextState) {
		LinkedList<Character> results = new  LinkedList<Character>();
		
		Character character = new Character();
		//if(this.inbrackets){character.setAttribute("in_bracket", "true");}
		character.setCharType("range_value");
		character.setName(characterName);
		
		String[] range = characterValue.split("\\s+to\\s+");//a or b, c, to d, c, e
		String[] tokens = range[0].replaceFirst("\\W$", "").replaceFirst("^.*?\\s+or\\s+", "").split("\\s*,\\s*"); //a or b, c, =>
		String from = getFirstCharacter(tokens[tokens.length-1]);
		tokens = range[1].split("\\s*,\\s*");
		String to = getFirstCharacter(tokens[0]);
		character.setFrom(from.replaceAll("-c-", " ")); //a or b to c => b to c
		character.setTo(to.replaceAll("-c-", " "));

		for(Chunk modifier : modifiers)
			character.appendModifier(modifier.getTerminalsText());
		
		if(parents.isEmpty())
			processingContextState.getUnassignedCharacters().add(character);
		for(Structure parentStructure : parents) {
			parentStructure.addCharacter(character);
		}
		results.add(character); 
		
		addClauseModifierConstraint(character, processingContextState);
		return results;
	}
	
	/**
	 * @param tokens: usually large
	 * @return: large
	 */
	protected String getFirstCharacter(String character) {
		String[] tokens = character.trim().split("\\s+");
		String result = "";
		for(int i = 0; i < tokens.length; i++){
			if(characterKnowledgeBase.getCharacterName(tokens[i]) != null){
				 result += tokens[i]+" ";
			}
		}
		return result.trim();
	}
	
	protected Character createCharacterElement(List<Structure> parents, List<Chunk> modifiers, 
			String characterValue, String characterName, String char_type, ProcessingContextState processingContextState) {
		log(LogLevel.DEBUG, "create character element " + characterName + ": " +  characterValue + " for parent:\n "  + parents);
		String modifierString = "";
		if(modifiers != null) {
			for(Chunk modifier : modifiers)
				modifierString += modifier.getTerminalsText() + "; ";
			if(modifierString.length() >= 2)
				modifierString = modifierString.substring(0, modifierString.length() - 2);
		}
			
		String parenthetical = null;
		Character character = null;
		if(characterValue.indexOf("( ")>=0){
			//contains parenthetical, textual expressions:  lanceolate ( outer ) as part of a character list; brackets in numerical expressions do not have a trailing space
			parenthetical = characterValue.substring(characterValue.indexOf("( ")).trim();
			characterValue = characterValue.substring(0, characterValue.indexOf("( ")).trim();
		}
		if(characterValue.matches("^-[RL][SR]B-/-[RL][SR]B-.*")){ //other textual, parenthetical expressions has -[RL][SR]B-/-[RL][SR]B- as a separate token
			parenthetical = characterValue;
			characterValue = "";
		}
		
		if(characterValue.length() > 0){
			character = new Character();
			//if(this.inbrackets){character.setAttribute("in_bracket", "true");}
			if(characterName.compareTo("count")==0 && characterValue.indexOf("-")>=0 && characterValue.indexOf("-")==characterValue.lastIndexOf("-")){
				String[] values = characterValue.split("-");
				character.setCharType("range_value");
				character.setName(characterName);
				character.setFrom(values[0]);
				if(values[1].endsWith("+")) {
					character.setTo(values[1].substring(0, values[1].length()-1));
					character.setUpperRestricted("false");
				} else
					character.setTo(values[1]);
				
			}else{
				if (characterName.compareTo("size") == 0) {
					String value = characterValue.replaceFirst("\\b(" + units + ")\\b", "").trim(); // 5-10 mm
					String unit = characterValue.replace(value, "").trim();
					if (unit.length() > 0) {
						character.setUnit(unit);
					}
					characterValue = value;
				}else if(characterValue.indexOf("_c_")>=0 && (characterName.equals("color") || characterName.equals("coloration"))){
					//-c- set in SentenceOrganStateMarkup
					String color = characterValue.substring(characterValue.lastIndexOf("_c_")+3); //pale-blue
					String m = characterValue.substring(0, characterValue.lastIndexOf("_c_")); //color = blue m=pale
					
					modifierString = modifierString.length()>0 ? modifierString + "; "+ m : m;
					characterValue = color;
				}
				if(char_type.length() > 0){
					character.setCharType(char_type);
				}
				character.setName(characterName);
				character.setValue(characterValue);
				if(!modifierString.isEmpty())
					character.setModifier(modifierString);
			}
			
			for(Structure parent : parents) {
				parent.addCharacter(character);
			}
			
			/*boolean usedModifiers = false;
			for(DescriptionTreatmentElement parent : parents) {
				if(modifierString.trim().length() >0) {
					character.setProperty("modifier", modifierString);
					usedModifiers = true;
				}
				parent.addTreatmentElement(character);
			}
			if(usedModifiers){
				modifierString = "";
			}*/
			
			addClauseModifierConstraint(character, processingContextState);
		}
		return character;
	}

	
	protected List<Element> linkObjects(List<Structure> subjectStructures, List<Chunk> modifiers, 
			Chunk preposition, Chunk object, boolean lastIsStruct, boolean lastIsChara, 
			ProcessingContext processingContext, ProcessingContextState processingContextState, String relation) {
		LinkedList<Element> result = new LinkedList<Element>();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		ChunkCollector chunkCollector = processingContext.getChunkCollector();
		
		List<Chunk> unassignedModifiers = processingContext.getCurrentState().getUnassignedModifiers();
		modifiers.addAll(unassignedModifiers);
		unassignedModifiers.clear();
		
		List<Structure> structures;
		structures = extractStructuresFromObject(object, processingContext, processingContextState);
		result.addAll(structures);
		String base = "";
		
		if(baseCountWords.contains(object.getTerminalsText())) {
			base = "each";
		}
		if(lastIsChara && !lastElements.isEmpty() && !processingContextState.isCommaAndOrEosEolAfterLastElements()) {
			Character lastElement = (Character)lastElements.getLast();
			//if last character is size, change to location: <margins> r[p[with] o[3�6 (spines)]] 1�3 {mm} r[p[{near}] o[(bases)]]. 
			//1-3 mm is not a size, but a location of spines
			if(lastElement.getName().equals("size") && 
					((lastElement.getValue() != null && lastElement.getValue().matches(".*?\\d.*")) || 
							(lastElement.getFrom() != null && lastElement.getFrom().matches(".*?\\d.*"))) 
				&& locationPrepositions.contains(preposition.getTerminalsText())) {
				lastElement.setName("location");
			}
			
			String modifierString = "";
			for(Chunk modifier : modifiers) {
				modifierString += modifier.getTerminalsText() + " ";
			}
			
			lastElement.setConstraint(modifierString + preposition.getTerminalsText() + " " + listStructureNames(object));
			lastElement.setConstraintId(listStructureIds(structures));
			/*if(!modifiers.isEmpty()) {
				for(Chunk modifier : modifiers) {
					lastElement.appendAttribute("modifier", modifier.getTerminalsText());
				}
			}*/
		} else {			
			if(relation == null)
				relation = relationLabel(preposition, subjectStructures, structures, object, chunkCollector);//determine the relation
			if(relation != null){
				result.addAll(createRelationElements(relation, subjectStructures, structures, modifiers, false, processingContext, processingContextState));//relation elements not visible to outside //// 7-12-02 add cs
				result.addAll(createRelationElements(relation, subjectStructures, structures, modifiers, false, processingContext, processingContextState));//relation elements not visible to outside //// 7-12-02 add cs
			}
			if(relation!= null && relation.compareTo("part_of")==0) 
				structures = subjectStructures; //part_of holds: make the organbeforeof/entity1 the return value, all subsequent characters should be refering to organbeforeOf/entity1
		}
		
		processingContext.getCurrentState().setLastElements(new LinkedList<Element>(structures));
		return result;
	}
	
	protected String relationLabel(Chunk preposition, 
			List<Structure> organsbeforepp, 
			List<Structure> organsafterpp, Chunk object, ChunkCollector chunkCollector) {		
		if(preposition.getTerminalsText().equals("of")) {			
			
			List<Chunk> chunks = chunkCollector.getChunks();
			
			boolean foundChunk = false;
			Chunk beforePPChunk = null;
			Chunk afterPPChunk = null;
			for(int i=0; i<chunks.size(); i++) { 
				Chunk chunk = chunks.get(i);
				if(chunk.containsAll(object.getTerminals())) {
					if(i+1 < chunks.size()) {
						afterPPChunk = chunks.get(i+1);
						break;
					}
					foundChunk = true;
				} else if(!foundChunk)
					beforePPChunk = chunk;
			}
			if(beforePPChunk.getChunkType().equals(ChunkType.PP)) {
				return "part_of";
			}
			if(afterPPChunk!=null && (afterPPChunk.isOfChunkType(ChunkType.END_OF_LINE) || afterPPChunk.isOfChunkType(ChunkType.END_OF_SUBCLAUSE) ||
					afterPPChunk.isOfChunkType(ChunkType.COUNT) || 
					(afterPPChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && afterPPChunk.getProperty("characterName").contains("count"))))
				return "consist_of";
			
			for(Chunk chunk : object.getChunks()) {
				if(chunk.isOfChunkType(ChunkType.COUNT) || (chunk.isOfChunkType(ChunkType.CHARACTER_STATE) && chunk.getProperty("characterName").contains("count"))) { 
					return "consist_of";
				}
			}
			return differentiateOf(organsbeforepp, organsafterpp);
		}
		return preposition.getTerminalsText();
	}
	
	protected String differentiateOf(List<Structure> organsBeforeOf, List<Structure> organsAfterOf) {
		String result = "part_of";
		
		for (int i = 0; i<organsBeforeOf.size(); i++){
			String b = organsBeforeOf.get(i).getName();
			if(clusters.contains(b)){
				result = "consist_of";
				break;
			}
			for(int j = 0; j<organsAfterOf.size(); j++){
				String a = organsAfterOf.get(j).getName();
				//String pattern = a+"[ ]+of[ ]+[0-9]+.*"+b+"[,\\.]"; //consists-of
				if(a.length()>0 && b.length()>0){
					String pb = inflector.getPlural(b);
					String pa = inflector.getPlural(a);
					String pattern = "("+b+"|"+pb+")"+"[ ]+of[ ]+[0-9]+.*"+"("+a+"|"+pa+")"+"[ ]?(,|;|\\.|and|or|plus)"; //consists-of
					
					/*for(HashMap<String, String> sentencesPerTreatment : terminologyLearner.getSentences().values()) {
						for(Entry<String, String> sentenceEntry : sentencesPerTreatment.entrySet()) {
							if(sentenceEntry.getValue().matches(pattern)) {
								result = "consists_of";
								break;
							}
						}
					}*/
					for(String sentence : terminologyLearner.getSentences()) {
						if(sentence.matches(pattern)) {
							result = "consists_of";
							break;
						}
					}
				}
			}
		}

		return result;
	}
	
	protected List<Relation> createRelationElements(String relation, 
			List<Structure> fromStructures, List<Structure> toStructures, 
			List<Chunk> modifiers, boolean symmetric, 
			ProcessingContext processingContext, ProcessingContextState processingContextState) {
		log(LogLevel.DEBUG, "create relation \"" + relation + "\" between: \n" + fromStructures + "\nto\n" + toStructures);
		//add relation elements
		LinkedList<Relation> relationElements = new LinkedList<Relation>();
		for(int i = 0; i < fromStructures.size(); i++) {
			String o1id = fromStructures.get(i).getId();
			for(int j = 0; j<toStructures.size(); j++){
				String o2id = toStructures.get(j).getId();
				boolean negation=false;
				
				Iterator<Chunk> modifiersIterator = modifiers.iterator();
				while(modifiersIterator.hasNext()) {
					Chunk modifier = modifiersIterator.next();
					if(modifier.getTerminalsText().equals("not")) {
						negation = true;
						modifiersIterator.remove();
					}
				}
				if(relation.matches(".*?\\bnot\\b.*")){
					negation = true;
					relation = relation.replace("not", "").trim();
				}
				relationElements.add(addRelation(relation, modifiers, symmetric, o1id, o2id, negation, processingContext, processingContextState));
			}
		}
		return relationElements;
	}
	
	protected Relation addRelation(String relationName, List<Chunk> modifiers,
			boolean symmetric, String o1id, String o2id, boolean negation, ProcessingContext processingContext, ProcessingContextState processingContextState) {
		Relation relation = new Relation();
		relation.setName(relationName);
		relation.setFrom(o1id);
		relation.setTo(o2id);
		relation.setNegation(String.valueOf(negation));
		relation.setId("r" + String.valueOf(processingContext.fetchAndIncrementRelationId(relation)));	
		
		for(Chunk modifier : modifiers) {
			relation.appendModifier(modifier.getTerminalsText());
		}
		
		addClauseModifierConstraint(relation, processingContextState);
		return relation;
	}
	
	protected boolean isNumerical(Chunk object) {
		return object.getTerminalsText().matches("\\d+");
	}

	
	protected List<Character> annotateNumericals(String text, String characterString, List<Chunk> modifiers, 
		List<Structure> parents, boolean resetFrom, ProcessingContextState processingContextState) {
		LinkedList<Character> result = new LinkedList<Character>();
			
		List<Character> characters = parseNumericals(text, characterString);
		if(characters.size()==0){//failed, simplify chunktext
			characters = parseNumericals(text, characterString);
		}
		
		for(Character character : characters) {
			if(resetFrom && character.getFrom() != null && character.getFrom().equals("0") && 
					(character.getFromInclusive()==null || character.getFromInclusive().equals("true"))) {// to 6[-9] m.
				character.setFrom(null);
				if(character.getFromUnit()!=null){
					character.setFromUnit(null);
				}
			}
			for(Chunk modifier : modifiers) {
				character.appendModifier(modifier.getTerminalsText());
			}

			addClauseModifierConstraint(character, processingContextState);
			for(Structure parent : parents) {
				parent.addCharacter(character);
			}
			result.add(character);
		}
		return result;
	}


	
	/**
	 * 
	 * @param numberexp : styles 2[10] mm diam.
	 * @param cname: 
	 * @return: characters marked up in XML format <character name="" value="">
	 */
	protected List<Character> parseNumericals(String numberexp, String cname){	
		LinkedList<Character> innertagstate = new LinkedList<Character>();
		int i,j;
		numberexp = numberexp.replaceAll("\\([\\s]?|\\[[\\s]?", "[");
		numberexp = numberexp.replaceAll("[\\s]?\\)|[\\s]?\\]", "]").trim();
		
		//4-5[+] => 4-5[-5+]
		Pattern p1 = Pattern.compile("(.*?\\b(\\d+))\\s*\\[\\+\\](.*)");
		Matcher m = p1.matcher(numberexp);
		if(m.matches()){
			numberexp = m.group(1)+"[-"+m.group(2)+"+]"+m.group(3);
			m = p1.matcher(numberexp);
		}
		//1-[2-5] => 1-1[2-5] => 1[2-5]
		//1-[4-5] => 1-3[4-5] 
		p1 = Pattern.compile("(.*?)(\\d+)-(\\[(\\d)-.*)");
		m = p1.matcher(numberexp);
		if(m.matches()){
			int n = Integer.parseInt(m.group(4))-1;
			if(n==Integer.parseInt(m.group(2))){
				numberexp = m.group(1)+n+m.group(3);
			}else{
				numberexp = m.group(1)+m.group(2)+"-"+n+m.group(3);
			}
		}
		
		///////////////////////////////////////////////////////////////////
		//      area                                               ////////
		
		Pattern pattern19 = Pattern.compile("([ \\d\\.\\[\\]+-]+\\s*([cmdµu]?m?))\\s*[×x]?(\\s*[ \\d\\.\\[\\]+-]+\\s*([cmdµu]?m?))?\\s*[×x]\\s*([ \\d\\.\\[\\]+-]+\\s*([cmdµu]?m))");
		Matcher matcher2 = pattern19.matcher(numberexp);
		if(matcher2.matches()){
			//get l, w, and h
			String width = "";
			String height = "";
			String lunit = "";
			String wunit = "";
			String hunit = "";
			String length = matcher2.group(1).trim();
			String g5 = matcher2.group(5).trim();
			if(matcher2.group(3)==null){
				width = g5;
			}else{
				width = matcher2.group(3);
				height = g5;
			}
			//make sure each has a unit
			if(height.length()==0){//2 dimensions
				wunit = matcher2.group(6);
				if(matcher2.group(2)==null || matcher2.group(2).trim().length()==0){
					lunit = wunit;
				}else{
					lunit = matcher2.group(2);
				}
			}else{//3 dimensions
				hunit = matcher2.group(6);
				if(matcher2.group(4)==null || matcher2.group(4).trim().length()==0){
					wunit = hunit;
				}else{
					wunit = matcher2.group(4);
				}
				if(matcher2.group(2)==null || matcher2.group(2).trim().length()==0){
					lunit = wunit;
				}else{
					lunit = matcher2.group(2);
				}
			}
			//format expression value+unit
			length = length.matches(".*[cmdµ]?m$")? length : length + " "+lunit;
			width = width.matches(".*[cmdµ]?m$")? width : width + " "+wunit;
			if(height.length()>0) height = height.matches(".*[cmdµ]?m$")? height : height + " "+hunit;
			
			//annotation
			annotateSize(length, innertagstate, "length");
			annotateSize(width, innertagstate, "width");
			if(height.length()>0) annotateSize(height, innertagstate, "height");
			
			numberexp = matcher2.replaceAll("#");
        	matcher2.reset();
		}
    	
    	////////////////////////////////////////////////////////////////////////////////////
    	//   ratio                                                              ////////////
    	Pattern pattern24 = Pattern.compile("l/w[\\s]?=[\\d\\.\\s\\+\\–\\-]+");
    	matcher2 = pattern24.matcher(numberexp);
    	while ( matcher2.find()){
    		if(numberexp.charAt(matcher2.start())==' '){
    			i=matcher2.start()+1;
    		}
    		else{
    			i=matcher2.start();
    		}
    		j=matcher2.end();
    		String match = numberexp.substring(i, j);
    		int en = match.indexOf('-');
    		if (match.contains("+")){
    			Character character = new Character();
    			character.setCharType("range_value");
    			character.setName("l_w_ratio");
    			//character.setAttribute("from", match.substring(match.indexOf('=')+2,en).trim());
    			character.setFrom(match.substring(match.indexOf('=')+1,en).trim());
    			character.setTo(match.substring(en+1, match.indexOf('+',en+1)).trim());
    			character.setUpperRestricted("false");
    			innertagstate.add(character);
    			//innertagstate=innertagstate.concat("<character char_type=\"range_value\" name=\"l_w_ratio\" from=\""+match.substring(match.indexOf('=')+2,en).trim()+"\" to=\""+match.substring(en+1, match.indexOf('+',en+1)).trim()+"\" upper_restricted=\"false\"/>");
    		}else{
    			Character character = new Character();
    			character.setCharType("range_value");
    			character.setName("l_w_ratio");
    			//character.setAttribute("from", match.substring(match.indexOf('=')+2,en).trim());
    			character.setFrom(match.substring(match.indexOf('=')+1,en).trim());
    			character.setTo(match.substring(en+1, match.indexOf(' ',en+1)).trim());
    			innertagstate.add(character);
    			//innertagstate=innertagstate.concat("<character char_type=\"range_value\" name=\"l_w_ratio\" from=\""+match.substring(match.indexOf('=')+2,en).trim()+"\" to=\""+match.substring(en+1, match.indexOf(' ',en+1)).trim()+"\"/>");
       		}
    	}
    	numberexp = matcher2.replaceAll("#");
    	matcher2.reset();
    	
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// size: deal with  "[5-]10-15[-20] cm", not deal with "5 cm - 10 cm"                        ////////////
    	//int sizect = 0;
		String toval;
		String fromval;
		numberexp = annotateSize(numberexp, innertagstate, "size");
    	
    	
    	
    	
    	
    	////////////////////////////////////////////////////////////////////////////////////////////
    	//   size                                                                             /////
    	Pattern pattern14 = Pattern.compile("[±\\d\\[\\]\\–\\-\\./\\s]+[\\s]?[\\–\\-]?(% of [\\w]+ length|height of [\\w]+|times as [\\w]+ as [\\w]+|total length|their length|(times)?[\\s]?length of [\\w]+)");
    	matcher2 = pattern14.matcher(numberexp);
    	toval="";
    	fromval="";
    	while ( matcher2.find()){
    		if(numberexp.charAt(matcher2.start())==' '){
    			i=matcher2.start()+1;
    		}
    		else{
    			i=matcher2.start();
    		}
    		j=matcher2.end();
    		String extreme = numberexp.substring(i,j);
			i = 0;
			j = extreme.length();
			Pattern pattern20 = Pattern.compile("\\[[±\\d\\.\\s\\+]+[\\–\\-]{1}[±\\d\\.\\s\\+\\–\\-]*\\]");
        	Matcher matcher1 = pattern20.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		if(extreme.charAt(q-2)=='–' | extreme.charAt(q-2)=='-'){
        			Character character = new Character();
        			character.setCharType("relative_range_value");
        			character.setName("atypical_size");
        			character.setFrom(extreme.substring(p+1,q-2).trim());
        			character.setTo("");
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" to=\"\"/>");
        		}else{
        			Character character = new Character();
        			character.setCharType("relative_range_value");
        			character.setName("atypical_size");
        			character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
        			character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
        			innertagstate.add(character);
        		    //innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
        		}
        	}
        	extreme = matcher1.replaceAll("#");
    		matcher1.reset();
    		if(extreme.contains("#"))
    			i = extreme.indexOf("#")+1;
    		Pattern pattern21 = Pattern.compile("\\[[�\\d\\.\\s\\+\\�\\-]*[\\�\\-]{1}[�\\d\\.\\s\\+]+\\]");
        	matcher1 = pattern21.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		if (extreme.charAt(p+1)=='�' | extreme.charAt(p+1)=='-'){
        			if (extreme.charAt(q-2)=='+'){
            			Character character = new Character();
            			character.setCharType("relative_range_value");
            			character.setName("atypical_size");
            			character.setFrom("");
            			character.setTo(extreme.substring(p+2,q-2).trim());
            			character.setUpperRestricted("false");
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-2).trim()+"\" upper_restricted=\"false\"/>");
        			}else{
            			Character character = new Character();
            			character.setCharType("relative_range_value");
            			character.setName("atypical_size");
            			character.setFrom("");
            			character.setTo(extreme.substring(p+2,q-1).trim());
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-1).trim()+"\"/>");
        			}
        		}
        		else{
        			if (extreme.charAt(q-2)=='+'){
            			Character character = new Character();
            			character.setCharType("relative_range_value");
            			character.setName("atypical_size");
            			character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
            			character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim());
            			character.setUpperRestricted("false");
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
        			}else{
            			Character character = new Character();
            			character.setCharType("relative_range_value");
            			character.setName("atypical_size");
            			character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
            			character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim() );
            			//character.setAttribute("upper_restricted", "true");
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
        		
        			}
        		}
        	}
        	extreme = matcher1.replaceAll("#");
    		matcher1.reset();
    		j = extreme.length();
    		Pattern pattern23 = Pattern.compile("\\[[±\\d\\.\\s\\+]+\\]");
        	matcher1 = pattern23.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		if (extreme.charAt(q-2)=='+'){
        			Character character = new Character();
        			character.setCharType("relative_value");
        			character.setName("atypical_size");
        			character.setFrom(extreme.substring(p+1,q-2).trim());
        			character.setUpperRestricted("false");
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character char_type=\"relative_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
        		}else{
        			Character character = new Character();
        			character.setCharType("relative_value");
        			character.setName("atypical_size");
        			character.setValue(extreme.substring(p+1,q-1).trim());
        			innertagstate.add(character);
    				//innertagstate = innertagstate.concat("<character char_type=\"relative_value\" name=\"atypical_size\" value=\""+extreme.substring(p+1,q-1).trim()+"\"/>");
        		}
        	}
        	extreme = matcher1.replaceAll("#");
        	matcher1.reset();
        	j = extreme.length();      	
        	if(extreme.substring(i,j).contains("–")|extreme.substring(i,j).contains("-") && !extreme.substring(i,j).contains("×") && !extreme.substring(i,j).contains("x") && !extreme.substring(i,j).contains("X")){
        		String extract = extreme.substring(i,j);
        		Pattern pattern18 = Pattern.compile("[\\s]?[\\–\\-]?(% of [\\w]+ length|height of [\\w]+|times as [\\w]+ as [\\w]+|total length|their length|(times)?[\\s]?length of [\\w]+)");
        		Matcher matcher3 = pattern18.matcher(extract);
            	String relative="";
            	if ( matcher3.find()){
            		relative = extract.substring(matcher3.start(), matcher3.end());
            	}
            	extract = matcher3.replaceAll("#");
            	matcher3.reset();
            	
    			Character character = new Character();
    			character.setCharType("relative_range_value");
    			character.setName("size");
    			character.setFrom(extract.substring(0, extract.indexOf('-')).trim());
    			character.setTo(extract.substring(extract.indexOf('-')+1,extract.indexOf('#')).trim());
    			//character.setRelativeConstraint("relative_constraint",relative.trim());
    			innertagstate.add(character);
            	//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"size\" from=\""+extract.substring(0, extract.indexOf('-')).trim()+"\" to=\""+extract.substring(extract.indexOf('-')+1,extract.indexOf('#')).trim()+"\" relative_constraint=\""+relative.trim()+"\"/>");
    			toval = extract.substring(0, extract.indexOf('-'));
    			fromval = extract.substring(extract.indexOf('-')+1,extract.indexOf('#'));
            	//sizect+=1;
    		}
    		else{
    			String extract = extreme.substring(i,j);
    			Pattern pattern18 = Pattern.compile("[\\s]?[\\–\\-]?(% of [\\w]+ length|height of [\\w]+|times as [\\w]+ as [\\w]+|total length|their length|(times)?[\\s]?length of [\\w]+)"); 
    			Matcher matcher3 = pattern18.matcher(extract);
            	String relative="";
            	if ( matcher3.find()){
            		relative = extract.substring(matcher3.start(), matcher3.end());
            	}
            	extract = matcher3.replaceAll("#");
            	matcher3.reset();
    			Character character = new Character();
    			character.setCharType("relative_value");
    			character.setName("size");
    			character.setValue(extract.substring(0,extract.indexOf('#')).trim());
    			//character.setRelativeConstraint("relative_constraint", relative.trim());
    			innertagstate.add(character);
            	//innertagstate = innertagstate.concat("<character char_type=\"relative_value\" name=\"size\" value=\""+extract.substring(0,extract.indexOf('#')).trim()+"\" relative_constraint=\""+relative.trim()+"\"/>");
    			toval = extract.substring(0,extract.indexOf('#'));
    			fromval = extract.substring(0,extract.indexOf('#'));
    		}
    		
        	for(Character character : innertagstate) {
    			if(character.getTo() != null && character.getTo().isEmpty()){
    				if(toval.endsWith("+")){
    					toval = toval.replaceFirst("\\+$", "");
    					character.setUpperRestricted("false");
    				}
    				character.setTo(toval.trim());
    				character.setToInclusive("false");
    			}
    			if(character.getFrom() != null && character.getFrom().isEmpty()){
    				character.setFrom(fromval.trim());
    				character.setFromInclusive("false");
    			}
    		}
        	
    		/*StringBuffer sb = new StringBuffer();
			Pattern pattern25 = Pattern.compile("to=\"\"");
			matcher1 = pattern25.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb, "to=\""+toval.trim()+"\"");
			}
			matcher1.appendTail(sb);
			innertagstate=sb.toString();
			matcher1.reset();
			StringBuffer sb1 = new StringBuffer();
			Pattern pattern26 = Pattern.compile("from=\"\"");
			matcher1 = pattern26.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb1, "from=\""+fromval.trim()+"\"");
			}
			matcher1.appendTail(sb1);
			innertagstate=sb1.toString();
			matcher1.reset();*/
    	}
    	numberexp = matcher2.replaceAll("#");
    	matcher2.reset();
    
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//   count                                                                                             ///////////////
    	/*p1 = Pattern.compile("^\\[(\\d+)\\](.*)");
    	m = p1.matcher(numberexp);
    	if(m.matches()){
    		Element character = new Element("characterName");
			character.setAttribute("name", "atypical_"+(cname==null?"count": cname));
			character.setAttribute("value", m.group(1));
			innertagstate.add(character);
			numberexp = m.group(2).trim();
    	}
    	
    	
    	p1 = Pattern.compile("^\\[(\\d+)\\+\\](.*)");
    	m = p1.matcher(numberexp);
    	if(m.matches()){
    		Element character = new Element("characterName");
    		character.setAttribute("char_type", "range_value");
			character.setAttribute("name", "atypical_"+(cname==null?"count": cname));
			character.setAttribute("from", m.group(1));
			character.setAttribute("upper_restricted", "false");
			innertagstate.add(character);
			numberexp = m.group(2);
    	}*/
    	
    	
    	//int countct = 0;
    	Pattern pattern15 = Pattern.compile("([\\[]?[±]?[\\d]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d]+[+]?[\\]]?|[\\[]?[±]?[\\d]+[+]?[\\]]?[\\s]?)[\\–\\–\\-]+[a-zA-Z]+");
    	matcher2 = pattern15.matcher(numberexp);
    	numberexp = matcher2.replaceAll("#");
    	matcher2.reset();     	
    	//Pattern pattern16 = Pattern.compile("(?<!([/][\\s]?))([\\[]?[�]?[\\d]+[\\]]?[\\s]?[\\[]?[\\�\\-][\\]]?[\\s]?[\\[]?[\\d]+[+]?[\\]]?[\\s]?([\\[]?[\\�\\-]?[\\]]?[\\s]?[\\[]?[\\d]+[+]?[\\]]?)*|[�]?[\\d]+[+]?)(?!([\\s]?[n/]|[\\s]?[\\�\\-]?% of [\\w]+ length|[\\s]?[\\�\\-]?height of [\\w]+|[\\s]?[\\�\\-]?times|[\\s]?[\\�\\-]?total length|[\\s]?[\\�\\-]?their length|[\\s]?[\\�\\-]?(times)?[\\s]?length of|[\\s]?[dcm�]?m))");
    	//Pattern pattern16 = Pattern.compile("(?<!([/][\\s]?))([\\[]?[�]?[\\d\\./%]+[\\]]?[\\s]?[\\[]?[\\�\\-][\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?[\\s]?([\\[]?[\\�\\-]?[\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?)*|[�]?[\\d\\./%]+[+]?)(?!([\\s]?[n/]|[\\s]?[\\�\\-]?% of [\\w]+ length|[\\s]?[\\�\\-]?height of [\\w]+|[\\s]?[\\�\\-]?times|[\\s]?[\\�\\-]?total length|[\\s]?[\\�\\-]?their length|[\\s]?[\\�\\-]?(times)?[\\s]?length of|[\\s]?[dcm�]?m))");
    	Pattern pattern16 = Pattern.compile("(?<!([/][\\s]?))([\\[]?[±]?[\\d\\./%]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?[\\s]?([\\[]?[\\–\\-]?[\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?)*|\\[?[±]?[\\d\\./%]+[+]?\\]?)(?!([\\s]?[n/]|[\\s]?[\\–\\-]?% of [\\w]+ length|[\\s]?[\\–\\-]?height of [\\w]+|[\\s]?[\\–\\-]?times|[\\s]?[\\–\\-]?total length|[\\s]?[\\–\\-]?their length|[\\s]?[\\–\\-]?(times)?[\\s]?length of|[\\s]?[dcmµ]?m))");
    	matcher2 = pattern16.matcher(numberexp);
    	while ( matcher2.find()){
    		i=matcher2.start();
    		j=matcher2.end();
    		String extreme = numberexp.substring(i,j);
			i = 0;
			j = extreme.length();
    		Pattern pattern20 = Pattern.compile("\\[[±\\d\\.\\s\\+]+[\\–\\-]{1}[±\\d\\.\\s\\+\\–\\-]*\\]");
        	Matcher matcher1 = pattern20.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		if(extreme.charAt(q-2)=='–' | extreme.charAt(q-2)=='-'){
        			Character character = new Character();
        			character.setCharType("range_value");
        			character.setName("atypical_"+(cname==null?"count": cname));
        			character.setFrom(extreme.substring(p+1,q-2).trim());
        			character.setTo("");
        			innertagstate.add(character);
        			
        			//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,q-2).trim()+"\" to=\"\"/>");
        		}else{
        			Character character = new Character();
        			character.setCharType("range_value");
        			character.setName("atypical_"+(cname==null?"count": cname));
        			character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
        			String tmp = extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim();
        			character.setTo(tmp.replaceFirst("[^0-9]+$", ""));
        			if(tmp.endsWith("+")){
        				character.setUpperRestricted("false");
        			}
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
        		}
        	}
        	extreme = matcher1.replaceAll("#");
    		matcher1.reset();
    		if(extreme.contains("#"))
    			i = extreme.indexOf("#")+1;
    		j = extreme.length(); //process from # to the end of extreme. but in 1-[2-5] (1-#), the value is before #
    		Pattern pattern21 = Pattern.compile("\\[[±\\d\\.\\s\\+\\–\\-]*[\\–\\-]{1}[±\\d\\.\\s\\+]+\\]");
        	matcher1 = pattern21.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		j = p;
        		if (extreme.charAt(p+1)=='–' | extreme.charAt(p+1)=='-'){
        			if (extreme.charAt(q-2)=='+'){
            			Character character = new Character();
            			character.setCharType("range_value");
            			character.setName("atypical_"+(cname==null?"count": cname));
            			character.setFrom("");
            			character.setTo(extreme.substring(p+2,q-2).trim());
            			character.setUpperRestricted("false");
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\"\" to=\""+extreme.substring(p+2,q-2).trim()+"\" upper_restricted=\"false\"/>");
        			}else{
            			Character character = new Character();
            			character.setCharType("range_value");
            			character.setName("atypical_"+(cname==null?"count": cname));
            			character.setFrom("");
            			character.setTo(extreme.substring(p+2,q-1).trim());
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\"\" to=\""+extreme.substring(p+2,q-1).trim()+"\"/>");
        			}
        		}
        		else{
        			if (extreme.charAt(q-2)=='+'){
            			Character character = new Character();
            			character.setCharType("range_value");
            			character.setName("atypical_"+(cname==null?"count": cname));
            			character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
            			character.setTo(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
            			character.setUpperRestricted("false");
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
        			}else{
            			Character character = new Character();
            			character.setCharType("range_value");
            			character.setName("atypical_"+(cname==null?"count": cname));
            			character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
            			character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
        			}
        		}
        			
        	}
    		matcher1.reset();
    		Pattern pattern23 = Pattern.compile("\\[[±\\d\\.\\s\\+]+\\]");
        	matcher1 = pattern23.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		j = p;
        		if (extreme.charAt(q-2)=='+'){
        			Character character = new Character();
        			character.setCharType("range_value");
        			character.setName("atypical_"+(cname==null?"count": cname));
        			character.setFrom(extreme.substring(p+1,q-2).trim());
        			character.setUpperRestricted("false");
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character name=\"atypical_count\" from=\""+extreme.substring(p+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
        		}else{
        			Character character = new Character();
        			character.setName("atypical_"+(cname==null?"count": cname));
        			character.setValue(extreme.substring(p+1,q-1).trim());
           			innertagstate.add(character);
    				//innertagstate = innertagstate.concat("<character name=\"atypical_count\" value=\""+extreme.substring(p+1,q-1).trim()+"\"/>");
        		}
        	}
        	matcher1.reset();
        	//# to the end
        	String extract = extreme.substring(i,j);
        	
        	if(extract.contains("–")|extract.contains("-") && !extract.contains("×") && !extract.contains("x") && !extract.contains("X")){
    			//String extract = extreme.substring(i,j);
    			Pattern pattern22 = Pattern.compile("[\\[\\]]+");
    			matcher1 = pattern22.matcher(extract);
    			extract = matcher1.replaceAll("");
    			matcher1.reset();
    			
    			String to = extract.substring(extract.indexOf('-')+1,extract.length()).trim();
    			boolean upperrestricted = true;
    			if(to.endsWith("+")){
    				upperrestricted = false;
    				to = to.replaceFirst("\\+$", "");
    			}
    			Character character = new Character();
    			character.setCharType("range_value");
    			character.setName(cname==null?"count": cname);
    			character.setFrom(extract.substring(0, extract.indexOf('-')).trim());
    			character.setTo(to);
    			if(!upperrestricted)
    				character.setUpperRestricted(upperrestricted+"");
    			innertagstate.add(character);
            	//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"count\" from=\""+extract.substring(0, extract.indexOf('-')).trim()+"\" to=\""+extract.substring(extract.indexOf('-')+1,extract.length()).trim()+"\"/>");
    			toval = extract.substring(0, extract.indexOf('-'));
    			fromval = extract.substring(extract.indexOf('-')+1,extract.length());
    			//countct+=1;
    		}else{
    			//String extract = extreme.substring(i,j).trim();
    			if(extract.length()>0){
        			Character character = new Character();
        			character.setName(cname==null?"count": cname);
        			if(extract.endsWith("+")){
        				extract = extract.replaceFirst("\\+$", "").trim();
        				character.setCharType("range_value");
        				character.setFrom(extract);
        				character.setUpperRestricted("false");
        			}else{
        				character.setValue(extract);
        			}
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character name=\"count\" value=\""+extract.trim()+"\"/>");
        			toval = extract;
        			fromval = extract;
    			}
    		}
    		//start to #, dupllicated above
    		if(i-1>0){
    		extract = extreme.substring(0, i-1);
    		if(extract.contains("–")|extract.contains("-") && !extract.contains("×") && !extract.contains("x") && !extract.contains("X")){
    			//String extract = extreme.substring(i,j);
    			Pattern pattern22 = Pattern.compile("[\\[\\]]+");
    			matcher1 = pattern22.matcher(extract);
    			extract = matcher1.replaceAll("");
    			matcher1.reset();
    			
    			String to = extract.substring(extract.indexOf('-')+1,extract.length()).trim();
    			boolean upperrestricted = true;
    			if(to.endsWith("+")){
    				upperrestricted = false;
    				to = to.replaceFirst("\\+$", "");
    			}
    			Character character = new Character();
    			character.setCharType("range_value");
    			character.setName(cname==null?"count": cname);
    			character.setFrom(extract.substring(0, extract.indexOf('-')).trim());
    			character.setTo(to);
    			if(!upperrestricted)
    				character.setUpperRestricted(upperrestricted+"");
    			innertagstate.add(character);
            	//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"count\" from=\""+extract.substring(0, extract.indexOf('-')).trim()+"\" to=\""+extract.substring(extract.indexOf('-')+1,extract.length()).trim()+"\"/>");
    			toval = extract.substring(0, extract.indexOf('-'));
    			fromval = extract.substring(extract.indexOf('-')+1,extract.length());
    			//countct+=1;
    		}else{
    			//String extract = extreme.substring(i,j).trim();
    			if(extract.length()>0){
        			Character character = new Character();
        			character.setName(cname==null?"count": cname);
        			if(extract.endsWith("+")){
        				extract = extract.replaceFirst("\\+$", "").trim();
        				character.setCharType("range_value");
        				character.setFrom(extract);
        				character.setUpperRestricted("false");
        			}else{
        				character.setValue(extract);
        			}
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character name=\"count\" value=\""+extract.trim()+"\"/>");
        			toval = extract;
        			fromval = extract;
    			}
    		}
    		}
    		
    		for(Character character : innertagstate) {
    			if(character.getTo() != null && character.getTo().isEmpty()){
    				if(toval.endsWith("+")){
    					toval = toval.replaceFirst("\\+$", "");
    					character.setUpperRestricted("false");
    				}
    				character.setTo(toval.trim());
    				character.setToInclusive("false");
    			}
    			if(character.getFrom() != null && character.getFrom().isEmpty()){
    				character.setFrom(fromval.trim());
    				character.setFromInclusive("false");
    			}
    		}
    		/*
    		StringBuffer sb = new StringBuffer();
			Pattern pattern25 = Pattern.compile("to=\"\"");
			matcher1 = pattern25.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb, "to=\""+toval.trim()+"\"");
			}
			matcher1.appendTail(sb);
			innertagstate=sb.toString();
			matcher1.reset();
			StringBuffer sb1 = new StringBuffer();
			Pattern pattern26 = Pattern.compile("from=\"\"");
			matcher1 = pattern26.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb1, "from=\""+fromval.trim()+"\"");
			}
			matcher1.appendTail(sb1);
			innertagstate=sb1.toString();
			matcher1.reset();*/
    	}
    	matcher2.reset();   

 		return innertagstate;
	}

	//find all () in object
	protected String listStructureNames(Chunk object){
		String organString = "";
		for(Chunk objectChunk : object.getChunks()) {
			if(objectChunk.isOfChunkType(ChunkType.CONSTRAINT))
				organString += objectChunk.getTerminalsText() + " ";
			if(objectChunk.isOfChunkType(ChunkType.ORGAN))
				organString += objectChunk.getTerminalsText() + ", ";
		}
		return organString.trim().replaceFirst(",$", "");
	}
	
	protected String listStructureIds(List<Structure> structures) {
		StringBuffer list = new StringBuffer();
		for(Structure structure : structures)
			list.append(structure.getId()).append(", ");
		return list.toString().trim().replaceFirst(",$", "");
	}

	/**
	 * 
	 * @param ckstring:r[p[in] o[outline]]
	 * @return if character has sufficiently been dealt with
	 */
	protected boolean characterPrep(Chunk chunk, ProcessingContextState processingContextState) {
		boolean done = false;
		
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		Set<String> characters = glossary.getWords("character");
		
		List<AbstractParseTree> terminals = chunk.getTerminals();
		if(terminals.size() < 2)
			return done;
		String lastWord = terminals.get(terminals.size()-1).getTerminalsText();
		if(characters.contains(lastWord)) {
			if(!lastElements.isEmpty()) {
				Element lastElement = lastElements.getLast();
				if(lastElement.isCharacter()) {//shell oval in outline
					for(Element element : lastElements) {
						if(element.isNamedElement())
							((NamedElement)element).setName(lastWord);
					}
					done = true;
				}else if(lastElement.isStructure()) {//shell in oval outline
					//String cvalue = ..
					Chunk characterValue = chunk.getChunkDFS(ChunkType.PREPOSITION);
							//ckstring.replaceFirst(".*?\\]", "").replaceAll("\\w+\\[","").replaceAll(lastword, "").replaceAll("[{}\\]\\[]", "");
					for(Element element : lastElements) {
						if(element.isCharacter()) {
							((Character)element).setName(lastWord);
							((Character)element).setValue(characterValue.getTerminalsText());
						}
					}
					done = true;
				}
			}
		}
		return done;
	}
	
	protected String annotateSize(String plaincharset, List<Character> innertagstate, String chara) {
		int i;
		int j;
		Matcher matcher2;
		Pattern pattern13 = Pattern.compile("[xX\\×±\\d\\[\\]\\–\\-\\.\\s\\+]+[\\s]?([dcmµ]?m)(?![\\w])(([\\s]diam)?([\\s]wide)?)");
		matcher2 = pattern13.matcher(plaincharset);
		String toval="";
		String fromval="";
		while ( matcher2.find()){
			String unit = matcher2.group(1);
			if(plaincharset.charAt(matcher2.start())==' '){
				i=matcher2.start()+1;
			}
			else{
				i=matcher2.start();
			}
			j=matcher2.end();
			String extreme = plaincharset.substring(i,j);
			i = 0;
			j = extreme.length();
			Pattern pattern20 = Pattern.compile("\\[[±\\d\\.\\s\\+]+[\\–\\-]{1}[±\\d\\.\\s\\+\\–\\-]*\\]");
			Matcher matcher1 = pattern20.matcher(extreme);
			if ( matcher1.find()){
				int p = matcher1.start();
				int q = matcher1.end();
				if(extreme.charAt(q-2)=='–' | extreme.charAt(q-2)=='-'){
					Character character = new Character();
					character.setCharType("range_value");
					character.setName("atypical_"+chara);
					character.setFrom(extreme.substring(p+1,q-2).trim());
					character.setTo("");
					character.setFromUnit(unit);
					character.setToUnit(unit);
					//character.setAttribute("upper_restricted", "false");
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" to=\"\"/>");
				}else{
					Character character = new Character();
					character.setCharType("range_value");
					character.setName("atypical_"+chara);
					character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
					character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
					character.setFromUnit(unit);
					character.setToUnit(unit);
					//character.setAttribute("upper_restricted", "??");
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
				}
			}
			extreme = matcher1.replaceAll("#");
			matcher1.reset();
			if(extreme.contains("#"))
				i = extreme.indexOf("#")+1;
			Pattern pattern21 = Pattern.compile("\\[[±\\d\\.\\s\\+\\–\\-]*[\\–\\-]{1}[±\\d\\.\\s\\+]+\\]");
			matcher1 = pattern21.matcher(extreme);
			if ( matcher1.find()){
				int p = matcher1.start();
				int q = matcher1.end();
				if (extreme.charAt(p+1)=='–' | extreme.charAt(p+1)=='-'){
					if (extreme.charAt(q-2)=='+'){
						Character character = new Character();
		    			character.setCharType("range_value");
		    			character.setName("atypical_"+chara);
		    			character.setFrom("");
		    			character.setTo(extreme.substring(p+2,q-2).trim());
		    			character.setFromUnit(unit);
		    			character.setToUnit(unit);
		    			character.setUpperRestricted("false");
		    			innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-2).trim()+"\" upper_restricted=\"false\"/>");
					}else{
						Character character = new Character();
		    			character.setCharType("range_value");
		    			character.setName("atypical_"+chara);
		    			character.setFrom("");
		    			character.setTo(extreme.substring(p+2,q-1).trim());
		    			character.setFromUnit(unit);
		    			character.setToUnit(unit);
		    			//character.setAttribute("upper_restricted", "true");
		    			innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-1).trim()+"\"/>");
					}
				}
				else{
					if (extreme.charAt(q-2)=='+'){
						Character character = new Character();
		    			character.setCharType("range_value");
		    			character.setName("atypical_"+chara);
		    			character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
		    			character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim());
		    			character.setFromUnit(unit);
		    			character.setToUnit(unit);
		    			character.setUpperRestricted("false");
		    			innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
					}else{
						Character character = new Character();
		    			character.setCharType("range_value");
		    			character.setName("atypical_"+chara);
		    			character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
		    			character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
		    			character.setFromUnit(unit);
		    			character.setToUnit(unit);
		    			//character.setAttribute("upper_restricted", "true");
		    			innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
					}
				}
			}
			extreme = matcher1.replaceAll("#");
			matcher1.reset();
			j = extreme.length();
			Pattern pattern23 = Pattern.compile("\\[[±\\d\\.\\s\\+]+\\]");
			matcher1 = pattern23.matcher(extreme);
			if ( matcher1.find()){
				int p = matcher1.start();
				int q = matcher1.end();
				if (extreme.charAt(q-2)=='+'){
					Character character = new Character();
					character.setName("atypical_"+chara);
					character.setFrom(extreme.substring(p+1,q-2).trim());
					character.setTo("");
					character.setFromUnit(unit);
					character.setToUnit(unit);
					character.setUpperRestricted("false");
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
				}else{
					Character character = new Character();
					character.setName("atypical_"+chara);
					character.setValue(extreme.substring(p+1,q-1).trim());
					character.setUnit(unit);
					//character.setAttribute("unit", extreme.substring(q-1).trim());
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character name=\"atypical_size\" value=\""+extreme.substring(p+1,q-1).trim()+"\"/>");
				}
			}
			extreme = matcher1.replaceAll("#");
			matcher1.reset();
			j = extreme.length();
			if(extreme.substring(i,j).contains("–")|extreme.substring(i,j).contains("-") && !extreme.substring(i,j).contains("×") && !extreme.substring(i,j).contains("x") && !extreme.substring(i,j).contains("X")){
				String extract = extreme.substring(i,j);
				Pattern pattern18 = Pattern.compile("[\\s]?[dcmµ]?m(([\\s]diam)?([\\s]wide)?)");
		    	Matcher matcher3 = pattern18.matcher(extract);
		    	unit="";
		    	if ( matcher3.find()){
		    		unit = extract.substring(matcher3.start(), matcher3.end());
		    	}
		    	extract = matcher3.replaceAll("#");
		    	matcher3.reset();
		    	String from = extract.substring(0, extract.indexOf('-')).trim();
		    	String to = extract.substring(extract.indexOf('-')+1,extract.indexOf('#')).trim();
		    	boolean upperrestricted = ! to.endsWith("+");
		    	to = to.replaceFirst("\\+$", "").trim();
		    	
		    	Character character = new Character();
				character.setCharType("range_value");
				character.setName(chara);
				character.setFrom(from);
				character.setFromUnit(unit.trim());
				character.setTo(to);
				character.setToUnit(unit.trim());
				if(!upperrestricted)
					character.setUpperRestricted(upperrestricted+"");
				innertagstate.add(character);
		    	//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"size\" from=\""+from+"\" from_unit=\""+unit.trim()+"\" to=\""+to+"\" to_unit=\""+unit.trim()+"\" upper_restricted=\""+upperrestricted+"\"/>");
				toval = extract.substring(0, extract.indexOf('-'));
				fromval = extract.substring(extract.indexOf('-')+1,extract.indexOf('#'));
		    	//sizect+=1;
			}
			else {
				String extract = extreme.substring(i,j);
				Pattern pattern18 = Pattern.compile("[\\s]?[dcm�]?m(([\\s]diam)?([\\s]wide)?)");
		    	Matcher matcher3 = pattern18.matcher(extract);
		    	unit="";
		    	if ( matcher3.find()){
		    		unit = extract.substring(matcher3.start(), matcher3.end());
		    	}
		    	extract = matcher3.replaceAll("#");
		    	matcher3.reset();
		    	
		    	Character character = new Character();
				character.setName(chara);
				character.setValue(extract.substring(0,extract.indexOf('#')).trim());
				character.setUnit(unit.trim());
				innertagstate.add(character);
		    	//innertagstate = innertagstate.concat("<character name=\"size\" value=\""+extract.substring(0,extract.indexOf('#')).trim()+"\" unit=\""+unit.trim()+"\"/>");
				toval = extract.substring(0,extract.indexOf('#'));
				fromval = extract.substring(0,extract.indexOf('#'));
			}
			
			for(Character character : innertagstate) {
				if(character.getTo() != null && character.getTo().isEmpty()){
					if(toval.endsWith("+")){
						toval = toval.replaceFirst("\\+$", "");
						character.setUpperRestricted("false");
					}
					character.setTo(toval.trim());
					character.setToInclusive("false");
				}
				if(character.getFrom() != null && character.getFrom().isEmpty()) {
					character.setFrom(fromval.trim());
					character.setFromInclusive("false");
				}
			}
		}
		plaincharset = matcher2.replaceAll("#");
		matcher2.reset();
		//log(LogLevel.DEBUG, "plaincharset2:"+plaincharset);
		return plaincharset;
	}
	

	protected List<Element> latest(Class<? extends Element> elementType, 
			List<Element> list) {
		LinkedList<Element> selected = new LinkedList<Element>();
		int size = list.size();
		for(int i = size-1; i>=0; i--){
			if(list.get(i).isOfType(elementType)) {
				selected.add(list.get(i));
			}else{
				break;
			}
		}
		return selected;
	}
	
	@Override
	public String getDescription() {
		return this.getClass().toString();
	}
	
	protected Element getFirstDescriptionElement(List<Element> elements, 
			Class<? extends Element> elementType) {
		Element result = null;
		for(int i=0; i<elements.size(); i++) {
			Element element = elements.get(i);
			if(element.isOfType(elementType)) {
				result = element;
				break;
			}
		}
		return result;
	}
	
	protected Element getLastDescriptionElement(List<Element> elements, 
			Class<? extends Element> elementType) {
		Element result = null;
		for(int i=elements.size()-1; i>=0; i--) {
			Element element = elements.get(i);
			if(element.isOfType(elementType)) {
				result = element;
				break;
			}
		}
		return result;
	}
}
