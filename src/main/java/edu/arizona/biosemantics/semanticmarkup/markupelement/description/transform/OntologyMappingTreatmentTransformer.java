package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.arizona.biosemantics.oto.common.model.GlossaryDownload;
import edu.arizona.biosemantics.oto.common.model.TermCategory;
import edu.arizona.biosemantics.oto.common.model.TermSynonym;
import edu.arizona.biosemantics.oto.common.model.lite.Decision;
import edu.arizona.biosemantics.oto.common.model.lite.Download;
import edu.arizona.biosemantics.oto.common.model.lite.Synonym;
import edu.arizona.biosemantics.oto.common.model.lite.UploadResult;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.ITerm;
import edu.arizona.biosemantics.semanticmarkup.know.lib.Term;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.INormalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParser;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.IPOSTagger;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.ITokenizer;
import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.ontology.search.FileSearcher;
import edu.arizona.biosemantics.common.ontology.search.Searcher;
import edu.arizona.biosemantics.common.ontology.search.TaxonGroupOntology;
import edu.arizona.biosemantics.common.ontology.search.model.Ontology;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.IDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.LearnException;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Processor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Resource;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Software;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;



/**
 * Transforms the treatments by semantically marking up the description treatment element of a treatment
 * This can be used for the second and hence the 'markup' application for the iPlant integration
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
			@Named("EtcUser")String etcUser, 
			@Named("TaxonGroup")TaxonGroup taxonGroup,
			@Named("OntologyMappingTreatmentTransformer_OntologyDirectory")String ontologyDirectory,
			@Named("WordNetAPI_Sourcefile")String wordNetSource) {
		super(version, parallelProcessing);
		this.etcUser = etcUser;
		this.taxonGroup = taxonGroup;
		
		searchers = new LinkedList<Searcher>();
		
		for(Ontology ontology : TaxonGroupOntology.getOntologies(taxonGroup)) 
			searchers.add(new FileSearcher(ontology, ontologyDirectory, wordNetSource));
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
						String searchTerm = (structure.getConstraint() + " " + structure.getName()).trim();
						String iri = getIRI(searchTerm);
						if(iri == null) {
							searchTerm = structure.getName();
							iri = getIRI(searchTerm);
						}
						if(iri != null)
							structure.setOntologyId(iri);
					}
				}
			}
		}
	}

	private String getIRI(String searchTerm) {
		for(Searcher searcher : searchers) {
			List<OntologyEntry> ontologyEntries = searcher.getEntries(searchTerm);
			if(ontologyEntries.get(0).getScore() == 1.0) {
				return ontologyEntries.get(0).getClassIRI();
			}
		}
		return null;
	}

}