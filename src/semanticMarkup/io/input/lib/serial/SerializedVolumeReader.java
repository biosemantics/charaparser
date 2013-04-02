package semanticMarkup.io.input.lib.serial;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.input.AbstractFileVolumeReader;

/**
 * SerializedVolumeReader reads a list of treatments from the output of a SerializedVolumeWriter
 * @author rodenhausen
 */
public class SerializedVolumeReader extends AbstractFileVolumeReader {

	public SerializedVolumeReader(String filepath) {
		super(filepath);
	}

	@Override
	public List<Treatment> read() throws Exception {
		ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filePath + ".ser"));
		List<Treatment> result = (List<Treatment>)inputStream.readObject();
        inputStream.close();
        return result;
	}
}
