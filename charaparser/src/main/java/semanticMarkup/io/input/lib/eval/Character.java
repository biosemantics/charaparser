package semanticMarkup.io.input.lib.eval;

import javax.xml.bind.annotation.XmlAttribute;

public class Character {

	private String name;
	private String value;
	private String modifier;
	private String constraint;
	private String constraintId;
	private String characterType;
	private String from;
	private String to;
	private String fromInclusive;
	private String toInclusive;
	private String fromUnit;
	private String toUnit;
	private String unit;
	private String upperRestricted;
	private String relativeConstraint;
	
	public Character() { }

	public Character(String name, String value, String modifier,
			String constraint, String constraintId, String characterType,
			String from, String to, String fromInclusive) {
		super();
		this.name = name;
		this.value = value;
		this.modifier = modifier;
		this.constraint = constraint;
		this.constraintId = constraintId;
		this.characterType = characterType;
		this.from = from;
		this.to = to;
		this.fromInclusive = fromInclusive;
	}
	
	@XmlAttribute
	public String getToInclusive() {
		return toInclusive;
	}

	public void setToInclusive(String toInclusive) {
		this.toInclusive = toInclusive;
	}

	@XmlAttribute
	public String getFromUnit() {
		return fromUnit;
	}

	public void setFromUnit(String fromUnit) {
		this.fromUnit = fromUnit;
	}

	@XmlAttribute
	public String getToUnit() {
		return toUnit;
	}

	public void setToUnit(String toUnit) {
		this.toUnit = toUnit;
	}

	@XmlAttribute
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@XmlAttribute
	public String getUpperRestricted() {
		return upperRestricted;
	}

	public void setUpperRestricted(String upperRestricted) {
		this.upperRestricted = upperRestricted;
	}

	@XmlAttribute
	public String getRelativeConstraint() {
		return relativeConstraint;
	}

	public void setRelativeConstraint(String relativeConstraint) {
		this.relativeConstraint = relativeConstraint;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@XmlAttribute
	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	@XmlAttribute
	public String getConstraint() {
		return constraint;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}

	@XmlAttribute
	public String getConstraintId() {
		return constraintId;
	}

	public void setConstraintId(String constraintId) {
		this.constraintId = constraintId;
	}

	@XmlAttribute
	public String getCharacterType() {
		return characterType;
	}

	public void setCharacterType(String characterType) {
		this.characterType = characterType;
	}

	@XmlAttribute
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	@XmlAttribute
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@XmlAttribute
	public String getFromInclusive() {
		return fromInclusive;
	}

	public void setFromInclusive(String fromInclusive) {
		this.fromInclusive = fromInclusive;
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();		
		sb.append("name " + name + "\n");
		sb.append("value " + value + "\n");
		sb.append("modifier" + modifier + "\n");
		sb.append("constraint " + constraint + "\n");
		sb.append("constraintId " + constraintId + "\n");
		sb.append("characterType " + characterType + "\n");
		sb.append("from " + from + "\n");
		sb.append("to " + to + "\n");
		sb.append("fromInclusive " + fromInclusive + "\n");
		return sb.toString();
	}
}
