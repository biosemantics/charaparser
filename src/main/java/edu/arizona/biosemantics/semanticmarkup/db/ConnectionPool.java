package edu.arizona.biosemantics.semanticmarkup.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

import edu.arizona.biosemantics.common.log.LogLevel;

public class ConnectionPool {
	
	private BoneCP connectionPool;
	private Driver mySqlDriver;
	
	@Inject
	public ConnectionPool(@Named("DatabaseHost") String databaseHost,
			@Named("DatabasePort") String databasePort,
			@Named("DatabaseName") String databaseName,
			@Named("DatabasePrefix") String databasePrefix, 
			@Named("DatabaseUser") String databaseUser, 
			@Named("DatabasePassword") String databasePassword
			) throws ClassNotFoundException, SQLException, IOException {
		Class.forName("com.mysql.jdbc.Driver");
		mySqlDriver = DriverManager.getDriver("jdbc:mysql://" + databaseHost + ":" + databasePort + "/");
		
		String jdbcUrl = "jdbc:mysql://" + databaseHost + ":" + databasePort + "/" 
				+ databaseName + "?connecttimeout=0&sockettimeout=0&autoreconnect=true";
		
		BoneCPConfig config = new BoneCPConfig();
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(databaseUser); 
		config.setPassword(databasePassword);
		config.setMinConnectionsPerPartition(10);
		config.setMaxConnectionsPerPartition(20);
		config.setPartitionCount(2);
		config.setPoolName("charaparserPool");
		config.setDisableJMX(true);
		
		connectionPool = new BoneCP(config);
	}
	
	public Connection getConnection() throws SQLException {
		return connectionPool.getConnection();
	}
	
	public void shutdown() {
		this.connectionPool.shutdown();
	}
	
}
