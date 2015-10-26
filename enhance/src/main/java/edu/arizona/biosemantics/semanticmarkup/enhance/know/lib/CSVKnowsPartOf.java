package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms.SynonymSet;

public class CSVKnowsPartOf implements KnowsPartOf {

	private HashMap<String, Set<String>> partOfMap;
	private IInflector inflector;

	public CSVKnowsPartOf(IInflector inflector) {
		this.inflector = inflector;
		partOfMap = new HashMap<String, Set<String>>();
		try {
			CSVReader reader = new CSVReader(new FileReader("part-of.csv"));
			List<String[]> lines = reader.readAll();
			
			for(String[] line : lines) {
				String bearer = line[0].trim().toLowerCase();
				String beared = line[1].trim().toLowerCase();
				if(!partOfMap.containsKey(bearer)) 
					partOfMap.put(bearer, new HashSet<String>());
				partOfMap.get(bearer).add(beared);
			}	
		} catch(IOException e) {
			log(LogLevel.ERROR, "Can't read CSV", e);
		}
	}
	
	@Override
	public boolean isPartOf(String part, String parent) {
		if(part.isEmpty() || parent.isEmpty())
			return false;
		
		part = part.trim().toLowerCase();
		parent = parent.trim().toLowerCase();
		String[] partParts = StringUtils.split(part, " ");
		String[] parentParts = StringUtils.split(parent, " ");
		
		String lastPartPart = partParts[partParts.length - 1];
		String lastParentPart = parentParts[parentParts.length - 1];
		lastPartPart = inflector.getSingular(lastPartPart).replaceAll("_", " ");
		lastParentPart = inflector.getSingular(lastParentPart).replaceAll("_", " ");
		
		partParts[partParts.length - 1] = lastPartPart;
		parentParts[parentParts.length - 1] = lastParentPart;
		String normalizedParent = StringUtils.join(parentParts, " ");
		String normalizedPart = StringUtils.join(partParts, " ");
		return partOfMap.containsKey(normalizedParent) && partOfMap.get(normalizedParent).contains(normalizedPart);
	}

}
