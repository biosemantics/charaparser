package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			"January|Feburary|March|April|June|July|August|September|October|November|December|Jan|Feb|Mar|March|April|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|spring|summer|fall|autumn|winter|midspring|"
					+ "midsummer|midwinter|midfall|midautumn|year[_-]round|\\w+ periods";
	private final Pattern timePattern = Pattern.compile("[^ ]*(_|-|\\b)(" + timeTermsPattern + ")(\\)|\\b)", Pattern.CASE_INSENSITIVE);
	private final String stageBreakingPattern = "^.*,\\s*(" + this.gerundVerbs + "|" + this.entities + ")$";
	private final String timeBreakingPattern = "^.*,\\s*(" + this.gerundVerbs + "|" + this.entities + "|" + this.stages + ")$";

	private final String timeTermModifiers = "early|mid|late";
	private final String modifiedTimeTermsPattern = "(" + timeTermModifiers + ")?\\s*(" + timeTermsPattern + ")?";
	private final String fromOutlierPattern = "\\((-*|(\\bfrom\\b)?)\\s*(" + modifiedTimeTermsPattern + ")\\+?\\s*-*\\s*.*\\)";
	private final String fromForeignPattern = "\\[(-*|(\\bfrom\\b)?)\\s*(" + modifiedTimeTermsPattern + ")\\+?\\s*-*\\s*.*\\]";
	private final String toOutlierPattern = "\\((-*|(\\bto\\b)?)\\s*(" + modifiedTimeTermsPattern + ")\\+?\\s*-*\\s*.*\\)";
	private final String toForeignPattern = "\\[(-*|(\\bto\\b)?)\\s*(" + modifiedTimeTermsPattern + ")\\+?\\s*-*\\s*.*\\]";
	private final String numericalRangeString = "(?<prefix>.*?)\\s*(?:(?<fromOutlier>" + fromOutlierPattern + ")|(?<fromForeign>" + fromForeignPattern + "))?\\s*"
			+ "(:?(?<from>(" + modifiedTimeTermsPattern + "))\\s*-+\\s*(?<to>(" + modifiedTimeTermsPattern + ")\\+?)|"
			+ "(?<singleDataPoint>(" + modifiedTimeTermsPattern + ")))\\s*(?:(?<toOutlier>" + toOutlierPattern + ")|(?<toForeign>" + toForeignPattern + "))?\\s*"
			+ "(?<postfix>.*?)";

	private final Pattern numericalRange = Pattern.compile(numericalRangeString, Pattern.CASE_INSENSITIVE);

	private List<String> verbForms;
	private List<String> biologicalEntityTerms;
	private List<String> stageTerms;
	private boolean biologicalEntityRequired;
	private String stopwords;
	private Pattern advModPattern;
	private String lyAdvPattern;
	private String modifierList;
	private String advModifiers;

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
			Pattern advModPattern, String stopwords, String lyAdvPattern, String modifierList, String advModifiers) {
		this.lyAdvPattern = lyAdvPattern;
		this.modifierList = modifierList;
		this.advModifiers = advModifiers;
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

					text = getTimeCharacters(timeText, values) + " " + remainderText;
				}
			}
			text = afterEntityText;
		}

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

		text = getTimeCharacters(timeText, values) + " " + remainderText;
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
	private String getTimeCharacters(String text, LinkedHashSet<Character> values) {
		String remainText = text;
		Matcher m = this.timePattern.matcher(text);
		int start = 0;
		while(m.find(start)){
			String timeString = text.substring(m.start(), m.end()).replaceAll("_", " ");

			String modifierBefore = modifierVerbatim(text.substring(start, m.start()), false); //before modi
			String modifierAfter = modifierVerbatim(text.substring(m.end()), true); //after modi
			start = m.end() +  modifierAfter.length();
			String modifierString = (modifierBefore.trim().replaceAll("^[.,:;]|[.,:;]$", "").trim()+"; "+modifierAfter).trim().replaceAll("^[.,:;]|[.,:;]$", "").trim();

			String name = "";
			if(!this.verbForms.isEmpty()) {
				name = this.verbForms.get(0) + " time";
			} else {
				name = this.biologicalEntityTerms.get(0) + " " + this.stageTerms.get(0) + " time";
			}

			String cleanTime = getCleanTime(timeString);
			String cleanModifier = getCleanModifier(modifierString);

			if(isTimeRange(cleanTime)) {
				List<String> time = Arrays.asList(cleanTime);
				if(isCoarseSpecificTimeRangeSplit(cleanTime)) {
					time = getCoarseSpecificTime(cleanTime);
				}
				for(String t : time) {
					values.addAll(getTimeRangeCharacters(name, t, cleanModifier));
				}
			} else {
				Character c = new Character();
				c.setName(name);
				c.setValue(cleanTime);
				c.setModifier(cleanModifier);
				values.add(c);
			}

			remainText = text.substring(m.end());
		}

		return remainText;
	}

	private List<String> getCoarseSpecificTime(String cleanTime) {
		List<String> result = new ArrayList<String>();
		result.add(cleanTime.split("\\(")[0]);
		result.add(cleanTime.split("\\(")[1]);
		return result;
	}

	private boolean isCoarseSpecificTimeRangeSplit(String time) {
		Pattern pattern = Pattern.compile(".*(" + modifiedTimeTermsPattern + ")\\s*-+\\s*(" + modifiedTimeTermsPattern + ")\\s*"
				+ "\\(\\s*(" + modifiedTimeTermsPattern + ")\\s*-+\\s*(" + modifiedTimeTermsPattern + ")\\s*\\).*");
		return pattern.matcher(time).matches();
	}

	private Collection<? extends Character> getTimeRangeCharacters(String name, String text,
			String modifier) {
		LinkedHashSet<Character> result = new LinkedHashSet<Character>();
		Matcher numericalRangeMatcher = numericalRange.matcher(text);
		if(numericalRangeMatcher.matches()){
			String from = numericalRangeMatcher.group("from");
			String to = numericalRangeMatcher.group("to");
			String singleDataPoint = numericalRangeMatcher.group("singleDataPoint");
			String fromOutlier = numericalRangeMatcher.group("fromOutlier");
			String fromForeign = numericalRangeMatcher.group("fromForeign");
			String toOutlier = numericalRangeMatcher.group("toOutlier");
			String toForeign = numericalRangeMatcher.group("toForeign");
			String fromOutlierConstraint = null;
			String fromForeignConstraint = null;
			String toOutlierConstraint = null;
			String toForeignConstraint = null;
			String prefix = numericalRangeMatcher.group("prefix");
			String postfix = numericalRangeMatcher.group("postfix");

			Pattern fromOutlierExtractPattern = Pattern.compile("\\(.*?(?<outlier>-?(" + modifiedTimeTermsPattern + ")\\+?)\\s*-*\\s*(?<constraint>.*)\\)", Pattern.CASE_INSENSITIVE);
			Pattern toOutlierExtractPattern = Pattern.compile("\\((-*|(\\bto\\b)?)\\s*(?<outlier>(" + modifiedTimeTermsPattern + ")\\+?)\\s*-*\\s*(?<constraint>.*)\\)", Pattern.CASE_INSENSITIVE);
			if(fromOutlier != null) {
				Matcher fromOutlierMatcher = fromOutlierExtractPattern.matcher(fromOutlier);
				if(fromOutlierMatcher.matches()) {
					fromOutlier = fromOutlierMatcher.group("outlier");
					fromOutlierConstraint = fromOutlierMatcher.group("constraint");
				}
			}
			if(toOutlier != null) {
				Matcher toOutlierMatcher = toOutlierExtractPattern.matcher(toOutlier);
				if(toOutlierMatcher.matches()) {
					toOutlier = toOutlierMatcher.group("outlier");
					toOutlierConstraint = toOutlierMatcher.group("constraint");
				}
			}

			Pattern fromForeignExtractPattern = Pattern.compile("\\[.*?(?<foreign>-?(" + modifiedTimeTermsPattern + ")\\+?)\\s*-*\\s*(?<constraint>.*)\\]", Pattern.CASE_INSENSITIVE);
			Pattern toForeignExtractPattern = Pattern.compile("\\[(-*|(\\bto\\b)?)\\s*(?<foreign>(" + modifiedTimeTermsPattern + ")\\+?)\\s*-*\\s*(?<constraint>.*)\\]", Pattern.CASE_INSENSITIVE);
			if(fromForeign != null) {
				Matcher fromForeignMatcher = fromForeignExtractPattern.matcher(fromForeign);
				if(fromForeignMatcher.matches()) {
					fromForeign = fromForeignMatcher.group("foreign");
					fromForeignConstraint = fromForeignMatcher.group("constraint");
				}
			}
			if(toForeign != null) {
				Matcher toForeignMatcher = toForeignExtractPattern.matcher(toForeign);
				if(toForeignMatcher.matches()) {
					toForeign = toForeignMatcher.group("foreign");
					toForeignConstraint = toForeignMatcher.group("constraint");
				}
			}

			String foreignConstraint = null;
			if(fromForeignConstraint != null && toForeignConstraint != null) {
				foreignConstraint = fromForeignConstraint + " " + toForeignConstraint;
			} else if(fromForeignConstraint != null) {
				foreignConstraint = fromForeignConstraint;
			} else if(toForeignConstraint != null) {
				foreignConstraint = toForeignConstraint;
			}

			String outlierConstraint = null;
			if(fromOutlierConstraint != null && toOutlierConstraint != null) {
				outlierConstraint = fromOutlierConstraint + " " + toOutlierConstraint;
			} else if(fromOutlierConstraint != null) {
				outlierConstraint = fromOutlierConstraint;
			} else if(toOutlierConstraint != null) {
				outlierConstraint = toOutlierConstraint;
			}


			boolean prefixModifier = false;
			if(prefix != null && !prefix.trim().isEmpty()) {
				String[] parts = prefix.split("\\s+");
				for(String part : parts) {
					if(part.matches(this.lyAdvPattern) || part.matches(this.modifierList) || part.matches(this.advModifiers)) {
						modifier += " " + part;
						prefixModifier = true;
					}
				}
			}

			String constraint = "";
			boolean postFixConstraint = false;
			if(postfix != null && !postfix.trim().isEmpty()) {
				postfix = normalize(postfix);
				Pattern postfixPattern = Pattern.compile("^\\s*\\((?<constraint>.*)\\)\\s*$", Pattern.CASE_INSENSITIVE);
				Matcher postfixMatcher = postfixPattern.matcher(postfix);
				if(postfixMatcher.matches()) {
					constraint = postfixMatcher.group("constraint");
					postFixConstraint = true;
				}
			}
			if(!prefixModifier && !postFixConstraint) {
				prefix = normalize(prefix);
				postfix = normalize(postfix);
				if(prefix.endsWith("[") || prefix.endsWith("(") || prefix.endsWith("{"))
					prefix = prefix.substring(0, prefix.length() - 1).trim();
				if(postfix.startsWith("]") || postfix.startsWith(")") || postfix.startsWith("}"))
					postfix = postfix.substring(1).trim();
				constraint += normalize(prefix) + " " + normalize(postfix).trim();
			}

			if(from == null && to == null && singleDataPoint != null) {
				from = singleDataPoint;
				to = singleDataPoint;
			}

			result.addAll(this.createCharactersForRange(name,
					from, to, fromOutlier, fromForeign, toOutlier, toForeign, modifier, constraint, outlierConstraint, foreignConstraint));
		}

		return result;
	}

	private List<Character> createCharactersForRange(String name, String from, String to,
			String fromOutlier, String fromForeign, String toOutlier,
			String toForeign, String modifier, String constraint, String outlierConstraint, String foreignConstraint) {
		List<Character> result = new ArrayList<Character>();

		outlierConstraint = normalize(outlierConstraint);

		Character range = new Character();
		range.setCharType("range_value");
		range.setFrom(from);
		range.setTo(to);
		range.setName(name);
		if(modifier != null && !modifier.trim().isEmpty()) {
			range.setModifier(modifier.trim());
			if(modifier.trim().matches("rarely|seldom|occasionally")) {
				range.setCharType("atypical_range");
			}
		}
		if(constraint != null && !constraint.trim().isEmpty()) {
			range.setConstraint(constraint);
		}
		result.add(range);

		if(fromOutlier != null && toOutlier != null) {
			Character atypicalRange = new Character();
			atypicalRange.setCharType("atypical_range");
			atypicalRange.setFrom(fromOutlier);
			atypicalRange.setTo(toOutlier);
			atypicalRange.setName(name);
			if(outlierConstraint != null && !outlierConstraint.trim().isEmpty())
				atypicalRange.setConstraint(outlierConstraint);
			result.add(atypicalRange);
		} else if(fromOutlier != null) {
			Character atypicalRange = new Character();
			atypicalRange.setCharType("atypical_range");
			atypicalRange.setFrom(fromOutlier);
			atypicalRange.setTo(to);
			atypicalRange.setName(name);
			if(outlierConstraint != null && !outlierConstraint.trim().isEmpty())
				atypicalRange.setConstraint(outlierConstraint);
			result.add(atypicalRange);
		} else if(toOutlier != null) {
			Character atypicalRange = new Character();
			atypicalRange.setCharType("atypical_range");
			atypicalRange.setFrom(from);
			atypicalRange.setTo(toOutlier);
			atypicalRange.setName(name);
			if(outlierConstraint != null && !outlierConstraint.trim().isEmpty())
				atypicalRange.setConstraint(outlierConstraint);
			result.add(atypicalRange);
		}

		if(fromForeign != null && toForeign != null) {
			Character foreignRange = new Character();
			foreignRange.setCharType("foreign_range");
			foreignRange.setFrom(fromForeign);
			foreignRange.setTo(toForeign);
			foreignRange.setName(name);
			if(foreignConstraint != null && !foreignConstraint.trim().isEmpty())
				foreignRange.setConstraint(foreignConstraint);
			result.add(foreignRange);
		} else if(fromForeign != null) {
			Character foreignRange = new Character();
			foreignRange.setCharType("foreign_range");
			foreignRange.setFrom(fromForeign);
			foreignRange.setTo(to);
			foreignRange.setName(name);
			if(foreignConstraint != null && !foreignConstraint.trim().isEmpty())
				foreignRange.setConstraint(foreignConstraint);
			result.add(foreignRange);
		} else if(toForeign != null) {
			Character foreignRange = new Character();
			foreignRange.setCharType("foreign_range");
			foreignRange.setFrom(from);
			foreignRange.setTo(toForeign);
			foreignRange.setName(name);
			if(foreignConstraint != null && !foreignConstraint.trim().isEmpty())
				foreignRange.setConstraint(foreignConstraint);
			result.add(foreignRange);
		}
		return result;
	}

	private boolean isTimeRange(String timeValue) {
		Matcher numericalRangeMatcher = numericalRange.matcher(timeValue);
		return numericalRangeMatcher.matches();
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

	protected String getCleanTime(String timeString) {
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

	protected String getCleanModifier(String modifier) {
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


	private String normalize(String text) {
		if(text == null)
			return text;
		text = text.replaceAll("–", "-");
		if(text.matches("^.*[,\\.;]$"))
			text = text.substring(0, text.length() - 1);

		if(text.matches("^[,\\.;].*$"))
			text = text.substring(1);
		return text.trim();
	}

}
