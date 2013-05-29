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
import semanticMarkup.core.transformation.lib.description.MarkupDescriptionTreatmentTransformer;
import semanticMarkup.io.input.GenericFileVolumeReader;
import semanticMarkup.know.lib.InMemoryGlossary;
import semanticMarkup.ling.learn.lib.DatabaseInputNoLearner;
import semanticMarkup.log.LogLevel;
import semanticMarkup.run.IPlantMarkupRun;
import semanticMarkup.run.MarkupRun;

/**
 * Markup CLI Entry point into the processing of the charaparser framework
 * @author thomas rodenhausen
 */
public class MarkupMain extends CLIMain {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CLIMain cliMain = new MarkupMain();
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
		options.addOption("z", "database-table-prefix", true, "database table prefix to use");
		options.addOption("w", "style mapping", true, "Optional style mapping to use for Word file input");
		
		//for iplant user hidden inputs, but still required or 'nice to have' configuration possibilities'
		options.addOption("n", "database-host", true, "dbms host");
		options.addOption("p", "database-port", true, "dbms port");
		options.addOption("d", "database-name", true, "name of database to use");
		options.addOption("u", "database-user", true, "database user to use");
		options.addOption("s", "database-password", true, "database password to use");
		options.addOption("t", "multi-threading", true, "use multi-threading to compute the result");
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
		    	log(LogLevel.ERROR, "You have to specify a configuration to use");
		    	System.exit(0);
		    	//use standard config RunConfig
		    }
		    
		    config.setMarkupCreatorVolumeReader(GenericFileVolumeReader.class);
		    if(!commandLine.hasOption("i")) {
		    	log(LogLevel.ERROR, "You have to specify an input file or directory");
		    	System.exit(0);
		    } else {
		    	config.setGenericFileVolumeReaderSource(commandLine.getOptionValue("i"));
		    }
		    if(commandLine.hasOption("w")) {
		    	config.setWordVolumeReaderStyleMappingFile(commandLine.getOptionValue("w"));
		    }
		    if(commandLine.hasOption("t")) {
		    	config.setMarkupDescriptionTreatmentTransformerParallelProcessing(true);
		    	String[] parallelParameters = commandLine.getOptionValues("t");
		    	if(parallelParameters.length != 2) {
		    		log(LogLevel.ERROR, "You have to specify 2 values for parameter t");
		    		System.exit(0);
		    	} else {
		    		try {
		    			int threadsPerDescriptionExtractor = Integer.parseInt(parallelParameters[0]);
		    			int threadsPerSentenceChunking = Integer.parseInt(parallelParameters[1]);
		    			config.setMarkupDescriptionTreatmentTransformerSentenceChunkerRunMaximum(threadsPerSentenceChunking);
		    			config.setMarkupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum(threadsPerDescriptionExtractor);
		    		} catch(Exception e) {
		    			log(LogLevel.ERROR, "Problem to convert parameter to Integer", e);
		    			System.exit(0);
		    		}
		    	}
		    }
		    if(commandLine.hasOption("n")) {
		    	config.setDatabaseHost(commandLine.getOptionValue("n"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a MySQL server hostname");
		    	System.exit(0);
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("p")) {
		    	config.setDatabasePort(commandLine.getOptionValue("p"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a MySQL server port");
		    	System.exit(0);
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("d")) {
		    	config.setDatabaseName(commandLine.getOptionValue("d"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database name");
		    	System.exit(0);
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("u")) {
		    	config.setDatabaseUser(commandLine.getOptionValue("u"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database user");
		    	System.exit(0);
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("s")) {
		    	config.setDatabasePassword(commandLine.getOptionValue("s"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database password");
		    	System.exit(0);
		    	//use standard value from RunConfig
		    }
			//TODO databaseTablePrefix has to be given as user as a ID he remembered from LearnMain
			//since we have no user information to be able to generate an ID that allows to know
			//at least whos data to pull
		    if(commandLine.hasOption("z")) {
		    	config.setDatabaseTablePrefix(commandLine.getOptionValue("z"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database table prefix");
		    	System.exit(0);
		    }
		} catch(ParseException e) {
			log(LogLevel.ERROR, "Problem parsing parameters", e);
		}
		
		config.setRun(IPlantMarkupRun.class);
		config.setMarkupDescriptionTreatmentTransformer(MarkupDescriptionTreatmentTransformer.class);
		config.setGlossary(InMemoryGlossary.class);
		config.setTerminologyLearner(DatabaseInputNoLearner.class);
	}
}
