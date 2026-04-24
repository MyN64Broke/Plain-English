package PlainEnglish;

public class BooleanInterpreterDataType extends InterpreterDataType {
    public boolean value = false;

    public String toString() {
        return String.valueOf(value);
    }
    
    public void Assign(InterpreterDataType incoming) {
    	if(incoming instanceof BooleanInterpreterDataType bidt) {
    		value = bidt.value;
    	}else {
    		throw new RuntimeException("Trying to assign " + incoming + " to boolean.");
    	}
    }
}
