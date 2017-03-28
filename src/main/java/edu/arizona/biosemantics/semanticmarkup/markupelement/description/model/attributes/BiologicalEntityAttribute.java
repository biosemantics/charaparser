package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes;

public class BiologicalEntityAttribute {

	/*<xs:sequence>
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
  <xs:attribute name="notes" type="xs:string"/>*/
	
	public static final String alter_name = "alter_name";
	public static final String constraint = "constraint";
	public static final String constraintid = "constraintid";
	public static final String geographical_constraint = "geographical_constraint";
	public static final String id = "id";
	public static final String in_bracket = "in_bracket";
	public static final String in_brackets = "in_brackets";
	public static final String name = "name";
	public static final String parallelism_constraint = "parallelism_constraint";
	public static final String taxon_constraint = "taxon_constraint";
	public static final String ontologyid = "ontologyid";
	public static final String provenance = "provenance";
	public static final String notes = "notes";
	
	public static final String constraintParentOrgan = "constraintParentOrgan";
	public static final String constraintType = "constraintType";
	public static final String name_original = "name_original";
	public static final String constraint_original = "constraint_original";
	public static final String type = "type";
	public static final String src = "src"; // to hold src statement ids. Information related to one BiologicalEntity could come from 2 or more sentences/statements.
}
