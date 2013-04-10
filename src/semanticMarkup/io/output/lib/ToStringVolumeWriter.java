package semanticMarkup.io.output.lib;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.output.AbstractFileVolumeWriter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * ToStringVolumeWriter writes a list of treatments to a String
 * @author rodenhausen
 */
public class ToStringVolumeWriter extends AbstractFileVolumeWriter {

	/**
	 * @param filePath
	 */
	@Inject
	public ToStringVolumeWriter(@Named("Run_OutDirectory") String outDirectory) {
		super(outDirectory);
	}

	@Override
	public void write(List<Treatment> treatments) throws Exception {
		FileWriter fileWriter = new FileWriter(new File(outDirectory + File.separator + "treatments.string"));
		fileWriter.write(treatments.toString());
		fileWriter.close();
	}
}
