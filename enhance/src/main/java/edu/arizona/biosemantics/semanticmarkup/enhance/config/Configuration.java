package edu.arizona.biosemantics.semanticmarkup.enhance.config;

import java.io.IOException;
import java.util.Properties;

import edu.arizona.biosemantics.common.log.Logger;

public class Configuration {

	private final static Logger logger = Logger.getLogger(Configuration.class);
	
	public static String ontologyDirectory;
	public static String wordNetDirectory;

	private static String projectVersion;
	
	static {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		try {
			properties.load(loader.getResourceAsStream("edu/arizona/biosemantics/semanticmarkup/enhance/static.properties"));
			projectVersion = properties.getProperty("project.version");
			
			properties.load(loader.getResourceAsStream("edu/arizona/biosemantics/semanticmarkup/enhance/config.properties"));
			ontologyDirectory = properties.getProperty("ontologyDirectory");
			wordNetDirectory = properties.getProperty("wordNetDirectory");
		} catch (IOException e) {
			logger.error("Couldn't read configuration", e);
		}
	}
}
