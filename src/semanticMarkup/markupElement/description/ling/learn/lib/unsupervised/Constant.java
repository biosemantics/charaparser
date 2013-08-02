package semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

public class Constant {
	
	public static final String NUMBER = "zero|one|ones|first|two|second|three|third|thirds|four|fourth|fourths|quarter|five|fifth|fifths|six|sixth|sixths|seven|seventh|sevenths|eight|eighths|eighth|nine|ninths|ninth|tenths|tenth";

	// the following two patterns are used in mySQL rlike
	public static final String PREFIX = "ab|ad|bi|deca|de|dis|di|dodeca|endo|end|e|hemi|hetero|hexa|homo|infra|inter|ir|macro|mega|meso|micro|mid|mono|multi|ob|octo|over|penta|poly|postero|post|ptero|pseudo|quadri|quinque|semi|sub|sur|syn|tetra|tri|uni|un|xero|[a-z0-9]+_";

	// 3_nerved, )_nerved, dealt with in subroutine
	public static final String SUFFIX = "er|est|fid|form|ish|less|like|ly|merous|most|shaped";

	// words in this list can not be treated as boundaries "to|a|b" etc.
	public static final String FORBIDDEN = "to|and|or|nor";

	public static final String PRONOUN = "all|each|every|some|few|individual|both|other";
	public static final String CHARACTER = "lengths|length|lengthed|width|widths|widthed|heights|height|character|characters|distribution|distributions|outline|outlines|profile|profiles|feature|features|form|forms|mechanism|mechanisms|nature|natures|shape|shapes|shaped|size|sizes|sized";
	public static final String PREPOSITION = "above|across|after|along|around|as|at|before|below|beneath|between|beyond|by|during|for|from|in|into|near|of|off|on|onto|out|outside|over|than|through|throughout|toward|towards|up|upward|with|without";
	public static final String TAGS = "";
	public static final String PLENDINGS = "[^aeiou]ies|i|ia|(x|ch|sh)es|ves|ices|ae|s";
	public static final String CLUSTERSTRING = "group|groups|clusters|cluster|arrays|array|series|fascicles|fascicle|pairs|pair|rows|number|numbers|\\d+";
	public static final String SUBSTRUCTURESTRING = "part|parts|area|areas|portion|portions";
	
	public static final String PROPERNOUN = "propernouns"; // EOL
	
	public static final String IGNOREPTN = "(IGNOREPTN)"; // disabled

	
	public static final String STOP = "state|page|fig|"
			+ "a|about|above|across|after|along|also|although|amp|an|and|are|as|at|be|because|become|becomes|becoming|been|before|behind|being|beneath|between|beyond|but|by|ca|can|could|did|do|does|doing|done|during|for|from|had|has|have|hence|here|how|if|in|into|inside|inward|is|it|its|least|may|might|more|most|near|no|not|of|off|on|onto|or|out|outside|outward|over|should|so|than|that|the|then|there|these|this|those|throughout|to|toward|towards|under|up|upward|via|was|were|what|when|where|whereas|which|why|with|within|without|would";
	

	public static final String NENDINGS = "\\w\\w(?:ist|sure)\\b";
	public static final String VENDINGS = "(ing)\\b";
	public static final String SENDINGS = "(on|is|ex|ix|um|us)\\b";
	public static final String PENDINGS = "(a|ia|es|ices|i|ae)\\b";
	
	// abbreviations may appear in original sentence 
	//public static final String PEOPLE_ABBR = "jr|mr|mrs|ms|dr|prof|sr|sens?|reps?|gov|attys?|supt|det|rev";
	public static final String PEOPLE_ABBR = "jr|mr|mrs|ms|dr|prof|sr|sens|reps|gov|attys|supt|det|rev";
	public static final String ARMY_ABBR= "col|gen|lt|cmdr|adm|capt|sgt|cpl|maj";
	public static final String INSTITUTES_ABBR = "dept|univ|assn|bros";
	public static final String COMPANIES_ABBR = "inc|ltd|co|corp";
	// the question mark mean the prior character can be exist or not
	public static final String PLACES_ABBR = "arc|al|ave|blv?d|cl|ct|cres|dr|expy?|dist|mt|ft|fw?y|hwa?y|la|pde?|pl|plz|rd|st|tce|Ala|Ariz|Ark|Cal|Calif|Col|Colo|Conn|Del|Fed|Fla|Ga|Ida|Id|Ill|Ind|Ia|Kan|Kans|Ken|Ky|La|Me|Md|Is|Mass|Mich|Minn|Miss|Mo|Mont|Neb|Nebr|Nev|Mex|Okla|Ok|Ore|Penna|Penn|Pa|Dak|Tenn|Tex|Ut|Vt|Va|Wash|Wis|Wisc|Wy|Wyo|USAFA|Alta|Man|Ont|Qu¨¦|Sask|Yuk";
	//public static final String PLACES_ABBR = "arc|al|ave|blvd|cl|ct|cres|dr|expy|dist|mt|ft|fwy|hway|la|pde|pl|plz|rd|st|tce|Ala|Ariz|Ark|Cal|Calif|Col|Colo|Conn|Del|Fed|Fla|Ga|Ida|Id|Ill|Ind|Ia|Kan|Kans|Ken|Ky|La|Me|Md|Is|Mass|Mich|Minn|Miss|Mo|Mont|Neb|Nebr|Nev|Mex|Okla|Ok|Ore|Penna|Penn|Pa|Dak|Tenn|Tex|Ut|Vt|Va|Wash|Wis|Wisc|Wy|Wyo|USAFA|Alta|Man|Ont|Qu¨¦|Sask|Yuk";
	public static final String MONTHS_ABBR = "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|sept";
	public static final String MISC_ABBR = "vs|etc|no|esp";
	public static final String BOT1_ABBR = "diam|sq|Rottb";
	public static final String BOT2_ABBR = "ca|fl|Fl|Fr|fr|var";
	public static final String LATIN_ABBR = "et al";
 

	public Constant() {
		// TODO Auto-generated constructor stub
	}

}
