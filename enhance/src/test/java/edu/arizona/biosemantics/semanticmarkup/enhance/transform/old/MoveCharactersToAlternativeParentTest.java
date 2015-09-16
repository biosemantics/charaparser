package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertTrue;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class MoveCharactersToAlternativeParentTest {
	
	private MoveCharactersToAlternativeParent transformer;
	private Element biologicalEntity1;
	private Element biologicalEntity2;
	private Element character0;
	private Element character1;
	private Element character2;
	
	public MoveCharactersToAlternativeParentTest() {
		this.transformer = new MoveCharactersToAlternativeParent();
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertTrue(biologicalEntity1.getChildren().contains(character0));
		assertTrue(!biologicalEntity1.getChildren().contains(character1));
		assertTrue(!biologicalEntity1.getChildren().contains(character2));
		assertTrue(!biologicalEntity2.getChildren().contains(character0));
		assertTrue(biologicalEntity2.getChildren().contains(character1));
		assertTrue(biologicalEntity2.getChildren().contains(character2));
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
		biologicalEntity2 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity1);
		statement.addContent(biologicalEntity2);

		text.setText("leafs some brown and more");
		biologicalEntity1.setAttribute("id", "o0");
		biologicalEntity1.setAttribute("name", "leaf");
		
		character0 = new Element("character");
		character0.setAttribute("name", "count");
		character1 = new Element("character");
		character1.setAttribute("name", "count");
		character1.setAttribute("notes", "alterIDs: o1");
		character2 = new Element("character");
		character2.setAttribute("notes", "alterIDs: o1");
		biologicalEntity1.addContent(character0);
		biologicalEntity1.addContent(character1);
		biologicalEntity1.addContent(character2);

		biologicalEntity2.setAttribute("id", "o1");
		biologicalEntity2.setAttribute("name", "stem");
		biologicalEntity2.setAttribute("name_original", "leafs");
		
		Document document = new Document(treatment);
		return document;
	}
}
