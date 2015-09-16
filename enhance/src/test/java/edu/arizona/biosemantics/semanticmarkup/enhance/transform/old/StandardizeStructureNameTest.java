package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.Arrays;
import java.util.HashSet;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

import edu.arizona.biosemantics.common.ling.know.CharacterMatch;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;

public class StandardizeStructureNameTest {
	
	private StandardizeStructureName transformer;
	private Element biologicalEntity1;
	private Element relation1;
	private Element relation2;
	private Element biologicalEntity2;
	private Element biologicalEntity3;
	
	public StandardizeStructureNameTest() {
		this.transformer = new StandardizeStructureName(new ICharacterKnowledgeBase() {
			@Override
			public CharacterMatch getCharacterName(String characterState) {
				return null;
			}
			@Override
			public boolean containsCharacterState(String characterState) {
				return false;
			}
			@Override
			public void addCharacterStateToName(String characterState,
					CharacterMatch match) {
			}
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
		}, new HashSet<String>(Arrays.asList(new String[] { })));
	}
	
	@Test
	public void test() {
		Document document = createTestDocument();
		transformer.transform(document);
	}

	private Document createTestDocument() {
		Element treatment = new Element("treatment", Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		Element description = new Element("description");
		treatment.addContent(description);
		Element statement = new Element("statement");
		description.addContent(statement);
		Element text = new Element("text");
		statement.addContent(text);
		biologicalEntity1 = new Element("biological_entity");
		biologicalEntity2 = new Element("biological_entity");
		biologicalEntity3 = new Element("biological_entity");
		relation1 = new Element("relation");
		relation2 = new Element("relation");
		statement.addContent(biologicalEntity1);
		statement.addContent(biologicalEntity2);
		statement.addContent(biologicalEntity3);
		statement.addContent(relation1);
		statement.addContent(relation2);
		
		description.setAttribute("type", "morphology");
		text.setText("This is a test sentence");
		biologicalEntity1.setAttribute("id", "o1");
		biologicalEntity1.setAttribute("name", "leaf");
		biologicalEntity1.setAttribute("constraint", "leaf-constraint");
		
		biologicalEntity2.setAttribute("id", "o2");
		biologicalEntity2.setAttribute("name", "apex");
		biologicalEntity2.setAttribute("constraint", "apex-constraint");
		
		biologicalEntity3.setAttribute("id", "o3");
		biologicalEntity3.setAttribute("name", "center");
		biologicalEntity3.setAttribute("constraint", "center-constraint");
		
		relation1.setAttribute("name", "part_of");
		relation1.setAttribute("from", "o1");
		relation1.setAttribute("to", "o2");
		relation2.setAttribute("name", "part_of");
		relation2.setAttribute("from", "o2");
		relation2.setAttribute("to", "o3");
		
		
		Document document = new Document(treatment);
		return document;
	}
}
