package semanticMarkup.markupElement.description.run;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markupElement.description.eval.IDescriptionMarkupEvaluator;
import semanticMarkup.markupElement.description.eval.IDescriptionMarkupResultReader;
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
	private IDescriptionMarkupResultReader createdVolumeReader;
	private IDescriptionMarkupResultReader goldStandardReader;

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
			@Named("EvaluationRun_CreatedVolumeReader")IDescriptionMarkupResultReader createdVolumeReader,
			@Named("EvaluationRun_GoldStandardReader")IDescriptionMarkupResultReader goldStandardReader) {
		super(guiceModuleFile, runOutDirectory);
		this.createdVolumeReader = createdVolumeReader;
		this.goldStandardReader = goldStandardReader;
		this.evaluator = evaluator;
	}


	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Evaluating markup using " + evaluator.getDescription() + "...");
		log(LogLevel.INFO, "read marked up result using " + createdVolumeReader.getClass());
		DescriptionMarkupResult descriptionMarkupResult = createdVolumeReader.read();
		log(LogLevel.INFO, "read gold standard using " + goldStandardReader.getClass());
		DescriptionMarkupResult goldStandard = goldStandardReader.read();
		
		evaluator.evaluate(descriptionMarkupResult, goldStandard);
		log(LogLevel.INFO, "Evaluation result: \n" + evaluator.getResult());
	}

}
