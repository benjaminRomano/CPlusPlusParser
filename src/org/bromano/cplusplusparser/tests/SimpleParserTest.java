package org.bromano.cplusplusparser.tests;

import org.bromano.cplusplusparser.parser.Parser;
import org.bromano.cplusplusparser.parser.SimpleParser;
import org.bromano.cplusplusparser.parser.nodes.*;

public class SimpleParserTest {

    @org.junit.Test
    public void testParse() throws Exception {
        Parser parser = new SimpleParser();
        parser.parse();

        //TranslationUnit node = new TranslationUnit();
        //node.print(1);
    }
}