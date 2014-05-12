package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;








import com.google.inject.Inject;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessorProvider;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IFirstChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.ILastChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.IDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib.NonOntologyBasedStandardizer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib.StructureNameStandardizer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib.TerminologyStandardizer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * SomeDescriptionExtractor poses an IDescriptionExtractor
 * @author rodenhausen
 */
public class SomeDescriptionExtractor implements IDescriptionExtractor {

	private IFirstChunkProcessor firstChunkProcessor;
	private ILastChunkProcessor lastChunkProcessor;

	private IChunkProcessorProvider chunkProcessorProvider;
	private ICharacterKnowledgeBase characterKnowledgeBase;
	private IGlossary glossary;
	
	//necessary to obtain unique IDs in files with multiple descriptions
	//return from a call to extract to some higher entity and inject back if ever required to
	//e.g. have a stateless DescriptionExtractor to e.g. run multiple descriptionextractors alongside
	private int structureId;
	private int relationId;
	/**
	 * @param glossary
	 * @param chunkProcessorProvider
	 * @param firstChunkProcessor
	 * @param lastChunkProcessor
	 */
	@Inject
	public SomeDescriptionExtractor(IGlossary glossary, 
			IChunkProcessorProvider chunkProcessorProvider, 
			IFirstChunkProcessor firstChunkProcessor, 
			ILastChunkProcessor lastChunkProcessor, ICharacterKnowledgeBase characterKnowledgeBase) {
		this.glossary = glossary;
		this.chunkProcessorProvider = chunkProcessorProvider;
		this.firstChunkProcessor = firstChunkProcessor;
		this.lastChunkProcessor = lastChunkProcessor;
		this.characterKnowledgeBase = characterKnowledgeBase;
	}

	
	@Override
	public void extract(Description description, int descriptionNumber, List<ChunkCollector> chunkCollectors) {
		ProcessingContext processingContext = new ProcessingContext(structureId, relationId);
		processingContext.setChunkProcessorsProvider(chunkProcessorProvider);

		// one chunk collector for one statement / sentence
		// this method is called per treatment
		
		//going through all sentences
		
		for(int i=0; i<chunkCollectors.size(); i++) {
			ChunkCollector chunkCollector = chunkCollectors.get(i);
			//processingContext.setChunkCollectors(chunkCollectors);
			processingContext.reset();
			Statement statement = new Statement();
			statement.setText(chunkCollector.getSentence());
			statement.setId("d" + descriptionNumber + "_s" + i);
			description.addStatement(statement);
			
			processingContext.setChunkCollector(chunkCollector);
			try {
				List<Element> descriptiveElements = getDescriptiveElements(processingContext, chunkCollector.getSentence(), i); //chunk to xml
				for(Element element : descriptiveElements) {
					if(element.isRelation())
						statement.addRelation((Relation)element);
					if(element.isStructure())
						statement.addStructure((Structure)element);
				}
			} catch (Exception e) {
				log(LogLevel.ERROR, "Problem extracting markup elements from sentence: " + chunkCollector.getSentence() + "\n" +
						"Sentence is contained in file: " + chunkCollector.getSource(),
						e);
			}
		}
		this.structureId = processingContext.getStructureId();
		this.relationId = processingContext.getRelationId();
		
		System.out.println("====1====");
		for(Statement statement: description.getStatements()){
		System.out.println(statement.toString());
	    }
		
		List<Element> xml = new LinkedList<Element>();
		for(Statement s: description.getStatements()){
			xml.addAll(s.getStructures());
			xml.addAll(s.getRelations());
		}
		
		new StructureNameStandardizer().standardize((LinkedList<Element>) xml);
		
	}
	




	private List<Element> getDescriptiveElements(ProcessingContext processingContext, String sentence, int sentIndex) {
		List<Element> result = new LinkedList<Element>();
		processingContext.setResult(result);

		ChunkCollector chunkCollector = processingContext.getChunkCollector();
		
		List<Chunk> chunks = chunkCollector.getChunks();
		ListIterator<Chunk> iterator = chunks.listIterator(); 
		processingContext.setChunkListIterator(iterator);
		
		log(LogLevel.DEBUG, "describe chunk using " + firstChunkProcessor.getDescription() + " ...");
		int skip = 0;
		if(sentIndex==0 && sentence.contains(":")){
			//identify and skip headings
			skip = firstChunkProcessor.skipHeading(chunks);
		}
		//addToResult(result, firstChunkProcessor.process(chunks.get(0), processingContext)); //process subject
		addToResult(result, firstChunkProcessor.process(chunks.get(skip), processingContext)); //process subject?
		log(LogLevel.DEBUG, "result:\n" + result);
		while(iterator.hasNext()) {
			if(!iterator.hasPrevious() && skip+firstChunkProcessor.skipFirstNChunk()>0) {
				for(int i = 0; i < skip+firstChunkProcessor.skipFirstNChunk(); i++)
					iterator.next();
				continue;
			}
			if(iterator.hasNext()) {
				addToResult(result, describeChunk(processingContext));
			}	
			log(LogLevel.DEBUG, "result:\n" + result);
		}
		
		addToResult(result, lastChunkProcessor.process(processingContext));
		
		/*StructureContainerTreatmentElement structureElement = new StructureContainerTreatmentElement("structureName", "Id", "constraint");
		structureElement.addTreatmentElement(new CharacterTreatmentElement("characterName", "value", "modifier", "constraint", 
				"constraintId", "characterType", "from", "to", true));
		RelationTreatmentElement relationElement = new RelationTreatmentElement("relationName", "id", "from", "to", false);
		result.add(structureElement);
		result.add(relationElement);*/
		new NonOntologyBasedStandardizer(glossary, sentence, processingContext).standardize((LinkedList<Element>) result);
		new TerminologyStandardizer(this.characterKnowledgeBase).standardize(result);
		return result;
	}
	
	private void addToResult(List<Element> result,
			List<? extends Element> toAdd) {
		for(Element element : toAdd)
			if(!result.contains(element) && (element.isStructure() || element.isRelation()))
				result.add(element);
	}


	
	private List<Element> describeChunk(ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		
		ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
		Chunk chunk = chunkListIterator.next();
		ChunkType chunkType = chunk.getChunkType();
		
		IChunkProcessor chunkProcessor = chunkProcessorProvider.getChunkProcessor(chunkType);
		if(chunkProcessor!=null) {
			log(LogLevel.DEBUG, "chunk processor for chunkType " + chunkType + " found; proceed using " + chunkProcessor.getDescription() + " ...");
			result.addAll(chunkProcessor.process(chunk, processingContext));
		}
		if(chunkType.equals(ChunkType.UNASSIGNED))
			processingContext.getCurrentState().setUnassignedChunkAfterLastElements(true);
		else
			processingContext.getCurrentState().setUnassignedChunkAfterLastElements(false);
		
		return result;
	}
	
	@Override
	public String getDescription() {
		return "some description extractor";
	}
}