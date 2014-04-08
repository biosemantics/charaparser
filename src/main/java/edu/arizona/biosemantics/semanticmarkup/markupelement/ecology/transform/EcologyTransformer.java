/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.transform;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.Ecology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.EcologyFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.Treatment;


/**
 * @author Hong Cui
 *
 */
public class EcologyTransformer implements IEcologyTransformer {

	@Override
	public void transform(List<EcologyFile> ecologyFiles) {

		for(EcologyFile ecologyFile : ecologyFiles) {
			int i = 0;
			for(Treatment treatment : ecologyFile.getTreatments()) {
				for(Ecology ecology : treatment.getEcology()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("ecology" + i++);
					statement.setText(ecology.getText());
					statements.add(statement);
					ecology.setStatements(statements);
				}
			}
		}
	}
}
