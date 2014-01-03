package edu.arizona.sirls.semanticMarkup.ling.mark;

import edu.arizona.sirls.semanticMarkup.ling.Token;

/**
 * A MarkedToken contains represents a token that is marked (e.g. by a type)
 * @author rodenhausen
 */
public class MarkedToken extends Token {

	private String mark;

	/**
	 * @param content
	 * @param mark
	 */
	public MarkedToken(String content, String mark) {
		super(content);
		this.mark = mark;
	}

	/**
	 * @return the mark
	 */
	public String getMark() {
		return mark;
	}

	/**
	 * @param mark to set
	 */
	public void setMark(String mark) {
		this.mark = mark;
	}
}
