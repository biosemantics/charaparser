package edu.arizona.sirls.semanticMarkup.markupElement.description.markup;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.sirls.semanticMarkup.markup.IMarkupResult;
import edu.arizona.sirls.semanticMarkup.markup.IMarkupResultVisitor;
import edu.arizona.sirls.semanticMarkup.markupElement.description.eval.model.Description;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.DescriptionsFile;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.DescriptionsFileList;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Relation;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Statement;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Structure;


public class DescriptionMarkupResult implements IMarkupResult {

	private List<Description> descriptions;

	public DescriptionMarkupResult(List<Description> descriptions) {
		this.descriptions = descriptions;
	}
	
	public DescriptionMarkupResult(DescriptionsFileList descriptionsFileList) {
		this.descriptions = new LinkedList<Description>();
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
			for(edu.arizona.sirls.semanticMarkup.markupElement.description.model.Description description : descriptionsFile.getDescriptions()) {
				Description newDescription = new Description();
				newDescription.setSource(descriptionsFile.getName());
				List<Relation> relations = new LinkedList<Relation>();
				List<Structure> structures = new LinkedList<Structure>();
				for(Statement statement : description.getStatements()) {
					relations.addAll(statement.getRelations());
					structures.addAll(statement.getStructures());
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
