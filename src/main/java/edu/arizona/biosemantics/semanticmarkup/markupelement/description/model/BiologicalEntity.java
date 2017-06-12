package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;


import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes.BiologicalEntityAttribute;
import edu.arizona.biosemantics.semanticmarkup.model.Element;
import edu.arizona.biosemantics.semanticmarkup.model.NamedElement;



/**
 * <xs:complexType>
<xs:sequence>
  <xs:element minOccurs="0" maxOccurs="unbounded" ref="character"/>
</xs:sequence>
<xs:attribute name="alter_name"/>
<xs:attribute name="constraint"/>
<xs:attribute name="constraintid" type="xs:NCName"/>
<xs:attribute name="geographical_constraint"/>
<xs:attribute name="id" use="required" type="xs:NCName"/>
<!--<xs:attribute name="in_bracket" type="xs:boolean"/>-->
<xs:attribute name="in_brackets" type="xs:boolean"/>
<xs:attribute name="name" use="required"/>
<xs:attribute name="parallelism_constraint" type="xs:NCName"/>
<xs:attribute name="taxon_constraint"/>
<xs:attribute name="ontologyid" type="xs:string"/>
<xs:attribute name="provenance" type="xs:string"/>
<xs:attribute name="notes" type="xs:string"/>
<xs:attribute name="name_original" type="xs:string" />
<xs:attribute name="type" type="biological_entity_type" />
</xs:complexType>
 * @author rodenhausen
 *
 */
@XmlRootElement
public class BiologicalEntity extends NamedElement implements Cloneable {

	@XmlPath("@" + BiologicalEntityAttribute.alter_name)
	private String alterName;
	@XmlPath("@" + BiologicalEntityAttribute.constraint)
	private String constraint;
	@XmlPath("@" + BiologicalEntityAttribute.constraintid)
	private String constraintId;
	@XmlPath("@" + BiologicalEntityAttribute.geographical_constraint)
	private String geographicalConstraint;
	@XmlPath("@" + BiologicalEntityAttribute.id)
	private String id;
	//@XmlPath("@" + StructureAttribute.in_bracket)
	//private String inBracket;
	@XmlPath("@" + BiologicalEntityAttribute.in_brackets)
	private String inBrackets;
	@XmlPath("@" + BiologicalEntityAttribute.parallelism_constraint)
	private String parallelismConstraint;
	@XmlPath("@" + BiologicalEntityAttribute.taxon_constraint)
	private String taxonConstraint;
	@XmlPath("@" + BiologicalEntityAttribute.ontologyid)
	private String ontologyId;
	@XmlPath("@" + BiologicalEntityAttribute.provenance)
	private String provenance;
	@XmlPath("@" + BiologicalEntityAttribute.notes)
	private String notes;	
	@XmlPath("@" + BiologicalEntityAttribute.name_original)
	private String nameOriginal;
	@XmlPath("@" + BiologicalEntityAttribute.constraint_original)
	private String constraintOriginal;
	@XmlPath("@" + BiologicalEntityAttribute.type)
	private String type;
	@XmlPath("@" + BiologicalEntityAttribute.src)
	private String src;

	@XmlPath("character")
	private LinkedHashSet<Character> characters = new LinkedHashSet<Character>();
	private LinkedHashSet<Relation> fromRelations = new LinkedHashSet<Relation>(); 
	private LinkedHashSet<Relation> toRelations = new LinkedHashSet<Relation>();


	public BiologicalEntity() { }


	public String getConstraint() {
		return constraint;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}

	public LinkedHashSet<Character> getCharacters() {
		return characters;
	}

	public void setCharacters(LinkedHashSet<Character> characters) {
		this.characters = characters;
	}

	public String getAlterName() {
		return alterName;
	}

	public void setAlterName(String alterName) {
		this.alterName = alterName;
	}

	public String getConstraintId() {
		return constraintId;
	}

	public void setConstraintId(String constraintId) {
		this.constraintId = constraintId;
	}

	public String getGeographicalConstraint() {
		return geographicalConstraint;
	}

	public void setGeographicalConstraint(String geographicalConstraint) {
		this.geographicalConstraint = geographicalConstraint;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/*public String getInBracket() {
		return inBracket;
	}

	public void setInBracket(String inBracket) {
		this.inBracket = inBracket;
	}*/

	public String getInBrackets() {
		return inBrackets;
	}

	public void setInBrackets(String inBrackets) {
		this.inBrackets = inBrackets;
	}

	public String getParallelismConstraint() {
		return parallelismConstraint;
	}

	public void setParallelismConstraint(String parallelismConstraint) {
		this.parallelismConstraint = parallelismConstraint;
	}

	public String getTaxonConstraint() {
		return taxonConstraint;
	}

	public void setTaxonConstraint(String taxonConstraint) {
		this.taxonConstraint = taxonConstraint;
	}

	public String getOntologyId() {
		return ontologyId;
	}

	public void setOntologyId(String ontologyId) {
		this.ontologyId = ontologyId;
	}

	public String getProvenance() {
		return provenance;
	}

	public void setProvenance(String provenance) {
		this.provenance = provenance;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNameOriginal() {
		return nameOriginal;
	}

	public void setNameOriginal(String originalName) {
		this.nameOriginal = originalName;
	}

	public String getConstraintOriginal() {
		return constraintOriginal;
	}

	public void setConstraintOriginal(String constraintOriginal) {
		this.constraintOriginal = constraintOriginal;
	}

	public void addFromRelation(Relation relation) {
		this.fromRelations.add(relation);
	}


	public void addToRelation(Relation relation) {
		this.toRelations.add(relation);
	}


	public LinkedHashSet<Relation> getFromRelations() {
		return fromRelations;
	}

	public LinkedHashSet<Relation> getToRelations() {
		return toRelations;
	}
	
	public void addCharacter(Character character) {
		this.characters.add(character);
	}


	public void appendConstraint(String constraint) {
		String newValue = "";
		if(this.constraint == null || this.constraint.isEmpty()) {
			newValue = constraint;
		} else if(!this.constraint.matches(".*?(^|; )"+constraint+"($|;).*")){
			newValue = this.constraint + "; " + constraint;
		}
		this.constraint = newValue;
	}


	public void addCharacters(LinkedHashSet<Character> characters) {
		this.characters.addAll(characters);
	}


	public void appendTaxonConstraint(String taxonConstraint) {
		String newValue = "";
		if(this.taxonConstraint == null || this.taxonConstraint.isEmpty()) {
			newValue = taxonConstraint;
		} else if(!this.taxonConstraint.matches(".*?(^|; )"+taxonConstraint+"($|;).*")){
			newValue = this.taxonConstraint + "; " + taxonConstraint;
		}
		this.taxonConstraint = newValue;
	}


	public void appendProvenance(String provenance) {
		String newValue = "";
		if(this.provenance == null || this.provenance.isEmpty()) {
			newValue = provenance;
		} else if(!this.provenance.matches(".*?(^|; )"+provenance+"($|;).*")){
			newValue = this.provenance + "; " + provenance;
		}
		this.provenance = newValue;
	}


	public void appendParallelismConstraint(String parallelismConstraint) {
		String newValue = "";
		if(this.parallelismConstraint == null || this.parallelismConstraint.isEmpty()) {
			newValue = parallelismConstraint;
		} else if(!this.parallelismConstraint.matches(".*?(^|; )"+parallelismConstraint+"($|;).*")){
			newValue = this.parallelismConstraint + "; " + parallelismConstraint;
		}
		this.parallelismConstraint = newValue;
	}


	public void appendOntologyId(String ontologyId) {
		String newValue = "";
		if(this.ontologyId == null || this.ontologyId.isEmpty()) {
			newValue = ontologyId;
		} else if(!this.ontologyId.matches(".*?(^|; )"+ontologyId+"($|;).*")){
			newValue = this.ontologyId + "; " + ontologyId;
		}
		this.ontologyId = newValue;
	}


	public void appendNotes(String notes) {
		String newValue = "";
		if(this.notes == null || this.notes.isEmpty()) {
			newValue = notes;
		} else if(!this.notes.matches(".*?(^|; )"+notes+"($|;).*")){
			newValue = this.notes + "; " + notes;
		}
		this.notes = newValue;
	}


	public void appendInBrackets(String inBrackets) {
		String newValue = "";
		if(this.inBrackets == null || this.inBrackets.isEmpty()) {
			newValue = inBrackets;
		} else if(!this.inBrackets.matches(".*?(^|; )"+inBrackets+"($|;).*")){
			newValue = this.inBrackets + "; " + inBrackets;
		}
		this.inBrackets = newValue;
	}


	/*public void appendInBracket(String inBracket) {
		String newValue = "";
		if(this.inBracket == null || this.inBracket.isEmpty()) {
			newValue = inBracket;
		} else {
			newValue = this.inBracket + "; " + inBracket;
		}
		this.inBracket = newValue;
	}*/


	public void appendId(String id) {
		String newValue = "";
		if(this.id == null || this.id.isEmpty()) {
			newValue = id;
		} else if(!this.id.matches(".*?(^|; )"+id+"($|;).*")){
			newValue = this.id + "; " + id;
		}
		this.id = newValue;
	}


	public void appendGeographicalConstraint(String geographicalConstraint) {
		String newValue = "";
		if(this.geographicalConstraint == null || this.geographicalConstraint.isEmpty()) {
			newValue = geographicalConstraint;
		} else if(!this.geographicalConstraint.matches(".*?(^|; )"+geographicalConstraint+"($|;).*")){
			newValue = this.geographicalConstraint + "; " + geographicalConstraint;
		}
		this.geographicalConstraint = newValue;
	}


	/* BiologicalEntity does not have constraintid attribute
	 * public void appendConstraintId(String constraintId) {
		String newValue = "";
		if(this.constraintId == null || this.constraintId.isEmpty()) {
			newValue = constraintId;
		} else if(!this.constraintId.matches(".*?(^|; )"+constraintId+"($|;).*")){
			newValue = this.constraintId + "; " + constraintId;
		}
		this.constraintId = newValue;
	}*/


	public void appendAlterName(String alterName) {
		String newValue = "";
		if(this.alterName == null || this.alterName.isEmpty()) {
			newValue = alterName;
		} else if(!this.alterName.matches(".*?(^|; )"+alterName+"($|;).*")){ 
			newValue = this.alterName + "; " + alterName;
		}
		this.alterName = newValue;
	}

	
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}
	
	public String getSrc(){
		return this.src;
	}
	
	public void setSrc(String statementId){
		this.src = statementId;
	}
	
	public void appendSrc(String statementId){
		String newValue = "";
		if(this.src == null || this.src.isEmpty()) {
			newValue = statementId;
		} else {
			if(!this.src.contains(statementId+";") && !this.src.endsWith(statementId))
				newValue = this.src + "; " + statementId.trim();
		}
		this.src = newValue;
	}

	@Override
	public void removeElementRecursively(Element element) {
		Iterator<Character> charactersIterator = characters.iterator();
		while(charactersIterator.hasNext()) {
			Character character = charactersIterator.next();
			if(character.equals(element))
				charactersIterator.remove();
			else
				character.removeElementRecursively(element);
		}
	}
	
	@Override
	public BiologicalEntity clone(){
		BiologicalEntity entity = new BiologicalEntity();
		entity.setAlterName(alterName);
		entity.setConstraint(constraint);
		entity.setConstraintId(constraintId);
		entity.setGeographicalConstraint(geographicalConstraint);
		entity.setId(id);
		entity.setInBrackets(inBrackets);
		entity.setName(name);
		entity.setNameOriginal(nameOriginal);
		entity.setNotes(notes);
		entity.setOntologyId(ontologyId);
		entity.setParallelismConstraint(parallelismConstraint);
		entity.setProvenance(provenance);
		entity.setTaxonConstraint(taxonConstraint);
		entity.setType(type);
		entity.setSrc(src);
	
		
		for(Relation fromR: fromRelations){
			entity.addFromRelation(fromR);
		}
		
		for(Relation toR: toRelations){
			entity.addToRelation(toR);
		}
		
		LinkedHashSet<Character> chars = new LinkedHashSet<Character>();
		for(Character character: characters){
			chars.add(character.clone());
		}
		entity.setCharacters(chars);
		
		return entity;
	}
}
