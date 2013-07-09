package semanticMarkup.io.validate.lib;

import java.io.File;

import semanticMarkup.io.validate.AbstractXMLVolumeValidator;

/**
 * TaxonxVolumeValidator validates input against taxonx format
 * @author rodenhausen
 */
public class TaxonxVolumeValidator extends AbstractXMLVolumeValidator {
	
	private File taxonxSchemaFile;

	/**
	 * @param taxonxSchemaFile
	 */
	public TaxonxVolumeValidator(File taxonxSchemaFile) {
		this.taxonxSchemaFile = taxonxSchemaFile;
	}
	
	@Override
	public boolean validate(File file) {
		if(!file.isFile())
			return false;
		return this.validateXMLFileWithSchema(file, taxonxSchemaFile);
	}


}
