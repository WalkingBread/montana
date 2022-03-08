package skorupinski.montana.lexer;

public class Token {

    public final TokenType type;

    public final String value;

    public final int line;

    public final int column;

    public final String file;

    public Token(TokenType type, String value, int line, int column, String file) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
        this.file = file;
    }

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
        this.line = 0;
        this.column = 0;
        this.file = "";
    }

    public boolean typeOf(TokenType type) {
        return this.type == type;
    }

    @Override
    public String toString() {
        return type + ": " + value;
    }

}
