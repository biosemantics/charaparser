package semanticMarkup.run;

import java.io.File;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markup.IMarkupCreator;

public class IPlantMarkupRun extends AbstractRun {

	private IMarkupCreator creator;

	@Inject
	public IPlantMarkupRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("Run_RootDirectory")String runRootDirectory,
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("MarkupCreator") IMarkupCreator creator) {
		super(guiceModuleFile, runRootDirectory, runOutDirectory);
		this.creator = creator;
	}

	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Creating markup using " + creator.getDescription() + "...");
		creator.create();
	}
	
	@Override
	protected boolean isValidRun(String runRootDirectory) {
		File file = new File(runRootDirectory + File.separator + "learnComplete");
		if(!file.exists()) {
			log(LogLevel.ERROR, "learning not complete yet.");
			return false;
		}
		return true;
	}	
	
}
