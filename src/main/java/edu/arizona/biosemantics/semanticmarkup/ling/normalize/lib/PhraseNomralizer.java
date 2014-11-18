/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib;

import java.util.Hashtable;
import java.util.Set;

/**
 * @author updates
 *
 */
public class PhraseNomralizer {
	static Hashtable<String, String> list = new Hashtable<String, String> (); //phrase pattern => a word
	static{
		list.put("a variety of", "varied");
		list.put("varieties of", "varied");
		list.put("in some cases\\s*,?", "sometimes");
		list.put("in most cases\\s*,?", "frequently");
		list.put("in rare cases\\s*,?", "rarely");
		list.put("in no case", "never");
	}
	
	static public String shorten(String string){
		string = string.replaceAll("\\s+", " ").trim();				
		Set<String> patterns = list.keySet();
		for(String pattern: patterns){
			string = string.replaceAll(pattern, list.get(pattern));
		}
		return string;
	}
}
