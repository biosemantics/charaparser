package semanticMarkup.eval.metric;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.eval.model.Element;



public class RecallCalculator<T extends Element> {

	private IMatcher<T> matcher;

	public RecallCalculator(IMatcher<T> matcher) {
		this.matcher = matcher;
	}

	public double getResult(List<T> testElements, List<T> correctElements) {
		List<T> correctElementsLeft = new LinkedList<T>(correctElements);
		for(T testElement : testElements) {
			Iterator<T> correctElementsLeftIterator = correctElementsLeft.iterator();
			while(correctElementsLeftIterator.hasNext()) {
				T correctElementLeft = correctElementsLeftIterator.next();
				if(matcher.isMatch(testElement, correctElementLeft))
					correctElementsLeftIterator.remove();
			}
		}
		
		int matches = correctElements.size() - correctElementsLeft.size();
		
		return (float)matches / correctElements.size();
	}

}
