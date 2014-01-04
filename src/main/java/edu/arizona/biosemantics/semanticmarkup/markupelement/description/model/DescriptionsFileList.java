package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model;


import java.util.List;

public class DescriptionsFileList {

	private List<AbstractDescriptionsFile> descriptionsFiles;

	public DescriptionsFileList(List<AbstractDescriptionsFile> descriptionsFiles) {
		super();
		this.descriptionsFiles = descriptionsFiles;
	}

	public List<AbstractDescriptionsFile> getDescriptionsFiles() {
		return descriptionsFiles;
	}

	public void setDescriptionsFiles(List<AbstractDescriptionsFile> descriptionsFiles) {
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
