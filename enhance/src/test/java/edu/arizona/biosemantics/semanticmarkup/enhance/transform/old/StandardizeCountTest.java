package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class StandardizeCountTest {
	
	private StandardizeCount transformer;
	private Element character1;
	private Element character2;
	private Element character3;
	private Element character4;
	private Element character5;
	private Element character11;
	private Element character22;
	private Element character33;
	
	public StandardizeCountTest() {
		this.transformer = new StandardizeCount();
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertEquals("0", character1.getAttributeValue("value"));
		assertEquals("3", character11.getAttributeValue("value"));
		assertEquals("absent", character2.getAttributeValue("value"));
		assertEquals("0", character22.getAttributeValue("value"));
		assertEquals("0", character3.getAttributeValue("value"));
		assertEquals("", character3.getAttributeValue("modifier"));
		assertEquals("present", character33.getAttributeValue("value"));
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
		character11 = new Element("character");
		character2 = new Element("character");
		character22 = new Element("character");
		character3 = new Element("character");
		character33 = new Element("character");
		character4 = new Element("character");
		character5 = new Element("character");
		biologicalEntity1.addContent(character1);
		biologicalEntity1.addContent(character11);
		biologicalEntity1.addContent(character2);
		biologicalEntity1.addContent(character22);
		biologicalEntity1.addContent(character3);
		biologicalEntity1.addContent(character33);
		
		character1.setAttribute("name", "count");
		character1.setAttribute("value", "none");
		
		character11.setAttribute("name", "count");
		character11.setAttribute("value", "3");
		
		character2.setAttribute("name", "count");
		character2.setAttribute("value", "absent");
		character2.setAttribute("modifier", "never");
		
		character22.setAttribute("name", "count");
		character22.setAttribute("value", "absent");
		
		character3.setAttribute("name", "count");
		character3.setAttribute("value", "present");
		character3.setAttribute("modifier", "never");
		
		character33.setAttribute("name", "count");
		character33.setAttribute("value", "present");
		
		Document document = new Document(treatment);
		return document;
	}
}
