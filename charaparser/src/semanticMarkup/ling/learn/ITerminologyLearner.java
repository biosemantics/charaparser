package semanticMarkup.ling.learn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import semanticMarkup.core.Treatment;

/**
 * For a better explanation of what is to be returned see PerlTerminologyLearner in combination with the perl code
 * @author rodenhausen
 */
public interface ITerminologyLearner {

	/**
	 * learns the terminology used in the treatments
	 * @param treatments
	 */
	public void learn(List<Treatment> treatments);
	
	/**
	 * @return <treatment, <source, sentence>> map
	 */
	public Set<String> getSentences();
	
	/**
	 * @return <treatment, <source, sentence> map used by OrganStateMarker
	 */
	public Map<Treatment, LinkedHashMap<String, String>> getSentencesForOrganStateMarker();
	
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
	public Map<String, Set<String>> getWordToSources();
	
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
	public Map<Treatment, LinkedHashMap<String, String>> getSentenceTags();
	
	/**
	 * @return <term, category set> map used in term_category table
	 */
	public Map<String, Set<String>> getTermCategories();

	
	public Set<String> getTags();
	
	public Set<String> getModifiers();

	public Map<String, Set<String>> getCategoryTerms();
	
}
