package semanticMarkup.eval.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

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
    </xs:complexType>
 * @author rodenhausen
 *
 */
public class Character extends Element {

	@XmlPath("@value")
	private String value;
	@XmlPath("@char_type")
	private String charType;
	@XmlPath("@constraint")
	private String constraint;
	@XmlPath("@constraintid")
	private String constraintId;
	@XmlPath("@notes")
	private String notes;
	@XmlPath("@provenance")
	private String provenance;
	@XmlPath("@ontologyid")
	private String ontologyId;
	@XmlPath("@upper_restricted")
	private String upperRestricted;
	@XmlPath("@unit")
	private String unit;
	@XmlPath("@type")
	private String type;
	@XmlPath("@to_unit")
	private String toUnit;
	@XmlPath("@to_inclusive")
	private String toInclusive;
	@XmlPath("@to")
	private String to;
	@XmlPath("@taxon_constraint")
	private String taxonConstraint;
	@XmlPath("@parallelism_constraint")
	private String parallelismConstraint;
	@XmlPath("@other_constraint")
	private String otherConstraint;
	@XmlPath("@organ_constraint")
	private String organConstraint;
	@XmlPath("@modifier")
	private String modifier;
	@XmlPath("@in_brackets")
	private String inBrackets;
	@XmlPath("@geographical_constraint")
	private String geographicalConstraint;
	@XmlPath("@from_unit")
	private String fromUnit;
	@XmlPath("@from_inclusive")
	private String fromInclusive;
	@XmlPath("@from")
	private String from;
	private Structure structure;
	
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
		this.from = from;
	}

	public void setStructure(Structure structure) {
		this.structure = structure;
	}	
	
	public Structure getStructure() {
		return this.structure;
	}
	
	@Override
	public String toString() {
		return this.getName() + " = " + this.getValue();
	}
}
