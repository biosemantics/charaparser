package edu.arizona.biosemantics.semanticmarkup.eval.metric;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.eval.matcher.IMatcher;
import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.model.Element;




public class PrecisionCalculator<T extends Element> {

	private IMatcher<T> matcher;

	public PrecisionCalculator(IMatcher<T> matcher) {
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
				
				log(LogLevel.DEBUG, "correct elemetns left " + correctElementsLeft.size());
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
		log(LogLevel.DEBUG, matches + " Matches from " + testElements.size() + " given ones");
		
		double result;
		if(testElements.size() == 0)
			//one may want to treat the precisin calculation different in this case..
			//if(correctElements.size() > 0)
				result = 1.0;
			//else
			//	result = Double.NaN;
		else 
			result = (float)matches / testElements.size();
		
		return result;
	}

}
