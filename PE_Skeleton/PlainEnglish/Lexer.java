package PlainEnglish;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

import PlainEnglish.Token.TokenTypes;

public class Lexer    {
	
	private final TextManager tm;
	private HashMap<String, TokenTypes> WordMap;
	private HashMap<String, TokenTypes> PuncMap;
	private Stack<Integer> indentation = new Stack<>();
	
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
    	indentation.push(0);
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
    			if(!tm.isAtEnd(1)) {
    				ListOfTokens.addAll(readWhitespace());
    			}
    		}else if(C == '"') {
    			int balance = balancedQuotes(C);
    			if(balance > 0) {
    				ListOfTokens.add(readLiteral(balance, C));
    			}else {
    				throw new SyntaxErrorException("Unbalanced quotation marks at: " + line + ", " + col, line, col);
    			}
    		}else if(C == '\'') {
    			int balance = balancedQuotes(C);
    			if(balance > 3) {
    				throw new SyntaxErrorException("Character too long: " + tm.getLine() + ", " + tm.getCol(), tm.getLine(), tm.getCol());
    			}else if(balance > 0){
    				ListOfTokens.add(readLiteral(balance, C));
    			}else {
    				throw new SyntaxErrorException("Unbalanced apostrophes at " + line + ", " + col, line, col);
    			}
    		}else if(C == '/') {
    			if(!tm.isAtEnd(1)) {
    				readComments();
    			}else {
    				throw new SyntaxErrorException("Erroneous / at end of text. Remove it. Now.", line, col);
    			}
    		}else {
    			throw new SyntaxErrorException("It looks like you have an unusable character at: " + line + ", " + col, line, col);
    		}
    	}
    	ListOfTokens.add(new Token(TokenTypes.NEWLINE, tm.getLine(), tm.getCol() + 1, "/n"));
    	while(!indentation.isEmpty()) {
    		indentation.pop();
    		ListOfTokens.add(new Token(TokenTypes.DEDENT, tm.getLine(), 0));
    	}
    }
    
    private Token readWord() throws SyntaxErrorException {
    	Token token;
    	String buffer = "";
    	int line = tm.getLine();
    	int col = tm.getCol();
    	while(tm.getCharacter() != ' ') {
    		char C = tm.getCharacter();
    		if(PuncMap.containsKey(Character.toString(C))) {
    			throw new SyntaxErrorException("Reserved symbol in word at: " + line + ", " + col, line, col);
    		}else {
	    		buffer += tm.getCharacter();
	    		if(!tm.isAtEnd()) {
	    			tm.increment();
	    		}
    		}
    	}
    	if(WordMap.containsKey(buffer)) {
    		token = new Token(WordMap.get(buffer), line, col, buffer);
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
    	if(tm.isAtEnd(1)) {
    		puncToken = new Token(PuncMap.get(buffer), tm.getLine(), tm.getCol(), buffer);
    	}else {
	    	char peek = tm.peekCharacter();
	    	if(PuncMap.containsKey(buffer + Character.toString(peek))) {
	    		buffer += Character.toString(peek);
	    		puncToken = new Token(PuncMap.get(buffer), tm.getLine(), tm.getCol(), buffer);
	    		tm.increment();
	    		if(!tm.isAtEnd()) {
	    			tm.increment();
	    		}
	    	}else if(!(Character.isLetter(peek) || Character.isDigit(peek) || peek == ' ')) {
	    		buffer += Character.toString(peek);
	    		throw new SyntaxErrorException("Invalid punctuation syntax: " + buffer + " at: " + tm.getLine() + ", " + tm.getCol(), tm.getLine(), tm.getCol());
	    	}else {
	    		puncToken = new Token(PuncMap.get(buffer), tm.getLine(), tm.getCol(), buffer);
	    		tm.increment();
	    	}
    	}
    	return puncToken;
    }
    
    private Token readLiteral(int numChars, char type) {
    	String buffer = Character.toString(tm.getCharacter());
    	int line = tm.getLine();
    	int col = tm.getCol();
    	//This for loop is the one mentioned in the comments inside balancedQuotes.
    	for(int i = 0; i < numChars; i++) {
    		buffer += tm.peekCharacter();
    		tm.increment();
    	}
    	if(!tm.isAtEnd()) {
    		tm.increment(); //This loop is peeking for the characters to add so it needs an extra increment
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
    		if(!tm.isAtEnd()) {
				while(tm.peekCharacter() != '\n') {
					if(!tm.isAtEnd()) {
						tm.increment();
					}
				}
				return;
    		}
		}else if(C == '*') {
			if(!tm.isAtEnd()) {
				while(true) {
					C = tm.getCharacter();
					if(C == '*' && !tm.isAtEnd() && tm.peekCharacter() == '/') {
						tm.increment();
						return;
					}
					if(!tm.isAtEnd()) {
						tm.increment();
					}
				}
    		}
		}
    }
    
    private ArrayList<Token> readWhitespace() throws SyntaxErrorException {
    	ArrayList<Token> tokens = new ArrayList<Token>();
    	int indent = 0;
		while(tm.getCharacter() == ' ' || tm.getCharacter() == '\t') {
			char C = tm.getCharacter();
			if(C == ' ') {
				indent++;
			}else {
				indent += 4;
			}
			if(!tm.isAtEnd()) {
				tm.increment();
			}else {
				break;
			}
		}
		if(indent > indentation.peek()) {
			indentation.push(indent);
			Token token = new Token(TokenTypes.INDENT, tm.getLine(), tm.getCol());
			tokens.add(token);
		}else if(indent < indentation.peek()) {
			try {
				while(indent != indentation.peek()) {
					indentation.pop();
					Token token = new Token(TokenTypes.DEDENT, tm.getLine(), tm.getCol());
					tokens.add(token);
				}
			}catch(Exception EmptyStackException) {
				throw new SyntaxErrorException("Improper indentation at: " + tm.getLine() + ", " + tm.getCol(), tm.getLine(), tm.getCol());
			}
		}
		return tokens;
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
		    		return i + 1;
		    	}
	    	}
	    }
    	return 0;
    }
}

