package semanticMarkup.ling.pos;

import semanticMarkup.ling.Token;

/**
 * A POSedToken represents a token that carries its part of speech
 * @author rodenhausen
 */
public class POSedToken extends Token {

	private POS pos;

	/**
	 * @param content
	 * @param pos
	 */
	public POSedToken(String content, POS pos) {
		super(content);
		this.pos = pos;
	}

	/**
	 * @return the part of speech
	 */
	public POS getPOS() {
		return pos;
	}

	/**
	 * @param pos to set
	 */
	public void setPOS(POS pos) {
		this.pos = pos;
	}
	
	@Override
	public String toString() {
		return this.content + "/" + pos.toString();
	}
}
