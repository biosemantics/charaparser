package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.lib;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.IElevationReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Elevation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Treatment;


public class JDOMElevationReader implements IElevationReader {

	@Override
	public ElevationsFileList read(String inputDirectory) throws Exception {
		List<ElevationsFile> elevationsFiles = new LinkedList<ElevationsFile>();
		
		File sourceDirectory = new File(inputDirectory);
		for(File file : sourceDirectory.listFiles()) {
			ElevationsFile elevationsFile = new ElevationsFile();
			
			SAXBuilder builder = new SAXBuilder();
			Document document = (Document) builder.build(file);
			Element rootNode = document.getRootElement();
			ElementFilter filter = new ElementFilter("description");
			List<Elevation> elevations = new LinkedList<Elevation>();
			for (Element element : rootNode.getDescendants(filter)) {
				String type = element.getAttributeValue("type");
				if(type != null && type.equals("elevation")) {
					Elevation elevation = new Elevation();
					elevation.setText(element.getText());
					elevations.add(elevation);
				}
			}
						
			Treatment treatment = new Treatment();
			treatment.setElevations(elevations);
			List<Treatment> treatments = new LinkedList<Treatment>();
			treatments.add(treatment);
			
			elevationsFile.setTreatments(treatments);
			elevationsFile.setFile(file);
			elevationsFiles.add(elevationsFile);
		}
		return new ElevationsFileList(elevationsFiles);
	}

}
