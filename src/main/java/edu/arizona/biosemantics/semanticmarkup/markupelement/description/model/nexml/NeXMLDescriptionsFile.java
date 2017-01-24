//package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.nexml;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
//import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
//
//
//public class NeXMLDescriptionsFile extends AbstractDescriptionsFile {
//
//	private List<StateDescription> stateDescriptions = new LinkedList<StateDescription>();
//	private List<CharacterDescription> characterDescriptions = new LinkedList<CharacterDescription>();
//	
//	@Override
//	public List<Description> getDescriptions() {
//		List<Description> result = new LinkedList<Description>();
//		result.addAll(stateDescriptions);
//		result.addAll(characterDescriptions);
//		return result;
//	}
//
//	public List<StateDescription> getStateDescriptions() {
//		return stateDescriptions;
//	}
//
//	public void setStateDescriptions(List<StateDescription> stateDescriptions) {
//		this.stateDescriptions = stateDescriptions;
//	}
//
//	public List<CharacterDescription> getCharacterDescriptions() {
//		return characterDescriptions;
//	}
//
//	public void setCharacterDescriptions(
//			List<CharacterDescription> characterDescriptions) {
//		this.characterDescriptions = characterDescriptions;
//	}
//
//}
//
