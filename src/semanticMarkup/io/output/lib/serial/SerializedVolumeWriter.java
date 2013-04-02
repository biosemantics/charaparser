package semanticMarkup.io.output.lib.serial;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.output.AbstractFileVolumeWriter;

/**
 * SerializedVolumeWriter java serializes treatments and writes them out
 * @author rodenhausen
 */
public class SerializedVolumeWriter extends AbstractFileVolumeWriter {

	/**
	 * @param filePath
	 */
	public SerializedVolumeWriter(String filePath) {
		super(filePath);
	}

	@Override
	public void write(List<Treatment> treatments) throws Exception {
		ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filePath + ".ser"));
		outputStream.writeObject(treatments);
		outputStream.close();
	}

}
