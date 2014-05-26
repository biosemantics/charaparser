package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.HashMap;
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
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IFirstChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.ParentTagProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * SomeFirstChunkProcessor poses an IFirstChunkProcessor
 * @author rodenhausen
 */
public class SomeFirstChunkProcessor extends AbstractChunkProcessor implements IFirstChunkProcessor {

	private int skipFirstNChunk = 0;
	private ParentTagProvider parentTagProvider;
	private boolean firstSentence = false;
	
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
	 * @param parentTagProvider
	 */
	@Inject
	public SomeFirstChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times,
			@Named("ParentTagProvider")ParentTagProvider parentTagProvider, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
		this.parentTagProvider = parentTagProvider;
	}

	/**
	 * chunks has a chunk containing ":". 
	 * If there is not a OrganChunk before ":", consider the chunks before ":" a heading and skip them (including the ":"). 
	 * @param chunks
	 */
	public int skipHeading(List<Chunk> chunks){
		boolean foundOrganChunk = false;
		int skip = 0;
		
		for(Chunk c: chunks){
			if(c.toString().equals(":")) break;
			if(c.containsChunkType(ChunkType.ORGAN)){
				foundOrganChunk = true;	
				break;
			}
			skip++;
		}
		
		if(!foundOrganChunk) ++skip;
		return skip;
	}
	@Override
	protected List<Structure> processChunk(Chunk firstChunk, ProcessingContext processingContext) {
		skipFirstNChunk = 0;
		List<Structure> result = new LinkedList<Structure>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		List<Chunk> chunks = processingContext.getChunkCollector().getChunks();
		///////////////////////////////////////////////////////////////////////////////////////////////
		/*
		this.statement = statement;
		this.sentsrc = sentsrc;
		if(sentsrc.endsWith("-0")) reset();
		//if(this.text.matches(".*?([A-Z]{2,})\\s*\\d+.*")){ //this.text must be originalsent where capitalization is preserved.
		//	this.annotatedMeasurements(this.text, cs); //this will not work on "Scape short to moderate, SI 59 - 83"
		//}
		this.text = cs.getText();
		boolean startsent = false;
		if(text.matches("[A-Z].*?")) startsent = true; 
		if(this.text.startsWith("Characters of ") && this.text.replaceAll("[^,;\\.:]", "").length()<=1){
			return this.statement; //characters of abc. one sentence.
		}
		if(! (cs.toString().replaceAll("\\S*?taxonname-\\S+", "").matches(".*?\\w.*"))){
			return this.statement; //the sentence contain 1 or more taxonames, no other text
		}
		//because sentence tags are not as reliable as chunkedsentence
		//no longer get subject text from cs
		//instead, annotate chunk by chunk
		Chunk ck = cs.nextChunk();
		if(ck instanceof ChunkOrgan){//start with a subject
			String content = ck.toString().replaceFirst("^z\\[", "").replaceFirst("\\]$", "");
			establishSubject(content, cs);// 7-12-02 add cs
			if(this.partofinference){
				this.cstructures.addAll(this.subjects);
			}
			cs.setInSegment(true); //inSegment = true: meaning found the subject.
			cs.setRightAfterSubject(true);
		}else{//not start with a subject
			if(ck instanceof ChunkPrep){	//check if the first chunk is a preposition chunk. If so, make the subjects and the latest elements from the previous sentence empty, and skip+ignore this chunk.
				//write code to make the latestelements nil
				this.latestelements = new ArrayList<Element>();
				String content = ck.toString();
				if(content.startsWith("r[p[with]")){ //r[p[with] o[1-2(-5)-(flowers)]] r[p[in] o[(axils)]] r[p[of] o[(bracts)]] . 
					//turn with-phrase to an organ chunk
					content = content.replaceFirst("^r\\[p\\[with\\] o\\[", "").replaceFirst("\\]+$", ""); //1-2(-5)-(flowers)
					establishSubject(content, cs);// 7-12-02 add cs
					if(this.partofinference){
						this.cstructures.addAll(this.subjects);
					}
					cs.setInSegment(true);
					cs.setRightAfterSubject(true);
				}else{// if (content.startsWith("r[p[without]")){ //r[p[without] o[{nodal} (spines)]] 
					//mostly r[p[at] o[(tips)]] r[p[of] o[(inflorescences)]] ; other prepchunks should be processed as well if they are not followed by an organ chunk.
					Chunk nextck = cs.nextChunk();
					if(!(nextck instanceof ChunkOrgan) && !(nextck instanceof ChunkNPList) && !(nextck instanceof ChunkNonSubjectOrgan)){ 
						reestablishSubject();
						cs.resetPointer(); //make sure the prep chunk is annotated later
						if(this.partofinference){
							this.cstructures.addAll(this.subjects);
						}
						cs.setInSegment(true);
						cs.setRightAfterSubject(true);

					}else{
						//didn't know why the above if was coded that way, because it is correct English syntax that a organ chunk follows a prep chunk, such as in spring flowers bloom.
						establishSubject(nextck.toString().replaceAll("(\\w\\[|\\])", ""), cs);
						if(this.partofinference){
							this.cstructures.addAll(this.subjects);
						}
						cs.setInSegment(true);
						cs.setRightAfterSubject(true);

					}
				}
			}else{ //ck is a character state
				if(!sentsrc.endsWith("-0")){ //&& !ck.toString().contains("character[")){
					reestablishSubject();	//reuse the previous subject only when this sentence is not the first one in the treatment
					cs.setInSegment(true);
					cs.setRightAfterSubject(true);
				}//else if (ck.toString().contains("character[")){
				//	reset(); //when sentence start with character (e.g. Diameter ...), clear up latestelement and subject caches. update: ?? could be fruit..., diameter
				//}//TODO: real cases exist for both, how could we decide?
				cs.resetPointer(); //make sure ck is annotated
			}
		}*/
		
		//starts with a organ (subject)
		if(firstChunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN) || firstChunk.isOfChunkType(ChunkType.NON_SUBJECT_ORGAN) || firstChunk.isOfChunkType(ChunkType.NP_LIST)) {
			result.addAll(establishSubject(firstChunk, processingContext, processingContextState));
			skipFirstNChunk = 1;
		} else if(firstChunk.isOfChunkType(ChunkType.PP)) {
			lastElements.clear();
			List<AbstractParseTree> chunkTerminals = firstChunk.getTerminals();
			if(chunkTerminals.get(0).equals("with")) {
				Chunk organChunk = firstChunk.getChunkDFS(ChunkType.ORGAN);
				result.addAll(establishSubject(organChunk, processingContext, processingContextState));
			} else {
				if(chunks.size()>1){
					Chunk nextChunk = chunks.get(1);
					//mostly r[p[at] o[(tips)]] r[p[of] o[(inflorescences)]] ; other prepchunks should be processed as well if they are not followed by an organ chunk.
					if(!(nextChunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN)) && !(nextChunk.isOfChunkType(ChunkType.NP_LIST)) && 
							!(nextChunk.isOfChunkType(ChunkType.NON_SUBJECT_ORGAN))) {
						result.addAll(reestablishSubject(processingContext, processingContextState));
						skipFirstNChunk = 1;
						return result;
					}else{
						//didn't know why the above 'if' was coded that way, because it is correct English syntax that a organ chunk follows a prep chunk,
						//such as 'in spring flowers bloom'.
						processingContextState.getUnassignedConstraints().add(firstChunk);
						result.addAll(establishSubject(nextChunk, processingContext, processingContextState)); //skip the first prepChunk
						skipFirstNChunk = 2; //after this, ready to process the chunk after second chunk
						return result;
					}
				}
			}
		} else {
			if(firstChunk.isOfChunkType(ChunkType.MODIFIER) || firstChunk.isOfChunkType(ChunkType.CONSTRAINT)){
				processingContextState.getUnassignedConstraints().add(firstChunk);
			}
			List<Structure> subjects = reestablishSubject(processingContext, processingContextState);
			//if(firstChunk.isOfChunkType(ChunkType.CHARACTER_STATE) && subjects.size()==0 && this.firstSentence){
			if(subjects.size()==0 && this.firstSentence){
				//use whole_organism
				Structure structureElement = new Structure();
				int structureIdString = processingContext.fetchAndIncrementStructureId(structureElement);
				structureElement.setId("o" + String.valueOf(structureIdString));	
				structureElement.setName("whole_organism"); 
				List<Structure> structureElements = new LinkedList<Structure>();
				structureElements.add(structureElement);
				result.addAll(establishSubject(structureElements, processingContextState));
			}else{
				result.addAll(subjects);
			}
			skipFirstNChunk = 0;
			return result;
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////////
		//starts with a organ (subject)
		/*if(firstChunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN)) {
			result.addAll(establishSubject(firstChunk, processingContext, processingContextState));
			skipFirstChunk = true;
		} else {
			Structure structureElement;
			
			if(processingContext.getChunkCollector().getSubjectTag().equals("general")) {
				structureElement = new Structure();
				int structureIdString = processingContext.fetchAndIncrementStructureId(structureElement);
				structureElement.setId("o" + String.valueOf(structureIdString));	
				structureElement.setName("whole_organism");
				List<Structure> structureElements = new LinkedList<Structure>();
				structureElements.add(structureElement);
				result.addAll(establishSubject(structureElements, processingContextState));
				skipFirstChunk = false;
			} else if(processingContext.getChunkCollector().getSubjectTag().equals("ditto")) {
				String previousMainSubjectOrgan = parentTagProvider.getParentTag(processingContext.getChunkCollector().getSource());
				previousMainSubjectOrgan = previousMainSubjectOrgan.equals("general")? "whole_organism" : previousMainSubjectOrgan;
				structureElement = new Structure();
				int structureIdString = processingContext.fetchAndIncrementStructureId(structureElement);
				structureElement.setId("o" + String.valueOf(structureIdString));	
				structureElement.setName(previousMainSubjectOrgan);
				
				List<Structure> structureElements = new LinkedList<Structure>();
				structureElements.add(structureElement);
				result.addAll(establishSubject(structureElements, processingContextState));
				skipFirstChunk = false;
			} else {
				//structureElement = new DescriptionTreatmentElement(DescriptionType.STRUCTURE);
				//int structureIdString = processingContextState.fetchAndIncrementStructureId(structureElement);
				//structureElement.setProperty("id", "o" + String.valueOf(structureIdString));	
				//structureElement.setProperty("name", "whole_organism"); 
			}	
			//LinkedList<DescriptionTreatmentElement> structureElements = new LinkedList<DescriptionTreatmentElement>();
			//structureElements.add(structureElement);
			//result.addAll(establishSubject(structureElements, processingContextState));
			//skipFirstChunk = false; 
			
			
			
			//does not start with an organ (subject)
			//if(firstChunk.isOfChunkType(ChunkType.PP)) {
			//	lastElements.clear();
			///	List<AbstractParseTree> chunkTerminals = firstChunk.getTerminals();
			//	if(chunkTerminals.get(0).equals("with")) {
			//		Chunk organChunk = firstChunk.getChunkDFS(ChunkType.ORGAN);
			//		result.addAll(establishSubject(organChunk, processingContextState));
			//	} else {
			//		if(!(secondChunk.isOfChunkType(ChunkType.MAIN_SUBJECT_ORGAN)) && !(secondChunk.isOfChunkType(ChunkType.NP_LIST)) && 
//							!(secondChunk.isOfChunkType(ChunkType.NON_SUBJECT_ORGAN))) {
//						result.addAll(reestablishSubject(processingContextState));
//						skipFirstChunk = true;
//						return result;
//					}
//				}
//			} else {
//				if(firstChunk.isOfChunkType(ChunkType.MODIFIER) || firstChunk.isOfChunkType(ChunkType.CONSTRAINT))
//					processingContextState.getUnassignedConstraints().add(firstChunk);
//				result.addAll(reestablishSubject(processingContextState));
//				skipFirstChunk = true;
//				return result;
//			}
		}*/
		
		//log(LogLevel.DEBUG, "Skip first chunk " + skipFirstChunk);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}
	
	public int skipFirstNChunk() {
		return this.skipFirstNChunk;
	}
	
	public void setFirstSentence(){
		this.firstSentence = true;
	}
	
	public void unsetFirstSentence(){
		this.firstSentence = false;
	}

}
