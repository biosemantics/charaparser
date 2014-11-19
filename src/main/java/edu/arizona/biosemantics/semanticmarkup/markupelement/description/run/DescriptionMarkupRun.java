package edu.arizona.biosemantics.semanticmarkup.markupelement.description.run;

import java.io.File;


import java.util.Arrays;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.io.validate.IVolumeValidator;
import edu.arizona.biosemantics.semanticmarkup.io.validate.lib.XMLVolumeValidator;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.IDescriptionMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.run.AbstractRun;
import edu.arizona.biosemantics.semanticmarkup.run.PostRun;

/**
 * A MarkupRun creates a markup of treatments using an IMarkupCreator
 * @author rodenhausen
 */
public class DescriptionMarkupRun extends AbstractRun {
	
	private IDescriptionMarkupCreator creator;
	private String validateSchemaFile;

	@Inject
	public DescriptionMarkupRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("InputDirectory")String inputDirectory, 
			@Named("Run_OutDirectory")String runOutDirectory, 
			IDescriptionMarkupCreator creator,
			@Named("MarkupRun_ValidateSchemaFile") String validateSchemaFile) {
		super(guiceModuleFile, inputDirectory, runOutDirectory);
		this.creator = creator;
		this.validateSchemaFile = validateSchemaFile;
	}

	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Creating markup using " + creator.getDescription() + "...");
		creator.create();
		
		PostRun r = new PostRun(runOutDirectory/*, validateSchemaFile*/);
		r.absorbKeys();
		
		IVolumeValidator volumeValidator = new XMLVolumeValidator(new File(validateSchemaFile));
		boolean result = volumeValidator.validate(Arrays.asList(new File(runOutDirectory).listFiles()));
		if(!result)
			throw new Exception("Created output is not valid against the schema: " + validateSchemaFile);
	}
}
