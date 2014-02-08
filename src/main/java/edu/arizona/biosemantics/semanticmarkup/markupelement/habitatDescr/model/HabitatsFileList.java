package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model;

import java.util.List;

public class HabitatsFileList {

	private List<HabitatsFile> habitatsFiles;

	public HabitatsFileList(List<HabitatsFile> habitatsFiles) {
		super();
		this.habitatsFiles = habitatsFiles;
	}

	public List<HabitatsFile> getHabitatsFiles() {
		return habitatsFiles;
	}

	public void setHabitatsFiles(List<HabitatsFile> habitatsFiles) {
		this.habitatsFiles = habitatsFiles;
	}
	
}
