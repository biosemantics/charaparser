package edu.arizona.sirls.semanticMarkup.markupElement.habitat.io.lib;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.markupElement.habitat.io.IHabitatReader;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.Habitat;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.HabitatsFile;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.HabitatsFileList;
import edu.arizona.sirls.semanticMarkup.markupElement.habitat.model.Treatment;

public class JDOMHabitatReader implements IHabitatReader {
	
	private String filePath;

	@Inject
	public JDOMHabitatReader(@Named("XMLHabitatReader_InputFile") String filePath) {
		this.filePath = filePath;
	}

	@Override
	public HabitatsFileList read(String inputDirectory) throws Exception {
		List<HabitatsFile> habitatsFiles = new LinkedList<HabitatsFile>();
		
		File sourceDirectory = new File(filePath);
		for(File file : sourceDirectory.listFiles()) {
			HabitatsFile habitatFile = new HabitatsFile();
			
			SAXBuilder builder = new SAXBuilder();
			Document document = (Document) builder.build(file);
			Element rootNode = document.getRootElement();
			ElementFilter filter = new ElementFilter("habitat");
			List<String> habitatParts = new LinkedList<String>();
			for (Element element : rootNode.getDescendants(filter)) 
				habitatParts.add(element.getValue());
			Habitat habitat = new Habitat();
			habitat.setHabitatParts(habitatParts);
			
			Treatment treatment = new Treatment();
			treatment.setHabitats(habitat);
			List<Treatment> treatments = new LinkedList<Treatment>();
			treatments.add(treatment);
			
			habitatFile.setTreatments(treatments);
			habitatFile.setFile(file);
			habitatsFiles.add(habitatFile);
		}
		return new HabitatsFileList(habitatsFiles);
	}
}
