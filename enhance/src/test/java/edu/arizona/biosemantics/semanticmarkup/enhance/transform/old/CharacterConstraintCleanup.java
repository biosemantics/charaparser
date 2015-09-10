package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class CharacterConstraintCleanup {
	
	private CharacterConstraintCleanupTransformer transformer;
	private Element character1;
	private Element character2;
	
	public CharacterConstraintCleanup() {
		this.transformer = new CharacterConstraintCleanupTransformer();
	}
	
	@Test
	public void testOntologyIdMapping() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertEquals(null, character1.getAttributeValue("constraint"));
		assertEquals(null, character1.getAttributeValue("constraintid"));
		assertEquals("something", character2.getAttributeValue("constraint"));
		assertEquals("o2", character2.getAttributeValue("constraintid"));
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
		biologicalEntity1.setAttribute("id", "o1");
		biologicalEntity1.addContent(character1);
		biologicalEntity1.addContent(character2);
		character1.setAttribute("constraint", "something else");
		character1.setAttribute("constraintid", "o1");
		character2.setAttribute("constraint", "something");
		character2.setAttribute("constraintid", "o2");
		
		Document document = new Document(treatment);
		return document;
	}
}
