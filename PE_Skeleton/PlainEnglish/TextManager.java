package PlainEnglish;

public class TextManager
{
    private final /*readonly*/ String text;
    private int position;

    public TextManager(String input)
    {
        position = 0;
        text = input;
    }

    public boolean isAtEnd() {
    	return true;
    }
    
    public char peekCharacter() {
    	return 'a';
    }
    
    public char peekCharacter(int distance) {
    	return 'a';
    }
    
    public char getCharacter() {
    	return 'a';
    }
}

