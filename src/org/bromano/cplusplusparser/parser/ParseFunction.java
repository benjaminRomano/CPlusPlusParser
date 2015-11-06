package org.bromano.cplusplusparser.parser;

@FunctionalInterface
public interface ParseFunction {
    void execute() throws ParserException;
}
