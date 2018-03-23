package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes.RelationAttribute;
import edu.arizona.biosemantics.semanticmarkup.model.Element;
import edu.arizona.biosemantics.semanticmarkup.model.NamedElement;

@XmlRootElement
public class Relation extends NamedElement implements Cloneable {
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
	@XmlPath("@" + RelationAttribute.src)
	private String src;
	@XmlTransient
	private BiologicalEntity toStructure;
	@XmlTransient
	private BiologicalEntity fromStructure;

	/**
	 * Need to use ALL applicable set methods to create a relation object
	 */
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

	public void setToStructure(BiologicalEntity structure) {
		this.toStructure = structure;

	}

	public void setFromStructure(BiologicalEntity structure) {
		this.fromStructure = structure;
	}

	@XmlTransient
	public BiologicalEntity getToStructure() {
		return toStructure;
	}

	@XmlTransient
	public BiologicalEntity getFromStructure() {
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
		return;
	}

	@Override
	public Relation clone(){
		Relation relation = new Relation();
		relation.setAlterName(alterName);
		relation.setFrom(from);
		relation.setFromStructure(fromStructure);
		relation.setGeographicalConstraint(geographicalConstraint);
		relation.setId(id);
		relation.setInBrackets(inBrackets);
		relation.setModifier(modifier);
		relation.setName(name);
		relation.setNegation(negation);
		relation.setNotes(notes);
		relation.setOntologyId(ontologyId);
		relation.setOrganConstraint(organConstraint);
		relation.setParallelismConstraint(parallelismConstraint);
		relation.setProvenance(provenance);
		relation.setTaxonConstraint(taxonConstraint);
		relation.setTo(to);
		relation.setToStructure(toStructure);
		relation.setSrc(src);
		return relation;
	}
}
