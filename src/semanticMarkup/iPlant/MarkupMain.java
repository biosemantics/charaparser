package semanticMarkup.iPlant;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import semanticMarkup.CLIMain;
import semanticMarkup.RunConfig;
import semanticMarkup.core.transformation.lib.MarkupDescriptionTreatmentTransformer;
import semanticMarkup.io.input.GenericFileVolumeReader;
import semanticMarkup.know.lib.InMemoryGlossary;
import semanticMarkup.ling.learn.lib.DatabaseInputNoLearner;
import semanticMarkup.log.LogLevel;
import semanticMarkup.run.LearnRun;
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
		for(String arg : args)
			System.out.println(arg);
		CLIMain cliMain = new MarkupMain();
		cliMain.parse(args);
		cliMain.run();
	}

	@Override
	public void parse(String[] args) {
		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		options.addOption("c", "config", true, "config to use");
		options.addOption("o", "output", true, "output directory");
		options.addOption("i", "input", true, "input file or directory");
		options.addOption("r", "reader", true, "force an input reader. If the option is not provided input format will be detected");
		options.addOption("p", "multi-threading", true, "use multi-threading to compute the result");
		options.addOption("db", "database", true, "database to use");
		options.addOption("dbu", "database-user", true, "database user to use");
		options.addOption("dbp", "database-password", true, "database password to use");
		options.addOption("dbtp", "database-table-prefix", true, "database table prefix to use");
		options.addOption("dbgt", "database-glossary-table", true, "database glossary table to use");
		options.addOption("g", "glossary", true, "csv glossary to use");
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
		    	//use standard value specified in RunConfig
		    }
		    if(!commandLine.hasOption("i")) {
		    	log(LogLevel.ERROR, "You have to specify an input file or directory");
		    	System.exit(0);
		    }
		    if(commandLine.hasOption("r")) {
		    	setReaderSpecificConfigValues(config, commandLine.getOptionValue("r"), commandLine.getOptionValue("i"));
		    } else {
		    	//use GenericFileVolumeReader
		    	config.setMarkupCreatorVolumeReader(GenericFileVolumeReader.class);
		    	config.setGenericFileVolumeReaderSource(commandLine.getOptionValue("i"));
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
		    	System.exit(0);
		    }
		}
		catch( ParseException exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		}
		
		config.setTerminologyLearner(DatabaseInputNoLearner.class);
		config.setRun(MarkupRun.class);
		config.setMarkupDescriptionTreatmentTransformer(MarkupDescriptionTreatmentTransformer.class);
		config.setGlossary(InMemoryGlossary.class);
		//TODO databaseTablePrefix has to be given as user as a ID he remembered from LearnMain
		//since we have no user information to be able to generate an ID that allows to know
		//at least whos data to pull
		config.setDatabaseTablePrefix("ant_agosti");
	}
}
