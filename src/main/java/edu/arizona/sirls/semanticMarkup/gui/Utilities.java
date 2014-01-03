package edu.arizona.sirls.semanticMarkup.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class Utilities {
	private static final Logger LOGGER = Logger.getLogger(Utilities.class);

	public static void resetFolder(File folder, String subfolder) {
		File d = new File(folder, subfolder);
		if(!d.exists()){
			d.mkdir();
		}else{ //empty folder
			Utilities.emptyFolder(d);
		}
	}
	
	public static void emptyFolder(File f){
			File[] fs = f.listFiles();
			for(int i =0; i<fs.length; i++){
				fs[i].delete();
			}
	}
	
	public static void copyFile(String f, File fromfolder, File tofolder){
		try{
			  File f1 = new File(fromfolder, f);
			  File f2 = new File(tofolder, f);
			  InputStream in = new FileInputStream(f1);
			  OutputStream out = new FileOutputStream(f2);

			  byte[] buf = new byte[1024];
			  int len;
			  while ((len = in.read(buf)) > 0){
				  out.write(buf, 0, len);
			  }
			  in.close();
			  out.close();
			  System.out.println("File copied.");
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
	
    private static boolean hasUnmatchedBrackets(String text) {
    	String[] lbrackets = new String[]{"\\[", "(", "{"};
    	String[] rbrackets = new String[]{"\\]", ")", "}"};
    	for(int i = 0; i<lbrackets.length; i++){
    		int left1 = text.replaceAll("[^"+lbrackets[i]+"]", "").length();
    		int right1 = text.replaceAll("[^"+rbrackets[i]+"]", "").length();
    		if(left1!=right1) return true;
    	}
		return false;
	}
	
	public static void main(String[] args) {
    	String text = "Trees, aromatic and resinous, glabrous or with simple hairs. Bark gray-brown, deeply furrowed; liquid, and Arabic ambar, amber] twigs and branches sometimes corky-winged. Dormant buds scaly, pointed, shiny, resinous, sessile. Leaves long-petiolate. Leaf blade fragrant when crushed, (3-)5(-7)-lobed, palmately veined, base deeply cordate to truncate, margins glandular-serrate, apex of each lobe long-acuminate. Inflorescences terminal, many-flowered heads; staminate heads in pedunculate racemes, each head a cluster of many stamens; pistillate heads pendent, long-pedunculate, the flowers � coalesced. Flowers unisexual, staminate and pistillate on same plant, appearing with leaves; calyx and corolla absent. Staminate flowers: anthers dehiscing longitudinally; staminodes absent. Pistillate flowers pale green to greenish yellow; staminodes 5-8; styles indurate and spiny in fruit, incurved. Capsules many, fused at base into long-pedunculate, spheric, echinate heads, 2-beaked, glabrous, septicidal. Seeds numerous, mostly aborting, 1-2 viable in each capsule, winged. x = 16.";
    	if(hasUnmatchedBrackets(text)) System.out.println("unmatched");
    }
}
