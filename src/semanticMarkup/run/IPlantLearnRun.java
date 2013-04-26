package semanticMarkup.run;

import java.io.File;

import semanticMarkup.ling.learn.ILearner;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class IPlantLearnRun extends AbstractRun {
	
	private ILearner learner;

	/**
	 * @param runRootDirectory
	 * @param outDirectory
	 * @param guiceModuleFile
	 * @param learner
	 */
	@Inject
	public IPlantLearnRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("Run_RootDirectory")String runRootDirectory,
			@Named("Run_OutDirectory")String runOutDirectory, 
			ILearner learner) {
		super(guiceModuleFile, runRootDirectory, runOutDirectory);
		this.learner = learner;
	}

	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Learning using " + learner.getDescription() + "...");
		learner.learn();
		
		File file = new File(runRootDirectory + File.separator + "learnComplete");
		file.createNewFile();
	}
}
