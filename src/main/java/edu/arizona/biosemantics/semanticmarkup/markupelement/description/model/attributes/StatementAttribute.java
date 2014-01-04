package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes;


public class StatementAttribute {

	/*<xs:complexType>
    <xs:sequence>
      <xs:element ref="text"/>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:element ref="relation"/>
        <xs:element ref="structure"/>
      </xs:choice>
    </xs:sequence>
    <xs:attribute name="id" use="required"/>
    <xs:attribute name="provenance" type="xs:string"/>
    <xs:attribute name="notes" type="xs:string"/>
  </xs:complexType>*/
	
	public static final String text = "text";
	public static final String id = "id";
	public static final String source = "source";
	public static final String provenance = "provenance";
	public static final String notes = "notes";
}
