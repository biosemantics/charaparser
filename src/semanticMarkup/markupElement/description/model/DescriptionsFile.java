package semanticMarkup.markupElement.description.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


public class DescriptionsFile {
	
	private File file; // source could later then be filename + descriptionID + sentenceID
	private Meta meta;
	private List<TreatmentRoot> treatmentRoots = new LinkedList<TreatmentRoot>();

	public List<TreatmentRoot> getTreatmentRoots() {
		return treatmentRoots;
	}

	public void setTreatmentRoots(List<TreatmentRoot> treatmentRoots) {
		this.treatmentRoots = treatmentRoots;
	}

	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public void setMeta(Meta meta) {
		this.meta = meta;
	}
	
	public Meta getMeta() {
		return this.meta;
	}

	public String getName() {
		return this.getFile().getName();
	}
	
	public List<Description> getDescriptions() {
		List<Description> result = new LinkedList<Description>();
		for(TreatmentRoot treatmentRoot : treatmentRoots) {
			result.addAll(treatmentRoot.getDescriptions());
		}
		return result;
	}
	
	/**
	 * Needed for input format where no <treatments><treatment>...</treatment></treatments> is given
	 * but instead only one level such as <treatment>...</treatment>
	 * To still maintain the generic model a treatmentroot has to be inserted 
	 * @return
	 */
	public void setDescriptions(List<Description> descriptions) {
		TreatmentRoot treatmentRoot = new TreatmentRoot();
		treatmentRoot.setDescriptions(descriptions);
		this.treatmentRoots.add(treatmentRoot);
	}

}

