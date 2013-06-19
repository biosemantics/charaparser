package semanticMarkup.core.transformation.lib.description;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oto.full.IOTOClient;
import oto.full.beans.GlossaryDownload;
import oto.lite.IOTOLiteClient;
import oto.lite.beans.Download;

import semanticMarkup.core.Treatment;
import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.chunk.ChunkerChain;
import semanticMarkup.ling.extract.IDescriptionExtractor;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.parse.IParser;
import semanticMarkup.ling.pos.IPOSTagger;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.log.LogLevel;

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
			@Named("MarkupDescriptionTreatmentTransformer_parallelProcessing")boolean parallelProcessing, 
			@Named("MarkupDescriptionTreatmentTransformer_descriptionExtractorRunMaximum")int descriptionExtractorRunMaximum, 
			@Named("MarkupDescriptionTreatmentTransformer_sentenceChunkerRunMaximum")int sentenceChunkerRunMaximum,  
			IOTOClient otoClient, 
			IOTOLiteClient otoLiteClient, 
			@Named("otoLiteTermReviewURL") String otoLiteTermReviewURL,
			@Named("databaseHost") String databaseHost,
			@Named("databasePort") String databasePort,
			@Named("databaseName")String databaseName,
			@Named("databaseUser")String databaseUser,
			@Named("databasePassword")String databasePassword,
			@Named("databasePrefix")String databasePrefix, 
			@Named("glossaryType")String glossaryType,
			IGlossary glossary, 
			@Named("selectedSources")Set<String> selectedSources, 
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
	public List<Treatment> transform(List<Treatment> treatments) {
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
		terminologyLearner.learn(treatments, glossaryTable);
		terminologyLearner.readResults(treatments);
		Map<Treatment, LinkedHashMap<String, String>> sentencesForOrganStateMarker = terminologyLearner.getSentencesForOrganStateMarker();
		// do the actual markup
		markupDescriptions(treatments, sentencesForOrganStateMarker);		
		return treatments;
	}
}
