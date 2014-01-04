package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.unsupervised;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class AdditionalBootstrapping implements LearningModule {
	private Utility myUtility;

	public AdditionalBootstrapping() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * bootstrapping using clues such as shared subject different boundary and
	 * one lead word
	 */
	@Override
	public DataHolder run(DataHolder myDataHolder) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.additionalBootStrapping");
		myLogger.trace("Enter additionalBootStrapping");

		int flag = 0;

		do {
			myLogger.trace(String.format("Enter one do-while loop iteration"));
			flag = 0;

			// warmup markup
			int cmReturn = wrapupMarkup();
			myLogger.trace(String
					.format("wrapupMarkup() returned %d", cmReturn));
			flag += cmReturn;

			// one lead word markup
			//Set<String> tags = myDataHolder.getCurrentTags();
			//int omReturn = oneLeadWordMarkup(myDataHolder, tags);
			//myLogger.trace(String.format("oneLeadWordMarkup() returned %d",
//					omReturn));
//			flag += omReturn;

			// doit markup
//			int dmReturn = myUtility.getLearnerUtility().doItMarkup(myDataHolder);
//			myLogger.trace(String.format("doItMarkup() returned %d", dmReturn));
//			flag += dmReturn;

			myLogger.trace(String.format("Quite this iteration with flag = %d",
					flag));
		} while (flag > 0);

		return myDataHolder;
	}


	
	public int oneLeadWordMarkup(DataHolder myDataHolder, List<String> tagList) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger
				.getLogger("learn.additionalBootStrapping.oneLeadWordMarkup");
		String tags = StringUtility.joinList("|", tagList);
		int sign = 0;
		myLogger.trace(String.format("Enter (%s)", tags));

		for (int i = 0; i < myDataHolder.getSentenceHolder().size(); i++) {
			SentenceStructure sentence = myDataHolder.getSentenceHolder().get(i);
			String tag = sentence.getTag();
			String lead = sentence.getLead();

			if ((tag == null) && (lead.matches("% %"))) {
				if (StringUtility.createMatcher(
						String.format("\\b%s\\|", lead), tags).find()) {
					// tagIt(i, lead);
					myLogger.trace(String.format(
							"updateDataHolder(%s, n, -, wordpos, 1)", lead));
					sign += myDataHolder.updateDataHolder(lead, "n", "-",
							"wordpos", 1);
				}
			}
		}

		myLogger.trace("Return: " + sign);
		return 0;
	}

	public int wrapupMarkup() {
		// TODO Auto-generated method stub
		return 0;
	}

}
