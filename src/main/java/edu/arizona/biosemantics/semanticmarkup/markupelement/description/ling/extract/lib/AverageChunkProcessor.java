/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * @author Hong Cui
 *
 */
public class AverageChunkProcessor extends AbstractChunkProcessor {

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
	public AverageChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps , @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		String chara = "average_";
		HashSet<String> characterNames = new HashSet<String>();
		String last = "";
		for(Element e: lastElements){
		  if(e.isCharacter()){
			  characterNames.add(((Character)e).getName());
			  last = ((Character)e).getName();
		  }
		  if(e.isStructure()){
			 LinkedHashSet<Character> chs = ((BiologicalEntity)e).getCharacters();
			 for(Character c: chs){
				 characterNames.add(c.getName());
				 last = c.getName();
			 }
		  }
		}
		if(characterNames.size()==1) chara = chara + characterNames.iterator().next();
		else if(characterNames.size()==2 && characterNames.contains("length") && characterNames.contains("width")) chara = chara + "area";
		else if(characterNames.size()>1) chara = chara + last;
		List<Chunk> modifiers = new LinkedList<Chunk>();
		ArrayList<String> alternativeIds = new ArrayList<String>();
		List<Element> characters = annotateNumericals(chunk.getTerminalsText(), chara, modifiers, 
				parentStructures(processingContext, processingContextState, alternativeIds), false, processingContextState);
		addAlternativeIds(characters, alternativeIds);
		processingContextState.setLastElements(characters);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return characters;
	}


}
