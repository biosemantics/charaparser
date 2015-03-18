/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.lib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.IOntology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * @author Hong Cui
 * 
 * use clues from text to attach appropriate parent organ to non-specific structures such as "apex" and "side"
 */
public class StructureNameStandardizer {
	Set<IOntology> ontologies;
	ICharacterKnowledgeBase learnedCharacterKnowledgeBase;
	Set<String> possess = new HashSet<String>(); //with, has, consist_of, possess
	String nonspecificParts = null;
	
	public StructureNameStandardizer(Set<IOntology> ontologies, ICharacterKnowledgeBase learnedCharacterKnowledgeBase, Set<String> possess){
		this.ontologies = ontologies;
		this.learnedCharacterKnowledgeBase = learnedCharacterKnowledgeBase;
		this.possess = possess;
		this.nonspecificParts = "apex|appendix|area|band|base|belt|body|cavity|cell|center|centre|chamber|component|content|crack|edge|element|end|face|groove|layer|line|margin|notch|part|pore|portion|protuberance|remnant|section|"
				+ "side|stratum|surface|tip|wall|zone";
		
	}
	
	public void standardize(Description description){
		log(LogLevel.DEBUG, description.getText());
		filterNonspecificParts(description);
		String parentorgan=null;
		BiologicalEntity structure = null;
		for(Statement s: description.getStatements()){
			if(s.getText()!=null && s.getText().matches("^[A-Z].*")){ //record parentorgan
				if(s.getBiologicalEntities().size()>0){
					structure = s.getBiologicalEntities().get(0); //get 1st structure
					if(structure!=null){
						//attach parent organ to other structures in this statement, return parentorgan used.
						parentorgan = attachPOto(description, s, structure, "");	
					}
				}
			}else{//sentences not starting with a capitalized structure names => those structures after ';'
				if(parentorgan!=null){
					if(s.getBiologicalEntities().size()>0){
						BiologicalEntity struct = s.getBiologicalEntities().get(0); //get 1st structure
						if(struct!=null){
							//apply parentorgan + localpo(struct) to other structures in the statement
							attachPOto(description, s, struct, parentorgan);

							//then apply parentorgan to the first structure 
							String pocp = parentorgan;
							String pchain = getStructureChain(description,struct, 0).replace(" of ", ",").trim(); //part of organ of organ
							if(pchain.length()>0){ //use explicit part_of 
								log(LogLevel.DEBUG, "===>[pchain] use '"+pchain+"' as constraint to '"+struct.getName()+"'");
								struct.appendConstraint(formatParentOrgan(pchain)); 
							}else{
								String part = struct.getName();				
								if(part.matches("\\b("+nonspecificParts+")\\b.*") && !part.contains(",") && parentorgan.compareTo("whole_organism")!=0){
									parentorgan = hasPart(structure, struct, description, parentorgan, part);
									if(parentorgan.length()>0){
										log(LogLevel.DEBUG,"===>[part of 2] use '"+parentorgan+"' as constraint to '"+struct.getName()+"'");
										struct.appendConstraint(formatParentOrgan(parentorgan));
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

	/**
	 * remove parts from this.nonspecificParts if parts start a sentence in the description. 
	 * for example "Body ellipsoidal" suggests that the organism has one body, and it is not a unspecific part of some other organ.
	 * @param text
	 */
	private void filterNonspecificParts(Description description) {
		
		for(Statement s: description.getStatements()){
			String sentence = s.getText().trim();
			if(sentence!=null && sentence.matches("^[A-Z].*")){ //record parentorgan
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
	public static String getStructureChain(Description description, BiologicalEntity from, int depth) {
		String chain = "";
		
		List<Statement> statements = description.getStatements();
		//check relations for evidence
		for(Statement s: statements){
			List<Relation> relations = s.getRelations();
			for(Relation relation: relations){
				if(relation.getFromStructure()!=null && relation.getFromStructure().equals(from) 
						&& relation.getName().matches("part_of") && relation.getToStructure()!=null){
					BiologicalEntity to = relation.getToStructure();
					chain += to.getName()+ ",";
					if(depth < 3){
						chain += getStructureChain(description, to, ++depth);
						return chain.replaceFirst(",$", "");
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
	 * @param parentstruct element holding a candidate parent
	 * @param parentofparentstructure string of the name of the parent organ of the parentstructure
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String attachPOto(Description description, Statement statement, BiologicalEntity parentstruct, String parentofparentstructure) {
		String parentorgan = null;
		String porgan = null;
		if(parentstruct!=null){
			//check for 'part_of' relation on parentstructure
			String pchain = getStructureChain(description, parentstruct, 0).replace(" of ", ",").trim(); //part of organ of organ
			if(pchain.length()>0){ //use explicit part_of 
				parentofparentstructure = pchain;
			}
			//add constraint organ to parentorgan list 
			String constraint = parentstruct.getConstraint() !=null? parentstruct.getConstraint() : null;
			if(constraint!=null){ 
				if(learnedCharacterKnowledgeBase.isEntityStructuralContraint(constraint)){
					//parentorgan = constraint; //use the constraint of parentstruct as parentorgan, e.g. leaf blade ..., petiole ..., vein ....
					parentofparentstructure = constraint +","+parentofparentstructure; //blade, leaf
				}
			}
			//add name organ to parentorgan list
				//parentorgan = parentofparentstructure+" "+parentstruct.getAttributeValue("name");//leaf blade
			porgan = parentstruct.getName()+","+parentofparentstructure; //blade, leaf
			
			//parentorgan = parentorgan.trim();
			//attach parentorgan to other 'structures' in this statement
			List<BiologicalEntity> structures = statement.getBiologicalEntities(); //could include 'relation' too
			for(BiologicalEntity struct: structures){ 
				//if(struct.getName().compareTo("structure")==0){
					if(!struct.equals(parentstruct)){//skip the 1st structure which is parentstruct
						if(!hasStructuralConstraint(struct)){
							String partpchain = getStructureChain(description, struct, 3).replace(" of ", ",").trim(); //part of organ of organ
							String part = struct.getName()+(partpchain.isEmpty()? "" : ","+partpchain);								
							
							if(part.matches("\\b("+nonspecificParts+")\\b.*") && !part.contains(",") && porgan.compareTo("whole_organism")!=0){
								parentorgan = hasPart(parentstruct, struct, description, porgan, part);
								if(parentorgan.length()>0){
									log(LogLevel.DEBUG,"===>[part of 1] use '"+parentorgan+"' as constraint to '"+struct.getName()+"'");
									((BiologicalEntity)struct).appendConstraint(formatParentOrgan(parentorgan));
								}else if(possess(parentstruct, struct, description)){
									parentorgan = formatParentOrgan(porgan);
									log(LogLevel.DEBUG,"===>[possess] use '"+parentorgan+"' as constraint to '"+struct.getName()+"'");
									((BiologicalEntity)struct).appendConstraint(formatParentOrgan(parentorgan));
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
	private String hasPart(BiologicalEntity parentStruct,
			BiologicalEntity partStruct, Description description, String parentName, String partName) {
		
		if(partName.contains(",")){
			return partName.substring(partName.indexOf(",")).trim();
		}
		
		List<Statement> statements = description.getStatements();
		//check relations for evidence
		boolean hasRelation = false;
		boolean hasPossessRelation = false;
		for(Statement s: statements){
			List<Relation> relations = s.getRelations();
			for(Relation r: relations){
				if((r.getFromStructure()!=null && r.getFromStructure().equals(parentStruct) && r.getToStructure()!=null && r.getToStructure().equals(partStruct))){
					hasRelation = true;
					if(possess.contains(r.getName()))
						hasPossessRelation = true;
						
				}
			}
		}
		
		//check character constraints for evidence
		String idw = parentStruct.getId();
		String idp = partStruct.getId();
		for(Statement statement: statements){
			List<BiologicalEntity> structures = statement.getBiologicalEntities();
			for(BiologicalEntity s: structures){
				LinkedHashSet<Character> characters = s.getCharacters();
				Iterator<Character> it = characters.iterator();
				while(it.hasNext()){
					Character c = it.next();
					if(c.getConstraintId()!=null && c.getConstraintId().compareTo(idp)==0 &&
							s.getId().compareTo(idw)==0 && c.getConstraint()!=null){
						hasRelation = true;
						if(possess.contains(c.getConstraint().contains(" ")? c.getConstraint().substring(0, c.getConstraint().indexOf(" ")): c.getConstraint())) //first word in constraint string is in possess
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
	private boolean hasStructuralConstraint(BiologicalEntity struct) {
		String constraint = struct.getConstraint();
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
	private boolean possess(BiologicalEntity parentstruct, BiologicalEntity struct,
			Description description) {
	
		List<Statement> statements = description.getStatements();
		//check relations for evidence
		for(Statement s: statements){
			List<Relation> relations = s.getRelations();
			for(Relation r: relations){
				if(possess.contains(r.getName()) && 
						(r.getNegation()==null || r.getNegation().compareTo("false")==0) &&
						(r.getFromStructure()!=null && r.getFromStructure().equals(parentstruct) && r.getToStructure()!=null && r.getToStructure().equals(struct))){
					return true;
				}
						
			}
		}
		
		//check character constraints for evidence
		String idw = parentstruct.getId();
		String idp = struct.getId();
		for(Statement statement: statements){
			List<BiologicalEntity> structures = statement.getBiologicalEntities();
			for(BiologicalEntity s: structures){
				LinkedHashSet<Character> characters = s.getCharacters();
				Iterator<Character> it = characters.iterator();
				while(it.hasNext()){
					Character c = it.next();
					if(c.getConstraintId()!=null && c.getConstraintId().compareTo(idp)==0 &&
							s.getId().compareTo(idw)==0 && c.getConstraint()!=null &&
							possess.contains(c.getConstraint())){
						return true;

					}

				}
			}
		}
		return false;
	}
	

}
