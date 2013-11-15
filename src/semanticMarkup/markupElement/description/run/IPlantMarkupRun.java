package semanticMarkup.markupElement.description.run;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markup.IMarkupCreator;
import semanticMarkup.run.AbstractRun;

public class IPlantMarkupRun extends AbstractRun {

	private IMarkupCreator creator;
	private String databaseHost;
	private String databasePort;
	private String databaseName;
	private String databaseUser;
	private String databasePassword;
	private String databasePrefix;

	@Inject
	public IPlantMarkupRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("MarkupCreator") IMarkupCreator creator, 
			@Named("databaseHost") String databaseHost,
			@Named("databasePort") String databasePort,
			@Named("databaseName") String databaseName,
			@Named("databaseUser") String databaseUser, 
			@Named("databasePassword") String databasePassword,
			@Named("databasePrefix") String databasePrefix) {
		super(guiceModuleFile, runOutDirectory);
		this.creator = creator;
		this.databaseHost = databaseHost;
		this.databasePort = databasePort;
		this.databaseName = databaseName;
		this.databaseUser = databaseUser;
		this.databasePassword = databasePassword;
		this.databasePrefix = databasePrefix;
	}

	@Override
	protected void doRun() throws Exception {
		if(!isValidRun()) {
			log(LogLevel.ERROR, "Not a valid run. The specified ID has not been found as having successfully completed learning.");
			return;
		}
		
		log(LogLevel.INFO, "Creating markup using " + creator.getDescription() + "...");
		creator.create();
	}
	
	private boolean isValidRun() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection("jdbc:mysql://" + databaseHost + ":" + databasePort +"/" + databaseName + "?connecttimeout=0&sockettimeout=0&autoreconnect=true", 
				databaseUser, databasePassword);
		
		String sql = "CREATE TABLE IF NOT EXISTS datasetprefixes (prefix varchar(100) NOT NULL, glossary_version varchar(10), oto_uploadid int(11) NOT NULL DEFAULT '-1', " +
				"oto_secret varchar(100) NOT NULL DEFAULT '', created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (prefix)) CHARACTER SET utf8 engine=innodb";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.execute();
		
		sql = "SELECT * FROM datasetprefixes WHERE prefix = ? AND oto_uploadid != -1";
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setString(1, databasePrefix);
		preparedStatement.execute();
		ResultSet resultSet = preparedStatement.getResultSet();
		if(resultSet.next()) {
			return true;
		}
		return false;
	}	
}
