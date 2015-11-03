package org.bromano.cplusplusparser.parser.nodes;

public abstract class BaseParseNode implements ParseNode {
    public NodeType type;

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
