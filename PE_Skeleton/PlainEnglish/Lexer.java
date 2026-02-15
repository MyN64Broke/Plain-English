package PlainEnglish;
import java.util.ArrayList;
import java.util.HashMap;

import PlainEnglish.Token.TokenTypes;

public class Lexer    {
	
	private final TextManager tm;
	private HashMap<String, TokenTypes> TypeMap;
	
    public Lexer(String input) {
        tm = new TextManager(input);
        TypeMap.put("if", TokenTypes.IF);
        //Add more here Owen for all the types
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
}

