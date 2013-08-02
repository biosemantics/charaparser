package semanticMarkup.ling.transform.lib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import semanticMarkup.ling.Token;
import semanticMarkup.ling.transform.ITokenizer;

public class UnsupervisedLearningTokenizer implements ITokenizer {

	private TokenizerME myTokenizer;

	public UnsupervisedLearningTokenizer(String OpenNLPTokenizerDir) {
		// Get OpenNLP tokenizer
		InputStream tokenModelIn;
		try {
			tokenModelIn = new FileInputStream(OpenNLPTokenizerDir);
			TokenizerModel model = new TokenizerModel(tokenModelIn);
			this.myTokenizer = new TokenizerME(model);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<Token> tokenize(String text) {
		// TODO Auto-generated method stub
		String[] tempTokens = this.myTokenizer.tokenize(text);

		List<Token> tokens = new LinkedList<Token>();
		for (int i = 0; i < tempTokens.length; i++) {
			Token token = new Token(tempTokens[i]);
			tokens.add(token);
		}

		return tokens;
	}

}
