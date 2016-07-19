package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms.SynonymSet;

public abstract class RemoveNonSpecificBiologicalEntities extends AbstractTransformer {
	
	private String nonSpecificParts = "apex|appendix|area|band|base|belt|body|cavity|cell|center|centre|chamber|component|content|crack|edge|element|end|"
			+ "face|groove|layer|line|margin|middle|notch|part|pore|portion|protuberance|remnant|section|"
			+ "side|stratum|surface|tip|wall|zone";
	protected KnowsPartOf knowsPartOf;
	protected ITokenizer tokenizer;
	protected KnowsSynonyms knowsSynonyms;
	protected ParenthesisRemover parenthesisRemover = new ParenthesisRemover();
	
	public RemoveNonSpecificBiologicalEntities(KnowsPartOf knowsPartOf, KnowsSynonyms knowsSynonyms, ITokenizer tokenizer) {
		this.knowsPartOf = knowsPartOf;
		this.knowsSynonyms = knowsSynonyms;
		this.tokenizer = tokenizer;
	}
		
	protected boolean isNonSpecificPart(String name) {
		Set<SynonymSet> synonymSets =  knowsSynonyms.getSynonyms(name);
		for(SynonymSet synonymSet : synonymSets) {
			if(synonymSet.getPreferredTerm().matches(nonSpecificParts)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isPartOfAConstraint(String part, String constraint) {
		for(String constraintPartition : getAllPartitions(constraint)) {
			if(knowsPartOf.isPartOf(part, constraintPartition)) {
				return true;
			}
		}
		return false;
	}
	
	private List<String> getAllPartitions(String constraint) {	
		List<String> result = new LinkedList<String>();
		List<Token> tokens = tokenizer.tokenize(constraint); 
		List<Token> remainingTokens = new LinkedList<Token>(tokens);
		for(int i=0; i<tokens.size(); i++) {
			result.addAll(getPartitionsStartingWithFirst(remainingTokens));
			remainingTokens.remove(0);
		}
		return result;
	}

	private Collection<? extends String> getPartitionsStartingWithFirst(List<Token> tokens) {
		List<String> result = new LinkedList<String>();
		for(int i=1; i<=tokens.size(); i++) {
			String partition = "";
			for(int j=0; j<i; j++) {
				partition += " " + tokens.get(j).getContent();
			}
			result.add(partition.trim());
		}
		return result;
	}

	protected Element findBiologicalEntityByNameInStatement(String term, Element statement) {
		for(Element biologicalEntity : statement.getChildren("biological_entity")) {
			if(biologicalEntity.getAttributeValue("name_original").equals(term)) {
				return biologicalEntity;
			}
		}
		return null;
	}
		
	protected Element getBiologicalEntity(String term, List<Element> searchStatements) {
		for(Element searchStatement : searchStatements) {
			for(Element biologicalEntity : searchStatement.getChildren("biological_entity")) {
				String name = biologicalEntity.getAttributeValue("name_original");
				name = name == null ? "" : name.trim();
				if(name.equals(term)) {
					return biologicalEntity;
				}
			}
		}
		return null;
	}

}
