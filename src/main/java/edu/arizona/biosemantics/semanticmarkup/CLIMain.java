package edu.arizona.biosemantics.semanticmarkup;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.RollingFileAppender;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.arizona.biosemantics.semanticmarkup.config.RunConfig;
import edu.arizona.biosemantics.semanticmarkup.config.taxongroup.AlgaeConfig;
import edu.arizona.biosemantics.semanticmarkup.config.taxongroup.CnidariaConfig;
import edu.arizona.biosemantics.semanticmarkup.config.taxongroup.FossilConfig;
import edu.arizona.biosemantics.semanticmarkup.config.taxongroup.GastropodsConfig;
import edu.arizona.biosemantics.semanticmarkup.config.taxongroup.HymenopteraConfig;
import edu.arizona.biosemantics.semanticmarkup.config.taxongroup.PlantConfig;
import edu.arizona.biosemantics.semanticmarkup.config.taxongroup.PoriferaConfig;
import edu.arizona.biosemantics.semanticmarkup.config.taxongroup.SpiderConfig;
import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.GenericDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.run.IRun;

/**
 * CLI Entry point into the processing of the charaparser framework
 * @author thomas rodenhausen
 */
public class CLIMain {

	protected RunConfig config;
	
	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		CLIMain cliMain = new CLIMain();
		cliMain.parse(args);
		cliMain.run();
	}

	protected void setupLogging(String debugLog, String errorLog) {
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.getLoggerRepository().resetConfiguration();
		addDebugErrorLoggers(rootLogger, debugLog, errorLog);
	}

	protected void addDebugErrorLoggers(Logger rootLogger, String debugLog, String errorLog) {
		PatternLayout layout = new PatternLayout();
		layout.setConversionPattern("%d [%t] %-5p %c:%L - %m%n");
		
		RollingFileAppender debugFileAppender = new RollingFileAppender();
		debugFileAppender.setEncoding("UTF-8");
		debugFileAppender.setFile(debugLog);
		debugFileAppender.setMaxFileSize("100MB");
		debugFileAppender.setAppend(false);
		debugFileAppender.setMaxBackupIndex(100);
		debugFileAppender.setLayout(layout);
		debugFileAppender.setThreshold(Level.DEBUG);
		debugFileAppender.activateOptions();
		
		RollingFileAppender errorFileAppender = new RollingFileAppender();
		errorFileAppender.setEncoding("UTF-8");
		errorFileAppender.setFile(errorLog);
		errorFileAppender.setMaxFileSize("100MB");
		errorFileAppender.setAppend(false);
		errorFileAppender.setMaxBackupIndex(100);
		errorFileAppender.setLayout(layout);
		errorFileAppender.setThreshold(Level.ERROR);
		errorFileAppender.activateOptions();
		
		ConsoleAppender consoleErrorAppender = new ConsoleAppender();
		consoleErrorAppender.setTarget("System.out");
		consoleErrorAppender.setLayout(layout);
		consoleErrorAppender.setThreshold(Level.ERROR);
		consoleErrorAppender.activateOptions();

		rootLogger.setLevel(Level.DEBUG);
		rootLogger.addAppender(debugFileAppender);
		rootLogger.addAppender(errorFileAppender);
		rootLogger.addAppender(consoleErrorAppender);
	}

	/**
	 * Run the Main
	 * @throws Throwable 
	 */
	public void run() throws Throwable {
		log(LogLevel.DEBUG, "run using config:");
		log(LogLevel.DEBUG, config.toString());
		Injector injector = Guice.createInjector(config);
		IRun run = injector.getInstance(IRun.class);
		
		log(LogLevel.INFO, "running " + run.getDescription() + "...");
		try {
			run.run();
		} catch (Throwable t) {
			log(LogLevel.ERROR, "Problem to execute the run", t);
			throw t;
		}
	}

	/**
	 * @param args to parse to set config appropriately
	 */
	public void parse(String[] args) throws Throwable {
		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		options.addOption("c", "config", true, "config to use");
		options.addOption("o", "output", true, "output directory");
		options.addOption("i", "input", true, "input file or directory");
		options.addOption("r", "reader", true, "force an input reader. If the option is not provided input format will be detected");
		Option threadingOption = new Option("p", "multi-threading", true, "use multi-threading to compute the result");
		//threadingOption.setValueSeparator(',');
		options.addOption(threadingOption);
		options.addOption("db", "database", true, "database to use");
		options.addOption("dbu", "database-user", true, "database user to use");
		options.addOption("dbp", "database-password", true, "database password to use");
		options.addOption("dbtp", "database-table-prefix", true, "database table prefix to use");
		options.addOption("dbgt", "database-glossary-table", true, "database glossary table to use");
		options.addOption("g", "glossary", true, "csv glossary to use");
		options.addOption("h", "help", false, "shows the help");
		
		String workspace = config.getWorkspaceDirectory();
		setupLogging(workspace + File.separator + "debug.log", workspace + File.separator + "error.log");
		
		try {
			config = new RunConfig();
		} catch(IOException e) {
			log(LogLevel.ERROR, "Couldn't instantiate default config", e);
			throw e;
		}
			
		try {
		    CommandLine commandLine = parser.parse( options, args );
		    if(commandLine.hasOption("h")) {
		    	HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "what is this?", options );
				return;
		    }
		    if(commandLine.hasOption("c")) {
		    	try {
		    		config = getConfig(commandLine.getOptionValue("c"));
		    	} catch(IOException e) {
		    		log(LogLevel.ERROR, "Couldn't instantiate selected config", e);
		    		throw e;
		    	}
		    } else {
		    	//use standard config RunConfig
		    }
		    if(!commandLine.hasOption("i")) {
		    	log(LogLevel.ERROR, "You have to specify an input file or directory");
		    	throw new IllegalArgumentException();
		    }
		    if(commandLine.hasOption("r")) {
		    	setReaderSpecificConfigValues(config, commandLine.getOptionValue("r"), commandLine.getOptionValue("i"));
		    } else {
		    	//use GenericFileVolumeReader
		    	config.setDescriptionReader(GenericDescriptionReader.class);
		    	config.setInputDirectory(commandLine.getOptionValue("i"));
		    }
		    if(commandLine.hasOption("p")) {
		    	config.setMarkupDescriptionTreatmentTransformerParallelProcessing(true);
		    	String parallelParameter = commandLine.getOptionValue("p");
		    	String[] parallelParameters = parallelParameter.split(",");
		    	if(parallelParameters.length != 2) {
		    		log(LogLevel.ERROR, "You have to specify 2 values for parameter p");
		    		throw new IllegalArgumentException();
		    	} else {
		    		try {
		    			int threadsPerDescriptionExtractor = Integer.parseInt(parallelParameters[0]);
		    			int threadsPerSentenceChunking = Integer.parseInt(parallelParameters[1]);
		    			config.setMarkupDescriptionTreatmentTransformerSentenceChunkerRunMaximum(threadsPerSentenceChunking);
		    			config.setMarkupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum(threadsPerDescriptionExtractor);
		    		} catch(Exception e) {
		    			log(LogLevel.ERROR, "Problem to convert parameter to Integer", e);
		    			throw e;
		    		}
		    	}
		    }
		    if(commandLine.hasOption("db")) {
		    	config.setDatabaseName(commandLine.getOptionValue("db"));
		    } else {
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("dbu")) {
		    	config.setDatabaseUser(commandLine.getOptionValue("dbu"));
		    } else {
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("dbp")) {
		    	config.setDatabasePassword(commandLine.getOptionValue("dbp"));
		    } else {
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("dbtp")) {
		    	config.setDatabaseTablePrefix(commandLine.getOptionValue("dbtp"));
		    } else {
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("dbgt")) {
		    	config.setDatabaseGlossaryTable(commandLine.getOptionValue("dbgt"));
		    } else {
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("g")) {
		    	config.setGlossaryFile(commandLine.getOptionValue("g"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a glossary file");
		    	throw new IllegalArgumentException();
		    }
		} catch(ParseException e) {
			log(LogLevel.ERROR, "Problem parsing parameters", e);
		}
	}

	protected void setReaderSpecificConfigValues(RunConfig config, String volumeReader, String input) throws IOException {
		try {
			config.setIODescriptionBindingsList(volumeReader);
		} catch(IOException e) {
			log(LogLevel.ERROR, "DescriptionReader unknown or couldn't read bindings");
			throw e;
		}
	}

	protected RunConfig getConfig(String config) throws IOException {
		TaxonGroup taxonGroup = TaxonGroup.valueOf(config.toUpperCase());
		switch(taxonGroup) {
		case ALGAE:
			return new AlgaeConfig();
		case CNIDARIA:
			return new CnidariaConfig();
		case FOSSIL:
			return new FossilConfig();
		case GASTROPODS:
			return new GastropodsConfig();
		case HYMENOPTERA:
			return new HymenopteraConfig();
		case PLANT:
			return new PlantConfig();
		case PORIFERA:
			return new PoriferaConfig();
		case SPIDER:
			return new SpiderConfig();
		default:
			log(LogLevel.ERROR, "Config unknown, fall back to default config based on Plant");
			return new PlantConfig();
		}
	}
}

