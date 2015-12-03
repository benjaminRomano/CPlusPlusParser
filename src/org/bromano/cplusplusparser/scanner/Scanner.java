package org.bromano.cplusplusparser.scanner;

import java.util.List;
import java.util.Map;

public interface Scanner {
    Token scan() throws ScannerException;
    List<Token> lex() throws ScannerException;
    void setText(String text);
    Map<String, TokenKind> generateKeywordMap();
    List<Token> getTokens();
}
