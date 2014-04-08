/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.transform;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.ecology.model.EcologyFile;


/**
 * @author Hong Cui
 *
 */
public interface IEcologyTransformer {

	void transform(List<EcologyFile> ecologyFiles);
}
