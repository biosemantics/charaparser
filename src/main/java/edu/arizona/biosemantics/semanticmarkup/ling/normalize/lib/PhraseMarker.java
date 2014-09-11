/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib;

import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.IPhraseMarker;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;

/**
 * @author Hong Cui
 * 
 * 
 *
 */
public class PhraseMarker implements IPhraseMarker{

		private Pattern phrasepattern; //"dorsal_fin|leaf_blade"
		private String phrasestr;
		private IInflector inflector;
		private IGlossary glossary;
		
		//private static final String BIN_FILE = "PO_phrases.bin";
		/**
		 * @param termsource: filepath to the serialized arraylist, holding the phrases 
		 */
		@SuppressWarnings("unchecked")
		@Inject
		public PhraseMarker(IGlossary glossary, IInflector inflector) {
			this.glossary = glossary;
			this.inflector = inflector;
			formPattern();
		}
		
		
		protected synchronized void formPattern (){
			try {
				ArrayList<String> phrases = new ArrayList<String>();
			    HashSet<String> structPhrases = new HashSet<String>();
			    structPhrases = (HashSet<String>) glossary.getStructurePhrasesWithSpaces();//phrases are words connected with " "	
			    phrases.addAll(structPhrases); 
			    phrases.addAll((HashSet<String>) glossary.getNonStructurePhrasesWithSpaces());
				Collections.sort(phrases, new PhraseComparable()); //longest phrases first
				phrasestr = "";
				for(String phrase: phrases){
					//hyomandibula-opercle joint
					phrase = phrase.replaceAll("\\([^)]*\\)", "").trim();
					if(phrase.length()>0 && phrase.indexOf(" ")>0){ //can't allow single-word phrase 
						phrase = phrase.replaceAll("-", "_");//hyomandibula_opercle joint
						phrase = phraseForms(phrase, structPhrases);//added plural forms for structure terms
						phrasestr += phrase+"|";
					}
				}
				if(!phrasestr.isEmpty()){
					phrasestr = phrasestr.replaceFirst("\\|$", ""); //space separated words in phrases
					//System.out.println(phrasestr);
					this.phrasepattern =Pattern.compile("(.*?\\b)("+phrasestr+")(\\b.*)", Pattern.CASE_INSENSITIVE);
				}
				/*serialize the updated phrases
				phrases.addAll(plphrases.keySet()); //add plurals
				ObjectOutput out = new ObjectOutputStream(
						new FileOutputStream(new File(Configuration.)))); //avoid increase the size of the original
				out.writeObject(phrases);
				out.close();
				//serialize the plural-singular mapping
				file = new File(ApplicationUtilities.getProperty("ontology.dir"),ApplicationUtilities.getProperty("ontology.uberon")+"_"+ApplicationUtilities.getProperty("uberonphrases.p2s.bin"));
				out = new ObjectOutputStream(
						new FileOutputStream(file));
				out.writeObject(newphrases);
				out.close();*/
			} catch (Exception e) {
				//LOGGER.error("Load the updated TaxonIndexer failed.", e);
				//StringWriter sw = new StringWriter();PrintWriter pw = new PrintWriter(sw);e.printStackTrace(pw);LOGGER.error(ApplicationUtilities.getProperty("CharaParser.version")+System.getProperty("line.separator")+sw.toString());
				e.printStackTrace();
			}
		}

		/**
		 * endochondral[ _-]element => endochondral_element|endochondral_elements
		 * @param structPhrases 
		 * @param phrase: typically in singular form
		 * @return alternative reg exp with original and plural forms
		 */
		private String phraseForms(String phrase, HashSet<String> structPhrases) {
			if(phrase.indexOf(" ")>0){
				String result = phrase.trim().replaceAll("[ _-]", "[ _-]");
				String noun = phrase.substring(phrase.lastIndexOf(" ")).trim();
				String modifier = phrase.substring(0, phrase.lastIndexOf(" ")).trim().replaceAll("[ _-]", "[ _-]");
				String pnoun = noun.matches("\\d+") || !structPhrases.contains(phrase)? noun : inflector.getPlural(noun);
				if(pnoun.compareTo(noun)!=0){
					result += "|"+modifier+"[_ -]"+pnoun;
					//this.plphrases.put(modifier+" "+pnoun, phrase); //plural=>singluar
				}
				return result;
			}
			return phrase;
		}

		/**
		 * @param sentence : leaf blade rounded
		 * @return leaf_blade rounded
		 */
		@Override
		public String markPhrases(String sentence){
			if(this.phrasepattern!=null){
				Matcher m = phrasepattern.matcher(sentence);
				System.out.println(this.phrasepattern);
				while(m.matches()){
					sentence = m.group(1)+m.group(2).replaceAll("[ _-]", "_#_")+m.group(3);
					m = phrasepattern.matcher(sentence);
				}				
			}
			return sentence.replaceAll("_#_", "_");
		}

	}


