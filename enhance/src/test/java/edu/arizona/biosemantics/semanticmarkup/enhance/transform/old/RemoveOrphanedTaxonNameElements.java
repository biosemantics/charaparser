package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class RemoveOrphanedTaxonNameElements {

	private RemoveOrphanedUnknownElementsTransformer transformer;
	private Element biologicalEntity1;
	private Element biologicalEntity2;
	private Element biologicalEntity3;
	
	public RemoveOrphanedTaxonNameElements() {
		this.transformer = new RemoveOrphanedUnknownElementsTransformer();
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertTrue(biologicalEntity1.getParent() == null);
		assertTrue(biologicalEntity2.getParent() != null);
		assertTrue(biologicalEntity3.getParent() != null);
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
		biologicalEntity3 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity1);
		statement.addContent(biologicalEntity2);
		statement.addContent(biologicalEntity3);

		text.setText("leafs some brown and more");
		biologicalEntity1.setAttribute("name", "whole_organism");

		biologicalEntity2.setAttribute("name", "whole_organism");
		Element character = new Element("character");
		biologicalEntity2.addContent(character);
		
		biologicalEntity3.setAttribute("name", "whole_organism");
		biologicalEntity3.setAttribute("id", "o3");
		Element relation = new Element("relation");
		relation.setAttribute("from", "o3");
		relation.setAttribute("to", "o4");
		statement.addContent(relation);
		
				
		Document document = new Document(treatment);
		return document;
	}
}
