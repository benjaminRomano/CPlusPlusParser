package org.bromano.cplusplusparser.parser;

import org.bromano.cplusplusparser.scanner.Token;

import java.util.List;
import java.util.Stack;

public interface Parser {
    void setTokens(List<Token> tokens);
    Stack<String> parse() throws ParserException;
}
