package semanticMarkup.iPlant;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import semanticMarkup.CLIMain;
import semanticMarkup.RunConfig;
import semanticMarkup.io.input.GenericFileVolumeReader;
import semanticMarkup.io.input.lib.xml.XMLVolumeReader;
import semanticMarkup.know.lib.InMemoryGlossary;
import semanticMarkup.ling.learn.lib.PerlTerminologyLearner;
import semanticMarkup.log.LogLevel;
import semanticMarkup.run.LearnRun;

/**
 * Learn CLI Entry point into the processing of the charaparser framework
 * @author thomas rodenhausen
 */
public class LearnMain extends CLIMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for(String arg : args)
			System.out.println(arg);
		CLIMain cliMain = new LearnMain();
		cliMain.parse(args);
		cliMain.run();
	}

	@Override
	public void parse(String[] args) {		
		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		
		//for iplant user shown configuration options
		options.addOption("i", "input", true, "input file or directory");
		options.addOption("c", "config", true, "config to use"); 
		options.addOption("dbtp", "database-table-prefix", true, "database table prefix to use");
		options.addOption("style", "style mapping", true, "Optional style mapping to use for Word file input");
		
		//for iplant user hidden inputs, but still required or 'nice to have' configuration possibilities'
		options.addOption("dbh", "database-host", true, "dbms host");
		options.addOption("dbp", "database-port", true, "dbms port");
		options.addOption("dbn", "database-name", true, "name of database to use");
		options.addOption("dbu", "database-user", true, "database user to use");
		options.addOption("dbp", "database-password", true, "database password to use");
		options.addOption("p", "multi-threading", true, "use multi-threading to compute the result");
		options.addOption("o", "output", true, "output directory");
		options.addOption("h", "help", false, "shows the help");
		
		config = new RunConfig();
		try {
		    CommandLine commandLine = parser.parse( options, args );
		    if(commandLine.hasOption("h")) {
		    	HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "what is this?", options );
				System.exit(0);
		    }
		    if(commandLine.hasOption("c")) {
		    	config = getConfig(commandLine.getOptionValue("c"));
		    } else {
		    	//use standard config RunConfig
		    }
		    if(commandLine.hasOption("o")) {
		    	config.setRunOutDirectory(commandLine.getOptionValue("o"));
		    } else {
		    	config.setRunOutDirectory("." + File.pathSeparator);
		    }
		    
		    config.setMarkupCreatorVolumeReader(GenericFileVolumeReader.class);
		    if(!commandLine.hasOption("i")) {
		    	log(LogLevel.ERROR, "You have to specify an input file or directory");
		    	System.exit(0);
		    } else {
		    	config.setGenericFileVolumeReaderSource(commandLine.getOptionValue("i"));
		    }
		    if(commandLine.hasOption("style")) {
		    	config.setWordVolumeReaderStyleMappingFile(commandLine.getOptionValue("style"));
		    }
		    if(commandLine.hasOption("p")) {
		    	config.setMarkupDescriptionTreatmentTransformerParallelProcessing(true);
		    	String[] parallelParameters = commandLine.getOptionValues("p");
		    	if(parallelParameters.length != 2) {
		    		log(LogLevel.ERROR, "You have to specify 2 values for parameter p");
		    		System.exit(0);
		    	} else {
		    		try {
		    			int threadsPerDescriptionExtractor = Integer.parseInt(parallelParameters[0]);
		    			int threadsPerSentenceChunking = Integer.parseInt(parallelParameters[1]);
		    			config.setMarkupDescriptionTreatmentTransformerSentenceChunkerRunMaximum(threadsPerSentenceChunking);
		    			config.setMarkupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum(threadsPerDescriptionExtractor);
		    		} catch(Exception e) {
		    			log(LogLevel.ERROR, e);
		    			System.exit(0);
		    		}
		    	}
		    }
		    if(commandLine.hasOption("dbh")) {
		    	config.setDatabaseHost(commandLine.getOptionValue("dbh"));
		    } else {
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("dbp")) {
		    	config.setDatabasePort(commandLine.getOptionValue("dbp"));
		    } else {
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("dbn")) {
		    	config.setDatabaseName(commandLine.getOptionValue("dbn"));
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
			//TODO databaseTablePrefix has to be given as user as a ID he remembered from LearnMain
			//since we have no user information to be able to generate an ID that allows to know
			//at least whos data to pull
		    if(commandLine.hasOption("dbtp")) {
		    	config.setDatabaseTablePrefix(commandLine.getOptionValue("dbtp"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database table prefix");
		    	System.exit(0);
		    }
		}
		catch( ParseException exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		}
		
		config.setRun(LearnRun.class);
		config.setGlossary(InMemoryGlossary.class);
		config.setTerminologyLearner(PerlTerminologyLearner.class);
		config.setDatabaseGlossaryTable("permanentGlossary");
	}
}
