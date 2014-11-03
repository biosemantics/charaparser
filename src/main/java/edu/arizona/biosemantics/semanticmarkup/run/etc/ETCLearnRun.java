package edu.arizona.biosemantics.semanticmarkup.run.etc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ILearner;
import edu.arizona.biosemantics.semanticmarkup.run.AbstractRun;

public class ETCLearnRun extends AbstractRun {
	
	private ILearner learner;
	private String databaseHost;
	private String databasePort;
	private String databaseName;
	private String databaseUser;
	private String databasePassword;
	private String databasePrefix;

	/**
	 * @param outDirectory
	 * @param guiceModuleFile
	 * @param learner
	 */
	@Inject
	public ETCLearnRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("DatabaseHost") String databaseHost,
			@Named("DatabasePort") String databasePort,
			@Named("DatabaseName") String databaseName,
			@Named("DatabaseUser") String databaseUser, 
			@Named("DatabasePassword") String databasePassword,
			@Named("DatabasePrefix") String databasePrefix,
			ILearner learner) {
		super(guiceModuleFile, inputDirectory, runOutDirectory);
		this.learner = learner;
		this.databaseHost = databaseHost;
		this.databasePort = databasePort;
		this.databaseName = databaseName;
		this.databaseUser = databaseUser;
		this.databasePassword = databasePassword;
		this.databasePrefix = databasePrefix;
	}

	@Override
	protected void doRun() throws Throwable {
		if(!isValidRun()) {
			log(LogLevel.ERROR, "Not a valid run. The specified ID has already been used.");
			throw new IllegalArgumentException();
		}
		
		log(LogLevel.INFO, "Learning using " + learner.getDescription() + "...");
		learner.learn();
	}
	
	protected boolean isValidRun() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection("jdbc:mysql://" + databaseHost + ":" + databasePort +"/" + databaseName + "?connecttimeout=0&sockettimeout=0&autoreconnect=true", 
				databaseUser, databasePassword);
		
		String sql = "CREATE TABLE IF NOT EXISTS datasetprefixes (prefix varchar(100) NOT NULL, glossary_version varchar(10), oto_uploadid int(11) NOT NULL DEFAULT '-1', " +
				"oto_secret varchar(100) NOT NULL DEFAULT '', created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (prefix)) CHARACTER SET utf8 engine=innodb";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.execute();
		
		sql = "LOCK TABLES datasetprefixes WRITE";
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.execute();
		
		sql = "SELECT * FROM datasetprefixes WHERE prefix = ?";
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setString(1, databasePrefix);
		preparedStatement.execute();
		ResultSet resultSet = preparedStatement.getResultSet();
		if(resultSet.next()) {
			return false;
		}
		
		sql = "INSERT INTO datasetprefixes (prefix) VALUES (?)";
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setString(1, databasePrefix);
		preparedStatement.execute();
		
		sql = "UNLOCK TABLES";
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.execute();
		return true;
	}
}