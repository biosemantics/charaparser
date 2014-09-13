/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.know.lib;

/**
 * @author Hong Cui
 *
 */
public class ElementRelationGroup {

	public static String entityElements = "structure|substance|taxon_name";
	public static String entityConstraintElements = "function|growth_order|position|structure_in_adjective_form|structure_subtype"; //constraint to a structure name
	public static String entityRefElements = "growth_order|position|structure_in_adjective_form|structure_subtype"; //used to refer to a structure, e.g. "the first".
	//public static String verbRelationElements = "position_relational"; //remove from categories.
	public static String possessPreps="with|without|devoid[_ -]of";

	
}
