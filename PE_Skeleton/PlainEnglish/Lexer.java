package PlainEnglish;
import java.util.ArrayList;
import java.util.HashMap;

import PlainEnglish.Token.TokenTypes;

public class Lexer    {
	
	private final TextManager tm;
	private HashMap<String, TokenTypes> WordMap;
	private HashMap<String, TokenTypes> PuncMap;
	
    public Lexer(String input) {
        tm = new TextManager(input);
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
    
    public void lex() throws SyntaxErrorException {
    	ArrayList<Token> ListOfTokens = new ArrayList<Token>();
    	while(!tm.isAtEnd()) {
    		char C = tm.getCharacter();
    		int line = tm.getLine();
    		int col = tm.getCol();
    		if(Character.isLetter(C)) {
    			ListOfTokens.add(readWord());
    		}else if(Character.isDigit(C)) {
				ListOfTokens.add(readNumber());
    		}else if(PuncMap.containsKey(Character.toString(C))) {
    			ListOfTokens.add(readPunctuation());
    		}else if(C == '\n') {
    			Token newline = new Token(TokenTypes.NEWLINE, line, col, Character.toString(C));
    			ListOfTokens.add(newline);
    			tm.newline();
    		}else if(C == '\t') {
    			Token tab = new Token(TokenTypes.INDENT, line, col, Character.toString(C));
    			ListOfTokens.add(tab);
    		}
    	}
    }
    
    private Token readWord() {
    	Token token;
    	String buffer = "";
    	int line = tm.getLine();
    	int col = tm.getCol();
    	while(tm.peekCharacter() != ' ' && !tm.isAtEnd()) {
    		buffer += tm.getCharacter();
    		tm.increment();
    	}
    	if(WordMap.containsKey(buffer)) {
    		token = new Token(WordMap.get(buffer), line, col, buffer);
    	}else {
    		token = new Token(TokenTypes.IDENTIFIER, line, col, buffer);
    	}
    	return token;
    }
    
    private Token readNumber() throws SyntaxErrorException {
    	int line = tm.getLine();
    	int col = tm.getCol();
    	String buffer = "";
    	while(tm.peekCharacter() != ' ' && !tm.isAtEnd()) {
    		char C = tm.getCharacter();
    		if(!Character.isDigit(C) && !(C == '.')) {
    			throw new SyntaxErrorException(buffer, tm.getLine(), tm.getCol());
    		}else {
    			buffer += C;
    			tm.increment();
    		}
    	}
    	Token token = new Token(TokenTypes.NUMBER, line, col, buffer);
    	return token;
    }
    
    private Token readPunctuation() throws SyntaxErrorException {
    	String buffer = Character.toString(tm.getCharacter());
    	char peek = tm.peekCharacter();
    	Token puncToken;
    	if(PuncMap.containsKey(buffer + Character.toString(peek))) {
    		buffer += Character.toString(peek);
    		puncToken = new Token(PuncMap.get(buffer), tm.getLine(), tm.getCol(), buffer);
    		tm.increment();
    		tm.increment();
    	}else if(!Character.isLetter(peek) && !Character.isDigit(peek) && peek != ' ') {
    		throw new SyntaxErrorException(buffer, tm.getLine(), tm.getCol());
    	}else {
    		puncToken = new Token(PuncMap.get(buffer), tm.getLine(), tm.getCol(), buffer);
    		tm.increment();
    	}
    	return puncToken;
    }
}

