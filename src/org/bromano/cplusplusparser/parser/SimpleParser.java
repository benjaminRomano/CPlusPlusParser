package org.bromano.cplusplusparser.parser;

import org.bromano.cplusplusparser.scanner.Token;
import org.bromano.cplusplusparser.scanner.TokenKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SimpleParser implements Parser {
    protected List<Token> tokens;
    protected Stack<String> tree;

    protected Stack<Integer> savedTreePos;

    protected int pos;
    protected int end;
    protected Stack<Integer> savedPos;

    public SimpleParser(List<Token> tokens) {
        this.setTokens(tokens);
    }

    public SimpleParser() {
        this.tokens = new ArrayList();
    }

    public void setTokens(List<Token> tokens) {
        this.savedPos = new Stack<Integer>();
        this.tokens = this.filterEOFToken(tokens);
        this.end = this.tokens.size();
        this.pos = 0;
    }

    protected List<Token> filterEOFToken(List<Token> tokens) {
        List<Token> filteredTokens = new ArrayList<>();

        for(Token token : tokens) {
            if(token.kind != TokenKind.EndOfFile) {
                filteredTokens.add(token);
            }

        }

        return filteredTokens;
    }

    protected void savePositions() {
        this.savePos();
        this.saveTreePos();
    }

    protected void unsavePositions() {
        this.unsavePos();
        this.unsaveTreePos();
    }

    protected void resetPositions() {
        this.resetPos();
        this.resetTreePos();
    }

    protected void savePos() {
        this.savedPos.push(this.pos);
    }

    protected void unsavePos() {
        this.savedPos.pop();
    }

    protected void resetPos() {
        this.pos = this.savedPos.pop();
    }

    protected void saveTreePos() {
        this.savedTreePos.push(this.tree.size());
    }

    protected void unsaveTreePos() {
        this.savedTreePos.pop();
    }

    protected void resetTreePos() {
        int treeSize = this.savedPos.pop();
        while(this.tree.size() > treeSize) {
            this.tree.pop();
        }
    }

    protected boolean check(TokenKind kind) {
        return this.pos < this.end && this.tokens.get(this.pos ).kind == kind;
    }

    protected boolean check(TokenKind[] kinds, int lookahead) {
        if(this.pos + lookahead >= this.end) {
            return false;
        }

        TokenKind currTokenKind = this.tokens.get(pos + lookahead).kind;
        for(TokenKind kind : kinds) {
            if(currTokenKind == kind) {
                return true;
            }
        }

        return false;
    }

    protected boolean check(TokenKind[] kinds) {
        if(this.pos >= this.end) {
            return false;
        }

        TokenKind currTokenKind = this.tokens.get(pos).kind;
        for(TokenKind kind : kinds) {
            if(currTokenKind == kind) {
                return true;
            }
        }

        return false;
    }

    protected boolean check(TokenKind kind, int lookahead) {
        return this.pos + lookahead < this.end && this.tokens.get(this.pos + lookahead).kind == kind;
    }

    protected String createNode(int depth, String text) {
        StringBuilder node = new StringBuilder();
        for(int i = 0; i < depth; i++) {
            node.append("\t");
        }
        node.append(text);
        return node.toString();

    }

    protected void addTreeNode(int depth, Token token) {
        this.tree.push(this.createNode(depth, token.kind.name()));

    }

    protected void addTreeNode(int depth, NodeType node) {
        this.tree.push(this.createNode(depth, node.name()));

    }

    protected Token match(int depth) throws ParserException {
        if(this.pos >= this.end) {
            throw new ParserException("Expected: token");
        }

        Token token = this.tokens.get(this.pos);

        this.addTreeNode(depth, token);

        this.pos++;
        return token;

    }

    protected Token match(int depth, TokenKind tokenKind) throws ParserException {
        if(this.pos >= this.end || this.tokens.get(this.pos).kind != tokenKind) {
            throw new ParserException("Expected: " + tokenKind.name());
        }

        Token token = this.tokens.get(this.pos);
        this.addTreeNode(depth, token);
        this.pos++;
        return token;
    }
    protected Token match(int depth, TokenKind[] tokenKinds) throws ParserException {
        StringBuilder errorMessageBuilder = new StringBuilder();
        errorMessageBuilder.append("Expected one of following: ");

        if(tokenKinds.length >= 1) {
            errorMessageBuilder.append(tokenKinds[0]);
        }

        for(int i = 1; i < tokenKinds.length; i++ ) {
            errorMessageBuilder.append(", ");
            errorMessageBuilder.append(tokenKinds[i]);

        }

        if(this.pos >= this.end) {
            throw new ParserException(errorMessageBuilder.toString());
        }

        Token token = this.tokens.get(this.pos);
        for(TokenKind tokenKind : tokenKinds) {
            if(token.kind == tokenKind) {
                this.pos++;
                this.addTreeNode(depth, token);
                return token;
            }
        }

        throw new ParserException(errorMessageBuilder.toString());
    }

    public Stack<String> parse() throws ParserException {
        parseTranslationUnit(0);

        return this.tree;
    }


    @FunctionalInterface
    public interface ParseFunction {
        void execute() throws ParserException;
    }

    protected boolean tryParse(ParseFunction func) {
        this.savePositions();
        try {
            func.execute();
            this.unsavePositions();
            return true;
        } catch (ParserException exception) {
            this.resetPositions();
        }

        return false;
    }

    protected boolean tryParse(ParseFunction[] funcs) {
        for(ParseFunction func : funcs) {
            if(tryParse(func)) {
                return true;
            }
        }
        return false;
    }

    protected void parseTranslationUnit(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TRANSLATION_UNIT);

        if(this.checkDeclarationSequence()) {
            parseDeclarationSequence(depth + 1);
        }

    }

    protected void parseDeclarationSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.DECLARATION_SEQUENCE);

        if(this.checkDeclarationSequence()) {
            boolean success = tryParse(() -> {
                parseDeclaration(depth + 1);
                parseDeclarationSequence(depth + 1);
            });

            if(success) {
                return;
            }
        }

        parseDeclaration(depth + 1);
    }


    protected void parseDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.DECLARATION);
        parseEmptyDeclaration(depth + 1);
    }

    protected void parseEmptyDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EMPTY_DECLARATION);
        this.match(depth + 1, TokenKind.Semicolon);
    }


    protected void parseFunctionDefinition(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.FUNCTION_DEFINITION);

        ParseFunction tryDeclarator = () -> {
            parseDeclarator(depth + 1);

            if(this.check(TokenKind.Equals)) {
                this.match(depth + 1, TokenKind.Equals);

                if (this.check(TokenKind.DefaultKeyword)) {
                    this.match(depth + 1, TokenKind.DefaultKeyword);
                } else {
                    this.match(depth + 1, TokenKind.DeleteKeyword);
                }

                this.match(depth + 1,TokenKind.Semicolon);
                return;
            }

            parseFunctionBody(depth + 1);
        };

        ParseFunction tryDeclarationSpecifierSequence = () -> {
            parseDeclarationSpecifierSequence(depth + 1);
            tryDeclarator.execute();
        };

        ParseFunction tryAttributeSpecifierSequence = () -> {
            parseAttributeSpecifierSequence(depth + 1);
            if(tryParse(tryDeclarationSpecifierSequence)) {
                return;
            }

            tryDeclarator.execute();
        };

        if(tryParse(tryAttributeSpecifierSequence)) return;
        if(tryParse(tryDeclarationSpecifierSequence)) return;
        tryDeclarator.execute();
    }

    protected void parseAttributeSpecifierSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTIBUTE_SPECIFIER_SEQUENCE);

        if(this.checkAttributeSpecifierSequence()) {
            boolean success = tryParse(() -> {
                parseAttributeSpecifier(depth + 1);
                parseAttributeSpecifierSequence(depth + 1);
            });

            if(success) {
                return;
            }
        }

        parseAttributeSpecifier(depth + 1);
    }

    protected void parseAttributeSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_SPECIFIER);

        if(this.check(TokenKind.OpenBracket)) {
            this.match(depth + 1 ,TokenKind.OpenBracket);
            this.match(depth + 1, TokenKind.OpenBracket);
            parseAttributeList();
            this.match(depth + 1, TokenKind.CloseBracket);
            this.match(depth + 1, TokenKind.CloseBracket);
            return;
        }

        parseAlignmentSpecifier(depth + 1);
    }

    protected void parseAttributeList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_LIST);

        if(this.checkAttribute()) {
            parseAttribute(depth + 1);

            if(this.check(TokenKind.DotDotDot)) {
                this.match(depth + 1, TokenKind.DotDotDot);

                if(this.check(TokenKind.Comma)) {
                    this.match(depth + 1, TokenKind.Comma);
                    parseAttributeList(depth + 1);
                }
            } else if(this.check(TokenKind.Comma)) {
                this.match(depth + 1, TokenKind.Comma);
                parseAttributeList(depth + 1);
                return;
            }
        }

        if(this.check(TokenKind.Comma)) {
            this.match(depth + 1, TokenKind.Comma);
            parseAttributeList(depth + 1);
        }
    }


    protected void parseAttribute(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE);

        parseAttributeToken(depth + 1);

        if(this.checkAttributeArgumentClause()) {
            tryParse(() -> parseAttributeArgumentClause(depth + 1));
        }
    }

    protected void parseAttributeToken(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_TOKEN);

        boolean success = tryParse(() -> parseAttributeScopedToken(depth + 1));

        if(success) {
            return;
        }

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseAttributeScopedToken(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_SCOPED_TOKEN);

        parseAttributeNamespace(depth + 1);

        this.match(depth + 1, TokenKind.ColonColon);
        this.match(depth + 1 ,TokenKind.Identifier);
    }

    protected void parseAttributeNamespace(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_NAMESPACE);

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseAttributeArgumentClause(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_ARGUMENT_CLAUSE);

        this.match(depth + 1, TokenKind.OpenParen);
        parseBalancedTokenSequence(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);
    }

    protected void parseBalancedTokenSequence(int depth) {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_ARGUMENT_CLAUSE);

        boolean success = tryParse(() -> {
            parseBalancedToken(depth + 1);
            parseBalancedTokenSequence(depth + 1);
        });

        if(success) {
            return;
        }

        this.parseBalancedToken();
    }

    protected void parseBalancedToken(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.BALANCED_TOKEN);

        if(this.check(new TokenKind[]{
                TokenKind.OpenBracket,
                TokenKind.OpenBrace,
                TokenKind.OpenParen
        })) {
            Token token = this.match(depth + 1, new TokenKind[]{
                    TokenKind.OpenBracket,
                    TokenKind.OpenBrace,
                    TokenKind.OpenParen
            });

            parseBalancedTokenSequence(depth + 1);

            if (token.kind == TokenKind.OpenBracket) {
                this.match(depth + 1, TokenKind.CloseBracket);
            } else if (token.kind == TokenKind.OpenBrace) {
                this.match(depth + 1, TokenKind.OpenBrace);
            } else {
                this.match(depth + 1 ,TokenKind.OpenParen);
            }

            return;
        }

        this.match(depth + 1);
    }

    protected void parseAlignmentSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ALIGNMENT_SPECIFIER);

        this.match(depth + 1, TokenKind.AlignasKeyword);
        this.match(depth + 1, TokenKind.OpenParen);

        if(this.checkTypeId()) {
            tryParse(() -> parseTypeId());
        } else {
            parseAlignnmentExpression();
        }

        if(this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
        }

        this.match(depth + 1, TokenKind.CloseParen);
    }

    protected void parseTypeId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPE_ID);

        parseTypeSpecifierSequence(depth + 1);

        if(this.checkAbstractDeclarator()) {
            tryParse(() -> parseAbstractDeclarator(depth + 1));
        }
    }

    protected void parseAbstractDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ABSTRACT_DECLARATOR);

        if(this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
            return;
        }

        if(this.checkNoPtrAbstractDeclarator()) {
            tryParse(() -> {
                parseNoPtrAbstractDeclarator(depth + 1);
                parseParametersAndQualiifiers(depth + 1);
                parseTrailingReturnType(depth + 1);
            });
        }

        if(this.checkPtrAbstractDeclarator()) {
            parsePtrAbstractDeclarator();
            return;
        }

        parseParametersAndQualifiers(depth + 1);
        parseTrailingReturnType(depth + 1);
    }

    protected void parsePtrAbstractDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PTR_ABSTRACT_DECLARATOR);

        if(this.checkPtrOperator()) {
            parsePtrOperator(depth + 1);

            tryParse(() -> {
                parsePtrAbstractDeclarator(depth + 1);
            });

            return;
        }

        parseNoPtrAbstractDeclarator(depth + 1);
    }

    protected void parsePtrOperator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PTR_OPERATOR);

        if(this.check(new TokenKind[] {
                TokenKind.Ampersand,
                TokenKind.AmpersandAmpersand
        })) {
            this.match(depth + 1, new TokenKind[]{
                    TokenKind.Ampersand,
                    TokenKind.AmpersandAmpersand
            });

            tryParse(() -> parseAttributeSpecifierSequence(depth + 1));

            return;
        }

        if(this.check(TokenKind.Asterisk)) {
            this.match(depth + 1, TokenKind.Asterisk);

            tryParse(() -> {
                parseAttributeSpecifierSequence(depth + 1);
                tryParse(() -> parseCvQualifier(depth + 1));
            });

            return;
        }

        if(this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        parseNestedNameSpecifier();
        this.match(depth + 1, TokenKind.Asterisk);

        tryParse(() -> {
            parseAttributeSpecifierSequence(depth + 1);
            tryParse(() -> parseCvQualifier(depth + 1));
        });
    }

    protected void parseNestedNameSpecifierHelper(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NESTED_NAME_SPECIFIER);

        if(this.check(TokenKind.TemplateKeyword)) {
            this.match(depth + 1, TokenKind.TemplateKeyword);
            parseSimpleTemplateId(depth + 1);
            this.match(depth + 1, TokenKind.ColonColon);
        }

        if(this.checkSimpleTemplateId()) {
            boolean success = tryParse(() -> {
                parseSimpleTemplateId(depth + 1);
                this.match(depth + 1, TokenKind.ColonColon);
            });

            if (success) return;
        }

        this.match(depth + 1, TokenKind.Identifier);
        tryParse(() -> parseNestedNameSpecifierHelper(depth + 1));
    }

    protected void parseNestedNameSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NESTED_NAME_SPECIFIER);

        if(this.checkDecltypeSpecifier()) {
            parseDecltypeSpecifier(depth + 1);
            match(depth + 1, TokenKind.ColonColon);
            tryParse(() -> parseNestedNameSpecifierHelper(depth + 1));
            return;
        }

        boolean success = tryParse(() -> {
            parseTypeName(depth + 1);
            match(depth + 1, TokenKind.ColonColon);
            tryParse(() -> parseNestedNameSpecifierHelper(depth + 1));
        });

        if(success) return;

        parseNamespaceName(depth + 1);
        match(depth + 1, TokenKind.ColonColon);
        tryParse(() -> parseNestedNameSpecifierHelper(depth + 1));
    }

    protected void parseNoPtrAbstractDeclaratorHelper(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NO_PTR_ABSTRACT_DECLARATOR);

        if(this.check(TokenKind.OpenParen)) {
            parseParametersAndQualifiers();
        } else {
            this.match(depth + 1, TokenKind.OpenBracket);
            parseConstantExpression();
            this.match(depth + 1, TokenKind.CloseBracket);
            tryParse(() -> parseAttributeSpecifierSequence(depth + 1));
        }

        tryParse(() -> parseNoPtrAbstractDeclaratorHelper(depth + 1));
    }

    protected void parseNoPtrAbstractDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NO_PTR_ABSTRACT_DECLARATOR);

        this.match(depth + 1, TokenKind.OpenParen);
        parsePtrAbstractDeclarator(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);
        tryParse(() -> parseNoPtrAbstractDeclaratorHelper(depth + 1));
    }

    protected void parseTypeSpecifierSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPE_SPECIFIER_SEQUENCE);

        parseTypeSpecifier(depth + 1);

        boolean success = tryParse(() -> {
            parseAttributeSpecifierSequence(depth + 1);
            parseTypeSpecifierSequence(depth + 1);
        });

        if (success) return;

        tryParse(() -> parseTypeSpecifierSequence(depth + 1));
    }

    protected void parseTypeSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPE_SPECIFIER);

        if(tryParse(() -> parseTrailingTypeSpecifier(depth + 1))) return;
        if(tryParse(() -> parseClassSpecifier(depth + 1))) return;

        parseEnumSpecifier(depth + 1);
    }

    protected void parseTrailingTypeSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TRAILING_TYPE_SPECIFIER;

        if(tryParse(() -> parseSimpleTypeSpecifier(depth + 1))) return;
        if(tryParse(() -> parseElaboratedTypeSpecifier(depth + 1))) return;
        if(tryParse(() -> parseTypenameSpecifier(depth + 1))) return;

        parseCvQualifier(depth + 1);
    }

    protected void parseSimpleTypeSpecifier(int depth) throws ParserException {
        if(this.check(new TokenKind[] {
                TokenKind.CharKeyword,
                TokenKind.Char16TKeyword,
                TokenKind.Char32TKeyword,
                TokenKind.WcharTKeyword,
                TokenKind.BoolKeyword,
                TokenKind.ShortKeyword,
                TokenKind.IntKeyword,
                TokenKind.LongKeyword,
                TokenKind.SignedKeyword,
                TokenKind.UnsignedKeyword,
                TokenKind.FloatKeyword,
                TokenKind.DoubleKeyword,
                TokenKind.VoidKeyword,
                TokenKind.AutoKeyword
        })) {
            this.match(depth + 1, new TokenKind[] {
                    TokenKind.CharKeyword,
                    TokenKind.Char16TKeyword,
                    TokenKind.Char32TKeyword,
                    TokenKind.WcharTKeyword,
                    TokenKind.BoolKeyword,
                    TokenKind.ShortKeyword,
                    TokenKind.IntKeyword,
                    TokenKind.LongKeyword,
                    TokenKind.SignedKeyword,
                    TokenKind.UnsignedKeyword,
                    TokenKind.FloatKeyword,
                    TokenKind.DoubleKeyword,
                    TokenKind.VoidKeyword,
                    TokenKind.AutoKeyword
            });

            return;
        } else if(this.checkDeclTypeSpecifier()) {
            parseDeclTypeSpecifier(depth + 1);
            return;
        } else if(this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        tryParse(() -> {
            parseNestedNameSpecifier(depth + 1);

            if(this.check(TokenKind.TemplateKeyword)) {
               this.match(depth + 1, TokenKind.TemplateKeyword);
                parseSimpleTemplateId(depth + 1);
                return;
            }

            parseTypeName(depth + 1);
        });

        parseTypeName(depth + 1);
    }

    protected void parseTypeName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPE_NAME);

        if(tryParse(() -> parseClassName(depth + 1))) return;
        if(tryParse(() -> parseEnumName(depth + 1))) return;
        if(tryParse(() -> parseTypedefName(depth + 1))) return;

        parseSimpleTemplateId(depth + 1);
    }

    protected void parseClassName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPE_NAME);

        if(this.checkSimpleTemplateId()) {
            tryParse(() -> parseSimpleTemplateId(depth + 1));
        }

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseEnumName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ENUM_NAME);

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseTypedefName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPEDEF_NAME);

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseElaboratedTypeSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ELABORATED_TYPE_SPECIFIER);

        ParseFunction tryNestedNameSpecifierIdentifier = () -> {
            if(this.check(TokenKind.ColonColon)) {
                this.match(depth + 1, TokenKind.ColonColon);
            }

            boolean success = tryParse(() -> {
                parseNestedNameSpecifier(depth + 1);
                this.match(depth + 1, TokenKind.Identifier);
            });

            if(success) return;

            this.match(depth + 1, TokenKind.Identifier);
        };

        //Enum
        if(this.check(TokenKind.EnumKeyword)) {
            this.match(depth + 1, TokenKind.EnumKeyword);
            tryNestedNameSpecifierIdentifier.execute();
        }

        //ClassKey
        parseClassKey(depth + 1);

        tryParse(() -> {
            parseAttributeSpecifierSequence(depth + 1);
            tryNestedNameSpecifierIdentifier.execute();
        });


        if(this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        if(this.checkNestedNameSpecifier()) {
            parseNestedNameSpecifier(depth + 1);
        }

        if(this.check(TokenKind.TemplateKeyword)) {
            this.match(depth + 1, TokenKind.TemplateKeyword);
        }

        parseSimpleTemplateId(depth + 1);
    }

    protected void parseClassKey(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CLASS_KEY);

        this.match(depth + 1, new TokenKind[] {
                TokenKind.ClassKeyword,
                TokenKind.StructKeyword,
                TokenKind.UnionKeyword
        });
    }

    protected void parseCvQualifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CV_QUALIFIER);

        this.match(depth + 1, new TokenKind[] {
                TokenKind.ConstKeyword,
                TokenKind.VolatileKeyword
        });
    }

    protected void parseTypenameSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPENAME_SPECIFIER);

        this.match(depth + 1, TokenKind.TypenameKeyword);
        if(this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        parseNestedNameSpecifier(depth + 1);

        if(this.check(TokenKind.Identifier)) {
            this.match(depth + 1, TokenKind.Identifier);
            return;
        }

        if(this.check(TokenKind.TemplateKeyword)) {
            this.match(depth + 1, TokenKind.TemplateKeyword);
        }

        parseSimpleTemplateId(depth + 1);
    }

    protected void parseSimpleTemplateId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.SIMPLE_TEMPLATE_ID);

        parseTemplateName();

        this.match(depth + 1, TokenKind.LessThan);

        if(this.checkTemplateArgumentList()) {
            tryParse(() -> parseTemplateArgumentList());
        }

        this.match(depth + 1, TokenKind.GreaterThan);
    }

    protected void parseTemplateName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TEMPLATE_NAME);

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseTemplateArgumentList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TEMPLATE_ARGUMENT_LIST);

       parseTemplateArgument();

        if(this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
        }

        if(this.check(TokenKind.Comma)) {
            parseTemplateArgumentList(depth + 1);
        }

        tryParse(() -> parseTemplateArgumentList(depth + 1));
    }

    protected void parseTemplateArgument(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TEMPLATE_ARGUMENT);

        if(this.checkConstantExpression()) {
            parseConstantExpression(depth + 1);
        }

        parseTypeId(depth + 1);
    }

    protected void parseExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXPRESSION);

        parseAssignmentExpression(depth + 1);

        if(this.check(TokenKind.Comma)) {
            this.match(depth + 1, TokenKind.Comma);
            parseExpression(depth + 1);
        }
    }

    protected void parseAssignmentExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ASSIGNMENT_EXPRESSION);

        if(this.checkThrowExpression()) {
           parseThrowExpression();
            return;
        }

        if(this.checkLogicalOrExpression()) {
            boolean success = tryParse(() -> {
                parseLogicalOrExpression(depth + 1);
                parseAssignmentOperator(depth + 1);
                parseInitializerClause(depth + 1);
            });
            if (success) return;
        }

        parseConditionalExpression();
    }

    protected void parseAssignmentOperator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ASSIGNMENT_OPERATOR);

        this.match(depth + 1, new TokenKind[] {
                TokenKind.Equals,
                TokenKind.AsteriskEquals,
                TokenKind.SlashEquals,
                TokenKind.PercentEquals,
                TokenKind.PlusEquals,
                TokenKind.MinusEquals,
                TokenKind.GreaterThanGreaterThanEquals,
                TokenKind.LessThanLessThanEquals,
                TokenKind.AmpersandEquals,
                TokenKind.CaretEquals,
                TokenKind.BarEquals
        });
    }

    protected void parseThrowExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ASSIGNMENT_OPERATOR);

        this.match(depth + 1, TokenKind.ThrowKeyword);

        if(this.checkAssignmentExpression()) {
            tryParse(() -> parseAssignmentExpression(depth + 1));
        }
    }

    protected void parseConstantExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CONSTANT_EXPRESSION);

        parseConditionalExpression(depth + 1);
    }

    protected void parseConditionalExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CONDITIONAL_EXPRESSION);

        parseLogicalOrExpression(depth + 1);

        if(!this.check(TokenKind.Question)) {
            return;
        }

        this.match(depth + 1, TokenKind.Question);
        parseExpression(depth + 1);
        this.match(depth + 1, TokenKind.Colon);
        parseAssignmentExpression(depth + 1);
    }

    protected void parseLogicalOrExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CONDITIONAL_EXPRESSION);

        parseLogicalAndExpression(depth + 1);

        if(!this.check(TokenKind.BarBar)) {
            return;
        }

        this.match(depth + 1, TokenKind.BarBar);
        parseLogicalOrExpression(depth + 1);
    }

    protected void parseLogicalAndExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.LOGICAL_AND_EXPRESSION;

        parseInclusiveOrExpression(depth + 1);

        if(!this.check(TokenKind.AmpersandAmpersand)) {
            return;
        }

        this.match(depth + 1, TokenKind.AmpersandAmpersand);
        parseLogicalAndExpression(depth + 1);
    }

    protected void parseInclusiveOrExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.INCLUSIVE_OR_EXPRESSION;

        parseExclusiveOrExpression(depth + 1);

        if(!this.check(TokenKind.Bar)) {
            return;
        }

        this.match(depth + 1, TokenKind.Bar);
        parseInclusiveOrExpression(depth + 1);
    }

    protected void parseExclusiveOrExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXCLUSIVE_OR_EXPRESSION;

        parseAndExpression(depth + 1);

        if(!this.check(TokenKind.Caret)) {
            return;
        }

        this.match(depth + 1, TokenKind.Caret);
        parseExclusiveOrExpression(depth + 1);
    }

    protected void parseAndExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.AND_EXPRESSION);

        parseEqualityExpression(depth + 1);

        if(!this.check(TokenKind.Ampersand)) {
            return;
        }

        this.match(depth + 1, TokenKind.Ampersand);
        parseAndExpression(depth + 1);
    }

    protected void parseEqualityExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EQUALITY_EXPRESSION);

        parseRelationalExpression(depth + 1);

        if(!this.check(new TokenKind[] {
                TokenKind.EqualsEquals,
                TokenKind.ExclamationEquals
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[] {
                TokenKind.EqualsEquals,
                TokenKind.ExclamationEquals
        });

        parseEqualityExpression(depth + 1);
    }

    protected void parseRelationalExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.RELATIONAL_EXPRESSION);

        parseShiftExpression(depth + 1);

        if(!this.check(new TokenKind[] {
                TokenKind.LessThan,
                TokenKind.GreaterThan,
                TokenKind.LessThanEquals,
                TokenKind.GreaterThanEquals
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[] {
                TokenKind.LessThan,
                TokenKind.GreaterThan,
                TokenKind.LessThanEquals,
                TokenKind.GreaterThanEquals
        });

        parseRelationalExpression(depth + 1);
    }

    protected void parseShiftExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.SHIFT_EXPRESSION);

        parseAdditiveExpression(depth + 1);

        if(!this.check(new TokenKind[] {
                TokenKind.LessThanLessThan,
                TokenKind.GreaterThanGreaterThan,
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[] {
                TokenKind.LessThanLessThan,
                TokenKind.GreaterThanGreaterThan,
        });

        parseShiftExpression(depth + 1);
    }

    protected void parseAdditiveExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ADDITIVE_EXPRESSION);

        parseMultiplicativeExpression(depth + 1);

        if(!this.check(new TokenKind[] {
                TokenKind.Plus,
                TokenKind.Minus,
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[] {
                TokenKind.Plus,
                TokenKind.Minus,
        });

        parseAdditiveExpression(depth + 1);
    }

    protected void parseMultiplicativeExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.MULTIPLICATIVE_EXPRESSION);

        parsePmExpression(depth + 1);

        if(!this.check(new TokenKind[] {
                TokenKind.Asterisk,
                TokenKind.Slash,
                TokenKind.Percent
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[] {
                TokenKind.Asterisk,
                TokenKind.Slash,
                TokenKind.Percent
        });

        parseMultiplicativeExpression(depth + 1);
    }

    protected void parsePmExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PM_EXPRESSION);

        parseCastExpression(depth + 1);

        if(!this.check(new TokenKind[] {
                TokenKind.DotAsterisk,
                TokenKind.MinusGreaterThanAsterisk
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[] {
                TokenKind.DotAsterisk,
                TokenKind.MinusGreaterThanAsterisk
        });

        parsePmExpression(depth + 1);
    }

    protected void parseCastExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CAST_EXPRESSION);

        tryParse(() -> {
            this.match(depth + 1, TokenKind.OpenParen);
            parseTypeId(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
            parseCastExpression(depth + 1);
        });

        parseUnaryExpression();
    }

    protected void parseUnaryExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.UNARY_EXPRESSION);

        //Alignof
        if(this.check(TokenKind.AlignofKeyword)) {
            this.match(depth + 1, TokenKind.AlignofKeyword);
            this.match(depth + 1, TokenKind.OpenParen);
            parseTypeId(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
            return;
        }

        boolean success = false;
        //Sizeof
        if(this.check(TokenKind.SizeofKeyword)) {
            this.match(depth + 1, TokenKind.SizeofKeyword);
            success = tryParse(() -> {
                this.match(depth + 1, TokenKind.DotDotDot);
                this.match(depth + 1, TokenKind.OpenParen);
                this.match(depth + 1, TokenKind.Identifier);
                this.match(depth + 1, TokenKind.CloseParen);

            });
            if (success) return;

            success = tryParse(() -> {
                this.match(depth + 1, TokenKind.OpenParen);
                parseTypeId(depth + 1);
                this.match(depth + 1, TokenKind.CloseParen);

            });

            if (success) return;

            parseUnaryExpression(depth + 1);
            return;
        }

        //New
        if(this.checkNewExpression() && tryParse(() -> parseNewExpression(depth + 1))) return;
        //Delete
        if(this.checkDeleteExpression() && tryParse(() -> parseDeleteExpression(depth + 1))) return;
        //Noexcept
        if(this.checkNoexceptExpression() && tryParse(() -> parseNoexceptExpression(depth + 1))) return;


        // ++, --
        if(this.check(new TokenKind[] {
                TokenKind.PlusPlus,
                TokenKind.MinusMinus
        })) {
            this.match(depth + 1, new TokenKind[] {
                    TokenKind.PlusPlus,
                    TokenKind.MinusMinus
            });

            parseCastExpression(depth + 1);
            return;
        }

        // unary operator
        if(this.checkUnaryOperator()) {
            parseUnaryOperator(depth + 1);
            parseCastExpression(depth + 1);
            return;
        }

        // postfix
        parsePostifxExpression(depth + 1);
    }

    protected void parseUnaryOperator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.UNARY_OPERATOR);

        this.match(depth + 1, new TokenKind[] {
                TokenKind.Asterisk,
                TokenKind.Ampersand,
                TokenKind.Plus,
                TokenKind.Minus,
                TokenKind.Exclamation,
                TokenKind.Tilde
        });
    }

    protected void parseDeleteExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.UNARY_OPERATOR);

        if(this.check(TokenKind.ColonColon)) {
           this.match(depth + 1, TokenKind.ColonColon);
        }

        this.match(depth + 1, TokenKind.DeleteKeyword);

        if(this.check(TokenKind.OpenBracket)) {

            boolean success = this.tryParse(() -> {
                this.match(depth + 1, TokenKind.OpenBracket);
                this.match(depth + 1, TokenKind.CloseBracket);
                parseCastExpression(depth + 1);
            });

            if (success) return;
        }

        parseCastExpression(depth + 1);
    }

    protected void parseNoexceptExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NOEXCEPT_EXPRESSION);

        this.match(depth + 1, TokenKind.NoexceptKeyword);
        this.match(depth + 1, TokenKind.OpenParen);
        parseExpression(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);
    }

    protected void parseNewExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NOEXCEPT_EXPRESSION);

        if(this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        this.match(depth + 1, TokenKind.NewKeyword);

        ParseFunction parseAfterNewPlacement = () -> {
            if(this.check(TokenKind.OpenParen)) {
                this.match(depth + 1, TokenKind.OpenParen);
                parseTypeId(depth + 1);
                this.match(depth + 1, TokenKind.OpenParen);
            } else {
                parseNewTypeId(depth + 1);
            }

            tryParse(() -> parseNewInitializer(depth + 1));
        };

        boolean success = tryParse(() -> {
            parseNewPlacement(depth + 1);
            parseAfterNewPlacement.execute();
        });

        if (success) return;

        parseAfterNewPlacement.execute();
    }

    /*
        Check functions
     */

    protected boolean checkElaboratedTypeSpecifier() {
        return this.check(TokenKind.EnumKeyword) ||
                this.checkClassKey();
    }

    protected boolean checkClassKey() {
        return this.check(new TokenKind[]{
            TokenKind.ClassKeyword,
            TokenKind.StructKeyword,
            TokenKind.UnionKeyword
        });
    }

    protected boolean checkSimpleTypeSpecifier() {
        return this.check(new TokenKind[]{
                TokenKind.ColonColon,
                TokenKind.CharKeyword,
                TokenKind.Char16TKeyword,
                TokenKind.Char32TKeyword,
                TokenKind.WcharTKeyword,
                TokenKind.ShortKeyword,
                TokenKind.IntKeyword,
                TokenKind.LongKeyword,
                TokenKind.SignedKeyword,
                TokenKind.UnsignedKeyword,
                TokenKind.FloatKeyword,
                TokenKind.DoubleKeyword,
                TokenKind.VoidKeyword,
                TokenKind.AutoKeyword
        }) || this.checkDeclTypeSpecifier();
    }

    protected boolean checkDeclTypeSpecifier() {
        return this.check(TokenKind.DecltypeKeyword);
    }

    protected boolean checkClassSpecifier() {
        return this.checkClassHead();
    }

    protected boolean checkClassHead() {
        return this.checkClassKey();
    }

    protected boolean checkEnumSpecifier() {
        return this.checkEnumHead();
    }

    protected boolean checkEnumHead() {
        return this.checkEnumKey();
    }

    protected boolean checkEnumKey() {
       return this.check(TokenKind.EnumKeyword);
    }

    protected boolean checkExpression() {
        return this.checkAssignmentExpression();
    }

    protected boolean checkAssignmentExpression() {
        return this.checkConditionalExpression() ||
                this.checkLogicalOrExpression() ||
                this.checkThrowExpression();
    }

    protected boolean checkThrowExpression() {
        return this.check(TokenKind.ThrowKeyword);
    }

    protected boolean checkConstantExpression() {
       return this.checkConditionalExpression();
    }

    protected boolean checkConditionalExpression() {
        return this.checkLogicalOrExpression();
    }

    protected boolean checkLogicalOrExpression() {
        return this.checkLogicalAndExpression();
    }

    protected boolean checkLogicalAndExpression() {
        return this.checkInclusiveOrExpression();
    }

    private boolean checkInclusiveOrExpression() {
        return this.checkExclusiveOrExpression();
    }

    protected boolean checkExclusiveOrExpression() {
        return this.checkAndExpression();
    }

    protected boolean checkAndExpression() {
        return this.checkEqualityExpression();
    }

    protected boolean checkEqualityExpression() {
        return this.checkRelationalExpression();
    }

    protected boolean checkRelationalExpression() {
        return this.checkShiftExpression();
    }

    protected boolean checkShiftExpression() {
        return this.checkAdditiveExpression();
    }

    protected boolean checkAdditiveExpression() {
        return this.checkMultiplcativeExpression();
    }

    protected boolean checkMultiplcativeExpression() {
        return this.checkPmExpression();
    }

    protected boolean checkPmExpression() {
        return this.checkCastExpression();
    }

    protected boolean checkCastExpression() {
        return this.checkUnaryExpression();
    }

    protected boolean checkUnaryExpression() {
        return this.checkPostfixExpression() ||
                this.checkNoexceptExpression() ||
                this.checkNewExpression() ||
                this.checkUnaryOperator() ||
                this.checkDeleteExpression() ||
                this.check(new TokenKind[] {
                        TokenKind.PlusPlus,
                        TokenKind.MinusMinus,
                        TokenKind.AlignofKeyword,
                        TokenKind.SizeofKeyword
                });
    }

    protected boolean checkNewExpression() {
        return this.check(TokenKind.ColonColon) ||
                this.check(TokenKind.NewKeyword);
    }

    protected boolean checkDeleteExpression() {
        return this.check(TokenKind.ColonColon) ||
                this.check(TokenKind.DeleteKeyword);
    }

    protected boolean checkNoexceptExpression() {
        return this.check(TokenKind.NoexceptKeyword);
    }

    protected boolean checkUnaryOperator() {
        return this.check(new TokenKind[] {
                TokenKind.Plus,
                TokenKind.Minus,
                TokenKind.Asterisk,
                TokenKind.Ampersand,
                TokenKind.Exclamation,
                TokenKind.Tilde
        });
    }

    protected boolean checkPostfixExpression() {
        return this.checkPrimaryExpression() ||
                this.checkSimpleTypeSpecifier() ||
                this.checkTypenameSpecifier() ||
                this.check(new TokenKind[] {
                        TokenKind.DynamicCastKeyword,
                        TokenKind.StaticCastKeyword,
                        TokenKind.StaticCastKeyword,
                        TokenKind.ReinterpretCastKeyword,
                        TokenKind.ConstCastKeyword,
                        TokenKind.TypeidKeyword
                });
    }

    protected boolean checkPrimaryExpression() {
       return this.checkLiteral() ||
               this.checkIdExpression() ||
               this.checkLambdaExpression() ||
               this.check(new TokenKind[] {
                       TokenKind.ThisKeyword,
                       TokenKind.OpenParen
               });
    }

    protected boolean checkLambdaExpression() {
        return this.checkLambdaIntroducer();
    }

    protected boolean checkLambdaIntroducer() {
        return this.check(TokenKind.OpenBracket);
    }

    protected boolean checkIdExpression() {
        return this.checkUnqualifiedId() ||
                this.checkQualifiedId();
    }

    protected boolean checkUnqualifiedId() {
        return this.checkOperatorFunctionId() ||
                this.checkConversionFunctionId() ||
                this.checkLiteralOperatorId() ||
                this.checkTemplateId() ||
                this.check(new TokenKind[] {
                TokenKind.Identifier,
                TokenKind.Tilde
        });
    }

    protected boolean checkOperatorFunctionId() {
        return this.check(TokenKind.OperatorKeyword);
    }

    protected boolean checkConversionFunctionId() {
        return this.check(TokenKind.OperatorKeyword);
    }

    //NOTE: Could possibly use double lookahead to remove ambiguity from conversion-function-id and operator-function-id
    protected boolean checkLiteralOperatorId() {
        return this.check(TokenKind.OperatorKeyword);
    }

    protected boolean checkTemplateId() {
       return this.checkSimpleTemplateId() ||
               this.checkOperatorFunctionId() ||
               this.checkLiteralOperatorId();
    }

    protected boolean checkSimpleTemplateId() {
       return this.checkTemplateName();
    }

    protected boolean checkTemplateName() {
       return this.check(TokenKind.Identifier);
    }

    protected boolean checkQualifiedId() {
        return this.check(TokenKind.ColonColon);
    }

    protected boolean checkLiteral() {
        return this.checkUserDefinedLiteral() ||
                this.check(new TokenKind[] {
                TokenKind.IntegerLiteral,
                TokenKind.CharacterLiteral,
                TokenKind.FloatingLiteral,
                TokenKind.StringLiteral,
                TokenKind.BooleanLiteral,
                TokenKind.PointerLiteral,
        });
    }

    protected boolean checkUserDefinedLiteral() {
        return this.check(new TokenKind[]{
                TokenKind.UserDefinedIntegerLiteral,
                TokenKind.UserDefinedFloatingLiteral,
                TokenKind.UserDefinedStringLiteral,
                TokenKind.UserDefinedCharacterLiteral
        });
    }

    protected boolean checkTranslationUnit() {
        return this.checkDeclarationSequence();
    }

    protected boolean checkDeclarationSequence() {
        return this.checkDeclaration();
    }

    protected boolean checkDeclaration() {
        return this.checkEmptyDeclaration();
    }

    protected boolean checkEmptyDeclaration() {
        return this.check(TokenKind.Semicolon);
    }

    protected boolean checkFunctionDefinition() {
        return this.checkAttributeSpecifierSequence();
    }

    protected boolean checkDeclarationSpecifierSequence() {
        return this.checkDeclarationSpecifier();
    }

    protected boolean checkDeclarationSpecifier() {
        return this.checkStorageClassSpecifier() ||
                this.checkTypeSpecifier() ||
                this.checkFunctionSpecifier() ||
                this.check(new TokenKind[] {
                        TokenKind.FriendKeyword,
                        TokenKind.TypedefKeyword,
                        TokenKind.ConstexprKeyword
                });
    }

    protected boolean checkStorageClassSpecifier() {
        return this.check(new TokenKind[] {
                TokenKind.AutoKeyword,
                TokenKind.RegisterKeyword,
                TokenKind.StaticKeyword,
                TokenKind.ThreadLocalKeyword,
                TokenKind.ExternKeyword,
                TokenKind.MutableKeyword
        });
    }

    protected boolean checkFunctionSpecifier() {
        return this.check(new TokenKind[] {
                TokenKind.InlineKeyword,
                TokenKind.VirtualKeyword,
                TokenKind.ExplicitKeyword
        });
    }

    protected boolean checkAttributeSpecifierSequence() {
        return this.checkAttributeSpecifier();
    }

    protected boolean checkAttributeSpecifier() {
        return this.check(TokenKind.OpenBracket) ||
                this.checkAlignmentSpecifier();
    }

    protected boolean checkAttributeList() {
        return this.checkAttribute();
    }

    protected boolean checkAttribute() {
        return this.check(TokenKind.Identifier);
    }

    protected boolean checkAttributeToken() {
        return this.check(TokenKind.Identifier) ||
                this.checkAttributeScopedToken();
    }

    protected boolean checkAttributeScopedToken() {
        return this.checkAttributeNamespace();
    }

    protected boolean checkAttributeNamespace() {
        return this.check(TokenKind.Identifier);
    }

    protected boolean checkAttributeArgumentClause() {
        return this.check(TokenKind.OpenParen);
    }

    protected boolean checkBalancedTokenSequence() {
        return this.checkBalancedToken();
    }

    protected boolean checkBalancedToken() {
        return true;
    }

    protected boolean checkAlignmentSpecifier() {
        return this.check(TokenKind.AlignasKeyword);
    }

    protected boolean checkTypeId() {
        return this.checkTypeSpecifierSequence();
    }

    protected boolean checkTypeSpecifierSequence() {
        return this.checkTypeSpecifier();
    }

    protected boolean checkTypeSpecifier() {
        return this.checkTrailingTypeSpecifier() ||
                this.checkClassSpecifier() ||
                this.checkEnumSpecifier();
    }

    protected boolean checkTrailingTypeSpecifier() {
        return this.checkSimpleTypeSpecifier() ||
                this.checkElaboratedTypeSpecifier() ||
                this.checkTypenameSpecifier() ||
                this.checkCvQualifier();
    }

    protected boolean checkCvQualifier() {
        return this.check(new TokenKind[] {
            TokenKind.ConstKeyword,
            TokenKind.VolatileKeyword
        });
    }

    protected boolean checkTypenameSpecifier() {
        return this.check(TokenKind.TypenameKeyword);
    }

    protected boolean checkTemplateArgumentList() {
        return this.checkConstantExpression() ||
                this.checkTypeId() ||
                this.checkIdExpression();
    }

    protected boolean checkAbstractDeclarator() {
        return this.checkPtrAbstractDeclarator() ||
                this.checkNoPtrAbstractDeclarator() ||
                this.checkParametersAndQualifiers() ||
                this.check(TokenKind.DotDotDot);
    }

    protected boolean checkParametersAndQualifiers() {
        return this.check(TokenKind.OpenParen);
    }

    protected boolean checkPtrAbstractDeclarator() {
        return this.checkNoPtrAbstractDeclarator() ||
                this.checkPtrOperator();
    }

    protected boolean checkPtrOperator() {
        return this.checkNestedNameSpecifier() ||
                this.check(new TokenKind[] {
                TokenKind.Asterisk,
                TokenKind.Ampersand,
                TokenKind.AmpersandAmpersand,
                TokenKind.ColonColon
        });
    }

    protected boolean checkNestedNameSpecifier() {
       return this.checkTypeName() ||
               this.checkNamespaceName() ||
               this.checkDeclTypeSpecifier() ||
               this.checkNestedNameSpecifier();
    }

    protected boolean checkNamespaceName() {
        return this.checkOriginalNamespaceName() ||
                this.namespaceAlias();
    }

    protected boolean checkOriginalNamespaceName() {
        return this.check(TokenKind.Identifier);
    }

    protected boolean namespaceAlias() {
        return this.check(TokenKind.Identifier);
    }

    protected boolean checkTypeName() {
        return this.checkClassName() ||
                this.checkEnumName() ||
                this.checkTypedefName() ||
                this.checkSimpleTemplateId();
    }

    protected boolean checkTypedefName() {
        return this.check(TokenKind.Identifier);
    }

    protected boolean checkEnumName() {
        return this.check(TokenKind.Identifier);
    }

    protected boolean checkClassName() {
        return this.check(TokenKind.Identifier) ||
                this.checkSimpleTemplateId();
    }

    protected boolean checkNoPtrAbstractDeclarator() {
        return this.check(TokenKind.OpenParen) ||
                this.check(TokenKind.OpenBracket);

    }

    protected boolean checkNewInitializer() {
        return this.check(TokenKind.OpenParen) ||
                this.checkBracedInitList();
    }

    protected boolean checkBracedInitList() {
        return this.check(TokenKind.OpenBrace);
    }
}

