package edu.arizona.biosemantics.semanticmarkup;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.arizona.biosemantics.semanticmarkup.config.RunConfig;
import edu.arizona.biosemantics.semanticmarkup.know.lib.InMemoryGlossary;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markup.MarkupChain;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.MOXyBinderDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.MOXyBinderDescriptionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.MOXyDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.DatabaseInputNoLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.run.iplant.IPlantMarkupRun;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.MarkupDescriptionTreatmentTransformer;
import edu.arizona.biosemantics.semanticmarkup.run.etc.ETCMarkupRun;


/**
 * Markup CLI Entry point into the processing of the charaparser framework
 * @author thomas rodenhausen
 */
public class ETCMarkupMain extends CLIMain {
	
	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		try {
			CLIMain cliMain = new ETCMarkupMain();
			cliMain.parse(args);
			cliMain.run();
		} catch(Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@Override
	public void parse(String[] args) throws IOException {
		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		
		//for iplant user shown configuration options
		options.addOption("i", "input", true, "input file or directory");
		options.addOption("c", "config", true, "config to use"); 
		options.addOption("z", "database-table-prefix", true, "database table prefix to use");
		options.addOption("m", "style mapping", true, "Optional style mapping to use for Word file input");
		options.addOption("y", "categorize terms", false, "If specified, indicates that one does not intend to categorize newly discovered terms to improve markup");
		
		//for iplant user hidden inputs, but still required or 'nice to have' configuration possibilities'
		options.addOption("f", "source", true, "source of the descriptions, e.g. fna v7");
		options.addOption("g", "user", true, "etc user submitting learned terms to oto lite");
		options.addOption("j", "bioportal user id", true, "bioportal user id to use for bioportal submission");
		options.addOption("k", "bioportal api key", true, "bioportal api key to use for bioportal submission");
		
		options.addOption("o", "oto lite url", true, "OTO Lite URL");
		options.addOption("b", "debug log", true, "location of debug log file");
		options.addOption("e", "error log", true, "location of error log file");
		options.addOption("w", "wordnet dict directory", true, "location of wordnet dict directory");
		options.addOption("l", "src directory", true, "location of src directory");
		options.addOption("a", "workspace directory", true, "location of workspace directory");
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
		    
			if (commandLine.hasOption("a")) {
				config.setWorkspaceDirectory(commandLine.getOptionValue("a"));
			}
			String workspace = config.getWorkspaceDirectory();
			if (commandLine.hasOption("b") && commandLine.hasOption("e")) {
				this.setupLogging(commandLine.getOptionValue("b"), commandLine.getOptionValue("e"));
			} else {
				setupLogging(workspace + File.separator +"debug.log", workspace + File.separator + "error.log");
			}
			if (commandLine.hasOption("f")) {
				config.setSourceOfDescriptions(commandLine.getOptionValue("f"));
			}
			if (commandLine.hasOption("g")) {
				config.setEtcUser(commandLine.getOptionValue("g"));
			}
			if (commandLine.hasOption("j")) {
				config.setBioportalUserId(commandLine.getOptionValue("j"));
			}
			if (commandLine.hasOption("k")) {
				config.setBioportalAPIKey(commandLine.getOptionValue("k"));
			}
			
			if(!commandLine.hasOption("y")) {
				config.setTermCategorizationRequired(true);
			}
		    
		    config.setDescriptionReader(MOXyBinderDescriptionReader.class);
		    if(!commandLine.hasOption("i")) {
		    	log(LogLevel.ERROR, "You have to specify an input file or directory");
		    	throw new IllegalArgumentException();
		    } else {
		    	config.setInputDirectory(commandLine.getOptionValue("i"));
		    }
		    if(commandLine.hasOption("m")) {
		    	//config.setWordVolumeReaderStyleMappingFile(commandLine.getOptionValue("m"));
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

			if (commandLine.hasOption("o")) {
				config.setOtoLiteClientURL(commandLine.getOptionValue("o"));
			}
            if (commandLine.hasOption("w")) {
                config.setWordNetSource(commandLine.getOptionValue("w"));
            }
			if (commandLine.hasOption("l")) {
				config.setPerlDirectory(commandLine.getOptionValue("l"));
			}
		} catch(ParseException e) {
			log(LogLevel.ERROR, "Problem parsing parameters", e);
		}
		
		config.setRun(ETCMarkupRun.class);
		config.setMarkupDescriptionTreatmentTransformer(MarkupDescriptionTreatmentTransformer.class);
		config.setGlossary(InMemoryGlossary.class);
		//no learning required, already passed learning and reviewed terms in OTO Lite 
		config.setTerminologyLearner(DatabaseInputNoLearner.class);
		config.setDescriptionWriter(MOXyBinderDescriptionWriter.class);
		config.setMarkupCreator(MarkupChain.class);
		config.setUseOtoCommuntiyDownload(true);
	}
	
	protected void setupLogging(String debugLog, String errorLog) {
		Logger rootLogger = Logger.getRootLogger();
		//rootLogger.getLoggerRepository().resetConfiguration(); //don't reset to keep log4j.properties configured logger for etc-wide logging
		addDebugErrorLoggers(rootLogger, debugLog, errorLog);
	}
}
