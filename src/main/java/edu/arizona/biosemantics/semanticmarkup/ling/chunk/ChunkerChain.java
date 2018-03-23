package edu.arizona.biosemantics.semanticmarkup.ling.chunk;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;


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
	 * @param description
	 * @param descriptionsFile
	 * @param source
	 * @param sentenceString
	 * @param originalSent
	 * @return the resulting chunkCollector
	 */
	public ChunkCollector chunk(AbstractParseTree parseTree, String subjectTag, Description description, AbstractDescriptionsFile descriptionsFile,
			String source, String sentenceString, String originalSent) {
		ChunkCollector chunkCollector = new ChunkCollector(parseTree, subjectTag, description, descriptionsFile, source, sentenceString, originalSent);
		for(IChunker chunker : chunkers) {//TODO Hong: chunkers hold the list of predefined chunk types
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

