package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class SortBiologicalEntityNameWithDistanceCharacterTest  {
	
	private SortBiologicalEntityNameWithDistanceCharacter transformer;
	private Element biologicalEntity;
	
	public SortBiologicalEntityNameWithDistanceCharacterTest() {
		this.transformer = new SortBiologicalEntityNameWithDistanceCharacter();
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertEquals("epigastrium-spiracle", biologicalEntity.getAttributeValue("name"));
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
		biologicalEntity = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity);
		biologicalEntity.setAttribute("name", "spiracle-epigastrium");
		Element character = new Element("character");
		character.setAttribute("name", "distance");
		biologicalEntity.addContent(character);
		
		Document document = new Document(treatment);
		return document;
	}
}
