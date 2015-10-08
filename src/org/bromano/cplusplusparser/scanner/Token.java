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

    @Override
    public boolean equals(Object obj) {
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Token token = (Token) obj;
        if(token.kind != this.kind || token.lexeme != this.lexeme) {
            return false;
        }
        return true;

    }
}
