package semanticMarkup.io.output;

import java.util.List;

import semanticMarkup.model.Treatment;

/**
 * IVolumeWriter writes a list of treatments out
 * @author rodenhausen
 */
public interface IVolumeWriter {

	/**
	 * @param treatments to write
	 * @throws Exception
	 */
	public void write(List<Treatment> treatments) throws Exception;
	
}
