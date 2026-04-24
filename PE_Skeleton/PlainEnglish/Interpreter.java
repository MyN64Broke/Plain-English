package PlainEnglish;
import java.util.HashMap;
import java.util.Map;

import PlainEnglish.AST.*;

public class Interpreter {
    private final Program program;
    private HashMap<String, InterpreterDataType> variables;

    public Interpreter(Program prog) {
        program = prog;
        variables =  new HashMap<>();
    }

    public void Start() {
    	
    }
    
    HashMap<String, InterpreterDataType> processStatement(Statement s, HashMap<String, InterpreterDataType> scope) {
    	if(s.$if.orElse(null) instanceof If) {
    		If theIf = s.$if.get();
    		if(evalBooleanExpression(theIf.boolexpterm, scope)) {
    			//TODO process statements of theIf.statementblock
    		}else if(theIf.$else) {
    			//TODO process statements of theIf.falseCase
    		}
    		return scope;
    	}else if(s.loop.orElse(null) instanceof Loop) {
    		return scope;
    	}else if(s.set.orElse(null) instanceof Set) {
    		InterpreterDataType target = scope.get(s.set.get().variablereference.name);
    		if(target == null) {
    			if(s.set.get().variablereference.of) {
    				target = scope.get(s.set.get().variablereference.$object);
    				//TODO
    			}
    			if(target == null) {
        			throw new RuntimeException("Variable " + target + " not declared before assignment.");
    			}
    			if(target instanceof ObjectInterpreterDataType t) {
    				if(t.fields.containsKey(s.set.get().variablereference.name)) {
    					//TODO
    					t.fields.put(s.set.get().variablereference.name, evalExpression(s.set.get().expression, scope));
    				}else {
    					throw new RuntimeException("Field " + s.set.get().variablereference.name + " not present in object " + t);
    				}
    			}
    		}else {
    			//TODO
    			target.Assign(evalExpression(s.set.get().expression, scope));
    		}
    		return scope;
    	}else if(s.make.orElse(null) instanceof Make) {
    		Make makeStatement = s.make.get();
    		if(scope.containsKey(makeStatement.name)) {
    			throw new RuntimeException("Variable " + makeStatement.name + " already exists in scope.");
    		}
    		findType:
    		switch (makeStatement.type) {
    			case "number", "Number":
    				NumberInterpreterDataType newNum = new NumberInterpreterDataType();
    				scope.put(makeStatement.name, newNum);
    				break;
    			case "string", "String":
    				StringInterpreterDataType newString = new StringInterpreterDataType();
    				scope.put(makeStatement.name, newString);
    				break;
    			case "boolean", "Boolean", "bool", "Bool":
    				BooleanInterpreterDataType newBool = new BooleanInterpreterDataType();
    				scope.put(makeStatement.name, newBool);
    				break;
    			default:
    				for(TypeDef typedef : program.typedef) {
    					if(typedef.name == makeStatement.name) {
    						ObjectInterpreterDataType newObject = new ObjectInterpreterDataType(); 
    						newObject = makeObjectVariable(newObject, typedef);
    						scope.put(s.make.get().name, newObject);
    						break findType;
    					}
    				}
    				throw new RuntimeException("Date type " + makeStatement.type + " does not exist.");
    		}
    		return scope;
    	}else if(s.functioncall.orElse(null) instanceof FunctionCall) {
    		FunctionCall funcCall = s.functioncall.get();
    		for(Method method : program.method) {
    			if(method.name == funcCall.name) {
    				int numParams = funcCall.parameter.size();
    				InterpreterDataType[] params = new InterpreterDataType[numParams];
    				for(int i = 0; i < numParams; i++) {
    					params[i] = evalExpression(funcCall.parameter.get(i), scope);
    				}
    				processFunctionCall(funcCall, params);
    				return scope;
    			}
    		}
    		throw new RuntimeException("Function " + funcCall.name + " does not exist.");
    	}else {
    		throw new RuntimeException("Unknown statement type.");
    	}
    }
    
    boolean evalBooleanExpression(BoolExpTerm exp, HashMap<String, InterpreterDataType> scope) {
    	if(exp.not) {
    		return evalBooleanExpression(exp.notTerm.get(), scope);
    	}
    	boolean firstTerm = evalBooleanFactor(exp.boolexpfactor.get(), scope);
    	if(exp.boolexpterm.size() > 0) {
    		int i = 0;
    		while(i < exp.boolexpterm.size()) {
    			boolean nextTerm = evalBooleanExpression(exp.boolexpterm.get(i), scope);
    			switch(exp.theandORor.get(i)) {
    				case and:
    					if(firstTerm && nextTerm) {
    						firstTerm = true;
    					}else {
    						firstTerm = false;
    					}
    					break;
    				case or:
    					if(firstTerm || nextTerm) {
    						firstTerm = true;
    					}else {
    						firstTerm = false;
    					}
    					break;
    			}
    			i++;
    		}
    	}
    	return firstTerm;
    }
    
    boolean evalBooleanFactor(BoolExpFactor factor, HashMap<String, InterpreterDataType> scope) {
    	if(factor.variablereference.isPresent()) {
    		String varRefName = factor.variablereference.get().name;
    		InterpreterDataType ref;
    		if(scope.containsKey(varRefName)) {
    			ref = scope.get(varRefName);
    		}else {
    			throw new RuntimeException("Variable " + varRefName + " does not exist in scope.");
    		}
    		if(ref instanceof BooleanInterpreterDataType b) {
    			return b.value;
    		}else {
    			throw new RuntimeException("Variable " + varRefName + " is not of type 'boolean'");
    		}
    	}else {
    		InterpreterDataType lhs = evalExpression(factor.lhs.get(), scope);
    		InterpreterDataType rhs = evalExpression(factor.rhs.get(), scope);
    		switch(factor.thecompareOps.get()) {
	    		case doubleequal:
	    			if(lhs instanceof NumberInterpreterDataType l && rhs instanceof NumberInterpreterDataType r) {
	    				if(Float.valueOf(l.value) == Float.valueOf(r.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else if(lhs instanceof StringInterpreterDataType l && rhs instanceof StringInterpreterDataType r) {
	    				if(l.value.equals(r.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else if(lhs instanceof BooleanInterpreterDataType l && rhs instanceof BooleanInterpreterDataType r) {
	    				if(l.value == r.value) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else if(lhs instanceof ObjectInterpreterDataType l && rhs instanceof ObjectInterpreterDataType r) {
	    				if(l.type.equals(r.type)) {
	    					if(l.fields.equals(r.fields)) {
	    						return true;
	    					}else {
	    						return false;
	    					}
	    				}else {
	    					return false;
	    				}
	    			}else {
	    				throw new RuntimeException("Cannot compare different types with == operator.");
	    			}
	    		case notequal:
	    			if(lhs instanceof NumberInterpreterDataType l && rhs instanceof NumberInterpreterDataType r) {
	    				if(Float.valueOf(l.value) != Float.valueOf(r.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else if(lhs instanceof StringInterpreterDataType l && rhs instanceof StringInterpreterDataType r) {
	    				if(!l.value.equals(r.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else if(lhs instanceof BooleanInterpreterDataType l && rhs instanceof BooleanInterpreterDataType r) {
	    				if(l.value != r.value) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else if(lhs instanceof ObjectInterpreterDataType l && rhs instanceof ObjectInterpreterDataType r) {
	    				if(!l.type.equals(r.type)) {
	    					return true;
	    				}else if(!l.fields.equals(r.fields)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else {
	    				throw new RuntimeException("Cannot compare different types with != operator.");
	    			}
	    		case greaterthanequal:
	    			if(lhs instanceof NumberInterpreterDataType l && rhs instanceof NumberInterpreterDataType r) {
	    				if(Float.valueOf(l.value) >= Float.valueOf(r.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else {
	    				throw new RuntimeException(">= operator can only be used to compare expressions of type Number.");
	    			}
	    		case lessthanequal:
	    			if(lhs instanceof NumberInterpreterDataType l && rhs instanceof NumberInterpreterDataType r) {
	    				if(Float.valueOf(l.value) <= Float.valueOf(r.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else {
	    				throw new RuntimeException("<= operator can only be used to compare expressions of type Number.");
	    			}
	    		case greaterthan:
	    			if(lhs instanceof NumberInterpreterDataType l && rhs instanceof NumberInterpreterDataType r) {
	    				if(Float.valueOf(l.value) > Float.valueOf(r.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else {
	    				throw new RuntimeException("> operator can only be used to compare expressions of type Number.");
	    			}
	    		case lessthan:
	    			if(lhs instanceof NumberInterpreterDataType l && rhs instanceof NumberInterpreterDataType r) {
	    				if(Float.valueOf(l.value) <= Float.valueOf(r.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else {
	    				throw new RuntimeException("< operator can only be used to compare expressions of type Number.");
	    			}
	    		default:
	    			throw new RuntimeException("How is there no comapirson op??? How did the parser not catch this???");
    		}
    	}
    	
    }
    
    //TODO
    InterpreterDataType evalExpression(Expression e, HashMap<String, InterpreterDataType> scope) {
    	InterpreterDataType d = new NumberInterpreterDataType();
    	return d;
    }
    
    //TODO
    float evalIntegerExpression(Expression e) {
    	float result = 0;
    	
    	return result;
    }

    ObjectInterpreterDataType makeObjectVariable(ObjectInterpreterDataType newObject, TypeDef typedef) {
    	for(Field field : typedef.field) {
    		findType:
	    	switch (field.type) {
				case "number", "Number":
					NumberInterpreterDataType newNum = new NumberInterpreterDataType();
					newObject.fields.put(field.name, newNum);
					break;
				case "string", "String":
					StringInterpreterDataType newString = new StringInterpreterDataType();
					newObject.fields.put(field.name, newString);
					break;
				case "boolean", "Boolean", "bool", "Bool":
					BooleanInterpreterDataType newBool = new BooleanInterpreterDataType();
					newObject.fields.put(field.name, newBool);
					break;
				default:
					for(TypeDef fieldTypedef : program.typedef) {
    					if(fieldTypedef.name == typedef.name) {
    						ObjectInterpreterDataType newFieldObject = new ObjectInterpreterDataType(); 
    						newFieldObject = makeObjectVariable(newFieldObject, fieldTypedef);
    						newObject.fields.put(field.name, newFieldObject);
    						break findType;
    					}
    				}
    				throw new RuntimeException("Date type " + field.type + " does not exist.");
	    	}
    	}
    	return newObject;
    }
    
    InterpreterDataType[] processFunctionCall(FunctionCall funcCall, InterpreterDataType[] params) {
    	
    }
}
