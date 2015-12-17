package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.paukov.combinatorics.CombinatoricsVector;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.paukov.combinatorics.util.ComplexCombinationGenerator;

import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms.SynonymSet;

/**
 * This implementation assumes biologoical entity names to have the organ name in the last word, Antyhing before are constraint/modifying
 * @author rodenhausen
 */
public class RemoveSynonyms extends AbstractTransformer {

	private List<KnowsSynonyms> hasBiologicalEntitySynonymsList;
	private List<KnowsSynonyms> hasCharacterSynonymsList;

	public RemoveSynonyms(List<KnowsSynonyms> hasBiologicalEntitySynonymsList, List<KnowsSynonyms> hasCharacterSynonymsList) {
		this.hasBiologicalEntitySynonymsList = hasBiologicalEntitySynonymsList;
		this.hasCharacterSynonymsList = hasCharacterSynonymsList;
	}
	
	@Override
	public void transform(Document document) {
		removeBiologicalEntitySynonyms(document);
		removeCharacterSynonyms(document);
	}
	
	private void removeBiologicalEntitySynonyms(Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			String name = biologicalEntity.getAttributeValue("name");
			if(name != null) {
				String newName = createSynonymReplacedValue(getSearchPartitionsForOrganName(name), hasBiologicalEntitySynonymsList);
				biologicalEntity.setAttribute("name", newName);
			}
		}
	}

	private void removeCharacterSynonyms(Document document) {
		for (Element character : this.characterPath.evaluate(document)) {
			String value = character.getAttributeValue("value");
			if(value != null && value.equals("2 times compound or rarely entire blade"))
				System.out.println(value);
			if(value != null) {
				if(value.split(" ").length < 5) { //explosion of combinations to check
					String newValue = createSynonymReplacedValue(getSearchPartitionsForCharacterValue(value), hasCharacterSynonymsList);
					character.setAttribute("value", newValue);
				}
			}
		}
	}	

	private String createSynonymReplacedValue(List<ICombinatoricsVector<ICombinatoricsVector<String>>> partitionsList, List<KnowsSynonyms> hasSynonymsList) {
		//pick partition with most of it's partitions (not in count but coverage) covered by synonyms
		double maximumSynonymCoverage = 0;
		ICombinatoricsVector<ICombinatoricsVector<String>> maximumSynonymCoveragePartitions = partitionsList.get(0);
		for(ICombinatoricsVector<ICombinatoricsVector<String>> partitions : partitionsList) {
			int synonymsFound = 0;
			for(ICombinatoricsVector<String> partition : partitions) {
				String search = StringUtils.join(partition.getVector(), " ");
				for(KnowsSynonyms hasSynonyms : hasSynonymsList) {
					Set<SynonymSet> synonymSets = hasSynonyms.getSynonyms(search);
					if(!synonymSets.isEmpty()) {
						synonymsFound++;
						break;
					}
				}
			}
			double coverage = synonymsFound / (double)partitions.getSize();
			if(coverage > maximumSynonymCoverage) {
				maximumSynonymCoverage = coverage;
				maximumSynonymCoveragePartitions = partitions;
			}
		}
		
		//replace partitions with synonyms found for the maximum coverage partitions
		List<String> synonymReplacedPartitions = new LinkedList<String>();
		for(ICombinatoricsVector<String> partition : maximumSynonymCoveragePartitions) {
			String search = StringUtils.join(partition.getVector(), " ");
			
			boolean replacedBySynonym = false;
			for(KnowsSynonyms hasSynonyms : hasSynonymsList) {
				Set<SynonymSet> synonymSets = hasSynonyms.getSynonyms(search);
				if(!synonymSets.isEmpty()) {
					SynonymSet synonymSet = pickSynonymSet(synonymSets, search);
					synonymReplacedPartitions.add(synonymSet.getPreferredTerm());
					replacedBySynonym = true;
					break;
				}
			}
			if(!replacedBySynonym)
				synonymReplacedPartitions.add(search);
		}
		
		//set the actual value in the xml document
		return StringUtils.join(synonymReplacedPartitions, " ");
	}

	//create all permutations of the parts
	//create all partitions of resulting permutations
	//favor bigger partitions over smaller ones (order of result list)
	private List<ICombinatoricsVector<ICombinatoricsVector<String>>> getSearchPartitionsForCharacterValue(String value) {
		List<ICombinatoricsVector<ICombinatoricsVector<String>>> result = new ArrayList<ICombinatoricsVector<ICombinatoricsVector<String>>>();
		ICombinatoricsVector<String> parts = Factory.createVector(StringUtils.split(value, " "));
		Generator<String> permutationGenerator = Factory.createPermutationGenerator(parts);
		for(ICombinatoricsVector<String> permutation : permutationGenerator) {
			for(int partitionCount = 1; partitionCount <= permutation.getSize(); partitionCount++) {
				Generator<ICombinatoricsVector<String>> combinationGenerator = new ComplexCombinationGenerator<String>(permutation, partitionCount);
				result.addAll(combinationGenerator.generateAllObjects());
			}
		}
		
		Collections.sort(result, new Comparator<ICombinatoricsVector<ICombinatoricsVector<String>>>() {
			@Override
			public int compare(ICombinatoricsVector<ICombinatoricsVector<String>> o1, ICombinatoricsVector<ICombinatoricsVector<String>> o2) {
				return o2.getSize() - o1.getSize();
			}
		});
		return result;
	}
	
	//Assumption: Organ name is always last part. Prepended text can vary in order. How to know "length" of last organ name part?
	//create all permutations of the parts
	//create all partitions of resulting permutations
	//favor bigger partitions over smaller ones (order of result list)
	private List<ICombinatoricsVector<ICombinatoricsVector<String>>> getSearchPartitionsForOrganName(String name) {
		List<ICombinatoricsVector<ICombinatoricsVector<String>>> result = new ArrayList<ICombinatoricsVector<ICombinatoricsVector<String>>>();
		String[] originalParts = StringUtils.split(name, " ");
		String[] parts = Arrays.copyOf(originalParts, originalParts.length - 1);
		
		ICombinatoricsVector<String> partsVector = Factory.createVector(parts);
		Generator<String> permutationGenerator = Factory.createPermutationGenerator(partsVector);
		for(ICombinatoricsVector<String> permutation : permutationGenerator) {
			permutation.addValue(originalParts[originalParts.length-1]);
			
			for(int i=0; i<permutation.getSize(); i++) {
				CombinatoricsVector<ICombinatoricsVector<String>> combinatoricsVector = new CombinatoricsVector<ICombinatoricsVector<String>>();
				CombinatoricsVector<String> organPart = new CombinatoricsVector<String>(permutation.getVector().subList(i, permutation.getSize()));
				List<String> prependPart = permutation.getVector().subList(0, i);
					
				ICombinatoricsVector<String> prependPartVector = Factory.createVector(prependPart);
				Generator<String> prependPermutationGenerator = Factory.createPermutationGenerator(prependPartVector);
				List<ICombinatoricsVector<String>> prependPermutations = prependPermutationGenerator.generateAllObjects();
				if(!prependPermutations.isEmpty())
					for(ICombinatoricsVector<String> prependPermutation : prependPermutations) {
						for(int partitionCount = 1; partitionCount <= prependPermutation.getSize(); partitionCount++) {
							Generator<ICombinatoricsVector<String>> combinationGenerator = new ComplexCombinationGenerator<String>(prependPermutation, partitionCount);
							List<ICombinatoricsVector<ICombinatoricsVector<String>>> combinations = combinationGenerator.generateAllObjects();
							for(ICombinatoricsVector<ICombinatoricsVector<String>> combination : combinations) {
								combination.addValue(organPart);
								result.add(combination);
							}
						}
					}
				else {
					combinatoricsVector.addValue(organPart);
				}
				result.add(combinatoricsVector);
			}
		}
		
		Collections.sort(result, new Comparator<ICombinatoricsVector<ICombinatoricsVector<String>>>() {
			@Override
			public int compare(ICombinatoricsVector<ICombinatoricsVector<String>> o1, ICombinatoricsVector<ICombinatoricsVector<String>> o2) {
				return o2.getSize() - o1.getSize();
			}
		});
		
		if(result.isEmpty()) {
			ICombinatoricsVector<ICombinatoricsVector<String>> vector = new CombinatoricsVector<ICombinatoricsVector<String>>();
			ICombinatoricsVector<String> organ = new CombinatoricsVector<String>();
			organ.addValue(originalParts[originalParts.length-1]);
			vector.addValue(organ);
			result.add(vector);
		}
			
		return result;
	}

	private SynonymSet pickSynonymSet(Set<SynonymSet> synonymSets, String part) {
		return synonymSets.iterator().next();
	}
	
	public static void main(String[] args) {
		List<KnowsSynonyms> hasBiologicalEntitySynonymsList = new LinkedList<KnowsSynonyms>();
		hasBiologicalEntitySynonymsList.add(new KnowsSynonyms() {
			@Override
			public Set<SynonymSet> getSynonyms(String term) {
				Set<SynonymSet> result = new HashSet<SynonymSet>();
				switch(term) {
				case "lateral":
					HashSet<String> synonyms = new HashSet<String>();
					synonyms.add("behind");
					synonyms.add("lateral");
					result.add(new SynonymSet("behind", synonyms));
					break;
				case "leaf":
					synonyms = new HashSet<String>();
					synonyms.add("leaf");
					synonyms.add("stem");
					result.add(new SynonymSet("stem", synonyms));
					break;
				}
				
				return result;
			}
		});
		List<KnowsSynonyms> hasCharacterSynonymsList = new LinkedList<KnowsSynonyms>();
		RemoveSynonyms removeSynonyms = new RemoveSynonyms(hasBiologicalEntitySynonymsList, hasCharacterSynonymsList);
		//removeSynonyms.createSynonymReplacedValue(removeCharacterSplitTransformer.getSearchPartitionsForOrganName("lateral leaf"));
	}
}
