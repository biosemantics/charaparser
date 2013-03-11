package semanticMarkup.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

public class MyModifierChunkProcessor extends AbstractChunkProcessor {

	@Inject
	public MyModifierChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("AttachToLast")boolean attachToLast, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, attachToLast, times);
	}

	@Override
	protected ArrayList<DescriptionTreatmentElement> processChunk(Chunk chunk,
			ProcessingContext processingContext) {
		//probably want to put the modifier somewhere to the processingContext
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
		if(!lastElements.isEmpty()) {
			DescriptionTreatmentElement lastElement = lastElements.getLast();
			
			if(!processingContextState.isCommaAndOrEosEolAfterLastElements() && !processingContextState.isUnassignedChunkAfterLastElements() && 
					(lastElement.isOfDescriptionType(DescriptionType.RELATION) || lastElement.isOfDescriptionType(DescriptionType.CHARACTER))) {
				lastElement.appendProperty("modifier", chunk.getTerminalsText());
			} else 
				processingContextState.getUnassignedModifiers().add(chunk);
		}
		return new ArrayList<DescriptionTreatmentElement>();
	}
}
