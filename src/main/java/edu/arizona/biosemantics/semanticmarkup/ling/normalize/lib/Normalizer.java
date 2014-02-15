package edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;








import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.INormalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.ParentTagProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.AdjectiveReplacementForNoun;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * Normalizer implements a strategy to normalize text according to the previous version of charaparser
 */
public abstract class Normalizer implements INormalizer {
	
	private String or = "_or_";
	private String units;
	private IGlossary glossary;
	private String numberPattern;
	private HashMap<String, String> singulars, plurals;
	private Pattern p1, p2, p3, p4, p5, p6, p7, p8, p75, lyAdverbPattern;
	private IPOSKnowledgeBase posKnowledgeBase;
	private ITerminologyLearner terminologyLearner;
	private Pattern viewPattern, countPattern, positionPattern, romanRangePattern, romanPattern;
	private String[] romanNumbers;
	private Set<String> stopWords;
	private String prepositionWords;
	private Pattern modifierList;
	private ICharacterKnowledgeBase characterKnowledgeBase;
	private Pattern range = Pattern.compile("(.*?)\\b(?:from|between)\\s*([\\d\\. /\\(\\)\\?+-]+)\\s*(?:to|and|-)\\s*([\\d\\. /\\(\\)\\?+-]+)(.*)");
	private Pattern numbergroup = Pattern.compile("(.*?)([()\\[\\]\\-\\–\\d\\.×x\\+²½/¼\\*/%\\?]*?[½/¼\\d]?[()\\[\\]\\-\\–\\d\\.,?×x\\+²½/¼\\*/%\\?]{1,}(?![a-z]))(.*)"); //added , and ? for chromosome counts, used {1, } to include single digit expressions such as [rarely 0]
	//private Pattern hyphenedtoorpattern = Pattern.compile("(.*?)((\\d-,\\s*)+ (to|or) \\d-\\{)(.*)");	
	private Pattern hyphenedtoorpattern = Pattern.compile("(.*?)((\\d-{0,1},{0,1}\\s*)+ (to|or) \\d-(\\w+))(\\b.*)");
	private Pattern numberpattern = Pattern.compile("[()\\[\\]\\-\\–\\d\\.×x\\+²½/¼\\*/%\\?]*?[½/¼\\d][()\\[\\]\\-\\–\\d\\.,?×x\\+²½/¼\\*/%\\?]{2,}(?![a-z])"); //added , and ? for chromosome counts
	//positionptn does not apply to FNA, it may apply to animal descriptions e.g. rib_I, rib_II,
	//private static Pattern positionptn = Pattern.compile("(<(\\S+?)> \\d+(?: and \\d+)?)"); // <{wing}> 1 – 3 cm ;
	private static Pattern positionptn = Pattern.compile("(<(\\S+?)> \\d+(?:(?: and |_)\\d+)?(?!\\s*(?:\\.|/|times)))");//changed to match "4_5", "4 and 5" but not "<structure> 2 / 5" or "<structure> 2 times"

	private Pattern modifierlist = Pattern.compile("(.*?\\b)(\\w+ly\\s+(?:to|or)\\s+\\w+ly)(\\b.*)");
	private String countp = "more|fewer|less|\\d+";
	private Pattern countptn = Pattern.compile("((?:^| )(?:"+countp+") (?:or|to) (?:"+countp+")(?: |$))");
	private Pattern colorpattern = Pattern.compile("(.*?)((coloration|color)\\s+%\\s+(?:(?:coloration|color|@|%) )*(?:coloration|color))\\s((?![^,;()\\[\\]]*[#]).*)");
	private Pattern distributePrepPattern = Pattern.compile("(^.*~list~)(.*?~with~)(.*?~or~)(.*)");
	private Pattern areapattern = Pattern.compile("(.*?)([\\d\\.()+-]+ ?[µucmd]?m?\\s*[x×]\\S*\\s*[\\d\\.()+-]+ [µucmd]?m\\s*[x×]?(\\S*\\s*[\\d\\.()+-]+ [µucmd]?m)?)(.*)");
	//private Pattern areapattern = Pattern.compile("(.*?)([\\d\\.()+-]+ \\{?[cmd]?m\\}?×\\S*\\s*[\\d\\.()+-]+ \\{?[cmd]?m\\}?×?(\\S*\\s*[\\d\\.()+-]+ \\{?[cmd]?m\\}?)?)(.*)");
	private Pattern viewptn = Pattern.compile( "(.*?\\b)((?:in|at)\\s+[a-z_ -]*\\s*(?:view|profile|closure))(\\s.*)"); //to match in dorsal view and in profile
	//private Pattern viewptn = Pattern.compile( "(.*?\\b)(in\\s+[a-z_<>{} -]*\\s*[<{]*(?:view|profile)[}>]*)(\\s.*)"); //to match in dorsal view and in profile
	private Pattern bulletpattern  = Pattern.compile("^(and )?([(\\[]\\s*\\d+\\s*[)\\]]|\\d+.)\\s+(.*)"); //( 1 ), [ 2 ], 12.
	private Pattern asaspattern = Pattern.compile("(.*?\\b)(as\\s+[\\w]+\\s+as)(\\b.*)");
	private IOrganStateKnowledgeBase organStateKnowledgeBase;
	private IInflector inflector;
	//private Pattern charalistpattern = Pattern.compile("(.*?(?:^| ))(([0-9a-z–\\[\\]\\+-]+ly )*([_a-z-]+ )+[& ]*([`@,;\\.] )+\\s*)(([_a-z-]+ |[0-9a-z–\\[\\]\\+-]+ly )*(\\4)+([0-9a-z–\\[\\]\\+-]+ly )*[`@,;\\.%\\[\\]\\(\\)&#a-z].*)");//
	//private Pattern charalistpattern = Pattern.compile("(.*?(?:^| ))(([0-9a-z–\\[\\]\\+-]+ly )*([_a-z-]+ )+[& ]*([@,;\\.] )+\\s*)(([_a-z-]+ |[0-9a-z–\\[\\]\\+-]+ly )*(\\4)+([0-9a-z–\\[\\]\\+-]+ly )*[@,;\\.%\\[\\]\\(\\)&#a-z].*)");//
	//private Pattern charalistpattern2 = Pattern.compile("(([a-z-]+ )*([a-z-]+ )+([0-9a-z–\\[\\]\\+-]+ly )*[& ]*([`@,;\\.] )+\\s*)(([a-z-]+ |[0-9a-z–\\[\\]\\+-]+ly )*(\\3)+([0-9a-z–\\[\\]\\+-]+ly )*[`@,;\\.%\\[\\]\\(\\)&#a-z].*)");//merely shape, @ shape
	//private Pattern charalistpattern2 = Pattern.compile("(([a-z-]+ )*([a-z-]+ )+([0-9a-z–\\[\\]\\+-]+ly )*[& ]*([@,;\\.] )+\\s*)(([a-z-]+ |[0-9a-z–\\[\\]\\+-]+ly )*(\\3)+([0-9a-z–\\[\\]\\+-]+ly )*[@,;\\.%\\[\\]\\(\\)&#a-z].*)");//merely shape, @ shape
	private Pattern vaguenumberptn1 = Pattern.compile("(.*?)\\b((?:equal[ _-]to|(?:more|greater|less|fewer) than|or| )+) ([\\d.]+)(.*)");
	private Pattern vaguenumberptn2 = Pattern.compile("(.*?)0(-[\\d.]+)( \\w+ )(?:and|but) ([\\d.]+)\\+(.*)");
	private String[] modifierphrases = new String[] {"at first"};
	private HashSet<String> modifiertokens = new HashSet<String>();
	private Pattern compoundPPptn;    
	private ParentTagProvider parentTagProvider;
	private String adjnounslist;
	private Map<String, String> adjnounsent;
	private Map<String, AdjectiveReplacementForNoun> replacements;
	private CharacterListNormalizer cln;
	
	
	/**
	 * @param glossary
	 * @param units
	 * @param numberPattern
	 * @param singulars
	 * @param plurals
	 * @param posKnowledgeBase
	 * @param lyAdverbPattern
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param p4
	 * @param p5
	 * @param p6
	 * @param p7
	 * @param p75
	 * @param p8
	 * @param terminologyLearner
	 * @param viewPattern
	 * @param countPattern
	 * @param positionPattern
	 * @param romanRangePattern
	 * @param romanPattern
	 * @param romanNumbers
	 * @param stopWords
	 * @param prepositionWords
	 * @param modifierList
	 * @param parentTagProvider
	 * @param characterKnowledgeBase
	 * @param organStateKnowledgeBase
	 * @param inflector
	 * @param compoundPPptn TODO
	 */
	@Inject
	public Normalizer(IGlossary glossary, @Named("Units") String units, @Named("NumberPattern")String numberPattern,
			@Named("Singulars")HashMap<String, String> singulars, @Named("Plurals")HashMap<String, String> plurals, 
			IPOSKnowledgeBase posKnowledgeBase, @Named("LyAdverbpattern") String lyAdverbPattern,
			@Named("P1")String p1, @Named("P2")String p2, @Named("P3")String p3, @Named("P4")String p4, @Named("P5")String p5, 
			@Named("P6")String p6, @Named("P7")String p7, @Named("P75")String p75, @Named("P8")String p8, 
			ITerminologyLearner terminologyLearner, 
			@Named("ViewPattern") String viewPattern,
			@Named("CountPattern") String countPattern,
			@Named("PositionPattern") String positionPattern,
			@Named("RomanRangePattern") String romanRangePattern,
			@Named("RomanPattern") String romanPattern,
			@Named("RomanNumbers") String[] romanNumbers, 
			@Named("StopWords") Set<String> stopWords, 
			@Named("PrepositionWords") String prepositionWords,
			@Named("ModifierList") String modifierList, 
			@Named("ParentTagProvider")ParentTagProvider parentTagProvider,
			ICharacterKnowledgeBase characterKnowledgeBase, 
			IOrganStateKnowledgeBase organStateKnowledgeBase, 
			IInflector inflector, 
			@Named("CompoundPrepWords")String compoundPPptn) {
		this.units = units;
		this.numberPattern = numberPattern;
		this.glossary = glossary;
		this.singulars = singulars;
		this.plurals = plurals;
		this.posKnowledgeBase = posKnowledgeBase;
		this.p1 = Pattern.compile(p1);
		this.p1 = Pattern.compile(p2);
		this.p1 = Pattern.compile(p3);
		this.p1 = Pattern.compile(p4);
		this.p1 = Pattern.compile(p5);
		this.p1 = Pattern.compile(p6);
		this.p1 = Pattern.compile(p7);
		this.p1 = Pattern.compile(p75);
		this.p1 = Pattern.compile(p8);
		this.lyAdverbPattern = Pattern.compile(lyAdverbPattern);
		this.numberPattern = numberPattern;
		this.terminologyLearner = terminologyLearner;
		this.viewPattern = Pattern.compile(viewPattern);
		this.countPattern = Pattern.compile(countPattern);
		this.positionPattern = Pattern.compile(positionPattern);
		this.romanRangePattern = Pattern.compile(romanRangePattern);
		this.romanPattern = Pattern.compile(romanPattern);
		this.romanNumbers = romanNumbers;
		this.stopWords = stopWords;
		this.prepositionWords = prepositionWords;
		this.modifierList = Pattern.compile(modifierList);
		this.characterKnowledgeBase = characterKnowledgeBase;
		this.organStateKnowledgeBase = organStateKnowledgeBase;
		this.inflector = inflector;
		this.parentTagProvider = parentTagProvider;
		this.compoundPPptn = Pattern.compile("(.*?)\\b("+compoundPPptn+")\\b(.*)");	
		
	}
	
	public void init(){
		adjnounsent = terminologyLearner.getAdjNounSent();
		List<String> adjnouns = terminologyLearner.getAdjNouns();
		if(adjnouns!=null && adjnounsent!=null){
			Collections.sort(adjnouns); 
			adjnounslist = "";
			for(int i = adjnouns.size()-1; i>=0; i--) {
				String adjnoun = adjnouns.get(i);
				if(adjnoun.contains("or") || adjnoun.contains("and")) {
					String[] parts = adjnoun.split("or|and");
					for(String part : parts) 
						adjnounslist += part.trim()+"|";			
				} else 
					adjnounslist += adjnoun+"|";			
			}
			adjnounslist = adjnounslist.trim().length()==0? null : adjnounslist.replaceFirst("(\\|+$|^\\|+)", "").replaceAll("\\|+", "|");
		}
		replacements = 
				terminologyLearner.getAdjectiveReplacementsForNouns();
		
		for(String mp: this.modifierphrases){
			this.modifiertokens.add(mp.replaceAll("\\s+", "-"));
		}
		
		cln = CharacterListNormalizer.getInstance(characterKnowledgeBase, organStateKnowledgeBase);
	}
	
	public Set<String> getModifierTokens(){
		return this.modifiertokens;
	}
	@Override
	public String normalize(String str, String tag, String modifier, String source) {	
		str = dataSetSpecificNormalization(str);
		str = str.replaceFirst("^—\\s*", ""); //remove the leading "—" in a sentence
		str = str.replaceAll("_", "-");//??

		//sent = sent.replace("taxonname_", ""); //clean up the mark from the Transformer step
		str = str.replaceAll("\\bshades of\\b", "shades_of");
		str = str.replaceAll("\\bat least\\b", "at_least");
		str = str.replaceAll("[ _-]+\\s*shaped", "-shaped");
		str = str.replaceAll("(?<=\\s)µ\\s*m\\b", "um");
		str = str.replaceAll("\\bdiam\\s*\\.(?=\\s?[,a-z])", "diam");
		str = str.replaceAll("more or less", "moreorless");
		str = str.replaceAll("&#176;", "°");
		str = str.replaceAll("\\b(ca|c)\\s*\\.?\\s*(?=\\d)", "");
		str = str.replaceAll("(?<=\\d)(?=("+units+")\\b)", " "); //23mm => 23 mm
		str = str.replaceAll("\\bten\\b", "10");
		str = str.replaceAll("\\bca\\s*\\.\\s*", ""); //remove ca.
		
		//str = stringColors(str);
		str = connectColors(str);
        //deal with numbers

		str = ratio2number(str);
		str = toNumber(str);

		str = formatNumericalRange(str, source);
		//text = text.replaceAll("\\bca\\s*\\.", "ca");
		str = stringCompoundPP(str);		
		
		String backupStr = str;
		str = normalizeInner(str, tag, source);
		if(str.equals(backupStr))
			str = normalizeInnerNew(str, tag, source);
		//if(!modifier.trim().isEmpty())
		//	str = addModifier(str, modifier, tag);
		
		
		
		//===========================================================================
		boolean containsArea = false;
		String strcp = str;
		str = normalizeSpacesRoundNumbers(str);
		
		/*str = str.replaceAll("\\b(?<=\\d+) \\. (?=\\d+)\\b", "."); //2 . 5 =>2.5
		str = str.replaceAll("(?<=\\d)\\s+/\\s+(?=\\d)", "/"); // 1 / 2 => 1/2
		str = str.replaceAll("(?<=\\d)\\s+[�-�]\\s+(?=\\d)", "-"); // 1 - 2 => 1-2*/
		/*if(str.indexOf(" -{")>=0){//1�2-{pinnately} or -{palmately} {lobed} => {1�2-pinnately-or-palmately} {lobed}
			str = str.replaceAll("\\s+or\\s+-\\{", "-or-").replaceAll("\\s+to\\s+-\\{", "-to-").replaceAll("\\s+-\\{", "-{");
		}*/
		//TODO: Hong [_-]?  _or_?
		if(str.matches(".*?-(or|to)\\b.*") || str.matches(".*?\\b(or|to)-.*") ){//1�2-{pinnately} or-{palmately} {lobed} => {1�2-pinnately-or-palmately} {lobed}
			str = str.replaceAll("-or\\s+", "-or-").replaceAll("\\s+or-", "-or-").replaceAll("-to\\s+", "-to-").replaceAll("\\s+to-", "-to-");
			//str = str.replaceAll("\\}?-or\\s+\\{?", "-or-").replaceAll("\\}?\\s+or-\\{?", "-or-").replaceAll("\\}?-to\\s+\\{?", "-to-").replaceAll("\\}?\\s+to-\\{?", "-to-").replaceAll("-or\\} \\{", "-or-").replaceAll("-to\\} \\{", "-to-");
		}
		//{often} 2-, 3-, or 5-{ribbed} ; =>{often} {2-,3-,or5-ribbed} ;  635.txt-16
		Matcher m = hyphenedtoorpattern.matcher(str); //TODO: _ribbed not in local learned terms set. why?
		while(m.matches()){
			String possibleCharacterState = m.group(5);
			boolean isCharacterState = this.organStateKnowledgeBase.isState(possibleCharacterState); //TODO should also check perm. gloss.
			if(isCharacterState) {
				str = m.group(1) + m.group(2).replaceAll("[,]", " ").replaceAll("\\s+", "-") + m.group(6);
				//str = m.group(1) + "{" + m.group(2).replaceAll("[,]", " ").replaceAll("\\s+", "-").replaceAll("\\{$", "")+ "}" + m.group(6);
				//str = m.group(1)+"{"+m.group(2).replaceAll("[, ]","").replaceAll("\\{$", "")+m.group(5);	
				m = hyphenedtoorpattern.matcher(str);
			} else 
				break;
		}
		str = str.replaceAll("-+", "-");
		str = str.replaceAll("(?<![\\d(\\[–—-]\\s?)[–—-]+\\s*(?="+numberpattern+"\\s+\\W?("+units+")\\W?)", " to "); //fna: tips>-2.5 {mm}
		//if(!scp.equals(str)){
		//	log(LogLevel.DEBUG, );
		//}

		//ArrayList<String> chunkedTokens = new ArrayList<String>(Arrays.asList(str.split("\\s+")));
    	str = normalizemodifier(str);//shallowly to deeply pinnatifid: this should be done before other normalization that involved composing new tokens using ~
		//position list does not apply to FNA.			
		//str = normalizePositionList(str); //TODO Hong 
		str = normalizeCountList(str);

		//lookupCharacters(str);//populate charactertokens
		ArrayList<String> characterTokensReversed = cln.lookupCharacters(str, false);//false: treating -ly as %
        if(characterTokensReversed.contains("color") || characterTokensReversed.contains("coloration")){
        	str = normalizeColorPatterns(str, characterTokensReversed);
        	//lookupCharacters(str);
        }
        //lookupCharacters(str, true); //treating -ly as -ly
        if(str.indexOf(" to ")>=0 ||str.indexOf(" or ")>=0){
        	//if(this.printCharacterList){
				//log(LogLevel.DEBUG, str);
			//}
        	//str = normalizeCharacterLists(str); //a set of states of the same character connected by ,/to/or => {color-blue-to-red}
        	str = cln.normalizeParentheses(str); 
        }

        if(str.matches(".*? as\\s+[\\w]+\\s+as .*")){
           str = normalizeAsAs(str);
        }
        

        
        /*if(str.matches(".*?(?<=[a-z])/(?=[a-z]).*")){ //tooth/lobe =>tooth-lobe TODO Hong?
        	str = str.replaceAll("(?<=[a-z])/(?=[a-z])", "-");
        }*/
        
        
        //10-20(-38) {cm}�6-10 {mm} 
        
		//try{
			String strcp2 = str;
			
			String strnum = null;
			/*
			//if(str.indexOf("}�")>0){//{cm}�
      		if(str.indexOf("�")>0){
				containsArea = true;
				String[] area = normalizeArea(str);
				str = area[0]; //with complete info
				strnum = area[1]; //contain only numbers
			}
			*/
			
	        //deal with (3) as bullet
			m = bulletpattern.matcher(str.trim());
			if(m.matches()){
				str = m.group(3);
			}
			if(str.indexOf("±")>=0){
				str = str.replaceAll("±(?!~[a-z])","moreorless").replaceAll("±(?!\\s+\\d)","moreorless");
			}
			/*to match {more} or {less}*/
			if(str.matches(".*?\\bmore\\s+or\\s+less\\b?.*")){
				str = str.replaceAll("more\\s+or\\s+less", "moreorless");
			}
			//if(str.matches(".*?\\bin\\s+[a-z_<>{} -]+\\s+[<{]?view[}>]?\\b.*")){//ants: "in full-face view"
			//if(str.matches(".*?\\bin\\s+[a-z_<>{} -]*\\s*[<{]?(view|profile)[}>]?\\b.*")){
			if(str.matches(".*\\b(in|at)\\b.*?\\b(view|profile|closure)\\b.*")){
				Matcher vm = viewptn.matcher(str);
				while(vm.matches()){
					str = vm.group(1)+" "+vm.group(2).replaceAll("\\s+", "-")+" "+vm.group(3); 
					vm = viewptn.matcher(str);
				}
			}
			
			if(str.indexOf("×")>0 || str.matches(".*?\\d\\s*\\b?("+units+")?\\b?\\s*x\\s*\\d.*")){
				containsArea = true;
				String[] area = normalizeArea(str);
				str = area[0]; //with complete info
				strnum = area[1]; //like str but with numerical expression normalized
			}

			for(String modifierphrase: modifierphrases){
				str = str.replaceAll(modifierphrase, modifierphrase.replace(" ", "_"));
			}
			//str = handleBrackets(str);

			//str = Utilities.handleBrackets(str);
			if(str.matches(".*?\\d.*")){
				str = normalizeVagueNumbers(str); //more than 5, less than 5
			}
			//stmt.execute("update "+this.tableprefix+"_markedsentence set rmarkedsent ='"+str+"' where source='"+src+"'");	
			
			/*if(containsArea){
				str = strnum;
			}*/
			
			//leave threeing out as multiple tokens can be given to sp and protect them from being split up 
			//str = threeingSentence(str);
			if(hasUnmatchedBrackets(str)){
				log(LogLevel.DEBUG, "unmatched: "+str);
			}
	            //if(strcp.compareTo(str)!=0){
        	//   log(LogLevel.DEBUG, "orig sent==>"+ strcp);
        	//   log(LogLevel.DEBUG, "rmarked==>"+ strcp2);
        	//   log(LogLevel.DEBUG, "threed-sent==>"+ str);
			//}
           //str = str.replaceAll("}>", "/NN").replaceAll(">}", "/NN").replaceAll(">", "/NN").replaceAll("}", "/JJ").replaceAll("[<{]", "");
		
		//str = str.replaceAll("\\{", "").replaceAll("\\}", "");//Hong TODO {}, remove }> from reg exps.
		
		/*if(!tag.equals("ditto"))
			this.parentTag = tag;
		this.previousSentenceParentTag = this.parentTag;*/
			
		return str;
	}
	
    /**
	 * more|greater than 5 => 5+
	 * more than or equal to 5 => 5+
	 * less|fewer than 5 => 0-5	
	 * at most 5 => 0 - 5
	 * @param str
	 * @return
	 */
	private String normalizeVagueNumbers(String str) {
		if(str.matches(".*? at most .*")){
			str = str.replaceAll("\\bat most\\s*", "0-" );
		}


		Matcher m = vaguenumberptn1.matcher(str);
		while(m.matches()){
			if(m.group(2).matches(".*?\\b(more|greater)\\b.*")){
				str = m.group(1)+" "+m.group(3)+"+"+m.group(4);
			}else if(m.group(2).matches(".*?\\b(less|fewer)\\b.*")){
				str = m.group(1)+" 0-"+m.group(3)+m.group(4);
			}else{
				return str; //not contain a vague number
			}
			m = vaguenumberptn1.matcher(str);
		}
		str = str.replaceAll("\\s+", " ");
		//4+ and 0-6 => 4-6
		str = str.replaceAll("(?<=\\d)\\+ (and|but) 0(?=-\\d)", "");
		//0-6 feet and 4+ =>4-6

		Matcher m2 = vaguenumberptn2.matcher(str);
		while(m2.matches()){
			str = m2.group(1)+" "+m2.group(4)+m2.group(2)+" "+m2.group(3)+" "+m2.group(5);
			m2 = vaguenumberptn2.matcher(str);
		}
		return str.replaceAll("\\s+", " ").trim();
	}



	/*
	 * Handles the compound prepositions
	 */
	 private String stringCompoundPP(String text) {
	        boolean did = false;
	        String result = "";
	        Matcher m = compoundPPptn.matcher(text);
	        while(m.matches()){
	            String linked = m.group(2).replaceAll("\\s+", "-");
	            result += m.group(1)+ linked;
	            text = m.group(3);
	            m = compoundPPptn.matcher(text);
	            did = true;
	        }
	        result += text;
	        if(did) log(LogLevel.DEBUG, "[compound pp]:"+result);
	        return result;
	    }
	

	/**
	 * from 5-6 to 10 => 5-10
	 * between 1.0 and 2.0 => 10-20
	 * from 1/3 to 1/2 
	 * 10 to 20
	 * 
	 * "reduced to 2" should not be included.
	 * @param text
	 * @return
	 */
	private String formatNumericalRange(String text, String src) {
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
			text = text.replaceAll("(?<=\\d\\s?("+units+")?) to (?=\\d)", " - ");// three to four???
			text = to_Range(text);				
			text = text.replaceAll(" (?<=0 - [\\d\\. ]{1,6} [a-z ]?)× (?=[\\d\\. ]{1,6} [a-z])", " × 0 - "); //deal with case 2
			text = text.replaceAll(" 0 - (?=[\\d\\.\\ ]{1,8} [-–])", " ");// 0 - 1 . 3  - 2 . 0 => 1 . 3 - 2 . 0
		}
		if(!copy.equals(text)){
			log(LogLevel.DEBUG, src+" [to range original] "+copy);
			log(LogLevel.DEBUG, src+" [to range now] "+text);
		}
		return text.replaceAll("\\s+", " ").trim();
	}
	
	/**
	 * deal with: to-range such as "to 3 cm", "to 24 × 5 mm", "to 2 . 7 × 1 . 7 – 2 mm", "3 – 20 ( – 25 )" 
	 * text = text.replaceAll(" (up )?to (?=[\\d\\. ]{1,6} )", " 0 - "); // <trees> to 3 cm => <trees> 0 - 3 cm: works for case 1,  3, (case 4 should not match)
	 * "verb-ed to" pattern is excluded
	 * @param text
	 * @return
	 */
	private String to_Range(String text) {
		//Pattern torangeptn = Pattern.compile("(.*?)\\b(\\S+)? to ([\\d\\. ]{1,6} )(.*)");// doesn't match "elongate , to 10 cm in diameter" because of '\b' not matching ','
		Pattern torangeptn = Pattern.compile("(.*?)\\b?(\\S+)? to ([\\d\\. ]{1,6} )(.*)");
		Matcher m = torangeptn.matcher(text);
		while(m.matches()){
			if(m.group(2).compareTo("up")==0 || ! this.posKnowledgeBase.isVerb(m.group(2))){
				text = m.group(1)+m.group(2).replaceFirst("\\bup\\b", "")+" 0 - "+m.group(3)+m.group(4);
			}else{
				text = m.group(1)+m.group(2)+" TO "+m.group(3)+m.group(4);
			}
			m = torangeptn.matcher(text);
		}
		return text.replaceAll("TO", "to");
	}
	
	private String normalizeInnerNew(String str, String tag, String source) {

		if(replacements!=null && replacements.containsKey(source)) {
			AdjectiveReplacementForNoun replacement = replacements.get(source);
			
			String newString = "";
			String remainder = str;
			while(!remainder.isEmpty()) {
				int j = remainder.indexOf(replacement.getAdjective());
				if(j == -1) {
					newString += remainder;
					remainder = "";
				} else {
					String[] wordTokens = remainder.split("\\b");
					String possibleNoun = wordTokens[0];
					String singular = this.inflector.getSingular(possibleNoun);
					String plural = this.inflector.getPlural(possibleNoun);
					if(!possibleNoun.equals(replacement.getNoun()) &&
						!singular.equals(replacement.getNoun()) &&
						!plural.equals(replacement.getNoun())) {
						newString += remainder.substring(0, j + replacement.getAdjective().length()) + " " + replacement.getNoun();
					} else {
						newString += remainder.substring(0, j + replacement.getAdjective().length());
					}					
					remainder = remainder.substring(j + replacement.getAdjective().length());
				}
			}
			
			str = newString;
			//str = str.replaceAll(replacement.getAdjective(), replacement.getNoun());
		}
		/*Map<String, String> tagAdjectiveMap = terminologyLearner.getAdjNounSent();
		List<String> adjectiveList = terminologyLearner.getAdjNouns();
		
		for(String adjective : adjectiveList) {
			if(str.contains(adjective)) {
				//check that no other adjective contains the adjective and is 
				//additionally contained in string, e.g. adjective=principal, 
				//otherAdjective=principal cauline, tag=leaf
				boolean foundOther = false;
				for(String otherAdjective : adjectiveList) {
					if(!otherAdjective.equals(adjective) && 
							otherAdjective.contains(adjective) &&
							str.contains(otherAdjective)) {
						foundOther = true;
						break;
					}
				}
				if(foundOther)
					continue;
				else {
					str = str.replaceAll(adjective, parentTagProvider.getParentTag(source));
				}
			}
		}*/
		return str;
	}

	/**
	 * @param sentence
	 * @return dataset specific normalization result
	 */
	protected abstract String dataSetSpecificNormalization(String sentence);


	/*private String addModifier(String str, String modifier, String tag) {
		String singularTag = inflector.getSingular(tag);
		String pluralTag = inflector.getPlural(tag);
		
		//log(LogLevel.DEBUG, "modifier " + modifier + " tag " + tag);
		Set<Integer> tagPositions = new HashSet<Integer>();
		
		int index = str.indexOf(singularTag);
		while (index >= 0) {
			tagPositions.add(index);
		    index = str.indexOf(singularTag, index + 1);
		}	
			
		index = str.indexOf(pluralTag);
		while (index >= 0) {
			tagPositions.add(index);
		    index = str.indexOf(singularTag, index + 1);
		}
		
		//int index = str.indexOf(tag);
		for(Integer position : tagPositions) {
		//if(!tagPositions.isEmpty()) {
		//while (index >= 0) {
			//log(LogLevel.DEBUG, "index " + index);
			//tagPositions.add(index);
				
			String prefixStr = str.substring(0, position).trim();
			String postfixStr = str.substring(position).trim();
			
			String[] prefixTokens = prefixStr.split("\\b");
			//int searchIndex = index + 1;
			//prefixStr.matches(".*\\b")
			if(!modifier.contains(prefixTokens[prefixTokens.length-1])) {
				str = prefixStr + " " + modifier + " " + postfixStr;
				//searchIndex = index + 1 + modifier.length();
			}
			
			//log(LogLevel.DEBUG, "search " + searchIndex);
		    //index = str.indexOf(tag, searchIndex);
		}
				
		//int i=0;
		//for(Integer position : tagPositions) {
		//	int correctPosition = position + (i * modifier.length());
		//	
		//	String prefixStr = str.substring(0, correctPosition);
		//	String postfixStr = str.substring(correctPosition);
		//	
		//	String[] prefixTokens = prefixStr.split("\\b");
		//	if(!modifier.contains(prefixTokens[prefixTokens.length-1])) {
		//		str = prefixStr + " " + modifier + " " + postfixStr;
		//		i++;
		//	}
		//}
	
		return str;
	}*/


	/**
	 * turn reddish purple to reddish-purple
	 * @param text
	 * @return text
	 */
	private String connectColors(String text) {
		String colors = colorsFromGloss();
		//String pt = "\\b(?<=" + colors + ")\\s+(?=" + colors + ")\\b";
		String pt = "\\b(<=" + colors + ")\\s+(=" + colors + ")\\b";
		Pattern p = Pattern.compile(pt);
		Matcher m = p.matcher(text);
		
		while(m.find()){
			String toReplace = m.group();
			String replacement = m.group().replaceAll("\\s+", "_c_");
			organStateKnowledgeBase.addState(replacement);
			characterKnowledgeBase.addCharacterStateToName(replacement, "coloration");
			text = text.replaceFirst(toReplace, replacement);
			m = p.matcher(text);
		}
		return text;
	}
	
	private String colorsFromGloss() {
		StringBuffer colorsString = new StringBuffer();
		Set<String> allColors = new HashSet<String>();
		
		Set<String> colorations = glossary.getWords("coloration");
		if(colorations!=null)
			allColors.addAll(colorations);
		Set<String> colors = glossary.getWords("color");
		if(colors!=null)
			allColors.addAll(colors);
		
		for(String color : allColors) {
			color = color.trim();
			//color = color.indexOf(" ") > 0? color.substring(color.lastIndexOf(" ") + 1) : color;
			String[] clrs = color.split("[ -]+");
			for(String clr: clrs)
				colorsString.append(clr + "|");
		}
		return colorsString.toString().replaceFirst("\\|$", "");
	}


	private String normalizeInner(String str, String tag, String source) {
				//if((adjnounsent.containsKey(tag) && str.matches(".*?\\b(?:"+adjnounslist+")[^ly ]*\\b.*")) || str.matches(".*? of \\b(?:"+adjnounslist+")[^ly ]*\\b.*")){	
		if(adjnounsent!=null && adjnounslist!=null && (adjnounsent.containsKey(tag) && str.matches(".*?\\b(?:"+adjnounslist+")\\b.*"))){
			str = fixInner(str, tag.replaceAll("\\W",""), adjnounslist, source);
			//need to put tag in after the modifier inner
		}
		return str;
	}

	/**
	 * 
	 * @param text: {oblanceolate} , 15-70×30-150+ cm , {flat} 
	 * @return two strings: one contains all text from text with rearranged spaces,
	 *  the other contains numbers as the place holder of the area expressions
	 */	
	private String[] normalizeArea(String text){
		String[] result = new String[2];
		String text2= text;
		Matcher m = areapattern.matcher(text);
		while(m.matches()){
			if(m.group(2).matches("[(\\[\\d].*")){
				text = m.group(1)+m.group(2).replaceAll("[ \\{\\}]", "")+ m.group(4);
				m = areapattern.matcher(text2); //match on text2 to keep the unit-free segment
				m.matches();
				text2 = m.group(1)+m.group(2).replaceAll("[cmd]?m", "").replaceAll("[ \\{\\}]", "")+ m.group(4);
				m = areapattern.matcher(text);
			}else {//{pistillate} 9-47 ( -55 in <fruit> ) ×5.5-19 mm , {flowering} <branchlet> 0-4 mm ; m.group(2)= ) ×5.5-19 mm , {flowering} <branchlet> 0-4 mm ;
				String left = "";
				if(m.group(2).startsWith(")")) left = "(";
				if(m.group(2).startsWith("]")) left = "[";
				if(left.length()>0){
				    //m.group(1) = {pistillate} 9-47 ( -55 in <fruit> 
					//find the starting brackets in temp and remove the braketed content
					//if(temp.matches(".*?\\d$")){
					text = m.group(1).substring(0, m.group(1).lastIndexOf(left)).trim() +  m.group(2).replaceFirst("^[)\\]]", "").replaceAll("[ \\{\\}]", "") + m.group(4);
					m = areapattern.matcher(text2);
					m.matches();
					text2 = m.group(1).substring(0, m.group(1).lastIndexOf(left)).trim() +  m.group(2).replaceFirst("^[)\\]]", "").replaceAll("[cmd]?m", "").replaceAll("[ \\{\\}]", "") + m.group(4);
					m = areapattern.matcher(text);
					//}
				}
			}
		}
		result[0] = text.replaceAll("x(?=\\.?\\d)", "×");//{oblanceolate} , 15-70×30-150+cm , {flat}  
		result[1] = text2.replaceAll("x(?=\\.?\\d)", "×");//{oblanceolate} , 15-70×30-150+ , {flat} 
		return result;
}

	/**
	 * 
	 * @param text
	 * @return two strings: one contains all text from text with rearranged
	 *         spaces, the other contains numbers as the place holder of the
	 *         area expressions
	 */
	/*private String[] normalizeArea(String text) {
		String[] result = new String[2];
		String text2 = text;
		Matcher m = areapattern.matcher(text);
		while (m.matches()) {
			text = m.group(1) + m.group(2).replaceAll("[ \\{\\}]", "")
					+ m.group(4);
			m = areapattern.matcher(text2);
			m.matches();
			text2 = m.group(1)
					+ m.group(2).replaceAll("[cmd]?m", "")
							.replaceAll("[ \\{\\}]", "") + m.group(4);
			m = areapattern.matcher(text);
		}
		result[0] = text;
		result[1] = text2;
		return result;
	}*/
		
	/**
	 * make "suffused with dark blue and purple or green" one token
	 * ch-ptn"color % color color % color @ color"
	 * @param chunkedTokens 
	 * @param characterTokensReversed 
	 * 
	 * @return color-pattern-normalized String
	 */
	private String normalizeColorPatterns(String str, ArrayList<String> characterTokensReversed) {
		String list = "";
		String result = "";
		String header = "ttt";
		
		ArrayList<String> chunkedTokens = new ArrayList<String>(Arrays.asList(str.split("\\s+")));
		
		for (int i = characterTokensReversed.size() - 1; i >= 0; i--) {
			list += characterTokensReversed.get(i) + " ";
		}
		list = list.trim() + " "; // need to have a trailing space
		// Pattern p =
		// Pattern.compile("(.*?)((color|coloration)\\s+%\\s+(?:(?:color|coloration|@|%) )+)(.*)");
		Matcher m = colorpattern.matcher(list);
		int base = 0;
		while (m.matches()) {
			int start = (m.group(1).trim() + " a").trim().split("\\s+").length
					+ base - 1;
			int end = start
					+ (m.group(2).trim() + " b").trim().split("\\s+").length
					- 1;
			String ch = m.group(3) + header;
			list = m.group(4);
			m = colorpattern.matcher(list);
			// form result string, adjust chunkedtokens
			for (int i = base; i < start; i++) {
				result += chunkedTokens.get(i) + " ";
			}
			if (end > start) { // if it is a list
				String t = "{" + ch + "~list~";
				for (int i = start; i < end; i++) {
					t += chunkedTokens.get(i).trim()
							.replaceAll("[{}]", "")
							.replaceAll("[,;\\.]", "punct")
							+ "~";
					chunkedTokens.set(i, "");
				}
				t = t.replaceFirst("~$", "}");
				t = distributePrep(t) + " ";
				chunkedTokens.set(end - 1, t.trim());// "suffused with ..."
															// will not form a
															// list with other
															// previously
															// mentioned colors,
															// but may with
															// following colors,
															// so put this list
															// close to the next
															// token.
				result += t;
			}
			// prepare for the next step
			base = end;
		}
		// dealing with the last segment of the list or the entire list if no
		// match
		for (int i = base; i < (list.trim() + " b").trim().split("\\s+").length
				+ base - 1; i++) {
			// for(int i = base+1;
			// i<(list.trim()+" b").trim().split("\\s+").length+base; i++){
			result += chunkedTokens.get(i) + " ";
		}

		return result;
	}
	
	
	/**
	 * 
	 * @param t
	 *            : {color~list~suffused~with~red~or~purple}
	 * @return {color~list~suffused~with~red~or~purple}
	 */
	private String distributePrep(String t) {
		Matcher m = distributePrepPattern.matcher(t);
		if (m.matches()) {
			t = m.group(1) + m.group(2) + m.group(3) + m.group(2) + m.group(4);
		}
		return t;
	}
		
	/**
	 * replace "few or/to more" with "count~list~few~or/to~more" in the string update
	 * this.chunkedTokens
	 * 
	 * @param str
	 * @param chunkedTokens 
	 */
	private String normalizeCountList(String str) {//Hong TODO: chunkedTokens
		Matcher m = this.countptn.matcher(str);
		while (m.find()) {
			int start = m.start(1);
			int end = m.end(1);
			String count = m.group(1).trim();
			String rcount = "count~list~"
					+ count.replaceAll(" ", "~");
			str = str.substring(0, start).trim() +" "+rcount+str.substring(end).trim();
			
			/*
			// synchronise this.chunkedtokens
			// split by single space to get an accurate count to elements that
			// would be in chunkedtokens
			int index = (str.substring(0, start).trim() + " a").trim().split(
					"\\s").length - 1; // number of tokens before the count
										// pattern
			chunkedTokens.set(index, rcount);
			int num = count.split("\\s+").length;
			for (int i = index + 1; i < index + num; i++) {
				chunkedTokens.set(i, "");
			}
			// resemble the str from chunkedtokens, counting all empty elements,
			// so the str and chunkedtokens are in synch.
			str = "";
			for (String t : chunkedTokens) {
				str += t + " ";
			}*/
			m = this.countptn.matcher(str);
		}
		return str.replaceAll("\\s+", " ").trim();
	}
		
		
	/**
	 * shallowly to deeply pinnatifid => //shallowly~to~deeply pinnatifid
	 * @param str
	 * @param chunkedTokens 
	 * @return modifier-normalized String
	 */
	private String normalizemodifier(String str) { //Hong TODO: why still maintain chunkedTokens?
		String result = "";
		int base = 0;
		Matcher m = modifierlist.matcher(str.trim());
		while (m.matches()) {
			result += m.group(1);
			int start = (m.group(1).trim() + " a").trim().split("\\s+").length
					+ base - 1;
			String l = m.group(2);
			int end = start + (l.trim() + " b").trim().split("\\s+").length - 1;
			str = m.group(3);
			m = modifierlist.matcher(str);
			String newtoken = l.replaceAll("\\s+", "~");
			result += newtoken;
			base = end;
		}
		result += str;
		return result;
	}
	
	/*challenging cases: 
	 * <petiole> 15 - 30 ( - 53 ) cm {long} ( 20 - 30 ( - 50 ) % of total <leaf> ) , <petiole> {glabrous} , {spinescent} for 20 - 35 % of {length} .*/
	private String normalizeSpacesRoundNumbers(String sent) {
		sent = sent.replaceAll("-+", "-");// 2--4 => 2-4
		sent = sent.replaceAll("(- )+", "- ");// 2 - - 4 => 2 - 4
		if(sent.contains("×")) sent = sent.replaceAll("(?<="+units+")\\s*\\.\\s*(?=×)", " "); //4 cm.x 6cm => 4 cm x 6cm
		sent = sent.replaceAll("(?<=\\d)\\s*/\\s*(?=\\d)", "/");
		sent = sent.replaceAll("(?<=\\d)\\s+(?=\\d)", "-"); //bhl: two numbers connected by a space
		sent = sent.replaceAll("\\btwice\\b", "2 times");
		sent = sent.replaceAll("\\bthrice\\b", "3 times");
		sent = sent.replaceAll("2\\s*n\\s*=", "2n=");
		sent = sent.replaceAll("2\\s*x\\s*=", "2x=");
		sent = sent.replaceAll("n\\s*=", "n=");
		sent = sent.replaceAll("x\\s*=", "x=");
		sent = sent.replaceAll("q\\s*=", "q=");

		//sent = sent.replaceAll("[–—-]", "-").replaceAll(",", " , ").replaceAll(";", " ; ").replaceAll(":", " : ").replaceAll("\\.", " . ").replaceAll("\\[", " [ ").replaceAll("\\]", " ] ").replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").replaceAll("\\s+", " ").trim();
		sent = sent.replaceAll("[~–—-]", "-").replaceAll("°", " ° ").replaceAll(",", " , ").replaceAll(";", " ; ").replaceAll(":", " : ").replaceAll("\\.", " . ").replaceAll("\\s+", " ").trim();
		sent = sent.replaceAll("(?<=\\d) (?=\\?)", ""); //deals especially x=[9 ? , 13] 12, 19 cases
		sent = sent.replaceAll("(?<=\\?) (?=,)", "");
		if(sent.matches(".*?[nxq]=.*")){
			sent = sent.replaceAll("(?<=[\\d?])\\s*,\\s*(?=\\d)", ","); //remove spaces around , for chromosome only so numericalHandler.numericalPattern can "3" them into one 3. Other "," connecting two numbers needs spaces to avoid being "3"-ed (fruits 10, 3 of them large) 
		}
		sent = sent.replaceAll("\\b(?<=\\d+) \\. (?=\\d+)\\b", ".");//2 . 5 => 2.5
		sent = sent.replaceAll("(?<=\\d)\\.(?=\\d[nx]=)", " . "); //pappi 0.2n=12
		
		
		//sent = sent.replaceAll("(?<=\\d)\\s+/\\s+(?=\\d)", "/"); // 1 / 2 => 1/2
		//sent = sent.replaceAll("(?<=[\\d()\\[\\]])\\s+[–—-]\\s+(?=[\\d()\\[\\]])", "-"); // 1 - 2 => 1-2
		//sent = sent.replaceAll("(?<=[\\d])\\s+[–—-]\\s+(?=[\\d])", "-"); // 1 - 2 => 1-2
		
		//4-25 [ -60 ] => 4-25[-60]: this works only because "(text)" have already been removed from sentence in perl program
		sent = sent.replaceAll("\\(\\s+(?=[\\d\\+\\-%])", "("). //"( 4" => "(4"
		replaceAll("(?<=[\\d\\+\\-%])\\s+\\((?!\\s?[{<a-zA-Z])", "("). //" 4 (" => "4("
		replaceAll("(?<![a-zA-Z}>]\\s?)\\)\\s+(?=[\\d\\+\\-%])", ")"). //") 4" => ")4"
		replaceAll("(?<=[\\d\\+\\-%])\\s+\\)", ")"). //"4 )" => "4)"
		replaceAll("\\((?=\\d+-\\{)", "( "); //except for ( 4-{angled} )
		
		sent = sent.replaceAll("\\[\\s+(?=[\\d\\+\\-%])", "["). //"[ 4" => "[4", not [ -subpalmately ]
		replaceAll("(?<=[\\d\\+\\-%])\\s+\\[(?!\\s?[{<a-zA-Z])", "["). //" 4 [" => "4["
		replaceAll("(?<![a-zA-Z}>]\\s?)\\]\\s+(?=[\\d\\+\\-%])", "]"). //"] 4" => "]4"
		replaceAll("(?<=[\\d\\+\\-%])\\s+\\]", "]"). //"4 ]" => "4]"
		replaceAll("\\[(?=\\d+-\\{)", "[ "); //except for [ 4-{angled} ]
		
		/*Pattern p = Pattern.compile("(.*?)(\\d*)\\s+\\[\\s+([ –—+\\d\\.,?×/-]+)\\s+\\]\\s+(\\d*)(.*)");  //4-25 [ -60 ] => 4-25[-60]. ? is for chromosome count
		Matcher m = p.matcher(sent);
		while(m.matches()){
			sent = m.group(1)+ (m.group(2).length()>0? m.group(2):" ")+"["+m.group(3).replaceAll("\\s*[–—-]\\s*", "-")+"]"+(m.group(4).length()>0? m.group(4):" ")+m.group(5);
			m = p.matcher(sent);
		}
		////keep the space after the first (, so ( 3-15 mm) will not become 3-15mm ) in POSTagger.
		p = Pattern.compile("(.*?)(\\d*)\\s+\\(\\s+([ –—+\\d\\.,?×/-]+)\\s+\\)\\s+(\\d*)(.*)");  //4-25 ( -60 ) => 4-25(-60)
		//p = Pattern.compile("(.*?)(\\d*)\\s*\\(\\s*([ –—+\\d\\.,?×/-]+)\\s*\\)\\s*(\\d*)(.*)");  //4-25 ( -60 ) => 4-25(-60)
		m = p.matcher(sent);
		while(m.matches()){
			sent = m.group(1)+ (m.group(2).length()>0? m.group(2):" ")+"("+m.group(3).replaceAll("\\s*[–—-]\\s*", "-")+")"+(m.group(4).length()>0? m.group(4):" ")+m.group(5);
			m = p.matcher(sent);
		}*/
		
		sent = sent.replaceAll("\\s+/\\s+", "/"); //and/or 1/2
		sent = sent.replaceAll("\\s+×\\s+", "×");
		sent = sent.replaceAll("\\s*\\+\\s*", "+"); // 1 + => 1+
		sent = sent.replaceAll("(?<![\\d()\\]\\[×-])\\+", " +");
		sent = sent.replaceAll("\\+(?![\\d()\\]\\[×-])", "+ ");
		sent = sent.replaceAll("(?<=(\\d))\\s*\\?\\s*(?=[\\d)\\]])", "?"); // (0? )
		sent = sent.replaceAll("\\s*-\\s*", "-"); // 1 - 2 => 1-2, 4 - {merous} => 4-{merous}
		sent = sent.replaceAll("(?<=[\\d\\+-][\\)\\]])\\s+(?=[\\(\\[][\\d-])", "");//2(–3) [–6]  ??
		//%,°, and ×
		sent = sent.replaceAll("(?<![a-z])\\s+%", "%").replaceAll("(?<![a-z])\\s+°", "°").replaceAll("(?<![a-z ])\\s*×\\s*(?![ a-z])", "×");
		/*if(sent.indexOf(" -{")>=0){//1–2-{pinnately} or -{palmately} {lobed} => {1–2-pinnately-or-palmately} {lobed}
			sent = sent.replaceAll("\\s+or\\s+-\\{", "-or-").replaceAll("\\s+to\\s+-\\{", "-to-").replaceAll("\\s+-\\{", "-{");
		}*/
		//mohan code 11/9/2011 to replace (?) by nothing
		sent = sent.replaceAll("\\(\\s*\\?\\s*\\)","");
		//end mohan code
		sent = sent.replaceAll("(?<=[xnq]=)\\s+(?=[\\d\\[(])", "");//2n= 44 => 2n=44
	
		//make sure brackets that are not part of a numerical expression are separated from the expression by a space
		if(sent.contains("(") || sent.contains(")")) sent = normalizeBrackets(sent, '(');
		if(sent.contains("[") || sent.contains("]")) sent = normalizeBrackets(sent, '[');
		
		sent = sent.replaceAll("\\[(?=-[a-z])", "[ ");//[-subpalmately ] => [ -subpalmately ]
		sent = sent.replaceAll("\\((?=-[a-z])", "( ");//[-subpalmately ] => [ -subpalmately ]
		
		sent = sent.replaceAll("\\bav\\s*\\.", "av.");
		return sent;
	}


	//private String normalizeSpacesRoundNumbers(String sent) {
	//	sent = ratio2number(sent);//bhl
	//	sent = sent.replaceAll("(?<=\\d)\\s*/\\s*(?=\\d)", "/");
	/*	sent = sent.replaceAll("(?<=\\d)\\s+(?=\\d)", "-"); //bhl: two numbers connected by a space
		sent = sent.replaceAll("at least", "at-least");
		sent = sent.replaceAll("<?\\{?\\btwice\\b\\}?>?", "2 times");
		sent = sent.replaceAll("<?\\{?\\bthrice\\b\\}?>?", "3 times");
		sent = sent.replaceAll("2\\s*n\\s*=", "2n=");
		sent = sent.replaceAll("2\\s*x\\s*=", "2x=");
		sent = sent.replaceAll("n\\s*=", "n=");
		sent = sent.replaceAll("x\\s*=", "x=");

		//sent = sent.replaceAll("[��-]", "-").replaceAll(",", " , ").replaceAll(";", " ; ").replaceAll(":", " : ").replaceAll("\\.", " . ").replaceAll("\\[", " [ ").replaceAll("\\]", " ] ").replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").replaceAll("\\s+", " ").trim();
		sent = sent.replaceAll("[~–—-]", "-").replaceAll("°", " ° ").replaceAll(",", " , ").replaceAll(";", " ; ").replaceAll(":", " : ").replaceAll("\\.", " . ").replaceAll("\\s+", " ").trim();
		sent = sent.replaceAll("(?<=\\d) (?=\\?)", ""); //deals especially x=[9 ? , 13] 12, 19 cases
		sent = sent.replaceAll("(?<=\\?) (?=,)", "");
		if(sent.matches(".*?[nx]=.*")){
			sent = sent.replaceAll("(?<=[\\d?])\\s*,\\s*(?=\\d)", ","); //remove spaces around , for chromosome only so numericalHandler.numericalPattern can "3" them into one 3. Other "," connecting two numbers needs spaces to avoid being "3"-ed (fruits 10, 3 of them large) 
		}
		sent = sent.replaceAll("\\b(?<=\\d+) \\. (?=\\d+)\\b", ".");//2 . 5 => 2.5
		sent = sent.replaceAll("(?<=\\d)\\.(?=\\d[nx]=)", " . "); //pappi 0.2n=12
		
		//4-25 [ -60 ] => 4-25[-60]: this works only because "(text)" have already been removed from sentence in perl program
		sent = sent.replaceAll("\\(\\s+(?=[\\d\\+\\-%])", "("). //"( 4" => "(4"
		replaceAll("(?<=[\\d\\+\\-%])\\s+\\((?!\\s?[{<a-zA-Z])", "("). //" 4 (" => "4("
		replaceAll("(?<![a-zA-Z}>]\\s?)\\)\\s+(?=[\\d\\+\\-%])", ")"). //") 4" => ")4"
		replaceAll("(?<=[\\d\\+\\-%])\\s+\\)", ")"). //"4 )" => "4)"
		replaceAll("\\((?=\\d+-\\{)", "( "); //except for ( 4-{angled} )
		
		sent = sent.replaceAll("\\[\\s+(?=[\\d\\+\\-%])", "["). //"[ 4" => "[4", not [ -subpalmately ]
		replaceAll("(?<=[\\d\\+\\-%])\\s+\\[(?!\\s?[{<a-zA-Z])", "["). //" 4 [" => "4["
		replaceAll("(?<![a-zA-Z}>]\\s?)\\]\\s+(?=[\\d\\+\\-%])", "]"). //"] 4" => "]4"
		replaceAll("(?<=[\\d\\+\\-%])\\s+\\]", "]"). //"4 ]" => "4]"
		replaceAll("\\[(?=\\d+-\\{)", "[ "); //except for [ 4-{angled} ]
		
		sent = sent.replaceAll("\\s+/\\s+", "/"); //and/or 1/2
		sent = sent.replaceAll("\\s+×\\s+", "×");
		sent = sent.replaceAll("\\s*\\+\\s*", "+"); // 1 + => 1+
		sent = sent.replaceAll("(?<![\\d()\\]\\[×-])\\+", " +");
		sent = sent.replaceAll("\\+(?![\\d()\\]\\[×-])", "+ ");
		sent = sent.replaceAll("(?<=(\\d))\\s*\\?\\s*(?=[\\d)\\]])", "?"); // (0? )
		sent = sent.replaceAll("\\s*-\\s*", "-"); // 1 - 2 => 1-2, 4 - {merous} => 4-{merous}
		sent = sent.replaceAll("(?<=[\\d\\+-][\\)\\]])\\s+(?=[\\(\\[][\\d-])", "");//2(�3) [�6]  ??
		//%,�, and �
		sent = sent.replaceAll("(?<![a-z])\\s+%", "%").replaceAll("(?<![a-z])\\s+°", "°").replaceAll("(?<![a-z ])\\s*×\\s*(?![ a-z])", "×");

		//mohan code 11/9/2011 to replace (?) by nothing
		sent = sent.replaceAll("\\(\\s*\\?\\s*\\)","");
		//end mohan code
		//make sure brackets that are not part of a numerical expression are separated from the expression by a space
		if(sent.contains("(") || sent.contains(")")) sent = normalizeBrackets(sent, '(');
		if(sent.contains("[") || sent.contains("]")) sent = normalizeBrackets(sent, '[');
		
		sent = sent.replaceAll("\\[(?=-[a-z])", "[ ");//[-subpalmately ] => [ -subpalmately ]
		sent = sent.replaceAll("\\((?=-[a-z])", "( ");//[-subpalmately ] => [ -subpalmately ]
		return sent;
	}*/
	
	
	private String ratio2number(String sent){
		String small = "\\b(?:one|two|three|four|five|six|seven|eight|nine)\\b";
		String big = "\\b(?:half|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth)s?\\b";
		//ratio
		Pattern ptn = Pattern.compile("(.*?)("+small+"\\s*-?_?\\s*"+big+")(.*)");
		Matcher m = ptn.matcher(sent);
		while(m.matches()){
			String ratio = m.group(2);
			ratio = toRatio(ratio);
			sent = m.group(1)+ratio+m.group(3);
			m = ptn.matcher(sent);
		}
		//number
		small = "\\b(?:two|three|four|five|six|seven|eight|nine)\\b";
		ptn = Pattern.compile("(.*?)("+small+")(.*)");
		m = ptn.matcher(sent);
		while(m.matches()){
			String number = m.group(2);
			number = toNumber(number);
			sent = m.group(1)+number+m.group(3);
			m = ptn.matcher(sent);
		}
		sent = sent.replaceAll("(?<=\\d)\\s*to\\s*(?=\\d)", "-");
		return sent;
	}
	
	private String toNumber(String ratio){
		ratio = ratio.replaceAll("\\btwo\\b", "2");
		ratio = ratio.replaceAll("\\bthree\\b", "3");
		ratio = ratio.replaceAll("\\bfour\\b", "4");
		ratio = ratio.replaceAll("\\bfive\\b", "5");
		ratio = ratio.replaceAll("\\bsix\\b", "6");
		ratio = ratio.replaceAll("\\bseven\\b", "7");
		ratio = ratio.replaceAll("\\beight\\b", "8");
		ratio = ratio.replaceAll("\\bnine\\b", "9");
		return ratio;
	}
	
	private String toRatio(String ratio){
		ratio = ratio.replaceAll("\\bone\\b", "1/");
		ratio = ratio.replaceAll("\\btwo\\b", "2/");
		ratio = ratio.replaceAll("\\bthree\\b", "3/");
		ratio = ratio.replaceAll("\\bfour\\b", "4/");
		ratio = ratio.replaceAll("\\bfive\\b", "5/");
		ratio = ratio.replaceAll("\\bsix\\b", "6/");
		ratio = ratio.replaceAll("\\bseven\\b", "7/");
		ratio = ratio.replaceAll("\\beight\\b", "8/");
		ratio = ratio.replaceAll("\\bnine\\b", "9/");
		ratio = ratio.replaceAll("\\bhalf\\b", "2");
		ratio = ratio.replaceAll("\\bthirds?\\b", "3");
		ratio = ratio.replaceAll("\\bfourths?\\b", "4");
		ratio = ratio.replaceAll("\\bfifths?\\b", "5");
		ratio = ratio.replaceAll("\\bsixthths?\\b", "6");
		ratio = ratio.replaceAll("\\bsevenths?\\b", "7");
		ratio = ratio.replaceAll("\\beighths?\\b", "8");
		ratio = ratio.replaceAll("\\bninths?\\b", "9");
		ratio = ratio.replaceAll("\\btenths?\\b", "10");
		ratio = ratio.replaceAll("-", "").replaceAll("\\s", "");
		return ratio;
	}
	
	private String normalizeBrackets(String sent, char bracket) {
		char l ='('; char r=')';
		switch (bracket){
			case '(': l = '('; r=')'; break;
			case '[': l = '['; r=']'; break;
		}
		//boolean changed = false;
		String sentorig = sent;
		String fixed = "";
		Matcher matcher = numbergroup.matcher(sent);
		while(matcher.matches()){
			String num = matcher.group(2);
			if(hasUnmatchedBracket(num, ""+l, ""+r)>0){ //has an extra (
				int index = indexOfunmatched(l, num);
				if(index==0) {//move ( to group(2)
					fixed += matcher.group(1)+l+" "+num.replaceFirst("\\"+l, "");
					sent = matcher.group(3);
				}else if(index == num.length()-1){ //move ( to group(3)
					fixed += matcher.group(1)+num.replaceFirst("\\"+l+"$", "");
					sent = " "+l+matcher.group(3);
				}else{//the extra ( is in the middle of the num expression, then either find the matching ) in group 3 or split the num at the (
					if(matcher.group(3).startsWith(" "+r)){//find the matching ), attach it to group(2)
						fixed += matcher.group(1)+matcher.group(2)+r;
						sent = matcher.group(3).replaceFirst("\\s*\\"+r, "");
					}else{//move text from the extra ( on to group(3)
						fixed += matcher.group(1)+matcher.group(2).substring(0, index);
						sent =" "+l+" "+matcher.group(2).substring(index+1)+matcher.group(3);
					}					
				}
			}else if(hasUnmatchedBracket(num, ""+l, ""+r)<0){ //has an extra )
				int index = indexOfunmatched(r, num);
				if(index==0) {//move ) to group(1)
					fixed += matcher.group(1)+r+" "+num.replaceFirst("\\"+r, "");
					sent = matcher.group(3);
				}else if(index == num.length()-1){ //move ) to group(3)
					fixed += matcher.group(1)+num.replaceFirst("\\"+r+"$", "");
					sent = " "+r+matcher.group(3);
				}else{//the extra ) is in the middle of the num expression, then either find the matching ( in group(1) or split the num at the )
					if(matcher.group(1).endsWith(l+" ")){//find the matching (, attach it to group(2)
						fixed += matcher.group(1).replaceFirst("\\"+l+"\\s*$", "")+"("+matcher.group(2);
						sent = matcher.group(3);
					}else{//move text from the extra ) on to group(1)
						fixed += matcher.group(1)+matcher.group(2).substring(0, index-1)+" "+r+" "+ matcher.group(2).substring(index+1);
						sent = matcher.group(3);
					}					
				}
			}else{
				fixed += matcher.group(1)+matcher.group(2);
				sent = matcher.group(3);
			}
			matcher = numbergroup.matcher(sent);
		}
		fixed +=sent;
		//if(printNormalizeBrackets  && !fixed.equals(sentorig)){
		//	log(LogLevel.DEBUG, "orig : "+sentorig);
		//	log(LogLevel.DEBUG, "fixed: "+fixed);
		//}
		return fixed.replaceAll("\\s+", " ");
	}
	
    private int hasUnmatchedBracket(String text, String lbracket, String rbracket) {
    	if(lbracket.equals("[")) lbracket = "\\[";
    	if(lbracket.equals("]")) lbracket = "\\]";
    	
    	int left = text.replaceAll("[^"+lbracket+"]", "").length();
    	int right = text.replaceAll("[^"+rbracket+"]", "").length();
    	if(left > right) return 1;
    	if(left < right) return -1;
		return 0;
	}
	
	
    private boolean hasUnmatchedBrackets(String text) {
    	//String[] lbrackets = new String[]{"\\[", "(", "{"};
    	//String[] rbrackets = new String[]{"\\]", ")", "}"};
    	String[] lbrackets = new String[]{"\\[", "("};
    	String[] rbrackets = new String[]{"\\]", ")"};
    	for(int i = 0; i<lbrackets.length; i++){
    		int left1 = text.replaceAll("[^"+lbrackets[i]+"]", "").length();
    		int right1 = text.replaceAll("[^"+rbrackets[i]+"]", "").length();
    		if(left1!=right1) return true;
    	}
		return false;
	}
    
    
    /**
     * if bracket is left, then refresh the index of a new positive count
     * if bracket is right, return the first index with a negative count
     * @param bracket
     * @param str
     * @return index of unmatched bracket in str
     */
	private int indexOfunmatched(char bracket, String str) {
		int cnt = 0;
		char l = '('; char r=')';
		switch(bracket){
			case '(':  l = '('; r =')'; break;
			case '[': l = '['; r =']'; break;
			case ')':  l = '('; r =')'; break;
			case ']': l = '['; r =']'; break;
		}		
		
		if(bracket == r){
			for(int i = 0; i < str.length(); i++) {
			    if(str.charAt(i)== l){
			    	cnt++;
			    }else if(str.charAt(i) == r){
			    	cnt--; 
			    }			
			    if(cnt<0) return i; //first index with negative count
			}
		}

		if(bracket == l){
			int index = -1;
			for(int i = 0; i < str.length(); i++) {
			    if(str.charAt(i)== l){
			    	cnt++;
			    	index = i;
			    }else if(str.charAt(i) == r){
			    	cnt--; 
			    }			
			    if(cnt==0) index = -1; //first index with negative count
			}
			return index;
		}
		return -1;
	}
	
	private String threeingSentence(String str) {
		//hide the numbers in count list: {count~list~9~or~less~} <fin> <rays>
		ArrayList<String> lists = new ArrayList<String>();
		str = hideLists(str, lists);		
		//threeing
		str = str.replaceAll("(?<=\\d)-(?=\\{)", " - "); //this is need to keep "-" in 5-{merous} after 3ed (3-{merous} and not 3 {merous}) 
		//Pattern pattern3 = Pattern.compile("[\\d]+[\\-\\�]+[\\d]+");
		//Pattern pattern3 = Pattern.compile(NumericalHandler.numberpattern);
		//Pattern pattern4 = Pattern.compile("(?<!(ca[\\s]?|diam[\\s]?))([\\d]?[\\s]?\\.[\\s]?[\\d]+[\\s]?[\\�\\-]+[\\s]?[\\d]?[\\s]?\\.[\\s]?[\\d]+)|([\\d]+[\\s]?[\\�\\-]+[\\s]?[\\d]?[\\s]?\\.[\\s]?[\\d]+)|([\\d]/[\\d][\\s]?[\\�\\-][\\s]?[\\d]/[\\d])|(?<!(ca[\\s]?|diam[\\s]?))([\\d]?[\\s]?\\.[\\s]?[\\d]+)|([\\d]/[\\d])");
		//Pattern pattern5 = Pattern.compile("[\\d�\\+\\�\\-\\���:�/�\"��\\_�\\׵%\\*\\{\\}\\[\\]=]+");
		//Pattern pattern5 = Pattern.compile("[\\d\\+���/�\"���\\׵%\\*]+(?!~[a-z])");
		Pattern pattern5 = Pattern.compile("[\\d\\+°²½/¼\"“”´\\×µ%\\*]+(?![a-z])"); //single numbers, not including individual "-", would turn 3-branched to 3 branched 
		//Pattern pattern6 = Pattern.compile("([\\s]*0[\\s]*)+(?!~[a-z])"); //condense multiple 0s.
		Pattern pattern6 = Pattern.compile("(?<=\\s)[0\\s]+(?=\\s)");
		//Pattern pattern5 = Pattern.compile("((?<!(/|(\\.[\\s]?)))[\\d]+[\\-\\�]+[\\d]+(?!([\\�\\-]+/|([\\s]?\\.))))|((?<!(\\{|/))[\\d]+(?!(\\}|/)))");
		//[\\d�\\+\\�\\-\\��.�:�/�\"��\\_;x�\\�\\s,�%\\*\\{\\}\\[\\]=(<\\{)(\\}>)]+
		Pattern pattern7 = Pattern.compile("[(\\[]\\s*\\d+\\s*[)\\]]"); // deal with ( 2 ), (23) is dealt with by NumericalHandler.numberpattern
		
		Matcher	 matcher1 = numberpattern.matcher(str);
        str = matcher1.replaceAll("0");
		matcher1.reset();
         
         /*matcher1 = pattern4.matcher(str);
         str = matcher1.replaceAll("0");
         matcher1.reset();*/
         
         matcher1 = pattern5.matcher(str);//single numbers
         str = matcher1.replaceAll("0");
         matcher1.reset();
         
         /* should not remove space around 0, because: pollen 70-80% 3-porate should keep 2 separate numbers: 70-80% and 3-porate
		* 
         String scptemp = str;
         matcher1 = pattern6.matcher(str);//remove space around 0
         str = matcher1.replaceAll("0");
         if(!scptemp.equals(str)){
		   log(LogLevel.DEBUG, );
         }
         matcher1.reset();*/
         
         matcher1 = pattern7.matcher(str);//added for (2)
         str = matcher1.replaceAll("0");
         matcher1.reset();
         //further normalization
         
         
         //3 -{many} or 3- {many}=> {3-many}
         str = str.replaceAll("0\\s*-\\s*", "0-").replaceAll("0(?!~[a-z])", "3").replaceAll("3\\s*[–-]\\{", "{3-").replaceAll("±(?!~[a-z])","{moreorless}").replaceAll("±","moreorless"); //stanford parser gives different results on 0 and other numbers.
         
         //2-or-{3-lobed} => {2-or-3-lobed}
         str = str.replaceAll("(?<=-(to|or)-)\\{", "").replaceAll("[^\\{]\\b(?=3-(to|or)-3\\S+\\})", " {");
		
         //unhide count list
         str = unCountLists(str, lists);
         return str;
	}
	
	/**
	 * hide lists such as
	 * {upper} {pharyngeal} <tooth> <plates_4_and_5>
	 * count~list~2~to~4
	 * so the numbers will not be turned into 3.
	 * @param str
	 * @param countlists
	 * @return hidden-list-normalized String
	 */
	private String hideLists(String str,
			ArrayList<String> lists) {
		if(str.contains("count~list~") || str.matches(".*?<\\S+_\\d.*")){
			String newstr = "";
			String[] tokens = str.split("\\s+");
			int count = 0;
			for(String t: tokens){
				if(t.indexOf("count~list~")>=0 || t.matches("<\\S+_\\d.*")){
					newstr +="# ";
					lists.add(t);
					count++;
				}else{
					newstr +=t+" ";
				}
			}			
			return newstr.trim();
		}else{
			return str;
		}
	}
	
	private static String unCountLists(String str, ArrayList<String> lists) {
		if(str.contains("#")){
			String newstr = "";
			String[] tokens = str.split("\\s+");
			int count = 0;
			for(String t: tokens){
				if(t.contains("#")){
					newstr += lists.get(count)+" ";
					count++;
				}else{
					newstr +=t+" ";
				}
			}
			return newstr.trim();
		}else{
			return str;
		}
	}
	
	
	/*private ArrayList<String> lookupCharacters(String str, boolean markadv, ArrayList<String> chunkedTokens) {		
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
	 * lookback
	 * @param saved
	 * @param index
	 * @return looked up String
	 */
	/*private String lastSaved(ArrayList<String> saved, int index){
		int inbrackets = 0;
		for(int i = index-1; i >=0 && i<saved.size(); i--){
			String c = saved.get(i).trim();
			if(c.equals("(") || c.equals("[")) inbrackets++; //ignore characters in brackets
			else if(c.equals(")") || c.equals("]")) inbrackets--;
			else if(inbrackets ==0 && c.length()>0) return c;
		}
		return "";
	}*/
	
	/**
	 * lookahead
	 * @param saved
	 * @param index
	 * @return looked up String
	 */
	/*private String nextSaved(ArrayList<String> saved, int index){
		int inbrackets = 0;
		for(int i = index+1; i <saved.size(); i++){
			String c = saved.get(i).trim();
			if(c.equals("(") || c.equals("[")) inbrackets++; //ignore characters in brackets
			else if(c.equals(")") || c.equals("]")) inbrackets--;
			else if(inbrackets ==0 && c.length()>0) return c;			
		}
		return "";
	}*/
	
	
	
	/*private void save(ArrayList<String> saved, int index, String ch){
		while(saved.size()<=index){
			saved.add("");
		}
		saved.set(index, ch);
	}*/
	
	
	/**
	 * as wide as => as-wide-as/IN
	 * as wide as or/to wider than inner
	 * as wide as inner
	 * as wide as long
	 * @return as-as-normalized String
	 */	
	private String normalizeAsAs(String str) {
		String result = "";
		Matcher m = asaspattern.matcher(str);
		while(m.matches()){
			result+=m.group(1);
			result+=m.group(2).replaceAll("\\s+", "-");
			str = m.group(3);
			m = asaspattern.matcher(str);
		}
		result+=str;
		return result.trim();
	}
	
	
	/**
	 * deal with sentences with parentheses
	 * @param chunkedTokens 
	 * @return paraentheses-normalized String
	 */
	/*private String normalizeParentheses(String src, ArrayList<String> chunkedTokens){
		ArrayList<String> characterTokensReversed = lookupCharacters(src, true, chunkedTokens); //treating -ly as -ly
		
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
							String range ="{"+chara+"~list~"+chunkedTokens.get(orphanedto-1).replaceAll("[{}]", "")+"~to~"+nextchara;
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
	}*/
	
	
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

	/**
	 * when "to"[@] is the last token in bracketed phrase:
	 * e.g. (, {yellow-gray}, to, ), {coloration~list~brown~to~black}
	 * @param chunkedTokens 
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
	 * put a list of states of the same character connected by to/or in a chunk
	 * color, color, or color
	 * color or color to color
	 * 
	 * {color-blue-to-red}
	 * @param chunkedTokens 
	 * @return updated string
	 */
	//private String normalizeCharacterLists(String list){
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
	


	/*private String segByWord(String listcopy, int startindex) {
		String seg = "";
		if(startindex < 0) return seg;
		String[] tokens = listcopy.trim().split("\\s+");
		for(int i = startindex; i < tokens.length; i++){
			seg += tokens[i]+" ";
		}
		return seg.trim();
	}*/
	
	
	
	/**
	 * mark Inner as organ for sent such as "inner red".
	 * @param adjnouns
	 * @param taggedsent
	 * @return inner-fixed String
	 */
	private String fixInner(String taggedsent, String tag, String adjnounslist, String source) {
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
			
			String[] beforeTokens = before.split(" ");
			String[] afterTokens = after.split(" ");
			log(LogLevel.DEBUG, "before token " + beforeTokens[beforeTokens.length-1]);
			log(LogLevel.DEBUG, String.valueOf(organStateKnowledgeBase.isOrgan(beforeTokens[beforeTokens.length-1])));
			if(!organStateKnowledgeBase.isOrgan(beforeTokens[beforeTokens.length-1]) &&
					!organStateKnowledgeBase.isOrgan(afterTokens[0])) {
				boolean beforeContainsOrgan = false;
				String organ = "";
				for(String beforeToken : beforeTokens) {
					if(organStateKnowledgeBase.isOrgan(beforeToken)) {
						beforeContainsOrgan = true;
						organ = "";
					}
					if(beforeContainsOrgan)
						organ += beforeToken + " ";
				}
				if(beforeTokens[beforeTokens.length-1].equals("of") && beforeContainsOrgan) {
					//String organ = before.substring(before.lastIndexOf("<"));
					if(copysent.startsWith(organ)){
						tag = parentTagProvider.getParentTag(source);
						//tag = parentTag;//tag may be null, remove before return
					}
					organ = organ.replaceFirst("\\s*of\\s*$", "").replaceAll("\\W", "");
					if(inflector.getSingular(organ).compareTo(tag)==0 || 
						(organ.matches("(apex|apices)") && tag.compareTo("base")==0)){
						tag = parentTagProvider.getGrandParentTag(source);
					}
				}
					
				String copyinner = inner.trim();
				//inner = copyinner.replaceAll("[<{}>]", "").replaceAll("\\{and\\}", "and").replaceAll("\\{or\\}", "or");
				//inner = "<"+inner+">";
				//inner = "{"+inner+"} <"+tag+">";
				fixed +=before + " " + inner + " ";
				//taggedsent = matchcount==1 && !before.trim().endsWith("of")? " "+after : "#<"+tag+">#"+" "+after;
				if(after.matches("^\\d\\s*/\\s*\\d.*")){//proximal 1 / 2
					taggedsent = " "+after;
				}else if(inner.endsWith("er") && after.startsWith("than")){
					taggedsent = " "+after;
				}else if(before.trim().endsWith("of")){
					taggedsent = tag + " " + after;
				//}else if(matchcount==1 && copysent.startsWith(copyinner)){
				//	taggedsent = " "+after;
				}else{
					String[] fixedTokens = fixed.split(" ");
					boolean fixedContainsOrgan = false;
					String fixedOrgan = "";
					for(String fixedToken : fixedTokens) {
						if(this.organStateKnowledgeBase.isOrgan(fixedToken)) {
							fixedContainsOrgan = true;
							fixedOrgan = fixedToken;
						}
					}
					
					int start = fixedContainsOrgan ? fixed.lastIndexOf(fixedOrgan) : 0;
					String segment = fixed.substring(start).trim();
					//if(segment.indexOf(",")<0 && !segment.startsWith("and")){
					//	taggedsent = " "+after;
					//}else{
						taggedsent = tag + " " + after;
					//}
				}
				needfix = true;
				changed = true;
			}
			
//			if(!before.trim().endsWith(">") &&!after.trim().startsWith("<")){//mark inner as organ
//				if(before.trim().endsWith("of") && before.lastIndexOf("<")>=0) { //"apices of inner" may appear at the main structure is mentioned, in these cases, matchcount>1					
//					String organ = before.substring(before.lastIndexOf("<"));
//					if(copysent.startsWith(organ)){
//						tag = parentTag;//tag may be null, remove before return
//					}
//					organ = organ.replaceFirst("\\s*of\\s*$", "").replaceAll("\\W", "");
//					if(inflector.getSingular(organ).compareTo(tag)==0 || 
//						(organ.matches("(apex|apices)") && tag.compareTo("base")==0)){
//						tag = this.previousSentenceParentTag;
//					}
//				}
//				String copyinner = inner.trim();
//				inner = copyinner.replaceAll("[<{}>]", "").replaceAll("\\{and\\}", "and").replaceAll("\\{or\\}", "or");
//				//inner = "<"+inner+">";
//				//inner = "{"+inner+"} <"+tag+">";
//				fixed +=before + " " + inner + " ";
//				//taggedsent = matchcount==1 && !before.trim().endsWith("of")? " "+after : "#<"+tag+">#"+" "+after;
//				if(after.matches("^\\d\\s*/\\s*\\d.*")){//proximal 1 / 2
//					taggedsent = " "+after;
//				}else if(inner.endsWith("er") && after.startsWith("than")){
//					taggedsent = " "+after;
//				}else if(before.trim().endsWith("of")){
//					taggedsent = tag + " " + after;
//				}else if(matchcount==1 && copysent.startsWith(copyinner)){
//					taggedsent = " "+after;
//				}else{
//					int start = fixed.lastIndexOf(">")>=0? fixed.lastIndexOf(">") : 0;
//					String segment = fixed.substring(start).trim();
//					if(segment.indexOf(",")<0 && !segment.startsWith("and")){
//						taggedsent = " "+after;
//					}else{
//						taggedsent = tag + " " + after;
//					}
//				}
//				needfix = true;
//				changed = true;
//			}
			//fixed +=before+" ";
			//taggedsent = inner+" "+after;
			m = p.matcher(taggedsent);
			//fixed = before+" "+inner+" "+after; //{outer} {pistillate}
			//m = p.matcher(fixed);
		}
		fixed +=taggedsent;
		if(needfix){
			//log(LogLevel.DEBUG, "fixed "+fixedcount+":["+source+"] "+fixed);
			//fixedcount++;
		}
		if(fixed.trim().length()<1){
			fixed = taggedsent;
		}
		
		return fixed.trim().replaceAll("\\s+", " ");//.replaceAll("<null>", "");
	}
}
