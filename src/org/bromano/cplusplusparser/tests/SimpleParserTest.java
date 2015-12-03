package org.bromano.cplusplusparser.tests;

import org.bromano.cplusplusparser.parser.Parser;
import org.bromano.cplusplusparser.parser.ParserException;
import org.bromano.cplusplusparser.parser.SimpleParser;
import org.bromano.cplusplusparser.scanner.ScannerException;
import org.bromano.cplusplusparser.scanner.SimpleScanner;
import org.bromano.cplusplusparser.scanner.Token;

import java.util.List;
import java.util.Stack;

public class SimpleParserTest {

    @org.junit.Test
    public void testParse() throws Exception {
        Parser parser = new SimpleParser(getTokens("int a(){ 10 * 5 + 5; }"));
        printStack(parser.parse());
    }

    public void printStack(Stack<String> stack) {
        while(!stack.isEmpty()) System.out.println(stack.pop());
    }


    public List<Token> getTokens(String code) throws ScannerException {
        return new SimpleScanner(code).lex();
    }

}