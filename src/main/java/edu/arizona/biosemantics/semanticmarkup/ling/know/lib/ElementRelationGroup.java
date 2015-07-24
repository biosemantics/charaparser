/**
 * 
 */
package edu.arizona.biosemantics.semanticmarkup.ling.know.lib;

import java.util.ArrayList;

/**
 * @author Hong Cui
 *
 */
public class ElementRelationGroup {

	public static ArrayList<String> entityTypes = new ArrayList<String> ();
	public static String entityElements = "structure|taxon_name|substance"; //solid entity element
	public static String entityConstraintElements = "function|growth_order|position|structure_in_adjective_form|structure_subtype"; //constraint to a structure name
	public static String entityRefElements = "growth_order|position|structure_in_adjective_form|structure_subtype"; //used to refer to a structure, e.g. "the first".
	public static String entityStructuralConstraintElements = "structure|structure_in_adjective_form|structure_subtype"; //used to refer to a structure, e.g. "the first".
	public static String verbRelationElements = "position_relational"; //no code uses this
	public static String possessPreps="with|without|devoid[_ -]of|lack"; //prepositions indicates a possession relation
	
	static{
		for(String type: entityElements.split("\\|")){
			entityTypes.add(type);
		}
	}

	
}
