package org.bromano.cplusplusparser.parser.nodes;

import org.bromano.cplusplusparser.parser.nodes.declarations.DeclarationSequence;

public class TranslationUnit extends BaseParseNode {
    public DeclarationSequence declarationSequence;

    public TranslationUnit() {
        this.type = NodeType.TRANSLATION_UNIT;
    }

    public TranslationUnit(DeclarationSequence declarationSequence) {
        this.declarationSequence = declarationSequence;
        this.type = NodeType.TRANSLATION_UNIT;
    }

    public void print(int depth) {
        super.print(depth);

        if(this.declarationSequence != null) {
            this.declarationSequence.print(depth + 1);
        }
    }
}
