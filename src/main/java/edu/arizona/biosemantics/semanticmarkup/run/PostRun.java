/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.run;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jdom2.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.validation.key.KeyElementValidator;
import edu.arizona.biosemantics.common.validation.key.KeyValidationException;


/**
 * @author Hong Cui
 * after Run is complete, perform post-markup tasks, which requires access to all marked up files simultaneously 
 * such as organizing descriptive characters in keys to morphological description elements
 * or creating new xml files holding character info for some taxa.
 * 
 * @TODO implements IRun
 *
 */
public class PostRun {
	String runOutDirectory;

	//String validateSchemaFile;
	static XMLOutputter out = new XMLOutputter();
	static XPathFactory fac = null;
	static XPathExpression<Element> keyPath = null;
	static XPathExpression<Element> detPath = null;
	static XPathExpression<Element> allDetPath = null;
	static XPathExpression<Element> nextIdPath = null;
	static XPathExpression<Element> namePath = null;
	static XPathExpression<Element> keyHead = null;

	

	static{
		fac = XPathFactory.instance();
		keyPath = fac.compile("//bio:treatment/key", Filters.element(), null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		detPath = fac.compile(".//determination", Filters.element()); //use "." to limit the selection in the key element (not the entire document)
		allDetPath = fac.compile("//determination", Filters.element()); //don't use "."
		nextIdPath = fac.compile(".//next_statement_id", Filters.element());
		keyHead  = fac.compile("//key//key_head", Filters.element()); //don't use "."
		//stateIdPath = fac.compile("//key//statement_id", Filters.element());
		namePath = fac.compile("//bio:treatment/taxon_identification[@status='ACCEPTED']", Filters.element(), null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		
	}

	/**
	 * 
	 */
	@Inject
	public PostRun(@Named("Run_OutDirectory")String runOutDirectory /*, @Named("MarkupRun_ValidateSchemaFile") String validateSchemaFile*/) {
		this.runOutDirectory = runOutDirectory;
		//this.validateSchemaFile = validateSchemaFile;
	}


	public void absorbKeys() throws Exception {
		//walk through files in runOutDirectory to find keys
		//create hashtables to link taxa to file names
		Hashtable<String, Document> taxa2doc = new Hashtable<String, Document>();
		Hashtable<Document, File> doc2file = new Hashtable<Document, File>();
		ArrayList<Document> containsKey = new ArrayList<Document>();

		SAXBuilder builder = new SAXBuilder();
		Document document;
		File [] files = new File(runOutDirectory).listFiles();
		for(File file: files){	
			try{
				document = (Document) builder.build(file);
				Element root = document.getRootElement();
				Element ti = (Element) namePath.evaluate(root).get(0);
				String name = "";
				for(Element tn: ti.getChildren("taxon_name")){
					name += tn.getAttributeValue("rank")+"_"+tn.getTextTrim().toLowerCase()+" ";
				}
				name = name.trim();
				if(!name.isEmpty()){
					taxa2doc.put(name, document);
					doc2file.put(document, file);
				}

				if(!keyPath.evaluate(root).isEmpty()){
					containsKey.add(document);
				}
			}catch(Exception e){
				log(LogLevel.ERROR, "Failed to read xml file: "+file);
				throw e;
			}

		}
		log(LogLevel.DEBUG, "Collected "+containsKey.size()+" files with a key");
		//absorb characters in keys into taxon descriptions (existing or new)
		//leaving the original key unchanged in the source document
		KeyElementValidator kev = new KeyElementValidator();
		
		//ArrayList<File> newFiles = new ArrayList<File> ();
		int count = 0;
		for(Document docWKey: containsKey){
			//baseURI:file:/C:/Users/updates/git/charaparser/workspace/rubus_fna_2/out/1.xml
			String keyFileName = docWKey.getBaseURI().substring(docWKey.getBaseURI().lastIndexOf("/")+1).replaceFirst("\\.xml$", "");
			log(LogLevel.DEBUG, "Processing keys in "+docWKey.getBaseURI());
			for(Element key: keyPath.evaluate(docWKey.getRootElement())){
				ArrayList<String> keyErrors = new ArrayList<String>();
				count++;
				log(LogLevel.DEBUG, "Processing key "+count+": "+key.getChildText("key_head"));
				//check if the key is formatted properly, break if not
				try{
					kev.validate(key, keyErrors);
				}catch(KeyValidationException e){
					log(LogLevel.DEBUG, "not a valid key in file "+docWKey.getBaseURI()+":"+key.getChildText("key_head"));
					for(String message : e.getAllErrors()){
						log(LogLevel.DEBUG, message);
					}
					break;
				}
				/*if(! ){
					log(LogLevel.DEBUG, "not a valid key in file "+docWKey.getBaseURI()+":"+key.getChildText("key_head"));
					break; //if one key is not valid in a document (other keys may refers to this bad key), go to the next doc
				}*/
				for(Object determination: detPath.evaluate(key)){
					//if determination matches a key_head, so is not a taxon name
					if(matchesKeyHead((Element)determination, key)) continue;
					
					String taxonName = getName((Element) determination, docWKey);
					Element description = new Element("description");
					description.setAttribute("type", "morphology_from_key");
					wrapCharacter(description, (Element)determination, (Element)key, keyFileName);
					
					if(description.getContentSize()==0) continue;
					
					Document doc = locateTaxonDocument(taxonName, taxa2doc);
					if(doc!=null){
						//add a new description element of type 'morphology_from_key' to the doc for the taxon
						//one determination may occur multiple times in one key, need to deduplicate description statements before add description to the related doc
						addDescription(doc, description);
						//doc.getRootElement().addContent(description);
						writeFile(doc, doc2file.get(doc));
						log(LogLevel.DEBUG, "Add info from a key in "+docWKey.getBaseURI()+" to morph. description in "+doc.getBaseURI());
					}else{
						//create a new file holding the description
						File file = createFileFor(taxonName, description, docWKey);
						//newFiles.add(file);
						log(LogLevel.DEBUG, "create new file "+file.getAbsolutePath()+" to hold info from a key in "+docWKey.getBaseURI());
					}
				}
			}
		}
		//validate new files
		/*XMLVolumeValidator volumeValidator = new XMLVolumeValidator(new File(validateSchemaFile));
		Boolean valid = volumeValidator.validate(newFiles);
		if(!valid){
			log(LogLevel.ERROR, "One or more newly created output xml files are not valid");
			throw new Exception("Created output is not valid against the schema: " + validateSchemaFile);
		}*/
	}


	
	/**
	 * one determination may occur multiple times in one key, need to deduplicate description statements before add description to the related doc
	 * doc may already contain some statements from descriptions, stop those from being added again.
	 * 
	 * @param doc
	 * @param description
	 */

	private void addDescription(Document doc, Element description) {
		if(doc==null || description == null) return;
		//<statement id="from_key_d90_s0">
		//remove statements with the same id as those to be added
		List<Element> statements = description.getChildren("statement");
		Iterator<Element> it = statements.iterator();
		while(it.hasNext()){
			Element state = it.next();
			String id = state.getAttributeValue("id");
			XPathExpression<Element> statemts  = fac.compile("//description/statement[@id='"+id+"']", Filters.element()); 
			if(!statemts.evaluate(doc).isEmpty()){
				it.remove();
			}
		}
		
		//add de-duplicated description
		doc.getRootElement().addContent(description);
	}


	/**
	 * create a new file using the file naming convention
	 * and save it in the output directory 
	 * @param taxonName
	 * @param description
	 */
	private File createFileFor(String taxonName, Element description, Document template) {
		File out = new File(this.runOutDirectory, "from_key_"+taxonName.replaceAll(" ", "_")+".xml");
		//make a template doc for the new description by removing un-needed elements
		Document doc = template.clone();
		Element root = doc.getRootElement();
		//ArrayList<Element> removes = new ArrayList<Element> ();
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while(it.hasNext()){
			Element child = it.next();
			String name = child.getName();
			if(!name.matches("meta|taxon_identification")){
				it.remove();
			}else if(name.compareTo("meta")==0){
				List<Element> otherInfoOnMeta = child.getChildren("other_info_on_meta");
				Iterator<Element> it2 = otherInfoOnMeta.iterator();
				while(it2.hasNext()){
					it2.next();
					it2.remove();
				}
			}else if(name.compareTo("taxon_identification")==0 && child.getAttributeValue("status").compareToIgnoreCase("ACCEPTED")!=0){
				it.remove();
			}
		}

		/*for(Element remove: removes){
			if(remove.getName().compareTo("meta")==0){
				List<Element> otherInfoOnMeta = remove.getChildren("other_info_on_meta");
				root.re(otherInfoOnMeta);				
			}
			root.removeContent(remove);
		}*/

		//update taxon_identification
		Element ti = root.getChild("taxon_identification");
		ti.removeContent();
		String[] names = taxonName.split("\\s+");
		for(String name: names){
			String [] rankname = name.split("_"); //some name may not contain the rank
			Element tn = new Element("taxon_name");
			tn.setAttribute("rank", rankname.length>1? rankname[0]:"unranked");
			tn.setAttribute("authority", "unknown");
			tn.setAttribute("date", "unknown");
			tn.setText(rankname.length>1? rankname[1]:rankname[0]);
			ti.addContent(tn);
		}

		//insert description
		root.addContent(description);

		//write out
		writeFile(doc, out);
		return out;
	}


	/**
	 * write doc to file
	 * @param doc
	 * @param file
	 * @throws  
	 */
	private void writeFile(Document doc, File file) {
		out.setFormat(Format.getPrettyFormat());	
		try {
			out.output(doc, new FileWriter(file));
		} catch (IOException e) {
			log(LogLevel.ERROR, "Failed to write xml file: "+file.getAbsolutePath());
			e.printStackTrace();
		}
	}


	/**
	 * 
	 * @param taxonName: may not have complete info about lower ranks, e.g., order_astrophorida ancorinidaes
	 * @param taxa2doc order_astrophorida family_ancorinidae=>
	 * @return
	 */
	private Document locateTaxonDocument(String taxonName,
			Hashtable<String, Document> taxa2doc) {
		//exact match
		Document doc = taxa2doc.get(taxonName);
		if(doc!=null) return doc;

		//try to find the best match
		Enumeration<String> en = taxa2doc.keys();
		int max = 0;
		String matchName = null;
		while(en.hasMoreElements()){
			int score = 0;
			String name = en.nextElement();
			List<String> parts1 = Arrays.asList(name.split("\\s+"));
			String[] parts2 = taxonName.split("\\s+");
			if(isMatch(parts1.get(parts1.size()-1), parts2[parts2.length-1])){//the name of the lowest rank must match
				for(String part1: parts1){
					for(String part2: parts2){
						if(isMatch(part1, part2)) score++;
					}
				}
			}
			if(score>max){
				max = score;
				matchName = name;
			}
		}	
		return matchName!=null? taxa2doc.get(matchName): null;
	}


	/**
	 * @param part1 contains rank info e.g., family_ancorinidae
	 * @param part2 rank info optional e.g., ancorinidaes
	 * @return
	 */
	private boolean isMatch(String part1, String part2) {
		return part1.compareTo(part2)==0 || part1.endsWith("_"+part2);
	}


	/**
	 * traces from determination up the key to collect all parsed descriptions applicable to the determination
	 * save the statements in the description
	 * need also update the ids for all the statements, structures, relations and references to them. 
	 * 
	 * handles also the situation where the determination points to another key such as "Key A"
	 * 
	 * @param description
	 * @param determination
	 * @param key
	 */
	private void wrapCharacter(Element description, Element determination,
			Element key, String keyFileName) {
			
			//tracing up
			String stmtid = determination.getParentElement().getChild("statement_id").getTextTrim();
			Element desc = determination.getParentElement().getChild("description").clone();
			Collection<Element> statements = prepStatmentsFrom(desc, keyFileName);
			if(statements!=null) description.addContent(statements);
	
			Element nextId = getNextIdStatement(stmtid, key);
			while(nextId!=null){
				desc = ((Element)nextId).getParentElement().getChild("description").clone();
				statements = prepStatmentsFrom(desc, keyFileName);
				if(statements!=null) description.addContent(statements);
				stmtid = ((Element)nextId).getParentElement().getChild("statement_id").getTextTrim();
				nextId = getNextIdStatement(stmtid, key);
			}
	
			//if key has a key_head, search for a key with the key_head as a determination, and continue to trace up the description chain
			if(key.getChild("key_head")!=null){
				Element upperDet = findUpperDetermination(key.getChild("key_head"), key);
				if(upperDet!=null){
					Element upperKey = upperDet.getParentElement().getParentElement();
					if(upperKey!=null) wrapCharacter(description, upperDet, upperKey, keyFileName);
				}
			}
	}
	
	/**
	 * search for the determination element that matches the key_head
	 * @param child
	 * @param key
	 * @return
	 */
	private Element findUpperDetermination(Element keyHead, Element key) {
		if(keyHead==null || key==null) return null;
		String kh = keyHead.getTextNormalize().toLowerCase();
		List<Element> dets = allDetPath.evaluate(key);
		for(Element det: dets){
			String detString = det.getTextNormalize().toLowerCase();
			detString = detString.replaceFirst("[(\\[,].*$", "").trim(); //187a. mutisieae (in part; acourtia
			if(kh.matches(".*?\\b"+detString+"\\b.*"))
				return det;
		}
		return null;
	}


	/**
	 * if determination matches a key_head in any of the keys in the document
	 * det: <determination>Group 2, v. 19, p. 14</determination>
	 * key_head: <key_head>Key to Genera of Group 2</key_head>
	 * @param determination
	 * @param key
	 * @return
	 */
	private boolean matchesKeyHead(Element determination, Element key) {
		if(determination==null || key==null) return false;
		String det = determination.getTextNormalize().toLowerCase();
		det = det.replaceFirst("[(\\[,].*$", "").trim(); //group 2 ; group 1 (cichorieae )
		List<Element> keyHeads = keyHead.evaluate(key);
		for(Element head: keyHeads){
			String kh = head.getTextNormalize().toLowerCase();
			if(kh.matches(".*?\\b"+det+"\\b.*")){
				return true;
			}
		}
		return false;
	}

	

	/**
	 * collect all parsed statements
	 * update the ids for all the statements, structures, relations and references to them
	 * @param desc
	 * @return
	 */


	private Collection<Element> prepStatmentsFrom(Element description, String keyFileName) {
		ArrayList<Element> states = new ArrayList<Element>(description.getChildren("statement"));		
		ArrayList<Element> statements = new ArrayList<Element>();
		for(int i = 0; i < states.size(); i++){
			//for(Element statement: description.getChildren("statement")){ //concurrent modification issue
			Element statement = states.get(i);
			statement.detach();
			updateIds(statement, keyFileName);
			statements.add(statement);
		}
		return statements;
	}


	/**
	 * append "from_key_" to all ids and references to ids in the statement
	 * @param statement
	 */

	private void updateIds(Element statement, String keyFileName) {

		if(statement.getAttribute("id")!=null)
			statement.setAttribute("id", "from_key_"+keyFileName+"_"+statement.getAttributeValue("id"));

		for(Element elem: statement.getChildren()){
			if(elem.getAttribute("id")!=null){
				elem.setAttribute("id", "from_key_"+keyFileName+"_"+elem.getAttributeValue("id"));
			}
			if(elem.getName().compareTo("relation")==0){
				if(elem.getAttribute("from")!=null){
					elem.setAttribute("from", "from_key_"+keyFileName+"_"+elem.getAttributeValue("from"));
				}
				if(elem.getAttribute("to")!=null){
					elem.setAttribute("to", "from_key_"+keyFileName+"_"+elem.getAttributeValue("to"));
				}
			}
		}

	}


	private Element getNextIdStatement(String stmtid, Element key) {
		for(Object nextId: nextIdPath.evaluate(key)){
			// only one element has the correct id
			if(((Element)nextId).getTextTrim().compareTo(stmtid)==0){
				return (Element) nextId;
			}
		}
		return null;
	}

	


	/**
	 * Example determination text
	 * 5 .  Hesperoyucca , p . 439  //document is on a family, so assuming this is genus.
	 * 9b . Eriophyllum lanatum var . grandiflorum ( in part ) //document is about Eriophyllum lanatum
	 * 
	 * @param determination
	 * @param document
	 * @return
	 */
	private String getName(Element determination, Document document) {
		//start from the first capital letter to non-period punctuation marks
		//look into document for names of higher ranks, prefix them to the determination name (watch for any overlap btw the two)
		//watch for abbreviated genus name.
		String det = determination.getTextTrim().replaceAll("(?<=\\S)(?=\\p{Punct})", " ").replaceAll("(?<=\\p{Punct})(?=\\S)", " ").replaceAll("\\s+", " ").trim(); //add spaces around punctuation mark
		Pattern start = Pattern.compile(".*? ([A-Z].*)");
		Matcher m = start.matcher(det);
		if(m.matches()){
			det = m.group(1);
		}

		String name = "";
		//String lastRank = "";
		//collect name/rank info from document
		Hashtable<String, String> nameRank = new Hashtable<String, String>();
		Element ti = document.getRootElement().getChild("taxon_identification");
		for(Element tn: ti.getChildren("taxon_name")){
			nameRank.put(tn.getTextTrim(), tn.getAttributeValue("rank"));
			name += tn.getAttributeValue("rank")+"_"+tn.getTextTrim().toLowerCase()+" ";
			//lastRank = tn.getAttributeValue("rank");
		}

		//now taking name related tokens only
		String [] tokens = det.toLowerCase().split("\\s+");
		for(String token: tokens){
			if(token.matches("var") && !name.contains(" variety_")){
				name += "variety_";
			}else if(token.matches("subsp") && !name.contains(" subspecies_")){
				name += "subspecies_";
			}else if(token.matches("subg") && !name.contains(" subgenus_")){
				name += "subgenus_";
			}else if(token.matches("subfam") && !name.contains(" subfamily_")){
				name += "subfamily_";
			}else if(token.matches("sect") && !name.contains(" section_")){
				name += "section_";
			}else if(token.matches(".")){
				if(!name.endsWith("_")) break;
			}else if(token.matches("\\W") || token.matches("\\d.*")){
				break;
			}else{
				if(nameRank.get(token)==null){
					if(name.endsWith("_")) name += token+" ";
					else{
						//String thisRank = getRankLowerThan(lastRank); //return the major rank that is lower than lastRank
						//name += thisRank+"_"+token+" ";
						name += token+" ";
					}
				}
			}
		}
		return name.trim();
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
