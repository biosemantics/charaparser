package semanticMarkup.io.input.lib.type2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.AttributeFilter;
import org.jdom2.filter.ContentFilter;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.io.input.AbstractFileVolumeReader;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Type2VolumeReader reads a list of treatments from a by the previous charaparser version termed Type 2 input format. This is a
 * description in XML format.
 * TODO Use JAXB instead of JDOM; Extract all content instead of description only
 * @author rodenhausen
 */
public class Type2VolumeReader extends  AbstractFileVolumeReader {

	private static final String NAMESPACE_URI = "http://schemas.openxmlformats.org/unknown";
	private static final Namespace namespace = Namespace.getNamespace("unknown", NAMESPACE_URI);
	private static final XPathFactory factory = XPathFactory.instance();	
	private static final Filter<Element> elementFilter = new ElementFilter();
	private static final Filter<Attribute> attributeFilter = new AttributeFilter();
	private static final Filter contentFilter = new ContentFilter();
	private XPathExpression<Element> descriptionExpression = 
			factory.compile("//treatment/description", elementFilter, null, namespace);
	
	/**
	 * @param filepath
	 */
	@Inject
	public Type2VolumeReader(@Named("Type2VolumeReader_Sourcefile") String filepath) {
		super(filepath);
	}

	@Override
	public List<Treatment> read() throws Exception {
		List<Treatment> treatments = new ArrayList<Treatment>();
		
		File sourceDirectory = new File(filePath);
		File[] files =  sourceDirectory.listFiles();
		SAXBuilder builder = new SAXBuilder();
		int total = files.length;
		
		for(int i = 0; i<total; i++) {
			Treatment treatment = new Treatment(String.valueOf(i));
		
			File file = files[i];
			Document doc = builder.build(file);
			Element root = doc.getRootElement();
			List<Element> descriptions = descriptionExpression.evaluate(root);
			for(Element description : descriptions) {
				String text = description.getTextNormalize();
				treatment.addTreatmentElement(new ValueTreatmentElement("description", text));
			}
			treatments.add(treatment);
		}
		
		return treatments;
	}

}
