package semanticMarkup.run;

import java.io.File;

import semanticMarkup.io.input.validate.IVolumeValidator;
import semanticMarkup.io.input.validate.lib.XMLVolumeValidator;
import semanticMarkup.log.LogLevel;
import semanticMarkup.markup.IMarkupCreator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A MarkupRun creates a markup of treatments using an IMarkupCreator
 * @author rodenhausen
 */
public class MarkupRun extends AbstractRun {
	
	private IMarkupCreator creator;
	private String validateSchemaFile;

	@Inject
	public MarkupRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("Run_RootDirectory")String runRootDirectory,
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("MarkupCreator") IMarkupCreator creator,
			@Named("MarkupRun_ValidateSchemaFile") String validateSchemaFile) {
		super(guiceModuleFile, runRootDirectory, runOutDirectory);
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
