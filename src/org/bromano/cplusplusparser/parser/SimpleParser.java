package org.bromano.cplusplusparser.parser;

import org.bromano.cplusplusparser.parser.nodes.ParseNode;
import org.bromano.cplusplusparser.scanner.Token;

import java.util.ArrayList;
import java.util.List;

public class SimpleParser implements Parser {
    protected List<Token> tokens;

    public SimpleParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public SimpleParser() {
        this.tokens = new ArrayList();
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;

    }

    public ParseNode parse() {
        return null;
    }
}
