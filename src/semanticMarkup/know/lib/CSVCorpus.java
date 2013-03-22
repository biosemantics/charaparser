package semanticMarkup.know.lib;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import semanticMarkup.know.ICorpus;
import au.com.bytecode.opencsv.CSVReader;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CSVCorpus implements ICorpus {

	private HashMap<String, Integer> frequencies = new HashMap<String, Integer>();
	
	@Inject
	public CSVCorpus(@Named("CSVCorpus_filePath") String filePath) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(filePath));
		List<String[]> lines = reader.readAll();
		for(String[] line : lines) {
			frequencies.put(line[0].toLowerCase(), Integer.parseInt(line[1]));
		}
		reader.close();
	}
	
	
	@Override
	public int getFrequency(String word) {
		word = word.toLowerCase();
		if(!frequencies.containsKey(word))
			return 0;
		return frequencies.get(word);
	}
}
