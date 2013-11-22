package semanticMarkup.markupElement.description.model;

import java.io.File;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

//XMLTransient is necesary because otherwise an xsi:type attribute will be generated when marshalling any subclass of this class.
//This can for example cause the output not to be valid against one of our schemas (that does not specify the xsi:type)
//At this time it is not known how to specify xml-transient for java type instead of a java attribute in a bindings file. Hence this annotation here.
@XmlTransient
public abstract class AbstractDescriptionsFile {

	private File file; // source could later then be filename + descriptionID + sentenceID
	private Meta meta;
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public void setMeta(Meta meta) {
		this.meta = meta;
	}
	
	public Meta getMeta() {
		return this.meta;
	}

	public String getName() {
		return this.getFile().getName();
	}
	
	public abstract List<Description> getDescriptions();
	
}
