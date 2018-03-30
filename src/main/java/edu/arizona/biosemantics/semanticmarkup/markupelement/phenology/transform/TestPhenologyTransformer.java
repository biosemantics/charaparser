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
				//"Sporulates summer",
				/*"Coning March--April.",
				"Coning late winter--early spring.",
				"Coning late winter--midspring (Mar--May).",
				"Coning late winter--midspring.",
				"Coning March--April.",
				"Sporulates essentially all year."*/
				//"Sporulation early spring--midsummer.",
				///"Sporulation late spring--early summer.",
				//"Sporulation early spring--midsummer.",
				//"Sporulating throughout the year.",

				"Spores mature in winter or spring.",

				/*
				"Sporulation spring--early summer (late summer, early winter in Florida).",
				typical spring - early summer
				atypical spring - late summer
				atypical spring - early winter (constraint florida)


				"Leaves appearing early-late spring, often with second flush later in season following summer rains.",
				typical early spring - late spring modifier="often with second flush later in season following summer rains"
				atypical value=later in season constraint "follower summer rains"

				typical early spring - late spring modifier="often with second flush later in season following summer rains"


				"Cones maturing in summer, or cones overwintering and shedding spores in spring.",
				cone maturing typical: summer
				spore producing: spring

				"Leaves usually drying up in midsummer together with other associated species.",
				leaves drying typical: midsummer constraint="together..."



				"Leaves appearing in early spring and dying in late spring and early summer, long before those of associated moonworts.",
				leave appearing: typical early spring
				leave dying: typical late spring
				leave dying: typical early summer

				constraint:  long before those of associated moonworts. could be attached anywhere nad it would be correct or absend.


				"Leaves seasonal, appearing in early spring and dying in late summer.",
				leave appearing: typical: early spring
				leave dying typical late summer

				"Leaves appearing mainly in late winter and early spring, sometimes also appearing later in season after heavy rains.",
				leaves apearing typicaL; modifier=mainly value late winter
				leaves apearing typicaL; modifier=mainly value early spring
				leaves apearing atypical; value=later in season after heavy rains. modifier="sometimes"


				"Leaves appearing in late fall and dying in early spring.",
				leave appearing: typical late fall
				leave dying: early spring



				"Leaves green over winter, arising at variable times during last half of summer, meiosis as late as September.",
				leaves appearing typical value="at variables times during last half of summer"
				leaves meiosis typical late as september
				(ideally: leaves not dying in winter)

				"Leaves green over winter, sporophores seasonal, new leaves appearing in spring.",
				leaves appearing: typical spring
				(ideally: leaves not dying in winter)


				"Leaves appearing in late winter and early spring; apparently absent during dry years.",
				leaves appearing typical: late winter  constraint="apparently absent during dry years"
				leaves appearing typical: early spring constraint="apparently absent during dry years"


				"Leaves green over winter, new leaves appearing in late spring.",
				leaves appearing: typical late spring


				"Leaves appearing in late spring or early summer, releasing spores later than most associated species, and dying as late as October.",
				leaves appearing: typical late spring
				leaves appearing: typical early summer
				spore appearing: value: later than most associated species
				leave dying: value as late as october

				"Cones maturing in late summer, or cones overwintering and shedding spores in spring",
				cone maturing time: late summer
				spore appearing: spring


				"Cones maturing in summer, old stems sometimes developing branches with cones in spring.",
				cone matruing time: summer
				cone appearing time; modifier sometimes; value=spring


				"Leaves green over winter, sporophores seasonal, new leaves appearing in late spring--early summer.",
				leave appearing: late spring-early summer


				"Leaves appearing in mid to late spring, dying in late summer; In extremely dry years of shorter duration or not appearing at all.",
				leave appearing value: mid to late spring constraint="in extremely dry years of shorter duration or not appearing at all."
				leave dying: late summer



				 */

				/*
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
