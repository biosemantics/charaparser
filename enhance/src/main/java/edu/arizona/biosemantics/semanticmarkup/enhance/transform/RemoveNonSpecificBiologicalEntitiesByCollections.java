package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsClassHierarchy;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

public class RemoveNonSpecificBiologicalEntitiesByCollections extends RemoveNonSpecificBiologicalEntities {

	private KnowsClassHierarchy knowsClassHiearchy;
	private CollapseBiologicalEntityToName collapseBiologicalEntityToName;
	private IInflector inflector;

	public RemoveNonSpecificBiologicalEntitiesByCollections(KnowsPartOf knowsPartOf, KnowsSynonyms knowsSynonyms, KnowsClassHierarchy knowsClassHiearchy, 
			ITokenizer tokenizer, CollapseBiologicalEntityToName collapseBiologicalEntityToName, IInflector inflector) {
		super(knowsPartOf, knowsSynonyms, tokenizer);
		this.knowsClassHiearchy = knowsClassHiearchy;
		this.collapseBiologicalEntityToName = collapseBiologicalEntityToName;
		this.inflector = inflector;
	}

	@Override
	public void transform(Document document) {
		List<Element> passedStatements = new LinkedList<Element>();
		for(Element statement : this.statementXpath.evaluate(document)) {
			passedStatements.add(0, statement);
			for(Element biologicalEntity : statement.getChildren("biological_entity")) { //Lists.reverse(statement.getChildren("biological_entity"))) {
				String name = biologicalEntity.getAttributeValue("name");
				name = name == null ? "" : name.trim();
				String constraint = biologicalEntity.getAttributeValue("constraint");
				constraint = constraint == null ? "" : constraint.trim();
				
				if(name.equals("array")) {
					System.out.println("test");
				}
				if(isNonSpecificPart(name)) {
					if(isCollection(name)) {
						if(!hasPartInConstraint(name, constraint)) {
							List<Element> searchStatements = new LinkedList<Element>(); //this is only in place because sometimes entities are placed in the next statement instead of the one in which it actually appears in text
							for(Element passedStatement : passedStatements) {
								searchStatements.add(0, passedStatement);
								String child = findChildInPassedStatements(collapseBiologicalEntityToName.collapse(biologicalEntity), document, searchStatements);
								if(child == null)
									child = findChildInPassedStatements(name, document, searchStatements);
								if(child != null) {
									constraint = (child + " " + constraint).trim();
									this.appendInferredConstraint(biologicalEntity, child);
									biologicalEntity.setAttribute("constraint", constraint);
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	private String findChildInPassedStatements(String name, Document document, List<Element> searchStatements) {
		Element searchStatement = searchStatements.get(0);
		String sentence = searchStatement.getChild("text").getValue().toLowerCase();
		sentence = parenthesisRemover.remove(sentence, '(', ')');
		List<Token> tokens = tokenizer.tokenize(sentence);
		//tokens = Lists.reverse(tokens);
		for(Token term : tokens) {
			Element termsBiologicalEntity = getBiologicalEntity(term.getContent(), searchStatements);
			
			if(termsBiologicalEntity != null) {
				String termNormalized = collapseBiologicalEntityToName.collapse(termsBiologicalEntity);
				if(knowsPartOf.isPartOf(termNormalized, name)) {
					return collapseBiologicalEntityToName.collapse(termsBiologicalEntity);
				} else {
					if(knowsPartOf.isPartOf(termsBiologicalEntity.getAttributeValue("name"), name)) 
						return collapseBiologicalEntityToName.collapse(termsBiologicalEntity);
				}
			} else {
				if(knowsPartOf.isPartOf(term.getContent(), name)) 
					return inflector.getSingular(term.getContent());
			}
		}
		return null;
	}

	private boolean hasPartInConstraint(String name, String constraint) {
		for(Token term : tokenizer.tokenize(constraint)) {
			if(knowsPartOf.isPartOf(term.getContent(), name)) {
				return true;
			}
		}
		return false;
	}

	private boolean isCollection(String name) {
		return knowsClassHiearchy.isSubclass(name, "collection");
	}

}
