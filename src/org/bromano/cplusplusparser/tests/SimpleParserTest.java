package org.bromano.cplusplusparser.tests;

import org.bromano.cplusplusparser.parser.nodes.*;

public class SimpleParserTest {

    @org.junit.Test
    public void testParse() throws Exception {
        TranslationUnit node = new TranslationUnit();
        node.print(1);
    }
}