package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class AbsencePresence {
	
	private AbsencePresenceTransformer transformer;
	private Element character1;
	private Element character2;
	private Element character3;
	private Element character4;
	private Element character5;
	
	public AbsencePresence() {
		this.transformer = new AbsencePresenceTransformer();
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertEquals("presence", character1.getAttributeValue("name"));
		assertEquals("absent", character1.getAttributeValue("value"));
		assertEquals("presence", character2.getAttributeValue("name"));
		assertEquals("present", character2.getAttributeValue("value"));
		assertEquals("presence", character3.getAttributeValue("name"));
		assertEquals("absent", character3.getAttributeValue("value"));
		assertEquals("quantity", character4.getAttributeValue("name"));
		assertEquals("4", character4.getAttributeValue("value"));
		assertEquals("count", character5.getAttributeValue("name"));
		assertEquals("3", character5.getAttributeValue("value"));
	}

	private Document createTestDocument() {
		Element treatment = new Element("treatment", Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		Element description = new Element("description");
		description.setAttribute("type", "morphology");
		treatment.addContent(description);
		Element statement = new Element("statement");
		description.addContent(statement);
		Element text = new Element("text");
		text.setText("This is a test sentence");
		Element biologicalEntity1 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity1);
		character1 = new Element("character");
		character2 = new Element("character");
		character3 = new Element("character");
		character4 = new Element("character");
		character5 = new Element("character");
		biologicalEntity1.addContent(character1);
		biologicalEntity1.addContent(character2);
		biologicalEntity1.addContent(character3);
		biologicalEntity1.addContent(character4);
		biologicalEntity1.addContent(character5);
		character1.setAttribute("name", "quantity");
		character1.setAttribute("value", "0");
		character2.setAttribute("name", "quantity");
		character2.setAttribute("value", "present");
		character3.setAttribute("name", "quantity");
		character3.setAttribute("value", "absent");
		character4.setAttribute("name", "quantity");
		character4.setAttribute("value", "4");
		character5.setAttribute("name", "count");
		character5.setAttribute("value", "3");
		
		Document document = new Document(treatment);
		return document;
	}
}
