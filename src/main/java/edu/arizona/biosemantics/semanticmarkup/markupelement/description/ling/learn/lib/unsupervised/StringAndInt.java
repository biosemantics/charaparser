package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.unsupervised;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class StringAndInt {

	private String s;
	private int i;
	
	public StringAndInt(String s, int i) {
		this.s = s;
		this.i = i;
	}
	
	public String getString() {
		return s;
	}
	
	public int getInt() {
		return i;
	}
	
	@Override
	public int hashCode() {	
		return new HashCodeBuilder(17, 37)
				.append(s)
			    .append(i)
			    .toHashCode();

		
		
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj==this){
			return true;
		}
		
		if (obj==null||obj.getClass()!=this.getClass()){
			return false;
		}
		
		StringAndInt myStringAndInt = (StringAndInt) obj;
		
		return (   (StringUtils.equals(this.s, myStringAndInt.getString()))
				&& (this.i == myStringAndInt.getInt())
				);
	}
	
	@Override
	public String toString() {
		return String.format("(%s, %d)", this.s, this.i);
	}

}
