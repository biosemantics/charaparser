package semanticMarkup.io.output.serial;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.output.AbstractFileVolumeWriter;

public class SerializedVolumeWriter extends AbstractFileVolumeWriter {

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
