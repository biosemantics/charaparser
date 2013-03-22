package semanticMarkup.io.input;

public abstract class AbstractFileVolumeReader implements IVolumeReader {

	protected String filePath;

	public AbstractFileVolumeReader(String filePath) {
		this.filePath = filePath;
	}
}
