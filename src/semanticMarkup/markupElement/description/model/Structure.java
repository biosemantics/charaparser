package semanticMarkup.markupElement.description.model;


import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import semanticMarkup.model.Element;
import semanticMarkup.model.NamedElement;
import semanticMarkup.model.description.attributes.StructureAttribute;


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
<xs:attribute name="in_bracket" type="xs:boolean"/>
<xs:attribute name="in_brackets" type="xs:boolean"/>
<xs:attribute name="name" use="required"/>
<xs:attribute name="parallelism_constraint" type="xs:NCName"/>
<xs:attribute name="taxon_constraint"/>
<xs:attribute name="ontologyid" type="xs:string"/>
<xs:attribute name="provenance" type="xs:string"/>
<xs:attribute name="notes" type="xs:string"/>
</xs:complexType>
 * @author rodenhausen
 *
 */
@XmlRootElement
public class Structure extends NamedElement {

	@XmlPath("@" + StructureAttribute.alter_name)
	private String alterName;
	@XmlPath("@" + StructureAttribute.constraint)
	private String constraint;
	@XmlPath("@" + StructureAttribute.constraintid)
	private String constraintId;
	@XmlPath("@" + StructureAttribute.geographical_constraint)
	private String geographicalConstraint;
	@XmlPath("@" + StructureAttribute.id)
	private String id;
	@XmlPath("@" + StructureAttribute.in_bracket)
	private String inBracket;
	@XmlPath("@" + StructureAttribute.in_brackets)
	private String inBrackets;
	@XmlPath("@" + StructureAttribute.parallelism_constraint)
	private String parallelismConstraint;
	@XmlPath("@" + StructureAttribute.taxon_constraint)
	private String taxonConstraint;
	@XmlPath("@" + StructureAttribute.ontologyid)
	private String ontologyId;
	@XmlPath("@" + StructureAttribute.provenance)
	private String provenance;
	@XmlPath("@" + StructureAttribute.notes)
	private String notes;	
	@XmlPath("character")
	private LinkedHashSet<Character> characters = new LinkedHashSet<Character>();
	private LinkedHashSet<Relation> fromRelations = new LinkedHashSet<Relation>();
	private LinkedHashSet<Relation> toRelations = new LinkedHashSet<Relation>();

	public Structure() { }


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

	public String getInBracket() {
		return inBracket;
	}

	public void setInBracket(String inBracket) {
		this.inBracket = inBracket;
	}

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
		} else {
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
		} else {
			newValue = this.taxonConstraint + "; " + taxonConstraint;
		}
		this.taxonConstraint = newValue;
	}


	public void appendProvenance(String provenance) {
		String newValue = "";
		if(this.provenance == null || this.provenance.isEmpty()) {
			newValue = provenance;
		} else {
			newValue = this.provenance + "; " + provenance;
		}
		this.provenance = newValue;
	}


	public void appendParallelismConstraint(String parallelismConstraint) {
		String newValue = "";
		if(this.parallelismConstraint == null || this.parallelismConstraint.isEmpty()) {
			newValue = parallelismConstraint;
		} else {
			newValue = this.parallelismConstraint + "; " + parallelismConstraint;
		}
		this.parallelismConstraint = newValue;
	}


	public void appendOntologyId(String ontologyId) {
		String newValue = "";
		if(this.ontologyId == null || this.ontologyId.isEmpty()) {
			newValue = ontologyId;
		} else {
			newValue = this.ontologyId + "; " + ontologyId;
		}
		this.ontologyId = newValue;
	}


	public void appendNotes(String notes) {
		String newValue = "";
		if(this.notes == null || this.notes.isEmpty()) {
			newValue = notes;
		} else {
			newValue = this.notes + "; " + notes;
		}
		this.notes = newValue;
	}


	public void appendInBrackets(String inBrackets) {
		String newValue = "";
		if(this.inBrackets == null || this.inBrackets.isEmpty()) {
			newValue = inBrackets;
		} else {
			newValue = this.inBrackets + "; " + inBrackets;
		}
		this.inBrackets = newValue;
	}


	public void appendInBracket(String inBracket) {
		String newValue = "";
		if(this.inBracket == null || this.inBracket.isEmpty()) {
			newValue = inBracket;
		} else {
			newValue = this.inBracket + "; " + inBracket;
		}
		this.inBracket = newValue;
	}


	public void appendId(String id) {
		String newValue = "";
		if(this.id == null || this.id.isEmpty()) {
			newValue = id;
		} else {
			newValue = this.id + "; " + id;
		}
		this.id = newValue;
	}


	public void appendGeographicalConstraint(String geographicalConstraint) {
		String newValue = "";
		if(this.geographicalConstraint == null || this.geographicalConstraint.isEmpty()) {
			newValue = geographicalConstraint;
		} else {
			newValue = this.geographicalConstraint + "; " + geographicalConstraint;
		}
		this.geographicalConstraint = newValue;
	}


	public void appendConstraintId(String constraintId) {
		String newValue = "";
		if(this.constraintId == null || this.constraintId.isEmpty()) {
			newValue = constraintId;
		} else {
			newValue = this.constraintId + "; " + constraintId;
		}
		this.constraintId = newValue;
	}


	public void appendAlterName(String alterName) {
		String newValue = "";
		if(this.alterName == null || this.alterName.isEmpty()) {
			newValue = alterName;
		} else {
			newValue = this.alterName + "; " + alterName;
		}
		this.alterName = newValue;
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
}
