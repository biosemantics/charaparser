package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;

public class NumericalPhraseParser {

	String units = null;
	public NumericalPhraseParser(@Named("Units")String units ) {
		this.units = units;

	}

	/**
	 * cases that are not handled well:
	[AREA: [(4-5[-6]×)3-5[-7]], mm]
	[3-12+[-15+], mm]
	[(5-)10-30+[-80], cm]
	[[5-7]8-12[13-16]]
	20 – 100 + [ – 200 + ]
	[[0-](3-)5(-8)[-15]]
	[1(-2-4)]
	AREA: [(4-14(-16)×)4-25+ mm]
	VALUE: [5.5-(6-8), mm]
	 *
	 *
	 * Other examples styles 2[10] mm diam.
	 *
	 * @param numberexp :(1-)5-300+ ; (1-2-) 4
	 * @param suggestedcharaname
	 * @return: characters marked up in XML format &lt;character name="" value=""&gt;
	 */

	public List<Character> parseNumericals(String numberexp, String suggestedcharaname) {
		LinkedList<Character> innertagstate = new LinkedList<Character>();
		try{
			numberexp = numberexp.replaceAll("–", "-");
			int i,j;
			numberexp = numberexp.replaceAll("\\([\\s]?|\\[[\\s]?", "[");
			numberexp = numberexp.replaceAll("[\\s]?\\)|[\\s]?\\]", "]").trim();
			String original = numberexp;

			//4-5[+] => 4-5[-5+]
			Pattern p1 = Pattern.compile("(.*?\\b(\\d+))\\s*\\[\\+\\](.*)");
			Matcher m = p1.matcher(numberexp);
			if(m.matches()){
				numberexp = m.group(1)+"[-"+m.group(2)+"+]"+m.group(3);
				m = p1.matcher(numberexp);
			}
			//1-[2-5] => 1-1[2-5] => 1[2-5]
			//1-[4-5] => 1-3[4-5]
			p1 = Pattern.compile("(.*?)(\\d+)-(\\[(\\d)-.*)");
			m = p1.matcher(numberexp);
			if(m.matches()){
				int n = Integer.parseInt(m.group(4))-1;
				if(n==Integer.parseInt(m.group(2))){
					numberexp = m.group(1)+n+m.group(3);
				}else{
					numberexp = m.group(1)+m.group(2)+"-"+n+m.group(3);
				}
			}

			///////////////////////////////////////////////////////////////////
			//      area                                               ////////


			//Pattern pattern19 = Pattern.compile("([ \\d\\.\\[\\]+-]+\\s*([cmdµu]?m?))\\s*[×x]?(\\s*[ \\d\\.\\[\\]+-]+\\s*([cmdµu]?m?))?\\s*[×x]\\s*([ \\d\\.\\[\\]+-]+\\s*([cmdµu]?m))");
			Pattern pattern19 = Pattern.compile("([ \\d\\.\\[\\]+-]+\\s*("+units+"?))\\s*[×x]?(\\s*[ \\d\\.\\[\\]+-]+\\s*("+units+"?))?\\s*[×x]\\s*([ \\d\\.\\[\\]+-]+\\s*("+units+")\\b)");
			Matcher matcher2 = pattern19.matcher(numberexp);
			if(matcher2.matches()){
				//get l, w, and h
				String width = "";
				String height = "";
				String lunit = "";
				String wunit = "";
				String hunit = "";
				String length = matcher2.group(1).trim();
				String g5 = matcher2.group(5).trim();
				if(matcher2.group(3)==null){
					width = g5;
				}else{
					width = matcher2.group(3);
					height = g5;
				}
				//make sure each has a unit
				if(height.length()==0){//2 dimensions
					wunit = matcher2.group(6);
					if(matcher2.group(2)==null || matcher2.group(2).trim().length()==0){
						lunit = wunit;
					}else{
						lunit = matcher2.group(2);
					}
				}else{//3 dimensions
					hunit = matcher2.group(6);
					if(matcher2.group(4)==null || matcher2.group(4).trim().length()==0){
						wunit = hunit;
					}else{
						wunit = matcher2.group(4);
					}
					if(matcher2.group(2)==null || matcher2.group(2).trim().length()==0){
						lunit = wunit;
					}else{
						lunit = matcher2.group(2);
					}
				}
				//format expression value+unit
				//length = length.matches(".*[cmdµu]?m$")? length : length + " "+lunit;
				//width = width.matches(".*[cmdµu]?m$")? width : width + " "+wunit;
				//if(height.length()>0) height = height.matches(".*[cmdµu]?m$")? height : height + " "+hunit;
				length = length.matches(".*\\b"+units+"$")? length : length + " "+lunit;
				width = width.matches(".*\\b"+units+"$")? width : width + " "+wunit;
				if(height.length()>0) height = height.matches(".*\\b"+units+"$")? height : height + " "+hunit;


				//annotation
				annotateSize(length, innertagstate, "length");
				annotateSize(width, innertagstate, "width");
				if(height.length()>0) annotateSize(height, innertagstate, "height");

				numberexp = matcher2.replaceAll("#");
				matcher2.reset();
			}

			////////////////////////////////////////////////////////////////////////////////////
			//   ratio                                                              ////////////
			/*Pattern pattern24 = Pattern.compile("l/w[\\s]?=[/\\d\\.\\s\\+\\–\\-]+");
		matcher2 = pattern24.matcher(numberexp);
		while ( matcher2.find()){
			if(numberexp.charAt(matcher2.start())==' '){
				i=matcher2.start()+1;
			}
			else{
				i=matcher2.start();
			}
			j=matcher2.end();
			String match = numberexp.substring(i, j);
			int en = match.indexOf('-');
			if(en>=0){
				//range
				if (match.contains("+")){
					Character character = new Character();
					character.setCharType("range_value");
					character.setName("l_w_ratio");
					//character.setAttribute("from", match.substring(match.indexOf('=')+2,en).trim());
					character.setFrom(match.substring(match.indexOf('=')+1,en).trim());
					character.setTo(match.substring(en+1, match.indexOf('+',en+1)).trim());
					character.setUpperRestricted("false");
					innertagstate.add(character);
					//innertagstate=innertagstate.concat("<character char_type=\"range_value\" name=\"l_w_ratio\" from=\""+match.substring(match.indexOf('=')+2,en).trim()+"\" to=\""+match.substring(en+1, match.indexOf('+',en+1)).trim()+"\" upper_restricted=\"false\"/>");
				}else{
					Character character = new Character();
					character.setCharType("range_value");
					character.setName("l_w_ratio");
					//character.setAttribute("from", match.substring(match.indexOf('=')+2,en).trim());
					character.setFrom(match.substring(match.indexOf('=')+1,en).trim());
					character.setTo(match.substring(en+1, match.indexOf(' ',en+1)).trim());
					innertagstate.add(character);
					//innertagstate=innertagstate.concat("<character char_type=\"range_value\" name=\"l_w_ratio\" from=\""+match.substring(match.indexOf('=')+2,en).trim()+"\" to=\""+match.substring(en+1, match.indexOf(' ',en+1)).trim()+"\"/>");
				}
			}else{
				Character character = new Character();
				character.setName("l_w_ratio");
				//character.setAttribute("from", match.substring(match.indexOf('=')+2,en).trim());
				character.setValue(match.substring(match.indexOf('=')+1).trim());
				innertagstate.add(character);
			}
		}
		numberexp = matcher2.replaceAll("#");
		matcher2.reset();
			 */
			/////////////////////////////////////////////////////////////////////////////////////////////////////////
			// size: deal with  "[5-]10-15[-20] cm", not deal with "5 cm - 10 cm"                        ////////////
			//int sizect = 0;
			String toval;
			String fromval;
			suggestedcharaname = suggestedcharaname==null || suggestedcharaname.length()==0? suggestedcharaname = "some_measurement" : suggestedcharaname;
			numberexp = annotateSize(numberexp, innertagstate, suggestedcharaname);





			////////////////////////////////////////////////////////////////////////////////////////////
			//   relative size                                                                             /////
			Pattern pattern14 = Pattern.compile("[±\\d\\[\\]\\–\\-\\./\\s]+[\\s]?[\\–\\-]?(% of [\\w]+ length|height of [\\w]+|times as [\\w]+ as [\\w]+|total length|their length|(times)?[\\s]?length of [\\w]+)");
			matcher2 = pattern14.matcher(numberexp);
			toval="";
			fromval="";
			while ( matcher2.find()){
				if(numberexp.charAt(matcher2.start())==' '){
					i=matcher2.start()+1;
				}
				else{
					i=matcher2.start();
				}
				j=matcher2.end();
				String extreme = numberexp.substring(i,j);
				i = 0;
				j = extreme.length();
				Pattern pattern20 = Pattern.compile("\\[[±\\d\\.\\s\\+]+[\\–\\-]{1}[±\\d\\.\\s\\+\\–\\-]*\\]");
				Matcher matcher1 = pattern20.matcher(extreme);
				if ( matcher1.find()){
					int p = matcher1.start();
					int q = matcher1.end();
					if(extreme.charAt(q-2)=='–' | extreme.charAt(q-2)=='-'){
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_size");
						character.setFrom(extreme.substring(p+1,q-2).trim());
						character.setTo("");
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" to=\"\"/>");
					}else{
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_size");
						character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
						character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
					}
				}
				extreme = matcher1.replaceAll("#");
				matcher1.reset();
				if(extreme.contains("#"))
					i = extreme.indexOf("#")+1;
				Pattern pattern21 = Pattern.compile("\\[[±\\d\\.\\s\\+\\–\\-]*[\\–\\-]{1}[±\\d\\.\\s\\+]+\\]");
				matcher1 = pattern21.matcher(extreme);
				if ( matcher1.find()){
					int p = matcher1.start();
					int q = matcher1.end();
					if (extreme.charAt(p+1)=='–' | extreme.charAt(p+1)=='-'){
						if (extreme.charAt(q-2)=='+'){
							Character character = new Character();
							character.setCharType("range_value");
							character.setName("atypical_size");
							character.setFrom("");
							character.setTo(extreme.substring(p+2,q-2).trim());
							character.setUpperRestricted("false");
							innertagstate.add(character);
							//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-2).trim()+"\" upper_restricted=\"false\"/>");
						}else{
							Character character = new Character();
							character.setCharType("range_value");
							character.setName("atypical_size");
							character.setFrom("");
							character.setTo(extreme.substring(p+2,q-1).trim());
							innertagstate.add(character);
							//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-1).trim()+"\"/>");
						}
					}
					else{
						if (extreme.charAt(q-2)=='+'){
							Character character = new Character();
							character.setCharType("range_value");
							character.setName("atypical_size");
							character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
							character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim());
							character.setUpperRestricted("false");
							innertagstate.add(character);
							//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
						}else{
							Character character = new Character();
							character.setCharType("range_value");
							character.setName("atypical_size");
							character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
							character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim() );
							//character.setAttribute("upper_restricted", "true");
							innertagstate.add(character);
							//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");

						}
					}
				}
				extreme = matcher1.replaceAll("#");
				matcher1.reset();
				j = extreme.length();
				Pattern pattern23 = Pattern.compile("\\[[±\\d\\.\\s\\+]+\\]");
				matcher1 = pattern23.matcher(extreme);
				if ( matcher1.find()){
					int p = matcher1.start();
					int q = matcher1.end();
					if (extreme.charAt(q-2)=='+'){
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_size");
						character.setFrom(extreme.substring(p+1,q-2).trim());
						character.setUpperRestricted("false");
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"relative_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
					}else{
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_size");
						character.setValue(extreme.substring(p+1,q-1).trim());
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"relative_value\" name=\"atypical_size\" value=\""+extreme.substring(p+1,q-1).trim()+"\"/>");
					}
				}
				extreme = matcher1.replaceAll("#");
				matcher1.reset();
				j = extreme.length();
				if(extreme.substring(i,j).contains("–")|extreme.substring(i,j).contains("-") && !extreme.substring(i,j).contains("×") && !extreme.substring(i,j).contains("x") && !extreme.substring(i,j).contains("X")){
					String extract = extreme.substring(i,j);
					Pattern pattern18 = Pattern.compile("[\\s]?[\\–\\-]?(% of [\\w]+ length|height of [\\w]+|times as [\\w]+ as [\\w]+|total length|their length|(times)?[\\s]?length of [\\w]+)");
					Matcher matcher3 = pattern18.matcher(extract);
					String relative="";
					if ( matcher3.find()){
						relative = extract.substring(matcher3.start(), matcher3.end());
					}
					extract = matcher3.replaceAll("#");
					matcher3.reset();

					Character character = new Character();
					character.setCharType("range_value");
					character.setName("some_measurement");
					character.setFrom(extract.substring(0, extract.indexOf('-')).trim());
					character.setTo(extract.substring(extract.indexOf('-')+1,extract.indexOf('#')).trim());
					//character.setRelativeConstraint("relative_constraint",relative.trim());
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character char_type=\"relative_range_value\" name=\"size\" from=\""+extract.substring(0, extract.indexOf('-')).trim()+"\" to=\""+extract.substring(extract.indexOf('-')+1,extract.indexOf('#')).trim()+"\" relative_constraint=\""+relative.trim()+"\"/>");
					toval = extract.substring(0, extract.indexOf('-'));
					fromval = extract.substring(extract.indexOf('-')+1,extract.indexOf('#'));
					//sizect+=1;
				}
				else{
					String extract = extreme.substring(i,j);
					Pattern pattern18 = Pattern.compile("[\\s]?[\\–\\-]?(% of [\\w]+ length|height of [\\w]+|times as [\\w]+ as [\\w]+|total length|their length|(times)?[\\s]?length of [\\w]+)");
					Matcher matcher3 = pattern18.matcher(extract);
					String relative="";
					if ( matcher3.find()){
						relative = extract.substring(matcher3.start(), matcher3.end());
					}
					extract = matcher3.replaceAll("#");
					matcher3.reset();
					Character character = new Character();
					character.setCharType("range_value");
					character.setName("some_measurement");
					character.setValue(extract.substring(0,extract.indexOf('#')).trim());
					//character.setRelativeConstraint("relative_constraint", relative.trim());
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character char_type=\"relative_value\" name=\"size\" value=\""+extract.substring(0,extract.indexOf('#')).trim()+"\" relative_constraint=\""+relative.trim()+"\"/>");
					toval = extract.substring(0,extract.indexOf('#'));
					fromval = extract.substring(0,extract.indexOf('#'));
				}

				for(Character character : innertagstate) {
					if(character.getTo() != null && character.getTo().isEmpty()){
						if(toval.endsWith("+")){
							toval = toval.replaceFirst("\\+$", "");
							character.setUpperRestricted("false");
							character.setCharType("range_value");
						}
						character.setTo(toval.trim());
						character.setToInclusive("false");
					}
					if(character.getFrom() != null && character.getFrom().isEmpty()){
						character.setFrom(fromval.trim());
						character.setFromInclusive("false");
					}
				}

				/*StringBuffer sb = new StringBuffer();
			Pattern pattern25 = Pattern.compile("to=\"\"");
			matcher1 = pattern25.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb, "to=\""+toval.trim()+"\"");
			}
			matcher1.appendTail(sb);
			innertagstate=sb.toString();
			matcher1.reset();
			StringBuffer sb1 = new StringBuffer();
			Pattern pattern26 = Pattern.compile("from=\"\"");
			matcher1 = pattern26.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb1, "from=\""+fromval.trim()+"\"");
			}
			matcher1.appendTail(sb1);
			innertagstate=sb1.toString();
			matcher1.reset();*/
			}
			numberexp = matcher2.replaceAll("#");
			matcher2.reset();

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//   count                                                                                             ///////////////
			/*p1 = Pattern.compile("^\\[(\\d+)\\](.*)");
    	m = p1.matcher(numberexp);
    	if(m.matches()){
    		Element character = new Element("characterName");
			character.setAttribute("name", "atypical_"+(cname==null?"count": cname));
			character.setAttribute("value", m.group(1));
			innertagstate.add(character);
			numberexp = m.group(2).trim();
    	}


    	p1 = Pattern.compile("^\\[(\\d+)\\+\\](.*)");
    	m = p1.matcher(numberexp);
    	if(m.matches()){
    		Element character = new Element("characterName");
    		character.setAttribute("char_type", "range_value");
			character.setAttribute("name", "atypical_"+(cname==null?"count": cname));
			character.setAttribute("from", m.group(1));
			character.setAttribute("upper_restricted", "false");
			innertagstate.add(character);
			numberexp = m.group(2);
    	}*/


			//int countct = 0;
			String text = original;
			boolean iscount = false;
			Pattern pattern15 = Pattern.compile("([\\[]?[±]?[\\d]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d]+[+]?[\\]]?|[\\[]?[±]?[\\d]+[+]?[\\]]?[\\s]?)[\\–\\–\\-]+[a-zA-Z]+");
			matcher2 = pattern15.matcher(numberexp);
			numberexp = matcher2.replaceAll("#");
			matcher2.reset();
			//Pattern pattern16 = Pattern.compile("(?<!([/][\\s]?))([\\[]?[±]?[\\d]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d]+[+]?[\\]]?[\\s]?([\\[]?[\\–\\-]?[\\]]?[\\s]?[\\[]?[\\d]+[+]?[\\]]?)*|[±]?[\\d]+[+]?)(?!([\\s]?[n/]|[\\s]?[\\–\\-]?% of [\\w]+ length|[\\s]?[\\–\\-]?height of [\\w]+|[\\s]?[\\–\\-]?times|[\\s]?[\\–\\-]?total length|[\\s]?[\\–\\-]?their length|[\\s]?[\\–\\-]?(times)?[\\s]?length of|[\\s]?[dcmµ]?m))");
			//Pattern pattern16 = Pattern.compile("(?<!([/][\\s]?))([\\[]?[±]?[\\d\\./%]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?[\\s]?([\\[]?[\\–\\-]?[\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?)*|[±]?[\\d\\./%]+[+]?)(?!([\\s]?[n/]|[\\s]?[\\–\\-]?% of [\\w]+ length|[\\s]?[\\–\\-]?height of [\\w]+|[\\s]?[\\–\\-]?times|[\\s]?[\\–\\-]?total length|[\\s]?[\\–\\-]?their length|[\\s]?[\\–\\-]?(times)?[\\s]?length of|[\\s]?[dcmµ]?m))");
			//Pattern pattern16 = Pattern.compile("(?<!([/][\\s]?))([\\[]?[±]?[\\d\\./%]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?[\\s]?([\\[]?[\\–\\-]?[\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?)*|\\[?[±]?[\\d\\./%]+[+]?\\]?)(?!([\\s]?[n/]|[\\s]?[\\–\\-]?% of [\\w]+ length|[\\s]?[\\–\\-]?height of [\\w]+|[\\s]?[\\–\\-]?times|[\\s]?[\\–\\-]?total length|[\\s]?[\\–\\-]?their length|[\\s]?[\\–\\-]?(times)?[\\s]?length of|[\\s]?[dcmµu]?m))");
			Pattern pattern16 = Pattern.compile("(?<!([/][\\s]?))([\\[]?[±]?[\\d\\./%]+[\\]]?[\\s]?[\\[]?[\\–\\-][\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?[\\s]?([\\[]?[\\–\\-]?[\\]]?[\\s]?[\\[]?[\\d\\./%]+[+]?[\\]]?)*|\\[?[±]?[\\d\\./%]+[+]?\\]?)(?!([\\s]?[n/]|[\\s]?[\\–\\-]?% of [\\w]+ length|[\\s]?[\\–\\-]?height of [\\w]+|[\\s]?[\\–\\-]?times|[\\s]?[\\–\\-]?total length|[\\s]?[\\–\\-]?their length|[\\s]?[\\–\\-]?(times)?[\\s]?length of|[\\s]?\\b"+units+"\\b))");

			matcher2 = pattern16.matcher(numberexp);
			while ( matcher2.find()){
				iscount = true;
				i=matcher2.start();
				j=matcher2.end();
				String extreme = numberexp.substring(i,j);
				text = text.replace(extreme, "").trim();
				i = 0;
				j = extreme.length();
				Pattern pattern20 = Pattern.compile("\\[[±\\d\\.\\s\\+]+[\\–\\-]{1}[±\\d\\.\\s\\+\\–\\-]*\\]");
				Matcher matcher1 = pattern20.matcher(extreme);
				if ( matcher1.find()){
					int p = matcher1.start();
					int q = matcher1.end();
					if(extreme.charAt(q-2)=='–' | extreme.charAt(q-2)=='-'){
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_"+(suggestedcharaname==null?"count": suggestedcharaname));
						character.setFrom(extreme.substring(p+1,q-2).trim());
						character.setTo(""); //hong 9/13
						innertagstate.add(character);

						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,q-2).trim()+"\" to=\"\"/>");
					}else{
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_"+(suggestedcharaname==null?"count": suggestedcharaname));
						character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
						String tmp = extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim();
						character.setTo(tmp.replaceFirst("[^0-9]+$", ""));
						if(tmp.endsWith("+")){
							character.setUpperRestricted("false");
						}
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
					}
				}
				extreme = matcher1.replaceAll("#");
				matcher1.reset();
				if(extreme.contains("#"))
					i = extreme.indexOf("#")+1;
				j = extreme.length(); //process from # to the end of extreme. but in 1-[2-5] (1-#), the value is before #
				Pattern pattern21 = Pattern.compile("\\[[±\\d\\.\\s\\+\\–\\-]*[\\–\\-]{1}[±\\d\\.\\s\\+]+\\]");
				matcher1 = pattern21.matcher(extreme);
				if ( matcher1.find()){
					int p = matcher1.start();
					int q = matcher1.end();
					j = p;
					if (extreme.charAt(p+1)=='–' | extreme.charAt(p+1)=='-'){
						if (extreme.charAt(q-2)=='+'){
							Character character = new Character();
							character.setCharType("range_value");
							character.setName("atypical_"+(suggestedcharaname==null?"count": suggestedcharaname));
							character.setFrom("");
							character.setTo(extreme.substring(p+2,q-2).trim());
							character.setUpperRestricted("false");
							innertagstate.add(character);
							//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\"\" to=\""+extreme.substring(p+2,q-2).trim()+"\" upper_restricted=\"false\"/>");
						}else{
							Character character = new Character();
							character.setCharType("range_value");
							character.setName("atypical_"+(suggestedcharaname==null?"count": suggestedcharaname));
							character.setFrom("");
							character.setTo(extreme.substring(p+2,q-1).trim());
							innertagstate.add(character);
							//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\"\" to=\""+extreme.substring(p+2,q-1).trim()+"\"/>");
						}
					}
					else{
						if (extreme.charAt(q-2)=='+'){
							Character character = new Character();
							character.setCharType("range_value");
							character.setName("atypical_"+(suggestedcharaname==null?"count": suggestedcharaname));
							character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
							character.setTo(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
							character.setUpperRestricted("false");
							innertagstate.add(character);
							//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
						}else{
							Character character = new Character();
							character.setCharType("range_value");
							character.setName("atypical_"+(suggestedcharaname==null?"count": suggestedcharaname));
							character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
							character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
							innertagstate.add(character);
							//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_count\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
						}
					}

				}
				matcher1.reset();
				Pattern pattern23 = Pattern.compile("\\[[±\\d\\.\\s\\+]+\\]");
				matcher1 = pattern23.matcher(extreme);
				if ( matcher1.find()){
					int p = matcher1.start();
					int q = matcher1.end();
					j = p;
					if (extreme.charAt(q-2)=='+'){
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_"+(suggestedcharaname==null?"count": suggestedcharaname));
						character.setFrom(extreme.substring(p+1,q-2).trim());
						character.setUpperRestricted("false");
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character name=\"atypical_count\" from=\""+extreme.substring(p+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
					}else{
						Character character = new Character();
						character.setName("atypical_"+(suggestedcharaname==null?"count": suggestedcharaname));
						character.setValue(extreme.substring(p+1,q-1).trim());
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character name=\"atypical_count\" value=\""+extreme.substring(p+1,q-1).trim()+"\"/>");
					}
				}
				matcher1.reset();
				//# to the end
				String extract = extreme.substring(i,j);

				if(extract.contains("–")|extract.contains("-") && !extract.contains("×") && !extract.contains("x") && !extract.contains("X")){
					//String extract = extreme.substring(i,j);
					Pattern pattern22 = Pattern.compile("[\\[\\]]+");
					matcher1 = pattern22.matcher(extract);
					extract = matcher1.replaceAll("");
					matcher1.reset();

					String to = extract.substring(extract.indexOf('-')+1,extract.length()).trim();
					boolean upperrestricted = true;
					if(to.endsWith("+")){
						upperrestricted = false;
						to = to.replaceFirst("\\+$", "");
					}
					Character character = new Character();
					character.setCharType("range_value");
					character.setName(suggestedcharaname==null?"count": suggestedcharaname);
					character.setFrom(extract.substring(0, extract.indexOf('-')).trim());
					character.setTo(to);
					if(!upperrestricted)
						character.setUpperRestricted(upperrestricted+"");
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"count\" from=\""+extract.substring(0, extract.indexOf('-')).trim()+"\" to=\""+extract.substring(extract.indexOf('-')+1,extract.length()).trim()+"\"/>");
					toval = extract.substring(0, extract.indexOf('-'));
					fromval = extract.substring(extract.indexOf('-')+1,extract.length());
					//countct+=1;
				}else{
					//String extract = extreme.substring(i,j).trim();
					if(extract.length()>0){
						Character character = new Character();
						character.setName(suggestedcharaname==null?"count": suggestedcharaname);
						if(extract.endsWith("+")){
							extract = extract.replaceFirst("\\+$", "").trim();
							character.setCharType("range_value");
							character.setFrom(extract);
							character.setUpperRestricted("false");
						}else{
							character.setValue(extract);
						}
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character name=\"count\" value=\""+extract.trim()+"\"/>");
						toval = extract;
						fromval = extract;
					}
				}
				//start to #, duplicated above
				if(i-1>0){
					extract = extreme.substring(0, i-1);
					if(extract.contains("–")|extract.contains("-") && !extract.contains("×") && !extract.contains("x") && !extract.contains("X")){
						//String extract = extreme.substring(i,j);
						Pattern pattern22 = Pattern.compile("[\\[\\]]+");
						matcher1 = pattern22.matcher(extract);
						extract = matcher1.replaceAll("");
						matcher1.reset();

						String to = extract.substring(extract.indexOf('-')+1,extract.length()).trim();
						boolean upperrestricted = true;
						if(to.endsWith("+")){
							upperrestricted = false;
							to = to.replaceFirst("\\+$", "");
						}
						Character character = new Character();
						character.setCharType("range_value");
						character.setName(suggestedcharaname==null?"count": suggestedcharaname);
						character.setFrom(extract.substring(0, extract.indexOf('-')).trim());
						character.setTo(to);
						if(!upperrestricted)
							character.setUpperRestricted(upperrestricted+"");
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"count\" from=\""+extract.substring(0, extract.indexOf('-')).trim()+"\" to=\""+extract.substring(extract.indexOf('-')+1,extract.length()).trim()+"\"/>");
						toval = extract.substring(0, extract.indexOf('-'));
						fromval = extract.substring(extract.indexOf('-')+1,extract.length());
						//countct+=1;
					}else{
						//String extract = extreme.substring(i,j).trim();
						if(extract.length()>0){
							Character character = new Character();
							character.setName(suggestedcharaname==null?"count": suggestedcharaname);
							if(extract.endsWith("+")){
								extract = extract.replaceFirst("\\+$", "").trim();
								character.setCharType("range_value");
								character.setFrom(extract);
								character.setUpperRestricted("false");
							}else{
								character.setValue(extract);
							}
							innertagstate.add(character);
							//innertagstate = innertagstate.concat("<character name=\"count\" value=\""+extract.trim()+"\"/>");
							toval = extract;
							fromval = extract;
						}
					}
				}

				for(Character character : innertagstate) {
					if(character.getTo() != null && character.getTo().isEmpty()){
						if(toval.endsWith("+")){
							toval = toval.replaceFirst("\\+$", "");
							character.setUpperRestricted("false");

						}
						character.setTo(toval.trim());
						character.setToInclusive("false");
						character.setCharType("range_value");
					}
					if(character.getFrom() != null && character.getFrom().isEmpty()){
						character.setFrom(fromval.trim());
						character.setFromInclusive("false");
						character.setCharType("range_value");
					}
				}
				/*
    		StringBuffer sb = new StringBuffer();
			Pattern pattern25 = Pattern.compile("to=\"\"");
			matcher1 = pattern25.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb, "to=\""+toval.trim()+"\"");
			}
			matcher1.appendTail(sb);
			innertagstate=sb.toString();
			matcher1.reset();
			StringBuffer sb1 = new StringBuffer();
			Pattern pattern26 = Pattern.compile("from=\"\"");
			matcher1 = pattern26.matcher(innertagstate);
			while ( matcher1.find()){
				matcher1.appendReplacement(sb1, "from=\""+fromval.trim()+"\"");
			}
			matcher1.appendTail(sb1);
			innertagstate=sb1.toString();
			matcher1.reset();*/
			}
			matcher2.reset();
			if(iscount && text.length()>0 && text.matches(".*?\\w.*")){//puncts can not be units
				//add units to all counts. //Eh, why counts need to have units? TODO Hong
				for(Character character: innertagstate){
					//Iterator<Element> it = innertagstate.iterator();
					//while(it.hasNext()){
					//Element chara = it.next();
					//Attribute unittext = new Attribute("unit", text);
					character.setUnit(text);
				}
			}
			//}

			//"atypical" measure to "average" measure if atypical values are in the range of typical values
			refinement(innertagstate); //TODO hong make this interpretation configurable?
		}catch(Throwable e){
			log(LogLevel.ERROR, "parseNumericals throwable: "+e.toString());
			e.printStackTrace();
		}
		return innertagstate;
	}


	/**
	 *
	 * @param numberexp : styles 2[10] mm diam.
	 * @param cname:
	 * @return: characters marked up in XML format <character name="" value="">
	 */


	/**
	 * change
	 * &lt;character name="atypical_count" value="2" /&gt;
	   &lt;character char_type="range_value" name="count" from="1" to="3" /&gt;

		to
	  &lt;character name="average_count" value="2" /&gt;
	  &lt;character char_type="range_value" name="count" from="1" to="3" /&gt;
	 * @param innertagstate
	 */

	private void refinement(List<Character> innertagstate) throws Exception{

		//test if innertagstate match the profile:
		boolean match = false;
		String character = null;
		for(Character chara: innertagstate){
			if(chara.getName().startsWith("atypical")){
				character = chara.getName().replaceFirst("atypical_", "");
			}
			if(character!=null && chara.getName().compareTo(character)==0 && chara.getCharType()!=null && chara.getCharType().startsWith("range_value")){
				match = true;
			}
		}

		if(! match) return;

		try{
			float atypicalfrom = -1f;
			float atypicalto = -1f;

			for(Character chara: innertagstate){
				if(chara.getName().startsWith("atypical")){
					character = chara.getName().replaceFirst("atypical_", "");
					if(chara.getFrom()!=null){
						atypicalfrom = Float.parseFloat(chara.getFrom().replaceFirst("\\+$", "").replaceFirst("^0(?=[1-9])", "-")); //015 = -15
					}
					if(chara.getTo()!=null){
						atypicalto = Float.parseFloat(chara.getTo().replaceFirst("\\+$", "").replaceFirst("^0(?=[1-9])", "-"));
					}
					if(chara.getValue()!=null){
						atypicalfrom = Float.parseFloat(chara.getValue().replaceFirst("\\+$", "").replaceFirst("^0(?=[1-9])", "-"));
						atypicalto =   Float.parseFloat(chara.getValue().replaceFirst("\\+$", "").replaceFirst("^0(?=[1-9])", "-"));
					}

					float[] typical = getTypical(innertagstate, character);
					if(typical!=null){
						if(atypicalfrom >= typical[0] && atypicalto<= typical[1]){
							chara.setName("average_"+character);
						}
					}
				}

			}
		}catch(Throwable e){
			log(LogLevel.ERROR, "refinement for average value throwable: "+e.toString());
			e.printStackTrace();
		}

		/*for(Character chara: innertagstate){
			if(chara.getAttributeValue("name").startsWith("atypical")){
				String character = chara.getAttributeValue("name").replaceFirst("atypical_", "");
				if(chara.getAttribute("from")!=null){
					atypicalfrom = Float.parseFloat(chara.getAttributeValue("from"));
				}
				if(chara.getAttribute("to")!=null){
					atypicalto = Float.parseFloat(chara.getAttributeValue("to"));
				}
				if(chara.getAttribute("value")!=null){
					atypicalfrom = Float.parseFloat(chara.getAttributeValue("value"));
					atypicalto = Float.parseFloat(chara.getAttributeValue("value"));
				}

				float[] typical = getTypical(innertagstate, character);
				if(typical!=null){
					if(atypicalfrom >= typical[0] && atypicalto<= typical[1]){
						chara.setAttribute("name", "average_"+character);
					}
				}
			}

		}*/
	}

	private float[] getTypical(List<Character> innertagstate,
			String character) {
		try {
			for(Character chara: innertagstate){
				if(chara.getName().compareTo(character)==0){
					float[] result = new float[2];
					if(chara.getFrom()!=null){
						result[0] = Float.parseFloat(chara.getFrom());
					}
					if(chara.getTo()!=null){
						result[1]= Float.parseFloat(chara.getTo());
					}
					if(chara.getValue()!=null){
						result[0] = Float.parseFloat(chara.getValue());
						result[1] = Float.parseFloat(chara.getValue());
					}
					return result;
				}
			}
		} catch(Exception e) {
			log(LogLevel.ERROR, "Could not parse typical", e);
			return null;
		}
		return null;
	}


	protected String annotateSize(String plaincharset, List<Character> innertagstate, String chara) {
		int i;
		int j;
		Matcher matcher2;
		//Pattern pattern13 = Pattern.compile("[xX\\×±\\d\\[\\]\\–\\-\\.\\s\\+]+[\\s]?([dcmµu]?m)(?![\\w])(([\\s]diam)?([\\s]wide)?)");
		//Pattern pattern13 = Pattern.compile("[xX\\×±\\d\\[\\]\\–\\-\\.\\s\\+]+[\\s]?(\\b"+units+"\\b)(?![\\w])(([\\s]diam)?([\\s]wide)?)");
		Pattern pattern13 = Pattern.compile("[xX\\×±\\d\\[\\]\\–\\-/\\.\\s\\+]+[\\s]?(\\b"+units+"\\b)(?![\\w])(([\\s]diam)?([\\s]wide)?)");
		matcher2 = pattern13.matcher(plaincharset);
		String toval="";
		String fromval="";
		while ( matcher2.find()){
			String unit = matcher2.group(1);
			if(unit.matches(".*[2]$")) chara = "area";
			if(unit.matches(".*[3]$")) chara = "volume";
			if(plaincharset.charAt(matcher2.start())==' '){
				i=matcher2.start()+1;
			}
			else{
				i=matcher2.start();
			}
			j=matcher2.end();
			String extreme = plaincharset.substring(i,j);
			i = 0;
			j = extreme.length();
			Pattern pattern20 = Pattern.compile("\\[[±\\d\\./\\s\\+]+[\\–\\-]{1}[±\\d\\./\\s\\+\\–\\-]*\\]");
			Matcher matcher1 = pattern20.matcher(extreme);
			if ( matcher1.find()){
				int p = matcher1.start();
				int q = matcher1.end();
				if(extreme.charAt(q-2)=='–' | extreme.charAt(q-2)=='-'){
					Character character = new Character();
					character.setCharType("range_value");
					character.setName("atypical_"+chara);
					character.setFrom(extreme.substring(p+1,q-2).trim());
					character.setTo("");
					character.setFromUnit(unit);
					character.setToUnit(unit);
					//character.setAttribute("upper_restricted", "false");
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" to=\"\"/>");
				}else{
					Character character = new Character();
					character.setCharType("range_value");
					character.setName("atypical_"+chara);
					character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
					character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
					character.setFromUnit(unit);
					character.setToUnit(unit);
					//character.setAttribute("upper_restricted", "??");
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
				}
			}
			extreme = matcher1.replaceAll("#");
			matcher1.reset();
			if(extreme.contains("#"))
				i = extreme.indexOf("#")+1;
			Pattern pattern21 = Pattern.compile("\\[[±\\d\\./\\s\\+\\–\\-]*[\\–\\-]{1}[±\\d\\./\\s\\+]+\\]");
			matcher1 = pattern21.matcher(extreme);
			if ( matcher1.find()){
				int p = matcher1.start();
				int q = matcher1.end();
				if (extreme.charAt(p+1)=='–' | extreme.charAt(p+1)=='-'){
					if (extreme.charAt(q-2)=='+'){
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_"+chara);
						character.setFrom("");
						character.setTo(extreme.substring(p+2,q-2).trim());
						character.setFromUnit(unit);
						character.setToUnit(unit);
						character.setUpperRestricted("false");
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-2).trim()+"\" upper_restricted=\"false\"/>");
					}else{
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_"+chara);
						character.setFrom("");
						character.setTo(extreme.substring(p+2,q-1).trim());
						character.setFromUnit(unit);
						character.setToUnit(unit);
						//character.setAttribute("upper_restricted", "true");
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\"\" to=\""+extreme.substring(p+2,q-1).trim()+"\"/>");
					}
				}
				else{
					if (extreme.charAt(q-2)=='+'){
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_"+chara);
						character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
						character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim());
						character.setFromUnit(unit);
						character.setToUnit(unit);
						character.setUpperRestricted("false");
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
					}else{
						Character character = new Character();
						character.setCharType("range_value");
						character.setName("atypical_"+chara);
						character.setFrom(extreme.substring(p+1,extreme.indexOf("-",p+1)).trim());
						character.setTo(extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim());
						character.setFromUnit(unit);
						character.setToUnit(unit);
						//character.setAttribute("upper_restricted", "true");
						innertagstate.add(character);
						//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"atypical_size\" from=\""+extreme.substring(p+1,extreme.indexOf("-",p+1)).trim()+"\" to=\""+extreme.substring(extreme.indexOf("-",p+1)+1,q-1).trim()+"\"/>");
					}
				}
			}
			extreme = matcher1.replaceAll("#");
			matcher1.reset();
			j = extreme.length();
			Pattern pattern23 = Pattern.compile("\\[[±\\d\\./\\s\\+]+\\]");
			matcher1 = pattern23.matcher(extreme);
			if ( matcher1.find()){
				int p = matcher1.start();
				int q = matcher1.end();
				if (extreme.charAt(q-2)=='+'){
					Character character = new Character();
					character.setName("atypical_"+chara);
					character.setFrom(extreme.substring(p+1,q-2).trim());
					character.setTo("");
					character.setFromUnit(unit);
					character.setToUnit(unit);
					character.setUpperRestricted("false");
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character name=\"atypical_size\" from=\""+extreme.substring(p+1,q-2).trim()+"\" upper_restricted=\"false\"/>");
				}else{
					Character character = new Character();
					character.setName("atypical_"+chara);
					character.setValue(extreme.substring(p+1,q-1).trim());
					character.setUnit(unit);
					//character.setAttribute("unit", extreme.substring(q-1).trim());
					innertagstate.add(character);
					//innertagstate = innertagstate.concat("<character name=\"atypical_size\" value=\""+extreme.substring(p+1,q-1).trim()+"\"/>");
				}
			}
			extreme = matcher1.replaceAll("#");
			matcher1.reset();
			j = extreme.length();
			if(extreme.substring(i,j).contains("–")|extreme.substring(i,j).contains("-") && !extreme.substring(i,j).contains("×") && !extreme.substring(i,j).contains("x") && !extreme.substring(i,j).contains("X")){
				String extract = extreme.substring(i,j);
				//Pattern pattern18 = Pattern.compile("[\\s]?[dcmµu]?m(([\\s]diam)?([\\s]wide)?)");
				Pattern pattern18 = Pattern.compile("[\\s]?\\b"+units+"\\b(([\\s]diam)?([\\s]wide)?)");
				Matcher matcher3 = pattern18.matcher(extract);
				unit="";
				if ( matcher3.find()){
					unit = extract.substring(matcher3.start(), matcher3.end());
					if(unit.matches(".*[2]$")) chara = "area";
					if(unit.matches(".*[3]$")) chara = "volume";
				}
				extract = matcher3.replaceAll("#");
				matcher3.reset();
				String from = extract.substring(0, extract.indexOf('-')).trim(); //2016
				String to = extract.substring(extract.indexOf('-')+1,extract.lastIndexOf('#')).replaceAll("#", "").trim();
				boolean upperrestricted = ! to.endsWith("+");
				to = to.replaceFirst("\\+$", "").trim();

				Character character = new Character();
				character.setCharType("range_value");
				character.setName(chara);
				character.setFrom(from.replaceAll("#", ""));
				character.setFromUnit(unit.trim());
				character.setTo(to);
				character.setToUnit(unit.trim());
				if(!upperrestricted)
					character.setUpperRestricted(upperrestricted+"");
				innertagstate.add(character);
				//innertagstate = innertagstate.concat("<character char_type=\"range_value\" name=\"size\" from=\""+from+"\" from_unit=\""+unit.trim()+"\" to=\""+to+"\" to_unit=\""+unit.trim()+"\" upper_restricted=\""+upperrestricted+"\"/>");
				extract = extract.replaceAll("^#+", "");//#5-10# =>5-10#
				toval = extract.substring(0, extract.indexOf('-'));
				fromval = extract.substring(extract.indexOf('-')+1, extract.indexOf('#'));
				//sizect+=1;
			}
			else {
				String extract = extreme.substring(i,j);
				//Pattern pattern18 = Pattern.compile("[\\s]?[dcmµμu]?m(([\\s]diam)?([\\s]wide)?)");
				Pattern pattern18 = Pattern.compile("[\\s]?\\b"+units+"\\b(([\\s]diam)?([\\s]wide)?)");
				Matcher matcher3 = pattern18.matcher(extract);
				unit="";
				if ( matcher3.find()){
					unit = extract.substring(matcher3.start(), matcher3.end());
					if(unit.matches(".*[2]$")) chara = "area";
					if(unit.matches(".*[3]$")) chara = "volume";
				}
				extract = matcher3.replaceAll("#");
				matcher3.reset();

				boolean upperrestricted = ! extract.endsWith("+#");
				Character character = new Character();
				character.setName(chara);
				if(!upperrestricted){
					character.setFrom(extract.substring(0,extract.indexOf("+#")).trim());
					character.setFromUnit(unit.trim());
					character.setUpperRestricted(upperrestricted+"");
					character.setCharType("range_value");
				}else{
					character.setValue(extract.substring(0,extract.indexOf('#')).trim());
					character.setUnit(unit.trim());
				}
				innertagstate.add(character);
				//innertagstate = innertagstate.concat("<character name=\"size\" value=\""+extract.substring(0,extract.indexOf('#')).trim()+"\" unit=\""+unit.trim()+"\"/>");
				toval = extract.substring(0,extract.indexOf('#'));
				fromval = extract.substring(0,extract.indexOf('#'));
			}

			for(Character character : innertagstate) {
				if(character.getTo() != null && character.getTo().isEmpty()){
					if(toval.endsWith("+")){
						toval = toval.replaceFirst("\\+$", "");
						character.setUpperRestricted("false");
					}
					character.setTo(toval.trim());
					character.setToInclusive("false");
					character.setCharType("range_value");
				}
				if(character.getFrom() != null && character.getFrom().isEmpty()) {
					character.setFrom(fromval.trim());
					character.setFromInclusive("false");
					character.setCharType("range_value");
				}
			}
		}
		plaincharset = matcher2.replaceAll("#");
		matcher2.reset();
		//log(LogLevel.DEBUG, "plaincharset2:"+plaincharset);
		return plaincharset;
	}


}
