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

    public boolean isAMatch(int startPos, String sequence) {
        int endPos = pos + sequence.length();
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
                    if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.ExclamationEquals);
                    }

                    return new Token(TokenKind.Exclamation);
                case '"':
                    throw new NotImplementedException();
                case '#':
                    while(pos < end) {
                        pos++;
                        if(isAMatch(pos, "\n")) {
                            continue;
                        }
                    }
                    continue;
                case '%':
                    pos++;
                    if(isAMatch(pos, ":%:")) {
                        pos += 3;
                        return new Token(TokenKind.PercentColonPercentColen);
                    } else if(isAMatch(pos, ":")) {
                        pos++;
                        return new Token(TokenKind.PercentColon);
                    } else if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.PercentEquals);
                    } else if(isAMatch(pos, ">")) {
                        pos++;
                        return new Token(TokenKind.PercentGreaterThan);
                    }

                    return new Token(TokenKind.Percent);
                case '&':
                    pos++;
                    if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.AmpersandEquals);
                    } else if(isAMatch(pos, "&")) {
                        pos++;
                        return new Token(TokenKind.AmpersandAmpersand);
                    }
                    return new Token(TokenKind.Ampersand);
                case '\'':
                    throw new NotImplementedException();
                case '(':
                    pos++;
                    return new Token(TokenKind.OpenParen);
                case ')':
                    pos++;
                    return new Token(TokenKind.CloseParen);
                case '*':
                    pos++;
                    if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.AsteriskEquals);
                    }
                    return new Token(TokenKind.Asterisk);
                case '+':
                    pos++;
                    if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.PlusEquals);
                    } else if(isAMatch(pos, "+")) {
                        pos++;
                        return new Token(TokenKind.PlusPlus);
                    }

                    return new Token(TokenKind.Plus);
                case ',':
                    pos++;
                    return new Token(TokenKind.Comma);
                case '-':
                    pos++;
                    if(isAMatch(pos, ">*")) {
                        pos += 2;
                        return new Token(TokenKind.MinusGreaterThanAsterisk);
                    } else if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.MinusEquals);
                    } else if(isAMatch(pos, ">")) {
                        pos++;
                        return new Token(TokenKind.MinusGreaterThan);
                    } else if(isAMatch(pos, "-")) {
                        pos++;
                        return new Token(TokenKind.MinusMinus);
                    }

                    return new Token(TokenKind.Minus);
                case '.':
                    pos++;
                    if(isAMatch(pos, "..")) {
                        pos += 2;
                        return new Token(TokenKind.DotDotDot);
                    } else if(isAMatch(pos, "*")) {
                        pos++;
                        return new Token(TokenKind.DotAsterisk);
                    }

                    return new Token(TokenKind.Dot);
                case '/':
                    pos++;
                    if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.SlashEquals);
                    }

                    return new Token(TokenKind.Slash);
                case ':':
                    pos++;
                    if(isAMatch(pos, ":")) {
                        pos++;
                        return new Token(TokenKind.ColonColon);
                    } else if(isAMatch(pos, ">")) {
                        pos++;
                        return new Token(TokenKind.ColonGreaterThan);
                    }

                    return new Token(TokenKind.Colon);
                case ';':
                    pos++;
                    return new Token(TokenKind.Semicolon);
                case '<':
                    pos++;

                    if(isAMatch(pos, "<=")) {
                        pos += 2;
                        return new Token(TokenKind.LessThanLessThanEquals);
                    } else if(isAMatch(pos, "<")) {
                        pos++;
                        return new Token(TokenKind.LessThanLessThan);
                    } else if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.LessThanEquals);
                    } else if(isAMatch(pos, ":")) {
                        pos++;
                        return new Token(TokenKind.LessThanColon);
                    } else if(isAMatch(pos, "%")) {
                        pos++;
                        return new Token(TokenKind.LessThanPercent);
                    }

                    return new Token(TokenKind.LessThan);
                case '=':
                    pos++;
                    if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.EqualsEquals);
                    }
                    return new Token(TokenKind.Equals);
                case '>':
                    pos++;
                    if(isAMatch(pos, ">=")) {
                        pos += 2;
                        return new Token(TokenKind.GreaterThanGreaterThanEquals);
                    } else if(isAMatch(pos, ">")) {
                        pos++;
                        return new Token(TokenKind.GreaterThanGreaterThan);
                    } else if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.GreaterThanEquals);
                    }

                    return new Token(TokenKind.GreaterThan);
                case '?':
                    pos++;
                    return new Token(TokenKind.Question);
                case '[':
                    pos++;
                    return new Token(TokenKind.OpenBracket);
                case '\\':
                    throw new NotImplementedException();
                case ']':
                    pos++;
                    return new Token(TokenKind.CloseBracket);
                case '^':
                    pos++;
                    if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.CaretEquals);
                    }
                    return new Token(TokenKind.Caret);
                case '_':
                    throw new NotImplementedException();
                case '{':
                    pos++;
                    return new Token(TokenKind.OpenBrace);
                case '|':
                    pos++;
                    if(isAMatch(pos, "|")) {
                       pos++;
                        return new Token(TokenKind.BarBar);
                    } else if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.BarEquals);
                    }
                    return new Token(TokenKind.Bar);
                case '}':
                    pos++;
                    return new Token(TokenKind.CloseBrace);
                case '~':
                    pos++;
                    return new Token(TokenKind.Tilde);
                default:
                    throw new NotImplementedException();
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
