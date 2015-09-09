package edu.arizona.biosemantics.semanticmarkup.enhance.run;

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
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.MapOntologyIdsTransformer;

public class Run {

	private SAXBuilder saxBuilder = new SAXBuilder();
	private List<AbstractTransformer> transformers = new LinkedList<AbstractTransformer>();
	
	public Run() {
	}
	
	public void run(File inputDirectory, File outputDirectory) {
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
	
	public void addTransformer(AbstractTransformer transformer) {
		this.transformers.add(transformer);
	}
	
	public static void main(String[] args) {
		Run transformer = new Run();
		transformer.run(new File("in"), new File("out"));
	}
	
}
