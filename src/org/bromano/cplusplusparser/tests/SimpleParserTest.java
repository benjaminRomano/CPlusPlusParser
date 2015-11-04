package org.bromano.cplusplusparser.tests;

import org.bromano.cplusplusparser.parser.Parser;
import org.bromano.cplusplusparser.parser.SimpleParser;
import org.bromano.cplusplusparser.parser.nodes.*;
import org.bromano.cplusplusparser.scanner.Scanner;
import org.bromano.cplusplusparser.scanner.SimpleScanner;
import org.bromano.cplusplusparser.scanner.Token;

import java.util.List;

public class SimpleParserTest {

    @org.junit.Test
    public void testParse() throws Exception {
        Parser parser = new SimpleParser(getTokens("; ;"));
        parser.parse().print();
    }


    public List<Token> getTokens(String code) {
        return new SimpleScanner(code).lex();
    }

}