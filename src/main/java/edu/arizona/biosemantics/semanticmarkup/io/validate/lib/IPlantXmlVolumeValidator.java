package edu.arizona.biosemantics.semanticmarkup.io.validate.lib;

import java.io.File;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.io.validate.AbstractXMLVolumeValidator;


public class IPlantXmlVolumeValidator extends AbstractXMLVolumeValidator {

	private File iplantXmlSchemaFile;

	/**
	 * @param iplantXmlSchemaFile
	 */
	public IPlantXmlVolumeValidator(File iplantXmlSchemaFile) {
		this.iplantXmlSchemaFile = iplantXmlSchemaFile;
	}

	/*@Override
	public boolean validate(File directory) {
		if(!directory.isDirectory())
			return false;

		File[] files =  directory.listFiles();
		int total = files.length;

		boolean result = true;
		for(int i = 0; i<total; i++) {
			File file = files[i];
			result &= validateXMLFileWithSchema(file, iplantXmlSchemaFile);
		}
		return result;
	}*/

	@Override
	public boolean validate(List<File> files) {
		boolean result = true;
		for(File file: files) {
			result &= validateXMLFileWithSchema(file, iplantXmlSchemaFile);
		}
		return result;
	}
}
