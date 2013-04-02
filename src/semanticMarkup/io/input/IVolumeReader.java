package semanticMarkup.io.input;

import java.util.List;

import semanticMarkup.core.Treatment;

/**
 * IVolumeReader reads a list of treatments given treatment descriptions
 * @author rodenhausen
 */
public interface IVolumeReader {

	/**
	 * @return list of treatments read
	 * @throws Exception
	 */
	public List<Treatment> read() throws Exception;
	
}
