package edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.ParentTagProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * FNAv19Normalizer extends Normalizer adding FNAv19 dataset specific normalization
 */
public class FNAv19Normalizer extends Normalizer {

	/**
	 *
	 * @param glossary
	 * @param units
	 * @param numberPattern
	 * @param singulars
	 * @param plurals
	 * @param posKnowledgeBase
	 * @param lyAdverbPattern
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param p4
	 * @param p5
	 * @param p6
	 * @param p7
	 * @param p75
	 * @param p8
	 * @param terminologyLearner
	 * @param viewPattern
	 * @param countPattern
	 * @param positionPattern
	 * @param romanRangePattern
	 * @param romanPattern
	 * @param romanNumbers
	 * @param stopWords
	 * @param prepositionWords
	 * @param modifierList
	 * @param advModifiers
	 * @param parentTagProvider
	 * @param characterKnowledgeBase
	 * @param inflector
	 * @param compoundPPptn
	 * @param adjNouns
	 * @param adjNounCounterParts
	 */
	@Inject
	public FNAv19Normalizer(IGlossary glossary, @Named("Units") String units, @Named("NumberPattern")String numberPattern,
			@Named("Singulars")HashMap<String, String> singulars, @Named("Plurals")HashMap<String, String> plurals,
			@Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase, @Named("LyAdverbpattern") String lyAdverbPattern,
			@Named("P1")String p1, @Named("P2")String p2, @Named("P3")String p3, @Named("P4")String p4, @Named("P5")String p5,
			@Named("P6")String p6, @Named("P7")String p7, @Named("P75")String p75, @Named("P8")String p8,
			ITerminologyLearner terminologyLearner,
			@Named("ViewPattern") String viewPattern,
			@Named("CountPattern") String countPattern,
			@Named("PositionPattern") String positionPattern,
			@Named("RomanRangePattern") String romanRangePattern,
			@Named("RomanPattern") String romanPattern,
			@Named("RomanNumbers") String[] romanNumbers,
			@Named("StopWords") Set<String> stopWords,
			@Named("PrepositionWords") String prepositionWords,
			@Named("ModifierList") String modifierList,
			@Named("AdvModifiers") String advModifiers,
			@Named("ParentTagProvider")ParentTagProvider parentTagProvider,
			ICharacterKnowledgeBase characterKnowledgeBase,
			/*IOrganStateKnowledgeBase organStateKnowledgeBase, */
			IInflector inflector,
			@Named("CompoundPrepWords")String compoundPPptn,
			@Named("AdjNouns") String adjNouns,
			@Named("AdjNounCounterParts") Hashtable<String, String> adjNounCounterParts) {
		super(glossary, units, numberPattern, singulars, plurals, posKnowledgeBase,
				lyAdverbPattern, p1, p2, p3, p4, p5, p6, p7, p75, p8,
				terminologyLearner, viewPattern, countPattern, positionPattern,
				romanRangePattern, romanPattern, romanNumbers, stopWords,
				prepositionWords, modifierList, advModifiers, parentTagProvider,
				characterKnowledgeBase/*, organStateKnowledgeBase*/, inflector, compoundPPptn, adjNouns, adjNounCounterParts);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String dataSetSpecificNormalization(String sentence) {
		return sentence;
	}



}
