package semanticMarkup.io.input;

/**
 * AbstractFileVolumeReader reads treatment descriptions from a file
 * @author rodenhausen
 */
public abstract class AbstractFileVolumeReader implements IVolumeReader {

	protected String filePath;

	/**
	 * @param filePath
	 */
	public AbstractFileVolumeReader(String filePath) {
		this.filePath = filePath;
	}
}
