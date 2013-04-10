package semanticMarkup.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.eval.IEvaluator;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.log.LogLevel;
import semanticMarkup.log.Timer;
import semanticMarkup.markup.IMarkupCreator;

import com.google.inject.Inject;
import com.google.inject.name.Named;


/**
 * A EvaluationRun evaluates marked up treatments using an IEvaluator
 * @author rodenhausen
 */
public class EvaluationRun extends AbstractRun {

	private String outDirectory;
	private IEvaluator evaluator;
	private IVolumeReader createdVolumeReader;
	private IVolumeReader goldStandardReader;

	/**
	 * @param outDirectory
	 * @param guiceModuleFile
	 * @param creator
	 * @param evaluator
	 * @param createdVolumeReader
	 * @param goldStandardReader
	 */
	@Inject
	public EvaluationRun(@Named("Run_OutDirectory")String outDirectory,
			@Named("GuiceModuleFile")String guiceModuleFile, 
			@Named("MarkupCreator") IMarkupCreator creator, 
			@Named("EvaluationRun_Evaluator")IEvaluator evaluator, 
			@Named("EvaluationRun_CreatedVolumeReader")IVolumeReader createdVolumeReader,
			@Named("EvaluationRun_GoldStandardReader")IVolumeReader goldStandardReader) {
		super(guiceModuleFile);
		this.outDirectory = outDirectory;
		this.createdVolumeReader = createdVolumeReader;
		this.goldStandardReader = goldStandardReader;
		this.evaluator = evaluator;
	}
	
	@Override
	public void run() throws Exception {
		new File(outDirectory + File.separator + "config.txt").getParentFile().mkdirs();
		BufferedWriter bwSetup = new BufferedWriter(new FileWriter(outDirectory + File.separator + "config.txt"));
		appendConfigFile(bwSetup);
		
		long startTime = Calendar.getInstance().getTimeInMillis();
		String startedAt = "started at " + startTime;
		bwSetup.append(startedAt + "\n\n");
		log(LogLevel.INFO, startedAt);
		
		log(LogLevel.INFO, "Evaluating markup using " + evaluator.getDescription() + "...");
		log(LogLevel.INFO, "read marked up result using " + createdVolumeReader.getClass());
		List<Treatment> markedUpResult = createdVolumeReader.read();
		log(LogLevel.INFO, "read gold standard using " + goldStandardReader.getClass());
		List<Treatment> goldStandard = goldStandardReader.read();
		
		evaluator.evaluate(markedUpResult, goldStandard);
		log(LogLevel.INFO, "Evaluation result: \n" + evaluator.getResult());
		
		long endEvaluationTime = Calendar.getInstance().getTimeInMillis();
		String wasDoneEvaluating = "was done at " + endEvaluationTime;
		bwSetup.append(wasDoneEvaluating + "\n");
		log(LogLevel.INFO, wasDoneEvaluating);
		long millisecondsEvaluating = endEvaluationTime - startTime;
		String tookMeEvaluating = "took me " + (endEvaluationTime - startTime) + " milliseconds";
		bwSetup.append(tookMeEvaluating + "\n");
		log(LogLevel.INFO, tookMeEvaluating);
		
		String timeStringEvaluating = getTimeString(millisecondsEvaluating);
		bwSetup.append(timeStringEvaluating + "\n");
		log(LogLevel.INFO, timeStringEvaluating);
		bwSetup.flush();
		bwSetup.close();
		
		
		log(LogLevel.INFO, "parse time " + this.getTimeString(Timer.getParseTime()));
	}
	


	@Override
	public String getDescription() {
		return "Evaluation Run";
	}

}
