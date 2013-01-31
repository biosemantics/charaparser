package semanticMarkup.markup;

import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.transformation.lib.OldPerlTreatmentTransformer;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.io.output.IVolumeWriter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class AfterPerlBlackBox implements IMarkupCreator {

	private OldPerlTreatmentTransformer inputTransformer;
	private IVolumeWriter volumeWriter;
	private IVolumeReader volumeReader;
	private List<Treatment> treatments;

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
			System.out.println("reading treatments using " + volumeReader.getClass());
			treatments = volumeReader.read();
			
			treatments = inputTransformer.transform(treatments);

			System.out.println("writing result using " + volumeWriter.getClass());
			volumeWriter.write(treatments);
				
		} catch (Exception e) {
			e.printStackTrace();
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
