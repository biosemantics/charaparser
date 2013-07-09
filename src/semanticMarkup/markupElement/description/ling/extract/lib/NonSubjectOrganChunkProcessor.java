package semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.markupElement.description.ling.extract.AbstractChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContextState;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.model.Structure;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * NonSubjectOrganChunkProcessor processes chunks of ChunkType.NON_SUBJECT_ORGAN
 * @author rodenhausen
 */
public class NonSubjectOrganChunkProcessor extends AbstractChunkProcessor {

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
	public NonSubjectOrganChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Structure> processChunk(Chunk chunk, ProcessingContext processingContext) {
		//List<DescriptionTreatmentElement> result = new ArrayList<DescriptionTreatmentElement>();
		
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		//LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
		
		//ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		//chunks.add(chunk);
		List<Structure> result = this.establishSubject(chunk, processingContextState);
		//LinkedList<DescriptionTreatmentElement> structures = this.createStructureElements(chunks, processingContextState);
		//result.addAll(structures);
		//DescriptionTreatmentElement lastElement = lastElements.isEmpty() ? null : lastElements.getLast();
		//if(lastElement != null && lastElement.isOfDescriptionType(DescriptionTreatmentElementType.STRUCTURE))
		//	annotateType(chunk, lastElement);
		//else 
		//	processingContextState.setLastElements(structures);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}

}
