package semanticMarkup.io.validate.lib;

import java.io.File;

import semanticMarkup.io.validate.AbstractXMLVolumeValidator;

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
	
	@Override
	public boolean validate(File directory) {
		if(!directory.isDirectory())
			return false;
		
		File[] files =  directory.listFiles();
		int total = files.length;
		
		boolean result = true;
		for(int i = 0; i<total; i++) {
			File file = files[i];
			result &= validateXMLFileWithSchema(file, xmlSchemaFile);
		}
		return result;
	}

}
