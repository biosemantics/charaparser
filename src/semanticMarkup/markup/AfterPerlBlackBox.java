package semanticMarkup.markup;

import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.transformation.lib.OldPerlTreatmentTransformer;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.io.output.IVolumeWriter;
import semanticMarkup.log.LogLevel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * AfterPerlBlackBox creates a markup by reading treatments, transforming them using OldPerlTreatmentTransformer and writing them out
 * @author thomas rodenhausen
 */
public class AfterPerlBlackBox implements IMarkupCreator {

	private OldPerlTreatmentTransformer inputTransformer;
	private IVolumeWriter volumeWriter;
	private IVolumeReader volumeReader;
	private List<Treatment> treatments;

	/**
	 * @param volumeReader
	 * @param inputTransformer
	 * @param volumeWriter
	 */
	@Inject
	public AfterPerlBlackBox(@Named("MarkupCreator_VolumeReader")IVolumeReader volumeReader, 
			OldPerlTreatmentTransformer inputTransformer, 
			@Named("MarkupCreator_VolumeWriter")IVolumeWriter volumeWriter) {	
		this.volumeReader = volumeReader;
		this.inputTransformer = inputTransformer;
		this.volumeWriter = volumeWriter;
	}
	
	@Override
	public void create() {
		try {
			log(LogLevel.DEBUG, "reading treatments using " + volumeReader.getClass());
			treatments = volumeReader.read();
			
			treatments = inputTransformer.transform(treatments);

			log(LogLevel.DEBUG, "writing result using " + volumeWriter.getClass());
			volumeWriter.write(treatments);
				
		} catch (Exception e) {
			e.printStackTrace();
			log(LogLevel.ERROR, e);
		}
	}

	@Override
	public String getDescription() {
		return "Perl Blackbox";
	}

	@Override
	public List<Treatment> getResult() {
		return treatments;
	}
}
