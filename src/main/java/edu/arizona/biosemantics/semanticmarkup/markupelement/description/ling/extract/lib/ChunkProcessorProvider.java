package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.HashMap;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessorProvider;

/**
 * ChunkProcessorProvider poses an IChunkProcessorProvider
 * @author rodenhausen
 */
public class ChunkProcessorProvider implements IChunkProcessorProvider {

	private HashMap<ChunkType, IChunkProcessor> chunkProcessors = new HashMap<ChunkType, IChunkProcessor>();
	
	/**
	 * @param areaChunkProcessor
	 * @param bracketedChunkProcessor
	 * @param characterStateProcessor
	 * @param chromChunkProcessor
	 * @param comparativeValueChunkProcessor
	 * @param eosChunkProcessor
	 * @param eolChunkProcessor
	 * @param mainSubjectOrganChunkProcessor
	 * @param modifierChunkProcessor
	 * @param nonSubjectOrganChunkProcessor
	 * @param npListChunkProcessor
	 * @param numericalChunkProcessor
	 * @param valueChunkProcessor
	 * @param countChunkProcessor
	 * @param basedCountChunkProcessor
	 * @param orChunkProcessor
	 * @param ppChunkProcessor
	 * @param ratioChunkProcessor
	 * @param sbarChunkProcessor
	 * @param specificPPChunkProcessor
	 * @param thanChunkProcessor
	 * @param thanCharacterChunkProcessor
	 * @param valuePercentageChunkProcessor
	 * @param valueDegreeChunkProcessor
	 * @param vpChunkProcessor
	 * @param unassignedChunkProcessor
	 * @param stateChunkProcessor
	 * @param commaChunkProcessor
	 * @param constraintChunkProcessor
	 * @param toChunkProcessor
	 * @param characterNameProcessor
	 * @param andChunkProcessor
	 * @param whereChunkProcessor
	 */
	@Inject
	public ChunkProcessorProvider(@Named("Area") IChunkProcessor areaChunkProcessor,
			@Named("Bracketed") IChunkProcessor bracketedChunkProcessor,
			@Named("CharacterState") IChunkProcessor characterStateProcessor,
			@Named("Chrom") IChunkProcessor chromChunkProcessor,
			@Named("ComparativeValue") IChunkProcessor comparativeValueChunkProcessor,
			@Named("Eos") IChunkProcessor eosChunkProcessor,
			@Named("Eol") IChunkProcessor eolChunkProcessor,
			@Named("MainSubjectOrgan") IChunkProcessor mainSubjectOrganChunkProcessor,
			@Named("Modifier") IChunkProcessor modifierChunkProcessor,
			@Named("NonSubjectOrgan") IChunkProcessor nonSubjectOrganChunkProcessor,
			@Named("NPList") IChunkProcessor npListChunkProcessor,
			@Named("Numerical") IChunkProcessor numericalChunkProcessor,
			@Named("Value") IChunkProcessor valueChunkProcessor,
			@Named("Count") IChunkProcessor countChunkProcessor,
			@Named("BasedCount") IChunkProcessor basedCountChunkProcessor,
			@Named("Or") IChunkProcessor orChunkProcessor,
			@Named("PP") IChunkProcessor ppChunkProcessor,
			@Named("Ratio") IChunkProcessor ratioChunkProcessor,
			@Named("SBAR") IChunkProcessor sbarChunkProcessor,
			@Named("SpecificPP") IChunkProcessor specificPPChunkProcessor,
			@Named("Than") IChunkProcessor thanChunkProcessor,
			@Named("ThanCharacter") IChunkProcessor thanCharacterChunkProcessor,
			@Named("ValuePercentage") IChunkProcessor valuePercentageChunkProcessor,
			@Named("ValueDegree") IChunkProcessor valueDegreeChunkProcessor,
			@Named("VP") IChunkProcessor vpChunkProcessor, 
			@Named("Unassigned") IChunkProcessor unassignedChunkProcessor, 
			@Named("State") IChunkProcessor stateChunkProcessor, 
			@Named("Comma") IChunkProcessor commaChunkProcessor, 
			@Named("Constraint") IChunkProcessor constraintChunkProcessor, 
			@Named("To") IChunkProcessor toChunkProcessor, 
			@Named("CharacterName") IChunkProcessor characterNameProcessor, 
			@Named("And") IChunkProcessor andChunkProcessor, 
			@Named("Where") IChunkProcessor whereChunkProcessor,
			@Named("Average") IChunkProcessor averageChunkProcessor) {
		chunkProcessors.put(ChunkType.AREA, areaChunkProcessor);
		chunkProcessors.put(ChunkType.BRACKETED, bracketedChunkProcessor);
		chunkProcessors.put(ChunkType.CHARACTER_STATE, characterStateProcessor);
		chunkProcessors.put(ChunkType.CHROM, chromChunkProcessor);
		chunkProcessors.put(ChunkType.COMPARATIVE_VALUE, comparativeValueChunkProcessor);
		chunkProcessors.put(ChunkType.END_OF_SUBCLAUSE, eosChunkProcessor);
		chunkProcessors.put(ChunkType.END_OF_LINE, eolChunkProcessor);
		chunkProcessors.put(ChunkType.MAIN_SUBJECT_ORGAN, mainSubjectOrganChunkProcessor);
		chunkProcessors.put(ChunkType.MODIFIER, modifierChunkProcessor);
		chunkProcessors.put(ChunkType.NON_SUBJECT_ORGAN, nonSubjectOrganChunkProcessor);
		chunkProcessors.put(ChunkType.NP_LIST, npListChunkProcessor);
		chunkProcessors.put(ChunkType.NUMERICALS, numericalChunkProcessor);
		chunkProcessors.put(ChunkType.VALUE, valueChunkProcessor);
		chunkProcessors.put(ChunkType.COUNT, countChunkProcessor);
		chunkProcessors.put(ChunkType.BASED_COUNT, basedCountChunkProcessor);
		chunkProcessors.put(ChunkType.OR, orChunkProcessor);
		chunkProcessors.put(ChunkType.PP, ppChunkProcessor);
		chunkProcessors.put(ChunkType.RATIO, ratioChunkProcessor);
		chunkProcessors.put(ChunkType.SBAR, sbarChunkProcessor);
		chunkProcessors.put(ChunkType.SPECIFIC_PP, specificPPChunkProcessor);
		chunkProcessors.put(ChunkType.THAN_PHRASE, thanChunkProcessor);
		chunkProcessors.put(ChunkType.THAN_CHARACTER_PHRASE, thanCharacterChunkProcessor);
		chunkProcessors.put(ChunkType.VALUE_PERCENTAGE, valuePercentageChunkProcessor);
		chunkProcessors.put(ChunkType.VALUE_DEGREE, valueDegreeChunkProcessor);
		chunkProcessors.put(ChunkType.VP, vpChunkProcessor);
		chunkProcessors.put(ChunkType.UNASSIGNED, unassignedChunkProcessor);
		chunkProcessors.put(ChunkType.STATE, stateChunkProcessor);
		chunkProcessors.put(ChunkType.COMMA, commaChunkProcessor);
		chunkProcessors.put(ChunkType.CONSTRAINT, constraintChunkProcessor);
		chunkProcessors.put(ChunkType.TO_PHRASE, toChunkProcessor);
		chunkProcessors.put(ChunkType.CHARACTER_NAME, characterNameProcessor);
		chunkProcessors.put(ChunkType.AND, andChunkProcessor);
		chunkProcessors.put(ChunkType.WHERE, whereChunkProcessor);
		chunkProcessors.put(ChunkType.WHEN, sbarChunkProcessor);
		chunkProcessors.put(ChunkType.AVERAGE, averageChunkProcessor);
		chunkProcessors.put(ChunkType.THAT, sbarChunkProcessor);
	}
	
	@Override
	public IChunkProcessor getChunkProcessor(ChunkType chunkType) {
		return chunkProcessors.get(chunkType);
	}

}
