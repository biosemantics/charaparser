package semanticMarkup.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.log.LogLevel;
import semanticMarkup.model.Element;
import semanticMarkup.model.description.DescriptionTreatmentElement;
import semanticMarkup.model.description.DescriptionTreatmentElementType;
import semanticMarkup.markupElement.description.model.Character;

/**
 * CharacterNameChunkProcessor processes chunks of ChunkType.CHARACTER_NAME
 * @author rodenhausen
 */
public class CharacterNameChunkProcessor extends AbstractChunkProcessor {

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
	public CharacterNameChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
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
		if(!processingContextState.getLastElements().isEmpty()) {
			List<Element> lastElements = processingContextState.getLastElements();
			//DescriptionTreatmentElement lastResult = lastElements.getLast();
			
			for(Element element : lastElements){
				if(element.isCharacter()) {
					((Character)element).setName(chunk.getTerminalsText());
				}
			}
			
			
		} else {
			processingContextState.setUnassignedCharacter(chunk.getTerminalsText().toLowerCase());
		}
		return new LinkedList<Element>();
	}

}
