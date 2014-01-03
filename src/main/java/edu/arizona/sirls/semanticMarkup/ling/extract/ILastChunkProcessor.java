package edu.arizona.sirls.semanticMarkup.ling.extract;

import java.util.List;

import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import edu.arizona.sirls.semanticMarkup.model.Element;


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
