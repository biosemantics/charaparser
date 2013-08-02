package semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class StringPair {
	private String s1;
	private String s2;

	public StringPair(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
	}
	
	public String getHead() {
		return this.s1;
	}
	
	public String getTail() {
		return this.s2;
	}

	@Override
	public boolean equals(Object obj){
		if (obj==this){
			return true;
		}
		
		if (obj==null||obj.getClass()!=this.getClass()){
			return false;
		}
		
		StringPair myStringPair = (StringPair) obj;
		
		boolean case1 = StringUtils.equals(this.s1, myStringPair.getHead());
		boolean case2 = StringUtils.equals(this.s2, myStringPair.getTail());
		
		return (case1 && case2);	
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(19, 29)
			.append(this.s1)
			.append(this.s2)
			.toHashCode();
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s]", this.s1, this.s2);
	}
	
}
