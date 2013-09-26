package semanticMarkup.config;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import semanticMarkup.core.transformation.TreatmentTransformerChain;
import semanticMarkup.core.transformation.lib.CharaparserTreatmentTransformerChain;
import semanticMarkup.core.transformation.lib.description.DescriptionTreatmentTransformer;
import semanticMarkup.core.transformation.lib.description.GUIDescriptionTreatmentTransformer;
import semanticMarkup.eval.IEvaluator;
import semanticMarkup.eval.PerfectPartialPrecisionRecallEvaluator;
import semanticMarkup.io.input.GenericFileVolumeReader;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.io.input.lib.db.EvaluationDBVolumeReader;
import semanticMarkup.io.input.lib.xml.XMLVolumeReader;
import semanticMarkup.io.output.IVolumeWriter;
import semanticMarkup.io.output.lib.xml.XMLVolumeWriter;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.lib.CSVGlossary;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.learn.lib.DatabaseInputNoLearner;
import semanticMarkup.ling.learn.lib.PerlTerminologyLearner;
import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;
import semanticMarkup.ling.normalize.lib.TreatisehNormalizer;
import semanticMarkup.markup.DescriptionMarkupCreator;
import semanticMarkup.markup.IMarkupCreator;
import semanticMarkup.run.IRun;
import semanticMarkup.run.MarkupRun;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Guice config file for parameters of a run
 * @author rodenhausen
 */
public class RunConfig extends BasicConfig {

	private String srcDirectory = "src" ;
	private String resourcesDirectory = "resources";
	private String workspaceDirectory = "workspace";
	private Class<? extends IRun> run = MarkupRun.class;
	//MarkupRun, EvaluationRun, MarkupEvaluationRun
	private Class<? extends IGlossary> glossary = CSVGlossary.class;
	private Class<? extends IEvaluator> evaluationRunEvaluator = PerfectPartialPrecisionRecallEvaluator.class;
	//SimplePrecisionRecallEvaluator, AdvancedPrecisionRecallEvaluator
	private Class<? extends IVolumeReader> evaluationGoldStandardReader = XMLVolumeReader.class;
	private Class<? extends IVolumeReader> evaluationRunCreatedVolumeReader = EvaluationDBVolumeReader.class;
	//OutputVolumeReader, EvaluationDBVolumeReader, PerlDBVolumeReader
	private Class<? extends IMarkupCreator> markupCreator = DescriptionMarkupCreator.class;
	//CharaParser.class //AfterPerlBlackBox
	private Class<? extends IVolumeReader> markupCreatorVolumeReader = EvaluationDBVolumeReader.class;
	//WordVolumeReader, XMLVolumeReader, PerlDBVolumeReader, EvaluationDBVolumeReader, GenericFileVolumeReaer
	private String iPlantXMLVolumeReaderSource = "";
	
	private String wordVolumeReaderSourceFile = "evaluationData" + File.separator + "FNA-v19-excerpt_Type1" + File.separator + 
			"source" + File.separator + "FNA19 Excerpt-source.docx";
	private String wordVolumeReaderStyleStartPattern = ".*?(Heading|Name).*";
	private String wordVolumeReaderStyleNamePattern = ".*?(Syn|Name).*";
	private String wordVolumeReaderStyleKeyPattern =  ".*?-Key.*";
	private String wordVolumeReaderTribegennamestyle = "caps";
	private String wordVolumeReaderStyleMappingFile = resourcesDirectory + File.separator + "stylemapping.properties";
	private String xmlVolumeReaderSourceDirectory = "evaluationData" + File.separator + "perlTest" + File.separator + "source" +
			File.separator;
	private String outputVolumeReaderSourceDirectory = "." + File.separator + "out" + File.separator;
	private String genericFileVolumeReaderSource = "evaluationData" + File.separator + "perlTest" + File.separator + "source" + File.separator;
	private String taxonxSchemaFile = resourcesDirectory + File.separator + "io" + File.separator + "taxonx" + File.separator + "taxonx1.xsd";
	private String xmlSchemaFile = resourcesDirectory + File.separator + "io" + File.separator + "FNAXMLSchemaInput.xsd";
	private String iplantSchemaFile = resourcesDirectory + File.separator + "io" + File.separator + "iplant.xsd";
	
	//"evaluationData" + File.separator + "DonatAnts_Type4" + File.separator + "source" + File.separator + "8538_pyr_mad_tx1.xml"
	//"evaluationData" + File.separator + "FNA-v19-excerpt_Type1" + File.separator + "source" + File.separator + "FNA19 Excerpt-source.docx"
	private String taxonxVolumeReaderSourceFile = "evaluationData" + File.separator + "DonatAnts_Type4" + File.separator + "source" + File.separator + "8538_pyr_mad_tx1.xml";
	private Class<? extends TreatmentTransformerChain> treatmentTransformerChain = CharaparserTreatmentTransformerChain.class;
	private Class<? extends DescriptionTreatmentTransformer> markupDescriptionTreatmentTransformer = GUIDescriptionTreatmentTransformer.class;
	private boolean markupDescriptionTreatmentTransformerParallelProcessing = false;
	private int markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum = 3; //30
	private int markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum = 3;
	private String databaseHost = "localhost";
	private String databasePort = "3306";
	private String databaseName = "local";
	private String databaseUser = "termsuser";
	private String databasePassword = "termspassword";
	private Class<? extends IVolumeWriter> volumeWriter = XMLVolumeWriter.class;
	//ToStringVolumeWriter, JSONVolumeWriter, XMLVolumeWriter XML2VolumeWriter
	
	private String standardVolumeReaderSourcefiles = "evaluationData" + File.separator + "FNAV19_AnsKey_CharaParser_Evaluation";
	//FNAV19_AnsKey_CharaParser_Evaluation , TIP_AnsKey_CharaParser_Evaluation
	private String databaseTablePrefix = "myrun";
	private String glossaryType = "plant";
	private String databaseGlossaryTable = "fnaglossaryfixed";
	private String csvCorpusPath = resourcesDirectory + File.separator + "brown.csv";
	private String wordNetSource = resourcesDirectory + File.separator + "wordNet3.1" + File.separator +"dict" + File.separator;
	//resources//wordNet2.1//dict//  resources//wordNet3.1//dict//
	private String glossaryFile = resourcesDirectory + File.separator + "fnaglossaryfixed.csv";
	private String otoLiteReviewFile = "TermReview.txt";
	private String otoLiteTermReviewURL = "http://biosemantics.arizona.edu:8080/OTOLite/";
	private String otoLiteClientURL = "http://biosemantics.arizona.edu:8080/OTOLite/";
	private String otoClientUrl = "http://biosemantics.arizona.edu:8080/OTO/";
	private Class<? extends INormalizer> normalizer = FNAv19Normalizer.class;
	private Class<? extends ITerminologyLearner> terminologyLearner = PerlTerminologyLearner.class;
	//PerlTerminologyLearner //DatabaseInputNoLearner;
	
	@Override 
	public void configure() {
		super.configure();
		bind(IRun.class).to(run);
		bind(IGlossary.class).to(glossary).in(Singleton.class);
		bind(String.class).annotatedWith(Names.named("ResourcesDirectory")).toInstance(this.resourcesDirectory);
		bind(String.class).annotatedWith(Names.named("SrcDirectory")).toInstance(this.srcDirectory);
		bind(String.class).annotatedWith(Names.named("WorkspaceDirectory")).toInstance(this.workspaceDirectory);
		bind(String.class).annotatedWith(Names.named("Run_RootDirectory")).toInstance(workspaceDirectory + File.separator + this.databaseTablePrefix);
		bind(String.class).annotatedWith(Names.named("Run_OutDirectory")).toInstance(workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "out");
		bind(String.class).annotatedWith(Names.named("Run_TemporaryPath")).toInstance(workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "temp");
		bind(IEvaluator.class).annotatedWith(Names.named("EvaluationRun_Evaluator")).to(evaluationRunEvaluator);
		bind(IVolumeReader.class).annotatedWith(Names.named("EvaluationRun_GoldStandardReader")).to(evaluationGoldStandardReader);
		bind(IVolumeReader.class).annotatedWith(Names.named("EvaluationRun_CreatedVolumeReader")).to(evaluationRunCreatedVolumeReader);
		bind(IMarkupCreator.class).annotatedWith(Names.named("MarkupCreator")).to(markupCreator);
		bind(IVolumeReader.class).annotatedWith(Names.named("MarkupCreator_VolumeReader")).to(markupCreatorVolumeReader);
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_Sourcefile")).toInstance(wordVolumeReaderSourceFile);
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_StyleStartPattern")).toInstance(wordVolumeReaderStyleStartPattern);
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_StyleNamePattern")).toInstance(wordVolumeReaderStyleNamePattern);
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_StyleKeyPattern")).toInstance(wordVolumeReaderStyleKeyPattern);
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_Tribegennamestyle")).toInstance(wordVolumeReaderTribegennamestyle);
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_StyleMappingFile")).toInstance(wordVolumeReaderStyleMappingFile);
		bind(String.class).annotatedWith(Names.named("XMLVolumeReader_SourceDirectory")).toInstance(xmlVolumeReaderSourceDirectory);
		bind(String.class).annotatedWith(Names.named("IPlantXMLVolumeReader_Source")).toInstance(iPlantXMLVolumeReaderSource);
		bind(String.class).annotatedWith(Names.named("OutputVolumeReader_SourceDirectory")).toInstance(outputVolumeReaderSourceDirectory);
		bind(String.class).annotatedWith(Names.named("GenericFileVolumeReader_Source")).toInstance(genericFileVolumeReaderSource);
		bind(String.class).annotatedWith(Names.named("Taxonx_SchemaFile")).toInstance(taxonxSchemaFile);
		bind(String.class).annotatedWith(Names.named("XML_SchemaFile")).toInstance(xmlSchemaFile);
		bind(String.class).annotatedWith(Names.named("iPlantXML_SchemaFile")).toInstance(iplantSchemaFile);
		
		bind(String.class).annotatedWith(Names.named("TaxonxVolumeReader_SourceFile")).toInstance(taxonxVolumeReaderSourceFile);
		bind(String.class).annotatedWith(Names.named("OTOClient_Url")).toInstance(otoClientUrl);
		bind(ITerminologyLearner.class).to(terminologyLearner ).in(Singleton.class);
		bind(TreatmentTransformerChain.class).to(treatmentTransformerChain);
		bind(DescriptionTreatmentTransformer.class).to(markupDescriptionTreatmentTransformer);
		bind(boolean.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_parallelProcessing")).toInstance(markupDescriptionTreatmentTransformerParallelProcessing);
		bind(int.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_descriptionExtractorRunMaximum")).toInstance(markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum);
		bind(int.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_sentenceChunkerRunMaximum")).toInstance(markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum);
		bind(String.class).annotatedWith(Names.named("databaseHost")).toInstance(databaseHost);
		bind(String.class).annotatedWith(Names.named("databasePort")).toInstance(databasePort);
		bind(String.class).annotatedWith(Names.named("databaseName")).toInstance(databaseName);
		bind(String.class).annotatedWith(Names.named("databaseUser")).toInstance(databaseUser);
		bind(String.class).annotatedWith(Names.named("databasePassword")).toInstance(databasePassword);
		bind(IVolumeWriter.class).annotatedWith(Names.named("MarkupCreator_VolumeWriter")).to(volumeWriter);
		
		bind(String.class).annotatedWith(Names.named("GuiceModuleFile")).toInstance(this.toString());
		bind(String.class).annotatedWith(Names.named("StandardVolumeReader_Sourcefiles")).toInstance(standardVolumeReaderSourcefiles);
		bind(new TypeLiteral<Set<String>>() {}).annotatedWith(Names.named("selectedSources")).toInstance(getSelectedSources(standardVolumeReaderSourcefiles));
		bind(String.class).annotatedWith(Names.named("databasePrefix")).toInstance(databaseTablePrefix); 
		bind(String.class).annotatedWith(Names.named("glossaryType")).toInstance(glossaryType);
		bind(String.class).annotatedWith(Names.named("otoLiteReviewFile")).toInstance(otoLiteReviewFile);
		bind(String.class).annotatedWith(Names.named("otoLiteTermReviewURL")).toInstance(otoLiteTermReviewURL);
		bind(String.class).annotatedWith(Names.named("OTOLiteClient_Url")).toInstance(otoLiteClientURL);
		bind(String.class).annotatedWith(Names.named("GlossaryTable")).toInstance(databaseGlossaryTable);
		bind(String.class).annotatedWith(Names.named("CSVCorpus_filePath")).toInstance(csvCorpusPath);
		bind(String.class).annotatedWith(Names.named("WordNetAPI_Sourcefile")).toInstance(wordNetSource);
		//resources//wordNet2.1//dict//  resources//wordNet3.1//dict//
		bind(String.class).annotatedWith(Names.named("CSVGlossary_filePath")).toInstance(glossaryFile); 
		bind(INormalizer.class).to(normalizer); 
	}
	
	protected HashSet<String> getSelectedSources(String evaluationDataPath) {
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
		File folder = new File(evaluationDataPath);
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
		} */
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

	public void setEvaluationRunEvaluator(
			Class<? extends IEvaluator> evaluationRunEvaluator) {
		this.evaluationRunEvaluator = evaluationRunEvaluator;
	}

	public Class<? extends IVolumeReader> getEvaluationGoldStandardReader() {
		return evaluationGoldStandardReader;
	}

	public void setEvaluationGoldStandardReader(
			Class<? extends IVolumeReader> evaluationGoldStandardReader) {
		this.evaluationGoldStandardReader = evaluationGoldStandardReader;
	}

	public Class<? extends IVolumeReader> getEvaluationRunCreatedVolumeReader() {
		return evaluationRunCreatedVolumeReader;
	}

	public void setEvaluationRunCreatedVolumeReader(
			Class<? extends IVolumeReader> evaluationRunCreatedVolumeReader) {
		this.evaluationRunCreatedVolumeReader = evaluationRunCreatedVolumeReader;
	}

	public Class<? extends IMarkupCreator> getMarkupCreator() {
		return markupCreator;
	}

	public void setMarkupCreator(Class<? extends IMarkupCreator> markupCreator) {
		this.markupCreator = markupCreator;
	}

	public Class<? extends IVolumeReader> getMarkupCreatorVolumeReader() {
		return markupCreatorVolumeReader;
	}

	public void setMarkupCreatorVolumeReader(
			Class<? extends IVolumeReader> markupCreatorVolumeReader) {
		this.markupCreatorVolumeReader = markupCreatorVolumeReader;
	}

	public String getWordVolumeReaderSourceFile() {
		return wordVolumeReaderSourceFile;
	}

	public void setWordVolumeReaderSourceFile(String wordVolumeReaderSourceFile) {
		this.wordVolumeReaderSourceFile = wordVolumeReaderSourceFile;
	}

	public String getWordVolumeReaderStyleStartPattern() {
		return wordVolumeReaderStyleStartPattern;
	}

	public void setWordVolumeReaderStyleStartPattern(
			String wordVolumeReaderStyleStartPattern) {
		this.wordVolumeReaderStyleStartPattern = wordVolumeReaderStyleStartPattern;
	}

	public String getWordVolumeReaderStyleNamePattern() {
		return wordVolumeReaderStyleNamePattern;
	}

	public void setWordVolumeReaderStyleNamePattern(
			String wordVolumeReaderStyleNamePattern) {
		this.wordVolumeReaderStyleNamePattern = wordVolumeReaderStyleNamePattern;
	}

	public String getWordVolumeReaderStyleKeyPattern() {
		return wordVolumeReaderStyleKeyPattern;
	}

	public void setWordVolumeReaderStyleKeyPattern(
			String wordVolumeReaderStyleKeyPattern) {
		this.wordVolumeReaderStyleKeyPattern = wordVolumeReaderStyleKeyPattern;
	}

	public String getWordVolumeReaderTribegennamestyle() {
		return wordVolumeReaderTribegennamestyle;
	}

	public void setWordVolumeReaderTribegennamestyle(
			String wordVolumeReaderTribegennamestyle) {
		this.wordVolumeReaderTribegennamestyle = wordVolumeReaderTribegennamestyle;
	}

	public String getWordVolumeReaderStyleMappingFile() {
		return wordVolumeReaderStyleMappingFile;
	}

	public void setWordVolumeReaderStyleMappingFile(
			String wordVolumeReaderStyleMappingFile) {
		this.wordVolumeReaderStyleMappingFile = wordVolumeReaderStyleMappingFile;
	}

	public String getXmlVolumeReaderSourceDirectory() {
		return xmlVolumeReaderSourceDirectory;
	}

	public void setXmlVolumeReaderSourceDirectory(
			String xmlVolumeReaderSourceDirectory) {
		this.xmlVolumeReaderSourceDirectory = xmlVolumeReaderSourceDirectory;
	}

	public String getOutputVolumeReaderSourceDirectory() {
		return outputVolumeReaderSourceDirectory;
	}

	public void setOutputVolumeReaderSourceDirectory(
			String outputVolumeReaderSourceDirectory) {
		this.outputVolumeReaderSourceDirectory = outputVolumeReaderSourceDirectory;
	}

	public String getGenericFileVolumeReaderSource() {
		return genericFileVolumeReaderSource;
	}

	public void setGenericFileVolumeReaderSource(
			String genericFileVolumeReaderSource) {
		this.genericFileVolumeReaderSource = genericFileVolumeReaderSource;
	}

	public String getTaxonxVolumeReaderSourceFile() {
		return taxonxVolumeReaderSourceFile;
	}

	public void setTaxonxVolumeReaderSourceFile(String taxonxVolumeReaderSourceFile) {
		this.taxonxVolumeReaderSourceFile = taxonxVolumeReaderSourceFile;
	}

	public Class<? extends TreatmentTransformerChain> getTreatmentTransformerChain() {
		return treatmentTransformerChain;
	}

	public void setTreatmentTransformerChain(
			Class<? extends TreatmentTransformerChain> treatmentTransformerChain) {
		this.treatmentTransformerChain = treatmentTransformerChain;
	}

	public Class<? extends DescriptionTreatmentTransformer> getMarkupDescriptionTreatmentTransformer() {
		return markupDescriptionTreatmentTransformer;
	}

	public void setMarkupDescriptionTreatmentTransformer(
			Class<? extends DescriptionTreatmentTransformer> markupDescriptionTreatmentTransformer) {
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

	public Class<? extends IVolumeWriter> getVolumeWriter() {
		return volumeWriter;
	}

	public void setVolumeWriter(Class<? extends IVolumeWriter> volumeWriter) {
		this.volumeWriter = volumeWriter;
	}

	public String getStandardVolumeReaderSourcefiles() {
		return standardVolumeReaderSourcefiles;
	}

	public void setStandardVolumeReaderSourcefiles(
			String standardVolumeReaderSourcefiles) {
		this.standardVolumeReaderSourcefiles = standardVolumeReaderSourcefiles;
	}

	public String getDatabaseTablePrefix() {
		return databaseTablePrefix;
	}

	public void setDatabaseTablePrefix(String databaseTablePrefix) {
		this.databaseTablePrefix = databaseTablePrefix;
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

	public String getiPlantXMLVolumeReaderSource() {
		return iPlantXMLVolumeReaderSource;
	}

	public void setiPlantXMLVolumeReaderSource(String iPlantXMLVolumeReaderSource) {
		this.iPlantXMLVolumeReaderSource = iPlantXMLVolumeReaderSource;
	}

	public void setResourcesDirectory(String resourcesDirectory) {
		String oldResourcesDirectory = this.getResourcesDirectory();
		this.csvCorpusPath = this.csvCorpusPath.replace(oldResourcesDirectory, resourcesDirectory);
		this.wordVolumeReaderStyleMappingFile = this.wordVolumeReaderStyleMappingFile.replace(oldResourcesDirectory, resourcesDirectory);
		this.taxonxSchemaFile = this.taxonxSchemaFile.replace(oldResourcesDirectory, resourcesDirectory);
		this.xmlSchemaFile = this.xmlSchemaFile.replace(oldResourcesDirectory, resourcesDirectory);
		this.iplantSchemaFile = this.iplantSchemaFile.replace(oldResourcesDirectory, resourcesDirectory);
		this.wordNetSource = this.wordNetSource.replace(oldResourcesDirectory, resourcesDirectory);
		this.glossaryFile = this.glossaryFile.replace(oldResourcesDirectory, resourcesDirectory);
		this.resourcesDirectory = resourcesDirectory;
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
	
	
	
}

