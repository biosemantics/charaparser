package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;





import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * ThanChunkProcessor processes chunks of ChunkType.THAN
 * @author rodenhausen
 */
public class ThanChunkProcessor extends AbstractChunkProcessor {

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
	public ThanChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
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
		List<Element> result = processTHAN(chunk, processingContextState.getSubjects(), processingContext, 
				processingContextState);
		processingContextState.setLastElements(result);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}
	
	/**
	 * size[{longer}] constraint[than (object)]";
	 * shape[{lobed} constraint[than (proximal)]]
	 */
	private List<Element> processTHAN(Chunk content, List<BiologicalEntity> parents, 
			ProcessingContext processingContext, ProcessingContextState processingContextState) {
		List<Element> result = new LinkedList<Element>();
		
		LinkedHashSet<Chunk> beforeThan = new LinkedHashSet<Chunk>();
		for(Chunk chunk : content.getChunks()) {
			if(chunk.isOfChunkType(ChunkType.THAN))
				break;
			else 
				beforeThan.add(chunk);
		}
		Chunk beforeChunk = new Chunk(ChunkType.UNASSIGNED, beforeThan);
		
		String characterName = null;
		if(beforeChunk.containsChunkType(ChunkType.CHARACTER_STATE)) 
			characterName = beforeChunk.getChunkDFS(ChunkType.CHARACTER_STATE).getProperty("characterName");
		
		Chunk thanChunk = content.getChildChunk(ChunkType.THAN);
		//if(!thanChunk.containsChunkType(ChunkType.PP)) {
			if(thanChunk.containsChunkType(ChunkType.CONSTRAINT) && !thanChunk.containsChunkType(ChunkType.ORGAN)) {
				Chunk constraint = thanChunk.getChunks(ChunkType.CONSTRAINT).get(0);
				IChunkProcessor chunkProcessor = processingContext.getChunkProcessor(ChunkType.CONSTRAINT);
				List<? extends Element> elements = chunkProcessor.process(constraint, processingContext);
				processingContextState.getCarryOverDataFrom(processingContext.getCurrentState());
				processingContext.setCurrentState(processingContextState); 
				log(LogLevel.DEBUG, "restored current state after "+chunkProcessor.getClass()+" is run.");
				List<BiologicalEntity> structures = new LinkedList<BiologicalEntity>();
				for(Element element : elements)
					if(element.isStructure())
						structures.add((BiologicalEntity)element);
				result.addAll(structures);
				this.createConstraintedCharacters(content, parents, beforeChunk, structures, processingContext);
				processingContextState.getCarryOverDataFrom(processingContext.getCurrentState());
				processingContext.setCurrentState(processingContextState);
				log(LogLevel.DEBUG, "restored current state after createConstraintedCharacters is run.");
		} else {
			Chunk thanObject = thanChunk.getChunkDFS(ChunkType.OBJECT);
			if(thanObject==null){
				//TODO narrower than a Value, narrower than in female
				
				
				
				
			}else{
			//Chunk thanObject = thanChunk.getChildChunk(ChunkType.PP).getChildChunk(ChunkType.OBJECT);
			LinkedHashSet<Chunk> beforeOrganChunks = new LinkedHashSet<Chunk>(); 
			LinkedHashSet<Chunk> organChunks = new LinkedHashSet<Chunk>();
			LinkedHashSet<Chunk> afterOrganChunks = new LinkedHashSet<Chunk>(); 
			
			
			this.getOrganChunks(thanObject, beforeOrganChunks, organChunks, afterOrganChunks);
			boolean foundOrgan = !organChunks.isEmpty();
			
			if(foundOrgan) {
				LinkedHashSet<Chunk> objectChunks = new LinkedHashSet<Chunk>();
				objectChunks.addAll(beforeOrganChunks);
				objectChunks.addAll(organChunks);
				Chunk objectChunk = new Chunk(ChunkType.UNASSIGNED, objectChunks);
				List<BiologicalEntity> structures = extractStructuresFromObject(content, null, objectChunk, processingContext, processingContextState);
				List<Element> characters = this.createConstraintedCharacters(content, parents, beforeChunk, structures, processingContext);
				processingContextState.getCarryOverDataFrom(processingContext.getCurrentState());
				processingContext.setCurrentState(processingContextState);
				log(LogLevel.DEBUG, "restored current state after createConstraintedCharacters is run.");
				result.addAll(characters);
				result.addAll(structures);
			} else {
				if(thanObject.containsChildOfChunkType(ChunkType.COUNT) || thanObject.containsChildOfChunkType(ChunkType.BASED_COUNT) ||
						thanObject.containsChildOfChunkType(ChunkType.AREA) || thanObject.containsChildOfChunkType(ChunkType.NUMERICALS) ||
						thanObject.containsChildOfChunkType(ChunkType.VALUE_DEGREE) || thanObject.containsChildOfChunkType(ChunkType.VALUE) ||
						thanObject.containsChildOfChunkType(ChunkType.VALUE_PERCENTAGE)) {
					//second case
					for(Chunk child : thanObject.getChunks()) {
						log(LogLevel.DEBUG, "child " + child);
						IChunkProcessor chunkProcessor = processingContext.getChunkProcessor(child.getChunkType());
						if(chunkProcessor != null) {
							List<? extends Element> characters = chunkProcessor.process(child, processingContext);
							result.addAll(characters);
						}
					}
					if(!beforeThan.isEmpty()) {
						for(Element element : result) {
							if(element.isCharacter())
								((Character)element).setModifier(beforeChunk.getTerminalsText() + " than");
							if(element.isRelation()) 
								((Relation)element).setModifier(beforeChunk.getTerminalsText() + " than");
						}
					}
				} else {
					//third case
					boolean foundCharacter = false;
					for(Chunk child : thanObject.getChunks()) {
						if(child.isOfChunkType(ChunkType.CHARACTER_STATE)) {
							foundCharacter = true;
							if(characterName != null)
								child.setProperty("characterName", characterName);
							LinkedHashSet<Chunk> characterTerminals = new LinkedHashSet<Chunk>();
							characterTerminals.addAll(content.getTerminals());
							child.getChunks(ChunkType.STATE).get(0).setChunks(characterTerminals);
							List<? extends Element> elements = processingContext.getChunkProcessor(child.getChunkType()).process(child, processingContext);
							result.addAll(elements);
							break;
						}
					}
					if(!foundCharacter && !thanObject.getChunks().isEmpty()) {
						result.addAll(this.createConstraintedCharacters(content, parents, beforeChunk, 
									 	new LinkedList<BiologicalEntity>(), processingContext));
						processingContextState.getCarryOverDataFrom(processingContext.getCurrentState());
						processingContext.setCurrentState(processingContextState);
						log(LogLevel.DEBUG, "restored current state after createConstraintedCharacters is run.");
					}
				}
			}
			}
		}
		return result; 	
		
		
		/*List<Chunk> beforeThan = new ArrayList<Chunk>();
		
		boolean foundThan = false;
		for(Chunk chunk : content.getChunks()) {
			if(chunk.isOfChunkType(ChunkType.THAN))
				foundThan = true;
			else if(!foundThan){ 
				//if(foundThan) 
				//	afterThan.add(chunk);
				//else 
					beforeThan.add(chunk);
			}
		}
		Chunk beforeChunk = new Chunk(ChunkType.UNASSIGNED, beforeThan);
		String beforeThanText = beforeChunk.getTerminalsText();
		
		LinkedHashSet<Chunk> afterThan = content.getChildChunk(ChunkType.THAN).getChildChunk(ChunkType.PP).getChildChunk(ChunkType.OBJECT).getChunks();
		Chunk afterChunk = new Chunk(ChunkType.UNASSIGNED, afterThan);
		
		String part0 = beforeChunk.getTerminalsText();
		String part1 = afterChunk.getTerminalsText();
		
		//if(content.startsWith("constraint")) {
		LinkedHashSet<Chunk> chunks = content.getChunks();
		boolean firstIsThan = false;
		for(Chunk chunk : chunks) {
			if(chunk.isOfChunkType(ChunkType.THAN)) {
				characters.addAll(latest(DescriptionType.CHARACTER, processingContextState.getLastElements()));
				firstIsThan = true;
			}
			break;
		}
		
		if(!firstIsThan) {
			if(beforeThanText.matches(".*?\\d.*") && 
					beforeChunk.containsChunkType(ChunkType.CHARACTER_STATE) && 
					beforeChunk.getChunkDFS(ChunkType.CHARACTER_STATE).getProperty("characterName").contains("some_measurement")) {
				//size[m[mostly] [0.5-]1.5-4.5] ;// often wider than 2 cm.
				
				Pattern p = Pattern.compile(numberPattern+" ?[{<(]?[cdm]?m?[)>}]?");
				Matcher m = p.matcher(beforeThanText);
				String numeric = "";
				if(m.find()){ //a series of number
					numeric = beforeThanText.substring(m.start(), m.end()).trim().replaceAll("[{<(]$", "");
				}else{
					p = Pattern.compile("\\d+ ?[{<(]?[cdm]?m?[)>}]?"); //1 number
					m = p.matcher(beforeThanText);
					m.find();
					numeric = beforeThanText.substring(m.start(), m.end()).trim().replaceAll("[{<(]$", "");
				}
				String modifier = beforeThanText.substring(0, beforeThanText.indexOf(numeric));
				if(afterThan.isEmpty()) {
					//parse out a constraint for further process
					String constraint = beforeThanText.substring(beforeThanText.indexOf(numeric)+numeric.length()).trim();
					String t = beforeThanText;
					//parts = new String[2];//parsed out a constraint for further process
					part0 = t;
					part0 = constraint;
				}
				characters = annotateNumericals(numeric.replaceAll("[{<()>}]", ""), "some_measurement", modifier.replaceAll("[{<()>}]", ""), parents, false, processingContextState);
			}else{//size[{shorter} than {plumose} {inner}]
				//TODO instead of processing every child of the "before than" part... find a character in the before part and make the character state
				// the whole text of the before part. the character name is the one of the character found
				
				for(Chunk child : afterChunk.getChunks()) {
					log(LogLevel.DEBUG, "child " + child);
					List<DescriptionTreatmentElement> result = processingContext.getChunkProcessor(child.getChunkType()).process(child, processingContext);
					characters.addAll(result);
				}
				
				//characters = processCharacterState(part0.replaceAll("(\\{|\\})", "").trim(), parents); //numeric part
			}
		}
		
		Chunk object = null;
		LinkedList<DescriptionTreatmentElement> structures = new LinkedList<DescriptionTreatmentElement>();
		if(!afterThan.isEmpty()){
			//parts[1]: than (other) {pistillate} (paleae)]
			if(afterChunk.containsChunkType(ChunkType.ORGAN)) {
				//String ostr = part1;
				//object = ostr.replaceFirst("^.*?(?=[({])", "").replaceFirst("\\]+$", ""); //(other) {pistillate} (paleae)
				//object = "o["+object+"]";
				afterChunk.setChunkType(ChunkType.OBJECT);
				object = afterChunk;
				if(object != null){
					structures.addAll(this.extractStructuresFromObject(object, processingContext, processingContextState));
				}
			}
			//have constraints even without an organ 12/15/10
			/*for(DescriptionTreatmentElement element : characters) {
				element.setProperty("constraint", part1);
				if(object!=null) {
					element.setProperty("constraintid", listStructureIds(structures));
					//TODO: check: some constraints are without constraintid
				}
			}*/
		/*}
		
		if(!beforeThan.isEmpty()) {
			for(DescriptionTreatmentElement element : characters) {
				element.setProperty("modifier", part0 + " than");
			}
		}
		
		if(structures.size() > 0){
			return structures;
		}else{
			return characters;
		} */
	}
	
	private List<Element> createConstraintedCharacters(Chunk content, List<BiologicalEntity> parents, Chunk beforeChunk, 
			/*Chunk thanObject,*/ List<BiologicalEntity> structures, 
			ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		List<? extends Element> characters = new LinkedList<Character>();
		for(Chunk child : beforeChunk.getChunks()) {
			IChunkProcessor processor = processingContext.getChunkProcessor(child.getChunkType());
			if(processor != null) {
				characters = processor.process(child, processingContext);
				result.addAll(characters); 
			}
		}
		for(Element element : characters) {
			if(element.isCharacter()){
				((Character)element).setValue(beforeChunk.getTerminalsText()); //why rewrite character value? 
				((Character)element).setModifier(null);
			}
		}
		
		for(Element element : characters) {
			if(element.isCharacter()) {
				ProcessingContextState processingContextState = processingContext.getCurrentState();
				String clauseModifierConstraint = processingContextState.getClauseModifierContraint();
				String constraint = "";
				if(clauseModifierConstraint != null) {
					constraint += clauseModifierConstraint + "; ";
				}
				constraint += content.getChildChunk(ChunkType.THAN).getTerminalsText();
				String clauseModifierConstraintId = processingContextState.getClauseModifierContraintId();
				String structureIds = "";
				if(clauseModifierConstraintId != null) {
					structureIds += clauseModifierConstraintId + " ";
				}
				if(!structures.isEmpty())
					structureIds += listStructureIds(structures);
				((Character)element).setConstraint(constraint);
				//if(thanObject!=null) {
					((Character)element).setConstraintId(structureIds);
					//TODO: check: some constraints are without constraintid
				//}
			}
		}
		
		//correct parent elements for characters from structures to parents, Hong 1/2015
		for(BiologicalEntity structure: structures){
			for(Element character: characters){
				structure.removeElementRecursively(character);
			}
		}
		
		for(BiologicalEntity structure: parents){
			for(Element character: characters){
				if(character.isCharacter())
					structure.addCharacter((Character)character);
			}
		}
		
		return result;
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
