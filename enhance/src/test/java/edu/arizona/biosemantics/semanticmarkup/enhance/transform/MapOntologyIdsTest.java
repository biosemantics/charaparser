package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

import edu.arizona.biosemantics.common.ontology.search.Searcher;
import edu.arizona.biosemantics.common.ontology.search.model.Ontology;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry.Type;

public class MapOntologyIdsTest {

	private MapOntologyIds transformer;
	private Element biologicalEntity1;
	private Element biologicalEntity2;

	public MapOntologyIdsTest() {
		List<Searcher> searchers = new LinkedList<Searcher>();
		//for(Ontology ontology : TaxonGroupOntology.getOntologies(TaxonGroup.PLANT)) 
		//	searchers.add(new FileSearcher(ontology, Configuration.ontologyDirectory, Configuration.wordNetDirectory));
		
		searchers.add(new Searcher() {
			@Override
			public List<OntologyEntry> getEntries(String term) {
				List<OntologyEntry> result = new LinkedList<OntologyEntry>();
				if(term.equals("test")) 
					result.add(new OntologyEntry(Ontology.PO, "dummyiri", Type.ENTITY, 1.0));
				return result;
			}
			@Override
			public List<OntologyEntry> getEntries(String term, Type type) {
				List<OntologyEntry> result = new LinkedList<OntologyEntry>();
				if(term == null || type == null)
					return result;
				if(term.equals("test")) {
					switch(type) {
					case ENTITY:
						result.add(new OntologyEntry(Ontology.PO, "dummyiri", Type.ENTITY, 1.0));
						break;
					default:
						break;
					
					}
				}
				return result;
			}
		});
		
		this.transformer = new MapOntologyIds(searchers);
	}
	
	@Test
	public void testOntologyIdMapping() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertEquals("dummyiri", biologicalEntity1.getAttributeValue("ontologyid")); 
		assertEquals("", biologicalEntity2.getAttributeValue("ontologyid"));
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
		biologicalEntity1 = new Element("biological_entity");
		biologicalEntity2 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity1);
		statement.addContent(biologicalEntity2);
		Element character = new Element("character");
		biologicalEntity1.addContent(character);
		biologicalEntity1.setAttribute("name", "test");
		biologicalEntity2.setAttribute("name", "test2");
		
		Document document = new Document(treatment);
		return document;
	}

}
