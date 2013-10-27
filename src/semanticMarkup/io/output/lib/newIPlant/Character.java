//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.27 at 10:05:22 AM MST 
//


package semanticMarkup.io.output.lib.newIPlant;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="char_type" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="constraint" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="constraintid" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="from" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="from_inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="from_unit" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="geographical_constraint" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="in_brackets" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="modifier" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="organ_constraint" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="other_constraint" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="parallelism_constraint" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="taxon_constraint" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="to" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="to_inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="to_unit" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="unit" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="upper_restricted" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ontologyid" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="provenance" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="notes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "character")
public class Character {

    @XmlAttribute(name = "char_type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String charType;
    @XmlAttribute(name = "constraint")
    @XmlSchemaType(name = "anySimpleType")
    protected String constraint;
    @XmlAttribute(name = "constraintid")
    @XmlSchemaType(name = "anySimpleType")
    protected String constraintid;
    @XmlAttribute(name = "from")
    @XmlSchemaType(name = "anySimpleType")
    protected String from;
    @XmlAttribute(name = "from_inclusive")
    protected Boolean fromInclusive;
    @XmlAttribute(name = "from_unit")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String fromUnit;
    @XmlAttribute(name = "geographical_constraint")
    @XmlSchemaType(name = "anySimpleType")
    protected String geographicalConstraint;
    @XmlAttribute(name = "in_brackets")
    protected Boolean inBrackets;
    @XmlAttribute(name = "modifier")
    @XmlSchemaType(name = "anySimpleType")
    protected String modifier;
    @XmlAttribute(name = "name")
    @XmlSchemaType(name = "anySimpleType")
    protected String name;
    @XmlAttribute(name = "organ_constraint")
    @XmlSchemaType(name = "anySimpleType")
    protected String organConstraint;
    @XmlAttribute(name = "other_constraint")
    @XmlSchemaType(name = "anySimpleType")
    protected String otherConstraint;
    @XmlAttribute(name = "parallelism_constraint")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String parallelismConstraint;
    @XmlAttribute(name = "taxon_constraint")
    @XmlSchemaType(name = "anySimpleType")
    protected String taxonConstraint;
    @XmlAttribute(name = "to")
    @XmlSchemaType(name = "anySimpleType")
    protected String to;
    @XmlAttribute(name = "to_inclusive")
    protected Boolean toInclusive;
    @XmlAttribute(name = "to_unit")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String toUnit;
    @XmlAttribute(name = "type")
    @XmlSchemaType(name = "anySimpleType")
    protected String type;
    @XmlAttribute(name = "unit")
    @XmlSchemaType(name = "anySimpleType")
    protected String unit;
    @XmlAttribute(name = "upper_restricted")
    protected Boolean upperRestricted;
    @XmlAttribute(name = "value")
    @XmlSchemaType(name = "anySimpleType")
    protected String value;
    @XmlAttribute(name = "ontologyid")
    protected String ontologyid;
    @XmlAttribute(name = "provenance")
    protected String provenance;
    @XmlAttribute(name = "notes")
    protected String notes;

    /**
     * Gets the value of the charType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharType() {
        return charType;
    }

    /**
     * Sets the value of the charType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharType(String value) {
        this.charType = value;
    }

    /**
     * Gets the value of the constraint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConstraint() {
        return constraint;
    }

    /**
     * Sets the value of the constraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConstraint(String value) {
        this.constraint = value;
    }

    /**
     * Gets the value of the constraintid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConstraintid() {
        return constraintid;
    }

    /**
     * Sets the value of the constraintid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConstraintid(String value) {
        this.constraintid = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrom(String value) {
        this.from = value;
    }

    /**
     * Gets the value of the fromInclusive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFromInclusive() {
        return fromInclusive;
    }

    /**
     * Sets the value of the fromInclusive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFromInclusive(Boolean value) {
        this.fromInclusive = value;
    }

    /**
     * Gets the value of the fromUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFromUnit() {
        return fromUnit;
    }

    /**
     * Sets the value of the fromUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromUnit(String value) {
        this.fromUnit = value;
    }

    /**
     * Gets the value of the geographicalConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeographicalConstraint() {
        return geographicalConstraint;
    }

    /**
     * Sets the value of the geographicalConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeographicalConstraint(String value) {
        this.geographicalConstraint = value;
    }

    /**
     * Gets the value of the inBrackets property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInBrackets() {
        return inBrackets;
    }

    /**
     * Sets the value of the inBrackets property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInBrackets(Boolean value) {
        this.inBrackets = value;
    }

    /**
     * Gets the value of the modifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * Sets the value of the modifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModifier(String value) {
        this.modifier = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the organConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganConstraint() {
        return organConstraint;
    }

    /**
     * Sets the value of the organConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganConstraint(String value) {
        this.organConstraint = value;
    }

    /**
     * Gets the value of the otherConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOtherConstraint() {
        return otherConstraint;
    }

    /**
     * Sets the value of the otherConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOtherConstraint(String value) {
        this.otherConstraint = value;
    }

    /**
     * Gets the value of the parallelismConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParallelismConstraint() {
        return parallelismConstraint;
    }

    /**
     * Sets the value of the parallelismConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParallelismConstraint(String value) {
        this.parallelismConstraint = value;
    }

    /**
     * Gets the value of the taxonConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxonConstraint() {
        return taxonConstraint;
    }

    /**
     * Sets the value of the taxonConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxonConstraint(String value) {
        this.taxonConstraint = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTo(String value) {
        this.to = value;
    }

    /**
     * Gets the value of the toInclusive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isToInclusive() {
        return toInclusive;
    }

    /**
     * Sets the value of the toInclusive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setToInclusive(Boolean value) {
        this.toInclusive = value;
    }

    /**
     * Gets the value of the toUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToUnit() {
        return toUnit;
    }

    /**
     * Sets the value of the toUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToUnit(String value) {
        this.toUnit = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     * Gets the value of the upperRestricted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUpperRestricted() {
        return upperRestricted;
    }

    /**
     * Sets the value of the upperRestricted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUpperRestricted(Boolean value) {
        this.upperRestricted = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the ontologyid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOntologyid() {
        return ontologyid;
    }

    /**
     * Sets the value of the ontologyid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOntologyid(String value) {
        this.ontologyid = value;
    }

    /**
     * Gets the value of the provenance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProvenance() {
        return provenance;
    }

    /**
     * Sets the value of the provenance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProvenance(String value) {
        this.provenance = value;
    }

    /**
     * Gets the value of the notes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotes(String value) {
        this.notes = value;
    }

}
