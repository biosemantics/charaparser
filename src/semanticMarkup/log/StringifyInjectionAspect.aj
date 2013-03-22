package semanticMarkup.log;

import org.codehaus.jackson.annotate.JsonIgnore;

public aspect StringifyInjectionAspect {
	
	@JsonIgnore
	private IPrintable IPrintable.thisObject;
	
	public String IPrintable.toString() {
		return ObjectStringifier.getInstance().stringify(thisObject);
	}
	
	pointcut objectConstruction(IPrintable object) : 
		initialization(IPrintable+.new(..)) && this(object);

	after(IPrintable object) : objectConstruction(object) {
		object.thisObject = object;
	}
	
	declare parents : semanticMarkup.* implements IPrintable;
	
	declare parents : semanticMarkup.core..* implements IPrintable;
	
	declare parents : semanticMarkup.eval..* implements IPrintable;
	
	// JAXB container classes may not implement an interface, JAXB can't handle interface, will throw exception
	declare parents : semanticMarkup.io.* implements IPrintable;
	declare parents : semanticMarkup.io.input.* implements IPrintable;
	declare parents : semanticMarkup.io.input.extract..* implements IPrintable;
	declare parents : semanticMarkup.io.input.lib.db.* implements IPrintable;
	declare parents : semanticMarkup.io.input.lib.serial.* implements IPrintable;
	declare parents : semanticMarkup.io.input.lib.type1 implements IPrintable;
	declare parents : semanticMarkup.io.input.lib.type2 implements IPrintable;
	declare parents : semanticMarkup.io.input.lib.type3 implements IPrintable;
	declare parents : semanticMarkup.io.input.lib.type4 implements IPrintable;
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
	declare parents : semanticMarkup.ling.learn..* implements IPrintable;
	//declare parents : semanticMarkup.ling.mark..* implements IPrintable;
	declare parents : semanticMarkup.ling.normalize..* implements IPrintable;
	//declare parents : semanticMarkup.ling.pos..* implements IPrintable;
	declare parents : semanticMarkup.ling.transform..* implements IPrintable;
	
	declare parents : semanticMarkup.markup..* implements IPrintable;
	
	declare parents : semanticMarkup.run..* implements IPrintable;
	
}
