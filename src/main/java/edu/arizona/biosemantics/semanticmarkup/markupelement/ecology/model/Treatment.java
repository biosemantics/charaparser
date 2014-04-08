/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model;

import java.util.LinkedList;
import java.util.List;


/**
 * @author Hong Cui
 *
 */
public class Treatment {
	private List<Ecology> ecology = new LinkedList<Ecology>();

	public List<Ecology> getEcology() {
		return ecology;
	}

	public void setEcology(List<Ecology> ecology) {
		this.ecology = ecology;
	}

}
