package edu.arizona.biosemantics.semanticmarkup.config;

import java.io.IOException;

import  edu.arizona.biosemantics.semanticmarkup.config.taxongroup.PlantConfig;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.markup.DescriptionMarkupCreator;

public class FNAConfig extends PlantConfig {

	public FNAConfig() throws IOException {
		super();
		
		this.setDescriptionMarkupCreator(DescriptionMarkupCreator.class);
	}

}
