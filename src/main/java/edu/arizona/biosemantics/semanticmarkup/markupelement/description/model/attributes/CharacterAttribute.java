package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes;

public class CharacterAttribute {

	/*
	 *       <xs:attribute name="char_type" type="xs:NCName"/>
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
	 */
	
	public static final String char_type = "char_type";
	public static final String constraint = "constraint";
	public static final String constraintid = "constraintid";
	public static final String from = "from";
	public static final String from_inclusive = "from_inclusive";
	public static final String from_unit = "from_unit";
	public static final String from_modifier = "from_modifier"; //elevation
	public static final String geographical_constraint = "geographical_constraint";
	public static final String in_brackets = "in_brackets";
	public static final String modifier = "modifier";
	public static final String name = "name";
	public static final String organ_constraint = "organ_constraint";
	public static final String other_constraint = "other_constraint";
	public static final String parallelism_constraint = "parallelism_constraint";
	public static final String taxon_constraint = "taxon_constraint";
	public static final String to = "to";
	public static final String to_inclusive = "to_inclusive";
	public static final String to_unit = "to_unit";
	public static final String to_modifier = "to_modifier"; //elevation
	public static final String type = "type";
	public static final String unit = "unit";
	public static final String upper_restricted = "upper_restricted";
	public static final String value = "value";
	public static final String ontologyid = "ontologyid";
	public static final String provenance = "provenance";
	public static final String notes = "notes";
	public static final String is_modifier="is_modifier";
	public static final String establishment_means = "establishment_means";
	public static final String src = "src"; // to hold src statement id. Information related to one BiologicalEntity could come from 2 or more sentences/statements.
	
}
