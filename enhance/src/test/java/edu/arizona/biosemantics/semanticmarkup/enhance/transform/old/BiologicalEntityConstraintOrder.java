package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class BiologicalEntityConstraintOrder {
	
	private BiologicalEntityConstraintOrderTransformer transformer;
	private Element biologicalEntity1;
	
	public BiologicalEntityConstraintOrder() {
		this.transformer = new BiologicalEntityConstraintOrderTransformer();
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertEquals(null, biologicalEntity1.getAttributeValue("constraint"));
		assertEquals(null, biologicalEntity1.getAttributeValue("constraintid"));
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

		text.setText("leafs some brown and more");
		biologicalEntity1.setAttribute("name", "leaf");
		biologicalEntity1.setAttribute("constraint", "and more ; some brown");
		biologicalEntity1.setAttribute("name_original", "leafs");
		
		Document document = new Document(treatment);
		return document;
	}
}
