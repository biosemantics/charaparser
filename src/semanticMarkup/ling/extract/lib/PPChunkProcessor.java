package semanticMarkup.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PPChunkProcessor extends AbstractChunkProcessor {

	@Inject
	public PPChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("AttachToLast")boolean attachToLast, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, attachToLast, times);
	}

	@Override
	protected ArrayList<DescriptionTreatmentElement> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ArrayList<DescriptionTreatmentElement> result = new ArrayList<DescriptionTreatmentElement>();
		
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
		List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
		
		//r[{} {} p[of] o[.....]]
		List<Chunk> modifier = new ArrayList<Chunk>();// chunk.getChunks(ChunkType.MODIFIER);
		Chunk preposition = chunk.getChunkDFS(ChunkType.PREPOSITION);
		LinkedHashSet<Chunk> prepositionChunks = preposition.getChunks();
		Chunk object = chunk.getChunkDFS(ChunkType.OBJECT);
		
		if(characterPrep(chunk, processingContextState))
			return result;	
		
		boolean lastIsStructure = false;
		boolean lastIsCharacter = false;
		boolean lastIsComma = false;
		
		if (lastElements.isEmpty()) {
			unassignedModifiers.add(chunk);
			return result;
		}
		
		DescriptionTreatmentElement lastElement = lastElements.getLast();
		if(lastElement != null) {
			lastIsStructure = lastElement.isOfDescriptionType(DescriptionTreatmentElementType.STRUCTURE);
			lastIsCharacter = lastElement.isOfDescriptionType(DescriptionTreatmentElementType.CHARACTER);
			
			if(lastIsStructure && isNumerical(object)) {
				List<Chunk> modifiers = new ArrayList<Chunk>();
				result.addAll(annotateNumericals(object.getTerminalsText(), "count", modifiers, lastElements, false, processingContextState));
				return result;
			}
			
			Set<Chunk> objects = new HashSet<Chunk>();
			if(object.containsChildOfChunkType(ChunkType.OR) || object.containsChildOfChunkType(ChunkType.AND) || object.containsChildOfChunkType(ChunkType.NP_LIST)) {
				objects = splitObject(object);
			} else {
				objects.add(object);
			}
			
			LinkedList<DescriptionTreatmentElement> subjectStructures = processingContextState.getLastElements();
			if(!lastIsStructure || processingContextState.isCommaAndOrEosEolAfterLastElements()) {
				subjectStructures = processingContextState.getSubjects();
			}
			for(Chunk aObject : objects) {
				boolean lastChunkIsOrgan = true;
				boolean foundOrgan = false;
				LinkedHashSet<Chunk> beforeOrganChunks = new LinkedHashSet<Chunk>(); 
				LinkedHashSet<Chunk> organChunks = new LinkedHashSet<Chunk>();
				LinkedHashSet<Chunk> afterOrganChunks = new LinkedHashSet<Chunk>(); 
				
				this.getOrganChunks(aObject, beforeOrganChunks, organChunks, afterOrganChunks);
				foundOrgan = !organChunks.isEmpty();
				lastChunkIsOrgan = afterOrganChunks.isEmpty() && foundOrgan;
				
				/*for(Chunk objectChunk : object.getChunks()) {
					if(objectChunk.isOfChunkType(ChunkType.ORGAN)) {
						lastChunkIsOrgan = true;
						foundOrgan = true;
						
						organChunks.add(objectChunk);
					} else {
						lastChunkIsOrgan = false;
						
						if(foundOrgan)
							afterOrganChunks.add(objectChunk);
						else
							beforeOrganChunks.add(objectChunk);
					}
				}*/
				if(lastChunkIsOrgan) {
					result.addAll(linkObjects(subjectStructures, modifier, preposition, aObject, lastIsStructure, lastIsCharacter, processingContext, processingContextState));
				} else if(foundOrgan) {
					LinkedHashSet<Chunk> objectChunks = new LinkedHashSet<Chunk>();
					objectChunks.addAll(beforeOrganChunks);
					objectChunks.addAll(organChunks);
					//obj = beforeOrganChunks + organChunks;
					//modi = afterOrganChunks;
					
					Chunk objectChunk = new Chunk(ChunkType.UNASSIGNED, objectChunks);
					result.addAll(linkObjects(subjectStructures, modifier, preposition, objectChunk, lastIsStructure, lastIsCharacter, processingContext, processingContextState)); 
					//result.addAll(structures);
				} else {
					if(lastIsStructure)
						lastElement.appendAttribute("constraint", chunk.getTerminalsText());
					else if(lastIsCharacter) {
						LinkedList<DescriptionTreatmentElement> objectStructures = 
								this.extractStructuresFromObject(object, processingContext, processingContextState); 
						lastElement.appendAttribute("constraint", chunk.getTerminalsText());
						if(!objectStructures.isEmpty()) {
							result.addAll(objectStructures);
							lastElement.setAttribute("constraintId", listStructureIds(objectStructures));
						}
					}
				}
			}
		}
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}

	private Set<Chunk> splitObject(Chunk object) {
		Set<Chunk> objectChunks = new HashSet<Chunk>();
		LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
		boolean collectedOrgan = false;
		
		if(object.containsChildOfChunkType(ChunkType.NP_LIST))
			object = object.getChildChunk(ChunkType.NP_LIST);
		
		for(Chunk chunk : object.getChunks()) {
			if((chunk.isOfChunkType(ChunkType.OR) || chunk.isOfChunkType(ChunkType.AND) || chunk.getTerminalsText().equals("and") || chunk.getTerminalsText().equals("or")) && collectedOrgan) {
				Chunk objectChunk = new Chunk(ChunkType.OBJECT, childChunks);
				objectChunks.add(objectChunk);				
				childChunks.clear();
			} else {
				for(AbstractParseTree terminal : chunk.getTerminals()) {
					if(chunk.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
						collectedOrgan = true;
						childChunks.add(chunk);
					} else {
						childChunks.add(chunk);
					}
				}
			}
		}
		Chunk objectChunk = new Chunk(ChunkType.OBJECT, childChunks);
		objectChunks.add(objectChunk);
		return objectChunks;
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
