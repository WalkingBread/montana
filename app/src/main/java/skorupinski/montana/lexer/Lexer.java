package skorupinski.montana.lexer;

import java.io.IOException;
import java.util.HashMap;

import skorupinski.montana.utils.Reader;
import skorupinski.montana.utils.Error.SyntaxError;

public class Lexer {
    
    public static final HashMap<String, TokenType> keywords = new HashMap<>() {{
        put("+", TokenType.PLUS);
        put("-", TokenType.MINUS);
        put("/", TokenType.DIV);
        put("//", TokenType.INT_DIV);
        put("*", TokenType.MULT);
        put("%", TokenType.MODULO);
        put("==", TokenType.EQUALS);
        put("!=", TokenType.NOT_EQUALS);
        put(">=", TokenType.MORE_OR_EQ);
        put("<=", TokenType.LESS_OR_EQ);
        put(">", TokenType.MORE);
        put("<", TokenType.LESS);
        put(":", TokenType.COLON);
        put(";", TokenType.SEMICOLON);
        put("||", TokenType.OR);
        put("&&", TokenType.AND);
        put(",", TokenType.COMMA);
        put("!", TokenType.NOT);
        put("^", TokenType.POWER);
        put("=", TokenType.ASSIGN);
        put("(", TokenType.L_PAREN);
        put(")", TokenType.R_PAREN);
        put("{", TokenType.L_CURLY);
        put("}", TokenType.R_CURLY);
        put("[", TokenType.L_SQUARED);
        put("]", TokenType.R_SQUARED);
        put("++", TokenType.INCREMENT);
        put("--", TokenType.DECREMENT);

        put("true", TokenType.BOOLEAN);
        put("false", TokenType.BOOLEAN);
        put("if", TokenType.IF);
        put("else", TokenType.ELSE);
        put("have", TokenType.VARIABLE_DECL);
        put("for", TokenType.FOR);
        put("while", TokenType.WHILE);
        put("return", TokenType.RETURN);
        put("fun", TokenType.FUNCTION);
        put("out", TokenType.PRINT);
        put("as", TokenType.AS);
        put("and", TokenType.AND);
        put("or", TokenType.OR);
        put("is", TokenType.EQUALS);
        put("none", TokenType.NONE);
        put("int", TokenType.CAST_INT);
        put("str", TokenType.CAST_STRING);
        put("float", TokenType.CAST_FLOAT);
        put("bool", TokenType.CAST_BOOL);
        put("import", TokenType.IMPORT);
        put("class", TokenType.CLASS);
    }};

    private int position;

    private int line;

    private int column;

    private char currentChar;

    private String code;

    private String file;

    public Lexer(String file) {
        this.file = file;

        try {
            code = Reader.readAndSeparateLines(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        position = 0;
        line = 1;
        column = 1;

        currentChar = code.length() > 0 ? code.charAt(position) : '\0';
    }

    private Token createToken(TokenType type, String value) {
        return new Token(type, value, line, column, file);
    }

    private void advance() {
        if(currentChar == '\n') {
            line++;
            column = 0;
        }
    
        position++;
        if(position > code.length() - 1) {
            currentChar = '\0';
        } else {
            currentChar = code.charAt(position);
            column++;
        }
    }

    private char peek() {
        int peekPosition = position + 1;

        if(peekPosition > code.length() - 1) {
            return '\0';
        }
        
        return code.charAt(peekPosition);
    }

    private void skipWhitespace() {
        while(currentChar != '\0' && Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    private String number() {
        String result = "";

        if(currentChar != '\0') {
            if(Character.isDigit(currentChar) || currentChar == '.') {
                result += currentChar;
                advance();
            } else {
                return result;
            }
        }

        return result + number();
    }

    private String string() {
        String result = "";
        advance();
    
        while(currentChar != '\'') {
            if(currentChar == '\n') {
                String message = "Reached end of line while parsing string.";
                new SyntaxError(file, line, column, message).cast();
            }
            result += currentChar;
            advance();
        }
        advance();
    
        return result;
    }

    private Token identifier() {
        String result = "";

        while(currentChar != '\0' && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            result += currentChar;
            advance();
        }

        if(keywords.containsKey(result)) {
            TokenType type = keywords.get(result);
            return createToken(type, result);
        }

        return createToken(TokenType.IDENTIFIER, result);
    }

    private Token builtInLib() {
        String result = "";
        advance();

        while(currentChar != '\0' && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            result += currentChar;
            advance();
        }

        return createToken(TokenType.BUILT_IN_LIB, result);
    }

    public Token nextToken() {
        while(currentChar != '\0') {
            if(Character.isWhitespace(currentChar)) {
                skipWhitespace();
    
                if(currentChar == '\0') {
                    return createToken(TokenType.END_OF_FILE, "");
                }
            }
    
            if(Character.isDigit(currentChar)) {
                return createToken(TokenType.FLOAT, number());
            }
    
            if(Character.isAlphabetic(currentChar) || currentChar == '_') {
                return identifier();
            }
    
            if(currentChar == '\'') {
                return createToken(TokenType.STRING, string());
            }
    
            if(currentChar == '$') {
                return builtInLib();
            }
    
            String single = Character.toString(currentChar);
            String peeked = single + Character.toString(peek());
    
            if(keywords.containsKey(peeked)) {
                advance();
                advance();
    
                return createToken(keywords.get(peeked), peeked);
            }
    
            if(keywords.containsKey(single)) {
                advance();
    
                return createToken(keywords.get(single), single);
            }
    
            String message = "Unidentified token: " + currentChar;
            new SyntaxError(file, line, column, message).cast();
        }
    
        return createToken(TokenType.END_OF_FILE, "");
    }

}
