package PlainEnglish;

public class StringInterpreterDataType extends InterpreterDataType {
    public String value = "";

    public String toString() {
        return value;
    }
    
    public void Assign(InterpreterDataType incoming) {
    	if(incoming instanceof StringInterpreterDataType sidt) {
    		value = sidt.value;
    	}else {
    		throw new RuntimeException("Trying to assign " + incoming + " to string.");
    	}
    }
}