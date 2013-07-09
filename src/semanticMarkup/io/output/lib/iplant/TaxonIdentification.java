//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.10 at 02:12:31 PM MST 
//


package semanticMarkup.io.output.lib.iplant;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element ref="{}taxon_name"/>
 *         &lt;element ref="{}other_info" maxOccurs="unbounded"/>
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
    "taxonName",
    "otherInfo"
})
@XmlRootElement(name = "TaxonIdentification")
public class TaxonIdentification {

    @XmlElement(name = "taxon_name", required = true)
    protected String taxonName;
    @XmlElement(name = "other_info", required = true)
    protected List<String> otherInfo;

    /**
     * Gets the value of the taxonName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxonName() {
        return taxonName;
    }

    /**
     * Sets the value of the taxonName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxonName(String value) {
        this.taxonName = value;
    }

    /**
     * Gets the value of the otherInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOtherInfo() {
        if (otherInfo == null) {
            otherInfo = new ArrayList<String>();
        }
        return this.otherInfo;
    }

}
