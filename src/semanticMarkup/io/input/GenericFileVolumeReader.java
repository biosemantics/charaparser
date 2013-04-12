package semanticMarkup.io.input;

import java.util.List;

import semanticMarkup.core.Treatment;

public class GenericFileVolumeReader extends AbstractFileVolumeReader {

	public GenericFileVolumeReader(String filePath) {
		super(filePath);
	}

	@Override
	public List<Treatment> read() throws Exception {
		//TODO validate input against the type 2 and type 4 xml schemas, if there is a fit delegate to appropriate reader
		//if not try type1 (validate possible?
		
		
		
		return null;
	}
}
