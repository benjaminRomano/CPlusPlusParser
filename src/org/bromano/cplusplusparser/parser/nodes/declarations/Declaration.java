package org.bromano.cplusplusparser.parser.nodes.declarations;

import org.bromano.cplusplusparser.parser.nodes.BaseParseNode;
import org.bromano.cplusplusparser.parser.nodes.NodeType;

public class Declaration extends BaseParseNode {
    IDeclaration declaration;

    public Declaration(IDeclaration declaration) {
        this.type = NodeType.DECLARATION;
        this.declaration = declaration;
    }

    public void print(int depth) {
        super.print(depth);

        if(this.declaration != null) {
            this.declaration.print(depth + 1);
        }
    }
}