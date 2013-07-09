package semanticMarkup.log;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * StringifyInjectionAspect specifies IPrintables and adds them a toString method
 * @author rodenhausen
 */
public aspect StringifyInjectionAspect {
	
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
	declare parents : semanticMarkup.* implements IPrintable;
	
	declare parents : semanticMarkup.model..* implements IPrintable;
	
	declare parents : semanticMarkup.eval..* implements IPrintable;
	declare parents : semanticMarkup.config.* implements IPrintable;
	
	// JAXB container classes may not implement an interface, JAXB can't handle interface, will throw exception
	declare parents : semanticMarkup.io.* implements IPrintable;
	declare parents : semanticMarkup.io.input.* implements IPrintable;
	declare parents : semanticMarkup.io.input.extract..* implements IPrintable;
	declare parents : semanticMarkup.io.input.lib.db.* implements IPrintable;
	declare parents : semanticMarkup.io.input.lib.serial.* implements IPrintable;
	declare parents : semanticMarkup.io.input.lib.xml implements IPrintable;
	declare parents : semanticMarkup.io.input.lib.word implements IPrintable;
	declare parents : semanticMarkup.io.output.* implements IPrintable;
	declare parents : semanticMarkup.io.output.lib.* implements IPrintable;
	declare parents : semanticMarkup.io.output.lib.serial.* implements IPrintable;
	//declare parents : semanticMarkup.io.input.* implements IPrintable;
	//declare parents : semanticMarkup.io.output..* implements IPrintable; 	
	//declare parents : semanticMarkup.io.output.lib.* implements IPrintable; 	
	//declare parents : semanticMarkup.io.output.lib.xml.* implements IPrintable; 	
	
	declare parents : semanticMarkup.know..* implements IPrintable;
	
	//chunkcollector and abstractparsetree, token, markedToken, posedToken printing is better done differently
	//declare parents : semanticMarkup.ling..* implements IPrintable;	
	//declare parents : semanticMarkup.ling.* implements IPrintable;
	declare parents : semanticMarkup.ling.extract..* implements IPrintable;
	declare parents : semanticMarkup.markupElement.description.ling.learn..* implements IPrintable;
	//declare parents : semanticMarkup.ling.mark..* implements IPrintable;
	declare parents : semanticMarkup.ling.normalize..* implements IPrintable;
	//declare parents : semanticMarkup.ling.pos..* implements IPrintable;
	declare parents : semanticMarkup.ling.transform..* implements IPrintable;
	
	declare parents : semanticMarkup.markup..* implements IPrintable;
	
	declare parents : semanticMarkup.run..* implements IPrintable;
	
}
