package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Elevation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Treatment;

public class TestElevationTransformer {

	public static void main(String[] args) {

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);

		String[] examples = {
				"0--700 m, rarely to 1600 m;",
				"below 10 m;",

				//works
				/*"0--700 m;",
				"200--800(--1600 m);",
				"0--100[--1500] m;",
				"(300--)800--2000 m;",
				"(-30–)200–1000(–1300) m;",
				"(40–)100–600(–1300) m;",
				"[0-]800-1600[-2500] m;",
				"[30-]600-1000[-1500] m;",
				"(30-)600-1000(-1500) m;",
				"[30-]600-1000(-1500) m;",
				"introduced; 10–300(–1000+) m",
				"mostly 0--1000 m;",
				"mostly low to moderate elevations (0-100 m);",
				"low to moderate elevations (10-1500 m);",
				"low to moderate elevations;",
				"0[--1800] m;",
				"mostly low elevations (0-100 m);",
				"moderate elevations;",
				"1800--2500 m (-- 4000 m, Central America);",
				"0--600 m (c Texas eastward and northward), 1600--2000 m (west)"*/

		};

		String modifierList = "(.*?\\b)(\\w+ly\\s+(?:to|or)\\s+\\w+ly)(\\b.*)";
		String lyPattern = "[a-z]{3,}ly";
		String stopWords = "a|about|above|across|after|along|also|although|amp|an|and|are|as|at|be|because|become|becomes|becoming|been|before|being|"
				+ "beneath|between|beyond|but|by|ca|can|could|did|do|does|doing|done|for|from|had|has|have|hence|here|how|however|if|in|into|inside|inward|is|it|its|"
				+ "may|might|more|most|near|of|off|on|onto|or|out|outside|outward|over|should|so|than|that|the|then|there|these|this|those|throughout|"
				+ "to|toward|towards|up|upward|was|were|what|when|where|which|why|with|within|without|would";
		String advModifiers = "at least|at first|at times|almost|always|never|not|often|quite|rather|sometimes|somewhat";

		String units= "(?:(?:pm|cm|mm|dm|km|ft|m|meters|meter|micro_m|micro-m|microns|micron|unes|µm|μm|um|centimeters|centimeter|millimeters|millimeter|transdiameters|transdiameter)[23]?)"; //squared or cubed
		IElevationTransformer transformer = new MyElevationTransformer(lyPattern, stopWords, modifierList, advModifiers, units);

		ElevationsFile elevationsFile = new ElevationsFile();
		List<Treatment> treatments = new ArrayList<Treatment>();
		Treatment treatment = new Treatment();
		List<Elevation> elevations = new ArrayList<Elevation>();
		for(String example : examples) {
			Elevation elevation = new Elevation();
			elevation.setText(example);
			elevations.add(elevation);
		}
		treatment.setElevations(elevations);
		treatments.add(treatment);
		elevationsFile.setTreatments(treatments);
		transformer.transform(Arrays.asList(new ElevationsFile[] { elevationsFile }));
	}
}
