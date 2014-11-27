package edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.IDescriptionTransformer;

public class DescriptionMarkupAndOntologyMappingCreator extends AbstractDescriptionMarkupCreator {

	@Inject
	public DescriptionMarkupAndOntologyMappingCreator(@Named("DescriptionMarkupCreator_DescriptionReader") IDescriptionReader reader,	
			@Named("DescriptionMarkupCreator_MarkupDescription")IDescriptionTransformer markupDescription,
			@Named("DescriptionMarkupCreator_OntologyMapping")IDescriptionTransformer ontologyMapping,
			@Named("DescriptionMarkupCreator_DescriptionWriter") IDescriptionWriter writer, 
			@Named("DescriptionReader_InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String outputDirectory) {
		super(reader, writer, inputDirectory, outputDirectory);
		this.add(markupDescription);
		this.add(ontologyMapping);
	}

	@Override
	public String getDescription() {
		return this.getClass().toString();
	}
	
}