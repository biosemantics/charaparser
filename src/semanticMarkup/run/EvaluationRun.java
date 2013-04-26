package semanticMarkup.run;

import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.eval.IEvaluator;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;


/**
 * A EvaluationRun evaluates marked up treatments using an IEvaluator
 * @author rodenhausen
 */
public class EvaluationRun extends AbstractRun {

	private IEvaluator evaluator;
	private IVolumeReader createdVolumeReader;
	private IVolumeReader goldStandardReader;

	/**
	 * @param guiceModuleFile
	 * @param runRootDirectory
	 * @param runOutDirectory
	 * @param evaluator
	 * @param createdVolumeReader
	 * @param goldStandardReader
	 */
	@Inject
	public EvaluationRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("Run_RootDirectory")String runRootDirectory,
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("EvaluationRun_Evaluator")IEvaluator evaluator, 
			@Named("EvaluationRun_CreatedVolumeReader")IVolumeReader createdVolumeReader,
			@Named("EvaluationRun_GoldStandardReader")IVolumeReader goldStandardReader) {
		super(guiceModuleFile, runRootDirectory, runOutDirectory);
		this.createdVolumeReader = createdVolumeReader;
		this.goldStandardReader = goldStandardReader;
		this.evaluator = evaluator;
	}


	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Evaluating markup using " + evaluator.getDescription() + "...");
		log(LogLevel.INFO, "read marked up result using " + createdVolumeReader.getClass());
		List<Treatment> markedUpResult = createdVolumeReader.read();
		log(LogLevel.INFO, "read gold standard using " + goldStandardReader.getClass());
		List<Treatment> goldStandard = goldStandardReader.read();
		
		evaluator.evaluate(markedUpResult, goldStandard);
		log(LogLevel.INFO, "Evaluation result: \n" + evaluator.getResult());
	}

}
