package semanticMarkup.markupElement.habitat.model;

import java.util.LinkedList;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.DefaultJDOMFactory;
import org.jdom2.JDOMFactory;

import semanticMarkup.model.Element;

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
}
