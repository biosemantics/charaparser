package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * CleanupChunker reformats the chunks after all other chunkers have been run to obtain an overall consistent chunking result
 * @author rodenhausen
 */
public class CleanupChunker extends AbstractChunker {

	private String skipWords;
	private String moreWords;
	private String numberPattern;
	private IPOSKnowledgeBase posKnowledgeBase;
	private String percentage;
	private String degree;
	private String counts;
	private String times;
	
	private LinkedHashSet<Chunk> unassignedModifier = new LinkedHashSet<Chunk>();
	private List<Integer> pastPointers = new ArrayList<Integer>();
	//private boolean rightAfterSubject = false;
	private ICharacterKnowledgeBase characterKnowledgeBase;

	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 * @param skipWords
	 * @param moreWords
	 * @param numberPattern
	 * @param posKnowledgeBase
	 * @param percentage
	 * @param degree
	 * @param counts
	 * @param times
	 * @param characterKnowledgeBase
	 */
	@Inject
	public CleanupChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, @Named("SkipWords")String skipWords, 
			@Named("MoreWords")String moreWords, @Named("NumberPattern") String numberPattern, IPOSKnowledgeBase posKnowledgeBase, 
			@Named("PercentageWords")String percentage, @Named("DegreeWords") String degree, @Named("CountWords")String counts, 
			@Named("TimesWords") String times, ICharacterKnowledgeBase characterKnowledgeBase, 
			IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, 
				terminologyLearner, inflector, organStateKnowledgeBase);
		this.skipWords = skipWords;
		this.moreWords = moreWords;
		this.numberPattern = numberPattern;
		this.posKnowledgeBase = posKnowledgeBase;
		this.percentage = percentage;
		this.degree = degree;
		this.counts = counts;
		this.times = times;
		this.characterKnowledgeBase = characterKnowledgeBase;
	}

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		for(int pointer=0; pointer < terminals.size();) {
			pointer = composeChunk(terminals, pointer, chunkCollector);
		}
	}

	
	private int composeChunk(List<AbstractParseTree> terminals, int pointer, ChunkCollector chunkCollector) {
		//log(LogLevel.DEBUG, terminals.get(pointer) + " " + chunkCollector.isPartOfANonTerminalChunk(terminals.get(pointer)));
		if(terminals.get(pointer).getTerminalsText().equals("quasi")){
			log(LogLevel.DEBUG, "quasi");
		}
		if(chunkCollector.isPartOfANonTerminalChunk(terminals.get(pointer))) {
			return ++pointer;
		}
		Chunk savedCharacterState = null;
		//String savedCharacterState = "";
		String role = "";
		boolean foundOrgan = false;//found organ
		boolean foundState = false;//found state
		
		//assign unassignedModifier to the saved character state
		if(!unassignedModifier.isEmpty()) {
			Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, unassignedModifier);
			if(savedCharacterState != null) {
				LinkedHashSet<Chunk> childChunks = savedCharacterState.getChunks();
				childChunks.add(modifierChunk);
				savedCharacterState.setChunks(childChunks);
				//todo when is it being added to chunkCollector?
			} else {
				savedCharacterState = modifierChunk;
			}	
			unassignedModifier.clear();
		}
		
		int i = 0;
		//skip over all terminals to compose chunks
		for(i = pointer; i < terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			String terminalsText = terminal.getTerminalsText();
			
			/* if one of the tokens match those in the stop list but not in skip list, skip it and get the next token- mohan 10/19/2011*/
			if(terminalsText.matches("("+stopWords+")") && !terminalsText.matches("("+skipWords+")")){
				i = i + 1;
				terminal = terminals.get(i);
				terminalsText = terminal.getTerminalsText();
			}
			/*end mohan 10/19/2011*/
			terminalsText = terminalsText.matches(".*?\\d.*") ? this.originalNumForm(terminalsText) : terminalsText;
			if(terminalsText.length()==0){
				continue;
			}
			terminal.setTerminalsText(terminalsText);
			
			//token = NumericalHandler.originalNumForm(token); //turn -LRB-/-LRB-2
			if(chunkCollector.isPartOfANonTerminalChunk(terminal)) {
				Chunk terminalChunk = chunkCollector.getChunk(terminal);
			//if(terminalsText.matches("^\\w+\\[.*")) { //modifier +  a chunk: m[usually] n[size[{shorter}] constraint[than or {equaling} (phyllaries)]]
				//if(scs.matches("\\w{2,}\\[.*") && token.matches("\\w{2,}\\[.*")){ // scs: position[{adaxial}] token: pubescence[{pubescence~list~glabrous~or~villous}]
				if(savedCharacterState!=null && savedCharacterState.containsChunkType(ChunkType.OBJECT)) {
				//if(savedCharacterState.matches(".*?\\bo\\[\\w+\\s.*")){
					pointer = i;
					//savedCharacterState = savedCharacterState.replaceAll("o\\[", "o[(").trim()+")]";
					chunkCollector.addChunk(new Chunk(ChunkType.NON_SUBJECT_ORGAN, savedCharacterState));
					return pointer;
					//return new ChunkNonSubjectOrgan("u["+savedCharacterState+"]");
				} else if(savedCharacterState!=null && savedCharacterState.containsChunkType(ChunkType.CHARACTER_STATE)) { //.matches(".*?\\w{2,}\\[.*")){
					pointer = i;
					chunkCollector.addChunk(savedCharacterState);
					return pointer;
					//return new ChunkSimpleCharacterState("a["+savedCharacterState.trim()+"]]"); 
				} else {
					//String type = chunkType(i, chunkCollector); //changed from pointer to i
					
					//savedCharacterState = savedCharacterState.trim().length() > 0 ? savedCharacterState.trim()+"] " : ""; //modifier 
					//String start = terminalsText.substring(0, terminalsText.indexOf("[") + 1); //becomes n[m[usually] size[{shorter}] constraint[than or {equaling} (phyllaries)]]
					//String end = terminalsText.replace(start, "");
					//String abc="";
					//String end = token.replaceFirst(start, abc);
					//terminalsText = start + savedCharacterState + end;
					
					ChunkType chunkType = getChunkType(i, chunkCollector);
					if(chunkType != null) {
						LinkedHashSet<Chunk> childChunks = terminalChunk.getChunks();
						//Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, savedCharacterState);
						LinkedHashSet<Chunk> newChildChunks = new LinkedHashSet<Chunk>();
						
						if(savedCharacterState!=null) {
							newChildChunks.add(savedCharacterState);
						}
						newChildChunks.addAll(childChunks);
						Chunk chunk = new Chunk(getChunkType(i, chunkCollector), newChildChunks);
						chunkCollector.addChunk(chunk);
					
						pointer = i+1;
						return pointer;
					} else {
						String t = "";
						do{
							if(pointer < terminals.size()){
								t = terminals.get(pointer++).getTerminalsText();
							}else{
								break;
							}
						}while (!t.matches("[,;:\\.]"));
						return pointer;
					}
				}
			}
			
			boolean isOrgan = chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN);
			Chunk organChunk = chunkCollector.getChunk(terminal);
			//role = terminalsText.substring(0, 1);//.charAt(0)+"";
			//terminalsText = terminalsText.replaceAll("[<>{}]", "");
			//<roots> {usually} <taproots> , {sometimes} {fibrous}.
			/*ChunkType chunkType;
			if(rightAfterSubject)
				chunkType = ChunkType.Type;
			else
				chunkType = ChunkType.ChunkOrgan;*/
			//String symbol = rightAfterSubject? "type" : "o";
			ChunkType organChunkType = this.isRightAfterSubject(i, terminals, chunkCollector) ? ChunkType.TYPE : ChunkType.OBJECT;
			
			if(!foundOrgan && isOrgan){
				LinkedHashSet<Chunk> objectChunks = new LinkedHashSet<Chunk>();
				objectChunks.add(organChunk);
				if(savedCharacterState!=null) {	
					LinkedHashSet<Chunk> childChunks = savedCharacterState.getChunks();
					childChunks.add(new Chunk(organChunkType, objectChunks));
					savedCharacterState.setChunks(childChunks);
				} else {
					savedCharacterState = new Chunk(organChunkType, objectChunks);
				}
				//savedCharacterState = (savedCharacterState.trim().length()>0? savedCharacterState.trim()+"] ": "")+symbol+"["+terminalsText+" ";
				foundOrgan = true;
			} else if(foundOrgan && isOrgan) {
				LinkedHashSet<Chunk> childChunks = savedCharacterState.getChunks();
				childChunks.add(terminal);
				savedCharacterState.setChunks(childChunks);
				//savedCharacterState += terminalsText + " ";
			} else if(foundOrgan && !isOrgan){
				pointer = i;
				//todo: how to do this????
				/*savedCharacterState = savedCharacterState.replaceFirst("^\\]\\s+", "").
						replaceFirst(symbol+"\\[", "###[").replaceAll("\\w+\\[", "m[").
						replaceAll("###\\[", symbol+"[").trim()+"]"; */ //change all non-type character to modifier: <Inflorescences> {indeterminate} <heads>
				/*if(!rightAfterSubject){
					//reformat m[] o[] o[] to m[] o[()] o[()]
					String m = savedCharacterState.substring(0, savedCharacterState.indexOf("o["));
					String o = savedCharacterState.substring(savedCharacterState.indexOf("o[")).replaceAll("\\[", "[(").replaceAll("\\]", ")]");
					savedCharacterState = m+o;
				}*/
				Chunk chunk = this.isRightAfterSubject(i, terminals, chunkCollector) ? new Chunk(ChunkType.CHARACTER_STATE, savedCharacterState) : 
					new Chunk(ChunkType.NON_SUBJECT_ORGAN, savedCharacterState); //must have type[ or o[
				chunkCollector.addChunk(chunk);
				return pointer;
			}
			
			if(terminalsText.matches(".*?" + numberPattern + "$") || terminalsText.matches("\\d+\\+?") || terminalsText.matches("^to~\\d.*")) { 
				//0. sentence ends with a number, the . is not separated by a space
				if(savedCharacterState.containsChunkType(ChunkType.CHARACTER_STATE)) {
						//savedCharacterState.matches(".*?\\w{2,}\\[.*")){//must have character[
					pointer=i;
					chunkCollector.addChunk(savedCharacterState);
					return pointer;
					//savedCharacterState = savedCharacterState.replaceFirst("^\\]\\s+", "").trim()+"]";
					//return new ChunkSimpleCharacterState("a["+savedCharacterState.trim()+"]");
				} else {
					pointer = i;
					return pointer;
					/*
					Chunk chunk = getNextNumerics(pointer, terminals, chunkCollector);
					if(chunk != null) {
						if(savedCharacterState != null) {
							savedCharacterState.getChunks().add(chunk);
						} else {
							savedCharacterState = chunk;
						}
						chunkCollector.addChunk(savedCharacterState);
						return pointer;
					} else {
						pointer++;
						return pointer;
						//return null, skip this token: parsing failure
					}*/
				}
			}

			
			//add to a state chunk until a) a preposition b) a punct mark or c)another state is encountered
			if(!isOrgan){
				if(posKnowledgeBase.isAdverb(terminalsText)) {
					if(savedCharacterState != null) {
						Chunk modifierChunk = savedCharacterState.getChunkDFS(ChunkType.MODIFIER);
						if(modifierChunk!=null)
							modifierChunk.getChunks().add(terminal);
					} else {
						LinkedHashSet<Chunk> modifier = new LinkedHashSet<Chunk>();
						modifier.add(terminal);
						savedCharacterState = new Chunk(ChunkType.MODIFIER, modifier);
					}
				} else if(savedCharacterState != null && (terminalsText.matches(".*[,;:\\.\\[].*") || terminalsText.matches("\\b(" + prepositionWords + "|or|and)\\b") 
						|| terminalsText.compareTo("-LRB-/-LRB-")==0 )){
					pointer = i;
					if(savedCharacterState.isOfChunkType(ChunkType.CHARACTER_STATE)) { //must have character[
						chunkCollector.addChunk(savedCharacterState);
						return pointer;
						//savedCharacterState = savedCharacterState.replaceFirst("^\\]\\s+", "").trim()+"]";
						//return new ChunkSimpleCharacterState("a["+savedCharacterState.trim()+"]");
					} else {
						if(savedCharacterState.getChunkDFS(ChunkType.MODIFIER) != null) {
							unassignedModifier = savedCharacterState.getChunks();
							//???? "{"+savedCharacterState.trim().replaceAll("(m\\[|\\])", "").replaceAll("\\s+", "} {")+"}";
						}
						if(this.pastPointers.contains(i)) {
							pointer = i+1;
						} else {
							this.pastPointers.add(i);
						}
						//if(terminalsText.matches("SG.SG"))
						//	chunkCollector.addChunk(new Chunk(ChunkType.EOS));
						return  pointer;
					}
				} else {
					String character = this.characterKnowledgeBase.getCharacterName(terminalsText);
					if(!foundState && character != null){
						AbstractParseTree characterTerminal = parseTreeFactory.create();
						characterTerminal.setTerminalsText(character);
						Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, characterTerminal);
						LinkedHashSet<Chunk> children = new LinkedHashSet<Chunk>();
						children.add(characterChunk);
						children.add(chunkCollector.getChunk(terminal));
						
						Chunk chunk = new Chunk(ChunkType.CHARACTER_STATE,  children);
						if(savedCharacterState!=null) {
							savedCharacterState.getChunks().add(chunk);
							savedCharacterState.setChunkType(ChunkType.CHARACTER_STATE);
						} else if(savedCharacterState == null) {
							savedCharacterState = chunk;
						} 
						foundState = true;
						if(i + 1 == terminals.size()){ //reach the end of chunkedtokens
							pointer = i + 1;
							chunkCollector.addChunk(savedCharacterState);
							return pointer;
						}
					}else if(foundState && character!=null && savedCharacterState.isOfChunkType(ChunkType.CHARACTER_STATE)) { 
						//coloration coloration: dark blue
						savedCharacterState.getChunks().add(chunkCollector.getChunk(terminal));

					}else if(foundState){
						pointer = i;
						chunkCollector.addChunk(savedCharacterState);
						return pointer;

					}else if(character==null){
						if(posKnowledgeBase.isVerb(terminalsText) && !foundState) {
							//construct ChunkVP or ChunkCHPP
							LinkedHashSet<Chunk> children = new LinkedHashSet<Chunk>();
							children.add(new Chunk(ChunkType.VERB, chunkCollector.getChunk(terminal)));
							//children.add(new Chunk(ChunkType.OBJECT, ???));
							
							Chunk chunk = new Chunk(ChunkType.VP, children);
							
							//chunk.setFunction(terminal);
							if(savedCharacterState==null) {
								savedCharacterState = chunk;
							} else if (savedCharacterState != null) {
								savedCharacterState.getChunks().add(chunk);
							}
							//savedCharacterState = (savedCharacterState.trim().length()>0? savedCharacterState.trim()+"] ": "")+"v[" + terminalsText + " ";
							//continue searching for either a <> or a r[]
							boolean findChunk = false; //find a chunk
							boolean findOrgan = false; //find an organ
							boolean findModifier = false; //find a modifier
							boolean findTerminal = false; //find a text token
							for(int j = i+1; j < terminals.size(); j++) {
								AbstractParseTree lookForwardTerminal = terminals.get(j);
								String lookForwardTerminalText = terminals.get(j).getTerminalsText();
								if(lookForwardTerminalText.length() == 0) 
									continue;
								if(chunkCollector.isPartOfChunkType(lookForwardTerminal, ChunkType.NON_SUBJECT_ORGAN)) {
									//form a vb chunk
									//lookForwardTerminal = lookForwardTerminal.replaceFirst("^u\\[", "").replaceFirst("\\]$", "");
									//String organ = lookForwardTerminal.substring(lookForwardTerminal.indexOf("o[")).trim();
									Chunk nonSubjectOrganChunk = chunkCollector.getChunk(lookForwardTerminal);
									
									LinkedHashSet<Chunk> childChunks = nonSubjectOrganChunk.getChunks();
									List<Chunk> beforeOrgan = new ArrayList<Chunk>();
									Chunk organ = null;
									for(Chunk childChunk : childChunks) {
										if(chunk.isOfChunkType(ChunkType.ORGAN))
											organ = chunk;
										if(organ==null)
											beforeOrgan.add(childChunk);
									}
									
									if(savedCharacterState!=null) {
										for(Chunk beforeOrganChunk : beforeOrgan) {
											String characterFound = characterKnowledgeBase.getCharacterName(beforeOrganChunk.getTerminalsText());
											if(characterFound!=null){
												LinkedHashSet<Chunk> characterStateChildren = new LinkedHashSet<Chunk>();
												AbstractParseTree characterTerminal = parseTreeFactory.create();
												characterTerminal.setTerminalsText(characterFound);
												characterStateChildren.add(new Chunk(ChunkType.CHARACTER_STATE, characterTerminal));
												characterStateChildren.add(beforeOrganChunk);
												savedCharacterState.getChunks().add(new Chunk(ChunkType.CHARACTER_STATE, characterStateChildren));
											}else{
												LinkedHashSet<Chunk> modifierChildren = new LinkedHashSet<Chunk>();
												modifierChildren.add(new Chunk(ChunkType.MODIFIER, beforeOrganChunk));
												savedCharacterState.getChunks().add(new Chunk(ChunkType.MODIFIER, modifierChildren));
											}
										}
										
										Chunk objectChunk = organChunk;
										objectChunk.setChunkType(ChunkType.OBJECT);
										savedCharacterState.getChunks().add(objectChunk);
									} else {
										savedCharacterState = organChunk;
									}
									
									pointer = j + 1;
									chunkCollector.addChunk(savedCharacterState);
									return pointer;
								}
								String lookForwardCharacter =  characterKnowledgeBase.getCharacterName(lookForwardTerminalText);
								if((!findChunk && !findOrgan) && (
										chunkCollector.isPartOfChunkType(lookForwardTerminal, ChunkType.NP_LIST) ||
										chunkCollector.isPartOfChunkType(lookForwardTerminal, ChunkType.TO) ||
										chunkCollector.isPartOfChunkType(lookForwardTerminal, ChunkType.PP))) {
									savedCharacterState.getChunks().add(chunkCollector.getChunk(lookForwardTerminal));
									findChunk = true;
								} else if(!findOrgan && chunkCollector.isPartOfChunkType(lookForwardTerminal, ChunkType.ORGAN)){
									if(savedCharacterState!=null) {
										savedCharacterState.getChunks().add(chunkCollector.getChunk(lookForwardTerminal));
									} else {
										savedCharacterState = new Chunk(ChunkType.ORGAN, lookForwardTerminal);
									}
									findOrgan = true;
								} else if(!findOrgan && !findChunk && lookForwardCharacter!=null){
									AbstractParseTree characterTerminal = parseTreeFactory.create();
									characterTerminal.setTerminalsText(character);
									Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, characterTerminal);

									LinkedHashSet<Chunk> characterStateChildren = new LinkedHashSet<Chunk>();
									characterStateChildren.add(characterChunk);
									characterStateChildren.add(chunkCollector.getChunk(lookForwardTerminal));
									Chunk characterStateChunk = new Chunk(ChunkType.CHARACTER_STATE, characterStateChildren);
									if(savedCharacterState != null) 
										savedCharacterState.getChunks().add(characterStateChunk);
									else 
										savedCharacterState = characterStateChunk;
									//savedCharacterState = (savedCharacterState.trim().length()>0? savedCharacterState.trim()+"] ": "")+lookForwardCharacter+"["+lookForwardTerminal.replaceAll("[{}]", "")+" ";
								} else if(!findOrgan && !findChunk && !findModifier && posKnowledgeBase.isAdverb(lookForwardTerminalText)) {
									if(savedCharacterState != null) {
										savedCharacterState.getChunks().add(new Chunk(ChunkType.MODIFIER, chunkCollector.getChunk(lookForwardTerminal)));
									} else {
										savedCharacterState = new Chunk(ChunkType.MODIFIER, chunkCollector.getChunk(lookForwardTerminal));
									}
									//savedCharacterState = (savedCharacterState.trim().length()>0? savedCharacterState.trim()+"] ": "")
									//		+"m["+lookForwardTerminal.replaceAll("[{}]", "")+" ";
									findModifier = true;
								} else if(!findOrgan && !findChunk && findModifier && posKnowledgeBase.isAdverb(lookForwardTerminalText)) {
									savedCharacterState.getChunks().add(chunkCollector.getChunk(lookForwardTerminal));
									//savedCharacterState += lookForwardTerminal.replaceAll("[{}]", "")+" ";
								} else if(findOrgan && chunkCollector.isPartOfChunkType(lookForwardTerminal, ChunkType.ORGAN)){
									savedCharacterState.getChunks().add(chunkCollector.getChunk(lookForwardTerminal));
									//savedCharacterState += lookForwardTerminal.replace("<", "(").replace(">", ")").replaceAll("[{}]", "")+" ";
								} else if((findOrgan || findChunk) && !chunkCollector.isPartOfChunkType(lookForwardTerminal, ChunkType.ORGAN)){ 
									//must have foundo or foundc
									pointer = j;
									//if(findOrgan){savedCharacterState = savedCharacterState.replaceFirst("^\\]\\s+", "").trim()+"]";}
									if(savedCharacterState.containsChunkType(ChunkType.PREPOSITION)) {
										savedCharacterState.setChunkType(ChunkType.SPECIFIC_PP);
										for(Chunk childrenChunk : savedCharacterState.getChunks()) {
											if(childrenChunk.isOfChunkType(ChunkType.VERB))
												childrenChunk.setChunkType(ChunkType.CHARACTER_STATE);
										}
										chunkCollector.addChunk(savedCharacterState);
										return pointer;
									} else {
										if(savedCharacterState.isOfChunkType(ChunkType.NP_LIST)) {
											savedCharacterState.setChunkType(ChunkType.ORGAN);
										}
										/* TODO
										savedCharacterState = savedCharacterState.replace("l[", "o[");
										if(savedCharacterState.matches(".*?\\bv\\[[^\\[]* m\\[.*")){//v[comprising] m[a] architecture[surrounding] o[(involucre)]
											savedCharacterState = format(savedCharacterState);
											//scs = scs.replaceFirst("\\] o\\[", " ").replaceFirst("\\] m\\[", "] o[");
										}else if(savedCharacterState.matches(".*?\\bv\\[[^\\[]* \\w{2,}\\[.*")){//v[comprising]  architecture[surrounding]
											savedCharacterState = format(savedCharacterState);
											//scs = scs.replaceFirst("\\] o\\[", " ").replaceFirst("\\] \\w{2,}\\[", "] o[");
										}
										return new ChunkVP("b["+savedCharacterState+"]"); 
										*/
										chunkCollector.addChunk(savedCharacterState);
										return pointer;
									}
								} else if(lookForwardTerminalText.matches(".*?\\W.*") || lookForwardTerminalText.matches(
										"\\b(" + prepositionWords + "|or|and)\\b") || lookForwardTerminalText.compareTo("-LRB-/-LRB-")==0){
									if(savedCharacterState.containsChunkType(ChunkType.CHARACTER_STATE)){ //borne {singly
										pointer = j;
										//savedCharacterState = (savedCharacterState.replaceFirst("^\\]", "").trim()+"]").replaceFirst("\\bv\\[[^\\[]*?\\]\\s*", "");
										savedCharacterState.setChunkType(ChunkType.CHARACTER_STATE);
										chunkCollector.addChunk(savedCharacterState);
										return pointer;
										//return new ChunkSimpleCharacterState("a["+savedCharacterState.trim()+"]");
									} else {
										//search failed
										if(pastPointers.contains(i)) {
											pointer = i+1;
										} else {
											pointer = i;
											pastPointers.add(i);
										}
										return pointer;
									}
								}else if(!findTerminal) { //usually v[comprising] m[a {surrounding}] o[involucre]
									if(savedCharacterState != null) {
										savedCharacterState.getChunks().add(chunkCollector.getChunk(lookForwardTerminal));
									} else {
										savedCharacterState.getChunks().add(new Chunk(ChunkType.MODIFIER, chunkCollector.getChunk(lookForwardTerminal)));
									}
									findTerminal = true;
								}else if(findTerminal){
									savedCharacterState.getChunks().add(chunkCollector.getChunk(lookForwardTerminal));
								}
							}
						}else{
							savedCharacterState = null;
							pointer++;
							break;
						}
					}
				}
			}
		}
		if(i == terminals.size()){
			pointer = terminals.size();
		}
		
		return pointer;
	}
	

	private boolean isRightAfterSubject(int i, List<AbstractParseTree> terminals, ChunkCollector chunkCollector) {
		Chunk firstChunk = chunkCollector.getChunk(terminals.get(0));
		if(firstChunk.isOfChunkType(ChunkType.PP))
			return true;
		
		for(int j=0; j<i; j++) {
			AbstractParseTree predecessorTerminal = terminals.get(i);
			Chunk terminalChunk = chunkCollector.getChunk(predecessorTerminal);
			return terminalChunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN);
		}
		return !chunkCollector.getSubjectTag().equals("ignore");
	}

	private String originalNumForm(String token){
		if(token.matches(".*[a-z].*?")){
			return token.replaceAll("-\\s*LRB-/-LRB\\s*-?", "(").replaceAll("-\\s*RRB-/-RRB\\s*-?", ")");
		}else{
			return token.replaceAll("-\\s*LRB-/-LRB\\s*-?", "[").replaceAll("-\\s*RRB-/-RRB\\s*-?", "]");
		}
	}
	
	
	/**
	 * use the un-collapsedTree (this.tree) to check the type of a chunk with
	 * the id,
	 * 
	 * @param i
	 * @return:
	 * 
	 *          SBAR: s VP: b[v/o] PP: r[p/o] VP-PP: t[c/r[p/o]]
	 *          ADJ-PP:t[c/r[p/o]] Than: n To: w NPList: l PPList: i main
	 *          subject: z[m/e] non-subject organ/structure u[m[] relief[] o[]]
	 *          character modifier: a[m[largely] relief[smooth] m[abaxially]]
	 */
	private ChunkType getChunkType(int id, ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		AbstractParseTree terminal = terminals.get(id);
		//log(LogLevel.DEBUG, "terminal " + terminal);
		Chunk terminalChunk = chunkCollector.getChunk(terminal);
		if(terminalChunk.containsChunkType(ChunkType.CHARACTER_STATE)) {
			return ChunkType.CHARACTER_STATE;
			//return "ChunkSL"; //state list
		}
		/*if(token.startsWith("q[")){
			return "ChunkQP";
		}*/
		/*if(token.startsWith("s[")){
			return "ChunkSBAR";
		}*/
		if(terminalChunk.isOfChunkType(ChunkType.VP)) {
			return ChunkType.VP;
			//return "ChunkVP";
		}
		//if(token.startsWith("r[") && token.indexOf("[of]") >= 0){
		//	return "ChunkOf";
		//}
		if(terminalChunk.isOfChunkType(ChunkType.PP)) { 
			//r[p[around] o[10 mm]] should be ChunkValue
			Chunk object = terminalChunk.getChunkDFS(ChunkType.OBJECT);
			if(object != null) {                             
				if(object.getTerminalsText().matches(".*\\(?[0-9+×x°²½/¼*/%-]+\\)?.*(" + units + ")")) {
					LinkedHashSet<Chunk> chunks = terminalChunk.getChunks();
					for(Chunk chunk : chunks) {
						switch(chunk.getChunkType()) {
							case PREPOSITION:
								chunk.setChunkType(ChunkType.MODIFIER);
								break;
							case OBJECT:
							case PP:
								chunk.setChunkType(ChunkType.UNASSIGNED);
								break;
							default:
								break;
						}
					}
					return ChunkType.VALUE;
					} else if(object.getTerminalsText().matches(".* \\(?[0-9+×x°²½/¼*/%-]+\\)?") && !terminalChunk.getTerminalsText().matches(".*[×x]")) { //at 30× is not a value
					LinkedHashSet<Chunk> chunks = terminalChunk.getChunks();
					for(Chunk chunk : chunks) {
						switch(chunk.getChunkType()) {
						case PREPOSITION:
							chunk.setChunkType(ChunkType.MODIFIER);
							break;
						case OBJECT:
						case PP:
							chunk.setChunkType(ChunkType.UNASSIGNED);
							break;
						default:
							break;
						}
					}
					return ChunkType.VALUE;
				} else {
					/* really necessary to do this? */
					/*terminalText = terminalText.replaceAll("r\\[p\\[of\\]\\]", "of");
					terminal.setTerminalsText(terminalText);
					if(terminalText.matches(".*?\\[p\\[\\w+\\] o\\[\\w+ r\\[p\\[.*")){
						Pattern p = Pattern.compile("(.*?\\[p\\[\\w+)(\\] o\\[)(\\w+ )(r\\[p\\[)(.*)");
						Matcher m = p.matcher(terminalText);
						if(m.matches()){
							terminalText = m.group(1)+" "+m.group(3)+m.group(5).replaceFirst("\\]\\]\\s*$", "");
							terminal.setTerminalsText(terminalText);
						}					
					}*/
					return ChunkType.PREPOSITION;
				}
			} else {
				return null;
			}
		} 

		if(terminalChunk.isOfChunkType(ChunkType.SPECIFIC_PP)){
			//this was for FNAv19, but it seemed all t[ chunks were only generated by composeChunk, bypassing this step. t[ chunks generated by chunking does not seem to need this reformatting.
			//reformat c[] in t[]: c: {loosely} {arachnoid} : should be m[loosely] architecture[arachnoid]
			/*Pattern p = Pattern.compile("(.*?\\b)c\\[([^]].*?)\\](.*)");
			Matcher m = p.matcher(token);
			String reformed = "";
			if(m.matches()){
				reformed += m.group(1);
				String c = reformCharacterState(m.group(2));
				reformed += c+ m.group(3);
			}
			this.chunkedtokens.set(id, reformed);*/
			return ChunkType.SPECIFIC_PP;
			//return "ChunkCHPP"; //character/state-pp
		}
		if((terminalChunk.isOfChunkType(ChunkType.THAN_PHRASE) || terminalChunk.isOfChunkType(ChunkType.THAN_CHARACTER_PHRASE)) 
				&& terminalChunk.getTerminals().size()!=0) { 
			//returns three different types of ChunkTHAN
			log(LogLevel.DEBUG, "the " + terminalChunk);
			List<AbstractParseTree> chunksTerminals = terminalChunk.getTerminals();
			int thanIndex = 0;
			for(; thanIndex< chunksTerminals.size(); thanIndex++) { //AbstractParseTree chunksTerminal : chunksTerminals) {
				if(chunksTerminals.get(thanIndex).getTerminalsText().equals("than")) {
					break;
				}
			}
			
			String characterWord = "";
			AbstractParseTree characterWordTerminal = null;
			if(thanIndex>0) {
				characterWordTerminal = chunksTerminals.get(thanIndex - 1);
				characterWord = characterWordTerminal.getTerminalsText();
			}
			String character = null;
			if(!characterWord.matches("("+moreWords+")")){
				character =  characterKnowledgeBase.getCharacterName(characterWord);
			}
			
			//String beforethan = token.substring(0, token.indexOf(" than "));
			//String beforethan = terminalText.substring(0, m.start()).trim();
			//String charword = beforethan.lastIndexOf(' ')>0 ? beforethan.substring(beforethan.lastIndexOf(' ')+1) : beforethan.replaceFirst("n\\[", "");
			// = beforethan.replace(charword, "").trim().replaceFirst("n\\[", "");
			StringBuilder beforeThanBuilder = new StringBuilder();
			for(int j=0; j<thanIndex; j++) {
				beforeThanBuilder.append(chunksTerminals.get(j).getTerminalsText());
			}
			String beforeCharacter = beforeThanBuilder.toString();
			
			StringBuilder afterThanBuilder = new StringBuilder();
			for(int j = thanIndex+1; j<chunksTerminals.size(); j++) {
				afterThanBuilder.append(chunksTerminals.get(j).getTerminalsText());
			}
			String afterThan = afterThanBuilder.toString();
			boolean containsOrganAfterThan = chunkCollector.containsPartOfChunkType(
					chunksTerminals.subList(thanIndex, chunksTerminals.size()-1), ChunkType.ORGAN);
			
			//Case B
			if(afterThan.matches(".*?\\d.*?\\b(" + units + "|long|length|wide|width)\\b.*") || 
					afterThan.matches(".*?\\d\\.\\d.*")){// "n[{longer} than 3 (cm)]" => n[size[{longer} than 3 (cm)]]
				if(character==null) 
					character="size";
				
				AbstractParseTree characterTerminal = this.parseTreeFactory.create();
				characterTerminal.setTerminalsText(character);
				Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, characterTerminal);
				
				terminalChunk.setChunkType(ChunkType.CHARACTER_STATE);
				terminalChunk.getChunks().add(characterChunk);
				
				LinkedHashSet<Chunk> children = new LinkedHashSet<Chunk>();
				children.add(terminalChunk);
				Chunk chunk = new Chunk(ChunkType.THAN_PHRASE, children);
				
				chunkCollector.addChunk(chunk);
				
				return ChunkType.THAN_PHRASE;
			} else if(afterThan.matches(".*?\\d.*")){// "n[{longer} than 3 (cm)]" => n[size[{longer} than 3 (cm)]]
				if(character==null) 
					character="count";
				
				AbstractParseTree characterTerminal = this.parseTreeFactory.create();
				characterTerminal.setTerminalsText(character);
				Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, characterTerminal);
				
				terminalChunk.setChunkType(ChunkType.CHARACTER_STATE);
				terminalChunk.getChunks().add(characterChunk);
				
				LinkedHashSet<Chunk> children = new LinkedHashSet<Chunk>();
				children.add(terminalChunk);
				Chunk chunk = new Chunk(ChunkType.THAN_PHRASE, children);
				
				chunkCollector.addChunk(chunk);
								
				return ChunkType.THAN_PHRASE;
			}//Case C
			else if(containsOrganAfterThan){ //contains organ
				if(character==null) {//is a constraint, lobed n[more than...]
					character = "constraint";
					
					AbstractParseTree characterTerminal = this.parseTreeFactory.create();
					characterTerminal.setTerminalsText(character);
					Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, characterTerminal);
					
					terminalChunk.setChunkType(ChunkType.CHARACTER_STATE);
					terminalChunk.getChunks().add(characterChunk);
					
					LinkedHashSet<Chunk> children = new LinkedHashSet<Chunk>();
					children.add(terminalChunk);
					Chunk chunk = new Chunk(ChunkType.THAN_PHRASE, children);
					
					chunkCollector.addChunk(chunk);
					
					return ChunkType.THAN_PHRASE;
				}else{//n[more deeply lobed than...
					LinkedHashSet<Chunk> modifierChildren = new LinkedHashSet<Chunk>();
					modifierChildren.addAll(chunksTerminals.subList(0, thanIndex-1));
					Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, modifierChildren);
					
					AbstractParseTree characterTerminal = this.parseTreeFactory.create();
					characterTerminal.setTerminalsText(character);
					Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, characterTerminal);
					LinkedHashSet<Chunk> characterStateChildren = new LinkedHashSet<Chunk>();
					characterStateChildren.add(characterChunk);
					if(characterWordTerminal!=null)
						characterStateChildren.add(characterWordTerminal);
					Chunk characterStateChunk = new Chunk(ChunkType.CHARACTER_STATE, characterStateChildren);
					
					LinkedHashSet<Chunk> constraintChildren = new LinkedHashSet<Chunk>();
					modifierChildren.addAll(chunksTerminals.subList(thanIndex+1, chunksTerminals.size()-1));
					Chunk constraintChunk = new Chunk(ChunkType.CONSTRAINT, constraintChildren);
					
					LinkedHashSet<Chunk> children = new LinkedHashSet<Chunk>();
					children.add(modifierChunk);
					children.add(characterStateChunk);
					children.add(constraintChunk);
					Chunk chunk = new Chunk(ChunkType.THAN_PHRASE, children);
					
					chunkCollector.addChunk(chunk);
					return ChunkType.THAN_PHRASE;
				}
			}//Case A n[wider than long]
			else {
				
				AbstractParseTree characterTerminal = this.parseTreeFactory.create();
				characterTerminal.setTerminalsText(character);
				Chunk characterChunk = new Chunk(ChunkType.CHARACTER_STATE, characterTerminal);
				
				terminalChunk.setChunkType(ChunkType.CHARACTER_STATE);
				terminalChunk.getChunks().add(characterChunk);
				
				LinkedHashSet<Chunk> children = new LinkedHashSet<Chunk>();
				children.add(terminalChunk);
				Chunk chunk = new Chunk(ChunkType.THAN_PHRASE, children);
				
				chunkCollector.addChunk(chunk);
				
				return ChunkType.THAN_CHARACTER_PHRASE;
			}
		}
		if(terminalChunk.isOfChunkType(ChunkType.TO)){//w[{proximal} to the (florets)] ; or w[to (midvine)]
			//reformat it to CHPP
			List<AbstractParseTree> chunkTerminals = terminalChunk.getTerminals();
			if(chunkTerminals.get(0).getTerminalsText().equals("to")) {
				
				LinkedHashSet<Chunk> prepositionChildChunks = new LinkedHashSet<Chunk>();
				prepositionChildChunks.add(chunkTerminals.get(0));
				Chunk prepositionChunk = new Chunk(ChunkType.PREPOSITION, prepositionChildChunks);
				
				LinkedHashSet<Chunk> objectChildChunks = new LinkedHashSet<Chunk>();
				objectChildChunks.addAll(chunkTerminals.subList(1, chunkTerminals.size()-1));
				Chunk objectChunk = new Chunk(ChunkType.OBJECT, objectChildChunks);
				
				LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
				childChunks.add(prepositionChunk);
				childChunks.add(objectChunk);
				Chunk ppChunk = new Chunk(ChunkType.PP, childChunks);
				
				chunkCollector.addChunk(ppChunk);
				return ChunkType.PREPOSITION;
			} else {
				terminalChunk.setChunkType(ChunkType.SPECIFIC_PP);
				
				for(int i=0; i<chunkTerminals.size(); i++) { //AbstractParseTree chunkTerminal : chunkTerminals) {
					if(chunkTerminals.get(i).getTerminalsText().equals("to")) {
						LinkedHashSet<Chunk> prepositionChildChunks = new LinkedHashSet<Chunk>();
						prepositionChildChunks.add(chunkTerminals.get(i));
						Chunk prepositionChunk = new Chunk(ChunkType.PREPOSITION, prepositionChildChunks);
						
						LinkedHashSet<Chunk> objectChildChunks = new LinkedHashSet<Chunk>();
						objectChildChunks.addAll(chunkTerminals.subList(i+1, chunkTerminals.size()-1));
						Chunk objectChunk = new Chunk(ChunkType.OBJECT, objectChildChunks);
						
						LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
						childChunks.add(prepositionChunk);
						childChunks.add(objectChunk);
						Chunk ppChunk = new Chunk(ChunkType.PP, childChunks);
						
						LinkedHashSet<Chunk> newChildChunks = new LinkedHashSet<Chunk>();
						for(Chunk childChunk : terminalChunk.getChunks()) {
							if(childChunk.getTerminals().contains(chunkTerminals.get(i))) {
								break;
							} else {
								newChildChunks.add(childChunk);
							}
						}
						newChildChunks.add(ppChunk);
						terminalChunk.setChunks(newChildChunks);
						break;
					}
				}
				chunkCollector.addChunk(terminalChunk);
				return ChunkType.SPECIFIC_PP;
			}
		}
		if(terminalChunk.isOfChunkType(ChunkType.NP_LIST)) {
			return ChunkType.NP_LIST;
		}
		if(terminalChunk.isOfChunkType(ChunkType.PP_LIST)) {
			return ChunkType.PP_LIST;
		}
		if(terminalChunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN)){
			return ChunkType.MAIN_SUBJECT_ORGAN;
		}
		if(terminalChunk.isOfChunkType(ChunkType.NON_SUBJECT_ORGAN)) {
			return ChunkType.NON_SUBJECT_ORGAN;
		}
		return null;
	}
	
	
	/**
	 * m[usually] v[comprising] m[a] architecture[surrounding] o[(involucre)]
	 * 
	 * m[usually] v[comprising] o[1 architecture[surrounding] (involucre)]
	 */
	private String format(String scs) {
		String first = scs.substring(0, scs.indexOf("v["));
		String rest = scs.replace(first, "");
		String v = rest.substring(0, rest.indexOf(']')+1+0);
		String o = rest.replace(v, "").trim(); //m[a] architecture[surrounding] o[(involucre)]
		String newo = "o[";
		do{
			String t = o.indexOf(' ')>=0? o.substring(0, o.indexOf(' ')) : o;
			//o = o.replaceFirst(t.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]").replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"),"").trim();
			o = o.replaceFirst(t.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]").replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}"),"").trim(); //to replace {string}
			if(t.startsWith("m[")){
				t = t.replaceAll("(m\\[|\\])", "").trim();
				if(t.compareTo("a") == 0 && !o.matches("(couple|few)")){
					t = "1";
				}
			}
			if(t.startsWith("o[")){
				t=t.replaceAll("(o\\[|\\])", "").trim();
			}
			newo+=t+" ";
			
		}while(o.length()>0);
		return first+v+" "+newo.trim()+"]";
	}


}
