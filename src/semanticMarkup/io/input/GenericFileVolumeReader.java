package semanticMarkup.io.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.mysql.jdbc.log.Log;

import semanticMarkup.FNAv19Config;
import semanticMarkup.core.Treatment;
import semanticMarkup.io.input.extract.lib.DistributionTreatmentRefiner;
import semanticMarkup.io.input.extract.lib.FloweringTimeTreatmentRefiner;
import semanticMarkup.io.input.lib.taxonx.TaxonxVolumeReader;
import semanticMarkup.io.input.lib.word.DocWordVolumeReader;
import semanticMarkup.io.input.lib.xml.XMLVolumeReader;
import semanticMarkup.log.LogLevel;

/**
 * GenericFileVolumeReader reads takes a filePath to check whether there is input available at the given format 
 * for any of XMLVolumeReader, TaxonxVolumeReader or DocWordVolumeReader. If there is it delegates the actual reading to the appropriate reader
 * @author rodenhausen
 */
public class GenericFileVolumeReader extends AbstractFileVolumeReader {

	private String styleStartPattern;
	private String styleNamePattern;
	private String styleKeyPattern;
	private String tribegennamestyle;
	private String styleMappingFile;
	private DistributionTreatmentRefiner distributionTreatmentRefiner;
	private FloweringTimeTreatmentRefiner floweringTimeTreatmentRefiner;
	private String temporaryPath;
	private String taxonxSchemaFile;
	private String xmlSchemaFile;

	/**
	 * @param filePath
	 * @param styleStartPattern
	 * @param styleNamePattern
	 * @param styleKeyPattern
	 * @param tribegennamestyle
	 * @param styleMappingFile
	 * @param distributionTreatmentRefiner
	 * @param floweringTimeTreatmentRefiner
	 * @param temporaryPath
	 * @param taxonxSchemaFile
	 * @param xmlSchemaFile
	 */
	@Inject
	public GenericFileVolumeReader(@Named("GenericFileVolumeReader_Source") String filePath,
			@Named("WordVolumeReader_StyleStartPattern") String styleStartPattern,
			@Named("WordVolumeReader_StyleNamePattern") String styleNamePattern,
			@Named("WordVolumeReader_StyleKeyPattern") String styleKeyPattern,
			@Named("WordVolumeReader_Tribegennamestyle") String tribegennamestyle, 
			@Named("WordVolumeReader_StyleMappingFile") String styleMappingFile, 
			DistributionTreatmentRefiner distributionTreatmentRefiner, 
			FloweringTimeTreatmentRefiner floweringTimeTreatmentRefiner,
			@Named("temporaryPath")String temporaryPath,
			@Named("Taxonx_SchemaFile")String taxonxSchemaFile,
			@Named("XML_SchemaFile")String xmlSchemaFile) {
		super(filePath);
		this.styleStartPattern = styleStartPattern;
		this.styleNamePattern = styleNamePattern;
		this.styleKeyPattern = styleKeyPattern;
		this.tribegennamestyle = tribegennamestyle;
		this.styleMappingFile = styleMappingFile;
		this.distributionTreatmentRefiner = distributionTreatmentRefiner;
		this.floweringTimeTreatmentRefiner = floweringTimeTreatmentRefiner;
		this.temporaryPath = temporaryPath;
		this.taxonxSchemaFile = taxonxSchemaFile;
		this.xmlSchemaFile = xmlSchemaFile;
	}

	@Override
	public List<Treatment> read() throws Exception {
		IVolumeReader reader = null;
		if(validXMLInput())
			reader = new XMLVolumeReader(filePath);
		if(validTaxonxInput()) 
			reader  = new TaxonxVolumeReader(filePath);
		if(validWordInput()) 
			reader = new DocWordVolumeReader(filePath, styleStartPattern, styleNamePattern,
					styleKeyPattern, tribegennamestyle, styleMappingFile, distributionTreatmentRefiner, floweringTimeTreatmentRefiner, 
					temporaryPath);
		
		if(reader != null) {
			log(LogLevel.DEBUG, "delegate reading treatments to " + reader.getClass());
			return reader.read();
		}
		log(LogLevel.ERROR, "no valid reader available for the input given");
		return new ArrayList<Treatment>();
	}

	private boolean validXMLInput() {
		if(!sourceIsDirectory())
			return false;
		
        File sourceDirectory = new File(filePath);
		File[] files =  sourceDirectory.listFiles();
		int total = files.length;
		
		boolean result = true;
		File schemaFile = new File(xmlSchemaFile);
		for(int i = 0; i<total; i++) {
			File file = files[i];
			result &= validateXML(file, schemaFile);
		}
		return result;
	}
	
	private boolean validTaxonxInput() {
		if(!sourceIsSingleFile())
			return false;
		File schemaFile = new File(taxonxSchemaFile);
		return this.validateXML(new File(filePath), schemaFile);
	}

	private boolean sourceIsSingleFile() {
		File file = new File(this.filePath);
		return file.isFile();
	}
	
	private boolean sourceIsDirectory() {
		File file = new File(this.filePath);
		return file.isDirectory();
	}

	private boolean validateXML(File xmlFile, File schemaFile) {
		try {			
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaSource = new StreamSource(schemaFile);
			Schema schema = factory.newSchema(schemaSource);
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new FileReader(xmlFile)));
			return true;
		} catch (Exception e) {
			log(LogLevel.DEBUG, e);
			return false;
		}
	}

	private boolean validWordInput() {
		if(!sourceIsSingleFile())
			return false;
		
		try {
			new XWPFDocument(new FileInputStream(new File(filePath)));
			return true;
		} catch (Exception e) {
			try {
				new HWPFDocument(new FileInputStream(new File(filePath)));
				return true;
			} catch (Exception e2) {
				log(LogLevel.DEBUG, e + " " + e2);
				return false;
			}
		}
	}
}
