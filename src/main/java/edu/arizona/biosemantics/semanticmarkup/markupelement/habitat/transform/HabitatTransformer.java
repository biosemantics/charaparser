package edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.transform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.model.HabitatsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.model.HabitatsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitat.model.Treatment;


public class HabitatTransformer {

	private String seedNList = "|";
	private String seedMList = "|";
	
	private HashMap<Treatment, String> habitatValues = new HashMap<Treatment, String>();
	private Pattern tagHabitatPattern = Pattern.compile("(.*?)(\\w+)(\\s+<.*)");
	private Pattern findNewPattern = Pattern.compile("(.*?)(\\w+)(\\s+<.*)");
	
	public void transform(HabitatsFileList habitatsFileList) {
		collectSeeds(habitatsFileList);
		bootstrap(habitatsFileList);
		transformTreatments(habitatsFileList);
	}
	
	private void transformTreatments(HabitatsFileList habitatsFileList) {
		for(HabitatsFile habitatsFile : habitatsFileList.getHabitatsFiles()) {
			for(Treatment treatment : habitatsFile.getTreatments()) {
				String habitatValue = habitatValues.get(treatment);
				treatment.getHabitat().setText(habitatValue);
			}
		}
	}
	
	private void bootstrap(HabitatsFileList habitatsFileList) {
		int discovery = 0;
		HashMap<Treatment, String> todos = null;
		do { 
			discovery = 0;			
			todos = getTodos(habitatsFileList);
			
			//this one returns a list of entries of the form sourceFileId@TextOfHabitat
			//todos = hpDbA.selectRecords("source, habitat_string", "isnull(habitat_values)", "", "");
			discovery += tagHabitatStrings(todos);
			discovery += findNew(todos);			
		} while (discovery > 0);

		for(Entry<Treatment, String> todo : todos.entrySet()) {
			String todoString = todo.getValue().trim().toLowerCase();
			if(todoString.length() > 1){
				todoString = todoString.replaceAll(">,", "@").replaceAll("(?<=s),", "@").replaceAll("[<>{}]", "");
				habitatValues.put(todo.getKey(), "??"+todoString);
			}
		}
	}

	private HashMap<Treatment, String> getTodos(HabitatsFileList habitatsFileList) {
		HashMap<Treatment, String> result = new HashMap<Treatment, String>();
		for(HabitatsFile habitatsFile : habitatsFileList.getHabitatsFiles()) {
			for(Treatment treatment : habitatsFile.getTreatments()) {
				if(!habitatValues.containsKey(treatment)) {
					String treatmentHabitatString = treatment.getHabitat().getText();
					result.put(treatment, treatmentHabitatString);
				}
			}
		}
		return result;
	}
	
	
	private int tagHabitatStrings(HashMap<Treatment, String> todos) {
		int discovery = 0;
		int index = 0;
		Iterator<Entry<Treatment, String>> todosIterator = todos.entrySet().iterator();
		while (todosIterator.hasNext()) {
			Entry<Treatment, String> habitat = todosIterator.next();
			String habitatString = habitat.getValue().trim().toLowerCase();
			Treatment treatment = habitat.getKey();
			habitatString = mark(habitatString, seedNList, "<", ">");
			habitatString = mark(habitatString, seedMList, "{", "}"); //could have <{ }>
			if(isDone(habitatString)){
				discovery += modifierBeforeH(tagHabitatPattern, habitatString);
				habitatString = habitatString.replaceAll(">,", "@").replaceAll("[<>{}]", "");
				habitatValues.put(treatment, habitatString);
				todosIterator.remove();
			} else {
				//is this really necessary?
				//text.set(index++, src+"@"+hs);
				//as hashmap repalced with arraylist no order given to put at specific position
				//text.set(index++, src+"@"+hs);
			}
		}
		return discovery;
	}
	
	private String mark(String s, String list, String l, String r){
		s = s.replaceAll("["+l+r+"]", "");
		String[] ns = list.split("\\|");
		for(int i = 0; i<ns.length; i++){
			if(!ns[i].equals("")){
				//escape 
				ns[i] = ns[i].replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]").replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
				s = s.replaceAll("\\b"+Pattern.quote(ns[i])+"\\b", l+ns[i]+r);
			}
		}
		return s;
	}
	
	private boolean isDone(String string){
	//	String sc = string;
		string = string.replaceAll(">,", "").replaceAll("},", "");
		if(string.indexOf(",") < 0){
			return true;
		}
		return false;
	}
	
	private int modifierBeforeH(Pattern p, String h) {
		int discovery = 0;
		Matcher m = p.matcher(h);
		while(m.matches()){
			h = m.group(3);
			String temp = m.group(2);
			if(temp.length()>1 && seedMList.indexOf("|"+temp+"|") < 0){
				this.seedMList+=temp+"|";
				discovery++;
			}			
			m = p.matcher(h);
		}
		return discovery;
	}
	
	private int findNew(HashMap<Treatment, String> todos) {
		int discovery = 0;
		Iterator<Entry<Treatment, String>> it  = todos.entrySet().iterator();
		while(it.hasNext()){
			String habitatString = (String)it.next().getValue().trim().toLowerCase();
			discovery += modifierBeforeH(findNewPattern, habitatString);
		}
		return discovery;
	}
	
	
	/**
	 * 
	 * @param treatments 
	 * @return a regexp consisting a list of seeds
	 */
	private void collectSeeds(HabitatsFileList habitatsFileList) {
		HashMap<Treatment, String> habitatStrings = getHabitatStrings(habitatsFileList);
		for(Entry<Treatment, String> habitat : habitatStrings.entrySet()) {
			String habitatString = habitat.getValue().trim().toLowerCase();
			habitatString = habitatString.replaceAll("\\s*\\([^)]*\\)\\s*", " ");
			habitatString = habitatString.replaceAll("[\\W|\\s]+$", "");
			//the last word in a statement must be a N
			String seed = habitatString.substring(habitatString.lastIndexOf(" ")+1, habitatString.length()).trim();
			if(seed.length() > 1 && seedNList.indexOf("|"+seed+"|") < 0){
				seedNList +=seed+"|";
			}
			
			//"distributed sites"
			if(habitatString.indexOf(",") < 0 && habitatString.indexOf(" ")>=0 && 
					habitatString.indexOf(" ") == habitatString.lastIndexOf(" ") && habitatString.indexOf(" and ")<0){
				String[] t = habitatString.split("\\s+");
				if(t[t.length-2].length()> 1 && seedMList.indexOf("|"+t[t.length-2]+"|") < 0){
					seedMList +=t[t.length-2]+"|";
				}
			}
			
			//"meadows and tundra"
			if(habitatString.matches(".*?\\w+ and \\w+$")){
				String[] t = habitatString.split("\\s+");
				if(t[t.length-3].length()> 1 && seedNList.indexOf("|"+t[t.length-3]+"|") < 0){
					seedNList +=t[t.length-3]+"|";
				}
			}
		}										
	}
	
	private HashMap<Treatment, String> getHabitatStrings(HabitatsFileList habitatsFileList) {
		HashMap<Treatment, String> result = new HashMap<Treatment, String>();
		
		for(HabitatsFile habitatsFile : habitatsFileList.getHabitatsFiles()) {
			for(Treatment treatment : habitatsFile.getTreatments()) {
				String treatmentHabitatString = treatment.getHabitat().getText();
				result.put(treatment, treatmentHabitatString);
			}
		}
		return result;
	}
}
