package semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DiscountedKey {
	
	private String word;
	private String pos;;

	public DiscountedKey(String w, String p) {
		// TODO Auto-generated constructor stub
		this.word=w;
		this.pos=p;
	}
	
	public String getWord(){
		return this.word;
	}
	
	public String getPOS(){
		return this.pos;
	}
	
	//public boolean equals(DiscountedKey dKey) {
	//	return ((this.word.equals(dKey.getWord())) 
	//			&& (this.pos.equals(dKey.getPOS())));
	//}
	
    @Override
	public boolean equals(Object obj){
		if (obj==this){
			return true;
		}
		
		if (obj==null||obj.getClass()!=this.getClass()){
			return false;
		}
		
		DiscountedKey myDiscountedKey = (DiscountedKey) obj;
		
		return ((this.word.equals(myDiscountedKey.getWord())) 
				&& (this.pos.equals(myDiscountedKey.getPOS())));
	}

    @Override
	public int hashCode() {
        return new HashCodeBuilder(19, 31).
        	       append(this.word).
        	       append(this.pos).
        	       toHashCode();
	}
	

}
