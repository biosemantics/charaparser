package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jdom2.Document;
import org.jdom2.Element;

import com.google.gwt.thirdparty.guava.common.collect.Lists;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

public class RemoveNonSpecificBiologicalEntitiesByPassedParents extends RemoveNonSpecificBiologicalEntities {
	
	private CollapseBiologicalEntityToName collapseBiologicalEntityToName;
	private IInflector inflector;

	public RemoveNonSpecificBiologicalEntitiesByPassedParents(
			KnowsPartOf knowsPartOf, KnowsSynonyms knowsSynonyms, ITokenizer tokenizer,
			CollapseBiologicalEntityToName collapseBiologicalEntityToName, 
			IInflector inflector) {
		super(knowsPartOf, knowsSynonyms, tokenizer);
		this.inflector = inflector;
		this.collapseBiologicalEntityToName = collapseBiologicalEntityToName;
	}

	@Override
	public void transform(Document document) {
		System.out.println("--------------------------------------------");
		
		List<Element> passedStatements = new LinkedList<Element>();
		for(Element statement : this.statementXpath.evaluate(document)) {
			String sentence = statement.getChildText("text");
			
			System.out.println(sentence);
			if(sentence.startsWith("the flagellum, mandibles, anterior margin of the")) {
				System.out.println();
			}
			
			passedStatements.add(0, statement);
			for(Element biologicalEntity : statement.getChildren("biological_entity")) { //Lists.reverse(statement.getChildren("biological_entity"))) {
				String name = biologicalEntity.getAttributeValue("name");
				name = name == null ? "" : name.trim();
				String constraint = biologicalEntity.getAttributeValue("constraint");
				constraint = constraint == null ? "" : constraint.trim();
				
				if(name.equals("margin") && sentence.startsWith("the flagellum, mandibles, anterior margin of the face, and the head".toLowerCase())) {
					System.out.println();
				}
				
				if(isNonSpecificPart(name)) {
					if(!isPartOfAConstraint(name, constraint)) {
						
						
						
						List<Element> searchStatements = new LinkedList<Element>(); //this is only in place because sometimes entities are placed in the next statement instead of the one in which it actually appears in text
						for(Element passedStatement : passedStatements) {
							searchStatements.add(0, passedStatement);
							String parent = findParentInPassedStatements(collapseBiologicalEntityToName.collapse(biologicalEntity), document, searchStatements);
							if(parent == null)
								parent = findParentInPassedStatements(name, document, searchStatements);
							if(parent != null) {
								constraint = (parent + " " + constraint).trim();
								this.appendInferredConstraint(biologicalEntity, parent);
								biologicalEntity.setAttribute("constraint", constraint);
								break;
							}
						}
					}
				}
			}
		}
	}

	private String findParentInPassedStatements(String name, Document document, List<Element> searchStatements) {
		Element searchStatement = searchStatements.get(0);
		String sentence = searchStatement.getChild("text").getValue().toLowerCase();
		sentence = parenthesisRemover.remove(sentence, '(', ')');
		List<Token> tokens = tokenizer.tokenize(sentence);
		//tokens = Lists.reverse(tokens);
		for(Token term : tokens) {
			Element termsBiologicalEntity = getBiologicalEntity(term.getContent(), searchStatements);
			
			if(termsBiologicalEntity != null) {
				String termNormalized = collapseBiologicalEntityToName.collapse(termsBiologicalEntity);
				if(knowsPartOf.isPartOf(name, termNormalized)) {
					return collapseBiologicalEntityToName.collapse(termsBiologicalEntity);
				} else {
					if(knowsPartOf.isPartOf(name, termsBiologicalEntity.getAttributeValue("name"))) 
						return collapseBiologicalEntityToName.collapse(termsBiologicalEntity);
				}
			} else {
				if(knowsPartOf.isPartOf(name, term.getContent())) 
					return inflector.getSingular(term.getContent());
			}
		}
	return null;
	}
	

}
