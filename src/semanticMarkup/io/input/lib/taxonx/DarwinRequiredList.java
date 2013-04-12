//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.11 at 02:48:30 PM MST 
//


package semanticMarkup.io.input.lib.taxonx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://digir.net/schema/conceptual/darwin/2003/1.0}DateLastModified"/>
 *         &lt;element ref="{http://digir.net/schema/conceptual/darwin/2003/1.0}InstitutionCode"/>
 *         &lt;element ref="{http://digir.net/schema/conceptual/darwin/2003/1.0}CollectionCode"/>
 *         &lt;element ref="{http://digir.net/schema/conceptual/darwin/2003/1.0}CatalogNumberText"/>
 *         &lt;element ref="{http://digir.net/schema/conceptual/darwin/2003/1.0}ScientificName"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dateLastModified",
    "institutionCode",
    "collectionCode",
    "catalogNumberText",
    "scientificName"
})
public class DarwinRequiredList {

    @XmlElement(name = "DateLastModified", namespace = "http://digir.net/schema/conceptual/darwin/2003/1.0", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateLastModified;
    @XmlElement(name = "InstitutionCode", namespace = "http://digir.net/schema/conceptual/darwin/2003/1.0", required = true, nillable = true)
    protected String institutionCode;
    @XmlElement(name = "CollectionCode", namespace = "http://digir.net/schema/conceptual/darwin/2003/1.0", required = true, nillable = true)
    protected String collectionCode;
    @XmlElement(name = "CatalogNumberText", namespace = "http://digir.net/schema/conceptual/darwin/2003/1.0", required = true, nillable = true)
    protected String catalogNumberText;
    @XmlElement(name = "ScientificName", namespace = "http://digir.net/schema/conceptual/darwin/2003/1.0", required = true, nillable = true)
    protected String scientificName;

    /**
     * Gets the value of the dateLastModified property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateLastModified() {
        return dateLastModified;
    }

    /**
     * Sets the value of the dateLastModified property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateLastModified(XMLGregorianCalendar value) {
        this.dateLastModified = value;
    }

    /**
     * Gets the value of the institutionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstitutionCode() {
        return institutionCode;
    }

    /**
     * Sets the value of the institutionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstitutionCode(String value) {
        this.institutionCode = value;
    }

    /**
     * Gets the value of the collectionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionCode() {
        return collectionCode;
    }

    /**
     * Sets the value of the collectionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionCode(String value) {
        this.collectionCode = value;
    }

    /**
     * Gets the value of the catalogNumberText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCatalogNumberText() {
        return catalogNumberText;
    }

    /**
     * Sets the value of the catalogNumberText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCatalogNumberText(String value) {
        this.catalogNumberText = value;
    }

    /**
     * Gets the value of the scientificName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScientificName() {
        return scientificName;
    }

    /**
     * Sets the value of the scientificName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScientificName(String value) {
        this.scientificName = value;
    }

}
