//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.09 at 03:50:25 PM MST 
//


package semanticMarkup.io.output.lib.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{}reference_discussion" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}reference" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="heading" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "referenceDiscussion",
    "reference"
})
@XmlRootElement(name = "references")
public class References {

    @XmlElement(name = "reference_discussion")
    protected List<String> referenceDiscussion;
    @XmlElement(required = true)
    protected List<String> reference;
    @XmlAttribute(name = "heading")
    @XmlSchemaType(name = "anySimpleType")
    protected String heading;

    /**
     * Gets the value of the referenceDiscussion property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceDiscussion property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceDiscussion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getReferenceDiscussion() {
        if (referenceDiscussion == null) {
            referenceDiscussion = new ArrayList<String>();
        }
        return this.referenceDiscussion;
    }

    /**
     * Gets the value of the reference property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reference property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getReference() {
        if (reference == null) {
            reference = new ArrayList<String>();
        }
        return this.reference;
    }

    /**
     * Gets the value of the heading property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeading() {
        return heading;
    }

    /**
     * Sets the value of the heading property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeading(String value) {
        this.heading = value;
    }

}
