package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.PhenologiesFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Phenology;
import edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.model.Treatment;

public class ExtractorBasedPhenologyTransformer implements IPhenologyTransformer {

	private final String timeTermsPattern =
			"jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|january|feburary|march|april|june|july|august|september|october|november|december|spring|summer|fall|autumn|winter|midspring|"
					+ "midsummer|midwinter|midfall|midautumn|year[_-]round|\\w+ periods";
	private final String timeModifierPattern = "latter|late|last|early|mid|middle";

	private ArrayList<PhenologyInfoExtractor> extractors;

	@Inject
	public ExtractorBasedPhenologyTransformer(@Named("AdvModifiers") String advModifiers,
			@Named("LyAdverbpattern") String lyAdvPattern, @Named("StopWordString") String stopwords){
		Pattern advModPattern = Pattern.compile( "("+advModifiers+"|"+lyAdvPattern+"|ca.)");

		this.extractors = new ArrayList<PhenologyInfoExtractor>();
		/** appearing extractors **/
		String[] flowerEntityTerms = new String[] { "flowers", "flower" };
		String[] fruitEntityTerms = new String[] { "fruits", "fruit" };
		String[] sporeEntityTerms = new String[] { "spores", "spore", "sporulate", "sporulates", "sporulation", "sporocarps", "sporophyte", "sporophytes",
				"sporophylls", "sporophyll"};
		String[] coneEntityTerms = new String[] { "cones", "cone" };
		String[] capsuleEntityTerms = new String[] { "capsules", "capsule" };
		String[] leafEntityTerms = new String[] { "leaves", "leaf" };
		String[] seedEntityTerms = new String[] { "seeds", "seed" };
		String[] appearingStageTerms = new String[] { "appearing", "arsing", "arise", "arises", "occuring", "occur", "occurs", "appear", "appears", "produced" };
		extractors.add(new PhenologyInfoExtractor("flowering",
				Arrays.asList(flowerEntityTerms),
				Arrays.asList(appearingStageTerms),
				false, advModPattern, stopwords));
		extractors.add(new PhenologyInfoExtractor("fruiting",
				Arrays.asList(fruitEntityTerms),
				Arrays.asList(appearingStageTerms),
				false, advModPattern, stopwords));
		extractors.add(new PhenologyInfoExtractor("sporulating",
				Arrays.asList(sporeEntityTerms),
				Arrays.asList(appearingStageTerms),
				false, advModPattern, stopwords));
		extractors.add(new PhenologyInfoExtractor(null,
				Arrays.asList(coneEntityTerms),
				Arrays.asList(appearingStageTerms),
				false, advModPattern, stopwords));
		extractors.add(new PhenologyInfoExtractor(null,
				Arrays.asList(capsuleEntityTerms),
				Arrays.asList(appearingStageTerms),
				false, advModPattern, stopwords));
		extractors.add(new PhenologyInfoExtractor(null,
				Arrays.asList(leafEntityTerms),
				Arrays.asList(appearingStageTerms),
				false, advModPattern, stopwords));
		extractors.add(new PhenologyInfoExtractor("seeding",
				Arrays.asList(seedEntityTerms),
				Arrays.asList(appearingStageTerms),
				false, advModPattern, stopwords));

		List<List<String>> stages = new ArrayList<List<String>>();
		stages.add(Arrays.asList(new String[] { "maturing", "matures", "mature", "maturity" }));
		stages.add(Arrays.asList(new String[] { "producing", "produce" }));
		stages.add(Arrays.asList(new String[] { "meiosis" }));
		stages.add(Arrays.asList(new String[] { "dying", "die", "dies" }));
		stages.add(Arrays.asList(new String[] { "persisting", "persist", "persists" }));

		for(List<String> stageTerms : stages) {
			extractors.add(new PhenologyInfoExtractor(null,
					Arrays.asList(flowerEntityTerms),
					stageTerms,
					false, advModPattern, stopwords));
			extractors.add(new PhenologyInfoExtractor(null,
					Arrays.asList(fruitEntityTerms),
					stageTerms,
					false, advModPattern, stopwords));
			extractors.add(new PhenologyInfoExtractor(null,
					Arrays.asList(sporeEntityTerms),
					stageTerms,
					false, advModPattern, stopwords));
			extractors.add(new PhenologyInfoExtractor(null,
					Arrays.asList(coneEntityTerms),
					stageTerms,
					false, advModPattern, stopwords));
			extractors.add(new PhenologyInfoExtractor(null,
					Arrays.asList(capsuleEntityTerms),
					stageTerms,
					false, advModPattern, stopwords));
			extractors.add(new PhenologyInfoExtractor(null,
					Arrays.asList(leafEntityTerms),
					stageTerms,
					false, advModPattern, stopwords));
			extractors.add(new PhenologyInfoExtractor(null,
					Arrays.asList(seedEntityTerms),
					stageTerms,
					false, advModPattern, stopwords));
		}
	}

	@Override
	public void transform(List<PhenologiesFile> phenologiesFiles) {
		for(PhenologiesFile phenologiesFile : phenologiesFiles) {
			int i = 0;
			int organId = 0;
			for(Treatment treatment : phenologiesFile.getTreatments()) {
				for(Phenology phenology : treatment.getPhenologies()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("phenology_" + i++);
					statement.setText(phenology.getText());

					BiologicalEntity be = new BiologicalEntity();
					be.setName("whole_organism");
					be.setId("phen_o"+organId++);
					be.setType("structure");
					be.setNameOriginal("");
					be.addCharacters(getPhenologyInfoCharacters(phenology.getText()));
					statement.addBiologicalEntity(be);

					statements.add(statement);
					phenology.setStatements(statements);
				}
			}
		}
	}

	private LinkedHashSet<Character> getPhenologyInfoCharacters(String text) {
		log(LogLevel.INFO, "text: " + text);
		LinkedHashSet<Character>  values = new LinkedHashSet<Character>();

		text = normalize(text);
		String[] statements = getStatements(text);

		for(String statement : statements) {
			for(PhenologyInfoExtractor extractor : this.extractors) {
				values.addAll(extractor.extract(statement));
			}
		}

		return values;
	}

	private String[] getStatements(String text) {
		return text.split("\\.|;");
	}


	private String normalize(String text) {
		text = text.toLowerCase();

		text = text.replaceAll("\\s+", " ").replaceAll("–", "-").replaceAll("-+", "-").replaceAll("year-round", "year_round"); // - to mean "to"
		String nText = normalizeTime(text);
		if(nText.compareToIgnoreCase(text)!=0){
			log(LogLevel.DEBUG, "Text normalized to: "+nText);
			System.out.println("Text normalized to: "+nText);
		}

		text = nText;
		return text;
	}


	/**
	 * late spring and early summer (May-Jun)
	 * =>late_spring-early_summer_(May-Jun)
	 *
	 * latter half of winter?
	 * =>latter_half_of_winter?
	 * @param text
	 * @return
	 */
	private String normalizeTime(String text){
		//all year, throughout the year, through the year, year round => year_round
		text = text.replaceAll("\\b(all year|througout the year|through the year|year around)\\b", "year_round");

		//late spring
		text = text.replaceAll("(?<=\\b(?:" + timeModifierPattern+")\\b) (?=(?:" + timeTermsPattern+")\\b)", "_");

		//latter half of winter
		Pattern p = Pattern.compile("\\b(?:" + timeModifierPattern+")\\b.*?\\b(?:" + timeTermsPattern+")\\b");
		Matcher m = p.matcher(text);
		while(m.find()){
			String matched = text.substring(m.start(), m.end());
			if(matched.split("\\s+").length<=4 && !matched.matches(".*?[()\\]\\[].*?")){
				String stringed = matched.replaceAll("\\s+", "_");
				text = text.replaceAll(matched, stringed);
			}
		}

		//and/or/to
		text = text.replaceAll("(?<=(\\b|_|-)(?:" + timeModifierPattern + "|" + timeTermsPattern +")\\b) (and|or|to|through) (?=(?:"+ timeModifierPattern+"|"+ timeTermsPattern+")(\\b|_|-))", "-");



		//summer (May-Jun)
		text = text.replaceAll("(?<=(\\b|_|-)(?:"+ timeTermsPattern+")\\b) (?=(?:\\([^ ]*(_|-|\\b)("+ timeTermsPattern+")\\)))", "_");
		return text;
	}

}
