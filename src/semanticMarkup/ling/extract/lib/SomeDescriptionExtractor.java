package semanticMarkup.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionType;
import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.IChunkProcessor;
import semanticMarkup.ling.extract.IChunkProcessorProvider;
import semanticMarkup.ling.extract.IDescriptionExtractor;
import semanticMarkup.ling.extract.IFirstChunkProcessor;
import semanticMarkup.ling.extract.ILastChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;

public class SomeDescriptionExtractor implements IDescriptionExtractor {

	private Set<String> lifeStyles;
	
	private IFirstChunkProcessor firstChunkProcessor;
	private ILastChunkProcessor lastChunkProcessor;

	private IChunkProcessorProvider chunkProcessorProvider;
	
	@Inject
	public SomeDescriptionExtractor(IGlossary glossary, 
			IChunkProcessorProvider chunkProcessorProvider, 
			IFirstChunkProcessor firstChunkProcessor, 
			ILastChunkProcessor lastChunkProcessor) {
		lifeStyles = glossary.getWords("life_style");
		this.chunkProcessorProvider = chunkProcessorProvider;
		this.firstChunkProcessor = firstChunkProcessor;
		this.lastChunkProcessor = lastChunkProcessor;
	}

	
	@Override
	public ContainerTreatmentElement extract(List<ChunkCollector> chunkCollectors) {
		ProcessingContext processingContext = new ProcessingContext();
		processingContext.setChunkProcessorsProvider(chunkProcessorProvider);

		// one chunk collector for one statement / sentence
		// this method is called per treatment
		// the treatmentElement returned will be set in the treatment instead of the one that was there before
		ContainerTreatmentElement descriptionTreatmentElement = new ContainerTreatmentElement("description");
		
		//going through all sentences
		for(ChunkCollector chunkCollector : chunkCollectors) {
			processingContext.reset();
			DescriptionTreatmentElement statement = new DescriptionTreatmentElement(DescriptionType.STATEMENT);
			statement.setProperty("text", chunkCollector.getSentence());
			statement.setProperty("source", chunkCollector.getSource());
			descriptionTreatmentElement.addTreatmentElement(statement);
			
			processingContext.setChunkCollector(chunkCollector);
			try {
				List<DescriptionTreatmentElement> descriptiveTreatmentElements = getDescriptiveTreatmentElements(processingContext);
				for(DescriptionTreatmentElement descriptiveTreatmentElement : descriptiveTreatmentElements) {
					statement.addTreatmentElement(descriptiveTreatmentElement);
				}
			} catch (Exception e) {
				log(LogLevel.ERROR, e);
				e.printStackTrace();
				System.out.println(chunkCollector);
			}
		}
		return descriptionTreatmentElement;
	}

	private List<DescriptionTreatmentElement> getDescriptiveTreatmentElements(ProcessingContext processingContext) {
		List<DescriptionTreatmentElement> result = new ArrayList<DescriptionTreatmentElement>();
		processingContext.setResult(result);

		ChunkCollector chunkCollector = processingContext.getChunkCollector();
		
		List<Chunk> chunks = chunkCollector.getChunks();
		ListIterator<Chunk> iterator = chunks.listIterator();
		processingContext.setChunkListIterator(iterator);
		
		log(LogLevel.DEBUG, "describe chunk using " + firstChunkProcessor.getDescription() + " ...");
		addToResult(result, firstChunkProcessor.process(chunks.get(0), processingContext));
		log(LogLevel.DEBUG, "result " + result);
		while(iterator.hasNext()) {
			if(!iterator.hasPrevious() && firstChunkProcessor.skipFirstChunk()) {
				iterator.next();
				continue;
			}
			if(iterator.hasNext()) {
				addToResult(result, describeChunk(processingContext));
			}
			log(LogLevel.DEBUG, "result " + result);
		}
		
		addToResult(result, lastChunkProcessor.process(processingContext));
		
		/*StructureContainerTreatmentElement structureElement = new StructureContainerTreatmentElement("structureName", "Id", "constraint");
		structureElement.addTreatmentElement(new CharacterTreatmentElement("characterName", "value", "modifier", "constraint", 
				"constraintId", "characterType", "from", "to", true));
		RelationTreatmentElement relationElement = new RelationTreatmentElement("relationName", "id", "from", "to", false);
		result.add(structureElement);
		result.add(relationElement);*/
		createWholeOrganismDescription(result);
		createMayBeSameRelations(result, processingContext.getCurrentState());
		return result;
	}
	
	private void addToResult(List<DescriptionTreatmentElement> result,
			List<DescriptionTreatmentElement> toAdd) {
		for(DescriptionTreatmentElement element : toAdd)
			if(!result.contains(element) && (element.isOfDescriptionType(DescriptionType.STRUCTURE) || element.isOfDescriptionType(DescriptionType.RELATION)))
				result.add(element);
	}


	private void createMayBeSameRelations(List<DescriptionTreatmentElement> result, ProcessingContextState processingContextState) {
		HashMap<String, Set<String>> names = new HashMap<String, Set<String>>();
		for (DescriptionTreatmentElement element : result) {
			if (element.isOfDescriptionType(DescriptionType.STRUCTURE)) {
				String name = element.getProperty("name");
				if (element.containsProperty("constraintType"))
					name = element.getProperty("constraintType") + " " + name;
				if (element.containsProperty("constraintParentOrgan"))
					name = element.getProperty("constraintParentOrgan") + " " + name;
				if (element.containsProperty("constraint"))
					name = element.getProperty("constraint") + " " + name;
				
				String id = element.getProperty("id");
				if(!names.containsKey(name)) 
					names.put(name, new HashSet<String>());
				names.get(name).add(id);
			}
		}
		
		for(Entry<String, Set<String>> nameEntry : names.entrySet()) {
			Set<String> ids = nameEntry.getValue();
			if(ids.size() > 1) {
				Iterator<String> idIterator = ids.iterator();
				while(idIterator.hasNext()) {
					String idA = idIterator.next();
					for(String idB : ids) {
						if(!idA.equals(idB)) {
							DescriptionTreatmentElement relationElement = new DescriptionTreatmentElement(DescriptionType.RELATION);
							relationElement.setProperty("name", "may_be_the_same");
							relationElement.setProperty("from", idA);
							relationElement.setProperty("to", idB);
							relationElement.setProperty("negation", String.valueOf(false));
							relationElement.setProperty("id", "r" + String.valueOf(processingContextState.fetchAndIncrementRelationId(relationElement)));	
						}
					}
					idIterator.remove();
				}
			}
		}
	}


	private void createWholeOrganismDescription(List<DescriptionTreatmentElement> result) {
		DescriptionTreatmentElement wholeOrganism = new DescriptionTreatmentElement(DescriptionType.STRUCTURE);
		for(DescriptionTreatmentElement element : result) {
			if(element.isOfDescriptionType(DescriptionType.STRUCTURE) && element.getProperty("name").equals("whole_organism")) {
				wholeOrganism = element;
				break;
			}
		}
		
		boolean modifiedWholeOrganism = false;
		Iterator<DescriptionTreatmentElement> resultIterator = result.iterator();
		while(resultIterator.hasNext()) {
			DescriptionTreatmentElement element = resultIterator.next();
			if(element.isOfDescriptionType(DescriptionType.STRUCTURE)) {
				String name = element.getProperty("name");
				if(lifeStyles.contains(name)) {
					
					if(element.containsProperty("constraintType")) {
						name = element.getProperty("constraintType") + " " + name;
					}
					if(element.containsProperty("constraintParentOrgan")) {
						name = element.getProperty("constraintParentOrgan") + " " + name;
					}
					
					HashMap<String, String> properties = element.getProperties();
					for(Entry<String, String> property : properties.entrySet()) {
						wholeOrganism.appendProperty(property.getKey(), property.getValue());
					}
					
					List<TreatmentElement> childElements = element.getTreatmentElements();
					wholeOrganism.addTreatmentElements(childElements);
					
					wholeOrganism.setProperty("name", "whole_organism");
					
					DescriptionTreatmentElement character = new DescriptionTreatmentElement(DescriptionType.CHARACTER);
					character.setProperty("name", "life_style");
					character.setProperty("value", name);
					wholeOrganism.addTreatmentElement(character);
					modifiedWholeOrganism = true;
					
					resultIterator.remove();
				}
			}	
		}
		
		if(modifiedWholeOrganism)
			result.add(wholeOrganism);
	}


	private List<DescriptionTreatmentElement> describeChunk(ProcessingContext processingContext) {
		List<DescriptionTreatmentElement> result = new ArrayList<DescriptionTreatmentElement>();
		
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