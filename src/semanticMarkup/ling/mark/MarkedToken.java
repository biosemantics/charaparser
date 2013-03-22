package semanticMarkup.ling.mark;

import semanticMarkup.ling.Token;

public class MarkedToken extends Token {

	private String mark;

	public MarkedToken(String content, String mark) {
		super(content);
		this.mark = mark;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}
}
