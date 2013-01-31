package semanticMarkup.know;

public interface IOrganStateKnowledgeBase {

	public boolean isOrgan(String word);
	
	public boolean isState(String word);

	public void addState(String word);
	
	public void addOrgan(String word);
	
}
