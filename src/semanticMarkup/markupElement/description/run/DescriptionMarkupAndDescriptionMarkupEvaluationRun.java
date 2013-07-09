package semanticMarkup.markupElement.description.run;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markupElement.description.eval.IDescriptionMarkupEvaluator;
import semanticMarkup.markupElement.description.eval.IDescriptionMarkupResultReader;
import semanticMarkup.markupElement.description.markup.DescriptionMarkupResult;
import semanticMarkup.markupElement.description.markup.IDescriptionMarkupCreator;
import semanticMarkup.run.AbstractRun;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A MarkupEvaluationRun creates a markup of treatments using an IMarkupCreator and afterwards evaluating the created markup 
 * using an IEvaluator
 * @author rodenhausen
 */
public class DescriptionMarkupAndDescriptionMarkupEvaluationRun extends AbstractRun {

	private IDescriptionMarkupCreator creator;
	private IDescriptionMarkupEvaluator evaluator;
	private IDescriptionMarkupResultReader descriptionMarkupResultReader;

	/**
	 * @param outDirectory
	 * @param guiceModuleFile
	 * @param creator
	 * @param evaluator
	 * @param goldStandardReader
	 */
	@Inject
	public DescriptionMarkupAndDescriptionMarkupEvaluationRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("MarkupCreator") IDescriptionMarkupCreator creator, 
			@Named("EvaluationRun_Evaluator")IDescriptionMarkupEvaluator evaluator, 
			@Named("EvaluationRun_DescriptionMarkupResultReader")IDescriptionMarkupResultReader descriptionMarkupResultReader) {
		super(guiceModuleFile, runOutDirectory);
		this.creator = creator;
		this.descriptionMarkupResultReader = descriptionMarkupResultReader;
		this.evaluator = evaluator;
	}

	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Creating markup using " + creator.getDescription() + "...");
		DescriptionMarkupResult result = creator.create();
		
		log(LogLevel.INFO, "Evaluating markup using " + evaluator.getDescription() + "...");
		
		log(LogLevel.INFO, "read gold standard using " + descriptionMarkupResultReader.getClass());
		DescriptionMarkupResult correctResult = descriptionMarkupResultReader.read();
		
		evaluator.evaluate(result, correctResult);
		log(LogLevel.INFO, "Evaluation result: \n" + evaluator.getResult());
	}
}
