package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Phenology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Treatment;

public class PhenologyTransformer implements IPhenologyTransformer {

	@Override
	public void transform(List<PhenologiesFile> phenologiesFiles) {
		for(PhenologiesFile phenologiesFile : phenologiesFiles) {
			int i = 0;
			for(Treatment treatment : phenologiesFile.getTreatments()) {
				for(Phenology phenology : treatment.getPhenologies()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("distribution" + i);
					statement.setText(phenology.getText());
					statements.add(statement);
					phenology.setStatements(statements);
				}
			}
		}
	}


}
