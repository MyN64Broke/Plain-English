package PlainEnglish;

import java.util.LinkedList;
import java.util.Optional;

import PlainEnglish.Token.TokenTypes;
import PlainEnglish.AST.*;

public class PlainEnglishParser {
	
	private final TokenManager tm;
	
	public PlainEnglishParser(LinkedList<Token> tokens) {
		this.tm = new TokenManager(tokens);
	}
	
	private void RequireNewline() throws SyntaxErrorException {
		if(tm.MatchAndRemove(TokenTypes.NEWLINE).isEmpty()) {
			throw new SyntaxErrorException("NEWLINE token required.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		while(tm.MatchAndRemove(TokenTypes.NEWLINE).isPresent()) {
			//Do nothing
		}
	}
	
	public Optional<Program> Program() throws SyntaxErrorException{
		Program program = new Program();
		while(tm.Peek(0).isPresent()) {
			if(tm.Peek(0).get().Type == TokenTypes.NEWLINE) {
				tm.MatchAndRemove(TokenTypes.NEWLINE);
			}else if(tm.Peek(0).get().getType() == TokenTypes.A || tm.Peek(0).get().getType() == TokenTypes.AN ) {
				program.typedef.add(TypeDef().get());
			}else if(tm.Peek(0).get().getType() == TokenTypes.TO) {
				program.method.add(Method().get());
			}
			//Potential setup for what the program would look like
			//If more it was more reliant upon Opitional.empty()
			/*
			while(true) {
				if(tm.Peek(0).get().Type == TokenTypes.NEWLINE) {
					tm.MatchAndRemove(TokenTypes.NEWLINE);
					break;
				}
				Optional<TypeDef> typedef = TypeDef();
				if(typedef.isPresent()) {
					program.typedef.add(typedef.get());
					break;
				}
				Optional<Method> method = Method();
				if(method.isPresent()) {
					program.method.add(method.get());
					break;
				}
				throw new SyntaxErrorException("This token does not constitute valid syntax for a TypeDef, Method or Newline.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			*/
		}
		return Optional.of(program);
	}
	
	public Optional<TypeDef> TypeDef() throws SyntaxErrorException{
		TypeDef typedef = new TypeDef();
		if(tm.MatchAndRemove(TokenTypes.A).isEmpty() && tm.MatchAndRemove(TokenTypes.AN).isEmpty()) {
			return Optional.empty();
		}
		Optional<Token> name = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
		if(name.isEmpty()) {
			throw new SyntaxErrorException("TypeDef must have a name.", tm.getCurrentLine(), tm.getCurrentColumn());
		}else {
			typedef.name = name.get().Value.get();
		}
		if(tm.MatchAndRemove(TokenTypes.IS).isEmpty()) {
			int line = tm.getCurrentLine();
			int col = tm.getCurrentColumn();
			throw new SyntaxErrorException("Unexpected Token at: " + line + ", " + col + ". Expected token IS for TypeDef", line, col);
		}
		RequireNewline();
		if(tm.MatchAndRemove(TokenTypes.INDENT).isEmpty()) {
			throw new SyntaxErrorException("Must have indent formatting for TypeDef.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		while(tm.Peek(1).get().Type != TokenTypes.DEDENT) {
			Optional<Field> field = Field();
			if(field.isEmpty()) {
				throw new SyntaxErrorException("Missing Field in TypeDef.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			typedef.field.add(field.get());
		}
		if(tm.MatchAndRemove(TokenTypes.DEDENT).isEmpty()) {
			throw new SyntaxErrorException("Must have dedent formatting for TypeDef.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		while(tm.Peek(1).get().Type == TokenTypes.NEWLINE) {
			tm.MatchAndRemove(TokenTypes.NEWLINE);
		}
		return Optional.of(typedef);
	}
	
	public Optional<Field> Field() throws SyntaxErrorException{
		Field field = new Field();
		Optional<Token> type = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
		if(type.isEmpty()) {
			throw new SyntaxErrorException("Field must have a type.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		field.type = type.get().Value.get();
		Optional<Token> name = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
		if(name.isEmpty()) {
			throw new SyntaxErrorException("Field must have a name.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		field.name = name.get().Value.get();
		RequireNewline();
		return Optional.of(field);
	}
	
	public Optional<Method> Method() throws SyntaxErrorException {
		Method method = new Method();
		if(tm.MatchAndRemove(TokenTypes.TO).isEmpty()) {
			throw new SyntaxErrorException("Method Definition must start with To token.",tm.getCurrentLine(), tm.getCurrentColumn());
		}
		Optional<Token> name = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
		if(name.isEmpty()) {
			throw new SyntaxErrorException("Method Definition must have a name.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		method.name = name.get().Value.get();
		if(tm.MatchAndRemove(TokenTypes.A).isPresent()) {
			name = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
			if(name.isEmpty()) {
				throw new SyntaxErrorException("Expected className after 'A' token.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			method.className = name.get().Value;
		}
		if(tm.MatchAndRemove(TokenTypes.WITH).isPresent()) {
			method.with = true;
			Optional<Parameter> parameter = Parameter();
			if(parameter.isEmpty()) {
				throw new SyntaxErrorException("Method Definition including 'with' needs at least one parameter.", tm.getCurrentLine(), tm.getCurrentColumn());	
			}
			method.parameter.add(parameter.get());
			while(tm.MatchAndRemove(TokenTypes.COMMA).isPresent()) {
				parameter = Parameter();
				if(parameter.isEmpty()) {
					throw new SyntaxErrorException("Parameter must follow a ','.", tm.getCurrentLine(), tm.getCurrentColumn());
				}
				method.parameter.add(parameter.get());
			}
		}else {
			method.with = false;
		}
		RequireNewline();
		Optional<StatementBlock> statementblock = StatementBlock();
		if(statementblock.isEmpty()) {
			throw new SyntaxErrorException("Method must have at least one statement.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		method.statementblock = statementblock.get();
		return Optional.of(method);
	}
	
	public Optional<Parameter> Parameter() throws SyntaxErrorException {
		Parameter parameter = new Parameter();
		Optional<Token> paramType = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
		if(paramType.isEmpty()) {
			throw new SyntaxErrorException("Parameter must have a type.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		parameter.paramType = paramType.get().Value.get();
		if(tm.MatchAndRemove(TokenTypes.NAMED).isPresent()) {
			parameter.named = true;
			Optional<Token> name = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
			if(name.isEmpty()) {
				throw new SyntaxErrorException("Name override with 'named' must have Identifier afterward.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			parameter.nameOverride = name.get().Value;
		}else {
			parameter.named = false;
		}
		return Optional.of(parameter);
	}
	
	public Optional<StatementBlock> StatementBlock() throws SyntaxErrorException {
		StatementBlock statementblock = new StatementBlock();
		if(tm.MatchAndRemove(TokenTypes.INDENT).isEmpty()) {
			throw new SyntaxErrorException("Statement block must start with an indent.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		while(tm.MatchAndRemove(TokenTypes.DEDENT).isEmpty()) {
			Optional<Statement> statement = Statement();
			if(statement.isPresent()) {
				statementblock.statement.add(statement.get());
			}
		}
		return Optional.of(statementblock);
	}
	
	public Optional<Statement> Statement() throws SyntaxErrorException {
		Statement statement = new Statement();
		if(tm.Peek(0).get().Type == TokenTypes.IF) {
			statement.$if = If();
		}else if(tm.Peek(0).get().Type == TokenTypes.LOOP) {
			statement.loop = Loop();
		}else if(tm.Peek(0).get().Type == TokenTypes.SET) {
			statement.set = Set();
		}else if(tm.Peek(0).get().Type == TokenTypes.MAKE) {
			statement.make = Make();
		}else if(tm.Peek(0).get().Type == TokenTypes.IDENTIFIER) {
			statement.functioncall = FunctionCall();
		}else {
			throw new SyntaxErrorException("No valid statement tokens: If, Loop, Set, Make or Function Call", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		return Optional.of(statement);
	}
	
	public Optional<If> If() throws SyntaxErrorException{
		/*
		If $if = new If();
		if(tm.MatchAndRemove(TokenTypes.IF).isEmpty()) {
			//How we get here, I don't know
			throw new SyntaxErrorException("If statement must start with 'if'.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		Optional<BoolExpTerm> boolExpTerm = BoolExpTerm();
		if(boolExpTerm.isEmpty()) {
			throw new SyntaxErrorException("If statement needs Boolean expression after start.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		$if.boolexpterm = boolExpTerm.get();
		RequireNewline();
		Optional<StatementBlock> statementblock = StatementBlock();
		if(statementblock.isEmpty()) {
			throw new SyntaxErrorException("If statement must have at least one statement in block.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		$if.statementblock = statementblock.get();
		if(tm.MatchAndRemove(TokenTypes.ELSE).isPresent()) {
			$if.$else = true;
			if(tm.MatchAndRemove(TokenTypes.NEWLINE).isEmpty()) {
				throw new SyntaxErrorException("Newline token is required after else statement.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			statementblock = StatementBlock();
			if(statementblock.isEmpty()) {
				throw new SyntaxErrorException("Else statement must have at least one statement in block.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			$if.falseCase = statementblock;	
		}else{
			$is.$else = false;
		}
		return Optional.of($if);
		*/
		return null;
	}
	
	public Optional<BoolExpTerm> BoolExpTerm() throws SyntaxErrorException{
		/*
		BoolExpTerm boolexpterm = new BoolExpTerm();
		Optional<BoolExpFactor> boolexpfactor = BoolExpFactor();
		if(boolexpfactor.isEmpty()) {
			throw new SyntaxErrorException("Boolean expression term must have at least one boolean expression factor.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		boolexpterm.boolexpfactor = boolexpfactor;
		if(tm.Peek(0).get().Type == TokenTypes.AND || tm.Peek(0).get().Type == TokenTypes.OR) {
			boolexpterm.not = false;
			while(tm.Peek(0).get().Type == TokenTypes.AND || tm.Peek(0).get().Type == TokenTypes.OR) {
				if(tm.MatchAndRemove(TokenTypes.AND).isPresent()) {
					boolexpterm.theandORor.add(andORor.and);
				}else if(tm.MatchAndRemove(TokenTypes.OR).isPresent()) {
					boolexpterm.theandORor.add(andORor.or);
				}
				Optional<BoolExpTerm> newTerm = BoolExpTerm();
				if(newTerm.isEmpty()) {
					throw new SyntaxErrorException("AND or OR token must be followed by a boolean expression.", tm.getCurrentLine(), tm.getCurrentColumn());
				}
				boolexpterm.boolexpterm.add(newTerm.get());
			}
		}else if(tm.MatchAndRemove(TokenTypes.NOT).isPresent()) {
			boolexpterm.not = true;
			Optional<BoolExpTerm> notTerm = BoolExpTerm();
			if(notTerm.isEmpty()) {
				throw new SyntaxErrorException("NOT token must be followed by boolean expression term.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			boolexpterm.notTerm = notTerm;
		}else {
			throw new SyntaxErrorException("Boolean expression factor must be followed by one of the following: and, or, not.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		return Optional.of(boolexpterm);
		*/
		return null;
	}
	
	public Optional<BoolExpFactor> BoolExpFactor() throws SyntaxErrorException{
		/*
		BoolExpFactor boolexpfactor = new BoolExpFactor();
		if(tm.Peek(0).get().Type == ) {
			
		}
		Optional<Expression> expression = Expression();
		if(expression.isEmpty()) {
			throw new SyntaxErrorException("Boolean expression factor must start with an expression.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		boolexpfactor.lhs = expression;
		
		return Optional.of(boolexpfactor);
		*/
		return null;
	}
	
	public Optional<Expression> Expression() throws SyntaxErrorException{
		Expression expression = new Expression();
		Optional<Term> term1 = Term();
		if(term1.isEmpty()) {
			throw new SyntaxErrorException("An expression must begin with a valid term.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		expression.term.add(term1.get());
		while(tm.Peek(0).get().Type == TokenTypes.PLUS || tm.Peek(0).get().Type == TokenTypes.HYPHEN) {
			if(tm.MatchAndRemove(TokenTypes.PLUS).isPresent()) {
				expression.theplusORhyphen.add(plusORhyphen.plus);
			}else if(tm.MatchAndRemove(TokenTypes.HYPHEN).isPresent()){
				expression.theplusORhyphen.add(plusORhyphen.hyphen);
			}
			Optional<Term> newTerm = Term();
			if(newTerm.isEmpty()) {
				throw new SyntaxErrorException("'+' or '-' tokens in an expression must be followed by a valid term.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			expression.term.add(newTerm.get());
		}
		return Optional.of(expression);
	}
	
	public Optional<Term> Term() throws SyntaxErrorException{
		Term term = new Term();
		Optional<Factor> factor1 = Factor();
		if(factor1.isEmpty()) {
			throw new SyntaxErrorException("Term must begin with a valid factor.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		term.factor.add(factor1.get());
		while(tm.Peek(0).get().Type == TokenTypes.ASTERISK || tm.Peek(0).get().Type == TokenTypes.SLASH || tm.Peek(0).get().Type == TokenTypes.PERCENT) {
			if(tm.MatchAndRemove(TokenTypes.ASTERISK).isPresent()) {
				term.theasteriskORslashORpercent.add(asteriskORslashORpercent.asterisk);
			}else if(tm.MatchAndRemove(TokenTypes.SLASH).isPresent()) {
				term.theasteriskORslashORpercent.add(asteriskORslashORpercent.slash);
			}else if(tm.MatchAndRemove(TokenTypes.PERCENT).isPresent()) {
				term.theasteriskORslashORpercent.add(asteriskORslashORpercent.percent);
			}
			Optional<Factor> newFactor = Factor();
			if(newFactor.isEmpty()) {
				throw new SyntaxErrorException("'*' or '/' or '%' token must be followed by a valid factor.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			term.factor.add(newFactor.get());
		}
		return Optional.of(term);
	}
	
	public Optional<Factor> Factor() throws SyntaxErrorException{
		Factor factor = new Factor();
		Optional<Token> nextToken = tm.Peek(0);
		switch(nextToken.get().Type) {
			case NUMBER:
				factor.number = nextToken.get().Value;
				factor.$true = false;
				factor.$false = false;
				tm.MatchAndRemove(TokenTypes.NUMBER);
				break;
			case IDENTIFIER:
				Optional<VariableReference> variablereference = VariableReference();
				if(variablereference.isEmpty()) {
					throw new SyntaxErrorException("Invalid variable reference for a factor.", tm.getCurrentLine(), tm.getCurrentColumn());
				}
				factor.variablereference = variablereference;
				factor.$true = false;
				factor.$false = false;
				break;
			case TRUE:
				factor.$true = true;
				factor.$false = false;
				tm.MatchAndRemove(TokenTypes.TRUE);
				break;
			case FALSE:
				factor.$true = false;
				factor.$false = true;
				tm.MatchAndRemove(TokenTypes.FALSE);
				break;
			case STRINGLITERAL:
				factor.stringliteral = nextToken.get().Value;
				factor.$true = false;
				factor.$false = false;
				tm.MatchAndRemove(TokenTypes.STRINGLITERAL);
				break;
			case CHARACTERLITERAL:
				factor.characterliteral = nextToken.get().Value;
				factor.$true = false;
				factor.$false = false;
				tm.MatchAndRemove(TokenTypes.CHARACTERLITERAL);
				break;
			case OPENPAREN:
				tm.MatchAndRemove(TokenTypes.OPENPAREN);
				Optional<Expression> expression = Expression();
				if(expression.isEmpty()) {
					throw new SyntaxErrorException("Invalid expression for a factor.", tm.getCurrentLine(), tm.getCurrentColumn());
				}
				factor.expression = expression;
				factor.$true = false;
				factor.$false = false;
				tm.MatchAndRemove(TokenTypes.CLOSEPAREN);
				break;
			default:
				throw new SyntaxErrorException("Factor must start with one of the following: Number, Variable Reference, true, false, String literal, Character literal, or an Expression within parentheses. This factor starts with no valid form of any of these.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		return Optional.of(factor);
	}
	
	public Optional<Loop> Loop(){
		return null;
	}
	
	public Optional<Set> Set() throws SyntaxErrorException {
		Set set = new Set();
		if(tm.MatchAndRemove(TokenTypes.SET).isEmpty()) {
			throw new SyntaxErrorException("Set statement must begin with 'SET' token.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		Optional<VariableReference> variablereference = VariableReference();
		if(variablereference.isEmpty()) {
			throw new SyntaxErrorException("Set statement must have a variable reference to assign to.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		set.variablereference = variablereference.get();
		if(tm.MatchAndRemove(TokenTypes.TO).isEmpty()) {
			throw new SyntaxErrorException("Set statement must have 'TO' token to assign value to a variable.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		Optional<Expression> expression = Expression();
		if(expression.isEmpty()) {
			throw new SyntaxErrorException("Set statement much have an expression to assign variable to.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		set.expression = expression.get();
		RequireNewline();
		return Optional.of(set);
	}
	
	public Optional<Make> Make() throws SyntaxErrorException {
		Make make = new Make();
		if(tm.MatchAndRemove(TokenTypes.MAKE).isEmpty()) {
			throw new SyntaxErrorException("Make statement must begin with 'MAKE' token.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		Optional<Token> type = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
		if(type.isEmpty()) {
			throw new SyntaxErrorException("Make statement must have a declared type.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		make.type = type.get().Value.get();
		if(tm.MatchAndRemove(TokenTypes.NAMED).isEmpty()) {
			throw new SyntaxErrorException("Make statement must have a name for object being made.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		Optional<Token> name = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
		if(name.isEmpty()) {
			throw new SyntaxErrorException("No name given for object to be made.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		make.name = name.get().Value.get();
		RequireNewline();
		return Optional.of(make);
	}
	
	public Optional<FunctionCall> FunctionCall(){
		return null;
	}
	
	public Optional<VariableReference> VariableReference() throws SyntaxErrorException{
		VariableReference variablereference = new VariableReference();
		Optional<Token> name = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
		if(name.isEmpty()) {
			throw new SyntaxErrorException("Variable reference must begin with a name.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		variablereference.name = name.get().Value.get();
		if(tm.MatchAndRemove(TokenTypes.OF).isPresent()) {
			variablereference.of = true;
			Optional<Token> $object = tm.MatchAndRemove(TokenTypes.IDENTIFIER); 
			if($object.isEmpty()) {
				throw new SyntaxErrorException("'OF' token in variable reference must be followed with identifier for object type.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			variablereference.$object = $object.get().Value;
		}else {
			variablereference.of = false;
		}
		return Optional.of(variablereference);
	}
}

