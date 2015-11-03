package org.bromano.cplusplusparser.parser.nodes.declarations;


import org.bromano.cplusplusparser.parser.nodes.BaseParseNode;
import org.bromano.cplusplusparser.parser.nodes.NodeType;

public class DeclarationSequence extends BaseParseNode {
    public DeclarationSequence declarationSequence;
    public IDeclaration declaration;

    public DeclarationSequence() {
        this.type = NodeType.DECLARATION_SEQUENCE;
    }

    public void print(int depth) {
        super.print(depth);

        if(this.declarationSequence != null) {
            this.declarationSequence.print(depth + 1);
        }

        if(this.declaration != null) {
            this.declaration.print();
        }
    }
}

