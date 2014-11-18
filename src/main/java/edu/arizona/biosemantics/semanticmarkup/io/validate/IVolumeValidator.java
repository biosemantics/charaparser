package edu.arizona.biosemantics.semanticmarkup.io.validate;

import java.io.File;
import java.util.List;

/**
 * An IVolumeValidator validates an input file(s) of a volume to be of appropriate format
 * @author rodenhausen
 */
public interface IVolumeValidator {

	public boolean validate(List<File> files);
	//public boolean validate(File directory);
	
}
