package semanticMarkup.io.input.lib.word;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semanticMarkup.io.input.AbstractFileVolumeReader;
import semanticMarkup.io.input.lib.word.refiner.DistributionTreatmentRefiner;
import semanticMarkup.io.input.lib.word.refiner.FloweringTimeTreatmentRefiner;
import semanticMarkup.model.ContainerTreatmentElement;
import semanticMarkup.model.Treatment;
import semanticMarkup.model.TreatmentElement;
import semanticMarkup.model.ValueTreatmentElement;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * AbstractWordVolumeReader reads a list of treatments from a by the previous charaparser version termed Type 1 input format. This is a
 * description in Microsoft Word format.
 * TODO: Look into http://poi.apache.org/ to see if it can be used to produce a better solution 
 * @author rodenhausen
 */
public abstract class AbstractWordVolumeReader extends AbstractFileVolumeReader {
	private String organNamesPattern ="2n|achene|anther|apex|awn|ax|bark|beak|blade|bract|bracteole|branch|branchlet|broad|calyx|capsule|cap_sule|caropohore|carpophore|caudex|cluster|corolla|corona|crown|cup_|cusp|cyme|cymule|embryo|endosperm|fascicle|filament|flower|fruit|head|herb|homophyllous|hypanthium|hypanth_ium|indument|inflore|inflorescence|inflores_cence|inflo_rescence|internode|involucre|invo_lucre|in_florescence|in_ternode|leaf|limb|lobe|margin|midvein|nectary|node|ocrea|ocreola|ovary|ovule|pair|papilla|pedicel|pedicle|peduncle|perennial|perianth|petal|petiole|plant|prickle|rhizome|rhi_zome|root|rootstock|rosette|scape|seed|sepal|shoot|spikelet|spur|stamen|stem|stigma|stipule|sti_pule|structure|style|subshrub|taproot|taprooted|tap_root|tendril|tepal|testa|tooth|tree|tube|tubercle|tubercule|tuft|twig|utricle|vein|vine|wing|x";
	private String organNamepPattern ="achenes|anthers|awns|axes|blades|bracteoles|bracts|branches|buds|bumps|calyces|capsules|clusters|crescents|crowns|cusps|cymes|cymules|ends|escences|fascicles|filaments|flowers|fruits|heads|herbs|hoods|inflores|inflorescences|internodes|involucres|leaves|lengths|limbs|lobes|margins|midribs|midveins|nectaries|nodes|ocreae|ocreolae|ovules|pairs|papillae|pedicels|pedicles|peduncles|perennials|perianths|petals|petioles|pistils|plants|prickles|pules|rescences|rhizomes|rhi_zomes|roots|rows|scapes|seeds|sepals|shoots|spikelets|stamens|staminodes|stems|stigmas|stipules|sti_pules|structures|styles|subshrubs|taproots|tap_roots|teeth|tendrils|tepals|trees|tubercles|tubercules|tubes|tufts|twigs|utricles|veins|vines|wings";
	private String usStates ="Ala\\.|Alabama|Alaska|Ariz\\.|Arizona|Ark\\.|Arkansas|Calif\\.|California|Colo\\.|Colorado|Conn\\.|Connecticut|Del\\.|Delaware|D\\.C\\.|District of Columbia|Fla\\.|Florida|Ga\\.|Georgia|Idaho|Ill\\.|Illinois|Ind\\.|Indiana|Iowa|Kans\\.|Kansas|Ky\\.|Kentucky|La\\.|Louisiana|Maine|Maryland|Md\\.|Massachusetts|Mass\\.|Michigan|Mich\\.|Minnesota|Minn\\.|Mississippi|Miss\\.|Missouri|Mo\\.|Montana|Mont\\.|Nebraska|Nebr\\.|Nevada|Nev\\.|New Hampshire|N\\.H\\.|New Jersey|N\\.J\\.|New Mexico|N\\.Mex\\.|New York|N\\.Y\\.|North Carolina|N\\.C\\.|North Dakota|N\\.Dak\\.|Ohio|Oklahoma|Okla\\.|Oregon|Oreg\\.|Pennsylvania|Pa\\.|Rhode Island|R\\.I\\.|South Carolina|S\\.C\\.|South Dakota|S\\.Dak\\.|Tennessee|Tenn\\.|Texas|Tex\\.|Utah|Vermont|Vt\\.|Virginia|Va\\.|Washington|Wash\\.|West Virginia|W\\.Va\\.|Wisconsin|Wis\\.|Wyoming|Wyo\\.";	
	private String caProvinces="Alta\\.|Alberta|B\\.C\\.|British Columbia|Manitoba|Man\\.|New Brunswick|N\\.B\\.|Newfoundland and Labrador|Nfld\\. and Labr|Northwest Territories|N\\.W\\.T\\.|Nova Scotia|N\\.S\\.|Nunavut|Ontario|Ont\\.|Prince Edward Island|P\\.E\\.I\\.|Quebec|Que\\.|Saskatchewan|Sask\\.|Yukon";
	
	private Pattern organPattern = 
			Pattern.compile("\\b("+this.organNamepPattern+"|"+this.organNamesPattern+")\\b", 
					Pattern.CASE_INSENSITIVE);
	private Pattern rankPattern = Pattern.compile
			("^((?:Genera|Genus|Species|Subspecies|Varieties|Subgenera).*?:)\\s*(introduced\\s*;)?(.*)");
	private Pattern floweringPattern = Pattern.compile("(Flowering.*?\\.)?(.*?(?:;|\\.$))?(\\s*of conservation concern\\s*(?:;|\\.$))?(.*?\\b(?:\\d+|m)\\b.*?(?:;|\\.$))?\\s*(introduced(?:;|\\.$))?(.*)");
	private Pattern caProvincesPattern = Pattern.compile(".*?\\b("+this.caProvinces+")(\\W|$).*");
	private Pattern referencePattern = Pattern.compile("(.*?\\d+–\\d+\\.\\]?)(\\s+[A-Z]\\w+,.*)");
	private Pattern markupKeyPattern1 = Pattern.compile("(.*?)(( ### [\\d ]+[a-z]?\\.| ?#* ?Group +\\d).*)");//determ
	private Pattern markupKeyPattern2 = Pattern.compile("^([\\d ]+[a-z]?\\..*?) (.? ?[A-Z].*)");//id   2. "Ray” corollas
	
	private Pattern nameAuthorityPattern1 = 
			Pattern.compile(".*?\\b(subfam|var|subgen|subg|subsp|ser|tribe|sect|subsect)\\b.*");
	private Pattern nameAuthorityPattern2 = 
			Pattern.compile("^([a-z]*?ceae)(\\b.*)", Pattern.CASE_INSENSITIVE);
	private Pattern nameAuthorityPattern3 = 
			Pattern.compile("^([A-Z][A-Z].*?)(\\b.*)"); 
	private Pattern nameAuthorityPattern4 = 
			Pattern.compile("^([A-Z].*?)\\s+([(A-Z].*)");
	
	private Pattern parseNamePattern1 = Pattern.compile("(.*?)(\\[.*?\\]$)");
	private Pattern parseNamePattern2 = 
			Pattern.compile("(.* [12]\\d\\d\\d)($|,|\\.| +)(.*)"); //TODO: a better fix is needed Brittonia 28: 427, fig. 1.  1977   ?  Yellow spinecape [For George Jones Goodman, 1904-1999
	private Pattern parseNamePattern3 = 
			Pattern.compile("\\((?:as )?(.*?)\\)(.*)");
	private Pattern parseNamePattern4 = 
			Pattern.compile("(.*?)[•·](.*?)(\\[.*|$)");
	private Pattern parseNamePattern5 = 
			Pattern.compile(".*?\\w+.*");
	
	private String styleMappingFile;
	private Properties styleMappings;
	private String styleStartPattern;
	private String styleNamePattern;
	
	private WordDocumentElementsExtractor documentElementsExtractor;
	private WordTaxonExtractor taxonExtractor;
	private DistributionTreatmentRefiner distributionTreatmentRefiner;
	private FloweringTimeTreatmentRefiner floweringTimeTreatmentRefiner;
	
	private String lastTextType;
	private int textcount;
	private String styleKeyPattern;
	private String tribegennamestyle;
	
	/**
	 * @param filepath
	 * @param styleStartPattern
	 * @param styleNamePattern
	 * @param styleKeyPattern
	 * @param tribegennamestyle
	 * @param styleMappingFile
	 * @param distributionTreatmentRefiner
	 * @param floweringTimeTreatmentRefiner
	 */
	@Inject
	public AbstractWordVolumeReader(@Named("WordVolumeReader_Sourcefile") String filepath,
			@Named("WordVolumeReader_StyleStartPattern") String styleStartPattern,
			@Named("WordVolumeReader_StyleNamePattern") String styleNamePattern,
			@Named("WordVolumeReader_StyleKeyPattern") String styleKeyPattern,
			@Named("WordVolumeReader_Tribegennamestyle") String tribegennamestyle, 
			//@Named("organnames") String organnamesPattern, 
			//@Named("organnamep") String organnamepPattern, 
			//@Named("usstates") String usStates, 
			//@Named("caprovinces") String caProvinces, 
			@Named("WordVolumeReader_StyleMappingFile") String styleMappingFile, 
			DistributionTreatmentRefiner distributionTreatmentRefiner, 
			FloweringTimeTreatmentRefiner floweringTimeTreatmentRefiner) {
		super(filepath);
		
		this.taxonExtractor = new WordTaxonExtractor();
		this.styleStartPattern = styleStartPattern;
		this.styleNamePattern = styleNamePattern;
		this.styleKeyPattern = styleKeyPattern;
		this.tribegennamestyle = tribegennamestyle;
		//this.organNamesPattern = organnamesPattern;
		//this.organNamepPattern = organnamepPattern;
		//this.usStates = usStates;
		//this.caProvinces = caProvinces;
		this.styleMappingFile = styleMappingFile;
		this.distributionTreatmentRefiner = distributionTreatmentRefiner;
		this.floweringTimeTreatmentRefiner = floweringTimeTreatmentRefiner;
	}
	
	@Override
	public List<Treatment> read() throws Exception {
		//File wordFile = new File(filePath);
		//Unzip.unzip(filePath, temporaryPath);
		//String xmlFilePath = temporaryPath + File.separator + wordFile.getName() + File.separator + "word" + File.separator + "document.xml";
		
		InputStream xmlInputStream = getXMLInputStream();
		WordDocumentElementsExtractor documentElementsExtractor = new WordDocumentElementsExtractor(
				xmlInputStream, this.styleStartPattern, this.styleNamePattern, this.styleKeyPattern,
				this.tribegennamestyle);
		
		//mapping of treatment to ... mapping of style to list of texts
		LinkedHashMap<Treatment, LinkedHashMap<String, ArrayList<DocumentElement>>> 
			documentElements = documentElementsExtractor.extract();
		taxonExtractor.extract(documentElements);
		
		//log(LogLevel.DEBUG, "---------------- read the document elements. Below the results: ");
		//log(LogLevel.DEBUG, "---------------- # of treatments: " + documentElements.size());
		//log(LogLevel.DEBUG, "---------------- the documentElements: \n" + new DocumentElementPrinter().print(documentElements));
		
		styleMappings = new Properties();
		styleMappings.load(new FileInputStream(styleMappingFile));
		
		List<Treatment> treatments = populateTreatments(documentElements);
		//log(LogLevel.DEBUG, "---------------- populate treatments. Below the results: ");
		//log(LogLevel.DEBUG, treatments.toString());
		
		
		
		return treatments;
	}

	protected abstract InputStream getXMLInputStream() throws Exception;

	private List<Treatment> populateTreatments(LinkedHashMap<Treatment, LinkedHashMap<String, 
			ArrayList<DocumentElement>>> documentElements) {
		List<Treatment> treatments = new ArrayList<Treatment>();
		for(Entry<Treatment, LinkedHashMap<String, ArrayList<DocumentElement>>> treatmentEntry 
			: documentElements.entrySet()) {
			Treatment treatment = treatmentEntry.getKey();
			HashMap<String, ArrayList<DocumentElement>> styleDocumentElementMappings = 
					treatmentEntry.getValue();
			textcount = 0;
			for(Entry<String, ArrayList<DocumentElement>> styleDocumentElementMapping :
				styleDocumentElementMappings.entrySet()) {
				String style = styleDocumentElementMapping.getKey();
				ArrayList<DocumentElement> documentElementsForStyle = 
						styleDocumentElementMapping.getValue();
				for(DocumentElement documentElement : documentElementsForStyle) {
					processDocumentElement(documentElement, style, treatment);
				}
			}

			// further mark up reference
			if(!treatment.containsContainerTreatmentElement("references"))
				treatment.addTreatmentElement(new ContainerTreatmentElement("references"));
			ContainerTreatmentElement referencesContainerTreatmentElement = treatment.getContainerTreatmentElements("references").get(0);
			
			for(ValueTreatmentElement reference : 
					treatment.getValueTreatmentElements("references"))
				furtherMarkupReference(reference, referencesContainerTreatmentElement, treatment);
			
			// further mark up keys <run_in_sidehead>
			List<TreatmentElement> keysAndCouplets = new LinkedList<TreatmentElement>();
			keysAndCouplets.addAll(treatment.getTreatmentElements("key"));
			keysAndCouplets.addAll(treatment.getTreatmentElements("couplet"));
			if(keysAndCouplets.size() > 0)
				furtherMarkupKeys(treatment);

			
			//takes created habitat descriptions previously and using regexp creates
			// a more detailed habitat list of <habitat>...</habitat> strings
			///HabitatParser4FNA hpf = new HabitatParser4FNA(dataPrefix);
			///hpf.parse();
			//VolumeFinalizer takes this list of habitat marked up strings and replaces
			// the previously existing habitat strings with the strings obtained above
			///VolumeFinalizer vf = new VolumeFinalizer(listener, null,
			///		dataPrefix, this.conn, glosstable, display);// display
																// output files
															// to listener
															// here.
			///vf.replaceWithAnnotated(hpf, "/treatment/habitat", "TRANSFORMED",
			///	true);
			// -> moved this part outside as not the responsibility of VolumeReader 
			//to further identify detailed information
			treatments.add(treatment);
		}
		return treatments;
	}

	private void processDocumentElement(DocumentElement documentElement, 
			String style, Treatment treatment) {
		//log(LogLevelDEBUG, "process documentElement \n" + documentElement.toString());
		
		String textType = styleMappings.getProperty(style);
		String text = documentElement.getText();
		if (style.matches(styleStartPattern)) {
			// process the name tag
			parseName(textType, text, treatment);
		} else if (style.matches(styleNamePattern)) {
			// process the synonym name tag
			parseSyn(textType, text, treatment);
		} else if (style.indexOf("Text") >= 0) {
			// process the description, distribution, discussion tag
			if (!text.trim().equals("")) {
				textcount++;
				lastTextType = parseText(textcount, text, treatment, lastTextType);
			}
		} else {
			treatment.addTreatmentElement(new ValueTreatmentElement(textType, 
					documentElement.getText()));
		}
	}

	private void furtherMarkupKeys(Treatment treatment) {
		assembleKeys(treatment);
		List<ContainerTreatmentElement> taxonKeys = 
				treatment.getContainerTreatmentElements("TaxonKey");
		//log(LogLevel.DEBUG, "taxonKeys size " + taxonKeys.size());
		for(ContainerTreatmentElement taxonKey : taxonKeys) {
			furtherMarkupKeyStatements(taxonKey, treatment);
		}
	}
	
	/* Turn individual statement :
	 *  <key>2. Carpels and stamens more than 5; plants perennial; leaves alternate; inflorescences ax-</key>
		 *	<key>illary, terminal, or leaf-opposed racemes or spikes ### 3. Phytolac ca ### (in part), p. 6</key>
	 * To:
	 * <key_statement>
	 * <statement_id>2</statement_id>
	 * <statement>Carpels and stamens more than 5; 
	 * plants perennial; leaves alternate; inflorescences ax-illary, terminal, 
	 * or leaf-opposed racemes or spikes</statement>
	 * <determination>3. Phytolacca (in part), p. 6</determination>
	 * </key_statement>
	 * 
	 * <determination> is optional, and may be replaced by <next_statement_id>.
	 * @param treatment
	 */
	private void furtherMarkupKeyStatements(ContainerTreatmentElement taxonKey, 
			Treatment treatment) {
		ArrayList<TreatmentElement> allStatements = new ArrayList<TreatmentElement>();
		ContainerTreatmentElement marked = new ContainerTreatmentElement("key");
		List<ValueTreatmentElement> states = taxonKey.getValueTreatmentElements();	
		String determinator = null;
		String id = "";
		String broken = "";
		String previousId = null;

		//process statements backwards
		for(int i = states.size()-1; i>=0; i--){
			ValueTreatmentElement state = states.get(i);
			if(state.getName().equals("key") || state.getName().equals("couplet")) {
				String text = state.getValue().trim()+broken;
				Matcher keyPatternMatcher = markupKeyPattern1.matcher(text);
				if(keyPatternMatcher.matches()){
					text = keyPatternMatcher.group(1).trim();
					determinator = keyPatternMatcher.group(2).trim();
				}
				keyPatternMatcher = markupKeyPattern2.matcher(text);
				if(keyPatternMatcher.matches()){//good, statement starts with an id
					id = keyPatternMatcher.group(1).trim();
					text = keyPatternMatcher.group(2).trim();
					broken = "";
					//form a statement
					ContainerTreatmentElement keyStatement = 
							new ContainerTreatmentElement("key_statement");
					ValueTreatmentElement stateid = 
							new ValueTreatmentElement("statement_id", 
									id.replaceAll("\\s*###\\s*", ""));
					ValueTreatmentElement statement = 
							new ValueTreatmentElement("statement", 
									text.replaceAll("\\s*###\\s*", ""));
					ValueTreatmentElement determination = null;
					ValueTreatmentElement nextId = null;
					if(determinator!=null) {
						determination = new ValueTreatmentElement("determination", 
								determinator.replaceAll("\\s*###\\s*", ""));
						determinator = null;
					} else if(previousId!=null) {
						nextId = new ValueTreatmentElement("next_statement_id",
								previousId.replaceAll("\\s*###\\s*", ""));
					}
					previousId = id;
					keyStatement.addTreatmentElement(stateid);
					keyStatement.addTreatmentElement(statement);
					if(determination!=null) keyStatement.addTreatmentElement(determination);
					if(nextId!=null) keyStatement.addTreatmentElement(nextId);
					allStatements.add(statement);
				}else if(text.matches("^[a-z]+.*")){//a broken statement, save it
					broken = text;
				}
			} else {
				ValueTreatmentElement stateClone = 
						(ValueTreatmentElement) state.clone();
				if(stateClone.getName().equals("run_in_sidehead")) {
					stateClone.setName("key_head");
				}
				allStatements.add(stateClone);//"discussion" remains
			}
		}
		
		for(int i = allStatements.size()-1; i >=0; i--){
			marked.addTreatmentElement(allStatements.get(i));
		}		
		treatment.addTreatmentElement(marked);
		treatment.removeTreatmentElement(taxonKey);
	}
	
	
	/**
	 * <treatment>
	 * <...>
	 * <references>...</references>
	 * <key>...</key>
	 * </treatment>
	 * deals with two cases:
	 * 1. the treatment contains one key with a set of "key/couplet" statements (no run_in_sidehead tags)
	 * 2. the treatment contains multiple keys that are started with <run_in_sidehead>Key to xxx (which may be also used to tag other content)
	 * @param treatment
	 */
	private void assembleKeys(Treatment treatment) {
		ContainerTreatmentElement key = null;
		boolean foundKey = false;
		List<TreatmentElement> detachedElements = new LinkedList<TreatmentElement>();
		for(ValueTreatmentElement element : treatment.getValueTreatmentElements()) {
			if(element.getName().equals("run_in_sidehead") &&
					(element.getValue().trim().startsWith("Key to ") || 
							element.getValue().trim().matches("Group \\d+.*"))){
				//log(LogLevel.DEBUG, "found key here");
				foundKey = true;
				if(key!=null)
					treatment.addTreatmentElement(key);
				key = new ContainerTreatmentElement("TaxonKey");
			}
			if(!foundKey && (element.getName().equals("key")) || 
					element.getName().equals("couplet")) {
				//log(LogLevel.DEBUG, "found key");
				foundKey = true;
				key = new ContainerTreatmentElement("TaxonKey");
			}	
			if(foundKey) {
				detachedElements.add(element);
				key.addTreatmentElement(element);
			}
		}
		if(key!=null)
			treatment.addTreatmentElement(key);
		for(TreatmentElement element : detachedElements)
			treatment.removeTreatmentElement(element);
	}
	
	
	/**
	 * turn
	 * <references>SELECTED REFERENCES Behnke, H.-D., C. Chang, I. J. Eifert, and T. J. Mabry. 1974. Betalains and P-type sieve-tube plastids in Petiveria and Agdestis (Phytolaccaceae). Taxon 23: 541–542. Brown, G. K. and G. S. Varadarajan. 1985. Studies in Caryophyllales I: Re-evaluation of classification of Phytolaccaceae s.l. Syst. Bot. 10: 49–63. Heimerl, A. 1934. Phytolaccaceae. In: H. G. A. Engler et al., eds. 1924+. Die natürlichen Pflanzenfamilien…, ed. 2. 26+ vols. Leipzig and Berlin. Vol. 16c, pp. 135–164. Nowicke, J. W. 1968. Palynotaxonomic study of the Phytolaccaceae. Ann. Missouri Bot. Gard. 55: 294–364. Rogers, G. K. 1985. The genera of Phytolaccaceae in the southeastern United States. J. Arnold Arbor. 66: 1–37. Thieret, J. W. 1966b. Seeds of some United States Phytolaccaceae and Aizoaceae. Sida 2: 352–360. Walter, H. P. H. 1906. Die Diagramme der Phytolaccaceen. Leipzig. [Preprinted from Bot. Jahrb. Syst. 37(suppl.): 1–57.] Walter, H. P. H. 1909. Phytolaccaceae. In: H. G. A. Engler, ed. 1900–1953. Das Pflanzenreich…. 107 vols. Berlin. Vol. 39[IV,83], pp. 1–154. Wilson, P. 1932. Petiveriaceae. In: N. L. Britton et al., eds. 1905+. North American Flora…. 47+ vols. New York. Vol. 21, pp. 257–266.</references>
	 * to
	 * <references><reference>Behnke, H.-D., C. Chang, I. J. Eifert, and T. J. Mabry. 1974. Betalains and P-type sieve-tube plastids in Petiveria and Agdestis (Phytolaccaceae). Taxon 23: 541–542. </reference> <reference>...</reference>....</references>
	 * @param treatment 
	 * @param ref
	 */
	private void furtherMarkupReference(ValueTreatmentElement referenceTreatmentElement, ContainerTreatmentElement referencesContainerElement,
			Treatment treatment) {
		treatment.removeTreatmentElement(referenceTreatmentElement);
		String reference = referenceTreatmentElement.getValue();
		Matcher referenceMatcher = referencePattern.matcher(reference);
		while(referenceMatcher.matches()){
			String referenceString = referenceMatcher.group(1);
			referencesContainerElement.addTreatmentElement(new ValueTreatmentElement("reference",
					referenceString));
			reference = referenceMatcher.group(2);
			referenceMatcher = referencePattern.matcher(reference);
		}
		referencesContainerElement.addTreatmentElement(new ValueTreatmentElement("reference", 
				"item:"+reference));
	}
	
	private void parseSyn(String textType, String text, Treatment treatment){
		if(treatment.containsTreatmentElement("variety_name")){
			textType = "synonym_of_variety_name";
		} else if(treatment.containsTreatmentElement("subspecies_name")){
			textType = "synonym_of_subspecies_name";
		} else if(treatment.containsTreatmentElement("species_name")){
			textType = "synonym_of_species_name";
		} else if(treatment.containsTreatmentElement("tribe_name")){
			textType = "synonym_of_tribe_name";
		} else if(treatment.containsTreatmentElement("genus_name")){
			textType = "synonym_of_genus_name";
		}
		treatment.addTreatmentElement(new ValueTreatmentElement(textType, text));
	}
	
	private String parseText(int textcount, String text, Treatment treatment, String lastTextType){
		String textType = "";
		Matcher m = organPattern.matcher(text);
		int organcount = 0;
		while(m.find()){
			organcount++;
		}
		if(textcount == 1 && organcount >= 2) {
			textType = "description";
			treatment.addTreatmentElement(new ValueTreatmentElement("description",
					text));
		} else if((textcount ==1 && organcount < 2) || lastTextType== "description") { //hong: 3/11/10 for FNA v19
			//treatment.addAttachment("distribution", text);
			textType = "distribution";
			//TODO: further markup distribution to: # of infrataxa, introduced, generalized distribution, flowering time,habitat, elevation, state distribution, global distribution 
			//addElement("distribution", text, treatment);
			parseDistriTag(text, treatment);
		}
		else if(lastTextType.equals("distribution") || lastTextType.equals("discussion"))
			treatment.addTreatmentElement(new ValueTreatmentElement("discussion",
					text));
		return textType;
	}
		
	private void parseDistriTag(String text, Treatment treatment){
		Matcher rankMatcher = rankPattern.matcher(text);
		if(rankMatcher.matches()){ //species and higher
			if(rankMatcher.group(1) != null)
				treatment.addTreatmentElement(new ValueTreatmentElement(
						"number_of_infrataxa", rankMatcher.group(1)));
			if(rankMatcher.group(2)!=null)
				treatment.addTreatmentElement(new ValueTreatmentElement(
						"introduced", rankMatcher.group(2)));
			if(rankMatcher.group(3) != null) {
				distributionTreatmentRefiner.refine(treatment, rankMatcher.group(3), "general_distribution");
			}	
		} else { //species and lower
			Matcher floweringMatcher = floweringPattern.matcher(text);
			if(floweringMatcher.matches()){//TODO:habitat, elevation, state distribution, global distribution
				if(floweringMatcher.group(1) != null)
					floweringTimeTreatmentRefiner.refine(treatment, floweringMatcher.group(1), "flowering_time");
				if(floweringMatcher.group(2)!= null)
					treatment.addTreatmentElement(new ValueTreatmentElement(
							"habitat", floweringMatcher.group(2)));
				if(floweringMatcher.group(3)!= null)
					treatment.addTreatmentElement(new ValueTreatmentElement(
							"conservation", floweringMatcher.group(3)));
				if(floweringMatcher.group(4)!= null)
					treatment.addTreatmentElement(new ValueTreatmentElement(
							"elevation", floweringMatcher.group(4)));
				if(floweringMatcher.group(5)!= null)
					treatment.addTreatmentElement(new ValueTreatmentElement(
							"introduced", floweringMatcher.group(5)));
				if(floweringMatcher.group(6)!= null){
					String[] distributions = floweringMatcher.group(6).split(";");
					for(int i= 0; i<distributions.length; i++) 
						if(distributions[i].matches(".*?\\b("+this.usStates+")(\\W|$).*")) 
							distributionTreatmentRefiner.refine(treatment, distributions[i], "us_distribution");
						else if(distributions[i].matches(".*?\\b("+this.caProvinces+")(\\W|$).*")) 
							distributionTreatmentRefiner.refine(treatment, distributions[i], "ca_distribution");
						else
							distributionTreatmentRefiner.refine(treatment, distributions[i], "global_distribution");
				}
			} else
				System.err.println("distribution not match: "+text);
		}
	}
	
	private String parseName(String nameRank, String line, Treatment treatment) {
		if(line == null || line.equals(""))
			return ""; //TODO: should not happen. but did happen with v. 19 295.xml==>VolumeExtractor JDOM problem.
	
		String name = taxonExtractor.getName(treatment);
		if(name == null || name.equals(""))
			return "";
		
		// make a copy of the line and will work on the new copy
		String text = new String(line);
		text = text.replaceAll(" ", " ").replaceAll("\\s+", " ").trim(); 
		//there are some whitespaces that are not really a space, don't know what they are. 
		
		String number = taxonExtractor.getNumber(treatment);
		// TODO: add the number tag to the sytle mapping
		
		//log(LogLevel.DEBUG, "parseName creates a number for treatment " + treatment.hashCode());
		if(!treatment.containsTreatmentElement("number"))
			treatment.addTreatmentElement(new ValueTreatmentElement("number", number));
		
		text = taxonExtractor.fixBrokenNames(text);
		text = text.replaceFirst("^.*?(?=[A-Z])", "").trim();;
		
		//namerank and name
		//(subfam|var|subgen|subg|subsp|ser|tribe|subsect)
		if(nameRank.indexOf("species_subspecies_variety_name")>=0){
			if(text.indexOf("var.") >=0){
				nameRank = "variety_name";
			}else if(text.indexOf("subsp.") >=0){
				nameRank = "subspecies_name";
			}else if(text.indexOf("ser.") >=0 && text.indexOf(",") > text.indexOf("ser.")){ //after "," is publication where ser. may appear.
				nameRank = "series_name";
			}else if(text.indexOf("sect.") >=0){
				nameRank = "section_name";
			}else if(text.indexOf("subsect.") >=0){
				nameRank = "subsection_name";
			}else {
				nameRank = "species_name";
			}
		}
		
		String[] nameinfo = getNameAuthority(name);
		if(nameinfo[0]!=null && nameinfo[1]!=null) {
			if(!treatment.containsTreatmentElement(nameRank)) 		
				treatment.addTreatmentElement(
						new ValueTreatmentElement(nameRank, nameinfo[0]));
			//	vtDbA.add2TaxonTable(number, name, namerank, index+1);
			if(nameinfo[1].length()>0){
				if(treatment.containsTreatmentElement("authority"))
					treatment.addTreatmentElement(
							new ValueTreatmentElement("authority", nameinfo[1]));
				//vtDbA.add2AuthorTable(nameinfo[1]);
			}
			text = text.replaceFirst("^\\s*.{"+name.length()+"}","").trim();
		}
		
		//derivation: deal with this first to remove [] and avoid pub-year match in []
		Matcher m = parseNamePattern1.matcher(text);
		if(m.matches()){
			if(m.group(2).trim().equals("")){
				treatment.addTreatmentElement(new ValueTreatmentElement("etymology", 
						m.group(2).trim()));
			}
			text = m.group(1).trim();
		}
		
		//place of publication 
		//Pattern p = Pattern.compile("(.* [12]\\d\\d\\d|.*(?=Â·)|.*(?=.))(.*)"); //TODO: a better fix is needed Brittonia 28: 427, fig. 1.  1977   ?  Yellow spinecape [For George Jones Goodman, 1904-1999
		m = parseNamePattern2.matcher(text);
		if(m.matches()){
			String publicationPlace = 
					m.group(1).replaceFirst("^\\s*[,\\.]", "").trim();			
			extractPublicationPlace(treatment, publicationPlace); //pp may be "Sp. Pl. 1: 480.  1753; Gen. Pl. ed. 5, 215.  1754"
			text = m.group(3).trim();
		}

		// conserved
		String conserved="name conserved";
		int	pos = text.indexOf(conserved);
		if(pos < 0){
			conserved="name proposed for conservation";
			pos = text.indexOf(conserved);
		}
		if(pos < 0){
			conserved="nom. cons.";
			pos = text.indexOf(conserved);
		}
		if (pos != -1) {
			//String conserved = text.substring(pos).trim();
			text = text.replace(conserved, "").trim();
			//conserved = conserved.replaceFirst("^\\s*[,;\\.]", "");
			treatment.addTreatmentElement(new ValueTreatmentElement(
					"conserved", conserved));
			// trim the text
			//int p1 = text.lastIndexOf(',', pos);
			//text = text.substring(0, p1);
		}

		//past_name
		m = parseNamePattern3.matcher(text);
		if(m.matches()){
			if(!m.group(1).trim().equals("")){
				treatment.addTreatmentElement(new ValueTreatmentElement(
						"past_name", m.group(1).trim()));
			}
			text = m.group(2).trim();
		}

		//common name
		m = parseNamePattern4.matcher(text);
		if(m.matches()){
			if(!m.group(2).trim().equals("")){
				String[] commonnames = m.group(2).trim().split("\\s*,\\s*");
				for(String cname: commonnames){
					treatment.addTreatmentElement(
							new ValueTreatmentElement("common_name", cname));
				}
			}
			text = (m.group(1)+" "+m.group(3)).trim();
		}

		m = parseNamePattern5.matcher(text.trim());
		if(m.matches()){
			treatment.addTreatmentElement(new ValueTreatmentElement("unparsed",
					text));
		}
		return nameRank.replace("_name", "");
	}
	
	/**
	 * family, genus, species has authority
	 * lower ranked taxon have authorities in names themselves
	 * 
	 * Cactaceae Jussieu subfam. O puntioideae Burnett
	 * @param name
	 * @return nameinfo array
	 */
	private String[] getNameAuthority(String name) {
		String[] nameinfo = new String[2];
		Matcher m = nameAuthorityPattern1.matcher(name);
		if(m.matches()){
			nameinfo[0] = name;
			nameinfo[1] = "";
			return nameinfo;
		}
		//family
		m = nameAuthorityPattern2.matcher(name);
		if(m.matches()){
			nameinfo[0] = m.group(1).replaceAll("\\s", "").trim(); //in case an extra space is there
			nameinfo[1] = m.group(2).trim();
			return nameinfo;
		}
		//genus
		m = nameAuthorityPattern3.matcher(name);
		if(m.matches()){
			nameinfo[0] = m.group(1).replaceAll("\\s", "").trim();
			nameinfo[1] = m.group(2).trim();
			return nameinfo;
		}
		//species
		m = nameAuthorityPattern4.matcher(name);
		if(m.matches()){
			nameinfo[0] = m.group(1).trim();
			nameinfo[1] = m.group(2).trim();
			return nameinfo;
		}
		return nameinfo;
	}

	private void extractPublicationPlace(Treatment treatment, 
			String publicationPlace) {
		publicationPlace = publicationPlace.replaceFirst("^\\s*,", "").trim();
		String publicationTitle = "";
		String placeInPublication = "";
		String[] publicationPlaces = publicationPlace.split(";");
		for(String apub: publicationPlaces){
			String place_in_publication="(.*?)(\\d.*?)";
			Matcher pubm=Pattern.compile(place_in_publication).matcher(apub);
			if(pubm.matches()){
				publicationTitle=pubm.group(1).trim();
				placeInPublication=pubm.group(2).trim();
			}
						
			ContainerTreatmentElement placeOfPub = 
					new ContainerTreatmentElement("place_of_publication");
			placeOfPub.addTreatmentElement(
					new ValueTreatmentElement("publication_title", 
							publicationTitle));
			placeOfPub.addTreatmentElement(
					new ValueTreatmentElement("place_in_publication", 
							placeInPublication));
			treatment.addTreatmentElement(placeOfPub);
			//vtDbA.add2PublicationTable(pub);
		}
	}
}
