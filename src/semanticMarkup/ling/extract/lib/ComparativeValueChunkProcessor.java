package semanticMarkup.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElementType;
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

public class ComparativeValueChunkProcessor extends AbstractChunkProcessor {
	
	private String times;
	
	@Inject
	public ComparativeValueChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
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
		LinkedList<DescriptionTreatmentElement> characters = processComparativeValue(chunk, 
				parents, processingContext, processingContextState);
		
		processingContextState.setLastElements(characters);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return characters;
	}
	
	
	/**
	 * 3 times n[...than...]
	   lengths 0.5–0.6+ times <bodies>
	   ca .3.5 times length of <throat>
       1–3 times {pinnately} {lobed}
       1–2 times shape[{shape~list~pinnately~lobed~or~divided}]
       4 times longer than wide
	 * @param content: 0.5–0.6+ times a[type[bodies]]
	 * @param subjects2
	 * @return
	 */
	private LinkedList<DescriptionTreatmentElement> processComparativeValue(Chunk content,
			List<DescriptionTreatmentElement> parents, ProcessingContext processingContext, 
			ProcessingContextState processingContextState) {
		List<Chunk> beforeTimes = new ArrayList<Chunk>();
		List<Chunk> onAndAfterTimes = new ArrayList<Chunk>();
		boolean afterTimes = false;
		for(Chunk chunk : content.getChunks()) {
			if(chunk.getTerminalsText().contains("(" + times + ")")) 
				afterTimes = true;
			if(afterTimes)
				onAndAfterTimes.add(chunk);
			else
				beforeTimes.add(chunk);
		}
		//String v = content.replaceAll("("+times+").*$", "").trim(); // v holds numbers ...before times
		//String n = content.replace(v, "").trim(); 	after times
		boolean containsThan = false;
		ChunkType thanType = ChunkType.THAN_PHRASE;
		boolean containsType = false;
		boolean containsCharacterState = false;
		boolean containsObjectMainSubjectNPList = false;
		for(Chunk chunk : onAndAfterTimes) {
			//1.5–2.5 times n[{longer} than (throat)]
			if(chunk.isOfChunkType(ChunkType.THAN_PHRASE) || chunk.isOfChunkType(ChunkType.THAN_CHARACTER_PHRASE)) {
				thanType = chunk.getChunkType();
				containsThan = true;
			}
			if(chunk.isOfChunkType(ChunkType.TYPE)) 
				containsType = true;
			if(chunk.isOfChunkType(ChunkType.OBJECT) || 
					chunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN) ||
					chunk.isOfChunkType(ChunkType.NP_LIST)) 
				containsObjectMainSubjectNPList = true;
			if(chunk.isOfChunkType(ChunkType.CHARACTER_STATE))
				containsCharacterState = true;
		}
		if(containsThan) {
			Chunk sizeChunk = new Chunk(ChunkType.CHARACTER_STATE, beforeTimes);
			sizeChunk.setProperty("characterName", "size");
			
			Chunk comparisonChunk = new Chunk(ChunkType.CONSTRAINT, onAndAfterTimes);
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(sizeChunk);
			childChunks.add(comparisonChunk);
			content.setChunks(childChunks);
			processingContext.getChunkProcessor(thanType).process(content, processingContext);
		}else if(containsType){
			//size[{longer}] constraint[than (object}]
			Chunk comparisonChunk = new Chunk(ChunkType.CONSTRAINT, onAndAfterTimes);
			Chunk sizeChunk = new Chunk(ChunkType.CHARACTER_STATE, beforeTimes);
			sizeChunk.setProperty("characterName",  "size");
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(sizeChunk);
			childChunks.add(comparisonChunk);
			content.setChunks(childChunks);
			processingContext.getChunkProcessor(thanType).process(content, processingContext);			
		}else if(containsObjectMainSubjectNPList){
			//ca .3.5 times length r[p[of] o[(throat)]]
			Chunk comparisonChunk = new Chunk(ChunkType.CONSTRAINT, onAndAfterTimes);
			//times o[(bodies)] => constraint[times (bodies)]
			Chunk sizeChunk = new Chunk(ChunkType.CHARACTER_STATE, beforeTimes);
			sizeChunk.setProperty("characterName",  "size");
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(sizeChunk);
			childChunks.add(comparisonChunk);
			content.setChunks(childChunks);
			 processingContext.getChunkProcessor(thanType).process(content, processingContext);	
		}else if(containsCharacterState) { 
			//characters:1–3 times {pinnately} {lobed}
			Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, beforeTimes);
			beforeTimes.add(onAndAfterTimes.get(0));
			onAndAfterTimes.remove(0);
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(modifierChunk);
			childChunks.addAll(onAndAfterTimes);
			content.setChunks(childChunks);
			processingContext.getChunkProcessor(ChunkType.CHARACTER_STATE).process(content, processingContext);
		}else { //if(content.indexOf("[")<0){ //{forked} {moreorless} unevenly ca . 3-4 times , 
			//content = 3-4 times; v = 3-4; n=times
			//marked as a constraint to the last character "forked". "ca." should be removed from sentences in SentenceOrganStateMarker.java
			LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
			if(!lastElements.isEmpty()) {
				DescriptionTreatmentElement lastElement = lastElements.getLast();
				if(lastElement.isOfDescriptionType(DescriptionTreatmentElementType.CHARACTER)) {
					for(DescriptionTreatmentElement element : processingContextState.getLastElements()) {
						if(!processingContextState.getUnassignedModifiers().isEmpty()){
							List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
							String modifierString = "";
							for(Chunk modifier : unassignedModifiers) 
								modifierString += modifier + " ";
							element.setAttribute("modifier", modifierString);
							processingContextState.clearUnassignedModifiers();
						}
						lastElement.setAttribute("constraint", content.getTerminalsText());
					}
				} else if(lastElement.isOfDescriptionType(DescriptionTreatmentElementType.STRUCTURE)){
					return new LinkedList<DescriptionTreatmentElement>(); //parsing failure
				}
				return processingContextState.getLastElements();
			}
		}
		return new LinkedList<DescriptionTreatmentElement>();
	}
}
