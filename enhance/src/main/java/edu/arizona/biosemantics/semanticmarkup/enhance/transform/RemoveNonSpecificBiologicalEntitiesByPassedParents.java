package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.jdom2.Document;
import org.jdom2.Element;

import com.google.gwt.thirdparty.guava.common.collect.Lists;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;

public class RemoveNonSpecificBiologicalEntitiesByPassedParents extends RemoveNonSpecificBiologicalEntities {
	
	private CollapseBiologicalEntityToName collapseBiologicalEntityToName;
	private IInflector inflector;

	public RemoveNonSpecificBiologicalEntitiesByPassedParents(
			KnowsPartOf knowsPartOf, ITokenizer tokenizer,
			CollapseBiologicalEntityToName collapseBiologicalEntityToName, 
			IInflector inflector) {
		super(knowsPartOf, tokenizer);
		this.inflector = inflector;
		this.collapseBiologicalEntityToName = collapseBiologicalEntityToName;
	}

	@Override
	public void transform(Document document) {
		List<Element> passedStatements = new LinkedList<Element>();
		for(Element statement : this.statementXpath.evaluate(document)) {
			passedStatements.add(0, statement);
			for(Element biologicalEntity : statement.getChildren("biological_entity")) {
				String name = biologicalEntity.getAttributeValue("name");
				name = name == null ? "" : name.trim();
				String constraint = biologicalEntity.getAttributeValue("constraint");
				constraint = constraint == null ? "" : constraint.trim();
				
				if(isNonSpecificPart(name)) {
					if(!isPartOfAConstraint(name, constraint)) {
						if(name.equals("apex") && statement.getChildText("text").startsWith("the mandibles ferruginous, longitudinally striated, and having two blunt teeth"))
							System.out.println("base found " + statement.getChildText("text"));
						String parent = findParentInPassedStatements(collapseBiologicalEntityToName.collapse(biologicalEntity), document, passedStatements);
						if(parent == null)
							parent = findParentInPassedStatements(name, document, passedStatements);
						if(parent != null) {
							constraint = (parent + " " + constraint).trim();
							biologicalEntity.setAttribute("constraint", constraint);
						}
					}
				}
			}
		}
	}

	private String findParentInPassedStatements(String name, Document document, List<Element> passedStatements) {
		for(Element passedStatement : passedStatements) {
			String sentence = passedStatement.getChild("text").getValue().toLowerCase();
			sentence = removeParenthesis(sentence, '(', ')');
			List<Token> tokens = tokenizer.tokenize(sentence);
			//tokens = Lists.reverse(tokens);
			for(Token term : tokens) {
				Element termsBiologicalEntity = getBiologicalEntity(term.getContent(), passedStatements);
				
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
		}
		return null;
	}
	
	private String removeParenthesis(String text, char parenthesisOpenCharacter, char parenthesisCloseCharacter) {
        int open = 0;
        int closed = 0;
        boolean changed = true;
        int startIndex = 0, openIndex = -1, closeIndex = -1;

        while (changed) {
            changed = false;
            for (int a = startIndex; a < text.length(); a++) {
                if (text.charAt(a) == parenthesisOpenCharacter) {
                    open++;
                    if (open == 1) {
                        openIndex = a;
                    }
                } else if (text.charAt(a) == parenthesisCloseCharacter) {
                    closed++;
                    if (open == closed) {
                        closeIndex = a;
                        text = text.substring(0, openIndex)
                                + text.substring(closeIndex + 1);
                        changed = true;
                        break;
                    }
                } else {
                    if (open == 0)
                        startIndex++;
                    continue;
                }
            }
        }
        return text;
	}

	private Element getBiologicalEntity(String term, List<Element> passedStatements) {
		for(Element passedStatement : passedStatements) {
			for(Element biologicalEntity : passedStatement.getChildren("biological_entity")) {
				String name = biologicalEntity.getAttributeValue("name_original");
				name = name == null ? "" : name.trim();
				if(name.equals(term)) {
					return biologicalEntity;
				}
			}
		}
		return null;
	}
}
