package semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import java.util.Comparator;


public class SentenceLeadLengthComparator implements Comparator<SentenceStructure> {

	private boolean isAscendOrder;
	
	public SentenceLeadLengthComparator(boolean isAscendOrder) {
		this.isAscendOrder = isAscendOrder;
	}
	
	@Override
	public int compare(SentenceStructure sentenceA, SentenceStructure sentenceB) {
		int leadLengthA = sentenceA.getLead().length();
		int leadLengthB = sentenceB.getLead().length();

		if (!this.isAscendOrder) {
			// descend order
			int temp = leadLengthA;
			leadLengthA = leadLengthB;
			leadLengthB = temp;
		}

		if (leadLengthA < leadLengthB) {
			return -1;
		} else if (leadLengthA == leadLengthB) {
			return 0;
		} else {
			return 1;
		}
	}

}
