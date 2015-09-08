/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib;

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
import edu.arizona.biosemantics.semanticmarkup.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
/**
 * @author Hong Cui
 * 1. replace synonyms to preferred terms
 */
public class TerminologyStandardizer {
	
	protected XPathFactory xpathFactory = XPathFactory.instance();
	protected XPathExpression<Element> sourceXpath = 
			xpathFactory.compile("/bio:treatment/meta/source", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> taxonIdentificationXpath = 
			xpathFactory.compile("/bio:treatment/taxon_identification[@status='ACCEPTED']/taxon_name", Filters.element(), null,
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> statementXpath = 
			xpathFactory.compile("//description[@type='morphology']/statement", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> biologicalEntityPath = 
			xpathFactory.compile("//description[@type='morphology']/statement/biological_entity", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> relationPath = 
			xpathFactory.compile("//description[@type='morphology']/statement/relation", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> characterPath = 
			xpathFactory.compile("//description[@type='morphology']/statement/biological_entity/character", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	
	private ICharacterKnowledgeBase characterKnowledgeBase;
	private String or = "_or_";
	
	@Inject
	public TerminologyStandardizer(ICharacterKnowledgeBase characterKnowledgeBase){
		this.characterKnowledgeBase = characterKnowledgeBase;
	}
	
	public void standardize(String directory) {
		SAXBuilder saxBuilder = new SAXBuilder();
		File dir = new File(directory);
		for(File file : dir.listFiles()) {
			if(file.isFile()) {
				try {
					Document document = null;
					try(InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF8")) {
						document = saxBuilder.build(inputStreamReader);
					}
					
					if(document != null) 
						standardize(document);
					
					File outputFile = new File(directory, file.getName());
					try(OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF8")) {
						XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
						xmlOutput.output(document, outputStreamWriter);
					}
				} catch (JDOMException | IOException e) {
					log(LogLevel.ERROR, "Can't read xml from file " + file.getAbsolutePath(), e);
				}
			}
		}
	}

	private void standardize(Document document) {
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
