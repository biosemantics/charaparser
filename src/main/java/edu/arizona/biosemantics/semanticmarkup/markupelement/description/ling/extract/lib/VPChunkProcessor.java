package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;




import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * ValuePercentageOrDegreeChunkProcessor processes chunks of ChunkType.VP
 * @author rodenhausen
 */
public class VPChunkProcessor extends AbstractChunkProcessor {

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
	public VPChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
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
		List<BiologicalEntity> parents = lastStructures(processingContext, processingContextState);
		List<Element> es = processVP(chunk, parents, processingContext, processingContextState); //apices of basal leaves spread 
		
		processingContextState.setLastElements(es);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return es;
	}
	
	
	/**
	 * 
	 * m[usually] v[comprising] o[a {surrounding} (involucre)]
	 */
	private List<Element> processVP(Chunk content, List<BiologicalEntity> parents, 
			ProcessingContext processingContext, ProcessingContextState processingContextState) {
		List<Element> results = new LinkedList<Element>();
		//String object = content.substring(content.indexOf("o["));
		Chunk object = content.getChunkDFS(ChunkType.OBJECT);
		Chunk verb = content.getChunkDFS(ChunkType.VERB);
		if(object.size() == 1 && object.containsChildOfChunkType(ChunkType.PP)) {
			Chunk pp = object.getChildChunk(ChunkType.PP);
			Chunk preposition = pp.getChunkBFS(ChunkType.PREPOSITION);
			verb.getChunks().add(preposition);
			object = pp.getChunkBFS(ChunkType.OBJECT);
		}
		//List<Chunk> modifiers = content.getChunks(ChunkType.MODIFIER);
		List<Chunk> modifiers = verb.getChunks(ChunkType.MODIFIER);
		verb.removeChunks(modifiers);
		modifiers.addAll(processingContextState.getUnassignedModifiers());
		//verb.removeChunks(modifiers);
		//String rest = content.replace(object, "").trim();
		//String relation = rest.substring(rest.indexOf("v["));
		//String modifier = rest.replace(relation, "").trim().replaceAll("(m\\[|\\])", "");
		
		if(object.containsChunkType(ChunkType.ORGAN)) { 
			List<BiologicalEntity> toStructures = this.extractStructuresFromObject(object, processingContext, processingContextState); 
			for(BiologicalEntity toStructure : toStructures)
				for(Character character : processingContextState.getUnassignedCharacters())
					toStructure.addCharacter(character);
			processingContextState.getUnassignedCharacters().clear();
			//TODO: fix content is wrong. i8: o[a] architecture[surrounding (involucre)]
			results.addAll(toStructures);
		
			String relation = verb.getTerminalsText();
			for(Chunk chunk : object.getChunks()) {
				if(chunk.isOfChunkType(ChunkType.UNASSIGNED)) {
					relation += " " + chunk.getTerminalsText();
				} else {
					break;
				}
			}
			
			/*List<Structure> fromStructures = new LinkedList<Structure>();
			for(Element element : processingContextState.getLastElements()) {
				if(element.isStructure())
					fromStructures.add((Structure)element);
			}
			if(fromStructures.isEmpty()) fromStructures = processingContextState.getSubjects();*/
			results.addAll(this.createRelationElements(relation, parents, toStructures, modifiers, false, processingContext, processingContextState));
		}
		return results;
	}
}
