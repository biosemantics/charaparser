package semanticMarkup.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.IChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ThanChunkProcessor extends AbstractChunkProcessor {

	@Inject
	public ThanChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
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
		LinkedList<DescriptionTreatmentElement> result = processTHAN(chunk, processingContextState.getSubjects(), processingContext, 
				processingContextState);
		processingContextState.setLastElements(result);
		processingContextState.setCommaEosEolAfterLastElements(false);
		return result;
	}
	
	/**
	 * size[{longer}] constraint[than (object)]";
	 * shape[{lobed} constraint[than (proximal)]]
	 * @param replaceFirst
	 * @param subjects2
	 * @return
	 */
	private LinkedList<DescriptionTreatmentElement> processTHAN(Chunk content, LinkedList<DescriptionTreatmentElement> parents, 
			ProcessingContext processingContext, ProcessingContextState processingContextState) {
		LinkedList<DescriptionTreatmentElement> result = new LinkedList<DescriptionTreatmentElement>();
		
		LinkedHashSet<Chunk> beforeThan = new LinkedHashSet<Chunk>();
		for(Chunk chunk : content.getChunks()) {
			if(chunk.isOfChunkType(ChunkType.THAN))
				break;
			else 
				beforeThan.add(chunk);
		}
		Chunk beforeChunk = new Chunk(ChunkType.UNASSIGNED, beforeThan);
		
		Chunk thanChunk = content.getChildChunk(ChunkType.THAN);
		if(!thanChunk.containsChunkType(ChunkType.PP)) {
			if(thanChunk.containsChunkType(ChunkType.CONSTRAINT) && !thanChunk.containsChunkType(ChunkType.ORGAN)) {
				Chunk constraint = thanChunk.getChunks(ChunkType.CONSTRAINT).get(0);
				IChunkProcessor chunkProcessor = processingContext.getChunkProcessor(ChunkType.CONSTRAINT);
				List<DescriptionTreatmentElement> structures = chunkProcessor.process(constraint, processingContext);
				result.addAll(structures);
				this.createConstraintedCharacters(content, beforeChunk, structures, processingContext);
			}
		} else {
			Chunk thanObject = thanChunk.getChunkDFS(ChunkType.OBJECT);
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
				LinkedList<DescriptionTreatmentElement> structures = extractStructuresFromObject(objectChunk, processingContext, processingContextState);
				result.addAll(structures);
				result.addAll(this.createConstraintedCharacters(content, beforeChunk, structures, processingContext));
			} else {
				if(thanObject.containsChildOfChunkType(ChunkType.COUNT) || thanObject.containsChildOfChunkType(ChunkType.BASED_COUNT) ||
						thanObject.containsChildOfChunkType(ChunkType.AREA) || thanObject.containsChildOfChunkType(ChunkType.NUMERICALS) ||
						thanObject.containsChildOfChunkType(ChunkType.VALUE_DEGREE) || thanObject.containsChildOfChunkType(ChunkType.VALUE) ||
						thanObject.containsChildOfChunkType(ChunkType.VALUE_PERCENTAGE)) {
					//second case
					for(Chunk child : thanObject.getChunks()) {
						log(LogLevel.DEBUG, "child " + child);
						List<DescriptionTreatmentElement> characters = processingContext.getChunkProcessor(child.getChunkType()).process(child, processingContext);
						result.addAll(characters);
					}
					if(!beforeThan.isEmpty()) {
						for(DescriptionTreatmentElement element : result) {
							element.setProperty("modifier", beforeChunk.getTerminalsText() + " than");
						}
					}
				} else {
					//third case
					for(Chunk child : thanObject.getChunks()) {
						log(LogLevel.DEBUG, "child " + child);
						if(child.isOfChunkType(ChunkType.CHARACTER_STATE)) {
							LinkedHashSet<Chunk> characterTerminals = new LinkedHashSet<Chunk>();
							characterTerminals.addAll(content.getTerminals());
							child.getChunks(ChunkType.STATE).get(0).setChunks(characterTerminals);
							List<DescriptionTreatmentElement> characters = processingContext.getChunkProcessor(child.getChunkType()).process(child, processingContext);
							result.addAll(characters);
							break;
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
					beforeChunk.getChunkDFS(ChunkType.CHARACTER_STATE).getProperty("characterName").equals("size")) {
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
				characters = annotateNumericals(numeric.replaceAll("[{<()>}]", ""), "size", modifier.replaceAll("[{<()>}]", ""), parents, false, processingContextState);
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
					element.setProperty("constraintId", listStructureIds(structures));
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
	
	private List<DescriptionTreatmentElement> createConstraintedCharacters(Chunk content, Chunk beforeChunk, /*Chunk thanObject,*/ List<DescriptionTreatmentElement> structures, 
			ProcessingContext processingContext) {
		List<DescriptionTreatmentElement> result = new LinkedList<DescriptionTreatmentElement>();
		List<DescriptionTreatmentElement> characters = new LinkedList<DescriptionTreatmentElement>();
		for(Chunk child : beforeChunk.getChunks()) {
			log(LogLevel.DEBUG, "child " + child);
			IChunkProcessor processor = processingContext.getChunkProcessor(child.getChunkType());
			if(processor != null) {
				characters = processor.process(child, processingContext);
				result.addAll(characters);
			}
		}
		
		for(DescriptionTreatmentElement element : characters) {
			element.setProperty("constraint", content.getChildChunk(ChunkType.THAN).getTerminalsText());
			//if(thanObject!=null) {
				element.setProperty("constraintId", listStructureIds(structures));
				//TODO: check: some constraints are without constraintid
			//}
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
