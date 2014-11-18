package edu.arizona.biosemantics.semanticmarkup.io.validate.lib;

import java.io.File;
import java.util.List;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.io.validate.AbstractXMLVolumeValidator;


/**
 * XMLVolumeValidator validates input against a specified XML format
 * @author rodenhausen
 */
public class XMLVolumeValidator extends AbstractXMLVolumeValidator {

	private File xmlSchemaFile;

	/**
	 * @param xmlSchemaFile
	 */
	public XMLVolumeValidator(File xmlSchemaFile) {
		this.xmlSchemaFile = xmlSchemaFile;
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
			boolean r = validateXMLFileWithSchema(file, xmlSchemaFile);
			result &= r;
			if(!r){
				log(LogLevel.ERROR, "Invalid xml output file "+file.getAbsolutePath());
				return result;
			}

		}
		return result;
	}*/

	
	@Override
	public boolean validate(List<File> files) {
		boolean result = true;
		for(File file: files) {
			boolean r = validateXMLFileWithSchema(file, xmlSchemaFile);
			result &= r;
			if(!r){
				log(LogLevel.ERROR, "Invalid xml output file "+file.getAbsolutePath());
				return result;
			}

		}
		return result;
	}
}
