package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model;

import java.util.LinkedList;
import java.util.List;

public class Treatment {

	private List<Distribution> distributions = new LinkedList<Distribution>();

	public List<Distribution> getDistributions() {
		return distributions;
	}

	public void setDistributions(List<Distribution> distributions) {
		this.distributions = distributions;
	}

}
