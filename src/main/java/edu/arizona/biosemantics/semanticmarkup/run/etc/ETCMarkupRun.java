package edu.arizona.biosemantics.semanticmarkup.run.etc;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.db.ConnectionPool;
import edu.arizona.biosemantics.semanticmarkup.enhance.run.Run;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.CreateOrPopulateWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.MoveCharacterToStructureConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.MoveCharactersToAlternativeParent;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.MoveNegationOrAdverbBiologicalEntityConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.OrderBiologicalEntityConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.RemoveUselessCharacterConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.RemoveUselessWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.RenameCharacter;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.ReplaceNegationCharacterByNegationOrAbsence;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.ReplaceTaxonNameByWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.SortBiologicalEntityNameWithDistanceCharacter;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.SplitCompoundBiologicalEntitiesCharacters;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.SplitCompoundBiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.StandardizeCount;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.StandardizeQuantityPresence;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.StandardizeStructureName;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.StandardizeTerminology;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.run.AbstractRun;
import edu.arizona.biosemantics.semanticmarkup.run.PostRun;


public class ETCMarkupRun extends AbstractRun {

	private IMarkupCreator creator;
	private String databasePrefix;
	private String validateSchemaFile;
	private IGlossary glossary;
	private IInflector inflector;
	private IPOSKnowledgeBase posKnowledgeBase;
	private ICharacterKnowledgeBase characterKnowledgeBase;
	private Set<String> possessWords;

	@Inject
	public ETCMarkupRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("MarkupCreator") IMarkupCreator creator, 
			ConnectionPool connectionPool,
			@Named("DatabasePrefix") String databasePrefix,
			@Named("MarkupRun_ValidateSchemaFile") String validateSchemaFile, 
			IGlossary glossary, IInflector inflector, 
			@Named("LearnedPOSKnowledgeBase")IPOSKnowledgeBase posKnowledgeBase, 
			ICharacterKnowledgeBase characterKnowledgeBase, 
			@Named("PossessWords") Set<String> possessWords) {
		super(guiceModuleFile, inputDirectory, runOutDirectory, connectionPool);
		this.creator = creator;
		this.databasePrefix = databasePrefix;
		this.validateSchemaFile = validateSchemaFile;
		this.glossary = glossary;
		this.inflector = inflector;
		this.posKnowledgeBase = posKnowledgeBase;
		this.characterKnowledgeBase = characterKnowledgeBase;
		this.possessWords = possessWords;
	}

	@Override
	protected void doRun() throws Throwable {
		if(!isValidRun()) {
			log(LogLevel.ERROR, "Not a valid run. The specified ID has not been found as having successfully completed learning.");
			throw new IllegalArgumentException();
		}

		
		log(LogLevel.INFO, "Creating markup using " + creator.getDescription() + "...");
		creator.create();
		
		
		PostRun r = new PostRun(runOutDirectory);
		r.absorbKeys();
		
		Run run = new Run();
		run.addTransformer(new SplitCompoundBiologicalEntity(inflector));
		run.addTransformer(new SplitCompoundBiologicalEntitiesCharacters(inflector));
		run.addTransformer(new SortBiologicalEntityNameWithDistanceCharacter());
		run.addTransformer(new MoveCharactersToAlternativeParent());
		run.addTransformer(new ReplaceTaxonNameByWholeOrganism());
		run.addTransformer(new CreateOrPopulateWholeOrganism(glossary.getWordsInCategory("life_style"), "growth_form"));
		run.addTransformer(new CreateOrPopulateWholeOrganism(glossary.getWordsInCategory("duration"), "duration"));
		run.addTransformer(new RemoveUselessWholeOrganism());
		run.addTransformer(new ReplaceNegationCharacterByNegationOrAbsence());
		run.addTransformer(new StandardizeCount());
		run.addTransformer(new RemoveUselessCharacterConstraint());
		run.addTransformer(new MoveCharacterToStructureConstraint());
		Map<String, String> renames = new HashMap<String, String>();
		renames.put("count", "quantity");
		renames.put("atypical_count", "atypical_quantity");
		renames.put("color", "coloration");
		run.addTransformer(new RenameCharacter(renames));
		run.addTransformer(new StandardizeQuantityPresence());
		run.addTransformer(new OrderBiologicalEntityConstraint());
		run.addTransformer(new MoveNegationOrAdverbBiologicalEntityConstraint(posKnowledgeBase));
		run.addTransformer(new StandardizeTerminology(characterKnowledgeBase));
		run.addTransformer(new StandardizeStructureName(characterKnowledgeBase, possessWords));
		run.run(new File(runOutDirectory), new File(runOutDirectory));
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