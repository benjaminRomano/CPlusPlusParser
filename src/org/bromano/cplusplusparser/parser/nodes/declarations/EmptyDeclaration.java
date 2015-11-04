package org.bromano.cplusplusparser.parser.nodes.declarations;

import org.bromano.cplusplusparser.parser.nodes.BaseParseNode;
import org.bromano.cplusplusparser.parser.nodes.NodeType;

public class EmptyDeclaration implements IDeclaration {
    public NodeType type;
    public EmptyDeclaration() {
        this.type = NodeType.EMPTY_DECLARATION;
    }

    public void print() {
        this.print(0);
    }

    public void print(int depth) {
        StringBuilder spaces = new StringBuilder();

        for(int i = 0; i < depth; i++) {
            spaces.append('\t');
        }

        System.out.println(spaces.toString() + type.name());
    }
}