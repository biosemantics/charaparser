package semanticMarkup.markupElement.description.run;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markupElement.description.eval.IDescriptionMarkupResultReader;
import semanticMarkup.markupElement.description.eval.io.IDescriptionMarkupEvaluator;
import semanticMarkup.markupElement.description.markup.DescriptionMarkupResult;
import semanticMarkup.run.AbstractRun;

import com.google.inject.Inject;
import com.google.inject.name.Named;


/**
 * A EvaluationRun evaluates marked up treatments using an IEvaluator
 * @author rodenhausen
 */
public class DescriptionEvaluationRun extends AbstractRun {

	private IDescriptionMarkupEvaluator evaluator;
	private IDescriptionMarkupResultReader testReader;
	private IDescriptionMarkupResultReader correctReader;
	private String testInputDirectory;
	private String correctInputDirectory;

	/**
	 * @param guiceModuleFile
	 * @param runRootDirectory
	 * @param runOutDirectory
	 * @param evaluator
	 * @param createdVolumeReader
	 * @param goldStandardReader
	 */
	@Inject
	public DescriptionEvaluationRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("EvaluationRun_Evaluator")IDescriptionMarkupEvaluator evaluator, 
			@Named("EvaluationRun_TestReader")IDescriptionMarkupResultReader testReader,
			@Named("EvaluationRun_CorrectReader")IDescriptionMarkupResultReader correctReader, 
			String testInputDirectory,
			String correctInputDirectory) {
		super(guiceModuleFile, runOutDirectory);
		this.testReader = testReader;
		this.correctReader = correctReader;
		this.evaluator = evaluator;
		this.testInputDirectory = testInputDirectory;
		this.correctInputDirectory = correctInputDirectory;
	}


	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Evaluating markup using " + evaluator.getDescription() + "...");
		log(LogLevel.INFO, "read marked up result using " + testReader.getClass());
		DescriptionMarkupResult testDescriptionMarkupResult = testReader.read(testInputDirectory);
		log(LogLevel.INFO, "read gold standard using " + correctReader.getClass());
		DescriptionMarkupResult correctDescriptionMarkupResult = correctReader.read(correctInputDirectory);
		
		evaluator.evaluate(testDescriptionMarkupResult, correctDescriptionMarkupResult);
		log(LogLevel.INFO, "Evaluation result: \n" + evaluator.getResult());
	}

}
