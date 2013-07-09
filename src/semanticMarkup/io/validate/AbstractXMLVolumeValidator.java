package semanticMarkup.io.validate;

import java.io.File;
import java.io.FileReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import semanticMarkup.log.LogLevel;

/**
 * AbstractXMLVolumeValidator provides functionality required by IVolumeValidators that validate XML format input
 * @author rodenhausen
 */
public abstract class AbstractXMLVolumeValidator implements IVolumeValidator {

	public abstract boolean validate(File file);

	protected boolean validateXMLFileWithSchema(File xmlFile, File schemaFile) {
		try {			
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaSource = new StreamSource(schemaFile);
			Schema schema = factory.newSchema(schemaSource);
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new FileReader(xmlFile)));
			return true;
		} catch (Exception e) {
			log(LogLevel.DEBUG, "Problem validating XML against schema", e);
			return false;
		}
	}
}
