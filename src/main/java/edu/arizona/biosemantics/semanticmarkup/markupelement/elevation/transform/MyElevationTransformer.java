package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.transform;

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
		LinkedHashSet<Character> result = new LinkedHashSet<Character>();

		text = normalize(text);
		System.out.println();
		System.out.println();
		System.out.println(text);

		String outlier = "\\(-*\\s*\\d+\\+?\\s*-*\\s*(" + units + ")\\s*.*\\)";
		String foreign = "\\[-*\\s*\\d+\\+?\\s*-*\\s*(" + units + ")\\s*.*\\]";
		String numericalRangeString = "(?<prefix>.*?)\\s*(?:(?<fromOutlier>" + outlier + ")|(?<fromForeign>" + foreign + "))?\\s*(?<from>\\d+)"
				+ "\\s*-+\\s*"
				+ "(?<to>\\d+)\\s*(?<unit1>" + units + ")\\s*(?:(?<toOutlier>" + outlier + ")|(?<toForeign>" + foreign + "))?\\s*(?<unit>" + units + ")?\\s*(?<postfix>.*?)";
		Pattern numericalRange = Pattern.compile(".*?" + numericalRangeString + ".*?");

		Matcher numericalRangeMatcher = numericalRange.matcher(text);
		if(numericalRangeMatcher.matches()){
			String from = numericalRangeMatcher.group("from");
			String to = numericalRangeMatcher.group("to");
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

			Pattern fromOutlierPattern = Pattern.compile("\\(.*?(?<outlier>-?\\d+\\+?)\\s*-*\\s*(?<unit>" + units + ")\\s*(?<constraint>.*)\\)");
			Pattern toOutlierPattern = Pattern.compile("\\(-*\\s*(?<outlier>\\d+\\+?)\\s*-*\\s*(?<unit>" + units + ")\\s*(?<constraint>.*)\\)");
			if(fromOutlier != null) {
				Matcher fromOutlierMatcher = fromOutlierPattern.matcher(fromOutlier);
				if(fromOutlierMatcher.matches()) {
					fromOutlier = fromOutlierMatcher.group("outlier");
					fromOutlierUnit = fromOutlierMatcher.group("unit");
					fromOutlierConstraint = fromOutlierMatcher.group("constraint");
				}
			}
			if(toOutlier != null) {
				Matcher toOutlierMatcher = toOutlierPattern.matcher(toOutlier);
				if(toOutlierMatcher.matches()) {
					toOutlier = toOutlierMatcher.group("outlier");
					toOutlierUnit = toOutlierMatcher.group("unit");
					toOutlierConstraint = toOutlierMatcher.group("constraint");
				}
			}

			Pattern fromForeignPattern = Pattern.compile("\\[.*?(?<foreign>-?\\d+\\+?)\\s*-*\\s*(?<unit>" + units + ")\\s*(?<constraint>.*)\\]");
			Pattern toForeignPattern = Pattern.compile("\\[-*\\s*(?<foreign>\\d+\\+?)\\s*-*\\s*(?<unit>" + units + ")\\s*(?<constraint>.*)\\]");
			if(fromForeign != null) {
				Matcher fromForeignMatcher = fromForeignPattern.matcher(fromForeign);
				if(fromForeignMatcher.matches()) {
					fromForeign = fromForeignMatcher.group("foreign");
					fromForeignUnit = fromForeignMatcher.group("unit");
					fromForeignConstraint = fromForeignMatcher.group("constraint");
				}
			}
			if(toForeign != null) {
				Matcher toForeignMatcher = toForeignPattern.matcher(toForeign);
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
			if(prefix != null && !prefix.trim().isEmpty()) {
				String[] parts = prefix.split("\\s+");
				for(String part : parts) {
					if(part.matches(this.lyAdvPattern) || part.matches(this.modifierList) || part.matches(this.advModifiers)) {
						modifier += " " + part;
					}
				}
			}

			/*System.out.println("from outlier: " + fromOutlier);
			System.out.println("from foreign: " + fromForeign);
			System.out.println("from: " + from);
			System.out.println("to: " + to);
			System.out.println("to outlier: " + toOutlier);
			System.out.println("to foreign: " + toForeign);
			System.out.println("Matches");*/

			unit = (unit == null ? (fromOutlierUnit == null ? (toOutlierUnit == null ? (fromForeignUnit == null ? (toForeignUnit == null ?
					"" : toForeignUnit) : fromForeignUnit) : toOutlierUnit) : fromOutlierUnit) : unit);

			Character range = new Character();
			range.setCharType("range_value");
			range.setFrom(from);
			range.setTo(to);
			range.setName("elevation");
			range.setToUnit(unit);
			range.setFromUnit(unit);
			if(modifier != null && !modifier.trim().isEmpty())
				range.setModifier(modifier.trim());
			result.add(range);

			if(fromOutlier != null && toOutlier != null) {
				Character atypicalRange = new Character();
				atypicalRange.setCharType("atypical_range");
				atypicalRange.setFrom(fromOutlier);
				atypicalRange.setTo(toOutlier);
				atypicalRange.setName("elevation");
				atypicalRange.setToUnit(unit);
				atypicalRange.setFromUnit(unit);
				atypicalRange.setConstraint(outlierConstraint);
				result.add(atypicalRange);
			} else if(fromOutlier != null) {
				Character atypicalRange = new Character();
				atypicalRange.setCharType("atypical_range");
				atypicalRange.setFrom(fromOutlier);
				atypicalRange.setTo(to);
				atypicalRange.setName("elevation");
				atypicalRange.setToUnit(unit);
				atypicalRange.setFromUnit(unit);
				atypicalRange.setConstraint(outlierConstraint);
				result.add(atypicalRange);
			} else if(toOutlier != null) {
				Character atypicalRange = new Character();
				atypicalRange.setCharType("atypical_range");
				atypicalRange.setFrom(from);
				atypicalRange.setTo(toOutlier);
				atypicalRange.setName("elevation");
				atypicalRange.setToUnit(unit);
				atypicalRange.setFromUnit(unit);
				atypicalRange.setConstraint(outlierConstraint);
				result.add(atypicalRange);
			}

			if(fromForeign != null && toForeign != null) {
				Character foreignRange = new Character();
				foreignRange.setCharType("foreign_range");
				foreignRange.setFrom(fromForeign);
				foreignRange.setTo(toForeign);
				foreignRange.setName("elevation");
				foreignRange.setToUnit(unit);
				foreignRange.setFromUnit(unit);
				foreignRange.setConstraint(foreignConstraint);
				result.add(foreignRange);
			} else if(fromForeign != null) {
				Character foreignRange = new Character();
				foreignRange.setCharType("foreign_range");
				foreignRange.setFrom(fromForeign);
				foreignRange.setTo(to);
				foreignRange.setName("elevation");
				foreignRange.setToUnit(unit);
				foreignRange.setFromUnit(unit);
				foreignRange.setConstraint(foreignConstraint);
				result.add(foreignRange);
			} else if(toForeign != null) {
				Character foreignRange = new Character();
				foreignRange.setCharType("foreign_range");
				foreignRange.setFrom(from);
				foreignRange.setTo(toForeign);
				foreignRange.setName("elevation");
				foreignRange.setToUnit(unit);
				foreignRange.setFromUnit(unit);
				foreignRange.setConstraint(foreignConstraint);
				result.add(foreignRange);
			}
		}

		System.out.println(result);
		return result;
	}

	private String normalize(String text) {
		text = text.replaceAll("–", "-");
		if(text.matches("^.*\\p{Punct}$"))
			text = text.substring(0, text.length() - 1);
		return text;
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