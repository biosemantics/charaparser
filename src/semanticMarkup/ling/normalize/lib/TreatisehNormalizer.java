package semanticMarkup.ling.normalize.lib;

import java.util.HashMap;
import java.util.Set;

import semanticMarkup.io.input.lib.db.ParentTagProvider;
import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class TreatisehNormalizer extends Normalizer {

	@Inject
	public TreatisehNormalizer(IGlossary glossary, @Named("Units") String units, @Named("NumberPattern")String numberPattern,
			@Named("Singulars")HashMap<String, String> singulars, @Named("Plurals")HashMap<String, String> plurals, 
			IPOSKnowledgeBase posKnowledgeBase, @Named("LyAdverbpattern") String lyAdverbPattern,
			@Named("p1")String p1, @Named("p2")String p2, @Named("p3")String p3, @Named("p4")String p4, @Named("p5")String p5, 
			@Named("p6")String p6, @Named("p7")String p7, @Named("p75")String p75, @Named("p8")String p8, 
			ITerminologyLearner terminologyLearner, 
			@Named("viewPattern") String viewPattern,
			@Named("countPattern") String countPattern,
			@Named("positionPattern") String positionPattern,
			@Named("romanRangePattern") String romanRangePattern,
			@Named("romanPattern") String romanPattern,
			@Named("romanNumbers") String[] romanNumbers, 
			@Named("StopWords") Set<String> stopWords, 
			@Named("PrepositionWords") String prepositionWords,
			@Named("modifierList") String modifierList, 
			@Named("parentTagProvider") ParentTagProvider parentTagProvider,
			ICharacterKnowledgeBase characterKnowledgeBase, 
			IOrganStateKnowledgeBase organStateKnowledgeBase, 
			IInflector inflector) {
		super(glossary, units, numberPattern, singulars, plurals, posKnowledgeBase,
				lyAdverbPattern, p1, p2, p3, p4, p5, p6, p7, p75, p8,
				terminologyLearner, viewPattern, countPattern, positionPattern,
				romanRangePattern, romanPattern, romanNumbers, stopWords,
				prepositionWords, modifierList, parentTagProvider,
				characterKnowledgeBase, organStateKnowledgeBase, inflector);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String dataSetSpecificNormalization(String sentence) {
		sentence = sentence.replace("<B>", "");
		sentence = sentence.replace("<M>", "");
		sentence = sentence.replace("<N>", "");
		sentence = sentence.replace("</B>", "");
		sentence = sentence.replace("</M>", "");
		sentence = sentence.replace("</N>", "");
		return sentence;
	}

}
