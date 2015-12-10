package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms.SynonymSet;

public class SimpleRemoveSynonyms extends AbstractTransformer {

	private KnowsSynonyms knowsSynonyms;

	public SimpleRemoveSynonyms(KnowsSynonyms knowsSynonyms) {
		this.knowsSynonyms = knowsSynonyms;
	}
	
	@Override
	public void transform(Document document) {
		removeBiologicalEntitySynonyms(document);
	}

	private void removeBiologicalEntitySynonyms(Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			String name = biologicalEntity.getAttributeValue("name");
			if(name != null) {
				String newName = createSynonymReplacedValue(name);
				biologicalEntity.setAttribute("name", newName);
			}
		}
	}

	private String createSynonymReplacedValue(String name) {
		Set<SynonymSet> synonymSets = knowsSynonyms.getSynonyms(name);
		if(synonymSets.size() > 1) {
			return name;
		} else {
			return synonymSets.iterator().next().getPreferredTerm();
		}
	}

}
