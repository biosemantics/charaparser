package semanticMarkup.know.net;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.arizona.sirls.beans.Sentence;
import edu.arizona.sirls.beans.TermCategory;
import edu.arizona.sirls.beans.TermSynonym;

public class OTOClient implements IOTOClient {

	private String url;
	private Client client;
	
	public OTOClient(String url) {
		this.url = url;
		this.client = new Client();
	}
	
	@Override
	public OTOGlossary read(String tablePrefix) {
		List<TermCategory> termCategories = this.readTermCategories(tablePrefix);
		List<TermSynonym> termSynonyms = this.readTermSynonyms(tablePrefix);
		return new OTOGlossary(termCategories, termSynonyms);
	}
	
	private List<TermCategory> readTermCategories(String tablePrefix) {
		String url = this.url + "rest/glossary/termCategory";
	    WebResource webResource = client.resource(url);
	    MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    List<TermCategory> termCategories = webResource.queryParams(queryParams).get(List.class);
	    return termCategories;
	}
	
	private List<TermSynonym> readTermSynonyms(String tablePrefix) {
		String url = this.url + "rest/glossary/termSynonym";
		WebResource webResource = client.resource(url);
	    MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    List<TermSynonym> termSynonyms = webResource.queryParams(queryParams).get(List.class);
	    return termSynonyms;
	}
	
	@Override
	public void put(LocalGlossary localGlossary, String tablePrefix) {
		this.putSentences(localGlossary.getSentences(), tablePrefix);
		this.putTermCategories(localGlossary.getTermCategories(), tablePrefix);
	}

	private void putSentences(List<Sentence> sentences, String tablePrefix) {
		String url = this.url + "rest/glossary/sentence";
	    WebResource webResource = client.resource(url);
	    MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    webResource.queryParams(queryParams).put();
	}

	private void putTermCategories(List<TermCategory> termCategories, String tablePrefix) {
		String url = this.url + "rest/glossary/sentence";
		WebResource webResource = client.resource(url);
	    MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    webResource.queryParams(queryParams).put();
	}
}
