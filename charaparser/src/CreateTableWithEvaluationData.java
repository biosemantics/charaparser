import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public class CreateTableWithEvaluationData {

	
	public static void main(String[] args) throws Exception {
		String evaluationDataPath = "evaluationData//FNAV19_AnsKey_CharaParser_Evaluation";
		
		String sqlNotIn = "";
		
		File folder = new File(evaluationDataPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String fileName = listOfFiles[i].getName();
				fileName = fileName.replace(".xml", "");
				String[] parts = fileName.split(".txt-");
				System.out.println("File " + listOfFiles[i].getName());
				System.out.println("treatment " + parts[0]);
				System.out.println("statement " + parts[1]);
				sqlNotIn += "'" + fileName + "',";
			}
		}
		sqlNotIn = sqlNotIn.substring(0, sqlNotIn.length() - 1);
		System.out.println(sqlNotIn);
		
		
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/" +
				"annotationevaluation?user=root&password=");

		// Statements allow to issue SQL queries to the database
		Statement statement = connection.createStatement();
		statement.executeUpdate("delete from fnav19_sentence WHERE source not in (" + sqlNotIn +")");
	}	
}

