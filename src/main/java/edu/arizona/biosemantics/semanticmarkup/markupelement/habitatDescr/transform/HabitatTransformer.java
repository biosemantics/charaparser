package edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.transform;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.name.Named;



import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Habitat;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.HabitatsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.habitatDescr.model.Treatment;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class HabitatTransformer implements IHabitatTransformer {
	private LexicalizedParser parser;
	ArrayList<String> adjs = new ArrayList<String>();
	public boolean breakNPPP = false;
	String ignoredfreqmodifiers="usually|often|sometimes|^and|probably|especially";
	HashSet<String> store = new HashSet<String>();

	@Inject
	public HabitatTransformer(@Named("HabitatParser_ModelFile")String modelFile){
		parser = LexicalizedParser.loadModel(modelFile);
	}
	@Override
	public void transform(List<HabitatsFile> habitatsFiles) {

		//englishFactored.ser.gz
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

	@Override
	public LinkedHashSet<Character> parse(String text) {
		TreebankLanguagePack tlp = parser.getOp().langpack();
		Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(text));
		List<? extends HasWord> sentence = toke.tokenize();
		Tree parse = parser.apply(sentence);
		text = parse.toString().replaceAll("(\\([A-Z]+ |\\)|\\([,:.])", "").trim().replaceAll("\\s+", " ");
		ArrayList<Tree> saved = new ArrayList<Tree> ();
		//ArrayList<Tree> left = new ArrayList<Tree>();
		ArrayList<Tree> savedleft = new ArrayList<Tree>();
		Iterator<Tree> it = parse.iterator();
		String element = "";
		while(it.hasNext()){
			Tree node = it.next();
			if(node.isPhrasal()){
				if(node.nodeString().matches("^(NP|PRN).*") && !node.toString().startsWith("(PRN (-LRB- -LRB-)")/* && isNPsParent(node) *&& !node.parent(parse).nodeString().startsWith("PP ")*/){
					//element = processNode(saved, left,node, element);
					element = processNode(saved,node, element);
					saved.add(node);
				}else if(node.toString().startsWith("(PRN (-LRB- -LRB-)")){
					if(include(saved, node)) continue;
					element = processPRN(element, node);
					saved.add(node);
				}else if(node.nodeString().matches("^(ADJP|VP|PP|ADVP).*")){ //PRN: parentheses
					if(!node.toString().contains("Habitats") && !include(saved, node) &&  !node.toString().replaceAll("(\\([A-Z]+ |\\))", "").trim().startsWith("include")){
						//left.add(node);
						//if(include(savedleft, node)) continue;
						//savedleft.add(node);
						if(include(saved, node)) continue;
						saved.add(node);
						element = processAPhrase(element, node);
					}
				}
			}
		}


		if(this.breakNPPP){//keep prep phrases in separate <habitat>
			element = element.replace("###", "");
		}else{//merge prep phrases to previous <habitat>
			String newelement = ""; //text: slopes in thin , rocky (-LRB- -LRB- limestone (-RRB- -RRB- soils , in woods with abundant oaks
			String textcp = text.replaceAll("\\(-LRB- -LRB- ", "(").replaceAll("\\(-RRB- -RRB-", ")").replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").replaceAll("\\s+", " ");
			while(element.contains("###")){
				int p = element.indexOf("###")+3;
				String prepphrase = element.substring(p, element.indexOf('<', p));
				log(LogLevel.DEBUG, "textcp ="+textcp);
				String before = textcp.substring(0, textcp.indexOf(prepphrase));
				if(before.trim().matches(".*?\\W *("+this.ignoredfreqmodifiers+")? *$")){
					//prepphrase is separated by a comma (,), should not be merged
					newelement += element.substring(0, p+prepphrase.length());
					newelement = newelement.replaceFirst("###", "");//keep tag, remove ###
				}else{
					//merge
					newelement += element.substring(0, p+prepphrase.length());
					newelement = newelement.replaceFirst("</habitat><habitat>###", " "); //merge
				}
				textcp = textcp.substring(textcp.indexOf(prepphrase)+prepphrase.length()); //shrinking
				element = element.substring(p+prepphrase.length());	//shrinking				
			}
			newelement+=element;
			element = newelement;
		}
		
		
		element = element.replaceAll("\\b("+this.ignoredfreqmodifiers+")\\b", "").trim();
		element = element.replaceAll("###</habitat><habitat>", " ");//attach (...) to the next <habitat> 
		element = element.replaceAll("</habitat><habitat>@@@", " "); //attach (...) to the previous <habitat> 
		element = element.replaceAll("(###|@@@)", "");
		element = element.replaceAll("</habitat><habitat>(?=to\\b)", " "); //attach (...) to the previous <habitat> 			
		
		for(String adj : adjs){
			element = element.replaceAll("(?<="+adj+")</habitat><habitat>", " ");
		}

		element = element.replaceAll("\\s+", " ").replaceAll("\\s+(?=<)", "").replaceAll("(?<=>)\\s+", "").trim();			
		LinkedHashSet<Character> values = new LinkedHashSet<Character>();
		String[] habitats = element.split("(</?habitat>)+");
		for(String habitat: habitats){
			if(habitat.trim().length()>0){
				habitat = habitat.trim();
				Character c = new Character();
				c.setName("habitat");
				c.setValue(habitat);
				values.add(c);
			}
		}
		


/*TreebankLanguagePack tlp = parser.getOp().langpack();
		Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(text));
		List<? extends HasWord> sentence = toke.tokenize();
		Tree tree = parser.apply(sentence);
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
		List<TypedDependency> tdl = gs.typedDependenciesCollapsed(true);
		tree.pennPrint();
		Set<Dependency<Label, Label, Object>> dependencies = tree.dependencies();
		for(Dependency<Label, Label, Object> d: dependencies){
			System.out.println(d.toString());
			System.out.println("object="+d.name());
			System.out.println("dependent label="+d.dependent());
			System.out.println("governer label="+d.governor());
		}

		for(TypedDependency d: tdl){
			System.out.println(d.toString());
			System.out.println("relation="+d.reln());
			System.out.println("dependent label="+d.dep());
			System.out.println("governer label="+d.gov());
		}
		if(gs.isConnected(tdl)){
			System.out.println("all dependencies are connected");
		}*/
		return values;
}



private String processPRN(String element, Tree node) {
	String txt = node.toString().replaceAll("(\\([A-Z]+ |\\)|\\([,:.])", "").trim().replaceAll("\\s+", " ");
	txt = txt.replaceAll("\\(-?LRB-? -?LRB-?", "(");
	txt = txt.replaceAll("\\(-?RRB-? -?RRB-?", ")");
	element += "<habitat>@@@"+txt+"</habitat>";
	System.out.println("@@@"+txt);
	return element;
}

/**
 * recursive
 * @param saved
 * @param node
 * @param element
 * @return
 */
private String processNode(ArrayList<Tree> saved, Tree node, String element) {
	//does this NP hold a list of other phrases?
	if(isPhraseParent(node)){
		List<Tree> phrases = node.getChildrenAsList();
		for(Tree phrase: phrases){
			if(phrase.toString().startsWith("(PRN (-LRB- -LRB-)")){
				if(include(saved, phrase)) return element;
				saved.add(phrase);
				element = processPRN(element, phrase);
			}
			else if(!phrase.nodeString().matches("^(,|CC).*")){
				if(include(saved, phrase)) return element;
				//saved.add(phrase);
				//element = processAPhrase(element, phrase);
				element = processNode(saved, phrase, element);
			}
		}
	}else{
		if(include(saved, node)) return element;
		saved.add(node);
		element = processAPhrase(element, node);
	}
	return element;
}

private String processAPhrase(String element, Tree node){
	if(node.nodeString().startsWith("ADVP") && node.getChildrenAsList().size()==1) return element;
	boolean adj = false;

	if(node.nodeString().startsWith("ADJP") || (node.nodeString().startsWith("NP") && node.getChildrenAsList().size()==1 && node.getChild(0).nodeString().startsWith("JJ"))){
		adj = true; //should be combined with the next <habitat>

	}
	String subtree = node.toString();
	if(!subtree.contains("Habitats")){
		//(NP (JJ dry) (, ,) (JJ rocky) (NNS slopes)) => (NP (JJ dry) (, @) (JJ rocky) (NNS slopes))
		String txt = subtree.replaceAll("(?<=\\(JJ \\w{3,20}\\) \\(, ),", "@");
		if(txt.contains("@")){
			addAdj(txt, "@");
		}
		txt = txt.replaceAll("(\\([A-Z]+ |\\)|\\([,:.])", "").trim().replaceAll("\\s+", " ");
		txt = txt.replaceAll("\\(-?LRB-? -?LRB-?", "(");
		txt = txt.replaceAll("\\(-?RRB-? -?RRB-?", ")");

		if(adj) this.adjs.add(txt);
		//txt = txt.replaceAll("\\b("+this.ignoredfreqmodifiers+")\\b", "").trim();
		if(txt.contains(",") && !subtree.contains("PP (IN "))  {
			String [] segs = txt.split("\\s*,\\s*");
			for(String seg: segs){
				element += "<habitat>"+seg.replaceAll("@",  ",")+"</habitat>";
				System.out.println("==="+seg);
			}
		}else{
			if(node.nodeString().matches("^PP.*") || node.toString().matches("^\\(VP[^,]* \\(PP.*")) txt = "###"+txt; //### indicates this is a prep phrase
			element += "<habitat>"+(adj? txt+"###":txt).replaceAll("@",  ",")+"</habitat>"; //###</habitat> indicates this an adj phrase 
			System.out.println("==="+(adj? txt+"###":txt).replaceAll("@",  ","));
		}
	}

	return element;
}

/**
 * dry
 * @param txt
 * @param string
 */
private void addAdj(String txt, String string) {
	//  (NP (JJ dry) (, @) (JJ rocky) (NNS slopes))
	Pattern p = Pattern.compile("\\(JJ (\\w{3,20})\\) \\(, @");
	Matcher m = p.matcher(txt);
	while(m.find()){
		String adj = txt.substring(m.start(1), m.end(1));
		this.adjs.add(adj);
	}
}

/**
 * remove saved from left
 * @param saved
 * @param left
 */
private void removeSaved(ArrayList<Tree> saved, Tree t) {
	String treestring = t.toString();
	for(Tree atree: saved){
		if(t.dominates(atree)) treestring = treestring.replace(treestring, atree.toString());
	}		

}

/**
 * 
 * @param node
 * @return true if node has  2 or more than 2 NPs
 */
private boolean isPhraseParent(Tree node) {
	List<Tree> children = node.getChildrenAsList();
	for(Tree child: children){
		if(!child.nodeString().matches("^(ADJP|NP|VP|PP|ADVP|,|CC|PRN).*")) return false;
	}
	return true;
}

private boolean include(ArrayList<Tree> saved, Tree node) {
	for(Tree t: saved){
		if((t.dominates(node) && t.contains(node)) || t==node) return true;
	}
	return false;
}

public boolean accept(Object obj) {
	if(obj instanceof Tree){
		return ((Tree)obj).isPhrasal();
	}
	return false;
}

}
