package semanticMarkup.gui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;

import java.util.ArrayList;
import java.util.regex.*;

import org.apache.log4j.Logger;

import semanticMarkup.know.IPOSKnowledgeBase;


/**
 * @author hongcui
 *
 */
@SuppressWarnings({ "unused" })
public class WordUtilities {
	public static String or = "_or_";
	
	public static Hashtable<String, String> singulars = new Hashtable<String, String>();
	public static Hashtable<String, String> plurals = new Hashtable<String, String>();
	public static ArrayList<String> sureVerbs = new ArrayList<String>();
	public static ArrayList<String> sureAdvs = new ArrayList<String>();
	public static ArrayList<String> partOfPrepPhrase = new ArrayList<String>();
	public static ArrayList<String> notSureVerbs = new ArrayList<String>();
	public static ArrayList<String> notSureAdvs = new ArrayList<String>();
	public static ArrayList<String> notPartOfPrepPhrase = new ArrayList<String>();
	public static final String prepositions = "above|across|after|along|among|amongst|around|as|at|before|behind|below|beneath|between|beyond|by|during|for|from|in|into|near|of|off|on|onto|out|outside|over|per|than|through|throughout|to|toward|towards|up|upward|with|without|according-to|ahead-of|along-with|apart-from|as-for|aside-from|as-per|as-to-as-well-as|away-from|because-of|but-for|by-means-of|close-to|contrary-to|depending-on|due-to|except-for|forward-of|further-to|in-addition-to|in-between|in-case-of|in-face-of|in-favour-of|in-front-of|in-lieu-of|in-spite-of|instead-of|in-view-of|near-to|next-to|on-account-of|on-behalf-of|on-board|on-to|on-top-of|opposite-to|other-than|out-of|outside-of|owing-to|preparatory-to|prior-to|regardless-of|save-for|thanks-to|together-with|up-against|up-to|up-until|vis-a-vis|with-reference-to|with-regard-to";
	public static boolean debug = false;
	public static boolean debugPOS = true;
	private IPOSKnowledgeBase posKnowledgeBase;
	//special cases
	
	public WordUtilities(IPOSKnowledgeBase posKnowledgeBase) {
		this.posKnowledgeBase = posKnowledgeBase;
	}
	
	/**
	 * word must be a verb if
	 * 1. its pos is "verb" only, or
	 * 2. "does not" word
	 * 3. has "verb" pos and seen patterns (word "a/the", or word prep <organ>) and not seen pattern (word \w+ly$). 
	 */
	public boolean mustBeVerb(String word, Connection conn, String prefix) {
		if(word.length()==0) return false;
		if(sureVerbs.contains(word)) return true;
		if(notSureVerbs.contains(word)) return false;
		//WordNetWrapper wnw = new WordNetWrapper(word);
		boolean v = this.posKnowledgeBase.isVerb(word);
		if(!posKnowledgeBase.isAdjective(word) && !posKnowledgeBase.isAdverb(word) && !posKnowledgeBase.isNoun(word) && v){
			sureVerbs.add(word);
			if(debugPOS) System.out.println(word+" is sureVerb");
			return true;
		}
		try{
			Statement stmt = conn.createStatement();
			String q = "select * from "+prefix+"_sentence " +
					"where originalsent like '%does not "+word+"%'";
			ResultSet rs = stmt.executeQuery(q);
			if(rs.next()){
				sureVerbs.add(word);
				if(debugPOS) System.out.println(word+" is sureVerb");
				return true;
			}
			if(v){
				q = "select * from "+prefix+"_heuristicnouns " +
						"where word = '"+word+"'";
				rs = stmt.executeQuery(q);
				if(rs.next()){
					notSureVerbs.add(word);
					return false;
				}
				
				q = "select * from "+prefix+"_sentence " +
						"where sentence rlike '(^| )"+word+" +[-a-z_]+ly$'";
				rs = stmt.executeQuery(q);
				if(rs.next()){
					notSureVerbs.add(word);
					return false;
				}
				
				q = "select sentence from "+prefix+"_sentence " +
						"where sentence rlike '(^| )"+word+" (a|an|the) '";
				rs = stmt.executeQuery(q);
				if(rs.next()){
					sureVerbs.add(word);
					if(debugPOS) System.out.println(word+" is sureVerb");
					return true;
				}
				
				if(word.endsWith("ed") || word.endsWith("ing")){
					q = "select sentence from "+prefix+"_sentence " +
							"where sentence rlike '(^| )"+word+" '";
					rs = stmt.executeQuery(q);
					while(rs.next()){
						String sent = rs.getString("sentence");
						String preps = WordUtilities.prepositions;
						Pattern p = Pattern.compile("\\b"+word+"\\b(?: (?:"+preps+")) +(\\S+)");
						Matcher m = p.matcher(sent);
						while(m.find()){
							String term = m.group(1);
							if(term.matches("(a|an|the|some|any|this|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth)")){
								sureVerbs.add(word);
								if(debugPOS) System.out.println(word+" is sureVerb");
								return true;
							}else if(isOrgan(term, conn, prefix)){
								sureVerbs.add(word);
								if(debugPOS) System.out.println(word+" is sureVerb");
								return true;
							}
						}		
					}
				}
			}			
		}catch(Exception e){
			e.printStackTrace();
		}
		notSureVerbs.add(word);
		return false;
	}
	
	private static boolean isOrgan(String term, Connection conn, String tablePrefix) {
		try{
			Statement stmt = conn.createStatement();
			String wordrolesable = tablePrefix+ "_wordroles";		
			ResultSet rs = stmt.executeQuery("select word from "+wordrolesable+" where semanticrole in ('os', 'op') and word='"+term+"'");		
			if(rs.next()){
				if(debugPOS) System.out.println(term+" is an organ");
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	public boolean mustBeAdv(String word){
		if(sureAdvs.contains(word)) return true;
		if(notSureAdvs.contains(word)) return false;
		if(!posKnowledgeBase.isAdjective(word) && posKnowledgeBase.isAdverb(word) && !posKnowledgeBase.isNoun(word) && !posKnowledgeBase.isVerb(word)){
			sureAdvs.add(word);
			if(debugPOS) System.out.println(word+" is sureAdv");
			return true;
		}
		notSureAdvs.add(word);
		return false;
	}
}
