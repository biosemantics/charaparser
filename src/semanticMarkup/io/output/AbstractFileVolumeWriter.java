package semanticMarkup.io.output;

public abstract class AbstractFileVolumeWriter implements IVolumeWriter {

	protected String outDirectory;

	public AbstractFileVolumeWriter(String outDirectory) {
		this.outDirectory = outDirectory;
	}

}
