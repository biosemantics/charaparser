package edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Distribution;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.DistributionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.distribution.model.Treatment;

public class DistributionTransformer implements IDistributionTransformer {
	
	static ArrayList<Pattern> USStates = new ArrayList<Pattern> ();
	static ArrayList<Pattern> CAProvinces = new ArrayList<Pattern> ();
	static{
        USStates.add(Pattern.compile("^Alabama\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Ala\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Alaska\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Ariz\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Arizona\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Ark\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Arkansas\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Calif\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^California\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Colo\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Colorado\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Conn\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Connecticut\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Del\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Delaware\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^D\\s?\\.?\\s?C\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^District of Columbia\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Fla\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Florida\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Ga\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Georgia\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Idaho\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Illinois\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Ill\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Indiana\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Ind\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Iowa\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Kansas\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Kans\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Kentucky\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Ky\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Louisiana\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^La\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Maine\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Maryland\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Md\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Massachusetts\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Mass\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Michigan\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Mich\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Minnesota\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Minn\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Mississippi\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Miss\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Missouri\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Mo\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Montana\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Mont\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Nebraska\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Nebr\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Nevada\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Nev\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^New Hampshire\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^N\\s?\\.?\\s?H\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^New Jersey\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^N\\s?\\.?\\s?J\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^New Mexico\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^N\\s?\\.?\\s?Mex\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^New York\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^N\\s?\\.?\\s?Y\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^North Carolina\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^N\\s?\\.?\\s?C\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^North Dakota\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^N\\s?\\.?\\s?Dak\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Ohio\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Oklahoma\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Okla\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Oregon\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Oreg\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Pennsylvania\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Pa\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Rhode Island\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^R\\s?\\.?\\s?I\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^South Carolina\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^S\\s?\\.?\\s?C\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^South Dakota\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^S\\s?\\.?\\s?Dak\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Tennessee\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Tenn\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Texas\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Tex\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Utah\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Vermont\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Vt\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Virginia\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Va\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Washington\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Wash\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^West Virginia\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^W\\s?\\.?\\s?Va\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Wisconsin\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Wis\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Wyoming\\.?$", Pattern.CASE_INSENSITIVE));
		USStates.add(Pattern.compile("^Wyo\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		
		CAProvinces.add(Pattern.compile("^Alberta\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Alta\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^British Columbia\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^B\\s?\\.?\\s?C\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Manitoba\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Man\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^New Brunswick\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^N\\s?\\.?\\s?B\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Newfoundland and Labrador\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Nfld\\s?\\.? and Labr\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Nfld\\s?\\.? and Labr\\s?\\.?\\s?\\(Nfld\\s?\\.?\\s?\\)$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Nfld\\s?\\.? and Labr\\s?\\.?\\s?\\(Labr\\s?\\.?\\s?\\)$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Northwest Territories\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^N\\s?\\.?\\s?W\\s?\\.?\\s?T\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Nova Scotia\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^N\\s?\\.?\\s?S\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Nunavut\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Ontario\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Ont\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Prince Edward Island\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^P\\s?\\.?\\s?E\\s?\\.?\\s?I\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Quebec\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Que\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Saskatchewan\\.?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Sask\\s?\\.?\\s?$", Pattern.CASE_INSENSITIVE));
		CAProvinces.add(Pattern.compile("^Yukon\\.?$", Pattern.CASE_INSENSITIVE));
		
	}
	
	/*static Hashtable<String, String> FNAcoverage = new Hashtable<String, String> (); //Alta. => US
		
	static{ //based on FNA contributor's guide
		
		FNAcoverage.put("Alabama", "US");
		FNAcoverage.put("Ala.", "US");
		FNAcoverage.put("Alaska", "US");
		FNAcoverage.put("Ariz.", "US");
		FNAcoverage.put("Arizona", "US");
		FNAcoverage.put("Ark.", "US");
		FNAcoverage.put("Arkansas", "US");
		FNAcoverage.put("Calif.", "US");
		FNAcoverage.put("California", "US");
		FNAcoverage.put("Colo.", "US");
		FNAcoverage.put("Colorado", "US");
		FNAcoverage.put("Conn.", "US");
		FNAcoverage.put("Connecticut", "US");
		FNAcoverage.put("Del.", "US");
		FNAcoverage.put("Delaware", "US");
		FNAcoverage.put("D.C.", "US");
		FNAcoverage.put("District of Columbia", "US");
		FNAcoverage.put("Fla.", "US");
		FNAcoverage.put("Florida", "US");
		FNAcoverage.put("Ga.", "US");
		FNAcoverage.put("Georgia", "US");
		FNAcoverage.put("Idaho", "US");
		FNAcoverage.put("Illinois", "US");
		FNAcoverage.put("Ill.", "US");
		FNAcoverage.put("Indiana", "US");
		FNAcoverage.put("Ind.", "US");
		FNAcoverage.put("Iowa", "US");
		FNAcoverage.put("Kansas", "US");
		FNAcoverage.put("Kans.", "US");
		FNAcoverage.put("Kentucky", "US");
		FNAcoverage.put("Ky.", "US");
		FNAcoverage.put("Louisiana", "US");
		FNAcoverage.put("La.", "US");
		FNAcoverage.put("Maine", "US");
		FNAcoverage.put("Maryland", "US");
		FNAcoverage.put("Md.", "US");
		FNAcoverage.put("Massachusetts", "US");
		FNAcoverage.put("Mass.", "US");
		FNAcoverage.put("Michigan", "US");
		FNAcoverage.put("Mich.", "US");
		FNAcoverage.put("Minnesota", "US");
		FNAcoverage.put("Minn.", "US");
		FNAcoverage.put("Mississippi", "US");
		FNAcoverage.put("Miss.", "US");
		FNAcoverage.put("Missouri", "US");
		FNAcoverage.put("Mo.", "US");
		FNAcoverage.put("Montana", "US");
		FNAcoverage.put("Mont.", "US");
		FNAcoverage.put("Nebraska", "US");
		FNAcoverage.put("Nebr.", "US");
		FNAcoverage.put("Nevada", "US");
		FNAcoverage.put("Nev.", "US");
		FNAcoverage.put("New Hampshire", "US");
		FNAcoverage.put("N.H.", "US");
		FNAcoverage.put("New Jersey", "US");
		FNAcoverage.put("N.J.", "US");
		FNAcoverage.put("New Mexico", "US");
		FNAcoverage.put("N.Mex.", "US");
		FNAcoverage.put("New York", "US");
		FNAcoverage.put("N.Y.", "US");
		FNAcoverage.put("North Carolina", "US");
		FNAcoverage.put("N.C.", "US");
		FNAcoverage.put("North Dakota", "US");
		FNAcoverage.put("N.Dak.", "US");
		FNAcoverage.put("Ohio", "US");
		FNAcoverage.put("Oklahoma", "US");
		FNAcoverage.put("Okla.", "US");
		FNAcoverage.put("Oregon", "US");
		FNAcoverage.put("Oreg.", "US");
		FNAcoverage.put("Pennsylvania", "US");
		FNAcoverage.put("Pa.", "US");
		FNAcoverage.put("Rhode Island", "US");
		FNAcoverage.put("R.I.", "US");
		FNAcoverage.put("South Carolina", "US");
		FNAcoverage.put("S.C.", "US");
		FNAcoverage.put("South Dakota", "US");
		FNAcoverage.put("S.Dak.", "US");
		FNAcoverage.put("Tennessee", "US");
		FNAcoverage.put("Tenn.", "US");
		FNAcoverage.put("Texas", "US");
		FNAcoverage.put("Tex.", "US");
		FNAcoverage.put("Utah", "US");
		FNAcoverage.put("Vermont", "US");
		FNAcoverage.put("Vt.", "US");
		FNAcoverage.put("Virginia", "US");
		FNAcoverage.put("Va.", "US");
		FNAcoverage.put("Washington", "US");
		FNAcoverage.put("Wash.", "US");
		FNAcoverage.put("West Virginia", "US");
		FNAcoverage.put("W.Va.", "US");
		FNAcoverage.put("Wisconsin", "US");
		FNAcoverage.put("Wis.", "US");
		FNAcoverage.put("Wyoming", "US");
		FNAcoverage.put("Wyo.", "US");
		FNAcoverage.put("Alberta", "Canada");
		FNAcoverage.put("Alta.", "Canada");
		FNAcoverage.put("British Columbia", "Canada");
		FNAcoverage.put("B.C.", "Canada");
		FNAcoverage.put("Manitoba", "Canada");
		FNAcoverage.put("Man.", "Canada");
		FNAcoverage.put("New Brunswick", "Canada");
		FNAcoverage.put("N.B.", "Canada");
		FNAcoverage.put("Newfoundland and Labrador", "Canada");
		FNAcoverage.put("Nfld. and Labr.", "Canada");
		FNAcoverage.put("Nfld. and Labr. (Nfld.)", "Canada");
		FNAcoverage.put("Northwest Territories", "Canada");
		FNAcoverage.put("N.W.T.", "Canada");
		FNAcoverage.put("Nova Scotia", "Canada");
		FNAcoverage.put("N.S.", "Canada");
		FNAcoverage.put("Nunavut", "Canada");
		FNAcoverage.put("Ontario", "Canada");
		FNAcoverage.put("Ont.", "Canada");
		FNAcoverage.put("Prince Edward Island", "Canada");
		FNAcoverage.put("P.E.I.", "Canada");
		FNAcoverage.put("Quebec", "Canada");
		FNAcoverage.put("Que.", "Canada");
		FNAcoverage.put("Saskatchewan", "Canada");
		FNAcoverage.put("Sask.", "Canada");
		FNAcoverage.put("Yukon", "Canada");
	}*/

	@Override
	public void transform(List<DistributionsFile> distributionsFiles) {
		for(DistributionsFile distributionsFile : distributionsFiles) {
			int i = 0;
			int organId = 0;
			for(Treatment treatment : distributionsFile.getTreatments()) {
				for(Distribution distribution : treatment.getDistributions()) {
					List<Statement> statements = new LinkedList<Statement>();
					Statement statement = new Statement();
					statement.setId("distribution_" + i++);
					statement.setText(distribution.getText());
					
					BiologicalEntity be = new BiologicalEntity();
					be.setName("whole_organism");
					be.setId("dis_o"+organId++);
					be.setType("structure");
					be.setNameOriginal("");
					be.addCharacters(parse(distribution.getText()));
					//statement.setValues();
					statement.addBiologicalEntity(be);
					
					statements.add(statement);				
					distribution.setStatements(statements);
				}
			}
		}
	}

	
	/**
	 *<description type=“distribution”>
      <statement id=“X”>
		<text>introduced; Alta., B.C., Yukon; Ala., Alaska; Eurasia.</text>
       	<biological_entity name=”whole_organism”>
		 <character name=“distribution” establishment_means=”introduced” value=“Alta”/>	
 		 <character name=“distribution” establishment_means=”introduced” value=“B.C.”/>	
         <character name=“distribution” establishment_means=”introduced” value=”Yukon.”/> 
		 <character name=“distribution” establishment_means=”introduced” value=“Ala.”/>
         <character name=“distribution” establishment_means=”introduced” value=“Alaska”/>	  
         <character name=”distribution” establishment_means=”native” value=”Eurasia>
        </biological_entity>
     </statement>
     </description>

	 *<description type=“distribution”>
      <statement id=“X”>
		<text>Alta., B.C., Yukon; Ala., Alaska; Introduced in Eurasia.</text>
       	<biological_entity name=”whole_organism”>
		 <character name=“distribution” establishment_means=”native” value=“Alta”/>	
 		 <character name=“distribution” establishment_means=”native” value=“B.C.”/>	
         <character name=“distribution” establishment_means=”native” value=”Yukon.”/> 
		 <character name=“distribution” establishment_means=”native” value=“Ala.”/>
         <character name=“distribution” establishment_means=”native” value=“Alaska”/>	  
         <character name=”distribution” establishment_means=”introduced” value=”Eurasia>
        </biological_entity>
     </statement>
     </description>
      
      Answers provided by Richard Rabeler <rabeler@umich.edu>:
      Here are a few examples taken from v 9.
	  1. North America, Eurasia, n Africa; introduced in Pacific Islands (New Zealand), Australia.
      Do we have the certainty that North America, Eurasia, n Africa are Native?
	  --> Yes 
	
	  2. introduced; St. Pierre and Miquelon; N.B., Nfld. and Labr. (Nfld.), N.S., Ont., P.E.I., Que.; Colo., Conn., Ill., Ind., Maine, Mass., Mich., Minn., N.H., N.J., N.Y., Ohio, Pa., Vt., W.Va., Wis.; Eurasia.
	  --> this is the most common style of FNA distribution statement. The plant is introduced in North America and native (see that semi-colon after "Wis"?) to Eurasia.
	
	  3. North America, Mexico, West Indies, Central America, South America, Eurasia, Africa, Pacific Islands (Hawaii, New Zealand), Australia; introduced widely.
		what should we do with introduced widely?  
	  --> This one suggests that the plant is native everywhere and introduced widely as well - which doesn't make a lot of sense. I might query the author (and JZ) on that one.
	
	  4. introduced; Ont.; Iowa, Ky., Mich., N.Y., Ohio, Pa.; Europe; w, c Asia (to w China); introduced also in South America (Argentina).
	  --> introduced in North America, native in Europe and w,c Asia (to w China), also introduced to S. America.
	  
	  Other examples
	  pantropical, of both humid and arid regions, with few temperate outliers in Europe, Asia, and North America.
	 */
	
	
	@Override
	public LinkedHashSet<Character> parse(String text) {
		log(LogLevel.INFO, "parsing distribution: "+text); //TODO:this message should pass to the user.
		//format text, replace [,;] in parentheses with @
		LinkedHashSet<Character> values = new LinkedHashSet<Character>();
		/*if(text.contains("Mexico(Nuevo")){
			log(LogLevel.INFO, "in distribution transformation, see text "+ text);
		}*/
		text = format(text); 
		//collect values
		
		String[] areas = text.split("\\s*;\\s*"); //split sentence by ;
		String NAestablishment = "native"; //North America areas
		String establishment = "";
		for(int i = 0; i<areas.length; i++){
			String area = areas[i].trim();
			if(area.matches("introduced;")){
				NAestablishment = "introduced"; //for all FNAcoverage locations
			}else if(area.startsWith("introduced")){
				establishment = "introduced"; //area-specific
			}else{
				establishment = "native"; 
			}
			
			String[] locations = getLocations(area); 
			for(String location: locations){
				if(location.indexOf("@")>=0){
					values.addAll(allValues(location, NAestablishment, establishment));
				}else{
					Character c = new Character();
					c.setName("distribution");
					String coverage = NACoverage(location.replaceAll("\\b[senwc]{0,2}\\b", "").replaceAll("\\(\\?\\)", "").trim()); //s Florida, Alta.(?)
					if(coverage != null){ //in US or Canada
						c.setValue(location+","+coverage);
						log(LogLevel.INFO, "distribution: "+location +","+coverage); 
						c.setEstablishedMeans(NAestablishment);
					}else{
						location = location.trim().replaceFirst("[,.]+$", "");
						log(LogLevel.INFO, "distribution: "+location + "[outside of US and Canada]"); 
						c.setValue(location);
						c.setEstablishedMeans(establishment);
					}
					values.add(c);
				}
			}
		}
		return values;
	}
	
	
	/**
	 *  
	 split on "," works for lists of US and Canada states/provinces
	 Iowa, Ky., Mich., N.Y., Ohio, Pa.; Europe; introduced also in South America (Argentina).
	 
	 but not for
	 pantropical, of both humid and arid regions, with few temperate outliers in Europe, Asia, and North America.
	 w, c Asia (to w China);
	 
	 
	 * @param area
	 * @return
	 */
	private String[] getLocations(String area) {
		//try split first then check the results
		List<String> locations = new LinkedList<String>(Arrays.asList(area.split("\\s*,\\s*"))); //St. Pierre and Miquelon is one place
		boolean split = true;
		Iterator<String> it = locations.iterator();
		Hashtable<Integer, List<String>> newOnes = new Hashtable<Integer, List<String>>();
		//int index = 0;
		while(it.hasNext()){
			String location = it.next();
			if(! location.matches("^([senwc]{0,2}| and )* ?[A-Z].*")){
			//if(! location.matches("^[senwc]{0,2} ?[A-Z].*")) { //se Asia
				split = false;
				break;
			}
			//split other 'and' but not the one in Newfoundland and Labrador
			/*if(!(location.contains("Newfoundland and Labrador") || location.matches("^Nfld\\s?\\.? and Labr\\s?\\.?\\s?\\(?.*?\\)?$")) && location.matches(".*\\sand\\s.*?")){
				it.remove();
				newOnes.put(index, Arrays.asList(location.split("\\s*and\\s*")));
			}*/
			//index++;
		}
		
		/*Enumeration<Integer> en = newOnes.keys();
		while(en.hasMoreElements()){
			int idx = en.nextElement();
			locations.addAll(idx, newOnes.get(idx));
		}*/
		
		if(split) return locations.toArray(new String[locations.size()]);
		else return new String[]{area};
	}


	/**
	 * 
	 * @param location
	 * @return US or Canada or null
	 */
	private String NACoverage(String location) {
		for(Pattern p: USStates){
			Matcher m = p.matcher(location);
			if(m.matches()) return "United States";
		}
		for(Pattern p: CAProvinces){
			Matcher m = p.matcher(location);
			if(m.matches()) return "Canada";
		}
		return null;
	}


	/**
	 * extract values from a segment containing @
	 * @param area
	 * @return
	 */
	private LinkedHashSet<Character> allValues(String area,  String NAestablishment, String establishment) {
		LinkedHashSet<Character> values = new LinkedHashSet<Character>();
		  Pattern p = Pattern.compile("(.*?)\\(([^)]*?@[^)]*?)\\)(.*)");
		  Matcher m = p.matcher(area);
		  if(m.matches()){
			   String com = m.group(1);
			   String partstr = m.group(2);
			   String rest = m.group(3);
			   rest = rest.matches("\\p{Punct}")? "" : rest;
			   
			   
			   String[] parts = partstr.split("\\s*@\\s*");
			   
			   for(int i = 0; i<parts.length; i++){
				   Character c = new Character();
					c.setName("distribution");
					String coverage = NACoverage(parts[i]);
					if(coverage != null){ //US, Canada, or Mexico
						c.setEstablishedMeans(NAestablishment);
					}else{
						c.setEstablishedMeans(establishment);
					}
					c.setValue(com+"("+parts[i]+")"+rest);
					log(LogLevel.INFO, "distribution: "+com+"("+parts[i]+")"+rest); 
					values.add(c);
				   }
			  }
		  return values;
		 }

	 public static String format(String text) {
		  String formated = "";
		  Pattern p = Pattern.compile("(.*?)(\\([^)]*,[^)]*\\))(.*)"); 
		  Matcher m = p.matcher(text);
		  while(m.matches()){
			   formated += m.group(1);
			   String t = m.group(2);
			   text = m.group(3);
			   t = t.replaceAll(",", "@");
			   formated +=t;
			   m = p.matcher(text);
			  }
		  formated +=text;
		  return formated;
		 }

}

