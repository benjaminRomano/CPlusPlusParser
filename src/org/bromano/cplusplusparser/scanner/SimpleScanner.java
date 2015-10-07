package org.bromano.cplusplusparser.scanner;

import java.util.ArrayList;
import java.util.List;

public class SimpleScanner implements Scanner {
    private String text;
    private int pos = 0;
    private int end = 0;

    public SimpleScanner() {
        this.setText("");
    }

    public SimpleScanner(String text) {
        this.setText(text);
    }

    public void setText(String text) {
        this.text = text;
        this.pos = 0;
        this.end = text.length();
    }

    public Token scan() {

        while(true) {
            if(pos >= end) {
                return new Token(TokenKind.EndOfFile);
            }

            char ch = text.charAt(pos);

            switch (ch) {
                case '\r':
                case '\n':
                    pos++;
                    continue;
                default:
                    pos++;
                    continue;
            }
        }
    }


    public List<Token> lex() {
        List<Token> tokens = new ArrayList<Token>();

        Token token = scan();
        tokens.add(token);

        while(token.kind != TokenKind.EndOfFile) {
            token = scan();
            tokens.add(token);
        }

        return tokens;
    }
}
