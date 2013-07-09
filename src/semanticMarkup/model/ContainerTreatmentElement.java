package semanticMarkup.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A ContainerTreatmentElement contains a number of children TreatmentElements and are associated with a name
 * @author rodenhausen
 */
public class ContainerTreatmentElement extends TreatmentElement implements Iterable<TreatmentElement> {

	//retains the order of elements.. TODO could use LinkedHashMap
	@XmlElement(name="attribute")
	protected List<TreatmentElement> children = new LinkedList<TreatmentElement>();

	//allows efficient access of same named children
	protected Map<String, List<TreatmentElement>> namedChildren = 
			new HashMap<String, List<TreatmentElement>>();

	/**
	 * JAXB needs a non-argument constructor
	 */
	public ContainerTreatmentElement() { }
	
	/**
	 * @param name
	 */
	public ContainerTreatmentElement(String name) {
		super(name);
	}
	
	/**
	 * @param treatmentElement to add to children
	 */
	public void addTreatmentElement(TreatmentElement treatmentElement) {
		children.add(treatmentElement);
		if(!namedChildren.containsKey(treatmentElement.getName()))
			namedChildren.put(treatmentElement.getName(), new LinkedList<TreatmentElement>());
		namedChildren.get(treatmentElement.getName()).add(treatmentElement);
	}
	
	/**
	 * @param treatmentElements to add to children
	 */
	public void addTreatmentElements(List<TreatmentElement> treatmentElements) {
		for(TreatmentElement treatmentElement : treatmentElements) 
			this.addTreatmentElement(treatmentElement);
	}
	
	/**
	 * @param elementName to remove
	 */
	public void removeTreatmentElement(String elementName) {
		List<TreatmentElement> child = namedChildren.get(elementName);
		children.remove(child);
	}
	
	/**
	 * @param treatmentElement to remove in any descendants
	 */
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
	
	/**
	 * @param treatmentElement to remove from children
	 */
	public void removeTreatmentElement(TreatmentElement treatmentElement) {
		children.remove(treatmentElement);
		namedChildren.get(treatmentElement.getName()).remove(treatmentElement);
	}
	
	/**
	 * @return the children treatmentElements
	 */
	public List<TreatmentElement> getTreatmentElements() {
		return this.children;
	}
	
	/**
	 * @return the children of type ValueTreatmentElement
	 */
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
	
	/**
	 * @return the children of type ContainerTreatmentElement
	 */
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
	
	/**
	 * @param treatmentElement
	 * @return if the treatmentElement is contained in the children
	 */
	public boolean contains(TreatmentElement treatmentElement) {
		return children.contains(treatmentElement);
	}
	
	/**
	 * @param treatmentElement
	 * @return if the treatmentElement is contained in any of the descendants
	 */
	public boolean containsAsDescendant(TreatmentElement treatmentElement) {
		return this.getParent(treatmentElement) != null;
	}
	
	/**
	 * @param treatmentElement
	 * @return the parent of treatmentElement, assuming it is contained in any of this descendants else null
	 */
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
	
	/**
	 * @param elementName
	 * @return if a treatmenetElement with elementName is contained in the children
	 */
	public boolean containsTreatmentElement(String elementName) {
		return this.namedChildren.containsKey(elementName);
	}
	
	/**
	 * @param elementName
	 * @return if a ValueTreatmentElement of elementName is contained in the children 
	 */
	public boolean containsValueTreatmentElement(String elementName) {
		if(!containsTreatmentElement(elementName))
			return false;
		if(getValueTreatmentElements(elementName).size()==0) 
			return false;
		return true;
	}
	
	/**
	 * @param elementName
	 * @return if a ContainerTreatmentElemetn of elementName is contained in the children
	 */
	public boolean containsContainerTreatmentElement(String elementName) {
		if(!containsTreatmentElement(elementName))
			return false;
		if(getContainerTreatmentElements(elementName).size()==0) 
			return false;
		return true;
	}
	
	/**
	 * @return the names of the children treatment elements
	 */
	@JsonIgnore
	public Set<String> getTreatmentElementNames() {
		return this.namedChildren.keySet();
	}
	
	/**
	 * @param elementName
	 * @return the treatmenetElements of elementName
	 */
	public List<TreatmentElement> getTreatmentElements(String elementName) {
		if(namedChildren.containsKey(elementName))
			return this.namedChildren.get(elementName);
		else
			return new LinkedList<TreatmentElement>();
	}
	
	/**
	 * @param elementName
	 * @return the first treatmentElement of elementName
	 */
	public TreatmentElement getTreatmentElement(String elementName) {
		if(namedChildren.containsKey(elementName))
			return this.namedChildren.get(elementName).get(0);
		else
			return null;
	}
	
	/**
	 * @param elementName
	 * @return the treatmenetElements of elementName and type ValueTreatmenetElement
	 */
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
	
	/**
	 * @param elementName
	 * @return the first treatmenetElements of elementName and type ValueTreatmenetElement
	 */
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
	
	/**
	 * @param elementName
	 * @return the treatmenetElements of elementName and type ContainerTreatmentElement
	 */
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
	
	/**
	 * @param elementName
	 * @return the first treatmenetElements of elementName and type ContainerTreatmentElement
	 */
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

	/**
	 * @return the first treatmenetElements of elementName and type ContainerTreatmentElement
	 */
	@JsonIgnore
	public List<ValueTreatmentElement> getValueTreatmentElementsRecursively() {
		List<ValueTreatmentElement> result = this.getValueTreatmentElements();
		List<ContainerTreatmentElement> containerElements = this.getContainerTreatmentElements();
		for(ContainerTreatmentElement element : containerElements) {
			result.addAll(element.getValueTreatmentElementsRecursively());
		}
		return result;
	}
	
	/**
	 * @param elementName
	 * @return the treatmenetElements of elementName and type ValueTreatmenetElement in any of the descendants
	 */
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
	public Iterator<TreatmentElement> iterator() {
		return children.iterator();
	}
}
