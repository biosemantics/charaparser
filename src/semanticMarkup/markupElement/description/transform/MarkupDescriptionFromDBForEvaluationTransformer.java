package semanticMarkup.markupElement.description.transform;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oto.full.IOTOClient;
import oto.lite.IOTOLiteClient;
import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.chunk.ChunkerChain;
import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.parse.IParser;
import semanticMarkup.ling.pos.IPOSTagger;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.markupElement.description.ling.extract.IDescriptionExtractor;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.DescriptionsFile;

import com.google.inject.Inject;
import com.google.inject.name.Named;


/**
 * Transforms the treatments by semantically marking up the description treatment element of a treatment
 * This can be used for the second and hence the 'markup' application for the iPlant integration
 * @author rodenhausen
 */
public class MarkupDescriptionFromDBForEvaluationTransformer extends MarkupDescriptionTreatmentTransformer {

	private String glossaryTable;


	@Inject
	public MarkupDescriptionFromDBForEvaluationTransformer(
			@Named("Version") String version,
			@Named("WordTokenizer")ITokenizer wordTokenizer, 
			IParser parser,
			@Named("ChunkerChain")ChunkerChain chunkerChain,
			IPOSTagger posTagger, 
			IDescriptionExtractor descriptionExtractor, 
			INormalizer normalizer,
			ITerminologyLearner terminologyLearner,
			@Named("MarkupDescriptionTreatmentTransformer_ParallelProcessing")boolean parallelProcessing, 
			@Named("MarkupDescriptionTreatmentTransformer_DescriptionExtractorRunMaximum")int descriptionExtractorRunMaximum, 
			@Named("MarkupDescriptionTreatmentTransformer_SentenceChunkerRunMaximum")int sentenceChunkerRunMaximum,  
			IOTOClient otoClient, 
			IOTOLiteClient otoLiteClient, 
			@Named("OTOLiteTermReviewURL") String otoLiteTermReviewURL,
			@Named("DatabaseHost") String databaseHost,
			@Named("DatabasePort") String databasePort,
			@Named("DatabaseName")String databaseName,
			@Named("DatabaseUser")String databaseUser,
			@Named("DatabasePassword")String databasePassword,
			@Named("DatabasePrefix")String databasePrefix, 
			@Named("GlossaryType")String glossaryType,
			IGlossary glossary, 
			@Named("SelectedSources")Set<String> selectedSources, 
			@Named("GlossaryTable")String glossaryTable)
			throws Exception {
		super(version, wordTokenizer, parser, chunkerChain, posTagger, descriptionExtractor,
				normalizer, terminologyLearner, parallelProcessing,
				descriptionExtractorRunMaximum, sentenceChunkerRunMaximum, otoClient,
				otoLiteClient, otoLiteTermReviewURL, databaseHost, databasePort,
				databaseName, databaseUser, databasePassword, databasePrefix,
				glossaryType, glossary, selectedSources, glossaryTable);
		this.glossaryTable = glossaryTable;
	}


	@Override
	public TransformationReport transform(List<DescriptionsFile> descriptionsFiles) {
		//evaluation runs with .csv glossaries as gold standard uses the
		//character categories used in the csvs.
		//OTOs character categories varies at times, which makes evaluation difficult.
		
		/*GlossaryDownload glossaryDownload = otoClient.download(glossaryType);
		Download download = new Download();
		
		log(LogLevel.DEBUG, "Size of permanent glossary downloaded:\n" +
				"Number of term categoy relations " + glossaryDownload.getTermCategories().size() + "\n" +
				"Number of term synonym relations " + glossaryDownload.getTermSynonyms().size());
		log(LogLevel.DEBUG, "Size of temporary glossary downloaded:\n" +
				"Number of term categoy relations " + download.getDecisions().size() + "\n" +
				"Number of term synonym relations " + download.getSynonyms().size());
		//storeInLocalDB(glossaryDownload, download);
		initGlossary(glossaryDownload, download);
		*/
		//this is needed to initialize terminologylearner (DatabaseInputNoLearner / fileTreatments)
		//even though no actual learning is taking place
		terminologyLearner.learn(descriptionsFiles, glossaryTable);
		terminologyLearner.readResults(descriptionsFiles);
		Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker = terminologyLearner.getSentencesForOrganStateMarker();
		// do the actual markup
		markupDescriptions(descriptionsFiles, sentencesForOrganStateMarker);		
		return new TransformationReport(version, "evaluation glossary", "evaluation glossary");
	}
}
