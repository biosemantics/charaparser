package semanticMarkup.run;

import semanticMarkup.log.LogLevel;
import semanticMarkup.markup.IMarkupCreator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MarkupDBSentencesForEvalautionRun extends AbstractRun {
	
	private IMarkupCreator creator;

	@Inject
	public MarkupDBSentencesForEvalautionRun(@Named("GuiceModuleFile")String guiceModuleFile,
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
}
