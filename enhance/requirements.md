requirement-notes
--------------------
###### Blackbox
* Input: 
 * text capture output, see https://github.com/biosemantics/schemas/blob/master/semanticMarkupOutput.xsd
 * ontologies that can be used to map structure (or possibly also quality terms against?)

* Output: 
 * text capture output
 
###### Preparations
* Resolve character splitting (utilize ontologies, leave room for other knowledge source) due to
   * quality term synonym
   * organ term synonym
* Resolve lumping of characters (utilize ontologies) due to 
   * organ identification problem (differentiate apex as leaf vs stem apex)
   * vision is: use only organ name anymore to *identify* organ unambiguously
      * by putting constraint to name
      * by others, see below

The above two are to standardize the character names 
* Character constraint is put as
  * part of character value
  * new relation 
     * different set of rules
        * if constraint has constraintid for other organ: create relation between this characters organ and the pointed organ

* Relation contributes to structure name/constraint
   * part_of, and positional relations such as in, on etc.

* Map ontology Ids to all terms used (entities, character states, ...?) to reduce effort down the road
  * This should be a separate module, so it can be optionally put after charaparser, without the other prep steps above. Or make this tool configurable.
 
###### Use of Schema
* before enhance: constraint as outputted by charaparser (stated information from text), inferred_constraint unused
* enhance populates constraint with stated information combined with inferred information in an order and combination that enhance deems best. inferred_constraint holds information added to constraint that was inferred from external resources (not text, e.g. ontologies). That is constraint - inferred_constraint = stated constraint from charaparser output.
