package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Phenology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Treatment;

public class PhenologyTransformer implements IPhenologyTransformer {


	static Hashtable<String, String> m2smapping = new Hashtable<String, String>();
	static Hashtable<String, String> s2mmapping = new Hashtable<String, String>();
	static String monthring="jan-feb-mar-apr-may-jun-jul-aug-sep-oct-nov-dec-jan-feb-mar-apr-may-jun-jul-aug-sep-oct-nov-dec";
	static String value="(.*?)((jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|spring|summer|fall|winter|year|round|late|early|mid|middle| |-)+)(.*)";             
	static String seasonring = "spring-summer-fall-winter-spring-summer-fall-winter";
	static String seasons = "(spring|summer|fall|winter)";
	static String months ="(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)";
	static{
		m2smapping.put("jan", "winter");
		m2smapping.put("feb", "winter");
		m2smapping.put("mar", "spring");
		m2smapping.put("apr", "spring");
		m2smapping.put("may", "spring");
		m2smapping.put("jun", "summer");
		m2smapping.put("jul", "summer");
		m2smapping.put("aug", "summer");
		m2smapping.put("sep", "fall");
		m2smapping.put("oct", "fall");
		m2smapping.put("nov", "fall");
		m2smapping.put("dec", "winter");

		s2mmapping.put("spring", "mar@apr@may");
		s2mmapping.put("summer", "jun@jul@aug");
		s2mmapping.put("fall", "sep@oct@nov");
		s2mmapping.put("winter", "dec@jan@feb");
	}


	@Override
	public void transform(List<PhenologiesFile> phenologiesFiles) {
		for(PhenologiesFile phenologiesFile : phenologiesFiles) {
			int i = 0;
			for(Treatment treatment : phenologiesFile.getTreatments()) {
				for(Phenology phenology : treatment.getPhenologies()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("phenology_" + i++);
					statement.setText(phenology.getText());

					BiologicalEntity be = new BiologicalEntity();
					be.setName("whole_organism");
					be.setType("structure");
					be.setNameOriginal("");
					be.addCharacters(parse(phenology.getText()));
					statement.addBiologicalEntity(be);

					statements.add(statement);
					phenology.setStatements(statements);
				}
			}
		}
	}

	@Override
	public LinkedHashSet<Character> parse(String text) {
		text = text.toLowerCase().replaceFirst("(flowering|fruiting)\\s+", "").replaceAll("_", "-");
		//System.out.println("original: "+text);
		//clean up the text
		Pattern p = Pattern.compile(value);
		Matcher m = p.matcher(text);
		String clean = "";
		while(m.matches()){
			clean += m.group(2)+"@";
			text = m.group(4);
			m=p.matcher(text);                      
		}
		//System.out.println("cleaned: "+clean);
		//fetch the values
		String[] ranges = clean.split("\\s*@\\s*");
		ArrayList<String> valueStrings = new ArrayList<String>();
		for(int i = 0; i<ranges.length; i++){
			String range = ranges[i].trim();
			if(range.indexOf("-")>=0){
				range = range.replaceFirst("^-+", "").replaceFirst("-+$", "");
				String times[] = range.split("-");
				if(times.length>1){
					valueStrings.addAll(allValuesInRange(times));
				}else{
					valueStrings.add(times[0]);
				}
			}else{
				valueStrings.add(range);
			}
		}

		LinkedHashSet<Character>  values = new LinkedHashSet<Character>();
		for(String vs: valueStrings){
			Character c = new Character();
			c.setName("phenology");
			c.setValue(vs);
			values.add(c);
		}

		values.addAll(formValues(valueStrings));
		return values;
	}

	private  LinkedHashSet<Character>  formValues(ArrayList<String> valueString) {
		LinkedHashSet<Character>  values = new LinkedHashSet<Character>();
		String includedseasons = getSeasons(valueString);
		String includedmonths = getMonths(valueString);      

		Iterator<String> it = valueString.iterator();

		while(it.hasNext()){
			String month = (String) it.next();
			if(month.compareTo("")!=0){
				//System.out.println("add: "+month);
				if(month.indexOf("year")>=0){
					values.addAll(addAllMonthsSeasons());
					includedseasons = PhenologyTransformer.seasons.replaceAll("\\W", "@");
					includedmonths = PhenologyTransformer.months.replaceAll("\\W", "@");
				}

				//add corresponding seasons for the month (if this is a month values)
				String season = PhenologyTransformer.m2smapping.get(month.toLowerCase());
				if(season !=null && includedseasons.indexOf(season)<0){
					Character c = new Character();
					c.setName("phenology");
					c.setValue(season);
					values.add(c);
					includedseasons +=season+"@";
				}                                       

				//add corresponding months for the season (if this is a season value)
				String monthlist = PhenologyTransformer.s2mmapping.get(month.toLowerCase());
				if( monthlist!=null){ 
					String[] months = monthlist.split("\\s*@\\s*");
					for(int i=0; i<months.length; i++){
						if(months[i].compareTo("")!=0 && includedmonths.indexOf(months[i])<0){
							Character c = new Character();
							c.setName("phenology");
							c.setValue(months[i]);
							values.add(c);
							includedmonths +=months[i]+"@";                                 
						}                       
					}
				}
			}
		}
		return values;
	}

	private LinkedHashSet<Character>  addAllMonthsSeasons() {
		LinkedHashSet<Character>  values = new LinkedHashSet<Character> ();
		Set<String> seasons = PhenologyTransformer.s2mmapping.keySet();
		Set<String> months = PhenologyTransformer.m2smapping.keySet();
		Iterator<String> s = seasons.iterator();
		while(s.hasNext()){
			Character c = new Character();
			c.setName("phenology");
			c.setValue((String)s.next());
			values.add(c);
		}

		Iterator<String> m = months.iterator();
		while(m.hasNext()){
			Character c = new Character();
			c.setName("phenology");
			c.setValue((String)m.next());
			values.add(c);
		}
		return values;
	}
	/**
	 * 
	 * @param times
	 * @return
	 */

	private ArrayList<String> allValuesInRange(String[] times) {
		ArrayList<String> results = new ArrayList<String>();
		String s = times[0];
		String e = times[times.length-1];
		String[] ss = s.split("\\s+");
		String[] es = e.split("\\s+");
		if((ss[ss.length-1].matches(PhenologyTransformer.seasons) && es[es.length-1].matches(PhenologyTransformer.months))||
				(ss[ss.length-1].matches(PhenologyTransformer.months) && es[es.length-1].matches(PhenologyTransformer.seasons))        ){
			//return original values
			dump2ArrayList(times, results);
		}else{
			Pattern p = Pattern.compile(".*?\\b("+ss[ss.length-1]+"\\b.*?\\b"+es[es.length-1]+")\\b.*");
			Matcher mm = p.matcher(PhenologyTransformer.monthring);
			Matcher sm = p.matcher(PhenologyTransformer.seasonring);
			if(mm.matches()){
				//collect all months
				dump2ArrayList(mm.group(1).split("-"), results);
			}else if(sm.matches()){
				dump2ArrayList(sm.group(1).split("-"), results);
			}                       
		}
		return results;
	}

	private void dump2ArrayList(String[] array, ArrayList<String> arrayList) {
		for(int i = 0; i <array.length; i++){
			arrayList.add(array[i]);
		}
	}

	/*
	 * return @-connected values
	 */
	private String getSeasons(ArrayList<String> values) {
		String seasons = "";
		Iterator<String> it = values.iterator();
		while(it.hasNext()){
			String v = ((String)it.next()).trim();
			String[] t = v.split("\\s+");
			if(t[t.length-1].matches(PhenologyTransformer.seasons)){
				seasons +=t[t.length-1]+"@";
			}
		}
		return seasons;
	}

	/*
	 * return @-connected values
	 */
	private String getMonths(ArrayList<String> values) {
		String months = "";
		Iterator<String> it = values.iterator();
		while(it.hasNext()){
			String v = ((String)it.next()).trim();
			String[] t = v.split("\\s+");
			if(t[t.length-1].matches(PhenologyTransformer.months)){
				months +=t[t.length-1]+"@";
			}
		}
		return months;
	}
}
