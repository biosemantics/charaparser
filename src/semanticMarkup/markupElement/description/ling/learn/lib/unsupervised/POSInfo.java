package semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class POSInfo implements Comparable<POSInfo>{

	private String word;
	private String POS;
	private String role;
	private int certaintyU;
	private int certaintyL;

	public POSInfo(String w, String p, String r, int cU, int cL) {
		this.word = w;
		this.POS = p;
		this.role = r;
		this.certaintyU = cU;
		this.certaintyL = cL;
	}
	
	public String getWord() {
		return this.word;
	}

	public String getPOS() {
		return this.POS;
	}

	public String getRole() {
		return this.role;
	}

	public int getCertaintyU() {
		return this.certaintyU;
	}

	public int getCertaintyL() {
		return this.certaintyL;
	}

	public int compareTo(POSInfo b) {		
		// aCU    bCU
		// --- =  ---
		// aCL    bCL
        // 
		// aCU * bCL = bCU*aCL

		int aCU = this.certaintyU;
		int aCL = this.certaintyL;

		int bCU = b.getCertaintyU();
		int bCL = b.getCertaintyL();

		if (aCU * bCL < bCU * aCL) {
			return -1;
		} else if (aCU * bCL == bCU * aCL) {
			return 0;
		} else {
			return 1;
		}
	}
	
	@Override
	public int hashCode() {	
		return new HashCodeBuilder(13, 31)
			.append(this.word)
			.append(this.POS)
			.append(this.role)
			.append(this.certaintyU)
			.append(this.certaintyL)
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
		
		POSInfo myPOSInfo = (POSInfo) obj;
		
		return (   (StringUtils.equals(this.word, myPOSInfo.getWord()))
				&& (StringUtils.equals(this.POS, myPOSInfo.getPOS()))
				&& (StringUtils.equals(this.role, myPOSInfo.getRole()))
				&& (this.certaintyU == myPOSInfo.getCertaintyU())
				&& (this.certaintyL == myPOSInfo.getCertaintyL())
				);
				
	}
	
	@Override
	public String toString(){
		return String.format("[%s, %s, %s, %d, %d]", this.word, this.POS, this.role, this.certaintyU, this.certaintyU);
	}
}
