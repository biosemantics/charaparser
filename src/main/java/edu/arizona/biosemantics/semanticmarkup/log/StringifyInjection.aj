package edu.arizona.biosemantics.semanticmarkup.log;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

import edu.arizona.biosemantics.common.log.AbstractStringifyInjection;
import edu.arizona.biosemantics.common.log.IPrintable;

public aspect StringifyInjection extends AbstractStringifyInjection {
	
	declare parents : edu.arizona.biosemantics.semanticmarkup.* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.config.* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.config.dataset.* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.eval..* implements IPrintable;
	//declare parents : edu.arizona.biosemantics.semanticmarkup.model.* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.gui.* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.io..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.know..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.ling..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.markup..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.run..* implements IPrintable;
	
	declare parents : edu.arizona.biosemantics.semanticmarkup.markupelement.description.* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.markupelement.description.io..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.markupelement.description.run..* implements IPrintable;
	declare parents : edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform..* implements IPrintable;
	
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
