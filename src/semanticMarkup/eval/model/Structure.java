package semanticMarkup.eval.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.persistence.oxm.annotations.XmlPath;


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
public class Structure extends Element {

	@XmlPath("@alter_name")
	private String alterName;
	@XmlPath("@constraint")
	private String constraint;
	@XmlPath("@constraintid")
	private String constraintId;
	@XmlPath("@geographical_constraint")
	private String geographicalConstraint;
	@XmlPath("@id")
	private String id;
	@XmlPath("@in_bracket")
	private String inBracket;
	@XmlPath("@in_brackets")
	private String inBrackets;
	@XmlPath("@parallelism_constraint")
	private String parallelismConstraint;
	@XmlPath("@taxon_constraint")
	private String taxonConstraint;
	@XmlPath("@ontologyid")
	private String ontologyId;
	@XmlPath("@provenance")
	private String provenance;
	@XmlPath("@notes")
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
	
	@Override
	public String toString() {
		return this.getName();
	}
}
