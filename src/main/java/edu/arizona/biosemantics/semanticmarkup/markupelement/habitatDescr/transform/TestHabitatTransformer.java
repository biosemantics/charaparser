package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Habitat;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Treatment;

public class TestHabitatTransformer {

	public static void main(String[] args) {

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);

		String[] examples = {
				"Relatively dry, usually sandy soils, old fields, pastures, usually disturbed;",
				"In woods;",
				"Sandy-bottomed ditches;",
				//"Outer coastal plain in alkaline lakes, ponds, warm springs, pools in marshes, sloughs, sluggish streams, ditches, and canals;",
				"Moist woods and streambanks;",
				"Shaded woods, subalpine ridges;",
				//"Dry rocky places on open arctic and alpine slopes and shores, moist grassland depressions, and open aspen woods;",
				//"Alluvial or shingly calcareous shores and talus;",
				"Moist, often slightly acidic and sandy soils, wooded floodplains, stream banks, borders of swamps, hammocks, roadsides;"
				/* */
		};

		String modifierList = "(.*?\\b)(\\w+ly\\s+(?:to|or)\\s+\\w+ly)(\\b.*)";
		String lyPattern = "[a-z]{3,}ly";
		String stopWords = "a|about|above|across|after|along|also|although|amp|an|and|are|as|at|be|because|become|becomes|becoming|been|before|being|"
				+ "beneath|between|beyond|but|by|ca|can|could|did|do|does|doing|done|for|from|had|has|have|hence|here|how|however|if|in|into|inside|inward|is|it|its|"
				+ "may|might|more|most|near|of|off|on|onto|or|out|outside|outward|over|should|so|than|that|the|then|there|these|this|those|throughout|"
				+ "to|toward|towards|up|upward|was|were|what|when|where|which|why|with|within|without|would";
		String advModifiers = "at least|at first|at times|almost|always|never|not|often|quite|rather|sometimes|somewhat";
		//IHabitatTransformer transformer = new HabitatTransformer("edu/stanford/nlp/models/lexparser/englishFactored.ser.gz",
		//lyPattern, stopWords, modifierList, advModifiers);
		IHabitatTransformer transformer = new MyHabitatTransformer("edu/stanford/nlp/models/lexparser/englishFactored.ser.gz",
				lyPattern, stopWords, modifierList, advModifiers);

		HabitatsFile habitatsFile = new HabitatsFile();
		List<Treatment> treatments = new ArrayList<Treatment>();
		Treatment treatment = new Treatment();
		List<Habitat> habitats = new ArrayList<Habitat>();
		for(String example : examples) {
			Habitat habitat = new Habitat();
			habitat.setText(example);
			habitats.add(habitat);
		}
		treatment.setHabitats(habitats);
		treatments.add(treatment);
		habitatsFile.setTreatments(treatments);
		transformer.transform(Arrays.asList(new HabitatsFile[] { habitatsFile }));
	}
}
