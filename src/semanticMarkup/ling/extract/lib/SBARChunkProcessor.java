package semanticMarkup.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.IChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.markupElement.description.model.Character;
import semanticMarkup.markupElement.description.model.Structure;
import semanticMarkup.model.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * SBARChunkProcessor processes chunks of ChunkType.SBAR
 * @author rodenhausen
 */
public class SBARChunkProcessor extends AbstractChunkProcessor {

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
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		//LinkedList<DescriptionTreatmentElement> subjectsCopy = processingContext.getSubjects();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		if(lastElements.getLast().isStructure()) {
			processingContextState.setSubjects(latest(Structure.class, lastElements));
		} else {
			ListIterator<Chunk> chunkIterator = processingContext.getChunkListIterator();
			chunkIterator.previous();
			
			//int p = cs.getPointer()-2;
			Chunk last = null; //the chunk before ck??
			int i=2;
			do {
				last = chunkIterator.previous();
				i++;
			} while(!last.getTerminalsText().matches(".*?\\S.*"));
			for(int j=0; j<i; j++) 
				chunkIterator.next();
			
			int constraintId;
			if(last.containsChunkType(ChunkType.ORGAN)) {
				constraintId = processingContextState.getStructureId() - 1;
				Structure lastStructure = processingContextState.getStructure(constraintId);
				LinkedList<Structure> newSubjects = new LinkedList<Structure>();
				newSubjects.add(lastStructure);
				processingContextState.setSubjects(newSubjects);
			}else{
				//do nothing
				System.err.println("no structure element found for the SBARChunk, use subjects instead ");
				//this only works for situations where states before subjects got reintroduced after subjects in skiplead
				//this will not work for misidentified nouns before "that/which" statements, in "of/among which", and other cases
			}
		}
		
		Chunk connectorChunk = null;
		List<Chunk> content = new ArrayList<Chunk>();
		boolean selectFirst = true;
		for(Chunk childChunk : chunk.getChunks()) {
			if(selectFirst) {
				connectorChunk = childChunk;
				selectFirst = false;
			} else
				content.add(childChunk);
		}
		String connector = connectorChunk.getTerminals().get(0).getTerminalsText();
		
		if(connector.equals("when")) {
			//rewrite content and its chunkedTokens
			
			List<Chunk> modifiers = new ArrayList<Chunk>();
			content.clear();
			boolean foundPunctuation = false;
			for(Chunk childChunk : chunk.getChunks()) {
				for(AbstractParseTree terminal : childChunk.getTerminals()) {
					if(terminal.getTerminalsText().matches("[\\.,:;]")) {
						foundPunctuation = true;
						break;
					}
					if(!foundPunctuation)
						modifiers.add(terminal);
				}
				if(foundPunctuation)
					content.add(childChunk);
			}
			
			//int end = ck.toString().indexOf(",") > 0? ck.toString().indexOf(",") : ck.toString().indexOf(".");
			//String modifier = chunk.getTerminalsText().substring(0, end).trim();//when mature, 
			//String contentString = chunk.getTerminalsText().substring(end).replaceAll("^\\W+", "").trim();
			Chunk modifierChunk = new Chunk(ChunkType.MODIFIER, modifiers);
			//Chunk contentChunk = new Chunk(ChunkType.UNASSIGNED, content);
			
			//attach modifier to the last characters
			if(lastElements.getLast().isCharacter()) {
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

		if(connector.equals("where")) {
			//retrieve the last non-comma, non-empty chunk					
			
			ListIterator<Chunk> chunkIterator = processingContext.getChunkListIterator();
			chunkIterator.previous();
			
			Chunk last = null;
			int i=2;
			do {
				last = chunkIterator.previous();
				i++;
			} while(!last.getTerminalsText().matches(".*?\\w.*"));
			for(int j=0; j<i; j++) 
				chunkIterator.next();
		
			if(last.containsChunkType(ChunkType.ORGAN)) {
				int constraintId = processingContextState.getStructureId() - 1;				
				processingContextState.setClauseModifierContraint(last.getTerminalsText());
				processingContextState.setClauseModifierContraintId(String.valueOf(constraintId));
			}
		}
		
		for(Chunk contentChunk : content)
			result.addAll(describeChunk(contentChunk, processingContext));
		
		//annotateByChunk(newcs, false); //no need to updateLatestElements				
		//processingContext.setSubjects(subjectsCopy);//return to original status
		processingContextState.setClauseModifierContraint(null);
		processingContextState.setClauseModifierContraintId("-1"); 
		//return to original status
		//this.unassignedmodifiers = null;
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}

	private List<? extends Element> describeChunk(Chunk chunk, ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		ChunkType chunkType = chunk.getChunkType();
		IChunkProcessor chunkProcessor = processingContext.getChunkProcessor(chunkType);
		result.addAll(chunkProcessor.process(chunk, processingContext));
		return result;
	}
	
}
