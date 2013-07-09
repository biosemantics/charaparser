package semanticMarkup.markupElement.description.run;

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

	@Inject
	public DescriptionMarkupRun(@Named("GuiceModuleFile")String guiceModuleFile,
			@Named("Run_OutDirectory")String runOutDirectory, 
			@Named("DescriptionMarkupCreator") IDescriptionMarkupCreator creator) {
		super(guiceModuleFile, runOutDirectory);
		this.creator = creator;
	}

	@Override
	protected void doRun() throws Exception {
		log(LogLevel.INFO, "Creating markup using " + creator.getDescription() + "...");
		creator.create();
	}
}
