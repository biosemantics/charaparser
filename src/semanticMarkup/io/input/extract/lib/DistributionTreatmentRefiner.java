package semanticMarkup.io.input.extract.lib;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semanticMarkup.core.Treatment;
import semanticMarkup.core.ValueTreatmentElement;
import semanticMarkup.io.input.extract.ITreatmentRefiner;

/**
 * DistributionTreatmentRefiner creates new descriptive treatment elements from distribution describing input
 * @author rodenhausen
 */
public class DistributionTreatmentRefiner implements ITreatmentRefiner {

	private Pattern allValuesPattern = Pattern.compile("(.*?)\\(([^)]*?@[^)]*?)\\)(.*)");
	private Pattern formatPattern = Pattern.compile("(.*?)(\\([^)]*,[^)]*\\))(.*)"); 

	@Override
	public void refine(Treatment treatment, String clue, String nameForValues) {
		ArrayList<String> values = new ArrayList<String>();
		clue = format(clue); 
		
		//collect values
		String[] areas = clue.split("[;,]");
		for(int i = 0; i<areas.length; i++) {
			String area = areas[i].trim();
			if(area.indexOf("@")>=0)
				values.addAll(allValues(area));				
			else
				values.add(area);
		}
		
		//form elements
		for(String value : values) {
			String area = value;
			if(area!="")
				treatment.addTreatmentElement(new ValueTreatmentElement(nameForValues, area));
		}	
	}

	
	/**
	 * mexican (a@b) =>mexican(a), mexican(b)
	 * @param area
	 * @return
	 */
	private ArrayList<String> allValues(String area) {
		ArrayList<String> values = new ArrayList<String>();
		Matcher m = allValuesPattern.matcher(area);
		if(m.matches()) {
			String com = m.group(1);
			String partstr = m.group(2);
			String rest = m.group(3);
			
			String[] parts = partstr.split("\\s*@\\s*");
			
			for(int i = 0; i<parts.length; i++)
				values.add(com+"("+parts[i]+")"+rest);
		}
		return values;
	}

	private String format(String text) {
		String formated = "";
		Matcher m = formatPattern.matcher(text);
		while(m.matches()) {
			formated += m.group(1);
			String t = m.group(2);
			text = m.group(3);
			t = t.replaceAll(",", "@");
			formated +=t;
			m = formatPattern.matcher(text);
		}
		formated +=text;
		return formated;
	}
}
