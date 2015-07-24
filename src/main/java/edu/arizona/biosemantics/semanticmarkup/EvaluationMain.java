package edu.arizona.biosemantics.semanticmarkup;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.arizona.biosemantics.semanticmarkup.config.RunConfig;
import edu.arizona.biosemantics.semanticmarkup.ling.know.lib.CSVGlossary;
import edu.arizona.biosemantics.semanticmarkup.ling.know.lib.InMemoryGlossary;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.EvaluationDBDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.DatabaseInputFromEvaluationNoLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.run.DescriptionMarkupRun;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.MarkupDescriptionFromDBForEvaluationTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.MarkupDescriptionTreatmentTransformer;


public class EvaluationMain extends CLIMain {
	
	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		CLIMain cliMain = new EvaluationMain();
		cliMain.parse(args);
		cliMain.run();
	}

	@Override
	public void parse(String[] args) throws IOException {
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
		Option threadingOption = new Option("t", "multi-threading", true, "use multi-threading to compute the result");
		//threadingOption.setValueSeparator(',');
		options.addOption(threadingOption);
		options.addOption("h", "help", false, "shows the help");
		
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
					log(LogLevel.ERROR, "Couldn't instantiate default config", e);
					throw e;
				}
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a configuration to use");
		    	throw new IllegalArgumentException();
		    	//use standard config RunConfig
		    }
		    
		    config.setDescriptionReader(EvaluationDBDescriptionReader.class);
		    if(!commandLine.hasOption("i")) {
		    	log(LogLevel.ERROR, "You have to specify an input file or directory");
		    	throw new IllegalArgumentException();
		    } else {
		    	config.setInputDirectory(commandLine.getOptionValue("i"));
		    }
		    if(commandLine.hasOption("w")) {
		    	//config.setWordVolumeReaderStyleMappingFile(commandLine.getOptionValue("w"));
		    }
		    if(commandLine.hasOption("t")) {
		    	config.setMarkupDescriptionTreatmentTransformerParallelProcessing(true);
		    	String parallelParameter = commandLine.getOptionValue("t");
		    	String[] parallelParameters = parallelParameter.split(",");
		    	if(parallelParameters.length != 2) {
		    		log(LogLevel.ERROR, "You have to specify 2 values for parameter t");
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
		    if(commandLine.hasOption("n")) {
		    	config.setDatabaseHost(commandLine.getOptionValue("n"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a MySQL server hostname");
		    	throw new IllegalArgumentException();
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("p")) {
		    	config.setDatabasePort(commandLine.getOptionValue("p"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a MySQL server port");
		    	throw new IllegalArgumentException();
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("d")) {
		    	config.setDatabaseName(commandLine.getOptionValue("d"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database name");
		    	throw new IllegalArgumentException();
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("u")) {
		    	config.setDatabaseUser(commandLine.getOptionValue("u"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database user");
		    	throw new IllegalArgumentException();
		    	//use standard value from RunConfig
		    }
		    if(commandLine.hasOption("s")) {
		    	config.setDatabasePassword(commandLine.getOptionValue("s"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database password");
		    	throw new IllegalArgumentException();
		    	//use standard value from RunConfig
		    }
			//TODO databaseTablePrefix has to be given as user as a ID he remembered from LearnMain
			//since we have no user information to be able to generate an ID that allows to know
			//at least whos data to pull
		    if(commandLine.hasOption("z")) {
		    	config.setDatabaseTablePrefix(commandLine.getOptionValue("z"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database table prefix");
		    	throw new IllegalArgumentException();
		    }
		} catch(ParseException e) {
			log(LogLevel.ERROR, "Problem parsing parameters", e);
		}
		
		config.setRun(DescriptionMarkupRun.class);
		config.setMarkupDescriptionTreatmentTransformer(MarkupDescriptionFromDBForEvaluationTransformer.class);
		config.setGlossary(CSVGlossary.class);
		config.setTerminologyLearner(DatabaseInputFromEvaluationNoLearner.class);
	}
}
