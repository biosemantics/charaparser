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
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.MOXyDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.DatabaseInputNoLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.DescriptionMarkupAndOntologyMappingCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.run.iplant.IPlantMarkupRun;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.MarkupDescriptionTreatmentTransformer;
import edu.arizona.biosemantics.semanticmarkup.run.etc.ETCMarkupRun;


/**
 * Markup CLI Entry point into the processing of the charaparser framework
 * @author thomas rodenhausen
 */
public class ETCMarkupMain extends CLIMain {
	
	private final static Logger logger = Logger.getLogger(ETCMarkupMain.class);
	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		try {
			CLIMain cliMain = new ETCMarkupMain();
			cliMain.parse(args);
			cliMain.run();
			logger.debug("SemanticMarkup [TextCapture] completed successfully");
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
		options.addOption("s", "input sentence", true, "input sentence");
		options.addOption("a", "author", true, "author of the input sentence");
		options.addOption("t", "title", true, "name of the taxon the input sentence describes");
		
		options.addOption("fn", "family-name", true, "family name of the taxon ");
		options.addOption("gn", "genus-name", true, "genus name of the taxon ");
		options.addOption("sectn", "section-name", true, "section name of the taxon ");
		options.addOption("sn", "species-name", true, "species name of the taxon ");
		options.addOption("subsn", "subsp-name", true, "subsp name of the taxon ");
		
		options.addOption("fy", "family-authority", true, "family name authority of the taxon ");
		options.addOption("gy", "genus-authority", true, "genus name authority of the taxon ");
		options.addOption("secty", "section-authority", true, "section name authority of the taxon ");
		options.addOption("sy", "species-authority", true, "species name authority of the taxon ");
		options.addOption("subsy", "subsp-authority", true, "subsp name authority of the taxon ");
		
		options.addOption("fd", "family-authority-year", true, "year of the family authority");
		options.addOption("gd", "genus-authority-year", true, "year of the genus authority");
		options.addOption("sectd", "section-authority-year", true, "year of the section authority");
		options.addOption("sd", "species-authority-year", true, "year of the species authority");
		options.addOption("subsd", "subsp-authority-year", true, "year of the subsp authority");
		
		options.addOption("c", "config", true, "config to use"); 
		options.addOption("x", "empty glossary", false, "use an empty glossary");
		options.addOption("z", "database-table-prefix", true, "database table prefix to use");
		options.addOption("f", "source", true, "source of the descriptions, e.g. fna v7");
		options.addOption("g", "user", true, "user submitting learned terms to oto lite");
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
		    if(commandLine.hasOption("x")) {
		    	config.setUseEmptyGlossary(true);
		    }
			String workspace = config.getWorkspaceDirectory();
			if (commandLine.hasOption("f")) {
				config.setSourceOfDescriptions(commandLine.getOptionValue("f"));
			}
			if (commandLine.hasOption("g")) {
				config.setUser(commandLine.getOptionValue("g"));
			}
			//TODO databaseTablePrefix has to be given as user as a ID he remembered from LearnMain
			//since we have no user information to be able to generate an ID that allows to know
			//at least whose data to pull
		    if(commandLine.hasOption("z")) {
		    	config.setDatabaseTablePrefix(commandLine.getOptionValue("z"));
		    } else {
		    	log(LogLevel.ERROR, "You have to specify a database table prefix");
		    	throw new IllegalArgumentException();
		    }
		    
		    config.setDescriptionReader(MOXyBinderDescriptionReader.class);
		    if(!commandLine.hasOption("i") && !commandLine.hasOption("s")) {
		    	log(LogLevel.ERROR, "You have to specify an input file, directory, or sentence.");
		    	throw new IllegalArgumentException();
		    } else if (commandLine.hasOption("i")){
		    	config.setInputDirectory(commandLine.getOptionValue("i"));
		    } else {
		    	//a, t, y, d
		    	//genus and species names are required, all others have a default value 'unknown'.
		    	if(commandLine.hasOption("a")){
		    		config.setAuthor(commandLine.getOptionValue("a"));
		    	}else{
		    		config.setAuthor("unknown");
		    		log(LogLevel.INFO, "author not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("t")){
		    		config.setTitle(commandLine.getOptionValue("t"));
		    	}else{
		    		config.setTitle("unknown");
		    		log(LogLevel.INFO, "taxon name not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("fn")){
		    		config.setFName(commandLine.getOptionValue("fn"));
		    	}else{
		    		config.setFName("unknown");
		    		log(LogLevel.INFO, "family name not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("gn")){
		    		config.setGName(commandLine.getOptionValue("gn"));
		    	}else{
		    		log(LogLevel.ERROR, "You have to specify a genus name for the input sentence.");
			    	throw new IllegalArgumentException();
		    	}
		    	
		    	if(commandLine.hasOption("sectn")){
		    		config.setSectName(commandLine.getOptionValue("sectn"));
		    	}else{
		    		config.setSectName("unknown");
		    		log(LogLevel.INFO, "section name not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("sn")){
		    		config.setSName(commandLine.getOptionValue("sn"));
		    	}else{
		    		log(LogLevel.ERROR, "You have to specify a species name for the input sentence.");
			    	throw new IllegalArgumentException();
		    	}
		    	
		    	if(commandLine.hasOption("subsn")){
		    		config.setSubsName(commandLine.getOptionValue("subsn"));
		    	}else{
		    		config.setSubsName("unknown");
		    		log(LogLevel.INFO, "subspecies name not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	
		    	if(commandLine.hasOption("fy")){
		    		config.setFAuthority(commandLine.getOptionValue("fy"));
		    	}else{
		    		config.setFAuthority("unknown");
		    		log(LogLevel.INFO, "family authority not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("gy")){
		    		config.setGAuthority(commandLine.getOptionValue("gy"));
		    	}else{
		    		config.setGAuthority("unknown");
		    		log(LogLevel.INFO, "genus authority not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("secty")){
		    		config.setSectAuthority(commandLine.getOptionValue("secty"));
		    	}else{
		    		config.setSectAuthority("unknown");
		    		log(LogLevel.INFO, "section authority not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("sy")){
		    		config.setSAuthority(commandLine.getOptionValue("sy"));
		    	}else{
		    		config.setSAuthority("unknown");
		    		log(LogLevel.INFO, "species authority not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("subsy")){
		    		config.setSubsAuthority(commandLine.getOptionValue("subsy"));
		    	}else{
		    		config.setSubsAuthority("unknown");
		    		log(LogLevel.INFO, "subspecies authority not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("fd")){
		    		config.setFYear(commandLine.getOptionValue("fd"));
		    	}else{
		    		config.setFYear("unknown");
		    		log(LogLevel.INFO, "family authority year not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("gd")){
		    		config.setGYear(commandLine.getOptionValue("gd"));
		    	}else{
		    		config.setGYear("unknown");
		    		log(LogLevel.INFO, "genus authority year not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("sectd")){
		    		config.setSectYear(commandLine.getOptionValue("sectd"));
		    	}else{
		    		config.setSectYear("unknown");
		    		log(LogLevel.INFO, "sect authority year not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("sd")){
		    		config.setSYear(commandLine.getOptionValue("sd"));
		    	}else{
		    		config.setSYear("unknown");
		    		log(LogLevel.INFO, "species authority year not set for input sentence, use 'unknow'.");
		    	}
		    	
		    	if(commandLine.hasOption("subsd")){
		    		config.setSubsYear(commandLine.getOptionValue("subsd"));
		    	}else{
		    		config.setFYear("unknown");
		    		log(LogLevel.INFO, "subspecies authority year not set for input sentence, use 'unknow'.");
		    	}
		    	
		    		    	
		    	//set this after the above
		    	config.setInputSentence(commandLine.getOptionValue("s"));
		    	
		    	
		    }
			
			
		} catch(ParseException e) {
			log(LogLevel.ERROR, "Problem parsing parameters", e);
		}

		config.setDescriptionMarkupCreator(DescriptionMarkupAndOntologyMappingCreator.class);
		config.setRun(ETCMarkupRun.class);
		config.setGlossary(InMemoryGlossary.class);
		//no learning required, already passed learning and reviewed terms in OTO Lite 
		config.setTerminologyLearner(DatabaseInputNoLearner.class);
		config.setDescriptionWriter(MOXyBinderDescriptionWriter.class);
		config.setMarkupCreator(MarkupChain.class);
		//config.setUseOtoCommuntiyDownload(true);
		config.setUseOtoCommuntiyDownload(false);
		config.setWorkspaceDirectory(Configuration.workspaceDirectory);
	}
}