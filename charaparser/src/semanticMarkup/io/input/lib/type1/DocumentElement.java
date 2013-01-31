package semanticMarkup.io.input.lib.type1;

public class DocumentElement {

	private String text;
	private String property;
	
	public DocumentElement(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	
	@Override
	public String toString() {
		if(this.property==null)
			return this.text;
		else 
			return this.text + ":" + this.property;
	}
}
