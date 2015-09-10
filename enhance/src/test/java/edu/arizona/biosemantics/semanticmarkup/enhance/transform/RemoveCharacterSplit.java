package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Test;

import edu.arizona.biosemantics.semanticmarkup.enhance.know.HasSynonyms;

public class RemoveCharacterSplit {

	private RemoveCharacterSplitTransformer removeCharacterSplitTransformer;
	private Element biologicalEntity1;
	private Element biologicalEntity2;
	private Element biologicalEntity3;
	private Element biologicalEntity4;
	private Element biologicalEntity6;
	private Element biologicalEntity5;
	private Element character1;
	private Element character2;
	private Element character3;
	
	public RemoveCharacterSplit() {
		List<HasSynonyms> hasSynonymsList = new LinkedList<HasSynonyms>();
		hasSynonymsList.add(new HasSynonyms() {
			@Override
			public Set<SynonymSet> getSynonyms(String term) {
				Set<SynonymSet> result = new HashSet<SynonymSet>();
				
				switch(term) {
					case "red":
					case "reddish":
						HashSet<String> synonyms = new HashSet<String>();
						synonyms.add("red");
						synonyms.add("reddish");
						result.add(new SynonymSet("red", synonyms));
						break;
					case "lateral":
					case "oblique":
						synonyms = new HashSet<String>();
						synonyms.add("oblique");
						synonyms.add("lateral");
						result.add(new SynonymSet("lateral", synonyms));
						break;
					case "apex":
					case "tip":
						synonyms = new HashSet<String>();
						synonyms.add("apex");
						synonyms.add("tip");
						result.add(new SynonymSet("apex", synonyms));
						break;
					}				
				return result;
			}
		});
		removeCharacterSplitTransformer = new RemoveCharacterSplitTransformer(hasSynonymsList);
	}
	
	@Test
	public void testRemoveSplit() {
		Document document = createTestDocument();
		removeCharacterSplitTransformer.transform(document);
		
		assertEquals("lateral apex", biologicalEntity1.getAttributeValue("name")); 
		assertEquals("lateral apex", biologicalEntity2.getAttributeValue("name")); 
		assertEquals("lateral apex", biologicalEntity3.getAttributeValue("name")); 
		assertEquals("apex lateral", biologicalEntity4.getAttributeValue("name")); 
		assertEquals("asdf apex", biologicalEntity5.getAttributeValue("name")); 
		assertEquals("some word", biologicalEntity6.getAttributeValue("name")); 
		
		assertEquals("red", character1.getAttributeValue("value")); 
		assertEquals("red", character2.getAttributeValue("value")); 
		assertEquals("green", character3.getAttributeValue("value")); 
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
		biologicalEntity3 = new Element("biological_entity");
		biologicalEntity4 = new Element("biological_entity");
		biologicalEntity5 = new Element("biological_entity");
		biologicalEntity6 = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity1);
		statement.addContent(biologicalEntity2);
		statement.addContent(biologicalEntity3);
		statement.addContent(biologicalEntity4);
		statement.addContent(biologicalEntity5);
		statement.addContent(biologicalEntity6);
		
		character1 = new Element("character");
		character1.setAttribute("value", "red");
		character2 = new Element("character");
		character2.setAttribute("value", "reddish");
		character3 = new Element("character");
		character3.setAttribute("value", "green");
		biologicalEntity1.addContent(character1);
		biologicalEntity1.addContent(character2);
		biologicalEntity1.addContent(character3);
		
		biologicalEntity1.setAttribute("name", "oblique tip");
		biologicalEntity2.setAttribute("name", "lateral apex");
		biologicalEntity3.setAttribute("name", "oblique apex");
		biologicalEntity4.setAttribute("name", "apex oblique");
		biologicalEntity5.setAttribute("name", "asdf apex");
		biologicalEntity6.setAttribute("name", "some word");
		
		Document document = new Document(treatment);
		return document;
	}
}
