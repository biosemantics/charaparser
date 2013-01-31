package semanticMarkup.run;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;

import semanticMarkup.markup.IMarkupCreator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * AnnotateRun runs an annotation process based on a configuration given
 * @author thomas rodenhausen
 */
public class MarkupRun extends AbstractRun {
	
	private String outFile;
	private String guiceModuleFile;
	private IMarkupCreator creator;

	@Inject
	public MarkupRun(@Named("Run_OutFile")String outFile,
			@Named("GuiceModuleFile")String guiceModuleFile, 
			@Named("MarkupCreator") IMarkupCreator creator) {
		super(guiceModuleFile);
		this.outFile = outFile;
		this.creator = creator;
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
		bwSetup.flush();
		bwSetup.close();
	}

	
	@Override
	public String getDescription() {
		return "MarkupRun";
	}
}
