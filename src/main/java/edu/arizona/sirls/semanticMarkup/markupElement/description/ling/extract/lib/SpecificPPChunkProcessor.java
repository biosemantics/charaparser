package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.know.ICharacterKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.know.IGlossary;
import edu.arizona.sirls.semanticMarkup.know.IPOSKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.ling.chunk.Chunk;
import edu.arizona.sirls.semanticMarkup.ling.chunk.ChunkType;
import edu.arizona.sirls.semanticMarkup.ling.extract.IChunkProcessor;
import edu.arizona.sirls.semanticMarkup.ling.transform.IInflector;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.ProcessingContextState;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Relation;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Structure;
import edu.arizona.sirls.semanticMarkup.model.Element;

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
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
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
		
		if(!nonModifiers.isEmpty()) {
			if(posKnowledgeBase.isVerb(nonModifiers.get(nonModifiers.size()-1).getTerminalsText()) || preposition.getTerminalsText().equals("to")) {
				//t[c[{connected}] r[p[by] o[{conspicuous} {arachnoid} <trichomes>]]] TODO: what if c was not included in this chunk?
				
				String relation = "";
				for(Chunk nonModifier : nonModifiers) 
					relation += nonModifier.getTerminalsText() + " ";
				relation += preposition.getTerminalsText();
				
				List<Structure> structures = extractStructuresFromObject(object, processingContext, 
						processingContextState);
				
				if(!processingContextState.getLastElements().isEmpty() && !structures.isEmpty()) {
					List<Structure> entity1 = null;
					Element lastElement = processingContextState.getLastElements().getLast();
					if(lastElement.isCharacter() || processingContextState.isCommaAndOrEosEolAfterLastElements()) {
						entity1 = processingContextState.getSubjects();
					} else {
						entity1 = new LinkedList<Structure>();
						for(Element element : processingContextState.getLastElements()) {
							if(element.isStructure())
								entity1.add((Structure)element);
						}
					}
					
					List<Relation> relationElement = createRelationElements(relation, entity1, structures, modifiers, false, processingContext, processingContextState);
					result.addAll(relationElement);
					processingContextState.setLastElements(relationElement);
					result.addAll(structures);
				}
			} else {
				//c: {loosely} {arachnoid} : should be m[loosly] architecture[arachnoid]
				//String[] tokens = c.replaceAll("[{}]", "").split("\\s+");
				//ArrayList<Element> charas = this.processCharacterText(tokens, this.subjects);

				Chunk tempChunk = new Chunk(ChunkType.UNASSIGNED, nonModifiers);			
				for(Chunk specifierChunk : specifierChunks) {
					if(chunk.isOfChunkType(ChunkType.CHARACTER_STATE)) {
						IChunkProcessor characterStateProcessor = processingContext.getChunkProcessor(ChunkType.CHARACTER_STATE);
						result.addAll(characterStateProcessor.process(specifierChunk, processingContext));
					}
				}
				IChunkProcessor ppProcessor = processingContext.getChunkProcessor(ChunkType.PP);
				result.addAll(ppProcessor.process(pp, processingContext)); //not as a relation
			}
		}
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}
}
