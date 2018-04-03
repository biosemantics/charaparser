package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;

/**
 * Extracts the time of when phenological events occur as is evident in phenological descriptions.
 *
 * Examples:
 * Flowering and fruiting mid Jun–mid Sep.
 * Sporophytes mature spring-summer (May, Jun, Jul).
 * Flowering early summer-fall; staminate plants generally dying after anthesis, pistillate plants remaining dark green, persisting until frost.
 *
 * (verbform) and (biologicalEnitty, stage) can be exchanged for each other without changing the meaning. Not all (biologicalEntity, stage) constructs
 * may have a (verbform) that is exchangable.
 *
 * E.g.
 * flowering = flower appear;
 * fruiting = fruit appear;
 * leafing? = leaf appearing;
 * [leaf] dying = leaf dies; (biological entity required in this case)
 * capsuling = capsule appearing
 * ? = seed maturing
 * ? = fruit maturing
 *
 *
 */

//TODO: use pattern instad of indexOf to make use of word boundary of terms
public class PhenologyInfoExtractor {

	private final String gerundVerbs = "flowering|fruiting|coning|sporulating|seeding";
	private final String entities = "flowers|flower|leaves|leaf|fruit|fruits|cone|cones|seeds|seed|sporulates|"
			+ "sporulation|sporulate|sporocarps|sporocarp|spores|spore|capsules|capsule|sporophytes|sporophyte|sporophylls|sporophyll";
	private final String stages = "appearing|arising|maturing|matures|mature|maturity|produced|meiosis|dying|persisting|persists|persist";
	private final String timeTermsPattern =
			"Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|Feburary|March|April|June|July|August|September|October|November|December|spring|summer|fall|autumn|winter|midspring|"
					+ "midsummer|midwinter|midfall|midautumn|year[_-]round|\\w+ periods";
	private final Pattern timePattern = Pattern.compile("[^ ]*(_|-|\\b)(" + timeTermsPattern + ")(\\)|\\b)", Pattern.CASE_INSENSITIVE);
	private final String stageBreakingPattern = "^.*,\\s*(" + this.gerundVerbs + "|" + this.entities + ")$";
	private final String timeBreakingPattern = "^.*,\\s*(" + this.gerundVerbs + "|" + this.entities + "|" + this.stages + ")$";

	private List<String> verbForms;
	private List<String> biologicalEntityTerms;
	private List<String> stageTerms;
	private boolean biologicalEntityRequired;
	private String stopwords;
	private Pattern advModPattern;

	/**
	 * @param verbForms
	 * @param biologicalEntityTerms
	 * @param stageTerms
	 * @param biologicalEntityRequired
	 * @param advModPattern
	 * @param stopwords
	 */
	public PhenologyInfoExtractor(List<String> verbForms, List<String> biologicalEntityTerms,
			List<String> stageTerms, boolean biologicalEntityRequired,
			Pattern advModPattern, String stopwords) {
		this.verbForms = verbForms;
		this.biologicalEntityTerms = biologicalEntityTerms;
		this.stageTerms = stageTerms;
		this.biologicalEntityRequired = biologicalEntityRequired;
		this.advModPattern = advModPattern;
		this.stopwords = stopwords;
	}

	public LinkedHashSet<Character> extract(String text) {
		//log(LogLevel.INFO, "Extracting text: " + text);
		//log(LogLevel.INFO, "Extractor: " + this.gerundVerb + "; " + this.biologicalEntityTerms + "; " + this.stageTerms);

		LinkedHashSet<Character>  values = new LinkedHashSet<Character>();
		while(!text.trim().isEmpty()) {
			int verbIndex = -1;
			String verbFormSelected = null;
			for(String verb : verbForms) {
				verbIndex = getIndex(text, verb);
				if(verbIndex != -1) {
					verbFormSelected = verb;
					break;
				}
			}

			int minEntityIndex = Integer.MAX_VALUE;
			String minEntityTerm = null;
			for(String entityTerm : this.biologicalEntityTerms) {
				int entityIndex = getIndex(text, entityTerm);
				if(entityIndex >= 0) {
					if(entityIndex < minEntityIndex) {
						minEntityIndex = entityIndex;
						minEntityTerm = entityTerm;
					}
				}
			}

			if(verbIndex >= 0 && verbIndex < minEntityIndex) {
				text = extractByVerbForm(verbFormSelected, values, text);
			} else if(minEntityIndex < Integer.MAX_VALUE && verbIndex == -1 || minEntityIndex < verbIndex) {
				text = extractByBiologicalEntityStage(values, text, minEntityTerm);
			} else {
				text = "";
			}
		}
		return values;
	}

	private int getIndex(String text, String value) {
		Pattern pattern = Pattern.compile("\\b" + value + "\\b", Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(text);
		if(m.find()) {
			return m.start();
		}
		return -1;
	}

	private boolean matches(String text, String pattern) {
		return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find();
	}

	private String extractByBiologicalEntityStage(LinkedHashSet<Character> values, String text, String entityTerm) {
		System.out.println("extract bio entity stage" + text);
		int index = getIndex(text, entityTerm);
		if(index >= 0) {
			String afterEntityText = text.substring(index + entityTerm.length());
			for(String stageTerm : this.stageTerms) {
				int stageIndex = getIndex(afterEntityText, stageTerm);
				if(stageIndex >= 0) {
					String upToStageText = afterEntityText.substring(0, stageIndex).trim();
					if(matches(upToStageText, stageBreakingPattern)) {
						continue;
					}
					String stageText = afterEntityText.substring(stageIndex + stageTerm.length());

					String timeText = "";
					String remainderText = "";
					String passedText = "";
					boolean beyondTimesText = false;
					for(String term : stageText.split("\\b")) {
						passedText += term;
						if(beyondTimesText || matches(passedText, timeBreakingPattern)) {
							beyondTimesText = true;
							remainderText += term;
						} else {
							timeText += term;
						}
					}

					List<Time> times = new ArrayList<Time>();
					text = extractTimes(times, timeText) + " " + remainderText;
					values.addAll(this.createCharacters(times));
				}
			}
			text = afterEntityText;
		}

		System.out.println(values);
		return text;
	}

	private String extractByVerbForm(String verbForm, LinkedHashSet<Character> values, String text) {
		text = text.substring(this.getIndex(text, verbForm) + verbForm.length());
		String timeText = "";
		String remainderText = "";
		String passedText = "";
		boolean beyondTimesText = false;
		for(String term : text.split("\\b")) {
			passedText += term;
			if(beyondTimesText || matches(passedText, timeBreakingPattern)) {
				beyondTimesText = true;
				remainderText += term;
			} else {
				timeText += term;
			}
		}

		List<Time> times = new ArrayList<Time>();
		text = extractTimes(times, timeText) + " " + remainderText;
		values.addAll(this.createCharacters(times));
		return text;
	}

	/**
	 * Greedily creates Time objects found in the provided text
	 * @param times: List to add time found
	 * @param text: Text to extract time from
	 *
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
	 * @param times
	 * @param text
	 * @return
	 */
	private String extractTimes(List<Time> times, String text) {
		String remainText = text;
		Matcher m = this.timePattern.matcher(text);
		int start = 0;
		while(m.find(start)){
			Time time = new Time();
			time.setTime(text.substring(m.start(), m.end()).replaceAll("_", " "));
			String modifierBefore = modifierVerbatim(text.substring(start, m.start()), false); //before modi
			String modifierAfter = modifierVerbatim(text.substring(m.end()), true); //after modi
			start = m.end() +  modifierAfter.length();
			time.setModifier((modifierBefore.trim().replaceAll("^[.,:;]|[.,:;]$", "").trim()+"; "+modifierAfter).trim().replaceAll("^[.,:;]|[.,:;]$", "").trim());
			times.add(time);

			remainText = text.substring(m.end());
		}
		return remainText;
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
				if(!potential.matches(".*?"+timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.startsWith("[")){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf("]")+1: text.length());
				if(!potential.matches(".*?"+timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.indexOf("(")<0 && text.indexOf(")")>0){ //...)
				String potential = text.substring(0, text.indexOf(")")>0? text.indexOf(")")+1: text.length());
				if(!potential.matches(".*?"+timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.indexOf("[")<0 && text.indexOf("]")>0){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf("]")+1: text.length());
				if(!potential.matches(".*?"+timePattern.pattern()+".*"))
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
				if(!potential.matches(".*?"+timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.indexOf("[")<0 && text.indexOf("]")>0){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf("]")+1: text.length());
				if(!potential.matches(".*?"+timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.startsWith("(") && text.endsWith(")")){ // (in frostfree coastal habitats.)?
				String potential = text.substring(0, text.indexOf(")")>0? text.indexOf(")")+1: text.length());
				if(!potential.matches(".*?"+timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}

			if(text.startsWith("[") && text.endsWith("]")){
				String potential = text.substring(0, text.indexOf("]")>0? text.indexOf("]")+1: text.length());
				if(!potential.matches(".*?"+timePattern.pattern()+".*"))
					return leads+potential.replaceFirst("("+this.stopwords+"| )+$", "");
			}
			// ... mostly [summer–autumn (Jun–Oct)]
			int start = text.length();
			Matcher m = this.advModPattern.matcher(text);
			while (m.find()){
				if(!text.substring(m.start()).matches(".*?"+timePattern.pattern()+".*")) //no \d between m.start and end of text
					start = m.start();
			}
			modifier = text.substring(start);
		}
		return leads+modifier.replaceFirst("("+this.stopwords+"| )+$", "");
	}

	private Collection<? extends Character> createCharacters(List<Time> times) {
		LinkedHashSet<Character>  values = new LinkedHashSet<Character>();
		for(Time time: times) {
			Character c = new Character();
			String name = "";
			if(!this.verbForms.isEmpty()) {
				name = this.verbForms.get(0) + " time";
			} else {
				name = this.biologicalEntityTerms.get(0) + " " + this.stageTerms.get(0) + " time";
			}

			c.setName(name);
			c.setValue(time.getCleanTime());
			c.setModifier(time.getCleanModifier());
			values.add(c);
			log(LogLevel.INFO, "Create character " + name + " --> " + time);
		}
		return values;
	}

}
