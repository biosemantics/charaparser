package semanticMarkup.ling.learn.lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.name.Named;

import edu.arizona.sirls.beans.Sentence;
import edu.arizona.sirls.beans.TermCategory;
import edu.arizona.sirls.beans.TermSynonym;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.net.IOTOClient;
import semanticMarkup.know.net.LocalGlossary;
import semanticMarkup.know.net.OTOGlossary;
import semanticMarkup.ling.learn.ILearner;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.log.LogLevel;

/**
 * OTOLearner learns by reading from an IVolumeReader and learning using an ITerminologyLearner
 * For learning, additional input is read from an IOTOClient
 * Moreover, learned results are handed to an IOTOClient
 * This can be used for the first and hence the 'learn' application for the iPlant integration
 * @author rodenhausen
 */
public class OTOLearner implements ILearner {

	private IVolumeReader volumeReader;
	private ITerminologyLearner terminologyLearner;
	private IOTOClient otoClient;
	private String databasePrefix;
	private IGlossary glossary;
	private Connection connection;
	
	/**
	 * @param volumeReader
	 * @param terminologyLearner
	 * @param otoClient
	 * @param databaseName
	 * @param databaseUser
	 * @param databasePassword
	 * @param databasePrefix
	 * @param glossary
	 * @throws Exception
	 */
	public OTOLearner(@Named("Learner_VolumeReader")IVolumeReader volumeReader, 
			ITerminologyLearner terminologyLearner, 
			IOTOClient otoClient, 
			@Named("databaseName")String databaseName,
			@Named("databaseUser")String databaseUser,
			@Named("databasePassword")String databasePassword,
			@Named("databasePrefix")String databasePrefix, 
			IGlossary glossary) throws Exception {	
		this.volumeReader = volumeReader;
		this.terminologyLearner = terminologyLearner;
		this.otoClient = otoClient;
		this.databasePrefix = databasePrefix;
		this.glossary = glossary;
		
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + databaseName, databaseUser, databasePassword);
	}
	
	@Override
	public void learn() throws Exception {
		List<Treatment> treatments = volumeReader.read();
		
		OTOGlossary otoGlossary = otoClient.read(databasePrefix);
		storeInLocalDB(otoGlossary);
		initGlossary(otoGlossary);
		
		terminologyLearner.learn(treatments);
		
		otoClient.put(readLocalGlossary(), databasePrefix);
	}
	
	/**
	 * TODO: OTO Webservice should probably only return one term category list.
	 * No need to return an extra term synonym list just because it might make sense to have them seperate in a relational database schema
	 * @param otoGlossary
	 */
	private void initGlossary(OTOGlossary otoGlossary) {
		for(TermCategory termCategory : otoGlossary.getTermCategories()) {
			glossary.addEntry(termCategory.getTerm(), termCategory.getCategory());
		}
	}

	
	private void storeInLocalDB(OTOGlossary otoGlossary) {
		try {
			Statement stmt = connection.createStatement();
	        String cleanupQuery = "DROP TABLE IF EXISTS " + 
									this.databasePrefix + "_term_category, " + 
									this.databasePrefix + "_syns;";
	        stmt.execute(cleanupQuery);
	        stmt.execute("CREATE TABLE IF NOT EXISTS " + this.databasePrefix + "_syns (`term` varchar(200) DEFAULT NULL, `synonym` varchar(200) DEFAULT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS " + this.databasePrefix + "_term_category (`term` varchar(100) DEFAULT NULL, `category` varchar(200) " +
					"DEFAULT NULL, `hasSyn` tinyint(1) DEFAULT NULL)");
			
			for(TermCategory termCategory : otoGlossary.getTermCategories()) {
				 stmt.execute("INSERT INTO " + this.databasePrefix + "_term_category (`term`, `category`, `hasSyn`) VALUES " +
				 		"('" + termCategory.getTerm() +"', '" + termCategory.getCategory() + "', '" + termCategory.getHasSyn() +"');");
					
			}
			for(TermSynonym termSynonym : otoGlossary.getTermSynonyms()) {
				 stmt.execute("INSERT INTO " + this.databasePrefix + "_syns (`term`, `synonym`) VALUES " +
					 		"('" + termSynonym.getTerm() +"', '" + termSynonym.getSynonym() + "');");
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, e);
		}
	}

	private LocalGlossary readLocalGlossary() {
		List<TermCategory> termCategories = new ArrayList<TermCategory>();
		List<Sentence> sentences = new ArrayList<Sentence>();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select * from " + this.databasePrefix + "_sentence order by sentid");
			while(resultSet.next()) {
				sentences.add(new Sentence(resultSet.getInt("sentid"), resultSet.getString("source"), resultSet.getString("sentence"),
						resultSet.getString("originalsentence"), resultSet.getString("lead"), resultSet.getString("status"), resultSet.getString("tag"),
						resultSet.getString("modifier"), resultSet.getString("charactersegment")));
			}
			
			resultSet = statement.executeQuery("select * from " + this.databasePrefix + "_term_category");
			while(resultSet.next()) {
				termCategories.add(new TermCategory(resultSet.getString("term"), resultSet.getString("category"), resultSet.getString("hasSyn")));
			}
		
		} catch(Exception e) {
			log(LogLevel.ERROR, e);
		}
		
		LocalGlossary localGlossary = new LocalGlossary(sentences, termCategories);
		return localGlossary;
	}

	@Override
	public String getDescription() {
		return this.getClass().toString();
	}

}
