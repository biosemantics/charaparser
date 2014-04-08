/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.EcologyFileList;
/**
 * @author Hong Cui
 *
 */
public interface IEcologyReader {
	EcologyFileList read(String inputDirectory) throws Exception;
}

