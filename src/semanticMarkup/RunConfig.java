package semanticMarkup;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import semanticMarkup.core.transformation.TreatmentTransformerChain;
import semanticMarkup.core.transformation.lib.CharaparserTreatmentTransformerChain;
import semanticMarkup.core.transformation.lib.MarkupDescriptionTreatmentTransformer;
import semanticMarkup.core.transformation.lib.OldPerlTreatmentTransformer;
import semanticMarkup.eval.AdvancedPrecisionRecallEvaluator;
import semanticMarkup.eval.IEvaluator;
import semanticMarkup.io.input.GenericFileVolumeReader;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.io.input.lib.db.EvaluationDBVolumeReader;
import semanticMarkup.io.input.lib.taxonx.TaxonxVolumeReader;
import semanticMarkup.io.input.lib.word.AbstractWordVolumeReader;
import semanticMarkup.io.input.lib.word.DocWordVolumeReader;
import semanticMarkup.io.input.lib.word.XMLWordVolumeReader;
import semanticMarkup.io.input.lib.xml.XMLVolumeReader;
import semanticMarkup.io.input.lib.xmlOutput.OutputVolumeReader;
import semanticMarkup.io.output.IVolumeWriter;
import semanticMarkup.io.output.lib.xml.XMLVolumeWriter;
import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;
import semanticMarkup.markup.AfterPerlBlackBox;
import semanticMarkup.markup.IMarkupCreator;
import semanticMarkup.run.IRun;
import semanticMarkup.run.MarkupEvaluationRun;
import semanticMarkup.run.MarkupRun;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Guice config file for parameters of a run
 * @author rodenhausen
 */
public class RunConfig extends BasicConfig {

	private Class run = MarkupRun.class;
	//MarkupRun, EvaluationRun, MarkupEvaluationRun
	private String runOutDirectory = "." + File.separator + "out" + File.separator;
	private Class evaluationRunEvaluator = AdvancedPrecisionRecallEvaluator.class;
	//SimplePrecisionRecallEvaluator, AdvancedPrecisionRecallEvaluator
	private Class evaluationGoldStandardReader = XMLVolumeReader.class;
	private Class evaluationRunCreatedVolumeReader = EvaluationDBVolumeReader.class;
	//OutputVolumeReader, EvaluationDBVolumeReader, PerlDBVolumeReader
	private Class markupCreator = AfterPerlBlackBox.class;
	//CharaParser.class //AfterPerlBlackBox
	private Class markupCreatorVolumeReader = GenericFileVolumeReader.class;
	//WordVolumeReader, XMLVolumeReader, PerlDBVolumeReader, EvaluationDBVolumeReader
	private String wordVolumeReaderSourceFile = "evaluationData" + File.separator + "FNA-v19-excerpt_Type1" + File.separator + 
			"source" + File.separator + "FNA19 Excerpt-source.docx";
	private String wordVolumeReaderStyleStartPattern = ".*?(Heading|Name).*";
	private String wordVolumeReaderStyleNamePattern = ".*?(Syn|Name).*";
	private String wordVolumeReaderStyleKeyPattern =  ".*?-Key.*";
	private String wordVolumeReaderTribegennamestyle = "caps";
	private String wordVolumeReaderStyleMappingFile = "" + "resources" + File.separator + "stylemapping.properties";
	private String xmlVolumeReaderSourceDirectory = "evaluationData" + File.separator + "perlTest" + File.separator + "source" +
			File.separator;
	private String outputVolumeReaderSourceDirectory = "." + File.separator + "out" + File.separator;
	private String genericFileVolumeReaderSource = "evaluationData" + File.separator + "perlTest" + File.separator + "source" + File.separator;
	//"evaluationData" + File.separator + "DonatAnts_Type4" + File.separator + "source" + File.separator + "8538_pyr_mad_tx1.xml"
	//"evaluationData" + File.separator + "FNA-v19-excerpt_Type1" + File.separator + "source" + File.separator + "FNA19 Excerpt-source.docx"
	private String taxonxSchemaFile = "." + File.separator + "resources" + File.separator + "io" + File.separator + "taxonx" + File.separator + "taxonx1.xsd";
	private String xmlSchemaFile = "." + File.separator + "resources" + File.separator + "io" + File.separator + "FNAXMLSchemaInput.xsd";
	private String taxonxVolumeReaderSourceFile = "evaluationData" + File.separator + "DonatAnts_Type4" + File.separator + "source" + File.separator + "8538_pyr_mad_tx1.xml";
	private Class treatmentTransformerChain = CharaparserTreatmentTransformerChain.class;
	private Class markupDescriptionTreatmentTransformer = OldPerlTreatmentTransformer.class;
	private boolean markupDescriptionTreatmentTransformerParallelProcessing = false;
	private int markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum = 30;
	private int markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum = Integer.MAX_VALUE;
	private String databaseName = "testNewCode";
	private String databaseUser = "termsuser";
	private String databasePassword = "termspassword";
	private Class volumeWriter = XMLVolumeWriter.class;
	//ToStringVolumeWriter, JSONVolumeWriter, XMLVolumeWriter XML2VolumeWriter
	
	private String evaluationDataPath = "evaluationData" + File.separator + "FNAV19_AnsKey_CharaParser_Evaluation";
	private String databaseTablePrefix = "type2";
	private String databaseGlossaryTable = "fnaglossaryfixed";
	private String glossaryFile = "resources" + File.separator + "fnaglossaryfixed.csv";
	private Class normalizer = FNAv19Normalizer.class;
	
	@Override 
	public void configure() {
		super.configure();
		bind(IRun.class).to(run);
		bind(String.class).annotatedWith(Names.named("Run_OutDirectory")).toInstance(runOutDirectory);
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
		bind(String.class).annotatedWith(Names.named("OutputVolumeReader_SourceDirectory")).toInstance(outputVolumeReaderSourceDirectory);
		bind(String.class).annotatedWith(Names.named("GenericFileVolumeReader_Source")).toInstance(genericFileVolumeReaderSource);
		bind(String.class).annotatedWith(Names.named("Taxonx_SchemaFile")).toInstance(taxonxSchemaFile);
		bind(String.class).annotatedWith(Names.named("XML_SchemaFile")).toInstance(xmlSchemaFile);
		bind(String.class).annotatedWith(Names.named("TaxonxVolumeReader_SourceFile")).toInstance(taxonxVolumeReaderSourceFile);
		bind(TreatmentTransformerChain.class).to(treatmentTransformerChain);
		bind(MarkupDescriptionTreatmentTransformer.class).to(markupDescriptionTreatmentTransformer);
		bind(boolean.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_parallelProcessing")).toInstance(markupDescriptionTreatmentTransformerParallelProcessing);
		bind(int.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_descriptionExtractorRunMaximum")).toInstance(markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum);
		bind(int.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_sentenceChunkerRunMaximum")).toInstance(markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum);
		bind(String.class).annotatedWith(Names.named("databaseName")).toInstance(databaseName);
		bind(String.class).annotatedWith(Names.named("databaseUser")).toInstance(databaseUser);
		bind(String.class).annotatedWith(Names.named("databasePassword")).toInstance(databasePassword);
		bind(IVolumeWriter.class).annotatedWith(Names.named("MarkupCreator_VolumeWriter")).to(volumeWriter);
		
		bind(String.class).annotatedWith(Names.named("GuiceModuleFile")).toInstance(this.toString());
		bind(String.class).annotatedWith(Names.named("StandardVolumeReader_Sourcefiles")).toInstance(evaluationDataPath);
		bind(new TypeLiteral<Set<String>>() {}).annotatedWith(Names.named("selectedSources")).toInstance(getSelectedSources(evaluationDataPath));
		bind(String.class).annotatedWith(Names.named("databasePrefix")).toInstance(databaseTablePrefix); 
		bind(String.class).annotatedWith(Names.named("GlossaryTable")).toInstance(databaseGlossaryTable);
		bind(String.class).annotatedWith(Names.named("CSVGlossary_filePath")).toInstance(glossaryFile); 
		bind(INormalizer.class).to(FNAv19Normalizer.class); 
	}

	public void setRun(Class run) {
		this.run = run;
	}

	public void setRunOutDirectory(String runOutDirectory) {
		this.runOutDirectory = runOutDirectory;
	}

	public void setEvaluationRunEvaluator(Class evaluationRunEvaluator) {
		this.evaluationRunEvaluator = evaluationRunEvaluator;
	}

	public void setEvaluationGoldStandardReader(Class evaluationGoldStandardReader) {
		this.evaluationGoldStandardReader = evaluationGoldStandardReader;
	}

	public void setEvaluationRunCreatedVolumeReader(
			Class evaluationRunCreatedVolumeReader) {
		this.evaluationRunCreatedVolumeReader = evaluationRunCreatedVolumeReader;
	}

	public void setMarkupCreator(Class markupCreator) {
		this.markupCreator = markupCreator;
	}

	public void setMarkupCreatorVolumeReader(Class markupCreatorVolumeReader) {
		this.markupCreatorVolumeReader = markupCreatorVolumeReader;
	}

	public void setWordVolumeReaderSourceFile(String wordVolumeReaderSourceFile) {
		this.wordVolumeReaderSourceFile = wordVolumeReaderSourceFile;
	}

	public void setWordVolumeReaderStyleStartPattern(
			String wordVolumeReaderStyleStartPattern) {
		this.wordVolumeReaderStyleStartPattern = wordVolumeReaderStyleStartPattern;
	}

	public void setWordVolumeReaderStyleNamePattern(
			String wordVolumeReaderStyleNamePattern) {
		this.wordVolumeReaderStyleNamePattern = wordVolumeReaderStyleNamePattern;
	}

	public void setWordVolumeReaderStyleKeyPattern(
			String wordVolumeReaderStyleKeyPattern) {
		this.wordVolumeReaderStyleKeyPattern = wordVolumeReaderStyleKeyPattern;
	}

	public void setWordVolumeReaderTribegennamestyle(
			String wordVolumeReaderTribegennamestyle) {
		this.wordVolumeReaderTribegennamestyle = wordVolumeReaderTribegennamestyle;
	}

	public void setWordVolumeReaderStyleMappingFile(
			String wordVolumeReaderStyleMappingFile) {
		this.wordVolumeReaderStyleMappingFile = wordVolumeReaderStyleMappingFile;
	}

	public void setXmlVolumeReaderSourceDirectory(
			String xmlVolumeReaderSourceDirectory) {
		this.xmlVolumeReaderSourceDirectory = xmlVolumeReaderSourceDirectory;
	}

	public void setOutputVolumeReaderSourceDirectory(
			String outputVolumeReaderSourceDirectory) {
		this.outputVolumeReaderSourceDirectory = outputVolumeReaderSourceDirectory;
	}

	public void setGenericFileVolumeReaderSource(
			String genericFileVolumeReaderSource) {
		this.genericFileVolumeReaderSource = genericFileVolumeReaderSource;
	}

	public void setTaxonxSchemaFile(String taxonxSchemaFile) {
		this.taxonxSchemaFile = taxonxSchemaFile;
	}

	public void setXmlSchemaFile(String xmlSchemaFile) {
		this.xmlSchemaFile = xmlSchemaFile;
	}

	public void setTaxonxVolumeReaderSourceFile(String taxonxVolumeReaderSourceFile) {
		this.taxonxVolumeReaderSourceFile = taxonxVolumeReaderSourceFile;
	}

	public void setTreatmentTransformerChain(Class treatmentTransformerChain) {
		this.treatmentTransformerChain = treatmentTransformerChain;
	}

	public void setMarkupDescriptionTreatmentTransformer(
			Class markupDescriptionTreatmentTransformer) {
		this.markupDescriptionTreatmentTransformer = markupDescriptionTreatmentTransformer;
	}

	public void setMarkupDescriptionTreatmentTransformerParallelProcessing(
			boolean markupDescriptionTreatmentTransformerParallelProcessing) {
		this.markupDescriptionTreatmentTransformerParallelProcessing = markupDescriptionTreatmentTransformerParallelProcessing;
	}

	public void setMarkupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum(
			int markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum) {
		this.markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum = markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum;
	}

	public void setMarkupDescriptionTreatmentTransformerSentenceChunkerRunMaximum(
			int markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum) {
		this.markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum = markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	public void setVolumeWriter(Class volumeWriter) {
		this.volumeWriter = volumeWriter;
	}

	public void setEvaluationDataPath(String evaluationDataPath) {
		this.evaluationDataPath = evaluationDataPath;
	}

	public void setDatabaseTablePrefix(String databaseTablePrefix) {
		this.databaseTablePrefix = databaseTablePrefix;
	}

	public void setDatabaseGlossaryTable(String databaseGlossaryTable) {
		this.databaseGlossaryTable = databaseGlossaryTable;
	}

	public void setGlossaryFile(String glossaryFile) {
		this.glossaryFile = glossaryFile;
	}

	public void setNormalizer(Class normalizer) {
		this.normalizer = normalizer;
	}
	
	protected HashSet<String> getSelectedSources(String evaluationDataPath) {
		HashSet<String> result = new HashSet<String>();
		
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
						result.add(file);
					// break; //TODO remove. only for test
				}
			}
		}*/
		return result;
	}

	public Class getRun() {
		return run;
	}

	public String getRunOutDirectory() {
		return runOutDirectory;
	}

	public Class getEvaluationRunEvaluator() {
		return evaluationRunEvaluator;
	}

	public Class getEvaluationGoldStandardReader() {
		return evaluationGoldStandardReader;
	}

	public Class getEvaluationRunCreatedVolumeReader() {
		return evaluationRunCreatedVolumeReader;
	}

	public Class getMarkupCreator() {
		return markupCreator;
	}

	public Class getMarkupCreatorVolumeReader() {
		return markupCreatorVolumeReader;
	}

	public String getWordVolumeReaderSourceFile() {
		return wordVolumeReaderSourceFile;
	}

	public String getWordVolumeReaderStyleStartPattern() {
		return wordVolumeReaderStyleStartPattern;
	}

	public String getWordVolumeReaderStyleNamePattern() {
		return wordVolumeReaderStyleNamePattern;
	}

	public String getWordVolumeReaderStyleKeyPattern() {
		return wordVolumeReaderStyleKeyPattern;
	}

	public String getWordVolumeReaderTribegennamestyle() {
		return wordVolumeReaderTribegennamestyle;
	}

	public String getWordVolumeReaderStyleMappingFile() {
		return wordVolumeReaderStyleMappingFile;
	}

	public String getXmlVolumeReaderSourceDirectory() {
		return xmlVolumeReaderSourceDirectory;
	}

	public String getOutputVolumeReaderSourceDirectory() {
		return outputVolumeReaderSourceDirectory;
	}

	public String getGenericFileVolumeReaderSource() {
		return genericFileVolumeReaderSource;
	}

	public String getTaxonxSchemaFile() {
		return taxonxSchemaFile;
	}

	public String getXmlSchemaFile() {
		return xmlSchemaFile;
	}

	public String getTaxonxVolumeReaderSourceFile() {
		return taxonxVolumeReaderSourceFile;
	}

	public Class getTreatmentTransformerChain() {
		return treatmentTransformerChain;
	}

	public Class getMarkupDescriptionTreatmentTransformer() {
		return markupDescriptionTreatmentTransformer;
	}

	public boolean isMarkupDescriptionTreatmentTransformerParallelProcessing() {
		return markupDescriptionTreatmentTransformerParallelProcessing;
	}

	public int getMarkupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum() {
		return markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum;
	}

	public int getMarkupDescriptionTreatmentTransformerSentenceChunkerRunMaximum() {
		return markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getDatabaseUser() {
		return databaseUser;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public Class getVolumeWriter() {
		return volumeWriter;
	}

	public String getEvaluationDataPath() {
		return evaluationDataPath;
	}

	public String getDatabaseTablePrefix() {
		return databaseTablePrefix;
	}

	public String getDatabaseGlossaryTable() {
		return databaseGlossaryTable;
	}

	public String getGlossaryFile() {
		return glossaryFile;
	}

	public Class getNormalizer() {
		return normalizer;
	}
}
