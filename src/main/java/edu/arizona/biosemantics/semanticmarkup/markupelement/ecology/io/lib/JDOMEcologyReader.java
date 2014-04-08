/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io.lib;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;

import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io.IEcologyReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.Ecology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.EcologyFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.EcologyFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.Treatment;

/**
 * @author Hong Cui
 *
 */
public class JDOMEcologyReader implements IEcologyReader{
	
	@Override
	public EcologyFileList read(String inputDirectory) throws Exception {
		List<EcologyFile> ecologyFiles = new LinkedList<EcologyFile>();
		
		File sourceDirectory = new File(inputDirectory);
		for(File file : sourceDirectory.listFiles()) {
			EcologyFile ecologyFile = new EcologyFile();
			
			SAXBuilder builder = new SAXBuilder();
			Document document = (Document) builder.build(file);
			Element rootNode = document.getRootElement();
			ElementFilter filter = new ElementFilter("description");
			List<Ecology> ecologys = new LinkedList<Ecology>();
			for (Element element : rootNode.getDescendants(filter)) {
				String type = element.getAttributeValue("type");
				if(type != null && type.equals("ecology")) {
					Ecology ecology = new Ecology();
					ecology.setText(element.getText());
					ecologys.add(ecology);
				}
			}
						
			Treatment treatment = new Treatment();
			treatment.setEcology(ecologys);
			List<Treatment> treatments = new LinkedList<Treatment>();
			treatments.add(treatment);
			
			ecologyFile.setTreatments(treatments);
			ecologyFile.setFile(file);
			ecologyFiles.add(ecologyFile);
		}
		return new EcologyFileList(ecologyFiles);
	}

}
