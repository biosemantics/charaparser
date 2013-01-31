package semanticMarkup.io.output;

import java.util.List;

import semanticMarkup.core.Treatment;


public interface IVolumeWriter {

	public void write(List<Treatment> treatments) throws Exception;
	
}
