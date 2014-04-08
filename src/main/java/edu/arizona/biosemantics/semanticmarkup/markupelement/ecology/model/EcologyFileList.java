/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;

/**
 * @author Hong Cui
 *
 */
public class EcologyFileList {
	private List<EcologyFile> ecologyFiles;

	public EcologyFileList(List<EcologyFile> ecologyFiles) {
		super();
		this.ecologyFiles = ecologyFiles;
	}

	public List<EcologyFile> getEcologyFiles() {
		return ecologyFiles;
	}

	public void setEcologyFiles(List<EcologyFile> ecologyFiles) {
		this.ecologyFiles = ecologyFiles;
	}
}
