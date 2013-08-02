package semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class GetNounsAfterPtnReturnValue {
	private List<String> nouns;
	private List<String> nounPtn;
	private String bWord;

	public GetNounsAfterPtnReturnValue(List<String> nouns, List<String> nounPtn, String bWord) {
		this.nouns = nouns;
		this.nounPtn = nounPtn;
		this.bWord = bWord;
	}
	
	public List<String> getNouns() {
		return this.nouns;
	}
	
	public List<String> getNounPtn() {
		return this.nounPtn;
	}
	
	public String getBoundaryWord(){
		return this.bWord;
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj==this){
			return true;
		}
		
		if (obj==null||obj.getClass()!=this.getClass()){
			return false;
		}
		
		GetNounsAfterPtnReturnValue myReturnValue = (GetNounsAfterPtnReturnValue) obj;
		
		return ((this.nouns.equals(myReturnValue.nouns))
				&& (this.nounPtn.equals(myReturnValue.nounPtn))
				&& (StringUtils.equals(this.bWord, myReturnValue.bWord))
				);
	}

}
