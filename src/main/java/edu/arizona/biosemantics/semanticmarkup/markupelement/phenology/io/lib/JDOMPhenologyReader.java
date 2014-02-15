package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io.lib;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io.IPhenologyReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Phenology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Treatment;

public class JDOMPhenologyReader implements IPhenologyReader {
	
	@Override
	public PhenologiesFileList read(String inputDirectory) throws Exception {
		List<PhenologiesFile> phenologiesFiles = new LinkedList<PhenologiesFile>();
		
		File sourceDirectory = new File(inputDirectory);
		for(File file : sourceDirectory.listFiles()) {
			PhenologiesFile phenologiesFile = new PhenologiesFile();
			
			SAXBuilder builder = new SAXBuilder();
			Document document = (Document) builder.build(file);
			Element rootNode = document.getRootElement();
			ElementFilter filter = new ElementFilter("description");
			List<Phenology> phenologies = new LinkedList<Phenology>();
			for (Element element : rootNode.getDescendants(filter)) {
				String type = element.getAttributeValue("type");
				if(type != null && type.equals("phenology")) {
					Phenology phenology = new Phenology();
					phenology.setText(element.getText());
					phenologies.add(phenology);
				}
			}
						
			Treatment treatment = new Treatment();
			treatment.setPhenology(phenologies);
			List<Treatment> treatments = new LinkedList<Treatment>();
			treatments.add(treatment);
			
			phenologiesFile.setTreatments(treatments);
			phenologiesFile.setFile(file);
			phenologiesFiles.add(phenologiesFile);
		}
		return new PhenologiesFileList(phenologiesFiles);
	}

}
