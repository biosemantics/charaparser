package semanticMarkup.markupElement.description.ling.learn.lib;

import semanticMarkup.markupElement.description.io.IDescriptionReader;
import semanticMarkup.markupElement.description.ling.learn.ILearner;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.model.DescriptionsFileList;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Learner learns by reading from an IVolumeReader and learning using an ITerminologyLearner
 * @author rodenhausen
 */
public class Learner implements ILearner {

	private IDescriptionReader descriptionReader;
	private ITerminologyLearner terminologyLearner;
	private String glossaryTable;
	private String inputDirectory;

	/**
	 * @param volumeReader
	 * @param terminologyLearner
	 */
	@Inject
	public Learner(@Named("DescriptionMarkupCreator_DescriptionReader")IDescriptionReader descriptionReader, 
			String inputDirectory,
			ITerminologyLearner terminologyLearner, 
			@Named("GlossaryTable") String glossaryTable) {	
		this.descriptionReader = descriptionReader;
		this.terminologyLearner = terminologyLearner;
		this.glossaryTable = glossaryTable;
		this.inputDirectory = inputDirectory;
	}

	@Override
	public void learn() throws Exception {
		DescriptionsFileList descriptionsFileList = this.descriptionReader.read(inputDirectory);
		this.terminologyLearner.learn(descriptionsFileList.getDescriptionsFiles(), glossaryTable);
	}

	@Override
	public String getDescription() {
		return this.getClass().toString();
	}
}
