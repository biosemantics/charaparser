package semanticMarkup.markupElement.description.io.lib;

import javax.xml.bind.Binder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Binding {

	private Document document;
	private Binder<Node> binder;
	
	public Binding(Document document, Binder<Node> binder) {
		super();
		this.document = document;
		this.binder = binder;
	}
	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
	public Binder<Node> getBinder() {
		return binder;
	}
	public void setBinder(Binder<Node> binder) {
		this.binder = binder;
	}
	
	
	
}
