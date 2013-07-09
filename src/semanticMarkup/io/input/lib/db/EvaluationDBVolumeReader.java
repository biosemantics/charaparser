package semanticMarkup.io.input.lib.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.model.Treatment;
import semanticMarkup.model.ValueTreatmentElement;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * EvaluationDBVolumeReader reads a list of 'dummy' treatments given treatment informations (from previous charaparser version 
 * generated from perl part) in a database 
 * This EvaluationDBVolumeReader is likely only used in transition and testing phase between the two charaparser version
 * @author rodenhausen
 */
public class EvaluationDBVolumeReader implements IVolumeReader {
	
	private String databasePrefix;
	private Connection connection;
	private Set<String> evaluationSources;

	/**
	 * @param databaseName
	 * @param databasePrefix
	 * @param databaseUser
	 * @param databasePassword
	 * @param selectedSources
	 * @throws Exception
	 */
	@Inject
	public EvaluationDBVolumeReader(
			@Named("databaseHost") String databaseHost,
			@Named("databasePort") String databasePort,
			@Named("databaseName") String databaseName,
			@Named("databasePrefix") String databasePrefix, 
			@Named("databaseUser") String databaseUser, 
			@Named("databasePassword") String databasePassword,
			@Named("selectedSources") Set<String> selectedSources) throws Exception {
		this.databasePrefix = databasePrefix;
		this.evaluationSources = selectedSources;
		
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://" + databaseHost + ":" + databasePort +"/" + databaseName + "?connecttimeout=0&sockettimeout=0&autoreconnect=true", 
				databaseUser, databasePassword);
	}
	


	@Override
	public List<Treatment> read() throws Exception {
		List<Treatment> treatments = new ArrayList<Treatment>();
		Set<String> addedTreatments = new HashSet<String>();
		
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select source, tag from " + this.databasePrefix + "_sentence order by sentid");
		while(resultSet.next()) {
			String source = resultSet.getString("source");
			
			if(evaluationSources.isEmpty() || evaluationSources.contains(source)) {
				String[] sourceIds = source.split(".txt-");
				
				String sourceId = sourceIds[0];//String.valueOf(Integer.valueOf(sourceIds[0])-1);
				if(!addedTreatments.contains(source)) {
					addedTreatments.add(source);
					Treatment t = new Treatment(source);
					t.addTreatmentElement(new ValueTreatmentElement("description", ""));
					treatments.add(t);
				}
			}
		}
		
		return treatments;
	}
}
