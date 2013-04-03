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
import semanticMarkup.ling.extract.IChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

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
	 * @param attachToLast
	 * @param times
	 */
	@Inject
	public SpecificPPChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
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
		LinkedList<DescriptionTreatmentElement> result = new LinkedList<DescriptionTreatmentElement>();
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
		
		if(!nonModifiers.isEmpty()) {
			if(posKnowledgeBase.isVerb(nonModifiers.get(nonModifiers.size()-1).getTerminalsText()) || preposition.getTerminalsText().equals("to")) {
				//t[c[{connected}] r[p[by] o[{conspicuous} {arachnoid} <trichomes>]]] TODO: what if c was not included in this chunk?
				
				String relation = "";
				for(Chunk nonModifier : nonModifiers) 
					relation += nonModifier.getTerminalsText() + " ";
				relation += preposition.getTerminalsText();
				
				LinkedList<DescriptionTreatmentElement> structures = extractStructuresFromObject(object, processingContext, 
						processingContextState);
				
				if(!processingContextState.getLastElements().isEmpty() && structures.isEmpty()) {
					List<DescriptionTreatmentElement> entity1 = null;
					DescriptionTreatmentElement lastElement = processingContextState.getLastElements().getLast();
					if(lastElement.isOfDescriptionType(DescriptionTreatmentElementType.CHARACTER) || processingContextState.isCommaAndOrEosEolAfterLastElements()) {
						entity1 = processingContextState.getSubjects();
					}else{
						entity1 = (LinkedList<DescriptionTreatmentElement>)processingContextState.getLastElements().clone();
					}
					
					LinkedList<DescriptionTreatmentElement> relationElement = createRelationElements(relation, entity1, structures, modifiers, false, processingContextState);
					result.addAll(relationElement);
					processingContextState.setLastElements(relationElement);
					result.addAll(structures);
				}
			} else {
				//c: {loosely} {arachnoid} : should be m[loosly] architecture[arachnoid]
				//String[] tokens = c.replaceAll("[{}]", "").split("\\s+");
				//ArrayList<Element> charas = this.processCharacterText(tokens, this.subjects);
	
				Chunk tempChunk = new Chunk(ChunkType.UNASSIGNED, nonModifiers);			
				IChunkProcessor characterStateProcessor = processingContext.getChunkProcessor(ChunkType.CHARACTER_STATE);
				result.addAll(characterStateProcessor.process(tempChunk, processingContext));
				IChunkProcessor ppProcessor = processingContext.getChunkProcessor(ChunkType.PP);
				result.addAll(ppProcessor.process(pp, processingContext)); //not as a relation
			}
		}
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}
}
