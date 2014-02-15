package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model;

import java.util.LinkedList;
import java.util.List;

public class Treatment {

	private List<Elevation> elevations = new LinkedList<Elevation>();

	public List<Elevation> getElevations() {
		return elevations;
	}

	public void setElevations(List<Elevation> elevations) {
		this.elevations = elevations;
	}

}
