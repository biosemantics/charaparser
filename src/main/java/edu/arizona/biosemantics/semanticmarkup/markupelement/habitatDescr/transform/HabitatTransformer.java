package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Habitat;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Treatment;

public class HabitatTransformer implements IHabitatTransformer {

	@Override
	public void transform(List<HabitatsFile> habitatsFiles) {
		for(HabitatsFile habitatsFile : habitatsFiles) {
			for(Treatment treatment : habitatsFile.getTreatments()) {
				for(Habitat habitat : treatment.getHabitats()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("d0");
					statement.setText(habitat.getText());
					statements.add(statement);
					habitat.setStatements(statements);
				}
			}
		}
	}


}
