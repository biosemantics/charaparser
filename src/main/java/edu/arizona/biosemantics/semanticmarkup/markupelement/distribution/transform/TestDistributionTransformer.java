package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Distribution;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Treatment;

public class TestDistributionTransformer {

	public static void main(String[] args) {

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);

		String[] examples = {
				"introduced, North America;",
		};

		String lyPattern = "[a-z]{3,}ly";
		String stopWords = "a|about|above|across|after|along|also|although|amp|an|and|are|as|at|be|because|become|becomes|becoming|been|before|being|"
				+ "beneath|between|beyond|but|by|ca|can|could|did|do|does|doing|done|for|from|had|has|have|hence|here|how|however|if|in|into|inside|inward|is|it|its|"
				+ "may|might|more|most|near|of|off|on|onto|or|out|outside|outward|over|should|so|than|that|the|then|there|these|this|those|throughout|"
				+ "to|toward|towards|up|upward|was|were|what|when|where|which|why|with|within|without|would";
		String advModifiers = "at least|at first|at times|almost|always|never|not|often|quite|rather|sometimes|somewhat";
		IDistributionTransformer transformer = new DistributionTransformer();

		DistributionsFile distributionsFile = new DistributionsFile();
		List<Treatment> treatments = new ArrayList<Treatment>();
		Treatment treatment = new Treatment();
		List<Distribution> distributions = new ArrayList<Distribution>();
		for(String example : examples) {
			Distribution distribution = new Distribution();
			distribution.setText(example);
			distributions.add(distribution);
		}
		treatment.setDistributions(distributions);
		treatments.add(treatment);
		distributionsFile.setTreatments(treatments);
		transformer.transform(Arrays.asList(new DistributionsFile[] { distributionsFile }));
	}
}
