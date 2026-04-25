package PlainEnglish;
import java.util.HashMap;
import java.util.Map;

import PlainEnglish.AST.*;

/*
 * TODO:
 * evalExpression
 * set evaluations
 */
public class Interpreter {
    private final Program program;
    private HashMap<String, InterpreterDataType> variables;

    public Interpreter(Program prog) {
        program = prog;
        variables =  new HashMap<>();
    }

    public void Start() {
    	PrintBuiltInMethod print = new PrintBuiltInMethod();
    	program.method.add(print);
    }
    
    InterpreterDataType processMethod(Method method, HashMap<String, InterpreterDataType> params, InterpreterDataType obj) {
    	HashMap<String, InterpreterDataType> scope = params;
    	scope.putAll(variables);
    	if(method.with) {
    		if(obj instanceof ObjectInterpreterDataType o) {
        		for(Map.Entry<String, InterpreterDataType> entry : o.fields.entrySet()) {
        			scope.put(entry.getKey(), entry.getValue());
        		}
        		for(Statement state : method.statementblock.statement) {
        			scope = processStatement(state, scope);
        		}
        		for(Map.Entry<String, InterpreterDataType> entry : o.fields.entrySet()) {
        			o.fields.put(entry.getKey(), scope.get(entry.getKey()));
        		}
        		obj = o;
        	}else if(obj instanceof NumberInterpreterDataType n){
        		scope.put("Number", obj);
        		for(Statement state : method.statementblock.statement) {
        			scope = processStatement(state, scope);
        		}
        		obj = scope.get("Number");
        	}else if(obj instanceof StringInterpreterDataType s) {
        		scope.put("String", s);
        		for(Statement state : method.statementblock.statement) {
        			scope = processStatement(state, scope);
        		}
        		obj = scope.get("String");
        	}else if(obj instanceof BooleanInterpreterDataType b) {
        		scope.put("Boolean", b);
        		for(Statement state : method.statementblock.statement) {
        			scope = processStatement(state, scope);
        		}
        		obj = scope.get("Boolean");
        	}
    		HashMap<String, InterpreterDataType> temp = scope;
			temp.keySet().removeAll(variables.keySet());
			scope.keySet().removeAll(temp.keySet());
			variables = scope;
    		return obj;
    	}else {
    		for(Statement state : method.statementblock.statement) {
    			scope = processStatement(state, scope);
    		}
    		HashMap<String, InterpreterDataType> temp = scope;
			temp.keySet().removeAll(variables.keySet());
			scope.keySet().removeAll(temp.keySet());
			variables = scope;
    		return null;
    	}
    }
    
    HashMap<String, InterpreterDataType> processStatement(Statement s, HashMap<String, InterpreterDataType> scope) {
    	if(s.$if.orElse(null) instanceof If) {
    		If theIf = s.$if.get();
    		if(evalBooleanExpression(theIf.boolexpterm, scope)) {
    			StatementBlock block = theIf.statementblock;
    			HashMap<String, InterpreterDataType> localVariables = scope;
    			for(Statement state : block.statement) {
    				HashMap<String, InterpreterDataType> newVariables = processStatement(state, localVariables);
    				if(newVariables != null) {
    					localVariables.putAll(newVariables);
    				}
    			}
    			//Make values of scope change to what they were altered to
    			//In block without carrying over all of the block scope variables
    			HashMap<String, InterpreterDataType> temp = localVariables;
    			temp.keySet().removeAll(scope.keySet());
    			localVariables.keySet().removeAll(temp.keySet());
    			scope = localVariables;
    		}else if(theIf.$else) {
    			StatementBlock block = theIf.falseCase.get();
    			HashMap<String, InterpreterDataType> localVariables = scope;
    			for(Statement state : block.statement) {
    				HashMap<String, InterpreterDataType> newVariables = processStatement(state, localVariables);
    				if(newVariables != null) {
    					localVariables.putAll(newVariables);
    				}
    			}
    			//Make values of scope change to what they were altered to
    			//In block without carrying over all of the block scope variables
    			HashMap<String, InterpreterDataType> temp = localVariables;
    			temp.keySet().removeAll(scope.keySet());
    			localVariables.keySet().removeAll(temp.keySet());
    			scope = localVariables;
    		}
    		return scope;
    	}else if(s.loop.orElse(null) instanceof Loop) {
    		Loop theLoop = s.loop.get();
    		while(evalBooleanExpression(theLoop.boolexpterm, scope)) {
    			HashMap<String, InterpreterDataType> localVariables = scope;
    			for(Statement state : theLoop.statementblock.statement) {
    				HashMap<String, InterpreterDataType> newVariables = processStatement(state, localVariables);
    				if(newVariables != null) {
    					localVariables.putAll(newVariables);
    				}
    			}
    			//Make values of scope change to what they were altered to
    			//In block without carrying over all of the block scope variables
    			HashMap<String, InterpreterDataType> temp = localVariables;
    			temp.keySet().removeAll(scope.keySet());
    			localVariables.keySet().removeAll(temp.keySet());
    			scope = localVariables;
    		}
    		return scope;
    	}else if(s.set.orElse(null) instanceof Set) {
    		InterpreterDataType target = scope.get(s.set.get().variablereference.name);
    		if(target == null) {
    			if(s.set.get().variablereference.of) {
    				target = scope.get(s.set.get().variablereference.$object.get());
    				if(target == null) {
            			throw new RuntimeException("Variable \"" + s.set.get().variablereference.$object.get() + "\" does not exist in scope.");
        			}
    				if(target instanceof ObjectInterpreterDataType t) {
        				if(t.fields.containsKey(s.set.get().variablereference.name)) {
        					t.fields.put(s.set.get().variablereference.name, evalExpression(s.set.get().expression, scope));
        					target = t;
        				}else {
        					throw new RuntimeException("Field \"" + s.set.get().variablereference.name + "\" not present in object \"" + t + "\".");
        				}
        			}else {
        				throw new RuntimeException("Variable \"" + s.set.get().variablereference.name + "\" is not of type Object.");
        			}
    			}else {
    				throw new RuntimeException("Variable \"" + s.set.get().variablereference.name + "\" does not exist in scope.");
    			}
    		}else {
    			target.Assign(evalExpression(s.set.get().expression, scope));
    		}
    		scope.put(s.set.get().variablereference.name, target);
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
    		// Potential way to explicitly call print
    		if(funcCall.name.equals("print") || funcCall.name.equals("Print")) {
    			HashMap<String, InterpreterDataType> params = new HashMap<>();
				int i = 0;
				for(Expression e : funcCall.parameter) {
					params.put("term" + i, evalExpression(e, scope));
					i++;
				}
				PrintBuiltInMethod printBuiltInMethod = new PrintBuiltInMethod();
				printBuiltInMethod.Execute(params);
				return scope;
    		}
    		for(Method method : program.method) {
    			if(method.name == funcCall.name) {
    				HashMap<String, InterpreterDataType> params = new HashMap<>();
    				int i = 0;
    				for(Expression e : funcCall.parameter) {
    					params.put(method.parameter.get(i).nameOverride.orElse("param" + i), evalExpression(e, scope));
    					i++;
    				}
    				InterpreterDataType obj = null;
    				if(funcCall.obj.isPresent()) {
    					obj = scope.get(funcCall.obj.get());
    					obj = processFunctionCall(funcCall, method, params, obj);
    					scope.put(funcCall.obj.get(), obj);
    				}else {
    					obj = processFunctionCall(funcCall, method, params, obj);
    				}
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
	    			if(lhs instanceof NumberInterpreterDataType l2 && rhs instanceof NumberInterpreterDataType r2) {
	    				if(Float.valueOf(l2.value) != Float.valueOf(r2.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else if(lhs instanceof StringInterpreterDataType l2 && rhs instanceof StringInterpreterDataType r2) {
	    				if(!l2.value.equals(r2.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else if(lhs instanceof BooleanInterpreterDataType l2 && rhs instanceof BooleanInterpreterDataType r2) {
	    				if(l2.value != r2.value) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else if(lhs instanceof ObjectInterpreterDataType l2 && rhs instanceof ObjectInterpreterDataType r2) {
	    				if(!l2.type.equals(r2.type)) {
	    					return true;
	    				}else if(!l2.fields.equals(r2.fields)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else {
	    				throw new RuntimeException("Cannot compare different types with != operator.");
	    			}
	    		case greaterthanequal:
	    			if(lhs instanceof NumberInterpreterDataType l3 && rhs instanceof NumberInterpreterDataType r3) {
	    				if(Float.valueOf(l3.value) >= Float.valueOf(r3.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else {
	    				throw new RuntimeException(">= operator can only be used to compare expressions of type Number.");
	    			}
	    		case lessthanequal:
	    			if(lhs instanceof NumberInterpreterDataType l4 && rhs instanceof NumberInterpreterDataType r4) {
	    				if(Float.valueOf(l4.value) <= Float.valueOf(r4.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else {
	    				throw new RuntimeException("<= operator can only be used to compare expressions of type Number.");
	    			}
	    		case greaterthan:
	    			if(lhs instanceof NumberInterpreterDataType l5 && rhs instanceof NumberInterpreterDataType r5) {
	    				if(Float.valueOf(l5.value) > Float.valueOf(r5.value)) {
	    					return true;
	    				}else {
	    					return false;
	    				}
	    			}else {
	    				throw new RuntimeException("> operator can only be used to compare expressions of type Number.");
	    			}
	    		case lessthan:
	    			if(lhs instanceof NumberInterpreterDataType l6 && rhs instanceof NumberInterpreterDataType r6) {
	    				if(Float.valueOf(l6.value) <= Float.valueOf(r6.value)) {
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
    
    InterpreterDataType evalExpression(Expression e, HashMap<String, InterpreterDataType> scope) {
    	InterpreterDataType[] terms = new InterpreterDataType[e.term.size()];
    	int i = 0;
    	for(Term t : e.term) {
    		InterpreterDataType[] factors = new InterpreterDataType[t.factor.size()];
    		int j = 0;
    		for(Factor f : t.factor) {
    			if(f.number.isPresent()) {
    				NumberInterpreterDataType newNum = new NumberInterpreterDataType();
    				newNum.value = Float.parseFloat(f.number.get());
    				factors[j] = newNum;
    			}else if(f.variablereference.isPresent()) {
    				InterpreterDataType newType = scope.get(f.variablereference.get().name);
    				if(newType == null) {
    					if(f.variablereference.get().of) {
    						newType = scope.get(f.variablereference.get().$object.get());
    						if(newType == null) {
    							throw new RuntimeException("Variable \"" + f.variablereference.get().$object.get() + "\" does not exist in scope.");
    						}
    						if(newType instanceof ObjectInterpreterDataType o) {
    							if(o.fields.containsKey(f.variablereference.get().name)) {
    								newType = o.fields.get(f.variablereference.get().name);
    							}else {
    								throw new RuntimeException("Field \"" + f.variablereference.get().name + "\" does not exist in object \"" + o + "\".");
    							}
    						}else {
    							throw new RuntimeException("Variable \"" + f.variablereference.get().$object.get() + "\" is not of type Object.");
    						}
    					}else {
    						throw new RuntimeException("Variable \"" + f.variablereference.get().name + "\" does not exist in scope.");
    					}
    				}else {
    					factors[j] = newType;
    				}
    			}else if(f.$true) {
    				BooleanInterpreterDataType newBool = new BooleanInterpreterDataType();
    				newBool.value = true;
    				factors[j] = newBool;
    			}else if(f.$false) {
    				BooleanInterpreterDataType newBool = new BooleanInterpreterDataType();
    				newBool.value = false;
    				factors[j] = newBool;
    			}else if(f.stringliteral.isPresent()) {
    				StringInterpreterDataType newString = new StringInterpreterDataType();
    				newString.value = f.stringliteral.get();
    				factors[j] = newString;
    			}else if(f.characterliteral.isPresent()) {
    				StringInterpreterDataType newChar = new StringInterpreterDataType();
    				newChar.value = f.characterliteral.get();
    				factors[j] = newChar;
    			}else if(f.expression.isPresent()) {
    				InterpreterDataType newType = evalExpression(f.expression.get(), scope);
    				factors[j] = newType;
    			}else {
    				throw new RuntimeException("Invalid factor, no valid value.");
    			}
    			j++;
    		}
    		terms[i] = evalTerm(t, factors);
    		i++;
    	}
    	/*
    	 * Workable types:
    	 * Number/Number: Number
    	 * Number/String: String
    	 * 
    	 * String/String: String
    	 * String/Number: String
    	 * String/Boolean: String
    	 * 
    	 * Boolean/Boolean: Boolean
    	 * Boolean/String: String
    	 */
    	InterpreterDataType expAnswer = terms[0];
    	for(i = 1; i < terms.length; i++) {
    		InterpreterDataType nextTerm = terms[i];
    		plusORhyphen theplusORhyphen = e.theplusORhyphen.get(i);
    		if(expAnswer instanceof NumberInterpreterDataType n) {
    			if(nextTerm instanceof NumberInterpreterDataType n2) {
    				switch(theplusORhyphen) {
    					case plus:
    						n.value += n2.value;
    						expAnswer = n;
    						break;
    					case hyphen:
    						n.value -= n2.value;
    						expAnswer = n;
    						break;
    					default:
    						throw new RuntimeException("A + or - is required to operate between two terms in an expression.");
    				}
    			}else if(nextTerm instanceof StringInterpreterDataType s) {
    				switch(theplusORhyphen) {
						case plus:
							s.value = s.value.concat(Float.toString(n.value));
							expAnswer = s;
							break;
						case hyphen:
							throw new RuntimeException("Cannot subtract String from Number.");
						default:
							throw new RuntimeException("A + or - is required to operate between two terms in an expression.");
    				}
    			}else {
    				throw new RuntimeException("Data type Number can only be added with Number and String.");
    			}
    		}else if(expAnswer instanceof StringInterpreterDataType s) {
    			if(nextTerm instanceof NumberInterpreterDataType n) {
    				switch(theplusORhyphen) {
						case plus:
							s.value = s.value.concat(Float.toString(n.value));
							expAnswer = s;
							break;
						case hyphen:
							throw new RuntimeException("Cannot subtract String.");
						default:
							throw new RuntimeException("A + or - is required to operate between two terms in an expression.");
    				}
    			}else if(nextTerm instanceof StringInterpreterDataType s2) {
    				switch(theplusORhyphen) {
						case plus:
							s.value = s.value.concat(s2.value);
							expAnswer = s;
							break;
						case hyphen:
							throw new RuntimeException("Cannot subtract from String.");
						default:
							throw new RuntimeException("A + or - is required to operate between two terms in an expression.");
    				}
    			}else if(nextTerm instanceof BooleanInterpreterDataType b) {
    				switch(theplusORhyphen) {
						case plus:
							s.value = s.value.concat(b.toString());
							expAnswer = s;
							break;
						case hyphen:
							throw new RuntimeException("Cannot subtract from String.");
						default:
							throw new RuntimeException("A + or - is required to operate between two terms in an expression.");
    				}	
    			}else {
    				throw new RuntimeException("Term cannot be evaluated to any primitive type.");
    			}
    		}else if(expAnswer instanceof BooleanInterpreterDataType b) {
    			if(nextTerm instanceof StringInterpreterDataType s) {
    				s.value = s.value.concat(b.toString());
    				expAnswer = s;
    			}else if(nextTerm instanceof BooleanInterpreterDataType b2) {
    				switch(theplusORhyphen) {
						case plus:
							if(b.value || b2.value) {
								b.value = true;
							}else {
								b.value = false;
							}
							expAnswer = b;
							break;
						case hyphen:
							if(b.value && !b2.value) {
								b.value = true;
							}else {
								b.value = false;
							}
							expAnswer = b;
							break;
						default:
							throw new RuntimeException("A + or - is required to operate between two terms in an expression.");
    				}
    			}else {
    				throw new RuntimeException("Data type Boolean can only be added with Boolean and String.");
    			}
    		}else {
    			throw new RuntimeException("Term cannot be resolved to any primitive type.");
    		}
    	}
    	return expAnswer;
    }
    
    InterpreterDataType evalTerm(Term term, InterpreterDataType[] factors) {
    	if(factors.length == 1) {
    		return factors[0];
    	}
    	if(!(factors[0] instanceof NumberInterpreterDataType)) {
			throw new RuntimeException("Operations *, /, % only compatible with type Number.");
		}
    	NumberInterpreterDataType currAnswer = (NumberInterpreterDataType) factors[0];
    	for(int i = 1; i < factors.length; i++) {
    		if(!(factors[i] instanceof NumberInterpreterDataType)) {
    			throw new RuntimeException("Operations *, /, % only compatible with type Number.");
    		}
    		NumberInterpreterDataType nextTerm = (NumberInterpreterDataType) factors[i];
    		asteriskORslashORpercent theASP = term.theasteriskORslashORpercent.get(i - 1);
    		switch(theASP) {
	    		case asterisk:
	    			currAnswer.value = currAnswer.value * nextTerm.value;
	    			break;
	    		case slash:
	    			currAnswer.value = currAnswer.value / nextTerm.value;
	    			break;
	    		case percent:
	    			currAnswer.value = currAnswer.value % nextTerm.value;
	    			break;
	    		default:
	    			throw new RuntimeException("There is no *, / or % between factors in term.");
    		}
    	}
    	return currAnswer;
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
    
    InterpreterDataType processFunctionCall(FunctionCall funcCall, Method method, HashMap<String, InterpreterDataType> params, InterpreterDataType obj) {
    	//object is required by function
    	if(method.className.isPresent()) {
    		if(funcCall.obj.isEmpty()) {
    			//object needed in method, not specified in funcCall
    			throw new RuntimeException("Function \"" + method.name + "\" requires an object be passed in to alter.");
    		}
    	//object is not required by function
    	}else {
    		if(funcCall.obj.isPresent()) {
    			//object not required by function, funcCall has an object
    			throw new RuntimeException("Function \"" + method.name + "\" does not require an object, but is sent one.");
    		}
    	}
    	if(method.with) {
	    	//Check amount of parameters
	    	if(method.parameter.size() != funcCall.parameter.size()) {
	    		throw new RuntimeException("Incorrect number of parameters for function \"" + method.name +"\". Number of parameters required: " + method.parameter.size());
	    	}
	    	int i = 0;
	    	//Type-check parameters
	    	for(Parameter p : method.parameter) {
	    		InterpreterDataType type = params.get(method.parameter.get(i).nameOverride.orElse("param" + i));
	    		findType:
	    		switch(p.paramType) {
		    		case "number", "Number":
		    			if(!(type instanceof NumberInterpreterDataType)) {
		    				throw new RuntimeException("Parameter \"" + p.nameOverride + "\" in function \"" + method.name + "\" must be of type " + p.paramType + ".");
		    			}
		    			break;
		    		case "string", "String":
		    			if(!(type instanceof StringInterpreterDataType)) {
		    				throw new RuntimeException("Parameter \"" + p.nameOverride + "\" in function \"" + method.name + "\" must be of type " + p.paramType + ".");
		    			}
		    			break;
		    		case "boolean", "Boolean", "bool", "Bool":
		    			if(!(type instanceof BooleanInterpreterDataType)) {
		    				throw new RuntimeException("Parameter \"" + p.nameOverride + "\" in function \"" + method.name + "\" must be of type " + p.paramType + ".");
		    			}
		    			break;
	    			default:
	    				if(type instanceof ObjectInterpreterDataType t) {
		    				for(TypeDef typedef : program.typedef) {
		    					if(typedef.name.equals(p.paramType)) {
		    						if(typedef.name.equals(t.type)) {
		    							break findType;
		    						}else {
		    							throw new RuntimeException("Parameter \"" + p.nameOverride + "\" in function \"" + method.name + "\" must be of type " + p.paramType + ".");
		    						}
		    					}
		    				}
		    				throw new RuntimeException("Date type " + p.paramType + " does not exist.");
	    				}else {
	    					throw new RuntimeException("Parameter \"" + p.nameOverride + "\" in function \"" + method.name + "\" must be of type " + p.paramType + ".");
	    				}
	    		}
	    		i++;
	    	}
	    //No parameters required, but parameters given
    	}else if(funcCall.parameter.size() > 0){
    		throw new RuntimeException("Function \"" + method.name + "\" takes no parameters, function call has parameters.");
    	}
    	//Do the function
    	obj = processMethod(method, params, obj);
    	return obj;
    }
}
