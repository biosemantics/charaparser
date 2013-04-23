package semanticMarkup.know.net;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import semanticMarkup.log.ObjectStringifier;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.arizona.sirls.beans.Sentence;
import edu.arizona.sirls.beans.Term;
import edu.arizona.sirls.beans.TermCategory;
import edu.arizona.sirls.beans.TermSynonym;
import edu.arizona.sirls.beans.WordRole;

/**
 * OTOClient obtains and sends glossary data from and to OTO to update the 'local' and 'remote' glossary
 * @author rodenhausen
 */
public class OTOClient implements IOTOClient {

	private String url;
	private Client client;
	
	/**
	 * @param url
	 */
	@Inject
	public OTOClient(@Named("OTOClient_Url")String url) {
		this.url = url;
		ClientConfig clientConfig = new DefaultClientConfig();
		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		client = Client.create(clientConfig);
		//client.addFilter(new LoggingFilter(System.out));
	}
	
	@Override
	public OTOGlossary read(String tablePrefix) {
		List<TermCategory> termCategories = this.readTermCategories(tablePrefix);
		List<TermSynonym> termSynonyms = this.readTermSynonyms(tablePrefix);
		List<WordRole> wordRoles = this.readWordRoles(tablePrefix);
		return new OTOGlossary(termCategories, termSynonyms, wordRoles);
	}
	
	private List<WordRole> readWordRoles(String tablePrefix) {
		String url = this.url + "rest/glossary/wordRole";
	    WebResource webResource = client.resource(url);
	    MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    List<WordRole> wordRoles = webResource.queryParams(queryParams).get(new GenericType<List<WordRole>>() {});
	    return wordRoles;
	}

	private List<TermCategory> readTermCategories(String tablePrefix) {
		String url = this.url + "rest/glossary/termCategory";
	    WebResource webResource = client.resource(url);
	    MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    List<TermCategory> termCategories = webResource.queryParams(queryParams).get(new GenericType<List<TermCategory>>() {});
	    return termCategories;
	}
	
	private List<TermSynonym> readTermSynonyms(String tablePrefix) {
		String url = this.url + "rest/glossary/termSynonym";
		WebResource webResource = client.resource(url);
	    MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    List<TermSynonym> termSynonyms = webResource.queryParams(queryParams).get(new GenericType<List<TermSynonym>>() {});
	    return termSynonyms;
	}
	
	@Override
	public void put(LocalGlossary localGlossary, String tablePrefix) {
		this.putPossibleOtherTerms(localGlossary.getStructures(), tablePrefix);
		this.putPossibleCharacters(localGlossary.getCharacters(), tablePrefix);
		this.putPossibleOtherTerms(localGlossary.getOtherTerms(), tablePrefix);
	}

	private void putSentences(List<Sentence> sentences, String tablePrefix) {
		String url = this.url + "rest/glossary/sentence";
	    WebResource webResource = client.resource(url);
	    MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    webResource.queryParams(queryParams).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).put(sentences);
	}

	private void putTermCategories(List<TermCategory> termCategories, String tablePrefix) {
		String url = this.url + "rest/glossary/termCategory";
		WebResource webResource = client.resource(url); 
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    webResource.queryParams(queryParams).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).put(termCategories);
	}
	
	private void putPossibleStructures(List<Term> structures, String tablePrefix) {
		String url = this.url + "rest/glossary/possibleStructure";
		WebResource webResource = client.resource(url); 
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    webResource.queryParams(queryParams).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).put(structures);
	}
	
	private void putPossibleCharacters(List<Term> characters, String tablePrefix) {
		String url = this.url + "rest/glossary/possibleStructure";
		WebResource webResource = client.resource(url); 
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    webResource.queryParams(queryParams).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).put(characters);
	}
	
	private void putPossibleOtherTerms(List<Term> otherTerms, String tablePrefix) {
		String url = this.url + "rest/glossary/possibleStructure";
		WebResource webResource = client.resource(url); 
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	    queryParams.add("tablePrefix", tablePrefix);
	    webResource.queryParams(queryParams).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).put(otherTerms);
	}
	
	
	
	public static void main(String[] args) {
		OTOClient otoClient = new OTOClient("http://localhost:8080/GlossaryWebservice/");
		List<Term> structures = new ArrayList<Term>();
		structures.add(new Term("a"));
		structures.add(new Term("b"));
		
		System.out.println(ObjectStringifier.getInstance().stringify(structures));
		
		otoClient.putPossibleStructures(structures, "ant_agosti");
		
		
		/*OTOGlossary glossary = otoClient.read("ant_agosti");
		for(TermCategory termCategory : glossary.getTermCategories()) {
			System.out.println("termCategory: " + termCategory.getTerm() + " " + termCategory.getCategory());
		}*/
	}
}
