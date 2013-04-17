package semanticMarkup.know.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.pos.POS;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * WordNetPOSKnowledgeBase poses an IPOSKnowledgeBase by relying on WordNet
 * Access to dictionary is to be synchronized as the dictionary is cached. Hence the underlying data structures constatnly subject to change.
 * Because of this parallel access to the dictionary may cause conflicts 
 * (e.g. first thread causes cached dictionary to change its content while second iterates over dictionary content)
 * @author rodenhausen
 */
public class WordNetPOSKnowledgeBase implements IPOSKnowledgeBase {

	private IDictionary dictionary;
	
	/**
	 * @param path of the wordnet source files
	 * @param loadInRAM specified whether the dictionary should be loaded into RAM or read from disk when needed
	 * @throws IOException
	 */
	@Inject
	public WordNetPOSKnowledgeBase(@Named("WordNetAPI_Sourcefile") String path, @Named("WordNetAPI_LoadInRAM") boolean loadInRAM) throws IOException {
		if(loadInRAM) 
			dictionary = new RAMDictionary(new File(path), RAMDictionary.BACKGROUND_LOAD);
		else 
			dictionary = new Dictionary(new File(path));
		dictionary.open();
	}

	@Override
	public boolean isNoun(String word) {
		synchronized(dictionary) {
			return dictionary.getIndexWord(word, edu.mit.jwi.item.POS.NOUN) != null;
		}
	}

	@Override
	public boolean isAdjective(String word) {
		synchronized(dictionary) {
			return dictionary.getIndexWord(word, edu.mit.jwi.item.POS.ADJECTIVE) != null;
		}
	}

	@Override
	public boolean isAdverb(String word) {
		synchronized(dictionary) {
			return dictionary.getIndexWord(word, edu.mit.jwi.item.POS.ADVERB) != null;
		}
	}

	@Override
	public boolean isVerb(String word) {
		synchronized(dictionary) {
			return dictionary.getIndexWord(word, edu.mit.jwi.item.POS.VERB) != null;
		}
	}
	
	/**
	 * Needs to be synchronized, otherwise not thread safe. Underlying linkedHashMap throws ConcurrentModificationException
	 */
	@Override
	public POS getMostLikleyPOS(String word) {
		WordnetStemmer stemmer = null;
		synchronized(dictionary) {
			stemmer = new WordnetStemmer(dictionary);
		}
		
		int maxCount = -1;
		edu.mit.jwi.item.POS mostLikelyPOS = null;
		for(edu.mit.jwi.item.POS pos : edu.mit.jwi.item.POS.values()) {
			
			//From JavaDoc: The surface form may or may not contain whitespace or underscores, and may be in mixed case.
			word = word.replaceAll("\\s", "").replaceAll("_", "");
			
			List<String> stems = null;
			synchronized(dictionary) {
				stems = stemmer.findStems(word, pos);
			}
			for(String stem : stems) {
				synchronized(dictionary) {
					IIndexWord indexWord = dictionary.getIndexWord(stem, pos);
					if(indexWord!=null) {
						int count = 0;
						for(IWordID wordId : indexWord.getWordIDs()) {
							IWord aWord = dictionary.getWord(wordId);
							//ISynset synset = aWord.getSynset();
							//log(LogLevel.DEBUG, synset.getGloss());
							ISenseEntry senseEntry = dictionary.getSenseEntry(aWord.getSenseKey());
							//log(LogLevel.DEBUG, senseEntry.getSenseNumber());
							count += senseEntry.getTagCount();
						}
						
						//int tagSenseCount = indexWord.getTagSenseCount();
						//int wordIdCount = indexWord.getWordIDs().size();
						if(count > maxCount) {
							maxCount = count;
							mostLikelyPOS = pos;
						}
					}
				}
			}
		}	
		
		return translateWordNetPOSToPennPOS(mostLikelyPOS);
	}
	
	private POS translateWordNetPOSToPennPOS(edu.mit.jwi.item.POS pos) {
		if(pos==null)
			return null;
		switch(pos) {
		case NOUN:
			return POS.NN;
		case VERB:
			return POS.VB;
		case ADJECTIVE:
			return POS.JJ;
		case ADVERB:
			return POS.RB;
		default:
			return null;
		}
	}

	@Override
	public boolean contains(String word) {
		for(edu.mit.jwi.item.POS pos : edu.mit.jwi.item.POS.values()) {
			synchronized(dictionary) {
				WordnetStemmer stemmer = new WordnetStemmer(dictionary);
				for(String stem : stemmer.findStems(word, pos)) {
					IIndexWord indexWord = dictionary.getIndexWord(stem, pos);
					if(indexWord!=null)
						return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public List<String> getSingulars(String word) {
		List<String> singulars = null;
		synchronized(dictionary) {
			WordnetStemmer stemmer = new WordnetStemmer(dictionary);
			singulars = stemmer.findStems(word, edu.mit.jwi.item.POS.NOUN);
		}
		List<String> result = new ArrayList<String>();
		
		TreeMap<Integer, List<String>> singularFrequencies = new TreeMap<Integer, List<String>>();
		for(String singular : singulars) {
			synchronized(dictionary) {
				IIndexWord indexWord = dictionary.getIndexWord(singular, edu.mit.jwi.item.POS.NOUN);
				if(indexWord!=null) {
					//int tagSenseCount = indexWord.getTagSenseCount();
					int wordIdCount = indexWord.getWordIDs().size();
					if(!singularFrequencies.containsKey(wordIdCount))
						singularFrequencies.put(wordIdCount, new ArrayList<String>());
					singularFrequencies.get(wordIdCount).add(singular);
				}
			}
		}
		Map<Integer, List<String>> reverseMap = singularFrequencies.descendingMap();
		for(Entry<Integer, List<String>> entry : reverseMap.entrySet())
			result.addAll(entry.getValue());
		
		if(result.isEmpty()) {
			if(word.endsWith("ies")) {
				Iterator<String> singularsIterator = singulars.iterator();
				while(singularsIterator.hasNext()) {
					String singular = singularsIterator.next();
					if(singular.endsWith("y")) {
						result.add(singular);
						singularsIterator.remove();
					}
				}
				result.addAll(singulars);
			} else {
				result = singulars;
			}
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException{
		
		WordNetPOSKnowledgeBase wordNetAPI = new WordNetPOSKnowledgeBase("src//main//resources//wordNet3.1//dict//", false);
		System.out.println(wordNetAPI.getMostLikleyPOS("green"));
		
	}

	@Override
	public void addVerb(String word) {}

	@Override
	public void addNoun(String word) {}

	@Override
	public void addAdjective(String word) {}

	@Override
	public void addAdverb(String word) {}
}
