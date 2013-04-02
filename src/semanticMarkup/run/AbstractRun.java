package semanticMarkup.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * An AbstractRun implements some shared functionality of concrete Run implementations such as time string formating, config reporting,..
 * @author rodenhausen
 */
public abstract class AbstractRun implements IRun {

	private String guiceModuleFile;

	/**
	 * @param guiceModuleFile
	 */
	@Inject
	public AbstractRun(@Named("GuiceModuleFile")String guiceModuleFile) {
		this.guiceModuleFile = guiceModuleFile;
	}
	
	public abstract void run() throws Exception;

	public abstract String getDescription();

	
	protected void appendConfigFile(BufferedWriter bwSetup) throws IOException {
		bwSetup.append("GuiceModule configuration of EvaluationRun \n" +
		  "---------------------\n");
		BufferedReader br = new BufferedReader(new FileReader(guiceModuleFile));
		String line;
		boolean recordGuiceModule = true;//false;
		while((line = br.readLine()) != null) {
			if(recordGuiceModule)
				bwSetup.append(line + "\n");
		}
		bwSetup.append("---------------------\n\n");
		br.close();
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
