package semanticMarkup.io.input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.input.extract.lib.DistributionTreatmentRefiner;
import semanticMarkup.io.input.extract.lib.FloweringTimeTreatmentRefiner;
import semanticMarkup.io.input.lib.taxonx.TaxonxVolumeReader;
import semanticMarkup.io.input.lib.word.DocWordVolumeReader;
import semanticMarkup.io.input.lib.xml.XMLVolumeReader;
import semanticMarkup.io.input.validate.IValidationRunListener;
import semanticMarkup.io.input.validate.IVolumeValidator;
import semanticMarkup.io.input.validate.ValidationRun;
import semanticMarkup.io.input.validate.lib.TaxonxVolumeValidator;
import semanticMarkup.io.input.validate.lib.WordVolumeValidator;
import semanticMarkup.io.input.validate.lib.XMLVolumeValidator;
import semanticMarkup.log.LogLevel;

/**
 * GenericFileVolumeReader reads takes a filePath to check whether there is input available at the given format 
 * for any of XMLVolumeReader, TaxonxVolumeReader or DocWordVolumeReader. If there is it delegates the actual reading to the appropriate reader
 * @author rodenhausen
 */
public class GenericFileVolumeReader extends AbstractFileVolumeReader implements IValidationRunListener {

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
	private IVolumeReader reader = null;
	private CountDownLatch latch = new CountDownLatch(1);

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
		ValidationRun xmlValidationRun = new ValidationRun(new XMLVolumeValidator(new File(xmlSchemaFile)), new File(filePath));
		ValidationRun taxonxValidationRun = new ValidationRun(new TaxonxVolumeValidator(new File(taxonxSchemaFile)), new File(filePath));
		ValidationRun wordValidationRun = new ValidationRun(new WordVolumeValidator(), new File(filePath));
		xmlValidationRun.addValidationRunListener(this);
		taxonxValidationRun.addValidationRunListener(this);
		wordValidationRun.addValidationRunListener(this);
		
		Thread xmlValidationThread = new Thread(xmlValidationRun);
		Thread taxonxValidationThread = new Thread(taxonxValidationRun);
		Thread wordValidationThread = new Thread(wordValidationRun);

		xmlValidationThread.start();
		taxonxValidationThread.start();
		wordValidationThread.start();
		
		latch.await();

		if(reader != null) {
			log(LogLevel.DEBUG, "delegate reading treatments to " + reader.getClass());
			return reader.read();
		}
		log(LogLevel.ERROR, "no valid reader available for the input given");
		return new ArrayList<Treatment>();
	}

	@Override
	public synchronized void validationDone(boolean result, IVolumeValidator volumeValidator) {
		if(reader != null) 
			return;
		if(result) {
			if(volumeValidator instanceof XMLVolumeValidator) {
				this.reader = new XMLVolumeReader(filePath);
			}
			
			if(volumeValidator instanceof TaxonxVolumeValidator) { 
				this.reader = new TaxonxVolumeReader(filePath);
			}
			
			if(volumeValidator instanceof WordVolumeValidator) {
				this.reader = new DocWordVolumeReader(filePath, styleStartPattern, styleNamePattern,
						styleKeyPattern, tribegennamestyle, styleMappingFile, distributionTreatmentRefiner, floweringTimeTreatmentRefiner, 
						temporaryPath);
			}
			latch.countDown();
		}
	}
}
