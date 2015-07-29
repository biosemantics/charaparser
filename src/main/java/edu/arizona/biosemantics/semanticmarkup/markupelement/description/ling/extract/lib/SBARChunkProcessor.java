package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;





import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * SBARChunkProcessor processes chunks of ChunkType.SBAR, ChunkType.THAT, ChunkType.WHERE, and ChunkType.WHEN
 * @author rodenhausen
 */
public class SBARChunkProcessor extends AbstractChunkProcessor  {

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
	public SBARChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		
		//find the real subject of the clause, which is the last organ before the clause starts -- this may not be the case for some clauses like 'grey when young' or 'red where injured'. 		
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<BiologicalEntity> savedSubjects = processingContextState.getSubjects();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		if(lastElements.size()>0 && lastElements.getLast().isStructure()) {
			List<Element> latestStructures = latest(BiologicalEntity.class, lastElements);
			LinkedList<BiologicalEntity> subjects = new LinkedList<BiologicalEntity>();
			for(Element structure : latestStructures) 
				subjects.add((BiologicalEntity)structure);
			processingContextState.setSubjects(subjects);
		} else {//why need the follow block?
			ListIterator<Chunk> chunkIterator = processingContext.getChunkListIterator();
			chunkIterator.previous();
			
			//int p = cs.getPointer()-2;
			Chunk last = null; //the chunk before ck
			//int i=2;
			int i=1;
			do {
				last = chunkIterator.previous();
				i++;
			} while(!last.getTerminalsText().matches(".*?\\S.*"));
			
			
			for(int j=0; j<i; j++)  //return chunkIterator to the original state
				chunkIterator.next();
			
			int constraintId;
			if(last.containsChunkType(ChunkType.ORGAN)) {
				constraintId = processingContext.getStructureId() - 1;
				BiologicalEntity lastStructure = processingContext.getStructure(constraintId);
				LinkedList<BiologicalEntity> subjects = new LinkedList<BiologicalEntity>();
				subjects.add(lastStructure);
				processingContextState.setSubjects(subjects);
			}else{
				//do nothing
				System.err.println("no structure element found for the SBARChunk, use subjects instead ");
				//this only works for situations where states before subjects got reintroduced after subjects in skiplead
				//this will not work for misidentified nouns before "that/which" statements, in "of/among which", and other cases
			}
		}
		
		//collect content of the chunk
		List<Chunk> content = new ArrayList<Chunk>();
		for(Chunk childChunk : chunk.getChunks()) {
			if(!childChunk.getTerminalsText().matches("which|that|where|when") )content.add(childChunk);
		}

		
		//when => all when clauses should be included as character modifiers.
		//some should be the modifier of the last character
		//others should be the unassignedmodifier of the next character 
		if(chunk.isOfChunkType(ChunkType.WHEN)) {
			List<Chunk> modifiers = assembleModifierContent(chunk, content);//reset content
			
			//int end = ck.toString().indexOf(",") > 0? ck.toString().indexOf(",") : ck.toString().indexOf(".");
			//String modifier = chunk.getTerminalsText().substring(0, end).trim();//when mature, 
			//String contentString = chunk.getTerminalsText().substring(end).replaceAll("^\\W+", "").trim();
			Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, modifiers);
			//Chunk contentChunk = new Chunk(ChunkType.UNASSIGNED, content);
			
			//attach modifier to the last characters
			if(!lastElements.isEmpty() && lastElements.getLast().isCharacter()) {
				for(Element lastElement : lastElements)
					if(lastElement.isCharacter())
						((Character)lastElement).setModifier(modifierChunk.getTerminalsText());
			} else { 
				//if(newcs!=null) 
					//processingContext.getUnassignedModifiers().add(modifierChunk);
					//newcs.unassignedmodifier = "m["+modifier+"]";//this when clause is a modifier for the subclause
				//else{
					//if(lastElements.getLast().getName().compareTo("comma")==0){
						//this.latestelements.remove(this.latestelements.size()-1); 
						//remove comma, so what follows when-clause may refer to the structure mentioned before as in <apex> 
						//r[p[of] o[(scape)]] , s[when laid {straight} {back} r[p[from] o[its (insertion)]] ,] 
						//just touches the {midpoint} r[p[of] o[the {posterior} (margin)]] r[p[in] o[(fullface)]] {view} ; 
					//}
				processingContextState.getUnassignedModifiers().add(modifierChunk);// = "m["+modifier.replaceAll("(\\w+\\[|\\]|\\(|\\)|\\{|\\})", "")+"]";
				//}
			}
		}

		//where => 
		//1. some should be used as the subject for the where clause to be process next (done at the beginning)
		//2. some should be character unassignedconstraints for the next character
		//3. some should be the modifier of the previous character
		if(chunk.isOfChunkType(ChunkType.WHERE)) {
			//retrieve the last non-comma, non-empty chunk					
			ListIterator<Chunk> chunkIterator = processingContext.getChunkListIterator();
			chunkIterator.previous();
			
			Chunk last = null; //the content-bearing chunk before 'chunk'
			//int i=2;
			int i=1;
			do {
				last = chunkIterator.previous();
				i++;
			} while(!last.getTerminalsText().matches(".*?\\w.*"));
			
			for(int j=0; j<i; j++) //return chunkIterator to the original state
				chunkIterator.next(); 
			
			//update processingContextState for future processing
			if(last.containsChunkType(ChunkType.ORGAN)) {
				int constraintId = processingContext.getStructureId() - 1;				
				processingContextState.setClauseModifierContraint(last.getTerminalsText()); 
				processingContextState.setClauseModifierContraintId("o"+constraintId);
			}
			//add modifier to the last elements that've already been generated
			if(lastElements.size()>0 && lastElements.getLast().isCharacter()) {
				List<Chunk> modifiers = assembleModifierContent(chunk, content);
				Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, modifiers);
				for(Element lastElement : lastElements)
					if(lastElement.isCharacter())
						((Character)lastElement).setModifier(modifierChunk.getTerminalsText());
			}
		}
		
		//process content for that and which (maybe when or where in cases [.;,:] are involved)
		for(Chunk contentChunk : content)
			result.addAll(describeChunk(contentChunk, processingContext));
		
		
		//processingContextState.setClauseModifierContraint(null);
		//processingContextState.setClauseModifierContraintId("-1"); // or null?
		//processingContextState.setSubjects(savedSubjects);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}

	private List<Chunk> assembleModifierContent(Chunk chunk, List<Chunk> content) {
		//rewrite content and its chunkedTokens
		List<Chunk> modifiers = new ArrayList<Chunk>();
		content.clear(); //reset content to empty
		boolean foundPunctuation = false;
		for(Chunk childChunk : chunk.getChunks()) {
			for(AbstractParseTree terminal : childChunk.getTerminals()) {
				if(terminal.getTerminalsText().matches("[\\.,:;]")) { //terminals before  puncts are modifiers
					foundPunctuation = true;
					break;
				}
				if(!foundPunctuation)
					modifiers.add(terminal);
			}
			if(foundPunctuation) //terminals after puncts, if there is any, need to be process further
				content.add(childChunk);
		}
		return modifiers;
	}

	private List<? extends Element> describeChunk(Chunk chunk, ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		ChunkType chunkType = chunk.getChunkType();
		IChunkProcessor chunkProcessor = processingContext.getChunkProcessor(chunkType);
		result.addAll(chunkProcessor.process(chunk, processingContext));
		return result;
	}
	
}
