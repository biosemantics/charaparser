package semanticMarkup.io.output.lib;

import java.io.File;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import semanticMarkup.io.output.AbstractFileVolumeWriter;
import semanticMarkup.model.Treatment;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * JSONVolumeWriter writes a list of treatments to JSON format
 * @author rodenhausen
 */
public class JSONVolumeWriter extends AbstractFileVolumeWriter {

	/**
	 * @param outDirectory
	 */
	@Inject
	public JSONVolumeWriter(@Named("Run_OutDirectory") String outDirectory) {
		super(outDirectory);
	}

	@Override
	public void write(List<Treatment> treatments) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
	    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
	    writer.writeValue(new File(outDirectory + File.separator + "treatments.json"), treatments);
	}

}
