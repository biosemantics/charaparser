package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class TaxonNameToWholeOrganism {
	
	private TaxonNameToWholeOrganismTransformer transformer;
	private Element biologicalEntity1;
	private Element biologicalEntity2;
	
	public TaxonNameToWholeOrganism() {
		this.transformer = new TaxonNameToWholeOrganismTransformer();
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertEquals("whole_organism", biologicalEntity1.getAttributeValue("name"));
		assertEquals("", biologicalEntity1.getAttributeValue("name_original"));
		assertEquals("structure", biologicalEntity1.getAttributeValue("type"));
		
		assertEquals("name", biologicalEntity2.getAttributeValue("name"));
		assertEquals("", biologicalEntity2.getAttributeValue("name_original"));
		assertEquals("taxon_name", biologicalEntity2.getAttributeValue("type"));
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
		biologicalEntity1.setAttribute("constraint", "");
		biologicalEntity1.setAttribute("type", "taxon_name");

		biologicalEntity2.setAttribute("constraint", "");
		biologicalEntity2.setAttribute("type", "taxon_name");
		biologicalEntity2.setAttribute("name", "name");
		biologicalEntity2.setAttribute("name_original", "");
				
		Document document = new Document(treatment);
		return document;
	}
}
