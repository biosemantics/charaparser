package semanticMarkup.log;

import java.util.HashMap;

public class Timer {

	private static HashMap<String, Long> times = new HashMap<String, Long>();
	
	public synchronized static void addParseTime(long time) {
		if(times.containsKey("parse")) 
			times.put("parse", times.get("parse") + time);
		else
			times.put("parse", time);
	}
	
	public static long getParseTime() {
		return times.get("parse");
	}
	
}
