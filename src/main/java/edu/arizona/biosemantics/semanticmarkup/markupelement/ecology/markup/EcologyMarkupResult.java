/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.markup;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResultVisitor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.Ecology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.EcologyFileList;

/**
 * @author Hong Cui
 *
 */
public class EcologyMarkupResult implements IMarkupResult{
	
	public EcologyMarkupResult(List<Ecology> ecology) {
		
	}
	
	public EcologyMarkupResult(EcologyFileList ecologyFileList) {
		
	}

	public List<Ecology> getResult() {
		return null;
	}

	@Override
	public void accept(IMarkupResultVisitor markupResultVisitor) {
		markupResultVisitor.visit(this);
	}



}
