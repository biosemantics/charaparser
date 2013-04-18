package semanticMarkup.know.net;

public interface IOTOClient {

	public OTOGlossary read(String tablePrefix);
		
	public void put(LocalGlossary localGlossary, String tablePrefix);
	
}
