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
 * A MarkupEvaluationRun creates a markup of treatments using an IMarkupCreator and afterwards evaluating the created markup 
 * using an IEvaluator
 * @author rodenhausen
 */
public class MarkupEvaluationRun extends AbstractRun {

	private String outDirectory;
	private String guiceModuleFile;
	private IMarkupCreator creator;
	private IEvaluator evaluator;
	private IVolumeReader goldStandardReader;

	/**
	 * @param outFile
	 * @param guiceModuleFile
	 * @param creator
	 * @param evaluator
	 * @param goldStandardReader
	 */
	@Inject
	public MarkupEvaluationRun(@Named("Run_OutDirectory")String outDirectory,
			@Named("GuiceModuleFile")String guiceModuleFile, 
			@Named("MarkupCreator") IMarkupCreator creator, 
			@Named("EvaluationRun_Evaluator")IEvaluator evaluator, 
			@Named("EvaluationRun_GoldStandardReader")IVolumeReader goldStandardReader) {
		super(guiceModuleFile);
		this.outDirectory = outDirectory;
		this.creator = creator;
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
		
		//the actual processing
		log(LogLevel.INFO, "Creating markup using " + creator.getDescription() + "...");
		creator.create();
		
		long endTime = Calendar.getInstance().getTimeInMillis();
		String wasDone = "was done at " + endTime;
		bwSetup.append(wasDone + "\n");
		log(LogLevel.INFO, wasDone);
		long milliseconds = endTime - startTime;
		String tookMe = "took me " + (endTime - startTime) + " milliseconds";
		bwSetup.append(tookMe + "\n");
		log(LogLevel.INFO, tookMe);
		
		String timeString = getTimeString(milliseconds);
		bwSetup.append(timeString + "\n");
		log(LogLevel.INFO, timeString);
		
		log(LogLevel.INFO, "Evaluating markup using " + evaluator.getDescription() + "...");
		List<Treatment> markedUpResult = creator.getResult();
		
		log(LogLevel.INFO, "read gold standard using " + goldStandardReader.getClass());
		List<Treatment> goldStandard = goldStandardReader.read();
		
		evaluator.evaluate(markedUpResult, goldStandard);
		log(LogLevel.INFO, "Evaluation result: \n" + evaluator.getResult());
		
		long endEvaluationTime = Calendar.getInstance().getTimeInMillis();
		String wasDoneEvaluating = "was done at " + endEvaluationTime;
		bwSetup.append(wasDoneEvaluating + "\n");
		log(LogLevel.INFO, wasDoneEvaluating);
		long millisecondsEvaluating = endEvaluationTime - endTime;
		String tookMeEvaluating = "took me " + (endEvaluationTime - endTime) + " milliseconds";
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
		return "EvaluationRun";
	}
}
