//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.11 at 02:48:30 PM MST 
//


package semanticMarkup.io.input.lib.taxonx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for nomenclature complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nomenclature">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="author" type="{http://www.taxonx.org/schema/v1}name"/>
 *         &lt;element name="citation" type="{http://www.taxonx.org/schema/v1}citation"/>
 *         &lt;element name="figures" type="{http://www.taxonx.org/schema/v1}figures"/>
 *         &lt;element name="name" type="{http://www.taxonx.org/schema/v1}name"/>
 *         &lt;element name="bibref" type="{http://www.taxonx.org/schema/v1}bibref"/>
 *         &lt;element name="pb" type="{http://www.taxonx.org/schema/v1}pb"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="synonomy">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence maxOccurs="unbounded">
 *                   &lt;element name="name" type="{http://www.taxonx.org/schema/v1}name"/>
 *                   &lt;element name="author" type="{http://www.taxonx.org/schema/v1}name" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="year" type="{http://www.taxonx.org/schema/v1}year" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="citation" type="{http://www.taxonx.org/schema/v1}citation" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="note" type="{http://www.taxonx.org/schema/v1}note" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="type" type="{http://www.taxonx.org/schema/v1}type"/>
 *         &lt;element name="type_loc" type="{http://www.taxonx.org/schema/v1}type_loc"/>
 *         &lt;element name="xmldata" type="{http://www.taxonx.org/schema/v1}xmldata"/>
 *         &lt;element name="year" type="{http://www.taxonx.org/schema/v1}year"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.taxonx.org/schema/v1}standardAttrs"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nomenclature", namespace = "http://www.taxonx.org/schema/v1", propOrder = {
    "content"
})
public class Nomenclature {

    @XmlElementRefs({
        @XmlElementRef(name = "type", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "synonomy", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "figures", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "pb", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "name", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "author", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "year", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "type_loc", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "status", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "bibref", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "citation", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "xmldata", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class, required = false)
    })
    @XmlMixed
    protected List<Serializable> content;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "n")
    protected String n;

    /**
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Nomenclature.Synonomy }{@code >}
     * {@link JAXBElement }{@code <}{@link Type }{@code >}
     * {@link JAXBElement }{@code <}{@link Figures }{@code >}
     * {@link JAXBElement }{@code <}{@link Pb }{@code >}
     * {@link JAXBElement }{@code <}{@link Name }{@code >}
     * {@link JAXBElement }{@code <}{@link Name }{@code >}
     * {@link JAXBElement }{@code <}{@link TypeLoc }{@code >}
     * {@link JAXBElement }{@code <}{@link Year }{@code >}
     * {@link JAXBElement }{@code <}{@link Bibref }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link String }
     * {@link JAXBElement }{@code <}{@link Citation }{@code >}
     * {@link JAXBElement }{@code <}{@link Xmldata }{@code >}
     * 
     * 
     */
    public List<Serializable> getContent() {
        if (content == null) {
            content = new ArrayList<Serializable>();
        }
        return this.content;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the n property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getN() {
        return n;
    }

    /**
     * Sets the value of the n property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setN(String value) {
        this.n = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence maxOccurs="unbounded">
     *         &lt;element name="name" type="{http://www.taxonx.org/schema/v1}name"/>
     *         &lt;element name="author" type="{http://www.taxonx.org/schema/v1}name" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="year" type="{http://www.taxonx.org/schema/v1}year" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="citation" type="{http://www.taxonx.org/schema/v1}citation" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="note" type="{http://www.taxonx.org/schema/v1}note" maxOccurs="unbounded" minOccurs="0"/>
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
        "nameAndAuthorAndYear"
    })
    public static class Synonomy {

        @XmlElementRefs({
            @XmlElementRef(name = "name", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class),
            @XmlElementRef(name = "citation", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class),
            @XmlElementRef(name = "year", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class),
            @XmlElementRef(name = "note", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class),
            @XmlElementRef(name = "author", namespace = "http://www.taxonx.org/schema/v1", type = JAXBElement.class)
        })
        protected List<JAXBElement<?>> nameAndAuthorAndYear;

        /**
         * Gets the value of the nameAndAuthorAndYear property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the nameAndAuthorAndYear property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getNameAndAuthorAndYear().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link JAXBElement }{@code <}{@link Name }{@code >}
         * {@link JAXBElement }{@code <}{@link Citation }{@code >}
         * {@link JAXBElement }{@code <}{@link Year }{@code >}
         * {@link JAXBElement }{@code <}{@link Name }{@code >}
         * {@link JAXBElement }{@code <}{@link Note }{@code >}
         * 
         * 
         */
        public List<JAXBElement<?>> getNameAndAuthorAndYear() {
            if (nameAndAuthorAndYear == null) {
                nameAndAuthorAndYear = new ArrayList<JAXBElement<?>>();
            }
            return this.nameAndAuthorAndYear;
        }

    }

}
