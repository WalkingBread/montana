package skorupinski.montana.utils;

import skorupinski.montana.lexer.Token;

public class Error {

    private String type;

    private String file;

    private String message;

    private int line;

    private int column;

    protected Error(String type, String file, int line, int column, String message) {
        this.type = type;
        this.file = file;
        this.line = line;
        this.column = column;
        this.message = message;
    }

    public static class SyntaxError extends Error {

        public SyntaxError(String file, int line, int column, String message) {
            super("SyntaxError", file, line, column, message);
        }

        public SyntaxError(Token token, String message) {
            super("SyntaxError", token.file, token.line, token.column, message);
        }
    }

    public static class ValueError extends Error {

        public ValueError(String file, int line, int column, String message) {
            super("ValueError", file, line, column, message);
        }

        public ValueError(Token token, String message) {
            super("ValueError", token.file, token.line, token.column, message);
        }
    }
    public static class NameError extends Error {

        public NameError(String file, int line, int column, String message) {
            super("NameError", file, line, column, message);
        }

        public NameError(Token token, String message) {
            super("NameError", token.file, token.line, token.column, message);
        }
    }

    public void cast() {
        System.out.println(type + ": In file: " + file + ", line: " + line + " column: " + column + " " + message);
        System.exit(0);
    }
}
