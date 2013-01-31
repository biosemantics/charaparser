package semanticMarkup.ling.chunk;

public enum ChunkType {
	/*ORGAN, 
	NON_SUBJECT_ORGAN, 
	NP_LIST, 
	SIMPLE_CHARACTER_STATE, 
	STATE_LIST, 
	THAN, 
	THAN_CHARACTER,
	NUMERICALS,
	AREA,
	RATIO,
	COMPARATIVE_VALUE,
	VALUE,
	VP,
	PREP,
	CHPP,
	OR,
	SBAR,
	BRACKETED,
	EOS,
	COMMA,
	PP_LIST,
	PP,
	TO,
	WHEN,
	WHERE,
	THAT,
	STATE,
	VPPP,
	ADJPP,
	MAIN_SUBJECT,
	TYPE,
	VALUE_PERCENTAGE,
	VALUE_DEGREE,
	COUNT,
	CHROM,
	EOL, */
	
	UNASSIGNED, 			// the leaf parseTrees that have not yet assigned a chunktype are assigned this type
	ORGAN,					// e.g. < > stands for organ at the beginning. later at some point ( ) used to stand for organ
	STATE,					// e.g. { } , will use this also for c[] because it is a state
	
	AREA,					// text only
	BRACKETED,				// text only
	CHROM,					// text only
	COMMA,					// text only
	VALUE,					// text only
	VALUE_DEGREE,			// text only
	VALUE_PERCENTAGE,		// text only
	RATIO,					// text only
	SBAR,					// text only
	NUMERICALS,				// text only
	OR,						// text only
	COUNT,					// text only
	BASED_COUNT,			// text only
	
	END_OF_LINE,			// no text, end of line (sentence)
	END_OF_SUBCLAUSE,		// no text, end of a clause (subsentence e.g. given by semicolon?)
	
	// o stands for object not organ and can be more than just a simple organ
	
	OBJECT,					// e.g. o[..]
	PREPOSITION,			// e.g. p[..]
	
	MODIFIER,				// e.g. m[sometimes]
	
	NON_SUBJECT_ORGAN,		// e.g. u[o[]]
	MAIN_SUBJECT_ORGAN,		// e.g. z[ ]   aka ChunkOrgan
	
	NP_LIST,				// e.g. l[ { }( ), ( ), ( ) and/or ( )] or not l[ o[ ], o[ ] and/or o[ ]]?
	
	PP,						// e.g. r[p[ ] o[ ]]    aka ChunkPrep
	PP_LIST,				// e.g. i[..] (used in sentencechunker4stanfordparser
	SPECIFIC_PP,			// e.g. t[c[proximal] r[p[to] o[ ... ]]]
	SPECIFIER,				// e.g. c[proximal]

	VERB,					// e.g. v[..]
	VP,						// e.g. b[m[sometimes] v[borne] o[r[p[in] o[1-2 whorles]]]]... minimally b[v[..] o[..]] aka ChunkVP
	
							// e.g  c[..] is used too, however not certain if can both be used similarly with CHARACTER see ChunkedSentence
	CHARACTER_STATE,		// e.g. a[...] .. contains a CHARACTER chunk, + a number of STATE chunks?! aka ChunkSimpleCharacterState
	COMPARATIVE_VALUE,		// 0.5-1 times a[CHARACTER[width]]
	
	TO_PHRASE,
	THAN_PHRASE,			// e.g. n[size[shorter than 6(-7) mm r[p[in][o[fruit]]]]]
	THAN_CHARACTER_PHRASE,	// e.g. n[size[narrower than outer]]
	THAN,
	
	TO,						// e.g. w[..] (see ChunkedSentence)
	THAT,					// e.g. s[..] (see SentenceChunker4StanfordParser)
	WHERE,					// e.g. s[..] (see SentenceChunker4StanfordParser)
	WHEN, 					// e.g. s[..] (see SentenceChunker4StanfordParser)
	
	TYPE,					// e.g. type[..] (see ChunkedSentence)
	CONSTRAINT,				// e.g. constraint[..] (see ChunkedSentence)
	//COMPARISON

	//TO //e.g. w[..]	
}
