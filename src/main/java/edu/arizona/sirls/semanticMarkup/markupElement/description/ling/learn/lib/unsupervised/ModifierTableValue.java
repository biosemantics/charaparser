package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ModifierTableValue {

	private int count;
	private boolean isTypeModifier;

	public ModifierTableValue(int c, boolean isTM) {
		this.count = c;
		this.isTypeModifier = isTM;
	}

	public void setCount(int c) {
		this.count = c;
	}

	public int getCount() {
		return this.count;
	}

	public void setIsTypeModifier(boolean isTM) {
		this.isTypeModifier = isTM;
	}

	public boolean getIsTypeModifier() {
		return this.isTypeModifier;
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj==this){
			return true;
		}
		
		if (obj==null||obj.getClass()!=this.getClass()){
			return false;
		}
		
		ModifierTableValue myModifierTableValue = (ModifierTableValue) obj;
		
		return ((this.count == myModifierTableValue.getCount()) 
				&& (this.isTypeModifier == myModifierTableValue.getIsTypeModifier()));
		
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(13, 37)
			.append(this.count)
			.append(this.isTypeModifier)
			.toHashCode();
		}
	
}
