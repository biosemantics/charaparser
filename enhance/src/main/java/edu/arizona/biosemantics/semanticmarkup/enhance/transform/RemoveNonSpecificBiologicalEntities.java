package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Element;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;

public abstract class RemoveNonSpecificBiologicalEntities extends AbstractTransformer {
	
	private String nonSpecificParts = "apex|appendix|area|band|base|belt|body|cavity|cell|center|centre|chamber|component|content|crack|edge|element|end|"
			+ "face|groove|layer|line|margin|middle|notch|part|pore|portion|protuberance|remnant|section|"
			+ "side|stratum|surface|tip|wall|zone";
	protected KnowsPartOf knowsPartOf;
	protected ITokenizer tokenizer;
	
	public RemoveNonSpecificBiologicalEntities(KnowsPartOf knowsPartOf, ITokenizer tokenizer) {
		this.knowsPartOf = knowsPartOf;
		this.tokenizer = tokenizer;
	}
		
	protected boolean isNonSpecificPart(String name) {
		return name.matches(nonSpecificParts);
	}

	protected boolean isPartOfAConstraint(String part, String constraint) {
		for(Token term : tokenizer.tokenize(constraint)) {
			if(knowsPartOf.isPartOf(part, term.getContent())) {
				return true;
			}
		}
		return false;
	}
	
	protected Element findBiologicalEntityByNameInStatement(String term, Element statement) {
		for(Element biologicalEntity : statement.getChildren("biological_entity")) {
			if(biologicalEntity.getAttributeValue("name_original").equals(term)) {
				return biologicalEntity;
			}
		}
		return null;
	}

}
