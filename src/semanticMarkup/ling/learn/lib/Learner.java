package semanticMarkup.ling.learn.lib;

import java.util.List;

import com.google.inject.name.Named;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.ling.learn.ILearner;
import semanticMarkup.ling.learn.ITerminologyLearner;

/**
 * Learner learns by reading from an IVolumeReader and learning using an ITerminologyLearner
 * @author rodenhausen
 */
public class Learner implements ILearner {

	private IVolumeReader volumeReader;
	private ITerminologyLearner terminologyLearner;

	/**
	 * @param volumeReader
	 * @param terminologyLearner
	 */
	public Learner(@Named("Learner_VolumeReader")IVolumeReader volumeReader, 
			ITerminologyLearner terminologyLearner) {	
		this.volumeReader = volumeReader;
		this.terminologyLearner = terminologyLearner;
	}

	@Override
	public void learn() throws Exception {
		List<Treatment> treatments = this.volumeReader.read();
		this.terminologyLearner.learn(treatments);
	}

	@Override
	public String getDescription() {
		return this.getClass().toString();
	}
}
