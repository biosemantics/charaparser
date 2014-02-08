package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model;

import java.util.List;

public class ElevationsFileList {

	private List<ElevationsFile> elevationsFiles;

	public ElevationsFileList(List<ElevationsFile> elevationsFiles) {
		super();
		this.elevationsFiles = elevationsFiles;
	}

	public List<ElevationsFile> getElevationsFiles() {
		return elevationsFiles;
	}

	public void setElevationsFiles(List<ElevationsFile> elevationsFiles) {
		this.elevationsFiles = elevationsFiles;
	}
	
}
