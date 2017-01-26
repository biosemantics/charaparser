package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Parent;
import org.junit.Test;

import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SplitCompoundBiologicalEntitiesCharacters;

public class SplitCompoundBiologicalEntitiesCharactersTest {
	
	private SplitCompoundBiologicalEntitiesCharacters transformer;
	private Element biologicalEntity1;
	private Element character1;
	private Element relationA;
	private Element relationB;
	
	public SplitCompoundBiologicalEntitiesCharactersTest() {
		this.transformer = new SplitCompoundBiologicalEntitiesCharacters(new IInflector() {
			@Override
			public String getSingular(String word) {
				return word;
			}

			@Override
			public String getPlural(String word) {
				return word;
			}

			@Override
			public boolean isPlural(String word) {
				return false;
			}
		});
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		
		Parent biologicalEntity1Parent = biologicalEntity1.getParent();
		int biologicalEntity1ParentIndex = biologicalEntity1Parent.indexOf(biologicalEntity1);
		
		Parent relationAParent = relationA.getParent();
		int relationAParentIndex = relationAParent.indexOf(relationA);
		Parent relationBParent = relationB.getParent();
		int relationBParentIndex = relationAParent.indexOf(relationB);
		transformer.transform(document);
		
		assertEquals(null, biologicalEntity1.getParent());
		Content contentA = biologicalEntity1Parent.getContent(biologicalEntity1ParentIndex);
		Content contentB = biologicalEntity1Parent.getContent(biologicalEntity1ParentIndex + 1);
		assertTrue(contentA instanceof Element);
		assertTrue(contentB instanceof Element);
		Element biologicalEntityA = (Element)contentA;
		Element biologicalEntityB = (Element)contentB;
		assertTrue(biologicalEntityA.getAttributeValue("name").equals("tibia"));
		assertTrue(biologicalEntityA.getAttributeValue("constraint").equals("leg-1"));
		assertTrue(biologicalEntityA.getChildren().get(0).getAttributeValue("name").equals("length"));
		assertTrue(biologicalEntityA.getChildren().get(0).getAttributeValue("value").equals("1.43"));
		
		assertTrue(biologicalEntityB.getAttributeValue("name").equals("metatarsu"));
		assertTrue(biologicalEntityB.getAttributeValue("constraint").equals("leg-1"));
		assertTrue(biologicalEntityB.getChildren().get(0).getAttributeValue("name").equals("length"));
		assertTrue(biologicalEntityB.getChildren().get(0).getAttributeValue("value").equals("1.27"));
		
		Content contentA1 = relationAParent.getContent(relationAParentIndex + 1);
		Content contentA2 = relationAParent.getContent(relationAParentIndex + 2);
		assertTrue(contentA1 instanceof Element);
		assertTrue(contentA2 instanceof Element);
		Element relationA1 = (Element)contentA1;
		Element relationA2 = (Element)contentA2;
		assertTrue(relationA1.getAttributeValue("from").equals("o0_0"));
		assertTrue(relationA2.getAttributeValue("from").equals("o0_1"));
		
		Content contentB1 = relationBParent.getContent(relationBParentIndex + 2);
		Content contentB2 = relationBParent.getContent(relationBParentIndex + 3);
		assertTrue(contentB1 instanceof Element);
		assertTrue(contentB2 instanceof Element);
		Element relationB1 = (Element)contentB1;
		Element relationB2 = (Element)contentB2;
		assertTrue(relationB1.getAttributeValue("to").equals("o0_0"));
		assertTrue(relationB2.getAttributeValue("to").equals("o0_1"));
	}

	/*<biological_entity constraint="leg-1" id="o3" name="tibia/metatarsu" name_original="tibia/metatarsus" type="structure">
	<character name="length" unit="mm" value="1.43/1.27" />*/
	private Document createTestDocument() {
		Element treatment = new Element("treatment", Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		Element description = new Element("description");
		description.setAttribute("type", "morphology");
		treatment.addContent(description);
		Element statement = new Element("statement");
		description.addContent(statement);
		Element text = new Element("text");
		biologicalEntity1 = new Element("biological_entity");
		Element biologicalEntity2 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity1);
		statement.addContent(biologicalEntity2);
		
		text.setText("leafs some brown and more");
		biologicalEntity1.setAttribute("id", "o0");
		biologicalEntity1.setAttribute("constraint", "leg-1");
		biologicalEntity1.setAttribute("name", "tibia/metatarsu");
		biologicalEntity1.setAttribute("name_original", "tibia/metatarsu");
		biologicalEntity1.setAttribute("type", "structure");
		biologicalEntity2.setAttribute("id", "o1");
		
		character1 = new Element("character");
		character1.setAttribute("name", "length");
		character1.setAttribute("unit", "mm");
		character1.setAttribute("value", "1.43/1.27");
		biologicalEntity1.addContent(character1);
		
		relationA = new Element("relation");
		relationA.setAttribute("from", "o0");
		relationA.setAttribute("to", "o1");
		relationB = new Element("relation");
		relationB.setAttribute("to", "o0");
		relationB.setAttribute("from", "o1");
		statement.addContent(relationA);
		statement.addContent(relationB);
		
		Document document = new Document(treatment);
		return document;
	}
}
