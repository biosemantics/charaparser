package semanticMarkup.ling.transform;

public interface IInflector {

	public String getSingular(String word);
	
	public String getPlural(String word);
	
	public boolean isPlural(String word);
	
	public boolean isSingular(String word);
	
}
