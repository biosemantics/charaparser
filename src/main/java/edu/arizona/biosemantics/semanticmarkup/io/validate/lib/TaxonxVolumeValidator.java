package edu.arizona.biosemantics.semanticmarkup.io.validate.lib;

import java.io.File;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.io.validate.AbstractXMLVolumeValidator;


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
	
	/*@Override
	public boolean validate(File file) {
		if(!file.isFile())
			return false;
		return this.validateXMLFileWithSchema(file, taxonxSchemaFile);
	}*/

	@Override
	public boolean validate(List<File> file) {
		return this.validateXMLFileWithSchema(file.get(0), taxonxSchemaFile);
	}
}
