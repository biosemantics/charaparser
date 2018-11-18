package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.google.inject.Inject;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessorProvider;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IFirstChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.ILastChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.IDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * SomeDescriptionExtractor poses an IDescriptionExtractor
 * @author rodenhausen
 */
public class SomeDescriptionExtractor implements IDescriptionExtractor {

	private IFirstChunkProcessor firstChunkProcessor;
	private ILastChunkProcessor lastChunkProcessor;
	private IChunkProcessorProvider chunkProcessorProvider;

	//private Set<String> possess;
	//private IOntology ontology;

	//necessary to obtain unique IDs in files with multiple descriptions
	//return from a call to extract to some higher entity and inject back if ever required to
	//e.g. have a stateless DescriptionExtractor to e.g. run multiple descriptionextractors alongside
	private int structureId;
	private int relationId;

	/**
	 *
	 * @param chunkProcessorProvider
	 * @param firstChunkProcessor
	 * @param lastChunkProcessor
	 */
	@Inject
	public SomeDescriptionExtractor(
			IChunkProcessorProvider chunkProcessorProvider,
			IFirstChunkProcessor firstChunkProcessor,
			ILastChunkProcessor lastChunkProcessor) {
		this.chunkProcessorProvider = chunkProcessorProvider;
		this.firstChunkProcessor = firstChunkProcessor;
		this.lastChunkProcessor = lastChunkProcessor;
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
			
			//Hong 11/18/18 retrieve the appropriate statement by matching text
			Statement statement = null;
			String statementId = null;
			for(Statement s: description.getStatements()){
				if(s.getText().compareToIgnoreCase(chunkCollector.getOriginalSentence())==0){
					statement = s;
					statementId = s.getId();
					break;
				}
			}
			if(statement == null || statementId==null){
				log(LogLevel.ERROR, "Failed to find the target statement matching text : "+chunkCollector.getSentence() + "\n" +
						"Sentence is contained in file: " + chunkCollector.getSource());
				log(LogLevel.ERROR,  "No parsed results output for " +chunkCollector.getSource());
				return;
			}
			
			/*Statement statement = new Statement();
			//statement.setText(chunkCollector.getSentence());
			statement.setText(chunkCollector.getOriginalSentence());
			String statementId = "d" + descriptionNumber + "_s" + i;
			statement.setId(statementId);
			description.addStatement(statement);*/

			processingContext.setChunkCollector(chunkCollector);
			try {
				List<Element> descriptiveElements = getDescriptiveElements(processingContext, chunkCollector.getSentence(), i); //chunk to xml
				//set src attributes
				for(Element element : descriptiveElements) {
					if(element.isRelation()){
						if(((Relation)element).getSrc()==null)
							((Relation)element).appendSrc(statementId);
						statement.addRelation((Relation)element);
					} else if(element.isStructure()){
						if(((BiologicalEntity)element).getSrc()==null)
							((BiologicalEntity)element).appendSrc(statementId);
						statement.addBiologicalEntity((BiologicalEntity)element);

						Iterator<Character> it = ((BiologicalEntity)element).getCharacters().iterator();
						while(it.hasNext()){
							Character ch = it.next();
							if(ch.getSrc()==null)
								ch.appendSrc(statementId); // when extracting src info from xml output, need to take into account of the "src"s of the entity and the character
						}
					}
				}
			} catch (Exception e) {
				log(LogLevel.ERROR, "Problem extracting markup elements from sentence: " + chunkCollector.getSentence() + "\n" +
						"Sentence is contained in file: " + chunkCollector.getSource(),
						e);
			}
		}
		this.structureId = processingContext.getStructureId();
		this.relationId = processingContext.getRelationId();
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
		if(sentence.contains(":")){
			if(sentIndex==0){
				//identify and skip headings
				skip = firstChunkProcessor.skipHeading(chunks);
			}
			firstChunkProcessor.setContainColon();
		}else{
			firstChunkProcessor.unsetContainColon();
		}
		if(sentIndex==0) firstChunkProcessor.setFirstSentence();
		else firstChunkProcessor.unsetFirstSentence();

		//addToResult(result, firstChunkProcessor.process(chunks.get(0), processingContext)); //process subject
		addToResult(result, firstChunkProcessor.process(chunks.get(skip), processingContext)); //process subject?
		log(LogLevel.DEBUG, "result:\n" + result);



		while(iterator.hasNext()) {
			if(!iterator.hasPrevious() && skip+firstChunkProcessor.skipFirstNChunk()>0) {
				for(int i = 0; i < skip+firstChunkProcessor.skipFirstNChunk(); i++){
					iterator.next();
				}
				continue;
			}
			if(iterator.hasNext()) {
				addToResult(result, describeChunk(processingContext));
			}
			log(LogLevel.DEBUG, "result:\n" + result);
		}

		addToResult(result, lastChunkProcessor.process(processingContext));
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
