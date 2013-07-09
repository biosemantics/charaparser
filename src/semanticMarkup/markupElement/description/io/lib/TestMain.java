package semanticMarkup.markupElement.description.io.lib;

import java.util.LinkedList;
import java.util.List;

import semanticMarkup.markupElement.description.io.IDescriptionReader;
import semanticMarkup.markupElement.description.io.IDescriptionWriter;
import semanticMarkup.markupElement.description.model.Character;
import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.DescriptionsFile;
import semanticMarkup.markupElement.description.model.DescriptionsFileList;
import semanticMarkup.markupElement.description.model.Meta;
import semanticMarkup.markupElement.description.model.Relation;
import semanticMarkup.markupElement.description.model.Statement;
import semanticMarkup.markupElement.description.model.Structure;
import semanticMarkup.markupElement.description.model.TreatmentRoot;

public class TestMain {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {		
		MOXyBinderDescriptionReaderWriter readerWriter = new MOXyBinderDescriptionReaderWriter("resources/io/bindings/description-bindings.xml");
		DescriptionsFileList descriptionsFileList = readerWriter.read("input");
		
		for(DescriptionsFile file : descriptionsFileList.getDescriptionsFiles()) {
			for(TreatmentRoot treatmentRoot : file.getTreatmentRoots()) {
				for(Description description : treatmentRoot.getDescriptions()) {
					System.out.println(description.getText());
				}
			}
		}
		TreatmentRoot treatmentRoot = descriptionsFileList.getDescriptionsFiles().get(0).getTreatmentRoots().get(0);
		List<Description> oldDescriptions = treatmentRoot.getDescriptions();
		
		
		Description oldDescription = oldDescriptions.get(0);
        Description newDescription = new Description();
        
        // description.setText("");
        Meta meta = new Meta();
        meta.setCharaparserVersion("0.1");
        meta.setGlossaryType("Plant");
        meta.setGlossaryVersion("0.2");
        
        Statement s1 = new Statement();
        s1.setText("some text");
        Statement s2 = new Statement();
        s2.setText("some other text");
        Structure st1 = new Structure();
        st1.setName("s1");
        Structure st2 = new Structure();
        st2.setName("st2");
        Relation r1 = new Relation();
        r1.setName("r1");
        Character c1 = new Character();
        c1.setName("c1");
        c1.setValue("value");
        
        s1.addRelation(r1);
        s1.addStructure(st1);
        s1.addStructure(st2);
        st1.addCharacter(c1);
        
        newDescription.addStatement(s1);
        newDescription.addStatement(s2);
        
        List<Description> newDescriptions = new LinkedList<Description>(oldDescriptions);
        newDescriptions.remove(0);
        newDescriptions.add(0, newDescription);
        treatmentRoot.setDescriptions(newDescriptions);

		
		readerWriter.write(descriptionsFileList, "output"); 
	}
}
