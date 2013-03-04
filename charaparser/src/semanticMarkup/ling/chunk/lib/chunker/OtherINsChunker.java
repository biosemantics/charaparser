package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTree;
import semanticMarkup.ling.parse.ParseTreeFactory;
import semanticMarkup.ling.pos.POS;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class OtherINsChunker extends AbstractChunker {

	private IPOSKnowledgeBase posKnowledgeBase;
	
	@Inject
	public OtherINsChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			@Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, terminologyLearner, inflector);
		this.posKnowledgeBase = posKnowledgeBase;
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
			if(isFunctionOfPPPhraseWithoutOrgan || (!chunkCollector.isPartOfANonTerminalChunk(terminal) && terminal.getTerminalsText().matches(prepositionWords))) { 
				//[of] ...onto]]
				// a prep is identified, needs normalization
				boolean startNoun = false;
				boolean foundOrgan = false;
				boolean npCopy = false;
				LinkedHashSet<Chunk> organTerminals = new LinkedHashSet<Chunk>(); //aka np
				int j = i + 1;
				for(; j<terminals.size(); j++) {
					AbstractParseTree lookAheadTerminal = terminals.get(j);
					if(j==i+1 && lookAheadTerminal.getTerminalsText().matches("[,;\\.]")){
						//"smooth throughout, ", but what about "smooth throughout OR hairy basally"?
						break;
					}
					
					if(!foundOrgan && startNoun && 
							!chunkCollector.isPartOfChunkType(lookAheadTerminal, ChunkType.ORGAN)
							&& !posKnowledgeBase.isNoun(lookAheadTerminal.getTerminalsText())) {
						npCopy = true;
					}
							
					if(startNoun && !foundOrgan && isHardStop(terminals, j, chunkCollector)){
						//hard stop encountered, break
						break;
					}
					
					if(foundOrgan && !chunkCollector.isPartOfChunkType(lookAheadTerminal, ChunkType.ORGAN)) { 
						break; //break, the end of the search is reached, found organ as object
					}
					
					//any word in betweens
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
					
					if(chunkCollector.isPartOfChunkType(lookAheadTerminal, ChunkType.ORGAN)) { //t may have []<>{}
						startNoun = true; //not break yet, may be the next token is also a noun
						foundOrgan = true;
					}
					
					if(!foundOrgan && posKnowledgeBase.isNoun(lookAheadTerminal.getTerminalsText())){ 
						//t may have []<>{}
						startNoun = true; 
						//won't affect the value of foundorgan, after foundorgan is true, "plus" problem
						if(inflector.isPlural(lookAheadTerminal.getTerminalsText())){
							foundOrgan = true;
						}
					}
				}
				
				if(foundOrgan || npCopy){
					LinkedHashSet<Chunk> function = new LinkedHashSet<Chunk>();
					function.add(terminal);
					
					//merge two PPs where the first PP does not have a object
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
					
					Chunk functionChunk = new Chunk(ChunkType.PREPOSITION, function);
					Chunk objectChunk = new Chunk(ChunkType.OBJECT, organTerminals);

					LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
					childChunks.add(functionChunk);
					childChunks.add(objectChunk);
					Chunk ppChunk = new Chunk(ChunkType.PP, childChunks);
					chunkCollector.addChunk(ppChunk);
					
					/*if(chunkCollector.isPartOfAChunk(terminal)) {
						Chunk organChunk = new Chunk(ChunkType.ChunkOrgan, organTerminals);
						chunkCollector.addChunk(organChunk);
					} else { //without [], one word per token
						List<IParseTree> function = new ArrayList<IParseTree>();
						function.add(terminal);
						Chunk ppChunk = new PPChunk(ChunkType.ChunkPP, function, organTerminals); 
					}*/
				}else{ 
					if(j - i != 1){
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
					}
				}
			}
		}
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
		
		if(chunkCollector.isPartOfANonTerminalChunk(terminal)) {
			Chunk nonTerminalChunk = chunkCollector.getChunk(terminal);
			if(!nonTerminalChunk.isOfChunkType(ChunkType.COUNT) && 
					!nonTerminalChunk.isOfChunkType(ChunkType.CONSTRAINT) &&
					!nonTerminalChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && 
					!nonTerminalChunk.isOfChunkType(ChunkType.COMMA) && 
					!nonTerminalChunk.isOfChunkType(ChunkType.UNASSIGNED) && 
					!nonTerminalChunk.isOfChunkType(ChunkType.ORGAN))	
				return true;
		}
		if(terminal.getTerminalsText().startsWith(".")){
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
	}
}
