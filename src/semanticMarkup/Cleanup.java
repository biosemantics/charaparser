package semanticMarkup;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Cleanup {

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		
		String databaseHost = args[0];
		String databasePort = args[1];
		String databaseName = args[2];
		String databaseUser = args[3];
		String databasePassword = args[4];
		int numberOfDays = Integer.parseInt(args[5]);
		
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection(
				"jdbc:mysql://" + databaseHost + ":" + databasePort + 
				"?connectTimeout=0&socketTimeout=0&autoReconnect=true", databaseUser, databasePassword);
		String findOldTablesSql = "SELECT prefix FROM " + databaseName + ".datasetprefixes WHERE created < CURDATE() - " + numberOfDays;
		Statement findOldTablesStatement = connection.createStatement();
		findOldTablesStatement.execute(findOldTablesSql);
		ResultSet findOldTablesResultSet = findOldTablesStatement.getResultSet();
		while(findOldTablesResultSet.next()) { 
			String prefixToDelete = findOldTablesResultSet.getString(1);
			
			//delete tables with the prefix
			String findTablesSql = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE " +
					"table_schema = '" + databaseName + "' " +
					"AND TABLE_NAME LIKE '" + prefixToDelete + "_%'";
			Statement findTablesStatement = connection.createStatement();
			findTablesStatement.execute(findTablesSql);
			ResultSet findTablesResultSet = findTablesStatement.getResultSet();
			while(findTablesResultSet.next()) {
				String tableToDelete = findTablesResultSet.getString(1);
				String dropTableSql = "DROP TABLE " + databaseName + "." + tableToDelete;
				Statement dropTableStatement = connection.createStatement();
				dropTableStatement.executeUpdate(dropTableSql);
				dropTableStatement.close();
			}
			findTablesStatement.close();
		}
		findOldTablesStatement.close();
		connection.close();
	}
	
}
