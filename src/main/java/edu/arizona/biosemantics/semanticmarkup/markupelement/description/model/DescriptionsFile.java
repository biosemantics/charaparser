package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Could be split in two subclasses of AbstractDescriptionsFile to accomodate for single/multiple treatmentRoots within a XML file separately
 * @author rodenhausen
 *
 */
public class DescriptionsFile extends AbstractDescriptionsFile {

	private List<TreatmentRoot> treatmentRoots = new LinkedList<TreatmentRoot>();
	private List<TaxonIdentification> taxonIdentifications = new LinkedList<TaxonIdentification> ();

	@Override
	public List<TaxonIdentification> getTaxonIdentifications(){
		return this.taxonIdentifications;
	}

	public void setTaxonIdentifications(List<TaxonIdentification> taxonIdentifications){
		this.taxonIdentifications = taxonIdentifications;
	}


	public List<TaxonIdentification> getTaxonIdentification(){
		return this.taxonIdentifications;
	}

	public void setTaxonIdentification(List<TaxonIdentification> taxonIdentifications){
		this.taxonIdentifications = taxonIdentifications;
	}
	
	public void setTaxonIdentification(TaxonIdentification taxonIdentification){
		this.taxonIdentifications.add(taxonIdentification);
	}

	public List<TreatmentRoot> getTreatmentRoots() {
		return treatmentRoots;
	}

	public void setTreatmentRoots(List<TreatmentRoot> treatmentRoots) {
		this.treatmentRoots = treatmentRoots;
	}

	/**
	 * Needed for input format where no &lt;treatments&gt;&lt;treatment&gt;...&lt;/treatment&gt;&lt;/treatments&gt; is given
	 * but instead only one level such as &lt;treatment&gt;...&lt;/treatment&gt;
	 * To still maintain the generic model a treatmentroot has to be added to the list
	 * Alternatively: Create two separate subclasses of AbstractDescriptionsFile that accomodate those differences and
	 * reference the respective one from bindings
	 */
	public void setTreatmentRoot(TreatmentRoot treatmentRoot) {
		this.treatmentRoots.add(treatmentRoot);
	}

	/**
	 * Needed for input format where no &lt;treatments&gt;&lt;treatment&gt;...&lt;/treatment&gt;&lt;/treatments&gt; is given
	 * but instead only one level such as &lt;treatment&gt;...&lt;/treatment&gt;
	 * To still maintain the generic model a treatmentroot has to be added to the list
	 * Alternatively: Create two separate subclasses of AbstractDescriptionsFile that accomodate those differences and
	 * reference the respective one from bindings
	 * @return
	 */
	public TreatmentRoot getTreatmentRoot() {
		return this.treatmentRoots.get(0);
	}

	@Override
	public List<Description> getDescriptions() {
		List<Description> result = new LinkedList<Description>();
		for(TreatmentRoot treatmentRoot : treatmentRoots) {
			result.addAll(treatmentRoot.getDescriptions());
		}
		return result;
	}


}


