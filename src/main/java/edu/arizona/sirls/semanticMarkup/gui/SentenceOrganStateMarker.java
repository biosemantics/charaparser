package edu.arizona.sirls.semanticMarkup.gui;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

import edu.arizona.sirls.semanticMarkup.ling.transform.IInflector;


/**
 * @author hongcui
 * last stable version: 653
 * this version: try to find additional nouns from unknown words, and mark them with <>. 
 * This is called in step 5, learning cooccurrence, before categorizing character terms to their specific categories in step 6.
 * Uses glossary table, tagged sentences, and semanticroles table.
 * The prefix_term_category table does not exist when the first time this class is called.
 */

@SuppressWarnings("unchecked")
public class SentenceOrganStateMarker {
	private static final Logger LOGGER = Logger.getLogger(SentenceOrganStateMarker.class);
	private Hashtable<String, String> sentences = new Hashtable<String, String>();
	private Connection conn = null;
	private boolean marked = false;
	private boolean fixadjnn = false;
	private int fixedcount  =0;
	
	private Hashtable<String, String> adjnounsent = null;
	private String adjnounslist = "";
	private String organnames = null;
	private String statenames = null;
	private String tableprefix = null;
	private String glosstable = null;
	private static String colors = null;
	private static String pt;
	private static Pattern colorpattern;
	private static Pattern range = Pattern.compile("(.*?)\\b(?:from|between)\\s*([\\d\\. /\\(\\)\\?+-]+)\\s*(?:to|and|-)\\s*([\\d\\. /\\(\\)\\?+-]+)(.*)");
	//private static Pattern thelargest = Pattern.compile("(.*)(,\\s*\\S+est})( [^<].*)"); //tested and failed. too general. many superlatives are not subjects.
	private static Pattern thelargest = Pattern.compile("(.*)(,\\s*(?:the )?\\S+est})( [^<].*)"); //narrowed down to size cases by checking group(3)
	public static String compoundprep = "according to|ahead of|along with|apart from|as for|aside from|as per|as to as well as|away from|because of|but for|by means of|close to|contrary to|depending on|due to|except for|forward of|further to|in addition to|in association with|in between|in case of|in combination with|in face of|in favour of|in front of|in lieu of|in spite of|instead of|in view of|near to|next to|on account of|on behalf of|on board|on to|on top of|opposite to|other than|out of|outside of|owing to|preparatory to|prior to|regardless of|save for|thanks to|together with|up against|up until|vis-a-vis|with reference to|with regard to";
	public static Pattern compreppattern = Pattern.compile("(.*?)\\b("+compoundprep+")\\b(.*)");
	private String ignoredstrings = "if at all|at all|as well (?!as)|was|is|were|are";
	//private static String compoundprep = "according to|ahead of|along with|apart from|as for|aside from|as per|as to as well as|away from|because of|but for|by means of|close to|contrary to|depending on|due to|except for|forward of|further to|in addition to|in association with|in between|in case of|in combination with|in face of|in favour of|in front of|in lieu of|in spite of|instead of|in view of|near to|next to|on account of|on behalf of|on board|on to|on top of|opposite to|other than|out of|outside of|owing to|preparatory to|prior to|regardless of|save for|thanks to|together with|up against|up to|up until|vis-a-vis|with reference to|with regard to";
	//private static Pattern compreppattern = Pattern.compile("(.*?)\\b("+compoundprep+")\\b(.*)");
	//private ArrayList<String> order = new ArrayList<String>();
	private Display display;
	private StyledText charLog;
	private boolean debug = false;
	private boolean superlative = false;
	private boolean printto = true;
	private boolean printCompoundPP = false;
	private IInflector inflector;
	public static String taxonnames = null;
	public static Pattern taxonnamepattern1 = null;
	public static Pattern taxonnamepattern2 = null;
	/**
	 * 
	 */
	public SentenceOrganStateMarker(Connection conn, String tableprefix, String glosstable, boolean fixadjnn, Display display, StyledText charLog, 
			IInflector inflector) {
		this.inflector = inflector;
		this.display = display;
		this.charLog = charLog;
		this.tableprefix = tableprefix;
		this.conn = conn;
		this.glosstable = glosstable;
		this.fixadjnn = fixadjnn;
		//preparing...
		this.adjnounsent = new Hashtable(); //source ->adjnoun (e.g. inner)
		ArrayList<String> adjnouns = new ArrayList<String>();//all adjnouns
	
			try{
				//collect all taxonnames to be used in processParentheses in ChunkedSentence
				Statement stmt = conn.createStatement();
				String taxonnames = "";
				/*ResultSet rs = stmt.executeQuery("select name from "+tableprefix+"_taxonnames");
				while(rs.next()){
					String tn = rs.getString("name").trim();
					if(tn.length()>0){
						tn = tn.replaceFirst("\\(.*?\\)", "").trim(); //(a) glutinosa => glutinosa
						tn = tn.substring(tn.indexOf(".")+1).trim(); //A.glutinosa =>glutinosa
						taxonnames += tn+"|";
					}
				}*/
				if(taxonnames!=null && taxonnames.length()!=0){
					taxonnames = taxonnames.replaceAll("\\|+", "|").replaceAll("(^\\||\\|$)", "").trim();
					SentenceOrganStateMarker.taxonnamepattern1 = Pattern.compile(".*?\\bin\\s+([A-Z]\\.\\s+)?(?<!\\{)("+taxonnames+")(?!\\})\\b.*");
					//this.taxonnamepattern2 = Pattern.compile(".*?\\b([A-Z]\\.[ ~])?(?<!\\{)("+taxonnames+")(?!\\})\\b.*", Pattern.CASE_INSENSITIVE);
					SentenceOrganStateMarker.taxonnamepattern2 = Pattern.compile(".*?\\b([a-z] \\. )?("+taxonnames+")\\b.*");
	
				}
			}catch(Exception e){
				e.printStackTrace();
			}

		try{
				Statement stmt = conn.createStatement();
				stmt.execute("drop table if exists "+this.tableprefix+"_markedsentence");
				stmt.execute("create table if not exists "+this.tableprefix+"_markedsentence (sentid int(11)NOT NULL Primary Key, source varchar(100) , markedsent text, rmarkedsent text)" +
						" CHARACTER SET utf8 engine=innodb");
				//stmt.execute("update "+this.tableprefix+"_sentence set charsegment =''");
				if(colors==null){
					colors = this.colorsFromGloss().trim();
					if(colors.length()==0) colors += "shades_of";
					else colors += "|shades_of";
					pt = "\\b(?<="+colors+")\\s+(?="+colors+")\\b";
					colorpattern = Pattern.compile(pt); //spaces that surrounded by colors
				}

				//ResultSet rs = stmt.executeQuery("select source, tag, originalsent from "+this.tableprefix+"_sentence");
				ResultSet rs = stmt.executeQuery("select source, modifier, tag, sentence, originalsent from "+this.tableprefix+"_sentence order by sentid desc");
				//leave ditto as it is
				while(rs.next()){//read sent in in reversed order
					String tag = rs.getString("tag");
					String sent = rs.getString("sentence").trim();
					if(sent.length()!=0){
					String source = rs.getString("source");
					//if(!source.equals("2.txt-3")) continue;
					String osent = rs.getString("originalsent");
					sent = sent.replaceAll("</?[BNOM]>", "");
					sent = sent.replaceAll("\\bshades of\\b", "shades_of");
					sent = sent.replaceAll("\\bat least\\b", "at_least");
					String text = stringColors(sent);
					text = text.replaceAll("[ _-]+\\s*shaped", "-shaped").replaceAll("(?<=\\s)�\\s+m\\b", "um");
					//deal with numbers
					//text = text.replaceAll("(?<=\\d)(?=("+ChunkedSentence.units+")\\b)", " "); //23mm => 23 mm
					//text = StanfordParser.ratio2number(text);
					text = toNumber(text);
					text = text.replaceAll("\\b(ca|c)\\s*\\.?\\s*(?=\\d)", "");
					text = formatNumericalRange(text);
					text = text.replaceAll("more or less", "moreorless");
					text = text.replaceAll("&#176;", "�");
					//text = text.replaceAll("\\bca\\s*\\.", "ca");
					text = text.replaceAll("\\bdiam\\s*\\.(?=\\s?[,a-z])", "diam");
					text = stringCompoundPP(text);
					text = markTaxonNames(text);
					text = rs.getString("modifier")+"##"+tag+"##"+text;
					sentences.put(source, text);
					}
				}
				//merge ditto sentences with previous sentences: this had the drawback of attaching nearest organ as the subject of the ditto sentence
				/*String dittos = "";
				while(rs.next()){//read sent in in reversed order
					String tag = rs.getString("tag");
					String sent = rs.getString("sentence");
					String source = rs.getString("source");
					String osent = rs.getString("originalsent");
					if(tag.compareTo("ditto")==0){ //attach ditto to the previous sentence
						dittos = sent.trim()+" "+dittos;
						//sentences.put(source, ""); //make ditto sent id's disappear
					}else{
						sent =sent.trim() +" "+ dittos.trim();
						if(osent.indexOf(dittos.trim())<0) osent =osent.trim() +" "+ dittos.trim(); //put a check here so dittos are not added multiple times when the user runs the Parser mutiple times on one document collection
						dittos = "";
						String text = stringColors(sent.replaceAll("</?[BNOM]>", ""));
						text = text.replaceAll("[ _-]+\\s*shaped", "-shaped").replaceAll("(?<=\\s)µ\\s+m\\b", "um");
						text = text.replaceAll("&#176;", "°");
						text = text.replaceAll("\\bca\\s*\\.", "ca");
						text = rs.getString("modifier")+"##"+tag+"##"+text;
						sentences.put(source, text);
						//update originalsent
						Statement st = conn.createStatement();
						st.execute("update "+this.tableprefix+"_sentence set originalsent='"+osent+"' where source='"+source+"'");
					}
				}*/
				//collect adjnouns
				rs = stmt.executeQuery("SELECT distinct modifier FROM "+this.tableprefix+"_sentence where modifier != \"\" and tag like \"[%\"");
				while(rs.next()){
					String modifier = rs.getString(1).replaceAll("\\[.*?\\]", "").trim();
					adjnouns.add(modifier);
				}
				//collect senteces that need adj-nn disambiguation
				rs = stmt.executeQuery("SELECT source, tag, modifier FROM "+this.tableprefix+"_sentence where modifier != \"\" and tag like \"[%\"");
				while(rs.next()){
					String modifier = rs.getString(2).replaceAll("\\[.*?\\]", "").trim(); 
					String tag = rs.getString("tag");
					adjnounsent.put(tag, modifier);//tag: [phyllary]
					//adjnounsent.put(tag.replaceAll("\\W", ""), modifier);//TODO: need to investigate more on this
				}
		}catch(Exception e){
			e.printStackTrace();
		}
		Collections.sort(adjnouns);
		for(int i = adjnouns.size()-1; i>=0; i--){
			this.adjnounslist +=adjnouns.get(i)+"|";
		}
		this.adjnounslist = this.adjnounslist.trim().length()==0? null : "[<{]*"+this.adjnounslist.replaceFirst("\\|$", "").replaceAll("\\|+", "|").replaceAll("\\|", "[}>]*|[<{]*").replaceAll(" ", "[}>]* [<{]*")+"[}>]*";
		this.organnames = collectOrganNames();
		this.statenames = collectStateNames();
	}
	
	private String toNumber(String text) {
		//text = StanfordParser.toNumber(text); //dealt with 2-9
		text = text.replaceAll("\\bten\\b", "10");
		return text;
	}

	/**
	 * from 5-6 to 10 => 5-10
	 * between 1.0 and 2.0 => 10-20
	 * from 1/3 to 1/2 
	 * 10 to 20
	 */
	private String formatNumericalRange(String text) {
		String copy = text;
		text = text.replaceAll("\\bone\\b\\s?\\b(?=to\\s?\\d)", "1 "); //one to 3 valves
		if(text.contains("from") || text.contains("between")){
			Matcher m = range.matcher(text);
			while(m.matches()){
				text = m.group(1)+m.group(2)+" - "+m.group(3)+m.group(4);
				m = range.matcher(text);
			}
		}
		text = text.replaceAll("\\bdiameter\\s+of\\b\\s*(?=\\d)", "diameter ");
		if(text.contains(" to ") || text.contains(" up to ")){
			//text = text.replaceAll("(?<=\\d\\s?("+ChunkedSentence.units+")?) to (?=\\d)", " - ");// three to four???
			//deal with: to-range such as "to 3 cm", "to 24 – 5 mm", "to 2 . 7 – 1 . 7 – 2 mm", "3 – 20 ( – 25 )" 
			text = text.replaceAll(" (up )?to (?=[\\d\\. ]{1,6} )", " 0 - "); // <trees> to 3 cm => <trees> 0 - 3 cm: works for case 1,  3, (case 4 should not match)
			text = text.replaceAll(" (?<=0 - [\\d\\. ]{1,6} [a-z ]?)� (?=[\\d\\. ]{1,6} [a-z])", " � 0 - "); //deal with case 2
			text = text.replaceAll(" 0 - (?=[\\d\\.\\ ]{1,8} [-�])", " ");// 0 - 1 . 3  - 2 . 0 => 1 . 3 - 2 . 0
		}
		if(!copy.equals(text) && printto){
			System.out.println("[to range original] "+copy);
			System.out.println("[to range now] "+text);
		}
		
		return text.replaceAll("\\s+", " ").trim();
	}
	private String markTaxonNames(String text) {
		//markup taxon names
		//formatting taxon names m . chamissoi => m-name-chamissoi
		if(taxonnamepattern2==null) return text; 
		Matcher m = taxonnamepattern2.matcher(text);
		String remain = text;
		while(m.matches()){
			String name = (m.group(1)==null? "" : m.group(1))+m.group(2);
			int end = m.end(2);
			remain = remain.substring(end);
			String formated = name.replaceAll("\\s+\\.\\s+", "-taxonname-");
			if(!formated.contains("-taxonname-")) formated = "taxonname-"+formated; //the taxon name doesn't have . in it: chamissoi => name~chamissoi
			text = text.replaceAll(name, formated);
			m = SentenceOrganStateMarker.taxonnamepattern2.matcher(remain);
		}
		return text;
	}
	
	/*
	 * Handles the compound prepositions
	 */
	 private String stringCompoundPP(String text) {
	        boolean did = false;
	        String result = "";
	        Matcher m = compreppattern.matcher(text);
	        while(m.matches()){
	            String linked = m.group(2).replaceAll("\\s+", "-");
	            result += m.group(1)+ linked;
	            text = m.group(3);
	            m = compreppattern.matcher(text);
	            did = true;
	        }
	        result += text;
	        if(did && printCompoundPP ) System.out.println("[result]:"+result);
	        return result;
	    }
	
//	private String stringCompoundPP(String text) {
//		boolean did = false;
//		String result = "";
//		Matcher m = compreppattern.matcher(text);
//		while(m.matches()){
//			String linked = m.group(2).replaceAll("\\s+", "-");
//			result += m.group(1)+ linked;
//			text = m.group(3);
//			m = compreppattern.matcher(text);
//			did = true;
//		}
//		result += text;
//		if(did) System.out.println("[result]:"+result);
//		return result;
//	}

	/**
	 * turn reddish purple to reddish_c_purple
	 * shades of yellow
	 */
	private String stringColors(String text) {
		if(this.colors==null || this.colors.length() == 0) return text;
		boolean did = false;
		Matcher m = colorpattern.matcher(text);
		while(m.find()){
			text = text.replaceFirst(pt, "_c_");
			m = colorpattern.matcher(text);
			did = true;
		}
		//if(did) System.out.println("[color]:"+text);
		return text;
	}

	public Hashtable markSentences(){
		if(this.marked){
			loadMarked();
		}else{
			//this.showOutputMessage("System is preparing the sentences...");
			//Iterator<String> it = order.iterator();
			//while(it.hasNext()){				
			Enumeration<String> en = sentences.keys();
			while(en.hasMoreElements()){
				String source = en.nextElement();
				//String source = it.next();
				String sent = (String)sentences.get(source);
				String taggedsent = "";
				//if(sent.trim().length()>0){
					String[] splits = sent.split("##");
					String modifier = splits[0];
					String tag = splits[1];
					sent = splits[2].trim().replaceAll("\\b("+this.ignoredstrings+")\\b", "");
					taggedsent = markASentence(source, modifier, tag.trim(), sent);
				//}
				
				if(debug) System.out.println(taggedsent);
				sentences.put(source, taggedsent);
				try{
					Statement stmt1 = conn.createStatement();
					ResultSet rs = stmt1.executeQuery("select sentid from "+this.tableprefix+"_sentence where source='"+source+"'");
					if(rs.next()){
						int id = rs.getInt("sentid");
						stmt1.execute("insert into "+this.tableprefix+"_markedsentence (sentid, source, markedsent) values("+id+",'"+source+"', '"+taggedsent+"')");
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}
		return sentences;
	}

	protected void loadMarked() {
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select source, markedsent from "+this.tableprefix+"_markedsentence");
			while(rs.next()){
				String source = (String)rs.getString("source");
				String taggedsent = (String)rs.getString("markedsent"); 
				sentences.put(source, taggedsent); //do this in addClause
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String markASentence(String source, String modifier, String tag, String sent) {
		String taggedsent = markthis(source, sent, organnames, "<", ">");
		taggedsent = markthis(source, taggedsent, statenames, "{", "}");
		taggedsent = taggedsent.replaceAll("[<{]or[}>]", "or"); //make sure to/or are left untagged
		taggedsent = taggedsent.replaceAll("[<{]to[}>]", "to");
		//tested and failed
		//, the {largest} to 3 cm => , the <largest> to 3cm
		Matcher m = thelargest.matcher(taggedsent);
		if(taggedsent.indexOf("est}")>=0 && m.matches()){
			String aftersuperlative = m.group(3);
			if(aftersuperlative.matches("\\s+(\\w+\\s+)?\\d.*")){//the superlative and the numbers may be separated by one word
				//this check will miss cases such as ", {longest} {white} {margined} , 25 – 45 ( – 58 ) mm ;"
				taggedsent = m.group(1)+m.group(2).replaceAll("\\}", ">").replaceAll("\\{", "<").replaceAll("<+", "<").replaceAll(">+", ">")+m.group(3);
				if(superlative) System.out.println("["+m.group(2)+"]:"+taggedsent);
			}
		}
		//remove "<>" for <{spine}>-{tipped}  =>spine-{tipped} or {spine}-{tipped}
		if(taggedsent.indexOf(">-")>=0){
			taggedsent = taggedsent.replaceAll(">-", "#-").replaceAll("<(?=\\S+#)", "").replaceAll("#", "");
		}
		if(this.fixadjnn && this.adjnounslist!=null){
			//if((adjnounsent.containsKey(tag)&& taggedsent.matches(".*?[<{]*\\b(?:"+adjnounslist+")\\b[}>]*.*")) || taggedsent.matches(".*? of [<{]*\\b(?:"+adjnounslist+")\\b[}>]*.*")){
			if((adjnounsent.containsKey(tag)&& taggedsent.matches(".*?[<{]*\\b(?:"+adjnounslist+")[^ly ]*\\b[}>]*.*")) || taggedsent.matches(".*? of [<{]*\\b(?:"+adjnounslist+")[^ly ]*\\b[}>]*.*")){
				taggedsent = fixInner(source, taggedsent, tag.replaceAll("\\W",""));//need to put tag in after the modifier inner
			}
			//including modifiers results in nouns are added to state adjs.
			//if(adjnounsent.containsKey(modifier) && taggedsent.matches(".*?[<{]*\\b(?:"+adjnounslist+")\\b[}>]*.*") ){
			//	taggedsent = fixInner(source, taggedsent, modifier, true);//@TODO: debug: need to put tag in after the modifier inner
			//}
		}
 		return taggedsent;
	}
	
	/**
	 * mark Inner as organ for sent such as inner red.
	 */
	private String fixInner(String source, String taggedsent, String tag) {
		//this.showOutputMessage("System is rewriting some sentences...");
		String fixed = "";
		String copysent = taggedsent;
		boolean needfix = false;
		boolean changed = true;
		//Pattern p =Pattern.compile("(.*?)(\\s*(?:[ <{]*\\b(?:"+adjnounslist+")\\b[}> ]*)+\\s*)(.*)");
		//Pattern p0 =Pattern.compile("(.*?)((?:^| )(?:(?:\\{|<\\{)*\\b(?:"+adjnounslist+")\\b(?:\\}>|\\})*) )(.*)");
		//Pattern p =Pattern.compile("(.*?)((?:^| )(?:(?:\\{|<\\{)*\\b(?:"+adjnounslist+")[^ly ]*\\b(?:\\}>|\\})*)\\s+)(.*)");
		Pattern p =Pattern.compile("(.*?)((?:^| )(?:(?:\\{|<\\{)*\\b(?:"+adjnounslist+")[^ly ]*\\b(?:\\}>|\\})*)\\s+)(((?!to\\s+\\D).*).*)");
		Matcher m = p.matcher(taggedsent);
		//Matcher m0 = p0.matcher(taggedsent);
		int matchcount = 0;
		while(m.matches() && changed){
			changed = false;
			matchcount++;
			String before = m.group(1);
			String inner = m.group(2);
			String after = m.group(3);
			//TODO: may be after should not start with "to" : proximal to heads tocheck: 3/30/11
			if(!before.trim().endsWith(">") &&!after.trim().startsWith("<")){//mark inner as organ
				if(before.trim().endsWith("of")&& before.lastIndexOf("<")>=0){ //"apices of inner" may appear at the main structure is mentioned, in these cases, matchcount>1					
					String organ = before.substring(before.lastIndexOf("<"));
					if(copysent.startsWith(organ)){
						tag = getParentTag(source);//tag may be null, remove before return
					}
					organ = organ.replaceFirst("\\s*of\\s*$", "").replaceAll("\\W", "");
					if(inflector.getSingular(organ).compareTo(tag)==0 || 
						(organ.matches("(apex|apices)") && tag.compareTo("base")==0)){
						String b = source.substring(0, source.indexOf("-")+1);
						String nsource = b +(Integer.parseInt(source.substring(source.indexOf("-")+1))-1);
						tag = getParentTag(nsource);
					}
				}
				String copyinner = inner.trim();
				inner = copyinner.replaceAll("[<{}>]", "").replaceAll("\\s+", "} {").replaceAll("\\{and\\}", "and").replaceAll("\\{or\\}", "or");
				//inner = "<"+inner+">";
				//inner = "{"+inner+"} <"+tag+">";
				fixed +=before+" "+"{"+inner+"} ";
				//taggedsent = matchcount==1 && !before.trim().endsWith("of")? " "+after : "#<"+tag+">#"+" "+after;
				if(after.matches("^\\d\\s*/\\s*\\d.*")){//proximal 1 / 2
					taggedsent = " "+after;
				}else if(inner.endsWith("er") && after.startsWith("than")){
					taggedsent = " "+after;
				}else if(before.trim().endsWith("of")){
					taggedsent = "<"+tag+">"+" "+after;
				}else if(matchcount==1 && copysent.startsWith(copyinner)){
					taggedsent = " "+after;
				}else{
					int start = fixed.lastIndexOf(">")>=0? fixed.lastIndexOf(">") : 0;
					String segment = fixed.substring(start).trim();
					if(segment.indexOf(",")<0 && !segment.startsWith("and")){
						taggedsent = " "+after;
					}else{
						taggedsent = "<"+tag+">"+" "+after;
					}
				}
				needfix = true;
				changed = true;
			}
			//fixed +=before+" ";
			//taggedsent = inner+" "+after;
			m = p.matcher(taggedsent);
			//fixed = before+" "+inner+" "+after; //{outer} {pistillate}
			//m = p.matcher(fixed);
		}
		fixed +=taggedsent;
		if(needfix){
			System.out.println("fixed "+fixedcount+":["+source+"] "+fixed);
			fixedcount++;
		}
		if(fixed.trim().length()<1){
			fixed = taggedsent;
		}
		return fixed.replaceAll("\\s+", " ").replaceAll("<null>", "");
	}

	private String getParentTag(String source) {
		String tag = null;
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select sentid from "+this.tableprefix+"_sentence where source='"+source+"'");
			if(rs.next()){
				int sentid = rs.getInt("sentid");
				sentid = sentid+1;
				do{
					sentid--;
					rs = stmt.executeQuery("select tag from "+this.tableprefix+"_sentence where sentid <"+sentid+" order by sentid desc limit 1");
					if(rs.next()){
						tag = (String)rs.getString("tag").replaceAll("\\W", ""); 	
					}
				}while(tag.compareTo("ditto")==0);
				
			}
			rs.close();
			stmt.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return tag;
	}

	/**
	 * retag {caline} 10 to <caline> 10 when an adjnoun does not follow an organ or proceeds  an organ.
	 */
	@SuppressWarnings("unused")
	private String fixAdjNouns(ArrayList adjnouns, String adjnoun, String taggedsent) {
		adjnoun = adjnoun.replaceAll("\\s+", "\\\\W+");
		taggedsent = Pattern.compile("[<{]*\\b"+adjnoun+"\\b[}>]*", Pattern.CASE_INSENSITIVE).matcher(taggedsent).replaceFirst("<"+adjnoun+">").replaceAll("W\\+", "> <").replaceAll("<and>", "and").replaceAll("<or>", "or");
		return taggedsent;
	}

	public static String markthis(String source, String sent, String parts, String leftmark, String rightmark) {
		//no need if select sentence (vs. originalsent)
		//remove ()
		//sent = sent.replaceAll("\\(.*?\\)", "");
		//remove (text)
		//sent = sent.replaceAll("\\(\\s+(?![\\d\\–\\-\\–]).*?(?<![\\d\\–\\-\\–])\\s+\\)", "");
		
		sent = sent.replaceAll("(?<=\\w)\\s+(?=[,\\.;:])", "");

		sent = sent.replaceAll("_", "-");
		
		Pattern tagsp = Pattern.compile("(.*?)\\b("+parts+")\\b(.*)", Pattern.CASE_INSENSITIVE);
		String taggedsent = "";
		Matcher m = tagsp.matcher(sent);
		while(m.matches()){
			taggedsent += m.group(1)+leftmark+m.group(2)+rightmark;
			sent = m.group(3);
			m = tagsp.matcher(sent);
		}
		taggedsent +=sent;
		
		String tsent = "";
		Pattern p = Pattern.compile("(.*\\}-)(\\w+)(.*)");
		m = p.matcher(taggedsent);
		while(m.matches()){
			tsent += m.group(1)+"{"+m.group(2)+"}";
			taggedsent = m.group(3);
			m = p.matcher(taggedsent);			
		}
		tsent +=taggedsent;
		tsent = tsent.replaceAll("\\}-\\{", "-"); // => {oblong}-{ovate} :  {oblong-ovate}
		/*p = Pattern.compile("(.*?<[^>]*) ([^<]*>.*)");//<floral cup> => <floral-cup>
		m = p.matcher(tsent);
		while(m.matches()){
			tsent = m.group(1)+"-"+m.group(2);
			m = p.matcher(tsent);
		}*/
		tsent = tsent.replaceAll("\\s*,\\s*", " , ");
		tsent = tsent.replaceAll("\\s*\\.\\s*", " . ");
		tsent = tsent.replaceAll("\\s*;\\s*", " ; ");
		tsent = tsent.replaceAll("\\s*:\\s*", " : ");
		tsent = tsent.replaceAll("\\s*\\]\\s*", " ] ");
		tsent = tsent.replaceAll("\\s*\\[\\s*", " [ ");
		//tsent = tsent.replaceAll("\\s*\\)\\s*", " ) ");
		//tsent = tsent.replaceAll("\\s*\\(\\s*", " ( ");
		tsent = tsent.replaceAll("\\s+", " ").trim();		
		return tsent;
	}
	
	protected String collectStateNames(){
		String statestring = "";
		try{
			Statement stmt = conn.createStatement();

			//ResultSet rs = stmt.executeQuery("select word from "+this.tableprefix+"_wordpos where pos ='b'");
			ResultSet rs = stmt.executeQuery("select word from "+this.tableprefix+"_wordroles where semanticrole ='c' ");

			while(rs.next()){
				String w = rs.getString("word");
				if(!w.matches("\\W+")) { // && !w.matches("("+ChunkedSentence.stop+")") &&!w.matches("("+ChunkedSentence.prepositions+")")){
					statestring += "|"+ w; 
				}
			}
			
			/*wordroles only holds word not in glossary, so need to use glossary to mark a sentence as well.*/
			rs = stmt.executeQuery("select distinct term from "+this.glosstable+" where category not in ('STRUCTURE', 'FEATURE', 'SUBSTANCE', 'PLANT', 'nominative', 'life_style')");
			while(rs.next()){
				String term = rs.getString("term").trim();
				if(term == null){continue;}
				term = term.indexOf(" ")> 0? term.substring(term.lastIndexOf(' ')+1) : term;
				if(!statestring.matches(".*\\b"+term+"\\b.*"))
					statestring+=("|"+ term);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return statestring.replaceAll("_", "|").replaceAll("\\b(and|or|to)\\b", "").replaceAll("\\\\d\\+", "").trim().replaceFirst("^\\|", "").replaceFirst("\\|$", "").replaceAll("\\|+", "|");
	}
	
	protected String collectOrganNames(){
		StringBuffer tags = new StringBuffer();
		try{
		Statement stmt = conn.createStatement();
		organNameFromGloss(tags, stmt);
		organNameFromSentences(tags, stmt);
		organNameFromPlNouns(tags, stmt);
	
		tags = tags.replace(tags.lastIndexOf("|"), tags.lastIndexOf("|")+1, "");
		//System.out.println(tags.toString().replaceAll("\\b\\d+\\b", "").replaceAll("\\|+", "|"));
		//System.out.println(tags.toString().replaceAll("\\b\\d+\\b", "").replaceAll("\\|+", "|").replaceAll("\\|[a-z]\\|", "\\|").replaceAll(":\\|", "\\|").replaceAll("\\.\\|", "\\|"));
		//System.out.println(tags.toString().replaceAll("\\b\\d+\\b", "").replaceAll("\\|+", "|").replaceAll("\\|[a-z]\\|", "\\|").replaceAll("(\\[|\\])", ""));
		}catch(Exception e){
			e.printStackTrace();
		}
		//return tags.toString().replaceAll("\\b\\d+\\b", "").replaceAll("\\|+", "|");
		return tags.toString().replaceAll("\\b\\d+\\b", "").replaceAll("\\|+", "|").replaceAll("\\|[a-z]\\|", "\\|").replaceAll("(\\[|\\])", "");
	}
	

	protected void organNameFromPlNouns(StringBuffer tags, Statement stmt)
			throws SQLException {
		ResultSet rs;
		String wordroletable = this.tableprefix + "_wordroles";
		rs = stmt.executeQuery("select word from "+wordroletable+" where semanticrole in ('op', 'os')");
		while(rs.next()){
			tags.append(rs.getString("word").trim()+"|");
		}
		/*
		String postable = this.tableprefix + "_"+ApplicationUtilities.getProperty("POSTABLE");
		rs = stmt.executeQuery("select word from "+postable+" where pos in ('p', 's', 'n') and word not in (select word from "+wordroletable+" where semanticrole in ('op', 'os'))");// and word not in (select term from "+this.glosstable+" where category ='life_style')");
		while(rs.next()){
			tags.append(rs.getString("word").trim()+"|");
		}*/
	}
	/**
	 * collect adj-noun structures such as "inner" as structure name
	 * @param tags
	 * @param stmt
	 * @throws SQLException
	 */
	protected void organNameFromSentences(StringBuffer tags, Statement stmt)
			throws SQLException {
		ResultSet rs;
		
		/*tag terms are already in WORDROLES
		 * rs = stmt.executeQuery("select distinct tag from sentence where tag not like '% %'");
		while(rs.next()){
			String tag = rs.getString("tag");
			if(tag == null || tag.indexOf("[")>=0|| tags.indexOf("|"+tag+"|") >= 0){continue;}
			tags.append(tag+"|");
		}*/
		
		rs = stmt.executeQuery("select modifier, tag from "+this.tableprefix+"_sentence where tag  like '[%]'"); //inner [tepal]
		while(rs.next()){
			String m = rs.getString("modifier");
			m = m.replaceAll("\\[^\\[*\\]", ""); 
			if(m.compareTo("")!= 0){
				String tag = null;
				if(m.lastIndexOf(" ")<0){
					tag = m;
				}else{
					tag = m.substring(m.lastIndexOf(" ")+1); //last word from modifier
				}
				if(tag == null ||tag.indexOf("[")>=0|| tags.indexOf("|"+tag+"|") >= 0 || tag.indexOf("[")>=0 || tag.matches(".*?(\\d|"+StateCollector.stop+").*")){continue;}
				tags.append(tag+"|");
			}
		}
	}
	
	protected void organNameFromGloss(StringBuffer tags, Statement stmt)
			throws SQLException {
		ResultSet rs = stmt.executeQuery("select distinct term from "+this.glosstable+" where category in ('STRUCTURE', 'FEATURE', 'SUBSTANCE', 'PLANT', 'nominative', 'structure')");
		while(rs.next()){
			String term = rs.getString("term").trim();
			if(term == null){continue;}
			term = term.indexOf(" ")> 0? term.substring(term.lastIndexOf(' ')+1) : term;
			tags.append(term+"|");
		}
	}
	
	protected String colorsFromGloss()
			throws SQLException {
		StringBuffer colors = new StringBuffer();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select distinct term from "+this.glosstable+" where category in ('coloration', 'color')");
		while(rs.next()){
			String term = rs.getString("term").trim();
			if(term == null){continue;}
			term = term.indexOf(" ")> 0? term.substring(term.lastIndexOf(' ')+1) : term;
			colors.append(term+"|");
		}
		return colors.toString().replaceFirst("\\|$", "");
	}
	
    private void resetOutputMessage() {
		display.syncExec(new Runnable() {
			public void run() {
				charLog.setText("");
			}
		});
	}
    
	private void showOutputMessage(final String message) {
		if(display!=null){
		display.syncExec(new Runnable() {
			public void run() {
				charLog.append(message+"\n");
			}
		});
		}
	}

}
