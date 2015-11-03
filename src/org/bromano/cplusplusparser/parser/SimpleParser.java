package org.bromano.cplusplusparser.parser;

import org.bromano.cplusplusparser.parser.nodes.*;
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
        ParserResult<TranslationUnit> parseTreeResults = tryParseTranslationUnit();

        //TODO: Fix this to actually handle errors in the way wanted
        if(parseTreeResults.error != null) {
            System.out.println(parseTreeResults.error);
            return null;
        }

        return parseTreeResults.value;
    }

    public ParserResult<TranslationUnit> tryParseTranslationUnit() {
        ParserResult<TranslationUnit> parseResult = new ParserResult("NOT IMPLEMENTED YET");
        return parseResult;
    }
}
