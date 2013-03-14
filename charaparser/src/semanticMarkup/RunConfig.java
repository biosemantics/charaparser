package semanticMarkup;

import semanticMarkup.core.transformation.TreatmentTransformerChain;
import semanticMarkup.core.transformation.lib.CharaparserTreatmentTransformerChain;
import semanticMarkup.core.transformation.lib.MarkupDescriptionTreatmentTransformer;
import semanticMarkup.core.transformation.lib.OldPerlTreatmentTransformer;
import semanticMarkup.eval.AdvancedPrecisionRecallEvaluator;
import semanticMarkup.eval.IEvaluator;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.io.input.lib.db.EvaluationDBVolumeReader;
import semanticMarkup.io.input.lib.eval.StandardVolumeReader;
import semanticMarkup.io.output.IVolumeWriter;
import semanticMarkup.io.output.lib.xml2.XML2VolumeWriter;
import semanticMarkup.markup.AfterPerlBlackBox;
import semanticMarkup.markup.IMarkupCreator;
import semanticMarkup.run.IRun;
import semanticMarkup.run.MarkupEvaluationRun;

import com.google.inject.name.Names;

/**
 * Guice config file for parameters of a run
 * @author rodenhausen
 */
public class RunConfig extends BasicConfig {

	@Override 
	public void configure() {
		super.configure();
		
		bind(IRun.class).to(MarkupEvaluationRun.class);
		//MarkupRun, EvaluationRun, MarkupEvaluationRun
		bind(String.class).annotatedWith(Names.named("Run_OutFile")).toInstance("outfile");
		
		bind(IEvaluator.class).annotatedWith(Names.named("EvaluationRun_Evaluator")).to(AdvancedPrecisionRecallEvaluator.class);
		//SimplePrecisionRecallEvaluator, AdvancedPrecisionRecallEvaluator
		bind(IVolumeReader.class).annotatedWith(Names.named("EvaluationRun_GoldStandardReader")).to(StandardVolumeReader.class);
		//OutputVolumeReader, StandardVolumeReader
		bind(IVolumeReader.class).annotatedWith(Names.named("EvaluationRun_CreatedVolumeReader")).to(EvaluationDBVolumeReader.class);
		//OutputVolumeReader, EvaluationDBVolumeReader, PerlDBVolumeReader
		
		bind(IMarkupCreator.class).annotatedWith(Names.named("MarkupCreator")).to(AfterPerlBlackBox.class);
		//CharaParser.class //AfterPerlBlackBox
		bind(IVolumeReader.class).annotatedWith(Names.named("MarkupCreator_VolumeReader")).to(EvaluationDBVolumeReader.class);
		//Type1VolumeReader, Type2VolumeReader, PerlDBVolumeReader, EvaluationDBVolumeReader
		bind(String.class).annotatedWith(Names.named("Type1VolumeReader_Sourcefile")).toInstance("document_test.xml");
		bind(String.class).annotatedWith(Names.named("Type1VolumeReader_StyleStartPattern")).toInstance(".*?(Heading|Name).*");
		bind(String.class).annotatedWith(Names.named("Type1VolumeReader_StyleNamePattern")).toInstance(".*?(Syn|Name).*");
		bind(String.class).annotatedWith(Names.named("Type1VolumeReader_StyleKeyPattern")).toInstance(".*?-Key.*");
		bind(String.class).annotatedWith(Names.named("Type1VolumeReader_Tribegennamestyle")).toInstance("caps");
		bind(String.class).annotatedWith(Names.named("Type1VolumeReader_StyleMappingFile")).toInstance("" +
				"resources//stylemapping.properties");
		bind(String.class).annotatedWith(Names.named("Type2VolumeReader_Sourcefile")).toInstance("someFile.xml");
		
		bind(String.class).annotatedWith(Names.named("OutputVolumeReader_Sourcefile")).toInstance("outfile");
		
		bind(TreatmentTransformerChain.class).to(CharaparserTreatmentTransformerChain.class);
		bind(MarkupDescriptionTreatmentTransformer.class).to(OldPerlTreatmentTransformer.class);
		
		bind(boolean.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_parallelProcessing")).toInstance(false);
		bind(int.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_descriptionExtractorRunMaximum")).toInstance(30);
		bind(int.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_sentenceChunkerRunMaximum")).toInstance(Integer.MAX_VALUE);
		
		bind(String.class).annotatedWith(Names.named("databaseName")).toInstance("annotationevaluation");
		//markedupdatasets
		bind(String.class).annotatedWith(Names.named("databaseUser")).toInstance("termsuser");
		bind(String.class).annotatedWith(Names.named("databasePassword")).toInstance("termspassword");
		
		bind(IVolumeWriter.class).annotatedWith(Names.named("MarkupCreator_VolumeWriter")).to(XML2VolumeWriter.class); 
		//ToStringVolumeWriter, JSONVolumeWriter, XMLVolumeWriter XML2VolumeWriter
	}
}
