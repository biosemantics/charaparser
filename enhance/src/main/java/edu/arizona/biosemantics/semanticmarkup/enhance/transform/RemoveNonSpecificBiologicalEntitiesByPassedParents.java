package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

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
		List<Element> passedStatements = new LinkedList<Element>();
		for(Element statement : this.statementXpath.evaluate(document)) {
			String sentence = statement.getChildText("text");

			passedStatements.add(0, statement);
			for(Element biologicalEntity : statement.getChildren("biological_entity")) { //Lists.reverse(statement.getChildren("biological_entity"))) {
				String name = biologicalEntity.getAttributeValue("name");
				name = name == null ? "" : name.trim();
				String constraint = biologicalEntity.getAttributeValue("constraint");
				constraint = constraint == null ? "" : constraint.trim();
				if(isNonSpecificPart(name)) {
					if(!isPartOfAConstraint(name, constraint)) {
						List<Element> searchStatements = new LinkedList<Element>(); //this is only in place because sometimes entities are placed in the next statement instead of the one in which it actually appears in text
						for(Element passedStatement : passedStatements) {
							searchStatements.add(0, passedStatement);
							NameAndSrc info = findParentInPassedStatements(collapseBiologicalEntityToName.collapse(biologicalEntity), document, searchStatements);
							if(info != null) {
								String parent = info.getName();
								if(parent == null){
									info = findParentInPassedStatements(name, document, searchStatements);
									if(info != null)
										parent = info.getName();
								}
								if(parent != null) {
									constraint = (parent + " " + constraint).trim();
									this.appendInferredConstraint(biologicalEntity, parent);
									biologicalEntity.setAttribute("constraint", constraint);
									biologicalEntity = mergeSrc(info.getSrc(), biologicalEntity);
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	private NameAndSrc findParentInPassedStatements(String name, Document document, List<Element> searchStatements) {
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
					return new NameAndSrc(collapseBiologicalEntityToName.collapse(termsBiologicalEntity), searchStatement.getAttributeValue("id"));
				} else {
					if(knowsPartOf.isPartOf(name, termsBiologicalEntity.getAttributeValue("name")))
						return new NameAndSrc(collapseBiologicalEntityToName.collapse(termsBiologicalEntity), searchStatement.getAttributeValue("id"));
				}
			} else {
				if(knowsPartOf.isPartOf(name, term.getContent()))
					return new NameAndSrc(inflector.getSingular(term.getContent()), searchStatement.getAttributeValue("id"));
			}
		}
		return null;
	}


}
