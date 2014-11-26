package edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResult;
import edu.arizona.biosemantics.semanticmarkup.markup.IMarkupResultVisitor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.eval.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFileList;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;


public class DescriptionMarkupResult implements IMarkupResult {

	private List<Description> descriptions;

	public DescriptionMarkupResult(List<Description> descriptions) {
		this.descriptions = descriptions;
	}
	
	public DescriptionMarkupResult(DescriptionsFileList descriptionsFileList) {
		this.descriptions = new LinkedList<Description>();
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
			for(edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description description : descriptionsFile.getDescriptions()) {
				Description newDescription = new Description();
				newDescription.setSource(descriptionsFile.getName());
				List<Relation> relations = new LinkedList<Relation>();
				List<BiologicalEntity> structures = new LinkedList<BiologicalEntity>();
				for(Statement statement : description.getStatements()) {
					relations.addAll(statement.getRelations());
					structures.addAll(statement.getBiologicalEntities());
				}
				newDescription.setRelations(relations);
				newDescription.setStructures(structures);
				descriptions.add(newDescription);
			}
		}
	}

	public List<Description> getResult() {
		return this.descriptions;
	}

	@Override
	public void accept(IMarkupResultVisitor markupResultVisitor) {
		markupResultVisitor.visit(this);
	}

}
