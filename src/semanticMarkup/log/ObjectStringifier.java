package semanticMarkup.log;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 * A singleton ObjectStringifier transforms objects into String representations by using Jackson
 * @author rodenhausen
 */
public class ObjectStringifier {

	private static ObjectStringifier instance;
	
	/**
	 * @return the singleton instance of ObjectStringifier
	 */
	public static ObjectStringifier getInstance() {
		if(instance == null) 
			instance = new ObjectStringifier();
		return instance;
	}
	
	private ObjectMapper mapper;
	private ObjectWriter writer;
	
	/**
	 * 
	 */
	private ObjectStringifier() { 
		this.mapper  = new ObjectMapper();
		this.writer = mapper.writerWithDefaultPrettyPrinter();
	}
	
	/**
	 * synchronization necessary, even though ObjectMapper and ObjectWriter JavaDoc state that they are thread-safe
	 * It might not be used in the right way then in this case. If removed and more threads print JSonMappingException is thrown for DescriptionTreatmentElement['name']
	 * @param object
	 * @return stringified object
	 */
	public synchronized String stringify(Object object) {
		try {
			return writer.writeValueAsString(object);
		} catch (Exception e) {
			log(LogLevel.ERROR, e);
			return null;
		}
	}
	
}
