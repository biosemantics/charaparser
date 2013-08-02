package semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SingularPluralPair implements Comparable<SingularPluralPair>{	
	private String singular;
	private String plural;

	public SingularPluralPair() {
		singular = null;
		plural = null;
	}
	
	public SingularPluralPair(String s, String p) {
		this.singular = s;
		this.plural = p;
	}
	
	public String getPlural() {
		return this.plural;
	}
	
	public String getSingular() {
		return this.singular;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
			.append(this.singular)
			.append(this.plural)
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
		
		SingularPluralPair mySingularPluralPair = (SingularPluralPair) obj;
		
		return ((this.singular.equals(mySingularPluralPair.getSingular())) 
				&&(this.plural.equals(mySingularPluralPair.getPlural())));
		
	}

	@Override
	public int compareTo(SingularPluralPair spp) {		
		String SingularAndPluralA = this.singular + this.plural;
		String SingularAndPluralB = spp.getSingular() + spp.getPlural();
		
		return SingularAndPluralA.compareTo(SingularAndPluralB);
	}

}
