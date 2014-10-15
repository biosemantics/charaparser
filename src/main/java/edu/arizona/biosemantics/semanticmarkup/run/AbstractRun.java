package edu.arizona.biosemantics.semanticmarkup.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;

/**
 * An AbstractRun implements some shared functionality of concrete Run implementations such as time string formating, config reporting,..
 * @author rodenhausen
 */
public abstract class AbstractRun implements IRun {

	protected String guiceModuleFile;
	protected String runOutDirectory;
	private String inputDirectory;

	/**
	 * @param guiceModuleFile
	 */
	@Inject
	public AbstractRun(@Named("GuiceModuleFile")String guiceModuleFile,
		@Named("InputDirectory")String inputDirectory,
		@Named("Run_OutDirectory")String runOutDirectory) {
		this.guiceModuleFile = guiceModuleFile;
		this.inputDirectory = inputDirectory;
		this.runOutDirectory = runOutDirectory;
	}
	
	public void run() throws Throwable {	
		StringBuilder config = new StringBuilder();
		appendConfigFile(config);
				
		long startTime = Calendar.getInstance().getTimeInMillis();
		String startedAt = "started at " + startTime;
		config.append(startedAt + "\n\n");
		log(LogLevel.INFO, startedAt);

		FileUtils.deleteDirectory(new File(runOutDirectory));
		new File(runOutDirectory).mkdirs();
		FileUtils.copyDirectory(new File(inputDirectory), new File(runOutDirectory));
		
		doRun();
		
		long endTime = Calendar.getInstance().getTimeInMillis();
		String wasDone = "was done at " + endTime;
		config.append(wasDone + "\n");
		log(LogLevel.INFO, wasDone);
		long milliseconds = endTime - startTime;
		String tookMe = "took me " + (endTime - startTime) + " milliseconds";
		config.append(tookMe + "\n");
		log(LogLevel.INFO, tookMe);
		
		String timeString = getTimeString(milliseconds);
		config.append(timeString + "\n");
		log(LogLevel.INFO, timeString);
		
		BufferedWriter bwSetup = new BufferedWriter(new FileWriter(runOutDirectory + File.separator + "config.txt"));
		bwSetup.append(config.toString());
		bwSetup.flush();
		bwSetup.close();
	}

	protected abstract void doRun() throws Throwable;

	public String getDescription() {
		return this.getClass().toString();
	}

	
	protected void appendConfigFile(StringBuilder stringBuilder) throws IOException {
		stringBuilder.append("GuiceModule configuration of Run \n" +
		  "---------------------\n");
		stringBuilder.append(this.guiceModuleFile);
		stringBuilder.append("---------------------\n\n");
	}
	
	protected String getTimeString(long milliseconds) {
		int hours = (int)Math.floor(milliseconds/(1000 * 60.0 * 60.0));
		milliseconds = milliseconds - hours * (1000 * 60 * 60);
		int minutes = (int)Math.floor(milliseconds/(1000.0 * 60.0));
		milliseconds = milliseconds - minutes * (1000 * 60);
		int seconds = (int)(milliseconds/1000.0);
		
		String timeString = "that's " + hours + " hours, " + minutes + 
							" minutes, and " + seconds + " seconds";
		return timeString;
	}
}
