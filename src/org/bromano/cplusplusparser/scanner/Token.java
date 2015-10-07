package org.bromano.cplusplusparser.scanner;

public class Token {
    public TokenKind kind;
    public String lexeme;
    //TODO: Add line number and position?

    public Token(TokenKind kind) {
        this.kind = kind;
        this.lexeme = null;
    }

    public Token(TokenKind kind, String lexeme) {
        this.kind = kind;
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return this.kind.name() + " " + this.lexeme;
    }
}
