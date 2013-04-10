//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.09 at 03:48:51 PM MST 
//


package semanticMarkup.io.input.lib.xml;

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
 *         &lt;element ref="{}statement_id"/>
 *         &lt;element ref="{}statement"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{}determination"/>
 *           &lt;element ref="{}next_statement_id"/>
 *         &lt;/choice>
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
    "statementId",
    "statement",
    "determination",
    "nextStatementId"
})
@XmlRootElement(name = "key_statement")
public class KeyStatement {

    @XmlElement(name = "statement_id", required = true)
    protected String statementId;
    @XmlElement(required = true)
    protected String statement;
    protected String determination;
    @XmlElement(name = "next_statement_id")
    protected String nextStatementId;

    /**
     * Gets the value of the statementId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatementId() {
        return statementId;
    }

    /**
     * Sets the value of the statementId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatementId(String value) {
        this.statementId = value;
    }

    /**
     * Gets the value of the statement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatement() {
        return statement;
    }

    /**
     * Sets the value of the statement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatement(String value) {
        this.statement = value;
    }

    /**
     * Gets the value of the determination property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDetermination() {
        return determination;
    }

    /**
     * Sets the value of the determination property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDetermination(String value) {
        this.determination = value;
    }

    /**
     * Gets the value of the nextStatementId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNextStatementId() {
        return nextStatementId;
    }

    /**
     * Sets the value of the nextStatementId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNextStatementId(String value) {
        this.nextStatementId = value;
    }

}
