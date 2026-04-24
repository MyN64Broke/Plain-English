package PlainEnglish;

public class NumberInterpreterDataType extends InterpreterDataType {
    public float value;

    public String toString() {
        return String.valueOf(value);
    }
    
    public void Assign(InterpreterDataType incoming) {
    	if(incoming instanceof NumberInterpreterDataType nidt) {
    		value = nidt.value;
    	}else {
    		throw new RuntimeException("Trying to assign " + incoming + " to number.");
    	}
    }
}