package org.bromano.cplusplusparser.parser;

import org.bromano.cplusplusparser.scanner.Token;
import org.bromano.cplusplusparser.scanner.TokenKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.bromano.cplusplusparser.parser.NodeType.ENUMERATOR_DEFINITION;
import static org.bromano.cplusplusparser.parser.NodeType.PARAMETER_DECLARATION;

public class SimpleParser implements Parser {
    protected List<Token> tokens;
     Stack<String> tree;

    protected Stack<Integer> savedTreePos;

    protected int pos;
    protected int end;
    protected Stack<Integer> savedPos;

    public SimpleParser(List<Token> tokens) {
        this.setTokens(tokens);
    }

    public SimpleParser() {
        this.setTokens(new ArrayList<>());
    }

    public Stack<String> getTree() {
        return this.reverseStack(this.tree);
    }

    public void setTokens(List<Token> tokens) {
        this.tree = new Stack<>();
        this.savedTreePos= new Stack<>();
        this.savedPos = new Stack<>();
        this.tokens = this.filterEOFToken(tokens);
        this.end = this.tokens.size();
        this.pos = 0;
    }

    protected List<Token> filterEOFToken(List<Token> tokens) {
        return tokens.stream().filter(token -> token.kind != TokenKind.EndOfFile).collect(Collectors.toList());
    }

    //NOTE: For testing purposes
    protected void printTree() {
        Stack<String> stack = this.reverseStack(this.tree);
        while(!stack.isEmpty()) System.out.println(stack.pop());
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
        int treeSize = this.savedTreePos.pop();
        while (this.tree.size() > treeSize) {
            this.tree.pop();
        }
    }

    protected boolean check(TokenKind kind) {
        return this.pos < this.end && this.tokens.get(this.pos).kind == kind;
    }

    protected boolean check(TokenKind[] kinds) {
        if (this.pos >= this.end) {
            return false;
        }

        TokenKind currTokenKind = this.tokens.get(pos).kind;
        for (TokenKind kind : kinds) {
            if (currTokenKind == kind) {
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
        for (int i = 0; i < depth; i++) {
            node.append("  ");
        }
        node.append(text);
        return node.toString();

    }

    protected void addTreeNode(int depth, Token token) {
        if(token.lexeme == null) {
            this.tree.push(this.createNode(depth, token.kind.name()));
            return;
        }

        this.tree.push(this.createNode(depth, token.kind.name() + " - " + token.lexeme.toString()));

    }

    protected void addTreeNode(int depth, NodeType node) {
        this.tree.push(this.createNode(depth, node.name()));

    }

    protected Token match(int depth) throws ParserException {
        if (this.pos >= this.end) {
            throw new ParserException("Expected: token");
        }

        Token token = this.tokens.get(this.pos);

        this.addTreeNode(depth, token);

        this.pos++;
        return token;

    }

    protected Token match(int depth, TokenKind tokenKind) throws ParserException {
        if (this.pos >= this.end || this.tokens.get(this.pos).kind != tokenKind) {
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

        if (tokenKinds.length >= 1) {
            errorMessageBuilder.append(tokenKinds[0]);
        }

        for (int i = 1; i < tokenKinds.length; i++) {
            errorMessageBuilder.append(", ");
            errorMessageBuilder.append(tokenKinds[i]);

        }

        if (this.pos >= this.end) {
            throw new ParserException(errorMessageBuilder.toString());
        }

        Token token = this.tokens.get(this.pos);
        for (TokenKind tokenKind : tokenKinds) {
            if (token.kind == tokenKind) {
                this.pos++;
                this.addTreeNode(depth, token);
                return token;
            }
        }

        throw new ParserException(errorMessageBuilder.toString());
    }

    public <T> Stack<T> reverseStack(Stack<T> stack) {
        Stack<T> reversedStack = new Stack<>();
        Stack<T> tempStack = new Stack<>();

        while(!stack.isEmpty()) {
            tempStack.push(stack.peek());
            reversedStack.push(stack.pop());
        }

        while(!tempStack.isEmpty()) {
            stack.push(tempStack.pop());
        }

        return reversedStack;
    }

    public Stack<String> parse() throws ParserException {
        parseTranslationUnit(0);

        return reverseStack(this.tree);
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

    protected void parseTranslationUnit(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TRANSLATION_UNIT);

        if (this.checkDeclarationSequence()) {
            parseDeclarationSequence(depth + 1);
        }

    }

    protected void parseDeclarationSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.DECLARATION_SEQUENCE);

        parseDeclaration(depth + 1);

        if(!this.checkDeclarationSequence()) return;
        parseDeclarationSequence(depth + 1);
    }

    protected void parseDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.DECLARATION);

        if(this.check(TokenKind.Semicolon)) {
            parseEmptyDeclaration(depth + 1);
            return;
        } else if (this.check(TokenKind.NamespaceKeyword)) {
            parseNamespaceDefinition(depth + 1);
            return;
        }

        if(this.checkFunctionDefinition() && tryParse(() -> parseFunctionDefinition(depth + 1))) return;
        if(this.checkExplicitInstantiation() && tryParse(() -> parseExplicitInstantiation(depth + 1))) return;
        if(this.checkExplicitSpecialization() && tryParse(() -> parseExplicitSpecialization(depth + 1))) return;
        if(this.checkLinkageSpecification() && tryParse(() -> parseLinkageSpecification(depth + 1))) return;
        if(this.checkNamespaceDefinition() && tryParse(() -> parseNamespaceDefinition(depth + 1))) return;
        if(this.checkAttributeDeclaration() && tryParse(() -> parseAttributeDeclaration(depth + 1))) return;

        parseBlockDeclaration(depth + 1);
    }

    protected void parseAttributeDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTIRBUTE_DECLARATION);
        parseAttributeSpecifierSequence(depth + 1);
        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseNamespaceDefinition(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NAMESPACE_DEFIITION);

        if(this.checkNamedNamespaceDefinition() && tryParse(() -> parseNamedNamespaceDefinition(depth + 1))) return;

        parseUnnamedNamespaceDefinition(depth + 1);
    }

    protected void parseUnnamedNamespaceDefinition(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NAMED_NAMESPACE_DEFINITION);

        if (this.check(TokenKind.InlineKeyword)) {
            this.match(depth + 1, TokenKind.InlineKeyword);
        }

        this.match(depth + 1, TokenKind.NamespaceKeyword);
        this.match(depth + 1, TokenKind.Identifier);
        this.match(depth + 1, TokenKind.OpenBrace);
        parseNamespaceBody(depth + 1);
        this.match(depth + 1, TokenKind.CloseBrace);
    }

    protected void parseNamedNamespaceDefinition(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NAMED_NAMESPACE_DEFINITION);

        if(this.checkOriginalNamespaceDefinition() && this.tryParse(() -> parseOriginalNamespaceDefinition(depth + 1))) return;
        parseExtensionNamespaceDefinition(depth + 1);
    }

    protected void parseOriginalNamespaceDefinition(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ORIGINAL_NAMESPACE_DECLARATION);

        if (this.check(TokenKind.InlineKeyword)) {
            this.match(depth + 1, TokenKind.InlineKeyword);
        }

        this.match(depth + 1, TokenKind.NamespaceKeyword);
        this.match(depth + 1, TokenKind.Identifier);
        this.match(depth + 1, TokenKind.OpenBrace);
        parseNamespaceBody(depth + 1);
        this.match(depth + 1, TokenKind.CloseBrace);

    }

    protected void parseExtensionNamespaceDefinition(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXTENSION_NAMESPACE_DEFINITION);
        if (this.check(TokenKind.InlineKeyword)) {
            this.match(depth + 1, TokenKind.InlineKeyword);
        }

        this.match(depth + 1, TokenKind.NamespaceKeyword);
        parseOriginalNamespaceName(depth + 1);
        this.match(depth + 1, TokenKind.OpenBrace);

        if(!this.check(TokenKind.CloseBrace)) {
            parseNamespaceBody(depth + 1);
        }

        this.match(depth + 1, TokenKind.CloseBrace);

    }

    protected void parseNamespaceBody(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NAMESPACE_BODY);
        parseDeclarationSequence(depth + 1);
    }

    protected void parseLinkageSpecification(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.LINKAGE_SPEIFICATION);
        this.match(depth + 1, TokenKind.ExternKeyword);
        this.match(depth + 1, TokenKind.StringLiteral);
        this.match(depth + 1, TokenKind.OpenBrace);

        if(this.checkDeclarationSequence()) {
           parseDeclarationSequence(depth + 1);
        }

        this.match(depth + 1, TokenKind.CloseBrace);
    }

    protected void parseExplicitSpecialization(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXPLICIT_SPECIALIZATION);

        this.match(depth + 1, TokenKind.TemplateKeyword);
        this.match(depth + 1, TokenKind.LessThan);
        this.match(depth + 1, TokenKind.GreaterThan);

        parseDeclaration(depth + 1);
    }

    protected void parseExplicitInstantiation(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXPLICIT_INSTANTIATION);

        if (this.check(TokenKind.ExternKeyword)) {
            this.match(depth + 1, TokenKind.ExternKeyword);
        }

        this.match(depth + 1, TokenKind.TemplateKeyword);

        parseDeclaration(depth + 1);
    }

    protected void parseEmptyDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EMPTY_DECLARATION);
        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseFunctionDefinition(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.FUNCTION_DEFINITION);

        if(this.checkAttributeSpecifierSequence()) {
            parseAttributeSpecifierSequence(depth + 1);
        }

        if(this.checkDeclSpecifierSequence() && tryParse(() -> {
                parseDeclSpecifierSequence(depth + 1);

                parseDeclarator(depth + 1);

                if (this.check(TokenKind.Equals)) {
                    this.match(depth + 1, TokenKind.Equals);

                    this.match(depth + 1,new TokenKind[] {
                            TokenKind.DeleteKeyword,
                            TokenKind.DefaultKeyword
                    });
                    return;
                }

                parseFunctionBody(depth + 1);

            })) return;

        parseDeclarator(depth + 1);

        if (this.check(TokenKind.Equals)) {
            this.match(depth + 1, TokenKind.Equals);

            this.match(depth + 1,new TokenKind[] {
                    TokenKind.DeleteKeyword,
                    TokenKind.DefaultKeyword
            });

            return;
        }

        parseFunctionBody(depth + 1);
    }

    protected void parseFunctionBody(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.FUNCTION_BODY);

        if(this.checkFunctionTryBlock()) {
            parseFunctionTryBlock(depth + 1);
            return;
        }

        if(this.checkCtorInitializer()) {
            parseCtorInitializer(depth + 1);
        }

        parseCompoundStatement(depth + 1);
    }

    protected void parseFunctionTryBlock(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.FUNCTION_TRY_BLOCK);

        this.match(depth + 1, TokenKind.TryKeyword);
        parseCompoundStatement(depth + 1);
        parseHandlerSequence(depth + 1);
    }

    protected void parseHandlerSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.HANDLER_SEQUENCE);

        parseHandler(depth + 1);

        if(this.checkHandlerSequence()) {
            parseHandler(depth + 1);
        }
    }

    protected void parseHandler(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.HANDLER);

        this.match(depth + 1, TokenKind.CatchKeyword);
        this.match(depth + 1, TokenKind.OpenParen);
        parseExceptionDeclaration(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);
        parseCompoundStatement(depth + 1);
    }

    protected void parseExceptionDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXCEPTION_DECLARATION);

        if(this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
            return;
        }

        if(this.checkAttributeSpecifierSequence()) {
            parseAttributeSpecifierSequence(depth + 1);
        }

        parseTypeSpecifierSequence(depth + 1);

        if(this.checkAbstractDeclarator() && tryParse(() -> parseAbstractDeclarator(depth + 1))) return;

        if(this.checkDeclarator()) {
           tryParse(() -> parseDeclarator(depth + 1));
        }

    }

    protected void parseCtorInitializer(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CTOR_INITIALIZER);

    }

    protected void parseCompoundStatement(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.COMPOUND_STATEMENT);

        this.match(depth + 1, TokenKind.OpenBrace);
        if(this.checkStatementSequence()) {
            parseStatementSequence(depth + 1);
        }
        this.match(depth + 1, TokenKind.CloseBrace);
    }

    protected void parseStatementSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.STATEMENT_SEQUENCE);

        parseStatement(depth + 1);

        if(this.checkStatementSequence()) {
            parseStatementSequence(depth + 1);
        }
    }

    protected void parseStatement(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.STATEMENT);

        if (this.check(TokenKind.Identifier)) {
            if(this.checkLabeledStatement() && tryParse(() -> parseLabeledStatement(depth + 1))) return;
            else if(this.checkDeclarationStatement() && tryParse(() -> parseDeclarationStatement(depth + 1))) return;
            else {
                parseExpressionStatement(depth + 1);
                return;
            }
        } else if (this.checkLabeledStatement()) {
            parseLabeledStatement(depth + 1);
            return;
        } else if (this.checkDeclarationStatement()) {
            parseDeclarationStatement(depth + 1);
            return;
        } else if (this.checkExpressionStatement()) {
            parseDeclarationStatement(depth + 1);
            return;
        } else if (this.checkDeclarationStatement()) {
            parseDeclarationStatement(depth + 1);
            return;
        } else if (this.checkAttributeSpecifierSequence()) {
            parseAttributeSpecifierSequence(depth + 1);
            return;
        } else if (this.checkCompoundStatement()) {
            parseCompoundStatement(depth + 1);
            return;
        } else if (this.checkIterationStatement()) {
            parseIterationStatement(depth + 1);
            return;
        } else if (this.checkSelectionStatement()) {
            parseSelectionStatement(depth + 1);
            return;
        }

        parseJumpStatement(depth + 1);
    }

    protected void parseDeclarationStatement(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.DECLARATION_STATEMENT);

        parseBlockDeclaration(depth + 1);
    }

    protected void parseBlockDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.BLOCK_DECLARATION);

        if(this.checkOpaqueEnumDeclaration() && tryParse(() -> parseOpaqueEnumDeclaration(depth + 1))) return;
        if(this.checkAsmDefinition() && tryParse(() -> parseAsmDefinition(depth + 1))) return;
        if(this.checkUsingDeclaration() && tryParse(() -> parseUsingDeclaration(depth + 1))) return;
        if(this.checkUsingDirective() && tryParse(() -> parseUsingDirective(depth + 1))) return;
        if(this.checkStaticAssertDeclaration() && tryParse(() -> parseStaticAssertDeclaration(depth + 1))) return;
        if(this.checkAliasDeclaration() && tryParse(() -> parseAliasDeclaration(depth + 1))) return;

        parseSimpleDeclaration(depth + 1);
    }

    protected void parseSimpleDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.SIMPLE_DECLARATION);

        if(this.checkAttributeSpecifierSequence()) {
            tryParse(() -> parseAttributeSpecifierSequence(depth + 1));
        }

        if(this.checkDeclSpecifierSequence()) {
            tryParse(() -> parseDeclSpecifierSequence(depth + 1));
        }

        if(this.checkInitDeclaratorList()) {
            tryParse(() -> parseInitDeclaratorList(depth + 1));
        }

        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseInitDeclaratorList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.INIT_DECLARATOR_LIST);

        parseInitDeclarator(depth + 1);

        if (!this.check(TokenKind.Comma)) return;

        this.match(depth + 1, TokenKind.Comma);

        parseInitDeclaratorList(depth + 1);
    }

    protected void parseInitDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.INIT_DECLARATOR);

        parseDeclarator(depth + 1);

        if(this.checkInitializer()) {
            tryParse(() -> parseInitializer(depth + 1));
        }
    }

    protected void parseInitializer(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.INITIALIZER);

        if(this.checkBraceOrEqualInitializer()) {
            parseBraceOrEqualInitializer(depth + 1);
            return;
        }

        this.match(depth + 1, TokenKind.OpenParen);
        parseExpressionList(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);
    }

    protected void parseBraceOrEqualInitializer(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.BRACE_OR_EQUAL_INITIALIZER);

        if(this.check(TokenKind.Equals)) {
            this.match(depth + 1, TokenKind.Equals);
            parseInitializerClause(depth + 1);
            return;
        }

        parseBracedInitList(depth + 1);
    }

    protected void parseAsmDefinition(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ASM_DEFINITION);

        this.match(depth + 1, TokenKind.AsmKeyword);
        this.match(depth + 1, TokenKind.StringLiteral);
        this.match(depth + 1, TokenKind.OpenParen);
        this.match(depth + 1, TokenKind.CloseParen);
        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseUsingDirective(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.USING_DIRECTIVE);

        if(this.checkAttributeSpecifierSequence()) {
            tryParse(() -> parseAttributeSpecifierSequence(depth + 1));
        }

        this.match(depth + 1, TokenKind.UsingKeyword);
        this.match(depth + 1, TokenKind.NamespaceKeyword);

        if(this.check(TokenKind.ColonColon)) {
            boolean success = tryParse(() -> parseNestedNameSpecifier(depth + 1));

            if (!success) {
                this.match(depth + 1, TokenKind.ColonColon);
            }
        }

        if(this.checkNestedNameSpecifier()) {
            tryParse(() -> parseNestedNameSpecifier(depth + 1));
        }

        parseNamespaceName(depth + 1);
        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseOpaqueEnumDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.OPAQUE_ENUM_DECLARATION);
        parseEnumKey(depth + 1);
        if(this.checkAttributeSpecifierSequence()) {
            parseAttributeSpecifierSequence(depth + 1);
        }

        this.match(depth + 1, TokenKind.Identifier);

        if(this.checkEnumBase()) {
            parseEnumBase(depth + 1);
        }
        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseIterationStatement(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ITERATION_STATEMENT);

        if(this.check(TokenKind.WhileKeyword)) {
            this.match(depth + 1, TokenKind.WhileKeyword);
            this.match(depth + 1, TokenKind.OpenParen);
            parseCondition(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
            parseStatement(depth + 1);
            return;
        }

        if(this.check(TokenKind.DoKeyword)) {
            this.match(depth + 1, TokenKind.DoKeyword);
            parseStatement(depth + 1);
            this.match(depth + 1, TokenKind.OpenParen);
            parseExpression(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
            this.match(depth + 1, TokenKind.Semicolon);
            return;
        }

        this.match(depth + 1, TokenKind.ForKeyword);
        this.match(depth + 1, TokenKind.OpenParen);

        boolean success = tryParse(() -> {
            parseForInitStatement(depth + 1);

            if(this.checkCondition()) {
                parseCondition(depth + 1);
            }
            this.match(depth + 1, TokenKind.Semicolon);
            parseExpression(depth + 1);

        });

        if(!success) {
            parseForRangeDeclaration(depth + 1);
            this.match(depth + 1, TokenKind.Colon);
            parseForRangeInitializer(depth + 1);
        }
        this.match(depth + 1, TokenKind.CloseParen);
        parseStatement(depth + 1);
    }

    protected void parseForRangeInitializer(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.FOR_RANGE_INITIALIZER);

        parseExpression(depth + 1);
        parseBracedInitList(depth + 1);
    }

    protected void parseForRangeDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.FOR_RANGE_DECLARATION);

        if(this.checkAttributeSpecifierSequence()) {
            parseAttributeSpecifierSequence(depth + 1);
        }
        parseTypeSpecifierSequence(depth + 1);
        parseDeclarator(depth + 1);
    }

    protected void parseForInitStatement(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.FOR_INIT_STATEMENT);

        if(this.checkExpressionStatement()) {
            parseExpression(depth + 1);
            return;
        }

        parseSimpleDeclaration(depth + 1);
    }

    protected void parseExpressionStatement(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXPRESSION_STATEMENT);
        if(this.checkExpression()) {
            parseExpression(depth + 1);
        }
        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseCondition(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CONDITION);

        if(this.checkAttributeSpecifierSequence()) {
            parseAttributeSpecifierSequence(depth + 1);
            parseDeclSpecifierSequence(depth + 1);
            parseDeclarator(depth + 1);
            if(this.check(TokenKind.Equals)) {
                this.match(depth + 1, TokenKind.Equals);
                parseInitializerClause(depth + 1);
                return;
            }
            parseBracedInitList(depth + 1);
        }

        parseExpression(depth + 1);
    }

    protected void parseSelectionStatement(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.SELECTION_STATEMENT);

        if(this.check(TokenKind.SwitchKeyword)) {
            this.match(depth + 1, TokenKind.SwitchKeyword);
            this.match(depth + 1, TokenKind.OpenParen);
            parseCondition(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
            parseStatement(depth + 1);
            return;
        }

        this.match(depth + 1, TokenKind.IfKeyword);
        this.match(depth + 1, TokenKind.OpenParen);
        parseCondition(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);
        parseStatement(depth + 1);
        if(!this.check(TokenKind.ElseKeyword)) return;

        this.match(depth + 1, TokenKind.ElseKeyword);
        parseStatement(depth + 1);
    }

    protected void parseJumpStatement(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.JUMP_STATEMENT);
        if(this.check(new TokenKind[] {
                TokenKind.BreakKeyword,
                TokenKind.ContinueKeyword
        })) {
            this.match(depth + 1, new TokenKind[]{
                    TokenKind.BreakKeyword,
                    TokenKind.ContinueKeyword

            });
        } else if(this.check(TokenKind.ReturnKeyword)) {
            this.match(depth + 1, TokenKind.ReturnKeyword);

            if(this.checkExpression()) {
                parseExpression(depth + 1);
            } else if(this.checkBracedInitList()) {
                parseBracedInitList(depth + 1);
            }

        } else {
            this.match(depth + 1, TokenKind.GotoKeyword);
            this.match(depth + 1, TokenKind.Identifier);
        }

        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseLabeledStatement(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.LABELED_STATEMENT);

        if(this.checkAttributeSpecifierSequence()) {
            parseAttributeSpecifierSequence(depth + 1);
        }

        if(this.check(TokenKind.CaseKeyword)) {
            this.match(depth + 1, TokenKind.CaseKeyword);
            parseConstantExpression(depth + 1);
        } else {
            this.match(depth + 1, new TokenKind[] {
                    TokenKind.Identifier,
                    TokenKind.DefaultKeyword

            });
        }

        this.match(depth + 1, TokenKind.Colon);
        parseStatement(depth + 1);
    }

    protected void parseAttributeSpecifierSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTIBUTE_SPECIFIER_SEQUENCE);

        if (this.checkAttributeSpecifierSequence()) {
            boolean success = tryParse(() -> {
                parseAttributeSpecifier(depth + 1);
                parseAttributeSpecifierSequence(depth + 1);
            });

            if (success) {
                return;
            }
        }

        parseAttributeSpecifier(depth + 1);
    }

    protected void parseAttributeSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_SPECIFIER);

        if (this.check(TokenKind.OpenBracket)) {
            this.match(depth + 1, TokenKind.OpenBracket);
            this.match(depth + 1, TokenKind.OpenBracket);
            parseAttributeList(depth + 1);
            this.match(depth + 1, TokenKind.CloseBracket);
            this.match(depth + 1, TokenKind.CloseBracket);
            return;
        }

        parseAlignmentSpecifier(depth + 1);
    }

    protected void parseAttributeList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_LIST);

        if (this.checkAttribute()) {
            parseAttribute(depth + 1);

            if (this.check(TokenKind.DotDotDot)) {
                this.match(depth + 1, TokenKind.DotDotDot);

                if (this.check(TokenKind.Comma)) {
                    this.match(depth + 1, TokenKind.Comma);
                    parseAttributeList(depth + 1);
                }
            } else if (this.check(TokenKind.Comma)) {
                this.match(depth + 1, TokenKind.Comma);
                parseAttributeList(depth + 1);
                return;
            }
        }

        if (this.check(TokenKind.Comma)) {
            this.match(depth + 1, TokenKind.Comma);
            parseAttributeList(depth + 1);
        }
    }


    protected void parseAttribute(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE);

        parseAttributeToken(depth + 1);

        if (this.checkAttributeArgumentClause()) {
            tryParse(() -> parseAttributeArgumentClause(depth + 1));
        }
    }

    protected void parseAttributeToken(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_TOKEN);

        boolean success = tryParse(() -> parseAttributeScopedToken(depth + 1));

        if (success) {
            return;
        }

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseAttributeScopedToken(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_SCOPED_TOKEN);

        parseAttributeNamespace(depth + 1);

        this.match(depth + 1, TokenKind.ColonColon);
        this.match(depth + 1, TokenKind.Identifier);
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

    protected void parseBalancedTokenSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ATTRIBUTE_ARGUMENT_CLAUSE);

        boolean success = tryParse(() -> {
            parseBalancedToken(depth + 1);
            parseBalancedTokenSequence(depth + 1);
        });

        if (success) {
            return;
        }

        this.parseBalancedToken(depth + 1);
    }

    protected void parseBalancedToken(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.BALANCED_TOKEN);

        if (this.check(new TokenKind[]{
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
                this.match(depth + 1, TokenKind.OpenParen);
            }

            return;
        }

        this.match(depth + 1);
    }

    protected void parseAlignmentSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ALIGNMENT_SPECIFIER);

        this.match(depth + 1, TokenKind.AlignasKeyword);
        this.match(depth + 1, TokenKind.OpenParen);

        if (this.checkTypeId()) {
            tryParse(() -> parseTypeId(depth + 1));
        } else {
            parseExpression(depth + 1);
        }

        if (this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
        }

        this.match(depth + 1, TokenKind.CloseParen);
    }

    protected void parseTypeId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPE_ID);

        parseTypeSpecifierSequence(depth + 1);

        if (this.checkAbstractDeclarator()) {
            tryParse(() -> parseAbstractDeclarator(depth + 1));
        }
    }

    protected void parseAbstractDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ABSTRACT_DECLARATOR);

        if (this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
            return;
        }

        if (this.checkNoPtrAbstractDeclarator()) {
            tryParse(() -> {
                parseNoPtrAbstractDeclarator(depth + 1);
                parseParametersAndQualifiers(depth + 1);
                parseTrailingReturnType(depth + 1);
            });
        }

        if (this.checkPtrAbstractDeclarator()) {
            parsePtrAbstractDeclarator(depth + 1);
            return;
        }

        parseParametersAndQualifiers(depth + 1);
        parseTrailingReturnType(depth + 1);
    }

    protected void parsePtrAbstractDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PTR_ABSTRACT_DECLARATOR);

        if (this.checkPtrOperator()) {
            parsePtrOperator(depth + 1);

            tryParse(() -> parsePtrAbstractDeclarator(depth + 1));

            return;
        }

        parseNoPtrAbstractDeclarator(depth + 1);
    }

    protected void parsePtrOperator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PTR_OPERATOR);

        if (this.check(new TokenKind[]{
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

        if (this.check(TokenKind.Asterisk)) {
            this.match(depth + 1, TokenKind.Asterisk);

            tryParse(() -> {
                parseAttributeSpecifierSequence(depth + 1);
                tryParse(() -> parseCvQualifier(depth + 1));
            });

            return;
        }

        if (this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        parseNestedNameSpecifier(depth + 1);
        this.match(depth + 1, TokenKind.Asterisk);

        tryParse(() -> {
            parseAttributeSpecifierSequence(depth + 1);
            tryParse(() -> parseCvQualifier(depth + 1));
        });
    }

    protected void parseNestedNameSpecifierHelper(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NESTED_NAME_SPECIFIER);

        if (this.check(TokenKind.TemplateKeyword)) {
            this.match(depth + 1, TokenKind.TemplateKeyword);
            parseSimpleTemplateId(depth + 1);
            match(depth + 1, TokenKind.ColonColon);
            if(this.checkNestedNameSpecifier()) {
                tryParse(() -> parseNestedNameSpecifierHelper(depth + 1));
            }
            return;
        }

        this.match(depth + 1, TokenKind.Identifier);
        match(depth + 1, TokenKind.ColonColon);
        if(this.checkNestedNameSpecifier()) {
            tryParse(() -> parseNestedNameSpecifierHelper(depth + 1));
        }
    }

    protected void parseNestedNameSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NESTED_NAME_SPECIFIER);

        if (this.checkDeclTypeSpecifier()) {
            parseDecltypeSpecifier(depth + 1);
            match(depth + 1, TokenKind.ColonColon);
            if(this.checkNestedNameSpecifier()) {
                tryParse(() -> parseNestedNameSpecifierHelper(depth + 1));
            }
            return;
        }

        if(this.checkNamespaceName()) {
            parseNamespaceName(depth + 1);
            match(depth + 1, TokenKind.ColonColon);
            if(this.checkNestedNameSpecifier()) {
                tryParse(() -> parseNestedNameSpecifierHelper(depth + 1));
            }
            return;
        }

        parseTypeName(depth + 1);
        match(depth + 1, TokenKind.ColonColon);
        if(this.checkNestedNameSpecifier()) {
            tryParse(() -> parseNestedNameSpecifierHelper(depth + 1));
        }
    }

    protected void parseNamespaceName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NAMESPACE_NAME);

        parseOriginalNamespaceName(depth + 1);
    }

    protected void parseOriginalNamespaceName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ORIGINAL_NAMESPACE_NAME);

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseNoPtrAbstractDeclaratorHelper(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NO_PTR_ABSTRACT_DECLARATOR);

        if (this.check(TokenKind.OpenParen)) {
            parseParametersAndQualifiers(depth + 1);
        } else {
            this.match(depth + 1, TokenKind.OpenBracket);
            parseConstantExpression(depth + 1);
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

        if (this.checkTrailingTypeSpecifier() && tryParse(() -> parseTrailingTypeSpecifier(depth + 1))) return;
        if (this.checkClassSpecifier() && tryParse(() -> parseClassSpecifier(depth + 1))) return;

        parseEnumSpecifier(depth + 1);
    }

    protected void parseClassSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CLASS_SPECIFIER);

        parseClassHead(depth + 1);
        this.match(depth + 1, TokenKind.OpenBrace);

        if (this.check(TokenKind.CloseBrace)) {
            this.match(depth + 1, TokenKind.CloseBrace);
            return;
        }

        parseMemberSpecification(depth + 1);
        this.match(depth + 1, TokenKind.CloseBrace);
    }


    protected void parseClassHeadAfterAttributeSpecifierSequence(int depth) throws ParserException {
        boolean success;

        if (this.checkClassHeadName()) {
            tryParse(() -> parseNestedNameSpecifier(depth + 1));
            this.match(depth + 1, TokenKind.Identifier);
            tryParse(() -> parseBaseClause(depth + 1));
            return;
        }

        success = tryParse(() -> {
            this.match(depth + 1, TokenKind.Identifier);
            tryParse(() -> parseBaseClause(depth + 1));
        });

        if (success) return;

        tryParse(() -> parseBaseClause(depth + 1));
    }

    protected void parseBaseClause(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.BASE_CLAUSE);

        this.match(depth + 1, TokenKind.Colon);
        parseBaseSpecifierList(depth + 1);
    }

    protected void parseBaseSpecifierList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.BASE_SPECIFIER_LIST);

        parseBaseSpecifier(depth + 1);
        if (this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
        }

        if (this.check(TokenKind.Comma)) {
            tryParse(() -> parseBaseSpecifierList(depth + 1));
        }
    }

    protected void parseBaseTypeSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.BASE_TYPE_SPECIFIER);

        parseClassOrDecltype(depth + 1);
    }

    protected void parseClassOrDecltype(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CLASS_OR_DECLTYPE);

        if (this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
            boolean success = tryParse(() -> {
                parseNestedNameSpecifier(depth + 1);
                parseClassName(depth + 1);
            });
            if (success) return;
            parseClassName(depth + 1);
            return;
        }

        parseDecltypeSpecifier(depth + 1);
    }

    protected void parseBaseSpecifierHelper(int depth) throws ParserException {
        if (this.checkBaseTypeSpecifier()) {
            parseBaseTypeSpecifier(depth + 1);
        }

        if (this.check(TokenKind.VirtualKeyword)) {
            this.match(depth + 1, TokenKind.VirtualKeyword);
            boolean success = tryParse(() -> {
                parseAccessSpecifier(depth + 1);
                parseBaseTypeSpecifier(depth + 1);
            });

            if (success) return;
            parseBaseTypeSpecifier(depth + 1);
            return;
        }

        parseAccessSpecifier(depth + 1);

        if (this.check(TokenKind.VirtualKeyword)) {
            this.match(depth + 1, TokenKind.VirtualKeyword);
        }
        parseBaseTypeSpecifier(depth + 1);
    }

    protected void parseBaseSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.BASE_SPECIFIER);

        if (this.checkAttributeSpecifierSequence()) {
            tryParse(() -> {
                parseAttributeSpecifierSequence(depth + 1);
                parseBaseSpecifierHelper(depth + 1);
            });
        }
    }

    protected void parseClassHead(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CLASS_HEAD);

        parseClassKey(depth + 1);

        if (this.checkAttributeSpecifierSequence()) {
            boolean success = tryParse(() -> {
                parseAttributeSpecifierSequence(depth + 1);
                parseClassHeadAfterAttributeSpecifierSequence(depth + 1);
            });
            if (success) return;
        }

        parseClassHeadAfterAttributeSpecifierSequence(depth + 1);
    }

    protected void parseMemberSpecification(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.MEMBER_SPECIFICATION);

        if (this.checkAccessSpecifier()) {
            parseAccessSpecifier(depth + 1);
            this.match(depth + 1, TokenKind.Colon);
            tryParse(() -> parseMemberSpecification(depth + 1));
            return;
        }

        parseMemberDeclaration(depth + 1);
        tryParse(() -> parseMemberSpecification(depth + 1));
    }

    protected void parseAccessSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ACCESS_SPECIFIER);

        this.match(depth + 1, new TokenKind[]{
                TokenKind.PrivateKeyword,
                TokenKind.ProtectedKeyword,
                TokenKind.PublicKeyword
        });
    }

    protected void parseMemberDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.MEMBER_DECLARATION);

        if(this.checkDeclSpecifierSequence() && tryParse(() -> {
            parseDeclSpecifierSequence(depth + 1);
            if (this.checkMemberDeclaratorList() && tryParse(() -> {
                parseMemberDeclaratorList(depth + 1);
                this.match(depth + 1, TokenKind.Semicolon);
            })) return;

            this.match(depth + 1, TokenKind.Semicolon);
        })) return;

        if (this.checkMemberDeclaratorList()) {
                parseMemberDeclaratorList(depth + 1);
            this.match(depth + 1, TokenKind.Semicolon);
            return;
        }

        if(tryParse(() -> {
            parseFunctionDefinition(depth + 1);
            if(this.check(TokenKind.Semicolon)) {
                this.match(depth + 1, TokenKind.Semicolon);
            }
        })) return;

        if (this.checkUsingDeclaration() && tryParse(() -> parseUsingDeclaration(depth + 1))) return;
        if(this.checkStaticAssertDeclaration() && tryParse(() -> parseStaticAssertDeclaration(depth + 1))) return;
        if(this.checkTemplateDeclaration() && tryParse(() -> parseTemplateDeclaration(depth + 1))) return;
        parseAliasDeclaration(depth + 1);
    }

    protected void parseAliasDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ALIAS_DECLARATION);

        this.match(depth + 1, TokenKind.UsingKeyword);
        this.match(depth + 1, TokenKind.Identifier);
        this.match(depth + 1, TokenKind.Equals);
        parseTypeId(depth + 1);
        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseTemplateDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TEMPLATE_DECLARATION);

        this.match(depth + 1, TokenKind.TemplateKeyword);
        this.match(depth + 1, TokenKind.LessThan);
        parseTemplateParameterList(depth + 1);
        this.match(depth + 1, TokenKind.LessThan);
        parseDeclaration(depth + 1);
    }

    protected void parseTemplateParameterList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TEMPLATE_PARAMETER_LIST);

        parseTemplateParameter(depth + 1);

        if(!this.check(TokenKind.Comma)) return;

        this.match(depth + 1, TokenKind.Comma);
        parseTemplateParameterList(depth + 1);
    }

    protected void parseTemplateParameter(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TEMPLATE_PARAMETER);

        if(tryParse(() -> parseTypeParameter(depth + 1))) return;
        parseParameterDeclaration(depth + 1);
    }

    protected void parseTypeParameter(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPE_PARAMETER);

        if(this.check(new TokenKind[]{
                TokenKind.TypenameKeyword,
                TokenKind.ClassKeyword
        })) {
            this.match(depth + 1, new TokenKind[] {
                    TokenKind.TypenameKeyword,
                    TokenKind.ClassKeyword
            });

            if(this.check(TokenKind.DotDotDot)) {
                this.match(depth + 1, TokenKind.DotDotDot);
                if(this.check(TokenKind.Identifier)) {
                    this.match(depth + 1, TokenKind.Identifier);
                }
                return;
            }

            if(this.check(TokenKind.Identifier)) {
                this.match(depth + 1, TokenKind.Identifier);
            }
            this.match(depth + 1, TokenKind.Equals);
            parseTypeId(depth + 1);
            return;
        }

        this.match(depth + 1, TokenKind.TemplateKeyword);
        this.match(depth + 1, TokenKind.LessThan);
        parseTemplateParameterList(depth + 1);
        this.match(depth + 1, TokenKind.GreaterThan);
        this.match(depth + 1, TokenKind.ClassKeyword);

        if(this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
            if(this.check(TokenKind.Identifier)) {
                this.match(depth + 1, TokenKind.Identifier);
            }
            return;
        }

        if(this.check(TokenKind.Identifier)) {
            this.match(depth + 1, TokenKind.Identifier);
        }
        this.match(depth + 1, TokenKind.Equals);
        this.match(depth + 1, TokenKind.Equals);
        parseIdExpression(depth + 1);

    }

    protected void parseStaticAssertDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.STATIC_ASSERT_DECLARATION);

        this.match(depth + 1, TokenKind.StaticAssertKeyword);
        this.match(depth + 1, TokenKind.OpenParen);
        parseConstantExpression(depth + 1);
        this.match(depth + 1, TokenKind.Comma);
        this.match(depth + 1, TokenKind.StringLiteral);
        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseUsingDeclarationHelper(int depth) throws ParserException {
        if(this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        parseNestedNameSpecifier(depth + 1);
        parseUnqualifiedId(depth + 1);
        this.match(depth + 1, TokenKind.Semicolon);
    }

    protected void parseUsingDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.USING_DECLARATION);

        this.match(depth + 1, TokenKind.UsingKeyword);

        if(this.check(TokenKind.TypenameKeyword)) {
            this.match(depth + 1, TokenKind.TypenameKeyword);
            parseUsingDeclarationHelper(depth);
        }

        if(this.check(TokenKind.ColonColon)) {
            if(tryParse(() -> {
                this.match(depth + 1, TokenKind.ColonColon);
                parseUnqualifiedId(depth + 1);
                this.match(depth + 1, TokenKind.Semicolon);
            })) return;
        }

        parseUsingDeclarationHelper(depth);
    }

    protected void parseMemberDeclaratorList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.MEMBER_DECLARATOR_LIST);

        parseMemberDeclarator(depth + 1);

        if(this.check(TokenKind.Comma)) {
            this.match(depth + 1, TokenKind.Comma);
            parseMemberDeclaratorList(depth + 1);
        }
    }

    protected void parseMemberDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.MEMBER_DECLARATOR);

        if(this.check(TokenKind.Identifier)) {
            if(tryParse(() -> {
                this.match(depth + 1, TokenKind.Identifier);

                if(this.checkAttributeSpecifierSequence()) {
                    tryParse(() -> parseAttributeSpecifierSequence(depth + 1));
                }
                if(this.checkVirtSpecifierSequence()) {
                    tryParse(() -> parseVirtSpecifierSequence(depth + 1));
                }

                this.match(depth + 1, TokenKind.Colon);
                parseConstantExpression(depth + 1);
            })) return;
        }

        parseDeclarator(depth + 1);
        if(this.checkVirtSpecifierSequence()) {
            tryParse(() -> parseVirtSpecifierSequence(depth + 1));
        }

        if(this.checkBracedInitList()) {
            if(tryParse(() -> parseBracedInitList(depth + 1))) return;
        }
        if(this.checkPureSpecifier()) {
            tryParse(() -> parseBracedInitList(depth + 1));
        }

    }

    protected void parseVirtSpecifierSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.VIRT_SPECIFIER_SEQUENCE);

        parseVirtSpecifier(depth + 1);

        if(this.checkVirtSpecifierSequence()) {
            tryParse(() -> parseVirtSpecifierSequence(depth + 1));
        }
    }

    protected void parseVirtSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.VIRT_SPECIFIER);

        this.match(depth + 1, new TokenKind[] {
                TokenKind.NewKeyword,
                TokenKind.FinalKeyword,
                TokenKind.OverrideKeyword
        });
    }

    protected void parseEnumSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ENUM_SPECIFIER);

        parseEnumHead(depth + 1);
        this.match(depth + 1, TokenKind.OpenBrace);

        if (this.checkEnumeratorList()) {
            parseEnumeratorList(depth + 1);

            if (this.check(TokenKind.Comma)) {
                this.match(depth + 1, TokenKind.Comma);
            }
        }

        this.match(depth + 1, TokenKind.CloseBrace);
    }

    protected void parseEnumHeadAfterAttributeSpecifierSequence(int depth) throws ParserException {
        boolean success;

        if (this.checkNestedNameSpecifier()) {
            parseNestedNameSpecifier(depth + 1);
            this.match(depth + 1, TokenKind.Identifier);
            tryParse(() -> parseEnumBase(depth + 1));
            return;
        }

        success = tryParse(() -> {
            this.match(depth + 1, TokenKind.Identifier);
            tryParse(() -> parseEnumBase(depth + 1));
        });

        if (success) return;

        tryParse(() -> parseEnumBase(depth + 1));
    }

    protected void parseEnumBase(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ENUM_BASE);

        this.match(depth + 1, TokenKind.Colon);
        parseTypeSpecifierSequence(depth + 1);
    }

    protected void parseEnumHead(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ENUM_HEAD);

        parseEnumKey(depth + 1);

        boolean success = tryParse(() -> {
            parseAttributeSpecifierSequence(depth + 1);
            tryParse(() -> parseEnumHeadAfterAttributeSpecifierSequence(depth + 1));
        });

        if (success) return;

        tryParse(() -> parseEnumHeadAfterAttributeSpecifierSequence(depth + 1));
    }

    protected void parseEnumKey(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ENUM_KEY);

        this.match(depth + 1, TokenKind.EnumKeyword);

        if (this.check(new TokenKind[]{
                TokenKind.ClassKeyword,
                TokenKind.StructKeyword
        })) {
            this.match(depth + 1, new TokenKind[]{
                    TokenKind.ClassKeyword,
                    TokenKind.StructKeyword
            });
        }
    }

    protected void parseEnumeratorList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ENUMERATOR_LIST);

        parseEnumeratorDefinition(depth + 1);

        if (!this.check(TokenKind.Comma)) return;
        parseEnumeratorList(depth + 1);
    }

    protected void parseEnumeratorDefinition(int depth) throws ParserException {
        this.addTreeNode(depth, ENUMERATOR_DEFINITION);

        parseEnumerator(depth + 1);

        if (!this.check(TokenKind.Equals)) return;

        tryParse(() -> {
            this.match(depth + 1, TokenKind.Equals);
            parseConstantExpression(depth + 1);
        });
    }

    protected void parseEnumerator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ENUMERATOR);

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseTrailingTypeSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TRAILING_TYPE_SPECIFIER);

        if (this.checkSimpleTypeSpecifier() && tryParse(() -> parseSimpleTypeSpecifier(depth + 1))) return;
        if (this.checkElaboratedTypeSpecifier() && tryParse(() -> parseElaboratedTypeSpecifier(depth + 1))) return;
        if (this.checkTypenameSpecifier() && tryParse(() -> parseTypenameSpecifier(depth + 1))) return;

        parseCvQualifier(depth + 1);
    }

    protected void parseSimpleTypeSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.SIMPLE_TYPE_SPECIFIER);

        if (this.check(TokenKind.LongKeyword)) {
            this.match(depth + 1, TokenKind.LongKeyword);
            if (this.check(new TokenKind[]{
                    TokenKind.IntKeyword,
                    TokenKind.DoubleKeyword})) {
                this.match(depth + 1, new TokenKind[]{
                        TokenKind.IntKeyword,
                        TokenKind.DoubleKeyword
                });
                return;
            }
            return;
        } else if (this.check(TokenKind.IntKeyword)) {
            this.match(depth + 1, TokenKind.IntKeyword);
            if (this.check(new TokenKind[]{
                    TokenKind.LongKeyword,
                    TokenKind.ShortKeyword})) {
                this.match(depth + 1, new TokenKind[]{
                        TokenKind.LongKeyword,
                        TokenKind.ShortKeyword
                });
                return;
            }
            return;
        } else if (this.check(TokenKind.ShortKeyword)) {
            this.match(depth + 1, TokenKind.ShortKeyword);
            if (this.check(TokenKind.IntKeyword)) {
                this.match(depth + 1, TokenKind.IntKeyword);
            }
            return;
        } else if (this.check(new TokenKind[]{
                TokenKind.SignedKeyword,
                TokenKind.UnsignedKeyword
        })) {
            this.match(depth + 1, new TokenKind[]{
                    TokenKind.SignedKeyword,
                    TokenKind.UnsignedKeyword,
            });
            if (this.check(new TokenKind[]{
                    TokenKind.CharKeyword,
                    TokenKind.ShortKeyword,
                    TokenKind.IntKeyword,
                    TokenKind.LongKeyword
            })) {
                this.match(depth + 1, new TokenKind[]{
                        TokenKind.CharKeyword,
                        TokenKind.ShortKeyword,
                        TokenKind.IntKeyword,
                        TokenKind.LongKeyword
                });
            }
            return;
        } else if (this.check(new TokenKind[]{
                TokenKind.FloatKeyword,
                TokenKind.CharKeyword,
                TokenKind.DoubleKeyword,
                TokenKind.Char16TKeyword,
                TokenKind.Char32TKeyword,
                TokenKind.WcharTKeyword,
                TokenKind.BoolKeyword,
                TokenKind.VoidKeyword,
                TokenKind.AutoKeyword
        })) {
            this.match(depth + 1, new TokenKind[]{
                    TokenKind.Char16TKeyword,
                    TokenKind.Char32TKeyword,
                    TokenKind.WcharTKeyword,
                    TokenKind.BoolKeyword,
                    TokenKind.FloatKeyword,
                    TokenKind.CharKeyword,
                    TokenKind.DoubleKeyword,
                    TokenKind.VoidKeyword,
                    TokenKind.AutoKeyword
            });

            return;
        } else if (this.checkDeclTypeSpecifier()) {
            parseDecltypeSpecifier(depth + 1);
        } else if (this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        if (tryParse(() -> {
            parseNestedNameSpecifier(depth + 1);

            if (this.check(TokenKind.TemplateKeyword)) {
                this.match(depth + 1, TokenKind.TemplateKeyword);
                parseSimpleTemplateId(depth + 1);
                return;
            }

            parseTypeName(depth + 1);
        })) return;

        parseTypeName(depth + 1);
    }

    protected void parseTypeName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPE_NAME);

        if (this.checkClassName() && tryParse(() -> parseClassName(depth + 1))) return;
        if (this.checkEnumName() && tryParse(() -> parseEnumName(depth + 1))) return;
        if (this.checkSimpleTemplateId() && tryParse(() -> parseSimpleTemplateId(depth + 1))) return;

        parseTypedefName(depth + 1);
    }

    protected void parseClassName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CLASS_NAME);

        if (this.checkSimpleTemplateId()) {
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

        //TODO: FORCING THIS TO THROW ERRORS
        throw new ParserException("FORCED ERROR");

        //this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseElaboratedTypeSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ELABORATED_TYPE_SPECIFIER);

        ParseFunction tryNestedNameSpecifierIdentifier = () -> {
            if (this.check(TokenKind.ColonColon)) {
                this.match(depth + 1, TokenKind.ColonColon);
            }

            boolean success = tryParse(() -> {
                parseNestedNameSpecifier(depth + 1);
                this.match(depth + 1, TokenKind.Identifier);
            });

            if (success) return;

            this.match(depth + 1, TokenKind.Identifier);
        };

        //Enum
        if (this.check(TokenKind.EnumKeyword)) {
            this.match(depth + 1, TokenKind.EnumKeyword);
            tryNestedNameSpecifierIdentifier.execute();
        }

        //ClassKey
        parseClassKey(depth + 1);

        tryParse(() -> {
            parseAttributeSpecifierSequence(depth + 1);
            tryNestedNameSpecifierIdentifier.execute();
        });


        if (this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        if (this.checkNestedNameSpecifier()) {
            parseNestedNameSpecifier(depth + 1);
        }

        if (this.check(TokenKind.TemplateKeyword)) {
            this.match(depth + 1, TokenKind.TemplateKeyword);
        }

        parseSimpleTemplateId(depth + 1);
    }

    protected void parseClassKey(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CLASS_KEY);

        this.match(depth + 1, new TokenKind[]{
                TokenKind.ClassKeyword,
                TokenKind.StructKeyword,
                TokenKind.UnionKeyword
        });
    }

    protected void parseCvQualifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CV_QUALIFIER);

        this.match(depth + 1, new TokenKind[]{
                TokenKind.ConstKeyword,
                TokenKind.VolatileKeyword
        });
    }

    protected void parseTypenameSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPENAME_SPECIFIER);

        this.match(depth + 1, TokenKind.TypenameKeyword);
        if (this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        parseNestedNameSpecifier(depth + 1);

        if (this.check(TokenKind.Identifier)) {
            this.match(depth + 1, TokenKind.Identifier);
            return;
        }

        if (this.check(TokenKind.TemplateKeyword)) {
            this.match(depth + 1, TokenKind.TemplateKeyword);
        }

        parseSimpleTemplateId(depth + 1);
    }

    protected void parseSimpleTemplateId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.SIMPLE_TEMPLATE_ID);

        parseTemplateName(depth + 1);

        this.match(depth + 1, TokenKind.LessThan);

        if (this.checkTemplateArgumentList()) {
            tryParse(() -> parseTemplateArgumentList(depth + 1));
        }

        this.match(depth + 1, TokenKind.GreaterThan);
    }

    protected void parseTemplateName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TEMPLATE_NAME);

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseTemplateArgumentList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TEMPLATE_ARGUMENT_LIST);

        parseTemplateArgument(depth + 1);

        if (this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
        }

        if (!this.check(TokenKind.Comma)) return;

        parseTemplateArgumentList(depth + 1);

    }

    protected void parseTemplateArgument(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TEMPLATE_ARGUMENT);

        if (this.checkConstantExpression()) {
            if(tryParse(() -> parseConstantExpression(depth + 1))) return;
        } else if (this.checkIdExpression()) {
            if(tryParse(() -> parseIdExpression(depth + 1))) return;
        }

        parseTypeId(depth + 1);
    }

    protected void parseExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXPRESSION);

        parseAssignmentExpression(depth + 1);

        if (this.check(TokenKind.Comma)) {
            this.match(depth + 1, TokenKind.Comma);
            parseExpression(depth + 1);
        }
    }

    protected void parseAssignmentExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ASSIGNMENT_EXPRESSION);

        if (this.checkThrowExpression()) {
            parseThrowExpression(depth + 1);
            return;
        }

        if (this.checkLogicalOrExpression()) {
            boolean success = tryParse(() -> {
                parseLogicalOrExpression(depth + 1);
                parseAssignmentOperator(depth + 1);
                parseInitializerClause(depth + 1);
            });
            if (success) return;
        }

        parseConditionalExpression(depth + 1);
    }

    protected void parseAssignmentOperator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ASSIGNMENT_OPERATOR);

        this.match(depth + 1, new TokenKind[]{
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

        if (this.checkAssignmentExpression()) {
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

        if (!this.check(TokenKind.Question)) {
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

        if (!this.check(TokenKind.BarBar)) {
            return;
        }

        this.match(depth + 1, TokenKind.BarBar);
        parseLogicalOrExpression(depth + 1);
    }

    protected void parseLogicalAndExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.LOGICAL_AND_EXPRESSION);

        parseInclusiveOrExpression(depth + 1);

        if (!this.check(TokenKind.AmpersandAmpersand)) {
            return;
        }

        this.match(depth + 1, TokenKind.AmpersandAmpersand);
        parseLogicalAndExpression(depth + 1);
    }

    protected void parseInclusiveOrExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.INCLUSIVE_OR_EXPRESSION);

        parseExclusiveOrExpression(depth + 1);

        if (!this.check(TokenKind.Bar)) {
            return;
        }

        this.match(depth + 1, TokenKind.Bar);
        parseInclusiveOrExpression(depth + 1);
    }

    protected void parseExclusiveOrExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXCLUSIVE_OR_EXPRESSION);

        parseAndExpression(depth + 1);

        if (!this.check(TokenKind.Caret)) {
            return;
        }

        this.match(depth + 1, TokenKind.Caret);
        parseExclusiveOrExpression(depth + 1);
    }

    protected void parseAndExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.AND_EXPRESSION);

        parseEqualityExpression(depth + 1);

        if (!this.check(TokenKind.Ampersand)) {
            return;
        }

        this.match(depth + 1, TokenKind.Ampersand);
        parseAndExpression(depth + 1);
    }

    protected void parseEqualityExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EQUALITY_EXPRESSION);

        parseRelationalExpression(depth + 1);

        if (!this.check(new TokenKind[]{
                TokenKind.EqualsEquals,
                TokenKind.ExclamationEquals
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[]{
                TokenKind.EqualsEquals,
                TokenKind.ExclamationEquals
        });

        parseEqualityExpression(depth + 1);
    }

    protected void parseRelationalExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.RELATIONAL_EXPRESSION);

        parseShiftExpression(depth + 1);

        if (!this.check(new TokenKind[]{
                TokenKind.LessThan,
                TokenKind.GreaterThan,
                TokenKind.LessThanEquals,
                TokenKind.GreaterThanEquals
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[]{
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

        if (!this.check(new TokenKind[]{
                TokenKind.LessThanLessThan,
                TokenKind.GreaterThanGreaterThan,
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[]{
                TokenKind.LessThanLessThan,
                TokenKind.GreaterThanGreaterThan,
        });

        parseShiftExpression(depth + 1);
    }

    protected void parseAdditiveExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ADDITIVE_EXPRESSION);

        parseMultiplicativeExpression(depth + 1);

        if (!this.check(new TokenKind[]{
                TokenKind.Plus,
                TokenKind.Minus,
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[]{
                TokenKind.Plus,
                TokenKind.Minus,
        });

        parseAdditiveExpression(depth + 1);
    }

    protected void parseMultiplicativeExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.MULTIPLICATIVE_EXPRESSION);

        parsePmExpression(depth + 1);

        if (!this.check(new TokenKind[]{
                TokenKind.Asterisk,
                TokenKind.Slash,
                TokenKind.Percent
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[]{
                TokenKind.Asterisk,
                TokenKind.Slash,
                TokenKind.Percent
        });

        parseMultiplicativeExpression(depth + 1);
    }

    protected void parsePmExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PM_EXPRESSION);

        parseCastExpression(depth + 1);

        if (!this.check(new TokenKind[]{
                TokenKind.DotAsterisk,
                TokenKind.MinusGreaterThanAsterisk
        })) {
            return;
        }

        this.match(depth + 1, new TokenKind[]{
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

        parseUnaryExpression(depth + 1);
    }

    protected void parseUnaryExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.UNARY_EXPRESSION);

        //Alignof
        if (this.check(TokenKind.AlignofKeyword)) {
            this.match(depth + 1, TokenKind.AlignofKeyword);
            this.match(depth + 1, TokenKind.OpenParen);
            parseTypeId(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
            return;
        }

        boolean success;
        //Sizeof
        if (this.check(TokenKind.SizeofKeyword)) {
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
        if (this.checkNewExpression() && tryParse(() -> parseNewExpression(depth + 1))) return;
        //Delete
        if (this.checkDeleteExpression() && tryParse(() -> parseDeleteExpression(depth + 1))) return;
        //Noexcept
        if (this.checkNoexceptExpression() && tryParse(() -> parseNoexceptExpression(depth + 1))) return;


        // ++, --
        if (this.check(new TokenKind[]{
                TokenKind.PlusPlus,
                TokenKind.MinusMinus
        })) {
            this.match(depth + 1, new TokenKind[]{
                    TokenKind.PlusPlus,
                    TokenKind.MinusMinus
            });

            parseCastExpression(depth + 1);
            return;
        }

        // unary operator
        if (this.checkUnaryOperator()) {
            parseUnaryOperator(depth + 1);
            parseCastExpression(depth + 1);
            return;
        }

        // postfix
        parsePostfixExpression(depth + 1);
    }

    protected void parsePostfixExpressionHelper(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.POSTFIX_EXPRESSION);

        if (this.check(TokenKind.OpenBracket)) {
            this.match(depth + 1, TokenKind.OpenBracket);
            boolean success = tryParse(() -> {
                tryParse(() -> parseBracedInitList(depth + 1));
                this.match(depth + 1, TokenKind.CloseBracket);
            });

            if (!success) {
                parseExpression(depth + 1);
                this.match(depth + 1, TokenKind.CloseBracket);
            }
        } else if (this.check(TokenKind.OpenParen)) {
            this.match(depth + 1, TokenKind.OpenParen);
            tryParse(() -> parseExpressionList(depth + 1));
            this.match(depth + 1, TokenKind.CloseParen);
        } else if (this.check(TokenKind.Dot)) {
            this.match(depth + 1, TokenKind.Dot);

            if (this.check(TokenKind.TemplateKeyword)) {
                this.match(depth + 1, TokenKind.TemplateKeyword);
                parseIdExpression(depth + 1);
            } else {
                boolean success = tryParse(() -> parsePseudoDestructorName(depth + 1));
                if (!success) parseIdExpression(depth + 1);
            }
        } else if (this.check(TokenKind.Minus)) {
            this.match(depth + 1, TokenKind.Minus);
            this.match(depth + 1, TokenKind.GreaterThan);

            if (this.check(TokenKind.TemplateKeyword)) {
                this.match(depth + 1, TokenKind.TemplateKeyword);
                parseIdExpression(depth + 1);
            } else {
                boolean success = tryParse(() -> parsePseudoDestructorName(depth + 1));
                if (!success) parseIdExpression(depth + 1);
            }
        } else {
            this.match(depth + 1, new TokenKind[]{
                    TokenKind.MinusMinus,
                    TokenKind.PlusPlus
            });
        }

        tryParse(() -> parsePostfixExpressionHelper(depth + 1));
    }

    protected void parsePseudoDestructorNameOptionalNestedNameSpecifier(int depth) throws ParserException {
        if (this.checkNestedNameSpecifier()) {
            boolean success = tryParse(() -> parseNestedNameSpecifier(depth + 1));

            if (success && this.check(TokenKind.TemplateKeyword)) {
                success = tryParse(() -> {
                    this.match(depth + 1, TokenKind.TemplateKeyword);
                    parseSimpleTemplateId(depth + 1);
                    this.match(depth + 1, TokenKind.ColonColon);
                    this.match(depth + 1, TokenKind.Tilde);
                    parseTypeName(depth + 1);
                });
                if (success) return;
            }

            parsePseudoDestructorNameEnding(depth);
        }
    }

    protected void parsePseudoDestructorNameEnding(int depth) throws ParserException {
        if (this.check(TokenKind.Tilde)) {
            this.match(depth + 1, TokenKind.Tilde);
            parseTypeName(depth + 1);
        }

        parseTypeName(depth + 1);
        this.match(depth + 1, TokenKind.ColonColon);
        this.match(depth + 1, TokenKind.Tilde);
        parseTypeName(depth + 1);
    }

    protected void parsePseudoDestructorName(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PSEUDO_DESTRUCTOR_NAME);

        if (this.check(TokenKind.Tilde)) {
            this.match(depth + 1, TokenKind.Tilde);
            parseDecltypeSpecifier(depth + 1);
            return;
        }

        boolean success;

        if (this.check(TokenKind.ColonColon)) {
            success = tryParse(() -> {
                boolean trySuccess = tryParse(() -> parsePseudoDestructorNameOptionalNestedNameSpecifier(depth));

                if (trySuccess) return;

                parsePseudoDestructorNameEnding(depth);
            });

            if (success) return;
        }

        if (this.checkNestedNameSpecifier()) {
            success = tryParse(() -> {
                boolean trySuccess = tryParse(() -> parsePseudoDestructorNameOptionalNestedNameSpecifier(depth));

                if (trySuccess) return;

                parsePseudoDestructorNameEnding(depth);
            });

            if (success) return;
        }

        parsePseudoDestructorNameEnding(depth);
    }

    protected void parsePostfixExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.POSTFIX_EXPRESSION);

        if (this.check(TokenKind.DynamicCastKeyword)) {
            this.match(depth + 1, TokenKind.DynamicCastKeyword);
            this.match(depth + 1, TokenKind.LessThan);
            parseTypeId(depth + 1);
            this.match(depth + 1, TokenKind.GreaterThan);
            this.match(depth + 1, TokenKind.OpenParen);
            parseExpression(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
        } else if (this.check(TokenKind.StaticCastKeyword)) {
            this.match(depth + 1, TokenKind.StaticCastKeyword);
            this.match(depth + 1, TokenKind.LessThan);
            parseTypeId(depth + 1);
            this.match(depth + 1, TokenKind.GreaterThan);
            this.match(depth + 1, TokenKind.OpenParen);
            parseExpression(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
        } else if (this.check(TokenKind.ReinterpretCastKeyword)) {
            this.match(depth + 1, TokenKind.ReinterpretCastKeyword);
            this.match(depth + 1, TokenKind.LessThan);
            parseTypeId(depth + 1);
            this.match(depth + 1, TokenKind.GreaterThan);
            this.match(depth + 1, TokenKind.OpenParen);
            parseExpression(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
        } else if (this.check(TokenKind.ConstCastKeyword)) {
            this.match(depth + 1, TokenKind.ConstCastKeyword);
            this.match(depth + 1, TokenKind.LessThan);
            parseTypeId(depth + 1);
            this.match(depth + 1, TokenKind.GreaterThan);
            this.match(depth + 1, TokenKind.OpenParen);
            parseExpression(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
        } else if (this.check(TokenKind.TypeidKeyword)) {
            this.match(depth + 1, TokenKind.TypeidKeyword);
            this.match(depth + 1, TokenKind.OpenParen);

            boolean success = tryParse(() -> parseTypeId(depth + 1));

            if (!success) {
                parseExpression(depth + 1);
            }

            this.match(depth + 1, TokenKind.CloseParen);
            parseExpression(depth + 1);
        } else if (this.checkTypenameSpecifier()) {
            parseTypenameSpecifier(depth + 1);

            if (this.check(TokenKind.OpenParen)) {
                this.match(depth + 1, TokenKind.OpenParen);
                tryParse(() -> parseExpressionList(depth + 1));
                this.match(depth + 1, TokenKind.CloseParen);
            } else {
                parseBracedInitList(depth + 1);
            }
        } else {
            boolean success = tryParse(() -> {
                parseSimpleTypeSpecifier(depth + 1);
                if (this.check(TokenKind.OpenParen)) {
                    this.match(depth + 1, TokenKind.OpenParen);
                    tryParse(() -> parseExpressionList(depth + 1));
                    this.match(depth + 1, TokenKind.CloseParen);
                } else {
                    parseBracedInitList(depth + 1);
                }
            });

            if (!success) parsePrimaryExpression(depth + 1);
        }

        tryParse(() -> parsePostfixExpressionHelper(depth + 1));
    }

    protected void parsePrimaryExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PRIMARY_EXPRESSION);

        if (this.checkLiteral()) {
            this.match(depth + 1, new TokenKind[]{
                    TokenKind.IntegerLiteral,
                    TokenKind.CharacterLiteral,
                    TokenKind.FloatingLiteral,
                    TokenKind.StringLiteral,
                    TokenKind.BooleanLiteral,
                    TokenKind.PointerLiteral,
                    TokenKind.UserDefinedStringLiteral,
                    TokenKind.UserDefinedCharacterLiteral,
                    TokenKind.UserDefinedFloatingLiteral,
                    TokenKind.UserDefinedIntegerLiteral
            });
            return;
        }

        if (this.check(TokenKind.ThisKeyword)) {
            this.match(depth + 1, TokenKind.ThisKeyword);
            return;
        }

        if (this.check(TokenKind.OpenParen)) {
            this.match(depth + 1, TokenKind.OpenParen);
            parseExpression(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
            return;
        }

        if (this.checkIdExpression()) {
            parseIdExpression(depth + 1);
            return;
        }

        parseLambdaExpression(depth + 1);
    }

    protected void parseLambdaExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.LAMBDA_EXPRESSION);

        parseLambdaIntroducer(depth + 1);

        if (this.checkLambdaDeclarator()) {
            tryParse(() -> parseLambdaDeclarator(depth + 1));
        }

        parseCompoundStatement(depth + 1);
    }

    protected void parseLambdaDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.LAMBDA_DECLARATOR);

        this.match(depth + 1, TokenKind.OpenParen);
        parseParameterDeclarationClause(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);

        if (this.check(TokenKind.MutableKeyword)) {
            this.match(depth + 1, TokenKind.MutableKeyword);
        }

        if (this.checkExceptionSpecification()) {
            tryParse(() -> parseExceptionSpecification(depth + 1));
        }

        if (this.checkAttributeSpecifierSequence()) {
            tryParse(() -> parseAttributeSpecifierSequence(depth + 1));
        }

        if (this.checkTrailingReturnType()) {
            tryParse(() -> parseTrailingReturnType(depth + 1));
        }
    }

    protected void parseParameterDeclarationClause(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PARAMETER_DECLARATION_CLAUSE);

        if (this.checkParameterDeclarationList()) {
            parseParameterDeclarationList(depth + 1);

            if (this.check(TokenKind.Comma)) {
                this.match(depth + 1, TokenKind.Comma);
                this.match(depth + 1, TokenKind.DotDotDot);
                return;
            }
        }

        if (this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
        }

    }

    protected void parseParameterDeclarationList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PARAMETER_DECLARATION_LIST);

        parseParameterDeclaration(depth + 1);

        if (!this.check(TokenKind.Comma)) return;

        this.match(depth + 1, TokenKind.Comma);

        parseParameterDeclarationList(depth + 1);
    }

    protected void parseParameterDeclaration(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PARAMETER_DECLARATION);

        if (this.checkAttributeSpecifierSequence()) {
            parseAttributeSpecifierSequence(depth + 1);
        }

        parseDeclSpecifierSequence(depth + 1);

        boolean success = tryParse(() -> {
            parseDeclarator(depth + 1);
            if (!this.check(TokenKind.Equals)) {
                return;
            }

            tryParse(() -> {
                this.match(depth + 1, TokenKind.Equals);
                parseInitializerClause(depth + 1);
            });
        });

        if (success) return;

        tryParse(() -> {
            parseAbstractDeclarator(depth + 1);

            if (!this.check(TokenKind.Equals)) {
                return;
            }

            tryParse(() -> {
                this.match(depth + 1, TokenKind.Equals);
                parseInitializerClause(depth + 1);
            });
        });
    }

    protected void parseDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.DECLARATOR);


        if(this.checkNoptrDeclarator()) {
            if(tryParse(() -> {
                parseNoptrDeclarator(depth + 1);
                parseParametersAndQualifiers(depth + 1);

                //TODO: SHOULD THIS BE OPTIONAL? Verify BNF is correct
                if (this.checkTrailingReturnType()) {
                    parseTrailingReturnType(depth + 1);
                }
            })) {
                return;
            }
        }

        parsePtrDeclarator(depth + 1);
    }

    protected void parsePtrDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PTR_DECLARATOR);

        if(this.checkPtrOperator()) {
            boolean success = tryParse(() -> {
                parsePtrOperator(depth + 1);
            });
            if(success) {
                parsePtrDeclarator(depth + 1);
                return;
            }
        }

        parseNoptrDeclarator(depth + 1);
    }

    protected void parseNoptrDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NOPTR_DECLARATOR);

        //If this case fails it could possibly be next case
        if(this.checkParametersAndQualifiers()) {
            if(tryParse(() -> {
                parseParametersAndQualifiers(depth + 1);
                parseNoptrDeclarator(depth + 1);
            })) return;
        }

        if(this.check(TokenKind.OpenParen)) {
            this.match(depth + 1, TokenKind.OpenParen);
            parsePtrDeclarator(depth + 1);
            this.match(depth + 1, TokenKind.CloseParen);
            return;
        }

        if(this.checkDeclaratorId()) {
            parseDeclaratorId(depth + 1);
            if(this.checkAttributeSpecifierSequence()) {
                tryParse(() -> parseAttributeSpecifierSequence(depth + 1));
            }
            return;
        }

        this.match(depth + 1, TokenKind.OpenBracket);
        if(this.checkConstantExpression()) {
            parseConstantExpression(depth + 1);
        }
        this.match(depth + 1, TokenKind.CloseBracket);
        if(this.checkAttributeSpecifierSequence()) {
            parseAttributeSpecifierSequence(depth + 1);
        }
        if(this.checkNoptrDeclarator()) {
            tryParse(() -> parseNoptrDeclarator(depth + 1));
        }
    }

    protected void parseDeclaratorId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.DECLARATOR_ID);

        if(this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
        }

        if(this.checkIdExpression() && tryParse(() -> parseIdExpression(depth + 1))) return;

        if(this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        if(this.checkNestedNameSpecifier()) {
            parseNestedNameSpecifier(depth + 1);
        }

        parseClassName(depth + 1);
    }

    protected void parseParametersAndQualifiers(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PARAMETERS_AND_QUALIFIERS);


        this.match(depth + 1, TokenKind.OpenParen);
        parseParameterDeclarationClause(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);

        if (this.checkAttributeSpecifierSequence()) {
            tryParse(() -> parseAttributeSpecifierSequence(depth + 1));
        }

        if (this.checkCvQualifierSequence()) {
            tryParse(() -> parseCvQualifierSequence(depth + 1));
        }

        if (this.checkRefQualifier()) {
            tryParse(() -> parseRefQualifier(depth + 1));
        }

        if (this.checkExceptionSpecification()) {
            tryParse(() -> parseExceptionSpecification(depth + 1));
        }
    }

    protected void parseCvQualifierSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CV_QUALIFIER);

        parseCvQualifier(depth + 1);

        if (!this.checkCvQualifier()) return;

        parseCvQualifierSequence(depth + 1);
    }

    protected void parseRefQualifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.REF_QUALIFIER);

        this.match(depth + 1, new TokenKind[]{
                TokenKind.Ampersand,
                TokenKind.AmpersandAmpersand
        });
    }

    protected void parseDeclSpecifierSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.DECL_SPECIFIER_SEQUENCE);

        if(this.checkStorageClassSpecifier()) {
            parseStorageClassSpecifier(depth + 1);
            if(this.checkFunctionSpecifier()) {
                parseFunctionSpecifier(depth + 1);
            }
            this.parseTypeSpecifier(depth + 1);
            return;
        } else if (this.check(TokenKind.FriendKeyword)) {
           this.match(depth + 1, TokenKind.FriendKeyword);
            if(this.checkFunctionSpecifier()) {
                parseFunctionSpecifier(depth + 1);
            }
            this.parseTypeSpecifier(depth + 1);
            return;
        } else if (this.checkFunctionSpecifier()) {
            parseFunctionSpecifier(depth + 1);
            this.parseTypeSpecifier(depth + 1);
            return;
        } else if (this.check(TokenKind.ConstKeyword)) {
            parseTypeSpecifier(depth + 1);
            if(this.checkTypeSpecifier()) {
                parseTypeSpecifier(depth + 1);
            }
            return;
        } else if (this.checkTypeSpecifier()) {
            parseTypeSpecifier(depth + 1);
            return;
        }

        if (this.check(TokenKind.TypedefKeyword)) {
            parseTypeSpecifier(depth + 1);
            this.match(depth + 1, TokenKind.Identifier);
        }
    }

    protected void parseStorageClassSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.STORAGE_CLASS_SPECIFIER);

        this.match(depth + 1, new TokenKind[]{
                TokenKind.RegisterKeyword,
                TokenKind.StaticKeyword,
                TokenKind.ThreadLocalKeyword,
                TokenKind.ExternKeyword,
                TokenKind.MutableKeyword
        });
    }

    protected void parseFunctionSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.FUNCTION_SPECIFIER);

        this.match(depth + 1, new TokenKind[]{
                TokenKind.InlineKeyword,
                TokenKind.VirtualKeyword,
                TokenKind.ExplicitKeyword
        });
    }

    protected void parseExceptionSpecification(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXCEPTION_SPECIFICATION);

        if (this.checkNoexceptionSpecification()) {
            parseNoexceptSpecification(depth + 1);
        }

        parseDynamicExceptionSpecification(depth + 1);
    }

    protected void parseDynamicExceptionSpecification(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.DYNAMIC_EXCEPTION_SPECIFICATION);

        this.match(depth + 1, TokenKind.ThrowKeyword);
        this.match(depth + 1, TokenKind.OpenParen);
        parseTypeIdList(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);
    }

    protected void parseTypeIdList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TYPE_ID_LIST);

        parseTypeId(depth + 1);

        if (this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
        }

        if (!this.check(TokenKind.Comma)) return;

        this.match(depth + 1, TokenKind.Comma);

        parseTypeIdList(depth + 1);
    }

    protected void parseNoexceptSpecification(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NOEXCEPT_SPECIFICATION);

        this.match(depth + 1, TokenKind.NoexceptKeyword);

        if (!this.check(TokenKind.OpenParen)) return;

        this.match(depth + 1, TokenKind.OpenParen);
        parseConstantExpression(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);
    }

    protected void parseTrailingReturnType(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TRAILING_RETURN_TYPE);

        this.match(depth + 1, TokenKind.Minus);
        this.match(depth + 1, TokenKind.GreaterThan);

        parseTrailingTypeSpecifierSequence(depth + 1);

        if (this.checkAbstractDeclarator()) {
            tryParse(() -> parseAbstractDeclarator(depth + 1));
        }
    }

    protected void parseTrailingTypeSpecifierSequence(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TRAILING_TYPE_SPECIFIER_SEQUENCE);

        parseTypeSpecifier(depth + 1);

        if (this.checkAttributeSpecifierSequence()) {
            parseAttributeSpecifierSequence(depth + 1);
        }

        if (this.checkTrailingTypeSpecifierSequence()) {
            tryParse(() -> parseTrailingTypeSpecifierSequence(depth + 1));
        }
    }

    protected void parseLambdaIntroducer(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.LAMBDA_INTRODUCER);

        this.match(depth + 1, TokenKind.OpenBracket);

        if (this.checkLambdaCapture()) {
            tryParse(() -> parseLambdaCapture(depth + 1));
        }

        this.match(depth + 1, TokenKind.CloseBracket);
    }

    protected void parseLambdaCapture(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.LAMBDA_CAPTURE);

        boolean success = tryParse(() -> parseCaptureList(depth + 1));
        if (success) return;

        parseCaptureDefault(depth + 1);

        if (!this.check(TokenKind.Comma)) return;
        this.match(depth + 1, TokenKind.Comma);
        parseCaptureList(depth + 1);
    }

    protected void parseCaptureList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CAPTURE_LIST);

        parseCapture(depth + 1);

        if (this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
        }

        if (!this.check(TokenKind.Comma)) {
            return;
        }

        this.match(depth + 1, TokenKind.Comma);
        parseCaptureList(depth + 1);
    }

    protected void parseCapture(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CAPTURE);

        if (this.check(TokenKind.Ampersand)) {
            this.match(depth + 1, TokenKind.Ampersand);
            this.match(depth + 1, TokenKind.Identifier);
            return;
        }

        this.match(depth + 1, new TokenKind[]{
                TokenKind.Identifier,
                TokenKind.ThisKeyword
        });
    }

    protected void parseCaptureDefault(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CAPTURE_DEFAULT);

        this.match(depth + 1, new TokenKind[]{
                TokenKind.Ampersand,
                TokenKind.Equals
        });
    }

    protected void parseIdExpression(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.ID_EXPRESSION);

        if (this.checkQualifiedId() && tryParse(() -> parseQualifiedId(depth + 1))) return;
        parseUnqualifiedId(depth + 1);
    }

    protected void parseUnqualifiedId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.UNQUALIFIED_ID);

        if (this.check(TokenKind.OperatorKeyword)) {
            boolean success = tryParse(() -> parseOperatorFunctionId(depth + 1));
            if (!success) success = tryParse(() -> parseLiteralOperatorId(depth + 1));
            if (!success) success = tryParse(() -> parseConversionFunctionId(depth + 1));
            if (!success) parseTemplateId(depth + 1);
            return;
        }

        if (this.check(TokenKind.Tilde)) {
            this.match(depth + 1, TokenKind.Tilde);
            boolean success = tryParse(() -> parseClassName(depth + 1));
            if (!success) parseDecltypeSpecifier(depth + 1);
            return;
        }

        boolean success = tryParse(() -> parseTemplateId(depth + 1));

        if (success) return;

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseDecltypeSpecifier(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.DECLTYPE_SPECIFIER);

        this.match(depth + 1, TokenKind.DecltypeKeyword);
        this.match(depth + 1, TokenKind.OpenParen);
        parseExpression(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);
    }

    protected void parseConversionFunctionId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CONVERSION_FUNCTION_ID);

        this.match(depth + 1, TokenKind.OperatorKeyword);
        parseConversionTypeId(depth + 1);
    }

    protected void parseConversionTypeId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CONVERSION_TYPE_ID);

        parseTypeSpecifierSequence(depth + 1);

        if (this.checkConversionDeclarator()) {
            tryParse(() -> parseConversionDeclarator(depth + 1));
        }
    }

    protected void parseConversionDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.CONVERSION_DECLARATOR);

        parsePtrOperator(depth + 1);

        if (this.checkConversionDeclarator()) {
            tryParse(() -> parseConversionDeclarator(depth + 1));
        }
    }

    protected void parseTemplateId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.TEMPLATE_ID);

        if (this.check(TokenKind.OperatorKeyword)) {
            if (this.check(TokenKind.StringLiteral, 1) || this.check(TokenKind.UserDefinedStringLiteral, 1)) {
                //NOTE: THIS ASSUMES A STRING IS JUST DOUBLE QUOTES. COULD FAIL!!!!!!
                parseLiteralOperatorId(depth + 1);
            } else {
                parseOperatorFunctionId(depth + 1);
            }
            this.match(depth + 1, TokenKind.LessThan);
            tryParse(() -> parseTemplateArgumentList(depth + 1));
            this.match(depth + 1, TokenKind.GreaterThan);
            return;
        }

        parseSimpleTemplateId(depth + 1);
    }

    protected void parseLiteralOperatorId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.LITERAL_OPERATOR_ID);

        //NOTE: THIS ASSUMES A STRING IS JUST DOUBLE QUOTES. COULD FAIL!!!!!!
        this.match(depth + 1, TokenKind.OperatorKeyword);
        this.match(depth + 1, new TokenKind[]{
                TokenKind.StringLiteral,
                TokenKind.UserDefinedStringLiteral
        });
        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseOperatorFunctionId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.OPERATOR_FUNCTION_ID);

        this.match(depth + 1, TokenKind.OperatorKeyword);
        parseOverloadableOperator(depth + 1);

        if (this.check(TokenKind.LessThan)) return;

        tryParse(() -> {
            this.match(depth + 1, TokenKind.LessThan);
            tryParse(() -> parseTemplateArgumentList(depth + 1));
            this.match(depth + 1, TokenKind.GreaterThan);
        });
    }

    protected void parseOverloadableOperator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.OVERLOADABLE_OPERATOR);

        if (this.check(new TokenKind[]{
                TokenKind.NewKeyword,
                TokenKind.DeleteKeyword
        })) {
            this.match(depth + 1, new TokenKind[]{
                    TokenKind.NewKeyword,
                    TokenKind.DeleteKeyword
            });

            tryParse(() -> {
                this.match(depth + 1, TokenKind.OpenBracket);
                this.match(depth + 1, TokenKind.CloseBracket);
            });
            return;
        }

        if (this.check(TokenKind.OpenParen)) {
            this.match(depth + 1, TokenKind.OpenParen);
            this.match(depth + 1, TokenKind.CloseParen);
            return;
        }

        if (this.check(TokenKind.OpenBracket)) {
            this.match(depth + 1, TokenKind.OpenBracket);
            this.match(depth + 1, TokenKind.CloseBracket);
            return;
        }

        this.match(depth + 1, new TokenKind[]{
                TokenKind.Plus,
                TokenKind.Minus,
                TokenKind.Asterisk,
                TokenKind.Slash,
                TokenKind.Percent,
                TokenKind.Caret,
                TokenKind.Ampersand,
                TokenKind.Bar,
                TokenKind.Tilde,
                TokenKind.Exclamation,
                TokenKind.Equals,
                TokenKind.LessThan,
                TokenKind.GreaterThan,
                TokenKind.PlusEquals,
                TokenKind.MinusEquals,
                TokenKind.AsteriskEquals,
                TokenKind.SlashEquals,
                TokenKind.PercentEquals,
                TokenKind.CaretEquals,
                TokenKind.BarEquals,
                TokenKind.LessThanLessThan,
                TokenKind.GreaterThanGreaterThan,
                TokenKind.LessThanLessThanEquals,
                TokenKind.GreaterThanGreaterThanEquals,
                TokenKind.EqualsEquals,
                TokenKind.ExclamationEquals,
                TokenKind.LessThanEquals,
                TokenKind.GreaterThanEquals,
                TokenKind.AmpersandAmpersand,
                TokenKind.BarBar,
                TokenKind.PlusPlus,
                TokenKind.MinusMinus,
                TokenKind.Comma,
                TokenKind.MinusGreaterThanAsterisk,
                TokenKind.MinusGreaterThan
        });
    }

    protected void parseQualifiedId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.QUALIFIED_ID);

        if(!this.check(TokenKind.ColonColon)) {
            parseNestedNameSpecifier(depth + 1);
            if (this.check(TokenKind.TemplateKeyword)) {
                this.match(depth + 1, TokenKind.TemplateKeyword);
            }

            parseUnqualifiedId(depth + 1);
            return;
        }

        this.match(depth + 1, TokenKind.ColonColon);

        if(tryParse(() -> {
            parseNestedNameSpecifier(depth + 1);
            if (this.check(TokenKind.TemplateKeyword)) {
                this.match(depth + 1, TokenKind.TemplateKeyword);
            }

            parseUnqualifiedId(depth + 1);

        })) return;

        if(this.checkOperatorFunctionId() && tryParse(() -> parseOperatorFunctionId(depth + 1))) return;
        if(this.checkLiteralOperatorId() && tryParse(() -> parseLiteralOperatorId(depth + 1))) return;
        if(this.checkLiteralOperatorId() && tryParse(() -> parseLiteralOperatorId(depth + 1))) return;
        if(this.checkTemplateId() && tryParse(() -> parseTemplateId(depth + 1))) return;

        this.match(depth + 1, TokenKind.Identifier);
    }

    protected void parseUnaryOperator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.UNARY_OPERATOR);

        this.match(depth + 1, new TokenKind[]{
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

        if (this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        this.match(depth + 1, TokenKind.DeleteKeyword);

        if (this.check(TokenKind.OpenBracket)) {

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

        if (this.check(TokenKind.ColonColon)) {
            this.match(depth + 1, TokenKind.ColonColon);
        }

        this.match(depth + 1, TokenKind.NewKeyword);

        ParseFunction parseAfterNewPlacement = () -> {
            if (this.check(TokenKind.OpenParen)) {
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

    protected void parseNewInitializer(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NEW_INITIALIZER);

        if (this.checkBracedInitList()) {
            parseBracedInitList(depth + 1);
        }

        this.match(depth + 1, TokenKind.OpenParen);
        if (this.checkExpressionList()) {
            tryParse(() -> parseExpressionList(depth + 1));
        }
        this.match(depth + 1, TokenKind.CloseParen);

    }

    protected void parseNewPlacement(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.PARSE_NEW_PLACEMENT);

        this.match(depth + 1, TokenKind.OpenParen);
        parseExpressionList(depth + 1);
        this.match(depth + 1, TokenKind.CloseParen);


    }

    protected void parseExpressionList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.EXPRESSION_LIST);

        parseInitializerList(depth + 1);
    }

    protected void parseInitializerList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.INITIALIZER_LIST);

        parseInitializerClause(depth + 1);
        if (this.check(TokenKind.DotDotDot)) {
            this.match(depth + 1, TokenKind.DotDotDot);
        }

        if (this.check(TokenKind.Comma)) {
            this.match(depth + 1, TokenKind.Comma);
            parseInitializerList(depth + 1);
        }
    }

    protected void parseInitializerClause(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.INITIALIZER_CLAUSE);

        if (this.checkBracedInitList()) {
            parseBracedInitList(depth + 1);
        }

        parseAssignmentExpression(depth + 1);
    }

    protected void parseBracedInitList(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.BRACED_INIT_LIST);

        this.match(depth + 1, TokenKind.OpenBrace);

        if (this.checkInitializerList()) {
            parseInitializerList(depth + 1);
            if (this.check(TokenKind.Comma)) {
                this.match(depth + 1, TokenKind.Comma);
            }
        }

        this.match(depth + 1, TokenKind.CloseBrace);
    }

    protected void parseNewTypeId(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NEW_TYPE_ID);

        parseTypeSpecifierSequence(depth + 1);

        if (!this.checkNewDeclarator()) {
            return;
        }

        tryParse(() -> parseNewDeclarator(depth + 1));
    }

    protected void parseNewDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NEW_DECLARATOR);

        if (this.checkPtrOperator()) {
            parsePtrOperator(depth + 1);
            tryParse(() -> parseNewDeclarator(depth + 1));
            return;
        }

        parseNoPtrNewDeclarator(depth + 1);
    }

    protected void parseNoPtrNewDeclarator(int depth) throws ParserException {
        this.addTreeNode(depth, NodeType.NO_PTR_NEW_DECLARATOR);


        this.match(depth + 1, TokenKind.OpenBracket);

        tryParse(() -> {
            parseExpression(depth + 1);
            this.match(depth + 1, TokenKind.CloseBracket);

            tryParse(() -> {
                parseAttributeSpecifierSequence(depth + 1);
                tryParse(() -> parseNoPtrNewDeclarator(depth + 1));
            });

            tryParse(() -> parseNoPtrNewDeclarator(depth + 1));
        });

        parseConstantExpression(depth + 1);
        this.match(depth + 1, TokenKind.CloseBracket);

        tryParse(() -> {
            parseAttributeSpecifierSequence(depth + 1);
            tryParse(() -> parseNoPtrNewDeclarator(depth + 1));
        });

        tryParse(() -> parseNoPtrNewDeclarator(depth + 1));
    }

    /*
        Check functions
     */

    protected boolean checkInitializerList() {
        return this.checkInitializerClause();
    }

    protected boolean checkInitializerClause() {
        return this.checkAssignmentExpression() ||
                this.checkBracedInitList();
    }

    protected boolean checkNewDeclarator() {
        return this.checkPtrOperator() ||
                this.checkNoPtrNewDeclarator();
    }

    protected boolean checkNoPtrNewDeclarator() {
        return this.check(TokenKind.OpenBracket);
    }

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
        return this.checkNestedNameSpecifier() ||
                this.checkTypeName() ||
                this.checkDeclTypeSpecifier() ||
                this.check(new TokenKind[]{
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
                });
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

    protected boolean checkInclusiveOrExpression() {
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
                this.check(new TokenKind[]{
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
        return this.check(new TokenKind[]{
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
                this.check(new TokenKind[]{
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
                this.check(new TokenKind[]{
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
                this.check(new TokenKind[]{
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
        return this.check(new TokenKind[] {
                TokenKind.ColonColon,
                TokenKind.Identifier
        });
    }

    protected boolean checkLiteral() {
        return this.checkUserDefinedLiteral() ||
                this.check(new TokenKind[]{
                        TokenKind.IntegerLiteral,
                        TokenKind.CharacterLiteral,
                        TokenKind.FloatingLiteral,
                        TokenKind.StringLiteral,
                        TokenKind.BooleanLiteral,
                        TokenKind.PointerLiteral
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
        return this.checkEmptyDeclaration() ||
                this.checkFunctionDefinition() ||
                this.checkBlockDeclaration() ||
                this.checkTemplateDeclaration() ||
                this.checkExplicitInstantiation() ||
                this.checkExplicitSpecialization() ||
                this.checkLinkageSpecification() ||
                this.checkNamespaceDefinition() ||
                this.checkAttributeDeclaration();
    }

    protected boolean checkEmptyDeclaration() {
        return this.check(TokenKind.Semicolon);
    }

    protected boolean checkFunctionDefinition() {
        return this.checkAttributeSpecifierSequence() ||
                this.checkDeclSpecifierSequence() ||
                this.checkDeclarator();
    }

    protected boolean checkDeclSpecifierSequence() {
        return this.checkDeclSpecifier();
    }

    protected boolean checkDeclSpecifier() {
        return this.checkStorageClassSpecifier() ||
                this.checkTypeSpecifier() ||
                this.checkFunctionSpecifier() ||
                this.check(new TokenKind[]{
                        TokenKind.FriendKeyword,
                        TokenKind.TypedefKeyword,
                        TokenKind.ConstexprKeyword
                });
    }

    protected boolean checkStorageClassSpecifier() {
        return this.check(new TokenKind[]{
                TokenKind.AutoKeyword,
                TokenKind.RegisterKeyword,
                TokenKind.StaticKeyword,
                TokenKind.ThreadLocalKeyword,
                TokenKind.ExternKeyword,
                TokenKind.MutableKeyword
        });
    }

    protected boolean checkFunctionSpecifier() {
        return this.check(new TokenKind[]{
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
        return this.check(new TokenKind[]{
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
                this.check(new TokenKind[]{
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
                this.checkSimpleTemplateId() ||
                this.check(new TokenKind[] {
                        TokenKind.Identifier,
                        TokenKind.TemplateKeyword,
                });
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

    protected boolean checkExpressionList() {
        return this.checkInitializerList();
    }

    protected boolean checkConversionDeclarator() {
        return this.checkPtrOperator();
    }

    protected boolean checkLambdaDeclarator() {
        return this.check(TokenKind.OpenParen);
    }

    protected boolean checkLambdaCapture() {
        return this.checkCaptureDefault() ||
                this.checkCaptureList();
    }

    protected boolean checkCaptureList() {
        return this.checkCapture();
    }

    protected boolean checkCapture() {
        return this.check(new TokenKind[]{
                TokenKind.Ampersand,
                TokenKind.Identifier,
                TokenKind.ThisKeyword
        });
    }

    protected boolean checkCaptureDefault() {
        return this.check(new TokenKind[]{
                TokenKind.Ampersand,
                TokenKind.Equals
        });
    }

    protected boolean checkExceptionSpecification() {
        return this.checkDynamicExceptionSpecification() ||
                this.checkNoexceptionSpecification();
    }

    protected boolean checkNoexceptionSpecification() {
        return this.check(TokenKind.NoexceptKeyword);
    }

    protected boolean checkDynamicExceptionSpecification() {
        return this.check(TokenKind.ThrowKeyword);
    }

    protected boolean checkTrailingReturnType() {
        return this.check(TokenKind.MinusGreaterThan);
    }

    protected boolean checkTrailingTypeSpecifierSequence() {
        return this.checkTrailingTypeSpecifier();
    }

    protected boolean checkCvQualifierSequence() {
        return this.checkCvQualifier();
    }

    protected boolean checkRefQualifier() {
        return this.check(new TokenKind[]{
                TokenKind.Ampersand,
                TokenKind.AmpersandAmpersand
        });
    }

    protected boolean checkParameterDeclarationList() {
        return this.checkParameterDeclaration();
    }

    protected boolean checkParameterDeclaration() {
        return this.checkAttributeSpecifierSequence() ||
                this.checkDeclSpecifierSequence();
    }

    protected boolean checkEnumeratorList() {
        return this.checkEnumeratorDefinition();
    }

    protected boolean checkEnumeratorDefinition() {
        return this.checkEnumerator();
    }

    protected boolean checkEnumerator() {
        return this.check(TokenKind.Identifier);
    }

    protected boolean checkAccessSpecifier() {
        return this.check(new TokenKind[]{
                TokenKind.PrivateKeyword,
                TokenKind.ProtectedKeyword,
                TokenKind.PublicKeyword
        });
    }

    protected boolean checkClassHeadName() {
        return this.checkClassName() ||
                this.checkNestedNameSpecifier();
    }

    protected boolean checkBaseTypeSpecifier() {
        return this.checkClassOrDecltype();
    }

    protected boolean checkClassOrDecltype() {
        return this.check(TokenKind.ColonColon) ||
                this.checkDeclTypeSpecifier() ||
                this.checkNestedNameSpecifier() ||
                this.checkClassName();
    }

    protected boolean checkMemberDeclaratorList() {
        return this.checkMemberDeclarator();
    }

    protected boolean checkMemberDeclarator() {
        return this.checkDeclarator() ||
                this.check(new TokenKind[] {
                        TokenKind.Identifier,
                        TokenKind.Colon
                }) ||
                this.checkVirtSpecifierSequence() ||
                this.braceOrEqualInitializer() ||
                this.checkPureSpecifier();
    }

    protected boolean checkPureSpecifier() {
        //NOTE: Doesn't actually check for 0
        return this.check(TokenKind.Equals) && this.check(TokenKind.IntegerLiteral, 1);
    }

    protected boolean braceOrEqualInitializer() {
        return this.check(TokenKind.Equals) ||
                this.checkBracedInitList();
    }

    protected boolean checkVirtSpecifierSequence() {
        return this.checkVirtSpecifier();
    }

    protected boolean checkVirtSpecifier() {
        return this.check(new TokenKind[] {
                TokenKind.FinalKeyword,
                TokenKind.ExplicitKeyword
        });
    }

    protected boolean checkDeclarator() {
        return this.checkPtrOperator() ||
                this.checkNoptrDeclarator();
    }

    protected boolean checkNoptrDeclarator() {
        return this.checkDeclaratorId() ||
                this.check(TokenKind.OpenParen);
    }

    protected boolean checkDeclaratorId() {
        return this.check(new TokenKind[] {
                TokenKind.DotDotDot,
                TokenKind.ColonColon
            }) ||
                this.checkIdExpression() ||
                this.checkNestedNameSpecifier() ||
                this.checkClassName();
    }

    protected boolean checkUsingDeclaration() {
        return this.check(TokenKind.UsingKeyword);
    }

    protected boolean checkTemplateDeclaration() {
        return this.check(TokenKind.TemplateKeyword);
    }

    protected boolean checkStaticAssertDeclaration() {
        return this.check(TokenKind.StaticAssertKeyword);
    }

    protected boolean checkFunctionTryBlock() {
        return this.check(TokenKind.TryKeyword);
    }
    protected boolean checkHandlerSequence() {
        return this.checkHandler();
    }

    protected boolean checkHandler() {
        return this.check(TokenKind.CatchKeyword);
    }

    protected boolean checkCtorInitializer() {
        return this.check(TokenKind.Colon);
    }

    protected boolean checkStatementSequence() {
        return this.checkStatement();
    }

    protected boolean checkCompoundStatement() {
        return this.check(TokenKind.OpenBrace);
    }

    protected boolean checkStatement() {
        return this.checkLabeledStatement() ||
                this.checkExpressionStatement() ||
                this.checkDeclarationStatement() ||
                this.checkSelectionStatement() ||
                this.checkIterationStatement() ||
                this.checkJumpStatement() ||
                this.declarationStatement() ||
                this.checkTryBlock() ||
                this.checkAttributeSpecifierSequence();
    }

    protected boolean checkDeclarationStatement() {
        return this.checkBlockDeclaration();
    }
    protected boolean checkLabeledStatement() {
        return this.checkAttributeSpecifierSequence() ||
                this.check(new TokenKind[] {
                TokenKind.Identifier,
                TokenKind.CaseKeyword,
                TokenKind.DefaultKeyword
        });
    }

    protected boolean checkExpressionStatement() {
        return this.checkExpression() ||
                this.check(TokenKind.Semicolon);
    }

    protected boolean checkSelectionStatement() {
        return this.check(new TokenKind[] {
                TokenKind.IfKeyword,
                TokenKind.SwitchKeyword
        });
    }

    protected boolean checkIterationStatement() {
        return this.check(new TokenKind[] {
                TokenKind.WhileKeyword,
                TokenKind.DoKeyword,
                TokenKind.ForKeyword
        });
    }

    protected boolean checkJumpStatement() {
        return this.check(new TokenKind[] {
                TokenKind.ReturnKeyword,
                TokenKind.BreakKeyword,
                TokenKind.GotoKeyword,
                TokenKind.ContinueKeyword
        });
    }

    protected boolean declarationStatement() {
        return this.checkBlockDeclaration();
    }

    protected boolean checkBlockDeclaration() {
        return this.checkSimpleDeclaration() ||
                this.checkAsmDefinition() ||
                this.checkNamespaceAliasDefinition() ||
                this.checkUsingDeclaration() ||
                this.checkUsingDirective() ||
                this.checkStaticAssertDeclaration() ||
                this.checkAliasDeclaration() ||
                this.checkOpaqueEnumDeclaration();
    }

    protected boolean checkSimpleDeclaration() {
        return this.checkAttributeSpecifierSequence() ||
                this.checkDeclSpecifierSequence() ||
                this.checkInitDeclaratorList() ||
                this.check(TokenKind.Semicolon);
    }

    protected boolean checkInitDeclaratorList() {
        return this.checkInitDeclarator();
    }

    protected boolean checkInitDeclarator() {
        return this.checkDeclarator();
    }

    protected boolean checkAsmDefinition() {
        return this.check(TokenKind.AsmKeyword);
    }

    protected boolean checkNamespaceAliasDefinition() {
        return this.check(TokenKind.NamespaceKeyword);
    }

    protected boolean checkUsingDirective() {
        return this.checkAttributeSpecifierSequence() ||
            this.check(TokenKind.UsingKeyword);
    }

    protected boolean checkAliasDeclaration() {
        return this.check(TokenKind.UsingKeyword);
    }

    protected boolean checkOpaqueEnumDeclaration() {
        return this.checkEnumKey();
    }

    protected boolean checkTryBlock() {
        return this.check(TokenKind.TryKeyword);
    }

    protected boolean checkCondition() {
        return this.checkAttributeSpecifierSequence() ||
                this.checkExpression() ||
                this.checkDeclSpecifierSequence();
    }

    protected boolean checkEnumBase() {
        return this.check(TokenKind.Colon);
    }

    protected boolean checkInitializer() {
        return this.checkBraceOrEqualInitializer() ||
                this.check(TokenKind.OpenParen);
    }

    protected boolean checkBraceOrEqualInitializer() {
        return this.checkBracedInitList() ||
                this.check(TokenKind.Equals);
    }

    protected boolean checkExplicitInstantiation() {
        return this.check(new TokenKind[] {
                TokenKind.ExternKeyword,
                TokenKind.TemplateKeyword,
        });
    }

    protected boolean checkExplicitSpecialization() {
        return this.check(TokenKind.TemplateKeyword);
    }

    protected boolean checkLinkageSpecification() {
        return this.check(TokenKind.ExternKeyword);
    }

    protected boolean checkNamespaceDefinition() {
        return this.checkNamedNamespaceDefinition() ||
                this.checkUnnamedNamespaceDefinition();
    }

    protected boolean checkUnnamedNamespaceDefinition() {
        return this.check(TokenKind.InlineKeyword) ||
                this.check(TokenKind.NamespaceKeyword);
    }

    protected boolean checkNamedNamespaceDefinition() {
        return this.checkOriginalNamespaceDefinition() ||
                this.checkExtensionNamespaceDefinition();
    }

    protected boolean checkOriginalNamespaceDefinition() {
        return this.check(TokenKind.InlineKeyword) ||
                this.check(TokenKind.NamespaceKeyword);
    }

    protected boolean checkExtensionNamespaceDefinition() {
        return this.check(TokenKind.InlineKeyword) ||
                this.check(TokenKind.NamespaceKeyword);
    }

    protected boolean checkAttributeDeclaration() {
        return this.checkAttributeSpecifierSequence();
    }

}
