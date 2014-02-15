package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.transform;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Elevation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Treatment;

public class ElevationTransformer implements IElevationTransformer {

	@Override
	public void transform(List<ElevationsFile> elevationsFiles) {
		for(ElevationsFile elevationsFile : elevationsFiles) {
			int i = 0;
			for(Treatment treatment : elevationsFile.getTreatments()) {
				for(Elevation elevation : treatment.getElevations()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("elevation" + i++);
					statement.setText(elevation.getText());
					statements.add(statement);
					elevation.setStatements(statements);
				}
			}
		}
	}

}
