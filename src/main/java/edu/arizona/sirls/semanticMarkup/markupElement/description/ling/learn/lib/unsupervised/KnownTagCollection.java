package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import java.util.Set;

public class KnownTagCollection {
	
	public Set<String> nouns;
	public Set<String> os;
	public Set<String> modifiers;
	public Set<String> boundaryWords;
	public Set<String> boundaryMarks;
	public Set<String> properNouns;
	
	public KnownTagCollection(Set<String> nouns, Set<String> os,
			Set<String> modifiers, Set<String> boundaryWords,
			Set<String> boundaryMarks, Set<String> properNouns) {
		this.nouns = nouns;
		this.os = os;
		this.modifiers = modifiers;
		this.boundaryWords = boundaryWords;
		this.boundaryMarks = boundaryMarks;
		this.properNouns = properNouns;
	}

}
