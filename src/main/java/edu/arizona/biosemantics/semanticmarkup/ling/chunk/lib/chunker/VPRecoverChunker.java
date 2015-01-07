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
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.lib.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * VPRecoverChunker chunks by handling verbs not parsed as such by the parser
 * @author rodenhausen
 */
public class VPRecoverChunker extends AbstractChunker {

	private IPOSKnowledgeBase posKnowledgeBase;

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
	public VPRecoverChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			@Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase, 
		ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, 
				glossary, terminologyLearner, inflector, learnedCharacterKnowledgeBase);
		this.posKnowledgeBase = posKnowledgeBase;
	}

	/**
	 * scan through a chunkedtokens to find Verbs not parsed as such by the parser
	 * find verbs by
	 * 1. look into this.verbs
	 * 2. find pattern o ting/ted by o, then t must be a verb and save this verb in verbs
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		/*List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		String text = "";
		for(int i = 0; i < terminals.size(); i++){
			text += terminals.get(i) + " ";
		}
		if(text.contains("usually whitish to stramineous"))
			log(LogLevel.DEBUG, "here");*/
		
		List<AbstractParseTree> terminals  = chunkCollector.getTerminals();
		for(int i = 0; i < terminals.size(); i++){
			AbstractParseTree terminal = terminals.get(i);
			if(terminal.getTerminalsText().contains("-"))
				continue;
			//!chunkCollector.isPartOfAChunk(terminal)
			if(!chunkCollector.isPartOfChunkType(terminal, ChunkType.VP) && 
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE) &&
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN) && 
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.PP) &&
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.CONSTRAINT) &&
					posKnowledgeBase.isVerb(terminal.getTerminalsText())) { 
				recoverVPChunk(terminals, i, chunkCollector);
			//!chunkCollector.isPartOfAChunk(terminal)
			} else if(!chunkCollector.isPartOfChunkType(terminal, ChunkType.VP) && 
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE) &&
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN) &&
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.PP) &&
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.CONSTRAINT) &&
					terminal.getTerminalsText().endsWith("ing")) {
				if(connectsTwoOrgans(terminals, i, chunkCollector)){
					 posKnowledgeBase.addVerb(terminal.getTerminalsText().replaceAll("\\W", ""));
					 recoverVPChunk(terminals, i, chunkCollector);
				 }
			} else if(i+1 < terminals.size()) {
				AbstractParseTree nextTerminal = terminals.get(i+1);
				if(!chunkCollector.isOfChunkType(terminal, ChunkType.VP) && 
						chunkCollector.isOfChunkType(terminal, ChunkType.CHARACTER_STATE) &&
						(chunkCollector.getChunk(terminal).getProperty("characterName").matches(".*?(^|_)("+ElementRelationGroup.entityRefElements+")(_|$).*") /*.contains("position")*/ ||
								chunkCollector.getChunk(terminal).getProperty("characterName").contains("size")) && 
								posKnowledgeBase.isVerb(terminal.getTerminalsText())) {
					if(chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.PP)) {
						recoverVPChunkFromVerbAndPP(terminals, i, chunkCollector);
					} else if(chunkCollector.isPartOfChunkType(nextTerminal, ChunkType.ORGAN)) {
						recoverVPChunkFromVerbAndOrgan(terminals, i, chunkCollector);
					}
				}
			}
		}
	}

	private void recoverVPChunkFromVerbAndOrgan(
			List<AbstractParseTree> terminals, int i,
			ChunkCollector chunkCollector) {
		AbstractParseTree verbTerminal = terminals.get(i);
		AbstractParseTree organTerminal = terminals.get(i+1);
		Chunk verbChunk = chunkCollector.getChunk(verbTerminal);
		Chunk newVerbChunk = new Chunk(ChunkType.VERB);
		LinkedHashSet<Chunk> newVerbChunkChildren = new LinkedHashSet<Chunk>();
		newVerbChunkChildren.addAll(verbChunk.getTerminals());
		newVerbChunk.setChunks(newVerbChunkChildren);
		Chunk organChunk = chunkCollector.getChunk(organTerminal);
		Chunk objectChunk = new Chunk(ChunkType.OBJECT, organChunk);
		
		LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
		childChunks.add(newVerbChunk);
		childChunks.add(objectChunk);
		Chunk vpChunk = new Chunk(ChunkType.VP, childChunks);
		chunkCollector.addChunk(vpChunk);
	}

	private boolean connectsTwoOrgans(List<AbstractParseTree> terminals, int i, ChunkCollector chunkCollector) {
		boolean organ1 = false;
		boolean organ2 = false;
		if(i >= 1 && terminals.size() > i+1){
			AbstractParseTree terminal = terminals.get(i-1);
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.OBJECT) || 
					chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
				organ1 = true;
			}
			do {
				i++;
				terminal = terminals.get(i);
			} while(terminal.getTerminalsText().length() == 0);
			
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.OBJECT) || 
					chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
				organ2 = true;
			}
		}
		return organ1 && organ2;
	}
	
	private void recoverVPChunkFromVerbAndPP(List<AbstractParseTree> terminals,
			int i, ChunkCollector chunkCollector) {
		AbstractParseTree verbTerminal = terminals.get(i);
		AbstractParseTree ppTerminal = terminals.get(i+1);
		Chunk verbChunk = chunkCollector.getChunk(verbTerminal);
		
		Chunk ppChunk = chunkCollector.getChunk(ppTerminal);
		Chunk prepositionChunk = ppChunk.getChildChunk(ChunkType.PREPOSITION);
		Chunk objectChunk = ppChunk.getChildChunk(ChunkType.OBJECT);
		if(objectChunk.containsChunkType(ChunkType.ORGAN)) {
			verbChunk.setChunkType(ChunkType.VERB);
			LinkedHashSet<Chunk> verbChildChunks = verbChunk.getChunks();
			verbChildChunks.addAll(prepositionChunk.getChunks());
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(verbChunk);
			childChunks.add(objectChunk);
			Chunk vpChunk = new Chunk(ChunkType.VP, childChunks);
			chunkCollector.addChunk(vpChunk);
		}
	}

	
	/**
	 * 
	 * @param i: the index of a possible verb
	 */
	private void recoverVPChunk(List<AbstractParseTree> terminals, int i, ChunkCollector chunkCollector) {
		boolean foundOrgan = false;
		List<AbstractParseTree> collectedTerminals = new ArrayList<AbstractParseTree>();
		for(int j=i+1; j < terminals.size(); j++){
			//scan for the end of the chunk TODO: may refactor with normalizeOtherINs on this search
			AbstractParseTree terminal = terminals.get(j);
			String terminalsText = terminal.getTerminalsText();
			if(j==i+1 && terminalsText.equals(","))
				//verb not have object
				return;
			if(terminalsText.matches("(;|\\.)")) 
				break;
			if(foundOrgan && (chunkCollector.isPartOfChunkType(terminal, ChunkType.STATE)
					|| terminalsText.contains("~list~") || terminalsText.matches("(\\w+|,|;|\\.)")
					|| chunkCollector.isPartOfANonTerminalChunk(terminal))) {
				break;
			}
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.VP) && chunkCollector.getChunk(terminal).containsChunkType(ChunkType.ORGAN)) {
				Chunk chunk = chunkCollector.getChunk(terminal);
				collectedTerminals.addAll(chunk.getTerminals());
				foundOrgan = true;
			} else if(chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
				collectedTerminals.add(terminal);
				foundOrgan = true;
			} else if(chunkCollector.isPartOfChunkType(terminal, ChunkType.OBJECT) || 
					chunkCollector.isPartOfChunkType(terminal, ChunkType.NP_LIST)) {//found noun)
				collectedTerminals.add(terminal);
				foundOrgan = true;
				j++;
				break;
			} else
				collectedTerminals.add(terminal);
		}
		if(!foundOrgan) 
			return;
		//format the chunk
		if(chunkCollector.isPartOfChunkType(collectedTerminals.get(collectedTerminals.size()-1), ChunkType.ORGAN)) {
			LinkedHashSet<Chunk> function = new LinkedHashSet<Chunk>(); 
			Chunk functionChunk = chunkCollector.getChunk(terminals.get(i));
			function.add(chunkCollector.getChunk(terminals.get(i)));
			if(functionChunk.isOfChunkType(ChunkType.CHARACTER_STATE))
				functionChunk = new Chunk(ChunkType.SPECIFIER, function);
			else
				functionChunk = new Chunk(ChunkType.VERB, function);
			LinkedHashSet<Chunk> object = new LinkedHashSet<Chunk>();
			for(AbstractParseTree terminal : collectedTerminals)
				object.add(chunkCollector.getChunk(terminal));
			Chunk objectChunk = new Chunk(ChunkType.OBJECT, object);
			
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(functionChunk);
			childChunks.add(objectChunk);
			Chunk chunk;
			if(functionChunk.isOfChunkType(ChunkType.SPECIFIER))
				chunk = new Chunk(ChunkType.SPECIFIC_PP, childChunks);
			else
				chunk = new Chunk(ChunkType.VP, childChunks);
			chunkCollector.addChunk(chunk);

		} else if(chunkCollector.containsPartOfChunkType(collectedTerminals, ChunkType.OBJECT)) {
			if(!chunkCollector.isPartOfChunkType(collectedTerminals.get(0), ChunkType.PP) && chunkCollector.containsPartOfChunkType(collectedTerminals, ChunkType.VP)) {
					Chunk vpChunk = chunkCollector.getChunkOfChunkType(collectedTerminals, ChunkType.VP);
					LinkedHashSet<Chunk> childChunks = (LinkedHashSet<Chunk>)vpChunk.getChunks().clone();
					for(AbstractParseTree terminal : collectedTerminals) {
						if(!vpChunk.containsOrEquals(terminal)) {
							List<Chunk> childChunksList = new ArrayList<Chunk>(childChunks);
							childChunks.clear();
							Chunk terminalChunk = chunkCollector.getChunk(terminal);
							childChunks.add(terminalChunk);
							childChunks.addAll(childChunksList);
							vpChunk = new Chunk(ChunkType.VP, childChunks);
						}
					}
					//System.out.println(childChunks.toString());
					vpChunk = new Chunk(ChunkType.VP, childChunks);
					chunkCollector.addChunk(vpChunk);
			} else if(chunkCollector.isPartOfChunkType(collectedTerminals.get(0), ChunkType.PP)) {
				//t[c[{extending}] r[p[to] o[(midvalve)]]]
				//chunk = chunk.replaceFirst("^r[p[", "b[v["+this.chunkedtokens.get(i)+ " "); 
				//need to make the v is taken as a relation in processChunkVP
				Chunk specifierChunk = new Chunk(ChunkType.SPECIFIER, chunkCollector.getChunk(terminals.get(i)));
				Chunk ppChunk = chunkCollector.getChunk(collectedTerminals.get(0));
				LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
				childChunks.add(specifierChunk);
				childChunks.add(ppChunk);
				Chunk chunk = new Chunk(ChunkType.SPECIFIC_PP, childChunks);
				chunkCollector.addChunk(chunk);
			} else if(chunkCollector.isPartOfChunkType(collectedTerminals.get(0), ChunkType.NP_LIST)) {
				//Chunk npListChunk = chunkCollector.getChunk(collectedTerminals.get(0));
				//npListChunk.setChunkType(ChunkType.ChunkOrgan);
				LinkedHashSet<Chunk> function = new LinkedHashSet<Chunk>();
				function.add(chunkCollector.getChunk(terminals.get(i)));
				Chunk functionChunk = new Chunk(ChunkType.VERB, function);
				
				LinkedHashSet<Chunk> object = new LinkedHashSet<Chunk>();
				for(AbstractParseTree terminal : collectedTerminals)
					object.add(chunkCollector.getChunk(terminal));
				Chunk objectChunk = new Chunk(ChunkType.OBJECT, object);
				
				LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
				childChunks.add(functionChunk);
				childChunks.add(objectChunk);
				Chunk chunk = new Chunk(ChunkType.VP, childChunks);
				chunkCollector.addChunk(chunk);
				
			} else if(chunkCollector.isPartOfChunkType(collectedTerminals.get(0), ChunkType.NON_SUBJECT_ORGAN)) {
				LinkedHashSet<Chunk> function = new LinkedHashSet<Chunk>();
				function.add(chunkCollector.getChunk(terminals.get(i)));
				Chunk functionChunk = new Chunk(ChunkType.VERB, function);
				
				LinkedHashSet<Chunk> object = new LinkedHashSet<Chunk>();
				for(AbstractParseTree terminal : collectedTerminals)
					object.add(chunkCollector.getChunk(terminal));
				Chunk objectChunk = new Chunk(ChunkType.OBJECT, object);
				
				LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
				childChunks.add(functionChunk);
				childChunks.add(objectChunk);
				Chunk chunk = new Chunk(ChunkType.VP, childChunks);
				chunkCollector.addChunk(chunk);
			}
		}	
	}
}
