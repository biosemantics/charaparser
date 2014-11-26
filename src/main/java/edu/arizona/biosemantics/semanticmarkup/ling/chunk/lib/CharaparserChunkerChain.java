package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.IChunker;

/**
 * CharaparserChunkerChain chains a number of IChunkers according to previous charaparser version 
 * @author rodenhausen
 */
public class CharaparserChunkerChain extends ChunkerChain {

	/**
	 * @param characterListChunker
	 * @param organChunker
	 * @param stateChunker
	 * @param modifierChunker
	 * @param numericalChunker
	 * @param npChunker
	 * @param ppListChunker
	 * @param characterNameChunker
	 * @param ppINChunker
	 * @param vbChunker
	 * @param thatChunker
	 * @param whereChunker
	 * @param whenChunker
	 * @param otherINsChunker
	 * @param thanChunker
	 * @param toChunker
	 * @param vpRecoverChunker
	 * @param conjunctedOrgansRecoverChunker
	 * @param organRecoverChunker
	 * @param cleanupChunker
	 * @param orChunker
	 * @param andChunker
	 * @param punctuationChunker
	 * @param specificPPChunker
	 */
	@Inject
	public CharaparserChunkerChain(
			@Named("CharacterListChunker") IChunker characterListChunker,
			@Named("OrganChunker") IChunker organChunker,
			@Named("StateChunker") IChunker stateChunker,
			@Named("ModifierChunker") IChunker modifierChunker,
			@Named("NumericalChunker") IChunker numericalChunker,
			@Named("NPListChunker") IChunker npChunker, 
			@Named("PPListChunker") IChunker ppListChunker,
			@Named("CharacterNameChunker") IChunker characterNameChunker,
			@Named("PPINChunker") IChunker ppINChunker,
			@Named("VBChunker") IChunker vbChunker,
			@Named("ThatChunker") IChunker thatChunker, 
			@Named("WhereChunker") IChunker whereChunker,
			@Named("WhenChunker") IChunker whenChunker, 
			@Named("OtherINsChunker") IChunker otherINsChunker,
			@Named("ThanChunker") IChunker thanChunker,
			@Named("ToChunker") IChunker toChunker,
			@Named("VPRecoverChunker") IChunker vpRecoverChunker,
			@Named("ConjunctedOrgansRecoverChunker") IChunker conjunctedOrgansRecoverChunker,
			@Named("OrganRecoverChunker") IChunker organRecoverChunker,
			@Named("CleanupChunker") IChunker cleanupChunker,
			@Named("OrChunker") IChunker orChunker,
			@Named("AndChunker") IChunker andChunker,
			@Named("PunctuationChunker") IChunker punctuationChunker,
			@Named("SpecificPPChunker") IChunker specificPPChunker,
			@Named("ChromosomeChunker") IChunker chromosomeChunker,
			@Named("AreaChunker") IChunker areaChunker
			) {
		this.add(areaChunker);
		this.add(characterListChunker);
		this.add(punctuationChunker);
		//this.add(chromosomeChunker);
		this.add(modifierChunker);
		this.add(organChunker);
		this.add(stateChunker);
		this.add(numericalChunker);
		this.add(npChunker);
		this.add(characterNameChunker);
		this.add(vbChunker);
		this.add(ppListChunker);
		this.add(ppINChunker);
		//this.add(ppListChunker);
		//this.add(thatChunker);
		//this.add(whereChunker);
		//this.add(whenChunker);
		this.add(otherINsChunker); 
		this.add(thanChunker);
		this.add(vpRecoverChunker);
		this.add(conjunctedOrgansRecoverChunker);
		this.add(organRecoverChunker);
		this.add(orChunker);
		this.add(andChunker);
		//hong moved chromosomeChunker after organChunker.
		this.add(chromosomeChunker);
		//hong moved 3 clause chunkers here, where/when before that
	    this.add(whereChunker);
		this.add(whenChunker);
		this.add(thatChunker);
		this.add(cleanupChunker);
		
	}
}
