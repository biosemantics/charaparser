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
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms.SynonymSet;

public class CSVKnowsPartOf implements KnowsPartOf {

	private HashMap<String, Set<String>> partOfMap;
	private KnowsSynonyms knowsSynonyms;
	private IInflector inflector;

	public CSVKnowsPartOf(String partOfFile, KnowsSynonyms knowsSynonyms, IInflector inflector) {
		this.inflector = inflector;
		this.knowsSynonyms = knowsSynonyms;
		partOfMap = new HashMap<String, Set<String>>();
		try(CSVReader reader = new CSVReader(new FileReader(partOfFile))) {
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
		
		//only temporary for evaluation: inflector doesnt create correct singular for e.g. 'area', 'cypsela'
		if(!lastPartPart.endsWith("a") && !lastPartPart.endsWith("i")) 
			lastPartPart = inflector.getSingular(lastPartPart).replaceAll("_", " ");
		if(!lastParentPart.endsWith("a") && !lastParentPart.endsWith("i"))
			lastParentPart = inflector.getSingular(lastParentPart).replaceAll("_", " ");
		
		partParts[partParts.length - 1] = lastPartPart;
		parentParts[parentParts.length - 1] = lastParentPart;
		String normalizedParent = StringUtils.join(parentParts, " ");
		String normalizedPart = StringUtils.join(partParts, " ");
		
		Set<String> parts = new HashSet<String>();
		parts.add(normalizedPart);
		Set<SynonymSet> partSynonymSets = knowsSynonyms.getSynonyms(normalizedPart);
		for(SynonymSet synonymSet : partSynonymSets) 
			parts.addAll(synonymSet.getSynonyms());
			
		Set<String> parents = new HashSet<String>();
		parents.add(normalizedParent);
		Set<SynonymSet> parentSynonymSets = knowsSynonyms.getSynonyms(normalizedParent);
		for(SynonymSet synonymSet : parentSynonymSets) 
			parents.addAll(synonymSet.getSynonyms());
		
		for(String aPart : parts) {
			for(String aParent : parents) {
				if(partOfMap.containsKey(aParent) && partOfMap.get(aParent).contains(aPart)) {
					return true;
				}
			}
		}
		return false;
	}

}