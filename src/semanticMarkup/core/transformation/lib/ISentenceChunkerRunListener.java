package semanticMarkup.core.transformation.lib;

/**
 * ISentenceChunkerRunListener listens to when SentenceChunkerRuns are done processing
 * @author rodenhausen
 */
public interface ISentenceChunkerRunListener {

	/**
	 * @param sentenceChunker that is done processing
	 */
	public void done(SentenceChunkerRun sentenceChunker);
	
}
