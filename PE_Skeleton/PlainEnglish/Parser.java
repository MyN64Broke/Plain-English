package PlainEnglish;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import PlainEnglish.Token.TokenTypes;
import PlainEnglish.AST.*;

public class Parser {
	
	private final LinkedList<Token> tokens;
	private final TokenManager tm;
	
	public Parser(LinkedList<Token> tokens) {
		this.tokens = tokens;
		this.tm = new TokenManager(tokens);
	}
	
	private void RequireNewLine() {
		
	}
	
	public Optional<Program> Program() throws SyntaxErrorException{
		Program program = new Program();
		while(tm.Peek(0).isPresent()) {
			if(tm.Peek(0).get().Type == TokenTypes.NEWLINE) {
				tm.MatchAndRemove(null);
			}else if(tm.Peek(0).get().getType() == TokenTypes.A || tm.Peek(0).get().getType() == TokenTypes.AN ) {
				program.typedef.add(TypeDef().get());
			}else if(tm.Peek(0).get().getType() == TokenTypes.TO) {
				program.method.add(Method().get());
			}
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
			typedef.name = Objects.toString(name.get().Value);
		}
		if(tm.MatchAndRemove(TokenTypes.IS).isEmpty()) {
			int line = tm.getCurrentLine();
			int col = tm.getCurrentColumn();
			throw new SyntaxErrorException("Unexpected Token at: " + line + ", " + col + ". Expected token IS for TypeDef", line, col);
		}
		int newlineCount = 0;
		while(tm.MatchAndRemove(TokenTypes.NEWLINE).isPresent()) {
			newlineCount++;
		}
		if(newlineCount == 0) {
			throw new SyntaxErrorException("TypeDef must have at least 1 NEWLINE after IS token.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
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
		field.type = Objects.toString(type.get().Value);
		Optional<Token> name = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
		if(name.isEmpty()) {
			throw new SyntaxErrorException("Field must have a name.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		field.name = Objects.toString(name.get().Value);
		int newlineCount = 0;
		while(tm.MatchAndRemove(TokenTypes.NEWLINE).isPresent()) {
			newlineCount++;
		}
		if(newlineCount == 0) {
			throw new SyntaxErrorException("Field must have at least 1 NEWLINE after NAME.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
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
		method.name = Objects.toString(name.get().Value);
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
		int newlineCount = 0;
		while(tm.MatchAndRemove(TokenTypes.NEWLINE).isPresent()) {
			newlineCount++;
		}
		if(newlineCount == 0) {
			throw new SyntaxErrorException("Method must have at least 1 NEWLINE after definition.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
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
		parameter.paramType = Objects.toString(paramType.get().Value);
		if(tm.MatchAndRemove(TokenTypes.NAMED).isPresent()) {
			parameter.named = true;
			Optional<Token> name = tm.MatchAndRemove(TokenTypes.IDENTIFIER);
			if(name.isEmpty()) {
				throw new SyntaxErrorException("Name override with 'named' must have Identifier afterward.", tm.getCurrentLine(), tm.getCurrentColumn());
			}
			parameter.nameOverride = name.get().Value;
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
		if(tm.Peek(1).get().Type == TokenTypes.IF) {
			statement.$if = If();
		}else if(tm.Peek(1).get().Type == TokenTypes.LOOP) {
			statement.loop = Loop();
		}else if(tm.Peek(1).get().Type == TokenTypes.SET) {
			statement.set = Set();
		}else if(tm.Peek(1).get().Type == TokenTypes.MAKE) {
			statement.make = Make();
		}else if(tm.Peek(1).get().Type == TokenTypes.IDENTIFIER) {
			statement.functioncall = FunctionCall();
		}else {
			throw new SyntaxErrorException("No valid statement tokens: If, Loop, Set, Make or Function Call", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		return Optional.of(statement);
	}
	
	public Optional<If> If() throws SyntaxErrorException{
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
		int newlineCount = 0;
		while(tm.MatchAndRemove(TokenTypes.NEWLINE).isPresent()) {
			newlineCount++;
		}
		if(newlineCount == 0) {
			throw new SyntaxErrorException("If statement must have at least 1 NEWLINE after definition.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
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
		}
		return Optional.of($if);
	}
	
	public Optional<BoolExpTerm> BoolExpTerm() throws SyntaxErrorException{
		BoolExpTerm boolexpterm = new BoolExpTerm();
		Optional<BoolExpFactor> boolexpfactor = BoolExpFactor();
		if(boolexpfactor.isEmpty()) {
			throw new SyntaxErrorException("Boolean expression term must have at least one boolean expression factor.", tm.getCurrentLine(), tm.getCurrentColumn());
		}
		boolexpterm.boolexpfactor = boolexpfactor;
		if(tm.Peek(0).get().Type == TokenTypes.AND || tm.Peek(0).get().Type == TokenTypes.OR) {
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
	}
	
	public Optional<BoolExpFactor> BoolExpFactor(){
		return null;
	}
	
	public Optional<Loop> Loop(){
		return null;
	}
	
	public Optional<Set> Set() {
		return null;
	}
	
	public Optional<Make> Make() {
		return null;
	}
	
	public Optional<FunctionCall> FunctionCall(){
		return null;
	}
}
