/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.partof.IOntology;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/**
 * @author Hong Cui
 * 
 * use clues from text to attach appropriate parent organ to non-specific structures such as "apex" and "side"
 */
public class StandardizeStructureNameTransformer extends AbstractTransformer {
	
	private Set<IOntology> ontologies;
	private ICharacterKnowledgeBase learnedCharacterKnowledgeBase;
	private Set<String> possess = new HashSet<String>(); //with, has, consist_of, possess
	private String nonspecificParts = null;
	
	public StandardizeStructureNameTransformer(Set<IOntology> ontologies, ICharacterKnowledgeBase learnedCharacterKnowledgeBase, Set<String> possess){
		this.ontologies = ontologies;
		this.learnedCharacterKnowledgeBase = learnedCharacterKnowledgeBase;
		this.possess = possess;
		this.nonspecificParts = "apex|appendix|area|band|base|belt|body|cavity|cell|center|centre|chamber|component|content|crack|edge|element|end|face|groove|layer|line|margin|notch|part|pore|portion|protuberance|remnant|section|"
				+ "side|stratum|surface|tip|wall|zone";
	}
	
	/**
	 * remove parts from this.nonspecificParts if parts start a sentence in the description. 
	 * for example "Body ellipsoidal" suggests that the organism has one body, and it is not a unspecific part of some other organ.
	 * @param text
	 */
	private void filterNonSpecificParts(Element description) {
		for(Element statement : description.getChildren("statement")) {
			String sentence = statement.getValue().trim();
			if (sentence != null && sentence.matches("^[A-Z].*")) { // record parentorgan
				String word1 = sentence.toLowerCase();
				if(sentence.indexOf(" ")>0)
					word1 = sentence.substring(0, sentence.indexOf(" ")).toLowerCase();
				this.nonspecificParts = this.nonspecificParts.replaceFirst("(^|\\|)"+word1+"(\\||$)", "|").replaceFirst("(^\\||\\|$)", "");
			}
		}
	}

	/**
	 * 
	 * @param parentorgan a list of organ, separated by ',', listed from suborgan to parent organ.
	 * @param partorgans a list of organ, separated by ',', listed from suborgan to parent organ.
	 * @return appropriate parentorgan for 'name' of <structure>. The parentorgan may be from the parentorgan list or an empty string. Format: "blade,leaf".
	 */
	private String hasPart(String parentorgans, String partorgans){
		parentorgans = parentorgans.trim().replaceAll("(^,|,$)", "");
		partorgans = partorgans.trim().replaceAll("(^,|,$)", "");
		
		//non-specific organ parts
		
		if(partorgans.matches("\\b("+nonspecificParts+")\\b.*") && !partorgans.contains(",")){
			parentorgans = parentorgans.replaceFirst("(\\b("+nonspecificParts+")\\b,? ?)+", ""); //"base of surface, stem" does not make sense.
			return parentorgans;
		}
		
		/* need work */
		//cross check btw parts and parents using the ontology
		String[] parts = partorgans.replaceFirst(".*(\\b("+nonspecificParts+")\\b,? ?)+", "").replaceFirst("^\\s*,", "").trim().split("\\s*,\\s*");
		String[] parents = parentorgans.split("\\s*,\\s*");
		
		int cut = -1;
		for(String part:  parts){
			for(int i = 0; i < parents.length; i++){
				for(IOntology ontology : ontologies) {
					if(ontology.isPart(part, parents[i])){
						cut = i;
						break;
					}
				}
			}
			if(cut>=0) break;
		}

		if(cut >=0){
			String po = "";
			for(int i = cut; i<parents.length; i++){
				if(parents[i].length()>0)
					po += parents[i]+" , ";
			}
			return po.replaceFirst(" , $", "");
		}

		//otherwise, return the first specific parental structure in partorgans
		if(partorgans.contains(",")){
			for(int i = parts.length-1; i >=0; i-- ){
				if(parts[i].replaceFirst("^\\b("+nonspecificParts+")\\b", "").length()>0)
					return parts[i];
			}
		}
		
		
		return "";
	}

	/**
	 * 
	 * @param parentorgans  "blade,leaf"
	 * @return "leaf blade"
	 */
	private String formatParentOrgan(String parentorgans) {
		String formatted = "";
		String[] words = parentorgans.split("\\s*,\\s*");
		for(int i = words.length-1; i>=0; i--){
			formatted += words[i]+" ";
		}
 		return formatted.trim();
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
	public String getStructureChain(Document document, Element description, Element from, int depth) {
		String chain = "";
		
		List<Element> statements = description.getChildren("statement");
		//check relations for evidence
		for(Element statement : statements){
			List<Element> relations = statement.getChildren("relation");
			for(Element relation : relations){
				String relationFrom = relation.getAttributeValue("from");
				String relationTo = relation.getAttributeValue("to");
				
				if(relationFrom != null && relationFrom.equals(from) 
						&& relation.getAttributeValue("name").matches("part_of") && relationTo != null) {
					Element to = getStructureWithId(document, relationTo);
					if(to != null) {
						chain += to.getAttributeValue("name") + ",";
						if(depth < 3) {
							chain += getStructureChain(document, description, to, ++depth);
							return chain.replaceFirst(",$", "");
						}
					}
				}
			}			
		}
		return chain.replaceFirst(",$", "");
	}

	private Element getStructureWithId(Document document, String id) {
		for(Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			if(biologicalEntity.getAttributeValue("id").equals(id)) {
				return biologicalEntity;
			}
		}
		return null;
	}

	/**
	 * form and apply a parent organ to parts
	 *
	 * @param description: 
	 * @param statement
	 * @param parentstruct element holding a candidate parent
	 * @param parentOfParentStructure string of the name of the parent organ of the parentstructure
	 * @return
	 */
	private String attachPOto(Document document, Element description, Element statement, Element parentstruct, String parentOfParentStructure) {
		String parentorgan = null;
		String porgan = null;
		if(parentstruct != null) {
			//check for 'part_of' relation on parentstructure
			String pChain = getStructureChain(document, description, parentstruct, 0).replace(" of ", ",").trim(); //part of organ of organ
			if(pChain.length() > 0){ //use explicit part_of 
				parentOfParentStructure = pChain;
			}
			//add constraint organ to parentorgan list 
			String constraint = parentstruct.getAttributeValue("constraint");
			if(constraint != null){ 
				if(learnedCharacterKnowledgeBase.isEntityStructuralContraint(constraint)){
					//parentorgan = constraint; //use the constraint of parentstruct as parentorgan, e.g. leaf blade ..., petiole ..., vein ....
					parentOfParentStructure = constraint +","+parentOfParentStructure; //blade, leaf
				}
			}
			//add name organ to parentorgan list
				//parentorgan = parentofparentstructure+" "+parentstruct.getAttributeValue("name");//leaf blade
			porgan = parentstruct.getName() + "," + parentOfParentStructure; // blade,
																				// leaf
			//parentorgan = parentorgan.trim();
			//attach parentorgan to other 'structures' in this statement
			List<Element> structures = statement.getChildren("biological_entity"); //could include 'relation' too
			for(Element struct : structures){ 
				//if(struct.getName().compareTo("structure")==0){
					if(!struct.equals(parentstruct)){//skip the 1st structure which is parentstruct
						if(!hasStructuralConstraint(struct)){
							String partpchain = getStructureChain(document, description, struct, 3).replace(" of ", ",").trim(); //part of organ of organ
							String part = struct.getAttributeValue("name") + (partpchain.isEmpty()? "" : ","+partpchain);								
							
							if(part.matches("\\b("+nonspecificParts+")\\b.*") && !part.contains(",") && porgan.compareTo("whole_organism")!=0){
								parentorgan = hasPart(parentstruct, struct, description, porgan, part);
								if(parentorgan.length()>0){
									log(LogLevel.DEBUG,"===>[part of 1] use '"+parentorgan+"' as constraint to '"+struct.getName()+"'");
									appendConstraint(struct, formatParentOrgan(parentorgan));
								}else if(possess(parentstruct, struct, description)){
									parentorgan = formatParentOrgan(porgan);
									log(LogLevel.DEBUG,"===>[possess] use '"+parentorgan+"' as constraint to '"+struct.getName()+"'");
									appendConstraint(struct, formatParentOrgan(parentorgan));
								}
							}
						}

					}
				//}
			}
		}
		return parentorgan !=null? parentorgan : porgan.replaceAll("(^,|,$)", "");
	}

	/**
	 * if parentStruct and partStruct has a non-possessing relation, return "" (e.g., The nucleus situated in the posterior part of the body. => Body is not part of Nucleus.)
	 * otherwise (i.e. has no relation, or has a possessing relation), return parent structure name as a string
	 * @param parentStruct
	 * @param partStruct
	 * @param description
	 * @param parentName a list of organ, separated by ',', listed from suborgan to parent organ.
	 * @param partName a list of organ, separated by ',', listed from suborgan to parent organ.
	 * @return the name of parent structure (Format: "blade,leaf"), or "" if parentStruct is not a parent 
	 */
	private String hasPart(Element parentStruct, Element partStruct, Element description, String parentName, String partName) {
		if(partName.contains(",")){
			return partName.substring(partName.indexOf(",")).trim();
		}
		
		List<Element> statements = description.getChildren("statement");
		//check relations for evidence
		boolean hasRelation = false;
		boolean hasPossessRelation = false;
		for(Element statement : statements){
			List<Element> relations = statement.getChildren("relation");
			for(Element relation : relations){
				String from = relation.getAttributeValue("from");
				String to = relation.getAttributeValue("to");
				if((from != null && from.equals(parentStruct) && to != null && to.equals(partStruct))) {
					hasRelation = true;
					if(possess.contains(relation.getName()))
						hasPossessRelation = true;
						
				}
			}
		}
		
		//check character constraints for evidence
		String idw = parentStruct.getAttributeValue("id");
		String idp = partStruct.getAttributeValue("id");
		for(Element statement: statements){
			List<Element> biologicalEnties = statement.getChildren("biological_entity");
			for(Element biologicalEntity : biologicalEnties){
				List<Element> characters = biologicalEntity.getChildren("character");
				Iterator<Element> it = characters.iterator();
				while(it.hasNext()){
					Element character = it.next();
					String constraintId = character.getAttributeValue("constraintid");
					String constraint = character.getAttributeValue("constraint");
					if(constraintId != null && constraintId.equals(idp) &&
						biologicalEntity.getAttributeValue("id").equals(idw) && constraint != null) {
						hasRelation = true;
						if(possess.contains(constraint.contains(" ") ? 
								constraint.substring(0, constraint.indexOf(" ")) : constraint)) //first word in constraint string is in possess
							hasPossessRelation = true;
					}
				}
			}
		}
		
		if(!hasRelation || (hasRelation && hasPossessRelation)){		
			if(partName.matches("\\b("+nonspecificParts+")\\b.*") && !partName.contains(",")){
				parentName = parentName.replaceFirst("(\\b("+nonspecificParts+")\\b,? ?)+", ""); //"base of surface, stem" does not make sense.
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
		if(learnedCharacterKnowledgeBase.isEntityStructuralContraint(constraint)) return true;
		return false;
	}

	/**
	 * 
	 * @param parentstruct
	 * @param struct
	 * @param description
	 * @return @return true if structure possess [with, has, posses] struct. This could be expressed as relation or as character constraint
	 */
	private boolean possess(Element parentstruct, Element struct, Element description) {
		List<Element> statements = description.getChildren("statement");
		//check relations for evidence
		for(Element s: statements){
			List<Element> relations = s.getChildren("relation");
			for(Element relation : relations) {
				String name = relation.getAttributeValue("name");
				String negation = relation.getAttributeValue("negation");
				String from = relation.getAttributeValue("from");
				String to = relation.getAttributeValue("to");
				if(possess.contains(name) && (negation==null || negation.equals("false")) &&
						(from != null && from.equals(parentstruct) && to !=null && to.equals(struct))) {
					return true;
				}
			}
		}
		
		//check character constraints for evidence
		String idw = parentstruct.getAttributeValue("id");
		String idp = struct.getAttributeValue("id");
		for(Element statement: statements){
			List<Element> biologicalEntities = statement.getChildren("biological_entity");
			for(Element biologicalEntity : biologicalEntities) {
				List<Element> characters = biologicalEntity.getChildren("character");
				Iterator<Element> it = characters.iterator();
				while(it.hasNext()) {
					Element character = it.next();
					String constraintId = character.getAttributeValue("constraintid");
					String constraint = character.getAttributeValue("constraint");
					if(constraintId != null && constraintId.equals(idp) &&
							biologicalEntity.getAttributeValue("id").equals(idw) && constraint !=null &&
							possess.contains(constraint)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void transform(Document document) {
		//log(LogLevel.DEBUG, description.getText());
		for(Element description : this.descriptionXpath.evaluate(document)) {
			filterNonSpecificParts(description);
			String parentorgan = null;
			Element biologicalEntityA = null;
			for(Element statement : description.getChildren("statement")) {
				if(statement.getValue() != null && statement.getValue().matches("^[A-Z].*")){ //record parentorgan
					List<Element> biologicalEntities = statement.getChildren("biological_entity");
					if(!biologicalEntities.isEmpty()) {
						biologicalEntityA = biologicalEntities.get(0); //get 1st structure
						if(biologicalEntityA != null) {
							//attach parent organ to other structures in this statement, return parentorgan used.
							parentorgan = attachPOto(document, description, statement, biologicalEntityA, "");	
						}
					}
				} else { //sentences not starting with a capitalized structure names => those structures after ';'
					if(parentorgan != null) {
						List<Element> biologicalEntities = statement.getChildren("biological_entity");
						if(!biologicalEntities.isEmpty()) {
							Element biologicalEntityB = biologicalEntities.get(0); //get 1st structure
							//apply parentorgan + localpo(struct) to other structures in the statement
							attachPOto(document, description, statement, biologicalEntityB, parentorgan);

							//then apply parentorgan to the first structure 
							String pocp = parentorgan;
							String pchain = getStructureChain(document, description, biologicalEntityB, 0).replace(" of ", ",").trim(); //part of organ of organ
							if(pchain.length() > 0) { //use explicit part_of 
								log(LogLevel.DEBUG, "===>[pchain] use '"
										+ pchain + "' as constraint to '"
										+ biologicalEntityA.getName() + "'");
								appendConstraint(biologicalEntityB, formatParentOrgan(pchain)); 
							} else {
								String part = biologicalEntityA.getName();				
								if(part.matches("\\b("+nonspecificParts+")\\b.*") && !part.contains(",") && parentorgan.compareTo("whole_organism")!=0){
									parentorgan = hasPart(biologicalEntityA, biologicalEntityB, description, parentorgan, part);
									if(parentorgan.length()>0){
										log(LogLevel.DEBUG,"===>[part of 2] use '"+parentorgan+"' as constraint to '"+biologicalEntityB.getName()+"'");
										appendConstraint(biologicalEntityB, formatParentOrgan(parentorgan));
										System.out.println();
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
					}
				}
			}
		}		 
	}
	

}
