package semanticMarkup.markupElement.description.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Could be split in two subclasses of AbstractDescriptionsFile to accomodate for single/multiple treatmentRoots within a XML file separately 
 * @author rodenhausen
 *
 */
public class DescriptionsFile extends AbstractDescriptionsFile {
	
	private List<TreatmentRoot> treatmentRoots = new LinkedList<TreatmentRoot>();

	public List<TreatmentRoot> getTreatmentRoots() {
		return treatmentRoots;
	}

	public void setTreatmentRoots(List<TreatmentRoot> treatmentRoots) {
		this.treatmentRoots = treatmentRoots;
	}
	
	/**
	 * Needed for input format where no <treatments><treatment>...</treatment></treatments> is given
	 * but instead only one level such as <treatment>...</treatment>
	 * To still maintain the generic model a treatmentroot has to be added to the list 
	 * @return
	 */
	public void setTreatmentRoot(TreatmentRoot treatmentRoot) {
		this.treatmentRoots.add(treatmentRoot);
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


