package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Parent;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import edu.arizona.biosemantics.common.log.LogLevel;

public abstract class AbstractTransformer {
	
	protected XPathFactory xpathFactory = XPathFactory.instance();
	protected XPathExpression<Element> sourceXpath = 
			xpathFactory.compile("/bio:treatment/meta/source", Filters.element(), null, 
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> taxonIdentificationXpath = 
			xpathFactory.compile("/bio:treatment/taxon_identification[@status='ACCEPTED']/taxon_name", Filters.element(), null,
					Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
	protected XPathExpression<Element> descriptionXpath = 
			xpathFactory.compile("//description[@type='morphology']", Filters.element(), null, 
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
	public abstract void transform(Document document);
	
	
	protected List<Element> getRelationsInvolve(Element biologicalEntity, Document document) {
		List<Element> result = new LinkedList<Element>();
		result.addAll(this.getFromRelations(biologicalEntity, document));
		result.addAll(this.getToRelations(biologicalEntity, document));
		return result;
	}
	
	protected List<Element> getFromRelations(Element biologicalEntity, Document document) {
		List<Element> result = new LinkedList<Element>();
		for(Element relation : new ArrayList<Element>(this.relationPath.evaluate(document))) {
			if(relation.getAttributeValue("from").equals(biologicalEntity.getAttributeValue("id")))
				result.add(relation);
		}
		return result;
	}
	
	protected List<Element> getToRelations(Element biologicalEntity, Document document) {
		List<Element> result = new LinkedList<Element>();
		for(Element relation : new ArrayList<Element>(this.relationPath.evaluate(document))) {
			if(relation.getAttributeValue("to").equals(biologicalEntity.getAttributeValue("id")))
				result.add(relation);
		}
		return result;
	}
	
	protected Element getBiologicalEntityWithId(Document document, String id) {
		for(Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			if(biologicalEntity.getAttributeValue("id").equals(id)) {
				return biologicalEntity;
			}
		}
		return null;
	}
	
	protected void appendInferredConstraint(Element biologicalEntity, String append) {
		String constraint = biologicalEntity.getAttributeValue("inferred_constraint");
		if(constraint == null)
			constraint = "";
		if(!constraint.isEmpty())
			constraint += "; " + append;
		else
			constraint += append;
		biologicalEntity.setAttribute("inferred_constraint", constraint);
	}
	
	protected void appendConstraint(Element biologicalEntity, String append) {
		String constraint = biologicalEntity.getAttributeValue("constraint");
		if(constraint == null)
			constraint = "";
		if(!constraint.isEmpty())
			constraint += "; " + append;
		else
			constraint += append;
		biologicalEntity.setAttribute("constraint", constraint);
	}
	
	protected void updateRelations(Document document, Element biologicalEntity, Map<String, Element> newBiologicalEntities) {
	    //if the original element involved in any relations, individualize the relations
		//to
		String[] attributes = { "to", "from" };
		List[] relations = { this.getToRelations(biologicalEntity, document), this.getFromRelations(biologicalEntity, document) };
		
		for(int i=0; i<attributes.length; i++) {
			String attribute = attributes[i];
			List<Element> relationsList = relations[i];
			for(Element relation : relationsList) {
				Parent parent = relation.getParent();
				int relationPosition = parent.indexOf(relation);
				relation.detach();
				int rid = 0;
				for(String newId : newBiologicalEntities.keySet()){
					Element clone = relation.clone();
					clone.setAttribute("id", clone.getAttributeValue("id") + "_" + (rid));
					clone.setAttribute(attribute, newId);
					parent.addContent(relationPosition + rid, clone);
					rid++;
				}
			}
		}
	}	
	
	/**
	 * 
	 * @param roman <= XXXVIII (38)
	 * @return
	 */
	protected String nextRoman(String roman){
		if(roman.endsWith("iv")){
			return roman.replaceFirst("iv$", "v");
		}else if(roman.endsWith("ix")){
			return roman.replaceFirst("ix$", "x");
		}else if(roman.endsWith("viii")){
			return roman.replaceFirst("viii$", "ix");
		}else if(roman.endsWith("iii")){
			return roman.replaceFirst("iii$", "iv");
		}else 
			return roman+"i";
	}
}
