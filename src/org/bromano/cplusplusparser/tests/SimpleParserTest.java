package org.bromano.cplusplusparser.tests;

import org.bromano.cplusplusparser.parser.Parser;
import org.bromano.cplusplusparser.parse.SimpleParser;
import org.bromano.cplusplusparser.parser.ParserException;
import org.bromano.cplusplusparser.scanner.SimpleScanner;
import org.bromano.cplusplusparser.scanner.Token;

import java.util.List;

public class SimpleParserTest {

    @FunctionalInterface
    public interface ParseFunction {
        void execute() throws ParserException;
    }

    @org.junit.Test
    public void testParse() throws Exception {
        Parser parser = new SimpleParser(getTokens("; ;"));
    }


    public List<Token> getTokens(String code) {
        return new SimpleScanner(code).lex();
    }

}