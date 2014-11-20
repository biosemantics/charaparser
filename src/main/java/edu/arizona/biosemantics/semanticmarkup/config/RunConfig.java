package edu.arizona.biosemantics.semanticmarkup.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.w3c.dom.Document;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import edu.arizona.biosemantics.oto.client.lite.OTOLiteClient;
import edu.arizona.biosemantics.oto.client.oto2.Client;
import edu.arizona.biosemantics.semanticmarkup.eval.IEvaluator;
import edu.arizona.biosemantics.semanticmarkup.io.InputStreamCreator;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.lib.CSVGlossary;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.INormalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib.FNAv19Normalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib.TreatisehNormalizer;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.IDescriptionMarkupResultReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.io.IDescriptionMarkupEvaluator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.io.lib.MOXyDescriptionMarkupResultReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.lib.PerfectPartialPrecisionRecallEvaluator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.IDescriptionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.EvaluationDBDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.MOXyBinderDescriptionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.MOXyDescriptionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.DatabaseInputNoLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.PerlTerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.IOntology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.ontologize.ontologies.PartOfFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.DescriptionMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.IDescriptionMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.run.DescriptionMarkupRun;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.GUIDescriptionTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.IDescriptionTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.io.IDistributionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.io.IDistributionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.markup.IDistributionMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.IElevationReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.IElevationWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.markup.IElevationMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io.IHabitatReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io.IHabitatWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.markup.HabitatMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.markup.IHabitatMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform.HabitatTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform.IHabitatTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.markup.ElevationMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.transform.ElevationTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.transform.IElevationTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.markup.DistributionMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.transform.DistributionTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.transform.IDistributionTransformer;
import edu.arizona.biosemantics.semanticmarkup.run.IRun;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io.lib.JDOMHabitatReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.io.lib.JDOMHabitatWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io.IPhenologyReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io.IPhenologyWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io.lib.JDOMPhenologyReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.io.lib.JDOMPhenologyWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.markup.IPhenologyMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.markup.PhenologyMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform.IPhenologyTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform.PhenologyTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.io.lib.JDOMDistributionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.io.lib.JDOMDistributionWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.lib.JDOMElevationReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.io.lib.JDOMElevationWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io.lib.JDOMEcologyReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io.lib.JDOMEcologyWriter;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.markup.IEcologyMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.markup.EcologyMarkupCreator;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.transform.EcologyTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.transform.IEcologyTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io.IEcologyReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io.IEcologyWriter;



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
	//private String otoLiteTermReviewURL = "http://biosemantics.arizona.edu:8080/OTOLite/";
	//private String otoLiteClientURL = "http://biosemantics.arizona.edu:8080/OTOLite/";
	//for general use
	//http://etc-dev.cs.umb.edu/etcsite/
	//private String otoLiteTermReviewURL = "http://etc.cs.umb.edu/otolite/";
	//private String otoLiteClientURL = "http://etc-dev.cs.umb.edu/etcsite/";
	//for local test
	private String otoLiteTermReviewURL = "http://etc-dev.cs.umb.edu/oto2/";
	private String otoLiteClientURL = "http://etc-dev.cs.umb.edu/oto2/";

	private String otoClientUrl = "http://biosemantics.arizona.edu:8080/OTO/";
	private String perlDirectory = "src/main/perl/edu/arizona/biosemantics/semanticmarkup/markupelement/description/ling/learn/lib/perl" ;
	private String workspaceDirectory = "workspace";
	private String glossaryFile = "edu/arizona/biosemantics/semanticmarkup/know/glossaries/fnaglossaryfixed.csv";
	private String csvCorpusPath = "edu/arizona/biosemantics/semanticmarkup/know/corpora/brown.csv";
	private String wordNetSource = "wordnet/wn31/dict";
	//resources//wordNet2.1//dict//  resources//wordNet3.1//dict//
	
	// IO
	private Class<? extends IDescriptionReader> descriptionReader = EvaluationDBDescriptionReader.class;
	private List<InputStream> descriptionReaderBindingsList = createIOXMLBindingsList();
	private List<InputStream> descriptionWriterBindingsList = createIOXMLBindingsList();
 	private String inputDirectory = "input";
 	private String ontologyFile = "ontopartof.bin";
	private Class<? extends IDescriptionWriter> descriptionWriter = MOXyBinderDescriptionWriter.class;
	private String markupRunValidateSchemaFile = "edu/arizona/biosemantics/semanticmarkup/markupelement/description/io/schemas/iplantOutputTreatment.xsd";
	private Class<? extends IHabitatReader> habitatReader = JDOMHabitatReader.class;
	private Class<? extends IHabitatWriter> habitatWriter = JDOMHabitatWriter.class;
	private Class<? extends IPhenologyReader> phenologyReader = JDOMPhenologyReader.class;
	private Class<? extends IPhenologyWriter> phenologyWriter = JDOMPhenologyWriter.class;
	private Class<? extends IDistributionReader> distributionReader = JDOMDistributionReader.class;
	private Class<? extends IDistributionWriter> distributionWriter = JDOMDistributionWriter.class;
	private Class<? extends IElevationReader> elevationReader = JDOMElevationReader.class;
	private Class<? extends IElevationWriter> elevationWriter = JDOMElevationWriter.class;
	
	// PROCESSING 
	private String glossaryType = "plant";
	private Class<? extends IRun> run = DescriptionMarkupRun.class;
	private String runRootDirectory = workspaceDirectory + File.separator + this.databaseTablePrefix;
	private String runOutDirectory = workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "out";
	private String runTemporaryDirectory = workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "temp";
	private String runOntologyDirectory = workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "onto";
	private Class<? extends IGlossary> glossary = CSVGlossary.class;

	private Class<? extends IMarkupCreator> markupCreator = DescriptionMarkupCreator.class;
	private Class<? extends IDescriptionMarkupCreator> descriptionMarkupCreator = DescriptionMarkupCreator.class;
	private Class<? extends IDescriptionTransformer> markupDescriptionTreatmentTransformer = GUIDescriptionTransformer.class;
	private Class<? extends IHabitatMarkupCreator> habitatMarkupCreator = HabitatMarkupCreator.class;
	private Class<? extends IElevationMarkupCreator> elevationMarkupCreator = ElevationMarkupCreator.class;
	private Class<? extends IDistributionMarkupCreator> distributionMarkupCreator = DistributionMarkupCreator.class;
	private Class<? extends IPhenologyMarkupCreator> phenologyMarkupCreator = PhenologyMarkupCreator.class;
	
	private boolean markupDescriptionTreatmentTransformerParallelProcessing = false;
	private int markupDescriptionTreatmentTransformerDescriptionExtractorRunMaximum = 3; //30
	private int markupDescriptionTreatmentTransformerSentenceChunkerRunMaximum = 3;
	private Class<? extends ITerminologyLearner> terminologyLearner = PerlTerminologyLearner.class;
	private Class<? extends INormalizer> normalizer = FNAv19Normalizer.class;
	private Class<? extends IDescriptionMarkupEvaluator> evaluationRunEvaluator = PerfectPartialPrecisionRecallEvaluator.class;
	private boolean termCategorizationRequired = false;
	private boolean useOtoCommuntiyDownload = true;
		
	// MISC
	//required for bioportal submission of oto lite
	private String sourceOfDescriptions = "";
	private String etcUser = "";
	private String bioportalAPIKey = "";
	private String bioportalUserId = "";
	//
	
	public RunConfig() throws IOException {
		super();
	}

	@Override 
	public void configure() {
		super.configure();
		
		try {
			// PROCESSING 
			bind(String.class).annotatedWith(Names.named("GlossaryType")).toInstance(glossaryType);
			bind(IRun.class).to(run);
			bind(IGlossary.class).to(glossary).in(Singleton.class);
			bind(String.class).annotatedWith(Names.named("PerlDirectory")).toInstance(this.perlDirectory);
			bind(String.class).annotatedWith(Names.named("WorkspaceDirectory")).toInstance(this.workspaceDirectory);
			bind(String.class).annotatedWith(Names.named("Run_RootDirectory")).toInstance(this.runRootDirectory);
			bind(String.class).annotatedWith(Names.named("Run_OutDirectory")).toInstance(this.runOutDirectory);
			bind(String.class).annotatedWith(Names.named("Run_TemporaryDirectory")).toInstance(this.runTemporaryDirectory);
			bind(String.class).annotatedWith(Names.named("Run_OntologyDirectory")).toInstance(this.runOntologyDirectory);
			bind(String.class).annotatedWith(Names.named("OntologyFile")).toInstance(this.ontologyFile);

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
			bind(Boolean.class).annotatedWith(Names.named("UseOtoCommunityDownload")).toInstance(useOtoCommuntiyDownload);
			
			bind(IHabitatMarkupCreator.class).to(habitatMarkupCreator).in(Singleton.class);
			bind(IHabitatTransformer.class).to(HabitatTransformer.class).in(Singleton.class);
			bind(IDistributionMarkupCreator.class).to(distributionMarkupCreator).in(Singleton.class);
			bind(IDistributionTransformer.class).to(DistributionTransformer.class).in(Singleton.class);
			bind(IElevationMarkupCreator.class).to(elevationMarkupCreator).in(Singleton.class);
			bind(IElevationTransformer.class).to(ElevationTransformer.class).in(Singleton.class);
			bind(IPhenologyMarkupCreator.class).to(phenologyMarkupCreator).in(Singleton.class);
			bind(IPhenologyTransformer.class).to(PhenologyTransformer.class).in(Singleton.class);
			
			//IO
			bind(String.class).annotatedWith(Names.named("InputDirectory")).toInstance(inputDirectory);
			bind(IDescriptionReader.class).annotatedWith(Names.named("DescriptionMarkupCreator_DescriptionReader")).to(descriptionReader).in(Singleton.class);
			bind(String.class).annotatedWith(Names.named("DescriptionReader_InputDirectory")).toInstance(runOutDirectory);
			bind(new TypeLiteral<List<InputStream>>() {}).annotatedWith(Names.named("DescriptionReader_BindingsFiles")).toInstance(descriptionReaderBindingsList);
			bind(new TypeLiteral<List<InputStream>>() {}).annotatedWith(Names.named("DescriptionWriter_BindingsFiles")).toInstance(descriptionWriterBindingsList);
			bind(new TypeLiteral<Set<String>>() {}).annotatedWith(Names.named("SelectedSources")).toInstance(getSelectedSources(runOutDirectory));
			bind(IDescriptionMarkupResultReader.class).annotatedWith(Names.named("EvaluationRun_CorrectReader")).toInstance(constructEvaluationCorrectReader());
			bind(IDescriptionMarkupResultReader.class).annotatedWith(Names.named("EvaluationRun_TestReader")).toInstance(constructEvaluationTestReader());
			bind(IDescriptionWriter.class).annotatedWith(Names.named("DescriptionMarkupCreator_DescriptionWriter")).to(descriptionWriter).in(Singleton.class);
			bind(String.class).annotatedWith(Names.named("MarkupRun_ValidateSchemaFile")).toInstance(markupRunValidateSchemaFile);
			
			bind(IHabitatReader.class).annotatedWith(Names.named("HabitatMarkupCreator_HabitatReader")).to(habitatReader).in(Singleton.class);
			bind(IHabitatWriter.class).annotatedWith(Names.named("HabitatMarkupCreator_HabitatWriter")).to(habitatWriter).in(Singleton.class);
			bind(String.class).annotatedWith(Names.named("HabitatReader_InputDirectory")).toInstance(runOutDirectory);
			bind(IPhenologyReader.class).annotatedWith(Names.named("PhenologyMarkupCreator_PhenologyReader")).to(phenologyReader).in(Singleton.class);
			bind(IPhenologyWriter.class).annotatedWith(Names.named("PhenologyMarkupCreator_PhenologyWriter")).to(phenologyWriter).in(Singleton.class);
			bind(String.class).annotatedWith(Names.named("PhenologyReader_InputDirectory")).toInstance(runOutDirectory);
			bind(IDistributionReader.class).annotatedWith(Names.named("DistributionMarkupCreator_DistributionReader")).to(distributionReader).in(Singleton.class);
		    bind(IDistributionWriter.class).annotatedWith(Names.named("DistributionMarkupCreator_DistributionWriter")).to(distributionWriter).in(Singleton.class);
			bind(String.class).annotatedWith(Names.named("DistributionReader_InputDirectory")).toInstance(runOutDirectory);
			bind(IElevationReader.class).annotatedWith(Names.named("ElevationMarkupCreator_ElevationReader")).to(elevationReader).in(Singleton.class);
			bind(IElevationWriter.class).annotatedWith(Names.named("ElevationMarkupCreator_ElevationWriter")).to(elevationWriter).in(Singleton.class);
			bind(String.class).annotatedWith(Names.named("ElevationReader_InputDirectory")).toInstance(runOutDirectory);
						
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
			bind(OTOLiteClient.class).to(Client.class);
			bind(String.class).annotatedWith(Names.named("OTOClient_Url")).toInstance(otoClientUrl);
			bind(String.class).annotatedWith(Names.named("GlossaryTable")).toInstance(databaseGlossaryTable);
			bind(InputStream.class).annotatedWith(Names.named("CSVCorpus")).toInstance(inputStreamCreator.readStreamFromString(csvCorpusPath));
			bind(String.class).annotatedWith(Names.named("WordNetAPI_Sourcefile")).toInstance(wordNetSource);
			//resources//wordNet2.1//dict//  resources//wordNet3.1//dict//
			bind(String.class).annotatedWith(Names.named("CSVGlossary_FilePath")).toInstance(glossaryFile); 
			
			//MISC
			bind(String.class).annotatedWith(Names.named("GuiceModuleFile")).toInstance(this.toString());
			bind(String.class).annotatedWith(Names.named("SourceOfDescriptions")).toInstance(sourceOfDescriptions);
			bind(String.class).annotatedWith(Names.named("EtcUser")).toInstance(etcUser);
			bind(String.class).annotatedWith(Names.named("BioportalAPIKey")).toInstance(bioportalAPIKey);
			bind(String.class).annotatedWith(Names.named("BioportalUserId")).toInstance(bioportalUserId);
		
		} catch(IOException | JAXBException e) {
			log(LogLevel.ERROR, "Exception loading configuration", e);
			throw new IllegalArgumentException();
		}
	}
	
	private List<InputStream> createEvaluationTestReaderBindingsList() throws IOException {
		List<InputStream> result = new LinkedList<InputStream>();
		result.add(inputStreamCreator.readStreamFromString("edu/arizona/biosemantics/semanticmarkup/markupelement/description/eval/model/bindings/baseBindings.xml"));
		result.add(inputStreamCreator.readStreamFromString("edu/arizona/biosemantics/semanticmarkup/markupelement/description/eval/model/bindings/testBindings.xml"));
		return result;
	}

	private List<InputStream> createEvaluationCorrectReaderBindingsList() throws IOException {
		List<InputStream> result = new LinkedList<InputStream>();
		result.add(inputStreamCreator.readStreamFromString("edu/arizona/biosemantics/semanticmarkup/markupelement/description/eval/model/bindings/baseBindings.xml"));
		result.add(inputStreamCreator.readStreamFromString("edu/arizona/biosemantics/semanticmarkup/markupelement/description/eval/model/bindings/correctBindings.xml"));
		return result;
	}

	private IDescriptionMarkupResultReader constructEvaluationTestReader() throws JAXBException, IOException {
		try {
			return new MOXyDescriptionMarkupResultReader(createEvaluationTestReaderBindingsList());
		} catch(JAXBException | IOException e) {
			log(LogLevel.ERROR, "Exception instantiating MOXyDescriptionMarkupResultReader", e);
			throw e;
		}
	}

	private IDescriptionMarkupResultReader constructEvaluationCorrectReader() throws JAXBException, IOException {
		try {
			return new MOXyDescriptionMarkupResultReader(createEvaluationCorrectReaderBindingsList());
		} catch(JAXBException | IOException e) {
			log(LogLevel.ERROR, "Exception instantiating MOXyDescriptionMarkupResultReader", e);
			throw e;
		}
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
		this.runRootDirectory = this.workspaceDirectory + File.separator + this.databaseTablePrefix;
		this.runOutDirectory = this.workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "out";
		this.runTemporaryDirectory = this.workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "temp";
		
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

	public String getInputDirectory() {
		return inputDirectory;
	}

	public void setInputDirectory(String inputDirectory) {
		this.inputDirectory = inputDirectory;
	}

	public Class<? extends IDescriptionMarkupCreator> getDescriptionMarkupCreator() {
		return descriptionMarkupCreator;
	}

	public void setDescriptionMarkupCreator(
			Class<? extends IDescriptionMarkupCreator> descriptionMarkupCreator) {
		this.descriptionMarkupCreator = descriptionMarkupCreator;
	}
	
	public void setPerlDirectory(String srcDirectory) {
		this.perlDirectory = srcDirectory;
	}

	public String getPerlDirectory() {
		return perlDirectory;
	}

	public void setWorkspaceDirectory(String workspaceDirectory) {
		this.workspaceDirectory = workspaceDirectory;
		this.runRootDirectory = this.workspaceDirectory + File.separator + this.databaseTablePrefix;
		this.runOutDirectory = this.workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "out";
		this.runTemporaryDirectory = this.workspaceDirectory + File.separator + this.databaseTablePrefix + File.separator + "temp";
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
	
	public boolean isUseOtoCommuntiyDownload() {
		return useOtoCommuntiyDownload;
	}

	public void setUseOtoCommuntiyDownload(boolean useOtoCommuntiyDownload) {
		this.useOtoCommuntiyDownload = useOtoCommuntiyDownload;
	}

	public String getMarkupRunValidateSchemaFile() {
		return markupRunValidateSchemaFile;
	}

	public void setMarkupRunValidateSchemaFile(String markupRunValidateSchemaFile) {
		this.markupRunValidateSchemaFile = markupRunValidateSchemaFile;
	}
	
	

	public void setIODescriptionBindingsList(String volumeReader) throws IOException {
		if(volumeReader.equals("XML")) {
			this.descriptionReaderBindingsList = createIOXMLBindingsList();
			this.descriptionWriterBindingsList = createIOXMLBindingsList();
		}
		if(volumeReader.equals("Taxonx")) {
			this.descriptionReaderBindingsList = createIOTaxonxBindingsList();
			this.descriptionWriterBindingsList = createIOTaxonxBindingsList();
		}
		if(volumeReader.equals("IPlant")) {
			this.descriptionReaderBindingsList = createIOIPlantBindingsList();
			this.descriptionWriterBindingsList = createIOIPlantBindingsList();
		}
	}

	private List<InputStream> createIOIPlantBindingsList() {
		// TODO Auto-generated method stub
		return null;
	}

	private List<InputStream> createIOTaxonxBindingsList() {
		// TODO Auto-generated method stub
		return null;
	}

	private List<InputStream> createIOXMLBindingsList() throws IOException {
		List<InputStream> result = new LinkedList<InputStream>();
		result.add(inputStreamCreator.readStreamFromString("edu/arizona/biosemantics/semanticmarkup/markupelement/description/model/bindings/semanticMarkupBaseBindings.xml"));
		result.add(inputStreamCreator.readStreamFromString("edu/arizona/biosemantics/semanticmarkup/markupelement/description/model/bindings/singleTreatmentDescriptionBindings.xml"));
		return result;
	}

	public String getWordNetSource() {
		return wordNetSource;
	}

	public void setWordNetSource(String wordNetSource) {
		this.wordNetSource = wordNetSource;
	}	
	
}
