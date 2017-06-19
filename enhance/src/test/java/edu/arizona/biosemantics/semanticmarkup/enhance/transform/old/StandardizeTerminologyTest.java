/*package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import static org.junit.Assert.assertEquals;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

import edu.arizona.biosemantics.common.ling.know.CharacterMatch;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;

public class StandardizeTerminologyTest {
	
	private StandardizeTerminology transformer;
	private Element biologicalEntity;
	private Element character;
	
	public StandardizeTerminologyTest() {
		this.transformer = new StandardizeTerminology(new ICharacterKnowledgeBase() {
			@Override
			public CharacterMatch getCharacterName(String characterState) {
				if(characterState.equals("name"))
					return  new CharacterMatch(null) {
						public String getLabel(String category){
							return "category";
						}
					};
				else if(characterState.equals("constraint")) {
					return  new CharacterMatch(null) {
						public String getLabel(String category){
							return "constraint2";
						}
					}; 
				} else if(characterState.equals("leading constraint")) { 
					return  new CharacterMatch(null) {
						public String getLabel(String category){
							return null;
						}
					};
				} else {
					return  new CharacterMatch(null) {
						public String getLabel(String category){
							return "red";
						}
					}; 
				}
			}
			@Override
			public boolean containsCharacterState(String characterState) {
				return false;
			}
			@Override
			public void addCharacterStateToName(String characterState,	CharacterMatch match) {		}
			@Override
			public boolean containsCharacterName(String characterName) {
				return false;
			}
			@Override
			public boolean isCategoricalState(String terminalsText) {
				return false;
			}
			@Override
			public boolean isEntity(String terminalsText) {
				return false;
			}
			@Override
			public String getEntityType(String singular, String organName) {
				return null;
			}
			@Override
			public boolean isEntityStructuralContraint(String constraint) {
				return false;
			}
		});
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
		
		assertEquals("category", biologicalEntity.getAttributeValue("name"));
		assertEquals("red", character.getAttributeValue("value"));
		assertEquals("leading constraint2", biologicalEntity.getAttributeValue("constraint"));
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
		biologicalEntity = new Element("biological_entity");
		statement.addContent(text);
		statement.addContent(biologicalEntity);
		biologicalEntity.setAttribute("name", "name");
		biologicalEntity.setAttribute("type", "type");
		biologicalEntity.setAttribute("constraint", "leading constraint");
		character = new Element("character");
		character.setAttribute("name", "color");
		character.setAttribute("value", "reddish");
		biologicalEntity.addContent(character);
		
		Document document = new Document(treatment);
		return document;
	}
}
*/