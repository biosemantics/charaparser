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

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessorProvider;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.INormalizer;
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
	String units = null;
	String defaultLow = "0.00";
	Pattern advModPattern = null;
	Pattern locPattern = null;
	String deliminator = "#";//single character that may be used in regular expression.
	String stopwords = null;

	@Inject
	public ElevationTransformer(@Named("Units")String units, @Named("AdvModifiers") String advModifiers, 
			@Named("LyAdverbpattern") String lyAdvPattern, @Named("StopWordString") String stopwords){
		this.units = units;
		this.stopwords = stopwords;
		this.npp = new NumericalPhraseParser(units);
		this.advModPattern = Pattern.compile(advModifiers+"|"+lyAdvPattern+"|ca.");
		this.locPattern = Pattern.compile("\\b([A-Z][a-zA-Z.]{2,}|\\w*tropical|\\w*tropics|\\w*equatorial|\\w*temperate|\\w*polar|west|north|east|south|westward|northward|eastward|southward)"); //in subtropical, tropical, Arizona	
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
					if(elevation.getText()!=null && elevation.getText().trim().length()>0){
						BiologicalEntity be = new BiologicalEntity();
						be.setName("whole_organism");
						be.setId("elev_o"+organId++);
						be.setType("structure");
						be.setNameOriginal("");
						be.addCharacters(parse(elevation.getText()));
						statement.addBiologicalEntity(be);
					}
					statements.add(statement);				
					elevation.setStatements(statements);

				}
			}
		}

	}

	@Override
	public LinkedHashSet<Character> parse(String text) {

		log(LogLevel.DEBUG, "To parse elevation text: "+text);
		System.out.println("To parse elevation text: "+text);

		text = text.replaceAll("–", "-").replaceAll("-+", "-");
		String nText = normalize(hideNegatives(text)); //in this order

		if(nText.compareToIgnoreCase(text)!=0) {
			log(LogLevel.DEBUG, "Text normalized to: "+nText);
			System.out.println("Text normalized to: "+nText);

		}


		//collect and resolve values to populate eri
		ArrayList<ElevationRangeInfo> eris = new ArrayList<ElevationRangeInfo>();
		populateERI(eris, nText);

		//output characters
		return outputCharacters(eris, text);

	}

	/**
	 * 0 (100–300 inland) m;
	 * => 0 m (100–300 m inland) m;
	 * 
	 * @param (Greenland, 0–)1000–2400(–4000, Colorado, Utah) m;
	 * @return (Greenland, 0–1000 m) 1000–2400 m (2400–4000, Colorado, Utah);
	 */
	private String normalize(String text) {

		/**
		 * (Greenland, 0–)1000–2400(–4000, Colorado, Utah) m;
		 *	=> (Greenland, 0–1000 m) 1000–2400 m (2400–4000, Colorado, Utah);
		 */
		if(text.matches(".*?\\(.*?[A-Za-z].*?\\d-\\)\\d.*") || text.matches(".*?\\d\\(-\\d.*?[A-Za-z].*?\\).*")){
			String unit = "";
			Pattern units = Pattern.compile("("+this.units+")");
			Matcher u = units.matcher(text);		
			if(u.find()) unit = text.substring(u.start(), u.end());		

			Pattern part = Pattern.compile("(\\(.*?\\w.*?\\d-)\\)(\\d+)");//(Greenland, 0–)1000
			Matcher m = part.matcher(text);
			if(m.find()){
				text = text.replace(text.substring(0, m.end()), 
						text.substring(0, m.start())+text.substring(m.start(1), m.end(1))+text.substring(m.start(2), m.end(2))+ " " +unit+") "+ text.substring(m.start(2), m.end(2)));
			}

			part = Pattern.compile("(\\d+)\\((-\\d+)(.*?\\w.*?\\))");//2400(–4000, Colorado, Utah) =>2400 m (2400–4000, Colorado, Utah)
			m = part.matcher(text);
			if(m.find()){
				text = text.replace(text.substring(m.start(), m.end()), text.substring(m.start(1), m.end(1))+ " "+unit+
						" ("+text.substring(m.start(1), m.end(1)) +  text.substring(m.start(2), m.end(2)) + " "+unit+" " + text.substring(m.start(3), m.end(3)));
			}

			text.replaceAll("(\\b["+unit+" ])+", unit); // m  m => m
		}


		/**
		 * 0 (100–300 inland) m;
		 * => 0 m (100–300 m inland) m;
		 * 
		 * 0 (Florida)-600 (Arkansas, Texas) m;
		 * => 0 (Florida)-600 (Arkansas, Texas) m;
		 */
		if(text.matches(".*?\\d (?!"+this.units+").*") && text.matches(".*\\) "+this.units+".*")){ //text contains unit by separated from the value by ()
			//find unit
			String unit = "";
			Pattern units = Pattern.compile("("+this.units+")");
			Matcher u = units.matcher(text);		
			if(u.find()) unit = text.substring(u.start(), u.end());	

			//insert unit after numbers
			String norm = "";
			int start = 0;
			Pattern p = Pattern.compile("\\d (?!"+this.units+")");
			u = p.matcher(text);
			while(u.find()){
				norm +=text.substring(start, u.end())+unit+" ";
				start = u.end();
			}
			norm+=text.substring(start);
			text = norm.replaceAll("\\s+", " ").replaceAll("(?<=\\s)[,.;:]", "");
		}
		return text;
	}
	/**
	 * low elevations => high elevations
	 * 500 m => high elevations
	 * 500-2000m
	 * @param eris
	 * @param text
	 * @return
	 */
	private LinkedHashSet<Character> outputCharacters(ArrayList<ElevationRangeInfo> eris, String text) {

		LinkedHashSet<Character> values = new LinkedHashSet<Character>();
		try{

			if(eris.isEmpty()){ //unknown, not known, varies
				Character c = new Character();
				c.setName("elevation");
				c.setValue(text); //output the input
				values.add(c);
			}


			for(ElevationRangeInfo eri: eris){
				List<Character> elevs = new LinkedList<Character>();
				if(eri.getCandidateValue()!=null && (eri.getCandidateValue().matches(".*\\d-.*") || eri.getCandidateValue().matches(".*-\\d.*") || eri.getCandidateValue().contains("+"))){
					elevs = npp.parseNumericals(eri.getCandidateValue(), "elevation");
					unhiddenNegatives(elevs);
				}else{
					Character c = new Character();
					c.setName("elevation");
					if(eri.getCandidateValue()!=null){
						String v = eri.getCandidateValue();
						String value = v.replaceFirst(units+"$", "");
						c.setValue(value.trim());
						if(v.compareTo(value)!=0) c.setUnit(v.replaceFirst(value, "").trim());
					}
					if(eri.getCandidates4High()!=null){
						String h = eri.getCandidates4High();
						String value = h.replaceFirst(units+"$", "");
						c.setTo(value.trim());
						if(h.compareTo(value)!=0) c.setToUnit(h.replaceFirst(value, "").trim());
						c.setCharType("range_value");
					}
					if(eri.getCandidates4Low()!=null){
						String l = eri.getCandidates4Low();
						String value = l.replaceFirst(units+"$", "");
						c.setFrom(value.trim());
						if(l.compareTo(value)!=0) c.setFromUnit(l.replaceFirst(value, "").trim());
						c.setCharType("range_value");
					}
					elevs.add(c);
				}


				if(elevs.isEmpty()){ //unknown, not known, varies
					Character c = new Character();
					c.setName("elevation");
					c.setValue(text); //output the input
					values.add(c);
				}else{
					for(Character elev: elevs){ //set modifiers for each character parsed out
						if(eri.getModifier4High()!=null && eri.getModifier4High().length()>0){ // to_modifier
							elev.setToModifier(format(eri.getModifier4High()));
						}
						if(eri.getModifier4Low()!=null && eri.getModifier4Low().length()>0){ //from_modifier
							elev.setfromModifier(format(eri.getModifier4Low()));
						}
						if(eri.getModifier()!=null && eri.getModifier().length()>0){
							elev.setModifier(format(eri.getModifier()));
						}
						//if(elev.getFrom()!=null && elev.getFrom().compareTo(this.defaultLow)==0) elev.setFrom(null);
					}
					values.addAll(elevs); //TODO add modifiers
				}
			}

			log(LogLevel.DEBUG, "Parsed elevation characters: ");

			for(Character c: values){
				log(LogLevel.DEBUG, c.toString());
				System.out.println(c.toString());
			}

		}catch(Throwable t){
			log(LogLevel.ERROR, "Parse error:", t);
			t.printStackTrace();
		}
		return values;


	}


	private String format(String modifier) {
		modifier = modifier.replaceAll("^\\W+|\\W+$", "");
		return modifier;
	}
	/**
	 * 015 => -15
	 * @param elevs
	 */
	private void unhiddenNegatives(List<Character> elevs) {
		for(Character elev: elevs){
			if(elev.getValue()!=null) 
				if(elev.getValue().matches("0[1-9].*")) elev.setValue(elev.getValue().replaceFirst("0", "-"));
			if(elev.getTo()!=null) 
				if(elev.getTo().matches("0[1-9].*")) elev.setTo(elev.getTo().replaceFirst("0", "-"));
			if(elev.getFrom()!=null) 
				if(elev.getFrom().matches("0[1-9].*")) elev.setFrom(elev.getFrom().replaceFirst("0", "-"));
		}		
	}

	/**
	 * replace negative signs with 0, without changing the length of input string
	 * @param negative
	 * @return
	 */

	private String hideNegatives(String text) {
		Pattern p = Pattern.compile("-\\d");
		Matcher m = p.matcher(text);
		String norm = "";
		int start = 0;
		while(m.find()){ //m.find continues operate on the updated negative because replacing - with 0 does not change the length of the string
			String leading = text.substring(start, m.start());

			//remove all (abc) : 0 (Florida)-600 (Arkansas, Texas) m;
			String last = leading;
			while(leading.trim().endsWith(")") || leading.trim().endsWith("]")){
				leading = leading.replaceFirst("\\s*[\\(\\[][^\\d]*[\\)\\]]\\s*$", "");
				if(leading.compareTo(last)==0) break;
				last = leading;
			}

			if(last.trim().length()==0 || 
					(last.trim().matches(".*[\\(\\)\\[\\]]+$") && ! last.trim().matches(".*\\d[\\(\\)\\[\\]]+$")) ||
					(!last.trim().matches(".*[\\(\\)\\[\\]]+$") && last.trim().matches(".*[^\\d]$"))) 
				norm += text.substring(start, m.end()).replace("-", "0"); // -70; abc -90
			else
				norm += text.substring(start, m.end());

			start = m.end();

			/*if((negative.substring(0, m.start()).matches(".*[\\(\\)\\[\\]]+$") && (! negative.substring(0, m.start()).matches(".*\\d[\\(\\)\\[\\]]+$"))) ||
					(! negative.substring(0, m.start()).matches(".*[\\(\\)\\[\\]]+$") && negative.substring(0, m.start()).matches(".*[^\\d]$")) ||
					negative.substring(0, m.start()).length()==0){ //not directly following another number
				negative = negative.substring(0, m.start())+negative.substring(m.start(), m.end()).replace("-", "0")+negative.substring(m.end());
			}else{
				negative = negative.substring(0, m.start())+negative.substring(m.start(), m.end())+negative.substring(m.end());
			}*/
		}
		norm += text.substring(start);
		return norm;
	}
	/**
	 * text holds info to populate a number of ElevationRangeInfo
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

	private void populateERI(ArrayList<ElevationRangeInfo> eris, String text) {
		text = text.replaceAll("\\s+"," ").trim();
		String previous = text;

		while(text.matches(".*?\\d.*")){
			//range pattern: low and high form a range
			if(text.matches(".*\\d-.*") || text.matches(".*-\\d.*") || text.contains("between") ||  text.contains("Between")){
				//(-15–)0–900(–1100) m;
				Pattern numericalRange = Pattern.compile("([\\d\\(\\)\\[\\]\\+\\-]*\\d[\\d\\(\\)\\[\\]\\+\\-]*)-([\\d\\(\\)\\[\\]\\+\\-]*\\d[\\d\\(\\)\\[\\]\\+\\-]*) ("+units+")\\b");
				Matcher m = numericalRange.matcher(text);

				if(m.find()){
					String m1 = "";
					String m2 = "";
					ElevationRangeInfo eri = new ElevationRangeInfo();
					m1 = modifierVerbatim(text.substring(0, m.start()), false);
					eri.appendModifier(m1);
					m2 = modifierVerbatim(text.substring(m.end()), true);
					eri.appendModifier(m2);
					String value = text.substring(m.start(), m.end());
					if(value.matches("^[\\(\\[].*") && !value.matches(".*[\\]\\)].*")) value=value.replaceAll("^[\\(\\[]+", "");
					if(value.matches("^[\\)\\]].*") && !value.matches(".*[\\[\\(].*")) value=value.replaceAll("^[\\)\\]]+", "");
					if(value.matches(".*[\\)\\]]$") && !value.matches(".*[\\[\\(].*")) value=value.replaceAll("[\\)\\]]+$", "");
					if(value.matches(".*[\\(\\[]$") && !value.matches(".*[\\]\\)].*")) value=value.replaceAll("[\\(\\[]+$", "");
					eri.setCandidateValue(value);
					//add eri
					assembleERIS(eris, eri); 
					//update text
					text = text.substring(0, m.start()).replaceFirst(escape(m1)+"\\s*$", "")+"#"+text.substring(m.end()).replaceFirst("^\\s*"+escape(m2), "");
				}	

				//between pattern: 0 (Florida)-600 (Arkansas, Texas) m;
				Pattern range = Pattern.compile("\\b(?:[Bb]etween )?(\\d+) ?(?:"+units+")? ?(\\([^#]*?\\))? ?(?:-| and )([^#]*?)?\\b(\\d+) ?("+units+")?\\b"); //.... 0 (Florida)–600 (Arkansas, Texas) m; ...
				//Pattern range = Pattern.compile("\\b(?:[Bb]etween )?(\\d+) ?(?:"+units+")? ?(\\(.*?\\))? ?(?:-| and )(.*?)?\\b(\\d+) ?(?:"+units+")? ?(\\(.*?\\))? ?("+units+")\\b"); //.... 0 (Florida)–600 (Arkansas, Texas) m; ...
				m = range.matcher(text);
				if(m.find()){
					String m1 = "";
					String m2 = "";
					ElevationRangeInfo eri = new ElevationRangeInfo();
					eri.setCandidates4Low(m.group(1)+" "+m.group(5));
					eri.setCandidates4High(m.group(4)+" "+m.group(5));
					if(m.group(2)!=null && m.group(2).trim().length()>0 && !m.group(2).matches(".*?([Ee]levations|[Aa]lpine|[Tr]eeline|[Tt]imberline|\\d).*?")) //separate modifiers
						eri.appendModifier4Low(m.group(2));
					if(m.group(3)!=null && m.group(3).trim().length()>0 && !m.group(3).matches(".*?([Ee]levations|[Aa]lpine|[Tr]eeline|[Tt]imberline|\\d).*?"))
						eri.appendModifier4High(m.group(3));
					//if(m.group(5)!=null && m.group(5).trim().length()>0 && !m.group(5).matches(".*?([Ee]levations|[Aa]lpine|[Tr]eeline|[Tt]imberline|\\d).*?"))
					//	eri.appendModifier4High(m.group(5));

					m1 = modifierVerbatim(text.substring(0, m.start()), false);
					m2 = modifierVerbatim(text.substring(m.end()), true);
					if(m1.trim().length()>0 ||(eri.getModifier4Low()!=null && eri.getModifier4Low().length()>0)){//separate modifiers
						eri.appendModifier4Low(m1);
						eri.appendModifier4High(m2);
					}else if(m2.trim().length()>0){//common modifier
						eri.appendModifier(m2);
					}

					//add eri
					assembleERIS(eris, eri);
					//update text
					text = text.substring(0, m.start()).replaceFirst(escape(m1)+"\\s*$", "")+"#"+text.substring(m.end()).replaceFirst("^\\s*"+escape(m2), "");
				}
			}			

			//above patterns
			if(text.matches(".*\\b([Aa]bove|[Hh]igher than|[Gg]reater than|[Oo]ver|higher|greater)\\b.*")){
				Pattern aboveN = Pattern.compile("\\b(?:[Aa]bove|[Hh]igher than|[Gg]reater than|[Oo]ver) ([\\d]+)( (?:"+units+")\\b)?"); //above 100 m
				Pattern Nabove = Pattern.compile("([\\d]+)( (?:"+units+"))?( (?:and|or|and/or))? (?:above|higher|greater|[Oo]ver)\\b"); //100 m and above

				Matcher m = aboveN.matcher(text);
				if(m.find()){
					String m1 = "";
					String m2 = "";
					ElevationRangeInfo eri = new ElevationRangeInfo();
					m1 = modifierVerbatim(text.substring(0, m.start()), false);
					eri.appendModifier4Low(m1);
					m2 = modifierVerbatim(text.substring(m.end()), true);
					eri.appendModifier4Low(m2);
					eri.setCandidates4Low(m.group(1)+" "+m.group(2).trim());
					//add eri
					assembleERIS(eris, eri); 
					//update text
					text = text.substring(0, m.start()).replaceFirst(escape(m1)+"\\s*$", "")+"#"+text.substring(m.end()).replaceFirst("^\\s*"+escape(m2), "");

				}

				m = Nabove.matcher(text);

				if(m.find()){
					String m1 = "";
					String m2 = "";
					ElevationRangeInfo eri = new ElevationRangeInfo();
					m1 = modifierVerbatim(text.substring(0, m.start()), false);
					eri.appendModifier4Low(m1);
					m2 = modifierVerbatim(text.substring(m.end()), true);
					eri.appendModifier4Low(m2);
					eri.setCandidates4Low(m.group(1)+" "+m.group(2).trim());

					//add eri
					assembleERIS(eris, eri);
					//update text
					text = text.substring(0, m.start()).replaceFirst(escape(m1)+"\\s*$", "")+"#"+text.substring(m.end()).replaceFirst("^\\s*"+escape(m2), "");
				}
			}

			//below patterns
			if(text.matches(".*\\b([Bb]elow|[Ll]ess than|[Ll]ower than|[Tt]o|lower|less)\\b.*")){
				Pattern belowN = Pattern.compile("\\b(?:[Bb]elow|[Ll]ess than|[Ll]ower than|[Tt]o) ([\\d]+)( (?:"+units+")\\b)?"); //below/to 100 m
				Pattern Nbelow = Pattern.compile("([\\d]+)( (?:"+units+"))?( (?:and|or|and/or))? (?:below|lower|less)\\b"); //100 m and below

				Matcher m = belowN.matcher(text);
				if(m.find()){
					String m1 = ""+"";
					String m2 = "";
					ElevationRangeInfo eri = new ElevationRangeInfo();
					m1 = modifierVerbatim(text.substring(0, m.start()), false);
					eri.appendModifier4High(m1);
					m2 = modifierVerbatim(text.substring(m.end()), true);
					eri.appendModifier4High(m2);
					eri.setCandidates4High(m.group(1)+" "+m.group(2).trim());

					//add eri
					assembleERIS(eris, eri);
					//update text
					text = text.substring(0, m.start()).replaceFirst(escape(m1)+"\\s*$", "")+"#"+text.substring(m.end()).replaceFirst("^\\s*"+escape(m2), "");

				}

				m = Nbelow.matcher(text);
				if(m.find()){
					String m1 = "";
					String m2 = "";
					ElevationRangeInfo eri = new ElevationRangeInfo();
					m1 = modifierVerbatim(text.substring(0, m.start()), false);
					eri.appendModifier4High(m1);
					m2 = modifierVerbatim(text.substring(m.end()), true);
					eri.appendModifier4High(m2);
					eri.setCandidates4High(m.group(1)+" "+m.group(2).trim());

					//add eri
					assembleERIS(eris, eri);
					//update text
					text = text.substring(0, m.start()).replaceFirst(escape(m1)+"\\s*$", "")+"#"+text.substring(m.end()).replaceFirst("^\\s*"+escape(m2), "");
				}
			}

			if(text.compareTo(previous)==0) break; 	//prevent infinite loop
			previous = text;
		}

		//range patterns no longer match, try single numerical value
		previous = text;
		while(text.matches(".*\\d.*")){
			Pattern single = Pattern.compile("(\\d+) "+units+"\\b");
			Matcher m = single.matcher(text); 
			if(m.find()){
				String m1 = "";
				String m2 = "";
				ElevationRangeInfo eri = new ElevationRangeInfo();
				m1 = modifierVerbatim(text.substring(0, m.start()), false);
				eri.appendModifier(m1);
				m2 = modifierVerbatim(text.substring(m.end()), true);
				eri.appendModifier(m2);
				eri.setCandidateValue(text.substring(m.start(), m.end()));

				//add eri
				assembleERIS(eris, eri);
				//update text
				text = text.substring(0, m.start()).replaceFirst(escape(m1)+"\\s*$", "")+"#"+text.substring(m.end()).replaceFirst("^\\s*"+escape(m2), "");
			}
			if(text.compareTo(previous)==0) break; 	//prevent infinite loop
			previous = text;
		}


		previous = text;
		//low (in northern regions) to high elevations of over 3300 m;
		while(text.matches(".*([Ee]levation|[Tt]imberline|[Tt]reeline).*")){
			Pattern elev = Pattern.compile("([Ll]ow|[Mm]oderate|[Mm]edium) (\\([^#]*?\\))? ?to (moderate|medium|high|alpine|treeline|timberline)\\b ?(elevations)?");
			Matcher m = elev.matcher(text); 
			if(m.find()){
				String m1 = "";
				String m2 = "";
				ElevationRangeInfo eri = new ElevationRangeInfo();
				eri.setCandidates4Low(m.group(1)+" elevations");
				eri.setCandidates4High(m.group(3)+ " elevations");
				if(m.group(2)!=null && m.group(2).trim().length()>0  && !m.group(2).matches(".*?([Ee]levations|[Aa]lpine|[Tr]eeline|[Tt]imberline|\\d).*?")) //separate modifiers
					eri.appendModifier4Low(m.group(2));
				//if(m.group(5)!=null && m.group(5).trim().length()>0  && !m.group(5).matches(".*?([Ee]levations|[Aa]lpine|[Tr]eeline|[Tt]imberline|\\d).*?"))
				//	eri.appendModifier4High(m.group(5));

				m1 = modifierVerbatim(text.substring(0, m.start()), false);
				m2 = modifierVerbatim(text.substring(m.end()), true);
				if(m1.trim().length()>0 ||( eri.getModifier4Low()!=null && eri.getModifier4Low().length()>0)){//separate modifiers
					eri.appendModifier4Low(m1);
					eri.appendModifier4High(m2);
				}else if(m2.trim().length()>0){//common modifier
					eri.appendModifier(m2);
				}

				//add eri
				assembleERIS(eris, eri);
				//update text
				text = text.substring(0, m.start()).replaceFirst(escape(m1)+"\\s*$", "")+"#"+text.substring(m.end()).replaceFirst("^\\s*"+escape(m2), "");
			}

			//to alphine elevations
			elev = Pattern.compile("\\b[Tt]o (moderate|medium|high|alpine|treeline|timberline)\\b ?(elevations)?");
			m = elev.matcher(text); 
			if(m.find()){
				String m1 = "";
				String m2 = "";
				ElevationRangeInfo eri = new ElevationRangeInfo();
				m1 = modifierVerbatim(text.substring(0, m.start()), false);
				eri.appendModifier4High(m1);
				m2 = modifierVerbatim(text.substring(m.end()), true);
				eri.appendModifier4High(m2);
				eri.setCandidates4High(m.group(1)+" elevations");

				//add eri
				assembleERIS(eris, eri);
				//update text
				text = text.substring(0, m.start()).replaceFirst(escape(m1)+"\\s*$", "")+"#"+text.substring(m.end()).replaceFirst("^\\s*"+escape(m2), "");
			}


			//low elevations, in alphine elevations
			elev = Pattern.compile("([Ll]ow|[Mm]oderate|[Mm]edium|[Hh]igh|[Aa]lpine|[Tt]reeline|[Tt]imberline)\\b ?(elevations)?");
			m = elev.matcher(text); 
			if(m.find()){
				String m1 = "";
				String m2 = "";
				ElevationRangeInfo eri = new ElevationRangeInfo();
				m1 = modifierVerbatim(text.substring(0, m.start()), false);
				eri.appendModifier4High(m1);
				m2 = modifierVerbatim(text.substring(m.end()), true);
				eri.appendModifier4High(m2);
				eri.setCandidateValue(m.group(1)+ " elevations");

				//add eri
				assembleERIS(eris, eri);
				//update text
				text = text.substring(0, m.start()).replaceFirst(escape(m1)+"\\s*$", "")+"#"+text.substring(m.end()).replaceFirst("^\\s*"+escape(m2), "");
			}

			//Elevations: low to medium?

			if(text.compareTo(previous)==0) break; 	//prevent infinite loop
			previous = text;
		}

	}

	/**
	 * 
	 * @param m1
	 * @return
	 */
	private String escape(String m1) {
		return m1.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
	}
	/**
	 * 
	 * text holds info to populate a number of ElevationRangeInfo
	 * above 2500 m in Calif., above 2000 m in Oreg.;
	 * above 2300 m in Cascades and 3000 m in Rockies;
	 * 10–700 m (East), 1500–2000 m (Arizona);
	 * 0 (Florida)–600 (Arkansas, Texas) m; => 0-600 m
	 * 0–200 m (to 2000 m, tropics);
	 * 600–1000[–1800] m
	 * mainly below 100 m;
	 * not known
	 * 
	 * 10--100 m
	 * 
	 * rules:
	 * 0. add eri to eris in sequence
	 * 1. if eri has complete range, just add it to the end of eris
	 * 2. if eri has incomplete range (only low or high value), 
	 *    	if eris is not empty
	 *      	check the last two eris in sequence and see if one existing eri could make the range complete (supply the high or the low value).# 
	 *      		if yes, complete the range by updating the existing eri.
	 *      		if no, add the new eri to the end of eris
	 *      if eris is empty
	 *          add the new eri to the end of eris
	 * 3. fill the missing low value with 0.00, leave the missing high value open    
	 * 
	 * 
	 * @param eris
	 * @param eri
	 */
	private void assembleERIS(ArrayList<ElevationRangeInfo> eris,
			ElevationRangeInfo eri) {
		if((eri.getCandidates4Low()!=null && eri.getCandidates4High()!=null)||eri.getCandidateValue()!=null){ //complete range
			eris.add(eri);
		}else{ //incomplete range
			if(eris.isEmpty()) eris.add(eri);
			else{
				if(!completeRange(eris, eri))	eris.add(eri);	
			}			
		}
	}

	/**
	 * # eri1 and eri2 can complete a range when one is low and other is high value, and 
	 * eri1 and eri2 don't have different location modifiers. 
	 * @param eris
	 * @param eri
	 * @return
	 */

	private boolean completeRange(ArrayList<ElevationRangeInfo> eris,
			ElevationRangeInfo incompleteEri2) {
		int size = eris.size();
		for(int i = 1; i <=2; i++){
			if(size-i>=0){
				ElevationRangeInfo eri1 = eris.get(size-1);
				//put low and high together
				if((eri1.getCandidates4High()==null && eri1.getCandidates4Low()!=null &&
						incompleteEri2.getCandidates4High()!=null && incompleteEri2.getCandidates4Low()==null) || //eri1 = low, eri2 = high
						(eri1.getCandidates4High()!=null && eri1.getCandidates4Low()==null &&
						incompleteEri2.getCandidates4High()==null && incompleteEri2.getCandidates4Low()!=null) //eri1 = high, eri2 = low
						){
					//are they have different location modifiers?
					if(!hasDifferentHLLocation(eri1, incompleteEri2)){
						//complete range: use incompleteEri2 update eri1
						if(incompleteEri2.getCandidates4High()!=null){
							eri1.setCandidates4High(incompleteEri2.getCandidates4High());
							eri1.appendModifier4High(incompleteEri2.getModifier4High());						
						}else if(incompleteEri2.getCandidates4Low()!=null){
							eri1.setCandidates4Low(incompleteEri2.getCandidates4Low());
							eri1.appendModifier4Low(incompleteEri2.getModifier4Low());
						}
						return true;
					}
				}

				//put value and high together: 500 m to timberline;
				if((eri1.getCandidates4High()==null && eri1.getCandidateValue()!=null &&  eri1.getCandidates4Low()==null && //eri value = 500m
						incompleteEri2.getCandidates4High()!=null && incompleteEri2.getCandidateValue()==null && incompleteEri2.getCandidates4Low()==null)  //eri2 high: timberline
						){
					//are they have different location modifiers?
					if(!hasDifferentLocation(eri1.getModifier(), incompleteEri2.getModifier4High())){
						//complete range: use incompleteEri2 update eri1

						//move value to low
						eri1.setCandidates4Low(eri1.getCandidateValue());
						eri1.appendModifier4Low(eri1.getModifier());
						eri1.setCandidateValue(null);
						eri1.resetModifier();
						//set high
						eri1.setCandidates4High(incompleteEri2.getCandidates4High());
						eri1.appendModifier4High(incompleteEri2.getModifier4High());						

						return true;
					}
				}
			}
		}
		return false;
	}


	/**
	 * 	
	 * check location modifier only for low/high 
	 * @param eri1
	 * @param incompleteEri2
	 * @return
	 */
	private boolean hasDifferentHLLocation(ElevationRangeInfo incompleteEri1,
			ElevationRangeInfo incompleteEri2) {

		String mod1 = "";
		String mod2 = "";

		if(incompleteEri1.getCandidates4High()!=null){
			mod1 =  incompleteEri1.getModifier4High();
			mod2 =  incompleteEri2.getModifier4Low();
		}else if(incompleteEri1.getCandidates4Low()!=null){
			mod1 =  incompleteEri1.getModifier4Low();
			mod2 =  incompleteEri2.getModifier4High();
		}

		if(mod1.length()==0 && mod2.length()==0) return false;

		return hasDifferentLocation(mod1, mod2);
	}


	private boolean hasDifferentLocation(String mod1, String mod2){
		String[] m1s = mod1.split(this.deliminator);
		String[] m2s = mod2.split(this.deliminator);
		for(String m1: m1s){
			if(! m1.matches(this.advModPattern.pattern())){ //not adv, but location modifier
				for(String m2: m2s){
					if(! m2.matches(this.advModPattern.pattern())){
						if(m1.toLowerCase().replaceAll("\\W", "").compareTo(m2.toLowerCase().replaceAll("\\W", "")) ==0 ){
							return false;
						}
					}
				}
			}
		}
		return true;
	}


	/**
	 * captures two types of modifiers: usually and in location
	 * in the exact form as in the original text, including punctuation marks etc.
	 * @param text
	 * @param lookAtBeginning true: look for the modifier at the beginning of the text. false: look for the modifer at the ending of the text
	 * @return "" if not a modifier, otherwise, return modifier
	 */
	private String modifierVerbatim(String text, boolean lookAtBeginning) {
		if(text.trim().length()==0 || text.matches("\\W+")) return "";

		String modifier = "";
		text = text.trim();
		if(lookAtBeginning){ // 100 m in Arizona, .... 
			if(text.startsWith("(")){ // (....)?
				String potential = text.substring(0, text.indexOf(")")>0? text.indexOf(")")+1: text.length());
				if(!potential.matches(".*\\d+.*"))
					return potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.startsWith("[")){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf("]")+1: text.length());
				if(!potential.matches(".*\\d+.*"))
					return potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.indexOf("(")<0 && text.indexOf(")")>0){ //...)
				String potential = text.substring(0, text.indexOf(")")>0? text.indexOf(")")+1: text.length());
				if(!potential.matches(".*\\d+.*"))
					return potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.indexOf("[")<0 && text.indexOf("]")>0){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf("]")+1: text.length());
				if(!potential.matches(".*\\d+.*"))
					return potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			Matcher m = this.locPattern.matcher(text);
			int stop = 0;
			while(m.find()){//aggressively searching for location, stop 
				if(!text.substring(stop, m.end()).matches(".*\\d.*") && !text.substring(stop, m.end()).matches(".*?([Ee]levations|[Aa]lpine|[Tr]eeline|[Tt]imberline).*?"))
					stop = m.end();
			}
			//extend the modifier to a good point, punct mark or a number
			modifier = text.substring(0, stop);
			if(modifier.length()>0){
				String[] rest = text.substring(stop).split("\\s");
				int i = 0;
				String space = "";
				for(String w: rest){
					if(i>0)  space=" ";
					i++;
					if(w.matches(".*?[#,;:.)]")){
						modifier = modifier +space+w;
						break;
					}else if (w.matches(".*?\\d+.*")){
						break;
					}else{
						modifier = modifier +space+w;
					}
				}
			}
			return modifier.replaceFirst("("+this.stopwords+"| )+$", "").trim(); //remove trailing "and" etc.
		}else{
			if(text.startsWith("(") && text.indexOf(")") < 0 ){ //if (), take the whole ()
				String potential = text.substring(0, text.indexOf(")")>0? text.indexOf(")")+1: text.length());
				if(!potential.matches(".*\\d+.*"))
					return potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.startsWith("[") && text.indexOf("]") < 0){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf(")")+1: text.length());
				if(!potential.matches(".*\\d+.*"))
					return potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}


			// ... In Arizona often 100 m
			//int start = text.length();
			/*Matcher m = this.advModPattern.matcher(text);
			String advModifier = "";
			while (m.find()){
				if(m.end() == text.trim().length()){
					advModifier = text.substring(m.start(), m.end());
					text = text.substring(0, m.start()).trim();
				}
			}*/	
			// ... In Arizona often 100 m
			String advModifier = "";
			int start = text.length();
			Matcher m = this.advModPattern.matcher(text);
			while (m.find()){
				if(!text.substring(m.start()).matches(".*\\d+.*")) //no \d between m.start and end of text
					start = m.start();
			}	
			advModifier = text.substring(start);

			//extend the modifier to a good point, punct mark or a number
			//modifier = text.substring(start);
			/*Matcher m = this.advModPattern.matcher(text);
			while (m.find() && m.end() == text.length()){
				modifier = modifier +this.deliminator+ text.substring(m.start(), m.end());
				text = text.substring(0, m.start()).trim();
				m = this.advModPattern.matcher(text);
			}*/


			//if(text.length()>0){
			start = text.length();
			m = this.locPattern.matcher(text);
			while (m.find()){
				if(!text.substring(m.start()).matches(".*?\\d.*?") && !text.substring(m.start()).matches(".*?([Ee]levations|[Aa]lpine|[Tr]eeline|[Tt]imberline).*?")) //no \d between m.start and end of text
					start = m.start();
			}	
			//extend the modifier to a good point, punct mark or a number
			modifier = text.substring(start);
			if(modifier.length()>0){
				String[] rest = text.substring(0, start).split("\\s");
				for(int i= rest.length-1; i>=0; i--){
					String w = rest[i];
					if(w.matches(".*?[#,;:.)(]") || w.matches(".*?\\d+.*")){
						break;
					}else{
						modifier = w+" "+ modifier;
					}
				}
			}
			
			if(advModifier.length()>0) modifier = modifier +" "+advModifier;
			//}
		}
		return modifier.replaceFirst("("+this.stopwords+"| )+$", "").trim();
	}
	/**
	 * above and below, and remove extra info
	 * 
	 * above 2500 m in Calif., =>2500+m
	 * mainly below 100 m; =>0-100m
	 * 250 m and/or above =>250+m
	 * 250 m and/or below =>0-250m
	 * 
	 * 0 (Florida)–600 (Arkansas, Texas) m; => 0-600 m
	 * @param elev
	 * @return
	 */
	/*private static String normalize(String elev) {
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

		//0 (Florida)–600 (Arkansas, Texas) m; => 0-600 
		//TODO
		return elev;
	}*/


	public static void main(String[] args){
		//-15 =>015
		String[] negatives = {		
				"-15-(-10);",
				"(-15)-((-10)),",
				"0-(-15)-((-10)),",
				"500-1000(-2000) m;",
				"(-15–)0–900(–1100)", 
				"600–1000[–1800] m;", 
				"0–ca. 1000 m;",
				"0(–50) m;",
				"(0-)1500-3000 m",
		}; 

		for(String negative: negatives){
			Pattern p = Pattern.compile("-\\d");
			Matcher m = p.matcher(negative);
			while(m.find()){
				if((negative.substring(0, m.start()).matches(".*[\\(\\)\\[\\]]+$") && (! negative.substring(0, m.start()).matches(".*\\d[\\(\\)\\[\\]]+$"))) ||
						(! negative.substring(0, m.start()).matches(".*[\\(\\)\\[\\]]+$") && negative.substring(0, m.start()).matches(".*[^\\d]$")) ||
						negative.substring(0, m.start()).length()==0){ //not directly following another number
					//if(negative.substring(0, m.start()).matches(".*?[^\\d][\\(\\)\\[\\]]*$")){ //not directly following another number
					negative = negative.substring(0, m.start())+negative.substring(m.start(), m.end()).replace("-", "0")+negative.substring(m.end());
				}else{
					negative = negative.substring(0, m.start())+negative.substring(m.start(), m.end())+negative.substring(m.end());
				}
			}
			System.out.println(negative);
		}

	}
	/**
	 * above 2300 m in Cascades and 3000 m in Rockies; =>
	 * above 2300 m in Cascades , 3000 m in Rockies;
	 * @param text
	 * @return
	 */
	/*private String andToComma(String text) {
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
	}*/

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
	/*public static String format(String text) {
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
		 }*/


	private class ElevationRangeInfo{
		String modifier4Low = ""; //modifier for low
		String modifier4High = ""; //modifier for high
		String modifier = ""; //apply to both low and high. If low/high modifier is not empty, modifier must be null, vice versa
		String candidates4Low = null;
		String candidates4High = null;
		String candidateValue = null; //single value elevation, not a range
		String deliminator = "#"; //single character that may be used in regular expression. must be the same as the value for the hosting class.

		protected String getCandidateValue() {
			return candidateValue;
		}
		protected void setCandidateValue(String candidateValue) {
			this.candidateValue = candidateValue;
		}

		protected String getModifier() {
			return modifier;
		}
		protected void appendModifier(String modifier) {
			if(modifier!=null && modifier.trim().length()>0)
				this.modifier +=  modifier +this.deliminator;
		} 
		protected void resetModifier() {
			this.modifier = "";
		} 
		protected String getModifier4Low() {
			return modifier4Low;
		}
		protected void appendModifier4Low(String modifier4Low) {
			if(modifier4Low!=null && modifier4Low.trim().length()>0)
				this.modifier4Low += modifier4Low+ deliminator;
		}

		protected String getModifier4High() {
			return modifier4High;
		}
		protected void appendModifier4High(String modifier4High) {
			if(modifier4High!=null && modifier4High.trim().length()>0)
				this.modifier4High +=  modifier4High + deliminator;
		}
		protected String getCandidates4Low() {
			return candidates4Low;
		}
		protected void setCandidates4Low(String candidates4Low) {
			this.candidates4Low = candidates4Low;
		}

		protected String getCandidates4High() {
			return candidates4High;
		}
		protected void setCandidates4High(String candidates4High) {
			this.candidates4High = candidates4High;
		}

		public String toString(){
			return "value ="+this.candidateValue+"\n" +
					"modifier ="+this.modifier+"\n"+
					"H value ="+this.candidates4High+"\n"+
					"H modifier ="+this.modifier4High+"\n"+
					"L value ="+this.candidates4Low+"\n"+
					"L modifier ="+this.modifier4Low+"\n";
		}
	}
}
