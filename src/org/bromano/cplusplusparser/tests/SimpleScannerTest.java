package org.bromano.cplusplusparser.tests;

import org.bromano.cplusplusparser.scanner.*;
import org.junit.Assert;

import java.util.List;
import java.util.Map;

public class SimpleScannerTest {

    @org.junit.Test
    public void testLex() throws Exception {
        Scanner s = new SimpleScanner();

        //White space
        s.setText("\t\n\r\f #hello\n //hello\n /* int s = 5; \n int y = 6; */");
        assertTokensMatch(new Token[]{
               new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("!=!");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.ExclamationEquals),
                new Token(TokenKind.Exclamation),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("%:%=%:%:%%>");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.PercentColon),
                new Token(TokenKind.PercentEquals),
                new Token(TokenKind.PercentColonPercentColen),
                new Token(TokenKind.Percent),
                new Token(TokenKind.PercentGreaterThan),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("&=&&&");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.AmpersandEquals),
                new Token(TokenKind.AmpersandAmpersand),
                new Token(TokenKind.Ampersand),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        //Tokens with only one case
        s.setText("{[()]}?;~");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.OpenBrace),
                new Token(TokenKind.OpenBracket),
                new Token(TokenKind.OpenParen),
                new Token(TokenKind.CloseParen),
                new Token(TokenKind.CloseBracket),
                new Token(TokenKind.CloseBrace),
                new Token(TokenKind.Question),
                new Token(TokenKind.Semicolon),
                new Token(TokenKind.Tilde),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("**=");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.Asterisk),
                new Token(TokenKind.AsteriskEquals),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("->*->---=-");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.MinusGreaterThanAsterisk),
                new Token(TokenKind.MinusGreaterThan),
                new Token(TokenKind.MinusMinus),
                new Token(TokenKind.MinusEquals),
                new Token(TokenKind.Minus),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("+=+++");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.PlusEquals),
                new Token(TokenKind.PlusPlus),
                new Token(TokenKind.Plus),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("/ /=");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.Slash),
                new Token(TokenKind.SlashEquals),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText(".....*");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.DotDotDot),
                new Token(TokenKind.Dot),
                new Token(TokenKind.DotAsterisk),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText(":::>:");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.ColonColon),
                new Token(TokenKind.ColonGreaterThan),
                new Token(TokenKind.Colon),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("<<=<=<:<%<<<");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.LessThanLessThanEquals),
                new Token(TokenKind.LessThanEquals),
                new Token(TokenKind.LessThanColon),
                new Token(TokenKind.LessThanPercent),
                new Token(TokenKind.LessThanLessThan),
                new Token(TokenKind.LessThan),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("===");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.EqualsEquals),
                new Token(TokenKind.Equals),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText(">>=>>>=>");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.GreaterThanGreaterThanEquals),
                new Token(TokenKind.GreaterThanGreaterThan),
                new Token(TokenKind.GreaterThanEquals),
                new Token(TokenKind.GreaterThan),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("^=^");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.CaretEquals),
                new Token(TokenKind.Caret),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        s.setText("|||=|");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.BarBar),
                new Token(TokenKind.BarEquals),
                new Token(TokenKind.Bar),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        //Testing hexadecimal integer literals
        s.setText("0xBu 0xBul 0xBull 0xBuL 0xBuLL 0xBU 0xBUl 0xBUll 0xBUL 0xBULL 0xABCDEF0123456789 0x0BZ");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.IntegerLiteral, "0xBu"),
                new Token(TokenKind.IntegerLiteral, "0xBul"),
                new Token(TokenKind.IntegerLiteral, "0xBull"),
                new Token(TokenKind.IntegerLiteral, "0xBuL"),
                new Token(TokenKind.IntegerLiteral, "0xBuLL"),
                new Token(TokenKind.IntegerLiteral, "0xBU"),
                new Token(TokenKind.IntegerLiteral, "0xBUl"),
                new Token(TokenKind.IntegerLiteral, "0xBUll"),
                new Token(TokenKind.IntegerLiteral, "0xBUL"),
                new Token(TokenKind.IntegerLiteral, "0xBULL"),
                new Token(TokenKind.IntegerLiteral, "0xABCDEF0123456789"),
                new Token(TokenKind.UserDefinedIntegerLiteral, "0x0BZ"),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        try {
            s.setText("0x");
            s.lex();
        } catch(RuntimeException e) {
            Assert.assertEquals("Expected hexadecimal digit", e.getMessage());
        }

        //Testing octal integer literals
        s.setText("07u 07ul 07ull 07uL 07uLL 07U 07Ul 07Ull 07UL 07ULL 0 01234567 0AB");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.IntegerLiteral, "07u"),
                new Token(TokenKind.IntegerLiteral, "07ul"),
                new Token(TokenKind.IntegerLiteral, "07ull"),
                new Token(TokenKind.IntegerLiteral, "07uL"),
                new Token(TokenKind.IntegerLiteral, "07uLL"),
                new Token(TokenKind.IntegerLiteral, "07U"),
                new Token(TokenKind.IntegerLiteral, "07Ul"),
                new Token(TokenKind.IntegerLiteral, "07Ull"),
                new Token(TokenKind.IntegerLiteral, "07UL"),
                new Token(TokenKind.IntegerLiteral, "07ULL"),
                new Token(TokenKind.IntegerLiteral, "0"),
                new Token(TokenKind.IntegerLiteral, "01234567"),
                new Token(TokenKind.UserDefinedIntegerLiteral, "0AB"),
                new Token(TokenKind.EndOfFile)
        }, s.lex());


        //Testing decimal integer literals
        s.setText("7u 7ul 7ull 7uL 7uLL 7U 7Ul 7Ull 7UL 7ULL 1234567890 1AB");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.IntegerLiteral, "7u"),
                new Token(TokenKind.IntegerLiteral, "7ul"),
                new Token(TokenKind.IntegerLiteral, "7ull"),
                new Token(TokenKind.IntegerLiteral, "7uL"),
                new Token(TokenKind.IntegerLiteral, "7uLL"),
                new Token(TokenKind.IntegerLiteral, "7U"),
                new Token(TokenKind.IntegerLiteral, "7Ul"),
                new Token(TokenKind.IntegerLiteral, "7Ull"),
                new Token(TokenKind.IntegerLiteral, "7UL"),
                new Token(TokenKind.IntegerLiteral, "7ULL"),
                new Token(TokenKind.IntegerLiteral, "1234567890"),
                new Token(TokenKind.UserDefinedIntegerLiteral, "1AB"),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        //Scanning string-literals with no non-raw strings
        s.setText("\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_-+=[]{}<>,./?~`|'\" \"\\u1234\" \"\\UFFFFAAAA\" \"\\'\\\"\\?\\\\\\a\\b\\f\\n\\r\\t\\v\" U\"\" u\"\" L\"\" u8\"\" \"\"A ");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.StringLiteral, "\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_-+=[]{}<>,./?~`|'\""),
                new Token(TokenKind.StringLiteral, "\"\\u1234\""),
                new Token(TokenKind.StringLiteral, "\"\\UFFFFAAAA\""),
                new Token(TokenKind.StringLiteral, "\"\\'\\\"\\?\\\\\\a\\b\\f\\n\\r\\t\\v\""),
                new Token(TokenKind.StringLiteral, "U\"\""),
                new Token(TokenKind.StringLiteral, "u\"\""),
                new Token(TokenKind.StringLiteral, "L\"\""),
                new Token(TokenKind.StringLiteral, "u8\"\""),
                new Token(TokenKind.UserDefinedStringLiteral, "\"\"A"),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        //Scanning string-literals with no non-raw strings
        s.setText("u8R\"()\" uR\"()\" UR\"()\" LR\"()\" R\"works(test()works\" R\"(test)\" R\"\"()\"\" R\"\"()\"\"A");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.StringLiteral, "u8R\"()\""),
                new Token(TokenKind.StringLiteral, "uR\"()\""),
                new Token(TokenKind.StringLiteral, "UR\"()\""),
                new Token(TokenKind.StringLiteral, "LR\"()\""),
                new Token(TokenKind.StringLiteral, "R\"works(test()works\""),
                new Token(TokenKind.StringLiteral, "R\"(test)\""),
                new Token(TokenKind.StringLiteral, "R\"\"()\"\""),
                new Token(TokenKind.UserDefinedStringLiteral, "R\"\"()\"\"A"),
                new Token(TokenKind.EndOfFile)
        }, s.lex());


        //Scanning character-literals
        s.setText("'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_-+=[]{}<>,./?~`|\"' '\\u1234' '\\UFFFFAAAA' '\\'\\\"\\?\\\\\\a\\b\\f\\n\\r\\t\\v' '\\x0123456789ABCDEF' '\\01234567' U'a' u'a' L'a'");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.CharacterLiteral, "'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_-+=[]{}<>,./?~`|\"'"),
                new Token(TokenKind.CharacterLiteral, "'\\u1234'"),
                new Token(TokenKind.CharacterLiteral, "'\\UFFFFAAAA'"),
                new Token(TokenKind.CharacterLiteral, "'\\'\\\"\\?\\\\\\a\\b\\f\\n\\r\\t\\v'"),
                new Token(TokenKind.CharacterLiteral, "'\\x0123456789ABCDEF'"),
                new Token(TokenKind.CharacterLiteral, "'\\01234567'"),
                new Token(TokenKind.CharacterLiteral, "U'a'"),
                new Token(TokenKind.CharacterLiteral, "u'a'"),
                new Token(TokenKind.CharacterLiteral, "L'a'"),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        //Scanning identifiers
        s.setText("\\U0000FFFF \\u0000 _\\u0000ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789\\U1234AAAA a_");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.Identifier, "\\U0000FFFF"),
                new Token(TokenKind.Identifier, "\\u0000"),
                new Token(TokenKind.Identifier, "_\\u0000ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789\\U1234AAAA"),
                new Token(TokenKind.Identifier, "a_"),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        //Scanning floating-literals
        s.setText("0e+5 0E-5 1e5 0.0f 0.F .1l 1.1L 0.e1 .1E1 0e+5A .1E1A");
        assertTokensMatch(new Token[]{
                new Token(TokenKind.FloatingLiteral, "0e+5"),
                new Token(TokenKind.FloatingLiteral, "0E-5"),
                new Token(TokenKind.FloatingLiteral, "1e5"),
                new Token(TokenKind.FloatingLiteral, "0.0f"),
                new Token(TokenKind.FloatingLiteral, "0.F"),
                new Token(TokenKind.FloatingLiteral, ".1l"),
                new Token(TokenKind.FloatingLiteral, "1.1L"),
                new Token(TokenKind.FloatingLiteral, "0.e1"),
                new Token(TokenKind.FloatingLiteral, ".1E1"),
                new Token(TokenKind.UserDefinedFloatingLiteral, "0e+5A"),
                new Token(TokenKind.UserDefinedFloatingLiteral, ".1E1A"),
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        //Scan keywords
        Map<String, TokenKind> keywordMap = s.generateKeywordMap();
        for(String keyword : keywordMap.keySet()) {
            s.setText(keyword);
            assertTokensMatch(new Token[] {
                    new Token(keywordMap.get(keyword), keyword),
                    new Token(TokenKind.EndOfFile)
            }, s.lex());
        }
    }

    private void assertTokensMatch(Token[] expected, List<Token> actual) {
        Assert.assertArrayEquals(expected, actual.toArray(new Token[actual.size()]));
    }
}