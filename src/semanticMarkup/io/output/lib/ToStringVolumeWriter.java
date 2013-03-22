package semanticMarkup.io.output.lib;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.output.AbstractFileVolumeWriter;

import com.google.inject.Inject;
import com.google.inject.name.Named;


public class ToStringVolumeWriter extends AbstractFileVolumeWriter {

	@Inject
	public ToStringVolumeWriter(@Named("Run_OutFile") String filePath) {
		super(filePath);
	}

	@Override
	public void write(List<Treatment> treatments) throws Exception {
		FileWriter fileWriter = new FileWriter(new File(filePath + ".string"));
		fileWriter.write(treatments.toString());
		fileWriter.close();
	}
}
