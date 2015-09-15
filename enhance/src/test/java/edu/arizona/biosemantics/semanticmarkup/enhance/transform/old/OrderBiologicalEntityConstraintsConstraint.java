package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertTrue;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class OrderBiologicalEntityConstraintsConstraint {
	
	private OrderBiologicalEntityConstraintsTransformer transformer;
	private Element biologicalEntity1;
	
	public OrderBiologicalEntityConstraintsConstraint() {
		this.transformer = new OrderBiologicalEntityConstraintsTransformer();
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);

		assertTrue(biologicalEntity1.getAttributeValue("constraint").equals("some brown ; more ; wider than long"));
	}

	private Document createTestDocument() {
		Element treatment = new Element("treatment", Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		Element description = new Element("description");
		description.setAttribute("type", "morphology");
		treatment.addContent(description);
		Element statement = new Element("statement");
		description.addContent(statement);
		Element text = new Element("text");
		biologicalEntity1 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity1);

		text.setText("leafs some brown and more wider than long");
		biologicalEntity1.setAttribute("id", "o0");
		biologicalEntity1.setAttribute("name", "leaf");
		biologicalEntity1.setAttribute("name_original", "leafs");
		biologicalEntity1.setAttribute("constraint", "more ; wider than long ; some brown");
		
		
		Document document = new Document(treatment);
		return document;
	}
}
