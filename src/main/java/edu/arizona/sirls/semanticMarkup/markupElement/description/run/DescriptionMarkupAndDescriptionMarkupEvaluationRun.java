package edu.arizona.sirls.semanticMarkup.markupElement.description.run;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.log.LogLevel;
import edu.arizona.sirls.semanticMarkup.markupElement.description.eval.IDescriptionMarkupResultReader;
import edu.arizona.sirls.semanticMarkup.markupElement.description.eval.io.IDescriptionMarkupEvaluator;
import edu.arizona.sirls.semanticMarkup.markupElement.description.markup.DescriptionMarkupResult;
import edu.arizona.sirls.semanticMarkup.markupElement.description.markup.IDescriptionMarkupCreator;
import edu.arizona.sirls.semanticMarkup.run.AbstractRun;

/**
 * A MarkupEvaluationRun creates a markup of treatments using an IMarkupCreator and afterwards evaluating the created markup 
 * using an IEvaluator
 * @author rodenhausen
 */
public class DescriptionMarkupAndDescriptionMarkupEvaluationRun extends AbstractRun {

	private IDescriptionMarkupCreator creator;
	private IDescriptionMarkupEvaluator evaluator;
	private IDescriptionMarkupResultReader descriptionMarkupResultReader;
	private String correctInputDirectory;

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
			@Named("EvaluationRun_DescriptionMarkupResultReader")IDescriptionMarkupResultReader descriptionMarkupResultReader, 
			String correctInputDirectory) {
		super(guiceModuleFile, runOutDirectory);
		this.creator = creator;
		this.descriptionMarkupResultReader = descriptionMarkupResultReader;
		this.evaluator = evaluator;
		this.correctInputDirectory = correctInputDirectory;
	}

	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Creating markup using " + creator.getDescription() + "...");
		DescriptionMarkupResult result = creator.create();
		
		log(LogLevel.INFO, "Evaluating markup using " + evaluator.getDescription() + "...");
		
		log(LogLevel.INFO, "read gold standard using " + descriptionMarkupResultReader.getClass());
		DescriptionMarkupResult correctResult = descriptionMarkupResultReader.read(correctInputDirectory);
		
		evaluator.evaluate(result, correctResult);
		log(LogLevel.INFO, "Evaluation result: \n" + evaluator.getResult());
	}
}
