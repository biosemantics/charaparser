//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.11 at 02:48:30 PM MST 
//


package semanticMarkup.io.input.lib.taxonx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * The not equals (!=) comparator type.  Can take any type of searchableData.
 * 
 * <p>Java class for notEquals complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="notEquals">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://digir.net/schema/protocol/2003/1.0}COP">
 *       &lt;choice>
 *         &lt;element ref="{http://digir.net/schema/protocol/2003/1.0}searchableData"/>
 *         &lt;element ref="{http://digir.net/schema/protocol/2003/1.0}searchableReturnableData"/>
 *       &lt;/choice>
 *       &lt;attribute name="symbol" type="{http://www.w3.org/2001/XMLSchema}string" fixed="!=" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "notEquals", namespace = "http://digir.net/schema/protocol/2003/1.0")
public class NotEquals
    extends COP
{


}
