package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * 4. allow the user to set the unit (m or mm etc.) to be used.
 * @author rodenhausen
 */

public class StandardizeUnits extends AbstractTransformer{
		@Override
		public void transform(Document document) {
			standardizeUnits(document);
		}

		public static enum Unit {
			//1 cm to m?
			//cm to mm = 10
			//m to mm = 1000
			//cm to m = 10/1000 => base/target
			nm(1/100000.0), 
			µ(1/1000.0),
			µm(1/1000.0), 
			mm(1.0), 
			cm(1/0.1), 
			dm(1/0.01),
			m(1/0.001), 
			km(1/0.000001), 
			in(1/0.03937007874016), 
			ft(1/0.003280839895013), 
			yd(1/0.001093613298338);
			
			private Double toMMFactor;

			private Unit(Double toMMFactor) {
				this.toMMFactor = toMMFactor;
			}
			
			public Double getToMMFactor() {
				return toMMFactor;
			}
			
		}
		
		private Unit targetUnit = Unit.mm;
		
		public void standardizeUnits(Document document) {
			if(targetUnit == null)
				return;

			Double targetUnitFactor = targetUnit.getToMMFactor();
			for(Element character : this.characterPath.evaluate(document)) {
				if(character.getAttribute("unit") != null && character.getAttribute("value")!=null && isNumeric(character.getAttributeValue("value"))) {
					try {
						Double doubleValue = Double.parseDouble(normalizeNumeric(character.getAttributeValue("value")));
						try{
							Double baseUnitFactor = Unit.valueOf(character.getAttributeValue("unit")).getToMMFactor();
							String normalizedValue = String.valueOf(doubleValue * baseUnitFactor / targetUnitFactor);
							character.setAttribute("value", normalizedValue);
							character.setAttribute("unit",targetUnit.toString());
						} catch(IllegalArgumentException e) {
							//log(LogLevel.ERROR, "Can't convert unit", e);
						}
					} catch(NumberFormatException e) {
						//log(LogLevel.ERROR, "Can't parse value", e);
					}
				}
				if(character.getAttribute("from") != null && character.getAttribute("from_unit") != null && isNumeric(character.getAttributeValue("from"))) {
					try{
						Double doubleValue = Double.parseDouble(normalizeNumeric(character.getAttributeValue("from")));
						try{
							Double baseUnitFactor = Unit.valueOf(character.getAttributeValue("from_unit")).getToMMFactor();
							character.setAttribute("from", String.valueOf(doubleValue * baseUnitFactor / targetUnitFactor));
							character.setAttribute("from_unit",targetUnit.toString());
						} catch(IllegalArgumentException e) {
							//log(LogLevel.ERROR, "Can't convert from unit", e);
						}
					} catch(NumberFormatException e) {
						//log(LogLevel.ERROR, "Can't parse from", e);
					}
				}
				
				if(character.getAttribute("to") != null && character.getAttribute("to_unit") != null && isNumeric(character.getAttributeValue("to"))) {
					try{
						Double doubleValue = Double.parseDouble(normalizeNumeric(character.getAttributeValue("to")));
						try{
							Double baseUnitFactor = Unit.valueOf(character.getAttributeValue("to_unit")).getToMMFactor();
							character.setAttribute("to", String.valueOf(doubleValue * baseUnitFactor / targetUnitFactor));
							character.setAttribute("to_unit",targetUnit.toString());
						} catch(IllegalArgumentException e) {
							//log(LogLevel.ERROR, "Can't convert from unit", e);
						}
					} catch(NumberFormatException e) {
						//log(LogLevel.ERROR, "Can't parse from", e);
					}
				}
			}
		}

		private String normalizeNumeric(String value) {
			return value.replaceAll(",", ".");
		}
		
		/**
		 * normalize somewhere 0.1 and 0,1 ? 
		 * @param value
		 * @return
		 */
		private boolean isNumeric(String value) {
			String exponential = "[[E[\\+\\-]?]\\d+]?$";
			// "+.3"; case or "-111.577" case
			if(value.matches("^[\\+\\-]\\d*[\\.,]\\d+" + exponential))
				return true;
			// "+1", "-4" case
			if(value.matches("^[\\+\\-]\\d+" + exponential))
				return true;
			// ".3" case or "111.577" case
			if(value.matches("^\\d*[\\.,]\\d+" + exponential))
				return true;
			// "123" case
			if(value.matches("^\\d" + exponential))
				return true;
			return false;
		}
		
		
		public static void main(String[] args) {
			
			String test = "E+";
			System.out.println(test.matches("^[E[\\+\\-]?]?$"));
			
			String a = "+.3";
			String b = "-.2";
			
			String c = ".4";
			String d = "4";
			String e = "1,3";
			String f = "-1.4";
			
			String g = ".";
			String h = "-";
			
			String i = "1.4E+";// "1.4E+03";
			String j = ".4E+3";
			String k = ".4E3";
			String l = ".4E-5";
			StandardizeUnits t = new StandardizeUnits();
			System.out.println(t.isNumeric(a));
			System.out.println(t.isNumeric(b));
			System.out.println(t.isNumeric(c));
			System.out.println(t.isNumeric(d));
			System.out.println(t.isNumeric(e));
			System.out.println(t.isNumeric(f));
			System.out.println(t.isNumeric(g));
			System.out.println(t.isNumeric(h));
			System.out.println(t.isNumeric(i));
			System.out.println(t.isNumeric(j));
			System.out.println(t.isNumeric(k));
			System.out.println(t.isNumeric(l));
			
		}

		public void setUnit(Unit unit) {
			this.targetUnit = unit;
		}
		
		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}



