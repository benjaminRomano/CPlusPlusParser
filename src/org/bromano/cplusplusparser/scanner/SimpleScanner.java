package org.bromano.cplusplusparser.scanner;

import java.util.*;

public class SimpleScanner implements Scanner {
    private String text;
    private Map<String, TokenKind> keywordMap;
    private int pos = 0;
    private int end = 0;

    public SimpleScanner() {
        this.setText("");
        this.keywordMap = generateKeywordMap();
    }

    public SimpleScanner(String text) {
        this.setText(text);
        this.keywordMap = generateKeywordMap();
    }

    public void setText(String text) {
        this.text = text;
        this.pos = 0;
        this.end = text.length();
    }

    public Map<String, TokenKind> generateKeywordMap() {
        Map<String, TokenKind> keywordMap = new HashMap<>();
        keywordMap.put("true", TokenKind.BooleanLiteral);
        keywordMap.put("false", TokenKind.BooleanLiteral);
        keywordMap.put("nullptr", TokenKind.PointerLiteral);
        keywordMap.put("alignas", TokenKind.AlignasKeyword);
        keywordMap.put("alignof", TokenKind.AlignofKeyword);
        keywordMap.put("asm", TokenKind.AsmKeyword);
        keywordMap.put("auto", TokenKind.AutoKeyword);
        keywordMap.put("bool", TokenKind.BoolKeyword);
        keywordMap.put("break", TokenKind.BreakKeyword);
        keywordMap.put("case", TokenKind.CaseKeyword);
        keywordMap.put("catch", TokenKind.CatchKeyword);
        keywordMap.put("char", TokenKind.CharKeyword);
        keywordMap.put("char16_t", TokenKind.Char16TKeyword);
        keywordMap.put("char32_t", TokenKind.Char32TKeyword);
        keywordMap.put("class", TokenKind.ClassKeyword);
        keywordMap.put("const", TokenKind.ConstKeyword);
        keywordMap.put("constexpr", TokenKind.ConstexprKeyword);
        keywordMap.put("const_cast", TokenKind.ConstCastKeyword);
        keywordMap.put("continue", TokenKind.ContinueKeyword);
        keywordMap.put("decltype", TokenKind.DecltypeKeyword);
        keywordMap.put("default", TokenKind.DefaultKeyword);
        keywordMap.put("delete", TokenKind.DeleteKeyword);
        keywordMap.put("do", TokenKind.DoKeyword);
        keywordMap.put("double", TokenKind.DoubleKeyword);
        keywordMap.put("dynamic_cast", TokenKind.DynamicCastKeyword);
        keywordMap.put("else", TokenKind.ElseKeyword);
        keywordMap.put("enum", TokenKind.EnumKeyword);
        keywordMap.put("explicit", TokenKind.ExplicitKeyword);
        keywordMap.put("export", TokenKind.ExportKeyword);
        keywordMap.put("extern", TokenKind.ExternKeyword);
        keywordMap.put("float", TokenKind.FloatKeyword);
        keywordMap.put("for", TokenKind.ForKeyword);
        keywordMap.put("friend", TokenKind.FriendKeyword);
        keywordMap.put("goto", TokenKind.GotoKeyword);
        keywordMap.put("if", TokenKind.IfKeyword);
        keywordMap.put("inline", TokenKind.InlineKeyword);
        keywordMap.put("int", TokenKind.IntKeyword);
        keywordMap.put("long", TokenKind.LongKeyword);
        keywordMap.put("mutable", TokenKind.MutableKeyword);
        keywordMap.put("namespace", TokenKind.NamespaceKeyword);
        keywordMap.put("new", TokenKind.NewKeyword);
        keywordMap.put("noexcept", TokenKind.NoexceptKeyword);
        keywordMap.put("operator", TokenKind.OperatorKeyword);
        keywordMap.put("private", TokenKind.PrivateKeyword);
        keywordMap.put("protected", TokenKind.ProtectedKeyword);
        keywordMap.put("public", TokenKind.PublicKeyword);
        keywordMap.put("register", TokenKind.RegisterKeyword);
        keywordMap.put("reinterpret_cast", TokenKind.ReinterpretCastKeyword);
        keywordMap.put("return", TokenKind.ReturnKeyword);
        keywordMap.put("short", TokenKind.ShortKeyword);
        keywordMap.put("signed", TokenKind.SignedKeyword);
        keywordMap.put("sizeof", TokenKind.SizeofKeyword);
        keywordMap.put("static", TokenKind.StaticKeyword);
        keywordMap.put("static_assert", TokenKind.StaticAssertKeyword);
        keywordMap.put("static_cast", TokenKind.StaticCastKeyword);
        keywordMap.put("struct", TokenKind.StructKeyword);
        keywordMap.put("switch", TokenKind.SwitchKeyword);
        keywordMap.put("template", TokenKind.TemplateKeyword);
        keywordMap.put("this", TokenKind.ThisKeyword);
        keywordMap.put("thread_local", TokenKind.ThreadLocalKeyword);
        keywordMap.put("throw", TokenKind.ThrowKeyword);
        keywordMap.put("try", TokenKind.TryKeyword);
        keywordMap.put("typedef", TokenKind.TypedefKeyword);
        keywordMap.put("typeid", TokenKind.TypeidKeyword);
        keywordMap.put("typename", TokenKind.TypenameKeyword);
        keywordMap.put("union", TokenKind.UnionKeyword);
        keywordMap.put("unsigned", TokenKind.UnsignedKeyword);
        keywordMap.put("using", TokenKind.UsingKeyword);
        keywordMap.put("virtual", TokenKind.VirtualKeyword);
        keywordMap.put("void", TokenKind.VoidKeyword);
        keywordMap.put("volatile", TokenKind.VolatileKeyword);
        keywordMap.put("wchar_t", TokenKind.WcharTKeyword);
        keywordMap.put("while", TokenKind.WhileKeyword);
        keywordMap.put("and", TokenKind.And);
        keywordMap.put("and_eq", TokenKind.AndEq);
        keywordMap.put("bitand", TokenKind.Bitand);
        keywordMap.put("bitor", TokenKind.Bitor);
        keywordMap.put("compl", TokenKind.Compl);
        keywordMap.put("not", TokenKind.Not);
        keywordMap.put("not_eq", TokenKind.NotEq);
        keywordMap.put("or", TokenKind.Or);
        keywordMap.put("or_eq", TokenKind.OrEq);
        keywordMap.put("xor", TokenKind.Xor);
        keywordMap.put("xor_eq", TokenKind.XorEq);

        return keywordMap;
    }

    private void error(String message) {
        throw new RuntimeException(message);
    }

    public boolean isAMatch(int pos, char[] chars) {
        if(pos >= end) {
            return false;
        }

        char currChar = text.charAt(pos);

        for(char ch : chars) {
            if(ch == currChar) {
                return true;
            }
        }

        return false;
    }

    public boolean isAMatch(int startPos, String sequence) {
        int endPos = startPos + sequence.length();

        return startPos < end && endPos <= end && text.substring(startPos, endPos).equals(sequence);

    }

    /*
        NOTE: Will not scan user-defined literals
    */
    public Token scan() {

        while(true) {
            String value = "";
            String userDefinedSuffix = "";
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
                    value = scanString();
                    userDefinedSuffix = scanIdentifier();

                    if(userDefinedSuffix.length() > 0) {
                        return new Token(TokenKind.UserDefinedStringLiteral, value + userDefinedSuffix);
                    }

                    return new Token(TokenKind.StringLiteral, value);
                case '#':
                    while(pos < end) {
                        pos++;
                        if(isAMatch(pos, "\n")) {
                            break;
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
                    value = scanCharSequence();
                    userDefinedSuffix = scanIdentifier();

                    if(userDefinedSuffix.length() > 0) {
                        return new Token(TokenKind.UserDefinedCharacterLiteral, value + userDefinedSuffix);
                    }
                    return new Token(TokenKind.CharacterLiteral, value);
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
                    } else if(isDecimalDigit(text.charAt(pos))) {
                        //Unconsume dot for floating-literal
                        pos--;
                        value = scanFloatingLiteral();
                        userDefinedSuffix = scanIdentifier();

                        if(userDefinedSuffix.length() > 0) {
                            return new Token(TokenKind.UserDefinedFloatingLiteral, value + userDefinedSuffix);
                        }

                        return new Token(TokenKind.FloatingLiteral, value);
                    }

                    return new Token(TokenKind.Dot);
                case '/':
                    pos++;
                    if(isAMatch(pos, "=")) {
                        pos++;
                        return new Token(TokenKind.SlashEquals);
                    } else if(isAMatch(pos, "/")) {
                        pos++;
                        while(pos < end) {
                            if(isAMatch(pos, "\n")) {
                                pos++;
                                break;
                            }
                            pos++;
                        }
                        continue;
                    } else if(isAMatch(pos, "*")) {
                        pos++;
                        while(pos < end) {
                            if(isAMatch(pos, "*/")) {
                                pos += 2;
                                break;
                            }
                            pos++;
                        }
                        continue;
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
                    return new Token(TokenKind.Identifier, scanIdentifier());
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
                    return new Token(TokenKind.Identifier, scanIdentifier());
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
                case '0':
                    pos++;
                    value = "0";
                    if(isAMatch(pos, "x") || isAMatch(pos, "X")) {
                        value += text.charAt(pos);
                        pos++;
                        value += scanHexDigits(1, true) + scanIntegerSuffix();
                        userDefinedSuffix = scanIdentifier();

                        if(userDefinedSuffix.length() > 0) {
                            return new Token(TokenKind.UserDefinedIntegerLiteral, value + userDefinedSuffix);
                        }

                        return new Token(TokenKind.IntegerLiteral, value);
                    } else if(isFloatingLiteral()) {
                        //Unconsume 0 for floating-literal
                        pos--;
                        value = scanFloatingLiteral();
                        userDefinedSuffix = scanIdentifier();

                        if(userDefinedSuffix.length() > 0) {
                            return new Token(TokenKind.UserDefinedFloatingLiteral, value + userDefinedSuffix);
                        }
                        return new Token(TokenKind.FloatingLiteral, value);

                    } else if(isOctalDigit(text.charAt(pos))) {
                        value += scanOctalDigits() + scanIntegerSuffix();
                        userDefinedSuffix = scanIdentifier();

                        if(userDefinedSuffix.length() > 0) {
                            return new Token(TokenKind.UserDefinedIntegerLiteral, value + userDefinedSuffix);
                        }

                        return new Token(TokenKind.IntegerLiteral, value);
                    }

                    userDefinedSuffix = scanIdentifier();

                    if(userDefinedSuffix.length() > 0) {
                        return new Token(TokenKind.UserDefinedIntegerLiteral, value + userDefinedSuffix);
                    }

                    return new Token(TokenKind.IntegerLiteral, value);
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if(isFloatingLiteral()) {
                        value = scanFloatingLiteral();
                        userDefinedSuffix = scanIdentifier();
                        if(userDefinedSuffix.length() > 0) {
                           return new Token(TokenKind.UserDefinedFloatingLiteral, value + userDefinedSuffix);
                        }
                        return new Token(TokenKind.FloatingLiteral, value);
                    }

                    value = scanDecimalDigits() + scanIntegerSuffix();
                    userDefinedSuffix = scanIdentifier();

                    if(userDefinedSuffix.length() > 0) {
                        return new Token(TokenKind.UserDefinedIntegerLiteral, value + userDefinedSuffix);
                    }

                    return new Token(TokenKind.IntegerLiteral, value);
                default:
                    if(isAMatch(pos, "u8R\"")) {
                        value = scanEncodingPrefix() + scanRawString();
                        userDefinedSuffix = scanIdentifier();

                        if(userDefinedSuffix.length() > 0) {
                            return new Token(TokenKind.UserDefinedStringLiteral, value + userDefinedSuffix);
                        }
                        return new Token(TokenKind.StringLiteral, value);

                    } else if (isAMatch(pos, "u8\"")) {
                        value = scanEncodingPrefix() + scanString();
                        userDefinedSuffix = scanIdentifier();

                        if(userDefinedSuffix.length() > 0) {
                            return new Token(TokenKind.UserDefinedStringLiteral, value + userDefinedSuffix);
                        }

                        return new Token(TokenKind.StringLiteral, value);
                    } else if(isAMatch(pos, new char[]{ 'u', 'U', 'L' })) {
                        if (isAMatch(pos + 1, "'")) {
                            value = scanCharPrefix() + scanCharSequence();
                            userDefinedSuffix = scanIdentifier();

                            if (userDefinedSuffix.length() > 0) {
                                return new Token(TokenKind.UserDefinedCharacterLiteral, value + userDefinedSuffix);
                            }

                            return new Token(TokenKind.CharacterLiteral, value);
                        } else if (isAMatch(pos + 1, "\"")) {
                            value = scanEncodingPrefix() + scanString();
                            userDefinedSuffix = scanIdentifier();

                            if(userDefinedSuffix.length() > 0) {
                                return new Token(TokenKind.UserDefinedStringLiteral, value + userDefinedSuffix);
                            }

                            return new Token(TokenKind.StringLiteral, value);
                        } else if (isAMatch(pos + 1, "R") && isAMatch(pos + 2, "\"")) {
                            value = scanEncodingPrefix() + scanRawString();
                            userDefinedSuffix = scanIdentifier();

                            if(userDefinedSuffix.length() > 0) {
                                return new Token(TokenKind.UserDefinedStringLiteral, value + userDefinedSuffix);
                            }

                            return new Token(TokenKind.StringLiteral, value);
                        }
                    } else if(isAMatch(pos, "R") && isAMatch(pos +1, "\"")) {
                        value = scanRawString();
                        userDefinedSuffix = scanIdentifier();

                        if(userDefinedSuffix.length() > 0) {
                            return new Token(TokenKind.UserDefinedStringLiteral, value + userDefinedSuffix);
                        }

                        return new Token(TokenKind.StringLiteral, value);
                    }

                    if(isNonDigit(ch)) {

                        String identifierOrKeyword = scanIdentifier();

                        if(keywordMap.containsKey(identifierOrKeyword)) {
                            return new Token(keywordMap.get(identifierOrKeyword), identifierOrKeyword);
                        }

                        return new Token(TokenKind.Identifier, identifierOrKeyword);
                    }

                    error("Cannot parse token");
            }
        }
    }

    private String scanFloatingLiteral() {
        if(!isFloatingLiteral()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String firstSequence = scanDecimalDigits();
        sb.append(firstSequence);
        if(isAMatch(pos, "e") || isAMatch(pos, "E")) {
            if(firstSequence.equals("")) {
                error("Floating-literal is not valid");
            }

            sb.append(scanExponentPart());
        } else if(isAMatch(pos, ".")) {
            sb.append(".");
            pos++;

            String secondSequence = scanDecimalDigits();
            if(secondSequence.equals("") && firstSequence.equals("")) {
                error("Floating-literal is not valid");
            }

            sb.append(secondSequence);

            sb.append(scanExponentPart());
        } else {
            //Should never reach here...
            error("Floating-literal is not valid");
        }

        sb.append(scanFloatingSuffix());
        return sb.toString();
    }

    private String scanExponentPart() {
        if(pos >= end) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        char ch = text.charAt(pos);
        if(ch == 'e' || ch == 'E') {
            sb.append(ch);
            pos++;
        } else {
            return "";
        }

        ch = text.charAt(pos);
        if(ch == '+' || ch == '-' ) {
            sb.append(ch);
            pos++;
        }

        String digitSequence = scanDecimalDigits();
        if(digitSequence.equals("")) {
            error("Invalid exponent-part for floating-literal");
        }

        sb.append(digitSequence);
        return sb.toString();
    }

    private String scanFloatingSuffix() {
        if(isAMatch(pos, new char[]{ 'l', 'L', 'f', 'F' })) {
            pos++;
            return String.valueOf(text.charAt(pos - 1));
        }
        return "";
    }

    private boolean isFloatingLiteral() {
        int lookaheadPos = pos;
        while (lookaheadPos < end) {
            char ch = text.charAt(lookaheadPos);

            if (isWhiteSpace(ch)) {
                return false;
            } else if (ch == 'e' || ch == 'E' || ch == '.') {
                return true;
            }

            lookaheadPos++;
        }
        return false;
    }

    private boolean isNonDigit(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    private boolean isWhiteSpace(char ch) {
       return ch == '\t' || ch == '\n' || ch == '\r' || ch == '\f' || ch == ' ';
    }

    private String scanIdentifier() {
        StringBuilder sb = new StringBuilder();

        boolean isFirstCharacter = true;
        while(pos < end) {
            char ch = text.charAt(pos);

            if(isNonDigit(ch)) {
                isFirstCharacter = false;
                sb.append(ch);
                pos++;
            } else if(ch == '\\') {
                isFirstCharacter = false;
               sb.append(scanUniversalCharacterName());
            } else if(isDecimalDigit(ch)) {
                if(isFirstCharacter) {
                    error("Invalid first character for identifier");
                }
                sb.append(ch);
                pos++;
            } else {
                break;
            }
        }

        if(isFirstCharacter) {
            return "";
        }

        return sb.toString();
    }

    private String scanCharPrefix() {
        if(isAMatch(pos, new char[] { 'u', 'U', 'L' })) {
            pos++;
            return String.valueOf(text.charAt(pos - 1));
        }
        return "";
    }

    private String scanCharSequence() {
        if(!isAMatch(pos, "'")) {
           return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("'");
        pos++;

        boolean hasClosingQuote = false;

        while(pos < end) {
            if(isAMatch(pos, "\\u") || isAMatch(pos, "\\U")) {
                   sb.append(scanUniversalCharacterName());
            } else if(isAMatch(pos, "\\x")) {
                sb.append(text.substring(pos, pos + 2));
                pos += 2;
                sb.append(scanHexDigits(1, true));
            } else if(isAMatch(pos, "\\") && pos + 1 < end && isOctalDigit(text.charAt(pos + 1))) {
                pos++;
                sb.append("\\");
                sb.append(scanOctalDigits());
            } else if(isAMatch(pos, "\\")) {
                sb.append(scanSimpleEscapeSequence());
            } else if (isAMatch(pos, "'")) {
                pos++;
                sb.append("'");
                hasClosingQuote = true;
                break;
            } else if(isAMatch(pos, "\n")) {
                error("Unexpected newline in character-literal");
            } else {
                sb.append(text.charAt(pos));
                pos++;
            }
        }

        if(!hasClosingQuote) {
            error("Character literal is missing closing quote");
            return "";
        } else if(sb.length() == 2) {
            error("Character literal is empty");
        }

        return sb.toString();
    }

    private String scanEncodingPrefix() {
        if(isAMatch(pos, "u8")) {
            pos += 2;
            return text.substring(pos - 2, pos);
        } else if(isAMatch(pos, new char[] { 'u', 'U', 'L'})) {
            pos++;
            return String.valueOf(text.charAt(pos - 1));
        }
        return "";
    }

    private String scanString() {
        if(!isAMatch(pos, "\"")) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        pos++;

        boolean hasClosingQuote = false;

        while(pos < end) {
            if(isAMatch(pos, "\\u") || isAMatch(pos, "\\U")) {
                sb.append(scanUniversalCharacterName());
            } else if(isAMatch(pos, "\\")) {
                sb.append(scanSimpleEscapeSequence());
            } else if(isAMatch(pos, "\n")) {
                error("Unexpected new-line character");
            } else if(isAMatch(pos, "\"")) {
                pos++;
                sb.append("\"");
                hasClosingQuote = true;
                break;
            } else {
                sb.append(text.charAt(pos));
                pos++;
            }
        }

        if(!hasClosingQuote) {
            error("String-literal is missing closing quote");
            return "";
        }

        return sb.toString();
    }

    private String scanSimpleEscapeSequence() {
        if(!isAMatch(pos, "\\")) {
            return "";
        }
        pos++;

        if(isAMatch(pos, new char[]{ '"', '\'', '?', '\\', 'a', 'b', 'f', 'n', 'r', 't', 'v'})) {
            pos++;
            return "\\" + text.charAt(pos - 1);
        }

        error("Invalid simple-escape-sequence");
        return "";
    }

    private String scanUniversalCharacterName() {
        StringBuilder sb = new StringBuilder();

        if(isAMatch(pos, "\\U")) {
            sb.append("\\U");
            pos += 2;

            sb.append(scanHexDigits(4, false));
            sb.append(scanHexDigits(4, false));
        } else if(isAMatch(pos, "\\u")) {
            sb.append("\\u");
            pos += 2;

            sb.append(scanHexDigits(4, false));
        }

        return sb.toString();
    }

    private String scanRawString() {
        if (!isAMatch(pos, "R\"")) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("R\"");
        pos += 2;

        String firstSequence = scanDCharSequence();
        sb.append(firstSequence);

        if(!isAMatch(pos, "(")) {
            error("Raw string is missing open paren");
        }
        sb.append("(");
        pos++;

        sb.append(scanRCharSequence());

        if(!isAMatch(pos, ")")) {
            error("Raw string is missing close paren");
        }
        sb.append(")");
        pos++;

        if(isAMatch(pos, "\"") && firstSequence.equals("")) {
            sb.append('"');
            pos++;
            return sb.toString();
        }

        if(!isAMatch(pos, firstSequence)) {
            error("Raw string D-Char sequences are not equivalent");
        }
        pos += firstSequence.length();
        sb.append(firstSequence);

        if(!isAMatch(pos, "\"")) {
            error("String-literal is missing closing quote");
        }
        sb.append("\"");
        pos++;

        return sb.toString();
    }

    private String scanRCharSequence() {
        StringBuilder sb = new StringBuilder();
        while (pos < end) {
            char ch = text.charAt(pos);
            if( ch == ')') {
                break;
            }

            sb.append(ch);
            pos++;
        }

        return sb.toString();
    }

    private String scanDCharSequence() {
        StringBuilder sb = new StringBuilder();
        while (pos < end) {
            char ch = text.charAt(pos);

            if (isWhiteSpace(ch) || ch == '\\' || ch == ')') {
                error("Invalid D-Char Sequence for raw string");
            } else if( ch == '(') {
                break;
            }

            sb.append(ch);
            pos++;
        }

        return sb.toString();
    }

    private String scanDecimalDigits() {
        StringBuilder sb = new StringBuilder();
        while(pos < end) {
            char ch = text.charAt(pos);
            if(!isDecimalDigit(ch)) {
                break;
            }
            sb.append(ch);
            pos++;
        }
        return sb.toString();
    }

    private boolean isDecimalDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private String scanOctalDigits() {
        StringBuilder sb = new StringBuilder();
        while(pos < end) {
            char ch = text.charAt(pos);
            if(!isOctalDigit(ch)) {
                break;
            }
            sb.append(ch);
            pos++;
        }
        return sb.toString();
    }

    private boolean isHexDigit(char ch) {
        return (ch >= '0' && ch <= '9')
                || (ch >= 'A' && ch <= 'F')
                || (ch >= 'a' && ch <= 'f');

    }

    private boolean isOctalDigit(char ch) {
        return ch >= '0' && ch <= '7';
    }

    private String scanHexDigits(int minCount, boolean scanAsManyAsPossible) {
        StringBuilder sb = new StringBuilder();
        int startPos = pos;

        while(pos < end && (sb.length() < minCount || scanAsManyAsPossible)) {
            char ch = text.charAt(pos);

            if(!isHexDigit(ch)) {
                break;
            }

            sb.append(ch);
            pos++;
        }

        if(sb.length() < minCount) {
            error("Expected hexadecimal digit");
            pos = startPos;
            return "";
        }

        return sb.toString();
    }

    private String scanIntegerSuffix() {
        if(pos >= end) {
            return "";
        }

        int startPos = pos;

        if(isAMatch(pos, new char[] { 'u', 'U'})) {
            pos++;
            if(isAMatch(pos, "ll") || isAMatch(pos, "LL")) {
                pos += 2;
            } else if(isAMatch(pos, new char[] { 'l', 'L'})) {
                pos++;
            }

        } else if(isAMatch(pos, "ll") || isAMatch(pos, "LL")) {
            pos += 2;
            if(isAMatch(pos, new char[] { 'u', 'U'})) {
                pos++;
            }

        } else if(isAMatch(pos, new char[]{ 'l', 'L'})) {
            pos++;
            if(isAMatch(pos, new char[] { 'u', 'U'})) {
                pos++;
            }
        }
        return text.substring(startPos, pos);
    }

    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();

        Token token = scan();
        tokens.add(token);

        while(token.kind != TokenKind.EndOfFile) {
            token = scan();
            tokens.add(token);
        }

        return tokens;
    }
}