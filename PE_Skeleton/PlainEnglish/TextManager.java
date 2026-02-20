package PlainEnglish;

public class TextManager
{
    private final /*readonly*/ String text;
    private int position; //This checks how far through the string we are
    private int positionCol;
    private int positionLine;

    public TextManager(String input)
    {
    	position = 1;
        positionCol = 1;
        positionLine = 1;
        text = input;
    }

    public boolean isAtEnd() {
    	if(position == text.length()) {
    		return true;
    	}else {
    		return false;
    	}
    }
    
    public char peekCharacter() {
    	char peekChar = text.charAt(position + 1);
    	return peekChar;
    }
    
    public char peekCharacter(int distance) {
    	char peekChar = text.charAt(position + distance);
    	return peekChar;
    }
    
    public char getCharacter() {
    	char currentChar = text.charAt(position);
    	return currentChar;
    }
    
    public void increment() {
    	position++;
    	positionCol++;
    }
    
    public void newline() {
    	position++;
    	positionCol = 0;
    	positionLine++;
    }
    
    public int getCol() {
    	return positionCol;
    }
    
    public int getLine() {
    	return positionLine;
    }
}

