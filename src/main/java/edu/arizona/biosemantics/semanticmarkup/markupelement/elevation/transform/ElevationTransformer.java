/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.transform;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Value;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Elevation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.Treatment;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.ElevationsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.transform.IElevationTransformer;

/**
 * @author Hong Cui
 *
 */
public class ElevationTransformer implements IElevationTransformer {

	/* (non-Javadoc)
	 * @see edu.arizona.biosemantics.semanticmarkup.markupelement.elevation.model.transform.IElevationTransformer#transform(java.util.List)
	 */
	@Override
	public void transform(List<ElevationsFile> elevationsFiles) {
		for(ElevationsFile elevationsFile : elevationsFiles) {
			int i = 0;
			for(Treatment treatment : elevationsFile.getTreatments()) {
				for(Elevation elevation : treatment.getElevations()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("distribution" + i++);
					statement.setText(elevation.getText());
					statement.setValues(parse(elevation.getText()));
					statements.add(statement);				
					elevation.setStatements(statements);
				}
			}
		}

	}
	
	@Override
	public List<Value> parse(String text) {
		//format text, hide [,;] in parentheses
		ArrayList<Value> values = new ArrayList<Value>();
		text = format(text); 
		//collect value
		String[] areas = text.split("[;,]");
		for(int i = 0; i<areas.length; i++){
			String area = areas[i].trim();
			if(area.indexOf("@")>=0){
				values.addAll(allValues(area));
			}else{
				values.add(new Value(area));
			}
		}
		return values;
	}
	
	private ArrayList<Value> allValues(String area) {
		  ArrayList<Value> values = new ArrayList<Value>();
		  Pattern p = Pattern.compile("(.*?)\\(([^)]*?@[^)]*?)\\)(.*)");
		  Matcher m = p.matcher(area);
		  if(m.matches()){
			   String com = m.group(1);
			   String partstr = m.group(2);
			   String rest = m.group(3);
			   
			   String[] parts = partstr.split("\\s*@\\s*");
			   
			   for(int i = 0; i<parts.length; i++){
				    values.add(new Value(com+"("+parts[i]+")"+rest));
				   }
			  }
		  return values;
		 }

	 public static String format(String text) {
		  String formated = "";
		  Pattern p = Pattern.compile("(.*?)(\\([^)]*,[^)]*\\))(.*)"); 
		  Matcher m = p.matcher(text);
		  while(m.matches()){
			   formated += m.group(1);
			   String t = m.group(2);
			   text = m.group(3);
			   t = t.replaceAll(",", "@");
			   formated +=t;
			   m = p.matcher(text);
			  }
		  formated +=text;
		  return formated;
		 }
	
	
}
