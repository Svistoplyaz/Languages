package semester8.Lab2;

import semester7.Lab3.Lexeme;
import semester7.Lab5.DataType;

public class NodeArray extends Node {
    private NodeType nType;
    private long[] array;

    public NodeArray(Node p, NodeType t, Lexeme l) {
        super(p, t.type, l);
        nType = t;
        array = new long[nType.getComboLength()];
    }
}
