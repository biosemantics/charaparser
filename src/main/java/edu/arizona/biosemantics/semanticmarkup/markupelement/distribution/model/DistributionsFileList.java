package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model;

import java.util.List;

public class DistributionsFileList {

	private List<DistributionsFile> distributionsFiles;

	public DistributionsFileList(List<DistributionsFile> distributionsFiles) {
		super();
		this.distributionsFiles = distributionsFiles;
	}

	public List<DistributionsFile> getDistributionsFiles() {
		return distributionsFiles;
	}

	public void setDescriptionsFiles(List<DistributionsFile> distributionsFiles) {
		this.distributionsFiles = distributionsFiles;
	}
	
}
