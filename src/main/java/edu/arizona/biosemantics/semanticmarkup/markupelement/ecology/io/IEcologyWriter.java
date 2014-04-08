/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.EcologyFileList;

/**
 * @author Hong Cui
 *
 */
public interface IEcologyWriter {

	void write(EcologyFileList ecologyFileList,
			String outputDirectory) throws Exception;
}
