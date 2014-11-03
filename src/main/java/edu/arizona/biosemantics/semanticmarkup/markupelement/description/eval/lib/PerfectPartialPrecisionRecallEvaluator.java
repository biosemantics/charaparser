package edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.lib;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

import edu.arizona.biosemantics.semanticmarkup.eval.IEvaluator;
import edu.arizona.biosemantics.semanticmarkup.eval.matcher.IMatcher;
import edu.arizona.biosemantics.semanticmarkup.eval.metric.PrecisionCalculator;
import edu.arizona.biosemantics.semanticmarkup.eval.metric.RecallCalculator;
import edu.arizona.biosemantics.semanticmarkup.eval.result.IEvaluationResult;
import edu.arizona.biosemantics.semanticmarkup.eval.result.PerfectPartialPrecisionRecallEvaluationResult;
import edu.arizona.biosemantics.semanticmarkup.eval.result.PrecisionRecallEvaluationResult;
import edu.arizona.biosemantics.semanticmarkup.eval.result.PrecisionRecallResult;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.io.IDescriptionMarkupEvaluator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.DescriptionMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.markup.DistributionMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.markup.EcologyMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.markup.ElevationMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.markup.HabitatMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.markup.PhenologyMarkupResult;


public class PerfectPartialPrecisionRecallEvaluator implements IDescriptionMarkupEvaluator {

	private PerfectPartialPrecisionRecallEvaluationResult result;
	private DescriptionMarkupResult testDescriptionMarkupResult = null;
	private DescriptionMarkupResult correctDescriptionMarkupResult = null;
	
	@Override
	public PerfectPartialPrecisionRecallEvaluationResult evaluate(IMarkupResult testDescriptionMarkupResult,
			IMarkupResult correctDescriptionMarkupResult) {
		testDescriptionMarkupResult = null;
		correctDescriptionMarkupResult = null;
		testDescriptionMarkupResult.accept(this);
		correctDescriptionMarkupResult.accept(this);
		
		log(LogLevel.DEBUG, "evaluate using " + this.getDescription());
		PerfectPartialPrecisionRecallEvaluationResult result = new PerfectPartialPrecisionRecallEvaluationResult();
		PrecisionRecallEvaluationResult perfectStructureResult = new PrecisionRecallEvaluationResult();
		PrecisionRecallEvaluationResult partialStructureResult = new PrecisionRecallEvaluationResult();
		PrecisionRecallEvaluationResult perfectCharacterResult = new PrecisionRecallEvaluationResult();
		PrecisionRecallEvaluationResult partialCharacterResult = new PrecisionRecallEvaluationResult();
		PrecisionRecallEvaluationResult perfectRelationResult = new PrecisionRecallEvaluationResult();
		PrecisionRecallEvaluationResult partialRelationResult = new PrecisionRecallEvaluationResult();
		result.put("Perfect Structure", perfectStructureResult);
		result.put("Partial Structure", partialStructureResult);
		result.put("Perfect Character", perfectCharacterResult);
		result.put("Partial Character", partialCharacterResult);
		result.put("Perfect Relation", perfectRelationResult);
		result.put("Partial Relation", partialRelationResult);
				
		List<Description> testDescriptions = this.testDescriptionMarkupResult.getResult();
		List<Description> correctDescriptions = this.testDescriptionMarkupResult.getResult();
		
		if(testDescriptions.size() != correctDescriptions.size()) { 
			log(LogLevel.ERROR, "unequal number of descriptions in test and correct set");
			return result;
		}
		
		for(int i=0; i<testDescriptions.size(); i++) {
			Description correctDescription = correctDescriptions.get(i);
			Description testDescription = testDescriptions.get(i);
			String source = correctDescription.getSource();
			PrecisionRecallResult precisionRecallResult = evaluatePerfectStructure(testDescription.getStructures(), correctDescription.getStructures());
			perfectStructureResult.put(source, precisionRecallResult);
			precisionRecallResult = evaluatePartialStructure(testDescription.getStructures(), correctDescription.getStructures());
			partialStructureResult.put(source, precisionRecallResult);
			precisionRecallResult = evaluatePerfectCharacter(testDescription.getCharacters(), correctDescription.getCharacters());
			perfectCharacterResult.put(source, precisionRecallResult);
			precisionRecallResult = evaluatePartialCharacter(testDescription.getCharacters(), correctDescription.getCharacters());
			partialCharacterResult.put(source, precisionRecallResult);
			precisionRecallResult = evaluatePerfectRelation(testDescription.getRelations(), correctDescription.getRelations());
			perfectRelationResult.put(source, precisionRecallResult);
			precisionRecallResult = evaluatePartialRelation(testDescription.getRelations(), correctDescription.getRelations());
			partialRelationResult.put(source, precisionRecallResult);
		}

		this.result = result;
		log(LogLevel.DEBUG, result.toString());
		return result;
	}

	private Unmarshaller getCorrectUnmarshaller() {
		Unmarshaller correctUnmarshaller = null;
		try {
			Map<String, Object> properties = new HashMap<String, Object>(1);
			properties.put(JAXBContextProperties.OXM_METADATA_SOURCE , "resources" + File.separator + "eval" + File.separator + "correctBindings.xml");
			JAXBContext correctJaxbContext = JAXBContextFactory.createContext(new Class[] {Description.class}, properties);
			correctUnmarshaller = correctJaxbContext.createUnmarshaller(); 

		} catch(JAXBException e) {
			log(LogLevel.ERROR, "Could not create JAXBContext or Unmarshaller for evaluation", e);
		}
		return correctUnmarshaller;
	}

	private Unmarshaller getTestUnmarshaller() {
		Unmarshaller testUnmarshaller = null;
		try {
			Map<String, Object> properties = new HashMap<String, Object>(1);
			properties.put(JAXBContextProperties.OXM_METADATA_SOURCE , "resources" + File.separator + "eval" + File.separator + "testBindings.xml");
			JAXBContext testJaxbContext = JAXBContextFactory.createContext(new Class[] {Description.class}, properties);
			testUnmarshaller = testJaxbContext.createUnmarshaller(); 

		} catch(JAXBException e) {
			log(LogLevel.ERROR, "Could not create JAXBContext or Unmarshaller for evaluation", e);
		}
		return testUnmarshaller;
	}

	private PrecisionRecallResult evaluatePartialRelation(List<Relation> testRelations, List<Relation> correctRelations) {
		log(LogLevel.DEBUG, "evaluate partial relations");
		IMatcher<Relation> partialRelationMatcher = 
				new edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.matcher.partial.RelationMatcher();
		PrecisionCalculator<Relation> precisionCalculator = new PrecisionCalculator<Relation>(partialRelationMatcher);
		RecallCalculator<Relation> recallCalculator = new RecallCalculator<Relation>(partialRelationMatcher);
		double precision = precisionCalculator.getResult(testRelations, correctRelations);
		double recall = recallCalculator.getResult(testRelations, correctRelations);
		return new PrecisionRecallResult(precision, recall);
	}

	private PrecisionRecallResult evaluatePerfectRelation(List<Relation> testRelations, List<Relation> correctRelations) {
		log(LogLevel.DEBUG, "evaluate perfect relations");
		IMatcher<Relation> perfectRelationMatcher = 
				new edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.matcher.perfect.RelationMatcher();
		PrecisionCalculator<Relation> precisionCalculator = new PrecisionCalculator<Relation>(perfectRelationMatcher);
		RecallCalculator<Relation> recallCalculator = new RecallCalculator<Relation>(perfectRelationMatcher);
		double precision = precisionCalculator.getResult(testRelations, correctRelations);
		double recall = recallCalculator.getResult(testRelations, correctRelations);
		return new PrecisionRecallResult(precision, recall);
	}

	private PrecisionRecallResult evaluatePartialCharacter(List<Character> testCharacters, List<Character> correctCharacters) {
		log(LogLevel.DEBUG, "evaluate partial characters");
		IMatcher<Character> partialCharacterMatcher = 
				new edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.matcher.partial.CharacterMatcher();		
		PrecisionCalculator<Character> precisionCalculator = new PrecisionCalculator<Character>(partialCharacterMatcher);
		RecallCalculator<Character> recallCalculator = new RecallCalculator<Character>(partialCharacterMatcher);
		double precision = precisionCalculator.getResult(testCharacters, correctCharacters);
		double recall = recallCalculator.getResult(testCharacters, correctCharacters);
		return new PrecisionRecallResult(precision, recall);
	}

	private PrecisionRecallResult evaluatePerfectCharacter(List<Character> testCharacters, List<Character> correctCharacters) {
		log(LogLevel.DEBUG, "evaluate perfect characters");
		IMatcher<Character> perfectCharacterMatcher = 
				new edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.matcher.perfect.CharacterMatcher();		
		PrecisionCalculator<Character> precisionCalculator = new PrecisionCalculator<Character>(perfectCharacterMatcher);
		RecallCalculator<Character> recallCalculator = new RecallCalculator<Character>(perfectCharacterMatcher);
		double precision = precisionCalculator.getResult(testCharacters, correctCharacters);
		double recall = recallCalculator.getResult(testCharacters, correctCharacters);
		return new PrecisionRecallResult(precision, recall);
	}

	private PrecisionRecallResult evaluatePartialStructure(List<Structure> testStructures, List<Structure> correctStructures) {
		log(LogLevel.DEBUG, "evaluate partial structures");
		IMatcher<Structure> partialStructureMatcher = 
				new edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.matcher.partial.StructureMatcher();
		PrecisionCalculator<Structure> precisionCalculator = new PrecisionCalculator<Structure>(partialStructureMatcher);
		RecallCalculator<Structure> recallCalculator = new RecallCalculator<Structure>(partialStructureMatcher);
		double precision = precisionCalculator.getResult(testStructures, correctStructures);
		double recall = recallCalculator.getResult(testStructures, correctStructures);
		return new PrecisionRecallResult(precision, recall);	
	}

	private PrecisionRecallResult evaluatePerfectStructure(List<Structure> testStructures, List<Structure> correctStructures) {
		log(LogLevel.DEBUG, "evaluate perfect structures");
		IMatcher<Structure> perfectStructureMatcher =
				new edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.matcher.perfect.StructureMatcher();
		PrecisionCalculator<Structure> precisionCalculator = new PrecisionCalculator<Structure>(perfectStructureMatcher);
		RecallCalculator<Structure> recallCalculator = new RecallCalculator<Structure>(perfectStructureMatcher);
		double precision = precisionCalculator.getResult(testStructures, correctStructures);
		double recall = recallCalculator.getResult(testStructures, correctStructures);
		return new PrecisionRecallResult(precision, recall);
	}


	@Override
	public String getDescription() {
		return this.getClass().toString();
	}
	
	@Override
	public PerfectPartialPrecisionRecallEvaluationResult getResult() {
		return this.result;
	}
	
	public static void main(String[] args) {
		//PerfectPartialPrecisionRecallEvaluator evaluator = new PerfectPartialPrecisionRecallEvaluator();
		//PerfectPartialPrecisionRecallEvaluationResult result = evaluator.evaluate("test", "correct");
	}

	@Override
	public void visit(DescriptionMarkupResult descriptionMarkupResult) {
		if(testDescriptionMarkupResult == null)
			testDescriptionMarkupResult = descriptionMarkupResult;
		else
			correctDescriptionMarkupResult = descriptionMarkupResult;
	}

	@Override
	public void visit(HabitatMarkupResult habitatMarkupResult) {	}

	@Override
	public void visit(DistributionMarkupResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElevationMarkupResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(PhenologyMarkupResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EcologyMarkupResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(
			edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.markup.HabitatMarkupResult result) {
		// TODO Auto-generated method stub
		
	}
}
