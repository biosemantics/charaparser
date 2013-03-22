package semanticMarkup.ling.mark.lib;

import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.Token;
import semanticMarkup.ling.mark.IMarkedTokenCreator;
import semanticMarkup.ling.mark.MarkedToken;

public class OrganStateMarkedTokenCreator implements IMarkedTokenCreator {

	private IGlossary glossary;

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
