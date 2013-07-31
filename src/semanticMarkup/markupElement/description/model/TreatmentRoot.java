package semanticMarkup.markupElement.description.model;

import java.util.LinkedList;
import java.util.List;

public class TreatmentRoot {

	private List<Description> descriptions = new LinkedList<Description>();

	public List<Description> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<Description> descriptions) {
		this.descriptions.addAll(descriptions);
	}

}
