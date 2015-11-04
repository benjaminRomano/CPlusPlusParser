package org.bromano.cplusplusparser.parser.nodes.declarations;

import org.bromano.cplusplusparser.parser.nodes.BaseParseNode;
import org.bromano.cplusplusparser.parser.nodes.NodeType;

public class BlockDeclaration implements IDeclaration {
    public NodeType type;
    protected IBlockDeclaration blockDeclaration;

    public BlockDeclaration(IBlockDeclaration blockDeclaration) {
        this.type = NodeType.BLOCK_DECLARATION;
        this.blockDeclaration = blockDeclaration;
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

        if(this.blockDeclaration != null) {
            this.blockDeclaration.print(depth + 1);
        }
    }
}