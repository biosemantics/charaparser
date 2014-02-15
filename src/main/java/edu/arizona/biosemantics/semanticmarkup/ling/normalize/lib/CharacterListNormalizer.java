package edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Singleton;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;

/**
 * 
 * @author Hong Cui
 * 
 * turn "brown to black" to "coloration~list~brown~to~black"
 * this is a singleton class
 *
 */
public class CharacterListNormalizer{
	private ICharacterKnowledgeBase characterKnowledgeBase;
	private IOrganStateKnowledgeBase organStateKnowledgeBase;
	private String negations = "not|never|seldom";//not -ly words. -ly words are already treated in character list patterns
	private String or = "_or_";
	private String noadjorg ="low"; //list of position terms that can not be used as a ref to an organ/part
	private Pattern charalistpattern = Pattern.compile("(.*?(?:^| ))(([0-9a-z–\\[\\]\\+-]+ly )*([_a-z-]+ )+[& ]*([`@,;\\.] )+\\s*)(([_a-z-]+ |[0-9a-z–\\[\\]\\+-]+ly )*(\\4)+([0-9a-z–\\[\\]\\+-]+ly )*[`@,;\\.%\\[\\]\\(\\)&#a-z].*)");//
	//private Pattern charalistpattern = Pattern.compile("(.*?(?:^| ))(([0-9a-z–\\[\\]\\+-]+ly )*([_a-z-]+ )+[& ]*([@,;\\.] )+\\s*)(([_a-z-]+ |[0-9a-z–\\[\\]\\+-]+ly )*(\\4)+([0-9a-z–\\[\\]\\+-]+ly )*[@,;\\.%\\[\\]\\(\\)&#a-z].*)");//
	private Pattern charalistpattern2 = Pattern.compile("(([a-z-]+ )*([a-z-]+ )+([0-9a-z–\\[\\]\\+-]+ly )*[& ]*([`@,;\\.] )+\\s*)(([a-z-]+ |[0-9a-z–\\[\\]\\+-]+ly )*(\\3)+([0-9a-z–\\[\\]\\+-]+ly )*[`@,;\\.%\\[\\]\\(\\)&#a-z].*)");//merely shape, @ shape
	//private Pattern charalistpattern2 = Pattern.compile("(([a-z-]+ )*([a-z-]+ )+([0-9a-z–\\[\\]\\+-]+ly )*[& ]*([@,;\\.] )+\\s*)(([a-z-]+ |[0-9a-z–\\[\\]\\+-]+ly )*(\\3)+([0-9a-z–\\[\\]\\+-]+ly )*[@,;\\.%\\[\\]\\(\\)&#a-z].*)");//merely shape, @ shape
	private static CharacterListNormalizer singleton = null;
	
	
	private CharacterListNormalizer(ICharacterKnowledgeBase characterKnowledgeBase, IOrganStateKnowledgeBase organStateKnowledgeBase){
		this.characterKnowledgeBase = characterKnowledgeBase;
		this.organStateKnowledgeBase = organStateKnowledgeBase;
	}
	
	public static CharacterListNormalizer getInstance(ICharacterKnowledgeBase characterKnowledgeBase, IOrganStateKnowledgeBase organStateKnowledgeBase){
		if(singleton == null)
			return new CharacterListNormalizer(characterKnowledgeBase, organStateKnowledgeBase);
		else 
			return singleton;
	}
	/**
	 * deal with sentences with or without parentheses
	 * @param chunkedTokens 
	 * @return paraentheses-normalized String
	 */
	String normalizeParentheses(String str){
		ArrayList<String> chunkedTokens = new ArrayList<String>(Arrays.asList(str.split("\\s+")));
		ArrayList<String> characterTokensReversed = lookupCharacters(str, true); //treating -ly as -ly
		
		//use & as place holders
		//create list by replace (...) with &s
		//create lists by replace things not in () with &s
		//normalizeCharacterLists(list)
		//merge result
		
		//e.g leaves lanceolate ( outer ) to linear ( inner ) 
		String inlist = ""; //represent tokens not in brackets: # size & & & @ size & & &
		String outlist = ""; //represent tokens in brackets   : & & & position & & & & position & 
		int inbrackets = 0;
		
		boolean hasbrackets = false;
		for(int i = characterTokensReversed.size() -1; i>=0; i--){
			String t = characterTokensReversed.get(i);
			if(t.equals("(") || t.equals("[")){
				inbrackets++;
				outlist += "& ";
				inlist += "& ";
				hasbrackets = true;
			}else if(t.equals(")") || t.equals("]")){
				inbrackets--;
				outlist += "& ";
				inlist += "& ";
				hasbrackets = true;
			}else if(inbrackets==0){
				outlist+=t+" ";
				inlist += "& ";
			}else{
				outlist+="& ";
				inlist+=t+" ";
			}
		}
		//log(LogLevel.DEBUG, "outList " + outlist);
		outlist = outlist.trim()+" "; //need to have a trailing space
		normalizeCharacterLists(outlist, chunkedTokens); //chunkedtokens updated

		if(hasbrackets){
			inlist = inlist.trim()+" "; //need to have a trailing space
			normalizeCharacterLists(inlist, chunkedTokens); //chunkedtokens updated
			//deal with cases where a range is separated by parentheses: eg. (, {yellow-gray}, to, ), {coloration~list~brown~to~black}
			int orphanedto = getIndexOfOrphanedTo(inlist, 0, chunkedTokens); //inlist as a list
			while(orphanedto >=0){
				String chara = getCharaOfTo(inlist, orphanedto);
				if(orphanedto+2 < chunkedTokens.size() && chunkedTokens.get(orphanedto+1).equals(")")){
					String nextchara = chunkedTokens.get(orphanedto+2);
					if(nextchara.contains(chara)){//form a range cross parenthetical boundary, eg. (, {yellow-gray}, to, ), {coloration~list~brown~to~black}
						if(nextchara.contains("~list~")){
							nextchara = nextchara.substring(nextchara.indexOf("~list~")+6);
							//form new range
							String range =chara+"~list~"+chunkedTokens.get(orphanedto-1)+"~to~"+nextchara;
							chunkedTokens.set(orphanedto-1, range);
							chunkedTokens.set(orphanedto, "");
						}
					}
				}
				orphanedto = getIndexOfOrphanedTo(inlist, ++orphanedto, chunkedTokens); 
			}
		}
		
		String result = "";
		for(int i = 0; i<chunkedTokens.size(); i++){
			result += chunkedTokens.get(i)+" ";
		}
		return result.replaceAll("\\s+", " ").trim(); //{shape~list~lanceolate~(~outer~)~to~linear}, note the constraint( inner ) after liner is not in the shape list, it will be associated to "linear" later in the process (in annotator) when more information become available for more reliable associations.
	}
	
	/**
	 * populate charactertokensReversed with a character representation of the str, in reversed token order.
	 * str: <blades> {lance-ovate} to {narrowly} {lanceolate} . 
	 * return: . shape narrowly ` shape #
	 * @param str
	 * @param markadv
	 */
	ArrayList<String> lookupCharacters(String str, boolean markadv) {		
		if(str.trim().length() ==0){
			return null;
		}
		ArrayList<String> characterTokensReversed = new ArrayList<String>();
		boolean save = false;
		boolean ambiguous = false;
		ArrayList<String> saved = new ArrayList<String>();
		
		ArrayList<String> amb = new ArrayList<String>();
		ArrayList<String> chunkedTokens = new ArrayList<String>(Arrays.asList(str.split("\\s+")));
		for(int i = chunkedTokens.size()-1; i>=0; i--){
			String word = chunkedTokens.get(i);	
			if(word.compareTo("one")==0 && encounteredCount(characterTokensReversed)){
				characterTokensReversed.add("count");
				continue;
			}
			if(word.indexOf("~list~")>0){
				String ch = word.substring(0, word.indexOf("~list~")).replaceAll("\\W", "").replaceFirst("ttt$", "");
				characterTokensReversed.add(ch);
			}else if(organStateKnowledgeBase.isState(word) && !organStateKnowledgeBase.isOrgan(word)) {
				String ch = characterKnowledgeBase.getCharacterName(word); //remember the char for this word (this word is a word before (to|or|\\W)
				if(ch==null){
					characterTokensReversed.add(word); //
				}else{
					//deal with cases where a position is used as a structure: {outer} {lance-ovate} to {narrowly} {lanceolate} 
					if(ch.compareTo("position")==0 && !word.matches(noadjorg) && characterTokensReversed.size()>0){//word = {outer}
						String character = characterTokensReversed.get(characterTokensReversed.size()-1); //character of the phrase following word in the str.
						if(character.matches("\\w+") && !hasModifiedStructure(characterTokensReversed) ){//not #, %, `,@
							//change {outer} to <outer>
							//word = word.replaceFirst("\\{", "<").replaceFirst("\\}", ">");
							chunkedTokens.set(i, word);
							characterTokensReversed.add("#");
							save = true;
							continue;
						}
					}
					characterTokensReversed.add(ch); //color
					if(save){
						save(saved, chunkedTokens.size()-1-i, ch); 
						if(ch.indexOf(or)>0){
							ambiguous = true;
							amb.add(chunkedTokens.size()-1-i+"");
						}
					}
					save = false;
				}
			}else if (organStateKnowledgeBase.isOrgan(word)){
				characterTokensReversed.add("#");
				save = true;
			}else if(word.matches("(or|and-or|and/or|and_or)") || word.matches("\\S+ly~(or|and-or|and/or|and_or)~\\S+ly")){//loosely~to~densely 
				characterTokensReversed.add("@"); //or
				save = true;
			}else if(word.equals("to") || word.matches("\\S+ly~to~\\S+ly")){//loosely~to~densely 
				characterTokensReversed.add("`"); //to
				save = true;
			}else if(word.compareTo("±")==0 || word.compareTo("moreorless")==0){//±
				characterTokensReversed.add("moreorlessly"); //,;. add -ly so it will be treated as an adv.
				save = true;
			}else if(word.matches("\\W")){
				if(word.matches("[()\\[\\]]")) save(saved, chunkedTokens.size()-1-i, word); 
				characterTokensReversed.add(word); //,;.
				save = true;
			}else if(markadv && word.endsWith("ly")){
				characterTokensReversed.add(word);
				save = true;
			}else if(word.matches("("+this.negations+")")){//not
				characterTokensReversed.add(word);
				save = true;
			}else{
				characterTokensReversed.add("%");
				save = true;
			}
		}
		
		//deal with a/b characters
		if(ambiguous){
			Iterator<String> it = amb.iterator();
			while(it.hasNext()){
				int i = Integer.parseInt(it.next());
				Pattern p = Pattern.compile("("+characterTokensReversed.get(i)+"|"+characterTokensReversed.get(i).replaceAll(or, "|")+")");
				String tl = lastSaved(saved, i);
				Matcher m = p.matcher(tl);
				//if(m.matches()){
				if(m.find()){
					characterTokensReversed.set(i, m.group(1));
				}else{
					String tn = nextSaved(saved, i);
					m = p.matcher(tn);
					//if(m.matches()){
					if(m.find()){
						characterTokensReversed.set(i, m.group(1));
					}
				}
			}
		}
		//log(LogLevel.DEBUG, "characterTokensReversed " + this.charactertokensReversed);
		return characterTokensReversed;
	}
	/*ArrayList<String> lookupCharacters(String str, boolean markadv, ArrayList<String> chunkedTokens) {		
		ArrayList<String> characterTokensReversed = new ArrayList<String>();
		boolean save = false;
		boolean ambiguous = false;
		ArrayList<String> saved = new ArrayList<String>();
		
		ArrayList<String> amb = new ArrayList<String>();
		for(int i = chunkedTokens.size()-1; i>=0+0; i--){
			String word = chunkedTokens.get(i);	
			if(word.indexOf("~list~")>0){
				String ch = word.substring(0, word.indexOf("~list~")).replaceAll("\\W", "").replaceFirst("ttt$", "");
				characterTokensReversed.add(ch);
			}else if(organStateKnowledgeBase.isState(word) && !organStateKnowledgeBase.isOrgan(word)) {
				String ch = characterKnowledgeBase.getCharacterName(word); //remember the char for this word (this word is a word before (to|or|\\W)
				if(ch==null){
					characterTokensReversed.add(word.replaceAll("[{}]", "")); //
				}else{
					characterTokensReversed.add(ch); //color
					if(save){
						save(saved, chunkedTokens.size()-1-i, ch); 
						if(ch.indexOf(or)>0){
							ambiguous = true;
							amb.add(chunkedTokens.size()-1-i+"");
						}
					}
					save = false;
				}
			}else if (organStateKnowledgeBase.isOrgan(word)){
				characterTokensReversed.add("#");
				save = true;
			}else if(word.matches("(to|or|and-or|and/or|and_or)") || word.matches("\\S+ly~(to|or|and-or|and/or|and_or)~\\S+ly")){//loosely~to~densely 
				characterTokensReversed.add("@"); //to|or
				save = true;
			}else if(word.compareTo("±")==0){//±
				characterTokensReversed.add("moreorlessly"); //,;. add -ly so it will be treated as an adv.
				save = true;
			}else if(word.matches("\\W")){
				if(word.matches("[()\\[\\]]")) save(saved, chunkedTokens.size()-1-i, word); 
				characterTokensReversed.add(word); //,;.
				save = true;
			}else if(markadv && word.endsWith("ly")){
				characterTokensReversed.add(word);
				save = true;
			}else{
				characterTokensReversed.add("%");
				save = true;
			}
		}
		
		//deal with a/b characters
		if(ambiguous){
			Iterator<String> it = amb.iterator();
			while(it.hasNext()){
				int i = Integer.parseInt(it.next());
				Pattern p = Pattern.compile("("+characterTokensReversed.get(i)+"|"+characterTokensReversed.get(i).replaceAll(or, "|")+")");
				String tl = lastSaved(saved, i);
				Matcher m = p.matcher(tl);
				//if(m.matches()){
				if(m.find()){
					characterTokensReversed.set(i, m.group(1));
				}else{
					String tn = nextSaved(saved, i);
					m = p.matcher(tn);
					//if(m.matches()){
					if(m.find()){
						characterTokensReversed.set(i, m.group(1));
					}
				}
			}
		}
		//log(LogLevel.DEBUG, "characterTokensReversed " + this.charactertokensReversed);
		return characterTokensReversed;
	}*/
	
	/**
	 * connect a list of character states of the same character together, 
	 * for example: keeled , elliptic to broadly ovate => shape~list~keeled~punct~elliptic~to~broadly~ovate 
	 * example 2: not keeled , elliptic to broadly ovate => shape~list~not~keeled~punct~elliptic~to~broadly~ovate //"rarely" will be treated the same way as "not"
	 * example 3: not keeled or ovate => shape~list~not~keeled~or~ovate //because of "or", the modifer needs to be applied to all states in the or-list
	 *
	 * @return updated string in format of {shape~list~elliptic~to~broadly~ovate} 
	 */
	private void normalizeCharacterLists(String list, ArrayList<String> chunkedTokens){
		ArrayList<String> copy = (ArrayList<String>) chunkedTokens.clone();
		//pattern match: collect state one by one
		String listcopy = list;
		int base = 0;
		//Pattern pt = Pattern.compile("(.*?(?:^| ))(([0-9a-z–\\[\\]\\+-]+ly )*([a-z-]+ )+([@,;\\.] )+\\s*)(([a-z-]+ )*(\\4)+[@,;\\.%\\[\\]\\(\\)#].*)");//
		Matcher mt = charalistpattern.matcher(list);
		String connector = "";
		while(mt.matches()){
			int start = (mt.group(1).trim()+" a").trim().split("\\s+").length+base-1; //"".split(" ") == 1
			String l = mt.group(2); //the first state
			String ch = mt.group(4).trim();
			list = mt.group(6);
			Matcher m = charalistpattern2.matcher(list);
			while(m.matches()){ //all following states
				String another = m.group(1);
				l += m.group(1);
				//connector = m.group(5).matches(".*?[@`].*")? m.group(5) : connector; //the last non-punct connector
				list = m.group(6);
				m = charalistpattern2.matcher(list);
			}
			//l += list.replaceFirst("[@,;\\.%\\[\\]\\(\\)#].*$", "");//take the last seg from the list
			//l += list.replaceFirst("[@,;\\.%\\[\\]\\(\\)&#].*$", "");//take the last seg from the list 6/29/2012
			l += list.replaceFirst("(?<="+ch+"(\\s[0-9a-z–\\[\\]\\+-]{1,10}ly)?).*$", "");//arrangement_or_shape @ arrangement_or_shape coating_or_texture # ;
			int end = start+(l.trim()+" b").trim().split("\\s+").length-1;
			//if(! l.matches(".*?@[^,;\\.]*") && l.matches(".*?,.*")){ //the last state is not connected by or/to, then it is not a list
			//	start = end;
			//}
			while(! l.matches(".*?[`@][^,;\\.]*") && l.matches(".*?,.*")){ //the last state is not connected by or/to, then it is not a list
				l = l.replaceFirst("[,;\\.][^@`]*$", "").trim();
			}
			if(l.indexOf('@')>0 || l.indexOf('`')>0){
				end = start+(l.trim()+" b").trim().split("\\s+").length-1;
			}else{
				start = end;
			}
				
			
			//list = list.replaceFirst("^.*?(?=[@,;\\.%\\[\\]\\(\\)#])", ""); //6/29/2012
			//list = list.replaceFirst("^.*?(?=[@,;\\.%\\[\\]\\(\\)&#])", "");
			list = segByWord(listcopy, end);
			mt = charalistpattern.matcher(list);
			

			//6/29/12
			//for(int i = base; i<start; i++){
			//	result += this.chunkedtokens.get(i)+" ";
			//}
			if(end>start){ //if it is a list
				connector = l.replaceAll("[^`@]", "").charAt(0)+"";
				//triage: "not a, b, or c" is fine; "not a, b to c" is not
				if(connector.trim().equals("`")){//if connector is "to", then "not"-modified state should be removed.
					//check if l starts with "not"
					while(l.matches("(not|never)\\b.*")){//remove negated states from the begaining of l one by one
						if(l.indexOf(",")<0) break;
						String notstate = l.substring(0, l.indexOf(",")+1);
						l = l.substring(l.indexOf(",")+1).trim();
						start = start + (notstate.trim()+" b").trim().split("\\s+").length - 1;
					}
				}
				//if connector is "or", then "not"-modified state should be included, no additional action is needed.
				
				//adjust this.chunkedtokens.
				String t= ch+"~list~";
				int leftsquare = 0;
				int leftround = 0;
				int i;
				for(i = start; i<end; i++){
					if(chunkedTokens.get(i).compareTo("[")==0) leftsquare++;//make sure the brackets in the chunk are all matched up.
					if(chunkedTokens.get(i).compareTo("(")==0) leftround++;
					if(chunkedTokens.get(i).compareTo("]")==0) leftsquare--;
					if(chunkedTokens.get(i).compareTo(")")==0) leftround--;
					if(chunkedTokens.get(i).length()>0){
						//if(t.indexOf("<")>0){
						if(this.organStateKnowledgeBase.isOrgan(t)){	
							//case: {shape~list~ovate~to~lance-ovate~(~glabrous~or~sparsely~glandular-pubescent~punct~<apices>~acute~to~acuminate~)} 
							//this is caused by adding more tokens to t to complete a open bracket
							//solution: abort normalization
							chunkedTokens = copy;
							return;								
						}
						t += chunkedTokens.get(i).trim().replaceAll("[,;\\.]", "punct")+"~";
					}else if(i == end-1){
						while(chunkedTokens.get(i).length()==0){
							i++;
						}
						t+=chunkedTokens.get(i).trim().replaceAll("[,;\\.]", "punct")+"~";
					}
					chunkedTokens.set(i, "");
				}
				
				for(; i<chunkedTokens.size(); i++){
					  if(leftsquare!=0 || leftround!=0){
						if(chunkedTokens.get(i).compareTo("[")==0) leftsquare++;
						if(chunkedTokens.get(i).compareTo("(")==0) leftround++;
						if(chunkedTokens.get(i).compareTo("]")==0) leftsquare--;
						if(chunkedTokens.get(i).compareTo(")")==0) leftround--;
						if(chunkedTokens.get(i).length()>0){
							if(this.organStateKnowledgeBase.isOrgan(t)/* t.indexOf("<")>0*/){
								//case: {shape~list~ovate~to~lance-ovate~(~glabrous~or~sparsely~glandular-pubescent~punct~<apices>~acute~to~acuminate~)} 
								//this is caused by adding more tokens to t to complete a open bracket
								//solution: abort normalization
								chunkedTokens = copy;
								return;								
							}
							t += chunkedTokens.get(i).trim().replaceAll("[,;\\.]", "punct")+"~";
						}else if(i == end-1){
							while(chunkedTokens.get(i).length()==0){
								i++;
							}
							t+=chunkedTokens.get(i).trim().replaceAll("[,;\\.]", "punct")+"~";
						}
						chunkedTokens.set(i, "");
					}else{
						break;
					}
				}
				t = t.replaceFirst("~$", "")+" ";
				if(t.indexOf("ttt~list")>=0) t = t.replaceAll("~color.*?ttt~list", "");
				chunkedTokens.set(start, t);
				//if(this.printCharacterList){
				//	System.out.println(this.src+":"+">>>"+t);
				//}
			}
			base = end;
		}
		
		//6/29/12
		//for(int i = base; i<(list.trim()+" b").trim().split("\\s+").length+base-1; i++){
		//	result += this.chunkedtokens.get(i)+" ";
		//}
		//return result.trim();
	}
	/*private void normalizeCharacterLists(String list, ArrayList<String> chunkedTokens){
		//charactertokens.toString
		//String list = ""; //6/29/12
		//String result = ""; //6/29/12
		
		//lookupCharacters(src, true); //treating -ly as -ly 6/29/12
		
		//6/29/12
		//for(int i = this.charactertokensReversed.size() -1; i>=0; i--){
		//	list+=this.charactertokensReversed.get(i)+" ";
		//}
		//list = list.trim()+" "; //need to have a trailing space
		
		//pattern match: collect state one by one
		String listcopy = list;
		//log(LogLevel.DEBUG, list);
		int base = 0;
		//Pattern pt = Pattern.compile("(.*?(?:^| ))(([0-9a-z�\\[\\]\\+-]+ly )*([a-z-]+ )+([@,;\\.] )+\\s*)(([a-z-]+ )*(\\4)+[@,;\\.%\\[\\]\\(\\)#].*)");//
		Matcher mt = charalistpattern.matcher(list);
		while(mt.matches()){
			int start = (mt.group(1).trim()+" a").trim().split("\\s+").length+base-1; //"".split(" ") == 1
			String l = mt.group(2);
			String ch = mt.group(4).trim();
			list = mt.group(6);
			//Pattern p = Pattern.compile("(([a-z-]+ )*([a-z-]+ )+([@,;\\.] )+\\s*)(([a-z-]+ )*(\\3)+[@,;\\.%\\[\\]\\(\\)#].*)");//merely shape, @ shape
			Matcher m = charalistpattern2.matcher(list);
			while(m.matches()){
				l += m.group(1);
				//list = m.group(5);
				list = m.group(6);
				m = charalistpattern2.matcher(list);
			}
			//l += list.replaceFirst("[@,;\\.%\\[\\]\\(\\)#].*$", "");//take the last seg from the list
			//l += list.replaceFirst("[@,;\\.%\\[\\]\\(\\)&#].*$", "");//take the last seg from the list 6/29/2012
			l += list.replaceFirst("(?<="+ch+"(\\s[0-9a-z–\\[\\]\\+-]{1,10}ly)?).*$", "");//arrangement_or_shape @ arrangement_or_shape coating_or_texture # ;
			int end = start+(l.trim()+" b").trim().split("\\s+").length-1;
			//if(! l.matches(".*?@[^,;\\.]*") && l.matches(".*?,.*")){ //the last state is not connected by or/to, then it is not a list
			//	start = end;
			//}
			while(! l.matches(".*?@[^,;\\.]*") && l.matches(".*?,.*")){ //the last state is not connected by or/to, then it is not a list
				l = l.replaceFirst("[,;\\.][^@]*$", "").trim();
			}
			if(l.indexOf('@')>0){
				end = start+(l.trim()+" b").trim().split("\\s+").length-1;
			}else{
				start = end;
			}
				
			
			//list = list.replaceFirst("^.*?(?=[@,;\\.%\\[\\]\\(\\)#])", ""); //6/29/2012
			//list = list.replaceFirst("^.*?(?=[@,;\\.%\\[\\]\\(\\)&#])", "");
			list = segByWord(listcopy, end);
			mt = charalistpattern.matcher(list);
			
			//6/29/12
			//for(int i = base; i<start; i++){
			//	result += this.chunkedtokens.get(i)+" ";
			//}
			if(end>start){ //if it is a list
				String t= "{"+ch+"~list~";
				for(int i = start; i<end; i++){
					if(chunkedTokens.get(i).length()>0){
						t += chunkedTokens.get(i).trim().replaceAll("[{}]", "").replaceAll("[,;\\.]", "punct")+"~";
					}else if(i == end-1){
						while(chunkedTokens.get(i).length()==0){
							i++;
						}
						t+=chunkedTokens.get(i).trim().replaceAll("[{}]", "").replaceAll("[,;\\.]", "punct")+"~";
					}
					chunkedTokens.set(i, "");
				}
				t = t.replaceFirst("~$", "}")+" ";
				if(t.indexOf("ttt~list")>=0) t = t.replaceAll("~color.*?ttt~list", "");
				chunkedTokens.set(start, t);
				//result +=t; //6/29/12
				//if(this.printCharacterList){
				//	if(this.src.equals("100.txt-1"))
				//		log(LogLevel.DEBUG, this.src+":"+">>>"+t);
				//}
			}
			base = end;
		}
		
		//6/29/12
		//for(int i = base; i<(list.trim()+" b").trim().split("\\s+").length+base-1; i++){
		//	result += this.chunkedtokens.get(i)+" ";
		//}
		//return result.trim();
	}*/

	/**
	 * when "to"[@] is the last token in bracketed phrase:
	 * e.g. (, {yellow-gray}, to, ), {coloration~list~brown~to~black}
	 * @param inlist: & & & & & & & & & & & & & & & coloration @ & & & & & & & & 
	 * @return first indexof such "@" as a word after startindex
	 */
	private int getIndexOfOrphanedTo(String inlist, int startindex, ArrayList<String> chunkedTokens) {
		List<String> symbols =  Arrays.asList(inlist.trim().split("\\s+"));
		boolean found = false;
		for(int i = startindex; i < chunkedTokens.size()-1; i++){		
			if(chunkedTokens.get(i).equals("to") && chunkedTokens.get(i+1).equals(")")){
				return i;
			}
		}
		return -1;
	}

	/**
	 * when "to"[@] is the last token in bracketed phrase:
	 * e.g. (, {yellow-gray}, to, ), {coloration~list~brown~to~black}
	 * @param inlist: & & & & & & & & & & & & & & & coloration @ & & & & & & & & 
	 * @param index of "@"
	 * @return the character before "@"
	 */
	
	private String getCharaOfTo(String inlist, int orphanedto) {
		List<String> symbols = Arrays.asList(inlist.trim().split("\\s+"));
        return  symbols.get(orphanedto-1);
	}
	
	
	private void save(ArrayList<String> saved, int index, String ch){
		while(saved.size()<=index){
			saved.add("");
		}
		saved.set(index, ch);
	}
	

	/**
	 * lookback
	 * @param saved
	 * @param index
	 * @return
	 */
	private String lastSaved(ArrayList<String> saved, int index){
		int inbrackets = 0;
		for(int i = index-1; i >=0 && i<saved.size(); i--){
			String c = saved.get(i).trim();
			if(c.equals("(") || c.equals("[")) inbrackets++; //ignore characters in brackets
			else if(c.equals(")") || c.equals("]")) inbrackets--;
			else if(inbrackets ==0 && c.length()>0) return c;
		}
		return "";
	}
	
	/**
	 * lookahead
	 * @param saved
	 * @param index
	 * @return looked up String
	 */
	private String nextSaved(ArrayList<String> saved, int index){
		int inbrackets = 0;
		for(int i = index+1; i <saved.size(); i++){
			String c = saved.get(i).trim();
			if(c.equals("(") || c.equals("[")) inbrackets++; //ignore characters in brackets
			else if(c.equals(")") || c.equals("]")) inbrackets--;
			else if(inbrackets ==0 && c.length()>0) return c;			
		}
		return "";
	}
	
	private String segByWord(String listcopy, int startindex) {
		String seg = "";
		if(startindex < 0) return seg;
		String[] tokens = listcopy.trim().split("\\s+");
		for(int i = startindex; i < tokens.length; i++){
			seg += tokens[i]+" ";
		}
		return seg.trim();
	}
	
	private boolean encounteredCount(ArrayList<String> characterTokensReversed) {
		for(int i = characterTokensReversed.size()-1; i >=0; i--){
			if(characterTokensReversed.get(i).matches(".*?\\w.*") && characterTokensReversed.get(i).compareTo("count")!=0) return false;
			if(characterTokensReversed.get(i).compareTo("count")==0) return true;
		}
		return false;
	}
	
	/**
	 * a test used to see if a postion term should be converted to a structure term
	 * if the position term hasModifiedStructure, then it should not be converted 
	 * Scan through charactertokensReversed from back to front
	 * @param charactertokensReversed2 "# color position"
	 * @return true: e.g., "with {medial} {red} <bloth>": {medial} should stay as position 
	 *         false: "or {outer} smaller ," {outer} should be converted to <outer>
	 */
	private boolean hasModifiedStructure(ArrayList<String> characterTokensReversed) {
		boolean has = true;
		for(int i = characterTokensReversed.size()-1; i >=0; i--){
			if(characterTokensReversed.get(i).compareTo("#")==0) return true; 
			if(characterTokensReversed.get(i).matches("[^a-z0-9_`@-]+")) return false; 
		}
		return has;
	}

}
