package edu.arizona.biosemantics.semanticmarkup.markupelement.phenology.transform;

public class Time {

	String timeString = null;
	String modifier = null;

	protected String getTime() {
		return timeString;
	}

	protected String getCleanTime() {
		//remove unmatched ),
		timeString = timeString.trim();
		String prev = timeString;
		while(timeString.matches(".*?[\\)\\]\\(\\[].*")){
			if(timeString.matches("^[\\(\\[].*") && !timeString.matches(".*?[\\)\\]].+"))
				timeString = timeString.substring(1);
			if(timeString.matches(".*[\\)\\]]$") && !timeString.matches(".*?[\\(\\[].+"))
				timeString = timeString.substring(0, timeString.length()-1);
			if(prev.compareTo(timeString)==0) break;
			prev = timeString;
		}
		//and leading/trailing puncts
		timeString = timeString.replaceAll("^[ .:;,]+|[ .:;,]+$", "");
		return timeString;
	}
	protected void setTime(String time) {
		this.timeString = time;
	}
	protected String getModifier() {
		return modifier;
	}
	protected String getCleanModifier() {
		//remove unmatched ),
		modifier = modifier.trim();
		String prev = modifier;
		while(modifier.matches(".*?[\\)\\]\\(\\[].*")){
			if(modifier.matches("^[\\(\\[].*") && !modifier.matches(".*?[\\)\\]].+"))
				modifier = modifier.substring(1);
			if(modifier.matches(".*[\\)\\]]$") && !modifier.matches(".*?[\\(\\[].+"))
				modifier = modifier.substring(0, modifier.length()-1);
			if(prev.compareTo(modifier)==0) break;
			prev = modifier;
		}
		//and leading/trailing puncts
		modifier = modifier.replaceAll("^[ .:;,]+|[ .:;,]+$", "");
		return modifier;
	}

	protected void setModifier(String modifier) {
		this.modifier = modifier;
	}
	@Override
	public String toString(){
		return "time = "+timeString+System.getProperty("line.separator")+
				"modifier = "+modifier +System.getProperty("line.separator");
	}
}