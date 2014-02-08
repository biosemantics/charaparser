package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model;

import java.util.List;

public class PhenologiesFileList {

	private List<PhenologiesFile> phenologiesFiles;

	public PhenologiesFileList(List<PhenologiesFile> phenologiesFiles) {
		super();
		this.phenologiesFiles = phenologiesFiles;
	}

	public List<PhenologiesFile> getPhenologiesFiles() {
		return phenologiesFiles;
	}

	public void setPhenologiesFiles(List<PhenologiesFile> phenologiesFiles) {
		this.phenologiesFiles = phenologiesFiles;
	}
	
}
