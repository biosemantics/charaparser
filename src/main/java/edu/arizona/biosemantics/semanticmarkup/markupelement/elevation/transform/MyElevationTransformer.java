package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Elevation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Treatment;

public class MyElevationTransformer implements IElevationTransformer {

	private String units;
	private String advModifiers;
	private String modifierList;
	private String stopwords;
	private String lyAdvPattern;

	@Inject
	public MyElevationTransformer(@Named("LyAdverbpattern") String lyAdvPattern, @Named("StopWordString") String stopwords,
			@Named("ModifierList") String modifierList, @Named("AdvModifiers") String advModifiers, @Named("Units")String units) {
		this.lyAdvPattern = lyAdvPattern;
		this.stopwords = stopwords;
		this.modifierList = modifierList;
		this.advModifiers = advModifiers;
		this.units = units;
	}

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

	public LinkedHashSet<Character> parse(String text) {
		List<Character> result = new ArrayList<Character>();

		text = normalize(text);
		System.out.println();
		System.out.println();
		System.out.println(text);

		List<String> items = splitTextIntoItems(text);
		for(String item : items) {
			result.addAll(parseItem(item, result.isEmpty() ? null : result.get(result.size() - 1)));
		}
		return new LinkedHashSet<Character>(result);
	}

	private List<String> splitTextIntoItems(String text) {
		List<String> result = new ArrayList<String>();

		String item = "";

		int openingRound = 0;
		int openingSquare = 0;
		for(int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			item += c;

			if(c == '(') {
				openingRound++;
			}
			if(c == '[') {
				openingSquare++;
			}
			if(c == ')') {
				openingRound--;
			}
			if(c == ']') {
				openingSquare--;
			}

			if(c == '.' || c == ';' || c == ',') {
				if(openingSquare == 0 && openingRound == 0) {
					result.add(item);
					item = "";
				}
			}
		}

		if(!item.trim().isEmpty())
			result.add(item);
		return result;
	}

	private Collection<? extends Character> parseItem(String text, Character previousCharacter) {
		LinkedHashSet<Character> result = new LinkedHashSet<Character>();

		String fromOutlierPattern = "\\((-*|(\\bfrom\\b)?)\\s*\\d+\\+?\\s*-*\\s*(" + units + ")?\\s*.*\\)";
		String fromForeignPattern = "\\[(-*|(\\bfrom\\b)?)\\s*\\d+\\+?\\s*-*\\s*(" + units + ")?\\s*.*\\]";

		String toOutlierPattern = "\\((-*|(\\bto\\b)?)\\s*\\d+\\+?\\s*-*\\s*(" + units + ")?\\s*.*\\)";
		String toForeignPattern = "\\[(-*|(\\bto\\b)?)\\s*\\d+\\+?\\s*-*\\s*(" + units + ")?\\s*.*\\]";
		String numericalRangeString = "(?<prefix>.*?)\\s*(?:(?<fromOutlier>" + fromOutlierPattern + ")|(?<fromForeign>" + fromForeignPattern + "))?\\s*"
				+ "(:?(?<from>\\d+)\\s*-+\\s*(?<to>\\d+)|(?<singleDataPoint>\\d+))\\s*(?<unit1>" + units + ")?"
				+ "\\s*(?:(?<toOutlier>" + toOutlierPattern + ")|(?<toForeign>" + toForeignPattern + "))?\\s*"
				+ "(?<unit>" + units + ")?\\s*(?<postfix>.*?)";
		Pattern numericalRange = Pattern.compile(numericalRangeString);

		Matcher numericalRangeMatcher = numericalRange.matcher(text);
		if(numericalRangeMatcher.matches()){
			String from = numericalRangeMatcher.group("from");
			String to = numericalRangeMatcher.group("to");
			String singleDataPoint = numericalRangeMatcher.group("singleDataPoint");
			String unit = numericalRangeMatcher.group("unit");
			if(unit == null)
				unit = numericalRangeMatcher.group("unit1");
			String fromOutlier = numericalRangeMatcher.group("fromOutlier");
			String fromForeign = numericalRangeMatcher.group("fromForeign");
			String toOutlier = numericalRangeMatcher.group("toOutlier");
			String toForeign = numericalRangeMatcher.group("toForeign");
			String fromOutlierUnit = null;
			String fromOutlierConstraint = null;
			String fromForeignUnit = null;
			String fromForeignConstraint = null;
			String toOutlierUnit = null;
			String toOutlierConstraint = null;
			String toForeignUnit = null;
			String toForeignConstraint = null;
			String prefix = numericalRangeMatcher.group("prefix");
			String postfix = numericalRangeMatcher.group("postfix");

			Pattern fromOutlierExtractPattern = Pattern.compile("\\(.*?(?<outlier>-?\\d+\\+?)\\s*-*\\s*(?<unit>" + units + ")?\\s*(?<constraint>.*)\\)");
			Pattern toOutlierExtractPattern = Pattern.compile("\\((-*|(\\bto\\b)?)\\s*(?<outlier>\\d+\\+?)\\s*-*\\s*(?<unit>" + units + ")?\\s*(?<constraint>.*)\\)");
			if(fromOutlier != null) {
				Matcher fromOutlierMatcher = fromOutlierExtractPattern.matcher(fromOutlier);
				if(fromOutlierMatcher.matches()) {
					fromOutlier = fromOutlierMatcher.group("outlier");
					fromOutlierUnit = fromOutlierMatcher.group("unit");
					fromOutlierConstraint = fromOutlierMatcher.group("constraint");

					try {
						int fromOutlierInt = Integer.valueOf(fromOutlier);
						int toInt = Integer.valueOf(to);
						int fromInt = Integer.valueOf(from);
						if(fromOutlierInt > toInt && (fromOutlierUnit == null || fromOutlierUnit.equalsIgnoreCase(unit))) {
							toOutlier = fromOutlier;
							toOutlierUnit = fromOutlierUnit;
							toOutlierConstraint = fromOutlierConstraint;
							fromOutlier = null;
							fromOutlierUnit = null;
							fromOutlierConstraint = null;
						}
					} catch(Exception e) {}
				}
			}
			if(toOutlier != null) {
				Matcher toOutlierMatcher = toOutlierExtractPattern.matcher(toOutlier);
				if(toOutlierMatcher.matches()) {
					toOutlier = toOutlierMatcher.group("outlier");
					toOutlierUnit = toOutlierMatcher.group("unit");
					toOutlierConstraint = toOutlierMatcher.group("constraint");

					try {
						int toOutlierInt = Integer.valueOf(toOutlier);
						int fromInt = Integer.valueOf(from);
						if(toOutlierInt < fromInt && (toOutlierUnit == null || toOutlierUnit.equalsIgnoreCase(unit))) {
							fromOutlier = toOutlier;
							fromOutlierUnit = toOutlierUnit;
							fromOutlierConstraint = toOutlierConstraint;
							toOutlier = null;
							toOutlierUnit = null;
							toOutlierConstraint = null;
						}
					} catch(Exception e) {}
				}
			}

			Pattern fromForeignExtractPattern = Pattern.compile("\\[.*?(?<foreign>-?\\d+\\+?)\\s*-*\\s*(?<unit>" + units + ")?\\s*(?<constraint>.*)\\]");
			Pattern toForeignExtractPattern = Pattern.compile("\\[(-*|(\\bto\\b)?)\\s*(?<foreign>\\d+\\+?)\\s*-*\\s*(?<unit>" + units + ")?\\s*(?<constraint>.*)\\]");
			if(fromForeign != null) {
				Matcher fromForeignMatcher = fromForeignExtractPattern.matcher(fromForeign);
				if(fromForeignMatcher.matches()) {
					fromForeign = fromForeignMatcher.group("foreign");
					fromForeignUnit = fromForeignMatcher.group("unit");
					fromForeignConstraint = fromForeignMatcher.group("constraint");
				}
			}
			if(toForeign != null) {
				Matcher toForeignMatcher = toForeignExtractPattern.matcher(toForeign);
				if(toForeignMatcher.matches()) {
					toForeign = toForeignMatcher.group("foreign");
					toForeignUnit = toForeignMatcher.group("unit");
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


			String modifier = "";

			List<Character> informalElevation = getInformalElevation(prefix);
			boolean prefixModifier = false;
			if(informalElevation.isEmpty()) {
				if(prefix != null && !prefix.trim().isEmpty()) {
					String[] parts = prefix.split("\\s+");
					for(String part : parts) {
						if(part.matches(this.lyAdvPattern) || part.matches(this.modifierList) || part.matches(this.advModifiers)) {
							modifier += " " + part;
							prefixModifier = true;
						}
					}
				}
			} else {
				result.addAll(informalElevation);
			}

			String constraint = "";
			boolean postFixConstraint = false;
			if(postfix != null && !postfix.trim().isEmpty()) {
				postfix = normalize(postfix);
				Pattern postfixPattern = Pattern.compile("^\\s*\\((?<constraint>.*)\\)\\s*$");
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

			unit = (unit == null ? (fromOutlierUnit == null ? (toOutlierUnit == null ? (fromForeignUnit == null ? (toForeignUnit == null ?
					"" : toForeignUnit) : fromForeignUnit) : toOutlierUnit) : fromOutlierUnit) : unit);

			if(from == null && to == null && singleDataPoint != null) {
				if(text.matches(".*\\bto\\b\\s+\\d+.*")) {
					to = singleDataPoint;
					from = previousCharacter.getFrom();
				}
				else if(text.matches(".*\\bfrom\\b\\s+\\d.*")) {
					from = singleDataPoint;
					to = previousCharacter.getTo();
				} else {
					if(prefix.matches(".*\\b(below|under)\\b.*")) {
						to = singleDataPoint;
						from = "any";
					} else if(prefix.matches(".*\\b(above|over)\\b.*")) {
						from = singleDataPoint;
						to = "any";
					} else {
						from = singleDataPoint;
						to = singleDataPoint;
					}
				}
			}

			result.addAll(this.createCharactersForRange(
					from, to, fromOutlier, fromForeign, toOutlier, toForeign, unit, modifier, constraint, outlierConstraint, foreignConstraint));
		} else {
			result.addAll(this.getInformalElevation(text));
		}

		System.out.println(result);
		return result;
	}

	private List<Character> getInformalElevation(String text) {
		List<Character> result = new ArrayList<Character>();
		text = normalize(text);
		System.out.println("informal: " + text);

		String dataPoint = "\\b(low|moderate|high|lowland)\\b";
		String informalRangeString = "(?<prefix>.*?)\\s*(?:(?<fromOutlier>" + dataPoint + ")|(?<fromForeign>" + dataPoint + "))?\\s*"
				+ "(:?(?<from>" + dataPoint + ")\\s*(:?-+|to)\\s*(?<to>" + dataPoint + ")|(?<singleDataPoint>" + dataPoint + "))"
				+ "\\s*(?:(?<toOutlier>" + dataPoint + ")|(?<toForeign>" + dataPoint + "))?\\s*(?<postfix>.*?)";
		Pattern informalRange = Pattern.compile(informalRangeString);

		Matcher informalRangeMatcher = informalRange.matcher(text);
		if(informalRangeMatcher.matches()) {
			String from = informalRangeMatcher.group("from");
			String to = informalRangeMatcher.group("to");
			String singleDataPoint = informalRangeMatcher.group("singleDataPoint");
			String fromOutlier = informalRangeMatcher.group("fromOutlier");
			String fromForeign = informalRangeMatcher.group("fromForeign");
			String toOutlier = informalRangeMatcher.group("toOutlier");
			String toForeign = informalRangeMatcher.group("toForeign");
			String prefix = informalRangeMatcher.group("prefix");
			String postfix = informalRangeMatcher.group("postfix");

			if(from == null && to == null && singleDataPoint != null) {
				from = singleDataPoint;
				to = singleDataPoint;
			}

			String modifier = "";
			if(prefix != null && !prefix.trim().isEmpty()) {
				String[] parts = prefix.split("\\s+");
				for(String part : parts) {
					if(part.matches(this.lyAdvPattern) || part.matches(this.modifierList) || part.matches(this.advModifiers)) {
						modifier += " " + part;
					}
				}
			}

			String constraint = "";
			if(postfix != null && !postfix.trim().isEmpty()) {
				Pattern postfixPattern = Pattern.compile("^\\s*\\((?<constraint>.*)\\)\\s*$");
				Matcher postfixMatcher = postfixPattern.matcher(postfix);
				if(postfixMatcher.matches()) {
					constraint = postfixMatcher.group("constraint");
				}
			}

			result.addAll(createCharactersForRange(from, to, fromOutlier, fromForeign, toOutlier, toForeign, null, modifier, constraint, null, null));
		}
		return result;
	}

	private List<Character> createCharactersForRange(String from, String to,
			String fromOutlier, String fromForeign, String toOutlier,
			String toForeign, String unit, String modifier, String constraint, String outlierConstraint, String foreignConstraint) {
		List<Character> result = new ArrayList<Character>();

		outlierConstraint = normalize(outlierConstraint);

		Character range = new Character();
		range.setCharType("range_value");
		range.setFrom(from);
		range.setTo(to);
		range.setName("elevation");
		if(unit != null) {
			range.setToUnit(unit);
			range.setFromUnit(unit);
		}
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
			atypicalRange.setName("elevation");
			if(unit != null) {
				atypicalRange.setToUnit(unit);
				atypicalRange.setFromUnit(unit);
			}
			if(outlierConstraint != null && !outlierConstraint.trim().isEmpty())
				atypicalRange.setConstraint(outlierConstraint);
			result.add(atypicalRange);
		} else if(fromOutlier != null) {
			Character atypicalRange = new Character();
			atypicalRange.setCharType("atypical_range");
			atypicalRange.setFrom(fromOutlier);
			atypicalRange.setTo(to);
			atypicalRange.setName("elevation");
			if(unit != null) {
				atypicalRange.setToUnit(unit);
				atypicalRange.setFromUnit(unit);
			}
			if(outlierConstraint != null && !outlierConstraint.trim().isEmpty())
				atypicalRange.setConstraint(outlierConstraint);
			result.add(atypicalRange);
		} else if(toOutlier != null) {
			Character atypicalRange = new Character();
			atypicalRange.setCharType("atypical_range");
			atypicalRange.setFrom(from);
			atypicalRange.setTo(toOutlier);
			atypicalRange.setName("elevation");
			if(unit != null) {
				atypicalRange.setToUnit(unit);
				atypicalRange.setFromUnit(unit);
			}
			if(outlierConstraint != null && !outlierConstraint.trim().isEmpty())
				atypicalRange.setConstraint(outlierConstraint);
			result.add(atypicalRange);
		}

		if(fromForeign != null && toForeign != null) {
			Character foreignRange = new Character();
			foreignRange.setCharType("foreign_range");
			foreignRange.setFrom(fromForeign);
			foreignRange.setTo(toForeign);
			foreignRange.setName("elevation");
			if(unit != null) {
				foreignRange.setToUnit(unit);
				foreignRange.setFromUnit(unit);
			}
			if(foreignConstraint != null && !foreignConstraint.trim().isEmpty())
				foreignRange.setConstraint(foreignConstraint);
			result.add(foreignRange);
		} else if(fromForeign != null) {
			Character foreignRange = new Character();
			foreignRange.setCharType("foreign_range");
			foreignRange.setFrom(fromForeign);
			foreignRange.setTo(to);
			foreignRange.setName("elevation");
			if(unit != null) {
				foreignRange.setToUnit(unit);
				foreignRange.setFromUnit(unit);
			}
			if(foreignConstraint != null && !foreignConstraint.trim().isEmpty())
				foreignRange.setConstraint(foreignConstraint);
			result.add(foreignRange);
		} else if(toForeign != null) {
			Character foreignRange = new Character();
			foreignRange.setCharType("foreign_range");
			foreignRange.setFrom(from);
			foreignRange.setTo(toForeign);
			foreignRange.setName("elevation");
			if(unit != null) {
				foreignRange.setToUnit(unit);
				foreignRange.setFromUnit(unit);
			}
			if(foreignConstraint != null && !foreignConstraint.trim().isEmpty())
				foreignRange.setConstraint(foreignConstraint);
			result.add(foreignRange);
		}
		return result;
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

	public static void main(String[] args) {
		String units = "m";
		String numericalRangeString = "([\\d\\(\\)\\[\\]\\+\\-]*\\d+[\\d\\(\\)\\[\\]\\+\\-]*)-([\\d\\(\\)\\[\\]\\+\\-]*\\d+[\\d\\(\\)\\[\\]\\+\\-]*) ("+units+")\\b";
		Pattern numericalRange = Pattern.compile("([\\d\\(\\)\\[\\]\\+\\-]*\\d[\\d\\(\\)\\[\\]\\+\\-]*)-([\\d\\(\\)\\[\\]\\+\\-]*\\d[\\d\\(\\)\\[\\]\\+\\-]*) ("+units+")\\b");

		String nonWorkingHyphens = "(-30–)200-1000(–1300) m";
		String workingHyphen = "(-30-)200-1000(-1300) m";
		System.out.println(workingHyphen.matches(numericalRangeString));
	}
}