package edu.arizona.biosemantics.semanticmarkup.markupelement.description.io;

import java.util.HashMap;

/**
 * ParentTagProvider returns tags of parent or grandparent sentences
 * @author rodenhausen
 */
public class ParentTagProvider {

	private HashMap<String, String> parentTags;
	private HashMap<String, String> grandParentTags;

	/**
	 * initialize provider with parentTags and grandParentTags
	 * @param parentTags
	 * @param grandParentTags
	 */
	public void init(HashMap<String, String> parentTags, HashMap<String, String> grandParentTags) {
		this.parentTags = parentTags;
		this.grandParentTags = grandParentTags;
	}
	
	/**
	 * @param source
	 * @return parentTag of the source
	 */
	public String getParentTag(String source) {
		String result =  this.parentTags.get(source);
		if(result!=null)
			result = result.replaceAll("\\[|\\]|>|<|(|)", "");
		return result;
	}
	
	/**
	 * @param source
	 * @return grandParentTag of the source
	 */
	public String getGrandParentTag(String source) {
		String result =  this.grandParentTags.get(source);
		result = result.replaceAll("\\[|\\]|>|<|(|)", "");
		return result;
	}

}
