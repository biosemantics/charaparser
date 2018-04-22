package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.AdjectiveReplacementForNoun;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.LearnException;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;

public class NoTerminologyLearner implements ITerminologyLearner {

	@Override
	public void learn(List<AbstractDescriptionsFile> descriptionsFiles, String glossaryTable) throws LearnException {

	}

	@Override
	public void readResults(List<AbstractDescriptionsFile> descriptionsFiles) {

	}

	@Override
	public Set<String> getSentences() {
		return new HashSet<String>();
	}

	@Override
	public int countMatchingSentences(String phrase) {
		return 0;
	}

	@Override
	public Map<Description, LinkedHashMap<String, String>> getSentencesForOrganStateMarker() {
		return new HashMap<Description, LinkedHashMap<String, String>>();
	}

	@Override
	public List<String> getAdjNouns() {
		return new ArrayList<String>();
	}

	@Override
	public Map<String, String> getAdjNounSent() {
		return new HashMap<String, String>();
	}

	@Override
	public Set<String> getBracketTags() {
		return new HashSet<String>();
	}

	@Override
	public Set<String> getWordRoleTags() {
		return new HashSet<String>();
	}

	@Override
	public Map<String, Map<String, Set<Integer>>> getWordToSources() {
		return new HashMap<String, Map<String, Set<Integer>>>();
	}

	@Override
	public Map<String, Set<String>> getRoleToWords() {
		return new HashMap<String, Set<String>>();
	}

	@Override
	public Map<String, Set<String>> getWordsToRoles() {
		return new HashMap<String, Set<String>>();
	}

	@Override
	public Map<String, String> getHeuristicNouns() {
		return new HashMap<String, String>();
	}

	@Override
	public Map<String, Set<String>> getTermCategories() {
		return new HashMap<String, Set<String>>();
	}

	@Override
	public Set<String> getTags() {
		return new HashSet<String>();
	}

	@Override
	public Set<String> getModifiers() {
		return new HashSet<String>();
	}

	@Override
	public Map<String, Set<String>> getCategoryTerms() {
		return new HashMap<String, Set<String>>();
	}

	@Override
	public Map<String, AdjectiveReplacementForNoun> getAdjectiveReplacementsForNouns() {
		return new HashMap<String, AdjectiveReplacementForNoun>();
	}

}
