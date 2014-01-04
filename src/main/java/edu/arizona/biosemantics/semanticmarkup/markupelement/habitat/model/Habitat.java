package edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.model;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.model.Element;


public class Habitat extends Element {

	private List<String> habitatParts = new LinkedList<String>();

	public String getText() {
		StringBuilder stringBuilder = new StringBuilder();
		for(String habitatPart : habitatParts) {
			stringBuilder.append(habitatPart + " ");
		}
		return stringBuilder.toString().trim();
	}

	public void setText(String text) {
		this.habitatParts = new LinkedList<String>();
		this.habitatParts.add(text);
	}

	public List<String> getHabitatParts() {
		return habitatParts;
	}

	public void setHabitatParts(List<String> habitatParts) {
		this.habitatParts = habitatParts;
	}

	@Override
	public void removeElementRecursively(Element element) {
		return;
	}
}
