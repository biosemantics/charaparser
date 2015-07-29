package edu.arizona.biosemantics.semanticmarkup;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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

import edu.arizona.biosemantics.semanticmarkup.config.Configuration;
import edu.arizona.biosemantics.semanticmarkup.config.RunConfig;
import edu.arizona.biosemantics.common.ling.know.lib.InMemoryGlossary;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markup.MarkupChain;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.MOXyBinderDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.MOXyBinderDescriptionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.DatabaseInputNoLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.PerlTerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.DescriptionMarkupAndOntologyMappingCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.run.iplant.IPlantLearnRun;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.MarkupDescriptionTreatmentTransformer;
import edu.arizona.biosemantics.semanticmarkup.run.etc.ETCLearnRun;


/**
 * Learn CLI Entry point into the processing of the charaparser framework
 * @author thomas rodenhausen
 */
public class ETCLearnMain extends CLIMain {

	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		try {
			CLIMain cliMain = new ETCLearnMain();
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
		options.addOption("x", "empty glossary", false, "use an empty glossary");
		options.addOption("z", "database-table-prefix", true, "database table prefix to use");
		options.addOption("f", "source", true, "source of the descriptions, e.g. fna v7");
        options.addOption("g", "user", true, "user submitting learned terms");
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
		    //TODO databaseTablePrefix has to be given as user as a ID he remembered from LearnMain
			//since we have no user information to be able to generate an ID that allows to know
			//at least whos data to pull
		    String tablePrefix = "";
		    if(commandLine.hasOption("z")) {
		    	tablePrefix = commandLine.getOptionValue("z");
		    	config.setDatabaseTablePrefix(tablePrefix);
		    	config.setDatabaseGlossaryTable(tablePrefix + "_permanentGlossary");
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database table prefix");
		    	throw new IllegalArgumentException();
		    }
		    if(commandLine.hasOption("x")) {
		    	config.setUseEmptyGlossary(true);
		    }	    
            if(commandLine.hasOption("f")) {
                config.setSourceOfDescriptions(commandLine.getOptionValue("f"));
	        }
	        if(commandLine.hasOption("g")) {
	        	config.setUser(commandLine.getOptionValue("g"));
	        }
			    
		    config.setDescriptionReader(MOXyBinderDescriptionReader.class);
		    if(!commandLine.hasOption("i")) {
		    	log(LogLevel.ERROR, "You have to specify an input file or directory");
		    	throw new IllegalArgumentException();
		    } else {
		    	config.setInputDirectory(commandLine.getOptionValue("i"));
		    	//config.setGenericFileVolumeReaderSource(commandLine.getOptionValue("i"));
		    }

		} catch (ParseException e) {
			log(LogLevel.ERROR, "Problem parsing parameters", e);
		}
		
		config.setDescriptionMarkupCreator(DescriptionMarkupAndOntologyMappingCreator.class);
		config.setRun(ETCLearnRun.class);
		config.setGlossary(InMemoryGlossary.class);
		config.setTerminologyLearner(PerlTerminologyLearner.class);
		config.setDescriptionWriter(MOXyBinderDescriptionWriter.class);
		config.setOto2ReviewFile("nextStep.html");
		config.setMarkupCreator(MarkupChain.class);	
		//config.setUseOtoCommuntiyDownload(true);
		config.setUseOtoCommuntiyDownload(false);
		config.setWorkspaceDirectory(Configuration.workspaceDirectory);
	}
}
