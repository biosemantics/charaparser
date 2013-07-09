package semanticMarkup.config;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import oto.full.IOTOClient;
import oto.full.OTOClient;
import oto.lite.IOTOLiteClient;
import oto.lite.OTOLiteClient;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.ICorpus;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.know.lib.CSVCorpus;
import semanticMarkup.know.lib.LearnedCharacterKnowledgeBase;
import semanticMarkup.know.lib.LearnedOrganStateKnowledgeBase;
import semanticMarkup.know.lib.LearnedPOSKnowledgeBase;
import semanticMarkup.know.lib.WordNetPOSKnowledgeBase;
import semanticMarkup.ling.chunk.ChunkerChain;
import semanticMarkup.ling.chunk.IChunker;
import semanticMarkup.ling.chunk.lib.CharaparserChunkerChain;
import semanticMarkup.ling.chunk.lib.chunker.AndChunker;
import semanticMarkup.ling.chunk.lib.chunker.CharacterListChunker;
import semanticMarkup.ling.chunk.lib.chunker.CharacterNameChunker;
import semanticMarkup.ling.chunk.lib.chunker.ChromosomeChunker;
import semanticMarkup.ling.chunk.lib.chunker.ConjunctedOrgansRecoverChunker;
import semanticMarkup.ling.chunk.lib.chunker.MyModifierChunker;
import semanticMarkup.ling.chunk.lib.chunker.MyNewCleanupChunker;
import semanticMarkup.ling.chunk.lib.chunker.MyStateChunker;
import semanticMarkup.ling.chunk.lib.chunker.NPListChunker;
import semanticMarkup.ling.chunk.lib.chunker.NumericalChunker;
import semanticMarkup.ling.chunk.lib.chunker.OrChunker;
import semanticMarkup.ling.chunk.lib.chunker.OrganChunker;
import semanticMarkup.ling.chunk.lib.chunker.OrganRecoverChunker;
import semanticMarkup.ling.chunk.lib.chunker.OtherINsChunker;
import semanticMarkup.ling.chunk.lib.chunker.PPINChunker;
import semanticMarkup.ling.chunk.lib.chunker.PPListChunker;
import semanticMarkup.ling.chunk.lib.chunker.PunctuationChunker;
import semanticMarkup.ling.chunk.lib.chunker.SpecificPPChunker;
import semanticMarkup.ling.chunk.lib.chunker.ThanChunker;
import semanticMarkup.ling.chunk.lib.chunker.ThatChunker;
import semanticMarkup.ling.chunk.lib.chunker.ToChunker;
import semanticMarkup.ling.chunk.lib.chunker.VBChunker;
import semanticMarkup.ling.chunk.lib.chunker.VPRecoverChunker;
import semanticMarkup.ling.chunk.lib.chunker.WhenChunker;
import semanticMarkup.ling.chunk.lib.chunker.WhereChunker;
import semanticMarkup.ling.extract.IChunkProcessor;
import semanticMarkup.ling.extract.IChunkProcessorProvider;
import semanticMarkup.ling.extract.IFirstChunkProcessor;
import semanticMarkup.ling.extract.ILastChunkProcessor;
import semanticMarkup.ling.parse.IParser;
import semanticMarkup.ling.parse.IParseTreeFactory;
import semanticMarkup.ling.parse.lib.StanfordParseTreeFactory;
import semanticMarkup.ling.parse.lib.StanfordParserWrapper;
import semanticMarkup.ling.pos.IPOSTagger;
import semanticMarkup.ling.pos.lib.OrganCharacterPOSTagger;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.ling.transform.IStanfordParserTokenTransformer;
import semanticMarkup.ling.transform.ITokenCombiner;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.ling.transform.lib.SomeInflector;
import semanticMarkup.ling.transform.lib.WhitespaceTokenCombiner;
import semanticMarkup.ling.transform.lib.WhitespaceTokenizer;
import semanticMarkup.ling.transform.lib.WordStanfordParserTokenTransformer;
import semanticMarkup.markupElement.description.io.ParentTagProvider;
import semanticMarkup.markupElement.description.ling.extract.IDescriptionExtractor;
import semanticMarkup.markupElement.description.ling.extract.lib.AndChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.AreaChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.BracketedChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.CharacterNameChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.ChromChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.ChunkProcessorProvider;
import semanticMarkup.markupElement.description.ling.extract.lib.CommaChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.ComparativeValueChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.ConstraintChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.CountChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.DummyChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.EosEolChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.MainSubjectOrganChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.MyCharacterStateChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.MyModifierChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.NPListChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.NonSubjectOrganChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.NumericalChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.OrChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.PPChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.RatioChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.SBARChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.SomeDescriptionExtractor;
import semanticMarkup.markupElement.description.ling.extract.lib.SomeFirstChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.SpecificPPChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.StateChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.ThanChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.ToChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.VPChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.ValuePercentageOrDegreeChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.lib.WhereChunkProcessor;
import semanticMarkup.markupElement.description.ling.learn.ILearner;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.ling.learn.lib.DatabaseInputNoLearner;
import semanticMarkup.markupElement.description.ling.learn.lib.Learner;
import semanticMarkup.markupElement.description.ling.learn.lib.OTOLearner;
import semanticMarkup.markupElement.description.ling.learn.lib.PerlTerminologyLearner;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Guice config file for basic parameters
 * TODO: arrange similar class layout as RunConfig
 * TODO: adapt paths to point to resource files, e.g. wordnet dictionary, brown corpus.. inside the jar instead of the file system
 * @author rodenhausen
 */
public class BasicConfig extends AbstractModule {
	
	  @Override 
	  protected void configure() {	 
		bind(String.class).annotatedWith(Names.named("Version")).toInstance("0.1");
		  
		 /* TODO: turn regex word lists into hashsets where appropriate */			
		
		bind(String.class).annotatedWith(Names.named("Taxonx_SchemaFile")).toInstance(
				"." + File.separator + "resources" + File.separator + "io" + File.separator + "taxonx" + File.separator + "taxonx1.xsd");
		bind(String.class).annotatedWith(Names.named("XML_SchemaFile")).toInstance(
				"." + File.separator + "resources" + File.separator + "io" + File.separator + "FNAXMLSchemaInput.xsd");
		bind(String.class).annotatedWith(Names.named("iPlantXML_SchemaFile")).toInstance(
				"." + File.separator + "resources" + File.separator + "io" + File.separator + "iplant.xsd");
		  
		bind(ParentTagProvider.class).annotatedWith(Names.named("parentTagProvider")).to(ParentTagProvider.class).in(Singleton.class);
		
		bind(ITokenizer.class).annotatedWith(Names.named("WordTokenizer")).to(WhitespaceTokenizer.class);
		bind(ITokenCombiner.class).annotatedWith(Names.named("WordCombiner")).to(WhitespaceTokenCombiner.class);
		
		bind(ICorpus.class).to(CSVCorpus.class).in(Singleton.class);
		bind(String.class).annotatedWith(Names.named("CSVCorpus_filePath")).toInstance("resources" + File.separator + "brown.csv");
		bind(String.class).annotatedWith(Names.named("WordNetAPI_Sourcefile")).toInstance("resources" + File.separator +"wordNet3.1" + File.separator +"dict" + File.separator);
		//resources//wordNet2.1//dict//  resources//wordNet3.1//dict//
		bind(Boolean.class).annotatedWith(Names.named("WordNetAPI_LoadInRAM")).toInstance(false);
		bind(IInflector.class).to(SomeInflector.class).in(Singleton.class);
		bind(ICharacterKnowledgeBase.class).to(LearnedCharacterKnowledgeBase.class).in(Singleton.class);;
		bind(IOrganStateKnowledgeBase.class).to(LearnedOrganStateKnowledgeBase.class).in(Singleton.class);;
		
		bind(IOTOClient.class).to(OTOClient.class).in(Singleton.class);
		bind(IOTOLiteClient.class).to(OTOLiteClient.class).in(Singleton.class);
		bind(ILearner.class).to(OTOLearner.class).in(Singleton.class);
		
		bind(String.class).annotatedWith(Names.named("markupMode")).toInstance("plain");
		
		bind(IPOSTagger.class).to(OrganCharacterPOSTagger.class); //NewOrganCharacterPOSTagger , OrganCharacterPOSTagger
		bind(IParser.class).to(StanfordParserWrapper.class).in(Singleton.class);
		bind(IStanfordParserTokenTransformer.class).to(WordStanfordParserTokenTransformer.class).in(Singleton.class);
		bind(String.class).annotatedWith(Names.named("StanfordParserWrapper_modelFile")).toInstance("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		bind(IParseTreeFactory.class).to(StanfordParseTreeFactory.class).in(Singleton.class);
		
		bind(ChunkerChain.class).annotatedWith(Names.named("ChunkerChain")).to(CharaparserChunkerChain.class).in(Singleton.class);
		//MyChunkerChain, CharaparserChunkChain
		bind(IChunker.class).annotatedWith(Names.named("OrganChunker")).to(OrganChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("StateChunker")).to(MyStateChunker.class).in(Singleton.class);
		//StateChunker, MyStateChunker
		bind(IChunker.class).annotatedWith(Names.named("NPListChunker")).to(NPListChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("PPListChunker")).to(PPListChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("PPINChunker")).to(PPINChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("VBChunker")).to(VBChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("ThatChunker")).to(ThatChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("WhereChunker")).to(WhereChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("WhenChunker")).to(WhenChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("OtherINsChunker")).to(OtherINsChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("ThanChunker")).to(ThanChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("ToChunker")).to(ToChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("VPRecoverChunker")).to(VPRecoverChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("ConjunctedOrgansRecoverChunker")).to(ConjunctedOrgansRecoverChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("OrganRecoverChunker")).to(OrganRecoverChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("CleanupChunker")).to(MyNewCleanupChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("ModifierChunker")).to(MyModifierChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("NumericalChunker")).to(NumericalChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("OrChunker")).to(OrChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("AndChunker")).to(AndChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("PunctuationChunker")).to(PunctuationChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("CharacterListChunker")).to(CharacterListChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("SpecificPPChunker")).to(SpecificPPChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("CharacterNameChunker")).to(CharacterNameChunker.class).in(Singleton.class);
		bind(IChunker.class).annotatedWith(Names.named("ChromosomeChunker")).to(ChromosomeChunker.class).in(Singleton.class);
		
		bind(IPOSKnowledgeBase.class).to(WordNetPOSKnowledgeBase.class).in(Singleton.class);
		bind(IPOSKnowledgeBase.class).annotatedWith(Names.named("LearnedPOSKnowledgeBase")).to(LearnedPOSKnowledgeBase.class).in(Singleton.class);
		
		bind(IDescriptionExtractor.class).to(SomeDescriptionExtractor.class).in(Singleton.class);
		bind(IFirstChunkProcessor.class).to(SomeFirstChunkProcessor.class).in(Singleton.class);
		bind(ILastChunkProcessor.class).to(EosEolChunkProcessor.class).in(Singleton.class);
		bind(IChunkProcessorProvider.class).to(ChunkProcessorProvider.class).in(Singleton.class);
		//ChunkProcessorProvider
		bind(IChunkProcessor.class).annotatedWith(Names.named("Area")).to(AreaChunkProcessor.class).in(Singleton.class);
		//AreaChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Bracketed")).to(BracketedChunkProcessor.class).in(Singleton.class);
		//BracketedChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("CharacterState")).to(MyCharacterStateChunkProcessor.class).in(Singleton.class);
		//CharacterStateChunkProcessor, MyCharacterStateChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Chrom")).to(ChromChunkProcessor.class).in(Singleton.class);
		//ChromChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("ComparativeValue")).to(ComparativeValueChunkProcessor.class).in(Singleton.class);
		//ComparativeValueChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Eos")).to(EosEolChunkProcessor.class).in(Singleton.class);
		//EosEolChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Eol")).to(EosEolChunkProcessor.class).in(Singleton.class);
		//EosEolChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("MainSubjectOrgan")).to(MainSubjectOrganChunkProcessor.class).in(Singleton.class);
		//MainSubjectOrganChunkProcessor, MyMainSubjectOrganChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Modifier")).to(MyModifierChunkProcessor.class).in(Singleton.class);
		//MyModifierChunkProcessor, MyMainSubjectOrganChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("NonSubjectOrgan")).to(NonSubjectOrganChunkProcessor.class).in(Singleton.class);
		//NonSubjectOrganChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("NPList")).to(NPListChunkProcessor.class).in(Singleton.class);
		//NPListChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Numerical")).to(NumericalChunkProcessor.class).in(Singleton.class);
		//NumericalsChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Value")).to(NumericalChunkProcessor.class).in(Singleton.class);
		//NumericalChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Count")).to(CountChunkProcessor.class).in(Singleton.class);
		//NumericalChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("BasedCount")).to(NumericalChunkProcessor.class).in(Singleton.class);
		//NumericalChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Or")).to(OrChunkProcessor.class).in(Singleton.class);
		//OrChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("And")).to(AndChunkProcessor.class).in(Singleton.class);
		//AndChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("PP")).to(PPChunkProcessor.class).in(Singleton.class);
		//PPChunkProcessor, MyPPChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Ratio")).to(RatioChunkProcessor.class).in(Singleton.class);
		//RatioChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("SBAR")).to(SBARChunkProcessor.class).in(Singleton.class);
		//SBARChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("SpecificPP")).to(SpecificPPChunkProcessor.class).in(Singleton.class);
		//SpecificPPChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Than")).to(ThanChunkProcessor.class).in(Singleton.class);
		//ThanChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("ThanCharacter")).to(ThanChunkProcessor.class).in(Singleton.class);
		//ThanChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("ValuePercentage")).to(ValuePercentageOrDegreeChunkProcessor.class).in(Singleton.class);
		//ValuePercentageOrDegreeChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("ValueDegree")).to(ValuePercentageOrDegreeChunkProcessor.class).in(Singleton.class);
		//ValuePercentageOrDegreeChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("VP")).to(VPChunkProcessor.class).in(Singleton.class); 
		//VPChunkProcessor, DummyChunkProcessor
		bind(IChunkProcessor.class).annotatedWith(Names.named("Unassigned")).to(DummyChunkProcessor.class).in(Singleton.class); 
		bind(IChunkProcessor.class).annotatedWith(Names.named("State")).to(StateChunkProcessor.class).in(Singleton.class); 
		bind(IChunkProcessor.class).annotatedWith(Names.named("Comma")).to(CommaChunkProcessor.class).in(Singleton.class);
		bind(IChunkProcessor.class).annotatedWith(Names.named("Constraint")).to(ConstraintChunkProcessor.class).in(Singleton.class);
		bind(IChunkProcessor.class).annotatedWith(Names.named("That")).to(SBARChunkProcessor.class).in(Singleton.class);
		bind(IChunkProcessor.class).annotatedWith(Names.named("When")).to(SBARChunkProcessor.class).in(Singleton.class);
		bind(IChunkProcessor.class).annotatedWith(Names.named("Where")).to(WhereChunkProcessor.class).in(Singleton.class);
		bind(IChunkProcessor.class).annotatedWith(Names.named("To")).to(ToChunkProcessor.class).in(Singleton.class);
		bind(IChunkProcessor.class).annotatedWith(Names.named("CharacterName")).to(CharacterNameChunkProcessor.class).in(Singleton.class);
	
		// create a Automata for these?
		Set<String> locationPPWords = getLocationPPWords();
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("LocationPrepositionWords")).toInstance(locationPPWords);
		String percentageWords = getPercentageWords();
		bind(String.class).annotatedWith(Names.named("PercentageWords")).toInstance(percentageWords);
		String degreeWords = getDegreeWords();
		bind(String.class).annotatedWith(Names.named("DegreeWords")).toInstance(degreeWords);
		String timesWords = getTimesWords();
		bind(String.class).annotatedWith(Names.named("TimesWords")).toInstance(timesWords);
		Set<String> perWords = getPerWords();
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("PerWords")).toInstance(perWords);
		Set<String> moreWords = getMoreWords();
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("MoreWords")).toInstance(moreWords);
		String countsWords = getCountsWords();
		bind(String.class).annotatedWith(Names.named("CountWords")).toInstance(countsWords);
		Set<String> baseCountWords = getBaseCountWords();
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("BaseCountWords")).toInstance(baseCountWords);
		Set<String> clusterWords = getClusterWords();
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("Clusters")).toInstance(clusterWords);
		Set<String> skipWords = getSkipWords();
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("SkipWords")).toInstance(skipWords);
		String prepositionWords = getPrepositionWords();
		bind(String.class).annotatedWith(Names.named("PrepositionWords")).toInstance(prepositionWords);
		Set<String> prepositionWordsSet = getPrepositionWordsSet();
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("PrepositionWordsSet")).toInstance(prepositionWordsSet);
		Set<String> stopWords = getStopWords();
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("StopWords")).toInstance(stopWords);
		String units = getUnits();
		bind(String.class).annotatedWith(Names.named("Units")).toInstance(units);
		Set<String> notInModifier = getNotInModifier();
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("NotInModifier")).toInstance(notInModifier);
		Set<String> delimiters = getDelimiters();
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("Delimiters")).toInstance(delimiters);
		String[] romanNumbers= { "i","ii","iii","iv","v","vi","vii","viii","ix","x","xi","xii","xiii","xiv","xv","xvi","xvii","xviii","xix","xx" };
		bind(String[].class).annotatedWith(Names.named("romanNumbers")).toInstance(romanNumbers);
		bind(new TypeLiteral<HashMap<String, String>>(){}).annotatedWith(Names.named("EqualCharacters")).toInstance(getEqualCharacters());
		bind(String.class).annotatedWith(Names.named("NumberPattern")).toInstance(
						"[()\\[\\]\\-\\–\\d\\.×x\\+°²½/¼\\*/%\\?]*?[½/¼\\d][()\\[\\]\\-\\–\\d\\.,?×x\\+°²½/¼\\*/%\\?]{2,}(?![a-z{}])");
		bind(new TypeLiteral<HashMap<String, String>>(){}).annotatedWith(Names.named("Singulars")).toInstance(getSingulars());
		bind(new TypeLiteral<HashMap<String, String>>(){}).annotatedWith(Names.named("Plurals")).toInstance(getPlurals());
		bind(String.class).annotatedWith(Names.named("LyAdverbpattern")).toInstance("[a-z]{3,}ly");
		bind(String.class).annotatedWith(Names.named("p1")).toInstance("(.*?[^aeiou])ies$");
		bind(String.class).annotatedWith(Names.named("p2")).toInstance("(.*?)i$");
		bind(String.class).annotatedWith(Names.named("p3")).toInstance("(.*?)ia$");
		bind(String.class).annotatedWith(Names.named("p4")).toInstance("(.*?(x|ch|sh|ss))es$");
		bind(String.class).annotatedWith(Names.named("p5")).toInstance("(.*?)ves$");
		bind(String.class).annotatedWith(Names.named("p6")).toInstance("(.*?)ices$");
		bind(String.class).annotatedWith(Names.named("p7")).toInstance("(.*?a)e$");
		bind(String.class).annotatedWith(Names.named("p75")).toInstance("(.*?)us$");
		bind(String.class).annotatedWith(Names.named("p8")).toInstance("(.*?)s$");
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("VBWords")).toInstance(getSetOfVBWords());
		bind(String.class).annotatedWith(Names.named("viewPattern")).toInstance("(.*?\\b)(in\\s+[a-z_<>{} -]*\\s*[<{]*(?:view|profile)[}>]*)(\\s.*)");	
		String count = "more|fewer|less|\\d";
		bind(String.class).annotatedWith(Names.named("countPattern")).toInstance("((?:^| |\\{)(?:"+count+")\\}? (?:or|to) \\{?(?:"+count+")(?:\\}| |$))");
		bind(String.class).annotatedWith(Names.named("positionPattern")).toInstance("(<(\\S+?)> \\d+(?:(?: and |_)\\d+)?(?!\\s*(?:/|times)))");
		String roman = "i|ii|iii|iv|v|vi|vii|viii|ix|x|xi|xii|xiii|xiv|xv|xvi|xvii|xviii|xix|xx|I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX";
		bind(String.class).annotatedWith(Names.named("romanRangePattern")).toInstance("(\\d+)-<?\\b("+roman+")\\b>?");
		bind(String.class).annotatedWith(Names.named("romanPattern")).toInstance("(<(\\S+?)> <?\\{?\\b("+roman+")\\b\\}?>?)");
		bind(String.class).annotatedWith(Names.named("modifierList")).toInstance("(.*?\\b)(\\w+ly\\s+(?:to|or)\\s+\\w+ly)(\\b.*)");	
  	}

	private Set<String> getDelimiters() {
		String delimiters = "comma|or";
		return this.getWordSet(delimiters);
	}

	private Set<String> getNotInModifier() {
		String notInModifier = "a|an|the";
		return this.getWordSet(notInModifier);
	}

	private String getUnits() {
		String units = "cm|mm|dm|m|meter|meters|microns|micron|unes|µm|um";
		return units;
	}

	private String getPrepositionWords() {
		String prepositionWords = "above|across|after|along|among|amongst|around|as|at|before|behind|below|beneath|between|beyond|by|during|for|from|in|into|near|of|off|on|onto|out"
				+ "|outside|over|per|than|through|throughout|to|toward|towards|up|upward|with|without";
		return prepositionWords;
	}
	
	private Set<String> getPrepositionWordsSet() {
		String prepositionWords = "above|across|after|along|among|amongst|around|as|at|before|behind|below|beneath|between|beyond|by|during|for|from|in|into|near|of|off|on|onto|out"
				+ "|outside|over|per|than|through|throughout|to|toward|towards|up|upward|with|without";
		return this.getWordSet(prepositionWords);
	}

	private Set<String> getSkipWords() {
		String skipWords = "and|becoming|if|or|that|these|this|those|to|what|when|where|which|why|not|throughout";
		return this.getWordSet(skipWords);
	}

	private Set<String> getClusterWords() {
		String clusterWords = "cluster|clusters|involucre|involucres|rosette|rosettes|pair|pairs|series|ornament|ornamentation|array|arrays";
		return this.getWordSet(clusterWords);
	}

	private Set<String> getBaseCountWords() {
		String baseCountsWords = "each|every|per";
		return this.getWordSet(baseCountsWords);
	}

	private String getCountsWords() {
		String countsWords = "few|several|many|none|numerous|single|couple";
		return countsWords;
	}

	private Set<String> getMoreWords() {
		String moreWords = "greater|more|less|fewer";
		return this.getWordSet(moreWords);
	}

	private Set<String> getPerWords() {
		String perWords = "per";
		return this.getWordSet(perWords);
	}

	private String getTimesWords() {
		String timesWords = "time|times|folds|lengths|widths";
		return timesWords;
	}

	private String getDegreeWords() {
		String degreeWords = "°|degree|degrees";
		return degreeWords;
	}

	private String getPercentageWords() {
		String percentageWords = "%|percentage";
		return percentageWords;
	}

	private Set<String> getWordSet(String regexString) {
		Set<String> set = new HashSet<String>();
		String[] wordsArray = regexString.split("\\|");
		for (String word : wordsArray)
			set.add(word.toLowerCase().trim());
		return set;
	}

	private Set<String> getLocationPPWords() {
		String words = "near|from";
		return this.getWordSet(words);
	}

	private Set<String> getSetOfVBWords() {
		String wordString = "";// "overtopping";
		return this.getWordSet(wordString);
	}

	private Set<String> getStopWords() {
		String stopWordsString = "a|about|above|across|after|along|also|although|amp|an|and|are|as|at|be|because|become|becomes|becoming|been|before|being|"
				+ "beneath|between|beyond|but|by|ca|can|could|did|do|does|doing|done|for|from|had|has|have|hence|here|how|if|in|into|inside|inward|is|it|its|"
				+ "may|might|more|most|near|no|not|of|off|on|onto|or|out|outside|outward|over|should|so|than|that|the|then|there|these|this|those|throughout|"
				+ "to|toward|towards|up|upward|was|were|what|when|where|which|why|with|within|without|would";
		return this.getWordSet(stopWordsString);
	}

	private HashMap<String, String> getEqualCharacters() {
		HashMap<String, String> equalCharacters = new HashMap<String, String>();
		equalCharacters.put("wide", "width");
		equalCharacters.put("long", "length");
		equalCharacters.put("broad", "width");
		equalCharacters.put("diam", "diameter");
		return equalCharacters;
	}

	private HashMap<String, String> getSingulars() {
		HashMap<String, String> singulars = new HashMap<String, String>();
		singulars.put("axis", "axis");
		singulars.put("axes", "axis");
		singulars.put("bases", "base");
		singulars.put("boss", "boss");
		singulars.put("buttress", "buttress");
		singulars.put("callus", "callus");
		singulars.put("frons", "frons");
		singulars.put("grooves", "groove");
		singulars.put("interstices", "interstice");
		singulars.put("lens", "len");
		singulars.put("media", "media");
		singulars.put("midnerves", "midnerve");
		singulars.put("process", "process");
		singulars.put("series", "series");
		singulars.put("species", "species");
		singulars.put("teeth", "tooth");
		singulars.put("valves", "valve");
		return singulars;
	}

	private HashMap<String, String> getPlurals() {
		HashMap<String, String> plurals = new HashMap<String, String>();
		plurals.put("axis", "axes");
		plurals.put("base", "bases");
		plurals.put("groove", "grooves");
		plurals.put("interstice", "interstices");
		plurals.put("len", "lens");
		plurals.put("media", "media");
		plurals.put("midnerve", "midnerves");
		plurals.put("tooth", "teeth");
		plurals.put("valve", "valves");
		plurals.put("boss", "bosses");
		plurals.put("buttress", "buttresses");
		plurals.put("callus", "calluses");
		plurals.put("frons", "fronses");
		plurals.put("process", "processes");
		plurals.put("series", "series");
		plurals.put("species", "species");
		return plurals;
	}
}