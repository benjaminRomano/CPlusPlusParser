package org.bromano.cplusplusparser.scanner;

import java.util.List;

public interface Scanner {
    Token scan();
    List<Token> lex();
    void setText(String text);
}
