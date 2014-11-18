package edu.arizona.biosemantics.semanticmarkup.io.validate.lib;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import edu.arizona.biosemantics.semanticmarkup.io.validate.IVolumeValidator;
import edu.arizona.biosemantics.common.log.LogLevel;


/**
 * WordVolumeValidator validates input against Microsoft Word format
 * @author rodenhausen
 */
public class WordVolumeValidator implements IVolumeValidator {

	/*@Override
	public boolean validate(File file) {
		if(!file.isFile())
			return false;
		
		try {
			new XWPFDocument(new FileInputStream(file));
			return true;
		} catch (Exception e) {
			try {
				new HWPFDocument(new FileInputStream(file));
				return true;
			} catch (Exception e2) {
				log(LogLevel.DEBUG, e + " " + e2);
				return false;
			}
		}
	}*/
	
	@Override
	public boolean validate(List<File> file) {
		try {
			new XWPFDocument(new FileInputStream(file.get(0)));
			return true;
		} catch (Exception e) {
			try {
				new HWPFDocument(new FileInputStream(file.get(0)));
				return true;
			} catch (Exception e2) {
				log(LogLevel.DEBUG, e + " " + e2);
				return false;
			}
		}
	}
}
