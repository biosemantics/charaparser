package semanticMarkup.io.output.lib;

import java.io.File;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.output.AbstractFileVolumeWriter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class JSONVolumeWriter extends AbstractFileVolumeWriter {

	@Inject
	public JSONVolumeWriter(@Named("Run_OutFile") String filePath) {
		super(filePath);
	}

	@Override
	public void write(List<Treatment> treatments) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
	    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
	    writer.writeValue(new File(filePath + ".json"), treatments);
	}

}
