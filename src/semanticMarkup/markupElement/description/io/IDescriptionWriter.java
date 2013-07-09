package semanticMarkup.markupElement.description.io;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import semanticMarkup.markupElement.description.model.DescriptionsFileList;


/**
 * DescriptionOutputter replaces the description elements of source XML documents
 * by a marked up descriptions
 * @author general
 */
public interface IDescriptionWriter {
	
	public void write(DescriptionsFileList descriptionsFileList, String writeDirectory) throws Exception;

}
