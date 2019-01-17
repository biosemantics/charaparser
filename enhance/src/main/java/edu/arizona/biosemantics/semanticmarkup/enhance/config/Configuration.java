package edu.arizona.biosemantics.semanticmarkup.enhance.config;

import java.io.IOException;
import java.util.Properties;

import edu.arizona.biosemantics.common.log.Logger;

public class Configuration {

	private final static Logger logger = Logger.getLogger(Configuration.class);
	
	public static String glossariesDownloadDirectory;
	public static String ontologyDirectory;
	public static String wordNetDirectory;
	public static String defaultEntityType = "structure";
	private static String projectVersion;

	public static String oto2Url;

	public static String databaseUser;
	public static String databasePassword;
	public static String databaseHost;
	public static int databasePort;
	public static String databaseName;

	public static String workspaceDir;
	
	public static String nonSpecificParts;
	public static String spatial;
	public static String connectBackwardToParent;
	public static String connectForwardToParent;
	
	static {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		try {
			properties.load(loader.getResourceAsStream("edu/arizona/biosemantics/semanticmarkup/enhance/static.properties"));
			projectVersion = properties.getProperty("project.version");
			
			properties.load(loader.getResourceAsStream("edu/arizona/biosemantics/semanticmarkup/enhance/config.properties"));
			ontologyDirectory = properties.getProperty("ontologyDirectory");
			wordNetDirectory = properties.getProperty("wordNetDirectory");
			glossariesDownloadDirectory = properties.getProperty("glossariesDownloadDirectory");
			
			databaseUser = properties.getProperty("databaseUser");
			databasePassword = properties.getProperty("databasePassword");
			databaseHost = properties.getProperty("databaseHost");
			databasePort =  Integer.valueOf(properties.getProperty("databasePort"));
			databaseName = properties.getProperty("databaseName");
			
			oto2Url = properties.getProperty("oto2Url");
			workspaceDir = properties.getProperty("workspaceDir");
			
			nonSpecificParts = properties.getProperty("nonSpecificParts");
			spatial= properties.getProperty("nonSpecificParts");
			connectBackwardToParent= properties.getProperty("connectBackwardToParent");
			connectForwardToParent= properties.getProperty("connectForwardToParent");
			
		} catch (IOException e) {
			logger.error("Couldn't read configuration", e);
		}
	}
}
