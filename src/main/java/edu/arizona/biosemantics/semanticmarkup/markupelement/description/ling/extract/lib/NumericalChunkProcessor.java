package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.HashMap;
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

/**
 * NPListChunkProcessor processes chunks of ChunkType.NUMERICALS
 * @author rodenhausen
 */
public class NumericalChunkProcessor extends AbstractChunkProcessor {

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
	public NumericalChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Character> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		//** find parents, modifiers
		//TODO: check the use of [ and ( in extreme values
		//ArrayList<Element> parents = lastStructures();
		String text = chunk.getTerminalsText().replaceAll("–", "-");
		boolean resetFrom = false;
		if(text.matches(".*\\bto \\d.*")){ //m[mostly] to 6 m ==> m[mostly] 0-6 m
			text = text.replaceFirst("to\\s+", "0-");
			resetFrom = true;
		}
		
		List<BiologicalEntity> parents = lastStructures(processingContext, processingContextState);
		
		/*String modifier1 = "";
		//m[mostly] [4-]8�12[-19] mm m[distally]; m[usually] 1.5-2 times n[size[{longer} than {wide}]]:consider a constraint
		String modifier2 = "";
		modifier1 = text.replaceFirst("\\[?\\d.*$", "");
		String rest = text.replace(modifier1, "");
		modifier1 = modifier1.replaceAll("(\\w\\[|\\]|\\{|\\})", "").trim();
		modifier2 = rest.replaceFirst(".*?(\\d|\\[|\\+|\\-|\\]|%|\\s|" + units + ")+\\s?(?=[a-z]|$)", "");// 4-5[+]
		String content = rest.replace(modifier2, "").replaceAll("(\\{|\\})", "").trim();
		modifier2 = modifier2.replaceAll("(\\w+\\[|\\]|\\{|\\})", "").trim(); */
		
		String content = "";
		for(Chunk childChunk : chunk.getChunks()) {
			if(!childChunk.isOfChunkType(ChunkType.MODIFIER))
				content += childChunk.getTerminalsText() + " ";
		}
		List<Chunk> modifiers = chunk.getChunks(ChunkType.MODIFIER);
		modifiers.addAll(processingContextState.getUnassignedModifiers());
		
		String character = text.indexOf("size") >= 0 ? "size" : null;
		if(character ==null)
			character = processingContextState.getUnassignedCharacter();
		if(character ==null)
			character = content.indexOf('/') > 0 || content.indexOf('%') > 0 || content.indexOf('.') > 0 ? "size_or_shape" : "size";

		List<Character> characters = annotateNumericals(content, character,
				modifiers, lastStructures(processingContext, processingContextState), resetFrom, processingContextState);
		processingContextState.setLastElements(characters);
		processingContextState.clearUnassignedModifiers();
		processingContextState.setUnassignedCharacter(null); //consumed
		
		if(parents.isEmpty()) {
			processingContextState.getUnassignedCharacters().addAll(characters);
		}/* else {
			for(DescriptionTreatmentElement parent : parents) {
				for(DescriptionTreatmentElement characterElement : characters) {
					parent.addTreatmentElement(characterElement);
				}
			}
		}*/
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return characters;
	}
	
	//** find parents, modifiers
	//TODO: check the use of [ and ( in extreme values
	//ArrayList<Element> parents = lastStructures();
	/*String text = ck.toString().replaceAll("�", "-");
	boolean resetfrom = false;
	if(text.matches(".*\\bto \\d.*")){ //m[mostly] to 6 m ==> m[mostly] 0-6 m
		text = text.replaceFirst("to\\s+", "0-");
		resetfrom = true;
	}
	ArrayList<Element> parents = this.attachToLast? lastStructures() : subjects;
	if(printAttach && subjects.get(0).getAttributeValue("name").compareTo(lastStructures().get(0).getAttributeValue("name")) != 0){
		log(LogLevel.DEBUG, text + " attached to "+parents.get(0).getAttributeValue("name"));
	}				
	if(debugNum){
		log(LogLevel.DEBUG, );
		log(LogLevel.DEBUG, ">>>>>>>>>>>>>"+text);
	}
	String modifier1 = "";//m[mostly] [4-]8�12[-19] mm m[distally]; m[usually] 1.5-2 times n[size[{longer} than {wide}]]:consider a constraint
	String modifier2 = "";
	modifier1 = text.replaceFirst("\\[?\\d.*$", "");
	String rest = text.replace(modifier1, "");
	modifier1 =modifier1.replaceAll("(\\w\\[|\\]|\\{|\\})", "").trim();
	modifier2 = rest.replaceFirst(".*?(\\d|\\[|\\+|\\-|\\]|%|\\s|"+ChunkedSentence.units+")+\\s?(?=[a-z]|$)", "");//4-5[+]
	String content = rest.replace(modifier2, "").replaceAll("(\\{|\\})", "").trim();
	modifier2 = modifier2.replaceAll("(\\w+\\[|\\]|\\{|\\})", "").trim();
	ArrayList<Element> chars = annotateNumericals(content, text.indexOf("size")>=0 || content.indexOf('/')>0 || content.indexOf('%')>0 || content.indexOf('.')>0? "size" : null, (modifier1+";"+modifier2).replaceAll("(^\\W|\\W$)", ""), lastStructures(), resetfrom);
	updateLatestElements(chars);*/

}
