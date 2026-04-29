package PlainEnglish;

import java.util.HashMap;
import java.util.Map;

public class ObjectInterpreterDataType extends InterpreterDataType {
    public String type;
    public HashMap<String, InterpreterDataType> fields = new HashMap<String, InterpreterDataType>();

    public String toString() {
        var sb = new StringBuilder();
        sb.append(type);
        for (var item : fields.keySet())
            sb.append("    ").append(item).append(" = ").append(fields.get(item));
        return sb.toString();
    }
    
    public void Assign(InterpreterDataType incoming) {
    	if(incoming instanceof ObjectInterpreterDataType o) {
    		if(o.type.equals(type)) {
    			fields.putAll(o.fields);
    		}else {
    			throw new RuntimeException("Trying to assign " + o.type + " to " + type);
    		}
    	}else {
    		throw new RuntimeException("Trying to assign " + incoming + " to " + type);
    	}
    }
}
