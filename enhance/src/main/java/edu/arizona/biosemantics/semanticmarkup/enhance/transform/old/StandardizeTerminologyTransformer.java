package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.inject.Inject;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;
/**
 * @author Hong Cui
 * 1. replace synonyms to preferred terms
 */
public class StandardizeTerminologyTransformer extends AbstractTransformer {

	private ICharacterKnowledgeBase characterKnowledgeBase;
	private String or = "_or_";
	
	public StandardizeTerminologyTransformer(ICharacterKnowledgeBase characterKnowledgeBase){
		this.characterKnowledgeBase = characterKnowledgeBase;
	}
	
	@Override
	public void transform(Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			String type = biologicalEntity.getAttributeValue("type");
			String name = biologicalEntity.getAttributeValue("name");
			String constraint = biologicalEntity.getAttributeValue("constraint");
			String preferedName = characterKnowledgeBase.getCharacterName(name).getLabel(type);
			if(preferedName!=null) biologicalEntity.setAttribute("name", preferedName);
			
			//standardize structural constraint, a word or a phrase
			//String constraint = struct.getConstraint(); //try to match longest segment anchored to the last word in the phrase.
			if(constraint!=null){
				constraint = constraint.trim();
				String leading = "";
				do{
					String prefered = characterKnowledgeBase.getCharacterName(constraint).getLabel(type);
					if(prefered!=null){
						biologicalEntity.setAttribute("constraint", (leading+" "+prefered).trim());
						break;
					}else{
						//remove the leading word
						leading = constraint.replaceFirst(" .*", "").trim();
						constraint = constraint.replaceFirst(leading, "").trim(); 
					}
				} while(!constraint.isEmpty());
			}
			
			
			//standardize character
			for(Element character : new ArrayList<Element>(biologicalEntity.getChildren("character"))) {
				preferedName = null;
				String value = character.getValue();
				if(value!=null && !value.trim().contains(" ") && !character.getAttributeValue("name").contains(or)){
					preferedName = characterKnowledgeBase.getCharacterName(value.trim()).getLabel(character.getAttributeValue("name"));
				}
				if(preferedName !=null) character.setAttribute(value, preferedName);
			}
		}
	}

}
