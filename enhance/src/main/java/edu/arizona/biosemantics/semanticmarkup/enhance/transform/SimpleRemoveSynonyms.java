package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms.SynonymSet;

public class SimpleRemoveSynonyms extends AbstractTransformer {

	private KnowsSynonyms knowsSynonyms;
	private String or = "_or_";

	public SimpleRemoveSynonyms(KnowsSynonyms knowsSynonyms) {
		this.knowsSynonyms = knowsSynonyms;
	}

	@Override
	public void transform(Document document) {
		removeBiologicalEntitySynonyms(document);
		removeCharacterSynonyms(document);
	}

	private void removeBiologicalEntitySynonyms(Document document) {
		/*for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			String name = biologicalEntity.getAttributeValue("name");
			if(name != null) {
				String newName = createSynonymReplacedValue(name);
				biologicalEntity.setAttribute("name", newName);
			}
		}*/
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			String type = biologicalEntity.getAttributeValue("type");
			String name = biologicalEntity.getAttributeValue("name");
			String constraint = biologicalEntity.getAttributeValue("constraint");
			String preferedName = createSynonymReplacedValue(name, type);
			if(preferedName != null)
				biologicalEntity.setAttribute("name", preferedName.replaceAll("_", "-"));

			//standardize structural constraint, a word or a phrase, e.g. "between eyes and nose"
			//String constraint = struct.getConstraint(); //try to match longest segment anchored to the last word in the phrase.
			String prefered = null;
			if(constraint != null){
				constraint = constraint.trim();
				if(constraint.startsWith("between")){
					String[] terms = constraint.replaceFirst("between", "").trim().split("\\s+(and|,)\\s+");
					prefered = "between ";
					for(int i = 0; i < terms.length; i++){
						String pref = createSynonymReplacedValue(terms[i], type);
						if(pref!=null)
							if(i == terms.length-2) prefered = prefered+" "+pref + " and ";
							else prefered = prefered+" "+pref + ", ";
						else
							if(i == terms.length-2) prefered = prefered+" "+terms[i] + " and ";
							else prefered = prefered+" "+terms[i] + ", ";
					}
					biologicalEntity.setAttribute("constraint", prefered.replaceFirst(", $", "").replaceAll("_", "-").replaceAll("\\s+", " "));
				}else{
					prefered = createSynonymReplacedValue(constraint, type);;
					if(prefered != null){
						biologicalEntity.setAttribute("constraint", prefered.replaceAll("_", "-"));
					}
				}
			}
			if(prefered != null && !prefered.equals(constraint))
				biologicalEntity.setAttribute("constraint_original", prefered.replaceAll("_", "-"));
		}
	}


	private void removeCharacterSynonyms(Document document) {
		for (Element character : this.characterPath.evaluate(document)) {
			String value = character.getAttributeValue("value");
			String category = character.getAttributeValue("name");
			if(value != null) {
				if(value.split(" ").length < 5) { //explosion of combinations to check
					String newValue = createSynonymReplacedValue(value, category);
					character.setAttribute("value", newValue.replaceAll("_", "-"));
					character.setAttribute("value_original", value);
				}
			}
		}
	}

	/*
	 * return all PreferredTerms as a string connected by this.or
	 */
	private String createSynonymReplacedValue(String name, String category) {
		//category may be a_or_b
		String[] cats = category.split(this.or);
		Set<String> preferredTerms = new HashSet<String>();
		for(String cat: cats){
			Set<SynonymSet> synonymSets = knowsSynonyms.getSynonyms(name, cat);
			//one name may have multiple preferred terms!! [stout has strong and increased size, two preferred terms]
			if(synonymSets.isEmpty()){
				preferredTerms.add(name);
			}else{
				Iterator<SynonymSet> it = synonymSets.iterator();
				while(it.hasNext()){
					preferredTerms.add(it.next().getPreferredTerm());
				}
			}
		}

		String result = "";
		Iterator<String> it = preferredTerms.iterator();
		while(it.hasNext())
			result = result + this.or + it.next();

		return result.replaceFirst("^"+this.or, "").trim();

		/*Set<SynonymSet> synonymSets = knowsSynonyms.getSynonyms(name, category);
		if(synonymSets.size() > 1) {
			return name;
		} else {
			return synonymSets.iterator().next().getPreferredTerm();
		}*/
	}

}
