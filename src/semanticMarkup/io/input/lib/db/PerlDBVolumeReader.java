package semanticMarkup.io.input.lib.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.io.input.IVolumeReader;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PerlDBVolumeReader implements IVolumeReader {

	private String databasePrefix;
	private Connection connection;

	@Inject
	public PerlDBVolumeReader(
			@Named("databaseName") String databaseName,
			@Named("databasePrefix") String databasePrefix, 
			@Named("databaseUser") String databaseUser, 
			@Named("databasePassword") String databasePassword) throws Exception {
		this.databasePrefix = databasePrefix;
		
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + databaseName, databaseUser, databasePassword);
	}
	
	@Override
	public List<Treatment> read() throws Exception {
		List<Treatment> treatments = new ArrayList<Treatment>();
		Set<String> addedTreatments = new HashSet<String>();
		
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select source from " + this.databasePrefix + "_sentence");
		while(resultSet.next()) {
			String source = resultSet.getString("source");
			
			String[] sourceIds = source.split(".txt-");
			
			String sourceId = String.valueOf(Integer.valueOf(sourceIds[0])-1);
			if(!addedTreatments.contains(sourceId)) {
				addedTreatments.add(sourceId);
				Treatment t = new Treatment(sourceId);
				t.addTreatmentElement(new ValueTreatmentElement("description", ""));
				treatments.add(t);
			}
		}
		
		return treatments;
	}
}
