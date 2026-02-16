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
    
    public void lex() {
    	ArrayList<Token> ListOfTokens = new ArrayList<Token>();
    	String currentWord;
    	while(!tm.isAtEnd()) {
    		char C = tm.peekCharacter();
    		if(Character.isLetter(C)) {
    			ListOfTokens.add(readWord());
    		}else if(Character.isDigit(C)) {
    			ListOfTokens.add(readNumber());
    		}
    	}
    }
    
    private Token readWord() {
    	
    }
    
    private Token readNumber() {
    	
    }
    
    private Token readPunctuation() {
    	
    }
}

