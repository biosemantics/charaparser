package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model;

import java.util.LinkedList;
import java.util.List;

public class Treatment {

	private List<Habitat> habitats = new LinkedList<Habitat>();

	public List<Habitat> getHabitats() {
		return habitats;
	}

	public void setHabitats(List<Habitat> habitats) {
		this.habitats = habitats;
	}

}
