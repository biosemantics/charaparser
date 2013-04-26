package semanticMarkup.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * An AbstractRun implements some shared functionality of concrete Run implementations such as time string formating, config reporting,..
 * @author rodenhausen
 */
public abstract class AbstractRun implements IRun {

	protected String guiceModuleFile;
	protected String runRootDirectory;
	protected String runOutDirectory;

	/**
	 * @param guiceModuleFile
	 */
	@Inject
	public AbstractRun(@Named("GuiceModuleFile")String guiceModuleFile,
		@Named("Run_RootDirectory")String runRootDirectory,
		@Named("Run_OutDirectory")String runOutDirectory) {
		this.guiceModuleFile = guiceModuleFile;
		this.runRootDirectory = runRootDirectory;
		this.runOutDirectory = runOutDirectory;
	}
	
	public void run() throws Exception {
		if(!isValidRun(runRootDirectory)) {
			log(LogLevel.ERROR, "Not a valid run. Stop.");
			return;
		}
		
		new File(runOutDirectory + File.separator + "config.txt").getParentFile().mkdirs();
		BufferedWriter bwSetup = new BufferedWriter(new FileWriter(runOutDirectory + File.separator + "config.txt"));
		appendConfigFile(bwSetup);
		
		long startTime = Calendar.getInstance().getTimeInMillis();
		String startedAt = "started at " + startTime;
		bwSetup.append(startedAt + "\n\n");
		log(LogLevel.INFO, startedAt);

		doRun();
		
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

	protected abstract void doRun() throws Exception;
	
	protected boolean isValidRun(String runRootDirectory) {
		File file = new File(runRootDirectory);
		file.mkdirs();
		//createNewFile is atomic operation of existance check and creation if doesnt exist yet.
		//it guarantees to finish the operation before other processes can access the file.
		//hence it is a sufficient check for concurrently running Runs
		File synchronizationFile = new File(runRootDirectory + File.separator + "learnStart");
		boolean result = false;
		try {
			result = synchronizationFile.createNewFile();
		} catch(Exception e) {
			log(LogLevel.ERROR, e);
		}
		if(!result) {
			log(LogLevel.ERROR, "databasePrefix has already been used.");
			return false;
		}
		return true;
	}


	public String getDescription() {
		return this.getClass().toString();
	}

	
	protected void appendConfigFile(BufferedWriter bwSetup) throws IOException {
		bwSetup.append("GuiceModule configuration of EvaluationRun \n" +
		  "---------------------\n");
		bwSetup.append(this.guiceModuleFile);
		bwSetup.append("---------------------\n\n");
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
