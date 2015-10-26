package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class CreateOrPopulateWholeOrganismTest {
	
	private CreateOrPopulateWholeOrganism transformer;
	private Element biologicalEntity1;
	private Element biologicalEntity2;
	private Element character1;
	private String target = "my_target";
	private String category = "my_category";
	private Element relation1;
	
	public CreateOrPopulateWholeOrganismTest() {
		Set<String> targets = new HashSet<String>();
		targets.add(target);
		this.transformer = new CreateOrPopulateWholeOrganism(targets, category);
	}
	
	@Test
	public void test1() {		
		Document document = createTestDocument1();
		transformer.transform(document);
		
		assertTrue(biologicalEntity2.getContent().contains(character1));
		assertEquals("whole_organism", biologicalEntity2.getAttributeValue("name"));
		assertEquals("", biologicalEntity2.getAttributeValue("name_original"));
		assertEquals("structure", biologicalEntity2.getAttributeValue("type"));
		Content categoryCharacter = biologicalEntity2.getContent().get(biologicalEntity2.getContentSize() - 1);
		assertTrue(categoryCharacter instanceof Element);
		Element categoryCharacterElement = (Element)categoryCharacter;
		assertEquals(category, categoryCharacterElement.getAttributeValue("name"));
		assertEquals(target, categoryCharacterElement.getAttributeValue("value"));
		assertEquals(biologicalEntity2.getAttributeValue("id"), relation1.getAttributeValue("from"));
		assertTrue(biologicalEntity1.getParent() == null);		
	}

	private Document createTestDocument1() {
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
		biologicalEntity1.setAttribute("id", "o1");
		biologicalEntity1.setAttribute("name", "my_target");
		character1 = new Element("character");
		biologicalEntity1.addContent(character1);
		
		relation1 = new Element("relation");
		relation1.setAttribute("from", "o1");
		relation1.setAttribute("to", "xzy");
		statement.addContent(relation1);
		
		biologicalEntity2.setAttribute("id", "o2");
		biologicalEntity2.setAttribute("name", "whole_organism");
				
		Document document = new Document(treatment);
		return document;
	}
	
	@Test
	public void test2() {		
		Document document = createTestDocument2();
		transformer.transform(document);
		
		assertTrue(biologicalEntity1.getContent().contains(character1));
		assertEquals("whole_organism", biologicalEntity1.getAttributeValue("name"));
		assertEquals("", biologicalEntity1.getAttributeValue("name_original"));
		assertEquals("structure", biologicalEntity1.getAttributeValue("type"));
		Content categoryCharacter = biologicalEntity1.getContent().get(biologicalEntity1.getContentSize() - 1);
		assertTrue(categoryCharacter instanceof Element);
		Element categoryCharacterElement = (Element)categoryCharacter;
		assertEquals(category, categoryCharacterElement.getAttributeValue("name"));
		assertEquals(target, categoryCharacterElement.getAttributeValue("value"));
		assertEquals(biologicalEntity1.getAttributeValue("id"), relation1.getAttributeValue("from"));
		assertTrue(biologicalEntity1.getParent() != null);		
	}
	
	private Document createTestDocument2() {
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
		biologicalEntity1.setAttribute("id", "o1");
		biologicalEntity1.setAttribute("name", "my_target");
		character1 = new Element("character");
		biologicalEntity1.addContent(character1);
		
		relation1 = new Element("relation");
		relation1.setAttribute("from", "o1");
		relation1.setAttribute("to", "xzy");
		statement.addContent(relation1);
				
		Document document = new Document(treatment);
		return document;
	}

	@Test
	public void test3() {		
		Document document = createTestDocument3();
		transformer.transform(document);
		
		assertTrue(biologicalEntity1.getContent().contains(character1));
		assertEquals("whole_organism", biologicalEntity1.getAttributeValue("name"));
		assertEquals("", biologicalEntity1.getAttributeValue("name_original"));
		assertEquals("structure", biologicalEntity1.getAttributeValue("type"));
		Content categoryCharacter = biologicalEntity1.getContent().get(biologicalEntity1.getContentSize() - 1);
		assertTrue(categoryCharacter instanceof Element);
		Element categoryCharacterElement = (Element)categoryCharacter;
		assertEquals(category, categoryCharacterElement.getAttributeValue("name"));
		assertEquals(target, categoryCharacterElement.getAttributeValue("value"));
		assertEquals(biologicalEntity1.getAttributeValue("id"), relation1.getAttributeValue("from"));
		assertTrue(biologicalEntity1.getParent() != null);		
	}
	
	private Document createTestDocument3() {
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
		biologicalEntity1.setAttribute("id", "o1");
		biologicalEntity1.setAttribute("name", "my_target");
		biologicalEntity1.setAttribute("name", "my_target");
		biologicalEntity1.setAttribute("geographical_constraint", "some constraint");
		character1 = new Element("character");
		biologicalEntity1.addContent(character1);
		
		relation1 = new Element("relation");
		relation1.setAttribute("from", "o1");
		relation1.setAttribute("to", "xzy");
		statement.addContent(relation1);
				
		Document document = new Document(treatment);
		return document;
	}
}
