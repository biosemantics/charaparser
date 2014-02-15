package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.io.lib;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.xmlbeans.impl.common.XPath;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.io.IDistributionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Distribution;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Treatment;

public class JDOMDistributionReader implements IDistributionReader {
	
	@Override
	public DistributionsFileList read(String inputDirectory) throws Exception {
		List<DistributionsFile> distributionsFiles = new LinkedList<DistributionsFile>();
		
		File sourceDirectory = new File(inputDirectory);
		for(File file : sourceDirectory.listFiles()) {
			DistributionsFile distributionsFile = new DistributionsFile();
			
			SAXBuilder builder = new SAXBuilder();
			Document document = (Document) builder.build(file);
			Element rootNode = document.getRootElement();
			ElementFilter filter = new ElementFilter("description");
			List<Distribution> distributions = new LinkedList<Distribution>();
			for (Element element : rootNode.getDescendants(filter)) {
				String type = element.getAttributeValue("type");
				if(type != null && type.equals("distribution")) {
					Distribution distribution = new Distribution();
					distribution.setText(element.getText());
					distributions.add(distribution);
				}
			}
						
			Treatment treatment = new Treatment();
			treatment.setDistributions(distributions);
			List<Treatment> treatments = new LinkedList<Treatment>();
			treatments.add(treatment);
			
			distributionsFile.setTreatments(treatments);
			distributionsFile.setFile(file);
			distributionsFiles.add(distributionsFile);
		}
		return new DistributionsFileList(distributionsFiles);
	}

}
