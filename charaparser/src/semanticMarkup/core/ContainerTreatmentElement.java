package semanticMarkup.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

public class ContainerTreatmentElement extends TreatmentElement implements Iterable<TreatmentElement> {

	//retains the order of elements.. TODO could use LinkedHashMap
	@XmlElement(name="attribute")
	protected List<TreatmentElement> children = new LinkedList<TreatmentElement>();

	//allows efficient access of same named children
	protected Map<String, List<TreatmentElement>> namedChildren = 
			new HashMap<String, List<TreatmentElement>>();

	// for JAXB
	public ContainerTreatmentElement() { }
	
	public ContainerTreatmentElement(String name) {
		super(name);
	}
	
	public void addTreatmentElement(TreatmentElement treatmentElement) {
		children.add(treatmentElement);
		if(!namedChildren.containsKey(treatmentElement.getName()))
			namedChildren.put(treatmentElement.getName(), new LinkedList<TreatmentElement>());
		namedChildren.get(treatmentElement.getName()).add(treatmentElement);
	}
	
	public void addTreatmentElements(List<TreatmentElement> treatmentElements) {
		for(TreatmentElement treatmentElement : treatmentElements) 
			this.addTreatmentElement(treatmentElement);
	}
	
	public void removeTreatmentElement(String elementName) {
		List<TreatmentElement> child = namedChildren.get(elementName);
		children.remove(child);
	}
	
	public void removeTreatmentElementRecursively(TreatmentElement treatmentElement) {
		if(this.contains(treatmentElement))
			this.removeTreatmentElement(treatmentElement);
		for(TreatmentElement child : children) {
			if(child instanceof ContainerTreatmentElement) {
				ContainerTreatmentElement containerChild = (ContainerTreatmentElement)child;
				containerChild.removeTreatmentElementRecursively(treatmentElement);
			}
		}
	}
	
	public void removeTreatmentElement(TreatmentElement treatmentElement) {
		children.remove(treatmentElement);
		namedChildren.get(treatmentElement.getName()).remove(treatmentElement);
	}
	
	public List<TreatmentElement> getTreatmentElements() {
		return this.children;
	}
	
	@JsonIgnore
	public List<ValueTreatmentElement> getValueTreatmentElements() {
		List<ValueTreatmentElement> result = 
				new LinkedList<ValueTreatmentElement>();
		for(TreatmentElement element : children) {
			if(element instanceof ValueTreatmentElement)
				result.add((ValueTreatmentElement)element);
		}
		return result;
	}
	
	@JsonIgnore
	public List<ContainerTreatmentElement> getContainerTreatmentElements() {
		List<ContainerTreatmentElement> result = 
				new LinkedList<ContainerTreatmentElement>();
		for(TreatmentElement element : children) {
			if(element instanceof ContainerTreatmentElement)
				result.add((ContainerTreatmentElement)element);
		}
		return result;
	}
	
	public boolean contains(TreatmentElement treatmentElement) {
		return children.contains(treatmentElement);
	}
	
	public boolean containsAsDescendant(TreatmentElement treatmentElement) {
		return this.getParent(treatmentElement) != null;
	}
	
	public TreatmentElement getParent(TreatmentElement treatmentElement) {
		if(contains(treatmentElement))
			return this;
		else
			for(TreatmentElement child : children) {
				if(child instanceof ContainerTreatmentElement) {
					ContainerTreatmentElement containerChild = (ContainerTreatmentElement)child;
					TreatmentElement parent = containerChild.getParent(treatmentElement);
					if(parent!=null)
						return parent;
				}
			}
		return null;
	}
	
	public boolean containsTreatmentElement(String elementName) {
		return this.namedChildren.containsKey(elementName);
	}
	
	public boolean containsValueTreatmentElement(String elementName) {
		if(!containsTreatmentElement(elementName))
			return false;
		if(getValueTreatmentElements(elementName).size()==0) 
			return false;
		return true;
	}
	
	public boolean containsContainerTreatmentElement(String elementName) {
		if(!containsTreatmentElement(elementName))
			return false;
		if(getContainerTreatmentElements(elementName).size()==0) 
			return false;
		return true;
	}
	
	@JsonIgnore
	public Set<String> getTreatmentElementNames() {
		return this.namedChildren.keySet();
	}
	
	public List<TreatmentElement> getTreatmentElements(String elementName) {
		if(namedChildren.containsKey(elementName))
			return this.namedChildren.get(elementName);
		else
			return new LinkedList<TreatmentElement>();
	}
	
	public TreatmentElement getTreatmentElement(String elementName) {
		if(namedChildren.containsKey(elementName))
			return this.namedChildren.get(elementName).get(0);
		else
			return null;
	}
	
	public List<ValueTreatmentElement> getValueTreatmentElements(String elementName) {
		List<ValueTreatmentElement> result = new LinkedList<ValueTreatmentElement>();
		if(namedChildren.containsKey(elementName)) 
			for(TreatmentElement element : namedChildren.get(elementName)) {
				if(element instanceof ValueTreatmentElement) {
					ValueTreatmentElement valueTreatmentElement = 
							(ValueTreatmentElement)element;
					result.add(valueTreatmentElement);
				}
			}
		return result;
	}
	
	public ValueTreatmentElement getValueTreatmentElement(String elementName) {
		if(namedChildren.containsKey(elementName)) {
			for(TreatmentElement element : namedChildren.get(elementName)) {
				if(element instanceof ValueTreatmentElement) {
					return (ValueTreatmentElement)element;
				}
			}
		}
		return null;
	}
	
	public List<ContainerTreatmentElement> getContainerTreatmentElements(String elementName) {
		List<ContainerTreatmentElement> result = 
				new LinkedList<ContainerTreatmentElement>();
		if(namedChildren.containsKey(elementName))
			for(TreatmentElement element : namedChildren.get(elementName)) {
				if(element instanceof ContainerTreatmentElement) {
					ContainerTreatmentElement containerTreatmentElement = 
							(ContainerTreatmentElement)element;
					result.add(containerTreatmentElement);
				}
			}
		return result;
	}
	
	public ContainerTreatmentElement getContainerTreatmentElement(String elementName) {
		if(namedChildren.containsKey(elementName)) {
			for(TreatmentElement element : namedChildren.get(elementName)) {
				if(element instanceof ContainerTreatmentElement) {
					return (ContainerTreatmentElement)element;
				}
			}
		}
		return null;
	}

	@JsonIgnore
	public List<ValueTreatmentElement> getValueTreatmentElementsRecursively() {
		List<ValueTreatmentElement> result = this.getValueTreatmentElements();
		List<ContainerTreatmentElement> containerElements = this.getContainerTreatmentElements();
		for(ContainerTreatmentElement element : containerElements) {
			result.addAll(element.getValueTreatmentElementsRecursively());
		}
		return result;
	}
	
	
	public List<ValueTreatmentElement> getValueTreatmentElementsRecursively(String elementName) {
		List<ValueTreatmentElement> result = 
				this.getValueTreatmentElements(elementName);
		List<ContainerTreatmentElement> containerElements = 
				this.getContainerTreatmentElements(elementName);
		for(ContainerTreatmentElement element : containerElements) {
			result.addAll(element.getValueTreatmentElementsRecursively(elementName));
		}
		return result;
	}
	
	@Override 
	public Object clone() {
		ContainerTreatmentElement clone = (ContainerTreatmentElement)super.clone();
		clone.children = new LinkedList<TreatmentElement>();
		clone.namedChildren = new HashMap<String, List<TreatmentElement>>();
		for(TreatmentElement child : this.children) {
			TreatmentElement childClone = (TreatmentElement)child.clone();
			clone.addTreatmentElement(childClone);
		}
		return clone;
	}
	
	@Override
	public String toString() {
		try {
			ObjectMapper mapper = new ObjectMapper();
		    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		    return writer.writeValueAsString(this);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public Iterator<TreatmentElement> iterator() {
		return children.iterator();
	}
}
