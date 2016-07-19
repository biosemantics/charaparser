package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;
import edu.stanford.nlp.util.StringUtils;

/**
 * Order multiple constraints of a structure in the natural order they occur in the text
 * 
 * Assumptions made: 
 * - term in constraint does not appear multiple times in the source sentence. If it does the correct sorting order is unclear.
 * - No punctuation marks have been introduced to connect terms of a constraint, 
 *   e.g. 4 locular from source sentence stays this way and is not put as 4-locular in constraint
 */
public class OrderBiologicalEntityConstraint extends AbstractTransformer {
	
	@Override
	public void transform(Document document) {
		for(Element statement : this.statementXpath.evaluate(document)) {
			String sentence = statement.getChildText("text").toLowerCase();
			for(Element structure : statement.getChildren("biological_entity")) {
				String constraints = structure.getAttributeValue("constraint");
				String originalName = structure.getAttributeValue("name_original");
				if(constraints != null && (constraints.contains(";") || constraints.contains(" "))) {
					constraints = order(constraints, sentence, originalName);;
					structure.setAttribute("constraint", constraints);
				}
			}
		}
	}
	
	protected String order(String constraints, String sentence, String nameOriginal) {
		StringBuilder result = new StringBuilder();
		int nameIndex = sentence.indexOf(nameOriginal);
		if(nameIndex != -1) 
			//constraint is per definition only before the entity name; don't let ordering be confused by duplicate term appearances
			sentence = sentence.substring(0, sentence.indexOf(nameOriginal));
		
		final ArrayList<String> sentenceParts = new ArrayList<String>(Arrays.asList(sentence.split("\\s+")));
		ArrayList<String> constraintParts = new ArrayList<String>(Arrays.asList(constraints.split("\\s*?[; ]\\s*")));
		
		Collections.sort(constraintParts, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				 return Integer.compare(sentenceParts.indexOf(o1), sentenceParts.indexOf(o2));
			}
		});
		
		for(String part : constraintParts) {
			if(!part.trim().isEmpty())
				result.append(part + " ");
		}
		return result.toString().trim();
	}	
}
