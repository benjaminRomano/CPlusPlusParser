package org.bromano.cplusplusparser.tests;

import org.bromano.cplusplusparser.scanner.*;
import org.junit.Assert;

import java.util.List;

import static org.junit.Assert.*;

public class SimpleScannerTest {

    @org.junit.Test
    public void testLex() throws Exception {
        Scanner s = new SimpleScanner();

        //White space
        s.setText("\t\n\r\f#hello\n");
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

        s.setText("//=");
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
        s.setText("0xBu 0xBul 0xBull 0xBuL 0xBuLL 0xBU 0xBUl 0xBUll 0xBUL 0xBULL 0xABCDEF0123456789");
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
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        //Testing octal integer literals
        s.setText("07u 07ul 07ull 07uL 07uLL 07U 07Ul 07Ull 07UL 07ULL 0 01234567");
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
                new Token(TokenKind.EndOfFile)
        }, s.lex());

        try {
            s.setText("0x");
            s.lex();
        } catch(RuntimeException e) {
            Assert.assertEquals("Expected hexadecimal digit", e.getMessage());
        }
    }

    private void assertTokensMatch(Token[] expected, List<Token> actual) {
        Assert.assertArrayEquals(expected, actual.toArray(new Token[actual.size()]));
    }
}