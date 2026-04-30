package PlainEnglish;
import java.util.HashMap;
import java.util.LinkedList;

import PlainEnglish.Token.TokenTypes;

public class Lexer    {
	
	private final TextManager tm;
	private HashMap<String, TokenTypes> WordMap;
	private HashMap<String, TokenTypes> PuncMap;
	private LinkedList<Token> tokens = new LinkedList<Token>();
	
    public Lexer(String input) {
        tm = new TextManager(input);
        WordMap = new HashMap<String, TokenTypes>();
        PuncMap = new HashMap<String, TokenTypes>();
        WordMap.put("to", TokenTypes.TO);
        WordMap.put("a", TokenTypes.A);
        WordMap.put("with", TokenTypes.WITH);
        WordMap.put("named", TokenTypes.NAMED);
        WordMap.put("an", TokenTypes.AN);
        WordMap.put("is", TokenTypes.IS);
        WordMap.put("if", TokenTypes.IF);
        WordMap.put("else", TokenTypes.ELSE);
        WordMap.put("loop", TokenTypes.LOOP);
        WordMap.put("set", TokenTypes.SET);
        WordMap.put("make", TokenTypes.MAKE);
        WordMap.put("of", TokenTypes.OF);
        WordMap.put("true", TokenTypes.TRUE);
        WordMap.put("false", TokenTypes.FALSE);
        WordMap.put("and", TokenTypes.AND);
        WordMap.put("or", TokenTypes.OR);
        WordMap.put("not", TokenTypes.NOT);
        WordMap.put("To", TokenTypes.TO);
        WordMap.put("A", TokenTypes.A);
        WordMap.put("With", TokenTypes.WITH);
        WordMap.put("Named", TokenTypes.NAMED);
        WordMap.put("An", TokenTypes.AN);
        WordMap.put("Is", TokenTypes.IS);
        WordMap.put("If", TokenTypes.IF);
        WordMap.put("Else", TokenTypes.ELSE);
        WordMap.put("Loop", TokenTypes.LOOP);
        WordMap.put("Set", TokenTypes.SET);
        WordMap.put("Make", TokenTypes.MAKE);
        WordMap.put("Of", TokenTypes.OF);
        WordMap.put("True", TokenTypes.TRUE);
        WordMap.put("False", TokenTypes.FALSE);
        WordMap.put("And", TokenTypes.AND);
        WordMap.put("Or", TokenTypes.OR);
        WordMap.put("Not", TokenTypes.NOT);
        PuncMap.put(",", TokenTypes.COMMA);
        PuncMap.put("+", TokenTypes.PLUS);
        PuncMap.put("=", TokenTypes.ASSIGN);
        PuncMap.put("-", TokenTypes.HYPHEN);
        PuncMap.put("*", TokenTypes.ASTERISK);
        PuncMap.put("/", TokenTypes.SLASH);
        PuncMap.put("%", TokenTypes.PERCENT);
        PuncMap.put("(", TokenTypes.OPENPAREN);
        PuncMap.put(")", TokenTypes.CLOSEPAREN);
        PuncMap.put("==", TokenTypes.DOUBLEEQUAL);
        PuncMap.put("!=", TokenTypes.NOTEQUAL);
        PuncMap.put("<=", TokenTypes.LESSTHANEQUAL);
        PuncMap.put(">=", TokenTypes.GREATERTHANEQUAL);
        PuncMap.put(">", TokenTypes.GREATERTHAN);
        PuncMap.put("<", TokenTypes.LESSTHAN);
    }
    
    public LinkedList<Token> lex() throws SyntaxErrorException {
    	int indentation = 0;
    	while(!tm.isAtEnd()) {
    		char C = tm.getCharacter();
    		int line = tm.getLine();
    		int col = tm.getCol();
    		if(Character.isLetter(C)) {
    			tokens.add(readWord());
    		}else if(Character.isDigit(C)) {
				tokens.add(readNumber());
    		}else if(C == '/' && (!tm.isAtEnd(1) && (tm.peekCharacter() == '/' || tm.peekCharacter() == '*'))) {
    			if(!tm.isAtEnd(1)) {
    				readComments();
    			}else {
    				throw new SyntaxErrorException("Erroneous / at end of text. Remove it. Now.", line, col);
    			}
    		}else if(PuncMap.containsKey(Character.toString(C)) || ( !tm.isAtEnd(1) && PuncMap.containsKey(Character.toString(C) + Character.toString(tm.peekCharacter())))) {
    			tokens.add(readPunctuation());
    		}else if(C == '\r' && tm.peekCharacter() == '\n') {
    			tm.increment();
    			tokens.add(new Token(TokenTypes.NEWLINE, line, col, Character.toString(C)));
    			tm.newline();
    			if(!tm.isAtEnd(1)) {
    				indentation = readWhitespace(indentation);
    			}
    		}else if(C == '\n' || C == '\r') {
    			tokens.add(new Token(TokenTypes.NEWLINE, line, col, Character.toString(C)));
    			tm.newline();
    			if(!tm.isAtEnd(1)) {
    				indentation = readWhitespace(indentation);
    			}
    		}else if(C == '"') {
    			int balance = balancedQuotes(C);
    			if(balance > 0) {
    				tokens.add(readLiteral(balance, C));
    			}else {
    				throw new SyntaxErrorException("Unbalanced quotation marks at: " + line + ", " + col, line, col);
    			}
    		}else if(C == '\'') {
    			int balance = balancedQuotes(C);
    			if(balance > 3) {
    				throw new SyntaxErrorException("Character too long: " + tm.getLine() + ", " + tm.getCol(), tm.getLine(), tm.getCol());
    			}else if(balance > 0){
    				tokens.add(readLiteral(balance, C));
    			}else {
    				throw new SyntaxErrorException("Unbalanced apostrophes at " + line + ", " + col, line, col);
    			}
    		}else if(C == ' ') {
    			tm.increment();
    		}else {
    			throw new SyntaxErrorException("It looks like you have unusable character \"" + C + "\" at: " + line + ", " + col, line, col);
    		}
    	}
    	while(indentation > 0) {
    		tokens.add(new Token(TokenTypes.DEDENT, tm.getLine(), tm.getCol()));
    		indentation--;
    	}
    	tokens.add(new Token(TokenTypes.NEWLINE, tm.getLine(), tm.getCol() + 1, "/n"));
    	return tokens;
    }
    
    private Token readWord() throws SyntaxErrorException {
    	int line = tm.getLine();
    	int col = tm.getCol();
    	String buffer = "";
    	Token token = new Token(TokenTypes.IDENTIFIER, line, col, buffer);
    	while(tm.getCharacter() != ' ') {
    		line = tm.getLine();
    		col = tm.getCol();
    		char C = tm.getCharacter();
    		if(C == '\n' || C == '\r' || PuncMap.containsKey(Character.toString(C))) {
    			break;
    		}else {
	    		buffer += C;
	    		if(!tm.isAtEnd()) {
	    			tm.increment();
	    		}
    		}
    	}
    	if(WordMap.containsKey(buffer)) {
    		token = new Token(WordMap.get(buffer), line, col);
    	}else {
    		token = new Token(TokenTypes.IDENTIFIER, line, col, buffer);
    	}
    	return token;
    }
    
    private Token readNumber(){
    	Token token;
    	int line = tm.getLine();
    	int col = tm.getCol();
    	String buffer = "";
    	boolean decimalInNum = false;
    	while(Character.isDigit(tm.getCharacter()) || (tm.getCharacter() == '.' && !decimalInNum)) {
    		char C = tm.getCharacter();
    		buffer += C;
    		if(C == '.') {
    			decimalInNum = true;
    		}
    		if(!tm.isAtEnd()) {
    			tm.increment();
    		}
    	}
    	token = new Token(TokenTypes.NUMBER, line, col, buffer);
    	return token;
    }
    
    private Token readPunctuation() throws SyntaxErrorException {
    	Token puncToken;
    	String buffer = Character.toString(tm.getCharacter());
    	int line = tm.getLine();
    	int col = tm.getCol();
    	if(tm.isAtEnd(1)) {
    		puncToken = new Token(PuncMap.get(buffer), line, col);
    	}else {
	    	char peek = tm.peekCharacter();
	    	if(PuncMap.containsKey(buffer + Character.toString(peek))) {
	    		buffer += Character.toString(peek);
	    		puncToken = new Token(PuncMap.get(buffer), line, col);
	    		tm.increment();
	    		if(!tm.isAtEnd()) {
	    			tm.increment();
	    		}
	    	}else if(!(Character.isLetter(peek) || Character.isDigit(peek) || peek == ' ' || peek == '\n' || peek == '\r')) {
	    		tm.increment();
	    		return puncToken = new Token(PuncMap.get(buffer), line, col);
	    	}else {
	    		puncToken = new Token(PuncMap.get(buffer), tm.getLine(), tm.getCol());
	    		tm.increment();
	    	}
    	}
    	return puncToken;
    }
    
    private Token readLiteral(int numChars, char type) {
    	tm.increment();
    	String buffer = "";
    	int line = tm.getLine();
    	int col = tm.getCol();
    	for(int i = 0; i < numChars; i++) {
    		buffer += tm.getCharacter();
    		tm.increment();
    	}
    	if(!tm.isAtEnd()) {
    		tm.increment();
    	}
    	Token token;
    	if(type == '"') {
    		token = new Token(TokenTypes.STRINGLITERAL, line, col, buffer);
    	}else {
    		token = new Token(TokenTypes.CHARACTERLITERAL, line, col, buffer);
    	}
    	return token;
    }
    
    //It's called readComments but that's basically the opposite of what it does
    private void readComments() {
    	tm.increment();
    	char C = tm.getCharacter();
    	if(C == '/') {
    		while(!tm.isAtEnd()) {
    			tm.increment();
				if(tm.getCharacter() == '\n' || tm.getCharacter() == '\r') {
					return;
				}
    		}
		}else if(C == '*') {
			if(!tm.isAtEnd()) {
				while(true) {
					C = tm.getCharacter();
					if(C == '*' && !tm.isAtEnd() && tm.peekCharacter() == '/') {
						tm.increment();
						tm.increment();
						return;
					}
					if(!tm.isAtEnd() && (C =='\r' && tm.peekCharacter() == '\n')){
						tm.increment();
						tm.newline();
					}else if(!tm.isAtEnd() && (C == '\n' || C == '\r')) {
						tm.newline();
					}else if(!tm.isAtEnd()) {
						tm.increment();
					}
				}
    		}
		}
    }
    
    private int readWhitespace(int currentIndent) throws SyntaxErrorException {
    	int indent = 0;
    	if(tm.getCharacter() == '\n' || tm.getCharacter() == '\r') {
    		return currentIndent;
    	}
    	while(tm.getCharacter() == ' ' || tm.getCharacter() == '\t') {
    		if(!tm.isAtEnd(4) && tm.getCharacter() == ' ' && tm.peekCharacter() == ' ' && tm.peekCharacter(2) == ' ' && tm.peekCharacter(3) == ' ') {
    			indent++;
    			tm.increment();
    			tm.increment();
    			tm.increment();
    			tm.increment();
    		}else if(tm.getCharacter() == '\t') {
    			indent++;
    			tm.increment();
    			tm.adjustColForIndent();
    		}else{
    			throw new SyntaxErrorException("Incorrect number of spaces to start new line.", tm.getLine(), tm.getCol());
    		}
    	}
    	if(indent > currentIndent + 1) {
    		throw new SyntaxErrorException("Too much indentation for new line based on indentation of last line.", tm.getLine(), tm.getCol());
    	}else if(indent == currentIndent + 1) {
    		tokens.add(new Token(TokenTypes.INDENT, tm.getLine(), tm.getCol()));
    	}else if(indent < currentIndent) {
    		while(indent < currentIndent) {
    			tokens.add(new Token(TokenTypes.DEDENT, tm.getLine(), tm.getCol()));
    			currentIndent--;
    		}
    	}
    	return indent;
    }
    
    private int balancedQuotes(char start) {
    	/*
    	 * So this method, when strings or characters are balanced, returns a positive number, necessarily.
    	 * The thing is, that number is very specifically the number of characters in the string or character
    	 * plus 1.
    	 * This looks strange, but that number is used later in the for loop that actually reads the tokens in,
    	 * and when reading in the tokens, I decided it should read in the quotation marks and apostrophes as well.
    	 * For this reason, the buffers in that for loop are initialized with the opening " or ', and loop all the way
    	 * to the second " or ', thanks to this value being i + 1.
    	 */
	    for(var i = 0; i < tm.getRemaining(); i++) {
	    	if(!tm.isAtEnd(i)){
		   		char C = tm.peekCharacter(i + 1);
		    	if(C == start) {
		    		return i;
		    	}
	    	}
	    }
    	return 0;
    }
}

