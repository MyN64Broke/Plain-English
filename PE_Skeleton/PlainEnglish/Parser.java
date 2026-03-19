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
	
	public Optional<Method> Method() {
		return null;
	}
	
	public Optional<Parameter> Parameter() {
		return null;
	}
	
	public Optional<StatementBlock> StatementBlock() {
		return null;
	}
	
	public Optional<Statement> Statement() {
		return null;
	}
	
	public Optional<Set> Set() {
		return null;
	}
	
	public Optional<Make> Make() {
		return null;
	}
}
