package org.bromano.cplusplusparser.parser.nodes;

public interface ParseNode {
    NodeType type = null;
    void print(int depth);
    void print();
}
