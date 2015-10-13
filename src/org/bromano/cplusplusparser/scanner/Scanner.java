package org.bromano.cplusplusparser.scanner;

import java.util.List;
import java.util.Map;

public interface Scanner {
    Token scan();
    List<Token> lex();
    void setText(String text);
    Map<String, TokenKind> generateKeywordMap();
}
