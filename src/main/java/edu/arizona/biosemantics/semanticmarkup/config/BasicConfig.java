package edu.arizona.biosemantics.semanticmarkup.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.oto.full.IOTOClient;
import edu.arizona.biosemantics.oto.full.OTOClient;
import edu.arizona.biosemantics.oto.lite.IOTOLiteClient;
import edu.arizona.biosemantics.oto.lite.OTOLiteClient;
import edu.arizona.biosemantics.semanticmarkup.io.InputStreamCreator;
import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.ICorpus;
import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.lib.CSVCorpus;
import edu.arizona.biosemantics.semanticmarkup.know.lib.LearnedCharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.lib.LearnedOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.lib.LearnedPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.lib.WordNetPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.IChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.CharaparserChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.AndChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.CharacterListChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.CharacterNameChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.ChromosomeChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.ConjunctedOrgansRecoverChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.MyModifierChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.MyNewCleanupChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.MyStateChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.NPListChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.NumericalChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.OrChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.OrganChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.OrganRecoverChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.OtherINsChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.PPINChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.PPListChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.PunctuationChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.SpecificPPChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.ThanChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.ThatChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.ToChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.VBChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.VPRecoverChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.WhenChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker.WhereChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessorProvider;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IFirstChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.ILastChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParser;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.lib.StanfordParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.lib.StanfordParserWrapper;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.IPOSTagger;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.lib.OrganCharacterPOSTagger;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IStanfordParserTokenTransformer;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.ITokenCombiner;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.lib.SomeInflector;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.lib.WhitespaceTokenCombiner;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.lib.WhitespaceTokenizer;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.lib.WordStanfordParserTokenTransformer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.ParentTagProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.Binding;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.IDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.AndChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.AreaChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.BracketedChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.CharacterNameChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.ChromChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.ChunkProcessorProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.CommaChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.ComparativeValueChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.ConstraintChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.CountChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.DummyChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.EosEolChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.MainSubjectOrganChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.MyCharacterStateChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.MyModifierChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.NPListChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.NonSubjectOrganChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.NumericalChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.OrChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.PPChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.RatioChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.SBARChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.SomeDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.SomeFirstChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.SpecificPPChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.StateChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.ThanChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.ToChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.VPChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.ValuePercentageOrDegreeChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib.WhereChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ILearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.DatabaseInputNoLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.Learner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.OTOLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.PerlTerminologyLearner;

/**
 * Guice config file for basic parameters
 * TODO: turn regex word lists into hashsets where appropriate		
 * TODO: arrange similar class layout as RunConfig
 * TODO: adapt paths to point to resource files, e.g. wordnet dictionary, brown corpus.. inside the jar instead of the file system
 * @author rodenhausen
 */
public class BasicConfig extends AbstractModule {
	
	  private String version = "0.1.5";
	  protected InputStreamCreator inputStreamCreator = new InputStreamCreator();
	  
	  @Override 
	  protected void configure() {	
		  try {
			  // ENVIRONMENTAL 
			  bind(InputStream.class).annotatedWith(Names.named("Taxonx_SchemaFile")).toInstance(inputStreamCreator.readStreamFromString(
					  "edu/arizona/biosemantics/semanticmarkup/description/io/schemas/taxonx/taxonx1.xsd"));
			  bind(InputStream.class).annotatedWith(Names.named("XML_SchemaFile")).toInstance(inputStreamCreator.readStreamFromString(
					  "edu/arizona/biosemantics/semanticmarkup/description/io/schemas/FNAXMLSchemaInput.xsd"));
			  bind(InputStream.class).annotatedWith(Names.named("iPlantXML_SchemaFile")).toInstance(inputStreamCreator.readStreamFromString(
					  "edu/arizona/biosemantics/semanticmarkup/description/io/schemas/iplant.xsd"));
			  bind(String.class).annotatedWith(Names.named("StanfordParserWrapper_ModelFile")).toInstance("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
			  
			  // IO
			  bind(new TypeLiteral<Map<File, Binding>>() {}).annotatedWith(Names.named("MOXyBinderDescriptionReaderWriter_FileDocumentMappings")).to(
					  new TypeLiteral<HashMap<File, Binding>>() {}).in(Singleton.class);
			  
			  // PROCESSING
			  bind(ICorpus.class).to(CSVCorpus.class).in(Singleton.class);
			  bind(Boolean.class).annotatedWith(Names.named("WordNetAPI_LoadInRAM")).toInstance(false);
			  bind(IInflector.class).to(SomeInflector.class).in(Singleton.class);
			  bind(ICharacterKnowledgeBase.class).to(LearnedCharacterKnowledgeBase.class).in(Singleton.class);;
			  bind(IOrganStateKnowledgeBase.class).to(LearnedOrganStateKnowledgeBase.class).in(Singleton.class);;
			  bind(IPOSKnowledgeBase.class).to(WordNetPOSKnowledgeBase.class).in(Singleton.class);
			  bind(IPOSKnowledgeBase.class).annotatedWith(Names.named("LearnedPOSKnowledgeBase")).to(LearnedPOSKnowledgeBase.class).in(Singleton.class);
			  bind(ITokenizer.class).annotatedWith(Names.named("WordTokenizer")).to(WhitespaceTokenizer.class);
			  bind(ITokenCombiner.class).annotatedWith(Names.named("WordCombiner")).to(WhitespaceTokenCombiner.class);
			  bind(IOTOClient.class).to(OTOClient.class).in(Singleton.class);
			  bind(IOTOLiteClient.class).to(OTOLiteClient.class).in(Singleton.class);
			  bind(ILearner.class).to(OTOLearner.class).in(Singleton.class);
			  bind(String.class).annotatedWith(Names.named("MarkupMode")).toInstance("plain");
			  bind(IPOSTagger.class).to(OrganCharacterPOSTagger.class); //NewOrganCharacterPOSTagger , OrganCharacterPOSTagger
			  bind(IParser.class).to(StanfordParserWrapper.class).in(Singleton.class);
			  bind(IStanfordParserTokenTransformer.class).to(WordStanfordParserTokenTransformer.class).in(Singleton.class);
			  bind(IParseTreeFactory.class).to(StanfordParseTreeFactory.class).in(Singleton.class);
			  bind(ParentTagProvider.class).annotatedWith(Names.named("ParentTagProvider")).to(ParentTagProvider.class).in(Singleton.class);
			  
			  bind(ChunkerChain.class).annotatedWith(Names.named("ChunkerChain")).to(CharaparserChunkerChain.class).in(Singleton.class);
			  bind(IChunker.class).annotatedWith(Names.named("OrganChunker")).to(OrganChunker.class).in(Singleton.class);
			  bind(IChunker.class).annotatedWith(Names.named("StateChunker")).to(MyStateChunker.class).in(Singleton.class);
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
			  
			  bind(IDescriptionExtractor.class).to(SomeDescriptionExtractor.class).in(Singleton.class);
			  bind(IFirstChunkProcessor.class).to(SomeFirstChunkProcessor.class).in(Singleton.class);
			  bind(ILastChunkProcessor.class).to(EosEolChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessorProvider.class).to(ChunkProcessorProvider.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Area")).to(AreaChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Bracketed")).to(BracketedChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("CharacterState")).to(MyCharacterStateChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Chrom")).to(ChromChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("ComparativeValue")).to(ComparativeValueChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Eos")).to(EosEolChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Eol")).to(EosEolChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("MainSubjectOrgan")).to(MainSubjectOrganChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Modifier")).to(MyModifierChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("NonSubjectOrgan")).to(NonSubjectOrganChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("NPList")).to(NPListChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Numerical")).to(NumericalChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Value")).to(NumericalChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Count")).to(CountChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("BasedCount")).to(NumericalChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Or")).to(OrChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("And")).to(AndChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("PP")).to(PPChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Ratio")).to(RatioChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("SBAR")).to(SBARChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("SpecificPP")).to(SpecificPPChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Than")).to(ThanChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("ThanCharacter")).to(ThanChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("ValuePercentage")).to(ValuePercentageOrDegreeChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("ValueDegree")).to(ValuePercentageOrDegreeChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("VP")).to(VPChunkProcessor.class).in(Singleton.class); 
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Unassigned")).to(DummyChunkProcessor.class).in(Singleton.class); 
			  bind(IChunkProcessor.class).annotatedWith(Names.named("State")).to(StateChunkProcessor.class).in(Singleton.class); 
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Comma")).to(CommaChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Constraint")).to(ConstraintChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("That")).to(SBARChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("When")).to(SBARChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("Where")).to(WhereChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("To")).to(ToChunkProcessor.class).in(Singleton.class);
			  bind(IChunkProcessor.class).annotatedWith(Names.named("CharacterName")).to(CharacterNameChunkProcessor.class).in(Singleton.class);
	
			  // MISC
			  bind(String.class).annotatedWith(Names.named("Version")).toInstance(version);
			  
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
			  bind(String[].class).annotatedWith(Names.named("RomanNumbers")).toInstance(romanNumbers);
			  bind(new TypeLiteral<HashMap<String, String>>(){}).annotatedWith(Names.named("EqualCharacters")).toInstance(getEqualCharacters());
			  bind(String.class).annotatedWith(Names.named("NumberPattern")).toInstance(
								"[()\\[\\]\\-\\–\\d\\.×x\\+°²½/¼\\*/%\\?]*?[½/¼\\d][()\\[\\]\\-\\–\\d\\.,?×x\\+°²½/¼\\*/%\\?]{2,}(?![a-z{}])");
			  bind(new TypeLiteral<HashMap<String, String>>(){}).annotatedWith(Names.named("Singulars")).toInstance(getSingulars());
			  bind(new TypeLiteral<HashMap<String, String>>(){}).annotatedWith(Names.named("Plurals")).toInstance(getPlurals());
			  bind(String.class).annotatedWith(Names.named("LyAdverbpattern")).toInstance("[a-z]{3,}ly");
			  bind(String.class).annotatedWith(Names.named("P1")).toInstance("(.*?[^aeiou])ies$");
			  bind(String.class).annotatedWith(Names.named("P2")).toInstance("(.*?)i$");
			  bind(String.class).annotatedWith(Names.named("P3")).toInstance("(.*?)ia$");
			  bind(String.class).annotatedWith(Names.named("P4")).toInstance("(.*?(x|ch|sh|ss))es$");
			  bind(String.class).annotatedWith(Names.named("P5")).toInstance("(.*?)ves$");
			  bind(String.class).annotatedWith(Names.named("P6")).toInstance("(.*?)ices$");
			  bind(String.class).annotatedWith(Names.named("P7")).toInstance("(.*?a)e$");
			  bind(String.class).annotatedWith(Names.named("P75")).toInstance("(.*?)us$");
			  bind(String.class).annotatedWith(Names.named("P8")).toInstance("(.*?)s$");
			  bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("VBWords")).toInstance(getSetOfVBWords());
			  bind(String.class).annotatedWith(Names.named("ViewPattern")).toInstance("(.*?\\b)(in\\s+[a-z_<>{} -]*\\s*[<{]*(?:view|profile)[}>]*)(\\s.*)");	
			  String count = "more|fewer|less|\\d";
			  bind(String.class).annotatedWith(Names.named("CountPattern")).toInstance("((?:^| |\\{)(?:"+count+")\\}? (?:or|to) \\{?(?:"+count+")(?:\\}| |$))");
			  bind(String.class).annotatedWith(Names.named("PositionPattern")).toInstance("(<(\\S+?)> \\d+(?:(?: and |_)\\d+)?(?!\\s*(?:/|times)))");
			  String roman = "i|ii|iii|iv|v|vi|vii|viii|ix|x|xi|xii|xiii|xiv|xv|xvi|xvii|xviii|xix|xx|I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX";
			  bind(String.class).annotatedWith(Names.named("RomanRangePattern")).toInstance("(\\d+)-<?\\b("+roman+")\\b>?");
			  bind(String.class).annotatedWith(Names.named("RomanPattern")).toInstance("(<(\\S+?)> <?\\{?\\b("+roman+")\\b\\}?>?)");
			  bind(String.class).annotatedWith(Names.named("ModifierList")).toInstance("(.*?\\b)(\\w+ly\\s+(?:to|or)\\s+\\w+ly)(\\b.*)");	
		  } catch(IOException e) {
			  e.printStackTrace();
		  }
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	
}