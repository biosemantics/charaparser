package semanticMarkup.io.input.lib.word;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.AttributeFilter;
import org.jdom2.filter.ContentFilter;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import semanticMarkup.model.Treatment;

import com.google.inject.Inject;


public class WordDocumentElementsExtractor {

	private String styleStartPattern;
	private String styleNamePattern;
	private String styleKeyPattern;
	private String tribegennamestyle;
	
	private static final String NAMESPACE_URI = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final Namespace wNamespace = Namespace.getNamespace("w", NAMESPACE_URI);
	private static final Filter<Element> elementFilter = new ElementFilter();
	private static final Filter<Attribute> attributeFilter = new AttributeFilter();
	private static final Filter contentFilter = new ContentFilter();
	private static final XPathFactory factory = XPathFactory.instance();	
	private XPathExpression<Element> wpExpression = factory.compile(
			"/w:document/w:body/w:p", elementFilter, null, wNamespace);
	private XPathExpression<Attribute> styleExpression = 
			factory.compile("./w:pPr/w:pStyle/@w:val", attributeFilter
					, null, wNamespace);
	private XPathExpression<Element> rTabExpression = 
			factory.compile("./w:r/w:tab", elementFilter, null, wNamespace);
	private XPathExpression<Element> rTExpression = 
			factory.compile("./w:r/w:t", elementFilter, null, wNamespace);
	private XPathExpression<Element> rExpression = 
			factory.compile("./w:r", elementFilter, null, wNamespace);
	private XPathExpression<Element> rPrExpression = 
			factory.compile("./w:rPr", elementFilter, null, wNamespace);
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private XPathExpression nameCheckExpression = factory.compile(
			"./w:" + tribegennamestyle, contentFilter, null, wNamespace);
	private XPathExpression<Element> tExpression = 
			factory.compile("./w:t", elementFilter, null, wNamespace);
	private InputStream fileInputStream;
	
	@Inject
	public WordDocumentElementsExtractor(InputStream fileInputStream, String styleStartPattern, String styleNamePattern, String styleKeyPattern,
			 String tribegennamestyle) {
		this.fileInputStream = fileInputStream;
		this.styleStartPattern = styleStartPattern;
		this.styleNamePattern = styleNamePattern;
		this.styleKeyPattern = styleKeyPattern;
		this.tribegennamestyle = tribegennamestyle;
	}

	public LinkedHashMap<Treatment, LinkedHashMap<String, ArrayList<DocumentElement>>> 
			extract() throws JDOMException, IOException {
		int treatmentId = 0;
		LinkedHashMap<Treatment, LinkedHashMap<String, ArrayList<DocumentElement>>> 
			documentElements = new LinkedHashMap<Treatment, 
			LinkedHashMap<String, ArrayList<DocumentElement>>>();
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(this.fileInputStream);
		List<Element> wpElements = wpExpression.evaluate(document);
		Treatment treatment = null;
		LinkedHashMap<String, ArrayList<DocumentElement>> styleTextMappingForTreatment = null;
		for(Element wpElement : wpElements) {
			Attribute styleAttribute = styleExpression.evaluateFirst(wpElement);
			if(styleAttribute!=null) {
				String style = styleAttribute.getValue();
				if (style.matches(styleStartPattern)) {
					treatment = new Treatment(String.valueOf(treatmentId++));
					styleTextMappingForTreatment = 
							new LinkedHashMap<String, ArrayList<DocumentElement>>();
					documentElements.put(treatment, styleTextMappingForTreatment);
				}
				
				if(!styleTextMappingForTreatment.containsKey(style)) 
					styleTextMappingForTreatment.put(style, 
							new ArrayList<DocumentElement>());
				ArrayList<DocumentElement> documentElementsForStyle	= 
						styleTextMappingForTreatment.get(style);
				if (style.matches(styleStartPattern) 
						|| style.matches(styleNamePattern)) {
					putWpTextForName(documentElementsForStyle, wpElement);
				}else if(style.matches(styleKeyPattern)) {
					//try to separate a key "statement" from "determination"
					putWpTextForKey(documentElementsForStyle, wpElement);
				}else {		
					putWpText(documentElementsForStyle, wpElement);
				}
			}
		}
		return documentElements;
	}

	private void putWpText(ArrayList<DocumentElement> documentElementsForStyle,	
			Element wpElement) {
		StringBuffer buffer = new StringBuffer();
		List<Element> elements = rTExpression.evaluate(wpElement);
		for (Element element : elements) {
			buffer.append(element.getText());
		}
		DocumentElement documentElement = 
				new DocumentElement(buffer.toString().replaceAll("\\s+", " ").trim());
		documentElementsForStyle.add(documentElement);
	}

	private void putWpTextForKey(ArrayList<DocumentElement> documentElementsForStyle,
			Element wpElement) {
		StringBuffer text = new StringBuffer();
		List<Element> tabElements = rTabExpression.evaluate(wpElement);
		//make w:tab also w:t named such that in the next step they 
		//can all be gathered
		for(Element tabElement : tabElements) {
			tabElement.setText("###");
			tabElement.setName("t");
		}
		List<Element> textElements = rTExpression.evaluate(wpElement);
		for(Element textElement : textElements) {
			text.append(textElement.getTextTrim() + " ");
		}
		DocumentElement documentElement = 
				new DocumentElement(text.toString().trim());
		documentElementsForStyle.add(documentElement);
	}

	private void putWpTextForName(ArrayList<DocumentElement> documentElementsForStyle,
			Element wpElement) {
		List<Element> rElements = rExpression.evaluate(wpElement);
		for (Element rElement : rElements) {
			//find smallcap
			Element rprElement = rPrExpression.evaluateFirst(rElement);
			String acase = null;
			if (rprElement != null && 
					nameCheckExpression.evaluateFirst(rprElement) != null) {
				acase = tribegennamestyle;
			}
			
			// collect text
			StringBuffer buffer = new StringBuffer();
			List<Element> tElements = tExpression.evaluate(rElement);
			for (Element tElement : tElements) {
				if(tElement.getText().trim().length() == 0)
					continue;
				buffer.append(tElement.getText()).append(" ");
			}
			DocumentElement documentElement = 
					new DocumentElement(buffer.toString().replaceAll("\\s+", " ").trim());
			if(acase != null)
				documentElement.setProperty(tribegennamestyle);
			documentElementsForStyle.add(documentElement);
		}
	}
}
