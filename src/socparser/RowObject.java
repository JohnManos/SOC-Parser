package socparser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class RowObject {
	/*public String course = "";
	public String gordonRule = "";
	public String genEd = "";
	public String section = "";
	public String classNum = "";
	public String minMaxCred = "";
	public String days = "";
	public String time = "";
	public String meetingPattern = "";
	public String spec = "";
	public String soc = "";
	public String courseTitle = "";
	public String instructor = "";
	public String enrCap = "";
	public String schedCodes = "";*/
	
	private LinkedHashMap<String, String> fields = new LinkedHashMap<String, String>();
	
	public void add(String label, String value) {
		fields.put(label, value);
	}
	
	public Set<Map.Entry<String, String>> getData() {
		return fields.entrySet();
	}
}
