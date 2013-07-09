package semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
 * ComparativeValueChunkProcessor processes chunks of ChunkType.COMPARATIVE_VALUE
 * @author rodenhausen
 */
public class ComparativeValueChunkProcessor extends AbstractChunkProcessor {
	
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
	public ComparativeValueChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern,  @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		List<Structure> parents = lastStructures(processingContext, processingContextState);
		LinkedList<Element> characters = processComparativeValue(chunk, 
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
	 	content: 0.5–0.6+ times a[type[bodies]]
	 	subjects2
	 */
	private LinkedList<Element> processComparativeValue(Chunk content,
			List<Structure> parents, ProcessingContext processingContext, 
			ProcessingContextState processingContextState) {
		List<Chunk> beforeTimes = new ArrayList<Chunk>();
		List<Chunk> onAndAfterTimes = new ArrayList<Chunk>();
		boolean afterTimes = false;
		for(Chunk chunk : content.getChunks()) {
			if(chunk.getTerminalsText().matches("(" + times + ")")) 
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
			if(chunk.containsChunkType(ChunkType.ORGAN) || chunk.containsChunkType(ChunkType.OBJECT) || 
					chunk.containsChunkType(ChunkType.NP_LIST)) 
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
			Chunk valueChunk = new Chunk(ChunkType.VALUE, beforeTimes);
			//Chunk sizeChunk = new Chunk(ChunkType.CHARACTER_STATE, beforeTimes);
			//sizeChunk.setProperty("characterName",  "size");
			//LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			//childChunks.add(sizeChunk);
			//childChunks.add(comparisonChunk);
			//content.setChunks(childChunks);
			List<Structure> structures = 
					this.extractStructuresFromObject(comparisonChunk, processingContext, processingContextState);
			processingContextState = processingContext.getCurrentState();
			processingContextState.setClauseModifierContraint(comparisonChunk.getTerminalsText());
			processingContextState.setLastElements(parents);
			processingContextState.setClauseModifierContraintId(this.listStructureIds(structures));
			LinkedList<Element> result = new LinkedList<Element>(
					processingContext.getChunkProcessor(ChunkType.VALUE).process(valueChunk, processingContext));
			processingContext.getCurrentState().clearUnassignedModifiers();
			return result;
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
			LinkedList<Element> lastElements = processingContextState.getLastElements();
			if(!lastElements.isEmpty()) {
				Element lastElement = lastElements.getLast();
				if(lastElement.isCharacter()) {
					for(Element element : processingContextState.getLastElements()) {
						if(element.isCharacter()) {
							if(!processingContextState.getUnassignedModifiers().isEmpty()){
								List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
								String modifierString = "";
								for(Chunk modifier : unassignedModifiers) 
									((Character)element).appendModifier(modifier.getTerminalsText());
								processingContextState.clearUnassignedModifiers();
							}
						}
						((Character)lastElement).setConstraint(content.getTerminalsText());
					}
				} else if(lastElement.isStructure()){
					return new LinkedList<Element>(); //parsing failure
				}
				return processingContextState.getLastElements();
			}
		}
		return new LinkedList<Element>();
	}
}
