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
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

//not used class because functionality already implemented in CleanupChunker
//however, CleanupChunker is a big class and could be separated according to handling numerics or other things
/**
 * NumericalChunker chunks by handling numerical terminals 
 * @author rodenhausen
 */
public class NumericalChunker extends AbstractChunker {

	private String percentageWords;
	private String degreeWords;
	private String countWords;
	private String timesWords;
	private String numberPattern;

	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 * @param percentageWords
	 * @param degreeWords
	 * @param countWords
	 * @param timesWords
	 * @param numberPattern
	 */
	@Inject
	public NumericalChunker(IParseTreeFactory parseTreeFactory,
			@Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector,
			@Named("PercentageWords") String percentageWords,
			@Named("DegreeWords") String degreeWords, 
			@Named("CountWords") String countWords, 
			@Named("TimesWords") String timesWords, 
			@Named("NumberPattern") String numberPattern, 
			ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,
				glossary, terminologyLearner, inflector,learnedCharacterKnowledgeBase);
		this.percentageWords = percentageWords;
		this.degreeWords = degreeWords;
		this.countWords = countWords;
		this.timesWords = timesWords;
		this.numberPattern = numberPattern;
	}


	/**
	 * TODO: deal with LRB-/-LRB
	 * e.g. 3 cm, what about "3 cm to 10 dm"?
	 * also 3 times (... longer than, as wide as ...)
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals(); 
		for(int i=0; i < terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			if(!chunkCollector.isPartOfChunkType(terminal, ChunkType.CHROM)) {
				String terminalsText = terminal.getTerminalsText();
				
				if(terminalsText.matches(".*?" + numberPattern + "$") || terminalsText.matches("\\(\\d+\\)") || terminalsText.matches("\\d+\\+?") || terminalsText.matches("^to~\\d.*")) { 
					terminalsText = originalNumForm(terminalsText).replaceAll("\\?", "");		
					if(terminalsText.matches("^to~\\d.*")){
						terminal.setTerminalsText(terminalsText.replaceAll("~",  " "));
						Chunk value = new Chunk(ChunkType.VALUE, chunkCollector.getChunk(terminal));
						chunkCollector.addChunk(value);
						continue;
					}
					if(terminalsText.matches(".*?(" + percentageWords + ")")){
						Chunk valuePercentage = new Chunk(ChunkType.VALUE_PERCENTAGE,  chunkCollector.getChunk(terminal));
						chunkCollector.addChunk(valuePercentage);
						continue;
					}
					if(terminalsText.matches(".*?(" + degreeWords + ")")){
						Chunk valueDegree = new Chunk(ChunkType.VALUE_DEGREE, chunkCollector.getChunk(terminal));
						chunkCollector.addChunk(valueDegree);
						continue;
					}
					if(terminalsText.matches(".*?[()\\[\\]\\-\\–\\d\\.×\\+°²½/¼\\*/%]*?[½/¼\\d][()\\[\\]\\-\\–\\d\\.×\\+°²½/¼\\*/%]*(-\\s*(" + countWords + ")\\b|$)")) {						
						//ends with a number
						if(i==terminals.size()-1) {
							Chunk count = new Chunk(ChunkType.COUNT,  chunkCollector.getChunk(terminal));
							chunkCollector.addChunk(count);
							continue;
						}
						
						i++;
						AbstractParseTree lookForwardTerminal = terminals.get(i);
						String lookForwardText = lookForwardTerminal.getTerminalsText();
						
						if(lookForwardText.matches("^(" + units + ")\\b.*?")){
							String combinedText = terminalsText + " " + lookForwardText;
							//adjustPointer4Dot(pointer, terminals);
							//in bhl, 10 cm . long, should skip the ". long" after the unit
							//numerics = numerics.replaceAll("[{(<>)}]", "").trim();
							LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
							childChunks.add(chunkCollector.getChunk(terminal));
							childChunks.add(chunkCollector.getChunk(lookForwardTerminal));
							if(combinedText.contains("×")) { //×
								Chunk area = new Chunk(ChunkType.AREA, childChunks);
								chunkCollector.addChunk(area);
							} else {
								Chunk value = new Chunk(ChunkType.VALUE,  childChunks);
								chunkCollector.addChunk(value);
							}
							continue;
						}
						if(lookForwardText.matches("(" + timesWords + ")\\b.*?")){
							/*
							 * 
							 *  lengths => lengths
								mostly => MODIFIER: [mostly]
								2-5 => 2-5
								widths => widths
							   
							   to 
							   
							    lengths => COMPARATIVE_VALUE: [ CHARACTER_STATE: characterName->character; [STATE: [lengths]], MODIFIER: [mostly], 2-5, CHARACTER_STATE: characterName->character; [STATE: [widths]]]
								mostly => COMPARATIVE_VALUE: [ CHARACTER_STATE: characterName->character; [STATE: [lengths]], MODIFIER: [mostly], 2-5, CHARACTER_STATE: characterName->character; [STATE: [widths]]]
								2-5 => COMPARATIVE_VALUE: [ CHARACTER_STATE: characterName->character; [STATE: [lengths]], MODIFIER: [mostly], 2-5, CHARACTER_STATE: characterName->character; [STATE: [widths]]]
								widths => COMPARATIVE_VALUE: [ CHARACTER_STATE: characterName->character; [STATE: [lengths]], MODIFIER: [mostly], 2-5, CHARACTER_STATE: characterName->character; [STATE: [widths]]]
								; => END_OF_LINE: [;]
							  
							 */
							LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
							//look behind for the comparison character 
							int j = i-2; //go back to the chunk before the "terminal"
							boolean findCharacter = false;
							boolean expectOf = false;
							boolean expectCharacter = false;
							
							
							for(; j>=0; j--){
								AbstractParseTree lookBehindTerminal = terminals.get(j);
								Chunk lookBehindChunk = chunkCollector.getChunk(lookBehindTerminal);
								
								if(expectOf && lookBehindTerminal.getTerminalsText().compareTo("of")==0){ //lengths of ...
									childChunks.add(lookBehindChunk);
									expectCharacter = true;
									continue;
								}else if(expectOf && !(lookBehindChunk.isOfChunkType(ChunkType.CONSTRAINT) || lookBehindChunk.isOfChunkType(ChunkType.ORGAN) 
										|| lookBehindChunk.isOfChunkType(ChunkType.NP_LIST) || lookBehindChunk.isOfChunkType(ChunkType.CHARACTER_STATE))){
									break;
								}
								
								if(expectCharacter && lookBehindChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && lookBehindChunk.getProperty("characterName").compareTo("character")==0){
									childChunks.add(lookBehindChunk);
									findCharacter = true;
									break;
								}else if(expectCharacter){
									break;
								}
									
								
								if(lookBehindChunk.isOfChunkType(ChunkType.CHARACTER_STATE)){
									if(lookBehindChunk.getProperty("characterName").compareTo("character")==0){
										childChunks.add(lookBehindChunk);
										findCharacter = true;
										break;
									}else{
										childChunks.add(lookBehindChunk);
									}
									
								}else if(lookBehindChunk.isOfChunkType(ChunkType.CONSTRAINT) || lookBehindChunk.isOfChunkType(ChunkType.ORGAN) || lookBehindChunk.isOfChunkType(ChunkType.NP_LIST)){
									childChunks.add(lookBehindChunk);
									expectOf = true;
									continue;
								}
								
								/*else if(lookBehindChunk.isOfChunkType(ChunkType.CONSTRAINT) || lookBehindChunk.isOfChunkType(ChunkType.ORGAN) || lookBehindChunk.isOfChunkType(ChunkType.NP_LIST)){
									LinkedHashSet<Chunk> childChunkCandidates = new LinkedHashSet<Chunk>();
									childChunkCandidates.add(lookBehindChunk);
									boolean expectCharacter = false;
									for(j = j-1; j>=0; j--){
										AbstractParseTree lookFurtherBehindTerminal = terminals.get(j);
										Chunk lookFurtherBehindChunk = chunkCollector.getChunk(lookFurtherBehindTerminal);
										if(expectCharacter && lookFurtherBehindChunk.getProperty("characterName").compareTo("character")==0){
											childChunkCandidates.add(lookFurtherBehindChunk);
											childChunks.addAll(childChunkCandidates);
											findCharacter = true;
											break;
										}else if(expectCharacter){
											break;
										}
										if(lookFurtherBehindChunk.isOfChunkType(ChunkType.CONSTRAINT) ||lookFurtherBehindChunk.isOfChunkType(ChunkType.CHARACTER_STATE) || lookFurtherBehindChunk.isOfChunkType(ChunkType.ORGAN) || lookFurtherBehindChunk.isOfChunkType(ChunkType.NP_LIST)){
											childChunkCandidates.add(lookFurtherBehindChunk);
										}else if(lookFurtherBehindTerminal.getTerminalsText().compareTo("of"+"")==0){ //lengths of ...
											childChunkCandidates.add(lookFurtherBehindChunk);
											expectCharacter = true;
										}
										
										
									}
								}*/else{
									childChunks.add(lookBehindChunk); //collect everything until any of the break point is met: find a character such as lengths or a (list of) organ
								}
							}
							
							if(!findCharacter) //reset childChunks
								childChunks = new LinkedHashSet<Chunk>();
							else{//reverse the order of chunks obtained through tracing back
								ArrayList<Chunk> clone = new ArrayList<Chunk>();
								clone.addAll((LinkedHashSet<Chunk>) childChunks.clone());
								childChunks = new LinkedHashSet<Chunk>();
								for(int k = clone.size()-1; k>=0; k--){
									childChunks.add(clone.get(k));
								}
							}
							
							i++;
							AbstractParseTree lookDoubleForwardTerminal = terminals.get(i);
							Chunk lookDoubleForwardChunk = chunkCollector.getChunk(lookDoubleForwardTerminal);
							
							childChunks.add(chunkCollector.getChunk(terminal));
							childChunks.add(chunkCollector.getChunk(lookForwardTerminal));
							
							if(lookDoubleForwardChunk.isOfChunkType(ChunkType.THAN_CHARACTER_PHRASE)) {
								childChunks.add(lookDoubleForwardChunk);				
								Chunk value = new Chunk(ChunkType.VALUE, childChunks);
								chunkCollector.addChunk(value);
							} else {
								//look ahead until a character (e.g. lengths), an organ/list of organ is found. also consider 'widths of organ' and "organ widths"
								boolean foundCharacter = false;
								boolean foundOrgan = false;
								
								for(; i < terminals.size(); i++){
									AbstractParseTree lookAheadTerminal = terminals.get(i);
									Chunk lookAheadChunk = chunkCollector.getChunk(lookAheadTerminal);
									if(foundCharacter || foundOrgan){
										if(!lookAheadChunk.isOfChunkType(ChunkType.CONSTRAINT) && ! lookAheadChunk.isOfChunkType(ChunkType.ORGAN)
												&& ! lookAheadChunk.isOfChunkType(ChunkType.NP_LIST) && !(lookAheadChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && 
												lookAheadChunk.getProperty("characterName").matches(".*?_?(character|width|length)_?.*")) 
												&& lookAheadTerminal.getTerminalsText().compareTo("of")!=0 && lookAheadTerminal.getTerminalsText().compareTo("than")!=0 ){
											break;
										}
									}
									if(foundCharacter && lookAheadTerminal.getTerminalsText().compareTo("of")==0){
										childChunks.add(lookAheadChunk);
										continue;
									}else if(foundOrgan && lookAheadTerminal.getTerminalsText().compareTo("of")==0){
										break;
									}else if(lookAheadChunk.isOfChunkType(ChunkType.UNASSIGNED) || lookAheadChunk.isOfChunkType(ChunkType.CHARACTER_STATE) ||lookAheadChunk.isOfChunkType(ChunkType.TO_PHRASE)){ //2 times pinnately lobed
										childChunks.add(lookAheadChunk);
									}else if(lookAheadChunk.isOfChunkType(ChunkType.CHARACTER_STATE)){
										if(lookAheadChunk.getProperty("characterName").matches(".*?_?(character|width|length)_?.*")){
											childChunks.add(lookAheadChunk);
											foundCharacter = true;
										}
									}else if(lookAheadChunk.isOfChunkType(ChunkType.CONSTRAINT) || lookAheadChunk.isOfChunkType(ChunkType.ORGAN) || lookAheadChunk.isOfChunkType(ChunkType.NP_LIST)){
										childChunks.add(lookAheadChunk);
										foundOrgan = true;
									}

								}
								Chunk comparativeValue = new Chunk(ChunkType.COMPARATIVE_VALUE, childChunks);
								chunkCollector.addChunk(comparativeValue);
							}
							continue;
						} 
						
						Chunk count = new Chunk(ChunkType.COUNT,  chunkCollector.getChunk(terminal));
						chunkCollector.addChunk(count);
					}
				}
			}
		}
		
		// l/w = X 
		// ratio chunking
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			String terminalsText = terminal.getTerminalsText();
			
			if(terminalsText.matches("l/w")){
				int j = i+1;
				boolean foundRatio = false;
				while(j < terminals.size()) {
					AbstractParseTree lookAheadTerminal = terminals.get(j);
					String lookAheadTerminalText = lookAheadTerminal.getTerminalsText();
					if(lookAheadTerminalText.equals("=")) {
						//j++;
						i = j+1;
						j = i;
					} else if(lookAheadTerminalText.matches(".*?\\d.*")) {
						foundRatio = true;
						j++;
					} else {
						break;
					}
				}
				if(foundRatio) {
					LinkedHashSet<Chunk> ratioChildren = new LinkedHashSet<Chunk>();
					for(;i<j;i++) {
						/*terminal.setTerminalsText(originalNumForm(terminalsText).trim());*/
						Chunk c = chunkCollector.getChunk(terminals.get(i));
						c.setChunkType(ChunkType.RATIO);
						ratioChildren.add(c);
					}
					Chunk ratioChunk = new Chunk(ChunkType.RATIO, ratioChildren); 
					chunkCollector.addChunk(ratioChunk);
				}
			}
		}
		
		//average values: av . 6 . 9 x 5 . 5 μm
		for(int i=0; i<terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			String terminalsText = terminal.getTerminalsText();
			
			if(terminalsText.matches("av")){
				int j = i+1;
				boolean foundAV = false;
				while(j < terminals.size()) {
					AbstractParseTree lookAheadTerminal = terminals.get(j);
					String lookAheadTerminalText = lookAheadTerminal.getTerminalsText();
					if(lookAheadTerminalText.matches("[.=:]")) {
						//j++;
						i = j+1;
						j = i;
					} else if(lookAheadTerminalText.matches(".*?\\d.*")) {
						foundAV = true;
						j++;
					} else {
						break;
					}
				}
				if(foundAV) {
					LinkedHashSet<Chunk> ratioChildren = new LinkedHashSet<Chunk>();
					for(;i<j;i++) {
						/*terminal.setTerminalsText(originalNumForm(terminalsText).trim());*/
						Chunk c = chunkCollector.getChunk(terminals.get(i));
						c.setChunkType(ChunkType.AVERAGE);
						ratioChildren.add(c);
					}
					Chunk ratioChunk = new Chunk(ChunkType.AVERAGE, ratioChildren); 
					chunkCollector.addChunk(ratioChunk);
				}
			}
		}
	}
	
	
	public String originalNumForm(String token){
		if(token.matches(".*[a-z].*?")){
			return token.replaceAll("-\\s*LRB-/-LRB\\s*-?", "(").replaceAll("-\\s*RRB-/-RRB\\s*-?", ")");
		}else{
			return token.replaceAll("-\\s*LRB-/-LRB\\s*-?", "[").replaceAll("-\\s*RRB-/-RRB\\s*-?", "]");
		}
	}
}
