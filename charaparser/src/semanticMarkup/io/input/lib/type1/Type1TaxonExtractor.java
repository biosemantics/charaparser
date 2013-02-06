package semanticMarkup.io.input.lib.type1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semanticMarkup.core.Treatment;


public class Type1TaxonExtractor {

	private HashMap<Treatment, String> taxonNumbers = 
			new HashMap<Treatment, String>();
	private HashMap<Treatment, String> taxonNames = 
			new HashMap<Treatment, String>();
	
	private Pattern fixBrokenNamesPattern1 = 
			Pattern.compile("(.*?(?:^| ))([A-Z] )(\\w.*)");
	//T HYRSOSTACHYS
	private Pattern fixBrokenNamesPattern2 = 
			Pattern.compile("(.*?(?:^| ))([A-Z]+ )([A-Z][A-Z].*)");
	private Pattern fixBrokenNamesPattern3 = 
			Pattern.compile("(.*?(?:^| ))(\\d+ )(\\d+.*)");//HOng 08/04/09 "3 9 . xxxx"
	//va r. 
	//make sure any of these are not broken: subfam|var|subgen|subg|subsp|ser|tribe		
	private Pattern fixBrokenNamesPattern4 = 
			Pattern.compile("(.*?)\\b(s ?u ?b ?f ?a ?m|v ?a ?r|s ?u ?b ?g ?e ?n|s ?u ?b ?g|s ?u ?b ?s ?p|s ?e ?r|t ?r ?i ?b ?e)\\b(.*)");
	
	private Pattern chunkPlaceOfPubPattern1 = 
			Pattern.compile(".*?[A-Z].*?\\d+.*"); //(Rydberg) Munz, Man. S. Calif. Bot., 598. 1935
	private Pattern chunkPlaceOfPubPattern2 = 
			Pattern.compile("(.*?[A-Z].*),\\s+([^\\d]+)\\s+\\d.*"); //(Rydberg) Munz, Man. S. Calif. Bot., 598. 1935
	private Pattern chunkPlaceOfPubPattern3 = 
			Pattern.compile(",[^\\d]+&"); //may be authority list: a, b & c
	private Pattern chunkPlaceOfPubPattern4 = Pattern.compile("\\([^()]*?,");//may be in ()
	
	private Pattern extractNamePattern1 = Pattern.compile(
			"\\b(?:subfam|var|subgen|subg|subsp|ser|tribe|subsect)[\\.\\s]+([-a-z]+)", Pattern.CASE_INSENSITIVE);
	private Pattern extractNamePattern2 = 
			Pattern.compile("\\bsect[\\.\\s]+([-a-zA-Z]+)"); 
		//Journal names may contain e.g. Sect. IV Hong 08/04/09
	private String namelist = "|";
	
	private Pattern familyName = Pattern.compile("^([a-z]*?ceae)\\b.*", Pattern.CASE_INSENSITIVE);
	private Pattern genusName = Pattern.compile("^([A-Z][A-Z].*?)\\b.*"); //NOTHOCALAIS with two dots on top of last I
	
	
	public void extract(
			LinkedHashMap<Treatment, LinkedHashMap<String, ArrayList<DocumentElement>>> 
			documentElements) {
		for(Entry<Treatment, 
				LinkedHashMap<String, ArrayList<DocumentElement>>> entry 
				: documentElements.entrySet()) {
			Treatment treatment = entry.getKey();
			LinkedHashMap<String, ArrayList<DocumentElement>> 
				treatmentsDocumentElements = entry.getValue();
			for(Entry<String, ArrayList<DocumentElement>> elementEntry
					: treatmentsDocumentElements.entrySet()) {
				taxonNumbers.put(treatment, 
						extractNumber(elementEntry.getValue().get(0)));
				break; //only the first one is needed
			}
			taxonNames.put(treatment, 
					extractName(treatmentsDocumentElements.values()));
		}
	}
	
	private String extractNumber(DocumentElement documentElement) {
		String text = documentElement.getText();
		text = text.replaceAll("\\s[a-zA-Z].*", ""); //1. Amstersdfds 12.c Ames
		if(text.matches("^\\d.*")) 
			return text; //add one by one in sequence
		else 
			return "0"; //add one by one in sequence
		
	}
	
	//extractName(Element pe, String filename
	private String extractName(Collection<ArrayList<DocumentElement>> documentElementsCollection) {
		//concat <text> elements into one string
		StringBuffer buffer=new StringBuffer();
		
		for(ArrayList<DocumentElement> documentElements : documentElementsCollection) {
			for(DocumentElement documentElement : documentElements) {
				buffer.append(documentElement.getText()).append(" ");
			}
		}
		//List<Element> additionalList = XPath.selectNodes(pe, "./text[@case='"+Registry.TribeGenusNameCase+"']");
		
		String text = buffer.toString().replaceAll("\\s+", " ").trim();
		text = text.replaceFirst("^.*?(?=[A-Z])", "").trim();
		text = text.replaceAll(" ", " ").replaceAll("\\s+", " ").trim(); 
		//there are some whitespaces that are not really a space, don't know what they are. 
		//fix broken names: T HYRSOSTACHYS;  va r. subhispida
		text = fixBrokenNames(text);
		text = chunkPlaceOfPub(text); //after this, text should only hold name information
		if(text.length() == 0) 
			return ""; //TODO: shouldn't happen, except for 295.xml
			
		//tribe: didn't need smallCaps info.
		Matcher m1 = extractNamePattern1.matcher(text);
		Matcher m2 = extractNamePattern2.matcher(text);
		boolean check = false;
		if (m1.find() || m2.find()) {// sub- names
			String aname = text;
			String laname = aname.toLowerCase();
			if(namelist.indexOf("|"+laname+"|")>=0)
				return aname;
			else {
				namelist += laname+"|";
				return aname;
			}
		} else {// family, genus, species names
			String namestring = text;
			namestring = namestring.replaceAll("^\\d.*?\\s+", "");
			namestring = namestring.replaceAll("^\\.\\s+", ""); // if there is a . Hong 08/04/09 e.g "4 . XXXX"
			Matcher m3 = familyName.matcher(namestring);
			if(m3.find()){
				//String aname = m.group(1);
				String aname = text;
				String laname = aname.toLowerCase();
				if(namelist.indexOf("|"+laname+"|")>=0){
					//listener.info("", filename, "Repeated taxon name:"+aname+" [family rank]");
					//log(LogLevel.DEBUG, "::::::::::duplicate "+aname+" [family rank]"); //should not occur
				} else {
					namelist += laname+"|";
					return aname; //family
				}
			} else {
				m3 = genusName.matcher(namestring);
				if(m3.find()){
					//String aname = m.group(1);
					String aname = text;
					String laname = aname.toLowerCase();
					if(namelist.indexOf("|"+laname+"|")>=0){
						//listener.info("", filename, "Repeated taxon name:"+aname+" [genus rank]");
						//log(LogLevel.DEBUG, "::::::::::duplicate "+aname+" [genus rank]"); //should not occur
					} else {
						namelist += laname+"|";
						return aname; //genus
					}
				} else{
		 			String aname = text;
		    		String laname = aname.toLowerCase();
					if(namelist.indexOf("|"+laname+"|")>=0){
						//listener.info("", filename, "Repeated taxon name:"+aname+" [species rank]");
					} else {
						namelist += laname+"|";
						return aname;
					}    	
			    }
			}
		}
		return null;
	}

	public String getNumber(Treatment treatment) {
		return taxonNumbers.get(treatment);
	}
	
	public String getName(Treatment treatment) {
		return taxonNames.get(treatment);
	}
	
	public String fixBrokenNames(String text){
		Matcher m = fixBrokenNamesPattern1.matcher(text);
		if(m.matches()){
			text = m.group(1)+m.group(2).trim()+m.group(3);
		}
		m = fixBrokenNamesPattern2.matcher(text);
		if(m.matches()){
			text = m.group(1)+m.group(2).trim()+m.group(3);
		}
		m = fixBrokenNamesPattern3.matcher(text);
		if(m.matches()){
			text = m.group(1)+m.group(2).trim()+m.group(3);
		}
		m = fixBrokenNamesPattern4.matcher(text);
		if(m.matches()){
			text = m.group(1)+m.group(2).replaceAll("\\s+", "")+m.group(3);
		}
		return text;
	}
	
	/**
	 * 
	 * @param text: 14c. Mirabilis linearis (Pursh) Heimerl var. subhispida (Heimerl) Spellenberg, Novon 12: 270. 2002
	 * @return: 14c. Mirabilis linearis (Pursh) Heimerl var. subhispida (Heimerl) Spellenberg
	 */
	private String chunkPlaceOfPub(String text) {
		String journal = null;
		Matcher m = null;
		//problems 489.xml 6a. Dysphania R. Brown sect. Adenois (Moquin-Tandon) Mosyakin & Clemants, Ukrayins’k. Bot. Zhurn., n. s. 59: 382. 2002
		m = chunkPlaceOfPubPattern1.matcher(text);
		while(m.matches()) {//4a. Echinocereus pectinatus (Scheidweiler) Engelmann var. wenigeri L. D. Benson, Cact. Succ. J. (Los Angeles) 40: 124, <=================>fig. 3. 1968 · Weniger’shedgehog,ashy white ra inbow cactus, langtry rainbow cactus
			m = chunkPlaceOfPubPattern2.matcher(text);
			if(m.matches()){
				text = m.group(1).trim();
				journal = m.group(2).replaceFirst(",\\s*$", "").trim();				
			}else{
				break;
			}
		}
		//post process to deal with some special cases: 
		//Boerhavia line arifolia A. Gray, Amer. J. Sci. Arts
		int in = text.indexOf(","); //suspecious
		if(in > 0){
			m = chunkPlaceOfPubPattern3.matcher(text);
			if(!m.find()){
				m = chunkPlaceOfPubPattern4.matcher(text);
				if(!m.find()){// deal with this case
					text = text.substring(0, in).trim();
				}
			}
		}
		
		//in Smith ed. 
		in = text.indexOf(" in ");
		if(in > 0){
			text = text.substring(0, in).trim();
			//String rest = textcp.substring(in).trim();
		}
		
		//PHYTOLACCACEAE R. Brown • Pokeweed Family
		in = text.indexOf("·");
		if(in < 0){
			in = text.indexOf("•");
		}
		if(in > 0){
			text = text.substring(0, in).trim();
			//String rest = textcp.substring(in).trim();
		}

		return text;
		//text now holds only the name, which should not contain a number
	}
}
