package semanticMarkup.markupElement.description.model;

import java.io.File;
import java.util.List;

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
