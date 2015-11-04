package org.bromano.cplusplusparser.parser;

import org.bromano.cplusplusparser.parser.nodes.ParseNode;
import org.bromano.cplusplusparser.scanner.Token;

import java.util.List;

public interface Parser {
    void setTokens(List<Token> tokens);
    ParseNode parse() throws ParserException;
}
