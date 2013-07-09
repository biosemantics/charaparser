//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.11 at 02:48:30 PM MST 
//


package semanticMarkup.io.input.lib.taxonx;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for ref_group complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ref_group">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="head" type="{http://www.taxonx.org/schema/v1}head" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="bibref" type="{http://www.taxonx.org/schema/v1}bibref"/>
 *           &lt;element name="ref_group" type="{http://www.taxonx.org/schema/v1}ref_group"/>
 *           &lt;element name="figure" type="{http://www.taxonx.org/schema/v1}figure"/>
 *           &lt;element name="note" type="{http://www.taxonx.org/schema/v1}note"/>
 *           &lt;element name="pb" type="{http://www.taxonx.org/schema/v1}pb"/>
 *           &lt;element name="p" type="{http://www.taxonx.org/schema/v1}p"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.taxonx.org/schema/v1}standardAttrs"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ref_group", namespace = "http://www.taxonx.org/schema/v1", propOrder = {
    "head",
    "bibrefOrRefGroupOrFigure"
})
public class RefGroup {

    protected Head head;
    @XmlElements({
        @XmlElement(name = "bibref", type = Bibref.class),
        @XmlElement(name = "ref_group", type = RefGroup.class),
        @XmlElement(name = "figure", type = Figure.class),
        @XmlElement(name = "note", type = Note.class),
        @XmlElement(name = "pb", type = Pb.class),
        @XmlElement(name = "p", type = P.class)
    })
    protected List<Object> bibrefOrRefGroupOrFigure;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "n")
    protected String n;

    /**
     * Gets the value of the head property.
     * 
     * @return
     *     possible object is
     *     {@link Head }
     *     
     */
    public Head getHead() {
        return head;
    }

    /**
     * Sets the value of the head property.
     * 
     * @param value
     *     allowed object is
     *     {@link Head }
     *     
     */
    public void setHead(Head value) {
        this.head = value;
    }

    /**
     * Gets the value of the bibrefOrRefGroupOrFigure property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bibrefOrRefGroupOrFigure property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBibrefOrRefGroupOrFigure().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Bibref }
     * {@link RefGroup }
     * {@link Figure }
     * {@link Note }
     * {@link Pb }
     * {@link P }
     * 
     * 
     */
    public List<Object> getBibrefOrRefGroupOrFigure() {
        if (bibrefOrRefGroupOrFigure == null) {
            bibrefOrRefGroupOrFigure = new ArrayList<Object>();
        }
        return this.bibrefOrRefGroupOrFigure;
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

}
