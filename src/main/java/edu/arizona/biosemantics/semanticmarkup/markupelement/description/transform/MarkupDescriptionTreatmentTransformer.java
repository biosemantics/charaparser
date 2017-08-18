package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.oto.client.WordRole;
import edu.arizona.biosemantics.oto.client.lite.OTOLiteClient;
import edu.arizona.biosemantics.oto.client.oto.OTOClient;
import edu.arizona.biosemantics.oto.model.GlossaryDownload;
import edu.arizona.biosemantics.oto.model.TermCategory;
import edu.arizona.biosemantics.oto.model.TermSynonym;
import edu.arizona.biosemantics.oto2.oto.server.rest.client.Client;
import edu.arizona.biosemantics.oto2.oto.shared.model.Collection;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label;
import edu.arizona.biosemantics.semanticmarkup.config.Configuration;
import edu.arizona.biosemantics.semanticmarkup.db.ConnectionPool;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.ling.know.lib.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.INormalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParser;
import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.Term;
import edu.arizona.biosemantics.common.ling.pos.IPOSTagger;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.IDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.LearnException;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Processor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Resource;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Software;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.TreatmentRoot;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * Transforms the treatments by semantically marking up the description treatment element of a treatment
 * This can be used for the second and hence the 'markup' application for the iPlant integration
 * @author rodenhausen
 */
public class MarkupDescriptionTreatmentTransformer extends AbstractDescriptionTransformer {

	protected IParser parser;
	protected IPOSTagger posTagger;
	protected IDescriptionExtractor descriptionExtractor;
	protected INormalizer normalizer;
	protected ITerminologyLearner terminologyLearner;
	protected ITokenizer wordTokenizer;
	protected ChunkerChain chunkerChain;
	protected int descriptionExtractorRunMaximum;
	protected int sentenceChunkerRunMaximum;
	protected Map<Description, Future<Description>> futureNewDescriptions = 
			new HashMap<Description, Future<Description>>();
	protected OTOClient otoClient;
	protected String databasePrefix;
	protected IGlossary glossary;
	protected TaxonGroup taxonGroup;
	protected Client oto2Client;
	protected String oto2TermReviewURL;
	protected Set<String> selectedSources;
	private String glossaryTable;
	private boolean termCategorizationRequired;
	private IInflector inflector;
	private String etcUser;
	private boolean useEmptyGlossary;
	private ConnectionPool connectionPool;
	
	/**
	 * @param wordTokenizer
	 * @param parser
	 * @param chunkerChain
	 * @param posTagger
	 * @param descriptionExtractor
	 * @param normalizer
	 * @param terminologyLearner
	 * @param parallelProcessing
	 * @param descriptionExtractorRunMaximum
	 * @param sentenceChunkerRunMaximum
	 * @param otoClient
	 * @param databasePrefix
	 * @param glossary
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception
	 */
	@Inject
	public MarkupDescriptionTreatmentTransformer(
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
			OTOClient otoClient, 
			Client oto2Client, 
			@Named("OTO2TermReviewURL") String oto2TermReviewURL,
			@Named("DatabasePrefix")String databasePrefix, 
			@Named("TaxonGroup")TaxonGroup taxonGroup,
			IGlossary glossary, 
			@Named("SelectedSources")Set<String> selectedSources, 
			@Named("GlossaryTable")String glossaryTable,
			@Named("termCategorizationRequired")boolean termCategorizationRequired,
			IInflector inflector, 
			@Named("User")String etcUser, 
			@Named("UseEmptyGlossary") boolean useEmptyGlossary, 
			ConnectionPool connectionPool) throws ClassNotFoundException, SQLException {
		super(version, parallelProcessing);
		this.inflector = inflector;
		this.parser = parser;
		this.posTagger = posTagger;
		this.chunkerChain = chunkerChain;
		this.descriptionExtractor = descriptionExtractor;
		this.normalizer = normalizer;
		this.terminologyLearner = terminologyLearner;
		this.wordTokenizer = wordTokenizer;
		this.descriptionExtractorRunMaximum = descriptionExtractorRunMaximum;
		this.sentenceChunkerRunMaximum = sentenceChunkerRunMaximum;
		this.otoClient = otoClient;
		this.oto2Client = oto2Client;
		this.oto2TermReviewURL = oto2TermReviewURL;
		this.databasePrefix = databasePrefix;
		this.glossary = glossary;
		this.taxonGroup = taxonGroup;
		this.selectedSources = selectedSources;
		this.glossaryTable = glossaryTable;
		this.termCategorizationRequired = termCategorizationRequired;
		this.etcUser = etcUser;
		this.useEmptyGlossary = useEmptyGlossary;
		this.connectionPool = connectionPool;
		
		//normalizer.init(); //moved after learn is complete so normalizer can read the results from learn
	}

	@Override
	public Processor transform(List<AbstractDescriptionsFile> descriptionsFiles) throws TransformationException, LearnException {
		//download gloss again from real OTO because the last download is no longer in memory
		//it is possible for gloss o change from last run, make sure to grab the correct version.
		//when remove MYSQL, take care of this issue
		
		//TODO: String version = otoClient.getLatestVersion();
		//String tablePrefix = glossaryType + "_" + glossaryDownload.getVersion();
		//String glossaryTable = tablePrefix + "_glossary";
		//if(!glossaryExistsLocally(tablePrefix)) {
		
		GlossaryDownload glossaryDownload = new GlossaryDownload();
		String glossaryVersion = "N/A";
		if(!useEmptyGlossary) {
			glossaryVersion = getGlossaryVersionOfLearn();
			if(glossaryVersion == null)
				glossaryVersion = "latest";
			boolean downloadSuccessful = false;
			otoClient.open();
			Future<GlossaryDownload> futureGlossaryDownload = otoClient.getGlossaryDownload(taxonGroup.getDisplayName(), glossaryVersion);
			
			try {
				glossaryDownload = futureGlossaryDownload.get();
				
				downloadSuccessful = glossaryDownload != null && 
						!glossaryDownload.getVersion().equals("Requested version not available") && 
						!glossaryDownload.getVersion().equals("No Glossary Available") &&
						!glossaryDownload.getVersion().contains("available") && 
						!glossaryDownload.getVersion().contains("Available");
				if(!downloadSuccessful) glossaryDownload = getLocalGlossaryDownload(taxonGroup);
			} catch (Exception e) {
				otoClient.close();
				log(LogLevel.ERROR, "Couldn't download glossary " + taxonGroup.getDisplayName() + " version: " + glossaryVersion, e);
				throw new TransformationException();
			}
			otoClient.close();
			
			glossaryVersion = glossaryDownload.getVersion();
		}
				
	    Collection collection = null;
		try {
			collection = readUploadResult();
		} catch (SQLException e) {
			this.log(LogLevel.ERROR, "Problem reading upload result", e);
			throw new TransformationException();
		}
		
		 
		if(collection != null) {
			try {
				oto2Client.open();
				Future<Collection> futureCollection = oto2Client.get(collection.getId(), collection.getSecret());
				collection = futureCollection.get();
				oto2Client.close();
			} catch(InterruptedException | ExecutionException e) {
				oto2Client.close();
				this.log(LogLevel.ERROR, "Problem downloading oto lite categorizations for upload " + collection.getId(), e);
				System.out.println("Problem downloading oto lite categorizations for upload " + collection.getId());
				throw new TransformationException();
			}
		}

		
		if(collection == null)
        /*if(!collection.isFinalized() && termCategorizationRequired) {
        	log(LogLevel.ERROR, "The term categorization has to be finalized to run markup. Please return to categorizing terms and finalize first.");
        	throw new TransformationException();
        }*/
		
		log(LogLevel.DEBUG, "Size of permanent glossary downloaded:\n" +
				"Number of term categoy relations " + glossaryDownload.getTermCategories().size() + "\n" +
				"Number of term synonym relations " + glossaryDownload.getTermSynonyms().size());
		if(collection != null) 
			log(LogLevel.DEBUG, "Size of temporary glossary downloaded:\n" +
					"Number of term categoy relations " + getTermCategoryRelationCount(collection) + "\n" +
					"Number of term synonym relations " + getSynonymRelationCount(collection) + "\n");
		//storeInLocalDB(glossaryDownload, download, this.databasePrefix);
		 
		initGlossary(glossaryDownload, collection); //turn "_" in glossary terms to "-"
		
		//this is needed to initialize terminologylearner (DatabaseInputNoLearner / fileTreatments)
		//even though no actual learning is taking place
		//Question TODO  all term category info are in the glossary, then why do we need terminologyLearner?
		terminologyLearner.learn(descriptionsFiles, glossaryTable);
		terminologyLearner.readResults(descriptionsFiles);
		
		normalizer.init();
		

		Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker = 
				terminologyLearner.getSentencesForOrganStateMarker(); //sentence level markup: modifier##tag##sentence text
		// do the actual markup
		markupDescriptions(descriptionsFiles, sentencesForOrganStateMarker);	
		
		
	

		Processor processor = new Processor();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		processor.setDate(dateFormat.format(new Date()));
		processor.setOperator(etcUser);
		Resource resource = new Resource();
		resource.setName(taxonGroup.getDisplayName());
		resource.setType("OTO Glossary");
		resource.setVersion(glossaryVersion);
		Software software = new Software();
		software.setName("CharaParser");
		software.setType("Semantic Markup");
		software.setVersion(version);
		processor.setSoftware(software);
		if(!useEmptyGlossary)
			processor.setResource(resource);
		
		return processor;
	}
	
    private int getSynonymRelationCount(Collection collection) {
    	int count = 0;
		for(Label label : collection.getLabels()) {
			for(edu.arizona.biosemantics.oto2.oto.shared.model.Term mainTerm : label.getMainTerms()) {
				count += label.getSynonyms(mainTerm).size();
			}
		}
		return count;
	}

	private int getTermCategoryRelationCount(Collection collection) {
		int count = 0;
		for(Label label : collection.getLabels()) {
			count += label.getMainTerms().size();
		}
		return count;
	}

	private Collection readUploadResult() throws SQLException {
    	try(Connection connection = connectionPool.getConnection()) {
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

	private String getGlossaryVersionOfLearn() {
		String glossaryVersion = null;
		try(Connection connection = connectionPool.getConnection()) {
			String sql = "SELECT glossary_version FROM datasetprefixes WHERE prefix = ?";
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				preparedStatement.setString(1, databasePrefix);
				preparedStatement.execute();
				
				try(ResultSet resultSet = preparedStatement.getResultSet()) {
				
					while(resultSet.next()) {
						glossaryVersion = resultSet.getString("glossary_version");
					}
				}
			}
		} catch (SQLException e) {
			log(LogLevel.ERROR, "Could not read glossary version used for learning", e);
		}
		return glossaryVersion;
	}

	private int readUploadId() throws SQLException {
		try(Connection connection = connectionPool.getConnection()) {
			int uploadId = -1;
			String sql = "SELECT oto_uploadid FROM datasetprefixes WHERE prefix = ?";
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				preparedStatement.setString(1, databasePrefix);
				preparedStatement.execute();
				try(ResultSet resultSet = preparedStatement.getResultSet()) {
					while(resultSet.next()) {
						uploadId = resultSet.getInt("oto_uploadid");
					}
					return uploadId;
				}
			}
		}
	}

	private GlossaryDownload getLocalGlossaryDownload(TaxonGroup taxonGroup) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(Configuration.glossariesDownloadDirectory + File.separator + 
				"GlossaryDownload." + taxonGroup.getDisplayName() + ".ser"));
		GlossaryDownload glossaryDownload = (GlossaryDownload) objectIn.readObject();
		objectIn.close();
		return glossaryDownload;
	}
	
	/**
	 * 
	 * Merge glossaryDownload and collection to one glossary which holds both terms and synonyms
	 * note: decisions from collection (results from term review by the user) takes priority over those from glossary
	 * note: synonyms and terms are disjoint -- add only term as entry to the glossary (addEntry), synonyms added as synonyms (addSynonym)
	 * For structure terms, both singular and plural forms are included in the synonyms
	 * @param otoGlossary
	 */
	protected void initGlossary(GlossaryDownload glossaryDownload, Collection collection) {

		log(LogLevel.DEBUG, "initiate in-memory glossary using glossaryDownload and collection...");
		log(LogLevel.DEBUG, "obtaining synonyms from glossaryDownload...");
		//1. obtain synonyms from glossaryDownload
		HashSet<Term> gsyns = new HashSet<Term>();
		obtainSynonymsFromGlossaryDownload(glossaryDownload, gsyns);
		
		log(LogLevel.DEBUG, "obtaining synonyms from collection...");
		//2. obtain synonyms from collection
		HashSet<Term> dsyns = new HashSet<Term>();
		obtainSynonymsFromCollection(collection, dsyns);
		
		log(LogLevel.DEBUG, "merging synonyms...");
		//3. merge synonyms into one set
		gsyns = mergeSynonyms(gsyns, dsyns);
		
		log(LogLevel.DEBUG, "adding synonyms to in-mem glossary...");
		//4. addSynonyms to glossary
		HashSet<Term> simpleSyns = addSynonyms2Glossary(gsyns);
		
		log(LogLevel.DEBUG, "adding preferred terms to in-mem glossary...");
		//5. addEntry 
		//the glossaryDownload, excluding syns
		for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
			if(!simpleSyns.contains(new Term(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory())))
				glossary.addEntry(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory()); //primocane_foliage =>primocane-foliage Hong 3/2014
			else
				log(LogLevel.DEBUG, "synonym not add to in-mem glossary: "+termCategory.getTerm().replaceAll("_", "-")+"<"+termCategory.getCategory()+">");
		}

		//the collection, excluding syns
		if(collection != null) {			
			for(Label label : collection.getLabels()) {
				for(edu.arizona.biosemantics.oto2.oto.shared.model.Term mainTerm : label.getMainTerms()) {
					if(!simpleSyns.contains(new Term(mainTerm.getTerm().replaceAll("_",  "-"), label.getName()))){//calyx_tube => calyx-tube
						glossary.addEntry(mainTerm.getTerm().replaceAll("_",  "-"), label.getName()); 
					    log(LogLevel.DEBUG, "adding collection term to in-mem glossary: "+ mainTerm.getTerm().replaceAll("_",  "-")+"<"+label.getName()+">");
					}else
						log(LogLevel.DEBUG, "synonym not add to in-mem glossary: "+ mainTerm.getTerm().replaceAll("_",  "-")+"<"+label.getName()+">");
				}
			}
		}
	}

	/**
	 * 
	 * @param gsyns
	 * @return set of synonyms added (minus the preferred terms)
	 */
	private HashSet<Term> addSynonyms2Glossary(HashSet<Term> gsyns) {
		HashSet<Term> simpleSyns = new HashSet<Term>();
		Iterator<Term> sit = gsyns.iterator();
		while(sit.hasNext()){
			Term syn = sit.next();
			String[] tokens = syn.getLabel().split(":");
			String category = syn.getCategory();
			glossary.addSynonym(tokens[0], category, tokens[1]);
		    log(LogLevel.DEBUG, "adding synonym to in-mem glossary: "+ tokens[0]+" U "+tokens[1]+"<"+category+">");
			simpleSyns.add(new Term(tokens[0], category));
		}
		return simpleSyns;
	}

	/**
	 * Term string takes the form of "syn:preferred"
	 * @param gsyns
	 * @param dsyns
	 * @return
	 */
	private HashSet<Term> mergeSynonyms(HashSet<Term> gsyns, HashSet<Term> dsyns) {
		HashSet<Term> merged = new HashSet<Term>();
		Iterator<Term> git = gsyns.iterator();
		while(git.hasNext()){ 
			Iterator<Term> dit = dsyns.iterator();
			Term gsyn = git.next();
			String gcat = gsyn.getCategory();
			List<String> gtokens = Arrays.asList(gsyn.getLabel().split(":"));
			while(dit.hasNext()){ //nested loop, very inefficient
				Term dsyn = dit.next();
				String dcat = dsyn.getCategory();
				List<String> dtokens = Arrays.asList(dsyn.getLabel().split(":"));
				if(!gcat.equals(dcat)){
					//add both to merged
					merged.add(gsyn);
				    log(LogLevel.DEBUG, "add to merged synonyms: "+ gsyn.toString());
					merged.add(dsyn);
					log(LogLevel.DEBUG, "add to merged synonyms: "+ dsyn.toString());
				}else{
					boolean isSame = false; //all four terms are synonyms
					for(String t: gtokens){
						if(dtokens.contains(t)) isSame = true;
					}
					if(isSame){
						//use preferred term of dsyns as the preferred term 
						if(dtokens.get(1).equals(gtokens.get(1))){//share the same preferred term,
							// add both to merged SET
							merged.add(gsyn);
							log(LogLevel.DEBUG, "add to merged synonyms: "+ gsyn.toString());
							merged.add(dsyn);	
							log(LogLevel.DEBUG, "add to merged synonyms: "+ dsyn.toString());
						}else{
							merged.add(dsyn);
							if(!gtokens.get(0).equals(dtokens.get(1))){ //don't add B:B
								merged.add(new Term(gtokens.get(0)+":"+dtokens.get(1), dcat));
								log(LogLevel.DEBUG, "add to merged synonyms: "+ new Term(gtokens.get(0)+":"+dtokens.get(1), dcat).toString());
							}
							if(!gtokens.get(1).equals(dtokens.get(1))){
								merged.add(new Term(gtokens.get(1)+":"+dtokens.get(1), dcat));
								log(LogLevel.DEBUG, "add to merged synonyms: "+ new Term(gtokens.get(1)+":"+dtokens.get(1), dcat).toString());
							}
							
						}
					}else{
						//add both to merged
						merged.add(gsyn);
						log(LogLevel.DEBUG, "add to merged synonyms: "+ gsyn.toString());
						merged.add(dsyn);
						log(LogLevel.DEBUG, "add to merged synonyms: "+ dsyn.toString());
					}
				}
			}
		}
		return merged;
	}

	private void obtainSynonymsFromCollection(Collection collection, HashSet<Term> dsyns) {
		if(collection != null) {			
			for(Label label : collection.getLabels()) {
				for(edu.arizona.biosemantics.oto2.oto.shared.model.Term mainTerm : label.getMainTerms()) {
					//if(!dsyns.contains(new Term(mainTerm.getTerm().replaceAll("_",  "-"), label.getName())))//calyx_tube => calyx-tube
					//	glossary.addEntry(mainTerm.getTerm().replaceAll("_",  "-"), label.getName());  
					
					//Hong TODO need to add category info to synonym entry in OTOLite
					//if(termSyn.getCategory().compareTo("structure")==0){
					if(label.getName().matches(ElementRelationGroup.entityElements)){
						for(edu.arizona.biosemantics.oto2.oto.shared.model.Term synonym : label.getSynonyms(mainTerm)) {
							//take care of singular and plural forms
							String syns = ""; 
							String synp = "";
							String terms = "";
							String termp = "";
							if(inflector.isPlural(synonym.getTerm().replaceAll("_",  "-"))){
								synp = synonym.getTerm().replaceAll("_",  "-");
								syns = inflector.getSingular(synp);					
							}else{
								syns = synonym.getTerm().replaceAll("_",  "-");
								synp = inflector.getPlural(syns);
							}
	
							if(inflector.isPlural(mainTerm.getTerm().replaceAll("_",  "-"))){
								termp = mainTerm.getTerm().replaceAll("_",  "-");
								terms = inflector.getSingular(termp);					
							}else{
								terms = mainTerm.getTerm().replaceAll("_",  "-");
								termp = inflector.getPlural(terms);
							}
							//plural forms are synonyms to the singular
							if(!syns.equals(terms)){ 							
								dsyns.add(new Term(syns+":"+terms, label.getName()));
								log(LogLevel.DEBUG, "synonym from collection: "+ new Term(syns+":"+terms, label.getName()).toString());
							}
							if(!synp.equals(terms)){ 
								dsyns.add(new Term(synp+":"+terms, label.getName()));
								log(LogLevel.DEBUG, "synonym from collection: "+ new Term(synp+":"+terms, label.getName()).toString());
							}
							if(!termp.equals(terms)){ 
								dsyns.add(new Term(termp+":"+terms, label.getName()));
								log(LogLevel.DEBUG, "synonym from collection: "+ new Term(termp+":"+terms, label.getName()).toString());
							}
						}
					} else {//forking_1 and forking are syns 5/5/14 hong test, shouldn't _1 have already been removed?
						for(edu.arizona.biosemantics.oto2.oto.shared.model.Term synonym : label.getSynonyms(mainTerm)) {
							//glossary.addSynonym(synonym.getTerm().replaceAll("_",  "-"), label.getName(), mainTerm.getTerm());
							dsyns.add(new Term(synonym.getTerm().replaceAll("_",  "-")+":"+mainTerm.getTerm().replaceAll("_",  "-"), label.getName()));
							log(LogLevel.DEBUG, "synonym from collection: "+ new Term(synonym.getTerm().replaceAll("_",  "-")+":"+mainTerm.getTerm(), label.getName()).toString());
						}
					}				
				}
			}
		}
	}

	private void obtainSynonymsFromGlossaryDownload(GlossaryDownload glossaryDownload, HashSet<Term> gsyns) {
		for(TermSynonym termSyn: glossaryDownload.getTermSynonyms()){

			//if(termSyn.getCategory().compareTo("structure")==0){
			if(termSyn.getCategory().matches(ElementRelationGroup.entityElements)){
				//take care of singular and plural forms
				String syns = ""; 
				String synp = "";
				String terms = "";
				String termp = "";
				if(inflector.isPlural(termSyn.getSynonym().replaceAll("_",  "-"))){ //must convert _ to -, as matching entity phrases will be converted from leg iii to leg-iii in the sentence.
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
				//plural forms are synonyms to the singular
				if(!syns.equals(terms)){ 
					gsyns.add(new Term(syns+":"+terms, termSyn.getCategory()));
					log(LogLevel.DEBUG, "synonym from glossaryDownload: "+new Term(syns+":"+terms, termSyn.getCategory()).toString());
				}
				if(!synp.equals(terms)){
					gsyns.add(new Term(synp+":"+terms, termSyn.getCategory()));
					log(LogLevel.DEBUG, "synonym from glossaryDownload: "+new Term(synp+":"+terms, termSyn.getCategory()).toString()); 
				}
				if(!termp.equals(terms)){ 
					gsyns.add(new Term(termp+":"+terms, termSyn.getCategory()));
					log(LogLevel.DEBUG, "synonym from glossaryDownload: "+ new Term(termp+":"+terms, termSyn.getCategory()).toString());
				}
			}else{
				//glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory(), termSyn.getTerm());
				gsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-")+":"+termSyn.getTerm().replaceAll("_",  "-"), termSyn.getCategory()));
				log(LogLevel.DEBUG, "synonym from glossaryDownload: "+new Term(termSyn.getSynonym().replaceAll("_",  "-")+":"+termSyn.getTerm(), termSyn.getCategory()).toString()); 
			}
		}
	}
	
	
	/*bad version
	protected void initGlossary(GlossaryDownload glossaryDownload, Collection collection) {

		//add the syn set of the glossary
		HashSet<Term> gsyns = new HashSet<Term>();
		for(TermSynonym termSyn: glossaryDownload.getTermSynonyms()){

			//if(termSyn.getCategory().compareTo("structure")==0){
			if(termSyn.getCategory().matches(ElementRelationGroup.entityElements)){
				//take care of singular and plural forms
				String syns = ""; 
				String synp = "";
				String terms = "";
				String termp = "";
				if(inflector.isPlural(termSyn.getSynonym().replaceAll("_",  "-"))){ //must convert _ to -, as matching entity phrases will be converted from leg iii to leg-iii in the sentence.
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
				glossary.addSynonym(syns, termSyn.getCategory(), terms);
				glossary.addSynonym(synp, termSyn.getCategory(), termp);
				gsyns.add(new Term(syns, termSyn.getCategory()));
				gsyns.add(new Term(synp, termSyn.getCategory()));
			}else{
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

		//add syn set of term_category
		HashSet<Term> dsyns = new HashSet<Term>();
		if(collection != null) {			
			for(Label label : collection.getLabels()) {
				for(edu.arizona.biosemantics.oto2.oto.shared.model.Term mainTerm : label.getMainTerms()) {
					if(!dsyns.contains(new Term(mainTerm.getTerm().replaceAll("_",  "-"), label.getName())))//calyx_tube => calyx-tube
						glossary.addEntry(mainTerm.getTerm().replaceAll("_",  "-"), label.getName());  
					
					//Hong TODO need to add category info to synonym entry in OTOLite
					//if(termSyn.getCategory().compareTo("structure")==0){
					if(label.getName().matches(ElementRelationGroup.entityElements)){
						for(edu.arizona.biosemantics.oto2.oto.shared.model.Term synonym : label.getSynonyms(mainTerm)) {
							//take care of singular and plural forms
							String syns = ""; 
							String synp = "";
							String terms = "";
							String termp = "";
							if(inflector.isPlural(synonym.getTerm().replaceAll("_",  "-"))){
								synp = synonym.getTerm().replaceAll("_",  "-");
								syns = inflector.getSingular(synp);					
							}else{
								syns = synonym.getTerm().replaceAll("_",  "-");
								synp = inflector.getPlural(syns);
							}
	
							if(inflector.isPlural(mainTerm.getTerm().replaceAll("_",  "-"))){
								termp = mainTerm.getTerm().replaceAll("_",  "-");
								terms = inflector.getSingular(termp);					
							}else{
								terms = mainTerm.getTerm().replaceAll("_",  "-");
								termp = inflector.getPlural(terms);
							}
							//glossary.addSynonym(syns, termSyn.getCategory(), terms);
							//glossary.addSynonym(synp, termSyn.getCategory(), termp);
							//dsyns.add(new Term(syns, termSyn.getCategory());
							//dsyns.add(new Term(synp, termSyn.getCategory());
							glossary.addSynonym(syns, label.getName(), terms);
							glossary.addSynonym(synp, label.getName(), termp);
							dsyns.add(new Term(syns, label.getName()));
							dsyns.add(new Term(synp, label.getName()));
						}
					} else {//forking_1 and forking are syns 5/5/14 hong test, shouldn't _1 have already been removed?
						for(edu.arizona.biosemantics.oto2.oto.shared.model.Term synonym : label.getSynonyms(mainTerm)) {
							glossary.addSynonym(synonym.getTerm().replaceAll("_",  "-"), label.getName(), mainTerm.getTerm());
							dsyns.add(new Term(synonym.getTerm().replaceAll("_",  "-"), label.getName()));
						}
					}				
				}
			}
		}
		
		//glossary.addEntry("distance", "character");
	
	}*/
	
//	private void storeInLocalDB(GlossaryDownload glossaryDownload, Download download, 
//			String tablePrefix) {
//		List<TermCategory> termCategories = new ArrayList<TermCategory>();
//		List<WordRole> wordRoles = new ArrayList<WordRole>();
//		List<TermSynonym> termSynonyms = new ArrayList<TermSynonym>();
//		
//		termCategories.addAll(glossaryDownload.getTermCategories());
//		termSynonyms.addAll(glossaryDownload.getTermSynonyms());
//		
//		List<Decision> decisions = download.getDecisions();
//		List<Synonym> synonyms = download.getSynonyms();
//
//		HashSet<String> hasSynSet = new HashSet<String>();
//		for(Synonym synonym : synonyms) {
//			TermSynonym termSynonym = new TermSynonym();
//			termSynonym.setTerm(synonym.getTerm());
//			termSynonym.setSynonym(synonym.getSynonym());
//			termSynonyms.add(termSynonym);
//			hasSynSet.add(synonym.getTerm());
//		}
//		
//		for(Decision decision : decisions) {
//			TermCategory termCategory = new TermCategory();
//			termCategory.setTerm(decision.getTerm());
//			termCategory.setCategory(decision.getCategory());
//			termCategory.setHasSyn(hasSynSet.contains(decision.getTerm()));
//			termCategories.add(termCategory);
//		}
//		
//		/** generate the wordroles from termcategories **/
//		//remove duplicates, term is primary key in the table
//		HashSet<String> wordSet = new HashSet<String>();
//		for(TermCategory termCategory : termCategories) {
//			if(wordSet.contains(termCategory.getTerm()))
//				continue;
//			wordSet.add(termCategory.getTerm());
//			WordRole wordRole = new WordRole();
//			wordRole.setWord(termCategory.getTerm());
//			String semanticRole = "c";
//			//if(termCategory.getCategory().equalsIgnoreCase("structure")) {
//			if(termCategory.getCategory().toLowerCase().matches(ElementRelationGroup.entityElements)) {
//				semanticRole = "op";
//			}
//			wordRole.setSemanticRole(semanticRole);
//			wordRole.setSavedid(""); //not really needed, is a left over from earlier charaparser times
//			wordRoles.add(wordRole);
//		}
//		
//		try(Connection connection = connectionPool.getConnection()) {	
//			try(Statement stmt = connection.createStatement()) {
//		        String cleanupQuery = "DROP TABLE IF EXISTS " + 
//										tablePrefix + "_term_category, " + 
//										tablePrefix + "_syns, " +
//										tablePrefix + "_wordroles;";
//		        stmt.execute(cleanupQuery);
//		        stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_syns (`term` varchar(200) DEFAULT NULL, `synonym` varchar(200) DEFAULT NULL)  CHARACTER SET utf8 engine=innodb");
//				stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_term_category (`term` varchar(100) DEFAULT NULL, `category` varchar(200) " +
//						"DEFAULT NULL, `hasSyn` tinyint(1) DEFAULT NULL)  CHARACTER SET utf8 engine=innodb");
//				stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_wordroles (`word` varchar(200) NOT NULL DEFAULT '', `semanticrole` varchar(2) " +
//						"NOT NULL DEFAULT '', `savedid` varchar(40) DEFAULT NULL, PRIMARY KEY (`word`,`semanticrole`))  CHARACTER SET utf8 engine=innodb");
//				
//				for(TermCategory termCategory : termCategories) {
//					try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tablePrefix + "_term_category (`term`, `category`, `hasSyn`) VALUES (?, ?, ?)")) {
//						preparedStatement.setString(1, termCategory.getTerm());
//						preparedStatement.setString(2, termCategory.getCategory());
//						preparedStatement.setBoolean(3, termCategory.isHasSyn());
//						preparedStatement.executeUpdate();
//					}
//				}
//				for(TermSynonym termSynonym : termSynonyms) {
//					try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tablePrefix + "_syns (`term`, `synonym`) VALUES (?, ?)")) {
//						preparedStatement.setString(1, termSynonym.getTerm());
//						preparedStatement.setString(2, termSynonym.getSynonym());
//						preparedStatement.executeUpdate();
//					}
//				}
//				for(WordRole wordRole : wordRoles) {
//					try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tablePrefix + "_wordroles" + " VALUES (?, ?, ?)")) {
//						preparedStatement.setString(1, wordRole.getWord());
//						preparedStatement.setString(2, wordRole.getSemanticRole());
//						preparedStatement.setString(3, wordRole.getSavedid());
//						preparedStatement.executeUpdate();
//					}
//				}
//			}
//		} catch(Exception e) {
//			log(LogLevel.ERROR, "Problem storing glossary in local DB", e);
//		}
//	}

	/**
	 * @param treatments
	 * @param sentencesForOrganStateMarker
	 */
	protected void markupDescriptions(List<AbstractDescriptionsFile> descriptionsFiles, Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker) {
		//configure exectuorService to only allow a number of threads to run at a time
		ExecutorService executorService = null;
		if(!this.parallelProcessing)
			executorService = Executors.newSingleThreadExecutor();
		if(this.parallelProcessing && this.descriptionExtractorRunMaximum < Integer.MAX_VALUE)
			executorService = Executors.newFixedThreadPool(descriptionExtractorRunMaximum);
		if(this.parallelProcessing && this.descriptionExtractorRunMaximum == Integer.MAX_VALUE)
			executorService = Executors.newCachedThreadPool();
		
		int descriptionCount = 0;
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			descriptionCount += descriptionsFile.getDescriptions().size();
		}
		CountDownLatch descriptionExtractorsLatch = new CountDownLatch(descriptionCount);

		// process each treatment separately
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			for(int i=0; i<descriptionsFile.getDescriptions().size(); i++) {
				Description description = descriptionsFile.getDescriptions().get(i);
				//start a DescriptionExtractorRun for the treatment to process as a separate thread
				DescriptionExtractorRun descriptionExtractorRun = new DescriptionExtractorRun(
						descriptionsFile, description, i, normalizer, wordTokenizer, 
						posTagger, parser, chunkerChain, descriptionExtractor, sentencesForOrganStateMarker, parallelProcessing, sentenceChunkerRunMaximum, 
						descriptionExtractorsLatch, selectedSources);
				Future<Description> futureNewDescription = executorService.submit(descriptionExtractorRun);
				this.futureNewDescriptions.put(description, futureNewDescription);
			}
		}
		
		//only continue when all threads are done
		try {
			descriptionExtractorsLatch.await();
			executorService.shutdown();
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, "Problem with descriptionExtractorsLatch or executorService", e);
		}
		
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			for(Description description : descriptionsFile.getDescriptions()) {
				Future<Description> futureNewDescription = futureNewDescriptions.get(description);
				description.setText("");
				/*try {
					//description.setText(futureNewDescription.get().getText());
				} catch (Exception e) {
					log(LogLevel.DEBUG, "Problem getting Future from new description", e);
				}*/
				log(LogLevel.DEBUG, " -> JAXB: ");
				log(LogLevel.DEBUG, description.toString());
			}
		}
	}

}
