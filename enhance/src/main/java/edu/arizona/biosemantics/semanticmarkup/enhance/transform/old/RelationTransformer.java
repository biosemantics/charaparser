package edu.arizona.biosemantics.semanticmarkup.enhance.transform.old;

public class RelationTransformer {

	/*private void createMayBeSameRelations(List<Element> result, ProcessingContext processingContext) {
	HashMap<String, Set<String>> names = new HashMap<String, Set<String>>();
	for (Element element : result) {
		if (element.isStructure()) {
			Structure structure = (Structure)element;
			String name = structure.getName();

			//if (element.containsAttribute("constraintType"))
			//	name = element.getCongetAttribute("constraintType") + " " + name;
			//if (element.containsAttribute("constraintParentOrgan"))
			//	name = element.getAttribute("constraintParentOrgan") + " " + name;
			//if (element.containsAttribute("constraint"))
			//	name = element.getAttribute("constraint") + " " + name;

			if (structure.getConstraint() != null && !structure.getConstraint().isEmpty())
				name = structure.getConstraint() + " " + name;

			String id = structure.getId();
			if(!names.containsKey(name)) 
				names.put(name, new HashSet<String>());
			names.get(name).add(id);
		}
	}

	for(Entry<String, Set<String>> nameEntry : names.entrySet()) {
		Set<String> ids = nameEntry.getValue();
		if(ids.size() > 1) {
			Iterator<String> idIterator = ids.iterator();
			while(idIterator.hasNext()) {
				String idA = idIterator.next();
				for(String idB : ids) {
					if(!idA.equals(idB)) {
						Relation relationElement = new Relation();
						relationElement.setName("may_be_the_same");
						relationElement.setFrom(idA);
						relationElement.setTo(idB);
						relationElement.setNegation(String.valueOf(false));
						relationElement.setToStructure(structure);
						relationElement.setFromStructure(structure);
						relationElement.setId("r" + String.valueOf(processingContext.fetchAndIncrementRelationId(relationElement)));	
					}
				}
				idIterator.remove();
			}
		}
	}
}*/
}
