package semanticMarkup.eval.metric;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.model.Element;
import semanticMarkup.log.LogLevel;



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
