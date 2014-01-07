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
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IFirstChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.ParentTagProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * SomeFirstChunkProcessor poses an IFirstChunkProcessor
 * @author rodenhausen
 */
public class SomeFirstChunkProcessor extends AbstractChunkProcessor implements IFirstChunkProcessor {

	private boolean skipFirstChunk = false;
	private ParentTagProvider parentTagProvider;
	
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
	 * @param parentTagProvider
	 */
	@Inject
	public SomeFirstChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times,
			@Named("ParentTagProvider")ParentTagProvider parentTagProvider) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
		this.parentTagProvider = parentTagProvider;
	}

	@Override
	protected List<Structure> processChunk(Chunk firstChunk, ProcessingContext processingContext) {
		skipFirstChunk = false;
		List<Structure> result = new LinkedList<Structure>();
		
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		List<Chunk> chunks = processingContext.getChunkCollector().getChunks();
		
		//starts with a organ (subject)
		if(firstChunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN)) {
			result.addAll(establishSubject(firstChunk, processingContext, processingContextState));
			skipFirstChunk = true;
		} else {
			Structure structureElement;
			
			if(processingContext.getChunkCollector().getSubjectTag().equals("general")) {
				structureElement = new Structure();
				int structureIdString = processingContext.fetchAndIncrementStructureId(structureElement);
				structureElement.setId("o" + String.valueOf(structureIdString));	
				structureElement.setName("whole_organism");
				List<Structure> structureElements = new LinkedList<Structure>();
				structureElements.add(structureElement);
				result.addAll(establishSubject(structureElements, processingContextState));
				skipFirstChunk = false;
			} else if(processingContext.getChunkCollector().getSubjectTag().equals("ditto")) {
				String previousMainSubjectOrgan = parentTagProvider.getParentTag(processingContext.getChunkCollector().getSource());
				previousMainSubjectOrgan = previousMainSubjectOrgan.equals("general")? "whole_organism" : previousMainSubjectOrgan;
				structureElement = new Structure();
				int structureIdString = processingContext.fetchAndIncrementStructureId(structureElement);
				structureElement.setId("o" + String.valueOf(structureIdString));	
				structureElement.setName(previousMainSubjectOrgan);
				
				List<Structure> structureElements = new LinkedList<Structure>();
				structureElements.add(structureElement);
				result.addAll(establishSubject(structureElements, processingContextState));
				skipFirstChunk = false;
			} else {
				/*structureElement = new DescriptionTreatmentElement(DescriptionType.STRUCTURE);
				int structureIdString = processingContextState.fetchAndIncrementStructureId(structureElement);
				structureElement.setProperty("id", "o" + String.valueOf(structureIdString));	
				structureElement.setProperty("name", "whole_organism"); */
			}	
			/*LinkedList<DescriptionTreatmentElement> structureElements = new LinkedList<DescriptionTreatmentElement>();
			structureElements.add(structureElement);
			result.addAll(establishSubject(structureElements, processingContextState));
			skipFirstChunk = false; */
			
			
			
			//does not start with an organ (subject)
			/*if(firstChunk.isOfChunkType(ChunkType.PP)) {
				lastElements.clear();
				List<AbstractParseTree> chunkTerminals = firstChunk.getTerminals();
				if(chunkTerminals.get(0).equals("with")) {
					Chunk organChunk = firstChunk.getChunkDFS(ChunkType.ORGAN);
					result.addAll(establishSubject(organChunk, processingContextState));
				} else {
					if(!(secondChunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN)) && !(secondChunk.isOfChunkType(ChunkType.NP_LIST)) && 
							!(secondChunk.isOfChunkType(ChunkType.NON_SUBJECT_ORGAN))) {
						result.addAll(reestablishSubject(processingContextState));
						skipFirstChunk = true;
						return result;
					}
				}
			} else {
				if(firstChunk.isOfChunkType(ChunkType.MODIFIER) || firstChunk.isOfChunkType(ChunkType.CONSTRAINT))
					processingContextState.getUnassignedConstraints().add(firstChunk);
				result.addAll(reestablishSubject(processingContextState));
				skipFirstChunk = true;
				return result;
			}*/
		}
		
		//log(LogLevel.DEBUG, "Skip first chunk " + skipFirstChunk);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}
	
	public boolean skipFirstChunk() {
		return this.skipFirstChunk;
	}

}