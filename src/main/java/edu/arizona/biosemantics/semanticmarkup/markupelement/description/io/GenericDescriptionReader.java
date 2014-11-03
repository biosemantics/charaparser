package edu.arizona.biosemantics.semanticmarkup.markupelement.description.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.arizona.biosemantics.semanticmarkup.io.validate.ValidationRun;
import edu.arizona.biosemantics.semanticmarkup.io.validate.lib.IPlantXmlVolumeValidator;
import edu.arizona.biosemantics.semanticmarkup.io.validate.lib.TaxonxVolumeValidator;
import edu.arizona.biosemantics.semanticmarkup.io.validate.lib.XMLVolumeValidator;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.MOXyDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFileList;



public class GenericDescriptionReader implements IDescriptionReader {

	private String xmlSchemaFile;
	private String taxonxSchemaFile;
	private String iplantSchemaFile;
	private String xmlBindingsFile;
	private String taxonxBindingsFile;
	private String iplantBindingsFile;
	private Map<Future<Boolean>, IDescriptionReader> futureValidationResults = new HashMap<Future<Boolean>, IDescriptionReader>();
	
	public GenericDescriptionReader(String xmlSchemaFile,
			String taxonxSchemaFile, String iplantSchemaFile,
			String xmlBindingsFile, String taxonxBindingsFile,
			String iplantBindingsFile,
			Map<Future<Boolean>, IDescriptionReader> futureValidationResults) {
		super();
		this.xmlSchemaFile = xmlSchemaFile;
		this.taxonxSchemaFile = taxonxSchemaFile;
		this.iplantSchemaFile = iplantSchemaFile;
		this.xmlBindingsFile = xmlBindingsFile;
		this.taxonxBindingsFile = taxonxBindingsFile;
		this.iplantBindingsFile = iplantBindingsFile;
		this.futureValidationResults = futureValidationResults;
	}

	@Override
	public DescriptionsFileList read(String inputDirectory) {
		Map<ValidationRun, IDescriptionReader> validationRuns = new HashMap<ValidationRun, IDescriptionReader>();
		validationRuns.put(new ValidationRun(new XMLVolumeValidator(new File(xmlSchemaFile)), new File(inputDirectory)), 
				new MOXyDescriptionReader(xmlBindingsFile));
		validationRuns.put(new ValidationRun(new TaxonxVolumeValidator(new File(taxonxSchemaFile)), new File(inputDirectory)), 
				new MOXyDescriptionReader(taxonxBindingsFile));
		validationRuns.put(new ValidationRun(new IPlantXmlVolumeValidator(new File(iplantSchemaFile)), new File(inputDirectory)),
				new MOXyDescriptionReader(iplantBindingsFile));
		
		ExecutorService executorService = Executors.newFixedThreadPool(validationRuns.size());
		CountDownLatch latch = new CountDownLatch(validationRuns.size());
		
		for(ValidationRun validationRun : validationRuns.keySet()) {
			validationRun.setLatch(latch);
			Future<Boolean> futureResult = executorService.submit(validationRun);
			futureValidationResults.put(futureResult, validationRuns.get(validationRun));
		}
		
		latch.await();
		executorService.shutdown();

		IDescriptionReader descriptionReader = null;
		for(Entry<Future<Boolean>, IDescriptionReader> entry : futureValidationResults.entrySet()) {
			Boolean result = false;
			result = entry.getKey().get();
			if(result) 
				descriptionReader = entry.getValue();
		}
		if(descriptionReader == null) {
			log(LogLevel.ERROR, "No valid format for any of the description readers found. Will exit");
			throw new IllegalArgumentException();
		}
		
		log(LogLevel.DEBUG, "delegate reading treatments to " + descriptionReader.getClass());
		return descriptionReader.read(inputDirectory);
	}

}
