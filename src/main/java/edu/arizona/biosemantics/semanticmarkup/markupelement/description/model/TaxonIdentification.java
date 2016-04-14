package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes.TaxonIdentificationAttribute;
/**
 * @author Hong Cui
 *
 */
import edu.arizona.biosemantics.semanticmarkup.model.Element;


public class TaxonIdentification extends Element {
	
	@XmlPath("@" + TaxonIdentificationAttribute.status)
	private String status;
	
	private List<TaxonName> taxonNames = new LinkedList<TaxonName>();
	
	private StrainNumber strainNumber;
	/*private List<StrainNumber> strainNumbers;
	private List<PlaceOfPublication> placeOfPublications;
	private TaxonHierarchy taxonHierarchy;
	private List<OtherInfoOnName> otherInfoOnNames;*/
	
	public List<TaxonName> getTaxonNames() {
		return taxonNames;
	}

	public boolean hasStrainNumber() {
		return this.strainNumber.getStrainNumber() != null;
	}
	
	public StrainNumber getStrainNumber() {
		return strainNumber;
	}

	public void setStrainNumber(StrainNumber strainNumber) {
		this.strainNumber = strainNumber;
	}

	public void setTaxonNames(List<TaxonName> taxonNames) {
		this.taxonNames = taxonNames;
	}
	
	/*public void setTaxonName(TaxonName taxonName) {
		this.taxonNames.add(taxonName);
	}
	
	public TaxonName getTaxonName() {
		return this.taxonNames.get(0);
	}*/
	

	/*public List<StrainNumber> getStrainNumbers() {
		return strainNumbers;
	}

	public void setStrainNumbers(List<StrainNumber> strainNumbers) {
		this.strainNumbers = strainNumbers;
	}

	public void addStrainNumbers(StrainNumber strainNumber) {
		this.strainNumbers.add(strainNumber);
	}

	
	public List<PlaceOfPublication> getPlaceOfPublications() {
		return placeOfPublications;
	}

	public void setPlaceOfPublications(List<PlaceOfPublication> placeOfPublications) {
		this.placeOfPublications = placeOfPublications;
	}


	public void addPlaceOfPublications(PlaceOfPublication placeOfPublication) {
		this.placeOfPublications.add(placeOfPublication);
	}

	
	public TaxonHierarchy getTaxonHierarchy() {
		return taxonHierarchy;
	}

	public void setTaxonHierarchy(TaxonHierarchy taxonHierarchy) {
		this.taxonHierarchy = taxonHierarchy;
	}

	public List<OtherInfoOnName> getOtherInfoOnNames() {
		return otherInfoOnNames;
	}

	public void setOtherInfoOnNames(List<OtherInfoOnName> otherInfoOnNames) {
		this.otherInfoOnNames = otherInfoOnNames;
	}
	
	public void addOtherInfoOnName(OtherInfoOnName> otherInfoOnNames) {
		this.otherInfoOnNames = otherInfoOnNames;
	}
*/
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public void removeElementRecursively(Element element) {

	}
}