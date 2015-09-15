package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertTrue;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class CharacterToStructureConstraint {
	
	private CharacterToStructureConstraintTransformer transformer;
	private Element biologicalEntity1;
	private Element character0;
	
	public CharacterToStructureConstraint() {
		this.transformer = new CharacterToStructureConstraintTransformer();
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertTrue(!biologicalEntity1.getChildren().contains(character0));
		assertTrue(biologicalEntity1.getAttributeValue("constraint").equals("existing constraint; value"));
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
		biologicalEntity1.setAttribute("id", "o0");
		biologicalEntity1.setAttribute("name", "leaf");
		
		character0 = new Element("character");
		character0.setAttribute("is_modifier", "true");
		character0.setAttribute("constraint", "existing constraint");
		character0.setAttribute("name", "structure");
		character0.setAttribute("value", "value");
		biologicalEntity1.addContent(character0);
		
		Document document = new Document(treatment);
		return document;
	}
}
