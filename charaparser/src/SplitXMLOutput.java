import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class SplitXMLOutput {

	public static void main(String[] args) throws Exception {
		
		String input = "outfile.xml"; //TIP_AnsKey_CharaParser_Evaluation //FNAV19_AnsKey_CharaParser_Evaluation
		String comparisonFiles = "evaluationData//FNAV19_AnsKey_CharaParser_Evaluation";
		//String comparisonFiles = "evaluationData//TIP_AnsKey_CharaParser_Evaluation";
		String outDir = "evaluationData//my";
		File inputFile = new File(input);
		String line = "";
		
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		
		File currentFile = null;
		FileWriter fw = null;
		Integer number = null;
		List<File> treatmentFiles = new ArrayList<File>();
		while((line = br.readLine()) != null) {
			
			if(line.contains("<statement>")) {
				currentFile = new File("tempSplit");
				fw = new FileWriter(currentFile);
			} 
			
			if(currentFile != null)
				fw.append(line + "\n");
			
			if(line.contains("<number>")) {
				Integer newNumber = Integer.parseInt(line.split("<number>")[1].split("</number>")[0]);
				number = newNumber;
			}
			
			if(line.contains("</statement>")) {
				fw.close();
				
				File newFile = new File(currentFile.getAbsolutePath() + treatmentFiles.size());
				copyfile(currentFile.getAbsolutePath(), newFile.getAbsolutePath());
				treatmentFiles.add(newFile);		
				
				currentFile = new File("tempSplit");
				fw = new FileWriter(currentFile);
			}
				
			if(line.contains("</treatment>")) {

				int statementId = -1;
				for(File file : treatmentFiles) {
					statementId++;
					
					for(; ; statementId++) {
						int realNumber = number + 1;
						File comparisonFile = new File(comparisonFiles + "/" + realNumber + ".txt-" + statementId + ".xml");
						System.out.println(comparisonFile);
						if(comparisonFile.exists())
							break;
					}
					
					copyfile(file.getAbsolutePath(), outDir + "/" + (number + 1) + ".txt-" + statementId + ".xml");
				}

				treatmentFiles = new ArrayList<File>();
			}
		}
		
	}
	
	
	
	public static void copyfile(String srFile, String dtFile) throws Exception {
		File f1 = new File(srFile);
		File f2 = new File(dtFile);
		InputStream in = new FileInputStream(f1);

		OutputStream out = new FileOutputStream(f2);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
	
}
