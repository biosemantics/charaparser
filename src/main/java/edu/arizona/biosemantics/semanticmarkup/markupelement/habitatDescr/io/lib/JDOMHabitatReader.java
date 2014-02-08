package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io.lib;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io.IHabitatReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Habitat;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Treatment;

public class JDOMHabitatReader implements IHabitatReader {
	
	@Override
	public HabitatsFileList read(String inputDirectory) throws Exception {
		List<HabitatsFile> habitatsFiles = new LinkedList<HabitatsFile>();
		
		File sourceDirectory = new File(inputDirectory);
		for(File file : sourceDirectory.listFiles()) {
			HabitatsFile habitatsFile = new HabitatsFile();
			
			SAXBuilder builder = new SAXBuilder();
			Document document = (Document) builder.build(file);
			Element rootNode = document.getRootElement();
			ElementFilter filter = new ElementFilter("description");
			List<Habitat> habitats = new LinkedList<Habitat>();
			for (Element element : rootNode.getDescendants(filter)) {
				String type = element.getAttributeValue("type");
				if(type != null && type.equals("habitat")) {
					Habitat habitat = new Habitat();
					habitat.setText(element.getText());
					habitats.add(habitat);
				}
			}
						
			Treatment treatment = new Treatment();
			treatment.setHabitats(habitats);
			List<Treatment> treatments = new LinkedList<Treatment>();
			treatments.add(treatment);
			
			habitatsFile.setTreatments(treatments);
			habitatsFile.setFile(file);
			habitatsFiles.add(habitatsFile);
		}
		return new HabitatsFileList(habitatsFiles);
	}

}
