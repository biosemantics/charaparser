package semanticMarkup.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markup.IMarkupCreator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A MarkupRun creates a markup of treatments using an IMarkupCreator
 * @author rodenhausen
 */
public class MarkupRun extends AbstractRun {
	
	private String outDirectory;
	private String guiceModuleFile;
	private IMarkupCreator creator;

	@Inject
	public MarkupRun(@Named("Run_OutDirectory")String outDirectory,
			@Named("GuiceModuleFile")String guiceModuleFile, 
			@Named("MarkupCreator") IMarkupCreator creator) {
		super(guiceModuleFile);
		this.outDirectory = outDirectory;
		this.creator = creator;
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
		bwSetup.flush();
		bwSetup.close();
	}

	
	@Override
	public String getDescription() {
		return "MarkupRun";
	}
}
