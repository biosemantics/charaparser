package semanticMarkup.io.output.lib;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import semanticMarkup.io.output.AbstractFileVolumeWriter;
import semanticMarkup.model.Treatment;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * SerializedVolumeWriter java serializes treatments and writes them out
 * @author rodenhausen
 */
public class SerializedVolumeWriter extends AbstractFileVolumeWriter {

	/**
	 * @param outDirectory
	 */
	@Inject
	public SerializedVolumeWriter(@Named("Run_OutDirectory")String outDirectory) {
		super(outDirectory);
	}

	@Override
	public void write(List<Treatment> treatments) throws Exception {
		ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(outDirectory + ".ser"));
		outputStream.writeObject(treatments);
		outputStream.close();
	}

}
