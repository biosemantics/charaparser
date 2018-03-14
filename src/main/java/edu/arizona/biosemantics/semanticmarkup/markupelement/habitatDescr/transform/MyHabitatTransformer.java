package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Habitat;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Treatment;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class MyHabitatTransformer implements IHabitatTransformer {

	private String modifierList;
	private String advModifiers;
	private LexicalizedParser parser;

	@Inject
	public MyHabitatTransformer(@Named("HabitatParser_ModelFile")String modelFile,
			@Named("LyAdverbpattern") String lyAdvPattern, @Named("StopWordString") String stopwords,
			@Named("ModifierList") String modifierList, @Named("AdvModifiers") String advModifiers){
		parser = LexicalizedParser.loadModel(modelFile);
		this.modifierList = modifierList;
		this.advModifiers = advModifiers;
	}

	@Override
	public void transform(List<HabitatsFile> habitatsFiles) {
		for(HabitatsFile habitatsFile : habitatsFiles) {
			int i = 0;
			int organId = 0;
			for(Treatment treatment : habitatsFile.getTreatments()) {
				for(Habitat habitat : treatment.getHabitats()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("habitat_" + i++);
					statement.setText(habitat.getText());

					BiologicalEntity be = new BiologicalEntity();
					be.setName("whole_organism");
					be.setId("hab_o"+organId++);
					be.setType("structure");
					be.setNameOriginal("");
					be.addCharacters(parse(habitat.getText()));
					statement.addBiologicalEntity(be);

					statements.add(statement);
					habitat.setStatements(statements);
				}
			}
		}
	}

	private LinkedHashSet<Character> parse(String text) {
		LinkedHashSet<Character> result = new LinkedHashSet<Character>();

		/*text = text.trim();
		if(text.matches("^.*\\p{Punct}$")) {
			text = text.substring(0, text.length() - 1);
		}*/

		TreebankLanguagePack tlp = parser.getOp().langpack();
		Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(text));
		List<? extends HasWord> sentence = toke.tokenize();
		Tree root = parser.apply(sentence);
		List<Tree> leaves = root.getLeaves();

		System.out.println(root.pennString());
		System.out.println(text);


		String danglingNonNNSValues = "";
		for(int i=0; i<leaves.size(); i++) {
			Tree leaf = leaves.get(i);
			Tree parent = leaf.parent(root);

			if(isLastPartOfNounPhrase(leaf, root)) {
				if(parent.label().value().equals("NNS")) {
					result.addAll(getCharacters(leaf, root, danglingNonNNSValues));
					danglingNonNNSValues = "";
				} else {
					danglingNonNNSValues += " " + leaf.nodeString();
				}
			} else if(!isPartOfNounPhraseWithNNS(leaf, root)) {
				danglingNonNNSValues += " " + leaf.nodeString();
			}
		}

		System.out.println(result);
		return result;
	}

	private boolean isPartOfNounPhraseWithNNS(Tree leaf, Tree root) {
		Tree np = this.getNp(leaf, root);
		if(np == null)
			return false;
		for(Tree l : np.getLeaves()) {
			Tree lParent = l.parent(root);
			if(lParent.label().value().equals("NNS"))
				return true;
		}
		return false;
	}

	private Tree getNp(Tree leaf, Tree root) {
		Tree ancestor = leaf;
		int i=1;
		Tree np = null;
		while(ancestor != root) {
			ancestor = leaf.ancestor(i++, root);
			if(ancestor != null) {
				if(ancestor.label().value().matches("NP")) {
					return ancestor;				}
			}
		}
		return null;
	}

	private List<Character> getCharacters(Tree leaf, Tree root, String danglingNonNNSValues) {
		List<Character> characters = new ArrayList<Character>();

		Tree np = getNpWithLastLeaf(leaf, root);

		String modifier = "";
		String value = "";

		List<Tree> npLeaves = np.getLeaves();
		Tree lastLeaf = npLeaves.get(npLeaves.size() - 1);
		for(int i=0 ; i<npLeaves.size(); i++) {
			Tree npLeafPrevious = i == 0 ? null : npLeaves.get(i-1);
			Tree npLeaf = npLeaves.get(i);

			Tree npLeafParent = npLeaf.parent(root);
			Tree npLeafPreviousParent = npLeafPrevious = i == 0 ? null : npLeafPrevious.parent(root);
			if(npLeafParent.label().value().matches("RB|RBR|RBS")) {
				modifier += " " + npLeaf.nodeString();
			} else {
				if(npLeaf.nodeString().matches("\\p{Punct}|and|or")) {
					String characterValue = value;
					if(npLeafPreviousParent != null && !npLeafPreviousParent.label().value().matches("NNS"))
						characterValue = value + " " + (lastLeaf.nodeString().matches("\\p{Punct}") ? "" : lastLeaf.nodeString());

					Character character = getCharacter(modifier, (danglingNonNNSValues.trim() + " " + characterValue.trim()).trim());
					if(character != null)
						characters.add(character);

					modifier = "";
					value = "";
				} else {
					value += " " + npLeaf.nodeString();
				}
			}
		}

		Character character = getCharacter(modifier, (danglingNonNNSValues.trim() + " " + value.trim()).trim());
		if(character != null)
			characters.add(character);
		return characters;
	}

	private Tree getNpWithLastLeaf(Tree leaf, Tree root) {
		Tree ancestor = leaf;
		int i=1;
		Tree np = null;
		while(ancestor != root) {
			ancestor = leaf.ancestor(i++, root);
			if(ancestor != null) {
				if(ancestor.label().value().matches("NP")) {

					List<Tree> leaves = ancestor.getLeaves();
					i = 1;
					Tree lastLeaf = leaves.get(leaves.size() - i);
					while(lastLeaf.nodeString().matches("^\\p{Punct}*$")) {
						lastLeaf = leaves.get(leaves.size() - ++i);
					}

					if(lastLeaf.equals(leaf)) {
						return ancestor;
					}
				}
			}
		}
		return null;
	}

	private boolean isLastPartOfNounPhrase(Tree leaf, Tree root) {
		Tree ancestor = leaf;
		int i=1;
		Tree minNP = null;
		while(ancestor != root) {
			ancestor = leaf.ancestor(i++, root);
			if(ancestor != null) {
				if(ancestor.label().value().matches("NP")) {
					minNP = ancestor;
					break;
				}
			}
		}

		if(minNP == null)
			return false;

		List<Tree> leaves = minNP.getLeaves();
		i = 1;
		Tree lastLeaf = leaves.get(leaves.size() - i);
		while(lastLeaf.nodeString().matches("^\\p{Punct}*$")) {
			lastLeaf = leaves.get(leaves.size() - ++i);
		}
		if(lastLeaf.equals(leaf)) {
			return true;
		}
		return false;
	}

	private Character getCharacter(String modifier, String value) {
		if(value.trim().isEmpty())
			return null;
		Character c = new Character();
		c.setName("habitat");
		c.setValue(value.trim().toLowerCase());
		c.setModifier(modifier.trim().toLowerCase());
		return c;
	}

	private boolean isPartOfNounPhrase(Tree leaf, Tree root) {
		Tree ancestor = leaf;
		int i=1;
		while(ancestor != root) {
			ancestor = leaf.ancestor(i++, root);
			if(ancestor != null) {
				if(ancestor.label().value().matches("NP|NNS|NNP|NNPS")) {
					return true;
				}
			}
		}
		return false;
	}

}
