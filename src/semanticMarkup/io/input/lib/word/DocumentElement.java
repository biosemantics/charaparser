package semanticMarkup.io.input.lib.word;

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
}
