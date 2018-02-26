package edu.arizona.biosemantics.semanticmarkup.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.ling.HasWord;





import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.db.ConnectionPool;

/**
 * An AbstractRun implements some shared functionality of concrete Run implementations such as time string formating, config reporting,..
 * @author rodenhausen
 */
public abstract class AbstractRun implements IRun {

	protected String guiceModuleFile;
	protected String runOutDirectory;
	private String inputDirectory;
	protected ConnectionPool connectionPool;

	/**
	 * @param guiceModuleFile
	 */
	@Inject
	public AbstractRun(@Named("GuiceModuleFile")String guiceModuleFile,
		@Named("InputDirectory")String inputDirectory,
		@Named("Run_OutDirectory")String runOutDirectory,
		ConnectionPool connectionPool) {
		this.guiceModuleFile = guiceModuleFile;
		this.inputDirectory = inputDirectory;
		this.runOutDirectory = runOutDirectory;
		this.connectionPool = connectionPool;
	}
	
	public void run() throws Throwable {	
		try {					
			long startTime = Calendar.getInstance().getTimeInMillis();
			String startedAt = "started at " + startTime;
			log(LogLevel.INFO, startedAt);
			
			FileUtils.deleteDirectory(new File(runOutDirectory));
			new File(runOutDirectory).mkdirs();
			FileUtils.copyDirectory(new File(inputDirectory), new File(runOutDirectory));
			
				doRun();
			
			long endTime = Calendar.getInstance().getTimeInMillis();
			String wasDone = "was done at " + endTime;
			log(LogLevel.INFO, wasDone);
			long milliseconds = endTime - startTime;
			String tookMe = "took me " + (endTime - startTime) + " milliseconds";
			log(LogLevel.INFO, tookMe);
			
			String timeString = getTimeString(milliseconds);
			log(LogLevel.INFO, timeString);
			
		} catch(Throwable t) {
			log(LogLevel.ERROR, "Problem to run, shut down connection pool...", t);
			connectionPool.shutdown();
		}
	}

	
	public void runWithAutoDirectory() throws Throwable {	
		try {					
			//put the sentence into the database table directly, skip perl processes
			File inputdir = new File(inputDirectory);
			//inputdir contains 2 files: prefix.xml and prefix.txt
			String prefix = inputdir.listFiles()[0].getName().replaceFirst(".(xml|txt)$", "");
			DocumentPreprocessor dp = new DocumentPreprocessor(inputDirectory+"/"+prefix+".txt");
			PreparedStatement statement = null;
			try(Connection connection = connectionPool.getConnection()) {
				
				statement = connection.prepareStatement("delete from "+ prefix+ "_sentence");
				statement.execute();
				int i = 0;
				for (List<HasWord> sentence : dp) {
					String sent = makeSentence(sentence);
		  			 statement = connection.prepareStatement("INSERT INTO " + 
				  			 prefix+ "_sentence (source, sentence, originalsent, tag, modifier ) values (?,?,?,?,?)");
		  			statement.setString(1, "000."+prefix+".txt-"+(i++)); //source needs to have two dots. The number before the first dot represents a paragraph/file
		  			statement.setString(2, sent.toLowerCase());
		  			statement.setString(3, sent);
		  			statement.setString(4, "");
		  			statement.setString(5, "");
		  			statement.execute();
				}
			}
			catch(Throwable t){
				log(LogLevel.ERROR, "failed to add input sentence to "+prefix+"_sentence table");
				t.printStackTrace();
			}finally{
	  			statement.close();
			}
				
			//now remove .txt file from the input directory
			FileUtils.forceDelete(new File(inputDirectory+"/"+prefix+".txt"));
			FileUtils.deleteDirectory(new File(runOutDirectory));
			new File(runOutDirectory).mkdirs();
			FileUtils.copyDirectory(new File(inputDirectory), new File(runOutDirectory));
			
			long startTime = Calendar.getInstance().getTimeInMillis();
			String startedAt = "started at " + startTime;
			log(LogLevel.INFO, startedAt);
			
			doRun();
				
			FileUtils.deleteDirectory(new File(inputDirectory));
			
			long endTime = Calendar.getInstance().getTimeInMillis();
			String wasDone = "was done at " + endTime;
			log(LogLevel.INFO, wasDone);
			long milliseconds = endTime - startTime;
			String tookMe = "took me " + (endTime - startTime) + " milliseconds";
			log(LogLevel.INFO, tookMe);
			
			String timeString = getTimeString(milliseconds);
			log(LogLevel.INFO, timeString);
			
		} catch(Throwable t) {
			log(LogLevel.ERROR, "Problem to run, shut down connection pool...", t);
			connectionPool.shutdown();
		}
	}
	

	private String makeSentence(List<HasWord> sentence) {
		StringBuffer sent = new StringBuffer();
		Iterator<HasWord> it = sentence.iterator();
		while (it.hasNext()){
			String w = it.next().word();
			if(w.matches("\\p{Punct}"))
				sent.append(w);
			else
				sent.append(" "+w);
		}
		return sent.toString().trim();
	}

	protected abstract void doRun() throws Throwable;

	public String getDescription() {
		return this.getClass().toString();
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
