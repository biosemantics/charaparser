package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;





import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * SpecificPPChunkProcessor processes chunks of ChunkType.SPECIFIC_PP
 * @author rodenhausen
 */
public class SpecificPPChunkProcessor extends AbstractChunkProcessor {

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
	public SpecificPPChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
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
		List<Element> result = new LinkedList<Element>();
		//having oval outline
		if(characterPrep(chunk, processingContextState))
			return result;
	
		Chunk specifier = chunk.getChunkDFS(ChunkType.SPECIFIER);
		Chunk pp = chunk.getChunkDFS(ChunkType.PP);
		if(!pp.containsChunkType(ChunkType.OBJECT)) {
			return result;
		}
		
		//{often} {dispersed} r[p[with] o[aid  r[p[from] o[(pappi)]]]] 
		Chunk object = pp.getChunkDFS(ChunkType.OBJECT);
		Chunk preposition = pp.getChunkDFS(ChunkType.PREPOSITION);
		
		LinkedHashSet<Chunk> specifierChunks = specifier.getChunks(); //mc, without modifiers c
		List<Chunk> modifiers = specifier.getChunks(ChunkType.MODIFIER); //m
		List<Chunk> nonModifiers = new ArrayList<Chunk>();
		for(Chunk specifierChunk : specifierChunks) 
			if(!specifierChunk.isOfChunkType(ChunkType.MODIFIER))
				nonModifiers.add(specifierChunk);
		
		//c: {loosely} {arachnoid}
		String unassignedChara = processingContextState.getUnassignedCharacter();
		if(!nonModifiers.isEmpty()) {
			if(unassignedChara==null && (posKnowledgeBase.isVerb(nonModifiers.get(nonModifiers.size()-1).getTerminalsText()) || preposition.getTerminalsText().equals("to"))) {
				//t[c[{connected}] r[p[by] o[{conspicuous} {arachnoid} <trichomes>]]] TODO: what if c was not included in this chunk?
				
				String relation = "";
				for(Chunk nonModifier : nonModifiers) 
					relation += nonModifier.getTerminalsText() + " ";
				relation += preposition.getTerminalsText();
				LinkedList<Element> lastElements = processingContextState.getLastElements();
				
				List<BiologicalEntity> structures = extractStructuresFromObject(object, processingContext, 
						processingContextState);
				
				if(!processingContextState.getLastElements().isEmpty() && !structures.isEmpty()) {
					List<BiologicalEntity> entity1 = null;
					//Element lastElement = processingContextState.getLastElements().getLast();
					Element lastElement = lastElements.getLast();
					if(lastElement.isCharacter() || processingContextState.isCommaAndOrEosEolAfterLastElements()) {
						entity1 = processingContextState.getSubjects();
					} else {
						entity1 = new LinkedList<BiologicalEntity>();
						//for(Element element : processingContextState.getLastElements()) {
						for(Element element : lastElements) {
							if(element.isStructure())
								entity1.add((BiologicalEntity)element);
						}
					}
					
					List<Relation> relationElement = createRelationElements(relation, entity1, structures, modifiers, false, processingContext, processingContextState);
					result.addAll(relationElement);
					processingContextState.setLastElements(relationElement);
					result.addAll(structures);
					processingContextState.setLastElements(structures);//Hong 9/11/14
				}
			} else {
				//c: {loosely} {arachnoid} : should be m[loosly] architecture[arachnoid]
				//String[] tokens = c.replaceAll("[{}]", "").split("\\s+");
				//ArrayList<Element> charas = this.processCharacterText(tokens, this.subjects);

				/*Chunk tempChunk = new Chunk(ChunkType.UNASSIGNED, nonModifiers);			
				for(Chunk specifierChunk : specifierChunks) {
					if(chunk.isOfChunkType(ChunkType.CHARACTER_STATE)) {
						IChunkProcessor characterStateProcessor = processingContext.getChunkProcessor(ChunkType.CHARACTER_STATE);
						result.addAll(characterStateProcessor.process(specifierChunk, processingContext));
					}
				}*/
				IChunkProcessor characterStateProcessor = processingContext.getChunkProcessor(ChunkType.CHARACTER_STATE);
				if(specifier.containsChunkType(ChunkType.CHARACTER_STATE)){ //could also contain modifiers etc.
					result.addAll(characterStateProcessor.process(specifier, processingContext));
				}else if(unassignedChara!=null){ //make the nonModifiers chunks CHARACTER_STATE chunks
					/*for(Chunk nm: nonModifiers){
						if(nm.isOfChunkType(ChunkType.UNASSIGNED)){
							Chunk stateChunk = new Chunk(ChunkType.STATE, nm);
							Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, stateChunk);
							characterChunk.setProperty("characterName", unassignedChara);
							nm = characterChunk;
						}else{
							nm.setChunkType(ChunkType.CHARACTER_STATE);
							nm.setProperty("characterName", unassignedChara);
						}
					}*/
					Chunk stateChunk = new Chunk(ChunkType.STATE, nonModifiers); //lost modifiers. SPECIFIC_PP: [SPECIFIER: [changing], PP: [PREPOSITION: [with], OBJECT: [the, ORGAN: [growth]]]]
					Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, stateChunk);
					characterChunk.setProperty("characterName", unassignedChara);
					processingContextState.setUnassignedCharacter(null);
					result.addAll(characterStateProcessor.process(characterChunk, processingContext));	//chunkCollector is not updated with characterChunk
				}
				processingContextState.getCarryOverDataFrom(processingContext.getCurrentState());
				processingContext.setCurrentState(processingContextState);
				log(LogLevel.DEBUG, "restored current state after "+characterStateProcessor.getClass()+" is run.");
				IChunkProcessor ppProcessor = processingContext.getChunkProcessor(ChunkType.PP);
				result.addAll(ppProcessor.process(pp, processingContext)); //not as a relation
				
			}
		}
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}
}
