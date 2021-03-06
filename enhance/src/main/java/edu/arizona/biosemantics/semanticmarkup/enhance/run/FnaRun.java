package edu.arizona.biosemantics.semanticmarkup.enhance.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.SingularPluralProvider;
import edu.arizona.biosemantics.common.ling.know.Term;
import edu.arizona.biosemantics.common.ling.know.lib.GlossaryBasedCharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.lib.InMemoryGlossary;
import edu.arizona.biosemantics.common.ling.know.lib.WordNetPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.common.ling.transform.lib.SomeInflector;
import edu.arizona.biosemantics.common.ling.transform.lib.WhitespaceTokenizer;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto.client.oto.OTOClient;
import edu.arizona.biosemantics.oto.model.GlossaryDownload;
import edu.arizona.biosemantics.oto.model.TermCategory;
import edu.arizona.biosemantics.oto.model.TermSynonym;
import edu.arizona.biosemantics.oto.model.lite.Decision;
import edu.arizona.biosemantics.oto.model.lite.Download;
import edu.arizona.biosemantics.oto.model.lite.Synonym;
import edu.arizona.biosemantics.oto2.oto.shared.model.Collection;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label;
import edu.arizona.biosemantics.semanticmarkup.enhance.config.Configuration;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.lib.CSVKnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.lib.CSVKnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CollapseBiologicalEntityToName;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CreateOrPopulateWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.MoveNegationOrAdverbBiologicalEntityConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.OrderBiologicalEntityConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveDuplicateValues;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByBackwardConnectors;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByForwardConnectors;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByPassedParents;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByRelations;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveOrphanRelations;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveUselessCharacterConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveUselessWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.ReplaceTaxonNameByWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SimpleRemoveSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SortBiologicalEntityNameWithDistanceCharacter;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.StandardizeCount;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.StandardizeQuantityPresence;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.MoveCharacterToStructureConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.StandardizeStructureNameBySyntax;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.StandardizeTerminology;

public class FnaRun {

	public static void main(String[] args) throws Exception {
		CommandLineParser parser = new BasicParser();
		Options options = new Options();

		options.addOption("i", "input", true, "input directory");
		options.addOption("s", "synonym file", true, "synonym csv file");
		options.addOption("p", "partof file", true, "part of csv file");
		options.addOption("z", "database-table-prefix", true, "database table prefix to use");

		try {
			CommandLine commandLine = parser.parse( options, args );

			String tablePrefix = "";
			if(commandLine.hasOption("z")) {
				tablePrefix = commandLine.getOptionValue("z");
			} else {
				System.out.println("You have to specify an database table prefix");
				throw new IllegalArgumentException();
			}

			String inputDir = "";
			if(!commandLine.hasOption("i")) {
				System.out.println("You have to specify an input directory");
				throw new IllegalArgumentException();
			} else {
				inputDir = commandLine.getOptionValue("i");
			}

			String outputDir = Configuration.workspaceDir + File.separator + tablePrefix + File.separator + "out_enhanced";
			File outputDirFile = new File(outputDir);
			outputDirFile.mkdirs();

			String synonymsCsvFile = "";
			if(!commandLine.hasOption("s")) {
				System.out.println("You have to specify a synonym csv file");
				throw new IllegalArgumentException();
			} else {
				synonymsCsvFile = commandLine.getOptionValue("s");
			}

			String partOfCsvFile = "";
			if(!commandLine.hasOption("p")) {
				System.out.println("You have to specify a part of csv file");
				throw new IllegalArgumentException();
			} else {
				partOfCsvFile = commandLine.getOptionValue("p");
			}

			Collection collection = null;
			try {
				collection = readUploadResult(tablePrefix);
			} catch (SQLException e) {
				System.out.println("Problem reading upload result");
				e.printStackTrace();
				throw e;
			}

			Client oto2Client = new Client(Configuration.oto2Url);
			try {
				oto2Client.open();
				Future<Collection> futureCollection = oto2Client.get(collection.getId(), collection.getSecret());
				collection = futureCollection.get();
				oto2Client.close();
			} catch(InterruptedException | ExecutionException e) {
				oto2Client.close();
				System.out.println("Problem downloading oto lite categorizations for upload " + collection.getId());
				e.printStackTrace();
				throw e;
			}

			String destination = inputDir + "_termReview";
			File file = new File(destination);
			file.mkdirs();
			String termReviewTermCategorization = createCategorizationFile(collection, destination);
			String termReviewSynonyms = createSynonymFile(collection, destination);
			createCategoriesFile(collection, destination);
			String taxonGroup = TaxonGroup.PLANT.toString();

			FnaRun enhance = new FnaRun(tablePrefix, inputDir, outputDir, synonymsCsvFile, partOfCsvFile,
					termReviewTermCategorization, termReviewSynonyms, TaxonGroup.valueOf(taxonGroup));
			enhance.run();
		} catch (ParseException e) {
			System.out.println("Problem parsing parameters");
			throw e;
		}
	}

	private static Collection readUploadResult(String databasePrefix) throws SQLException {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser(Configuration.databaseUser);
		dataSource.setPassword(Configuration.databasePassword);
		dataSource.setServerName(Configuration.databaseHost);
		dataSource.setPort(Configuration.databasePort);
		dataSource.setDatabaseName(Configuration.databaseName);

		try(Connection connection = dataSource.getConnection()) {
			int uploadId = -1;
			String secret = "";
			String sql = "SELECT oto_uploadid, oto_secret FROM datasetprefixes WHERE prefix = ?";
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				preparedStatement.setString(1, databasePrefix);
				preparedStatement.execute();
				try(ResultSet resultSet = preparedStatement.getResultSet()) {
					while(resultSet.next()) {
						uploadId = resultSet.getInt("oto_uploadid");
						secret = resultSet.getString("oto_secret");
					}
					Collection collection = new Collection();
					collection.setId(uploadId);
					collection.setSecret(secret);
					return collection;
				}
			}
		}
	}

	private static String createCategoriesFile(Collection collection, String destination) throws IOException {
		String path = destination + File.separator + "category_definition.csv";
		File file = new File(path);
		file.createNewFile();
		try(CSVWriter csvWriter = new CSVWriter(new FileWriter(file))) {
			csvWriter.writeNext(new String[] {"category", "definition"});
			for(Label label : collection.getLabels()) {
				csvWriter.writeNext(new String[] {label.getName(), label.getDescription()});
			}
		}
		return file.getAbsolutePath();
	}

	private static String createSynonymFile(Collection collection, String destination) throws IOException {
		String path = destination + File.separator + "category_mainterm_synonymterm.csv";
		File file = new File(path);
		file.createNewFile();

		try(CSVWriter csvWriter = new CSVWriter(new FileWriter(file))) {
			csvWriter.writeNext(new String[] {"category", "term", "synonym"});
			for(Label label : collection.getLabels()) {
				for(edu.arizona.biosemantics.oto2.oto.shared.model.Term mainTerm : label.getMainTerms()) {
					List<edu.arizona.biosemantics.oto2.oto.shared.model.Term> synonyms = label.getSynonyms(mainTerm);
					for(edu.arizona.biosemantics.oto2.oto.shared.model.Term synonym : synonyms) {
						csvWriter.writeNext(new String[] { label.getName(), mainTerm.getTerm(), synonym.getTerm() });
					}
				}
			}
		}
		return file.getAbsolutePath();
	}

	private static String createCategorizationFile(Collection collection, String destination) throws IOException {
		String path = destination + File.separator + "category_term.csv";
		File file = new File(path);
		file.createNewFile();

		try(CSVWriter csvWriter = new CSVWriter(new FileWriter(file))) {
			csvWriter.writeNext(new String[] {"category", "term"});
			for(Label label : collection.getLabels()) {
				for(edu.arizona.biosemantics.oto2.oto.shared.model.Term term : label.getTerms())
					csvWriter.writeNext(new String[] {label.getName(), term.getTerm()});
			}
		}
		return file.getAbsolutePath();
	}

	private String negWords = "no|not|never";
	private String advModifiers = "at least|at first|at times";
	private String stopWords = "a|about|above|across|after|along|also|although|amp|an|and|are|as|at|be|because|become|becomes|becoming|been|before|being|"
			+ "beneath|between|beyond|but|by|ca|can|could|did|do|does|doing|done|for|from|had|has|have|hence|here|how|however|if|in|into|inside|inward|is|it|its|"
			+ "may|might|more|most|near|of|off|on|onto|or|out|outside|outward|over|should|so|than|that|the|then|there|these|this|those|throughout|"
			+ "to|toward|towards|up|upward|was|were|what|when|where|which|why|with|within|without|would";
	private String units = "(?:(?:pm|cm|mm|dm|ft|m|meters|meter|micro_m|micro-m|microns|micron|unes|µm|μm|um|centimeters|centimeter|millimeters|millimeter|transdiameters|transdiameter)[23]?)"; //squared or cubed
	private TaxonGroup taxonGroup;
	private WordNetPOSKnowledgeBase wordNetPOSKnowledgeBase = new WordNetPOSKnowledgeBase(Configuration.wordNetDirectory, false);
	private IGlossary glossary = new InMemoryGlossary();
	private HashMap<String, String> renames;
	private SingularPluralProvider singularPluralProvider = new SingularPluralProvider();
	private IInflector inflector = new SomeInflector(wordNetPOSKnowledgeBase, singularPluralProvider.getSingulars(), singularPluralProvider.getPlurals());
	private ITokenizer tokenizer = new WhitespaceTokenizer();
	private Set<String> lifeStyles;
	private GlossaryBasedCharacterKnowledgeBase characterKnowledgeBase;
	private Set<String> possessionTerms = getWordSet("with|has|have|having|possess|possessing|consist_of");
	private Set<String> durations;
	private String input;
	private String output;
	private String termReviewTermCategorization;
	private String termReviewSynonyms;
	private String synonymCsvFile;
	private String partOfCsvFile;
	private String runId;

	public FnaRun(String runId, String input, String output, String synonymCsvFile, String partOfCsvFile,
			String termReviewTermCategorization, String termReviewSynonyms, TaxonGroup taxonGroup) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
		this.runId = runId;
		this.input = input;
		this.output = output;
		this.synonymCsvFile = synonymCsvFile;
		this.partOfCsvFile = partOfCsvFile;
		this.termReviewTermCategorization = termReviewTermCategorization;
		this.termReviewSynonyms = termReviewSynonyms; //Question for Thomas: synonymCsvFile and termReivewSynonyms are not the same?
		this.taxonGroup = taxonGroup;

		initGlossary(glossary, inflector, taxonGroup, termReviewTermCategorization, termReviewSynonyms);

		renames = new HashMap<String, String>();
		renames.put("count", "quantity");
		renames.put("atypical_count", "atypical_quantity");
		renames.put("color", "coloration");

		lifeStyles = glossary.getWordsInCategory("life_style");
		lifeStyles.addAll(glossary.getWordsInCategory("growth_form"));
		durations = glossary.getWordsInCategory("duration");

		characterKnowledgeBase = new GlossaryBasedCharacterKnowledgeBase(glossary, negWords, advModifiers, stopWords, units, inflector);

	}

	private void setupLogging(String runId) {
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.getLoggerRepository().resetConfiguration();
		addDebugErrorLoggers(rootLogger, Configuration.workspaceDir + File.separator + runId + File.separator + "enhance_debug.log",
				Configuration.workspaceDir + File.separator + runId + File.separator + "enhance_error.log");
	}

	public void run() throws OWLOntologyCreationException {
		setupLogging(this.runId);
		log(LogLevel.DEBUG, "Running enhance");
		Run run = new Run();
		ArrayList<String> partOfCsvFiles = new ArrayList<String>();
		partOfCsvFiles.add(partOfCsvFile);
		
		KnowsSynonyms knowsSynonyms = new CSVKnowsSynonyms(synonymCsvFile, inflector);
		try{ //when knows are there
			KnowsPartOf knowsPartOf = new CSVKnowsPartOf(partOfCsvFiles,knowsSynonyms, inflector);
			RemoveNonSpecificBiologicalEntitiesByRelations transformer1 = new RemoveNonSpecificBiologicalEntitiesByRelations(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName());
			RemoveNonSpecificBiologicalEntitiesByBackwardConnectors transformer2 = new RemoveNonSpecificBiologicalEntitiesByBackwardConnectors(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName());
			RemoveNonSpecificBiologicalEntitiesByForwardConnectors transformer3 = new RemoveNonSpecificBiologicalEntitiesByForwardConnectors(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName());
			RemoveNonSpecificBiologicalEntitiesByPassedParents transformer4 = new RemoveNonSpecificBiologicalEntitiesByPassedParents(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName(), inflector);
	
			run.addTransformer(new SimpleRemoveSynonyms(knowsSynonyms));
			run.addTransformer(transformer1);
			run.addTransformer(transformer2);
			run.addTransformer(transformer3);
			run.addTransformer(transformer4);
		}catch (Exception e){
			System.out.println("knows are not there");
		}

		run.addTransformer(new RemoveUselessWholeOrganism());
		run.addTransformer(new RemoveUselessCharacterConstraint());
		run.addTransformer(new MoveCharacterToStructureConstraint());
		run.addTransformer(new MoveNegationOrAdverbBiologicalEntityConstraint(wordNetPOSKnowledgeBase));
		run.addTransformer(new ReplaceTaxonNameByWholeOrganism());
		run.addTransformer(new CreateOrPopulateWholeOrganism(lifeStyles, "growth_form"));
		run.addTransformer(new CreateOrPopulateWholeOrganism(durations, "duration"));
		run.addTransformer(new StandardizeQuantityPresence());
		run.addTransformer(new StandardizeCount());
		run.addTransformer(new SortBiologicalEntityNameWithDistanceCharacter());
		run.addTransformer(new OrderBiologicalEntityConstraint());
		run.addTransformer(new StandardizeStructureNameBySyntax(characterKnowledgeBase, possessionTerms));
		run.addTransformer(new StandardizeTerminology(characterKnowledgeBase));
		run.addTransformer(new RemoveOrphanRelations());
		run.addTransformer(new RemoveDuplicateValues());

		run.run(new File(input), new File(output));

		log(LogLevel.DEBUG, "Completed running enhance!");
	}



	private static Set<String> getWordSet(String regexString) {
		Set<String> set = new HashSet<String>();
		String[] wordsArray = regexString.split("\\|");
		for (String word : wordsArray)
			set.add(word.toLowerCase().trim());
		return set;
	}

	private void initGlossary(IGlossary glossary, IInflector inflector, TaxonGroup taxonGroup, String termReviewTermCategorization, String termReviewSynonyms) throws IOException, InterruptedException, ClassNotFoundException, ExecutionException {
		addPermanentGlossary(glossary, inflector, taxonGroup);
		addTermReviewGlossary(glossary, inflector, termReviewTermCategorization, termReviewSynonyms);
	}

	private void addTermReviewGlossary(IGlossary glossary,	IInflector inflector, String termReviewTermCategorization,	String termReviewSynonyms) throws IOException {
		List<Synonym> synonyms = new LinkedList<Synonym>();
		Set<String> hasSynonym = new HashSet<String>();

		if(termReviewSynonyms != null && new File(termReviewSynonyms).exists()) {
			try(CSVReader reader = new CSVReader(new FileReader(termReviewSynonyms))) {
				List<String[]> lines = reader.readAll();
				int i=0;
				for(String[] line : lines) {
					synonyms.add(new Synonym(String.valueOf(i), line[1], line[0], line[2]));
					hasSynonym.add(line[1]);
				}
			}
		}

		if(termReviewTermCategorization != null && new File(termReviewTermCategorization).exists()) {
			try(CSVReader reader = new CSVReader(new FileReader(termReviewTermCategorization))) {
				List<String[]> lines = reader.readAll();
				List<Decision> decisions = new LinkedList<Decision>();
				int i=0;
				for(String[] line : lines) {
					decisions.add(new Decision(String.valueOf(i), line[1], line[0], hasSynonym.contains(line[1]), ""));
				}
				Download download = new Download(true, decisions, synonyms);

				//add syn set of term_category
				HashSet<Term> dsyns = new HashSet<Term>();
				if(download != null) {
					for(Synonym termSyn: download.getSynonyms()){
						//Hong TODO need to add category info to synonym entry in OTOLite
						//if(termSyn.getCategory().compareTo("structure")==0){
						if(termSyn.getCategory().matches("structure|taxon_name|substance")){
							//take care of singular and plural forms
							String syns = "";
							String synp = "";
							String terms = "";
							String termp = "";
							if(inflector.isPlural(termSyn.getSynonym().replaceAll("_",  "-"))){
								synp = termSyn.getSynonym().replaceAll("_",  "-");
								syns = inflector.getSingular(synp);
							}else{
								syns = termSyn.getSynonym().replaceAll("_",  "-");
								synp = inflector.getPlural(syns);
							}

							if(inflector.isPlural(termSyn.getTerm().replaceAll("_",  "-"))){
								termp = termSyn.getTerm().replaceAll("_",  "-");
								terms = inflector.getSingular(termp);
							}else{
								terms = termSyn.getTerm().replaceAll("_",  "-");
								termp = inflector.getPlural(terms);
							}
							//glossary.addSynonym(syns, termSyn.getCategory(), terms);
							//glossary.addSynonym(synp, termSyn.getCategory(), termp);
							//dsyns.add(new Term(syns, termSyn.getCategory());
							//dsyns.add(new Term(synp, termSyn.getCategory());
							glossary.addSynonym(syns, termSyn.getCategory(), terms);
							glossary.addSynonym(synp,termSyn.getCategory(), termp);
							dsyns.add(new Term(syns, termSyn.getCategory()));
							dsyns.add(new Term(synp, termSyn.getCategory()));
						}else{//forking_1 and forking are syns 5/5/14 hong test, shouldn't _1 have already been removed?
							glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory(), termSyn.getTerm());
							dsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory()));
						}
					}

					//term_category from OTO, excluding dsyns
					for(Decision decision : download.getDecisions()) {
						if(!dsyns.contains(new Term(decision.getTerm().replaceAll("_",  "-"), decision.getCategory())))//calyx_tube => calyx-tube
							glossary.addEntry(decision.getTerm().replaceAll("_",  "-"), decision.getCategory());
					}
				}
			}
		}
	}

	private void addPermanentGlossary(IGlossary glossary, IInflector inflector, TaxonGroup taxonGroup) throws InterruptedException, IOException, ClassNotFoundException, ExecutionException {
		/*OTOClient otoClient = new OTOClient("http://biosemantics.arizona.edu:8080/OTO");
		GlossaryDownload glossaryDownload = new GlossaryDownload();
		String glossaryVersion = "latest";
		otoClient.open();
		Future<GlossaryDownload> futureGlossaryDownload = otoClient.getGlossaryDownload(taxonGroup.getDisplayName(), glossaryVersion);
		glossaryDownload = futureGlossaryDownload.get();
		otoClient.close();
		*/
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(Configuration.glossariesDownloadDirectory + File.separator +
				"GlossaryDownload." + taxonGroup.getDisplayName() + ".ser"));
		GlossaryDownload glossaryDownload = (GlossaryDownload) objectIn.readObject();
		objectIn.close();
		//add the syn set of the glossary
		HashSet<Term> gsyns = new HashSet<Term>();
		for(TermSynonym termSyn : glossaryDownload.getTermSynonyms()) {

			//if(termSyn.getCategory().compareTo("structure")==0){
			if(termSyn.getCategory().matches("structure|taxon_name|substance")) {
				//take care of singular and plural forms
				String syns = "";
				String synp = "";
				String terms = "";
				String termp = "";
				if(inflector.isPlural(termSyn.getSynonym().replaceAll("_",  "-"))){ //must convert _ to -, as matching entity phrases will be converted from leg iii to leg-iii in the sentence.
					synp = termSyn.getSynonym().replaceAll("_",  "-");
					syns = inflector.getSingular(synp);
				} else {
					syns = termSyn.getSynonym().replaceAll("_",  "-");
					synp = inflector.getPlural(syns);
				}

				if(inflector.isPlural(termSyn.getTerm().replaceAll("_",  "-"))){
					termp = termSyn.getTerm().replaceAll("_",  "-");
					terms = inflector.getSingular(termp);
				}else{
					terms = termSyn.getTerm().replaceAll("_",  "-");
					termp = inflector.getPlural(terms);
				}
				glossary.addSynonym(syns, termSyn.getCategory(), terms);
				glossary.addSynonym(synp, termSyn.getCategory(), termp);
				gsyns.add(new Term(syns, termSyn.getCategory()));
				gsyns.add(new Term(synp, termSyn.getCategory()));
			} else {
				//glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), "arrangement", termSyn.getTerm());
				glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory(), termSyn.getTerm());
				gsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory()));
				//gsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), "arrangement"));
			}
		}

		//the glossary, excluding gsyns
		for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
			if(!gsyns.contains(new Term(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory())))
				glossary.addEntry(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory()); //primocane_foliage =>primocane-foliage Hong 3/2014
		}
	}

	protected void addDebugErrorLoggers(Logger rootLogger, String debugLog, String errorLog) {
		PatternLayout layout = new PatternLayout();
		layout.setConversionPattern("%d [%t] %-5p %c:%L - %m%n");

		RollingFileAppender debugFileAppender = new RollingFileAppender();
		debugFileAppender.setEncoding("UTF-8");
		debugFileAppender.setFile(debugLog);
		debugFileAppender.setMaxFileSize("100MB");
		debugFileAppender.setAppend(false);
		debugFileAppender.setMaxBackupIndex(100);
		debugFileAppender.setLayout(layout);
		debugFileAppender.setThreshold(Level.DEBUG);
		debugFileAppender.activateOptions();

		RollingFileAppender errorFileAppender = new RollingFileAppender();
		errorFileAppender.setEncoding("UTF-8");
		errorFileAppender.setFile(errorLog);
		errorFileAppender.setMaxFileSize("100MB");
		errorFileAppender.setAppend(false);
		errorFileAppender.setMaxBackupIndex(100);
		errorFileAppender.setLayout(layout);
		errorFileAppender.setThreshold(Level.ERROR);
		errorFileAppender.activateOptions();

		ConsoleAppender consoleErrorAppender = new ConsoleAppender();
		consoleErrorAppender.setTarget("System.out");
		consoleErrorAppender.setLayout(layout);
		consoleErrorAppender.setThreshold(Level.ERROR);
		consoleErrorAppender.activateOptions();

		rootLogger.setLevel(Level.DEBUG);
		rootLogger.addAppender(debugFileAppender);
		rootLogger.addAppender(errorFileAppender);
		rootLogger.addAppender(consoleErrorAppender);
	}
}
