package semanticMarkup.io.input.lib.db;

import java.util.HashMap;

public class ParentTagProvider {

	private HashMap<String, String> parentTags;
	private HashMap<String, String> grandParentTags;

	public void init(HashMap<String, String> parentTags, HashMap<String, String> grandParentTags) {
		this.parentTags = parentTags;
		this.grandParentTags = grandParentTags;
	}
	
	public String getParentTag(String source) {
		String result =  this.parentTags.get(source);
		result = result.replaceAll("\\[|\\]|>|<|(|)", "");
		return result;
	}
	
	public String getGrandParentTag(String source) {
		String result =  this.grandParentTags.get(source);
		result = result.replaceAll("\\[|\\]|>|<|(|)", "");
		return result;
	}

}
