package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.ontology.search.FileSearcher;
import edu.arizona.biosemantics.common.ontology.search.Searcher;
import edu.arizona.biosemantics.common.ontology.search.TaxonGroupOntology;
import edu.arizona.biosemantics.common.ontology.search.model.Ontology;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry.Type;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.LearnException;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Processor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Resource;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Software;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;

/**
 * @author rodenhausen, cui
 * 4/10/18: added character mapping and modified the format of mapping results
 */
public class OntologyMappingTreatmentTransformer extends AbstractDescriptionTransformer {

	private String etcUser;
	private Collection<Searcher> searchers;
	private TaxonGroup taxonGroup;

	@Inject
	public OntologyMappingTreatmentTransformer(
			@Named("Version") String version,
			@Named("MarkupDescriptionTreatmentTransformer_ParallelProcessing")boolean parallelProcessing, 
			@Named("User")String etcUser, 
			@Named("TaxonGroup")TaxonGroup taxonGroup,
			@Named("OntologiesDirectory")String ontologiesDirectory,
			@Named("WordNetAPI_Sourcefile")String wordNetSource) {
		super(version, parallelProcessing);
		this.etcUser = etcUser;
		this.taxonGroup = taxonGroup;
		searchers = new LinkedList<Searcher>();
		for(Ontology entityOntology : TaxonGroupOntology.getEntityOntologies(taxonGroup)) 
			searchers.add(new FileSearcher(entityOntology, ontologiesDirectory, wordNetSource));
		
	}

	@Override
	public Processor transform(List<AbstractDescriptionsFile> descriptionsFiles) throws TransformationException {
		createOntologyMapping(descriptionsFiles);		

		Processor processor = new Processor();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		processor.setDate(dateFormat.format(new Date()));
		processor.setOperator(etcUser);
		
		//TODO: Allow multiple resources per processor in schema
		String ontologies = "";
		for(Ontology ontology : TaxonGroupOntology.getEntityOntologies(taxonGroup)) {
			ontologies += ontology.toString() + ",  ";
		}
		if(!ontologies.isEmpty())
			ontologies = ontologies.substring(0, ontologies.length() - 2);
		Resource resource = new Resource();
		resource.setName(ontologies);
		resource.setType("Ontology(ies)");
		resource.setVersion("N/A");
		
		Software software = new Software();
		software.setName("CharaParser - Ontology Mapping");
		software.setType("Semantic Markup");
		software.setVersion(version);
		processor.setSoftware(software);
		processor.setResource(resource);
		
		return processor;
	}
	
	
	/*
	 * Note the format of a match: classIRI[search term:matching parent label/matching label: matching score]
	 * format of multiple matches match1;match2;
	 */
	protected void createOntologyMapping(List<AbstractDescriptionsFile> descriptionsFiles) {
		//TODO: Parallel processing similar to description markup
		
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			for(Description description : descriptionsFile.getDescriptions()) {
				for(Statement statement : description.getStatements()) {
					for(BiologicalEntity structure : statement.getBiologicalEntities()) {
						//collect non-numerical character/quality values
						LinkedHashSet<Character> characters = structure.getCharacters();
						ArrayList<String> modifierCharacters = new ArrayList<String>();
						ArrayList<Character> allCharacters = new ArrayList<Character>();
						
						for(Character c: characters){
							allCharacters.add(c);
							if(c.getIsModifier()!=null && c.getIsModifier().compareTo("true")==0)
								modifierCharacters.add(c.getValue());	
						}
						
						//map biological entities
						HashSet<String> searchEntityTerms = new HashSet<String> ();
						//simple organ name
						String sName = structure.getName().trim();
						//constrained organ name
						if(structure.getConstraint() != null) 
							sName = structure.getConstraint().trim() + " " + sName;
						searchEntityTerms.add(sName);
						
						//modified organ name
						StringBuffer modifiers = new StringBuffer();
						for(String mValue: modifierCharacters){
							modifiers.append(mValue+" ");
						}
						searchEntityTerms.add(modifiers.toString()+sName);
						
						//search organ names
						ArrayList<String> matchedNames = new ArrayList<String> ();
						StringBuffer matches = new StringBuffer();
						for(String searchTerm: searchEntityTerms){
							OntologyEntry entry = getEntity(searchTerm);
							if(entry != null) {
								matchedNames.add(searchTerm);
								log(LogLevel.DEBUG, "Found IRI: " + entry.getClassIRI() + " for term " + searchTerm);
								matches.append(entry.getClassIRI()+"["+searchTerm+":"+entry.getParentLabel()+"/"+entry.getLabel()+":"+entry.getScore()+"];"); //Note the format of multiple matches: IRI[search term:matching parent term/matching term: matching score];IRI[search term:matching parent term/matching term: matching score];
							}
						}
						if(matches.toString().length()>0)
							structure.setOntologyId(matches.toString());
						
						//search character values
						for(Character character: allCharacters){
							if(!(character.getIsModifier()!=null && character.getIsModifier().compareTo("true")==0 && isMatched(character.getValue(), matchedNames))){
							String cValue = character.getValue();
							if(cValue!=null && !cValue.matches("[.0-9()]+")){
								OntologyEntry entry = getQuality(cValue);
									if(entry != null) {
										log(LogLevel.DEBUG, "Found IRI: " + entry.getClassIRI() + " for term " + cValue);
										character.setOntologyId(entry.getClassIRI()+"["+cValue+":"+entry.getParentLabel()+"/"+entry.getLabel()+":"+entry.getScore()+"];"); ;
										}
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	
    /**
     * 
     * @param word
     * @param matchedNames
     * @return true if word is part of any matchedName
     */
	private boolean isMatched(String word, ArrayList<String> matchedNames) {

		for(String matchedName: matchedNames){
			if(matchedName.matches(".*?\\b"+word+"\\b.*?")) return true;
		}
		
		return false;
	}

	private OntologyEntry getEntity(String searchTerm) {
		for(Searcher searcher : searchers) {
			List<OntologyEntry> ontologyEntries = searcher.getEntityEntries(searchTerm, "", "");
			if(!ontologyEntries.isEmpty()) {
				OntologyEntry entry = ontologyEntries.get(0);
				log(LogLevel.DEBUG, "Highest scored ontology entity" + entry.getScore());
				//if(entry.getScore()==1.0) {
					return entry;
				//}
			}
		}
		return null;
	}
	
	private OntologyEntry getQuality(String searchTerm) {
		for(Searcher searcher : searchers) {
			List<OntologyEntry> ontologyEntries = searcher.getQualityEntries(searchTerm);
			if(!ontologyEntries.isEmpty()) {
				OntologyEntry entry = ontologyEntries.get(0);
				log(LogLevel.DEBUG, "Highest scored ontology entity" + entry.getScore());
				//if(entry.getScore() == 1.0) {
					return entry;
				//}
			}
		}
		return null;
	}

}
