package semanticMarkup.io.input.lib.word;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import semanticMarkup.io.input.extract.lib.DistributionTreatmentRefiner;
import semanticMarkup.io.input.extract.lib.FloweringTimeTreatmentRefiner;

/**
 * XMLWordVolumeReader reads a list of treatments from a by the previous charaparser version termed Type 1 input format. This is a
 * description in Microsoft Word format. 
 * The XMLWordVolumeReader expects the "WordVolumeReader_Sourcefile" to point to the .XML file contained in the word file
 * @author rodenhausen
 */
public class XMLWordVolumeReader extends AbstractWordVolumeReader {

	/**
	 * @param filepath
	 * @param styleStartPattern
	 * @param styleNamePattern
	 * @param styleKeyPattern
	 * @param tribegennamestyle
	 * @param styleMappingFile
	 * @param distributionTreatmentRefiner
	 * @param floweringTimeTreatmentRefiner
	 * @param temporaryPath
	 */
	@Inject
	public XMLWordVolumeReader(@Named("WordVolumeReader_Sourcefile") String filepath,
			@Named("WordVolumeReader_StyleStartPattern") String styleStartPattern,
			@Named("WordVolumeReader_StyleNamePattern") String styleNamePattern,
			@Named("WordVolumeReader_StyleKeyPattern") String styleKeyPattern,
			@Named("WordVolumeReader_Tribegennamestyle") String tribegennamestyle, 
			@Named("WordVolumeReader_StyleMappingFile") String styleMappingFile, 
			DistributionTreatmentRefiner distributionTreatmentRefiner, 
			FloweringTimeTreatmentRefiner floweringTimeTreatmentRefiner,
			@Named("temporaryPath")String temporaryPath) {
		super(filepath, styleStartPattern, styleNamePattern, styleKeyPattern,
				tribegennamestyle, styleMappingFile, distributionTreatmentRefiner,
				floweringTimeTreatmentRefiner, temporaryPath);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected InputStream getXMLInputStream() throws Exception {
		return new FileInputStream(this.filePath);
	}
}