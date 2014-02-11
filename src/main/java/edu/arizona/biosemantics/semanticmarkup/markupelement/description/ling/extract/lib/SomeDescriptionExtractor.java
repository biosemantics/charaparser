package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

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

	private Set<String> lifeStyles;
	
	private IFirstChunkProcessor firstChunkProcessor;
	private ILastChunkProcessor lastChunkProcessor;

	private IChunkProcessorProvider chunkProcessorProvider;
	
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
			ILastChunkProcessor lastChunkProcessor) {
		lifeStyles = glossary.getWords("life_style");
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
			processingContext.reset();
			Statement statement = new Statement();
			statement.setText(chunkCollector.getSentence());
			statement.setId("d" + descriptionNumber + "_s" + i);
			description.addStatement(statement);
			
			processingContext.setChunkCollector(chunkCollector);
			try {
				List<Element> descriptiveElements = getDescriptiveElements(processingContext);
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
	}

	private List<Element> getDescriptiveElements(ProcessingContext processingContext) {
		List<Element> result = new LinkedList<Element>();
		processingContext.setResult(result);

		ChunkCollector chunkCollector = processingContext.getChunkCollector();
		
		List<Chunk> chunks = chunkCollector.getChunks();
		ListIterator<Chunk> iterator = chunks.listIterator();
		processingContext.setChunkListIterator(iterator);
		
		log(LogLevel.DEBUG, "describe chunk using " + firstChunkProcessor.getDescription() + " ...");
		addToResult(result, firstChunkProcessor.process(chunks.get(0), processingContext));
		log(LogLevel.DEBUG, "result:\n" + result);
		while(iterator.hasNext()) {
			if(!iterator.hasPrevious() && firstChunkProcessor.skipFirstChunk()) {
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
		createWholeOrganismDescription(result);
		createMayBeSameRelations(result, processingContext);
		return result;
	}
	
	private void addToResult(List<Element> result,
			List<? extends Element> toAdd) {
		for(Element element : toAdd)
			if(!result.contains(element) && (element.isStructure() || element.isRelation()))
				result.add(element);
	}


	private void createMayBeSameRelations(List<Element> result, ProcessingContext processingContext) {
		HashMap<String, Set<String>> names = new HashMap<String, Set<String>>();
		for (Element element : result) {
			if (element.isStructure()) {
				Structure structure = (Structure)element;
				String name = structure.getName();
				
				/*if (element.containsAttribute("constraintType"))
					name = element.getCongetAttribute("constraintType") + " " + name;
				if (element.containsAttribute("constraintParentOrgan"))
					name = element.getAttribute("constraintParentOrgan") + " " + name;
				if (element.containsAttribute("constraint"))
					name = element.getAttribute("constraint") + " " + name;*/
				
				if (structure.getConstraint() != null && !structure.getConstraint().isEmpty())
					name = structure.getConstraint() + " " + name;
				
				String id = structure.getId();
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
							Relation relationElement = new Relation();
							relationElement.setName("may_be_the_same");
							relationElement.setFrom(idA);
							relationElement.setTo(idB);
							relationElement.setNegation(String.valueOf(false));
							relationElement.setId("r" + String.valueOf(processingContext.fetchAndIncrementRelationId(relationElement)));	
						}
					}
					idIterator.remove();
				}
			}
		}
	}


	private void createWholeOrganismDescription(List<Element> result) {
		Structure wholeOrganism = new Structure();
		for(Element element : result) {
			if(element.isStructure() && ((Structure)element).getName().equals("whole_organism")) {
				wholeOrganism = (Structure)element;
				break;
			}
		}
		
		boolean modifiedWholeOrganism = false;
		Iterator<Element> resultIterator = result.iterator();
		while(resultIterator.hasNext()) {
			Element element = resultIterator.next();
			if(element.isStructure()) {
				Structure structure = (Structure)element;
				String name = structure.getName();
				if(lifeStyles.contains(name)) {
					
					/*if(element.containsAttribute("constraintType")) {
						name = element.getAttribute("constraintType") + " " + name;
					}
					if(element.containsAttribute("constraintParentOrgan")) {
						name = element.getAttribute("constraintParentOrgan") + " " + name;
					}*/
					
					wholeOrganism.appendAlterName(structure.getAlterName());
					wholeOrganism.appendConstraint(structure.getConstraint());
					wholeOrganism.appendConstraintId(structure.getConstraintId());
					wholeOrganism.appendGeographicalConstraint(structure.getGeographicalConstraint());
					wholeOrganism.appendId(structure.getId());
					wholeOrganism.appendInBracket(structure.getInBracket());
					wholeOrganism.appendInBrackets(structure.getInBrackets());
					wholeOrganism.appendNotes(structure.getNotes());
					wholeOrganism.appendOntologyId(structure.getOntologyId());
					wholeOrganism.appendParallelismConstraint(structure.getParallelismConstraint());
					wholeOrganism.appendProvenance(structure.getProvenance());
					wholeOrganism.appendTaxonConstraint(structure.getTaxonConstraint());
					
					LinkedHashSet<Character> characters = structure.getCharacters();
					wholeOrganism.addCharacters(characters);
					
					wholeOrganism.setName("whole_organism");
					
					Character character = new Character();
					character.setName("life_style");
					character.setValue(name);
					wholeOrganism.addCharacter(character);
					modifiedWholeOrganism = true;
					
					resultIterator.remove();
				}
			}	
		}
		
		if(modifiedWholeOrganism)
			result.add(wholeOrganism);
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