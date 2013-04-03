package semanticMarkup.ling.mark.lib;

import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.Token;
import semanticMarkup.ling.mark.IMarkedTokenCreator;
import semanticMarkup.ling.mark.MarkedToken;

/**
 * An OrganStateMarkedTokenCreator poses an IMarkedTokenCreator. It uses a IGlossary to mark tokens with organ or state and thus
 * obtain MarkedTokens.
 * @author rodenhausen
 */
public class OrganStateMarkedTokenCreator implements IMarkedTokenCreator {

	private IGlossary glossary;

	/**
	 * @param glossary
	 */
	public OrganStateMarkedTokenCreator(IGlossary glossary) {
		this.glossary = glossary;
	}
	
	@Override
	public MarkedToken getMarkedToken(Token token) {
		Set<String> categories = glossary.getCategories(token.getContent());
		if(categories.contains("Organ"))
			return new MarkedToken(token.getContent(), "organ");
		return new MarkedToken(token.getContent(), "");
	}

}
