package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.pos.POS;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.MoveNegationOrAdverbBiologicalEntityConstraint;

public class MoveNegationOrAdverbBiologicalEntityConstraintTest {
	
	private MoveNegationOrAdverbBiologicalEntityConstraint transformer;
	private Element character11;
	private Element biologicalEntity11;
	private Element biologicalEntity21;
	private Element character21;
	private Element character22;
	private Element character23;
	private Element relation21;
	private Element character31;
	private Element character32;
	private Element biologicalEntity31;
	private Element biologicalEntity41;
	private Element character41;
	private Element character42;
	private Element character43;
	private Element relation41;
	private Element relation42;
	private Element biologicalEntity51;
	
	public MoveNegationOrAdverbBiologicalEntityConstraintTest() {
		this.transformer = new MoveNegationOrAdverbBiologicalEntityConstraint(new IPOSKnowledgeBase() {
			@Override
			public boolean isNoun(String word) {
				return false;
			}
			@Override
			public boolean isAdjective(String word) {
				return false;
			}
			@Override
			public boolean isAdverb(String word) {
				if(word.equals("adverb")) {
					return true;
				}
				return false;
			}
			@Override
			public boolean isVerb(String word) {
				return false;
			}
			@Override
			public POS getMostLikleyPOS(String word) {
				return null;
			}
			@Override
			public boolean contains(String word) {
				return false;
			}
			@Override
			public List<String> getSingulars(String word) {
				return null;
			}
			@Override
			public void addVerb(String word) {
			}
			@Override
			public void addNoun(String word) {
			}
			@Override
			public void addAdjective(String word) {
			}
			@Override
			public void addAdverb(String word) {
			}
		});
	}

	@Test
	public void testConstraintNegationAndIsModifierCharacter() {
		Document document = createTestDocument1();
		transformer.transform(document);
		assertEquals(null, character11.getParent());
		assertEquals("red", biologicalEntity11.getAttributeValue("constraint"));
		
		assertTrue(biologicalEntity11.getContent().get(biologicalEntity11.getContentSize() - 1) instanceof Element);
		Element countElement = (Element)biologicalEntity11.getContent().get(biologicalEntity11.getContentSize() - 1);
		assertTrue(countElement.getAttributeValue("name").equals("count"));
		assertTrue(countElement.getAttributeValue("value").equals("0"));
	}
	
	@Test
	public void testConstraintNegationAndNoIsModifierCharacter() {
		Document document = createTestDocument2();
		transformer.transform(document);
		
		assertEquals("not", character21.getAttributeValue("modifier"));
		assertEquals("", character22.getAttributeValue("modifier"));
		assertEquals("not laterally", character23.getAttributeValue("modifier"));
		assertEquals("true", relation21.getAttributeValue("negation"));
	}
	
	@Test
	public void testAdverbConstraintHandleTrueCharacters() {
		Document document = createTestDocument3();
		transformer.transform(document);
		
		System.out.println(character31.getAttributeValue("modifier"));
		System.out.println(character32.getAttributeValue("modifier"));
		assertEquals(null, character31.getParent());
		assertEquals("adverb laterally", character32.getAttributeValue("modifier"));
		assertEquals("red", biologicalEntity31.getAttributeValue("constraint"));
	}
	
	@Test
	public void testAdverbConstraintModifyRelations() {
		Document document = createTestDocument4();
		transformer.transform(document);
		
		assertEquals("adverb", relation41.getAttributeValue("modifier"));
		assertEquals("adverb widely", relation42.getAttributeValue("modifier"));
	}
	
	@Test
	public void testAdverbConstraintCountElement() {
		Document document = createTestDocument5();
		transformer.transform(document);
		
		Element content = biologicalEntity51.getChildren().get(biologicalEntity51.getChildren().size()-1);
		assertEquals("count", content.getAttributeValue("name"));
		assertEquals("present", content.getAttributeValue("value"));
		assertEquals("adverb", content.getAttributeValue("modifier"));
	}
	
	private Document createTestDocument5() {
		Element treatment = new Element("treatment", Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		Element description = new Element("description");
		description.setAttribute("type", "morphology");
		treatment.addContent(description);
		Element statement = new Element("statement");
		description.addContent(statement);
		Element text = new Element("text");
		text.setText("This is a test sentence");
		biologicalEntity51 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity51);
		
		biologicalEntity51.setAttribute("type", "structure");
		biologicalEntity51.setAttribute("constraint", "adverb");
		
		Document document = new Document(treatment);
		return document;
	}

	private Document createTestDocument4() {
		Element treatment = new Element("treatment", Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		Element description = new Element("description");
		description.setAttribute("type", "morphology");
		treatment.addContent(description);
		Element statement = new Element("statement");
		description.addContent(statement);
		Element text = new Element("text");
		text.setText("This is a test sentence");
		biologicalEntity41 = new Element("biological_entity");
		Element biologicalEntity42 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity41);
		statement.addContent(biologicalEntity42);
		character41 = new Element("character");
		character42 = new Element("character");
		character43 = new Element("character");
		biologicalEntity41.addContent(character41);
		biologicalEntity41.addContent(character42);
		biologicalEntity41.addContent(character43);
		
		biologicalEntity41.setAttribute("type", "structure");
		biologicalEntity41.setAttribute("constraint", "adverb");
		biologicalEntity41.setAttribute("id", "o1");
		biologicalEntity42.setAttribute("id", "o2");
		
		character41.setAttribute("value", "red");
		character42.setAttribute("value", "red");
		character42.setAttribute("modifier", "not");
		character43.setAttribute("value", "red");
		character43.setAttribute("modifier", "laterally");
		
		relation41 = new Element("relation");
		relation41.setAttribute("from", "o1");
		relation41.setAttribute("negation", "false");
		relation41.setAttribute("to", "o2");
		
		relation42 = new Element("relation");
		relation42.setAttribute("modifier", "widely");
		relation42.setAttribute("from", "o1");
		relation42.setAttribute("negation", "false");
		relation42.setAttribute("to", "o2");
		
		statement.addContent(relation41);
		statement.addContent(relation42);
		
		Document document = new Document(treatment);
		return document;
	}

	private Document createTestDocument3() {
		Element treatment = new Element("treatment", Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		Element description = new Element("description");
		description.setAttribute("type", "morphology");
		treatment.addContent(description);
		Element statement = new Element("statement");
		description.addContent(statement);
		Element text = new Element("text");
		text.setText("This is a test sentence");
		
		biologicalEntity31 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity31);
		biologicalEntity31.setAttribute("constraint", "adverb");
		biologicalEntity31.setAttribute("type", "structure");
		
		character31 = new Element("character");
		biologicalEntity31.addContent(character31);
		character31.setAttribute("is_modifier", "true");
		character31.setAttribute("value", "red");
		
		character32 = new Element("character");
		biologicalEntity31.addContent(character32);
		character32.setAttribute("is_modifier", "false");
		character32.setAttribute("modifier", "laterally");
		character32.setAttribute("value", "red");
		
		
		Document document = new Document(treatment);
		return document;
	}

	private Document createTestDocument2() {
		Element treatment = new Element("treatment", Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		Element description = new Element("description");
		description.setAttribute("type", "morphology");
		treatment.addContent(description);
		Element statement = new Element("statement");
		description.addContent(statement);
		Element text = new Element("text");
		text.setText("This is a test sentence");
		biologicalEntity21 = new Element("biological_entity");
		Element biologicalEntity22 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity21);
		statement.addContent(biologicalEntity22);
		character21 = new Element("character");
		character22 = new Element("character");
		character23 = new Element("character");
		biologicalEntity21.addContent(character21);
		biologicalEntity21.addContent(character22);
		biologicalEntity21.addContent(character23);
		
		biologicalEntity21.setAttribute("type", "structure");
		biologicalEntity21.setAttribute("constraint", "never");
		biologicalEntity21.setAttribute("id", "o1");
		biologicalEntity22.setAttribute("id", "o2");
		
		character21.setAttribute("value", "red");
		character22.setAttribute("value", "red");
		character22.setAttribute("modifier", "not");
		character23.setAttribute("value", "red");
		character23.setAttribute("modifier", "laterally");
		
		relation21 = new Element("relation");
		relation21.setAttribute("from", "o1");
		relation21.setAttribute("negation", "false");
		relation21.setAttribute("to", "o2");
		
		statement.addContent(relation21);
		
		Document document = new Document(treatment);
		return document;
	}

	private Document createTestDocument1() {
		Element treatment = new Element("treatment", Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		Element description = new Element("description");
		description.setAttribute("type", "morphology");
		treatment.addContent(description);
		Element statement = new Element("statement");
		description.addContent(statement);
		Element text = new Element("text");
		text.setText("This is a test sentence");
		biologicalEntity11 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity11);
		character11 = new Element("character");
		biologicalEntity11.addContent(character11);
		
		biologicalEntity11.setAttribute("type", "structure");
		biologicalEntity11.setAttribute("constraint", "never");
		
		character11.setAttribute("is_modifier", "true");
		character11.setAttribute("value", "red");
		
		Document document = new Document(treatment);
		return document;
	}
}
