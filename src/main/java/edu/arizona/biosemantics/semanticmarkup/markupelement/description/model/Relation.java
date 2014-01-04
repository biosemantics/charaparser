package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;


import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes.RelationAttribute;
import edu.arizona.biosemantics.semanticmarkup.model.Element;
import edu.arizona.biosemantics.semanticmarkup.model.NamedElement;


/**
 *     <xs:complexType>
      <xs:attribute name="alter_name"/>
      <xs:attribute name="from" use="required" type="xs:NCName"/>
      <xs:attribute name="geographical_constraint"/>
      <xs:attribute name="id" use="required" type="xs:NCName"/>
      <xs:attribute name="in_brackets" type="xs:boolean"/>
      <xs:attribute name="modifier"/>
      <xs:attribute name="name" use="required"/>
      <xs:attribute name="negation" use="required" type="xs:boolean"/>
      <xs:attribute name="organ_constraint"/>
      <xs:attribute name="parallelism_constraint" type="xs:NCName"/>
      <xs:attribute name="taxon_constraint"/>
      <xs:attribute name="to" use="required" type="xs:NCName"/>
      <xs:attribute name="ontologyid" type="xs:string"/>
      <xs:attribute name="provenance" type="xs:string"/>
      <xs:attribute name="notes" type="xs:string"/>
    </xs:complexType>
 * @author rodenhausen
 *
 */
@XmlRootElement
public class Relation extends NamedElement {
	@XmlPath("@" + RelationAttribute.alter_name)
	private String alterName;
	@XmlPath("@" + RelationAttribute.from)
	private String from;
	@XmlPath("@" + RelationAttribute.geographical_constraint)
	private String geographicalConstraint;
	@XmlPath("@" + RelationAttribute.id)
	private String id;
	@XmlPath("@" + RelationAttribute.in_brackets)
	private String inBrackets;
	@XmlPath("@" + RelationAttribute.modifier)
	private String modifier;
	@XmlPath("@" + RelationAttribute.negation)
	private String negation;
	@XmlPath("@" + RelationAttribute.organ_constraint)
	private String organConstraint;
	@XmlPath("@" + RelationAttribute.parallelism_constraint)
	private String parallelismConstraint;
	@XmlPath("@" + RelationAttribute.taxon_constraint)
	private String taxonConstraint;
	@XmlPath("@" + RelationAttribute.to)
	private String to;
	@XmlPath("@" + RelationAttribute.ontologyid)
	private String ontologyId;
	@XmlPath("@" + RelationAttribute.provenance)
	private String provenance;
	@XmlPath("@" + RelationAttribute.notes)
	private String notes;
	private Structure toStructure;
	private Structure fromStructure;
	

	public Relation() { }

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getAlterName() {
		return alterName;
	}

	public void setAlterName(String alterName) {
		this.alterName = alterName;
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

	public String getInBrackets() {
		return inBrackets;
	}

	public void setInBrackets(String inBrackets) {
		this.inBrackets = inBrackets;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getNegation() {
		return negation;
	}

	public void setNegation(String negation) {
		this.negation = negation;
	}

	public String getOrganConstraint() {
		return organConstraint;
	}

	public void setOrganConstraint(String organConstraint) {
		this.organConstraint = organConstraint;
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

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
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

	public void setToStructure(Structure structure) {
		this.toStructure = structure;
		
	}

	public void setFromStructure(Structure structure) {
		this.fromStructure = structure;
	}

	public Structure getToStructure() {
		return toStructure;
	}

	public Structure getFromStructure() {
		return fromStructure;
	}
	
	public void appendModifier(String modifier) {
		String newValue = "";
		if(this.modifier == null || this.modifier.isEmpty()) {
			newValue = modifier;
		} else {
			newValue = this.modifier + "; " + modifier;
		}
		this.modifier = newValue;
	}

	@Override
	public void removeElementRecursively(Element element) {
		return;
	}
}
