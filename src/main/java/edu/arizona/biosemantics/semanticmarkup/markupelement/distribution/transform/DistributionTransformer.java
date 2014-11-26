package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.transform;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Distribution;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Treatment;

public class DistributionTransformer implements IDistributionTransformer {

	@Override
	public void transform(List<DistributionsFile> distributionsFiles) {
		for(DistributionsFile distributionsFile : distributionsFiles) {
			int i = 0;
			for(Treatment treatment : distributionsFile.getTreatments()) {
				for(Distribution distribution : treatment.getDistributions()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("distribution_" + i++);
					statement.setText(distribution.getText());
					
					BiologicalEntity be = new BiologicalEntity();
					be.setName("whole_organism");
					be.setType("structure");
					be.setNameOriginal("");
					be.addCharacters(parse(distribution.getText()));
					//statement.setValues();
					statement.addBiologicalEntity(be);
					
					statements.add(statement);				
					distribution.setStatements(statements);
				}
			}
		}
	}

	@Override
	public LinkedHashSet<Character> parse(String text) {
		//format text, hide [,;] in parentheses
		LinkedHashSet<Character> values = new LinkedHashSet<Character>();
		text = format(text); 
		//collect value
		String[] areas = text.split("[;,]");
		for(int i = 0; i<areas.length; i++){
			String area = areas[i].trim();
			if(area.indexOf("@")>=0){
				values.addAll(allValues(area));
			}else{
				Character c = new Character();
				c.setName("distribution");
				c.setValue(area);
				values.add(c);
			}
		}
		return values;
	}
	
	private LinkedHashSet<Character> allValues(String area) {
		LinkedHashSet<Character> values = new LinkedHashSet<Character>();
		  Pattern p = Pattern.compile("(.*?)\\(([^)]*?@[^)]*?)\\)(.*)");
		  Matcher m = p.matcher(area);
		  if(m.matches()){
			   String com = m.group(1);
			   String partstr = m.group(2);
			   String rest = m.group(3);
			   
			   String[] parts = partstr.split("\\s*@\\s*");
			   
			   for(int i = 0; i<parts.length; i++){
				   Character c = new Character();
					c.setName("distribution");
					c.setValue(com+"("+parts[i]+")"+rest);
					values.add(c);
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

