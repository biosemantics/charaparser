//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.26 at 06:10:52 PM MST 
//


package semanticMarkup.io.input.lib.newIPlant;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for processed_by complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="processed_by">
 *   &lt;complexContent>
 *     &lt;extension base="{}processed_by">
 *       &lt;redefine>
 *         &lt;complexType name="processed_by">
 *           &lt;complexContent>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *               &lt;choice maxOccurs="unbounded" minOccurs="0">
 *                 &lt;element name="processor" type="{}processor"/>
 *               &lt;/choice>
 *             &lt;/restriction>
 *           &lt;/complexContent>
 *         &lt;/complexType>
 *       &lt;/redefine>
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="charaparser" type="{}charaparser"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processed_by", propOrder = {
    "charaparser"
})
public class ProcessedBy
    extends OriginalProcessedBy
{

    protected List<Charaparser> charaparser;

    /**
     * Gets the value of the charaparser property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the charaparser property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCharaparser().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Charaparser }
     * 
     * 
     */
    public List<Charaparser> getCharaparser() {
        if (charaparser == null) {
            charaparser = new ArrayList<Charaparser>();
        }
        return this.charaparser;
    }

}
