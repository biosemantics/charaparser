package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SentenceStructure {
	private int ID;
	private String source;
	private String sentence;
	private String originalSentence;
	private String lead;
	private String status;
	private String tag;
	private String modifier;
	private String type;

	public SentenceStructure(int id, String source, String sentence, String originalSentence, String lead, String status, String tag, String modifier, String type) {
		// TODO Auto-generated constructor stub
		this.ID = id;
		this.source=source;
		this.sentence=sentence;
		this.originalSentence=originalSentence;
		this.lead=lead;
		this.status=status;
		this.tag=tag;
		this.modifier=modifier;
		this.type=type;
	}
	
	public int getID() {
		return this.ID;
	}
	
	public String getSource() {
		return this.source;
	}
	
	public void setSource(String source) {
		this.source=source;
	}
	
	public String getSentence() {
		return this.sentence;
	}
	
	public void setSentence(String sentence) {
		this.sentence=sentence;
	}
	
	public String getOriginalSentence() {
		return this.originalSentence;
	}
	
	public void setOriginalSentence(String originalSentence) {
		this.originalSentence=originalSentence;
	}
	
	public String getLead() {
		return this.lead;
	}
	
	public void setLead(String lead) {
		this.lead=lead;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public void setStatus(String status) {
		this.status=status;
	}
	
	public String getTag() {
		return this.tag;
	}
	
	public void setTag(String tag) {
		this.tag=tag;
	}
	
	public String getModifier() {
		return this.modifier;
	}
	
	public void setModifier(String modifier) {
		this.modifier=modifier;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type=type;
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj==this){
			return true;
		}
		
		if (obj==null||obj.getClass()!=this.getClass()){
			return false;
		}
		
		SentenceStructure mySentence = (SentenceStructure) obj;
		
		return ((this.ID == mySentence.ID)
				&&(StringUtils.equals(this.source, mySentence.source))
				&&(StringUtils.equals(this.sentence, mySentence.sentence))
				&&(StringUtils.equals(this.originalSentence, mySentence.originalSentence))
				&&(StringUtils.equals(this.lead, mySentence.lead))
				&&(StringUtils.equals(this.status, mySentence.status))
				&&(StringUtils.equals(this.tag, mySentence.tag))
				&&(StringUtils.equals(this.modifier, mySentence.modifier))
				&&(StringUtils.equals(this.type, mySentence.type))
				);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(13, 37)
		.append(this.ID)
		.append(this.source)
		.append(this.sentence)
		.append(this.originalSentence)
		.append(this.lead)
		.append(this.status)
		.append(this.tag)
		.append(this.modifier)
		.append(this.type)
		.toHashCode();
	}
	
	@Override
	public String toString() {
		String sentenceString = "\n"
				+ "Sentence ID: " + this.getID() + "\n"
				+ "\tSource: " + this.getSource() + "\n"
				+ "\tSentence: " + this.getSentence() + "\n" 
				+ "\tOriginal Sentence: " + this.getSentence() + "\n"
				+ "\tLead: " + this.getLead() + "\n"
				+ "\tStatus: " + this.getStatus() + "\n" 
				+ "\tTag: " + this.getTag() + "\n" 
				+ "\tModifier: " + this.getModifier() + "\n"
				+ "\tType: " + this.getType() + "\n";

		return sentenceString;
	}
	
}
