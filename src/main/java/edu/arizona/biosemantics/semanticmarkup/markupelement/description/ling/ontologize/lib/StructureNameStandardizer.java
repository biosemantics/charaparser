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
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * @author Hong Cui
 * using part_of relations from ontologies to attach appropriate parent organ to structures such as 'blade' and 'apex'
 */
public class StructureNameStandardizer {
	IOntology ontology;
	ICharacterKnowledgeBase learnedCharacterKnowledgeBase;
	Set<String> possess = new HashSet<String>(); //with, has, consist_of, possess
	
	public StructureNameStandardizer(IOntology ontology, ICharacterKnowledgeBase learnedCharacterKnowledgeBase, Set<String> possess){
		this.ontology = ontology;
		this.learnedCharacterKnowledgeBase = learnedCharacterKnowledgeBase;
		this.possess = possess;
	}
	
	public void standardize(Description description){
		log(LogLevel.DEBUG, description.getText());
		String parentorgan=null;
		for(Statement s: description.getStatements()){
			if(s.getText()!=null && s.getText().matches("^[A-Z].*")){ //record parentorgan
				if(s.getStructures().size()>0){
					Structure structure = s.getStructures().get(0); //get 1st structure
					if(structure!=null){
						//attach parent organ to other structures in this statement, return parentorgan used.
						parentorgan = attachPOto(description, s, structure, "");	
					}
				}
			}else{//sentences not starting with a capitalized structure names => those structures after ';'
				if(parentorgan!=null){
					if(s.getStructures().size()>0){
						Structure struct = s.getStructures().get(0); //get 1st structure
						if(struct!=null){
							//apply parentorgan + localpo(struct) to other structures in the statement
							attachPOto(description, s, struct, parentorgan);

							//then apply parentorgan to the first structure 
							String pocp = parentorgan;
							String pchain = getStructureChain(description,struct, 0).replace(" of ", ",").trim(); //part of organ of organ
							if(pchain.length()>0){ //use explicit part_of 
								//log(LogLevel.DEBUG, "===>[pchain] use '"+pchain+"' as constraint to '"+struct.getName()+"'");
								struct.appendConstraint(formatParentOrgan(pchain)); 
							}else{
								String part = struct.getName();								
								parentorgan = hasPart(parentorgan, part);
								if(parentorgan.length()>0){
									//log(LogLevel.DEBUG,"===>[part of 2] use '"+parentorgan+"' as constraint to '"+struct.getName()+"'");
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

	/**
	 * 
	 * @param parentorgan a list of organ, separated by ',', listed from suborgan to parent organ.
	 * @param partorgans a list of organ, separated by ',', listed from suborgan to parent organ.
	 * @return appropriate parentorgan for 'name' of <structure>. The parentorgan may be from the parentorgan list or an empty string. Format: leaf blade.
	 */
	private String hasPart(String parentorgans, String partorgans){
		parentorgans = parentorgans.trim().replaceAll("(^,|,$)", "");
		partorgans = partorgans.trim().replaceAll("(^,|,$)", "");
		
		//non-specific organ parts
		String nonspecificparts = "apex|appendix|area|band|base|belt|body|cell|center|centre|component|content|crack|edge|element|end|face|layer|line|margin|part|pore|portion|protuberance|remnant|section|"
				+ "side|stratum|surface|tip|wall|zone";
		if(partorgans.matches("\\b("+nonspecificparts+")\\b.*")){
			parentorgans.replaceFirst("(\\b("+nonspecificparts+")\\b,? ?)+", ""); //"base of surface, stem" does not make sense.
			return parentorgans;
		}
		String[] parts = partorgans.replaceFirst(".*(\\b("+nonspecificparts+")\\b,? ?)+", "").replaceFirst("^\\s*,", "").trim().split("\\s*,\\s*");
		String[] parents = parentorgans.split("\\s*,\\s*");
		
		int cut = -1;
		for(String part:  parts){
			for(int i = 0; i < parents.length; i++){
				if(ontology.isPart(part, parents[i])){
					cut = i;
					break;
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
	public static String getStructureChain(Description description, Structure from, int depth) {
		String chain = "";
		
		List<Statement> statements = description.getStatements();
		//check relations for evidence
		for(Statement s: statements){
			List<Relation> relations = s.getRelations();
			for(Relation relation: relations){
				if(relation.getFromStructure()!=null && relation.getFromStructure().equals(from) 
						&& relation.getName().matches("part_of") && relation.getToStructure()!=null){
					Structure to = relation.getToStructure();
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
	private String attachPOto(Description description, Statement statement, Structure parentstruct, String parentofparentstructure) {
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
				if(learnedCharacterKnowledgeBase.isEntity(constraint)){
					//parentorgan = constraint; //use the constraint of parentstruct as parentorgan, e.g. leaf blade ..., petiole ..., vein ....
					parentofparentstructure = constraint +","+parentofparentstructure; //blade, leaf
				}
			}
			//add name organ to parentorgan list
				//parentorgan = parentofparentstructure+" "+parentstruct.getAttributeValue("name");//leaf blade
			porgan = parentstruct.getName()+","+parentofparentstructure; //blade, leaf
			
			//parentorgan = parentorgan.trim();
			//attach parentorgan to other 'structures' in this statement
			List<Structure> structures = statement.getStructures(); //could include 'relation' too
			for(Structure struct: structures){ 
				//if(struct.getName().compareTo("structure")==0){
					if(!struct.equals(parentstruct)){//skip the 1st structure which is parentstruct
						String partpchain = getStructureChain(description, struct, 3).replace(" of ", ",").trim(); //part of organ of organ
						String part = struct.getName()+","+partpchain;								
						//
						parentorgan = hasPart(porgan, part);
						if(parentorgan.length()>0){
							//log(LogLevel.DEBUG,"===>[part of 1] use '"+parentorgan+"' as constraint to '"+struct.getName()+"'");
							((Structure)struct).appendConstraint(formatParentOrgan(parentorgan));
						}else if(possess(parentstruct, struct, description)){
							parentorgan = formatParentOrgan(porgan);
							//log(LogLevel.DEBUG,"===>[possess] use '"+parentorgan+"' as constraint to '"+struct.getName()+"'");
							((Structure)struct).appendConstraint(formatParentOrgan(parentorgan));
						}

					}
				//}
			}
		}
		return parentorgan !=null? parentorgan : porgan.replaceAll("(^,|,$)", "");
	}

	/**
	 * 
	 * @param parentstruct
	 * @param struct
	 * @param description
	 * @return @return true if structure possess [with, has, posses] struct. This could be expressed as relation or as character constraint
	 */
	private boolean possess(Structure parentstruct, Structure struct,
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
			List<Structure> structures = statement.getStructures();
			for(Structure s: structures){
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
