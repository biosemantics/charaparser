package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * ConjunctedOrgansRecoverChunker attempts to include broken-away conjuncted organs to pp and vb phrase
 * @author rodenhausen
 */
public class ConjunctedOrgansRecoverChunker extends AbstractChunker {

	@Inject
	public ConjunctedOrgansRecoverChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, 
				terminologyLearner, inflector, organStateKnowledgeBase);
	}

	/**
	 * attempts to include broken-away conjuncted organs to pp and vb phrase
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		
		for(int i = 0; i < terminals.size(); i++){
			AbstractParseTree terminal = terminals.get(i);
			//log(LogLevel.DEBUG, "terminal " + terminal);
			if(terminals.size() > i + 2) {
				if((chunkCollector.isPartOfChunkType(terminal, ChunkType.PP) 
						|| chunkCollector.isPartOfChunkType(terminal, ChunkType.VP)) && 
						((terminals.get(i+1).getTerminalsText().matches("(and|or|plus)") && isLastOfChunk(terminals.get(i+1), chunkCollector.getChunk(terminals.get(i+1)))) ||
								(terminals.get(i+1).getTerminalsText().matches(",") && 
										terminals.get(i+2).getTerminalsText().matches("(and|or|plus)")))) {//check 211
					recoverConjunctedOrgans4PP(terminals, i, chunkCollector);
				}
				
				else if((chunkCollector.isPartOfChunkType(terminal, ChunkType.PP) || 
						chunkCollector.isPartOfChunkType(terminal, ChunkType.VP)) && 
						chunkCollector.isPartOfChunkType(terminals.get(i+1), ChunkType.ORGAN)) {		
					//found a broken away noun
					int j = i;
					LinkedHashSet<Chunk> newOrgan = new LinkedHashSet<Chunk>();
					AbstractParseTree organ = terminals.get(++j);					
					do {
						//log(LogLevel.DEBUG, "do loop " + terminals.get(j));
						//log(LogLevel.DEBUG, "add " + chunkCollector.getChunk(organ));
						newOrgan.add(chunkCollector.getChunk(organ));
						organ = terminals.get(++j);					
					} while(chunkCollector.isPartOfChunkType(organ, ChunkType.ORGAN) && j+1 < terminals.size());
					
					Chunk chunk = chunkCollector.getChunk(terminal);
					newOrgan.remove(chunk);
					LinkedHashSet<Chunk> childChunks = chunk.getChunks();
					//log(LogLevel.DEBUG, "chunkCollector " + chunkCollector.toString());
					childChunks.addAll(newOrgan);
					//log(LogLevel.DEBUG, "childChunks");
					//log(LogLevel.DEBUG, childChunks);
					
					chunk = new Chunk(chunk.getChunkType(), childChunks);
					
					chunkCollector.addChunk(chunk);
					/*
					if(chunk instanceof TwoValuedChunk) {
						TwoValuedChunk twoValuedChunk = (TwoValuedChunk)chunk;
						twoValuedChunk.getArgument().addAll(newOrgan);
						chunkCollector.addChunk(chunk);
					} else {
						chunk.getParseTrees().addAll(newOrgan);
					}*/
				}
			}
		}
		//log(LogLevel.DEBUG, "done here");
	}
	
	
	private boolean isLastOfChunk(AbstractParseTree abstractParseTree, Chunk chunk) {
		LinkedHashSet<Chunk> chunks = chunk.getChunks();
		Iterator<Chunk> chunkIterator = chunks.iterator();
		while(chunkIterator.hasNext()) {
			Chunk child = chunkIterator.next();
			if(!chunkIterator.hasNext() && child.equals(abstractParseTree))
				return true;
		}
		return false;
	}

	/**
	 * recover if what follows the PP is "and|or|plus" and a (modified) organ followed by a , or a series of chunks
	 * @param chunkCollector 
	 * @param terminals 
	 * @param i: the index where a PP-chunk followed by and|or|plus is found
	 */
	private void recoverConjunctedOrgans4PP(List<AbstractParseTree> terminals, int i, ChunkCollector chunkCollector) {
		LinkedHashSet<Chunk> collectedTerminals = new LinkedHashSet<Chunk>();
		collectedTerminals.add(chunkCollector.getChunk(terminals.get(i + 1))); //and|or|plus
		boolean foundOrgan = false;
		boolean recover = true;
		int endindex = 0;
		for(int j = i+2; j < terminals.size(); j++){
			AbstractParseTree lookForwardTerminal = terminals.get(j);
			if(!foundOrgan && (chunkCollector.isPartOfChunkType(lookForwardTerminal, ChunkType.STATE) || 
					lookForwardTerminal.getTerminalsText().equals(",") || lookForwardTerminal.getTerminalsText().contains("~list~"))) {
				//states before an organ 
				collectedTerminals.add(chunkCollector.getChunk(lookForwardTerminal));
			} else if(chunkCollector.isPartOfChunkType(lookForwardTerminal, ChunkType.ORGAN) || 
					chunkCollector.isPartOfChunkType(lookForwardTerminal, ChunkType.NP_LIST)){ 
				//organ
				collectedTerminals.add(chunkCollector.getChunk(lookForwardTerminal));
				endindex = j;
				foundOrgan = true;
			} else if(foundOrgan && lookForwardTerminal.getTerminalsText().matches("(,|;|\\.)")) {
				//states before an organ 
				break; //organ followed by ",",  should recover
			} else if(foundOrgan && chunkCollector.isPartOfANonTerminalChunk(lookForwardTerminal) && 
					!lookForwardTerminal.getTerminalsText().contains("~list~")){
				//found or not found organ
				//do nothing
			} else{
				recover = false;
				break;
			}
		}
		
		if(recover) {
			Chunk chunk = chunkCollector.getChunk(terminals.get(i));
			collectedTerminals.remove(chunk);
			LinkedHashSet<Chunk> childChunks = chunk.getChunks();
			childChunks.addAll(collectedTerminals);
			chunk = new Chunk(chunk.getChunkType(), childChunks);
			chunkCollector.addChunk(chunk);
			
			/*if(chunk instanceof TwoValuedChunk) {
				TwoValuedChunk twoValuedChunk = (TwoValuedChunk)chunk;
				twoValuedChunk.getArgument().addAll(collectedTerminals);
			}
			else {
				chunk.getParseTrees().addAll(collectedTerminals);
			}*/
			
			//reformat: insert recovered before the last set of ] 
			/*String chunk = this.chunkedtokens.get(i);
			String p1 = chunk.replaceFirst("\\]+$", "");
			String p2 = chunk.replace(p1, "");
			chunk = p1+" "+recovered + p2; */
		}
	}
}
