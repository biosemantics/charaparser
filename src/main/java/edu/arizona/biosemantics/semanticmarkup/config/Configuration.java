package edu.arizona.biosemantics.semanticmarkup.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import edu.arizona.biosemantics.common.log.Logger;

public class Configuration {

	private final static Logger logger = Logger.getLogger(Configuration.class);

	public static String databaseHost;
	public static String databasePort;
	public static String databaseName;
	public static String databaseUser;
	public static String databasePassword;
	public static int database_minConnectionsPerPartition;
	public static int database_maxConnectionsPerPartition;
	public static int database_partitionCount;
	public static String workspaceDirectory;
	public static String perlSourceDirectory;
	public static String wordNetSourceDirectory;
	public static String glossariesDownloadDirectory;
	public static String ontologiesDirectory;
	public static Set<String> ontologiesToUse;
	public static String otoUrl;
	public static String oto2Url;
	public static boolean threadingActive;
	public static int descriptionThreads;
	public static int sentenceThreads;
	
	private static Properties properties;
	
	static {		
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			properties = new Properties(); 
			properties.load(loader.getResourceAsStream("edu/arizona/biosemantics/semanticmarkup/config.properties"));
			
			databaseHost = properties.getProperty("databaseHost");
			databasePort = properties.getProperty("databasePort");
			databaseName = properties.getProperty("databaseName");
			databaseUser = properties.getProperty("databaseUser");
			databasePassword = properties.getProperty("databasePassword");
			database_minConnectionsPerPartition = Integer.valueOf(properties.getProperty("database_minConnectionsPerPartition"));
			database_maxConnectionsPerPartition = Integer.valueOf(properties.getProperty("database_maxConnectionsPerPartition"));
			database_partitionCount = Integer.valueOf(properties.getProperty("database_partitionCount"));
			workspaceDirectory = properties.getProperty("workspaceDirectory");
			perlSourceDirectory = properties.getProperty("perlSourceDirectory");
			wordNetSourceDirectory = properties.getProperty("wordNetSourceDirectory");
			glossariesDownloadDirectory = properties.getProperty("glossariesDownloadDirectory");
			ontologiesDirectory = properties.getProperty("ontologiesDirectory");
			otoUrl = properties.getProperty("otoUrl");
			oto2Url = properties.getProperty("oto2Url");
			
			String threading = properties.getProperty("threading");
			String[] threadingParts = threading.split(",");
			if(threadingParts.length == 3) {
				threadingActive = Boolean.valueOf(threadingParts[0]);
				descriptionThreads = Integer.valueOf(threadingParts[1]);
				sentenceThreads = Integer.valueOf(threadingParts[2]);
			} else {
				threadingActive = false;
				descriptionThreads = 1;
				sentenceThreads = 1;
			}
		} catch(Exception e) {
			logger.error("Couldn't read configuration", e);
		}
	}
	
	public static String asString() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
			return writer.writeValueAsString(properties);
		} catch (Exception e) {
			logger.error("Couldn't write configuration as string", e);
			return null;
		}
	}
	
}
