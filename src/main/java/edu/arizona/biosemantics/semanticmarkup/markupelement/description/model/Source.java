package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import edu.arizona.biosemantics.semanticmarkup.model.Element;

public class Source extends Element {

	private String author;
	private String date;
	private String title;
	private String pages;
	
	public Source(String author, String date, String title, String pages) {
		super();
		this.author = author;
		this.date = date;
		this.title = title;
		this.pages = pages;
	}

	public Source() {}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	@Override
	public void removeElementRecursively(Element element) {
		return;
	}

}
