package semanticMarkup;

import java.io.File;

import semanticMarkup.core.transformation.TreatmentTransformerChain;
import semanticMarkup.core.transformation.lib.CharaparserTreatmentTransformerChain;
import semanticMarkup.core.transformation.lib.MarkupDescriptionTreatmentTransformer;
import semanticMarkup.core.transformation.lib.OldPerlTreatmentTransformer;
import semanticMarkup.eval.AdvancedPrecisionRecallEvaluator;
import semanticMarkup.eval.IEvaluator;
import semanticMarkup.io.input.IVolumeReader;
import semanticMarkup.io.input.lib.db.EvaluationDBVolumeReader;
import semanticMarkup.io.input.lib.taxonx.TaxonxVolumeReader;
import semanticMarkup.io.input.lib.word.AbstractWordVolumeReader;
import semanticMarkup.io.input.lib.word.DocWordVolumeReader;
import semanticMarkup.io.input.lib.word.XMLWordVolumeReader;
import semanticMarkup.io.input.lib.xml.XMLVolumeReader;
import semanticMarkup.io.input.lib.xmlOutput.OutputVolumeReader;
import semanticMarkup.io.output.IVolumeWriter;
import semanticMarkup.io.output.lib.xml.XMLVolumeWriter;
import semanticMarkup.markup.AfterPerlBlackBox;
import semanticMarkup.markup.IMarkupCreator;
import semanticMarkup.run.IRun;
import semanticMarkup.run.MarkupEvaluationRun;
import semanticMarkup.run.MarkupRun;

import com.google.inject.name.Names;

/**
 * Guice config file for parameters of a run
 * @author rodenhausen
 */
public class RunConfig extends BasicConfig {

	@Override 
	public void configure() {
		super.configure();
		
		bind(IRun.class).to(MarkupRun.class);
		//MarkupRun, EvaluationRun, MarkupEvaluationRun
		
		bind(String.class).annotatedWith(Names.named("Run_OutDirectory")).toInstance("." + File.separator + "out" + File.separator);
		
		bind(IEvaluator.class).annotatedWith(Names.named("EvaluationRun_Evaluator")).to(AdvancedPrecisionRecallEvaluator.class);
		//SimplePrecisionRecallEvaluator, AdvancedPrecisionRecallEvaluator
		bind(IVolumeReader.class).annotatedWith(Names.named("EvaluationRun_GoldStandardReader")).to(XMLVolumeReader.class);
		//OutputVolumeReader, StandardVolumeReader
		bind(IVolumeReader.class).annotatedWith(Names.named("EvaluationRun_CreatedVolumeReader")).to(EvaluationDBVolumeReader.class);
		//OutputVolumeReader, EvaluationDBVolumeReader, PerlDBVolumeReader
		
		bind(IMarkupCreator.class).annotatedWith(Names.named("MarkupCreator")).to(AfterPerlBlackBox.class);
		//CharaParser.class //AfterPerlBlackBox
		bind(IVolumeReader.class).annotatedWith(Names.named("MarkupCreator_VolumeReader")).to(TaxonxVolumeReader.class);
		//WordVolumeReader, XMLVolumeReader, PerlDBVolumeReader, EvaluationDBVolumeReader
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_Sourcefile")).toInstance("evaluationData" + File.separator + "FNA-v19-excerpt_Type1" + File.separator + 
				"source" + File.separator + "FNA19 Excerpt-source.docx");//"document.xml");//"FNA19 Excerpt-source.docx");
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_StyleStartPattern")).toInstance(".*?(Heading|Name).*");
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_StyleNamePattern")).toInstance(".*?(Syn|Name).*");
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_StyleKeyPattern")).toInstance(".*?-Key.*");
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_Tribegennamestyle")).toInstance("caps");
		bind(String.class).annotatedWith(Names.named("WordVolumeReader_StyleMappingFile")).toInstance("" +
				"resources" + File.separator + "stylemapping.properties");
		bind(String.class).annotatedWith(Names.named("XMLVolumeReader_SourceDirectory")).toInstance("evaluationData" + File.separator + "perlTest" + File.separator + "source" +
				File.separator);
		
		bind(String.class).annotatedWith(Names.named("OutputVolumeReader_SourceDirectory")).toInstance("." + File.separator + "out" + File.separator);
		
		bind(String.class).annotatedWith(Names.named("TaxonxVolumeReader_SourceFile")).toInstance("evaluationData" + File.separator + "DonatAnts_Type4" + File.separator + 
				"source" + File.separator + "8538_pyr_mad_tx1.xml");
		
		bind(TreatmentTransformerChain.class).to(CharaparserTreatmentTransformerChain.class);
		bind(MarkupDescriptionTreatmentTransformer.class).to(OldPerlTreatmentTransformer.class);
		
		bind(boolean.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_parallelProcessing")).toInstance(false);
		bind(int.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_descriptionExtractorRunMaximum")).toInstance(30);
		bind(int.class).annotatedWith(Names.named("MarkupDescriptionTreatmentTransformer_sentenceChunkerRunMaximum")).toInstance(Integer.MAX_VALUE);
		
		bind(String.class).annotatedWith(Names.named("databaseName")).toInstance("testNewCode");
		//markedupdatasets
		bind(String.class).annotatedWith(Names.named("databaseUser")).toInstance("termsuser");
		bind(String.class).annotatedWith(Names.named("databasePassword")).toInstance("termspassword");
		
		bind(IVolumeWriter.class).annotatedWith(Names.named("MarkupCreator_VolumeWriter")).to(XMLVolumeWriter.class); 
		//ToStringVolumeWriter, JSONVolumeWriter, XMLVolumeWriter XML2VolumeWriter
	}
}
