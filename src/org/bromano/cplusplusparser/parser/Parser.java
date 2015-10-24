package org.bromano.cplusplusparser.parser;

import org.bromano.cplusplusparser.parser.nodes.ParseNode;
import org.bromano.cplusplusparser.scanner.Token;

import java.util.List;

public interface Parser {
    ParseNode parse(List<Token> tokens);
}
