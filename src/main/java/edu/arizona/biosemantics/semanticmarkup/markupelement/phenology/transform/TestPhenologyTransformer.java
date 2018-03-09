package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Phenology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Treatment;

public class TestPhenologyTransformer {

	public static void main(String[] args) {

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);

		String[] examples = {
				"Flowering and fruiting mid Jun–mid Sep.",
				"Flowering late winter-early spring, fruiting mid summer.",
				"Flowering spring-late summer (Apr-Aug); fruiting 2-5 months after flowering.", //hold back for complicated; currently doing some better than existing
				"Flowering early Jun-early Aug, fruiting Jul-early Sep.",
				"Flowering Mar-Sep(-Dec in south, fruiting shortly after).", //hold back for complicated; existing not better
				"Flowering when leafless at end of dry season, fruiting as leaves emerge.", //hold back for complicates; doing same as old
				"Flowering Jun-Sep, fruiting Jul-Oct.",
				"Spores 15-24 µm, mature Jun-Sep.",
				"Spores mature Oct-Mar.",
				"Sporophytes mature spring-summer (May, Jun, Jul).", //different in modifier sense with old but i think as good/bad as
				"Sporophytes infrequent, capsules mature Jun-Aug.",
				"Sporulating summer--fall.",
				"Leaves green over winter, sporophores seasonal, new leaves appearing in late spring--early summer.",
				"Sporocarps produced spring--fall (Apr--Oct).", //get the stage to display verb right, produced -> appearing; sporulating -> mature
				"Seeds maturing late summer--early fall.",
				"Leaves appearing in spring, dying in late summer.",
				"Flowering early summer-fall; staminate plants generally dying after anthesis, pistillate plants remaining dark green, persisting until frost.",
				"Leaves appearing in late spring or early summer, releasing spores later than most associated species, and dying as late as October."
				/* */
		};

		String lyPattern = "[a-z]{3,}ly";
		String stopWords = "a|about|above|across|after|along|also|although|amp|an|and|are|as|at|be|because|become|becomes|becoming|been|before|being|"
				+ "beneath|between|beyond|but|by|ca|can|could|did|do|does|doing|done|for|from|had|has|have|hence|here|how|however|if|in|into|inside|inward|is|it|its|"
				+ "may|might|more|most|near|of|off|on|onto|or|out|outside|outward|over|should|so|than|that|the|then|there|these|this|those|throughout|"
				+ "to|toward|towards|up|upward|was|were|what|when|where|which|why|with|within|without|would";
		String advModifiers = "at least|at first|at times|almost|always|never|not|often|quite|rather|sometimes|somewhat";
		IPhenologyTransformer transformer = new ExtractorBasedPhenologyTransformer(advModifiers, lyPattern, stopWords);
		//IPhenologyTransformer transformer = new PhenologyTransformer(advModifiers, lyPattern, stopWords);

		PhenologiesFile phenologiesFile = new PhenologiesFile();
		List<Treatment> treatments = new ArrayList<Treatment>();
		Treatment treatment = new Treatment();
		List<Phenology> phenologies = new ArrayList<Phenology>();
		for(String example : examples) {
			Phenology phenology = new Phenology();
			phenology.setText(example);
			phenologies.add(phenology);
		}
		treatment.setPhenology(phenologies);
		treatments.add(treatment);
		phenologiesFile.setTreatments(treatments);
		transformer.transform(Arrays.asList(new PhenologiesFile[] { phenologiesFile }));
	}
}
