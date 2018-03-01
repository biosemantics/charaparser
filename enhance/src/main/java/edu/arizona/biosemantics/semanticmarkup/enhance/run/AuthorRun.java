package edu.arizona.biosemantics.semanticmarkup.enhance.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.json.JSONObject;
import org.json.XML;

import edu.arizona.biosemantics.common.log.LogLevel;

public class AuthorRun extends Run {
	public static int PRETTY_PRINT_INDENT_FACTOR = 4;
	 
	public AuthorRun() {
		super();
	}

	public void run(File inputDirectory, File outputDirectory) {
		super.run(inputDirectory, outputDirectory);
		File[] xmlFiles = inputDirectory.listFiles();
		XMLOutputter outputter = new XMLOutputter();
		//one xml file => one json file
		for(File xmlFile: xmlFiles){
			String fileName = xmlFile.getName();
			if(!fileName.endsWith(".xml")) continue;
			StringBuffer xmlString = new StringBuffer();
			//read biological entity and relation elements
			//all these elements are sibling nodes
			Document document = null;
			try(InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(xmlFile), "UTF8")) {
				document = saxBuilder.build(inputStreamReader);
			} catch (JDOMException | IOException e) {
				log(LogLevel.ERROR, "Can't read xml from file " + xmlFile.getAbsolutePath(), e);
			}
			 
		    XPathFactory xFactory = XPathFactory.instance();
		    XPathExpression<Element> expr = xFactory.compile("(//biological_entity|//relation)", Filters.element());
	        List<Element> bioRels = expr.evaluate(document);
	        for (Element element : bioRels) {
	        	xmlString.append(outputter.outputString(element));
	        }

	        try {
	        	JSONObject xmlJSONObj = XML.toJSONObject(xmlString.toString());
	        	String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
	        	File jsonFile = new File(outputDirectory, fileName.replaceFirst("\\.xml$",".json"));
	        	OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(jsonFile), "UTF8");
	        	outputStreamWriter.write(jsonPrettyPrintString);
	        	outputStreamWriter.close();
	        	//System.out.println(jsonPrettyPrintString);
	        } catch (Exception e) {
	        	log(LogLevel.ERROR, "Can't create JSON file ", e);
	        	e.printStackTrace();
	        }
		}
	}
	
	
	
	public static void main(String[] args) {
		AuthorRun ar = new AuthorRun();
		ar.run(new File("C:/Users/hongcui/git/charaparser-author/workspace/C2/out"), new File("C:/Users/hongcui/git/charaparser-author/workspace/C2/out"));
/**
 String xmlString = "<biological_entity id=\"o0\" name=\"leaf\" name_original=\"leaves\" src=\"d0_s0\" type=\"structure\"> "
				+ "<character is_modifier=\"false\" name=\"coloration\" src=\"d0_s0\" value=\"red\" /> "
				+ " <character is_modifier=\"false\" name=\"shape\" src=\"d0_s0\" value=\"rounded\" /> "
				+ "<character name=\"length\" src=\"d0_s0\" unit=\"cm\" value=\"3.5\" /> "
				+ " <character name=\"length\" src=\"d0_s0\" unit=\"cm\" value=\"3\" /> "
				+ " <character name=\"width\" src=\"d0_s0\" unit=\"cm\" value=\"4\" />"
				+ " </biological_entity>";
				
JSON:
 
 
		 {"biological_entity": {
    "character": [
        {
            "src": "d0_s0",
            "name": "coloration",
            "is_modifier": false,
            "value": "red"
        },
        {
            "src": "d0_s0",
            "name": "shape",
            "is_modifier": false,
            "value": "rounded"
        },
        {
            "unit": "cm",
            "src": "d0_s0",
            "name": "length",
            "value": 3.5
        },
        {
            "unit": "cm",
            "src": "d0_s0",
            "name": "length",
            "value": 3
        },
        {
            "unit": "cm",
            "src": "d0_s0",
            "name": "width",
            "value": 4
        }
    ],
    "src": "d0_s0",
    "name": "leaf",
    "name_original": "leaves",
    "id": "o0",
    "type": "structure"
}}

		 
		 
		 
		 */
	}

}
