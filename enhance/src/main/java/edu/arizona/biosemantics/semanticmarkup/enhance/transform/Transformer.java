package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.log.LogLevel;

public class Transformer {

	private SAXBuilder saxBuilder = new SAXBuilder();
	private List<AbstractTransformer> transformers = new LinkedList<AbstractTransformer>();
	
	public Transformer() {
		transformers.add(new MapOntologyIdsTransformer(TaxonGroup.PLANT));
	}
	
	public void transform(File inputDirectory, File outputDirectory) {
		for(File file : inputDirectory.listFiles()) {
			if(file.isFile()) {
				try {
					Document document = null;
					try(InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF8")) {
						document = saxBuilder.build(inputStreamReader);
					}
					
					if(document != null) 
						for(AbstractTransformer transformer : transformers) 
							transformer.transform(document);
					
					File outputFile = new File(outputDirectory, file.getName());
					try(OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF8")) {
						XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
						xmlOutput.output(document, outputStreamWriter);
					}
				} catch (JDOMException | IOException e) {
					log(LogLevel.ERROR, "Can't read xml from file " + file.getAbsolutePath(), e);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Transformer transformer = new Transformer();
		transformer.transform(new File("in"), new File("out"));
	}
	
}
