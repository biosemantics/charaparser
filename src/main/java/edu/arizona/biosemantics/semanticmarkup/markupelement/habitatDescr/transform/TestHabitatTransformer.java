package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.ling.know.lib.InMemoryGlossary;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.oto.client.oto.OTOClient;
import edu.arizona.biosemantics.oto2.oto.server.rest.client.Client;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Habitat;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Treatment;

public class TestHabitatTransformer {

	public static void main(String[] args) throws Exception {

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);

		String[] examples = {
				"Moist to wet, calcareous habitats, swamps, meadows, pastures, open woods, or rarely on rock;"
				//"Terrestrial or less commonly epiphytic in forested, relatively wet habitats, e.g., swamps, but occasionally thickets, roadsides, or clearings;",
				/*"Shaded, usually moss-covered boulders and ledges, usually on limestone or other basic rocks, but occasionally on sandstone or other acidic rocks, rarely on fallen tree trunks;",
				"Chaparral and foothill woodland, often on serpentine;",

				"Margins of lakes, ponds, and streams, tidal shores and estuaries;",
				"Lightly shaded limestone outcrops of the Edwards Plateau;", //of phrase
				"Montane to subalpine forests of the Siskiyou Mountains;", //of phrase*/
				/*"In dark moist cavities and rock shelters in noncalcareous rocks. Occasionally epiphytic on tree bases in narrow ravines;",


				"Terrestrial or epiphytic in forested, often moist habitats, e.g., swamps, hammocks, or relatively open, disturbed habitats;",
				"New growth produced in spring, dying by late summer. Sheltered calcareous cliff crevices and rock ledges, typically in coniferous forest or other boreal habitats;",
				"Dry sandstone crevices, sandy soil or clay soil;",
				"Terrestrial or on rock in limestone hammocks;",

				//"Cracks and ledges on rock outcrops, on a variety of substrates including granite and dolomite;",

				//in on of phrases
				/*"Hammocks, on limestone rock faces;", //on phrase but with puncutation; //, on limestone rock faces, either extra character or as constraint of hammocks.
				"Epiphytic in tropical hammocks;", //in phrase. Epiphytic is a NNP/NP
				"Montane to subalpine forests of the Siskiyou Mountains;", //of phrase
				"Lightly shaded limestone outcrops of the Edwards Plateau;", //of phrase

				//punctuation
				"Aquatic to semiaquatic, lakes and ponds;", // good except punctu

				//duplication
				"Dry sandstone crevices, sandy soil or clay soil;", //duplication; weakend NNS, NNP and NN; get clay clay soil and sandy sandy soil somehow
				"Dry, rocky soil and slopes;", //good except duplication of rocky and dry

				//unfixable until further thought
				"Pinyon-juniper woodland, foothills, mesas, tablelands;", // tablelands accidentally as V

				"Hammocks in shade near streams;",
				"Terrestrial in sandy borrow pits, ditches, lakeshore swales, and conifer swamps, rarely on acidic, igneous rock or calcareous coast cliffs;",
				"Cliffs and rocky slopes, on various substrates but rarely observed on limestone;",

				"Relatively dry, usually sandy soils, old fields, pastures, usually disturbed;",
				"In woods;",
				"Sandy-bottomed ditches;",
				"Outer coastal plain in alkaline lakes, ponds, warm springs, pools in marshes, sloughs, sluggish streams, ditches, and canals;",
				"Moist woods and streambanks;",
				"Shaded woods, subalpine ridges;",
				"Dry rocky places on open arctic and alpine slopes and shores, moist grassland depressions, and open aspen woods;",
				"Alluvial or shingly calcareous shores and talus;",
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
		OTOClient otoClient = new OTOClient("http://biosemantics.arizona.edu/OTO");
		IHabitatTransformer transformer = new MyHabitatTransformer("edu/stanford/nlp/models/lexparser/englishFactored.ser.gz",
				lyPattern, stopWords, modifierList, advModifiers, new InMemoryGlossary(), new GlossaryInitializer(otoClient, TaxonGroup.PLANT, null,
						null, new Client("http://localhost"), new IInflector() {
					@Override
					public String getSingular(String word) {
						return word;
					}
					@Override
					public String getPlural(String word) {
						return word;
					}
					@Override
					public boolean isPlural(String word) {
						return false;
					}
				}));

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
