package semanticMarkup.markupElement.description.markup;

import java.util.LinkedList;
import java.util.List;

import semanticMarkup.markup.IMarkupResult;
import semanticMarkup.markup.IMarkupResultVisitor;
import semanticMarkup.markupElement.description.eval.model.Description;
import semanticMarkup.markupElement.description.model.DescriptionsFile;
import semanticMarkup.markupElement.description.model.DescriptionsFileList;
import semanticMarkup.markupElement.description.model.Relation;
import semanticMarkup.markupElement.description.model.Statement;
import semanticMarkup.markupElement.description.model.Structure;

public class DescriptionMarkupResult implements IMarkupResult {

	private List<Description> descriptions;

	public DescriptionMarkupResult(List<Description> descriptions) {
		this.descriptions = descriptions;
	}
	
	public DescriptionMarkupResult(DescriptionsFileList descriptionsFileList) {
		this.descriptions = new LinkedList<Description>();
		for(DescriptionsFile descriptionsFile : descriptionsFileList.getDescriptionsFiles()) {
			for(semanticMarkup.markupElement.description.model.Description description : descriptionsFile.getDescriptions()) {
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
