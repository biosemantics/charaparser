package semanticMarkup.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionType;
import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CountChunkProcessor extends AbstractChunkProcessor {

	@Inject
	public CountChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("AttachToLast")boolean attachToLast, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, attachToLast, times);
	}

	@Override
	protected List<DescriptionTreatmentElement> processChunk(Chunk chunk, ProcessingContext processingContext) {
		LinkedList<DescriptionTreatmentElement> result = new LinkedList<DescriptionTreatmentElement>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		List<Chunk> modifiers = processingContextState.getUnassignedModifiers();
		
		LinkedList<DescriptionTreatmentElement> parents = processingContextState.getLastElements();

		if(!parents.isEmpty() && parents.getLast().isOfDescriptionType(DescriptionType.STRUCTURE)) {
			List<DescriptionTreatmentElement> characterElement = 
					this.annotateNumericals(chunk.getTerminalsText(), "count", modifiers, parents, false, processingContextState);
			//DescriptionTreatmentElement characterElement = createCharacterElement(parents, modifiers, chunk.getTerminalsText(), 
			//		"count", "", processingContextState);
			if(characterElement!=null)
				result.addAll(characterElement);
			processingContextState.clearUnassignedModifiers();
		} else {
			List<DescriptionTreatmentElement> characterElement = 
					this.annotateNumericals(chunk.getTerminalsText(), "count", modifiers, parents, false, processingContextState);
			//DescriptionTreatmentElement characterElement = createCharacterElement(parents, modifiers, chunk.getTerminalsText(), 
			//		"count", "", processingContextState);
			processingContextState.getUnassignedCharacters().addAll(characterElement);
		}
		
		processingContextState.setLastElements(result);
		processingContextState.setCommaEosEolAfterLastElements(false);
		return result;
	}

}
