package semanticMarkup.io.input.lib.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.io.input.IVolumeReader;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class EvaluationDBVolumeReader implements IVolumeReader {
	
	private String databasePrefix;
	private Connection connection;
	private Set<String> evaluationSources;
	private ParentTagProvider parentTagProvider;

	@Inject
	public EvaluationDBVolumeReader(
			@Named("databaseName") String databaseName,
			@Named("databasePrefix") String databasePrefix, 
			@Named("databaseUser") String databaseUser, 
			@Named("databasePassword") String databasePassword,
			@Named("selectedSources") Set<String> selectedSources,
			@Named("parentTagProvider") ParentTagProvider parentTagProvider) throws Exception {
		this.databasePrefix = databasePrefix;
		this.evaluationSources = selectedSources;
		this.parentTagProvider = parentTagProvider;
		
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + databaseName, databaseUser, databasePassword);
	}
	


	@Override
	public List<Treatment> read() throws Exception {
		List<Treatment> treatments = new ArrayList<Treatment>();
		Set<String> addedTreatments = new HashSet<String>();
		HashMap<String, String> parentTags = new HashMap<String, String>();
		HashMap<String, String> grandParentTags = new HashMap<String, String>();
		
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select source, tag from " + this.databasePrefix + "_sentence order by sentid");
		String parentTag = "";
		String grandParentTag = "";
		while(resultSet.next()) {
			String source = resultSet.getString("source");
			String tag = resultSet.getString("tag");
			parentTags.put(source, parentTag);
			grandParentTags.put(source, grandParentTag);
			
			grandParentTag = parentTag;
			if(!tag.equals("ditto"))
				parentTag = tag;
			
				
			if(evaluationSources.contains(source)) {
				String[] sourceIds = source.split(".txt-");
				
				String sourceId = String.valueOf(Integer.valueOf(sourceIds[0])-1);
				if(!addedTreatments.contains(sourceId)) {
					addedTreatments.add(sourceId);
					Treatment t = new Treatment(sourceId);
					t.addTreatmentElement(new ValueTreatmentElement("description", ""));
					treatments.add(t);
				}
			}
		}
		this.parentTagProvider.init(parentTags, grandParentTags);
		
		return treatments;
	}
}
