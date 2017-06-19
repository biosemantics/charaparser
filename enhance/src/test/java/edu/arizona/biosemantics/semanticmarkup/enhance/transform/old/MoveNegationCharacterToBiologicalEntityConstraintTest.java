/*package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertTrue;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class MoveNegationCharacterToBiologicalEntityConstraintTest {
	
	private MoveNegationCharacterToBiologicalEntityConstraint transformer;
	private Element biologicalEntity1;
	private Element character1;
	
	public MoveNegationCharacterToBiologicalEntityConstraintTest() {
		this.transformer = new MoveNegationCharacterToBiologicalEntityConstraint();
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertTrue(!biologicalEntity1.getContent().contains(character1));
		assertTrue(biologicalEntity1.getAttributeValue("constraint").equals("no constraint"));
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
		character1 = new Element("character");
		statement.addContent(text);
		statement.addContent(biologicalEntity1);
		biologicalEntity1.addContent(character1);

		text.setText("leafs some brown and more wider than long");
		biologicalEntity1.setAttribute("id", "o0");
		biologicalEntity1.setAttribute("name", "leaf");
		biologicalEntity1.setAttribute("name_original", "leafs");
		biologicalEntity1.setAttribute("constraint", "constraint");
		
		character1.setAttribute("name", "count");
		character1.setAttribute("value", "no");
		character1.setAttribute("is_modifier", "true");
		
		Document document = new Document(treatment);
		return document;
	}
}
*/