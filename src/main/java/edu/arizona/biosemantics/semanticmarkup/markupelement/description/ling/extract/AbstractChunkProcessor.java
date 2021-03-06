package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.NumericalPhraseParser;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

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
	protected String compoundPreps;
	protected Set<String> stopWords;

	/**
	 *
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
	 * @param compoundpreps
	 * @param stopWords
	 */
	@Inject
	public AbstractChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner,
			ICharacterKnowledgeBase characterKnowledgeBase,
			@Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords,
			@Named("LocationPrepositions")Set<String> locationPrepositions,
			@Named("Clusters")Set<String> clusters,
			@Named("Units")String units,
			@Named("EqualCharacters")HashMap<String, String> equalCharacters,
			@Named("NumberPattern")String numberPattern,
			@Named("Times")String times,
			@Named("CompoundPrepWords") String compoundpreps,
			@Named("StopWords")Set<String> stopWords) {
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
		this.compoundPreps = "("+compoundpreps.replaceAll("\\s+", "-")+")";
		this.stopWords = stopWords;
	}

	/**
	 * [Important]The current processingContextState of the given processingContext will be cloned and preserved for restore
	 * [Important]Due to the clone, after a ChunkProcesser calls another ChunkProcessor,  the processingContextState.getCarryOverDataFrom(processingContext.getCurrentState()) should be called to stored the current state for the former processor;
	 * @param chunk
	 * @param processingContext
	 * @return list of DescriptionTreatmentElements resulting from the processing of chunk in processingContext
	 * TODO: it shouldnt be the chunk processors responsibility and freedom to or not to preserve the processingContextState
	 * This should be taken care of elsewhere
	 */
	@Override
	public List<? extends Element> process(Chunk chunk, ProcessingContext processingContext) {
		log(LogLevel.DEBUG, "process chunk " + chunk);
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		processingContext.addState(chunk, processingContextState);
		ProcessingContextState newState = (ProcessingContextState)processingContextState.clone();
		processingContext.setCurrentState(newState);
		List<? extends Element> results = processChunk(chunk, processingContext);
		if(results.size()==0 && chunk!=null && chunk.getTerminalsText().compareTo(".")!=0)  processingContext.setLastChunkYieldElement(false); //ignore unprocessed '.', "15 cm. long"
		else processingContext.setLastChunkYieldElement(true);

		return results;

	}

	/**
	 * @param chunk
	 * @param processingContext
	 * @return list of DescriptionTreatmentElements resulting from the processing of chunk in processingContext
	 */
	protected abstract List<? extends Element> processChunk(Chunk chunk, ProcessingContext processingContext);

	protected List<BiologicalEntity> establishSubject(List<BiologicalEntity> subjectStructures,
			ProcessingContextState processingContextState) {
		List<BiologicalEntity> result = new LinkedList<BiologicalEntity>();
		result.addAll(subjectStructures);

		LinkedList<BiologicalEntity> subjects = processingContextState.getSubjects();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		subjects.clear();
		lastElements.clear();

		for(BiologicalEntity structure : subjectStructures) {
			subjects.add(structure);
			lastElements.add(structure);
		}
		return result;
	}

	protected List<BiologicalEntity> establishSubject(
			Chunk subjectChunk, ProcessingContext processingContext, ProcessingContextState processingContextState) {
		log(LogLevel.DEBUG, "establish subject from " + subjectChunk);

		List<Chunk> subjectChunks = new LinkedList<Chunk>();
		subjectChunks.addAll(processingContextState.getUnassignedConstraints());
		subjectChunks.add(subjectChunk);
		processingContextState.clearUnassignedConstraints();
		List<BiologicalEntity> subjectStructures = createStructureElements(subjectChunks, processingContext, processingContextState);
		processingContext.setLastSubjects(subjectStructures); //remember last subjects and update it whenever new subjects are established
		return this.establishSubject(subjectStructures, processingContextState);
	}


	protected List<BiologicalEntity> reestablishSubject(ProcessingContext processingContext, ProcessingContextState processingContextState) {
		log(LogLevel.DEBUG, "reestablish subject");
		List<BiologicalEntity> result = new LinkedList<BiologicalEntity>();


		List<BiologicalEntity> subjects = processingContextState.getSubjects();
		if(subjects.size()==0){
			subjects = processingContext.getLastSubjects();
		}
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		lastElements.clear();

		for(BiologicalEntity structure : subjects) {
			lastElements.add(structure);
			//element.detach();
			//result.remove(element);
			result.add(structure);
		}
		return result;
	}

	protected void establishWholeOrganismAsSubject(
			ProcessingContext processingContext, List<BiologicalEntity> result,
			ProcessingContextState processingContextState) {
		//use whole_organism
		BiologicalEntity structureElement = new BiologicalEntity();
		int structureIdString = processingContext.fetchAndIncrementStructureId(structureElement);
		structureElement.setId("o" + String.valueOf(structureIdString));
		structureElement.setName("whole_organism");
		structureElement.setNameOriginal("");
		structureElement.setType("structure");
		List<BiologicalEntity> structureElements = new LinkedList<BiologicalEntity>();
		structureElements.add(structureElement);
		result.addAll(establishSubject(structureElements, processingContextState));
	}

	/**
	 * i-iii or i to iii: [MAIN_SUBJECT_ORGAN: [CONSTRAINT: [legs], ORGAN: [i-iii]]] to return 1 compound organ (1-3)
	 * MAIN_SUBJECT_ORGAN: [NP_LIST: [CONSTRAINT: [legs], ORGAN: [i], AND: [and], ORGAN: [ii]]] to return a list of individual organs
	 * MAIN_SUBJECT_ORGAN: [NP_LIST: [CONSTRAINT: [legs], ORGAN: [i], COMMA: [,], ORGAN: [ii], COMMA: [,], AND: [and], ORGAN: [iii]]] to return a list of individual organs
	 * [ORGAN: [horns], AND: [AND: [and]], NON_SUBJECT_ORGAN: [ORGAN: [abdomen]]] to reconstructed list of organs that are object of a preposition.
	 *
	 * @param subjectChunks
	 * @param processingContext
	 * @param processingContextState
	 * @return
	 */
	protected List<BiologicalEntity> createStructureElements(List<Chunk> subjectChunks, ProcessingContext processingContext, ProcessingContextState processingContextState) {
		LinkedList<BiologicalEntity> results = new LinkedList<BiologicalEntity>();
		Chunk subjectChunk = new Chunk(ChunkType.UNASSIGNED, subjectChunks);
		log(LogLevel.DEBUG, "create structure element from subjectChunks:\n " + subjectChunks);
		List<Chunk> organChunks = subjectChunk.getChunks(ChunkType.ORGAN);
		if(!organChunks.isEmpty()) {
			for(Chunk organChunk : organChunks) {
				BiologicalEntity structure = new BiologicalEntity();
				int structureIdString = processingContext.fetchAndIncrementStructureId(structure);
				structure.setId("o" + String.valueOf(structureIdString));
				Chunk constraintChunk = getConstraintOf(organChunk, subjectChunk);

				if(constraintChunk != null) {
					if(!constraintChunk.getTerminalsText().isEmpty())
						structure.setConstraint(constraintChunk.getTerminalsText());

					String organName = organChunk.getTerminalsText();
					String singular = getSingular(organName);
					String entityType = characterKnowledgeBase.getEntityType(singular, organName);
					if(entityType==null || entityType.length()==0) entityType = "structure";

					if(entityType.equals("structure"))
						structure.setName(singular);
					else
						structure.setName(organName); //taxon_name and substance do not need to be singularized
					structure.setNameOriginal(organName);
					structure.setType(entityType);

					List<BiologicalEntity> parents = new LinkedList<BiologicalEntity>();
					parents.add(structure);

					/*List<AbstractParseTree> terminals = subjectChunk.getTerminals();
					for(int i=0; i<terminals.size(); i++) {
						if(organChunk.containsOrEquals(terminals.get(i))) {
							if(i-1>=0 && (terminals.get(i-1).getTerminalsText().equals("a") || terminals.get(i-1).getTerminalsText().equals("an"))) {
								this.createCharacterElement(parents, null, "1", "count", "", processingContextState, true);
							}
							break;
						}
					}*/

					List<Character> unassignedCharacters = processingContextState.getUnassignedCharacters();
					for(Character unassignedCharacter : unassignedCharacters) {
						structure.addCharacter(unassignedCharacter);
					}
					unassignedCharacters.clear();

					LinkedHashSet<Chunk> characterStateChunks = getCharacterStatesOf(organChunk, subjectChunk);
					for(Chunk characterStateChunk : characterStateChunks) {
						String character = characterStateChunk.getProperty("characterName");
						Chunk state = characterStateChunk.getChunkDFS(ChunkType.STATE);
						if(character.compareTo("character")==0){
							processingContextState.setUnassignedCharacter(inflector.getSingular(state.getTerminalsText()));
						}else{

							LinkedHashSet<Chunk> modifierChunks = getModifiersOf(characterStateChunk, subjectChunk);
							List<Chunk> modifierChunkList = new LinkedList<Chunk>(modifierChunks);
							//List<Chunk> modifierChunks = characterStateChunk.getChunks(ChunkType.MODIFIER);
							//modifierChunks.addAll(subjectChunk.getChunks(ChunkType.MODIFIER))

							this.createCharacterElement(parents, modifierChunkList, state.getTerminalsText(), character, "", processingContextState, true);

							//Chunk modifierChunk = new Chunk(ChunkType.UNASSIGNED, modifierChunks);
							//DescriptionTreatmentElement characterElement = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
							//characterElement.setProperty(, value)
						}
					}



					results.add(structure);
				}
			}
		}
		return results;
	}

	/**
	 * horns-abdomens to horn-abdomen
	 * @param organNames
	 * @return
	 */
	private String getSingular(String organNames) {
		String[] list = organNames.split("[-]");
		String singular = "";
		for(String organName: list){
			singular = singular+inflector.getSingular(organName)+"-";
		}
		return singular.replaceAll("-$", "");
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
			boolean seenConstraint = false;
			ArrayList<AbstractParseTree> last = new ArrayList<AbstractParseTree> ();
			for(AbstractParseTree terminal : subjectChunk.getTerminals()) {
				if(subjectChunk.isPartOfChunkType(terminal, ChunkType.CONSTRAINT)) {
					seenConstraint = true;
					Chunk constraintChunk = subjectChunk.getChunkOfTypeAndTerminal(ChunkType.CONSTRAINT, terminal);
					if(constraintChunk!=null){
						constraints.addAll(constraintChunk.getTerminals());
						last.addAll(constraintChunk.getTerminals());
					}
				}else if(seenConstraint && (terminal.getTerminalsText().equals("and") || terminal.getTerminalsText().equals("or") || terminal.getTerminalsText().equals("to"))){
					constraints.add(terminal); //TODO  'and/or' would be treated as constraints in case of "mid and inner petals", but not "trees or shrubs".
					last.add(terminal);
				}else{
					seenConstraint = false; //inner scales or bristles
				}
				if(organChunk.containsOrEquals(terminal)) {
					if(!last.isEmpty() && last.get(last.size()-1).getTerminalsText().matches("and|or|to")) constraints.remove(last.get(last.size()-1));
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
		if (clauseModifierConstraint != null){
			if(element.isCharacter()){
				((Character)element).setConstraint(clauseModifierConstraint);
			}
			if(element.isStructure()){
				((BiologicalEntity)element).setConstraint(clauseModifierConstraint);
			}
		}
		if (clauseModifierConstraintId != null && !clauseModifierConstraintId.startsWith("-")){ //default id could be -1
			if(element.isCharacter())
				((Character)element).setConstraintId(clauseModifierConstraintId);
			if(element.isStructure())
				((BiologicalEntity)element).setConstraintId(clauseModifierConstraintId);
		}
		processingContextState.setClauseModifierContraint(null);
		processingContextState.setClauseModifierContraintId(null);
	}



	/**
	 *
	 * @param processingContext
	 * @param processingContextState
	 * @param alternativeStructureIDs biologicalentities considered but are not used as the parents.
	 * @return biologicalentities to be used as the parent.
	 */
	protected List<BiologicalEntity> parentStructures(ProcessingContext processingContext,
			ProcessingContextState processingContextState, ArrayList<String> alternativeStructureIDs) {

		boolean newSegment = processingContext.getCurrentState().isCommaAndOrEosEolAfterLastElements();

		/* 5/21/2015
		LinkedList<BiologicalEntity> parents = new LinkedList<BiologicalEntity>();
		if(!newSegment && (processingContextState.getLastElements().size()> 0 &&
				processingContextState.getLastElements().getLast().isStructure())) {
			for(Element lastElement : processingContextState.getLastElements())
				if(lastElement.isStructure())
					parents.add((BiologicalEntity)lastElement);
		}else{
			parents.addAll(processingContextState.getSubjects());
		}
		return parents;
		 */

		LinkedList<BiologicalEntity> lastStructures = lastStructures(processingContextState);
		LinkedList<BiologicalEntity> subjects =processingContextState.getSubjects();

		if(newSegment){
			if(!processingContextState.getSubjects().isEmpty()){
				structureIDs (subjects, lastStructures,alternativeStructureIDs);
				return subjects;
			}
			else{
				structureIDs (lastStructures, subjects,alternativeStructureIDs);
				return lastStructures;
			}
		}else{
			if(!lastStructures.isEmpty()){
				return lastStructures;
			}
			else {
				return subjects;
			}
		}


	}

	protected LinkedList<BiologicalEntity> lastStructures(
			ProcessingContextState processingContextState) {
		LinkedList<BiologicalEntity> lastStructures = new LinkedList<BiologicalEntity>();
		if(processingContextState.getLastElements().size()> 0 &&
				processingContextState.getLastElements().getLast().isStructure()) {
			for(Element lastElement : processingContextState.getLastElements())
				if(lastElement.isStructure())
					lastStructures.add((BiologicalEntity)lastElement);
		}
		return lastStructures;
	}

	/**
	 *
	 * @param parents
	 * @param alternatives
	 * @param alternativeStructureIDs:  return alternatives if it is disjoint from parents, otherwise, return empty list.
	 */
	protected void structureIDs (LinkedList<BiologicalEntity> parents, LinkedList<BiologicalEntity> alternatives, ArrayList<String> alternativeStructureIDs){

		boolean disjoint = true;
		for(BiologicalEntity structure: alternatives){
			String id = structure.getId();
			alternativeStructureIDs.add(id);
			for(BiologicalEntity parent: parents){
				if(parent.getId().compareTo(id)==0){
					disjoint = false;
					break;
				}
			}
			if(!disjoint){
				alternativeStructureIDs.clear();
				return;
			}
		}
	}

	/**
	 *  characters may need to be associated with the alternative structures in post-parsing normalization
	 */
	/*protected void addAlternativeIds(List<Character> characters, ArrayList<String>alternativeIds){
		String ids = "";
		for(String id: alternativeIds){
			ids += id+" ";
		}
		ids.trim();
		if(!ids.isEmpty()){
			for(Character character: characters){
				character.setNotes("alterIDs:"+ids);
			}
		}
	}*/

	/**
	 *  characters may need to be associated with the alternative structures in post-parsing normalization
	 */
	protected void addAlternativeIds(List<Element> characters, ArrayList<String>alternativeIds){
		String ids = "";
		for(String id: alternativeIds){
			ids += id+" ";
		}
		ids = ids.trim();
		if(!ids.isEmpty()){
			for(Element character: characters){
				if(character.isCharacter()){
					((Character)character).setNotes("alterIDs:"+ids);
				}
			}
		}
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




	/**
	 *separate o[......... {m} {m} (o1) and {m} (o2)] to two parts: the last part include all organ names
	 * e.g., o[(cypselae) -LSB-/-LSB- {minute} (crowns) -RSB-/-RSB-]
	 *
	 * when preposition is 'between', then
	 * need to find two structures, e.g.,
	 * "distance between horns and abdomen" (also handle the case when abdomen is not part of the pp chunk)
	 * "distance between horns"
	 *
	 * @param object
	 * @param processingContext
	 * @param processingContextState
	 * @return
	 */
	protected List<LinkedList<Chunk>> separate(Chunk ppChunk, Chunk preposition, Chunk object, ProcessingContext processingContext,
			ProcessingContextState processingContextState) {
		ArrayList<LinkedList<Chunk>> twoParts  = new ArrayList<LinkedList<Chunk>>();
		LinkedList<Chunk> nonOrgan = new LinkedList<Chunk>();
		LinkedList<Chunk> organ = new LinkedList<Chunk>();

		boolean foundOrgan = false;

		List<Chunk> constraintCandidates = new LinkedList<Chunk>();

		List<Chunk> chunks = new LinkedList<Chunk>(object.getChunks());
		boolean foundAnd = false;
		boolean foundConjOrgans = false;
		for(int i=0; i<chunks.size(); i++) {
			Chunk chunk = chunks.get(i);
			if(chunk.isOfChunkType(ChunkType.CONSTRAINT) ||
					chunk.getTerminalsText().equals("and") || chunk.getTerminalsText().equals("or")
					|| chunk.isOfChunkType(ChunkType.COMMA)) {
				constraintCandidates.add(chunk);
				if(chunk.getTerminalsText().equals("and")) foundAnd = true;
			}
			else if(chunk.containsChunkType(ChunkType.ORGAN) || foundOrgan) {
				organ.addAll(constraintCandidates);
				organ.add(chunk);
				foundOrgan = true;
				if(foundAnd) foundConjOrgans = true;
			}
			else {
				nonOrgan.add(chunk);
				//constraintCandidates.clear(); Hong Jan 2015, "a primary ascending skeleton"
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


		if(ppChunk!=null && preposition!=null && preposition.getTerminalsText().matches("between") && !foundConjOrgans){ //check if two organs or one plural organs
			//next if the chunk following object Chunk is "and", if so bring it in
			List<Chunk> allChunks = processingContext.getChunkCollector().getChunks();
			Chunk next = null;
			Chunk afterNext = null;
			int index = allChunks.indexOf(ppChunk);
			if(allChunks.size()>index+2){
				next = allChunks.get(index+1);
				afterNext = (allChunks.get(index+2));
			}
			if(next!=null && afterNext!=null && next.getTerminalsText().matches("and") && afterNext.containsChunkType(ChunkType.ORGAN)){
				//bring it into
				organ.add(next);
				organ.add(afterNext);
				processingContext.getChunkListIterator().next(); //move iterator pointer forward by 2 positions
				processingContext.getChunkListIterator().next();
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
	protected List<BiologicalEntity> extractStructuresFromObject(Chunk ppChunk, Chunk prepostion, Chunk object, ProcessingContext processingContext,
			ProcessingContextState processingContextState) {
		ChunkCollector chunkCollector = processingContext.getChunkCollector();

		List<BiologicalEntity> structures;
		List<LinkedList<Chunk>> twoParts = separate(ppChunk, prepostion, object, processingContext, processingContextState);
		//find the organs in object o[.........{m} {m} (o1) and {m} (o2)]

		//log(LogLevel.DEBUG, "twoParts " + twoParts);
		structures = createStructureElements(twoParts.get(1), processingContext, processingContextState);
		// 7-12-02 add cs//to be added structures found in 2nd part, not rewrite this.latestelements yet
		if(!twoParts.get(0).isEmpty()) {
			LinkedList<BiologicalEntity> structuresCopy = new LinkedList<BiologicalEntity>(structures);

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
			if(!structures.isEmpty()){
				//processingContext.getCurrentState().setCommaAndOrEosEolAfterLastElements(false);
				processCharacterText(twoParts.get(0), structures, null, processingContextState, processingContext, true);
			}
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
	protected List<Element> processCharacterText(List<Chunk> tokens, List<BiologicalEntity> parents,
			String character, ProcessingContextState processingContextState, ProcessingContext processingContext, boolean isModifier) {
		LinkedList<Element> results = new LinkedList<Element>();
		//determine characters and modifiers
		List<Chunk> modifiers = new LinkedList<Chunk>();

		for(Chunk token : tokens) {
			if(stopWords.contains(token.getTerminalsText())) continue;
			//processingContextState = processingContext.getCurrentState();
			if(token.isOfChunkType(ChunkType.TO_PHRASE)) {
				processingContextState.setLastElements(new LinkedList<Element>(parents));
				processingContextState.setCommaAndOrEosEolAfterLastElements(false);
				IChunkProcessor processor = processingContext.getChunkProcessor(ChunkType.TO_PHRASE);
				List<? extends Element> result = processor.process(token, processingContext);
				for(Element c: result){
					if(c.isCharacter()){
						((Character)c).setIsModifier(isModifier+"");
					}
				}
				results.addAll(result);
				processingContextState.getCarryOverDataFrom(processingContext.getCurrentState());
				processingContext.setCurrentState(processingContextState);
				log(LogLevel.DEBUG, "restored current state after "+processor.getClass()+" is run.");
				//results = this.processCharacterList(token, parents, processingContextState, processingContext);
			} else {
				List<Chunk> chunkModifiers = token.getChunks(ChunkType.MODIFIER);
				modifiers.addAll(chunkModifiers);

				String w = token.getTerminalsText();

				if(token.containsChunkType(ChunkType.STATE))
					w = token.getChunkBFS(ChunkType.STATE).getTerminalsText();
				String tokensCharacter = null;
				if(processingContextState.getUnassignedCharacter()!=null) { //override character with unassigned character
					tokensCharacter = processingContextState.getUnassignedCharacter();
					processingContextState.setUnassignedCharacter(null);
				}else{
					if(token.isOfChunkType(ChunkType.CHARACTER_STATE)) {
						tokensCharacter = token.getProperty("characterName");
					} else {
						tokensCharacter = characterKnowledgeBase.getCharacterName(w).getCategories();
					}
				}
				//Hong test
				if(tokensCharacter==null && w.matches("no")){ //e.g. no flowers
					tokensCharacter = "count";
				}
				if(tokensCharacter==null && posKnowledgeBase.isAdverb(w) && !modifiers.contains(token)) {
					//TODO: can be made more efficient, since sometimes character is already given
					modifiers.add(token);
				}else if(w.matches(".*?\\d.*") && !w.matches(".*?[a-z].*")){//TODO: 2 times =>2-times?
					List<Element> charas = this.annotateNumericals(w, "count", modifiers, parents, false, processingContextState);
					for(Element c: charas){
						if(c.isCharacter()){
							((Character)c).setIsModifier(isModifier+"");
						}
					}
					results.addAll(charas);

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
								((Character)lastelement).updateCharacterName(w);
							}
						}else{
							String[] characterValues = w.split("\\bor\\b|\\band\\b");
							for(String characterValue : characterValues)
								results.add(createCharacterElement(parents, modifiers, characterValue.trim(), tokensCharacter, "", processingContextState, isModifier));
							//default type "" = individual vaues
							modifiers.clear();
						}
					} else {
						processingContextState.setLastElements(new LinkedList<Element>(parents));
						processingContextState.setCommaAndOrEosEolAfterLastElements(false);
						IChunkProcessor processor = processingContext.getChunkProcessor(token.getChunkType());

						if(processor != null) {
							List<? extends Element> result = processor.process(token, processingContext);
							for(Element c: result){
								if(c.isCharacter()){
									((Character)c).setIsModifier(isModifier+"");
								}
							}
							results.addAll(result);
							//restore CurrentState
							processingContextState.getCarryOverDataFrom(processingContext.getCurrentState());
							processingContext.setCurrentState(processingContextState);
							log(LogLevel.DEBUG, "restored current state after "+processor.getClass()+" is run.");
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
	 * @param modifiers
	 * @param characterValue
	 * @param characterName
	 * @param processingContextState
	 */
	protected List<Element> createRangeCharacterElement(List<BiologicalEntity> parents,
			List<Chunk> modifiers, String characterValue, String characterName, ProcessingContextState processingContextState) {
		LinkedList<Element> results = new  LinkedList<Element>();
		if(characterValue.indexOf(" to ") < 0) return results;

		Character character = new Character();
		//if(this.inbrackets){character.setAttribute("in_bracket", "true");}
		character.setCharType("range_value");
		character.setName(characterName);


		String[] range = characterValue.split("\\s+to\\s+");//a or b, c, to d, c, e
		String[] tokens = range[0].replaceFirst("\\W$", "").replaceFirst("^.*?\\s+(or|and/or)\\s+", "").split("\\s*,\\s*"); //a or b, c, =>
		if(tokens.length==0)
			return results;
		String from = getFirstCharacter(tokens[tokens.length-1]);
		tokens = range[1].split("\\s*,\\s*");
		String to = getFirstCharacter(tokens[0]);
		character.setFrom(from.replaceAll("-c-", " ").replaceAll("~", " ")); //a or b to c => b to c
		character.setTo(to.replaceAll("-c-", " ").replaceAll("~", " "));

		for(Chunk modifier : modifiers)
			character.appendModifier(modifier.getTerminalsText());

		if(parents.isEmpty())
			processingContextState.addUnassignedCharacters(character);
		for(BiologicalEntity parentStructure : parents) {
			parentStructure.addCharacter(character);
		}
		results.add(character);

		addClauseModifierConstraint(character, processingContextState);
		return results;
	}

	/**
	 * @param character: usually large
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

	protected Character createCharacterElement(List<BiologicalEntity> parents, List<Chunk> modifiers,
			String characterValue, String characterName, String char_type, ProcessingContextState processingContextState, boolean isConstraintModifier) {
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
				//character.setIsConstraintModifier(isConstraintModifier);
				character.setIsModifier(isConstraintModifier+"");
				character.setFrom(values[0]);
				if(values[1].endsWith("+")) {
					character.setTo(values[1].substring(0, values[1].length()-1));
					character.setUpperRestricted("false");
				} else
					character.setTo(values[1]);
			}else{
				if (characterName.compareTo("some_measurement") == 0) {
					String value = characterValue.replaceFirst("(\\b" + units + "\\b)", "").trim(); // 5-10 mm
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
				}else if(characterValue.contains("~")){
					characterValue = characterValue.replaceAll("~", " ");
				}
				if(char_type.length() > 0){
					character.setCharType(char_type);
				}
				character.setName(characterName);
				character.setValue(characterValue);
				//character.setIsConstraintModifier(isConstraintModifier);
				character.setIsModifier(isConstraintModifier+"");
				if(!modifierString.isEmpty())
					character.setModifier(modifierString);
			}

			for(BiologicalEntity parent : parents) {
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


	protected List<Element> linkObjects(List<BiologicalEntity> subjectStructures, List<Chunk> modifiers,
			Chunk ppChunk, Chunk preposition, Chunk object, boolean lastIsStruct, boolean lastIsChara,
			ProcessingContext processingContext, ProcessingContextState processingContextState, String relation, Element lastE) {

		LinkedList<Element> result = new LinkedList<Element>();
		LinkedList<Element> lastElements = processingContextState.getLastElements(); //lastElements changed after extractStructuresFromObject
		ChunkCollector chunkCollector = processingContext.getChunkCollector();

		List<Chunk> unassignedModifiers = processingContext.getCurrentState().getUnassignedModifiers();
		modifiers.addAll(unassignedModifiers);
		unassignedModifiers.clear();

		List<BiologicalEntity> structures = extractStructuresFromObject(ppChunk, preposition, object, processingContext, processingContextState); //extractStructuresFromObject changed lastElements

		result.addAll(structures);
		String base = "";

		if(baseCountWords.contains(object.getTerminalsText())) {
			base = "each";
		}

		//last element was a character, this pp will be rendered as a constraint
		if(lastIsChara && !lastElements.isEmpty() && !processingContextState.isCommaAndOrEosEolAfterLastElements()) {
			//Character lastElement = (Character)lastElements.getLast();
			Character lastElement = (Character) lastE;
			//if last character is size, change to location: <margins> r[p[with] o[3�6 (spines)]] 1�3 {mm} r[p[{near}] o[(bases)]].
			//1-3 mm is not a size, but a location of spines
			if(lastElement.getName().equals("some_measurement") &&
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
			processingContext.getCurrentState().setLastElements(new LinkedList<Element>(structures));
		} else {
			//deal with between a and b
			if(preposition.getTerminalsText().matches("between")){ //area between a and b, 'between' will not establish a relation
				String entityNameOriginal1 = (structures.get(0).getConstraint()!=null? structures.get(0).getConstraint()+" ":"")+structures.get(0).getNameOriginal();
				String entityNameOriginal2 = "";
				if(structures.size()>1)
					entityNameOriginal2 = (structures.get(1).getConstraint()!=null? structures.get(1).getConstraint()+" ":"")+structures.get(1).getNameOriginal();
				else
					entityNameOriginal2 = (structures.get(0).getConstraint()!=null? structures.get(0).getConstraint()+" ":"")+structures.get(0).getNameOriginal();

				int count = structures.size();
				String entityNameOriginal = entityNameOriginal1+(count>1? " and "+entityNameOriginal2: "");
				if(lastIsStruct && !processingContextState.isCommaAndOrEosEolAfterLastElements()){
					String newConstraint = ""; //add "between a-b" to 'area'.
					if(((BiologicalEntity)lastE).getConstraint()!=null)
						if(((BiologicalEntity)lastE).getConstraint().startsWith("between"))
							newConstraint = ((BiologicalEntity)lastE).getConstraint()+" and "+entityNameOriginal;
						else
							newConstraint = ((BiologicalEntity)lastE).getConstraint()+"; between "+entityNameOriginal;
					else
						newConstraint = "between "+entityNameOriginal;

					((BiologicalEntity)lastE).setConstraint(newConstraint);

					String constraintIDs = consolidateResultRConstraints(
							subjectStructures, result, structures,
							entityNameOriginal1, entityNameOriginal2);
					String existingCId = "";
					if(((BiologicalEntity)lastE).getConstraintId()!=null) existingCId = ((BiologicalEntity)lastE).getConstraintId();

					((BiologicalEntity)lastE).setConstraintId((existingCId+ "-"+constraintIDs).replaceAll("^-+|-+$", "").trim());

					result.add(lastE);
					//keep lastElement as the last element
				}else{ //distance between a and b*/
					AbstractParseTree terminal = ((Chunk)object.clone()).getChunks(ChunkType.ORGAN).get(0).getTerminals().get(0); //obtain the structure needed to construct a new terminal
					terminal.setTerminalsText(entityNameOriginal1+"-"+entityNameOriginal2);
					Chunk entity = new Chunk(ChunkType.NON_SUBJECT_ORGAN, new Chunk(ChunkType.ORGAN, terminal)); //NON_SUBJECT_ORGAN: [ORGAN: [horn-horn]]
					ArrayList<Chunk> entityChunks= new ArrayList<Chunk>();
					entityChunks.add(entity);
					List<BiologicalEntity> nStructures = this.createStructureElements(entityChunks, processingContext, processingContextState); //1 bioEntity:'a-b'
					nStructures.get(0).setNameOriginal(entityNameOriginal);

					String constraintIDs = consolidateResultRConstraints(
							subjectStructures, result, structures,
							entityNameOriginal1, entityNameOriginal2);
					nStructures.get(0).setId(nStructures.get(0).getId()+"."+constraintIDs+"."); //id: o13.o10-o12.
					result.addAll(nStructures);
					processingContext.getCurrentState().setLastElements(new LinkedList<Element>(nStructures));
				}
			}else{
				//render as a relation
				if(relation == null){
					relation = relationLabel(preposition, subjectStructures, structures, object, chunkCollector);//determine the relation
				}
				if(relation!=null){
					result.addAll(createRelationElements(relation, subjectStructures, structures, modifiers, false, processingContext, processingContextState));//relation elements not visible to outside //// 7-12-02 add cs
					//result.addAll(createRelationElements(relation, subjectStructures, structures, modifiers, false, processingContext, processingContextState));//relation elements not visible to outside //// 7-12-02 add cs
				}
				if(relation!= null && relation.compareTo("part_of")==0)
					structures = subjectStructures; //part_of holds: make the organbeforeof/entity1 the return value, all subsequent characters should be refering to organbeforeOf/entity1
				processingContext.getCurrentState().setLastElements(new LinkedList<Element>(structures));
			}
		}

		//processingContext.getCurrentState().setLastElements(new LinkedList<Element>(structures));
		return result;
	}

	private String consolidateResultRConstraints(
			List<BiologicalEntity> subjectStructures,
			LinkedList<Element> result, List<BiologicalEntity> structures,
			String entityNameOriginal1, String entityNameOriginal2) {
		//link a and b to their ids. a or b may also be mentioned before, try to find the id of the first mention in subjects
		//use ref id as part of the constraintIDs
		String constraintIDs = "";
		for(BiologicalEntity ref: subjectStructures){
			for(BiologicalEntity str:structures){ //if constraint+name matches, consider a match
				if(((str.getConstraint()==null && ref.getConstraint()==null)||(str.getConstraint()!=null && ref.getConstraint()==null && str.getConstraint().equals(ref.getConstraint()))) && str.getName().equals(ref.getName())){
					result.remove(str);
					constraintIDs += ref.getId()+"-";
				}else{
					constraintIDs += str.getId()+"-"; //id-id
				}
			}
		}
		return constraintIDs.replaceAll("-+$", "").trim();
	}


	protected String relationLabel(Chunk preposition,
			List<BiologicalEntity> organsbeforepp,
			List<BiologicalEntity> organsafterpp, Chunk object, ChunkCollector chunkCollector) {
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
			if(beforePPChunk != null && beforePPChunk.getChunkType().equals(ChunkType.PP)) {
				return "part_of";
			}
			if(afterPPChunk!=null && (/*afterPPChunk.isOfChunkType(ChunkType.END_OF_LINE) || afterPPChunk.isOfChunkType(ChunkType.END_OF_SUBCLAUSE) ||*/
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

	protected String differentiateOf(List<BiologicalEntity> organsBeforeOf, List<BiologicalEntity> organsAfterOf) {
		String result = "part_of";

		for (int i = 0; i<organsBeforeOf.size(); i++){
			String b = organsBeforeOf.get(i).getName();
			if(b.compareTo("part")==0) return "part_of";
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
			List<BiologicalEntity> fromStructures, List<BiologicalEntity> toStructures,
			List<Chunk> modifiers, boolean symmetric,
			ProcessingContext processingContext, ProcessingContextState processingContextState) {
		//compoundprep relations have "-" in them, remove '-' before output
		if(relation.contains("-")){
			String[] tokens = relation.split("\\s+");
			String newrel = "";
			for(String t: tokens){//remove added - for compound preps
				if(t.matches(this.compoundPreps)){
					t = t.replaceAll("-", " ");
				}
				newrel += t + " ";
			}
			relation = newrel.trim();
		}
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
				Relation rel = addRelation(relation, modifiers, symmetric, o1id, o2id, fromStructures.get(i), toStructures.get(j), negation, processingContext, processingContextState);
				relationElements.add(rel);
				//update from/to relations of BiologicalEntities
				fromStructures.get(i).addFromRelation(rel);
				toStructures.get(j).addToRelation(rel);
			}
		}
		return relationElements;
	}

	protected Relation addRelation(String relationName, List<Chunk> modifiers,
			boolean symmetric, String fromId, String toId,BiologicalEntity fromStructure, BiologicalEntity toStructure,  boolean negation, ProcessingContext processingContext, ProcessingContextState processingContextState) {
		Relation relation = new Relation();
		relation.setName(relationName);
		relation.setFrom(fromId);
		relation.setTo(toId);
		relation.setNegation(String.valueOf(negation));
		relation.setFromStructure(fromStructure);
		relation.setToStructure(toStructure);
		relation.setId("r" + String.valueOf(processingContext.fetchAndIncrementRelationId(relation)));

		for(Chunk modifier : modifiers) {
			relation.appendModifier(modifier.getTerminalsText());
		}

		addClauseModifierConstraint(relation, processingContextState);
		return relation;
	}

	protected boolean isNumerical(Chunk object) {
		if(object!=null && object.getTerminalsText()!=null)
			return object.getTerminalsText().matches("\\d+");
		return false;
	}


	protected List<Element> annotateNumericals(String text, String characterString, List<Chunk> modifiers,
			List<BiologicalEntity> parents, boolean resetFrom, ProcessingContextState processingContextState) {
		LinkedList<Element> result = new LinkedList<Element>();
		boolean average = characterString.startsWith("average_");
		NumericalPhraseParser npp = new NumericalPhraseParser(units);
		List<Character> characters = npp.parseNumericals(text, characterString);
		if(characters.size()==0){//failed, simplify chunktext
			characters = npp.parseNumericals(text, characterString);
		}

		for(Character c: characters){
			if(average && !c.getName().contains("average_")) c.setName("average_"+c.getName());
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
			for(BiologicalEntity parent : parents) {//TODO Hong parents could be empty, character is only add to result
				parent.addCharacter(character);
			}
			result.add(character);
		}
		return result;
	}





	/*
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
		numberexp = annotateSize(numberexp, innertagstate, "some_measurement");





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
    			character.setName("some_measurement");
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
    			character.setName("some_measurement");
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

    	}
    	numberexp = matcher2.replaceAll("#");
    	matcher2.reset();

    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//   count                                                                                             ///////////////



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

    	}
    	matcher2.reset();

 		return innertagstate;
	}
	 */
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

	protected String listStructureIds(List<BiologicalEntity> structures) {
		StringBuffer list = new StringBuffer();
		for(BiologicalEntity structure : structures)
			list.append(structure.getId()).append(", ");
		return list.toString().trim().replaceFirst(",$", "");
	}

	/**
	 * @param chunk ckstring:r[p[in] o[outline]]
	 * @param processingContextState
	 * @return if character has sufficiently been dealt with
	 */
	protected boolean characterPrep(Chunk chunk, ProcessingContextState processingContextState) {
		boolean done = false;

		LinkedList<Element> lastElements = processingContextState.getLastElements();
		Set<String> characters = glossary.getWordsInCategory("character");

		List<AbstractParseTree> terminals = chunk.getTerminals();
		if(terminals.size() < 2)
			return done;
		String lastWord = terminals.get(terminals.size()-1).getTerminalsText();
		if(characters.contains(lastWord)) {
			if(!lastElements.isEmpty()) {
				Element lastElement = lastElements.getLast();
				if(lastElement.isCharacter()) {//shell oval in outline
					for(Element element : lastElements) {
						if(element.isCharacter()){
							((Character)element).updateCharacterName(lastWord);
						}
					}
					done = true;
				}else if(lastElement.isStructure()) {//shell in oval outline
					//String cvalue = ..
					Chunk characterValue = chunk.getChunkDFS(ChunkType.PREPOSITION);
					//ckstring.replaceFirst(".*?\\]", "").replaceAll("\\w+\\[","").replaceAll(lastword, "").replaceAll("[{}\\]\\[]", "");
					for(Element element : lastElements) {
						if(element.isCharacter()) {
							((Character)element).updateCharacterName(lastWord);
							((Character)element).setValue(characterValue.getTerminalsText());
						}
					}
					done = true;
				}
			}
		}
		return done;
	}



	/*	protected String annotateSize(String plaincharset, List<Character> innertagstate, String chara) {
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
	}*/


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
