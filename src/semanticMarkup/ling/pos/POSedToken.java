package semanticMarkup.ling.pos;

import semanticMarkup.ling.Token;

public class POSedToken extends Token {

	private POS pos;

	public POSedToken(String content, POS pos) {
		super(content);
		this.pos = pos;
	}

	public POS getPOS() {
		return pos;
	}

	public void setPOS(POS pos) {
		this.pos = pos;
	}
	
	@Override
	public String toString() {
		return this.content + "/" + pos.toString();
	}
}
