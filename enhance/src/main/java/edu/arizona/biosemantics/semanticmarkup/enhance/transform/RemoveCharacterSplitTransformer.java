package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.arizona.biosemantics.common.log.LogLevel;

public class RemoveCharacterSplitTransformer extends AbstractTransformer {

	@Override
	public void transform(Document document) {
		collapseSynonymOrgans();
		collapseSynonymQualities();
	}

	private void collapseSynonymQualities() {
		// TODO Auto-generated method stub
		
	}

	private void collapseSynonymOrgans() {
		/*for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			
			String name = biologicalEntity.getAttributeValue("name");
			if(name != null)
				name = name.trim();
			String constraint = biologicalEntity.getAttributeValue("constraint");
			if(constraint != null)
				constraint = constraint.trim();
			
			String searchTerm = name;
			if(constraint != null)
				searchTerm = constraint + " " + name;
			
			if(searchTerm != null) {
				Collection<String> iris = getIRIs(searchTerm);
				String value = StringUtils.join(iris, "; ");
				if(!iris.isEmpty())
					log(LogLevel.INFO, "Found IRIs: " + value + " for term " + searchTerm);
				biologicalEntity.setAttribute("ontologyid", value);
			}
		}*/
	}

}
