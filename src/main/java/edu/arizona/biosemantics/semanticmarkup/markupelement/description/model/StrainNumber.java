package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.attributes.TaxonNameAttribute;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

public class StrainNumber extends Element {

	@XmlPath("text()")
	private String strainNumber;
	@XmlPath("@" + "equivalent_strain_numbers")
	private String equivalentStrainNumbers;
	@XmlPath("@" + "accession_number_16s_rrna")
	private String accessionNumber16sRrna;
	@XmlPath("@" + "accession_number_for_genome_sequence")
	private String accessionNumberForGenomeSequence;
	
	public StrainNumber() { }
	
	public StrainNumber(String strainNumber, String equivalentStrainNumbers,
			String accessionNumber16sRrna,
			String accessionNumberForGenomeSequence) {
		super();
		this.strainNumber = strainNumber;
		this.equivalentStrainNumbers = equivalentStrainNumbers;
		this.accessionNumber16sRrna = accessionNumber16sRrna;
		this.accessionNumberForGenomeSequence = accessionNumberForGenomeSequence;
	}
	public String getStrainNumber() {
		return strainNumber;
	}
	public void setStrainNumber(String strainNumber) {
		this.strainNumber = strainNumber;
	}
	public String getEquivalentStrainNumbers() {
		return equivalentStrainNumbers;
	}
	public void setEquivalentStrainNumbers(String equivalentStrainNumbers) {
		this.equivalentStrainNumbers = equivalentStrainNumbers;
	}
	public String getAccessionNumber16sRrna() {
		return accessionNumber16sRrna;
	}
	public void setAccessionNumber16sRrna(String accessionNumber16sRrna) {
		this.accessionNumber16sRrna = accessionNumber16sRrna;
	}
	public String getAccessionNumberForGenomeSequence() {
		return accessionNumberForGenomeSequence;
	}
	public void setAccessionNumberForGenomeSequence(
			String accessionNumberForGenomeSequence) {
		this.accessionNumberForGenomeSequence = accessionNumberForGenomeSequence;
	}

	@Override
	public void removeElementRecursively(Element element) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
