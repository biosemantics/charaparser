package edu.arizona.biosemantics.semanticmarkup.enhance.transform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParenthesisRemover {

	public String remove(String text, char parenthesisOpenCharacter, char parenthesisCloseCharacter) {
		//"\\([^()]*\\)";
        String expr = "\\([^\\" + parenthesisOpenCharacter + "\\" + parenthesisCloseCharacter + "]*\\)";// "\\" + parenthesisOpenCharacter + "[^+ " + parenthesisOpenCharacter + parenthesisCloseCharacter + "]*\\)";
        Pattern p = Pattern.compile(expr);
        Matcher m = p.matcher(text);
        while (m.find()) {
            text = m.replaceAll("");
            m = p.matcher(text);
        }
		return text;
	}
	
}
