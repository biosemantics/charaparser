package semanticMarkup.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionType;
import semanticMarkup.io.input.lib.db.ParentTagProvider;
import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.IFirstChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SomeFirstChunkProcessor extends AbstractChunkProcessor implements IFirstChunkProcessor {

	private boolean skipFirstChunk = false;
	private ParentTagProvider parentTagProvider;
	
	@Inject
	public SomeFirstChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("AttachToLast")boolean attachToLast, @Named("TimesWords")String times,
			@Named("parentTagProvider") ParentTagProvider parentTagProvider) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, attachToLast, times);
		this.parentTagProvider = parentTagProvider;
	}

	@Override
	protected List<DescriptionTreatmentElement> processChunk(Chunk firstChunk, ProcessingContext processingContext) {
		skipFirstChunk = false;
		List<DescriptionTreatmentElement> result = new ArrayList<DescriptionTreatmentElement>();
		
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
		List<Chunk> chunks = processingContext.getChunkCollector().getChunks();
		Chunk secondChunk = chunks.get(1);
		
		//starts with a organ (subject)
		if(firstChunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN)) {
			result.addAll(establishSubject(firstChunk, processingContextState));
			skipFirstChunk = true;
		} else {
			DescriptionTreatmentElement structureElement;
			if(processingContext.getChunkCollector().getSubjectTag().equals("ditto")) {
				String previousMainSubjectOrgan = parentTagProvider.getParentTag(processingContext.getChunkCollector().getSource());
				previousMainSubjectOrgan = previousMainSubjectOrgan.equals("general")? "whole_organism" : previousMainSubjectOrgan;
				structureElement = new DescriptionTreatmentElement(DescriptionType.STRUCTURE);
				int structureIdString = processingContextState.fetchAndIncrementStructureId(structureElement);
				structureElement.setProperty("id", "o" + String.valueOf(structureIdString));	
				structureElement.setProperty("name", previousMainSubjectOrgan);
				
				LinkedList<DescriptionTreatmentElement> structureElements = new LinkedList<DescriptionTreatmentElement>();
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
		processingContextState.setCommaEosEolAfterLastElements(false);
		return result;
	}
	
	public boolean skipFirstChunk() {
		return this.skipFirstChunk;
	}

}
