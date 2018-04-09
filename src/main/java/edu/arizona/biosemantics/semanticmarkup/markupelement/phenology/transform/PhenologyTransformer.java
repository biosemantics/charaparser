package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Phenology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Treatment;

public class PhenologyTransformer implements IPhenologyTransformer {


	//static Hashtable<String, String> m2smapping = new Hashtable<String, String>();
	//static Hashtable<String, String> s2mmapping = new Hashtable<String, String>();
	//static String monthring="jan-feb-mar-apr-may-jun-jul-aug-sep-oct-nov-dec-jan-feb-mar-apr-may-jun-jul-aug-sep-oct-nov-dec";
	//static String value="(.*?)((jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|spring|summer|fall|winter|year|round|late|early|mid|middle| |-)+)(.*)";
	//static String seasonring = "spring-summer-fall-winter-spring-summer-fall-winter";
	//static String seasons = "(spring|summer|fall|winter)";
	//static String months ="(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)";
	static String keywordsPtn = "flowers|flowering|flower|leaves|fruiting|coning|cones|seeds|sporulating|sporulates|sporulation|sporocarps|spores|capsules|capsule|sporophytes|sporophyte|sporophylls";
	static String stagesPtn ="appearing|arising|maturing|matures|mature|maturity|produced|meiosis|dying|persisting|persists|persist";
	static String timePtn = "Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|Feburary|March|April|June|July|August|September|October|November|December|spring|summer|fall|autumn|winter|midspring|midsummer|midwinter|midfall|midautumn|year[_-]round|\\w+ periods";
	static String timeModifierPtn="latter|late|last|early|mid|middle";
	static Pattern timePattern = Pattern.compile("[^ ]*(_|-|\\b)("+timePtn+")(\\)|\\b)", Pattern.CASE_INSENSITIVE);

	static Hashtable<String, String> phenologyNames = null;
	static Hashtable<String, String> stages = null;
	static{
		stages = new Hashtable<String, String>();
		//stage phrase => standard stages
		//appear
		stages.put("occuring", "appearing");
		stages.put("appearing", "appearing");
		stages.put("arising", "appearing");
		stages.put("produced", "appearing");

		//mature
		stages.put("maturing", "maturing");
		stages.put("mature", "maturing");
		stages.put("matures", "maturing");
		stages.put("maturity", "maturing");


		//persist
		stages.put("persisting", "persisting");
		stages.put("persists", "persisting");
		stages.put("persist", "persisting");

		//meiosis
		stages.put("meiosis", "meiosis");

		//dying
		stages.put("dying", "dying");

		//use standardized stages as defined above in keys
		phenologyNames = new Hashtable<String, String>();
		//flower appearing time
		phenologyNames.put("flowering", "flowering time");
		phenologyNames.put("flowers", "flowering time");
		phenologyNames.put("flower", "flowering time");
		//flowers dying time
		phenologyNames.put("flowers dying", "flowers dying time");

		//leaves appearing time
		phenologyNames.put("leaves appearing", "leaves appearing time");
		//leaves meiosis time
		phenologyNames.put("leaves meiosis", "leaves meiosis time");
		//leaves dying time
		phenologyNames.put("leaves dying", "leaves dying time");

		//fruits appearing time
		phenologyNames.put("fruiting", "fruiting_time");

		//cones appearing
		phenologyNames.put("coning", "cones appearing time");
		phenologyNames.put("cones appearing", "cones appearing time");
		//cones maturing
		phenologyNames.put("cones maturing", "cones maturing time");

		//seeds maturing
		phenologyNames.put("seeds maturing", "seed maturing time");

		//spore maturity
		phenologyNames.put("sporulating", "spores maturing time");
		phenologyNames.put("sporulates", "spores maturing time");
		phenologyNames.put("spores maturing", "spores maturing time");
		phenologyNames.put("sporulation", "spores maturing time");

		//capsules maturing
		phenologyNames.put("capsules maturing", "capsules maturing time");
		phenologyNames.put("capsule maturing", "capsules maturing time");

		//sporocarps appearing
		phenologyNames.put("sporocarps appearing", "sporocarps appearing time");
		//sporocarps maturing
		phenologyNames.put("sporocarps maturing", "sporocarps maturing time");

		//sporophtyes maturing
		phenologyNames.put("sporophtyes maturing", "sporophytes maturing time");
		phenologyNames.put("sporophtye maturing", "sporophytes maturing time");

		//sporophylls
		phenologyNames.put("sporophylls appearing", "sporophylls appearing time");
		phenologyNames.put("sporophylls persisting", "sporophylls persisting time");

	}

	/*static{
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
	}*/

	String stopwords = null;
	Pattern advModPattern = null;
	@Inject
	public PhenologyTransformer(@Named("AdvModifiers") String advModifiers,
			@Named("LyAdverbpattern") String lyAdvPattern, @Named("StopWordString") String stopwords){
		this.stopwords = stopwords;
		this.advModPattern = Pattern.compile( "("+advModifiers+"|"+lyAdvPattern+"|ca.)");
	}

	@Override
	public void transform(List<PhenologiesFile> phenologiesFiles) {
		for(PhenologiesFile phenologiesFile : phenologiesFiles) {
			int i = 0;
			int organId = 0;
			for(Treatment treatment : phenologiesFile.getTreatments()) {
				for(Phenology phenology : treatment.getPhenologies()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("phenology_" + i++);
					statement.setText(phenology.getText());

					BiologicalEntity be = new BiologicalEntity();
					be.setName("whole_organism");
					be.setId("phen_o"+organId++);
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

	public LinkedHashSet<Character> parse(String text) {
		log(LogLevel.DEBUG, "Text to parse: "+text);
		System.out.println("Text to parse: "+text);
		text = text.replaceAll("\\s+", " ").replaceAll("–", "-").replaceAll("-+", "-").replaceAll("year-round", "year_round"); // - to mean "to"
		String nText = normalizeTime(text);
		if(nText.compareToIgnoreCase(text)!=0){
			log(LogLevel.DEBUG, "Text normalized to: "+nText);
			System.out.println("Text normalized to: "+nText);
		}

		text = nText;

		LinkedHashSet<Character>  values = new LinkedHashSet<Character>();
		ArrayList<PhenologyInfo> pis = new ArrayList<PhenologyInfo> ();

		//no time and no keyword = not a phenology statement
		/*if(!text.matches(".*?("+this.timePtn+").*?") && ! text.matches(".*?("+this.keywordsPtn+").*?")){
			return values;
		}*/

		//check for keywords, stage, then time
		//text="  and dying in late_spring-early_summer,";
		//Pattern p = Pattern.compile("(?<=(?:[,;.] |^))((?:"+this.keywordsPtn+")\\b)? ?(\\b(?:"+this.stagesPtn+")\\b)?(.*?[^ ]*(?:"+this.timePtn+")\\b)", Pattern.CASE_INSENSITIVE);
		/*Pattern p = Pattern.compile("\\b((?:"+this.keywordsPtn+")\\b)?+ ?(\\b(?:"+this.stagesPtn+")\\b)?+(.*?[^ ]*(?:"+this.timePtn+")\\b)", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);
		String keyword = "";
		int start = 0;
		while(m.find(start)){
			if(m.start(1) != -1)
				keyword = text.substring(m.start(1), m.end(1)).toLowerCase();
			String stage = "";
			if(m.start(2) != -1)
				stage = text.substring(m.start(2), m.end(2));
			String time = text.substring(m.start(3), m.end(3));
			String extend = completeTime(text.substring(m.end(3)));
			time = time +extend; //one time = the time for a keyword + a stage
			start = m.end(3)+extend.length();
			//System.out.println("next start index: "+start);


			ArrayList<Time> times = parseTime(time);

			PhenologyInfo pi = new PhenologyInfo();
			pi.setName(keyword, stage);
			pi.addAllTime(times);
			pis.add(pi);

			log(LogLevel.DEBUG, "add a PhenologyInfo object: "+pi.toString());
			System.out.println("add a PhenologyInfo object: "+pi.toString());
		}*/
		String keyword = "";
		Matcher m = PhenologyTransformer.timePattern.matcher(text);
		int start = 0;
		while(m.find(start)){
			Pattern p = Pattern.compile("\\b((?:"+PhenologyTransformer.keywordsPtn+")\\b)", Pattern.CASE_INSENSITIVE);
			Matcher m1 = p.matcher(text);

			if(m1.find(start)){
				keyword = text.substring(m1.start(), m1.end()).toLowerCase();
				start = m1.end();
			}
			String stage = "";
			p = Pattern.compile("\\b((?:"+PhenologyTransformer.stagesPtn+")\\b)", Pattern.CASE_INSENSITIVE);
			m1 = p.matcher(text);

			if(m1.find(start)){
				if(text.substring(start, m1.start()).toLowerCase().matches(".*?\\b((?:"+PhenologyTransformer.keywordsPtn+")\\b).*")) //a keyword in between
					continue;
				stage = text.substring(m1.start(), m1.end());
				start = m1.end();
			}

			String time = "";
			m1 = PhenologyTransformer.timePattern.matcher(text);

			if(m1.find(start)){
				if(text.substring(start, m1.start()).toLowerCase().matches(".*?\\b((?:"+PhenologyTransformer.keywordsPtn+")\\b).*")) //a keyword in between
					continue;
				time = text.substring(start, m1.end());
				String extend = completeTime(text.substring(m1.end()));
				time = time +extend; //one time = the time for a keyword + a stage
				start = m1.end()+extend.length();
				//System.out.println("next start index: "+start);


				ArrayList<Time> times = parseTime(time);

				PhenologyInfo pi = new PhenologyInfo();
				pi.setName(keyword, stage);
				pi.addAllTime(times);
				pis.add(pi);

				log(LogLevel.DEBUG, "add a PhenologyInfo object: "+pi.toString());
				System.out.println("add a PhenologyInfo object: "+pi.toString());

			}
		}

		//check for keywords embedded in the middle of a sentence
		//and the cases of unknown, not reported, undetermined, not determined, no ... data are available

		for(PhenologyInfo pi: pis){
			ArrayList<Time> times = pi.getTime();
			for(Time time: times){
				Character c = new Character();
				c.setName(pi.getName()==null? "phenology" : pi.getName());
				c.setValue(time.getCleanTime());
				c.setModifier(time.getCleanModifier());
				values.add(c);
			}
		}







		/*text = text.toLowerCase().replaceFirst("(flowering|fruiting)\\s+", "").replaceAll("_", "-");
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


		for(String vs: valueStrings){
			Character c = new Character();
			c.setName("phenology");
			c.setValue(vs);
			values.add(c);
		}
		 */
		//values.addAll(formValues(valueStrings)); //do not translate May to Summer, year around to Jan-Dec.
		return values;
	}

	/**
	 *  spring–summer (year-round south)
	 *  mostly summer–autumn (Jun–Oct), sometimes year-round in frostfree coastal habitats.
	 *  (chasmogamous) Mar–early Jun, (cleistogamous) Aug–Nov.
	 *  spring–summer (year-round south)
	 *  late winter--mid spring (early Mar--mid Apr)
	 *  in early spring and dying in late spring and early summer, long before those of associated moonworts.
	 *  in latter half of winter and early spring, sometimes with second flush in same year after heavy rains
	 *  during wet periods
	 *
	 *
	 *  time:spring-summer
	 *  time:year-round modifier:south
	 * @param text
	 * @return
	 */
	private ArrayList<Time> parseTime(String text) {

		ArrayList<Time> times = new ArrayList<Time>();
		Matcher m = this.timePattern.matcher(text);
		int start = 0;
		while(m.find(start)){
			Time time = new Time();
			time.setTime(text.substring(m.start(), m.end()).replaceAll("_", " "));
			String modifierB = modifierVerbatim(text.substring(start, m.start()), false); //before modi
			String modifierA = modifierVerbatim(text.substring(m.end()), true); //after modi
			start = m.end() +  modifierA.length();
			time.setModifier((modifierB.trim().replaceAll("^[.,:;]|[.,:;]$", "").trim()+"; "+modifierA).trim().replaceAll("^[.,:;]|[.,:;]$", "").trim());
			times.add(time);
		}
		return times;
	}

	/**
	 * adv and preposition phrases
	 * @param text
	 * @param lookAtBeginning
	 * @return
	 */
	private String modifierVerbatim(String text, boolean lookAtBeginning) {
		if(text.trim().length()==0 || text.matches("\\W+")) return "";

		String modifier = "";
		String leads = text.startsWith(" ")? " ": "";
		text = text.trim();
		if(lookAtBeginning){ // the target is at the beginning of text: .... in frostfree coastal habitats.
			if(text.startsWith("(")){ // (in frostfree coastal habitats.)?
				String potential = text.substring(0, text.indexOf(")")>0? text.indexOf(")")+1: text.length());
				if(!potential.matches(".*?"+PhenologyTransformer.timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.startsWith("[")){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf("]")+1: text.length());
				if(!potential.matches(".*?"+PhenologyTransformer.timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.indexOf("(")<0 && text.indexOf(")")>0){ //...)
				String potential = text.substring(0, text.indexOf(")")>0? text.indexOf(")")+1: text.length());
				if(!potential.matches(".*?"+PhenologyTransformer.timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.indexOf("[")<0 && text.indexOf("]")>0){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf("]")+1: text.length());
				if(!potential.matches(".*?"+PhenologyTransformer.timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			Matcher m =this.timePattern.matcher(text);
			if(m.find()){
				modifier = text.substring(0, m.start());
				modifier = modifier.split("[,;.]")[0];
			}else{
				modifier = text;
			}
			return leads+modifier.replaceFirst("("+this.stopwords+"| )+$", ""); //remove trailing "and" etc.
		}else{ //the target is at the end of the text: in frostfree coastal habitats ....
			if(text.matches("^[ ,;:.].*")){
				String leadingPuncts = text.replaceAll("(?<=^[,;:.] ).*", "");
				text = text.replaceFirst(leadingPuncts, "");
				leads = leads + leadingPuncts;
			}

			if(text.indexOf("(")<0 && text.indexOf(")")>0){ //...)
				String potential = text.substring(0, text.indexOf(")")>0? text.indexOf(")")+1: text.length());
				if(!potential.matches(".*?"+PhenologyTransformer.timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.indexOf("[")<0 && text.indexOf("]")>0){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf("]")+1: text.length());
				if(!potential.matches(".*?"+PhenologyTransformer.timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.startsWith("(") && text.endsWith(")")){ // (in frostfree coastal habitats.)?
				String potential = text.substring(0, text.indexOf(")")>0? text.indexOf(")")+1: text.length());
				if(!potential.matches(".*?"+PhenologyTransformer.timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.startsWith("[") && text.endsWith("]")){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf("]")+1: text.length());
				if(!potential.matches(".*?"+PhenologyTransformer.timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}
			// ... mostly [summer–autumn (Jun–Oct)]
			int start = text.length();
			Matcher m = this.advModPattern.matcher(text);
			while (m.find()){
				if(!text.substring(m.start()).matches(".*?"+PhenologyTransformer.timePattern.pattern()+".*")) //no \d between m.start and end of text
					start = m.start();
			}
			modifier = text.substring(start);
		}
		return leads+modifier.replaceFirst("("+this.stopwords+"| )+$", "");
	}

	/**
	 * late spring and early summer (May-Jun)
	 * to late_spring-early_summer_(May-Jun)
	 *
	 * latter half of winter?
	 * to latter_half_of_winter?
	 * @param text
	 * @return
	 */
	private String normalizeTime(String text){
		//all year, throughout the year, through the year, year round => year_round
		text = text.replaceAll("\\b(all year|througout the year|through the year|year around)\\b", "year_round");

		//late spring
		text = text.replaceAll("(?<=\\b(?:"+PhenologyTransformer.timeModifierPtn+")\\b) (?=(?:"+PhenologyTransformer.timePtn+")\\b)", "_");

		//latter half of winter
		Pattern p = Pattern.compile("\\b(?:"+PhenologyTransformer.timeModifierPtn+")\\b.*?\\b(?:"+PhenologyTransformer.timePtn+")\\b");
		Matcher m = p.matcher(text);
		while(m.find()){
			String matched = text.substring(m.start(), m.end());
			if(matched.split("\\s+").length<=4 && !matched.matches(".*?[()\\]\\[].*?")){
				String stringed = matched.replaceAll("\\s+", "_");
				text = text.replaceAll(matched, stringed);
			}
		}

		//and/or/to
		text = text.replaceAll("(?<=(\\b|_|-)(?:"+PhenologyTransformer.timeModifierPtn+"|"+PhenologyTransformer.timePtn+")\\b) (and|or|to|through) (?=(?:"+PhenologyTransformer.timeModifierPtn+"|"+PhenologyTransformer.timePtn+")(\\b|_|-))", "-");



		//summer (May-Jun)
		text = text.replaceAll("(?<=(\\b|_|-)(?:"+PhenologyTransformer.timePtn+")\\b) (?=(?:\\([^ ]*(_|-|\\b)("+PhenologyTransformer.timePtn+")\\)))", "_");
		return text;
	}

	/**
	 * complete the time value from the string [following], e.g.:
	 *
	 * sometimes year-round [in frostfree coastal habitats].
	 * spring–summer (year-round [south)]
	 * Leaves appearing in June[, dying in September].
	 * @param follow
	 * @return [in frostfree coastal habitats],  [south)], and "";
	 */
	private String completeTime(String follow) {
		//start from 0 and determine where to stop
		//keep everything including space in the string
		Pattern t = Pattern.compile("\\b("+this.stagesPtn+")\\b", Pattern.CASE_INSENSITIVE);
		Matcher m = t.matcher(follow);
		if(m.find()){
			follow = follow.substring(0, m.start());
		}
		return follow.replaceFirst("("+this.stopwords+"| )+$", "");

		/*t = Pattern.compile("[^ ]*("+this.timePtn+")\\b", Pattern.CASE_INSENSITIVE);
		m = t.matcher(follow);
		int stop = 0;
		while(m.find()){
			stop = m.end();
		}
		follow = follow.substring(stop);

		//...)
		if(follow.replaceAll("[^\\(\\[\\)\\],;\\.]", "").matches("^[\\)\\],;.].*")){
			stop = follow.indexOf(")");
			if(stop>=0) return follow.substring(0, stop+1);
			else if(follow.indexOf("]") >=0)
					return follow.substring(0, follow.indexOf("]")+1);
			else if(follow.indexOf(",") >=0)
				return follow.substring(0, follow.indexOf(","));
			else if(follow.indexOf(";") >=0)
				return follow.substring(0, follow.indexOf(";"));
			else if(follow.indexOf(".") >=0)
				return follow.substring(0, follow.indexOf("."));
		}
		return "";*/
	}

	/*private  LinkedHashSet<Character>  formValues(ArrayList<String> valueString) {
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

	/*private ArrayList<String> allValuesInRange(String[] times) {
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
	}*/

	/*private void dump2ArrayList(String[] array, ArrayList<String> arrayList) {
		for(int i = 0; i <array.length; i++){
			arrayList.add(array[i]);
		}
	}*/

	/*
	 * return @-connected values
	 */
	/*private String getSeasons(ArrayList<String> values) {
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
	/*private String getMonths(ArrayList<String> values) {
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
	}*/

	private class Time{
		String timeString = null;
		String modifier = null;
		protected String getTime() {
			return timeString;
		}

		protected String getCleanTime() {
			//remove unmatched ),
			timeString = timeString.trim();
			String prev = timeString;
			while(timeString.matches(".*?[\\)\\]\\(\\[].*")){
				if(timeString.matches("^[\\(\\[].*") && !timeString.matches(".*?[\\)\\]].+"))
					timeString = timeString.substring(1);
				if(timeString.matches(".*[\\)\\]]$") && !timeString.matches(".*?[\\(\\[].+"))
					timeString = timeString.substring(0, timeString.length()-1);
				if(prev.compareTo(timeString)==0) break;
				prev = timeString;
			}
			//and leading/trailing puncts
			timeString = timeString.replaceAll("^[ .:;,]+|[ .:;,]+$", "");
			return timeString;
		}
		protected void setTime(String time) {
			this.timeString = time;
		}
		protected String getModifier() {
			return modifier;
		}
		protected String getCleanModifier() {
			//remove unmatched ),
			modifier = modifier.trim();
			String prev = modifier;
			while(modifier.matches(".*?[\\)\\]\\(\\[].*")){
				if(modifier.matches("^[\\(\\[].*") && !modifier.matches(".*?[\\)\\]].+"))
					modifier = modifier.substring(1);
				if(modifier.matches(".*[\\)\\]]$") && !modifier.matches(".*?[\\(\\[].+"))
					modifier = modifier.substring(0, modifier.length()-1);
				if(prev.compareTo(modifier)==0) break;
				prev = modifier;
			}
			//and leading/trailing puncts
			modifier = modifier.replaceAll("^[ .:;,]+|[ .:;,]+$", "");
			return modifier;
		}

		protected void setModifier(String modifier) {
			this.modifier = modifier;
		}
		@Override
		public String toString(){
			return "time = "+timeString+System.getProperty("line.separator")+
					"modifier = "+modifier +System.getProperty("line.separator");
		}
	}

	private class PhenologyInfo{
		String name = null;
		ArrayList<Time> times = new ArrayList<Time>(); //time => modifier/constraint for the time

		protected String getName() {
			return name;
		}

		protected void setName(String keyword, String stage) {
			String stdStage = PhenologyTransformer.stages.get(stage);
			this.name = PhenologyTransformer.phenologyNames.get(keyword+(stdStage==null? "": " "+stdStage));
		}

		protected ArrayList<Time> getTime(){
			return this.times;
		}

		protected void addTime(Time time){
			this.times.add(time);
		}

		protected void addAllTime(ArrayList<Time> time){
			this.times.addAll(time);
		}

		@Override
		public String toString(){
			String s = "name = "+name+System.getProperty("line.separator");
			for(Time time: this.times){
				s = s+time.toString()+System.getProperty("line.separator");
			}
			return s;
		}

	}
}
