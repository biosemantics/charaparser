package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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

/**
 * @author rodenhausen
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
		for(Ontology ontology : TaxonGroupOntology.getOntologies(taxonGroup)) 
			searchers.add(new FileSearcher(ontology, ontologiesDirectory, wordNetSource));
		
	}

	@Override
	public Processor transform(List<AbstractDescriptionsFile> descriptionsFiles) throws TransformationException, LearnException {
		createOntologyMapping(descriptionsFiles);		

		Processor processor = new Processor();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		processor.setDate(dateFormat.format(new Date()));
		processor.setOperator(etcUser);
		
		//TODO: Allow multiple reosures per processor in schema
		String ontologies = "";
		for(Ontology ontology : TaxonGroupOntology.getOntologies(taxonGroup)) {
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
	

	protected void createOntologyMapping(List<AbstractDescriptionsFile> descriptionsFiles) {
		//TODO: Parallel processing similar to description markup
		
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			for(Description description : descriptionsFile.getDescriptions()) {
				for(Statement statement : description.getStatements()) {
					for(BiologicalEntity structure : statement.getBiologicalEntities()) {
						String searchTerm = structure.getName().trim();
						if(structure.getConstraint() != null) 
							searchTerm = structure.getConstraint().trim() + " " + searchTerm;
						String iri = getIRI(searchTerm);
						if(iri != null) {
							log(LogLevel.DEBUG, "Found IRI: " + iri + " for term " + searchTerm);
							structure.setOntologyId(iri);
						}
					}
				}
			}
		}
	}

	private String getIRI(String searchTerm) {
		log(LogLevel.DEBUG, "Search " + searchTerm);
		for(Searcher searcher : searchers) {
			List<OntologyEntry> ontologyEntries = searcher.getEntries(searchTerm, Type.ENTITY);
			if(!ontologyEntries.isEmpty()) {
				log(LogLevel.DEBUG, "Highest scored ontology entity" + ontologyEntries.get(0).getScore());
				if(ontologyEntries.get(0).getScore() == 1.0) {
					return ontologyEntries.get(0).getClassIRI();
				}
			}
		}
		return null;
	}

}
