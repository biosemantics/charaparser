package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
 * ComparativeValueChunkProcessor processes chunks of ChunkType.COMPARATIVE_VALUE
 * @author rodenhausen
 */
public class ComparativeValueChunkProcessor extends AbstractChunkProcessor {
	
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
	public ComparativeValueChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern,  @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		List<BiologicalEntity> parents = lastStructures(processingContext, processingContextState);
		List<Element> characters = processComparativeValue(chunk, 
				parents, processingContext, processingContextState);
		
		//processingContextState.setLastElements(characters); //set only the characters as the last elements in processComparativeValue
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return characters;
	}
	
	
	/**
	 * must have "n times":
	 * 
	 * 3 times as long as wide.
	 * 3 times longer than wide
	   lengths 0.5�0.6+ times <bodies>
	   ca .3.5 times length of <throat>
       1�3 times {pinnately} {lobed}
       1�2 times shape[{shape~list~pinnately~lobed~or~divided}]
       4 times longer than wide
	 	0.5�0.6+ times a[type[bodies]]
	 	lengths of a 2 times width of b
	 
	 COMPARATIVE_VALUE: [1-2+, times, CHARACTER_STATE: characterName->shape; [MODIFIER: [pinnately], STATE: [lobed]]]
	 COMPARATIVE_VALUE: [CHARACTER_STATE: characterName->character; [STATE: [lengths]], of, CHARACTER_STATE: characterName->position; [STATE: [proximal]], ORGAN: [branches], 2, times, array, CHARACTER_STATE: characterName->character; [STATE: [heights]]]
	 COMPARATIVE_VALUE: [2-3, times, as-long-as, CHARACTER_STATE: characterName->width; [STATE: [wide]]]
	 COMPARATIVE_VALUE: [CHARACTER_STATE: characterName->character; [STATE: [lengths]], 4-7+, times, CHARACTER_STATE: characterName->character; [STATE: [widths]]]	
	 COMPARATIVE_VALUE: [0-5, times, CHARACTER_STATE: characterName->length_or_size; [STATE: [longer]], than, CHARACTER_STATE: characterName->width; [STATE: [wide]]]
	 COMPARATIVE_VALUE: [2, times, as-long-as, CONSTRAINT: [corolla], ORGAN: [throat]]
	 
	 How to process:
	 	1. 3-times lobed: value=whole text
	 	2. convert length/width comparison to l/w
	  	3. organ length/width comparison: value = original text, constraint = organ
	 */
	private List<Element> processComparativeValue(Chunk content,
			List<BiologicalEntity> parents, ProcessingContext processingContext, 
			ProcessingContextState processingContextState) {
		List<Element> results = new LinkedList<Element> ();
		LinkedHashSet<Chunk> chunks = content.getChunks();
		LinkedHashSet<Chunk> organsBeforeNumber = new LinkedHashSet<Chunk>();
		LinkedHashSet<Chunk> organsAfterNumber = new LinkedHashSet<Chunk>();
		LinkedHashSet<Chunk> charaBeforeNumber = new LinkedHashSet<Chunk>();
		LinkedHashSet<Chunk> charaAfterNumber = new LinkedHashSet<Chunk>();
		LinkedHashSet<Chunk> characters = new LinkedHashSet<Chunk>(); //1-2+ times lobed (shape)
		LinkedHashSet<Chunk> modifiersBeforeNumber = new LinkedHashSet<Chunk>();
		String nTimes = "";
		StringBuffer origText = new StringBuffer(); //n times and all text after
		boolean beforeNumber = true;
		boolean collectBeforeOrgan = false;
		boolean collectAfterOrgan = false;
		boolean collectText = false;
		boolean isSizeCharacter = false;
		//collect info
		for(Chunk chunk: chunks){
			isSizeCharacter = isSizeCharacter(chunk.getTerminalsText());
			
			if(collectAfterOrgan){
				organsAfterNumber.add(chunk);
			}
			
			if(chunk.getTerminalsText().matches("times") || chunk.getTerminalsText().matches("[\\d()\\[\\]\\+\\./-]+")){
				if(!chunk.getTerminalsText().matches("times")) nTimes = chunk.getTerminalsText().trim(); //expect one N
				beforeNumber = false;
				collectBeforeOrgan = false;
				collectText = true;
			}else if(collectBeforeOrgan){
				organsBeforeNumber.add(chunk);
			}else if(chunk.isOfChunkType(ChunkType.MODIFIER) && beforeNumber){
				modifiersBeforeNumber.add(chunk);
			}else if(chunk.isOfChunkType(ChunkType.CHARACTER_STATE) || isSizeCharacter || chunk.containsChunkType(ChunkType.CHARACTER_STATE)){
				if((chunk.getProperty("characterName")!=null && chunk.getProperty("characterName").compareTo("character")==0) || isSizeCharacter){
					if(beforeNumber) charaBeforeNumber.add(chunk);
					else charaAfterNumber.add(chunk);
				}else{
					if(!beforeNumber) characters.add(chunk);
				}
			}else if(chunk.getTerminalsText().matches("of")){ //
				if(beforeNumber)
					collectBeforeOrgan = true;
				else
					collectAfterOrgan = true;
			}else if(!beforeNumber && !chunk.getTerminalsText().matches("than")){//can tighten up to require CONSTRAINT and ORGAN chunks
				organsAfterNumber.add(chunk);
			}
			
			if(collectText) origText.append(chunk.getTerminalsText()+" ");
		}
		

		//output
		//collect modifiers
		List<Chunk> modifiers = new LinkedList<Chunk>();
		if(!processingContextState.getUnassignedModifiers().isEmpty()){
			modifiers = processingContextState.getUnassignedModifiers();
			processingContextState.clearUnassignedModifiers();
		}
		modifiers.addAll(modifiersBeforeNumber);
		
		List<BiologicalEntity> constraints = null;
		Character chara = null;
		
		//2+ lobed
		if(organsBeforeNumber.isEmpty() && charaBeforeNumber.isEmpty() && charaAfterNumber.isEmpty() && organsAfterNumber.isEmpty()){ 
			String cName = "";
			for(Chunk character: characters){ //expect one character or a TO_PHRASE
				cName = character.getProperty("characterName");
				if(cName==null){
					cName = character.getChildChunk(ChunkType.CHARACTER_STATE).getProperty("characterName");
				}
			}
			chara = this.createCharacterElement(parents, modifiers, origText.toString(), cName==null? "unknown_character": cName, "", processingContextState, false);
		} else{
			//beforeOrgan => create new parent elements 
			if(!organsBeforeNumber.isEmpty()){
				//use the organ as the bioentity
				Chunk object = new Chunk(ChunkType.OBJECT, organsBeforeNumber);
				parents = this.extractStructuresFromObject(object, processingContext, processingContextState);
				if(!parents.isEmpty()) results.addAll(parents);
			}
		
			//afterOrgan => create constraint structure

			if(!organsAfterNumber.isEmpty()){
				//use the organ as the bioentity
				Chunk object = new Chunk(ChunkType.OBJECT, organsAfterNumber);
				constraints = this.extractStructuresFromObject(object, processingContext, processingContextState); //lastElement is now the constraints
				if(!constraints.isEmpty()) results.addAll(constraints);
			}
			
			//characters
			String beforeChar =""+"";
			String afterChar ="";
			if(!charaBeforeNumber.isEmpty()){
				//character name
				StringBuffer characterName = new StringBuffer();
				for(Chunk character: charaBeforeNumber){
					if(character.getProperty("characterName").compareTo("character")==0){
						characterName.append(getCharacter(character.getChildChunk(ChunkType.STATE).getTerminalsText())+"_or_");
					}
				}
				beforeChar = characterName.toString().replaceFirst("_or_$","");
			}
			
			if(!charaAfterNumber.isEmpty()){
				//character name:[CHARACTER_STATE: characterName->length; [STATE: [longest]], CHARACTER_STATE: characterName->character; [STATE: [diams]]]
				StringBuffer characterName = new StringBuffer();
				for(Chunk character: charaAfterNumber){
					if(character.getTerminalsText().matches("as-.*?-as")){
						characterName.append(getCharacter(character.getTerminalsText().replaceAll("-?as-?", ""))+"_or_");						
					}else if(character.getProperty("characterName")!=null && character.getProperty("characterName").compareTo("character")==0){
						characterName.append(getCharacter(character.getChildChunk(ChunkType.STATE).getTerminalsText())+"_or_"); //TODO: lengths => length
					}else if(character.getProperty("characterName")!=null){
						characterName.append(character.getProperty("characterName")+"_or_");
					}
					
				}
				afterChar = characterName.toString().replaceFirst("_or_$","");
			}
						

			boolean translate = false;
			//translate to l/w?
			if(organsAfterNumber.isEmpty() && !charaAfterNumber.isEmpty()){//"2 times longer than width", "lengths 2 times widths" "widths 2 times lengths"
				String characterValue = "";
				if(origText.toString().contains(" longer than wide") || origText.toString().contains(" longer than width") || origText.toString().contains(" longer wide") || origText.toString().contains(" longer width") || origText.toString().contains(" as-long-as wide") ){
					//TODO +1 //TODO make it more flexiable for other size characters
					characterValue = nTimes;
					translate = true;
				}else if(origText.toString().contains(" wider than long") || origText.toString().contains(" wider than length") || origText.toString().contains(" wider long") || origText.toString().contains(" wider length") || origText.toString().contains(" as-wide-as long") ){
					characterValue = "1/"+(nTimes.contains("-")? "("+nTimes+")" : nTimes);
					translate = true;
				}else if(!charaBeforeNumber.isEmpty()){
					if(beforeChar.contains("length") && afterChar.contains("width")){
						characterValue = nTimes;
						translate = true;
					}
					else if(afterChar.contains("length") && beforeChar.contains("width")){
						characterValue = "1/"+(nTimes.contains("-")? "("+nTimes+")" : nTimes);
						translate = true;
					}
					
				}
				if(translate)
					chara = this.createCharacterElement(parents, modifiers, characterValue, "l_w_ratio", "", processingContextState, false);
			}
			
			if(!translate){ //no translation, 
				// form character value
				chara = this.createCharacterElement(parents, modifiers, origText.toString().trim().replaceAll("\\bas-", "as ").replaceAll("-as\\b", " as"), 
						beforeChar.isEmpty()? (afterChar.isEmpty()? "size_or_quantity": afterChar): beforeChar, 
								"", processingContextState, false);
			}
			
			
			//add constraints to chara
			if(constraints!=null && !constraints.isEmpty()){
				String constraintIDs = "";
				String constraint = "";
				for(BiologicalEntity entity: constraints){
					constraint += entity.getName()+"; ";
					constraintIDs += entity.getId()+" ";
				}
				chara.setConstraintId(constraintIDs.trim());
				chara.setConstraint(constraint.replaceFirst("; $", ""));
			}
		}
	
		if(chara!=null){
			//record the character in ContextState
			List<Element> charas = new LinkedList<Element> ();
			charas.add(chara);
			processingContextState.setLastElements(charas); //set only the characters as the last elements
			results.add((Element) chara);
			return results;
		}
		return new LinkedList<Element>();
		/* not the last constraint structure
		if(lastChunk.isOfChunkType(ChunkType.ORGAN) || lastChunk.isOfChunkType(ChunkType.NP_LIST)){
			processingContextState.setLastElements(constraints);
			LinkedList<Element> results = new LinkedList<Element> ();
			for(BiologicalEntity entity: constraints)
				results.add((Element) entity);
			return results;
		}
		
		return new LinkedList<Element>();*/
		
	}

	private boolean isSizeCharacter(String string) {
		string = string.replaceAll("\\bas-", "").replaceAll("-as\\b", "").trim();
		return getCharacter(string)!=null;
	}

	/**
	 * wide, long, broad, diam, high, tall, thick
	 * see also BasicConfiguration.getEqualCharacters()
	 * @param string
	 * @return
	 */
	private String getCharacter(String string) {
		if(string.startsWith("long")) return "length";
		if(string.startsWith("wide")) return "width";
		if(string.startsWith("broad")) return "width";
		if(string.equals("diam")) return "diameter";
		if(string.equals("diams")) return "diameter";
		if(string.startsWith("high")) return "height";
		if(string.startsWith("tall")) return "heigth";
		if(string.startsWith("thick")) return "thickness";
		
		if(string.equals("lengths")) return "length";
		if(string.equals("widths")) return "width";
		if(string.equals("heights")) return "height";
		if(string.equals("length")) return "length";
		if(string.equals("width")) return "width";
		if(string.equals("height")) return "height";
		if(string.equals("thicknesses")) return "thickness";
		
		return null;
	}
	/*private LinkedList<Element> processComparativeValue(Chunk content,
			List<BiologicalEntity> parents, ProcessingContext processingContext, 
			ProcessingContextState processingContextState) {
		List<Chunk> beforeTimes = new ArrayList<Chunk>();
		List<Chunk> onAndAfterTimes = new ArrayList<Chunk>();
		boolean afterTimes = false;
		for(Chunk chunk : content.getChunks()) {
			if(chunk.getTerminalsText().matches("(" + times + ")")) 
				afterTimes = true;
			if(afterTimes)
				onAndAfterTimes.add(chunk);
			else
				beforeTimes.add(chunk);
		}
		//String v = content.replaceAll("("+times+").*$", "").trim(); // v holds numbers ...before times
		//String n = content.replace(v, "").trim(); 	after times
		boolean containsThan = false;
		ChunkType thanType = ChunkType.THAN_PHRASE;
		boolean containsType = false;
		boolean containsCharacterState = false;
		boolean containsObjectMainSubjectNPList = false;
		for(Chunk chunk : onAndAfterTimes) {
			//1.5�2.5 times n[{longer} than (throat)]
			if(chunk.isOfChunkType(ChunkType.THAN_PHRASE) || chunk.isOfChunkType(ChunkType.THAN_CHARACTER_PHRASE)) {
				thanType = chunk.getChunkType();
				containsThan = true;
			}
			if(chunk.isOfChunkType(ChunkType.TYPE)) 
				containsType = true;
			if(chunk.containsChunkType(ChunkType.ORGAN) || chunk.containsChunkType(ChunkType.OBJECT) || 
					chunk.containsChunkType(ChunkType.NP_LIST)) 
				containsObjectMainSubjectNPList = true;
			if(chunk.isOfChunkType(ChunkType.CHARACTER_STATE))
				containsCharacterState = true;
		}
		if(containsThan) {
			Chunk sizeChunk = new Chunk(ChunkType.CHARACTER_STATE, beforeTimes);
			sizeChunk.setProperty("characterName", "size");
			
			Chunk comparisonChunk = new Chunk(ChunkType.CONSTRAINT, onAndAfterTimes);
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(sizeChunk);
			childChunks.add(comparisonChunk);
			content.setChunks(childChunks);
			processingContext.getChunkProcessor(thanType).process(content, processingContext);
		}else if(containsType){
			//size[{longer}] constraint[than (object}]
			Chunk comparisonChunk = new Chunk(ChunkType.CONSTRAINT, onAndAfterTimes);
			Chunk sizeChunk = new Chunk(ChunkType.CHARACTER_STATE, beforeTimes);
			sizeChunk.setProperty("characterName",  "size");
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(sizeChunk);
			childChunks.add(comparisonChunk);
			content.setChunks(childChunks);
			processingContext.getChunkProcessor(thanType).process(content, processingContext);			
		}else if(containsObjectMainSubjectNPList){
			//ca .3.5 times length r[p[of] o[(throat)]]
			Chunk comparisonChunk = new Chunk(ChunkType.CONSTRAINT, onAndAfterTimes);
			//times o[(bodies)] => constraint[times (bodies)]
			Chunk valueChunk = new Chunk(ChunkType.VALUE, beforeTimes);
			//Chunk sizeChunk = new Chunk(ChunkType.CHARACTER_STATE, beforeTimes);
			//sizeChunk.setProperty("characterName",  "size");
			//LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			//childChunks.add(sizeChunk);
			//childChunks.add(comparisonChunk);
			//content.setChunks(childChunks);
			List<BiologicalEntity> structures = 
					this.extractStructuresFromObject(comparisonChunk, processingContext, processingContextState);
			processingContextState = processingContext.getCurrentState();
			processingContextState.setClauseModifierContraint(comparisonChunk.getTerminalsText());
			processingContextState.setLastElements(parents);
			processingContextState.setClauseModifierContraintId(this.listStructureIds(structures));
			LinkedList<Element> result = new LinkedList<Element>(
					processingContext.getChunkProcessor(ChunkType.VALUE).process(valueChunk, processingContext));
			processingContext.getCurrentState().clearUnassignedModifiers();
			return result;
		}else if(containsCharacterState) { 
			//characters:1�3 times {pinnately} {lobed}
			Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, beforeTimes);
			beforeTimes.add(onAndAfterTimes.get(0));
			onAndAfterTimes.remove(0);
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(modifierChunk);
			childChunks.addAll(onAndAfterTimes);
			content.setChunks(childChunks);
			processingContext.getChunkProcessor(ChunkType.CHARACTER_STATE).process(content, processingContext);
		}else { //if(content.indexOf("[")<0){ //{forked} {moreorless} unevenly ca . 3-4 times , 
			//content = 3-4 times; v = 3-4; n=times
			//marked as a constraint to the last character "forked". "ca." should be removed from sentences in SentenceOrganStateMarker.java
			LinkedList<Element> lastElements = processingContextState.getLastElements();
			if(!lastElements.isEmpty()) {
				Element lastElement = lastElements.getLast();
				if(lastElement.isCharacter()) {
					for(Element element : processingContextState.getLastElements()) {
						if(element.isCharacter()) {
							if(!processingContextState.getUnassignedModifiers().isEmpty()){
								List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
								String modifierString = "";
								for(Chunk modifier : unassignedModifiers) 
									((Character)element).appendModifier(modifier.getTerminalsText());
								processingContextState.clearUnassignedModifiers();
							}
						}
						((Character)lastElement).setConstraint(content.getTerminalsText());
					}
				} else if(lastElement.isStructure()){
					return new LinkedList<Element>(); //parsing failure
				}
				return processingContextState.getLastElements();
			}
		}
		return new LinkedList<Element>();
	}*/
}
