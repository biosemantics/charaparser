package semanticMarkup.ling.pos;

/**
 * A POS poses a tag of the penn treebank of any level. Also, additional tags that are necessary for marking are added.
 * @author rodenhausen
 */
public enum POS {
	//clause level
	S, 		
	SBAR,
	SBARQ,
	SINV,
	SQ,
	
	//phrase level
	ADJP,
	ADVP,
	CONJP,
	FRAG,
	INTJ,
	LST,
	NAC,
	NP,
	NX,
	PP,
	PRN,
	PRT,
	QP,
	RRC,
	UCP,
	VP,
	WHADJP,
	WHAVP,
	WHNP,
	WHPP,
	X,
	
	//word level
	CC,		//	Coordinating conjunction
	CD,		//	Cardinal number
	DT,		//	Determiner
	EX,		//	Existential there
	FW,		//	Foreign word
	IN,		//	Preposition or subordinating conjunction
	JJ,		// 	Adjective
	JJR,	//	Adjective, comparative
	JJS,	//	Adjective, superlative
	LS,		//	List item marker
	MD,		//	Modal
	NN,		//	Noun, singular or mass
	NNS,	//	Noun, plural
	NNP,	//	Proper noun, singular
	NNPS,	//	Proper noun, plural
	PDT,	//	Predeterminer
	POS,	//	Possessive ending
	PRTP,	//	Personal pronoun
	PRP$, 	//	Possessive pronoun
	RB, 	//	Adverb
	RBR,	//	Adverb, comparative
	RBS,	// 	Adverb, superlative
	RP,		//	Particle
	SYM, 	//	Symbol
	TO,		//	to
	UH,		//	Interjection
	VB,		//	Verb, base form	
	VBD,	//	Verb, past tense
	VBG,	//	Verb, gerund or present participle
	VBN,	//	Verb, past participle
	VBP,	//	Verb, non-3rd person singular present
	VBZ,	//	Verb, 3rd person singular present
	WDT,	//	Wh-determiner
	WP,		//	Wh-pronoun
	WP$,	//	Possessive wh-pronoun
	WRB,		//	Wh-adverb
	
	//function tags
	ADV,
	NOM,
	DTV,
	LGS,
	PRD,
	PUT,
	SBJ,
	TPC,
	VOC,
	BNF,
	DIR,
	EXT,
	LOC,
	MNR,
	PRP,
	TMP,
	CLR,
	CLF,
	HLN,
	TTL,
	
	
	//misc
	MINUS_ADV,
	MINUS_BNF,
	MINUS_CLF,
	MINUS_CLR,
	MINUS_DIR,
	MINUS_DTV,
	MINUS_EXT,
	MINUS_HLN,
	MINUS_LGS,
	MINUS_LOC,
	MINUS_MNR,
	MINUS_NOM,
	MINUS_PRD,
	MINUS_PRP,
	PRP_MINUS_S,
	MINUS_PUT,
	MINUS_SBJ,
	MINUS_TMP,
	MINUS_TPC,
	MINUS_TTL,
	MINUS_VOC,
	WHADVP,
	WPS,
	WP_MINUS_S,
	
	NONE,	//e.g. punctuation
	
	//"own pos tags", indicate e.g. collapsed parts of an original parse tree
	COLLAPSED_THAT,
	COLLAPSED_WHERE,
	COLLAPSED_WHEN,
	COLLAPSED_NP,
	COLLAPSED_VB,
	COLLAPSED_PPIN,
	VERB,
	PREPOSITION,
	OBJECT,
	COLLAPSED_THAN, 
	COLLAPSED_TO,
	
	VP_CHECKED,
	PP_CHECKED,
	
	PUNCT
}
