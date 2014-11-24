package edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Meta;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Processor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Resource;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Software;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.IDescriptionTransformer;

/**
 * CharaParserMarkupCreator creates a markup by reading treatments, transforming them using a TreatmentTransformerChain and writing them out
 * @author thomas rodenhausen
 */
public class DescriptionMarkupCreator extends AbstractDescriptionMarkupCreator {
	
	@Inject
	public DescriptionMarkupCreator(@Named("DescriptionMarkupCreator_DescriptionReader") IDescriptionReader reader,	
			@Named("DescriptionMarkupCreator_MarkupDescription")IDescriptionTransformer descriptionTransformer,
			@Named("DescriptionMarkupCreator_DescriptionWriter") IDescriptionWriter writer, 
			@Named("DescriptionReader_InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String outputDirectory) {
		super(reader, writer, inputDirectory, outputDirectory);
		this.add(descriptionTransformer);
	}

	@Override
	public String getDescription() {
		return this.getClass().toString();
	}
}

