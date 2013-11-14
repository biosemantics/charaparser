package semanticMarkup.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.w3c.dom.Document;

import semanticMarkup.eval.IEvaluator;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.lib.CSVGlossary;
import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;
import semanticMarkup.ling.normalize.lib.TreatisehNormalizer;
import semanticMarkup.log.LogLevel;
import semanticMarkup.markup.IMarkupCreator;
import semanticMarkup.markupElement.description.transform.IDescriptionTransformer;
import semanticMarkup.run.IRun;
import semanticMarkup.markupElement.description.run.DescriptionMarkupRun;
import semanticMarkup.markupElement.description.io.IDescriptionReader;
import semanticMarkup.markupElement.description.io.IDescriptionWriter;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.ling.learn.lib.DatabaseInputNoLearner;
import semanticMarkup.markupElement.description.ling.learn.lib.PerlTerminologyLearner;
import semanticMarkup.markupElement.description.markup.DescriptionMarkupCreator;
import semanticMarkup.markupElement.description.markup.IDescriptionMarkupCreator;
import semanticMarkup.markupElement.description.transform.GUIDescriptionTransformer;
import semanticMarkup.markupElement.description.eval.IDescriptionMarkupResultReader;
import semanticMarkup.markupElement.description.eval.io.IDescriptionMarkupEvaluator;
import semanticMarkup.markupElement.description.eval.lib.PerfectPartialPrecisionRecallEvaluator;
import semanticMarkup.markupElement.description.eval.io.lib.MOXyDescriptionMarkupResultReader;
import semanticMarkup.markupElement.description.io.lib.EvaluationDBDescriptionReader;
import semanticMarkup.markupElement.description.io.lib.MOXyDescriptionWriter;
import semanticMarkup.markupElement.description.io.lib.MOXyBinderDescriptionWriter;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Guice config file for parameters of a run
 * @author rodenhausen
 */
public class RunConfig extends BasicConfig {

	// ENVIRONMENTAL
	private String databaseHost = "localhost";
	private String databasePort = "3306";
	private String databaseName = "local";
	private String databaseUser = "termsuser";
	private String databasePassword = "termspassword";
	private String databaseTablePrefix = "myrun";
	private String databaseGlossaryTable = "fnaglossaryfixed";
	private String otoLiteReviewFile = "TermReview.txt";
	private String otoLiteTermReviewURL = "http://biosemantics.arizona.edu:8080/OTOLite/";
	private String otoLiteClientURL = "http://biosemantics.arizona.edu:8080/OTOLite/";
	private String otoClientUrl = "http://biosemantics.arizona.edu:8080/OTO/";
	private String srcDirectory = "src" ;
	private String resourcesDirectory = "resources";
	private String workspaceDirectory = "workspace";
	private String glossaryFile = resourcesDirectory + File.separator + "fnaglossaryfixed.csv";
	private String csvCorpusPath = resourcesDirectory + File.separator + "brown.csv";
	private String wordNetSource = resourcesDirectory + File.separator + "wordNet3.1" + File.separator +"dict" + File.separator;
	//resources//wordNet2.1//dict//  resources//wordNet3.1//dict//
	
	// IO
	private Class<? extends IDescriptionReader> descriptionReader = EvaluationDBDescriptionReader.class;
	private String descriptionReaderInputDirectory = "input";
	private List<String> descriptionReaderBindingsList = createDescriptionReaderBindingsList();
	private List<String> evaluationCorrectReaderBindingsList = createEvaluationCorrectReaderBindingsList();
	private List<String> evaluationTestReaderBindingsList = createEvaluationTestReaderBindingsList();
	private Class<? extends IDescriptionWriter> descriptionWriter = MOXyBinderDescriptionWriter.class;
	private String markupRunValidateSchemaFile = resourcesDirectory + File.separator + "io" + File.separator + "iplantOutputTreatment.xsd";
	
	// PROCESSING 
	private String glossaryType = "plant";
	private Class<? extends IRun> run = DescriptionMarkupRun.class;
	private String runRootDirectory = "workspace" + File.separator + this.databaseTablePrefix;
	private String runOutDirectory = "workspace" + File.separator + this.databaseTablePrefix + File.separator + "out";
	private String runTemporaryDirectory = "workspace" + File.separator + this.databaseTablePrefix + File.separator + "temp";
	private Class<? extends IGlossary> glossary = CSVGlossary.class;
	private Class<? extends IMarkupCreator> markupCreator = DescriptionMarkupCreator.class;
	private Class<? extends IDescriptionMarkupCreator> descriptionMarkupCreator = DescriptionMarkupCreator.class;
	private Class<? extends IDescriptionTransformer> markupDescriptionTreatmentTransformer = GUIDescriptionTransformer.class;
	private boolean markupDescriptionTreatmentTransformerParallelProcessing = false;
	private int markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum = 3; //30
	private int markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum = 3;
	private Class<? extends ITerminologyLearner> terminologyLearner = PerlTerminologyLearner.class;
	private Class<? extends INormalizer> normalizer = FNAv19Normalizer.class;
	private Class<? extends IDescriptionMarkupEvaluator> evaluationRunEvaluator = PerfectPartialPrecisionRecallEvaluator.class;
	private boolean termCategorizationRequired = false;
		
	// MISC
	//required for bioportal submission of oto lite
	private String sourceOfDescriptions = "";
	private String etcUser = "";
	private String bioportalAPIKey = "";
	private String bioportalUserId = "";
	//

	@Override 
	public void configure() {
		super.configure();
		
		// PROCESSING 
		bind(String.class).annotatedWith(Names.named("GlossaryType")).toInstance(glossaryType);
		bind(IRun.class).to(run);
		bind(IGlossary.class).to(glossary).in(Singleton.class);
		bind(String.class).annotatedWith(Names.named("ResourcesDirectory")).toInstance(this.resourcesDirectory);
		bind(String.class).annotatedWith(Names.named("SrcDirectory")).toInstance(this.srcDirectory);
		bind(String.class).annotatedWith(Names.named("WorkspaceDirectory")).toInstance(this.workspaceDirectory);
		bind(String.class).annotatedWith(Names.named("Run_RootDirectory")).toInstance(workspaceDirectory + File.separator + this.databaseTablePrefix);
		bind(String.class).annotatedWith(Names.named("Run_OutDirectory")).toInstance(workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "out");
		bind(String.class).annotatedWith(Names.named("Run_TemporaryPath")).toInstance(workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "temp");

		bind(IMarkupCreator.class).annotatedWith(Names.named("MarkupCreator")).to(markupCreator).in(Singleton.class);
		bind(IDescriptionMarkupCreator.class).to(descriptionMarkupCreator).in(Singleton.class);
		bind(IDescriptionTransformer.class).to(markupDescriptionTreatmentTransformer).in(Singleton.class);
		bind(boolean.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_ParallelProcessing")).toInstance(markupDescriptionTreatmentTransformerParallelProcessing);
		bind(int.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_DescriptionExtractorRunMaximum")).toInstance(markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum);
		bind(int.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_SentenceChunkerRunMaximum")).toInstance(markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum);
		bind(ITerminologyLearner.class).to(terminologyLearner ).in(Singleton.class); 
		bind(INormalizer.class).to(normalizer).in(Singleton.class);
		bind(IDescriptionMarkupEvaluator.class).annotatedWith(Names.named("EvaluationRun_Evaluator")).to(evaluationRunEvaluator);
		bind(Boolean.class).annotatedWith(Names.named("termCategorizationRequired")).toInstance(termCategorizationRequired);
		
		//IO
		bind(IDescriptionReader.class).annotatedWith(Names.named("DescriptionMarkupCreator_DescriptionReader")).to(descriptionReader).in(Singleton.class);
		bind(String.class).annotatedWith(Names.named("DescriptionReader_InputDirectory")).toInstance(descriptionReaderInputDirectory);
		bind(new TypeLiteral<List<String>>() {}).annotatedWith(Names.named("DescriptionReader_BindingsFiles")).toInstance(descriptionReaderBindingsList);
		bind(new TypeLiteral<Set<String>>() {}).annotatedWith(Names.named("SelectedSources")).toInstance(getSelectedSources(descriptionReaderInputDirectory));
		bind(IDescriptionMarkupResultReader.class).annotatedWith(Names.named("EvaluationRun_CorrectReader")).toInstance(constructEvaluationCorrectReader());
		bind(IDescriptionMarkupResultReader.class).annotatedWith(Names.named("EvaluationRun_TestReader")).toInstance(constructEvaluationTestReader());
		bind(IDescriptionWriter.class).annotatedWith(Names.named("DescriptionMarkupCreator_DescriptionWriter")).to(descriptionWriter).in(Singleton.class);
		bind(String.class).annotatedWith(Names.named("MarkupRun_ValidateSchemaFile")).toInstance(markupRunValidateSchemaFile);
		
		//ENVIRONMENTAL
		bind(String.class).annotatedWith(Names.named("DatabasePrefix")).toInstance(databaseTablePrefix); 
		bind(String.class).annotatedWith(Names.named("DatabaseHost")).toInstance(databaseHost);
		bind(String.class).annotatedWith(Names.named("DatabasePort")).toInstance(databasePort);
		bind(String.class).annotatedWith(Names.named("DatabaseName")).toInstance(databaseName);
		bind(String.class).annotatedWith(Names.named("DatabaseUser")).toInstance(databaseUser);
		bind(String.class).annotatedWith(Names.named("DatabasePassword")).toInstance(databasePassword);
		bind(String.class).annotatedWith(Names.named("OTOLiteReviewFile")).toInstance(otoLiteReviewFile);
		bind(String.class).annotatedWith(Names.named("OTOLiteTermReviewURL")).toInstance(otoLiteTermReviewURL);
		bind(String.class).annotatedWith(Names.named("OTOLiteClient_Url")).toInstance(otoLiteClientURL);
		bind(String.class).annotatedWith(Names.named("OTOClient_Url")).toInstance(otoClientUrl);
		bind(String.class).annotatedWith(Names.named("GlossaryTable")).toInstance(databaseGlossaryTable);
		bind(String.class).annotatedWith(Names.named("CSVCorpus_filePath")).toInstance(csvCorpusPath);
		bind(String.class).annotatedWith(Names.named("WordNetAPI_Sourcefile")).toInstance(wordNetSource);
		//resources//wordNet2.1//dict//  resources//wordNet3.1//dict//
		bind(String.class).annotatedWith(Names.named("CSVGlossary_FilePath")).toInstance(glossaryFile); 
		
		//MISC
		bind(String.class).annotatedWith(Names.named("GuiceModuleFile")).toInstance(this.toString());
		bind(String.class).annotatedWith(Names.named("SourceOfDescriptions")).toInstance(sourceOfDescriptions);
		bind(String.class).annotatedWith(Names.named("EtcUser")).toInstance(etcUser);
		bind(String.class).annotatedWith(Names.named("BioportalAPIKey")).toInstance(bioportalAPIKey);
		bind(String.class).annotatedWith(Names.named("BioportalUserId")).toInstance(bioportalUserId);
	}
	
	private List<String> createEvaluationTestReaderBindingsList() {
		List<String> result = new LinkedList<String>();
		result.add("resources" + File.separator + "eval" + File.separator + "bindings" + File.separator + "baseBindings.xml");
		result.add("resources" + File.separator + "eval" + File.separator + "bindings" + File.separator + "testBindings.xml");
		return result;
	}

	private List<String> createEvaluationCorrectReaderBindingsList() {
		List<String> result = new LinkedList<String>();
		result.add("resources" + File.separator + "eval" + File.separator + "bindings" + File.separator + "baseBindings.xml");
		result.add("resources" + File.separator + "eval" + File.separator + "bindings" + File.separator + "correctBindings.xml");
		return result;
	}

	private List<String> createDescriptionReaderBindingsList() {
		List<String> result = new LinkedList<String>();
		result.add("resources" + File.separator + "io" + File.separator + "bindings" + File.separator + "semanticMarkup.markupElement.description.model" + File.separator + "baseBindings.xml");
		result.add("resources" + File.separator + "io" + File.separator + "bindings" + File.separator + "semanticMarkup.markupElement.description.model" + File.separator + "singleTreatmentDescriptionBindings.xml");
		return result;
	}

	private IDescriptionMarkupResultReader constructEvaluationTestReader() {
		try {
			return new MOXyDescriptionMarkupResultReader(evaluationTestReaderBindingsList);
		} catch(Exception e) {
			log(LogLevel.ERROR, "Exception instantiating MOXyDescriptionMarkupResultReader", e);
			System.exit(0);
		}
		return null;
	}

	private IDescriptionMarkupResultReader constructEvaluationCorrectReader() {
		try {
			return new MOXyDescriptionMarkupResultReader(evaluationCorrectReaderBindingsList);
		} catch(Exception e) {
			log(LogLevel.ERROR, "Exception instantiating MOXyDescriptionMarkupResultReader", e);
			System.exit(0);
		}
		return null;
	}

	protected HashSet<String> getSelectedSources(String path) {
		HashSet<String> result = new HashSet<String>();

		/*result.add("1297.txt-1");
		result.add("1297.txt-2");
		result.add("1297.txt-3");
		result.add("1297.txt-4");
		result.add("1297.txt-5");
		result.add("1297.txt-6");
		result.add("1297.txt-7");
		result.add("1297.txt-8");
		result.add("1297.txt-9");
		result.add("1297.txt-10");
		result.add("1297.txt-11");
		result.add("1297.txt-12");
		result.add("1297.txt-13");
		result.add("1297.txt-14");
		result.add("1297.txt-15");
		result.add("1297.txt-16");
		result.add("1297.txt-17");
		result.add("1297.txt-18");
		result.add("1297.txt-19");
		result.add("1297.txt-20");
		result.add("1297.txt-21");
		result.add("1297.txt-22");
		result.add("1297.txt-23");
		result.add("1297.txt-24");
		result.add("1297.txt-25");
		result.add("1297.txt-26");
		result.add("1297.txt-27");
		result.add("1297.txt-28");
		result.add("1297.txt-29");
		result.add("1297.txt-30");*/
		
		//result.add("568.txt-8");
		//result.add("2110.txt-1");
		//result.add("1196.txt-4");
		
		//result.add("3.txt-3");
		
		//result.add("735.txt-21");
		//result.add("121.txt-3");
		//result.add("765.txt-6");
		
		
		/*String file;
		File folder = new File(path);
		if (folder.exists()) {
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					file = listOfFiles[i].getName();
					file = file.replace(".xml", "");
					// if(file.startsWith("175") || file.startsWith("174"))
					// if(file.equals("346.txt-15"))
					// if(file.equals("349.txt-1"))
					// if(file.equals("369.txt-11"))
					// if(file.equals("177.txt-2"))
					// if(file.equals("108.txt-9"))
					// if(file.equals("788.txt-3"))
					// if(file.equals("203.txt-2"))
					// if(file.equals("131.txt-5"))
					// if(file.equals("212.txt-4"))
					// if(file.equals("118.txt-1"))
					// if(file.equals("359.txt-10"))
					// if(file.equals("346.txt-15"))
					// if(file.equals("120.txt-1"))
					// if(file.equals("163.txt-7"))
					// if(file.equals("544.txt-6"))
					// if(file.equals("51.txt-13"))
					//if(file.equals("15.txt-7") || file.equals("15.txt-11") || file.equals("148.txt-12"))
					//if(file.equals("148.txt-12"))
					//if(file.equals("132.txt-18"))
					//if(file.equals("165.txt-2"))
					
					//if(file.equals("462.txt-6"))
					//if(file.equals("474.txt-7"))
					//if(file.equals("108.txt-9"))
					//if(file.equals("174.txt-6"))
					//if(file.equals("578.txt-7"))
					//if(file.equals("111.txt-8"))
					//if(file.equals("516.txt-7"))
					//if(file.equals("754.txt-11"))
					//if(file.equals("765.txt-2"))
					//if(file.equals("264.txt-3"))
					//if(file.equals("878.txt-3"))
					//if(file.equals("100.txt-12"))
					//if(file.equals("491.txt-4"))
					if(file.equals("797.txt-2"))
						result.add(file);
					// break; //TODO remove. only for test
				}
			}
		}*/
		//result.add("000.txt-9");
		//result.add("000.txt-8");
		//result.add("000.txt-6");
		//result.add("002.txt-4");
		//result.add("006.txt-2");
		//result.add("005.txt-4");
		//result.add("005.txt-0");
		//result.add("004.txt-4");
		//result.add("004.txt-0");
		//result.add("004.txt-5");
		//result.add("020.txt-8");
		//result.add("000.txt-11");
		//result.add("002.txt-11");
		//result.add("022.txt-6");
		return result;
	}

	public Class<? extends IRun> getRun() {
		return run;
	}

	public void setRun(Class<? extends IRun> run) {
		this.run = run;
	}

	public Class<? extends IEvaluator> getEvaluationRunEvaluator() {
		return evaluationRunEvaluator;
	}

	public Class<? extends IMarkupCreator> getMarkupCreator() {
		return markupCreator;
	}

	public void setMarkupCreator(Class<? extends IMarkupCreator> markupCreator) {
		this.markupCreator = markupCreator;
	}

	public Class<? extends IDescriptionTransformer> getMarkupDescriptionTreatmentTransformer() {
		return markupDescriptionTreatmentTransformer;
	}

	public void setMarkupDescriptionTreatmentTransformer(
			Class<? extends IDescriptionTransformer> markupDescriptionTreatmentTransformer) {
		this.markupDescriptionTreatmentTransformer = markupDescriptionTreatmentTransformer;
	}

	public boolean isMarkupDescriptionTreatmentTransformerParallelProcessing() {
		return markupDescriptionTreatmentTransformerParallelProcessing;
	}

	public void setMarkupDescriptionTreatmentTransformerParallelProcessing(
			boolean markupDescriptionTreatmentTransformerParallelProcessing) {
		this.markupDescriptionTreatmentTransformerParallelProcessing = markupDescriptionTreatmentTransformerParallelProcessing;
	}

	public int getMarkupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum() {
		return markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum;
	}

	public void setMarkupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum(
			int markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum) {
		this.markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum = markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum;
	}

	public int getMarkupDescriptionTreatmentTransformerSentenceChunkerRunMaximum() {
		return markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum;
	}

	public void setMarkupDescriptionTreatmentTransformerSentenceChunkerRunMaximum(
			int markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum) {
		this.markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum = markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDatabaseUser() {
		return databaseUser;
	}

	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	public String getDatabaseTablePrefix() {
		return databaseTablePrefix;
	}

	public void setDatabaseTablePrefix(String databaseTablePrefix) {
		this.databaseTablePrefix = databaseTablePrefix;
		this.runRootDirectory = "workspace" + File.separator + this.databaseTablePrefix;
		this.runOutDirectory = "workspace" + File.separator + this.databaseTablePrefix + File.separator + "out";
		this.runTemporaryDirectory = "workspace" + File.separator + this.databaseTablePrefix + File.separator + "temp";
		
	}

	public String getDatabaseGlossaryTable() {
		return databaseGlossaryTable;
	}

	public void setDatabaseGlossaryTable(String databaseGlossaryTable) {
		this.databaseGlossaryTable = databaseGlossaryTable;
	}

	public String getGlossaryFile() {
		return glossaryFile;
	}

	public void setGlossaryFile(String glossaryFile) {
		this.glossaryFile = glossaryFile;
	}

	public Class<? extends INormalizer> getNormalizer() {
		return normalizer;
	}

	public void setNormalizer(Class<? extends INormalizer> normalizer) {
		this.normalizer = normalizer;
	}

	public Class<? extends IGlossary> getGlossary() {
		return glossary;
	}

	public void setGlossary(Class<? extends IGlossary> glossary) {
		this.glossary = glossary;
	}

	public Class<? extends ITerminologyLearner> getTerminologyLearner() {
		return terminologyLearner;
	}

	public void setTerminologyLearner(
			Class<? extends ITerminologyLearner> terminologyLearner) {
		this.terminologyLearner = terminologyLearner;
	}

	public String getOtoClientUrl() {
		return otoClientUrl;
	}

	public void setOtoClientUrl(String otoClientUrl) {
		this.otoClientUrl = otoClientUrl;
	}

	public String getDatabaseHost() {
		return databaseHost;
	}

	public void setDatabaseHost(String databaseHost) {
		this.databaseHost = databaseHost;
	}

	public String getDatabasePort() {
		return databasePort;
	}

	public void setDatabasePort(String databasePort) {
		this.databasePort = databasePort;
	}

	public String getGlossaryType() {
		return glossaryType;
	}

	public void setGlossaryType(String glossaryType) {
		this.glossaryType = glossaryType;
	}

	public String getOtoLiteReviewFile() {
		return otoLiteReviewFile;
	}

	public void setOtoLiteReviewFile(String otoLiteReviewFile) {
		this.otoLiteReviewFile = otoLiteReviewFile;
	}

	public String getOtoLiteTermReviewURL() {
		return otoLiteTermReviewURL;
	}

	public void setOtoLiteTermReviewURL(String otoLiteTermReviewURL) {
		this.otoLiteTermReviewURL = otoLiteTermReviewURL;
	}

	public String getOtoLiteClientURL() {
		return otoLiteClientURL;
	}

	public void setOtoLiteClientURL(String otoLiteClientURL) {
		this.otoLiteClientURL = otoLiteClientURL;
	}

	public String getRunRootDirectory() {
		return runRootDirectory;
	}

	public void setRunRootDirectory(String runRootDirectory) {
		this.runRootDirectory = runRootDirectory;
	}

	public String getRunOutDirectory() {
		return runOutDirectory;
	}

	public void setRunOutDirectory(String runOutDirectory) {
		this.runOutDirectory = runOutDirectory;
	}

	public String getRunTemporaryDirectory() {
		return runTemporaryDirectory;
	}

	public void setRunTemporaryDirectory(String runTemporaryDirectory) {
		this.runTemporaryDirectory = runTemporaryDirectory;
	}

	public Class<? extends IDescriptionReader> getDescriptionReader() {
		return descriptionReader;
	}

	public void setDescriptionReader(
			Class<? extends IDescriptionReader> descriptionReader) {
		this.descriptionReader = descriptionReader;
	}

	public Class<? extends IDescriptionWriter> getDescriptionWriter() {
		return descriptionWriter;
	}

	public void setDescriptionWriter(Class<? extends IDescriptionWriter> descriptionWriter) {
		this.descriptionWriter = descriptionWriter;
	}

	public void setEvaluationRunEvaluator(
			Class<? extends IDescriptionMarkupEvaluator> evaluationRunEvaluator) {
		this.evaluationRunEvaluator = evaluationRunEvaluator;
	}

	public String getDescriptionReaderInputDirectory() {
		return descriptionReaderInputDirectory;
	}

	public void setDescriptionReaderInputDirectory(
			String descriptionReaderInputDirectory) {
		this.descriptionReaderInputDirectory = descriptionReaderInputDirectory;
	}

	public List<String> getDescriptionReaderBindingsList() {
		return descriptionReaderBindingsList;
	}

	public void setDescriptionReaderBindingsList(
			List<String> descriptionReaderBindingsList) {
		this.descriptionReaderBindingsList = descriptionReaderBindingsList;
	}

	public List<String> getEvaluationCorrectReaderBindingsList() {
		return evaluationCorrectReaderBindingsList;
	}

	public void setEvaluationCorrectReaderBindingsList(
			List<String> evaluationCorrectReaderBindingsList) {
		this.evaluationCorrectReaderBindingsList = evaluationCorrectReaderBindingsList;
	}

	public List<String> getEvaluationTestReaderBindingsList() {
		return evaluationTestReaderBindingsList;
	}

	public void setEvaluationTestReaderBindingsList(
			List<String> evaluationTestReaderBindingsList) {
		this.evaluationTestReaderBindingsList = evaluationTestReaderBindingsList;
	}

	public Class<? extends IDescriptionMarkupCreator> getDescriptionMarkupCreator() {
		return descriptionMarkupCreator;
	}

	public void setDescriptionMarkupCreator(
			Class<? extends IDescriptionMarkupCreator> descriptionMarkupCreator) {
		this.descriptionMarkupCreator = descriptionMarkupCreator;
	}
	
	public void setResourcesDirectory(String resourcesDirectory) {
		String oldResourcesDirectory = this.getResourcesDirectory();
		this.csvCorpusPath = this.csvCorpusPath.replace(oldResourcesDirectory, resourcesDirectory);
		this.csvCorpusPath.replace(oldResourcesDirectory, resourcesDirectory);
		this.wordNetSource.replace(oldResourcesDirectory, resourcesDirectory);
		this.glossaryFile.replace(oldResourcesDirectory, resourcesDirectory);
		this.markupRunValidateSchemaFile = this.markupRunValidateSchemaFile.replace(oldResourcesDirectory, resourcesDirectory);
	}

	public void setSrcDirectory(String srcDirectory) {
		this.srcDirectory = srcDirectory;
	}

	public String getSrcDirectory() {
		return srcDirectory;
	}

	public String getResourcesDirectory() {
		return resourcesDirectory;
	}

	public void setWorkspaceDirectory(String workspaceDirectory) {
		this.workspaceDirectory = workspaceDirectory;
	}

	public String getWorkspaceDirectory() {
		return workspaceDirectory;
	}
	
	public String getSourceOfDescriptions() {
		return sourceOfDescriptions;
	}

	public void setSourceOfDescriptions(String sourceOfDescriptions) {
		this.sourceOfDescriptions = sourceOfDescriptions;
	}

	public String getEtcUser() {
		return etcUser;
	}

	public void setEtcUser(String etcUser) {
		this.etcUser = etcUser;
	}

	public String getBioportalAPIKey() {
		return bioportalAPIKey;
	}

	public void setBioportalAPIKey(String bioportalAPIKey) {
		this.bioportalAPIKey = bioportalAPIKey;
	}

	public String getBioportalUserId() {
		return bioportalUserId;
	}

	public void setBioportalUserId(String bioportalUserId) {
		this.bioportalUserId = bioportalUserId;
	}

	public boolean isTermCategorizationRequired() {
		return termCategorizationRequired;
	}

	public void setTermCategorizationRequired(boolean termCategorizationRequired) {
		this.termCategorizationRequired = termCategorizationRequired;
	}

	public String getMarkupRunValidateSchemaFile() {
		return markupRunValidateSchemaFile;
	}

	public void setMarkupRunValidateSchemaFile(String markupRunValidateSchemaFile) {
		this.markupRunValidateSchemaFile = markupRunValidateSchemaFile;
	}
}
