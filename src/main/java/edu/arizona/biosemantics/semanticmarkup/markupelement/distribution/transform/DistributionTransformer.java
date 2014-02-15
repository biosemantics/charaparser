package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.transform;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Distribution;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Treatment;

public class DistributionTransformer implements IDistributionTransformer {

	@Override
	public void transform(List<DistributionsFile> distributionsFiles) {
		for(DistributionsFile distributionsFile : distributionsFiles) {
			int i = 0;
			for(Treatment treatment : distributionsFile.getTreatments()) {
				for(Distribution distribution : treatment.getDistributions()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("distribution" + i++);
					statement.setText(distribution.getText());
					statements.add(statement);
					distribution.setStatements(statements);
				}
			}
		}
	}

}
