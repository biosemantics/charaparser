package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn;

/**
 * ILearner learns given some unspecified kind of input e.g. textual descriptions in a file
 * @author rodenhausen
 */
public interface ILearner {

	/**
	 * learns
	 * @throws Exception
	 */
	public void learn() throws Throwable;

	/**
	 * @return a description of this ILearner implementation
	 */
	public String getDescription();

}
