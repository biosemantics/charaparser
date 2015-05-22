package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;




import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.lib.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.POS;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * OtherINsChunker chunks IN tags of the parse tree not handled by another chunker 
 * @author rodenhausen
 */
public class OtherINsChunker extends AbstractChunker {

	private IPOSKnowledgeBase posKnowledgeBase;
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
	 * @param posKnowledgeBase
	 */
	@Inject
	public OtherINsChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			@Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase, 
			 ICharacterKnowledgeBase characterKnowledgeBase, ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, terminologyLearner, 
				inflector,  learnedCharacterKnowledgeBase);
		this.posKnowledgeBase = posKnowledgeBase;
		this.characterKnowledgeBase = characterKnowledgeBase;
	}

	/**
	 * only for PP_Checked not VP_Checked. RecoverVP will take care of verbs
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		String prepositionWords = this.prepositionWords.replaceFirst("\\bthan\\|", "").replaceFirst("\\bto\\|", "");
		
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.COMPARATIVE_VALUE)) //COMPARATIVE_VALUE may constains PPs such as of, than, as-long-as, but they don't belong to PPChunks
				continue;
			
			boolean isFunctionOfPPWithoutOrgan = isFunctionOfPPWithoutOrgan(terminal, chunkCollector);
			if(isFunctionOfPPWithoutOrgan) {
				Chunk ppChunk = chunkCollector.getChunk(terminal);
				Chunk objectChunk = ppChunk.getChildChunk(ChunkType.OBJECT);
				if(objectChunk != null && posKnowledgeBase.isNoun(objectChunk.getTerminalsText()) && objectChunk.getTerminalsText().endsWith("ing")) {
					Chunk modifierChunk = new Chunk(ChunkType.MODIFIER);
					LinkedHashSet<Chunk> chunks = new LinkedHashSet<Chunk>();
					chunks.addAll(ppChunk.getTerminals());
					modifierChunk.setChunks(chunks);
					chunkCollector.addChunk(modifierChunk);
					continue;
				}
			}
			
			
			boolean isFunctionOfPPPhraseWithoutOrgan = isFunctionOfPPPhraseWithoutOrgan(terminal, chunkCollector);
			boolean isRangePreposition = isRangePrepositionPhrase(terminal, chunkCollector);
			
			if((isFunctionOfPPPhraseWithoutOrgan || 
					(!chunkCollector.isPartOfANonTerminalChunk(terminal) && terminal.getTerminalsText().matches(prepositionWords)))
					&& !isRangePreposition) { 
				//[of] ...onto]]
				// a prep is identified, needs normalization
				
				//ChunkCollector copy = chunkCollector.;
				//HashMap<IParseTree, Chunk> copy = chunkCollector.getChunks();
				//List<Chunk> ctcopy = null;
				String npcopy = null;
				String np = "";
				boolean foundStructureRef = false;
				boolean foundCharacter = false;
				
				boolean startNoun = false;
				boolean foundOrgan = false;

				boolean npCopy = false;
				//LinkedHashSet<Chunk> organTerminals = new LinkedHashSet<Chunk>(); //aka np
				int j = i + 1;
				int memo = -1; //for finding compound nouns
				int stop = -1; //index of the first soft stop
				for(; j<terminals.size(); j++) {
					AbstractParseTree lookAheadTerminal = terminals.get(j);
					if(j==i+1 && lookAheadTerminal.getTerminalsText().matches("[,;\\.]")){
						//"smooth throughout, ", but what about "smooth throughout OR hairy basally"?
						break;
					}
					
					if(stop < 0 && 
							(lookAheadTerminal.getTerminalsText().matches("\\.|;|,|and|or")||
									lookAheadTerminal.isOfChunkType(ChunkType.PP)||lookAheadTerminal.isOfChunkType(ChunkType.VP)
							)
					  ){
						stop = j;
					}
					
					if(!foundOrgan && startNoun && 
							!chunkCollector.isPartOfChunkType(lookAheadTerminal, ChunkType.ORGAN)
							&& !posKnowledgeBase.isNoun(lookAheadTerminal.getTerminalsText())) {
						//npCopy = true;

						/*
						 //test whole t, not the last word once a noun has been found
						//npcopy = npcopy == null? np : npcopy;
						//ctcopy = ctcopy == null? (ArrayList<String>)this.chunkedtokens.clone():ctcopy;
						 */
						npCopy = true;
						npcopy = npcopy == null? np : npcopy;
						memo = memo <0? j : memo; //memorize the last index of terminals that will be included in organTerminals
					}
							
					if(startNoun && !foundOrgan && isHardStop(terminals, j, chunkCollector)){
						//hard stop encountered, break
						
						//break;
						/*
						 np = npcopy == null? np : npcopy;
						this.chunkedtokens = ctcopy;
						break; 
						 */
						np = npcopy == null? np : npcopy;
						j = memo;
						break;
					}
					
					//if(ishardstop(j)) break; //this should be after "startn && !foundorgan && ishardstop(j)"
					if(isHardStop(terminals, j, chunkCollector)) break; //this should be after "startn && !foundorgan && ishardstop(j)"
					
					if(foundOrgan && !chunkCollector.isPartOfChunkType(lookAheadTerminal, ChunkType.ORGAN)) { 
						break; //break, the end of the search is reached, found organ as object
					}
					
					/*
					if(foundposition && (t.compareTo(",")==0 || ishardstop(j))){
						foundorgan = true;
						break;
					}
	
					if(foundcharacter && (t.compareTo(",")==0 || ishardstop(j))){
						foundorgan = true;
						break;
					}
					 */
					
					if(foundStructureRef && (lookAheadTerminal.getTerminalsText().matches(",|and|or|but") || isHardStop(terminals, j, chunkCollector))){
						//foundOrgan = true;
						break;
					}
	
					if(foundCharacter && (lookAheadTerminal.getTerminalsText().matches(",|and|or|but") ||  isHardStop(terminals, j, chunkCollector))){
						foundOrgan = true;
						break;
					}
					
					np +=lookAheadTerminal.getTerminalsText()+" "; //any word in between
					
					//any word in between
					//do this after finding the "j" (the index terminating the NP)
					/*Chunk lookAheadChunk = chunkCollector.getChunk(lookAheadTerminal);
					if(lookAheadChunk.equals(chunkCollector.getChunk(terminal))) {
						List<Chunk> lookAheadChunks = lookAheadChunk.getChunksWithoutTerminal(terminal);
						for(Chunk chunk : lookAheadChunks) {
							if(chunk.isOfChunkType(ChunkType.OBJECT))
								organTerminals.addAll(chunk.getChunks());
							else
								organTerminals.add(chunk);
						}
					} else {
						Chunk chunk = chunkCollector.getChunk(lookAheadTerminal);
						if(!chunk.isOfChunkType(ChunkType.END_OF_LINE) && !chunk.isOfChunkType(ChunkType.END_OF_SUBCLAUSE))
							organTerminals.add(chunk); 
					}*/
					
					
					
					if(chunkCollector.isPartOfChunkType(lookAheadTerminal, ChunkType.ORGAN)) { //t may have []<>{}
						startNoun = true; //not break yet, may be the next token is also a noun
						foundOrgan = true;
					}
					
					/*
					 String[] charinfo = Utilities.lookupCharacter(t, conn, characterhash, this.glosstable, this.tableprefix);
					if(!foundposition && charinfo!=null && charinfo[0].contains("position")){
						foundposition = true;
					}

					if(!foundcharacter && charinfo!=null && charinfo[0].contains("character")){ //in diameter
						foundcharacter = true;
					}
					
					if(!foundorgan && Utilities.isNoun(t, nouns, notnouns)){ //t may have []<>{}
						startn = true; //won't affect the value of foundorgan, after foundorgan is true, "plus" problem
						if(Utilities.isPlural(t, MainForm.conn)){
							foundorgan = true;
							np = np.trim();
							if(np.lastIndexOf(" ")>0){
								np = np.substring(0, np.lastIndexOf(" "))+" "+ "("+t.replaceAll("\\W", "")+") ";
							}else{
								np = "("+np.replaceAll("\\W", "")+") ";
							}
						}
					}
					 */
					
					
					String character = characterKnowledgeBase.getCharacterName(lookAheadTerminal.getTerminalsText()).getCategories();
					//if(!foundStructureRef && character!=null && character.contains("position")){
					if(!foundStructureRef && character!=null && character.matches(".*?(^|_)("+ElementRelationGroup.entityRefElements+")(_|$).*")){
						foundStructureRef = true;
					}

					if(!foundCharacter && character!=null && character.contains("character")){ //in diameter
						foundCharacter = true;
					}
					
					if(!foundOrgan && posKnowledgeBase.isNoun(lookAheadTerminal.getTerminalsText())){ 
						//t may have []<>{}
						startNoun = true; 
						//won't affect the value of foundorgan, after foundorgan is true, "plus" problem
						if(inflector.isPlural(lookAheadTerminal.getTerminalsText())){
							foundOrgan = true;
							/*
							 	np = np.trim();
							if(np.lastIndexOf(" ")>0){
								np = np.substring(0, np.lastIndexOf(" "))+" "+ "("+t.replaceAll("\\W", "")+") ";
							}else{
								np = "("+np.replaceAll("\\W", "")+") ";
							}*/
						}
					}
				}
				
				boolean ppChunkFormed = false;
				//form a PP chunk to include terminals from index i to before j
				if(foundOrgan || npCopy ||foundStructureRef){
					LinkedHashSet<Chunk> function = new LinkedHashSet<Chunk>();
					function.add(terminal); //add IN
					
					//organTerminals = terminals from index i to before j
					LinkedHashSet<Chunk> organTerminals = new LinkedHashSet<Chunk>(); //aka np
		
					for(int k=i+1; k<j; k++) {
						AbstractParseTree lookAheadTerminal = terminals.get(k);
						Chunk lookAheadChunk = chunkCollector.getChunk(lookAheadTerminal);
						if(lookAheadChunk.equals(chunkCollector.getChunk(terminal))) {
							List<Chunk> lookAheadChunks = lookAheadChunk.getChunksWithoutTerminal(terminal);
							for(Chunk chunk : lookAheadChunks) {
								if(chunk.isOfChunkType(ChunkType.OBJECT))
									organTerminals.addAll(chunk.getChunks());
								else
									organTerminals.add(chunk);
							}
						} else {
							Chunk chunk = chunkCollector.getChunk(lookAheadTerminal);
							if(foundStructureRef && k==j-1){
								//turn position character chunk to an organ chunk by replace the chunk of lookAheadTerminal with a new organ chunk
								chunk = new Chunk(ChunkType.ORGAN, lookAheadTerminal);
								chunkCollector.addChunk(chunk);
								
							}
							if(!chunk.isOfChunkType(ChunkType.END_OF_LINE) && !chunk.isOfChunkType(ChunkType.END_OF_SUBCLAUSE))
								organTerminals.add(chunk); 
						}
					}
					
					//merge two PPs where the first PP does not have a object
					//check:
					Chunk tempObjectChunk = new Chunk(ChunkType.OBJECT, organTerminals);
					boolean foundOtherPP = tempObjectChunk.containsChildOfChunkType(ChunkType.PP);
					Chunk otherPP = tempObjectChunk.getChildChunk(ChunkType.PP);
					if(foundOtherPP) {
						boolean punctuation = false;
						for(Chunk organTerminal : organTerminals) {
							if(organTerminal.equals(otherPP))
								break;
							if(organTerminal.containsChunkType(ChunkType.COMMA) || 
									organTerminal.containsChunkType(ChunkType.END_OF_LINE) ||
									organTerminal.containsChunkType(ChunkType.END_OF_SUBCLAUSE)) {
								punctuation = true;
								break;
							}
						}
						
						if(!punctuation) {
							for(Chunk organTerminal : organTerminals) {
								if(organTerminal.equals(otherPP)) {
									function.add(organTerminal.getChildChunk(ChunkType.PREPOSITION));
									break;
								}
								function.add(organTerminal);
							}
						}
						
						organTerminals.removeAll(function);
						organTerminals.remove(otherPP);
						LinkedHashSet<Chunk> newOrganTerminals = new LinkedHashSet<Chunk>();
						newOrganTerminals.add(otherPP.getChildChunk(ChunkType.OBJECT));
						newOrganTerminals.addAll(organTerminals);
						organTerminals = newOrganTerminals;
					}
					//end merge
					
					//form and add PP chunk to chunkCollector
					Chunk functionChunk = new Chunk(ChunkType.PREPOSITION, function);
					Chunk objectChunk = new Chunk(ChunkType.OBJECT, organTerminals);

					LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
					childChunks.add(functionChunk);
					childChunks.add(objectChunk);
					Chunk ppChunk = new Chunk(ChunkType.PP, childChunks);
					chunkCollector.addChunk(ppChunk);
					ppChunkFormed = true;
					
					/*if(chunkCollector.isPartOfAChunk(terminal)) {
						Chunk organChunk = new Chunk(ChunkType.ChunkOrgan, organTerminals);
						chunkCollector.addChunk(organChunk);
					} else { //without [], one word per token
						List<IParseTree> function = new ArrayList<IParseTree>();
						function.add(terminal);
						Chunk ppChunk = new PPChunk(ChunkType.ChunkPP, function, organTerminals); 
					}*/
				}else if(j-i != 1){
					//if np =~ ^or and the next token is a prep chunk, then merge np and the chunk: r[i[throughout or only in] o[ultimate branches]]
					if(np!=null && np.startsWith("or ") && terminals.size()>j && terminals.get(j).isOfChunkType(ChunkType.PP)){
						AbstractParseTree nextoken = terminals.get(j);
						//concat two pps
						LinkedHashSet<Chunk> pps = new LinkedHashSet<Chunk>();
						pps.add(terminal);
						pps.add(nextoken.getChildChunk(ChunkType.PREPOSITION));
						//new components for merged chunk
						LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
						childChunks.add(new Chunk(ChunkType.PREPOSITION, pps));
						childChunks.add(nextoken.getChildChunk(ChunkType.OBJECT));
						//construct merged chunk
						Chunk merged = new Chunk(ChunkType.PP, childChunks);
						chunkCollector.addChunk(merged);
						ppChunkFormed = true;
						/*token ="r[p["+token+" "+np.trim()+" "+nextoken.replaceFirst("^r\\[\\w\\[", "");
						this.chunkedtokens.set(i,  token);
						for(int k = i+1; k<=j; k++){
							this.chunkedtokens.set(k, "");
						}*/
					}else if(np!=null && stop - i != 1){
						//collect from i to stop
						LinkedHashSet<Chunk> organTerminals = new LinkedHashSet<Chunk>(); //aka np
						int k = i+1;
						for(k=i+1; k<stop; k++) {
							AbstractParseTree lookAheadTerminal = terminals.get(k);
							Chunk lookAheadChunk = chunkCollector.getChunk(lookAheadTerminal);
							if(lookAheadChunk.equals(chunkCollector.getChunk(terminal))) {
								List<Chunk> lookAheadChunks = lookAheadChunk.getChunksWithoutTerminal(terminal);
								for(Chunk chunk : lookAheadChunks) {
									if(chunk.isOfChunkType(ChunkType.OBJECT))
										organTerminals.addAll(chunk.getChunks());
									else
										organTerminals.add(chunk);
								}
							} else {
								Chunk chunk = chunkCollector.getChunk(lookAheadTerminal);
								if(!chunk.isOfChunkType(ChunkType.END_OF_LINE) && !chunk.isOfChunkType(ChunkType.END_OF_SUBCLAUSE))
									organTerminals.add(chunk); 
							}
						}
						
						//if next token is r[p[ too, join the pp
						if(!startNoun && terminals.size()>k && terminals.get(k).isOfChunkType(ChunkType.PP)){//join
							AbstractParseTree nextoken = terminals.get(k);
							//concat two pps
							LinkedHashSet<Chunk> pps = new LinkedHashSet<Chunk>();
							pps.addAll(organTerminals);
							pps.add(nextoken.getChildChunk(ChunkType.PREPOSITION));
							//new components for merged chunk
							LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
							childChunks.add(new Chunk(ChunkType.PREPOSITION, pps));
							childChunks.add(nextoken.getChildChunk(ChunkType.OBJECT));
							//construct merged chunk
							Chunk merged = new Chunk(ChunkType.PP, childChunks);
							chunkCollector.addChunk(merged);	
							ppChunkFormed = true;
							
						
							/*token = token.replaceAll("(\\w\\[|\\])", "");
							token = chunkedtokens.get(k).replaceFirst("r\\[p\\[", "r[p["+token+" ");
							this.chunkedtokens.set(k, token);
							this.chunkedtokens.set(i, "");*/
						}else{
							//this.chunkedtokens.set(i, token);
							LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
							childChunks.add(new Chunk(ChunkType.PREPOSITION, terminal));
							childChunks.add(new Chunk(ChunkType.OBJECT, organTerminals));
							chunkCollector.addChunk(new Chunk(ChunkType.PP, childChunks));	
							ppChunkFormed = true;
						}
						
						/*if(this.printNorm){
							System.out.println("!default normalized to (.|;|,|and|or|PP)!: "+token);
						}*/
	
						
					/*
					 					if(j-i==1){
						//cancel the normalization attempt on this prep, return to the original chunkedtokens
						this.chunkedtokens = copy;
					}else{//reached the end of the sentence or hit a hardstop. This is the case for "plumose on distal 80 % ."? or "throughout or only r[p[in] o[ultimate branches]]"
						this.chunkedtokens = copy;
						//if np =~ ^or and the next token is a prep chunk, then merge np and the chunk: r[i[throughout or only in] o[ultimate branches]]
						String nextoken = this.chunkedtokens.get(j);
						if(np!=null && np.startsWith("or ") && nextoken.startsWith("r[")){
							token ="r[p["+token+" "+np.trim()+" "+nextoken.replaceFirst("^r\\[\\w\\[", "");
							this.chunkedtokens.set(i,  token);
							for(int k = i+1; k<=j; k++){
								this.chunkedtokens.set(k, "");
							}
						}else if(np!=null){
							//np = np.replaceAll("\\s+", " ").trim();
							String head = token.replaceFirst("\\]+$", "").trim();//assuming token is like r[p[in]]
							String brackets = token.replace(head, "").replaceFirst("\\]$", "").trim();
							String rest = np.replaceFirst(".*?(?=(\\.|;|,|\\band\\b|\\bor\\b|\\w\\[))", "").trim();
							if(!rest.equals(np.trim())) np = np.replace(rest, ""); //perserve spaces for later
							String object = np.replaceAll("\\s+", " ").trim();
							if(object.length()>0){
								token = head + "] o["+np.replaceAll("\\s+", " ").trim()+"]"+brackets; //token = r[p[on] o[{proximal} 2/3-3/4]]: <leaves> on {proximal} 2/3-3/4
								if(!token.startsWith("r[")) token = "r[p["+token+"]";
								//if next token is r[p[ too, join the pp
								int npsize = np.split("\\s").length; //split on single space to perserve correct count of tokens
								int k = i+1;
								for(; k<=i+npsize; k++){
									this.chunkedtokens.set(k, "");
								}
								while(this.chunkedtokens.get(k).length()==0)k++;
								if(!startn && this.chunkedtokens.get(k).startsWith("r[p[")){//join
									token = token.replaceAll("(\\w\\[|\\])", "");
									token = chunkedtokens.get(k).replaceFirst("r\\[p\\[", "r[p["+token+" ");
									this.chunkedtokens.set(k, token);
									this.chunkedtokens.set(i, "");
								}else{
									this.chunkedtokens.set(i, token);
								}
								if(this.printNorm){
									System.out.println("!default normalized to (.|;|,|and|or|r[)!: "+token);
								}
								count++;
							}
						} 
					 */
					
					
					//if(j - i != 1){
						//cancel the normalization attempt on this prep, return to the original chunkedtokens
						//this.chunkedtokens = copy;
					//}else{//reached the end of the sentence.This is the case for "plumose on distal 80 % ."?
						//this.chunkedtokens = copy;
						//np = np.replaceAll("\\s+", " ").trim();
						
						/* String head = token.replaceFirst("\\]+$", "").trim(); 
						String brackets = token.replace(head, "").replaceFirst("\\]$", "").trim();
						String rest = np.replaceFirst(".*?(?=(\\.|;|,|\\band\\b|\\bor\\b|\\w\\[))", "").trim();
						np = np.replace(rest, ""); //perserve spaces for later
						String object = np.replaceAll("\\s+", " ").trim();
						if(object.length()>0){
							token = head + "] o["+np.replaceAll("\\s+", " ").trim()+"]"+brackets;
							this.chunkedtokens.set(i, token);
							int npsize = np.split("\\s").length; //split on single space to perserve correct count of tokens
							for(int k = i+1; k<=i+npsize; k++){
								this.chunkedtokens.set(k, "");
							}
						} */
						/*LinkedHashSet<Chunk> function = new LinkedHashSet<Chunk>();
						function.add(chunkCollector.getChunk(terminal));
						Chunk functionChunk = new Chunk(ChunkType.PREPOSITION, function);
						Chunk objectChunk = new Chunk(ChunkType.OBJECT, organTerminals);
						
						LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
						childChunks.add(functionChunk);
						childChunks.add(objectChunk);
						Chunk ppChunk = new Chunk(ChunkType.PP, childChunks);
						chunkCollector.addChunk(ppChunk);*/
						//chunkCollector.addChunk(new Chunk(ChunkType.ChunkOrgan, organTerminals));
					//}
				}
			}
			if(ppChunkFormed && terminal.getTerminalsText().compareTo("of")==0){
				charaPP(chunkCollector, terminal);
			}
		}
		}
	}




	private boolean isRangePrepositionPhrase(AbstractParseTree terminal, ChunkCollector chunkCollector) {
		Chunk chunk = chunkCollector.getChunk(terminal);
		
		if(terminal.getTerminalsText().equals("to")) {
			boolean foundCount = false;
			for(AbstractParseTree chunkTerminal : chunk.getTerminals()) {
				if(chunkCollector.isPartOfChunkType(chunkTerminal, ChunkType.COUNT) || 
						chunkCollector.isPartOfChunkType(chunkTerminal, ChunkType.VALUE))
					foundCount = true;
				//e.g. to 1 cm think, to 1 cm in width
				if(foundCount && (chunkCollector.isPartOfChunkType(chunkTerminal, ChunkType.STATE) || 
						(chunkCollector.isPartOfChunkType(chunkTerminal, ChunkType.CHARACTER_NAME)))) 
					return true;
			}
		}
		return false;
	}

	private boolean isFunctionOfPPPhraseWithoutOrgan(
			AbstractParseTree terminal, ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		return ((terminal.getAncestor(2, parseTree).getPOS().equals(POS.PP_CHECKED) || 
				terminal.getAncestor(2,  parseTree).getPOS().equals(POS.COLLAPSED_PPIN))
			&& (terminal.getParent(parseTree).getPOS().equals(POS.PREPOSITION) || 
					terminal.getParent(parseTree).getPOS().equals(POS.IN))) && 
					!chunkCollector.getChunk(terminal).containsChunkType(ChunkType.ORGAN);
	}

	private boolean isFunctionOfPPWithoutOrgan(AbstractParseTree terminal,
			ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		return (terminal.getAncestor(2,  parseTree).getPOS().equals(POS.COLLAPSED_PPIN))
			&& (terminal.getParent(parseTree).getPOS().equals(POS.PREPOSITION)) && 
					!chunkCollector.getChunk(terminal).containsChunkType(ChunkType.ORGAN);
	}

	private boolean isHardStop(List<AbstractParseTree> terminals, int j, ChunkCollector chunkCollector) {
		AbstractParseTree terminal = terminals.get(j);
		
		if(terminal.getTerminalsText().matches("-[RL][SR]B-")){
			return true;
		}
		if(terminal.getTerminalsText().matches("when|where|that|which")){
			return true;
		}
		
		if(chunkCollector.isPartOfANonTerminalChunk(terminal)) { //if terminal_j starts a chunk, return true
			Chunk nonTerminalChunk = chunkCollector.getChunk(terminal);
            if(nonTerminalChunk.getTerminals().indexOf(terminal)==0 && //if terminal_j starts a chunk
            		!nonTerminalChunk.isOfChunkType(ChunkType.COUNT) &&  //these chunks could be a component in a PP chunk, is this complete?
					!nonTerminalChunk.isOfChunkType(ChunkType.CONSTRAINT) &&
					!nonTerminalChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && 
					!nonTerminalChunk.isOfChunkType(ChunkType.COMMA) && 
					!nonTerminalChunk.isOfChunkType(ChunkType.UNASSIGNED) && 
					!nonTerminalChunk.isOfChunkType(ChunkType.ORGAN))	
				return true;
		}
		if(terminal.getTerminalsText().startsWith(".") || terminal.getTerminalsText().startsWith(";")){
			return true;
		}
		
		if(terminals.size()==j+1){
			return true;
		}

		AbstractParseTree nextTerminal = terminals.get(j+1);
		if(terminal.getTerminalsText().startsWith(",") && 
				chunkCollector.getChunk(nextTerminal).isOfChunkType(ChunkType.ORGAN)) { //("^\\W*[<(].*")){ // the start of a organ, [ argument? 
																		// or (??
			return true;
		}
		return false;
		
		
		/*
		 		String t1 = this.chunkedtokens.get(j).trim();
		
		if(t1.equals("-RRB-/-RRB-") ||t1.equals("-LRB-/-LRB-") ||t1.equals("-RSB-/-RSB-") ||t1.equals("-LSB-/-RSB-") ){
			return true;
		}
		
		if(t1.matches("^\\w\\[.*")){
			return true;
		}
		if(t1.startsWith(".") || t1.startsWith(";")){
			return true;
		}
		
		if(this.chunkedtokens.size()==j+1){
			return true;
		}

		String t2 = this.chunkedtokens.get(j+1).trim();
		if(t1.startsWith(",") && t2.matches("^\\W*[<(].*")){
			return true;
		}
		return false;		  */
	}
	
}
