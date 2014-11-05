/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.run;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.jdom2.Document;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jdom2.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;

/**
 * @author Hong Cui
 * after Run is complete, perform post-markup tasks, 
 * such as organizing descriptive characters in keys to morphological description elements
 * or creating new xml files holding character info for some taxa.
 *
 */
public class PostRun {
	String runOutDirectory;

	/**
	 * 
	 */
	@Inject
	public PostRun(@Named("Run_OutDirectory")String runOutDirectory) {
			this.runOutDirectory = runOutDirectory;
			absorbKeys(); 
	}

	
	private void absorbKeys() {
		//walk through files in runOutDirectory to find keys
		//create hashtables to link taxa to file names
		Hashtable<String, Document> taxa2doc = new Hashtable<String, Document>();
		Hashtable<Document, File> doc2file = new Hashtable<Document, File>();
		ArrayList<Document> containsKey = new ArrayList<Document>();
		
		XPathFactory fac = XPathFactory.instance();
		XPathExpression<Element> keyPath = fac.compile("bio:treatment/key", Filters.element());
		XPathExpression<Element> detPath = fac.compile("key//determination", Filters.element());
		XPathExpression<Element> nextIdPath = fac.compile("key//next_statement_id", Filters.element());
		XPathExpression<Element> namePath = fac.compile("bio:treatment/taxon_identification[@status='ACCEPTED']", Filters.element());
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
					name += tn.getAttributeValue("rank")+"_"+tn.getTextTrim()+" ";
				}
				taxa2doc.put(name, document);
				doc2file.put(document, file);
						
				if(!keyPath.evaluate(root).isEmpty()){
					containsKey.add(document);
				}
			}catch(Exception e){
				log(LogLevel.ERROR, "Failed to read xml file: "+file);
			}
		
		}
		
		//absorb characters in keys into taxon descriptions (existing or new)
		//leaving the original key unchanged in the source document
		for(Document docWKey: containsKey){
			for(Object key: keyPath.evaluate(docWKey.getRootElement())){
				for(Object determination: detPath.evaluate(key)){
					String taxonName = getName((Element) determination, docWKey);
					Element description = new Element("description");
					description.setAttribute("type", "key");
					wrapCharacter(description, nextIdPath, (Element)determination, (Element)key);
					
					Document doc = locateTaxonDocument(taxonName, taxa2doc);
					if(doc!=null){
						//insert an new description element of type key to the doc for the taxon
						doc.addContent(description);
						updateFile(doc, doc2file.get(doc));
					}else{
						//create a new file holding the description
						createFileFor(taxonName, description);
					}
				}
			}
		}		
	}

	
	
	

	private void createFileFor(String taxonName, Element description) {
		// TODO Auto-generated method stub
	}


	private void updateFile(Document doc, File file) {
		// TODO Auto-generated method stub
		
	}


	private Document locateTaxonDocument(String taxonName,
			Hashtable<String, Document> taxa2doc) {
		Document doc = taxa2doc.get(taxonName);
		if(doc!=null) return doc;
		
		Enumeration<String> en = taxa2doc.keys();
		while(en.hasMoreElements()){
			String name = en.nextElement();
			if(name.endsWith(taxonName)) return taxa2doc.get(name);
		}		
		return null;
	}


	/**
	 * trace from determination up the key to collect all statements applicable to the determination
	 * save the statements in description
	 * need also update the ids for the statements.
	 * 
	 * @param description
	 * @param determination
	 * @param key
	 */
	private void wrapCharacter(Element description, XPathExpression nextIdPath, Element determination,
			Element key) {
		String stmtid = determination.getParentElement().getChild("statement_id").getTextTrim();
		Element stmt = determination.getParentElement().getChild("statement").clone();
		description.addContent(stmt);
		
		
		for(Object nextId: nextIdPath.evaluate(key)){
			// only one element has the correct id
			if(((Element)nextId).getTextTrim().compareTo(stmtid)==0){
				stmt = ((Element)nextId).getParentElement().getChild("statement").clone();
				description.addContent(stmt);
				stmtid = ((Element)nextId).getParentElement().getChild("statement_id").getTextTrim();
				break;
			}
		}
		
		
		
		
		
	}


	/**
	 * Example determination text
	 * 5 .  Hesperoyucca , p . 439
	 * 9b . Eriophyllum lanatum var . grandiflorum ( in part )
	 * 
	 * @param determination
	 * @param document
	 * @return
	 */
	private String getName(Element determination, Document document) {
		
		return null;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
