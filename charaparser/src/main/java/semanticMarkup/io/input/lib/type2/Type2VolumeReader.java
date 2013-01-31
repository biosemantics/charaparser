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

import com.google.inject.name.Named;


public class Type2VolumeReader extends  AbstractFileVolumeReader {

	private static final String NAMESPACE_URI = "http://schemas.openxmlformats.org/unknown";
	private static final Namespace namespace = Namespace.getNamespace("unknown", NAMESPACE_URI);
	private static final XPathFactory factory = XPathFactory.instance();	
	private static final Filter<Element> elementFilter = new ElementFilter();
	private static final Filter<Attribute> attributeFilter = new AttributeFilter();
	private static final Filter contentFilter = new ContentFilter();
	private XPathExpression<Element> descriptionExpression = 
			factory.compile("//treatment/description", elementFilter, null, namespace);
	
	public Type2VolumeReader(@Named("Type2VolumeReader_Sourcefile") String filepath) {
		super(filepath);
	}

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
		}
		
		return treatments;
	}

}
