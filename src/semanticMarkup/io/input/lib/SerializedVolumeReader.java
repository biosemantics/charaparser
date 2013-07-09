package semanticMarkup.io.input.lib;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import semanticMarkup.io.input.AbstractFileVolumeReader;
import semanticMarkup.model.Treatment;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * SerializedVolumeReader reads a list of treatments from the output of a SerializedVolumeWriter
 * @author rodenhausen
 */
public class SerializedVolumeReader extends AbstractFileVolumeReader {

	/**
	 * @param filepath
	 */
	@Inject
	public SerializedVolumeReader(@Named("SerializedVolumeReader_SourceFile") String filepath) {
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
