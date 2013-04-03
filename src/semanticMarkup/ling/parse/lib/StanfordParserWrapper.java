package semanticMarkup.ling.parse.lib;

import java.util.ArrayList;
import java.util.List;

import semanticMarkup.ling.Token;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParser;
import semanticMarkup.ling.transform.IStanfordParserTokenTransformer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

/**
 * A StanfordParserWrapper parses a list of tokens using stanford parser
 * @author rodenhausen
 */
public class StanfordParserWrapper implements IParser {

	private LexicalizedParser parser;
	private IStanfordParserTokenTransformer tokenTransformer;
	
	/**
	 * @param modelFile
	 * @param tokenTransformer
	 */
	@Inject
	public StanfordParserWrapper(@Named("StanfordParserWrapper_modelFile")String modelFile, IStanfordParserTokenTransformer tokenTransformer) {
		this.tokenTransformer = tokenTransformer;
		parser = LexicalizedParser.loadModel(modelFile);
	}

	@Override
	public AbstractParseTree parse(List<? extends Token> sentence) {
		List<HasWord> stanfordSentence = new ArrayList<HasWord>();
		for(Token token : sentence) {
			HasWord hasWord = tokenTransformer.transform(token);
			stanfordSentence.add(hasWord);
		}
		Tree tree = parser.parseTree(stanfordSentence);
		return new StanfordParseTree(tree);
	}
}
