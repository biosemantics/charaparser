package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn;

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
	 * @param descriptionsFiles
	 * @param glossaryTable
	 * @throws LearnException
	 */
	public void learn(List<AbstractDescriptionsFile> descriptionsFiles, String glossaryTable) throws LearnException;

	/**
	 * Reads the results for the treatments, so that any other function returns the 'freshest' results
	 * @param descriptionsFiles
	 */
	public void readResults(List<AbstractDescriptionsFile> descriptionsFiles);

	/**
	 * @return &lt;treatment, &lt;source, sentence&gt;&gt; map
	 */
	public Set<String> getSentences();

	/**
	 *
	 * @param phrase
	 * @return the number of sentences matching the rlike criterion
	 */
	public int countMatchingSentences(String phrase);

	/**
	 * @return &lt;treatment, &lt;source, sentence&gt; map used by OrganStateMarker
	 */
	public Map<Description, LinkedHashMap<String, String>> getSentencesForOrganStateMarker();

	/**
	 * @return modifier list used in the sentence table
	 */
	public List<String> getAdjNouns();

	/**
	 * @return &lt;tag, modifier&gt; map created from sentence table
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
	 * @return &lt;word, source set&gt; map used in sentence table
	 */
	//public Map<String, Set<String>> getWordToSources();
	public Map<String, Map<String, Set<Integer>>> getWordToSources();

	/**
	 * @return &lt;semanticrole, word set&gt; map used in wordroles table
	 */
	public Map<String, Set<String>> getRoleToWords();

	/**
	 * @return &lt;word, semanticrole set&gt; map used in wordroles table
	 */
	public Map<String, Set<String>> getWordsToRoles();

	/**
	 * @return &lt;word, type&gt; map used in heuristicnouns table
	 */
	public Map<String, String> getHeuristicNouns();

	/**
	 * @return &lt;treatment, &lt;source, tag&gt; map used in sentence table
	 */
	//public Map<Description, LinkedHashMap<String, String>> getSentenceTags();

	/**
	 * @return &lt;term, category set&gt; map used in term_category table
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
	 * @return &lt;category, term set&gt; map used in term_category table
	 */
	public Map<String, Set<String>> getCategoryTerms();

	/**
	 * @return &lt;source, AjectiveReplacementForNoun&gt; map used in sentence table
	 */
	public Map<String, AdjectiveReplacementForNoun> getAdjectiveReplacementsForNouns();

	//public Hashtable<String, String> selectMatchingSentences(String string);

}
