package edu.arizona.biosemantics.semanticmarkup.log;

import java.util.HashMap;

/**
 * Timer can accumulate times (e.g. how much computation is used for parsing)
 * @author rodenhausen
 */
public class Timer {

	private static HashMap<String, Long> times = new HashMap<String, Long>();
	
	/**
	 * @param time to add
	 */
	public synchronized static void addParseTime(long time) {
		if(times.containsKey("parse")) 
			times.put("parse", times.get("parse") + time);
		else
			times.put("parse", time);
	}
	
	/**
	 * @return the time
	 */
	public static long getParseTime() {
		return times.get("parse");
	}
	
}
