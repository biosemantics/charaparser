package semanticMarkup.markupElement.description.run;

import java.io.File;

import semanticMarkup.io.validate.IVolumeValidator;
import semanticMarkup.io.validate.lib.XMLVolumeValidator;
import semanticMarkup.log.LogLevel;
import semanticMarkup.markupElement.description.markup.IDescriptionMarkupCreator;
import semanticMarkup.run.AbstractRun;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A MarkupRun creates a markup of treatments using an IMarkupCreator
 * @author rodenhausen
 */
public class DescriptionMarkupRun extends AbstractRun {
	
	private IDescriptionMarkupCreator creator;
	private String validateSchemaFile;

	@Inject
	public DescriptionMarkupRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("Run_OutDirectory")String runOutDirectory, 
			IDescriptionMarkupCreator creator,
			@Named("MarkupRun_ValidateSchemaFile") String validateSchemaFile) {
		super(guiceModuleFile, runOutDirectory);
		this.creator = creator;
		this.validateSchemaFile = validateSchemaFile;
	}

	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Creating markup using " + creator.getDescription() + "...");
		creator.create();
		
		IVolumeValidator volumeValidator = new XMLVolumeValidator(new File(validateSchemaFile));
		boolean result = volumeValidator.validate(new File(runOutDirectory));
		if(!result)
			throw new Exception("Created output is not valid against the schema: " + validateSchemaFile);
	}
}
