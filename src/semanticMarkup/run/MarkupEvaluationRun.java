package semanticMarkup.run;

import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.eval.IEvaluator;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.log.LogLevel;
import semanticMarkup.markup.IMarkupCreator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A MarkupEvaluationRun creates a markup of treatments using an IMarkupCreator and afterwards evaluating the created markup 
 * using an IEvaluator
 * @author rodenhausen
 */
public class MarkupEvaluationRun extends AbstractRun {

	private IMarkupCreator creator;
	private IEvaluator evaluator;
	private IVolumeReader goldStandardReader;

	/**
	 * @param outDirectory
	 * @param guiceModuleFile
	 * @param creator
	 * @param evaluator
	 * @param goldStandardReader
	 */
	@Inject
	public MarkupEvaluationRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("Run_RootDirectory")String runRootDirectory,
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("MarkupCreator") IMarkupCreator creator, 
			@Named("EvaluationRun_Evaluator")IEvaluator evaluator, 
			@Named("EvaluationRun_GoldStandardReader")IVolumeReader goldStandardReader) {
		super(guiceModuleFile, runRootDirectory, runOutDirectory);
		this.creator = creator;
		this.goldStandardReader = goldStandardReader;
		this.evaluator = evaluator;
	}

	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Creating markup using " + creator.getDescription() + "...");
		creator.create();
		
		log(LogLevel.INFO, "Evaluating markup using " + evaluator.getDescription() + "...");
		List<Treatment> markedUpResult = creator.getResult();
		
		log(LogLevel.INFO, "read gold standard using " + goldStandardReader.getClass());
		List<Treatment> goldStandard = goldStandardReader.read();
		
		evaluator.evaluate(markedUpResult, goldStandard);
		log(LogLevel.INFO, "Evaluation result: \n" + evaluator.getResult());
	}
}
