package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * @author Hong Cui
 * 
 * use clues from text to attach appropriate parent organ to non-specific structures such as "apex" and "side"
 * Resolve lumping of characters (due to non specific parts)
 */
public class StandardizeStructureName extends AbstractTransformer {
	
	private ICharacterKnowledgeBase characterKnowledgeBase;
	private Set<String> possessionTerms = new HashSet<String>(); //with, has, consist_of, possess
	private String nonSpecificParts = "apex|appendix|area|band|base|belt|body|cavity|cell|center|centre|chamber|component|content|crack|edge|element|end|"
			+ "face|groove|layer|line|margin|notch|part|pore|portion|protuberance|remnant|section|"
			+ "side|stratum|surface|tip|wall|zone";
	
	public StandardizeStructureName(ICharacterKnowledgeBase characterKnowledgeBase, Set<String> possessionTerms){
		this.characterKnowledgeBase = characterKnowledgeBase;
		this.possessionTerms = possessionTerms;
	}
	
	@Override
	public void transform(Document document) {
		for(Element description : this.descriptionXpath.evaluate(document)) {
			String nonSpecificParts = removeNonSpecificPartsIfFirstWord(description);
			
			String parentOrgan = null;
			Element biologicalEntityA = null;
			for(Element statement : description.getChildren("statement")) {
				List<Element> biologicalEntities = statement.getChildren("biological_entity");
				Element textElement = statement.getChild("text");
				String text = textElement.getValue();
				
				if(text != null && text.matches("^[A-Z].*")){ //record parentorgan
					if(!biologicalEntities.isEmpty()) {
						biologicalEntityA = biologicalEntities.get(0);
						//attach parent organ to other structures in this statement, return parentorgan used.
						parentOrgan = attachParentOrganToOtherBiologicalEntities(document, description, statement, biologicalEntityA, "", nonSpecificParts);	
					}
				} else { 
					//sentences not starting with a capitalized structure names => those structures after ';'
					if(parentOrgan != null) {
						if(!biologicalEntities.isEmpty()) {
							//then apply parentorgan to the first structure 
							biologicalEntityA = biologicalEntities.get(0); 
							//apply parentorgan + localpo(struct) to other structures in the statement
							attachParentOrganToOtherBiologicalEntities(document, description, statement, biologicalEntityA, parentOrgan, nonSpecificParts);
							handlePartOfOrganOfOrgan(document, description, biologicalEntityA, parentOrgan);
						}
					}
				}
			}
		}		 
	}

	private void handlePartOfOrganOfOrgan(Document document, Element description, Element biologicalEntity, String parentOrgan) {
		String partOfChain = getPartOfChain(document, description, biologicalEntity, 0).replace(" of ", ",").trim(); 
		if(!partOfChain.isEmpty()) { 
			//use explicit part_of 
			log(LogLevel.DEBUG, "===>[pchain] use '" + partOfChain + "' as constraint to '" + biologicalEntity.getName() + "'");
			appendConstraint(biologicalEntity, formatParentOrgan(partOfChain)); 
		} else {
			String part = biologicalEntity.getName();				
			if(part.matches("\\b(" + nonSpecificParts + ")\\b.*") && !part.contains(",") && parentOrgan.equals("whole_organism")){
				parentOrgan = hasPart(biologicalEntity, biologicalEntity, description, parentOrgan, part, nonSpecificParts);
				if(!parentOrgan.isEmpty()){
					log(LogLevel.DEBUG,"===>[part of 2] use '" + parentOrgan + "' as constraint to '" + biologicalEntity.getName() + "'");
					appendConstraint(biologicalEntity, formatParentOrgan(parentOrgan));
				}/*else{
					//quite strong an assumption that the organ of the first clause is the parent organ of all following clauses.
					//If this is not true, or the part_of hashtable is complete, comment out this part.
					//log(LogLevel.DEBUG,"===>[default] use '"+pocp+"' as constraint to '"+struct.getName()+"'");
					struct.appendConstraint(formatParentOrgan(pocp));
					parentorgan = pocp;
				}*/
			}
		}
	}
	
	/**
	 * remove parts from this.nonSpecificParts if parts start a sentence in the description. 
	 * for example "Body ellipsoidal" suggests that the organism has one body, and it is not a unspecific part of some other organ.
	 * @param text
	 * @return 
	 */
	private String removeNonSpecificPartsIfFirstWord(Element description) {
		String result = this.nonSpecificParts;
		for(Element statement : description.getChildren("statement")) {
			String sentence = statement.getValue().toLowerCase().trim();
			if (sentence != null && sentence.matches("^[A-Z].*")) { // record parentorgan
				String word1 = sentence.toLowerCase();
				if(sentence.indexOf(" ") > 0)
					word1 = sentence.substring(0, sentence.indexOf(" ")).toLowerCase();
				result = this.nonSpecificParts.replaceFirst("(^|\\|)"+word1+"(\\||$)", "|").replaceFirst("(^\\||\\|$)", "");
			}
		}
		return result;
	}

	/**
	 * 
	 * @param parentOrgans  "blade,leaf"
	 * @return "leaf blade"
	 */
	private String formatParentOrgan(String parentOrgans) {
		String result = "";
		String[] words = parentOrgans.split("\\s*,\\s*");
		for (int i = words.length - 1; i >= 0; i--) {
			result += words[i] + " ";
		}
		return result.trim();
	}


	/**
	 * trace part_of relations of structid to get all its parent structures,
	 * separated by , in order
	 * 
	 * 
	 * TODO treat "in|on" as part_of? probably not
	 * @param root
	 * @param xpath
	 *            : "//relation[@name='part_of'][@from='"+structid+"']"
	 * @count number of rounds in the iteration
	 * @return ,-separated organs from part to whole
	 */
	private String getPartOfChain(Document document, Element description, Element from, int depth) {
		String chain = "";
		
		List<Element> statements = description.getChildren("statement");
		//check relations for evidence
		for(Element statement : statements){
			List<Element> relations = statement.getChildren("relation");
			for(Element relation : relations){
				String relationFrom = relation.getAttributeValue("from");
				String relationTo = relation.getAttributeValue("to");
				
				if (relationFrom != null
						&& relationFrom.equals(from.getAttributeValue("id"))
						&& relation.getAttributeValue("name")
								.matches("part_of") && relationTo != null) {
					Element to = getBiologicalEntityWithId(document, relationTo);
					if(to != null) {
						chain += to.getAttributeValue("name") + ",";
						if(depth < 3) {
							chain += getPartOfChain(document, description, to, ++depth);
							return chain.replaceFirst(",$", "");
						}
					}
				}
			}			
		}
		return chain.replaceFirst(",$", "");
	}



	/**
	 * form and apply a parent organ to parts
	 *
	 * @param description: 
	 * @param statement
	 * @param parentStructure element holding a candidate parent
	 * @param parentOfParentStructure string of the name of the parent organ of the parentstructure
	 * @return
	 */
	private String attachParentOrganToOtherBiologicalEntities(Document document, Element description, Element statement, Element parentStructure, String parentOfParentStructure, String nonSpecificParts) {
		String parentOrgan = getParentOrgan(document, description, parentStructure, parentOfParentStructure);
		
		//parentorgan = parentorgan.trim();
		//attach parentorgan to other 'structures' in this statement
		List<Element> biologicalEntities = statement.getChildren("biological_entity"); //could include 'relation' too
		
		String parentStructureName = null;
		for(Element biologicalEntity : biologicalEntities) { 
			if(!biologicalEntity.equals(parentStructure)){
				//skip the 1st structure which is parentstruct
				if(!hasStructuralConstraint(biologicalEntity)) {
					String partOfChain = getPartOfChain(document, description, biologicalEntity, 3).replace(" of ", ",").trim(); //part of organ of organ
					String part = biologicalEntity.getAttributeValue("name") + (partOfChain.isEmpty() ? "" : "," + partOfChain);							
					
					if(part.matches("\\b(" + nonSpecificParts + ")\\b.*") && !part.contains(",") && parentOrgan.equals("whole_organism")) {
						parentStructureName = hasPart(parentStructure, biologicalEntity, description, parentOrgan, part, nonSpecificParts);
						if(!parentStructureName.isEmpty()) {
							log(LogLevel.DEBUG,"===>[part of 1] use '" + parentStructureName + "' as constraint to '" + biologicalEntity.getName() + "'");
							appendConstraint(biologicalEntity, formatParentOrgan(parentStructureName));
						} else if(possess(parentStructure, biologicalEntity, description)) {
							parentStructureName = formatParentOrgan(parentOrgan);
							log(LogLevel.DEBUG,"===>[possess] use '" + parentStructureName + "' as constraint to '" + biologicalEntity.getName()+"'");
							appendConstraint(biologicalEntity, formatParentOrgan(parentStructureName));
						}
					}
				}
			}
		}
		return parentStructureName != null ? parentStructureName : parentOrgan.replaceAll("(^,|,$)", "");
	}

	private String getParentOrgan(Document document, Element description, Element parentStructure, String parentOfParentStructure) {
		//check for 'part_of' relation on parentstructure
		String partOfChain = getPartOfChain(document, description, parentStructure, 0).replace(" of ", ",").trim(); //part of organ of organ
		if(!partOfChain.isEmpty()) { //use explicit part_of 
			parentOfParentStructure = partOfChain;
		}
		
		//add constraint organ to parentorgan list 
		String parentStructureConstraint = parentStructure.getAttributeValue("constraint");
		if(parentStructureConstraint != null){ 
			if(characterKnowledgeBase.isEntityStructuralContraint(parentStructureConstraint)){
				//parentorgan = constraint; //use the constraint of parentstruct as parentorgan, e.g. leaf blade ..., petiole ..., vein ....
				parentOfParentStructure = parentStructureConstraint + "," + parentOfParentStructure; //blade, leaf
			}
		}
		//add name organ to parentorgan list
		//parentorgan = parentofparentstructure+" "+parentstruct.getAttributeValue("name");//leaf blade
		return parentStructure.getAttributeValue("name") + "," + parentOfParentStructure; // blade, leaf
	}

	/**
	 * if parentStruct and partStruct has a non-possessing relation, return "" (e.g., The nucleus situated in the posterior part of the body. => Body is not part of Nucleus.)
	 * otherwise (i.e. has no relation, or has a possessing relation), return parent structure name as a string
	 * @param parentStructure
	 * @param partStructure
	 * @param description
	 * @param parentName a list of organ, separated by ',', listed from suborgan to parent organ.
	 * @param partName a list of organ, separated by ',', listed from suborgan to parent organ.
	 * @return the name of parent structure (Format: "blade,leaf"), or "" if parentStruct is not a parent 
	 */
	private String hasPart(Element parentStructure, Element partStructure, Element description, String parentName, String partName, String nonSpecificParts) {
		if(partName.contains(",")) {
			return partName.substring(partName.indexOf(",")).trim();
		}
		
		List<Element> statements = description.getChildren("statement");
		//check relations for evidence
		boolean hasRelation = false;
		boolean hasPossessRelation = false;
		for(Element statement : statements) {
			List<Element> relations = statement.getChildren("relation");
			for(Element relation : relations) {
				String from = relation.getAttributeValue("from");
				String to = relation.getAttributeValue("to");
				if((from != null && from.equals(parentStructure.getAttributeValue("id")) && 
						to != null && to.equals(partStructure.getAttributeValue("id")))) {
					hasRelation = true;
					if(possessionTerms.contains(relation.getName()))
						hasPossessRelation = true;	
				}
			}
		}
		
		//check character constraints for evidence
		String parentId = parentStructure.getAttributeValue("id");
		String partId = partStructure.getAttributeValue("id");
		for(Element statement : statements){
			List<Element> biologicalEnties = statement.getChildren("biological_entity");
			for(Element biologicalEntity : biologicalEnties) {
				for(Element character : biologicalEntity.getChildren("character")) {
					String constraintId = character.getAttributeValue("constraintid");
					String constraint = character.getAttributeValue("constraint");
					if(constraintId != null && constraintId.equals(partId) &&
						biologicalEntity.getAttributeValue("id").equals(parentId) && constraint != null) {
						hasRelation = true;
						if(possessionTerms.contains(constraint.contains(" ") ? 
								constraint.substring(0, constraint.indexOf(" ")) : constraint)) //first word in constraint string is in possess
							hasPossessRelation = true;
					}
				}
			}
		}
		
		if(!hasRelation || (hasRelation && hasPossessRelation)){		
			if(partName.matches("\\b(" + nonSpecificParts + ")\\b.*") && !partName.contains(",")){
				parentName = parentName.replaceFirst("(\\b(" + nonSpecificParts + ")\\b,? ?)+", ""); //"base of surface, stem" does not make sense.
				return parentName;
			}
		}
		return "";
	}

	/**
	 * 
	 * @param struct
	 * @return true if the structure element has a constraint that refers to a(nother) structure
	 */
	private boolean hasStructuralConstraint(Element struct) {
		String constraint = struct.getAttributeValue("constraint");
		if(constraint==null || constraint.isEmpty()) return false;
		if(characterKnowledgeBase.isEntityStructuralContraint(constraint)) return true;
		return false;
	}

	/**
	 * 
	 * @param parentStructure
	 * @param structure
	 * @param description
	 * @return @return true if structure possess [with, has, posses] struct. This could be expressed as relation or as character constraint
	 */
	private boolean possess(Element parentStructure, Element structure, Element description) {
		List<Element> statements = description.getChildren("statement");
		//check relations for evidence
		for(Element s: statements){
			List<Element> relations = s.getChildren("relation");
			for(Element relation : relations) {
				String name = relation.getAttributeValue("name");
				String negation = relation.getAttributeValue("negation");
				String from = relation.getAttributeValue("from");
				String to = relation.getAttributeValue("to");
				if(possessionTerms.contains(name) && (negation==null || negation.equals("false")) &&
						(from != null && from.equals(parentStructure.getAttributeValue("id")) && to !=null && 
						to.equals(structure.getAttributeValue("id")))) {
					return true;
				}
			}
		}
		
		//check character constraints for evidence
		String parentStructureId = parentStructure.getAttributeValue("id");
		String structureId = structure.getAttributeValue("id");
		for(Element statement: statements){
			List<Element> biologicalEntities = statement.getChildren("biological_entity");
			for(Element biologicalEntity : biologicalEntities) {
				List<Element> characters = biologicalEntity.getChildren("character");
				Iterator<Element> it = characters.iterator();
				while(it.hasNext()) {
					Element character = it.next();
					String constraintId = character.getAttributeValue("constraintid");
					String constraint = character.getAttributeValue("constraint");
					if(constraintId != null && constraintId.equals(structureId) &&
							biologicalEntity.getAttributeValue("id").equals(parentStructureId) && constraint !=null &&
							possessionTerms.contains(constraint)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	
}
