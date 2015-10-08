package org.bromano.cplusplusparser.scanner;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class SimpleScanner implements Scanner {
    private String text;
    private int pos = 0;
    private int end = 0;

    public SimpleScanner() {
        this.setText("");
    }

    public SimpleScanner(String text) {
        this.setText(text);
    }

    public void setText(String text) {
        this.text = text;
        this.pos = 0;
        this.end = text.length();
    }

//    private boolean isAMatch(int pos, char[] chars) {
//        if(pos >= end) {
//            return false;
//        }
//
//        char currChar = text.charAt(pos);
//        for(char ch : chars) {
//            if(currChar == ch) {
//                return true;
//            }
//        }
//
//        return false;
//    }

    private boolean isAMatch(int pos, char ch) {
        if(pos >= end || text.charAt(pos) != ch) {
            return false;
        }

        return true;
    }

    public boolean isAMatch(int startPos, int endPos, String sequence) {
        if(startPos >= end || endPos > end) {
            return false;
        }

        return text.substring(startPos, endPos).equals(sequence);
    }

    public Token scan() {

        while(true) {
            if(pos >= end) {
                return new Token(TokenKind.EndOfFile);
            }

            char ch = text.charAt(pos);

            switch (ch) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    pos++;
                    continue;
                case '!':
                    pos++;
                    if(isAMatch(pos, '=')) {
                        pos++;
                        return new Token(TokenKind.ExclamationEquals);
                    }

                    return new Token(TokenKind.Exclamation);
                case '"':
                    throw new NotImplementedException();
                case '#':
                    while(pos < end) {
                        pos++;
                        if(isAMatch(pos, '\n')) {
                            continue;
                        }
                    }
                    continue;
                case '%':
                    pos++;
                    if(isAMatch(pos, pos + 3, ":%:")) {
                        pos += 3;
                        return new Token(TokenKind.PercentColonPercentColen);
                    } else if(isAMatch(pos, ':')) {
                        pos++;
                        return new Token(TokenKind.PercentColon);
                    } else if(isAMatch(pos, '=')) {
                        pos++;
                        return new Token(TokenKind.PercentEquals);
                    } else if(isAMatch(pos, '>')) {
                        pos++;
                        return new Token(TokenKind.PercentGreaterThan);
                    }

                    return new Token(TokenKind.Percent);
                case '&':
                    pos++;
                    if(isAMatch(pos, '=')) {
                        pos++;
                        return new Token(TokenKind.AmpersandEquals);
                    } else if(isAMatch(pos, '&')) {
                        pos++;
                        return new Token(TokenKind.AmpersandAmpersand);
                    }
                    return new Token(TokenKind.Ampersand);
                case '(':
                    pos++;
                    return new Token(TokenKind.OpenParen);
                case ')':
                    pos++;
                    return new Token(TokenKind.CloseParen);
                case '*':
                    pos++;
                    if(isAMatch(pos, '=')) {
                        pos++;
                        return new Token(TokenKind.AsteriskEquals);
                    }
                    return new Token(TokenKind.Asterisk);
                case '+':
                    pos++;
                    if(isAMatch(pos, '=')) {
                        pos++;
                        return new Token(TokenKind.PlusEquals);
                    } else if(isAMatch(pos, '+')) {
                        pos++;
                        return new Token(TokenKind.PlusPlus);
                    }

                    return new Token(TokenKind.Plus);
                case ',':
                    pos++;
                    return new Token(TokenKind.Comma);
                case '-':
                    pos++;
                    if(isAMatch(pos, pos+2, ">*")) {
                        pos += 2;
                        return new Token(TokenKind.MinusGreaterThanAsterisk);
                    } else if(isAMatch(pos, '=')) {
                        pos++;
                        return new Token(TokenKind.Minus);
                    } else if(isAMatch(pos, '>')) {
                        pos++;
                        return new Token(TokenKind.MinusGreaterThan);
                    } else if(isAMatch(pos, '-')) {
                        pos++;
                        return new Token(TokenKind.MinusMinus);
                    }

                    return new Token(TokenKind.Minus);
                case '.':
                    pos++;
                    if(isAMatch(pos, pos+2, "..")) {
                        pos += 2;
                        return new Token(TokenKind.DotDotDot);
                    } else if(isAMatch(pos, '*')) {
                        pos++;
                        return new Token(TokenKind.DotAsterisk);
                    }

                    return new Token(TokenKind.Dot);
                case '/':
                    pos++;
                    if(isAMatch(pos, '=')) {
                        pos++;
                        return new Token(TokenKind.SlashEquals);
                    }

                    return new Token(TokenKind.Slash);
                case ':':
                    pos++;
                    if(isAMatch(pos, ':')) {
                        pos++;
                        return new Token(TokenKind.ColonColon);
                    } else if(isAMatch(pos, '>')) {
                        pos++;
                        return new Token(TokenKind.ColonGreaterThan);
                    }

                    return new Token(TokenKind.Colon);
                case ';':
                    pos++;
                    return new Token(TokenKind.Semicolon);
                case '<':
                    //TODO: Make sure to finish this
                    pos++;
                    return new Token(TokenKind.LessThan);
                case '[':
                    pos++;
                    return new Token(TokenKind.OpenBracket);
                case ']':
                    pos++;
                    return new Token(TokenKind.CloseBracket);
                case '{':
                    pos++;
                    return new Token(TokenKind.OpenBrace);
                case '}':
                    pos++;
                    return new Token(TokenKind.CloseBrace);
                default:
                    pos++;
                    //TODO: If char doesn't match a tokenKind throw error
                    continue;
            }
        }
    }


    public List<Token> lex() {
        List<Token> tokens = new ArrayList<Token>();

        Token token = scan();
        tokens.add(token);

        while(token.kind != TokenKind.EndOfFile) {
            token = scan();
            tokens.add(token);
        }

        return tokens;
    }
}
