package edu.arizona.biosemantics.semanticmarkup.log;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * StringifyInjectionAspect specifies IPrintables and adds them a toString method
 * @author rodenhausen
 */
public aspect StringifyInjectionAspect {
	
	@XmlTransient
	@Transient
	@JsonIgnore
	private IPrintable IPrintable.thisObject;
	
	/**
	 * toString method is defined for IPrintables
	 */
	public String IPrintable.toString() {
		return ObjectStringifier.getInstance().stringify(thisObject);
	}
	
	/**
	 * Pointcut specification for object construction of an IPrintable
	 * @param object
	 */
	pointcut objectConstruction(IPrintable object) : 
		initialization(IPrintable+.new(..)) && this(object);

	/**
	 * Advice for after object construction lets an IPrintable have a reference of his own for use in toString()
	 * @param object
	 */
	after(IPrintable object) : objectConstruction(object) {
		object.thisObject = object;
	}
	
	/**
	 * IPrintables are specified
	 */
	
	declare parents : edu.arizona.biosemantics.semanticmarkup.* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.config..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.eval..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.model..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.gui..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.io..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.know..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.ling..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.markup..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.markupelement..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.run..* implements IPrintable;
	
	/*description.model..* implements IPrintable;
	declare parents : semanticmarkup.markupelement.description.eval.model..* implements IPrintable;
	declare parents : semanticmarkup.markupelement.habitat.model..* implements IPrintable;
	
	declare parents : semanticmarkup.* implements IPrintable;
	
	declare parents : semanticmarkup.model..* implements IPrintable;
	
	declare parents : semanticmarkup.eval..* implements IPrintable;
	declare parents : semanticmarkup.config.* implements IPrintable;
	
	// JAXB container classes may not implement an interface, JAXB can't handle interface, will throw exception
	declare parents : semanticmarkup.io..* implements IPrintable;
	declare parents : semanticmarkup.io.output.* implements IPrintable;
	declare parents : semanticmarkup.io.output.lib.* implements IPrintable;
	declare parents : semanticmarkup.io.output.lib.serial.* implements IPrintable;
	//declare parents : semanticmarkup.io.input.* implements IPrintable;
	//declare parents : semanticmarkup.io.output..* implements IPrintable; 	
	//declare parents : semanticmarkup.io.output.lib.* implements IPrintable; 	
	//declare parents : semanticmarkup.io.output.lib.xml.* implements IPrintable; 	
	
	declare parents : semanticmarkup.know..* implements IPrintable;
	
	//chunkcollector and abstractparsetree, token, markedToken, posedToken printing is better done differently
	//declare parents : semanticmarkup.ling..* implements IPrintable;	
	//declare parents : semanticmarkup.ling.* implements IPrintable;
	declare parents : semanticmarkup.ling.extract..* implements IPrintable;
	declare parents : semanticmarkup.markupelement.description.ling.learn..* implements IPrintable;
	//declare parents : semanticmarkup.ling.mark..* implements IPrintable;
	declare parents : semanticmarkup.ling.normalize..* implements IPrintable;
	//declare parents : semanticmarkup.ling.pos..* implements IPrintable;
	declare parents : semanticmarkup.ling.transform..* implements IPrintable;
	
	declare parents : semanticmarkup.markup..* implements IPrintable;
	
	declare parents : semanticmarkup.run..* implements IPrintable;*/
	
}
