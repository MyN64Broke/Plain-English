package PlainEnglish;

import java.util.LinkedList;
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
	
	public Program Program() throws SyntaxErrorException{
		TypeDef typeDef = new TypeDef();
		Program program = new Program();
		program.typedef.add(typeDef);
		return program;
	}
}
