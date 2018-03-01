package semester8.Lab2;

import semester7.Lab3.Lexeme;
import semester7.Lab5.DataType;

public class NodeId extends Node {
    boolean isConst;
    long value = 0;

    public NodeId(Node p, DataType t, Lexeme l, boolean _isConst) {
        super(p, t, l);
        isConst = _isConst;
    }

    public NodeId(Node p, DataType t, Lexeme l, boolean _isConst, long _value) {
        super(p, t, l);
        isConst = _isConst;
        value = _value;
    }
}
