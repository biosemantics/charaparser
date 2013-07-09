package semanticMarkup.io.validate.lib;

import java.io.File;

import semanticMarkup.io.validate.AbstractXMLVolumeValidator;

public class IPlantXmlVolumeValidator extends AbstractXMLVolumeValidator {
	
	private File iplantXmlSchemaFile;

	/**
	 * @param xmlSchemaFile
	 */
	public IPlantXmlVolumeValidator(File iplantXmlSchemaFile) {
		this.iplantXmlSchemaFile = iplantXmlSchemaFile;
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
			result &= validateXMLFileWithSchema(file, iplantXmlSchemaFile);
		}
		return result;
	}

}
