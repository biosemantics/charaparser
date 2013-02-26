package semanticMarkup.ling.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionType;
import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class AbstractChunkProcessor implements IChunkProcessor {

	protected IInflector inflector;
	protected IGlossary glossary;
	protected ITerminologyLearner terminologyLearner;
	protected Set<String> baseCountWords;
	protected Set<String> locationPrepositions;
	protected Set<String> clusters;
	protected ICharacterKnowledgeBase characterKnowledgeBase;
	protected IPOSKnowledgeBase posKnowledgeBase;
	protected String units;
	protected HashMap<String, String> equalCharacters;
	protected String numberPattern;
	protected boolean attachToLast;
	protected String times;
	
	@Inject
	public AbstractChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositions")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("AttachToLast")boolean attachToLast, @Named("Times")String times) {
		this.inflector = inflector;
		this.glossary = glossary;
		this.characterKnowledgeBase = characterKnowledgeBase;
		this.posKnowledgeBase = posKnowledgeBase;
		this.terminologyLearner = terminologyLearner;
		this.baseCountWords = baseCountWords;
		this.locationPrepositions = locationPrepositions;
		this.clusters = clusters;
		this.units = units;
		this.equalCharacters = equalCharacters;
		this.numberPattern = numberPattern;
		this.attachToLast = attachToLast;
		this.times = times;
	}
	

	public List<DescriptionTreatmentElement> process(Chunk chunk, ProcessingContext processingContext) {
		log(LogLevel.DEBUG, "process chunk " + chunk);
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		processingContext.addState(chunk, processingContextState);
		ProcessingContextState newState = (ProcessingContextState)processingContextState.clone();
		processingContext.setCurrentState(newState);
		return processChunk(chunk, processingContext);
	}
	
	protected abstract List<DescriptionTreatmentElement> processChunk(Chunk chunk, ProcessingContext processingContext);
		
	protected ArrayList<DescriptionTreatmentElement> establishSubject(LinkedList<DescriptionTreatmentElement> subjectStructures, 
			ProcessingContextState processingContextState) {
		ArrayList<DescriptionTreatmentElement> result = new ArrayList<DescriptionTreatmentElement>();
		result.addAll(subjectStructures);
		
		LinkedList<DescriptionTreatmentElement> subjects = processingContextState.getSubjects();
		LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
		subjects.clear();
		
		lastElements.clear();
		
		for(DescriptionTreatmentElement element : subjectStructures) {
			if(element.isOfDescriptionType(DescriptionType.STRUCTURE)) {
				//ignore character elements
				subjects.add(element);
				lastElements.add(element);
			}
		}
		return result;
	}
	
	protected ArrayList<DescriptionTreatmentElement> establishSubject(
			Chunk subjectChunk, ProcessingContextState processingContextState) {
		log(LogLevel.DEBUG, "establish subject from " + subjectChunk);
		ArrayList<DescriptionTreatmentElement> result = new ArrayList<DescriptionTreatmentElement>();
		
		ArrayList<Chunk> subjectChunks = new ArrayList<Chunk>();
		subjectChunks.add(subjectChunk);
		subjectChunks.addAll(processingContextState.getUnassignedConstraints());
		processingContextState.clearUnassignedConstraints();
		LinkedList<DescriptionTreatmentElement> subjectStructures = createStructureElements(subjectChunks, processingContextState);
		return this.establishSubject(subjectStructures, processingContextState);
	}

	
	protected ArrayList<DescriptionTreatmentElement> reestablishSubject(ProcessingContextState processingContextState) {
		log(LogLevel.DEBUG, "reestablish subject");
		ArrayList<DescriptionTreatmentElement> result = new ArrayList<DescriptionTreatmentElement>();
		
		LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
		LinkedList<DescriptionTreatmentElement> subjects = processingContextState.getSubjects();
		lastElements.clear();
		for(DescriptionTreatmentElement element : subjects) {
			lastElements.add(element);
			//element.detach();
			//result.remove(element);
			result.add(element);
		}
		return result;
	}

	protected LinkedList<DescriptionTreatmentElement> createStructureElements(List<Chunk> subjectChunks, ProcessingContextState processingContextState) {
		//assumption: all the information can more easily be extracted if chunk structure (subchunks) is considered instead of just the plain string
		//therefore use a very simplified implementation now and then later go from examples to tune
		
		LinkedList<DescriptionTreatmentElement> results = new LinkedList<DescriptionTreatmentElement>();	
		//String[] organs = listofstructures.replaceAll(" (and|or|plus) ", " , ").split("\\)\\s*,\\s*"); //TODO: flower and leaf blades???
		
		//subjectChunk split according to and or plus see below
		//String[] organs = structuresString.split("\\)\\s+(and|or|plus|,)\\s+"); //TODO: flower and leaf blades???		
		//mohan 28/10/2011. If the first organ is a preposition then join the preposition with the following organ
		
		/*for (int i = 0; i < organs.length; i++) {
			if (organs[i].matches("\\{r\\[p\\[.*\\]\\]\\}\\s+\\{.*\\}\\s+.*")) {
				organs[i] = organs[i].replaceAll("\\]\\]\\}\\s\\{", "]]}-{");
			}
		}*/
		
		//assumption: this can be extracted
		
		Chunk subjectChunk = new Chunk(ChunkType.UNASSIGNED, subjectChunks);
		log(LogLevel.DEBUG, "create structure element from subjectChunks " + subjectChunks);
		Chunk organChunk = subjectChunk.getChunkDFS(ChunkType.ORGAN);
		//subjectChunk.getChunks(ChunkType.ORGAN);

		
		//log(LogLevel.DEBUG, "organChunk " + organChunk);
		if(organChunk!=null) {
			
			DescriptionTreatmentElement structureElement = new DescriptionTreatmentElement(DescriptionType.STRUCTURE);
			int structureIdString = processingContextState.fetchAndIncrementStructureId(structureElement);
			structureElement.setProperty("id", "o" + String.valueOf(structureIdString));
		
			List<Chunk> constraintChunks = subjectChunk.getChunks(ChunkType.CONSTRAINT);
			if(!constraintChunks.isEmpty()) {
				Chunk constraintsChunk = new Chunk(ChunkType.UNASSIGNED, constraintChunks);
				structureElement.setProperty("constraint", constraintsChunk.getTerminalsText());
			}
	
			String organName = organChunk.getTerminalsText();
			structureElement.setProperty("name", inflector.getSingular(organName));
			
			
			LinkedList<DescriptionTreatmentElement> parents = new LinkedList<DescriptionTreatmentElement>();
			parents.add(structureElement);
			
			List<AbstractParseTree> terminals = subjectChunk.getTerminals();
			for(int i=0; i<terminals.size(); i++) {
				if(organChunk.contains(terminals.get(i))) {
					if(i-1>=0 && (terminals.get(i-1).getTerminalsText().equals("a") || terminals.get(i-1).getTerminalsText().equals("an"))) {
						this.createCharacterElement(parents, null, "1", "count", "", processingContextState);
					}
					break;
				}
			}
			
			List<Chunk> characterStateChunks = subjectChunk.getChunks(ChunkType.CHARACTER_STATE);
			for(Chunk characterStateChunk : characterStateChunks) {
				String character = characterStateChunk.getProperty("characterName");
				
				//constraint rather than 
				if(character.equals("insertion_or_position")) {
					
				}
				Chunk state = characterStateChunk.getChunkDFS(ChunkType.STATE);
				List<Chunk> modifierChunks = subjectChunk.getChunks(ChunkType.MODIFIER);
				//List<Chunk> modifierChunks = characterStateChunk.getChunks(ChunkType.MODIFIER);
				//modifierChunks.addAll(subjectChunk.getChunks(ChunkType.MODIFIER))
				
				this.createCharacterElement(parents, modifierChunks, state.getTerminalsText(), character, "", processingContextState);
				
				//Chunk modifierChunk = new Chunk(ChunkType.UNASSIGNED, modifierChunks);
				//DescriptionTreatmentElement characterElement = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
				//characterElement.setProperty(, value)
			}
			
			List<DescriptionTreatmentElement> unassignedCharacters = processingContextState.getUnassignedCharacters();
			for(DescriptionTreatmentElement unassignedCharacter : unassignedCharacters) {
				structureElement.addTreatmentElement(unassignedCharacter);
			}
			unassignedCharacters.clear();
		
			results.add(structureElement);
		}
		
		//todo extract constraints/modifiers
		
		//String[] sharedcharacters = null;
		/*for(int i = 0; i < organs.length; i++){
			String[] organDescription = organs[i].trim().split("\\s+");
			//for each organ mentioned, find organ name
			String organName = "";
			int j = 0;
			for(j = organDescription.length-1; j >=0; j--){
				if(organStateKnowlegeBase.isOrgan(organDescription[j])) { 	
					organName = organDescription[j] + " " + organName;
					organDescription[j] = "";
					break; //take the last organ name
				}
			}
			
			//create element, 
			StructureContainerTreatmentElement structureElement = new StructureContainerTreatmentElement();

			String structureId = "o" + this.structureId;
			this.structureId++;
			structureElement.setId(structureId);
			structureElement.setName(inflector.getSingular(organName));
			
			results.add(structureElement); //results only adds e
			
			//determine constraints
			while(j >= 0 && organDescription[j].trim().length()==0)
				j--;
				
			//cauline leaf abaxial surface trichmode hair long
			boolean terminate =false;
			boolean distribute = false;
			String constraint = "";//plain
			for(;j >=0; j--){
				if(terminate) 
					break;
	
				String w = organDescription[j];//.replaceAll("(\\w+\\[|\\]|\\{\\(|\\)\\}|\\(\\{|\\}\\))", "");
				//mohan code to make w keep all the tags for a preposition chunk
				if(organDescription[j].matches("\\{?r\\[p\\[.*")) {
					w = organDescription[j];
				}
				//end mohan code//
				if(w.equals(",")){
					distribute = true;
					continue;
				}
				String type = null;
				if(w.startsWith("(")) type="parent_organ";
				else type = constraintType(w, organName);
				if(type!=null){
					organDescription[j] = "";
					constraint = w+" " +constraint; //plain
				}else{
					break;
				}				
			}
			j++;
			//plain
			if(constraint.trim().length() >0){
				addAttribute(e, "constraint", constraint.replaceAll("(\\(|\\))", "").trim()); //may not have.
			}
			//plain
			
			//determine character/modifier
			ArrayList<Element> list = new ArrayList<Element>();
			list.add(e);
			//process text reminding in organ
			if(organDescription[0].trim().length()>0){//has c/m remains, may be shared by later organs
				sharedcharacters = organDescription;
			}else if(sharedcharacters !=null){//share c/m from a previous organ
				organDescription = sharedcharacters;
			}
			processCharacterText(organDescription, list, null); //characters created here are final and all the structures will have, therefore they shall stay local and not visible from outside
		}*/
		return results;
	}
	
	protected void addClauseModifierConstraint(DescriptionTreatmentElement descriptionElement, ProcessingContextState processingContextState) {
		String clauseModifierConstraint = processingContextState.getClauseModifierContraint();
		int clauseModifierConstraintId = processingContextState.getClauseModifierContraintId();
		if (clauseModifierConstraint != null)
			descriptionElement.setProperty("constraint", clauseModifierConstraint);
		if (clauseModifierConstraintId != -1)
			descriptionElement.setProperty("constraintId", String.valueOf(clauseModifierConstraintId));
	}
	
	protected LinkedList<DescriptionTreatmentElement> lastStructures(ProcessingContext processingContext, 
			ProcessingContextState processingContextState) {
		LinkedList<DescriptionTreatmentElement> parents = new LinkedList<DescriptionTreatmentElement>();
		if(!processingContext.isNewSegment() && (processingContextState.getLastElements().size()> 0 && 
				processingContextState.getLastElements().getLast().isOfDescriptionType(DescriptionType.STRUCTURE))) {
			parents.addAll(processingContextState.getLastElements());
		}else{
			parents.addAll(processingContextState.getSubjects());
		}
		return parents;
	}
	
	/*protected void setLastElements(LinkedList<DescriptionTreatmentElement> elementsToSet, ProcessingContext processingContext) {
		LinkedList<DescriptionTreatmentElement> lastElements = processingContext.getLastElements();
		lastElements.clear();
		lastElements.addAll(elementsToSet);
	}*/	
	
	protected Chunk getLastOrgan(ArrayList<Chunk> chunks) {
		for(int i=chunks.size()-1; i>=0; i--) {
			Chunk chunk = chunks.get(i);
			if(chunk.isOfChunkType(ChunkType.ORGAN)) 
				return chunk;
		}
		return null;
	}

	protected LinkedHashSet<Chunk> plusFollowsOrgan(ArrayList<Chunk> chunks, ChunkCollector chunkCollector) {
		LinkedHashSet<Chunk> beforePlus = null;
		
		ArrayList<AbstractParseTree> terminals = new ArrayList<AbstractParseTree>();
		for(Chunk chunk : chunks) {
			terminals.addAll(chunk.getTerminals());
		}
		
		for(int i=0; i < terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			if(terminal.getTerminalsText().equals("plus") && 
					chunkCollector.isPartOfChunkType(terminals.get(i-1), ChunkType.ORGAN)) {
				return beforePlus;
			}
			
			Chunk chunk = chunkCollector.getChunk(terminal);
			if(beforePlus == null)
				beforePlus = new LinkedHashSet<Chunk>();
			beforePlus.add(chunk);
		}
		
		return null;
	}

	//separate o[......... {m} {m} (o1) and {m} (o2)] to two parts: the last part include all organ names
	//e.g., o[(cypselae) -LSB-/-LSB- {minute} (crowns) -RSB-/-RSB-]
	protected ArrayList<ArrayList<Chunk>> separate(Chunk object) {
		ArrayList<ArrayList<Chunk>> twoParts  = new ArrayList<ArrayList<Chunk>>();
		ArrayList<Chunk> nonOrgan = new ArrayList<Chunk>();
		ArrayList<Chunk> organ = new ArrayList<Chunk>();
		
		boolean foundOrgan = false;
		
		List<Chunk> constraintCandidates = new LinkedList<Chunk>();
		
		List<Chunk> chunks = new ArrayList<Chunk>(object.getChunks());
		for(int i=0; i<chunks.size(); i++) {
			Chunk chunk = chunks.get(i);
			Chunk nextChunk = null;
			if(i+1<chunks.size())
				 nextChunk = chunks.get(i+1);
			Chunk previousChunk = null;
			if(i-1>=0)
				previousChunk = chunks.get(i-1);
			
			if(chunk.isOfChunkType(ChunkType.CONSTRAINT) || 
					chunk.getTerminalsText().equals("and") || chunk.getTerminalsText().equals("or") 
					|| chunk.isOfChunkType(ChunkType.COMMA)) {
				constraintCandidates.add(chunk);
			}
			//else if(objectChunk.isOfChunkType(ChunkType.ORGAN) || foundOrgan) {
			else if(chunk.containsChunkType(ChunkType.ORGAN) || foundOrgan) {
				organ.addAll(constraintCandidates);
				organ.add(chunk);
				foundOrgan = true;
			}
			else {
				nonOrgan.add(chunk);
				constraintCandidates.clear();
			}
		}
		
		if(!nonOrgan.isEmpty()) {
			Chunk lastNonOrgan = nonOrgan.get(nonOrgan.size()-1);
			if(lastNonOrgan.getTerminalsText().equals("a") ||
					lastNonOrgan.getTerminalsText().equals("an")) {
				organ.add(0, lastNonOrgan);
				nonOrgan.remove(lastNonOrgan);
			}
		}
		twoParts.add(nonOrgan);
		twoParts.add(organ);
		
		//TODO simplified, check if result is not satisfactory
		/*
		object = object.replaceFirst("^o\\[", "").replaceFirst("\\]$", "").replaceAll("<", "(").replaceAll(">", ")");
		String part2 = "";
		int objectstart = 0;
		if(object.startsWith("(") || object.indexOf("(")<0){
			part2 = object;
		}else if(object.indexOf(" (")>=0){
			objectstart = object.indexOf(" (");
			//do not separate a pair of brackets into two parts
			part2 = object.substring(objectstart); //object='1–5(–15+) (series)'
			part2 = part2.replaceFirst(" \\(", "~("); //to avoid match this bad object again in while loop
			int left = part2.replaceAll("-L[RS]B-/-L[RS]B-", "#").replaceAll("[^#]", "").length();
			int right = part2.replaceAll("-R[RS]B-/-R[RS]B-", "#").replaceAll("[^#]", "").length();;
			while(left!=right){
				objectstart += part2.indexOf(" (");
				part2 = part2.substring(part2.indexOf(" ("));
				left = part2.replaceAll("-L[RS]B-/-L[RS]B-", "#").replaceAll("[^#]", "").length();
				right = part2.replaceAll("-R[RS]B-/-R[RS]B-", "#").replaceAll("[^#]", "").length();;
			}
		//}else if(object.lastIndexOf(" ")>=0){
		//	part2 = object.substring(object.lastIndexOf(" ")).trim();
		}//else{
		//	part2 = object;
		//}
		part2 = part2.trim();
		String part1 = object.substring(0, objectstart);
		//String part1 = object.replace(part2, "").trim();
		if(part1.length()>0){
			//part 1 may still have modifiers of the first organ in part 2
			String[] ws1 = part1.split("\\s+");
			String[] ws2 = part2.split("\\s+");
			String o = "";
			for(int i =0; i<ws2.length; i++){
				if(ws2[i].indexOf("(")>=0){
					o +=ws2[i]+" ";
				}else{
					break;
				}
			}
			o = o.trim();
			for(int i = ws1.length-1; i>=0; i--){
				String escaped = ws1[i].replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
				if(constraintType(ws1[i].replaceAll("\\W", ""), o)!=null){
					part1 = part1.replaceFirst("\\s*"+escaped+"$", "");
					part2 = ws1[i]+" "+part2;
				}else{
					break;
				}
			}
			part1 = part1.replaceAll("\\s+", " ").trim();
			part2 = part2.replaceAll("\\s+", " ").trim();
		}
		twoparts[0] = part1;
		twoparts[1] = part2;*/
		return twoParts;
	}
	
	/**
	 * o[.........{m} {m} (o1) and {m} (o2)]
	 * o[each {bisexual} , architecture[{architecture-list-functionally-staminate-punct-or-pistillate}] (floret)]] ; 
	 * @param object
	 * @return
	 */
	/*protected Stack<DescriptionTreatmentElement> extractStructuresFromObject(Chunk object, ProcessingContext processingContext) {
		ChunkCollector chunkCollector = processingContext.getChunkCollector();
		
		Stack<DescriptionTreatmentElement> structures;		
		ArrayList<ArrayList<Chunk>> twoParts = separate(object);  //find the organs in object o[.........{m} {m} (o1) and {m} (o2)]
		
		structures = createStructureElements(twoParts.get(1), processingContext);// 7-12-02 add cs//to be added structures found in 2nd part, not rewrite this.latestelements yet
		if(!twoParts.get(0).isEmpty()) {
			Stack<DescriptionTreatmentElement> structuresCopy = (Stack<DescriptionTreatmentElement>) structures.clone();
			ArrayList<Chunk> tokens = twoParts.get(0);//add character elements
			
			LinkedHashSet<Chunk> beforePlus = plusFollowsOrgan(twoParts.get(1), chunkCollector);
			if(beforePlus != null) {//(teeth) plus 1-2 (bristles), the structure comes after "plus" should be excluded
				
				LinkedHashSet<Chunk> firstOrgans = beforePlus;
				Chunk lastOrgan = getLastOrgan(twoParts.get(1));
				for(int i = structures.size()-1; i>=0;  i--){
					if(!structures.get(i).getProperty("characterName").equals(inflector.getSingular(lastOrgan.getTerminalsText()))){
						structures.remove(i);
					}
				}
			}
			processCharacterText(tokens, structures, null, processingContext);// 7-12-02 add cs //process part 1, which applies to all lateststructures, invisible
			structures = structuresCopy;
		}
		return structures;
	}*/
	protected LinkedList<DescriptionTreatmentElement> extractStructuresFromObject(Chunk object, ProcessingContext processingContext, 
			ProcessingContextState processingContextState) {
		ChunkCollector chunkCollector = processingContext.getChunkCollector();
		
		LinkedList<DescriptionTreatmentElement> structures;		
		ArrayList<ArrayList<Chunk>> twoParts = separate(object);  
		//find the organs in object o[.........{m} {m} (o1) and {m} (o2)]
		
		//log(LogLevel.DEBUG, "twoParts " + twoParts);
		structures = createStructureElements(twoParts.get(1), processingContextState);
		// 7-12-02 add cs//to be added structures found in 2nd part, not rewrite this.latestelements yet
		if(!twoParts.get(0).isEmpty()) {
			LinkedList<DescriptionTreatmentElement> structuresCopy = 
					(LinkedList<DescriptionTreatmentElement>) structures.clone();
			//ArrayList<Chunk> tokens = twoParts.get(0);//add character elements
			
			LinkedHashSet<Chunk> beforePlus = plusFollowsOrgan(twoParts.get(1), chunkCollector);
			if(beforePlus != null) {
				//(teeth) plus 1-2 (bristles), the structure comes after "plus" should be excluded
				LinkedHashSet<Chunk> firstOrgans = beforePlus;
				Chunk lastOrgan = getLastOrgan(twoParts.get(1));
				for(int i = structures.size()-1; i>=0;  i--){
					//log(LogLevel.DEBUG, structures.get(i));
					//log(LogLevel.DEBUG, lastOrgan);
					if(!structures.get(i).getProperty("name").equals(inflector.getSingular(lastOrgan.getTerminalsText()))){
						structures.remove(i);
					}
				}
			}
			
			//String[] expectedFormatTokens = convertChunksToTokens(twoParts.get(0));
			processCharacterText(twoParts.get(0), structures, null, processingContextState, processingContext);
			// 7-12-02 add cs //process part 1, which applies to all lateststructures, invisible
			structures = structuresCopy;
		}
		return structures;
	}
	
	/*protected String[] convertChunksToTokens(ArrayList<Chunk> chunks) {
		String[] result = new String[chunks.size()];
		for(int i=0; i<chunks.size(); i++)
			result[i] = chunks.get(i).getTerminalsText();
		return result;
	}*/
	
	
	/**
	 * 
	 * @param chunked: "m[sometimes 1-2+] pinnated "
	 * @param parents
	 * @param character
	 * @return
	 */
	/*private Stack<DescriptionTreatmentElement> processCharacterText(String chunked, 
			Stack<DescriptionTreatmentElement> parents, String character, ProcessingContext processingContext) {
		Stack<DescriptionTreatmentElement> results = new Stack<DescriptionTreatmentElement>();
		String modifiers;
		String word;
		if(chunked.indexOf("m[")==0){
			String[] parts = chunked.split("\\]");
			modifiers = parts[0].replaceFirst("^m\\[", "").trim();
			word = parts[1].trim();
		}else{
			modifiers = "";
			word = chunked;
		}
		createCharacterElement(parents, results,
				modifiers, word, character, "", processingContext);// 7-12-02 add cs
		return results;
	}*/
	


	/**
	 * bases and tips mostly rounded 
	 * @param tokens
	 * @param parents
	 */
	protected LinkedList<DescriptionTreatmentElement> processCharacterText(List<Chunk> tokens, LinkedList<DescriptionTreatmentElement> parents, 
			String character, ProcessingContextState processingContextState, ProcessingContext processingContext) {
		LinkedList<DescriptionTreatmentElement> results = new LinkedList<DescriptionTreatmentElement>();
		//determine characters and modifiers
		List<Chunk> modifiers = new ArrayList<Chunk>();
		for(Chunk token : tokens) {
			if(token.isOfChunkType(ChunkType.TO_PHRASE)) {
				processingContextState.setLastElements(parents);
				IChunkProcessor processor = processingContext.getChunkProcessor(ChunkType.TO_PHRASE);
				List<DescriptionTreatmentElement> result = processor.process(token, processingContext);
				results.addAll(result);
				//results = this.processCharacterList(token, parents, processingContextState, processingContext);
			} else {
				List<Chunk> chunkModifiers = token.getChunks(ChunkType.MODIFIER);
				modifiers.addAll(chunkModifiers);
				
				String w = token.getTerminalsText();
				if(token.containsChunkType(ChunkType.STATE))
					w = token.getChunkBFS(ChunkType.STATE).getTerminalsText();
				String tokensCharacter = null;
				if(token.isOfChunkType(ChunkType.CHARACTER_STATE)) {
					tokensCharacter = token.getProperty("characterName");
				}
				tokensCharacter = characterKnowledgeBase.getCharacter(w);
				if(tokensCharacter==null && w.matches("no")){
					tokensCharacter = "presence";
				}
				if(tokensCharacter==null && posKnowledgeBase.isAdverb(w)) {
					//TODO: can be made more efficient, since sometimes character is already given
					modifiers.add(token);
				}else if(w.matches(".*?\\d.*") && !w.matches(".*?[a-z].*")){//TODO: 2 times =>2-times?
					results = this.annotateNumericals(w, "count", modifiers, parents, false, processingContextState);
					//annotateCount(parents, w, modifiers);
					modifiers.clear();
				}else{
					//String chara = MyPOSTagger.characterhash.get(w);
					if(tokensCharacter != null){
						if(character!=null){
							tokensCharacter = character;
						}
						if(tokensCharacter.equals("characterName") && modifiers.size() ==0) {
							//high relief: character=relief, reset the character of "high" to "relief"
							DescriptionTreatmentElement lastelement = null;
							if(results.size() >= 1){
								lastelement = results.getLast();
							}else if(processingContextState.getLastElements().size() >= 1){
								lastelement = processingContextState.getLastElements().getLast();
							}
							if(lastelement != null && lastelement.isOfDescriptionType(DescriptionType.CHARACTER)){
								lastelement.setProperty("name", w);
							}
						}else{
							String[] characterValues = w.split("\\bor\\b|\\band\\b");
							for(String characterValue : characterValues) 
								results.add(createCharacterElement(parents, modifiers, characterValue.trim(), tokensCharacter, "", processingContextState)); 
							//default type "" = individual vaues
							modifiers.clear();
						}
					}
				}
			}
		}
		return results;
	}
	
	
	/**
	 * bases and tips mostly rounded 
	 * @param tokens
	 * @param parents
	 */
	/*protected Stack<DescriptionTreatmentElement> processCharacterText(ArrayList<Chunk> tokens, Stack<DescriptionTreatmentElement> parents, 
			String character, ProcessingContext processingContext){
		Stack<DescriptionTreatmentElement> results = new Stack<DescriptionTreatmentElement>();
		//determine characters and modifiers
		String modifiers = "";
		for(int j = 0; j <tokens.size(); j++){
			if(tokens.get(j).getTerminalsText().trim().length()>0){ //nested: "{inner} -LRB-/-LRB- functionally {staminate} -LSB-/-LSB- {bisexual} -RSB-/-RSB- -RRB-/-RRB-" 
				if(tokens.get(j).getTerminalsText().matches("-L[RS]B-/-L[SR]B-")){
					//collect everything in brackets
					int lr = 0;
					int ls = 0;
					if(tokens.get(j).equals("-LRB-/-LRB-"))	lr++;
					if(tokens.get(j).equals("-LSB-/-LSB-"))	ls++;
					String bracketed = tokens.get(j) + " ";
					while(j+1 < tokens.size() && (lr != 0 || ls !=0)){
						j++;
						if(tokens.get(j).equals("-LRB-/-LRB-"))	lr++;
						if(tokens.get(j).equals("-LSB-/-LSB-"))	ls++;
						if(tokens.get(j).equals("-RRB-/-RRB-"))	lr--;
						if(tokens.get(j).equals("-RSB-/-RSB-"))	ls--;
						bracketed += tokens.get(j) + " ";
					}
					//bracketed += tokens[j];
					createCharacterElement(parents, results, "", bracketed.trim(), "", "", processingContext);
				}else{
					if(tokens.get(j).getTerminalsText().indexOf("~list~")>=0){
						results = this.processCharacterList(tokens.get(j), parents, processingContext);// 7-12-02 add cs
					}else{
						Chunk w = tokens.get(j);
						String wText = w.getTerminalsText();
						String chara= null;
						chara = this.characterKnowledgeBase.getCharacter(wText);
						if(chara==null && wText.matches("no")){
							chara = "presence";
						}
						if(chara==null && posKnowledgeBase.isAdverb(wText)) {
							//TODO: can be made more efficient, since sometimes character is already given
							modifiers +=wText+" ";
						}else if(wText.matches(".*?\\d.*") && !wText.matches(".*?[a-z].*")){//TODO: 2 times =>2-times?
							results = this.annotateNumericals(w, "count", modifiers, parents, false, processingContext);
							//annotateCount(parents, w, modifiers);
							modifiers = "";
						}else{
							//String chara = MyPOSTagger.characterhash.get(w);
							if(chara != null){
								if(character!=null){
									chara = character;
								}
								if(chara.compareToIgnoreCase("characterName")==0 && modifiers.length() ==0){
									//high relief: character=relief, reset the character of "high" to "relief"
									DescriptionTreatmentElement lastelement = null;
									if(results.size()>=1){
										lastelement = results.get(results.size()-1);
									}else if(processingContext.getLastElements().size()>=1){
										lastelement = processingContext.getLastElements().lastElement();
									}
									if(lastelement != null && lastelement.getDescriptionType().equals(DescriptionType.CHARACTER)) {
										lastelement.setProperty("characterName", wText);
									}
								}else{
									createCharacterElement(parents, results, modifiers, wText, chara, "", processingContext);
									modifiers = "";
								}
							}
						}
					}
					
				}
			}
		}
		return results;
	}*/
	
	
	/**
	 * TODO: {shape-list-usually-flat-to-convex-punct-sometimes-conic-or-columnar}
	 *       {pubescence-list-sometimes-bristly-or-hairy}
	 * @param content: pubescence[m[not] {pubescence-list-sometimes-bristly-or-hairy}]
	 * @param parents
	 * @return
	 */
	protected LinkedList<DescriptionTreatmentElement> processCharacterList(Chunk content,
			LinkedList<DescriptionTreatmentElement> parents, ProcessingContextState processingContextState, ProcessingContext processingContext) {
		LinkedList<DescriptionTreatmentElement> results= new LinkedList<DescriptionTreatmentElement>();
			
		List<Chunk> modifiers = content.getChunks(ChunkType.MODIFIER);
		modifiers.addAll(processingContextState.getUnassignedModifiers());
		processingContextState.clearUnassignedModifiers();
		Chunk characterChunk = content.getChunkDFS(ChunkType.STATE);
		/*List<Chunk> nonModifiers = new ArrayList<Chunk>();
		for(Chunk chunk : content.getChunks())
			if(!chunk.isOfChunkType(ChunkType.MODIFIER))
				nonModifiers.add(chunk);
		*/
		/*if(content.indexOf("m[")>=0){
			modifier=content.substring(content.indexOf("m["), content.indexOf("{"));
			content = content.replace(modifier, "");
			modifier = modifier.trim().replaceAll("(m\\[|\\])", "");
		}
		content = content.replace(modifier, "");
		String[] parts = content.split("\\[");*/
		//String[] parts = content.split("\\[?\\{");

		//if(nonModifiers.size() < 2) 
		//	return results;
		/*if(parts.length<2){
			return results; //@TODO: parsing failure
		}*/
		
		String characterName = content.getProperty("characterName");
		log(LogLevel.DEBUG, "characterName " + characterName);
		//String characterName = parts[0];
		if(processingContextState.getUnassignedCharacter() != null){
			characterName = processingContextState.getUnassignedCharacter();
			processingContextState.setUnassignedCharacter(null);
		}
		//String cvalue = parts[1].replaceFirst("\\{"+cname+"~list~", "").replaceFirst("\\W+$", "").replaceAll("~", " ").trim();
		String characterValue = characterChunk.getTerminalsText();
		characterValue = characterValue.replaceFirst("\\{"+characterName+"-list", "").replaceFirst("\\W+$", "").replaceAll("-", " ").replaceAll("_c_", " ").trim();
		//String characterValue = parts[1].replaceFirst("\\{"+characterName+"-list", "").replaceFirst("\\W+$", "").replaceAll("-", " ").trim();
		//String cvalue = parts[1].replaceFirst("^"+cname+"~list~", "").replaceFirst("\\W+$", "").replaceAll("~", " ").trim();
		if(characterName.endsWith("ttt")){
			this.createCharacterElement(parents, modifiers, characterValue, characterName.replaceFirst("ttt$", ""), "", processingContextState);
			return results;
		}
		if(characterValue.contains(" to ")){
			createRangeCharacterElement(parents, results, modifiers, characterValue.replaceAll("punct", ","), characterName, processingContextState); 
			//add a general statement: coloration="red to brown"
		}
		
		//TODO has to be rebuild for chunks
		/*String mAll = "";
		boolean findm = false;
		//gather modifiers from the end of cvalues[i]. this modifier applies to all states
		do{
			findm = false;
			String last = characterValue.substring(characterValue.lastIndexOf(' ')+1);
			if(characterKnowledgeBase.getCharacter(last) == null && posKnowledgeBase.isAdverb(last)) {
				mAll +=last+ " ";
				characterValue = characterValue.replaceFirst(last+"$", "").trim();
				findm = true;
			}
		} while(findm);
		*/
		//String[] cvalues = characterValue.split("\\b(to|or|punct)\\b");//add individual values
		for(AbstractParseTree terminal : characterChunk.getTerminals()) {
			if(!terminal.getTerminalsText().matches("to|or|punct")) {
				//String state = cvalues[i].trim();//usually papillate to hirsute distally
				List<Chunk> tokens = new ArrayList<Chunk>();
				tokens.addAll(modifiers);
				tokens.add(terminal);
				//gather modifiers from the beginning of cvalues[i]. a modifier takes effect for all state until a new modifier is found
				/*Chunk m; // = "";
				do {
					findm = false;
					if(state.length()==0){
						break;
					}
					int end = state.indexOf(' ')== -1? state.length():state.indexOf(' ');
					String w = state.substring(0, end);
					if(characterKnowledgeBase.getCharacter(w) == null && posKnowledgeBase.isAdverb(w)) {
						m += w+ " ";
						//w = w.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}").replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\+", "\\\\+");
						state = state.replaceFirst(w, "").trim();
						findm = true;
					}
				}while (findm);
				*/
				//List<Chunk> tokens = new ArrayList<Chunk>();
				/*String modifiersString = "";
				for(Chunk modifier : modifiers) 
					modifiersString += modifier.getTerminalsText() + " ";
				*/
				/*if(m.length() == 0){
					state = (modifiersString + " "+mAll+" "+state.replaceAll("\\s+", "#")).trim(); //prefix the previous modifier 
				}else{
					modifiersString = modifiersString.matches(".*?\\bnot\\b.*")? modifiersString +" "+m : m; //update modifier
					//cvalues[i] = (mall+" "+cvalues[i]).trim();
					state = (modifiersString+" "+mAll+" "+state.replaceAll("\\s+", "#")).trim(); //prefix the previous modifier 
				}*/
				
				//String[] tokens = state.split("\\s+");
				//tokens[tokens.length-1] = tokens[tokens.length-1].replaceAll("#", " ");
				results.addAll(this.processCharacterText(tokens, parents, characterName, processingContextState, processingContext));
				//results.addAll(this.processCharacterText(new String[]{state}, parents, cname));
				
			}
		}

		return results;
	}

	
	/**
	 * TODO: {shape-list-usually-flat-to-convex-punct-sometimes-conic-or-columnar}
	 *       {pubescence-list-sometimes-bristly-or-hairy}
	 * @param content: pubescence[m[not] {pubescence-list-sometimes-bristly-or-hairy}]
	 * @param parents
	 * @return
	 *//*
	private Stack<DescriptionTreatmentElement> processCharacterList(Chunk content,
			Stack<DescriptionTreatmentElement> parents, ProcessingContext processingContext)  {
		Stack<DescriptionTreatmentElement> results = new Stack<DescriptionTreatmentElement>();
		String modifiersString = "";
		
		if(content.containsChunkType(ChunkType.MODIFIER)) {
			List<Chunk> modifiers = content.getChunks(ChunkType.MODIFIER);
			for(Chunk modifier : modifiers) {
				modifiersString += modifier.getTerminalsText() + ", ";
			}
			modifiersString = modifiersString.substring(0, modifiersString.length() - 3);
		}
		
		LinkedHashSet<Chunk> childChunks = content.getChunks();
		//String[] parts = content.split("\\[?\\{");
		if(childChunks.size()<2){
			return results; //@TODO: parsing failure
		}
		
		String characterName;
		String characterValue;
		int j=0;
		for(Chunk childChunk : childChunks) {
			if(j==0)
				characterName = childChunk.getTerminalsText();
			if(j==1)
				characterValue = childChunk.getTerminalsText();
		}
		
		if(processingContext.getUnassignedCharacter() != null){
			characterName = processingContext.getUnassignedCharacter();
			processingContext.setUnassignedCharacter(null);
		}
		characterValue = characterValue.replaceFirst("\\{"+characterName+"~list~", "").replaceFirst("\\W+$", "").replaceAll("~", " ").trim();
		//String cvalue = parts[1].replaceFirst("\\{"+cname+"-list", "").replaceFirst("\\W+$", "").replaceAll("-", " ").trim();
		//String cvalue = parts[1].replaceFirst("^"+cname+"~list~", "").replaceFirst("\\W+$", "").replaceAll("~", " ").trim();
		if(characterName.endsWith("ttt")){
			this.createCharacterElement(parents, results, modifiersString, characterValue, 
					characterName.replaceFirst("ttt$", ""), "", processingContext);
			return results;
		}
		if(characterValue.indexOf(" to ")>=0){
			createRangeCharacterElement(parents, results, modifiersString, 
					characterValue.replaceAll("punct", ",").replaceAll("(\\{|\\})", ""), characterName, processingContext); 
			//add a general statement: coloration="red to brown" // 7-12-02 add cs
		}
		String mall = "";
		boolean findm = false;
		//gather modifiers from the end of cvalues[i]. this modifier applies to all states
		do{
			findm = false;
			String last = characterValue.substring(characterValue.lastIndexOf(' ')+1);
			if(characterKnowledgeBase.getCharacter(last) == null && posKnowledgeBase.isAdverb(last)) {
				mall +=last+ " ";
				characterValue = characterValue.replaceFirst(last+"$", "").trim();
				findm = true;
			}
		}while(findm);
		
		String[] characterValues = characterValue.split("\\b(to|or|punct)\\b");//add individual values
		for(int i = 0; i<characterValues.length; i++){
			String state = characterValues[i].trim();//usually papillate to hirsute distally
			if(state.length() == 0) continue;
			//gather modifiers from the beginning of cvalues[i]. a modifier takes effect for all state until a new modifier is found
			String m = "";
			do{
				findm = false;
				if(state.length()==0){
					break;
				}
				int end = state.indexOf(' ')== -1? state.length():state.indexOf(' ');
				String w = state.substring(0, end);
				if(characterKnowledgeBase.getCharacter(w) == null && posKnowledgeBase.isAdverb(w)) {
					m +=w+ " ";
					w = w.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}").replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\+", "\\\\+");
					state = state.replaceFirst(w, "").trim();
					findm = true;
				}
			}while (findm);
			
			if(m.length()==0){
				modifiersString = (modifiersString+" "+mall).trim();
				state = state.replaceAll("\\s+", " ").trim(); 
			}else{
				modifiersString = modifiersString.matches(".*?\\bnot\\b.*")? modifiersString +" "+m : m; //update modifier
				modifiersString = (modifiersString+" "+mall).trim();
				state = state.replaceAll("\\s+", " ").trim(); 
			}
			results.addAll(this.processCharacterText("m["+modifiersString+"] "+state, parents, characterName, processingContext));
		}

		return results;
	}*/
	

	/**
	 * crowded to open
	 * for categorical range-value
	 * @param parents
	 * @param results
	 * @param modifier
	 * @param cvalue
	 * @param cname
	 */
	protected String createRangeCharacterElement(LinkedList<DescriptionTreatmentElement> parents,
			LinkedList<DescriptionTreatmentElement> results, List<Chunk> modifiers, String characterValue,
			String characterName, ProcessingContextState processingContextState) {
		String modifiersString = "";
		for(Chunk modifier : modifiers)
			modifiersString += modifier.getTerminalsText() + " ";
		
		DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
		//if(this.inbrackets){character.setAttribute("in_bracket", "true");}
		character.setProperty("type", "range_value");
		character.setProperty("name", characterName);
		
		String[] range = characterValue.split("\\s+to\\s+");//a or b, c, to d, c, e
		String[] tokens = range[0].replaceFirst("\\W$", "").replaceFirst("^.*?\\s+or\\s+", "").split("\\s*,\\s*"); //a or b, c, =>
		String from = getFirstCharacter(tokens[tokens.length-1]);
		tokens = range[1].split("\\s*,\\s*");
		String to = getFirstCharacter(tokens[0]);
		character.setProperty("from", from.replaceAll("-c-", " ")); //a or b to c => b to c
		character.setProperty("to", to.replaceAll("-c-", " "));
		
		boolean usedModifiers = false;
		for(DescriptionTreatmentElement parent : parents) {
			if(modifiersString.trim().length() > 0){
				character.setProperty("modifier", modifiersString);
				usedModifiers = true;
			}
			results.add(character); //add to results
			parent.addTreatmentElement(character);
		}
		if(usedModifiers){
			modifiersString = "";
		}
		addClauseModifierConstraint(character, processingContextState);
		return modifiersString;
	}
	
	/**
	 * @param tokens: usually large
	 * @return: large
	 */
	protected String getFirstCharacter(String character) {
		String[] tokens = character.trim().split("\\s+");
		String result = "";
		for(int i = 0; i < tokens.length; i++){
			if(characterKnowledgeBase.getCharacter(tokens[i]) != null){
				 result += tokens[i]+" ";
			}
		}
		return result.trim();
	}
	
	protected DescriptionTreatmentElement createCharacterElement(LinkedList<DescriptionTreatmentElement> parents, List<Chunk> modifiers, 
			String characterValue, String characterName, String char_type, ProcessingContextState processingContextState) {
		log(LogLevel.DEBUG, "create character element " + characterName + ": " +  characterValue + " for parent "  + parents);
		String modifierString = "";
		if(modifiers != null)
			for(Chunk modifier : modifiers)
				modifierString += modifier.getTerminalsText() + " ";
		modifierString = modifierString.trim();
			
		String parenthetical = null;
		DescriptionTreatmentElement character = null;
		if(characterValue.indexOf("( ")>=0){
			//contains parenthetical, textual expressions:  lanceolate ( outer ) as part of a character list; brackets in numerical expressions do not have a trailing space
			parenthetical = characterValue.substring(characterValue.indexOf("( ")).trim();
			characterValue = characterValue.substring(0, characterValue.indexOf("( ")).trim();
		}
		if(characterValue.matches("^-[RL][SR]B-/-[RL][SR]B-.*")){ //other textual, parenthetical expressions has -[RL][SR]B-/-[RL][SR]B- as a separate token
			parenthetical = characterValue;
			characterValue = "";
		}
		
		if(characterValue.length() > 0){
			character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
			//if(this.inbrackets){character.setAttribute("in_bracket", "true");}
			if(characterName.compareTo("count")==0 && characterValue.indexOf("-")>=0 && characterValue.indexOf("-")==characterValue.lastIndexOf("-")){
				String[] values = characterValue.split("-");
				character.setProperty("type", "range_value");
				character.setProperty("name", characterName);
				character.setProperty("from", values[0]);
				if(values[1].endsWith("+")) {
					character.setProperty("to", values[1].substring(0, values[1].length()-1));
					character.setProperty("upperRestricted", "false");
				} else
					character.setProperty("to", values[1]);
				
			}else{
				if (characterName.compareTo("size") == 0) {
					String value = characterValue.replaceFirst("\\b(" + units + ")\\b", "").trim(); // 5-10 mm
					String unit = characterValue.replace(value, "").trim();
					if (unit.length() > 0) {
						character.setProperty("unit", unit);
					}
					characterValue = value;
				}else if(characterValue.indexOf("_c_")>=0 && (characterName.equals("color") || characterName.equals("coloration"))){
					//-c- set in SentenceOrganStateMarkup
					String color = characterValue.substring(characterValue.lastIndexOf("_c_")+3); //pale-blue
					String m = characterValue.substring(0, characterValue.lastIndexOf("_c_")); //color = blue m=pale
					
					modifierString = modifierString.length()>0 ? modifierString + ";"+ m : m;
					characterValue = color;
				}
				if(char_type.length() > 0){
					character.setProperty("type", char_type);
				}
				character.setProperty("name", characterName);
				character.setProperty("value", characterValue);
			}
			boolean usedModifiers = false;
			for(DescriptionTreatmentElement parent : parents) {
				if(modifierString.trim().length() >0) {
					character.setProperty("modifier", modifierString);
					usedModifiers = true;
				}
				parent.addTreatmentElement(character);
			}
			if(usedModifiers){
				modifierString = "";
			}
			
			addClauseModifierConstraint(character, processingContextState);
		}
		return character;
	}

	
	protected LinkedList<DescriptionTreatmentElement> linkObjects(LinkedList<DescriptionTreatmentElement> subjectStructures, List<Chunk> modifiers, 
			Chunk preposition, Chunk object, boolean lastIsStruct, boolean lastIsChara, 
			ProcessingContext processingContext, ProcessingContextState processingContextState) {
		LinkedList<DescriptionTreatmentElement> result = new LinkedList<DescriptionTreatmentElement>();
		LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
		ChunkCollector chunkCollector = processingContext.getChunkCollector();
		
		List<Chunk> unassignedModifiers = processingContext.getCurrentState().getUnassignedModifiers();
		modifiers.addAll(unassignedModifiers);
		unassignedModifiers.clear();
		
		LinkedList<DescriptionTreatmentElement> structures;
		structures = extractStructuresFromObject(object, processingContext, processingContextState);
		result.addAll(structures);
		String base = "";
		
		if(baseCountWords.contains(object.getTerminalsText())) {
			base = "each";
		}
		if(lastIsChara && !lastElements.isEmpty()) {
			DescriptionTreatmentElement lastElement = lastElements.getLast();
			//if last character is size, change to location: <margins> r[p[with] o[3–6 (spines)]] 1–3 {mm} r[p[{near}] o[(bases)]]. 
			//1-3 mm is not a size, but a location of spines
			if(lastElement.getProperty("name").equals("size") && 
					((lastElement.getProperty("value") != null && lastElement.getProperty("value").matches(".*?\\d.*")) || 
							(lastElement.getProperty("from") != null && lastElement.getProperty("from").matches(".*?\\d.*"))) 
				&& locationPrepositions.contains(preposition.getTerminalsText())) {
				lastElement.setProperty("name", "location");
			}
			lastElement.setProperty("constraint", preposition.getTerminalsText() + " " + listStructureNames(object));
			lastElement.setProperty("constraintId", listStructureIds(structures));
			if(!modifiers.isEmpty()) {
				for(Chunk modifier : modifiers) {
					lastElement.appendProperty("modifier", modifier.getTerminalsText());
				}
			}
		} else {			
			String relation = relationLabel(preposition, subjectStructures, structures, object, chunkCollector);//determine the relation
			if(relation != null){
				result.addAll(createRelationElements(relation, subjectStructures, structures, modifiers, false, processingContextState));//relation elements not visible to outside //// 7-12-02 add cs
			}
			if(relation!= null && relation.compareTo("part_of")==0) 
				structures = subjectStructures; //part_of holds: make the organbeforeof/entity1 the return value, all subsequent characters should be refering to organbeforeOf/entity1
		}
		
		processingContextState.setLastElements(structures);
		return result;
	}
	
	protected String relationLabel(Chunk preposition, 
			LinkedList<DescriptionTreatmentElement> organsbeforepp, 
			LinkedList<DescriptionTreatmentElement> organsafterpp, Chunk object, ChunkCollector chunkCollector) {		
		if(preposition.getTerminalsText().equals("of")) {			
			
			List<Chunk> chunks = chunkCollector.getChunks();
			
			Chunk afterPPChunk = null;
			for(int i=0; i<chunks.size(); i++) { 
				Chunk chunk = chunks.get(i);
				if(chunk.contains(object) && i+1 < chunks.size()) {
					afterPPChunk = chunks.get(i+1);
					break;
				}
			}
			if(afterPPChunk!=null && (afterPPChunk.isOfChunkType(ChunkType.END_OF_LINE) || afterPPChunk.isOfChunkType(ChunkType.END_OF_SUBCLAUSE) ||
					afterPPChunk.isOfChunkType(ChunkType.COUNT) || 
					(afterPPChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && afterPPChunk.getProperty("characterName").equals("count"))))
				return "consist_of";
			
			for(Chunk chunk : object.getChunks()) {
				if(chunk.isOfChunkType(ChunkType.COUNT) || (chunk.isOfChunkType(ChunkType.CHARACTER_STATE) && chunk.getProperty("characterName").equals("count"))) { 
					return "consist_of";
				}
			}
			return differentiateOf(organsbeforepp, organsafterpp);
		}
		return preposition.getTerminalsText();
	}
	
	protected String differentiateOf(LinkedList<DescriptionTreatmentElement> organsBeforeOf,
			LinkedList<DescriptionTreatmentElement> organsAfterOf) {
		String result = "part_of";
		
		for (int i = 0; i<organsBeforeOf.size(); i++){
			String b = organsBeforeOf.get(i).getProperty("name");
			if(clusters.contains(b)){
				result = "consist_of";
				break;
			}
			for(int j = 0; j<organsAfterOf.size(); j++){
				String a = organsAfterOf.get(j).getProperty("name");
				//String pattern = a+"[ ]+of[ ]+[0-9]+.*"+b+"[,\\.]"; //consists-of
				if(a.length()>0 && b.length()>0){
					String pb = inflector.getPlural(b);
					String pa = inflector.getPlural(a);
					String pattern = "("+b+"|"+pb+")"+"[ ]+of[ ]+[0-9]+.*"+"("+a+"|"+pa+")"+"[ ]?(,|;|\\.|and|or|plus)"; //consists-of
					
					/*for(HashMap<String, String> sentencesPerTreatment : terminologyLearner.getSentences().values()) {
						for(Entry<String, String> sentenceEntry : sentencesPerTreatment.entrySet()) {
							if(sentenceEntry.getValue().matches(pattern)) {
								result = "consists_of";
								break;
							}
						}
					}*/
					for(String sentence : terminologyLearner.getSentences()) {
						if(sentence.matches(pattern)) {
							result = "consists_of";
							break;
						}
					}
				}
			}
		}

		return result;
	}
	
	protected LinkedList<DescriptionTreatmentElement> createRelationElements(String relation, 
			List<DescriptionTreatmentElement> fromStructures, List<DescriptionTreatmentElement> toStructures, List<Chunk> modifiers, boolean symmetric, 
			ProcessingContextState processingContextState) {
		log(LogLevel.DEBUG, "create relation " + relation + " between " + fromStructures + " to " + toStructures);
		//add relation elements
		LinkedList<DescriptionTreatmentElement> relationElements = new LinkedList<DescriptionTreatmentElement>();
		for(int i = 0; i < fromStructures.size(); i++) {
			String o1id = fromStructures.get(i).getProperty("id");
			for(int j = 0; j<toStructures.size(); j++){
				String o2id = toStructures.get(j).getProperty("id");
				boolean negation=false;
				
				Iterator<Chunk> modifiersIterator = modifiers.iterator();
				while(modifiersIterator.hasNext()) {
					Chunk modifier = modifiersIterator.next();
					if(modifier.getTerminalsText().equals("not")) {
						negation = true;
						modifiersIterator.remove();
					}
				}
				if(relation.matches(".*?\\bnot\\b.*")){
					negation = true;
					relation = relation.replace("not", "").trim();
				}
				relationElements.add(addRelation(relation, modifiers, symmetric, o1id, o2id, negation, processingContextState));
			}
		}
		return relationElements;
	}
	
	protected DescriptionTreatmentElement addRelation(String relation, List<Chunk> modifiers,
			boolean symmetric, String o1id, String o2id, boolean negation, ProcessingContextState processingContextState) {
		DescriptionTreatmentElement relationElement = new DescriptionTreatmentElement(DescriptionType.RELATION);
		relationElement.setProperty("name", relation);
		relationElement.setProperty("from", o1id);
		relationElement.setProperty("to", o2id);
		relationElement.setProperty("negation", String.valueOf(negation));
		relationElement.setProperty("id", "r" + String.valueOf(processingContextState.fetchAndIncrementRelationId(relationElement)));	
		if(modifiers.size() > 0){
			String modifierString = "";
			for(Chunk modifier : modifiers) {
				modifierString += modifier.getTerminalsText() + ", ";
			}
			
			relationElement.appendProperty("modifier", modifierString.substring(0, modifierString.length() - 2));
		}
		addClauseModifierConstraint(relationElement, processingContextState);
		return relationElement;
	}
	
	protected boolean isNumerical(Chunk object) {
		return object.getTerminalsText().matches("\\d+");
	}
	
	
	protected LinkedList<DescriptionTreatmentElement> annotateNumericals(String text, String character, String modifierString, 
			LinkedList<DescriptionTreatmentElement> parents, boolean resetFrom, ProcessingContextState processingContextState) {
		LinkedList<DescriptionTreatmentElement> result = new LinkedList<DescriptionTreatmentElement>();
			
		LinkedList<DescriptionTreatmentElement> characters = parseNumericals(text, character);
		if(characters.size()==0){//failed, simplify chunktext
			characters = parseNumericals(text, character);
		}
		
		for(DescriptionTreatmentElement element : characters) {
			if(resetFrom && element.getProperty("from") != null && element.getProperty("from").equals("0") && 
					(element.getProperty("fromInclusive")==null || element.getProperty("fromInclusive").equals("true"))){// to 6[-9] m.
				element.removeProperty("from");
				if(element.getProperty("fromUnit")!=null){
					element.removeProperty("fromUnit");
				}
			}
			if(modifierString !=null && modifierString.compareTo("")!=0) {
				element.setProperty("modifier", modifierString);
			}

			addClauseModifierConstraint(element, processingContextState);
			for(DescriptionTreatmentElement parent : parents) {
				parent.addTreatmentElement(element);
			}
			result.add(element);
		}
		return result;
	}
	
	
	protected LinkedList<DescriptionTreatmentElement> annotateNumericals(String text, String character, List<Chunk> modifiers, 
			LinkedList<DescriptionTreatmentElement> parents, boolean resetFrom, ProcessingContextState processingContextState) {
		String modifierString = "";
		for(Chunk modifier : modifiers)
			modifierString += modifier.getTerminalsText() + " ";
		return annotateNumericals(text, character, modifierString.trim(), parents, resetFrom, processingContextState);
	}


	
	/**
	 * 
	 * @param numberexp : styles 2[10] mm diam.
	 * @param cname: 
	 * @return: characters marked up in XML format <character name="" value="">
	 */
	protected LinkedList<DescriptionTreatmentElement> parseNumericals(String numberexp, String cname){	
		LinkedList<DescriptionTreatmentElement> innertagstate = new LinkedList<DescriptionTreatmentElement>();
		int i,j;
		numberexp = numberexp.replaceAll("\\([\\s]?|\\[[\\s]?", "[");
		numberexp = numberexp.replaceAll("[\\s]?\\)|[\\s]?\\]", "]").trim();
		
		//4-5[+] => 4-5[-5+]
		Pattern p1 = Pattern.compile("(.*?\\b(\\d+))\\s*\\[\\+\\](.*)");
		Matcher m = p1.matcher(numberexp);
		if(m.matches()){
			numberexp = m.group(1)+"[-"+m.group(2)+"+]"+m.group(3);
			m = p1.matcher(numberexp);
		}
		//1-[2-5] => 1-1[2-5] => 1[2-5]
		//1-[4-5] => 1-3[4-5] 
		p1 = Pattern.compile("(.*?)(\\d+)-(\\[(\\d)-.*)");
		m = p1.matcher(numberexp);
		if(m.matches()){
			int n = Integer.parseInt(m.group(4))-1;
			if(n==Integer.parseInt(m.group(2))){
				numberexp = m.group(1)+n+m.group(3);
			}else{
				numberexp = m.group(1)+m.group(2)+"-"+n+m.group(3);
			}
		}
		
		///////////////////////////////////////////////////////////////////
		//      area                                               ////////
		
		Pattern pattern19 = Pattern.compile("([ \\d\\.\\[\\]+-]+\\s*([cmdµu]?m?))\\s*[×x]?(\\s*[ \\d\\.\\[\\]+-]+\\s*([cmdµu]?m?))?\\s*[×x]\\s*([ \\d\\.\\[\\]+-]+\\s*([cmdµu]?m))");
		Matcher matcher2 = pattern19.matcher(numberexp);
		if(matcher2.matches()){
			//get l, w, and h
			String width = "";
			String height = "";
			String lunit = "";
			String wunit = "";
			String hunit = "";
			String length = matcher2.group(1).trim();
			String g5 = matcher2.group(5).trim();
			if(matcher2.group(3)==null){
				width = g5;
			}else{
				width = matcher2.group(3);
				height = g5;
			}
			//make sure each has a unit
			if(height.length()==0){//2 dimensions
				wunit = matcher2.group(6);
				if(matcher2.group(2)==null || matcher2.group(2).trim().length()==0){
					lunit = wunit;
				}else{
					lunit = matcher2.group(2);
				}
			}else{//3 dimensions
				hunit = matcher2.group(6);
				if(matcher2.group(4)==null || matcher2.group(4).trim().length()==0){
					wunit = hunit;
				}else{
					wunit = matcher2.group(4);
				}
				if(matcher2.group(2)==null || matcher2.group(2).trim().length()==0){
					lunit = wunit;
				}else{
					lunit = matcher2.group(2);
				}
			}
			//format expression value+unit
			length = length.matches(".*[cmdµ]?m$")? length : length + " "+lunit;
			width = width.matches(".*[cmdµ]?m$")? width : width + " "+wunit;
			if(height.length()>0) height = height.matches(".*[cmdµ]?m$")? height : height + " "+hunit;
			
			//annotation
			annotateSize(length, innertagstate, "length");
			annotateSize(width, innertagstate, "width");
			if(height.length()>0) annotateSize(height, innertagstate, "height");
			
			numberexp = matcher2.replaceAll("#");
        	matcher2.reset();
		}
    	
    	////////////////////////////////////////////////////////////////////////////////////
    	//   ratio                                                              ////////////
    	Pattern pattern24 = Pattern.compile("l/w[\\s]?=[\\d\\.\\s\\+\\–\\-]+");
    	matcher2 = pattern24.matcher(numberexp);
    	while ( matcher2.find()){
    		if(numberexp.charAt(matcher2.start())==' '){
    			i=matcher2.start()+1;
    		}
    		else{
    			i=matcher2.start();
    		}
    		j=matcher2.end();
    		String match = numberexp.substring(i, j);
    		int en = match.indexOf('-');
    		if (match.contains("+")){
    			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
    			character.setProperty("type", "range_value");
    			character.setProperty("name", "l_w_ratio");
    			//character.setAttribute("from", match.substring(match.indexOf('=')+2,en).trim());
    			character.setProperty("from", match.substring(match.indexOf('=')+1,en).trim());
    			character.setProperty("to", match.substring(en+1, match.indexOf('+',en+1)).trim());
    			character.setProperty("upperRestricted", "false");
    			innertagstate.add(character);
    			//innertagstate=innertagstate.concat("<character char_type=\"range_value\" name=\"l_w_ratio\" from=\""+match.substring(match.indexOf('=')+2,en).trim()+"\" to=\""+match.substring(en+1, match.indexOf('+',en+1)).trim()+"\" upper_restricted=\"false\"/>");
    		}else{
    			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
    			character.setProperty("type", "range_value");
    			character.setProperty("name", "l_w_ratio");
    			//character.setAttribute("from", match.substring(match.indexOf('=')+2,en).trim());
    			character.setProperty("from", match.substring(match.indexOf('=')+1,en).trim());
    			character.setProperty("to", match.substring(en+1, match.indexOf(' ',en+1)).trim());
    			innertagstate.add(character);
    			//innertagstate=innertagstate.concat("<character char_type=\"range_value\" name=\"l_w_ratio\" from=\""+match.substring(match.indexOf('=')+2,en).trim()+"\" to=\""+match.substring(en+1, match.indexOf(' ',en+1)).trim()+"\"/>");
       		}
    	}
    	numberexp = matcher2.replaceAll("#");
    	matcher2.reset();
    	
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// size: deal with  "[5-]10-15[-20] cm", not deal with "5 cm - 10 cm"                        ////////////
    	//int sizect = 0;
		String toval;
		String fromval;
		numberexp = annotateSize(numberexp, innertagstate, "size");
    	
    	
    	
    	
    	
    	////////////////////////////////////////////////////////////////////////////////////////////
    	//   size                                                                             /////
    	Pattern pattern14 = Pattern.compile("[±\\d\\[\\]\\–\\-\\./\\s]+[\\s]?[\\–\\-]?(% of [\\w]+ length|height of [\\w]+|times as [\\w]+ as [\\w]+|total length|their length|(times)?[\\s]?length of [\\w]+)");
    	matcher2 = pattern14.matcher(numberexp);
    	toval="";
    	fromval="";
    	while ( matcher2.find()){
    		if(numberexp.charAt(matcher2.start())==' '){
    			i=matcher2.start()+1;
    		}
    		else{
    			i=matcher2.start();
    		}
    		j=matcher2.end();
    		String extreme = numberexp.substring(i,j);
			i = 0;
			j = extreme.length();
    		Pattern pattern20 = Pattern.compile("\\[[±\\d\\.\\s\\+]+[\\–\\-]{1}[±\\d\\.\\s\\+\\–\\-]*\\]");
        	Matcher matcher1 = pattern20.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		if(extreme.charAt(q-2)=='–' | extreme.charAt(q-2)=='-'){
        			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
        			character.setProperty("type", "relative_range_value");
        			character.setProperty("name", "atypical_size");
        			character.setProperty("from", extreme.substring(p+1,q-2).trim());
        			character.setProperty("to", "");
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" to=\"\"/>");
        		}else{
        			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
        			character.setProperty("type", "relative_range_value");
        			character.setProperty("name", "atypical_size");
        			character.setProperty("from", extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
        			character.setProperty("to", extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
        			innertagstate.add(character);
        		    //innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
        		}
        	}
        	extreme = matcher1.replaceAll("#");
    		matcher1.reset();
    		if(extreme.contains("#"))
    			i = extreme.indexOf("#")+1;
    		Pattern pattern21 = Pattern.compile("\\[[±\\d\\.\\s\\+\\–\\-]*[\\–\\-]{1}[±\\d\\.\\s\\+]+\\]");
        	matcher1 = pattern21.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		if (extreme.charAt(p+1)=='–' | extreme.charAt(p+1)=='-'){
        			if (extreme.charAt(q-2)=='+'){
            			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
            			character.setProperty("type", "relative_range_value");
            			character.setProperty("name", "atypical_size");
            			character.setProperty("from", "");
            			character.setProperty("to", extreme.substring(p+2,q-2).trim());
            			character.setProperty("upperRestricted", "false");
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-2).trim()+"\" upper_restricted=\"false\"/>");
        			}else{
            			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
            			character.setProperty("type", "relative_range_value");
            			character.setProperty("name", "atypical_size");
            			character.setProperty("from","");
            			character.setProperty("to", extreme.substring(p+2,q-1).trim());
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-1).trim()+"\"/>");
        			}
        		}
        		else{
        			if (extreme.charAt(q-2)=='+'){
            			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
            			character.setProperty("type", "relative_range_value");
            			character.setProperty("name", "atypical_size");
            			character.setProperty("from", extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
            			character.setProperty("to", extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim());
            			character.setProperty("upperRestricted", "false");
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
        			}else{
            			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
            			character.setProperty("type", "relative_range_value");
            			character.setProperty("name", "atypical_size");
            			character.setProperty("from", extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
            			character.setProperty("to", extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim() );
            			//character.setAttribute("upper_restricted", "true");
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
        		
        			}
        		}
        	}
        	extreme = matcher1.replaceAll("#");
    		matcher1.reset();
    		j = extreme.length();
    		Pattern pattern23 = Pattern.compile("\\[[±\\d\\.\\s\\+]+\\]");
        	matcher1 = pattern23.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		if (extreme.charAt(q-2)=='+'){
        			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
        			character.setProperty("type", "relative_value");
        			character.setProperty("name", "atypical_size");
        			character.setProperty("from", extreme.substring(p+1,q-2).trim());
        			character.setProperty("upperRestricted", "false");
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character char_type=\"relative_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
        		}else{
        			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
        			character.setProperty("type", "relative_value");
        			character.setProperty("name", "atypical_size");
        			character.setProperty("value", extreme.substring(p+1,q-1).trim());
        			innertagstate.add(character);
    				//innertagstate = innertagstate.concat("<character char_type=\"relative_value\" name=\"atypical_size\" value=\""+extreme.substring(p+1,q-1).trim()+"\"/>");
        		}
        	}
        	extreme = matcher1.replaceAll("#");
        	matcher1.reset();
        	j = extreme.length();      	
        	if(extreme.substring(i,j).contains("–")|extreme.substring(i,j).contains("-") && !extreme.substring(i,j).contains("×") && !extreme.substring(i,j).contains("x") && !extreme.substring(i,j).contains("X")){
    			String extract = extreme.substring(i,j);
    			Pattern pattern18 = Pattern.compile("[\\s]?[\\–\\-]?(% of [\\w]+ length|height of [\\w]+|times as [\\w]+ as [\\w]+|total length|their length|(times)?[\\s]?length of [\\w]+)");
            	Matcher matcher3 = pattern18.matcher(extract);
            	String relative="";
            	if ( matcher3.find()){
            		relative = extract.substring(matcher3.start(), matcher3.end());
            	}
            	extract = matcher3.replaceAll("#");
            	matcher3.reset();
            	
    			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
    			character.setProperty("type", "relative_range_value");
    			character.setProperty("name", "size");
    			character.setProperty("from", extract.substring(0, extract.indexOf('-')).trim());
    			character.setProperty("to", extract.substring(extract.indexOf('-')+1,extract.indexOf('#')).trim());
    			character.setProperty("relativeConstraint",relative.trim());
    			innertagstate.add(character);
            	//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"size\" from=\""+extract.substring(0, extract.indexOf('-')).trim()+"\" to=\""+extract.substring(extract.indexOf('-')+1,extract.indexOf('#')).trim()+"\" relative_constraint=\""+relative.trim()+"\"/>");
    			toval = extract.substring(0, extract.indexOf('-'));
    			fromval = extract.substring(extract.indexOf('-')+1,extract.indexOf('#'));
            	//sizect+=1;
    		}
    		else{
    			String extract = extreme.substring(i,j);
    			Pattern pattern18 = Pattern.compile("[\\s]?[\\–\\-]?(% of [\\w]+ length|height of [\\w]+|times as [\\w]+ as [\\w]+|total length|their length|(times)?[\\s]?length of [\\w]+)");
            	Matcher matcher3 = pattern18.matcher(extract);
            	String relative="";
            	if ( matcher3.find()){
            		relative = extract.substring(matcher3.start(), matcher3.end());
            	}
            	extract = matcher3.replaceAll("#");
            	matcher3.reset();
    			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
    			character.setProperty("type", "relative_value");
    			character.setProperty("name", "size");
    			character.setProperty("value", extract.substring(0,extract.indexOf('#')).trim());
    			character.setProperty("relativeConstraint", relative.trim());
    			innertagstate.add(character);
            	//innertagstate = innertagstate.concat("<character char_type=\"relative_value\" name=\"size\" value=\""+extract.substring(0,extract.indexOf('#')).trim()+"\" relative_constraint=\""+relative.trim()+"\"/>");
    			toval = extract.substring(0,extract.indexOf('#'));
    			fromval = extract.substring(0,extract.indexOf('#'));
    		}
    		
        	for(DescriptionTreatmentElement element : innertagstate) {
    			if(element.getProperty("to") != null && element.getProperty("to").compareTo("")==0){
    				if(toval.endsWith("+")){
    					toval = toval.replaceFirst("\\+$", "");
    					element.setProperty("upperRestricted", "false");
    				}
    				element.setProperty("to", toval.trim());
    				element.setProperty("toInclusive", "false");
    			}
    			if(element.getProperty("from") != null && element.getProperty("from").compareTo("")==0){
    				element.setProperty("from", fromval.trim());
    				element.setProperty("fromInclusive", "false");
    			}
    		}
        	
    		/*StringBuffer sb = new StringBuffer();
			Pattern pattern25 = Pattern.compile("to=\"\"");
			matcher1 = pattern25.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb, "to=\""+toval.trim()+"\"");
			}
			matcher1.appendTail(sb);
			innertagstate=sb.toString();
			matcher1.reset();
			StringBuffer sb1 = new StringBuffer();
			Pattern pattern26 = Pattern.compile("from=\"\"");
			matcher1 = pattern26.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb1, "from=\""+fromval.trim()+"\"");
			}
			matcher1.appendTail(sb1);
			innertagstate=sb1.toString();
			matcher1.reset();*/
    	}
    	numberexp = matcher2.replaceAll("#");
    	matcher2.reset();
    
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//   count                                                                                             ///////////////
    	/*p1 = Pattern.compile("^\\[(\\d+)\\](.*)");
    	m = p1.matcher(numberexp);
    	if(m.matches()){
    		Element character = new Element("characterName");
			character.setAttribute("name", "atypical_"+(cname==null?"count": cname));
			character.setAttribute("value", m.group(1));
			innertagstate.add(character);
			numberexp = m.group(2).trim();
    	}
    	
    	
    	p1 = Pattern.compile("^\\[(\\d+)\\+\\](.*)");
    	m = p1.matcher(numberexp);
    	if(m.matches()){
    		Element character = new Element("characterName");
    		character.setAttribute("char_type", "range_value");
			character.setAttribute("name", "atypical_"+(cname==null?"count": cname));
			character.setAttribute("from", m.group(1));
			character.setAttribute("upper_restricted", "false");
			innertagstate.add(character);
			numberexp = m.group(2);
    	}*/
    	
    	
    	//int countct = 0;
    	Pattern pattern15 = Pattern.compile("([\\[]?[±]?[\\d]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d]+[+]?[\\]]?|[\\[]?[±]?[\\d]+[+]?[\\]]?[\\s]?)[\\–\\–\\-]+[a-zA-Z]+");
    	matcher2 = pattern15.matcher(numberexp);
    	numberexp = matcher2.replaceAll("#");
    	matcher2.reset();     	
    	//Pattern pattern16 = Pattern.compile("(?<!([/][\\s]?))([\\[]?[±]?[\\d]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d]+[+]?[\\]]?[\\s]?([\\[]?[\\–\\-]?[\\]]?[\\s]?[\\[]?[\\d]+[+]?[\\]]?)*|[±]?[\\d]+[+]?)(?!([\\s]?[n/]|[\\s]?[\\–\\-]?% of [\\w]+ length|[\\s]?[\\–\\-]?height of [\\w]+|[\\s]?[\\–\\-]?times|[\\s]?[\\–\\-]?total length|[\\s]?[\\–\\-]?their length|[\\s]?[\\–\\-]?(times)?[\\s]?length of|[\\s]?[dcmµ]?m))");
    	//Pattern pattern16 = Pattern.compile("(?<!([/][\\s]?))([\\[]?[±]?[\\d\\./%]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?[\\s]?([\\[]?[\\–\\-]?[\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?)*|[±]?[\\d\\./%]+[+]?)(?!([\\s]?[n/]|[\\s]?[\\–\\-]?% of [\\w]+ length|[\\s]?[\\–\\-]?height of [\\w]+|[\\s]?[\\–\\-]?times|[\\s]?[\\–\\-]?total length|[\\s]?[\\–\\-]?their length|[\\s]?[\\–\\-]?(times)?[\\s]?length of|[\\s]?[dcmµ]?m))");
    	Pattern pattern16 = Pattern.compile("(?<!([/][\\s]?))([\\[]?[±]?[\\d\\./%]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?[\\s]?([\\[]?[\\–\\-]?[\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?)*|\\[?[±]?[\\d\\./%]+[+]?\\]?)(?!([\\s]?[n/]|[\\s]?[\\–\\-]?% of [\\w]+ length|[\\s]?[\\–\\-]?height of [\\w]+|[\\s]?[\\–\\-]?times|[\\s]?[\\–\\-]?total length|[\\s]?[\\–\\-]?their length|[\\s]?[\\–\\-]?(times)?[\\s]?length of|[\\s]?[dcmµ]?m))");
    	matcher2 = pattern16.matcher(numberexp);
    	while ( matcher2.find()){
    		i=matcher2.start();
    		j=matcher2.end();
    		String extreme = numberexp.substring(i,j);
			i = 0;
			j = extreme.length();
    		Pattern pattern20 = Pattern.compile("\\[[±\\d\\.\\s\\+]+[\\–\\-]{1}[±\\d\\.\\s\\+\\–\\-]*\\]");
        	Matcher matcher1 = pattern20.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		if(extreme.charAt(q-2)=='–' | extreme.charAt(q-2)=='-'){
        			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
        			character.setProperty("type", "range_value");
        			character.setProperty("name", "atypical_"+(cname==null?"count": cname));
        			character.setProperty("from", extreme.substring(p+1,q-2).trim());
        			character.setProperty("to", "");
        			innertagstate.add(character);
        			
        			//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,q-2).trim()+"\" to=\"\"/>");
        		}else{
        			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
        			character.setProperty("type", "range_value");
        			character.setProperty("name", "atypical_"+(cname==null?"count": cname));
        			character.setProperty("from", extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
        			String tmp = extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim();
        			character.setProperty("to", tmp.replaceFirst("[^0-9]+$", ""));
        			if(tmp.endsWith("+")){
        				character.setProperty("upperRestricted", "false");
        			}
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
        		}
        	}
        	extreme = matcher1.replaceAll("#");
    		matcher1.reset();
    		if(extreme.contains("#"))
    			i = extreme.indexOf("#")+1;
    		j = extreme.length(); //process from # to the end of extreme. but in 1-[2-5] (1-#), the value is before #
    		Pattern pattern21 = Pattern.compile("\\[[±\\d\\.\\s\\+\\–\\-]*[\\–\\-]{1}[±\\d\\.\\s\\+]+\\]");
        	matcher1 = pattern21.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		j = p;
        		if (extreme.charAt(p+1)=='–' | extreme.charAt(p+1)=='-'){
        			if (extreme.charAt(q-2)=='+'){
            			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
            			character.setProperty("type", "range_value");
            			character.setProperty("name", "atypical_"+(cname==null?"count": cname));
            			character.setProperty("from", "");
            			character.setProperty("to", extreme.substring(p+2,q-2).trim());
            			character.setProperty("upperRestricted", "false");
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\"\" to=\""+extreme.substring(p+2,q-2).trim()+"\" upper_restricted=\"false\"/>");
        			}else{
            			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
            			character.setProperty("type", "range_value");
            			character.setProperty("name", "atypical_"+(cname==null?"count": cname));
            			character.setProperty("from", "");
            			character.setProperty("to", extreme.substring(p+2,q-1).trim());
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\"\" to=\""+extreme.substring(p+2,q-1).trim()+"\"/>");
        			}
        		}
        		else{
        			if (extreme.charAt(q-2)=='+'){
            			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
            			character.setProperty("type", "range_value");
            			character.setProperty("name", "atypical_"+(cname==null?"count": cname));
            			character.setProperty("from", extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
            			character.setProperty("to", extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
            			character.setProperty("upperRestricted", "false");
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
        			}else{
            			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
            			character.setProperty("type", "range_value");
            			character.setProperty("name", "atypical_"+(cname==null?"count": cname));
            			character.setProperty("from", extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
            			character.setProperty("to", extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
            			innertagstate.add(character);
        				//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
        			}
        		}
        			
        	}
    		matcher1.reset();
    		Pattern pattern23 = Pattern.compile("\\[[±\\d\\.\\s\\+]+\\]");
        	matcher1 = pattern23.matcher(extreme);
        	if ( matcher1.find()){
        		int p = matcher1.start();
        		int q = matcher1.end();
        		j = p;
        		if (extreme.charAt(q-2)=='+'){
        			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
        			character.setProperty("type", "range_value");
        			character.setProperty("name", "atypical_"+(cname==null?"count": cname));
        			character.setProperty("from", extreme.substring(p+1,q-2).trim());
        			character.setProperty("upperRestricted", "false");
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character name=\"atypical_count\" from=\""+extreme.substring(p+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
        		}else{
        			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
        			character.setProperty("name", "atypical_"+(cname==null?"count": cname));
        			character.setProperty("value", extreme.substring(p+1,q-1).trim());
           			innertagstate.add(character);
    				//innertagstate = innertagstate.concat("<character name=\"atypical_count\" value=\""+extreme.substring(p+1,q-1).trim()+"\"/>");
        		}
        	}
        	matcher1.reset();
        	//# to the end
        	String extract = extreme.substring(i,j);
        	
    		if(extract.contains("–")|extract.contains("-") && !extract.contains("×") && !extract.contains("x") && !extract.contains("X")){
    			//String extract = extreme.substring(i,j);
    			Pattern pattern22 = Pattern.compile("[\\[\\]]+");
    			matcher1 = pattern22.matcher(extract);
    			extract = matcher1.replaceAll("");
    			matcher1.reset();
    			
    			String to = extract.substring(extract.indexOf('-')+1,extract.length()).trim();
    			boolean upperrestricted = true;
    			if(to.endsWith("+")){
    				upperrestricted = false;
    				to = to.replaceFirst("\\+$", "");
    			}
    			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
    			character.setProperty("type", "range_value");
    			character.setProperty("name", cname==null?"count": cname);
    			character.setProperty("from", extract.substring(0, extract.indexOf('-')).trim());
    			character.setProperty("to", to);
    			if(!upperrestricted)
    				character.setProperty("upperRestricted", upperrestricted+"");
    			innertagstate.add(character);
            	//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"count\" from=\""+extract.substring(0, extract.indexOf('-')).trim()+"\" to=\""+extract.substring(extract.indexOf('-')+1,extract.length()).trim()+"\"/>");
    			toval = extract.substring(0, extract.indexOf('-'));
    			fromval = extract.substring(extract.indexOf('-')+1,extract.length());
    			//countct+=1;
    		}else{
    			//String extract = extreme.substring(i,j).trim();
    			if(extract.length()>0){
        			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
        			character.setProperty("name", cname==null?"count": cname);
        			if(extract.endsWith("+")){
        				extract = extract.replaceFirst("\\+$", "").trim();
        				character.setProperty("type", "range_value");
        				character.setProperty("from", extract);
        				character.setProperty("upperRestricted", "false");
        			}else{
        				character.setProperty("value", extract);
        			}
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character name=\"count\" value=\""+extract.trim()+"\"/>");
        			toval = extract;
        			fromval = extract;
    			}
    		}
    		//start to #, dupllicated above
    		if(i-1>0){
    		extract = extreme.substring(0, i-1);
    		if(extract.contains("–")|extract.contains("-") && !extract.contains("×") && !extract.contains("x") && !extract.contains("X")){
    			//String extract = extreme.substring(i,j);
    			Pattern pattern22 = Pattern.compile("[\\[\\]]+");
    			matcher1 = pattern22.matcher(extract);
    			extract = matcher1.replaceAll("");
    			matcher1.reset();
    			
    			String to = extract.substring(extract.indexOf('-')+1,extract.length()).trim();
    			boolean upperrestricted = true;
    			if(to.endsWith("+")){
    				upperrestricted = false;
    				to = to.replaceFirst("\\+$", "");
    			}
    			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
    			character.setProperty("type", "range_value");
    			character.setProperty("name", cname==null?"count": cname);
    			character.setProperty("from", extract.substring(0, extract.indexOf('-')).trim());
    			character.setProperty("to", to);
    			if(!upperrestricted)
    				character.setProperty("upperRestricted", upperrestricted+"");
    			innertagstate.add(character);
            	//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"count\" from=\""+extract.substring(0, extract.indexOf('-')).trim()+"\" to=\""+extract.substring(extract.indexOf('-')+1,extract.length()).trim()+"\"/>");
    			toval = extract.substring(0, extract.indexOf('-'));
    			fromval = extract.substring(extract.indexOf('-')+1,extract.length());
    			//countct+=1;
    		}else{
    			//String extract = extreme.substring(i,j).trim();
    			if(extract.length()>0){
        			DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
        			character.setProperty("name", cname==null?"count": cname);
        			if(extract.endsWith("+")){
        				extract = extract.replaceFirst("\\+$", "").trim();
        				character.setProperty("type", "range_value");
        				character.setProperty("from", extract);
        				character.setProperty("upperRestricted", "false");
        			}else{
        				character.setProperty("value", extract);
        			}
        			innertagstate.add(character);
        			//innertagstate = innertagstate.concat("<character name=\"count\" value=\""+extract.trim()+"\"/>");
        			toval = extract;
        			fromval = extract;
    			}
    		}
    		}
    		
    		for(DescriptionTreatmentElement element : innertagstate) {
    			if(element.getProperty("to") != null && element.getProperty("to").compareTo("")==0){
    				if(toval.endsWith("+")){
    					toval = toval.replaceFirst("\\+$", "");
    					element.setProperty("upperRestricted", "false");
    				}
    				element.setProperty("to", toval.trim());
    				element.setProperty("toInclusive", "false");
    			}
    			if(element.getProperty("from") != null && element.getProperty("from").compareTo("")==0){
    				element.setProperty("from", fromval.trim());
    				element.setProperty("fromInclusive", "false");
    			}
    		}
    		/*
    		StringBuffer sb = new StringBuffer();
			Pattern pattern25 = Pattern.compile("to=\"\"");
			matcher1 = pattern25.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb, "to=\""+toval.trim()+"\"");
			}
			matcher1.appendTail(sb);
			innertagstate=sb.toString();
			matcher1.reset();
			StringBuffer sb1 = new StringBuffer();
			Pattern pattern26 = Pattern.compile("from=\"\"");
			matcher1 = pattern26.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb1, "from=\""+fromval.trim()+"\"");
			}
			matcher1.appendTail(sb1);
			innertagstate=sb1.toString();
			matcher1.reset();*/
    	}
    	matcher2.reset();   

 		return innertagstate;
	}

	//find all () in object
	protected String listStructureNames(Chunk object){
		String organString = "";
		for(Chunk objectChunk : object.getChunks()) {
			if(objectChunk.isOfChunkType(ChunkType.ORGAN))
				organString += objectChunk.getTerminalsText() + ", ";
		}
		return organString.trim().replaceFirst(",$", "");
	}
	
	protected String listStructureIds(List<DescriptionTreatmentElement> structures) {
		StringBuffer list = new StringBuffer();
		for(DescriptionTreatmentElement element : structures)
			list.append(element.getProperty("id")).append(", ");
		return list.toString().trim().replaceFirst(",$", "");
	}

	/**
	 * 
	 * @param ckstring:r[p[in] o[outline]]
	 * @return
	 */
	protected boolean characterPrep(Chunk chunk, ProcessingContextState processingContextState) {
		boolean done = false;
		
		LinkedList<DescriptionTreatmentElement> lastElements = processingContextState.getLastElements();
		Set<String> characters = glossary.getWords("characterName");
		
		List<AbstractParseTree> terminals = chunk.getTerminals();
		if(terminals.size() < 2)
			return done;
		String lastWord = terminals.get(terminals.size()-1).getTerminalsText();
		if(characters.contains(lastWord)) {
			DescriptionTreatmentElement lastElement = lastElements.getLast();
			if(lastElement.isOfDescriptionType(DescriptionType.CHARACTER)) {//shell oval in outline
				for(DescriptionTreatmentElement element : lastElements)
					element.setProperty("name", lastWord);
				done = true;
			}else if(lastElement.isOfDescriptionType(DescriptionType.STRUCTURE)) {//shell in oval outline
				//String cvalue = ..
				Chunk characterValue = chunk.getChunkDFS(ChunkType.PREPOSITION);
						//ckstring.replaceFirst(".*?\\]", "").replaceAll("\\w+\\[","").replaceAll(lastword, "").replaceAll("[{}\\]\\[]", "");
				for(DescriptionTreatmentElement element : lastElements) {
					element.setProperty("name", lastWord);
					element.setProperty("value", characterValue.getTerminalsText());
				}
				done = true;
			}
		}
		return done;
	}
	
	protected void annotateType(Chunk chunk, DescriptionTreatmentElement lastElement) {
		List<Chunk> modifiers = chunk.getChunks(ChunkType.MODIFIER);
		List<Chunk> objects = chunk.getChunks(ChunkType.OBJECT);
		
		DescriptionTreatmentElement typeElement = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
		lastElement.addTreatmentElement(typeElement);
		//if(this.inbrackets) this.addAttribute(type, "in_bracket", "true");
		for(Chunk object : objects) 
			typeElement.appendProperty("type", object.getTerminalsText());
		for(Chunk modifier : modifiers) 
			typeElement.appendProperty("modifier", modifier.getTerminalsText());
	}
	
	protected String annotateSize(String plaincharset, LinkedList<DescriptionTreatmentElement> innertagstate, String chara) {
		int i;
		int j;
		Matcher matcher2;
		Pattern pattern13 = Pattern.compile("[xX\\×±\\d\\[\\]\\–\\-\\.\\s\\+]+[\\s]?([dcmµ]?m)(?![\\w])(([\\s]diam)?([\\s]wide)?)");
		matcher2 = pattern13.matcher(plaincharset);
		String toval="";
		String fromval="";
		while ( matcher2.find()){
			String unit = matcher2.group(1);
			if(plaincharset.charAt(matcher2.start())==' '){
				i=matcher2.start()+1;
			}
			else{
				i=matcher2.start();
			}
			j=matcher2.end();
			String extreme = plaincharset.substring(i,j);
			i = 0;
			j = extreme.length();
			Pattern pattern20 = Pattern.compile("\\[[±\\d\\.\\s\\+]+[\\–\\-]{1}[±\\d\\.\\s\\+\\–\\-]*\\]");
			Matcher matcher1 = pattern20.matcher(extreme);
			if ( matcher1.find()){
				int p = matcher1.start();
				int q = matcher1.end();
				if(extreme.charAt(q-2)=='–' | extreme.charAt(q-2)=='-'){
					DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
					character.setProperty("type", "range_value");
					character.setProperty("name", "atypical_"+chara);
					character.setProperty("from", extreme.substring(p+1,q-2).trim());
					character.setProperty("to", "");
					character.setProperty("fromUnit", unit);
					character.setProperty("toUnit", unit);
					//character.setAttribute("upper_restricted", "false");
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" to=\"\"/>");
				}else{
					DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
					character.setProperty("type", "range_value");
					character.setProperty("name", "atypical_"+chara);
					character.setProperty("from", extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
					character.setProperty("to", extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
					character.setProperty("fromUnit", unit);
					character.setProperty("toUnit", unit);
					//character.setAttribute("upper_restricted", "??");
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
				}
			}
			extreme = matcher1.replaceAll("#");
			matcher1.reset();
			if(extreme.contains("#"))
				i = extreme.indexOf("#")+1;
			Pattern pattern21 = Pattern.compile("\\[[±\\d\\.\\s\\+\\–\\-]*[\\–\\-]{1}[±\\d\\.\\s\\+]+\\]");
			matcher1 = pattern21.matcher(extreme);
			if ( matcher1.find()){
				int p = matcher1.start();
				int q = matcher1.end();
				if (extreme.charAt(p+1)=='–' | extreme.charAt(p+1)=='-'){
					if (extreme.charAt(q-2)=='+'){
						DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
		    			character.setProperty("type", "range_value");
		    			character.setProperty("name", "atypical_"+chara);
		    			character.setProperty("from", "");
		    			character.setProperty("to", extreme.substring(p+2,q-2).trim());
		    			character.setProperty("fromUnit", unit);
		    			character.setProperty("toUnit", unit);
		    			character.setProperty("upperRestricted", "false");
		    			innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-2).trim()+"\" upper_restricted=\"false\"/>");
					}else{
						DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
		    			character.setProperty("type", "range_value");
		    			character.setProperty("name", "atypical_"+chara);
		    			character.setProperty("from", "");
		    			character.setProperty("to", extreme.substring(p+2,q-1).trim());
		    			character.setProperty("fromUnit", unit);
		    			character.setProperty("toUnit", unit);
		    			//character.setAttribute("upper_restricted", "true");
		    			innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-1).trim()+"\"/>");
					}
				}
				else{
					if (extreme.charAt(q-2)=='+'){
						DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
		    			character.setProperty("type", "range_value");
		    			character.setProperty("name", "atypical_"+chara);
		    			character.setProperty("from", extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
		    			character.setProperty("to", extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim());
		    			character.setProperty("fromUnit", unit);
		    			character.setProperty("toUnit", unit);
		    			character.setProperty("upperRestricted", "false");
		    			innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
					}else{
						DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
		    			character.setProperty("type", "range_value");
		    			character.setProperty("name", "atypical_"+chara);
		    			character.setProperty("from", extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
		    			character.setProperty("to", extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
		    			character.setProperty("fromUnit", unit);
		    			character.setProperty("toUnit", unit);
		    			//character.setAttribute("upper_restricted", "true");
		    			innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
					}
				}
			}
			extreme = matcher1.replaceAll("#");
			matcher1.reset();
			j = extreme.length();
			Pattern pattern23 = Pattern.compile("\\[[±\\d\\.\\s\\+]+\\]");
			matcher1 = pattern23.matcher(extreme);
			if ( matcher1.find()){
				int p = matcher1.start();
				int q = matcher1.end();
				if (extreme.charAt(q-2)=='+'){
					DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
					character.setProperty("name", "atypical_"+chara);
					character.setProperty("from", extreme.substring(p+1,q-2).trim());
					character.setProperty("to", "");
					character.setProperty("fromUnit", unit);
					character.setProperty("toUnit", unit);
					character.setProperty("upperRestricted", "false");
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
				}else{
					DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
					character.setProperty("name", "atypical_"+chara);
					character.setProperty("value", extreme.substring(p+1,q-1).trim());
					character.setProperty("unit", unit);
					//character.setAttribute("unit", extreme.substring(q-1).trim());
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character name=\"atypical_size\" value=\""+extreme.substring(p+1,q-1).trim()+"\"/>");
				}
			}
			extreme = matcher1.replaceAll("#");
			matcher1.reset();
			j = extreme.length();
			if(extreme.substring(i,j).contains("–")|extreme.substring(i,j).contains("-") && !extreme.substring(i,j).contains("×") && !extreme.substring(i,j).contains("x") && !extreme.substring(i,j).contains("X")){
				String extract = extreme.substring(i,j);
				Pattern pattern18 = Pattern.compile("[\\s]?[dcmµ]?m(([\\s]diam)?([\\s]wide)?)");
		    	Matcher matcher3 = pattern18.matcher(extract);
		    	unit="";
		    	if ( matcher3.find()){
		    		unit = extract.substring(matcher3.start(), matcher3.end());
		    	}
		    	extract = matcher3.replaceAll("#");
		    	matcher3.reset();
		    	String from = extract.substring(0, extract.indexOf('-')).trim();
		    	String to = extract.substring(extract.indexOf('-')+1,extract.indexOf('#')).trim();
		    	boolean upperrestricted = ! to.endsWith("+");
		    	to = to.replaceFirst("\\+$", "").trim();
		    	
		    	DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
				character.setProperty("type", "range_value");
				character.setProperty("name", chara);
				character.setProperty("from", from);
				character.setProperty("fromUnit", unit.trim());
				character.setProperty("to", to);
				character.setProperty("toUnit", unit.trim());
				if(!upperrestricted)
					character.setProperty("upperRestricted", upperrestricted+"");
				innertagstate.add(character);
		    	//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"size\" from=\""+from+"\" from_unit=\""+unit.trim()+"\" to=\""+to+"\" to_unit=\""+unit.trim()+"\" upper_restricted=\""+upperrestricted+"\"/>");
				toval = extract.substring(0, extract.indexOf('-'));
				fromval = extract.substring(extract.indexOf('-')+1,extract.indexOf('#'));
		    	//sizect+=1;
			}
			else{
				String extract = extreme.substring(i,j);
				Pattern pattern18 = Pattern.compile("[\\s]?[dcmµ]?m(([\\s]diam)?([\\s]wide)?)");
		    	Matcher matcher3 = pattern18.matcher(extract);
		    	unit="";
		    	if ( matcher3.find()){
		    		unit = extract.substring(matcher3.start(), matcher3.end());
		    	}
		    	extract = matcher3.replaceAll("#");
		    	matcher3.reset();
		    	
		    	DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
				character.setProperty("name", chara);
				character.setProperty("value", extract.substring(0,extract.indexOf('#')).trim());
				character.setProperty("unit", unit.trim());
				innertagstate.add(character);
		    	//innertagstate = innertagstate.concat("<character name=\"size\" value=\""+extract.substring(0,extract.indexOf('#')).trim()+"\" unit=\""+unit.trim()+"\"/>");
				toval = extract.substring(0,extract.indexOf('#'));
				fromval = extract.substring(0,extract.indexOf('#'));
			}
			
			for(DescriptionTreatmentElement element : innertagstate) {
				if(element.getProperty("to") != null && element.getProperty("to").compareTo("")==0){
					if(toval.endsWith("+")){
						toval = toval.replaceFirst("\\+$", "");
						element.setProperty("upperRestricted", "false");
					}
					element.setProperty("to", toval.trim());
					element.setProperty("toInclusive", "false");
				}
				if(element.getProperty("from") != null && element.getProperty("from").compareTo("")==0){
					element.setProperty("from", fromval.trim());
					element.setProperty("fromInclusive", "false");
				}
			}
		}
		plaincharset = matcher2.replaceAll("#");
		matcher2.reset();
		//log(LogLevel.DEBUG, "plaincharset2:"+plaincharset);
		return plaincharset;
	}
	

	protected LinkedList<DescriptionTreatmentElement> latest(DescriptionType descriptionType, List<DescriptionTreatmentElement> list) {
		LinkedList<DescriptionTreatmentElement> selected = new LinkedList<DescriptionTreatmentElement>();
		int size = list.size();
		for(int i = size-1; i>=0; i--){
			if(list.get(i).isOfDescriptionType(descriptionType)) {
				selected.add(list.get(i));
			}else{
				break;
			}
		}
		return selected;
	}
	
	@Override
	public String getDescription() {
		return this.getClass().toString();
	}
	
	public DescriptionTreatmentElement getFirstDescriptionElement(List<DescriptionTreatmentElement> elements, DescriptionType descriptionType) {
		DescriptionTreatmentElement result = null;
		for(int i=0; i<elements.size(); i++) {
			DescriptionTreatmentElement element = elements.get(i);
			if(element.isOfDescriptionType(descriptionType)) {
				result = element;
				break;
			}
		}
		return result;
	}
	
	public DescriptionTreatmentElement getLastDescriptionElement(List<DescriptionTreatmentElement> elements, DescriptionType descriptionType) {
		DescriptionTreatmentElement result = null;
		for(int i=elements.size()-1; i>=0; i--) {
			DescriptionTreatmentElement element = elements.get(i);
			if(element.isOfDescriptionType(descriptionType)) {
				result = element;
				break;
			}
		}
		return result;
	}
}
