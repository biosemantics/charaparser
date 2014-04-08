/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Hong Cui
 *
 */
public class EcologyFile {


	private File file;
	private List<Treatment> treatments = new LinkedList<Treatment>();

	public List<Treatment> getTreatments() {
		return treatments;
	}

	public void setTreatments(List<Treatment> treatments) {
		this.treatments = treatments;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
