package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsClassHierarchy;

public class CSVKnowsClassHierarchy implements KnowsClassHierarchy {

	private HashMap<String, Set<String>> subclassMap;
	private HashMap<String, Set<String>> superclassMap;
	private IInflector inflector;

	public CSVKnowsClassHierarchy(IInflector inflector) {
		this.inflector = inflector;
		subclassMap = new HashMap<String, Set<String>>();
		superclassMap = new HashMap<String, Set<String>>();
		try(CSVReader reader = new CSVReader(new FileReader("class-hiearchy.csv"))) {
			List<String[]> lines = reader.readAll();
			
			for(String[] line : lines) {
				String superclass = line[0].trim().toLowerCase();
				String subclass = line[1].trim().toLowerCase();
				if(!superclassMap.containsKey(superclass)) 
					superclassMap.put(superclass, new HashSet<String>());
				superclassMap.get(superclass).add(subclass);
				if(!subclassMap.containsKey(subclass)) 
					subclassMap.put(subclass, new HashSet<String>());
				subclassMap.get(subclass).add(superclass);
			}	
		} catch(IOException e) {
			log(LogLevel.ERROR, "Can't read CSV", e);
		}
	}
	
	@Override
	public boolean isSuperclass(String superclass, String clazz) {
		if(superclassMap.containsKey(superclass)) 
			return superclassMap.get(superclass).contains(clazz);
		return false;
	}

	@Override
	public boolean isSubclass(String subclass, String clazz) {
		if(subclassMap.containsKey(subclass)) 
			return subclassMap.get(subclass).contains(clazz);
		return false;
	}

	@Override
	public Set<String> getSuperclasses(String clazz) {
		return superclassMap.get(clazz);
	}

	@Override
	public Set<String> isSubclasses(String clazz) {
		return subclassMap.get(clazz);
	}

}
