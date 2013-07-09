package semanticMarkup.markupElement.description.model;


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DescriptionsFileList {

	private List<DescriptionsFile> descriptionsFiles;

	public DescriptionsFileList(List<DescriptionsFile> descriptionsFiles) {
		super();
		this.descriptionsFiles = descriptionsFiles;
	}

	public List<DescriptionsFile> getDescriptionsFiles() {
		return descriptionsFiles;
	}

	public void setDescriptionsFiles(List<DescriptionsFile> descriptionsFiles) {
		this.descriptionsFiles = descriptionsFiles;
	}

	/*public Collection<Description> getDescriptions() {
		List<Description> descriptions = new LinkedList<Description>();
		for(DescriptionsFile descriptionsFile : descriptionsFiles) {
			for(Description description : descriptionsFile.getDescriptions()) {
				description.setDescriptionsFile(descriptionsFile);
			}
			descriptions.addAll(descriptionsFile.getDescriptions());
		}
		return descriptions;
	}*/
}
