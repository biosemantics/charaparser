package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes.CharacterAttribute;
import edu.arizona.biosemantics.semanticmarkup.model.Element;
import edu.arizona.biosemantics.semanticmarkup.model.NamedElement;

/**
 *     <xs:complexType>
      <xs:attribute name="char_type" type="xs:NCName"/>
      <xs:attribute name="constraint"/>
      <xs:attribute name="constraintid"/>
      <xs:attribute name="from"/>
      <xs:attribute name="from_inclusive" type="xs:boolean"/>
      <xs:attribute name="from_unit" type="xs:NCName"/>
      <xs:attribute name="geographical_constraint"/>
      <xs:attribute name="in_brackets" type="xs:boolean"/>
      <xs:attribute name="modifier"/>
      <xs:attribute name="name"/>
      <xs:attribute name="organ_constraint"/>
      <xs:attribute name="other_constraint"/>
      <xs:attribute name="parallelism_constraint" type="xs:NCName"/>
      <xs:attribute name="taxon_constraint"/>
      <xs:attribute name="to"/>
      <xs:attribute name="to_inclusive" type="xs:boolean"/>
      <xs:attribute name="to_unit" type="xs:NCName"/>
      <xs:attribute name="type"/>
      <xs:attribute name="unit"/>
      <xs:attribute name="upper_restricted" type="xs:boolean"/>
      <xs:attribute name="value"/>
      <xs:attribute name="ontologyid" type="xs:string"/>
      <xs:attribute name="provenance" type="xs:string"/>
      <xs:attribute name="notes" type="xs:string"/>
      <xs:attribute name="is_modifier" type="xs:boolean"/>
    </xs:complexType>
 * @author rodenhausen
 *
 */
@XmlRootElement
public class Character extends NamedElement implements Cloneable {

	@XmlPath("@" + CharacterAttribute.value)
	private String value;
	@XmlPath("@" + CharacterAttribute.char_type)
	private String charType;
	@XmlPath("@" + CharacterAttribute.constraint)
	private String constraint;
	@XmlPath("@" + CharacterAttribute.constraintid)
	private String constraintId;
	@XmlPath("@" + CharacterAttribute.notes)
	private String notes; //"alterID:o1 o2"; "[duplicate value]"
	@XmlPath("@" + CharacterAttribute.provenance)
	private String provenance;
	@XmlPath("@" + CharacterAttribute.ontologyid)
	private String ontologyId;
	@XmlPath("@" + CharacterAttribute.upper_restricted)
	private String upperRestricted;
	@XmlPath("@" + CharacterAttribute.unit)
	private String unit;
	@XmlPath("@" + CharacterAttribute.type)
	private String type;
	@XmlPath("@" + CharacterAttribute.to_unit)
	private String toUnit;
	@XmlPath("@" + CharacterAttribute.to_inclusive)
	private String toInclusive;
	@XmlPath("@" + CharacterAttribute.to)
	private String to;
	@XmlPath("@" + CharacterAttribute.taxon_constraint)
	private String taxonConstraint;
	@XmlPath("@" + CharacterAttribute.parallelism_constraint)
	private String parallelismConstraint;
	@XmlPath("@" + CharacterAttribute.other_constraint)
	private String otherConstraint;
	@XmlPath("@" + CharacterAttribute.organ_constraint)
	private String organConstraint;
	@XmlPath("@" + CharacterAttribute.modifier)
	private String modifier;
	@XmlPath("@" + CharacterAttribute.in_brackets)
	private String inBrackets;
	@XmlPath("@" + CharacterAttribute.geographical_constraint)
	private String geographicalConstraint;
	@XmlPath("@" + CharacterAttribute.from_unit)
	private String fromUnit;
	@XmlPath("@" + CharacterAttribute.from_inclusive)
	private String fromInclusive;
	@XmlPath("@" + CharacterAttribute.from)
	private String from;
	@XmlPath("@" + CharacterAttribute.is_modifier)
	private String isModifier;
	
	@XmlTransient
	private BiologicalEntity structure;
	//@XmlTransient
	//private boolean isConstraintModifier; //red leaves, red is a modifier for leaves
	
	public Character() { }
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCharType() {
		return charType;
	}

	public void setCharType(String charType) {
		this.charType = charType;
	}

	public String getConstraint() {
		return constraint;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}

	public String getConstraintId() {
		return constraintId;
	}

	public void setConstraintId(String constraintId) {
		this.constraintId = constraintId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getProvenance() {
		return provenance;
	}

	public void setProvenance(String provenance) {
		this.provenance = provenance;
	}

	public String getOntologyId() {
		return ontologyId;
	}

	public void setOntologyId(String ontologyId) {
		this.ontologyId = ontologyId;
	}

	public String getUpperRestricted() {
		return upperRestricted;
	}
	
	public void setUpperRestricted(String upperRestricted) {
		this.upperRestricted = upperRestricted;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getToUnit() {
		return toUnit;
	}

	public void setToUnit(String toUnit) {
		this.toUnit = toUnit;
	}

	public String getToInclusive() {
		return toInclusive;
	}

	public void setToInclusive(String toInclusive) {
		this.toInclusive = toInclusive;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		if(to!=null && to.matches(".*?\\d-")) to = to.replaceFirst("-+$", "").trim(); //8- => 8
		this.to = to;
	}

	public String getTaxonConstraint() {
		return taxonConstraint;
	}

	public void setTaxonConstraint(String taxonConstraint) {
		this.taxonConstraint = taxonConstraint;
	}

	public String getParallelismConstraint() {
		return parallelismConstraint;
	}

	public void setParallelismConstraint(String parallelismConstraint) {
		this.parallelismConstraint = parallelismConstraint;
	}

	public String getOtherConstraint() {
		return otherConstraint;
	}

	public void setOtherConstraint(String otherConstraint) {
		this.otherConstraint = otherConstraint;
	}

	public String getOrganConstraint() {
		return organConstraint;
	}

	public void setOrganConstraint(String organConstraint) {
		this.organConstraint = organConstraint;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getInBrackets() {
		return inBrackets;
	}

	public void setInBrackets(String inBrackets) {
		this.inBrackets = inBrackets;
	}

	public String getGeographicalConstraint() {
		return geographicalConstraint;
	}

	public void setGeographicalConstraint(String geographicalConstraint) {
		this.geographicalConstraint = geographicalConstraint;
	}

	public String getFromUnit() {
		return fromUnit;
	}

	public void setFromUnit(String fromUnit) {
		this.fromUnit = fromUnit;
	}

	public String getFromInclusive() {
		return fromInclusive;
	}

	public void setFromInclusive(String fromInclusive) {
		this.fromInclusive = fromInclusive;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		if(from!=null && from.matches(".*?\\d-")) from = from.replaceFirst("-+$", "").trim(); //8- => 8 //should be moved to calling functions.
		this.from = from;
	}

	public void setStructure(BiologicalEntity structure) {
		this.structure = structure;
	}	
	
	public BiologicalEntity getStructure() {
		return this.structure;
	}
	
	public void setIsModifier(String isModifier){
		this.isModifier = isModifier;
	}
	
	public String getIsModifier(){
		return isModifier;
	}

	public void appendModifier(String modifier) {
		if(!modifier.contains(" ") && modifier.contains("-")) modifier = modifier.replaceAll("-+", " ");
		String newValue = "";
		if(this.modifier == null || this.modifier.isEmpty()) {
			newValue = modifier;
		} else {
			newValue = this.modifier + "; " + modifier;
		}
		this.modifier = newValue;
	}

	public void appendConstraint(String constraint) {
		String newValue = "";
		if(this.constraint == null || this.constraint.isEmpty()) {
			newValue = constraint;
		} else {
			newValue = this.constraint + "; " + constraint;
		}
		this.constraint = newValue;
	}

	/*@XmlTransient
	public boolean getIsConstraintModifier(){
		return this.isConstraintModifier;
	}
	
	@XmlTransient
	public void setIsConstraintModifier(boolean isConstraintModifier){
		this.isConstraintModifier = isConstraintModifier;
	}*/
	
	
	
	@Override
	public void removeElementRecursively(Element element) {
		return;
	}
	
	/**using characterName to update the name of this character**/
	public void updateCharacterName(String characterName){
		
		String newName = "";
		if(this.name.contains("atypical")) newName = "atypical_";
		if(this.name.contains("average")) newName = "average_";
		this.setName(newName+characterName);
	}
	
	@Override
	public Character clone() {
		Character character = new Character();
		character.setCharType(this.getCharType());
		character.setConstraint(this.getConstraint());
		character.setConstraintId(this.getConstraintId());
		character.setFrom(this.getFrom());
		character.setFromInclusive(this.getFromInclusive());
		character.setFromUnit(this.getFromUnit());
		character.setGeographicalConstraint(this.getGeographicalConstraint());
		character.setInBrackets(this.getInBrackets());
		character.setModifier(this.getModifier());
		character.setName(this.getName());
		character.setNotes(this.getNotes());
		character.setOntologyId(this.getOntologyId());
		character.setOrganConstraint(this.getOrganConstraint());
		character.setOtherConstraint(this.getOtherConstraint());
		character.setParallelismConstraint(this.getParallelismConstraint());
		character.setProvenance(this.getProvenance());
		character.setStructure(this.getStructure());
		character.setTaxonConstraint(this.getTaxonConstraint());
		character.setTo(this.getTo());
		character.setToInclusive(this.getToInclusive());
		character.setToUnit(this.getToUnit());
		character.setType(this.getType());
		character.setUnit(this.getUnit());
		character.setUpperRestricted(this.getUpperRestricted());
		character.setValue(this.getValue());
		character.setIsModifier(this.getIsModifier());
		return character;
	}

}
