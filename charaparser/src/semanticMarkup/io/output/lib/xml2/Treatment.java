package semanticMarkup.io.output.lib.xml2;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class Treatment {

	private String number;
	private String family_name;
	private String authority;
	private String common_name;
	private ArrayList<String> author_of_family = new ArrayList<String>();
	private Description description;
	private String number_of_infrataxa;
	private ArrayList<String> general_distribution = new ArrayList<String>();
	private ArrayList<String> discussion = new ArrayList<String>();
	private ArrayList<String> reference = new ArrayList<String>();
	private ArrayList<Key> key = new ArrayList<Key>();
	
	public Treatment() { }
	
	public Treatment(String number, String family_name, String authority,
			String common_name, ArrayList<String> author_of_family,
			Description description, String number_of_infrataxa,
			ArrayList<String> general_distribution,
			ArrayList<String> discussion, ArrayList<String> reference,
			ArrayList<Key> key) {
		super();
		this.number = number;
		this.family_name = family_name;
		this.authority = authority;
		this.common_name = common_name;
		this.author_of_family = author_of_family;
		this.description = description;
		this.number_of_infrataxa = number_of_infrataxa;
		this.general_distribution = general_distribution;
		this.discussion = discussion;
		this.reference = reference;
		this.key = key;
	}

	@XmlElement
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@XmlElement
	public String getFamily_name() {
		return family_name;
	}

	public void setFamily_name(String family_name) {
		this.family_name = family_name;
	}

	@XmlElement
	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	@XmlElement
	public String getCommon_name() {
		return common_name;
	}

	public void setCommon_name(String common_name) {
		this.common_name = common_name;
	}

	@XmlElement
	public ArrayList<String> getAuthor_of_family() {
		return author_of_family;
	}

	public void setAuthor_of_family(ArrayList<String> author_of_family) {
		this.author_of_family = author_of_family;
	}

	@XmlElement
	public Description getDescription() {
		return description;
	}

	public void setDescription(Description description) {
		this.description = description;
	}

	@XmlElement
	public String getNumber_of_infrataxa() {
		return number_of_infrataxa;
	}

	public void setNumber_of_infrataxa(String number_of_infrataxa) {
		this.number_of_infrataxa = number_of_infrataxa;
	}

	@XmlElement
	public ArrayList<String> getGeneral_distribution() {
		return general_distribution;
	}

	public void setGeneral_distribution(
			ArrayList<String> general_distribution) {
		this.general_distribution = general_distribution;
	}

	@XmlElement
	public ArrayList<String> getDiscussion() {
		return discussion;
	}

	public void setDiscussion(ArrayList<String> discussion) {
		this.discussion = discussion;
	}

	@XmlElement
	public ArrayList<String> getReference() {
		return reference;
	}

	public void setReference(ArrayList<String> reference) {
		this.reference = reference;
	}

	@XmlElement
	public ArrayList<Key> getKey() {
		return key;
	}

	public void setKey(ArrayList<Key> key) {
		this.key = key;
	}
	
	
	
}
