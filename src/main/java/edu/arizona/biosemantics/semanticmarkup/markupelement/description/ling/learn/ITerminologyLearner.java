package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;


/**
 * ITerminologyLearner learns the terminology of a list of treatments
 * @author rodenhausen
 */
public interface ITerminologyLearner {

	/**
	 * Learns the terminology used in the treatments
	 * @param treatments
	 * @throws LearnException 
	 */
	public void learn(List<AbstractDescriptionsFile> descriptionsFiles, String glossaryTable) throws LearnException;
	
	/**
	 * Reads the results for the treatments, so that any other function returns the 'freshest' results
	 * @param treatments
	 */
	public void readResults(List<AbstractDescriptionsFile> descriptionsFiles);
	
	/**
	 * @return <treatment, <source, sentence>> map
	 */
	public Set<String> getSentences();
	
	/**
	 * 
	 * @param criterion
	 * @return the number of sentences matching the rlike criterion
	 */
	public int countMatchingSentences(String phrase);
	
	/**
	 * @return <treatment, <source, sentence> map used by OrganStateMarker
	 */
	public Map<Description, LinkedHashMap<String, String>> getSentencesForOrganStateMarker();
	
	/**
	 * @return modifier list used in the sentence table 
	 */
	public List<String> getAdjNouns();
	
	/**
	 * @return <tag, modifier> map created from sentence table
	 */
	public Map<String, String> getAdjNounSent();
	
	/**
	 * @return modifier set used in sentence table where [%] tag is set
	 */
	public Set<String> getBracketTags();
	
	/**
	 * @return word set where semanticrole 'op' or 'os'
	 */
	public Set<String> getWordRoleTags();
	
	/**
	 * @return <word, source set> map used in sentence table
	 */
	//public Map<String, Set<String>> getWordToSources();
	public Map<String, Map<String, Set<Integer>>> getWordToSources();
	
	/**
	 * @return <semanticrole, word set> map used in wordroles table
	 */
	public Map<String, Set<String>> getRoleToWords();
	
	/**
	 * @return <word, semanticrole set> map used in wordroles table
	 */
	public Map<String, Set<String>> getWordsToRoles();
	
	/**
	 * @return <word, type> map used in heuristicnouns table
	 */
	public Map<String, String> getHeuristicNouns();
	
	/**
	 * @return <treatment, <source, tag> map used in sentence table
	 */
	//public Map<Description, LinkedHashMap<String, String>> getSentenceTags();
	
	/**
	 * @return <term, category set> map used in term_category table
	 */
	public Map<String, Set<String>> getTermCategories();
	
	/**
	 * @return tag set used in sentence table
	 */
	public Set<String> getTags();
	
	/**
	 * @return modifier set used in sentence table
	 */
	public Set<String> getModifiers();

	/**
	 * @return <category, term set> map used in term_category table
	 */
	public Map<String, Set<String>> getCategoryTerms();
	
	/**
	 * @return <source, AjectiveReplacementForNoun> map used in sentence table
	 */
	public Map<String, AdjectiveReplacementForNoun> getAdjectiveReplacementsForNouns();

	//public Hashtable<String, String> selectMatchingSentences(String string);
	
}
