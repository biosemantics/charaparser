package edu.arizona.biosemantics.semanticmarkup.ling.extract;

import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.model.Element;


/**
 * ILastChunkProcessor is used for the last chunk appearing in a sentence
 * @author rodenhausen
 */
public interface ILastChunkProcessor {

	/**
	 * @param processingContext
	 * @return list of DescriptionTreatmentElements resulting from processing information stored in processingContext
	 */
	public List<? extends Element> process(ProcessingContext processingContext);

}
