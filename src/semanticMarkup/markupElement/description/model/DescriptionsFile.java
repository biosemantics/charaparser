package semanticMarkup.markupElement.description.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


public class DescriptionsFile {
	
	private File file; // source could later then be filename + descriptionID + sentenceID
	private Meta meta;
	private List<Description> descriptions = new LinkedList<Description>();

	public List<Description> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<Description> descriptions) {
		this.descriptions = descriptions;
	}

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

}
