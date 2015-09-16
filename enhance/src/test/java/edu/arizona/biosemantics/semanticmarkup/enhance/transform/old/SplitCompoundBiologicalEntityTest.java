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

public class SplitCompoundBiologicalEntityTest {
	
	private SplitCompoundBiologicalEntity transformer;
	private Element biologicalEntity1;
	private Element character1;
	private Element relationA;
	private Element relationB;
	
	public SplitCompoundBiologicalEntityTest() {
		this.transformer = new SplitCompoundBiologicalEntity(new IInflector() {
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
		Content contentC = biologicalEntity1Parent.getContent(biologicalEntity1ParentIndex + 2);
		assertTrue(contentA instanceof Element);
		assertTrue(contentB instanceof Element);
		assertTrue(contentC instanceof Element);
		Element biologicalEntityA = (Element)contentA;
		Element biologicalEntityB = (Element)contentB;
		Element biologicalEntityC = (Element)contentC;
		assertTrue(biologicalEntityA.getAttributeValue("name").equals("i"));
		assertTrue(biologicalEntityA.getAttributeValue("constraint").equals("legs"));
		assertTrue(biologicalEntityA.getChildren().get(0).getAttributeValue("name").equals("fragility"));
		assertTrue(biologicalEntityA.getChildren().get(0).getAttributeValue("value").equals("stronger"));
		
		assertTrue(biologicalEntityB.getAttributeValue("name").equals("ii"));
		assertTrue(biologicalEntityB.getAttributeValue("constraint").equals("legs"));
		assertTrue(biologicalEntityB.getChildren().get(0).getAttributeValue("name").equals("fragility"));
		assertTrue(biologicalEntityB.getChildren().get(0).getAttributeValue("value").equals("stronger"));

		assertTrue(biologicalEntityC.getAttributeValue("name").equals("iii"));
		assertTrue(biologicalEntityC.getAttributeValue("constraint").equals("legs"));
		assertTrue(biologicalEntityC.getChildren().get(0).getAttributeValue("name").equals("fragility"));
		assertTrue(biologicalEntityC.getChildren().get(0).getAttributeValue("value").equals("stronger"));
		
		Content contentA1 = relationAParent.getContent(relationAParentIndex + 2);
		Content contentA2 = relationAParent.getContent(relationAParentIndex + 3);
		Content contentA3 = relationAParent.getContent(relationAParentIndex + 4);
		assertTrue(contentA1 instanceof Element);
		assertTrue(contentA2 instanceof Element);
		assertTrue(contentA3 instanceof Element);
		Element relationA1 = (Element)contentA1;
		Element relationA2 = (Element)contentA2;
		Element relationA3 = (Element)contentA3;
		assertTrue(relationA1.getAttributeValue("from").equals("o0_0"));
		assertTrue(relationA2.getAttributeValue("from").equals("o0_1"));
		assertTrue(relationA3.getAttributeValue("from").equals("o0_2"));
		
		Content contentB1 = relationBParent.getContent(relationBParentIndex + 4);
		Content contentB2 = relationBParent.getContent(relationBParentIndex + 5);
		Content contentB3 = relationBParent.getContent(relationBParentIndex + 6);
		assertTrue(contentB1 instanceof Element);
		assertTrue(contentB2 instanceof Element);
		assertTrue(contentB3 instanceof Element);
		Element relationB1 = (Element)contentB1;
		Element relationB2 = (Element)contentB2;
		Element relationB3 = (Element)contentB3;
		assertTrue(relationB1.getAttributeValue("to").equals("o0_0"));
		assertTrue(relationB2.getAttributeValue("to").equals("o0_1"));
		assertTrue(relationB3.getAttributeValue("to").equals("o0_2"));
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
		biologicalEntity1.setAttribute("constraint", "legs");
		biologicalEntity1.setAttribute("name", "i-iii");
		biologicalEntity1.setAttribute("name_original", "i-iii");
		biologicalEntity1.setAttribute("type", "structure");
		biologicalEntity2.setAttribute("id", "o1");
		biologicalEntity2.setAttribute("id", "o1");
		
		character1 = new Element("character");
		character1.setAttribute("name", "fragility");
		character1.setAttribute("value", "stronger");
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
