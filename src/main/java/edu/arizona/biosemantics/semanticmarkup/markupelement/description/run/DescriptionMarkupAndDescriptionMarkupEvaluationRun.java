package edu.arizona.biosemantics.semanticmarkup.markupelement.description.run;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.db.ConnectionPool;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.IDescriptionMarkupResultReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.io.IDescriptionMarkupEvaluator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.DescriptionMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.IDescriptionMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.run.AbstractRun;

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
	 *
	 * @param guiceModuleFile
	 * @param inputDirectory
	 * @param runOutDirectory
	 * @param creator
	 * @param evaluator
	 * @param descriptionMarkupResultReader
	 * @param correctInputDirectory
	 * @param connectionPool
	 */
	@Inject
	public DescriptionMarkupAndDescriptionMarkupEvaluationRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("InputDirectory")String inputDirectory,
			@Named("Run_OutDirectory")String runOutDirectory,
			@Named("MarkupCreator") IDescriptionMarkupCreator creator,
			@Named("EvaluationRun_Evaluator")IDescriptionMarkupEvaluator evaluator,
			@Named("EvaluationRun_DescriptionMarkupResultReader")IDescriptionMarkupResultReader descriptionMarkupResultReader,
			String correctInputDirectory,
			ConnectionPool connectionPool) {
		super(guiceModuleFile, inputDirectory, runOutDirectory, connectionPool);
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
