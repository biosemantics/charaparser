package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model;

import java.util.LinkedList;
import java.util.List;

public class Treatment {

	private List<Phenology> phenologies = new LinkedList<Phenology>();

	public List<Phenology> getPhenologies() {
		return phenologies;
	}

	public void setPhenology(List<Phenology> phenologies) {
		this.phenologies = phenologies;
	}

}
