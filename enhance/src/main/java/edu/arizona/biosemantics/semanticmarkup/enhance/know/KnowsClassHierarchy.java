package edu.arizona.biosemantics.semanticmarkup.enhance.know;

import java.util.Set;

public interface KnowsClassHierarchy {

	public boolean isSuperclass(String superclass, String clazz);

	public boolean isSubclass(String subclass, String clazz);
	
	public Set<String> getSuperclasses(String clazz);
	
	public Set<String> isSubclasses(String clazz);
	
}
