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

	@Inject
	public MyElevationTransformer(@Named("Units")String units) {
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

		String outlier = "\\(-?\\d+-?\\)";
		String foreign = "\\[-?\\d+-?\\]";
		String numericalRangeString = "(?:(?<fromOutlier>" + outlier + ")|(?<fromForeign>" + foreign + "))?(?<from>\\d+)"
				+ "-+"
				+ "(?<to>\\d+)(?:(?<toOutlier>" + outlier + ")|(?<toForeign>" + foreign + "))? (?<unit>" + units + ")";
		Pattern numericalRange = Pattern.compile(".*?" + numericalRangeString + ".*?");

		Matcher numericalRangeMatcher = numericalRange.matcher(text);
		if(numericalRangeMatcher.matches()){
			String from = numericalRangeMatcher.group("from");
			String to = numericalRangeMatcher.group("to");
			String fromOutlier = numericalRangeMatcher.group("fromOutlier");
			String fromForeign = numericalRangeMatcher.group("fromForeign");
			String toOutlier = numericalRangeMatcher.group("toOutlier");
			String toForeign = numericalRangeMatcher.group("toForeign");


			Pattern outlierPattern = Pattern.compile("\\(-?(?<outlier>\\d+)-?\\)");
			if(fromOutlier != null) {
				Matcher fromOutlierMatcher = outlierPattern.matcher(fromOutlier);
				if(fromOutlierMatcher.matches()) {
					fromOutlier = fromOutlierMatcher.group("outlier");
				}
			}
			if(toOutlier != null) {
				Matcher toOutlierMatcher = outlierPattern.matcher(toOutlier);
				if(toOutlierMatcher.matches()) {
					toOutlier = toOutlierMatcher.group("outlier");
				}
			}

			Pattern foreignPattern = Pattern.compile("\\[-?(?<foreign>\\d+)-?\\]");
			if(fromForeign != null) {
				Matcher fromForeignMatcher = foreignPattern.matcher(fromForeign);
				if(fromForeignMatcher.matches()) {
					fromForeign = fromForeignMatcher.group("foreign");
				}
			}
			if(toForeign != null) {
				Matcher toForeignMatcher = foreignPattern.matcher(toForeign);
				if(toForeignMatcher.matches()) {
					toForeign = toForeignMatcher.group("foreign");
				}
			}

			System.out.println("from outlier: " + fromOutlier);
			System.out.println("from foreign: " + fromForeign);
			System.out.println("from: " + from);
			System.out.println("to: " + to);
			System.out.println("to outlier: " + toOutlier);
			System.out.println("to foreign: " + toForeign);
			System.out.println("Matches");
		}

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