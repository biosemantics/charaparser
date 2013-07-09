package semanticMarkup.io.validate.lib;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import semanticMarkup.io.validate.IVolumeValidator;
import semanticMarkup.log.LogLevel;

/**
 * WordVolumeValidator validates input against Microsoft Word format
 * @author rodenhausen
 */
public class WordVolumeValidator implements IVolumeValidator {

	@Override
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
	}
}
