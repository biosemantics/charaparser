package semanticMarkup.io.input;

import java.util.List;

import semanticMarkup.core.Treatment;


public interface IVolumeReader {

	public List<Treatment> read() throws Exception;
	
}
