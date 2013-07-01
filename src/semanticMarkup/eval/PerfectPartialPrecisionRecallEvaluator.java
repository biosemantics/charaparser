package semanticMarkup.eval;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextProperties;

import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.eval.metric.PrecisionCalculator;
import semanticMarkup.eval.metric.RecallCalculator;
import semanticMarkup.eval.model.Description;
import semanticMarkup.eval.model.Structure;
import semanticMarkup.eval.model.Character;
import semanticMarkup.eval.model.Relation;
import semanticMarkup.eval.result.IEvaluationResult;
import semanticMarkup.eval.result.PerfectPartialPrecisionRecallEvaluationResult;
import semanticMarkup.eval.result.PrecisionRecallEvaluationResult;
import semanticMarkup.eval.result.PrecisionRecallResult;
import semanticMarkup.log.LogLevel;

public class PerfectPartialPrecisionRecallEvaluator implements IEvaluator {

	@Override
	public PerfectPartialPrecisionRecallEvaluationResult evaluate(String testDirectoryPath, String correctDirectoryPath) {		
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
		
		Unmarshaller testUnmarshaller = getTestUnmarshaller();
		Unmarshaller correctUnmarshaller = getCorrectUnmarshaller();
		if(testUnmarshaller == null || correctUnmarshaller == null)
			return result;
		
		File testDirectory = new File(testDirectoryPath);
		for(File testFile : testDirectory.listFiles()) {
			File correctFile = new File(correctDirectoryPath + File.separator + testFile.getName());
			if(!correctFile.exists()) {
				log(LogLevel.INFO, testFile.getName() + " does not have a matching correct file");
			} else {
				Description correctDescription;
				Description testDescription;
				try {
					correctDescription = (Description) correctUnmarshaller.unmarshal(correctFile);
					testDescription = (Description) testUnmarshaller.unmarshal(testFile);
				} catch(JAXBException e) {
					log(LogLevel.ERROR, "Could not unmarshall correct or test file in evaluation", e);
					continue;
				}
				
				String source = testFile.getName();
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
		}

		return result;
	}

	private Unmarshaller getCorrectUnmarshaller() {
		Unmarshaller correctUnmarshaller = null;
		try {
			Map<String, Object> properties = new HashMap<String, Object>(1);
			properties.put(JAXBContextProperties.OXM_METADATA_SOURCE , "resources" + File.separator + "eval" + File.separator + "correctBindings.xml");
			JAXBContext correctJaxbContext = JAXBContext.newInstance(new Class[] {Description.class}, properties);
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
			JAXBContext testJaxbContext = JAXBContext.newInstance(new Class[] {Description.class}, properties);
			testUnmarshaller = testJaxbContext.createUnmarshaller(); 

		} catch(JAXBException e) {
			log(LogLevel.ERROR, "Could not create JAXBContext or Unmarshaller for evaluation", e);
		}
		return testUnmarshaller;
	}

	private PrecisionRecallResult evaluatePartialRelation(List<Relation> testRelations, List<Relation> correctRelations) {
		IMatcher<Relation> partialRelationMatcher = new semanticMarkup.eval.matcher.partial.RelationMatcher();
		PrecisionCalculator<Relation> precisionCalculator = new PrecisionCalculator<Relation>(partialRelationMatcher);
		RecallCalculator<Relation> recallCalculator = new RecallCalculator<Relation>(partialRelationMatcher);
		double precision = precisionCalculator.getResult(testRelations, correctRelations);
		double recall = recallCalculator.getResult(testRelations, correctRelations);
		return new PrecisionRecallResult(precision, recall);
	}

	private PrecisionRecallResult evaluatePerfectRelation(List<Relation> testRelations, List<Relation> correctRelations) {
		//perfect relation evaluation
		IMatcher<Relation> perfectRelationMatcher = new semanticMarkup.eval.matcher.perfect.RelationMatcher();
		PrecisionCalculator<Relation> precisionCalculator = new PrecisionCalculator<Relation>(perfectRelationMatcher);
		RecallCalculator<Relation> recallCalculator = new RecallCalculator<Relation>(perfectRelationMatcher);
		double precision = precisionCalculator.getResult(testRelations, correctRelations);
		double recall = recallCalculator.getResult(testRelations, correctRelations);
		return new PrecisionRecallResult(precision, recall);
	}

	private PrecisionRecallResult evaluatePartialCharacter(List<Character> testCharacters, List<Character> correctCharacters) {
		IMatcher<Character> partialCharacterMatcher = new semanticMarkup.eval.matcher.partial.CharacterMatcher();		
		PrecisionCalculator<Character> precisionCalculator = new PrecisionCalculator<Character>(partialCharacterMatcher);
		RecallCalculator<Character> recallCalculator = new RecallCalculator<Character>(partialCharacterMatcher);
		double precision = precisionCalculator.getResult(testCharacters, correctCharacters);
		double recall = recallCalculator.getResult(testCharacters, correctCharacters);
		return new PrecisionRecallResult(precision, recall);
	}

	private PrecisionRecallResult evaluatePerfectCharacter(List<Character> testCharacters, List<Character> correctCharacters) {
		IMatcher<Character> perfectCharacterMatcher = new semanticMarkup.eval.matcher.perfect.CharacterMatcher();		
		PrecisionCalculator<Character> precisionCalculator = new PrecisionCalculator<Character>(perfectCharacterMatcher);
		RecallCalculator<Character> recallCalculator = new RecallCalculator<Character>(perfectCharacterMatcher);
		double precision = precisionCalculator.getResult(testCharacters, correctCharacters);
		double recall = recallCalculator.getResult(testCharacters, correctCharacters);
		return new PrecisionRecallResult(precision, recall);
	}

	private PrecisionRecallResult evaluatePartialStructure(List<Structure> testStructures, List<Structure> correctStructures) {
		IMatcher<Structure> partialStructureMatcher = new semanticMarkup.eval.matcher.partial.StructureMatcher();
		PrecisionCalculator<Structure> precisionCalculator = new PrecisionCalculator<Structure>(partialStructureMatcher);
		RecallCalculator<Structure> recallCalculator = new RecallCalculator<Structure>(partialStructureMatcher);
		double precision = precisionCalculator.getResult(testStructures, correctStructures);
		double recall = recallCalculator.getResult(testStructures, correctStructures);
		return new PrecisionRecallResult(precision, recall);	
	}

	private PrecisionRecallResult evaluatePerfectStructure(List<Structure> testStructures, List<Structure> correctStructures) {
		IMatcher<Structure> perfectStructureMatcher = new semanticMarkup.eval.matcher.perfect.StructureMatcher();
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
	
	public static void main(String[] args) {
		PerfectPartialPrecisionRecallEvaluator evaluator = new PerfectPartialPrecisionRecallEvaluator();
		PerfectPartialPrecisionRecallEvaluationResult result = evaluator.evaluate("test", "correct");
		System.out.println(result.toString());
	}

}
