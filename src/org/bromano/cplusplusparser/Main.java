package org.bromano.cplusplusparser;
import org.bromano.cplusplusparser.scanner.*;

public class Main {

    public static void main(String[] args) {
        //Testing Token
        Token s = new Token(TokenKind.Ampersand, "&");
        System.out.println(s.kind);
        System.out.println(s.lexeme);
        System.out.println(s);
    }
}
