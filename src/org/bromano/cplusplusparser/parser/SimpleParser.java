package org.bromano.cplusplusparser.parser;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Template;
import org.bromano.cplusplusparser.parser.nodes.*;
import org.bromano.cplusplusparser.parser.nodes.attributes.*;
import org.bromano.cplusplusparser.parser.nodes.declarations.*;
import org.bromano.cplusplusparser.parser.nodes.declarations.declarators.CvQualifier;
import org.bromano.cplusplusparser.parser.nodes.declarations.declarators.Declarator;
import org.bromano.cplusplusparser.parser.nodes.declarations.functions.FunctionDefinition;
import org.bromano.cplusplusparser.parser.nodes.declarations.names.TypeId;
import org.bromano.cplusplusparser.parser.nodes.declarations.types.TrailingTypeSpecifier;
import org.bromano.cplusplusparser.parser.nodes.declarations.types.TypeSpecifier;
import org.bromano.cplusplusparser.parser.nodes.declarations.types.TypeSpecifierSequence;
import org.bromano.cplusplusparser.parser.nodes.primaries.NestedNameSpecifier;
import org.bromano.cplusplusparser.parser.nodes.templates.names.SimpleTemplateId;
import org.bromano.cplusplusparser.parser.nodes.templates.names.TemplateArgumentList;
import org.bromano.cplusplusparser.parser.nodes.templates.names.TemplateName;
import org.bromano.cplusplusparser.scanner.Token;
import org.bromano.cplusplusparser.scanner.TokenKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SimpleParser implements Parser {
    protected List<Token> tokens;
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

    protected void savePos() {
        this.savedPos.push(this.pos);
    }

    protected void resetPos() {
        this.pos = this.savedPos.pop();
    }

    protected boolean check(TokenKind kind) {
        if(this.pos >= this.end || this.tokens.get(this.pos).kind != kind) {
           return false;
        }

        return true;
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
        if(this.pos + lookahead >= this.end || this.tokens.get(this.pos + lookahead).kind != kind) {
            return false;
        }

        return true;
    }

    protected Token match() throws ParserException {
        if(this.pos >= this.end) {
            throw new ParserException("Expected: token");
        }

        Token token = this.tokens.get(this.pos);

        this.pos++;
        return token;

    }

    protected Token match(TokenKind tokenKind) throws ParserException {
        if(this.pos >= this.end || this.tokens.get(this.pos).kind != tokenKind) {
            throw new ParserException("Expected: " + tokenKind.name());
        }

        Token token = this.tokens.get(this.pos);
        this.pos++;
        return token;
    }
    protected Token match(TokenKind[] tokenKinds) throws ParserException {
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
                return token;
            }
        }

        throw new ParserException(errorMessageBuilder.toString());
    }

    public ParseNode parse() throws ParserException {
        return parseTranslationUnit();
    }

    protected boolean checkTranslationUnit() {
        return this.checkDeclarationSequence();
    }

    protected TranslationUnit parseTranslationUnit() throws ParserException {
        return new TranslationUnit(parseDeclarationSequence());
    }

    protected boolean checkDeclarationSequence() {
        return this.checkDeclaration();
    }

    protected DeclarationSequence parseDeclarationSequence() throws ParserException {
        this.savePos();
        try {
            return new DeclarationSequence(parseDeclaration(), parseDeclarationSequence());
        } catch (ParserException exception) {
            this.resetPos();
        }

        return new DeclarationSequence(parseDeclaration());
    }

    protected boolean checkDeclaration() {
        return this.checkEmptyDeclaration();
    }

    protected Declaration parseDeclaration() throws ParserException {
        return new Declaration(parseEmptyDeclaration());
    }

    protected boolean checkEmptyDeclaration() {
        return this.check(TokenKind.Semicolon);
    }

    protected EmptyDeclaration parseEmptyDeclaration() throws ParserException {
        this.match(TokenKind.Semicolon);
        return new EmptyDeclaration();
    }

    protected boolean checkFunctionDefinition() {
        return this.checkAttributeSpecifierSequence();
    }

    protected FunctionDefinition parseFunctionDefinition() throws ParserException {
        AttributeSpecifierSequence attributeSpecifierSequence = null;
        DeclarationSpecifierSequence declarationSpecifierSequence = null;
        Declarator declarator = null;

        if(this.checkAttributeSpecifierSequence()) {
            this.savePos();

            try {
                attributeSpecifierSequence = parseAttributeSpecifierSequence();
            } catch (ParserException exception) {
                this.resetPos();
            }
        }

        if(this.checkDeclarationSpecifierSequence()) {
            this.savePos();

            try {
                declarationSpecifierSequence = parseDeclarationSpecifierSequence();
            } catch(ParserException exception) {
                this.resetPos();
            }
        }

        if(this.check(TokenKind.Equals)) {
            this.match(TokenKind.Equals);

            if(this.check(TokenKind.DefaultKeyword)) {
                this.match(TokenKind.DefaultKeyword);
            } else {
                this.match(TokenKind.DeleteKeyword);
            }

            this.match(TokenKind.Semicolon);

            return new FunctionDefinition(attributeSpecifierSequence, declarationSpecifierSequence, declarator);
        }

        return new FunctionDefinition(attributeSpecifierSequence, declarationSpecifierSequence, declarator, parseFunctionBody());
    }

    protected boolean checkAttributeSpecifierSequence() {
        return this.checkAttributeSpecifier();
    }

    protected AttributeSpecifierSequence parseAttributeSpecifierSequence() {
        this.savePos();
        try {
            return new AttributeSpecifierSequence(parseAttributeSpecifier(), parseAttributeSpecifierSequence());
        } catch (ParserException exception) {
            this.resetPos();
        }

        return new AttributeSpecifierSequence(parseAttributeSpecifier());
    }

    protected boolean checkAttributeSpecifier() {
        return this.check(TokenKind.OpenBracket) ||
                this.checkAlignmentSpecifier();
    }

    protected AttributeSpecifier parseAttributeSpecifier() throws ParserException {
        if(this.check(TokenKind.OpenBracket)) {
            this.match(TokenKind.OpenBracket);
            this.match(TokenKind.OpenBracket);
            AttributeList attributeList = parseAttributeList();
            this.match(TokenKind.CloseBracket);
            this.match(TokenKind.CloseBracket);
            return new AttributeSpecifier(attributeList);
        }

        return new AttributeSpecifier(parseAlignmentSpecifier());
    }

    protected boolean checkAttributeList() {
        return this.checkAttribute();
    }

    protected AttributeList parseAttributeList() throws ParserException {
        if(this.checkAttribute()) {
            Attribute attribute = parseAttribute();

            if(this.check(TokenKind.DotDotDot)) {
                this.match(TokenKind.DotDotDot);

                if(this.check(TokenKind.Comma)) {
                    this.match(TokenKind.Comma);
                    return new AttributeList(attribute, parseAttributeList());
                }

                return new AttributeList(attribute);

            } else if(this.check(TokenKind.Comma)) {
                this.match(TokenKind.Comma);
                return new AttributeList(attribute, parseAttributeList());
            }

            return new AttributeList(attribute);
        }

        if(this.check(TokenKind.Comma)) {
            this.match(TokenKind.Comma);
            return parseAttributeList();
        }

        return new AttributeList();
    }

    protected boolean checkAttribute() {
        return this.checkAttribute();
    }

    protected Attribute parseAttribute() {
        AttributeToken attributeToken = parseAttributeToken();

        if(this.checkAttributeArgumentClause()) {
            AttributeArgumentClause attributeArgumentClause = parseAttributeArgumentClause();
            return new Attribute(attributeToken, attributeArgumentClause);
        }

        return new Attribute(attributeToken);
    }

    protected boolean checkAttributeToken() {
        return this.check(TokenKind.Identifier) ||
                this.checkAttributeScopedToken();
    }

    protected AttributeToken parseAttributeToken() throws ParserException {
        if(this.check(TokenKind.ColonColon, 1)) {
            return new AttributeToken(parseAttributeScopedToken());
        }

        this.match(TokenKind.Identifier);
        return new AttributeToken();
    }

    protected boolean checkAttributeScopedToken() {
        return this.checkAttributeNamespace();
    }

    protected AttributeScopedToken parseAttributeScopedToken() throws ParserException {
        AttributeNamespace attributeNamespace = parseAttributeNamespace();
        this.match(TokenKind.ColonColon);
        this.match(TokenKind.Identifier);
        return new AttributeScopedToken(attributeNamespace);
    }

    protected boolean checkAttributeNamespace() {
        return this.check(TokenKind.Identifier);
    }

    protected AttributeNamespace parseAttributeNamespace() throws ParserException {
        this.match(TokenKind.Identifier);
        return new AttributeNamespace();
    }

    protected boolean checkAttributeArgumentClause() {
        return this.check(TokenKind.OpenParen);
    }

    protected AttributeArgumentClause parseAttributeArgumentClause() throws ParserException {
        this.match(TokenKind.OpenParen);
        BalancedTokenSequence balancedTokenSequence = parseBalancedTokenSequence();
        this.match(TokenKind.CloseParen);
        return new AttributeArgumentClause(balancedTokenSequence);
    }

    protected boolean checkBalancedTokenSequence() {
        return this.checkBalancedToken();
    }

    protected BalancedTokenSequence parseBalancedTokenSequence() {
        this.savePos();
        try {
            return new BalancedTokenSequence(parseBalancedToken(), parseBalancedTokenSequence());
        } catch (ParserException exception) {
            this.resetPos();
        }

        return new BalancedTokenSequence(parseBalancedToken());
    }

    protected boolean checkBalancedToken() {
        return true;
    }

    protected BalancedToken parseBalancedToken() throws ParserException {
        if(this.check(new TokenKind[]{
                TokenKind.OpenBracket,
                TokenKind.OpenBrace,
                TokenKind.OpenParen
        })) {
            Token token = this.match(new TokenKind[]{
                    TokenKind.OpenBracket,
                    TokenKind.OpenBrace,
                    TokenKind.OpenParen
            });

            BalancedTokenSequence balancedTokenSequence = parseBalancedTokenSequence();

            Token closingToken = null;
            if (token.kind == TokenKind.OpenBracket) {
                closingToken = this.match(TokenKind.CloseBracket);
            } else if (token.kind == TokenKind.OpenBrace) {
                closingToken = this.match(TokenKind.OpenBrace);
            } else {
                closingToken = this.match(TokenKind.OpenParen);
            }

            return new BalancedToken(token, balancedTokenSequence, closingToken);
        }

        return new BalancedToken(this.match());
    }

    protected boolean checkAlignmentSpecifier() {
        return this.check(TokenKind.AlignasKeyword);
    }

    protected AlignmentSpecifier parseAlignmentSpecifier() throws ParserException {
        this.match(TokenKind.AlignasKeyword);
        this.match(TokenKind.OpenParen);

        boolean hasDotDotDot = false;
        if(this.check(TokenKind.DotDotDot)) {
            hasDotDotDot = true;
            this.match(TokenKind.DotDotDot);
        }

        if(this.checkTypeId()) {
            TypeId typeId = parseTypeId();
            this.match(TokenKind.CloseParen);
            return new AlignmentSpecifier(typeId, hasDotDotDot);
        }

        AlignmentExpression alignmentExpression = parseAlignmentExpression();
        this.match(TokenKind.CloseParen);
        return new AlignmentSpecifier(alignmentExpression, hasDotDotDot);
    }

    protected boolean checkTypeId() {
        return this.checkTypeSpecifierSequence();
    }

    protected TypeId parseTypeId() {
        TypeSpecifierSequence typeSpecifierSequence = parseTypeSpecifierSequence();

        if(this.checkAbstractDeclarator()) {
            return new TypeId(typeSpecifierSequence, parseAbstractorDeclarator());
        }

        return new TypeId(typeSpecifierSequence);
    }

    protected boolean checkTypeSpecifierSequence() {
        return this.checkTypeSpecifier();
    }

    protected TypeSpecifierSequence parseTypeSpecifierSequence() {
        this.savePos();
        try {
            return new TypeSpecifierSequence(parseTypeSpecifier(), parseTypeSpecifierSequence());
        } catch (ParserException exception) {
            this.resetPos();
        }

        TypeSpecifier typeSpecifier = parseTypeSpecifier();

        if(this.checkAttributeSpecifierSequence()) {
            return new TypeSpecifierSequence(typeSpecifier, parseAttributeSpecifierSequence());

        }

        return new TypeSpecifierSequence(typeSpecifier);
    }

    protected boolean checkTypeSpecifier() {
        return this.checkTrailingTypeSpecifier() ||
                this.checkClassSpecifier() ||
                this.checkEnumSpecifier();
    }

    protected TypeSpecifier parseTypeSpecifier() {

        if(this.checkTrailingTypeSpecifier()) {
            if(this.checkClassSpecifier()) {
                this.savePos();

                try {
                    return new TypeSpecifier(parseClassSpecifier());
                } catch(ParserException exception) {
                    this.resetPos();
                }
            } else if(this.checkEnumSpecifier()) {
                this.savePos();

                try {
                    return new TypeSpecifier(parseClassSpecifier());
                } catch(ParserException exception) {
                    this.resetPos();
                }
            }

            return new TypeSpecifier(parseTrailingTypeSpecifier());
        } else if(this.checkClassSpecifier()) {
            return new TypeSpecifier(parseClassSpecifier());
        }
        return new TypeSpecifier(parseEnumSpecifier());
    }

    protected boolean checkTrailingTypeSpecifier() {
        return this.checkSimpleTypeSpecifier() ||
                this.checkElaboratedTypeSpecifier() ||
                this.checkTypenameSpecifier() ||
                this.checkCvQualifier();
    }

    protected TrailingTypeSpecifier parseTrailingTypeSpecifier() {
        if(this.checkSimpleTypeSpecifier()) {
            return TrailingTypeSpecifier(parseSimpleTypeSpecifier());
        } else if(this.checkElaboratedTypeSpecifier()) {
            return TrailingTypeSpecifier(parseElaboratedTypeSpecifier());
        } else if(this.checkTypenameSpecifier()) {
            return TrailingTypeSpecifier(parseTypeNameSpecifier());
        }

        return TrailingTypeSpecifier(parseCvQualifier());
    }

    protected boolean checkCvQualifier() {
       return this.check(new TokenKind[]{
               TokenKind.ConstKeyword,
               TokenKind.VolatileKeyword
       });
    }

    protected CvQualifier parseCvQualifier() throws ParserException {
        return new CvQualifier(this.match(new TokenKind[] {
                TokenKind.ConstKeyword,
                TokenKind.VolatileKeyword
        }));
    }

    protected boolean checkTypenameSpecifier() {
        return this.check(TokenKind.TypenameKeyword);
    }

    protected TypenameSpecifier parseTypeNameSpecifier() throws ParserException {
        this.match(TokenKind.TypenameKeyword);
        boolean hascolonColon = false;
        if(this.check(TokenKind.ColonColon)) {
            this.match(TokenKind.ColonColon);
            hascolonColon = true;
        }

        NestedNameSpecifier nestedNameSpecifier = parseNestedNameSpecifier();

        if(this.check(TokenKind.Identifier)) {
            return new TypenameSpecifier(hascolonColon, nestedNameSpecifier, this.match(TokenKind.Identifier);
        }

        boolean hasTemplate = false;
        if(this.check(TokenKind.TemplateKeyword)) {
            this.match(TokenKind.TemplateKeyword);
            hasTemplate = true;
        }

        return new TypenameSpecifier(hascolonColon, nestedNameSpecifier, hasTemplate, parseSimpleTemplateId());
    }

    protected SimpleTemplateId parseSimpleTemplateId() throws ParserException {
        TemplateName templateName = parseTemplateName();

        this.match(TokenKind.LessThan);

        if(this.checkTemplateArgumentList()) {
            TemplateArgumentList templateArgumentList = parseTemplateArgumentList();
            this.match(TokenKind.GreaterThan);
            return new SimpleTemplateId(templateName, templateArgumentList);
        }

        this.match(TokenKind.GreaterThan);
        return new SimpleTemplateId(templateName);
    }

    protected TemplateName parseTemplateName() {
        return new TemplateName(this.match(TokenKind.Identifier));
    }

    protected boolean checkTemplateArgumentList() {
        return this.checkConstantExpression() ||
                this.checkTypeId() ||
                this.checkIdExpression();
    }

    protected TemplateArgumentList parseTemplateArgumentList() {

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
        return this.checkClassSpecifier();
    }

    protected boolean checkEnumSpecifier() {
        return this.checkEnumSpecifier();
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

}
