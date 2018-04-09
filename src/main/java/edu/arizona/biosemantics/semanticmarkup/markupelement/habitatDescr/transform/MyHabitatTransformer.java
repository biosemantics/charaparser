package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
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
	private GlossaryInitializer glossaryInitializer;
	private IGlossary glossary;

	@Inject
	public MyHabitatTransformer(@Named("HabitatParser_ModelFile")String modelFile,
			@Named("LyAdverbpattern") String lyAdvPattern, @Named("StopWordString") String stopwords,
			@Named("ModifierList") String modifierList, @Named("AdvModifiers") String advModifiers,
			IGlossary glossary,
			GlossaryInitializer glossaryInitializer) {
		parser = LexicalizedParser.loadModel(modelFile);
		this.modifierList = modifierList;
		this.advModifiers = advModifiers;
		this.glossary = glossary;
		this.glossaryInitializer = glossaryInitializer;
	}

	@Override
	public void transform(List<HabitatsFile> habitatsFiles) throws Exception {
		glossaryInitializer.initialize(glossary);

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

	private LinkedHashSet<Character> parse(String originalText) {
		LinkedHashSet<Character> result = new LinkedHashSet<Character>();

		String text = normalize(originalText);

		TreebankLanguagePack tlp = parser.getOp().langpack();
		Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(text));
		List<? extends HasWord> sentence = toke.tokenize();
		Tree root = parser.apply(sentence);
		List<Tree> leaves = root.getLeaves();

		System.out.println(root.pennString());
		System.out.println(text);

		if(root.label().value().equals("X")) {
			result.addAll(parseStanfordUnparsable(text));
			return result;
		}

		String danglingNonNNSValues = "";
		boolean danglingTO = false;
		List<Integer> processedLeaves = new ArrayList<Integer>();
		for(int i=0; i<leaves.size(); i++) {
			Tree leaf = leaves.get(i);
			if(isUppercaseAndNotFirstToken(leaf.toString(), originalText))
				continue;
			//System.out.println(leaf);
			if(processedLeaves.contains(leaf.nodeNumber(root)))
				continue;
			Tree parent = leaf.parent(root);

			if(isLastPartOfNounPhrase(leaf, root)) {
				//System.out.println(leaf.toString());
				if(parent.label().value().matches("NNS|NNP|NN") && !leaf.toString().equalsIgnoreCase("e.g.")) {
					result.addAll(getCharacters(leaf, root, danglingNonNNSValues, danglingTO, processedLeaves, originalText));
					danglingNonNNSValues = "";
					danglingTO = false;
				} else {
					if(!leaf.nodeString().startsWith("e.g")) {
						danglingNonNNSValues += " " + leaf.nodeString();
						if(leaf.parent(root).label().value().equals("TO"))
							danglingTO = true;
					}
				}
			} else if(!isPartOfNounPhraseWithNNS(leaf, root)) {
				if(!leaf.nodeString().startsWith("e.g")) {
					danglingNonNNSValues += " " + leaf.nodeString();
					if(leaf.parent(root).label().value().equals("TO"))
						danglingTO = true;
				}
			}
			processedLeaves.add(leaf.nodeNumber(root));
		}

		for(Character character : new ArrayList<Character>(result)) {
			if(character.getValue().equalsIgnoreCase("e.g."))
				result.remove(character);

			if(character.getValue().startsWith("e.g."))
				character.setValue(character.getValue().replace("e.g.", "").trim());
			if(character.getValue().startsWith("e.g"))
				character.setValue(character.getValue().replace("e.g", "").trim());
		}

		Set<String> habitats = glossary.getMainTermsOfCategory("habitat");
		for(int i=0; i<leaves.size(); i++) {
			Tree leaf = leaves.get(i);
			if(habitats.contains(leaf.toString().toLowerCase())) {
				boolean foundInCharacterValue = false;
				for(Character character : result) {
					if(character.getValue().contains(leaf.toString())) {
						foundInCharacterValue = true;
					}
				}

				if(!foundInCharacterValue) {
					Tree parentParent = leaf.parent(root).parent(root);
					String modifier = "";
					for(Tree phraseLeaf : parentParent.getLeaves()) {
						if(phraseLeaf.parent(root).label().value().matches("RB|RBR|RBS") && !phraseLeaf.nodeString().startsWith("e.g")) {
							modifier += phraseLeaf.toString() + " ";
						}
					}

					result.add(getCharacter(modifier.trim(), leaf.toString()));
				}
			}
		}

		//if no character found; create a character containing the entire text as last resort
		if(result.isEmpty()) {
			result.add(getCharacter("", text.trim()));
		}

		result = filterDuplicates(result);
		System.out.println(result);
		return result;
	}

	private boolean isUppercaseAndNotFirstToken(String token, String originalText) {
		String uppercaseToken = token.substring(0, 1).toUpperCase() + token.substring(1);
		if(originalText.contains(uppercaseToken) && originalText.indexOf(uppercaseToken) != 0)
			return true;
		return false;
	}

	private LinkedHashSet<Character> filterDuplicates(
			LinkedHashSet<Character> result) {
		LinkedHashSet<Character> r = new LinkedHashSet<Character>();
		Set<String> seen = new HashSet<String>();
		for(Character c : result) {
			if(!seen.contains(c.toString())) {
				seen.add(c.toString());
				r.add(c);
			}
		}
		return r;
	}

	private List<Character> parseStanfordUnparsable(String text) {
		List<Character> result = new ArrayList<Character>();
		if(!text.matches(".*[,|;|.].*")) {
			result.add(getCharacter("", text));
			return result;
		}

		String[] parts = text.split("[,|;|.]");
		for(String part : parts) {
			result.addAll(this.parse(part));
		}
		return result;
	}

	private String normalize(String text) {
		/*text = text.trim();
		if(text.matches("^.*\\p{Punct}$")) {
			text = text.substring(0, text.length() - 1);
		}*/

		return text.toLowerCase().trim();
	}

	private boolean isPartOfNounPhraseWithNNS(Tree leaf, Tree root) {
		Tree np = this.getNp(leaf, root);
		if(np == null)
			return false;
		for(Tree l : np.getLeaves()) {
			Tree lParent = l.parent(root);
			if(lParent.label().value().matches("NNS|NNP|NN"))
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

	private List<Character> getCharacters(Tree leaf, Tree root, String danglingNonNNSValues, boolean danglingTO, List<Integer> processedLeaves, String originalText) {
		//System.out.println("get characters: " + leaf);
		//System.out.println(processedLeaves);
		List<Character> characters = new ArrayList<Character>();

		danglingNonNNSValues = danglingNonNNSValues.trim();
		while(danglingNonNNSValues.matches("^.*\\p{Punct}$"))
			danglingNonNNSValues = danglingNonNNSValues.substring(0, danglingNonNNSValues.length() - 1).trim();

		Tree np = getNpWithLastLeaf(leaf, root);

		String modifier = danglingTO ? "" : danglingNonNNSValues;
		String value = "";

		List<Tree> npLeaves = np.getLeaves();
		Tree lastLeaf = npLeaves.get(npLeaves.size() - 1);
		for(int i=0 ; i<npLeaves.size(); i++) {
			Tree npLeafPrevious = i == 0 ? null : npLeaves.get(i-1);
			Tree npLeaf = npLeaves.get(i);
			//System.out.println(npLeaf);
			//System.out.println(npLeaf.nodeNumber(root));
			//if(processedLeaves.contains(npLeaf.nodeNumber(root)))
			//	continue;

			Tree npLeafParent = npLeaf.parent(root);
			Tree npLeafPreviousParent = npLeafPrevious = i == 0 ? null : npLeafPrevious.parent(root);
			if(npLeafParent.label().value().matches("RB|RBR|RBS") && !npLeaf.nodeString().startsWith("e.g")) {
				modifier += " " + npLeaf.nodeString();
			} else {
				if(npLeaf.nodeString().matches("\\p{Punct}|and|or")) {
					String characterValue = value;


					//if(npLeafPreviousParent != null && !npLeafPreviousParent.label().value().matches("NNS"))
					//	characterValue = value + " " + (lastLeaf.nodeString().matches("\\p{Punct}") ? "" : lastLeaf.nodeString());

					Character character = getCharacter(modifier, danglingTO ? (danglingNonNNSValues.trim() + " " + characterValue.trim()).trim() : characterValue.trim());
					if(character != null)
						characters.add(character);

					modifier = "";
					value = "";
				} else {
					value += " " + npLeaf.nodeString();
				}
			}
		}
		Character character = getCharacter(modifier, danglingTO ? (danglingNonNNSValues.trim() + " " + value.trim()).trim() : value.trim());
		if(character != null)
			characters.add(character);

		String constraint = "";
		Tree npParent = np.parent(root);
		if(npParent != null) {
			int npIndex = npParent.indexOf(np);
			if(npIndex + 1 < npParent.children().length) {
				Tree followNode = npParent.getChild(npIndex + 1);
				npIndex = npIndex + 1;
				while(followNode.label().value().equals(",") && npIndex + 1 < npParent.children().length) {
					followNode = npParent.getChild(npIndex + 1);
					npIndex = npIndex + 1;
				}
				if(followNode.label().value().equals("PP")) {
					if(followNode.children().length > 0) {
						Tree ppChildNode = followNode.getChild(0);
						if(ppChildNode.label().value().equals("IN")) {
							constraint = getLeafString(followNode);
							//for(Tree ppInLeaf : followNode.getLeaves())
							//	processedLeaves.add(ppInLeaf.nodeNumber(root));
						}
					}
				}
			}
		}

		for(Character c : characters) {
			c.setConstraint(constraint);
		}

		return characters;
	}

	private String getLeafString(Tree tree) {
		String result = "";
		for(Tree leaf : tree.getLeaves()) {
			result += leaf.toString() + " ";
		}
		return result.trim();
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
