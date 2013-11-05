package semanticMarkup.io.input.validate.lib;

import java.io.File;

import semanticMarkup.io.input.validate.AbstractXMLVolumeValidator;

public class NewIPlantXmlVolumeValidator extends AbstractXMLVolumeValidator {

	private File newIplantXmlSchemaFile;
	private String error = "";

	/**
	 * @param xmlSchemaFile
	 */
	public NewIPlantXmlVolumeValidator(File newIplantXmlSchemaFile) {
		this.newIplantXmlSchemaFile = newIplantXmlSchemaFile;
	}
	
	@Override
	public boolean validate(File directory) {
		if(!directory.isDirectory()) {
			error = "Input is not a directory";
			return false;
		}
		
		File[] files =  directory.listFiles();
		int total = files.length;
		
		boolean result = true;
		StringBuilder errorBuilder = new StringBuilder();
		for(int i = 0; i<total; i++) {
			File file = files[i];
			boolean resultFile = validateXMLFileWithSchema(file, newIplantXmlSchemaFile);
			if(!resultFile)
				errorBuilder.append(file.getName() + " is not valid against schema\n");
			result &= resultFile;
		}
		error = errorBuilder.toString();
		return result;
	}

	@Override
	public String getError() {
		return error;
	}
}
