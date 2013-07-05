package semanticMarkup.eval.matcher;

import java.util.Objects;

import semanticMarkup.eval.model.Element;
import semanticMarkup.log.LogLevel;

public abstract class AbstractMatcher {


	protected boolean valuesNullOrContainedEitherWay(String attributeName, String valueA, String valueB) {
		boolean result = (valueA == null && valueB == null) || 
				((valueA != null && valueB != null) && 	(valueA.contains(valueB) || valueB.contains(valueA)));
		if(!result) {
			log(LogLevel.DEBUG, attributeName + "'s fault");
		}
		return result;
	}
	
	protected boolean equalsOrNull(String attributeName, String valueA, String valueB) {
		boolean result = Objects.equals(valueA, valueB);
		
		if(!result) {
			log(LogLevel.DEBUG, attributeName + "'s fault");
		}
		return result;
	}
	
	protected boolean areNotNull(String attributeName, Element elementA, Element elementB) {
		boolean result = elementA != null && elementB != null;
		
		if(!result) {
			log(LogLevel.DEBUG, attributeName + "'s fault");
		}
		return result;
	}

}
