package semanticMarkup.markupElement.habitat.model;


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class HabitatsFileList {

	private List<HabitatsFile> habitatsFiles;

	public HabitatsFileList(List<HabitatsFile> habitatsFiles) {
		super();
		this.habitatsFiles = habitatsFiles;
	}

	public List<HabitatsFile> getHabitatsFiles() {
		return habitatsFiles;
	}

	public void setHabitatsFiles(List<HabitatsFile> habitatsFiles) {
		this.habitatsFiles = habitatsFiles;
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
