package semanticMarkup.ling.chunk.lib;

import semanticMarkup.ling.chunk.ChunkerChain;
import semanticMarkup.ling.chunk.IChunker;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CharaparserChunkerChain extends ChunkerChain {

	@Inject
	public CharaparserChunkerChain(
			@Named("CharacterListChunker") IChunker characterListChunker,
			@Named("OrganChunker") IChunker organChunker,
			@Named("StateChunker") IChunker stateChunker,
			@Named("ModifierChunker") IChunker modifierChunker,
			@Named("NumericalChunker") IChunker numericalChunker,
			@Named("NPListChunker") IChunker npChunker, 
			@Named("PPListChunker") IChunker ppListChunker, 
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
			@Named("PunctuationChunker") IChunker punctuationChunker,
			@Named("SpecificPPChunker") IChunker specificPPChunker
			) {
		
		this.add(characterListChunker);
		this.add(punctuationChunker);
		this.add(modifierChunker);
		this.add(organChunker);
		this.add(stateChunker);
		this.add(numericalChunker);
		this.add(npChunker);
		this.add(ppINChunker);
		this.add(ppListChunker);
		this.add(vbChunker);
		this.add(thatChunker);
		this.add(whereChunker);
		this.add(whenChunker);
		this.add(otherINsChunker); 
		this.add(thanChunker);
		this.add(vpRecoverChunker);
		this.add(conjunctedOrgansRecoverChunker);
		this.add(organRecoverChunker);
		this.add(orChunker);
		this.add(cleanupChunker);
	}
}
