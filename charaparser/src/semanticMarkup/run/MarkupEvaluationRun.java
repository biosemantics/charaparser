package semanticMarkup.run;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.eval.IEvaluator;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.markup.IMarkupCreator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MarkupEvaluationRun extends AbstractRun {

	private String outFile;
	private String guiceModuleFile;
	private IMarkupCreator creator;
	private IEvaluator evaluator;
	private IVolumeReader goldStandardReader;

	@Inject
	public MarkupEvaluationRun(@Named("Run_OutFile")String outFile,
			@Named("GuiceModuleFile")String guiceModuleFile, 
			@Named("MarkupCreator") IMarkupCreator creator, 
			@Named("EvaluationRun_Evaluator")IEvaluator evaluator, 
			@Named("EvaluationRun_GoldStandardReader")IVolumeReader goldStandardReader) {
		super(guiceModuleFile);
		this.outFile = outFile;
		this.creator = creator;
		this.goldStandardReader = goldStandardReader;
		this.evaluator = evaluator;
	}
	
	public void run() throws Exception {
		BufferedWriter bwSetup = new BufferedWriter(new FileWriter(outFile + ".config.txt"));
		appendConfigFile(bwSetup);
		
		long startTime = Calendar.getInstance().getTimeInMillis();
		String startedAt = "started at " + startTime;
		bwSetup.append(startedAt + "\n\n");
		System.out.println(startedAt);
		
		//the actual processing
		System.out.println("Creating markup using " + creator.getDescription() + "...");
		creator.create();
		
		long endTime = Calendar.getInstance().getTimeInMillis();
		String wasDone = "was done at " + endTime;
		bwSetup.append(wasDone + "\n");
		System.out.println(wasDone);
		long milliseconds = endTime - startTime;
		String tookMe = "took me " + (endTime - startTime) + " milliseconds";
		bwSetup.append(tookMe + "\n");
		System.out.println(tookMe);
		
		String timeString = getTimeString(milliseconds);
		bwSetup.append(timeString + "\n");
		System.out.println(timeString);
		System.out.println();
		
		System.out.println("Evaluating markup using " + evaluator.getDescription() + "...");
		List<Treatment> markedUpResult = creator.getResult();
		
		System.out.println("read gold standard using " + goldStandardReader.getClass());
		List<Treatment> goldStandard = goldStandardReader.read();
		
		evaluator.evaluate(markedUpResult, goldStandard);
		System.out.println("Evaluation result: \n" + evaluator.getResult());
		
		long endEvaluationTime = Calendar.getInstance().getTimeInMillis();
		String wasDoneEvaluating = "was done at " + endEvaluationTime;
		bwSetup.append(wasDoneEvaluating + "\n");
		System.out.println(wasDoneEvaluating);
		long millisecondsEvaluating = endEvaluationTime - endTime;
		String tookMeEvaluating = "took me " + (endEvaluationTime - endTime) + " milliseconds";
		bwSetup.append(tookMeEvaluating + "\n");
		System.out.println(tookMeEvaluating);
		
		String timeStringEvaluating = getTimeString(millisecondsEvaluating);
		bwSetup.append(timeStringEvaluating + "\n");
		System.out.println(timeStringEvaluating);
		bwSetup.flush();
		bwSetup.close();
	}

	@Override
	public String getDescription() {
		return "EvaluationRun";
	}
}
