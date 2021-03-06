package edu.arizona.biosemantics.semanticmarkup.eval.metric;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.eval.matcher.IMatcher;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.model.Element;




public class RecallCalculator<T extends Element> {

	private IMatcher<T> matcher;

	public RecallCalculator(IMatcher<T> matcher) {
		this.matcher = matcher;
	}

	public double getResult(List<T> testElements, List<T> correctElements) {
		List<T> correctElementsLeft = new LinkedList<T>(correctElements);
		
		int i=1;
		for(T testElement : testElements) {
			log(LogLevel.DEBUG, i++ + " of " + testElements.size());
			Iterator<T> correctElementsLeftIterator = correctElementsLeft.iterator();
			while(correctElementsLeftIterator.hasNext()) {
				T correctElementLeft = correctElementsLeftIterator.next();
				
				log(LogLevel.DEBUG, "correct elements left " + correctElementsLeft.size());
				log(LogLevel.DEBUG, testElement.toString() + " " + correctElementLeft.toString());
				
				if(matcher.isMatch(testElement, correctElementLeft)) {
					log(LogLevel.DEBUG, "match found");
					correctElementsLeftIterator.remove();
					break;
				} else {
					log(LogLevel.DEBUG, "not a match");
				}
			}
		}
		
		int matches = correctElements.size() - correctElementsLeft.size();
		log(LogLevel.DEBUG, matches + " Matches from " + correctElements.size() + " possibles");
		
		double result;
		if(correctElements.size() == 0) 
			//one may want to treat the precisin calculation different in this case..
			result = 1.0;
			//result = Double.NaN;
		else 
			result = (float)matches / correctElements.size();
		
		return result;
	}

}
