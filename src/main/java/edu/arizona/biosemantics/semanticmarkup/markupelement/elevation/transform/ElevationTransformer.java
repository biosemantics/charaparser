/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.transform;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessorProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Elevation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.NumericalPhraseParser;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Treatment;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.transform.IElevationTransformer;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * @author Hong Cui
 *
 */
public class ElevationTransformer implements IElevationTransformer {
	
	NumericalPhraseParser npp = null;
	
	@Inject
	public ElevationTransformer(@Named("Units")String units){
		npp = new NumericalPhraseParser(units);
	}
	/* (non-Javadoc)
	 * @see edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.transform.IElevationTransformer#transform(java.util.List)
	 */
	@Override
	public void transform(List<ElevationsFile> elevationsFiles) {
		for(ElevationsFile elevationsFile : elevationsFiles) {
			int i = 0;
			int organId = 0;
			for(Treatment treatment : elevationsFile.getTreatments()) {
				for(Elevation elevation : treatment.getElevations()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("elevation_" + i++);
					statement.setText(elevation.getText());
					BiologicalEntity be = new BiologicalEntity();
					be.setName("whole_organism");
					be.setId("elev_o"+organId++);
					be.setType("structure");
					be.setNameOriginal("");
					be.addCharacters(parse(elevation.getText()));
					statement.addBiologicalEntity(be);

					statements.add(statement);				
					elevation.setStatements(statements);
				}
			}
		}

	}
	
	/**
	 * above 2500 m in Calif., above 2000 m in Oreg.;
	 * above 2300 m in Cascades and 3000 m in Rockies;
	 * 10–700 m (East), 1500–2000 m (Arizona);
	 * 0 (Florida)–600 (Arkansas, Texas) m; => 0-600 m
	 * 0–200 m (to 2000 m, tropics);
	 * mainly below 100 m;
	 * not known
	 * 600–1000[–1800] m
	 * 10--100 m
	 */
	@Override
	public LinkedHashSet<Character> parse(String text) {
		//text = text.replaceAll("\\-+", "-").replaceAll("\\bto", replacement);
		
		//format text, hide [,;] in parentheses
		LinkedHashSet<Character> values = new LinkedHashSet<Character>();
		text = format(text); 
		text = andToComma(text);
		//collect value
		String[] elevation = text.split("[;,]");
		for(int i = 0; i<elevation.length; i++){
			String elev = elevation[i].trim();
			elev = normalize(elev);
			List<Character> elevs = npp.parseNumericals(elev, "elevation");
			if(elevs.isEmpty()){ //unknown, not known, varies
				 Character c = new Character();
				 c.setName("elevation");
				 c.setValue(elev);
				values.add(c);
			}else{
				values.addAll(elevs);
			}
		}
		return values;
	}
	
	/**
	 * above and below, and remove extra info
	 * 
	 * above 2500 m in Calif., =>2500+m
	 * 0 (Florida)–600 (Arkansas, Texas) m; => 0-600 m
	 * mainly below 100 m; =>0-100m
	 * 250 m and/or above =>250+m
	 * 250 m and/or below =>0-250m
	 * @param elev
	 * @return
	 */
	private String normalize(String elev) {
		if(elev.matches(".*\\babove\\b.*")){
			Pattern aboveN = Pattern.compile("(.*)\\babove ([\\d]+)(.*)");
			Pattern Nabove = Pattern.compile("(.*[\\d]+)( m?) ?(?:and|or)? ?above\\b(.*?)");
			
			Matcher m = aboveN.matcher(elev);
			if(m.matches()){
				elev = m.group(1)+m.group(2)+"+"+m.group(3);
			}else{
				m = Nabove.matcher(elev);
				if(m.matches()){
					elev = m.group(1)+"+"+m.group(2)+m.group(3);
				}
			}
		}else if (elev.matches(".*\\bbelow\\b.*")){
			Pattern belowN = Pattern.compile("(.*)\\bbelow ([\\d]+)(.*)");
			Pattern Nbelow = Pattern.compile("(.*)(\\b[\\d]+)( m?) ?(?:and|or)? ?below\\b(.*?)");
		
			Matcher m = belowN.matcher(elev);
			if(m.matches()){
				elev = m.group(1)+"0-"+m.group(2)+m.group(3);
			}else{
				m = Nbelow.matcher(elev);
				if(m.matches()){
					elev = m.group(1)+"0-"+m.group(2)+m.group(3);
				}
			}
		}
		
		//0 (Florida)–600 (Arkansas, Texas) m; => 0-600 m
		
		
		return null;
	}
	/**
	 * above 2300 m in Cascades and 3000 m in Rockies; =>
	 * above 2300 m in Cascades , 3000 m in Rockies;
	 * @param text
	 * @return
	 */
	private String andToComma(String text) {
		String[] split = text.split("\\band\\b");
		String result = split[0];
		for(int i = 1; i < split.length; i++){
			if(split[i].matches(".*\\d+.*")){
				result = result +", "+ split[i];
			}else{
				result = result +"and"+ split[i];
			}
		}
		return result;
	}
	
	/*private LinkedHashSet<Character> allValues(String elevation) {
		LinkedHashSet<Character> values = new LinkedHashSet<Character>();
		  Pattern p = Pattern.compile("(.*?)\\(([^)]*?@[^)]*?)\\)(.*)");
		  Matcher m = p.matcher(elevation);
		  if(m.matches()){
			   String com = m.group(1);
			   String partstr = m.group(2);
			   String rest = m.group(3);
			   
			   String[] parts = partstr.split("\\s*@\\s*");
			   
			   for(int i = 0; i<parts.length; i++){
				   Character c = new Character();
					c.setName("elevation");
					c.setValue(com+"("+parts[i]+")"+rest);
					values.add(c);
				   }
			  }
		  return values;
		 }*/

	/*
	 * format text, hide [,;] in parentheses
	 */
	 public static String format(String text) {
		  String formated = "";
		  Pattern p = Pattern.compile("(.*?)(\\([^)]*,[^)]*\\))(.*)"); 
		  Matcher m = p.matcher(text);
		  while(m.matches()){
			   formated += m.group(1);
			   String t = m.group(2);
			   text = m.group(3);
			   t = t.replaceAll(",", "@");
			   formated +=t;
			   m = p.matcher(text);
			  }
		  formated +=text;
		  return formated;
		 }
	
	
}
