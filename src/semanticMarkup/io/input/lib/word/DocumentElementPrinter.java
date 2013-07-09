package semanticMarkup.io.input.lib.word;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import semanticMarkup.model.Treatment;


public class DocumentElementPrinter {

	public String print(LinkedHashMap<Treatment, LinkedHashMap<String, ArrayList<DocumentElement>>> documentElements) {
		StringBuffer stringBuffer = new StringBuffer();
		for(Entry<Treatment, LinkedHashMap<String, ArrayList<DocumentElement>>> treatmentsDocumentElements : documentElements.entrySet()) {
			stringBuffer.append("<treatment>\n");
			for(Entry<String, ArrayList<DocumentElement>> styledDocumentElements : treatmentsDocumentElements.getValue().entrySet()) {
				stringBuffer.append("<style ");
				stringBuffer.append("name=\"");
				stringBuffer.append(styledDocumentElements.getKey());
				stringBuffer.append("\">\n");
				for(DocumentElement documentElement : styledDocumentElements.getValue()) {
					stringBuffer.append("<documentElement");
					if(documentElement.getProperty()!=null) {
						stringBuffer.append(" name=\"");
						stringBuffer.append(documentElement.getProperty());
						stringBuffer.append("\">");
					} else 
						stringBuffer.append(">");
					stringBuffer.append(documentElement.getText());
					stringBuffer.append("</documentElement>\n");
				}
				stringBuffer.append("</style>\n");
			}
			stringBuffer.append("</treatment>\n");
		}
		return stringBuffer.toString();
	}
}
