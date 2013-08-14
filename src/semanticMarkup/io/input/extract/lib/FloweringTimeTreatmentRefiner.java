package semanticMarkup.io.input.extract.lib;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.io.input.extract.ITreatmentRefiner;

/**
 * DistributionTreatmentRefiner creates new descriptive treatment elements from flowering time describing input
 * @author rodenhausen
 */
public class FloweringTimeTreatmentRefiner implements ITreatmentRefiner {

	private Hashtable<String, String> m2smapping = new Hashtable<String, String>();
	private Hashtable<String, String> s2mmapping = new Hashtable<String, String>();
	private String monthring="jan-feb-mar-apr-may-jun-jul-aug-sep-oct-nov-dec-jan-feb-mar-apr-may-jun-jul-aug-sep-oct-nov-dec";
	private String value="(.*?)((jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|spring|summer|fall|winter|year|round|late|early|mid|middle| |-)+)(.*)";		
	private Pattern valuePattern = Pattern.compile(value);
	private String seasonring = "spring-summer-fall-winter-spring-summer-fall-winter";
	private String seasons = "(spring|summer|fall|winter)";
	private String months ="(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)";
	
	/**
	 * 
	 */
	public FloweringTimeTreatmentRefiner(){
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
	public void refine(Treatment treatment, String clue, String nameForValues) {
		clue = clue.toLowerCase().replaceFirst("flowering\\s+", "").replaceAll("–", "-");
		//log(LogLevel.DEBUG, "original: "+text);
		//clean up the text
		Matcher m = valuePattern.matcher(clue);
		String clean = "";
		while(m.matches()){
			clean += m.group(2)+"@";
			clue = m.group(4);
			m=valuePattern.matcher(clue);			
		}
		//log(LogLevel.DEBUG, "cleaned: "+clean);
		//fetch the values
		String[] ranges = clean.split("\\s*@\\s*");
		ArrayList<String> values = new ArrayList<String>();
		for(int i = 0; i<ranges.length; i++){
			String range = ranges[i].trim();
			if(range.indexOf("-")>=0){
				range = range.replaceFirst("^-+", "").replaceFirst("-+$", "");
				String times[] = range.split("-");
				if(times.length>1)
					values.addAll(allValuesInRange(times));
				else
					values.add(times[0]);
			} else
				values.add(range);
		}
		
		formElements(treatment, values, nameForValues);
	}
	
	

	private void formElements(Treatment treatment, ArrayList<String> values, String nameForValues) {
		String includedseasons = getSeasons(values);
		String includedmonths = getMonths(values);	
			
		Iterator<String> it = values.iterator();
		
		while(it.hasNext()){
			String month = (String) it.next();
			if(month != "") {
				treatment.addTreatmentElement(new ValueTreatmentElement(nameForValues, month));
				if(month.indexOf("year") >= 0) {
					addAllMonthsSeasons(treatment, nameForValues);
					includedseasons = seasons.replaceAll("\\W", "@");
					includedmonths = months.replaceAll("\\W", "@");
				}
				
				//add corresponding seasons for the month (if this is a month values)
				String season = m2smapping.get(month.toLowerCase());
				if(season !=null && includedseasons.indexOf(season) < 0){
					treatment.addTreatmentElement(new ValueTreatmentElement(nameForValues, season));
					includedseasons +=season+"@";
				}					
				
				//add corresponding months for the season (if this is a season value)
				String monthlist = s2mmapping.get(month.toLowerCase());
				if(monthlist!=null) { 
					String[] months = monthlist.split("\\s*@\\s*");
					for(int i=0; i<months.length; i++)
						if(!months[i].equals("") && includedmonths.indexOf(months[i])<0) {
							treatment.addTreatmentElement(new ValueTreatmentElement(nameForValues, months[i]));
							includedmonths +=months[i]+"@";
						}
				}
			}
		}
	}

	private void addAllMonthsSeasons(Treatment treatment, String nameForValues) {
		Set<String> seasons = s2mmapping.keySet();
		Set<String> months = m2smapping.keySet();
		Iterator<String> s = seasons.iterator();
		while(s.hasNext()){
			String season = (String)s.next();
			treatment.addTreatmentElement(new ValueTreatmentElement(nameForValues, season));
		}
		
		Iterator<String> m = months.iterator();
		while(m.hasNext()){
			String season = (String)m.next();
			treatment.addTreatmentElement(new ValueTreatmentElement(nameForValues, season));
		}
	}
	
	private ArrayList<String> allValuesInRange(String[] times) {
		ArrayList<String> results = new ArrayList<String>();
		String s = times[0];
		String e = times[times.length-1];
		String[] ss = s.split("\\s+");
		String[] es = e.split("\\s+");
		if((ss[ss.length-1].matches(seasons) && es[es.length-1].matches(months))||
		   (ss[ss.length-1].matches(months) && es[es.length-1].matches(seasons))	){
			//return original values
			dump2ArrayList(times, results);
		}else{
			Pattern p = Pattern.compile(".*?\\b("+ss[ss.length-1]+"\\b.*?\\b"+es[es.length-1]+")\\b.*");
			Matcher mm = p.matcher(monthring);
			Matcher sm = p.matcher(seasonring);
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


	private String getSeasons(ArrayList<String> values) {
		String seasons = "";
		for(String v : values) {
			String[] t = v.trim().split("\\s+");
			if(t[t.length-1].matches(seasons)){
				seasons +=t[t.length-1]+"@";
			}
		}
		return seasons;
	}


	private String getMonths(ArrayList<String> values) {
		String months = "";
		for(String v : values) {
			String[] t = v.trim().split("\\s+");
			if(t[t.length-1].matches(months)){
				months +=t[t.length-1]+"@";
			}
		}
		return months;
	}

}
