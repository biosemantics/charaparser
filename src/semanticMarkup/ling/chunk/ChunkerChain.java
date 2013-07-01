package semanticMarkup.ling.chunk;

import java.util.LinkedList;
import java.util.List;

import semanticMarkup.core.Treatment;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.log.LogLevel;

/**
 * ChunkChain allows to chain a number of IChunkers and execute them consecutively
 * populates chunkCollector
 * @author rodenhausen
 */
public class ChunkerChain {
	
	protected List<IChunker> chunkers = new LinkedList<IChunker>();
	
	/**
	 * Execute chunkers consecutively
	 * @param parseTree
	 * @param subjectTag
	 * @param treatment
	 * @param source
	 * @param sentenceString
	 * @return the resulting chunkCollector
	 */
	public ChunkCollector chunk(AbstractParseTree parseTree, String subjectTag, Treatment treatment, String source, String sentenceString) {
		ChunkCollector chunkCollector = new ChunkCollector(parseTree, subjectTag, treatment, source, sentenceString);
		for(IChunker chunker : chunkers) {
			log(LogLevel.DEBUG, "Chunker " + chunker.getName() + " is run ...");
			chunkCollector.resetHasChanged();
			try {
				chunker.chunk(chunkCollector);	
			} catch (Exception e) {
				log(LogLevel.ERROR, "Problem executing chunkerChain for sentence: " + chunkCollector.getSentence() + "\n" +
						"Sentence is contained in file: " + chunkCollector.getSource(), e);
			}
			if(chunkCollector.hasChanged()) 
				log(LogLevel.DEBUG, chunkCollector.toString());
		}
		return chunkCollector;
	}

	/**
	 * @param chunker
	 * @return if the chunker was added successfully
	 */
	public boolean add(IChunker chunker) {
		return chunkers.add(chunker);
	}

	/**
	 * @param chunker
	 * @return if the chunker was removed successfully
	 */
	public boolean remove(IChunker chunker) {
		return chunkers.remove(chunker);
	}
	
}

