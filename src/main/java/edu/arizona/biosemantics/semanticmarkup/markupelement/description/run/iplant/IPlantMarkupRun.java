package edu.arizona.biosemantics.semanticmarkup.markupelement.description.run.iplant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.db.ConnectionPool;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.run.AbstractRun;

public class IPlantMarkupRun extends AbstractRun {

	private IMarkupCreator creator;
	private String databasePrefix;

	@Inject
	public IPlantMarkupRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("MarkupCreator") IMarkupCreator creator,  
			@Named("DatabasePrefix") String databasePrefix, 
			ConnectionPool connectionPool) {
		super(guiceModuleFile, inputDirectory, runOutDirectory, connectionPool);
		this.creator = creator;
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
		try(Connection connection = connectionPool.getConnection()) {
			String sql = "CREATE TABLE IF NOT EXISTS datasetprefixes (prefix varchar(100) NOT NULL, glossary_version varchar(10), oto_uploadid int(11) NOT NULL DEFAULT '-1', " +
					"oto_secret varchar(100) NOT NULL DEFAULT '', created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (prefix)) CHARACTER SET utf8 engine=innodb";
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				preparedStatement.execute();
			}
			
			sql = "SELECT * FROM datasetprefixes WHERE prefix = ? AND oto_uploadid != -1";
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				preparedStatement.setString(1, databasePrefix);
				preparedStatement.execute();
				try(ResultSet resultSet = preparedStatement.getResultSet()) {
					if(resultSet.next()) {
						return true;
					}
					return false;
				}
			}
		}
	}	
}