package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.Term;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto.client.oto.OTOClient;
import edu.arizona.biosemantics.oto.model.GlossaryDownload;
import edu.arizona.biosemantics.oto.model.TermCategory;
import edu.arizona.biosemantics.oto.model.TermSynonym;
import edu.arizona.biosemantics.oto2.oto.server.rest.client.Client;
import edu.arizona.biosemantics.oto2.oto.shared.model.Collection;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label;
import edu.arizona.biosemantics.semanticmarkup.config.Configuration;
import edu.arizona.biosemantics.semanticmarkup.db.ConnectionPool;
import edu.arizona.biosemantics.semanticmarkup.ling.know.lib.ElementRelationGroup;

public class GlossaryInitializer {

	//private OTOClient otoClient;
	private TaxonGroup taxonGroup;
	private ConnectionPool connectionPool;
	private String databasePrefix;
	private Client oto2Client;
	private IInflector inflector;

	@Inject
	public GlossaryInitializer(/*OTOClient otoClient,*/
			@Named("TaxonGroup")TaxonGroup taxonGroup,
			ConnectionPool connectionPool,
			@Named("DatabasePrefix")String databasePrefix,
			Client oto2Client,
			IInflector inflector) {
		//this.otoClient = otoClient;
		this.taxonGroup = taxonGroup;
		this.connectionPool = connectionPool;
		this.databasePrefix = databasePrefix;
		this.oto2Client = oto2Client;
		this.inflector = inflector;
	}

	public void initialize(IGlossary glossary) throws Exception {
		GlossaryDownload glossaryDownload = new GlossaryDownload();
		String glossaryVersion = "latest";
		//boolean downloadSuccessful = false;
		//otoClient.open();
		//Future<GlossaryDownload> futureGlossaryDownload = otoClient.getGlossaryDownload(taxonGroup.getDisplayName(), glossaryVersion);
		try {
			//glossaryDownload = futureGlossaryDownload.get();
			/*downloadSuccessful = glossaryDownload != null &&
					!glossaryDownload.getVersion().equals("Requested version not available") &&
					!glossaryDownload.getVersion().equals("No Glossary Available") &&
					!glossaryDownload.getVersion().contains("available") &&
					!glossaryDownload.getVersion().contains("Available");*/
			/*if(!downloadSuccessful)*/ glossaryDownload = getLocalGlossaryDownload(taxonGroup);
		} catch (Exception e) {
			//otoClient.close();
			log(LogLevel.ERROR, "Couldn't download glossary " + taxonGroup.getDisplayName() + " version: " + glossaryVersion, e);
			throw e;
		}
		//otoClient.close();

		glossaryVersion = glossaryDownload.getVersion();

		Collection collection = null;
		try {
			collection = readUploadResult();
		} catch (Exception e) {
			this.log(LogLevel.ERROR, "Problem reading upload result", e);
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
				throw e;
			}
		}



		log(LogLevel.DEBUG, "Size of permanent glossary downloaded:\n" +
				"Number of term categoy relations " + glossaryDownload.getTermCategories().size() + "\n" +
				"Number of term synonym relations " + glossaryDownload.getTermSynonyms().size());
		if(collection != null)
			log(LogLevel.DEBUG, "Size of temporary glossary downloaded:\n" +
					"Number of term categoy relations " + getTermCategoryRelationCount(collection) + "\n" +
					"Number of term synonym relations " + getSynonymRelationCount(collection) + "\n");
		//storeInLocalDB(glossaryDownload, download, this.databasePrefix);

		initGlossary(glossary, glossaryDownload, collection); //turn "_" in glossary terms to "-"
	}

	private GlossaryDownload getGlossaryDownload() throws ClassNotFoundException, IOException {
		/*otoClient.open();
		Future<GlossaryDownload> futureGlossaryDownload = otoClient.getGlossaryDownload(taxonGroup.getDisplayName());

		boolean downloadSuccessful = false;
		GlossaryDownload glossaryDownload = null;
		try {
			glossaryDownload = futureGlossaryDownload.get();
			downloadSuccessful = glossaryDownload != null &&
					!glossaryDownload.getVersion().equals("Requested version not available") &&
					!glossaryDownload.getVersion().equals("No Glossary Available") &&
					!glossaryDownload.getVersion().contains("available") &&
					!glossaryDownload.getVersion().contains("Available");
		} catch(Throwable t) {
			log(LogLevel.ERROR, "Couldn't download glossary will fallback to locally stored glossary", t);
		}

		otoClient.close();
		if(downloadSuccessful)
			storeToLocalGlossaryDownload(glossaryDownload, taxonGroup);
		else*/
		GlossaryDownload	glossaryDownload = getLocalGlossaryDownload(taxonGroup);
		return glossaryDownload;
	}

	private void storeToLocalGlossaryDownload(GlossaryDownload glossaryDownload, TaxonGroup taxonGroup) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Configuration.glossariesDownloadDirectory + File.separator +
					"GlossaryDownload." + taxonGroup.getDisplayName() + ".ser"));
			out.writeObject(glossaryDownload);
			out.close();
		} catch(Exception e) {
			log(LogLevel.ERROR, "Couldn't store glossaryDownload locally", e);
		}
	}

	private GlossaryDownload getLocalGlossaryDownload(TaxonGroup taxonGroup) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(Configuration.glossariesDownloadDirectory + File.separator +
				"GlossaryDownload." + taxonGroup.getDisplayName() + ".ser"));
		GlossaryDownload glossaryDownload = (GlossaryDownload) objectIn.readObject();
		objectIn.close();
		return glossaryDownload;
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

	/**
	 *
	 * Merge glossaryDownload and collection to one glossary which holds both terms and synonyms
	 * note: decisions from collection (results from term review by the user) takes priority over those from glossary
	 * note: synonyms and terms are disjoint -- add only term as entry to the glossary (addEntry), synonyms added as synonyms (addSynonym)
	 * For structure terms, both singular and plural forms are included in the synonyms
	 * @param glossaryDownload
	 * @param collection
	 */
	protected void initGlossary(IGlossary glossary, GlossaryDownload glossaryDownload, Collection collection) {

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
		HashSet<Term> simpleSyns = addSynonyms2Glossary(glossary, gsyns);

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
	private HashSet<Term> addSynonyms2Glossary(IGlossary glossary, HashSet<Term> gsyns) {
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

}
