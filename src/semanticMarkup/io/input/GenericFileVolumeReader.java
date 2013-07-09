package semanticMarkup.io.input;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import semanticMarkup.io.input.lib.iplant.IPlantXMLVolumeReader;
import semanticMarkup.io.input.lib.taxonx.TaxonxVolumeReader;
import semanticMarkup.io.input.lib.word.DocWordVolumeReader;
import semanticMarkup.io.input.lib.word.refiner.DistributionTreatmentRefiner;
import semanticMarkup.io.input.lib.word.refiner.FloweringTimeTreatmentRefiner;
import semanticMarkup.io.input.lib.xml.XMLVolumeReader;
import semanticMarkup.io.validate.ValidationRun;
import semanticMarkup.io.validate.lib.IPlantXmlVolumeValidator;
import semanticMarkup.io.validate.lib.TaxonxVolumeValidator;
import semanticMarkup.io.validate.lib.WordVolumeValidator;
import semanticMarkup.io.validate.lib.XMLVolumeValidator;
import semanticMarkup.log.LogLevel;
import semanticMarkup.model.Treatment;

import com.google.inject.Inject;
import com.google.inject.name.Named;

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
	private String taxonxSchemaFile;
	private String xmlSchemaFile;
	private Map<Future<Boolean>, IVolumeReader> futureValidationResults = new HashMap<Future<Boolean>, IVolumeReader>();
	private String iplantXmlSchemaFile;

	/**
	 * @param filePath
	 * @param styleStartPattern
	 * @param styleNamePattern
	 * @param styleKeyPattern
	 * @param tribegennamestyle
	 * @param styleMappingFile
	 * @param distributionTreatmentRefiner
	 * @param floweringTimeTreatmentRefiner
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
			@Named("Taxonx_SchemaFile")String taxonxSchemaFile,
			@Named("XML_SchemaFile")String xmlSchemaFile, 
			@Named("iPlantXML_SchemaFile")String iplantXmlSchemaFile) {
		super(filePath);
		this.styleStartPattern = styleStartPattern;
		this.styleNamePattern = styleNamePattern;
		this.styleKeyPattern = styleKeyPattern;
		this.tribegennamestyle = tribegennamestyle;
		this.styleMappingFile = styleMappingFile;
		this.distributionTreatmentRefiner = distributionTreatmentRefiner;
		this.floweringTimeTreatmentRefiner = floweringTimeTreatmentRefiner;
		this.taxonxSchemaFile = taxonxSchemaFile;
		this.xmlSchemaFile = xmlSchemaFile;
		this.iplantXmlSchemaFile = iplantXmlSchemaFile;
	}

	@Override
	public List<Treatment> read() throws Exception {
		Map<ValidationRun, IVolumeReader> validationRuns = new HashMap<ValidationRun, IVolumeReader>();
		validationRuns.put(new ValidationRun(new XMLVolumeValidator(new File(xmlSchemaFile)), new File(filePath)), 
				new XMLVolumeReader(filePath));
		validationRuns.put(new ValidationRun(new TaxonxVolumeValidator(new File(taxonxSchemaFile)), new File(filePath)), 
				new TaxonxVolumeReader(filePath));
		validationRuns.put(new ValidationRun(new IPlantXmlVolumeValidator(new File(iplantXmlSchemaFile)), new File(filePath)),
				new IPlantXMLVolumeReader(filePath));
		validationRuns.put(new ValidationRun(new WordVolumeValidator(), new File(filePath)),
				new DocWordVolumeReader(filePath, styleStartPattern, styleNamePattern,
						styleKeyPattern, tribegennamestyle, styleMappingFile, distributionTreatmentRefiner, floweringTimeTreatmentRefiner));
		
		ExecutorService executorService = Executors.newFixedThreadPool(validationRuns.size());
		CountDownLatch latch = new CountDownLatch(validationRuns.size());
		
		for(ValidationRun validationRun : validationRuns.keySet()) {
			validationRun.setLatch(latch);
			Future<Boolean> futureResult = executorService.submit(validationRun);
			futureValidationResults.put(futureResult, validationRuns.get(validationRun));
		}
		
		latch.await();
		executorService.shutdown();

		IVolumeReader volumeReader = null;
		for(Entry<Future<Boolean>, IVolumeReader> entry : futureValidationResults.entrySet()) {
			Boolean result = false;
			result = entry.getKey().get();
			if(result) 
				volumeReader = entry.getValue();
		}
		if(volumeReader == null) {
			log(LogLevel.ERROR, "No valid format for any of the volume readers found. Will exit");
			System.exit(0);
		}
		
		log(LogLevel.DEBUG, "delegate reading treatments to " + volumeReader.getClass());
		return volumeReader.read();
	}
}
