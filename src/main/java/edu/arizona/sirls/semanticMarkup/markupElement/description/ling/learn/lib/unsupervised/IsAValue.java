package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class IsAValue {
	
	private String instance;
	private String cls;

	public IsAValue(String i, String c) {
		this.instance = i;
		this.cls = c;
	}
	
	public String getInstance() {
		return this.instance;
	}
	
	public String getCls() {
		return this.cls;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 37)
			.append(this.instance)
			.append(this.cls)
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
		
		IsAValue myIsAValue = (IsAValue) obj;
		
		return ((StringUtils.equals(this.instance, myIsAValue.getInstance())) 
				&& (this.cls == myIsAValue.getCls()));
	}

}
