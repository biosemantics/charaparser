package semanticMarkup.io.output;

public abstract class AbstractFileVolumeWriter implements IVolumeWriter {

	protected String filePath;

	public AbstractFileVolumeWriter(String filePath) {
		this.filePath = filePath;
	}

}
