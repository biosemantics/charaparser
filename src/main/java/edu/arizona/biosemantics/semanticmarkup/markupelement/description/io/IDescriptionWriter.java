package edu.arizona.biosemantics.semanticmarkup.markupelement.description.io;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFileList;
//transforming

/**
 * DescriptionOutputter replaces the description elements of source XML documents
 * by a marked up descriptions
 * @author general
 */
public interface IDescriptionWriter {
	
	public void write(DescriptionsFileList descriptionsFileList, String writeDirectory) throws Exception;

}
