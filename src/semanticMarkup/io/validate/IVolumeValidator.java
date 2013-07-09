package semanticMarkup.io.validate;

import java.io.File;

/**
 * An IVolumeValidator validates an input file(s) of a volume to be of appropriate format
 * @author rodenhausen
 */
public interface IVolumeValidator {

	public boolean validate(File file);
	
}
