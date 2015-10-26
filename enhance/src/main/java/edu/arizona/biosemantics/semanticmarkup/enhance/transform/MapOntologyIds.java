package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.ontology.search.Searcher;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry.Type;

public class MapOntologyIds extends AbstractTransformer {

	private Collection<Searcher> searchers;

	public MapOntologyIds(Collection<Searcher> searchers) {
		this.searchers = searchers;
	}
	
	@Override
	public void transform(Document document) {
		mapEntities(document);
		mapCharacters(document);
	}
	
	private void mapCharacters(Document document) {
		for (Element character : this.characterPath.evaluate(document)) {
			String value = character.getAttributeValue("value");
			String charType = character.getAttributeValue("char_type");
			if(value != null)
				value = value.trim();
			if(charType != null)
				charType = charType.trim();
			
			if(charType == null || !charType.equals("range_value")) {
				//if(value != null) {
					String searchTerm = value;
					Collection<String> iris = getIRIs(searchTerm);
					value = StringUtils.join(iris, "; ");
					if(!iris.isEmpty())
						log(LogLevel.INFO, "Found IRIs: " + value + " for term " + searchTerm);
					character.setAttribute("ontologyid", value);
				//}
			}
		}
	}

	private void mapEntities(Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			
			String name = biologicalEntity.getAttributeValue("name");
			if(name != null)
				name = name.trim();
			String constraint = biologicalEntity.getAttributeValue("constraint");
			if(constraint != null)
				constraint = constraint.trim();
			
			String searchTerm = name;
			if(constraint != null)
				searchTerm = constraint + " " + name;
			
			if(searchTerm != null) {
				Collection<String> iris = getIRIs(searchTerm);
				String value = StringUtils.join(iris, "; ");
				if(!iris.isEmpty())
					log(LogLevel.INFO, "Found IRIs: " + value + " for term " + searchTerm);
				biologicalEntity.setAttribute("ontologyid", value);
			}
		}
	}

	private Collection<String> getIRIs(String searchTerm) {
		Collection<String> result = new HashSet<String>();
		log(LogLevel.DEBUG, "Search " + searchTerm);
		for(Searcher searcher : searchers) {
			List<OntologyEntry> ontologyEntries = searcher.getEntries(searchTerm, Type.ENTITY);
			if(!ontologyEntries.isEmpty()) {
				log(LogLevel.DEBUG, "Highest scored ontology entity: " + ontologyEntries.get(0).getClassIRI() + " score: " + ontologyEntries.get(0).getScore());
				if(ontologyEntries.get(0).getScore() == 1.0) 
					result.add(ontologyEntries.get(0).getClassIRI());
			}
		}
		return result;
	}

}
